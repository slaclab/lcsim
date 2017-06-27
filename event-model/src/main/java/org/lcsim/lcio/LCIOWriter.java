package org.lcsim.lcio;

import hep.io.sio.SIOOutputStream;
import hep.io.sio.SIOWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.lcsim.event.EventHeader;
import org.lcsim.event.EventHeader.LCMetaData;

/**
 * A class for writing out LCIO files.
 * The LCIO writer maintains a list of collections to ignore, and also a list of collections
 * to write out. If the list of collections to write out is not empty then all collections 
 * are written out except those in the ignore list. If the list of collections to write out
 * is not empty, then only the specified collections are written out (and the ignore list is
 * ignored).
 * <p>
 * Collections for which the <code>LCIOConstants.BITTransient</code> bit is set in the meta-data 
 * are never written out.
 * @author Tony Johnson
 */
public class LCIOWriter
{
	private final static Logger log = Logger.getLogger(LCIOWriter.class.getName());
	private SIOWriter writer;
	private final HandlerManager manager = HandlerManager.instance();
	private int lastRunNumber = -1;
	private String lastDetectorName = "";
	private Set<String> ignore = new HashSet<String>();
	private Set<String> only = new HashSet<String>();
	private File file;

	/**
	 * Create a writer for writing LCIO files
	 * @param file The file to write
	 */
	public LCIOWriter(File file) throws IOException
	{
		this.file = file;
		reOpen();
	}
	
	/**
	 * Create a writer for writing LCIO files
	 * @param file The name of the file to write
	 */
	public LCIOWriter(String file) throws IOException
	{
		this(new File(file));
	}
	
	/**
	 * Create a writer for writing LCIO files
	 * @param file The file to write
	 * @param ignoreCollections The collections that should not be written out.
	 */
	public LCIOWriter(File file, Collection<String> ignoreCollections) throws IOException
	{
		this(file);
		this.ignore = new HashSet(ignoreCollections);
	}
	
	/**
	 * Create a writer for writing LCIO files
	 * @param file The name of the file to write
	 * @param ignoreCollections The collections that should not be written out.
	 */
	public LCIOWriter(String file, Collection<String> ignoreCollections) throws IOException
	{
		this(file);
		this.ignore = new HashSet(ignoreCollections);
	}
	
	/**
	 * Add an entry to the list of collections to ignore (not write out)
	 * @param collection The collection to tadd
	 */
	public void addIgnore(String collection)
	{
		ignore.add(collection);
	}

	public void addAllIgnore(Collection<String> collections)
	{
		ignore.addAll(collections);
	}
	
	/**
	 * Remove an entry to the list of collections to ignore (not write out)
	 * @param collection The collection to remove
	 */
	public void removeIgnore(String collection)
	{
		ignore.remove(collection);
	}
	/** Clear the list of ignored collections.
	 */
	public void clearIgnore()
	{
		ignore.clear();
	}
	/**
	 * Add an entry to the list of collections to write out
	 * @param collection The collection to tadd
	 */
	public void addWriteOnly(String collection)
	{
		only.add(collection);
	}
	
	public void addAllWriteOnly(Collection<String> collections)
	{
		only.addAll(collections);
	}
	
	/**
	 * Remove an entry to the list of collections to write out
	 * @param collection The collection to remove
	 */
	public void removeWriteOnly(String collection)
	{
		only.remove(collection);
	}
	
	/** Clear the list of collections to be written out
	 */
	public void clearWriteOnly()
	{
		only.clear();
	}
	
	public void close() throws IOException
	{
		writer.close();
		writer = null;
	}
	
	public void flush() throws IOException
	{
		writer.flush();
	}
	
	public void reOpen() throws IOException
	{
		if (writer == null)
		{
			writer = new SIOWriter(new FileOutputStream(file));
		}
		
		// Reset lastRunNumber to force creation of a new RunHeader.  --JM
		lastRunNumber = -1;
	}
	
	private void writeData(EventHeader event, boolean headerOnly) throws IOException
	{
		if (headerOnly)
		{
			SIOOutputStream out = writer.createBlock(LCIOConstants.eventHeaderBlockName, LCIOConstants.MAJORVERSION, LCIOConstants.MINORVERSION);
			out.writeInt(event.getRunNumber());
			out.writeInt(event.getEventNumber());
			out.writeLong(event.getTimeStamp());
			out.writeString(event.getDetectorName());

			Map<String,String> blocks = new HashMap<String,String>();
			List<List<Object>> collections = event.get(Object.class);
			for (List<Object> collection : collections)
			{
				LCMetaData md = event.getMetaData(collection);
				if (LCIOUtil.bitTest(md.getFlags(),LCIOConstants.BITTransient)) continue;
				else if (!only.isEmpty() && !only.contains(md.getName())) continue;
				else if (ignore.contains(md.getName())) continue;
				else
				{
					Class type = md.getType();
					LCIOBlockHandler bh = manager.handlerForClass(type,md.getFlags());
					if (bh == null) {
						//log.warning("No handler found for block "+md.getName()+" of class "+type.getName()); // This is too chatty so I have disabled it. -- Mat.
					}
					else
					{
						if (!isValidCollectionName(md.getName())) throw new IOException("Collection name "+md.getName()+" is invalid for LCIO");
						blocks.put(md.getName(),bh.getType());
					}
				}
			}

			out.writeInt(blocks.size());
			for (Map.Entry<String,String> entry : blocks.entrySet() )
			{
				out.writeString(entry.getKey());
				out.writeString(entry.getValue());
			}
			Map<String,int[]> intMap = event.getIntegerParameters();
			Map<String,float[]> floatMap = event.getFloatParameters();
			Map<String,String[]> stringMap = event.getStringParameters();
			SIOLCParameters.write(intMap,floatMap,stringMap,out);
			out.close();
		}
		else
		{
			List<List<Object>> collections = event.get(Object.class);
			for (List<Object> collection : collections)
			{
				LCMetaData md = event.getMetaData(collection);

				if (LCIOUtil.bitTest(md.getFlags(),LCIOConstants.BITTransient)) continue;
				else if (!only.isEmpty() && !only.contains(md.getName())) continue;
				else if (ignore.contains(md.getName())) continue;
				else
				{
					LCIOBlockHandler bh = manager.handlerForClass(md.getType(),md.getFlags());
					if (bh != null) bh.writeBlock(writer,collection,md);
				}
			}
		}
	}
	
	private void writeData(LCIORunHeader header) throws IOException
	{
		SIOOutputStream out = writer.createBlock(LCIOConstants.runBlockName, LCIOConstants.MAJORVERSION, LCIOConstants.MINORVERSION);
		out.writeInt(header.getRunNumber());
		out.writeString(header.getDetectorName());
		out.writeString(header.getDescription());
		String[] active = header.getActiveSubdetectors();
		out.writeInt(active.length);
		for (int i=0; i<active.length; i++) out.writeString(active[i]);
		Map<String,int[]> intMap = header.getIntegerParameters();
		Map<String,float[]> floatMap = header.getFloatParameters();
		Map<String,String[]> stringMap = header.getStringParameters();
		SIOLCParameters.write(intMap,floatMap,stringMap,out);
		out.close();
	}
	
	public void write(EventHeader event) throws IOException
	{
		if (event.getRunNumber() != lastRunNumber || !lastDetectorName.equals(event.getDetectorName()))
		{
			lastRunNumber = event.getRunNumber();
			lastDetectorName = event.getDetectorName();
			if (lastDetectorName == null) lastDetectorName = "";
			write(new DefaultRunHeader(lastRunNumber,lastDetectorName,new SIOLCParameters()));
		}
		writer.createRecord(LCIOConstants.eventHeaderRecordName,true);
		writeData(event,true);
		writer.createRecord(LCIOConstants.eventRecordName,true);
		writeData(event,false);
	}
	
	public void write(LCIORunHeader header) throws IOException
	{
		writer.createRecord(LCIOConstants.runRecordName,true);
		writeData(header);
	}
	
	private static class DefaultRunHeader implements LCIORunHeader
	{
		private final int run;
		private final String name;
		private final static String[] noDetectors = new String[0];
		private SIOLCParameters runParameters;
		DefaultRunHeader(int run, String name, SIOLCParameters runParameters)
		{
			this.run = run;
			this.name = name;
			this.runParameters = runParameters;
		}

		public String[] getActiveSubdetectors()
		{
			return noDetectors;
		}

		public String getDescription()
		{
			return "";
		}

		public String getDetectorName()
		{
			return name;
		}

		public int getRunNumber()
		{
			return run;
		}

		public Map<String, float[]> getFloatParameters() 
		{
			return runParameters.getFloatMap();
		}

		public Map<String, int[]> getIntegerParameters() 
		{
			return runParameters.getIntMap();
		}

		public Map<String, String[]> getStringParameters() 
		{
			return runParameters.getStringMap();
		} 
	}
	
	private boolean isValidCollectionName(String name)
	{
		int len = name.length() ;
		if(name.length()==0) return false ;
		char c0 = name.charAt(0) ;      
		if (!Character.isLetter(c0) && c0 != '_') return false;

		for (int i=1; i< len; i++)
		{
			char c = name.charAt(i) ;
			if (!Character.isLetterOrDigit(c0) && c != '_' ) return false ;
		}
		return true ;
	}
}
