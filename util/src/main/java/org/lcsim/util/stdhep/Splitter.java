/*
 * Splitter.java
 *
 * Created on August 31, 2001, 5:47 PM
 *
 * Modified June 15, 2002 N.A. Graf
 * Output file names are now derived from input file names.
 * Trap normal EOF exceptions.
 * Added usage hints.
 *
 */
package org.lcsim.util.stdhep;

import java.io.*;
import java.util.StringTokenizer;
import hep.io.stdhep.*;

/**
 * @author tonyj
 */
public class Splitter
{
	/**
	* @param args the command line arguments
	*/
	public static void main(String args[]) throws Exception
	{
		// remind user of correct usage
		if(args.length<1) usage();
		if(args.length==1 && args[0].equals("-h")) usage();
		
		// get input file
		String inputFile = args[0];                

		// does it exist?
		File f = new File(inputFile);
		if (!f.exists())
		{
			System.out.println("\n\n  File "+f + " does not exist!");	
			System.exit(1);
		}

		// set up the output file names
		String outputFile = null;
		StringTokenizer st = new StringTokenizer(inputFile, ".");
		if(st.hasMoreTokens()) outputFile = st.nextToken();
		if (outputFile==null)
		{
			System.out.println("\n\n  Problem parsing input file name");
			System.out.println(" \n\n File name should be in file.stdhep format");
			System.exit(1);
		}

		// get number of events to put into each split output file
		// default is 100
		int nevts = 100;
		if ( args.length > 1 ) nevts = Integer.parseInt(args[1]);
		
		StdhepReader in = null;
		try
		{
		  in = new StdhepReader(inputFile);
		}
		catch(IOException ex)
		{
			System.out.println("Problem opening "+inputFile+" !");
			System.exit(1);
		}
		
		int readEvents = 0;
		int r=0;
		try
		{
			for (r = 0;;r++)
			{
				int n = 0;
				StdhepWriter out = null;
                try
                {
                   for(;;)
    					{
	    					StdhepRecord record = in.nextRecord();
	    					if (record instanceof StdhepEvent)
	    					{
	    						if (out == null)
	    						{
    								String name = outputFile+"-"+r+"-"+nevts+".stdhep";
	    							System.out.println("Writing "+name);
	    							out = new StdhepWriter(name,"title","comment",nevts);
	    						}
	    						out.writeRecord(record);
	    						readEvents++;
	    						if (++n == nevts) break;
    						}
    					}
    			}
                finally
                {
                   if (out != null) out.close();
				}
         	}
		}
		catch( EOFException ex)
		{
			System.out.println("\n\n Read "+readEvents+" events and created "+r +" output files.");
		}	  
		finally
		{
			in.close();
		}
	}

	public static void usage()
	{
		System.out.println("Splitter: an application to split input stdhep files into smaller files.\n");
		System.out.println("Usage: \n\n >> java Splitter filename.stdhep [nevents] \n");
		System.out.println(" Where \n  filename is a file in stdhep format \n  nevents is the number of events in each output file [ default is 100 ]");
		System.out.println("  Output files will be named filename-nnn-nevents.stdhep");
		System.exit(0);
	}

}
