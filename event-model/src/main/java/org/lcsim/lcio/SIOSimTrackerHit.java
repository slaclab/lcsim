package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import hep.io.sio.SIORef;
import hep.physics.vec.BasicHep3Vector;

import java.io.IOException;

import org.lcsim.event.base.BaseSimTrackerHit;
import org.lcsim.event.MCParticle;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.event.EventHeader.LCMetaData;

/**
 * The SIO implementation of {@link org.lcsim.event.SimTrackerHit}.
 * @author Tony Johnson
 * @author Jeremy McCormick
 * @version $Id: SIOSimTrackerHit.java,v 1.18 2012/07/10 00:11:24 jeremy Exp $
 */
class SIOSimTrackerHit extends BaseSimTrackerHit
{    
    private Object particleref;
        
    SIOSimTrackerHit(SIOInputStream in, int flags, int version, LCMetaData meta) throws IOException
    {
        // Read in the two 32-bit cell IDs.
        cellID0 = in.readInt();
        if(version >= 1060)
        {
            if(LCIOUtil.bitTest(flags, LCIOConstants.THBIT_ID1))
            {
                cellID1 = in.readInt();
            }
        }
        
        // Make the 64-bit ID.
        this.id = ((long) cellID1) << 32 | cellID0;

        // Position.
        position[0] = in.readDouble();
        position[1] = in.readDouble();
        position[2] = in.readDouble();
        positionVec = new BasicHep3Vector(position[0], position[1], position[2]);
        
        // Energy.
        dEdx = in.readFloat();
        
        // Time.
        time = in.readFloat();
        
        // MCParticle pointer.
        particleref = in.readPntr();        
       
        // Momentum.
        if (LCIOUtil.bitTest(flags, LCIOConstants.THBIT_MOMENTUM))
        {
            momentum[0] = in.readFloat();
            momentum[1] = in.readFloat();
            momentum[2] = in.readFloat();
            if (version > 1006)
                pathLength = in.readFloat();
        }

        // Pointer tag.
        if (version > 1000)
            in.readPTag(this);

        setMetaData(meta);
    }   

    public MCParticle getMCParticle()
    {
        if (mcparticle == null) 
        { // Check if this has been cached already.
            if (particleref instanceof SIORef)
            {
                particleref = ((SIORef) particleref).getObject();
                mcparticle = (MCParticle)particleref;
            }
            else 
            {
                // Added a throw here.  I think this probably constitutes a fatal error.  --JM
                // mg May 22/2010 remove this exception
//                throw new RuntimeException("Referenced object is not an MCParticle!");
                
            }
        }
        return mcparticle;
    }
    
    static void write(SimTrackerHit hit, SIOOutputStream out, int flags) throws IOException
    {
        // Cell ID.
        long cellID = hit.getCellID64();
        out.writeInt((int) cellID);
        if (LCIOUtil.bitTest(flags, LCIOConstants.THBIT_ID1))
        {
            out.writeInt((int) (cellID >> 32));
        }

        double[] pos = hit.getPoint();
        out.writeDouble(pos[0]);
        out.writeDouble(pos[1]);
        out.writeDouble(pos[2]);
        out.writeFloat((float) hit.getdEdx());
        out.writeFloat((float) hit.getTime());
        out.writePntr(hit.getMCParticle());
        if (LCIOUtil.bitTest(flags, LCIOConstants.THBIT_MOMENTUM))
        {
            double[] p = hit.getMomentum();
            out.writeFloat((float) p[0]);
            out.writeFloat((float) p[1]);
            out.writeFloat((float) p[2]);
            out.writeFloat((float) hit.getPathLength());
        }
        out.writePTag(hit);
    }
}
