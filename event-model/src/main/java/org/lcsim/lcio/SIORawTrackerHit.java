package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import hep.io.sio.SIORef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lcsim.detector.identifier.Identifier;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.base.BaseRawTrackerHit;

/**
 * 
 * -Added support for reading/writing list of SimTrackerHits.  FIXME: Non-standard LCIO.
 * 
 * @author Tony Johnson
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: SIORawTrackerHit.java,v 1.9 2011/02/03 20:11:45 jeremy Exp $
 */
class SIORawTrackerHit extends BaseRawTrackerHit
{  
	private List<SIORef> tempHits;

	SIORawTrackerHit(SIOInputStream in, int flags, int version, LCMetaData meta) throws IOException
	{
		int cellid0 = in.readInt();
		int cellid1 = LCIOUtil.bitTest(flags,31) ? in.readInt() : 0;
		cellId = ((long) cellid1)<<32 | cellid0;
		packedID = new Identifier(cellId);
		time = in.readInt();
		int nAdc = in.readInt();
		adcValues = new short[nAdc];
		for (int i=0; i<nAdc; i++)
		{
			adcValues[i] = in.readShort();
		}
		in.pad();
		// Add support for reading in a list of SimTrackerHits.  --JM
		// FIXME: Not supported by standard LCIO.
		/*
		if ((flags & (1<<LCIOConstants.RTHBIT_HITS)) != 0) {
			int nhits = in.readInt();
			tempHits = new ArrayList<SIORef>(nhits);
			for (int i=0; i<nhits; i++) {
				tempHits.add(in.readPntr());
			}
		}
		*/
		in.readPTag(this);
            setMetaData(meta);
	}

	static void write(RawTrackerHit hit, SIOOutputStream out, int flags) throws IOException
	{
		long cellID = hit.getCellID();
		out.writeInt((int) cellID);
		if (LCIOUtil.bitTest(flags,31)) out.writeInt((int) (cellID>>32));
		out.writeInt(hit.getTime());
		short[] adcValues = hit.getADCValues();
		out.writeInt(adcValues.length);
		for (short s : adcValues) out.writeShort(s);
		out.pad();
		// Add support for writing out a list of SimTrackerHits.  --JM
		// FIXME: Not supported by standard LCIO.
		/*
		if ((flags & (1<<LCIOConstants.RTHBIT_HITS)) != 0) {
			out.writeInt(hit.getSimTrackerHits().size());    
			for (SimTrackerHit simhit : hit.getSimTrackerHits()) 
			{
				out.writePntr(simhit);
			}
		}
		*/
			
		out.writePTag(hit);
	}

	/*
	public List<SimTrackerHit> getSimTrackerHits()
	{
		if (simTrackerHits == null && tempHits != null)
		{
			simTrackerHits = new ArrayList<SimTrackerHit>(tempHits.size());
			for (SIORef ref : tempHits)
			{
				simTrackerHits.add((SimTrackerHit) ref.getObject());
			}
			tempHits.clear();
			tempHits = null;
		}
		return simTrackerHits;
	}	
	*/
}
