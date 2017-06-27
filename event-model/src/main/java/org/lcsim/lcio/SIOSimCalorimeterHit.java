package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import hep.physics.vec.BasicHep3Vector;

import java.io.IOException;
import java.util.ArrayList;

import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.event.base.BaseSimCalorimeterHit;

/**
 * @author Tony Johnson
 * @author Jeremy McCormick
 * @version $Id: SIOSimCalorimeterHit.java,v 1.13 2012/07/20 09:23:15 grefe Exp $
 */
public class SIOSimCalorimeterHit extends BaseSimCalorimeterHit 
{       
    // constructor from LCIO data file
    SIOSimCalorimeterHit(SIOInputStream in, int flags, int version, LCMetaData meta) throws IOException
    {           
        int cellid0 = in.readInt();
        int cellid1 = 0;
        if (LCIOUtil.bitTest(flags,LCIOConstants.RCHBIT_ID1) || version==8) {
            cellid1 = in.readInt();
        }

        id = ((long) cellid1)<<32 | cellid0;
        rawEnergy = in.readFloat();

        if (LCIOUtil.bitTest(flags,LCIOConstants.CHBIT_LONG))
        {
            positionVec = new BasicHep3Vector(in.readFloat(), in.readFloat(), in.readFloat());
        }
        nContributions = in.readInt();
        particle = new Object[nContributions];
        energyContrib = new float[nContributions];
        times = new float[nContributions];
        steps = new ArrayList();

        boolean hasPDG = LCIOUtil.bitTest(flags,LCIOConstants.CHBIT_PDG);
        if (hasPDG) pdg = new int[nContributions];
        for (int i = 0; i < nContributions; i++)
        {
            particle[i] = in.readPntr();
            energyContrib[i] = in.readFloat();
            times[i] = in.readFloat();
            if (hasPDG) 
            {
                pdg[i] = in.readInt();
                if(version > 1051)
                {
                    float[] st = new float[3];
                    st[0] = in.readFloat();
                    st[1] = in.readFloat();
                    st[2] = in.readFloat();
                    steps.add(st);
                }

            }            
        }
        if ( version > 1000 ) in.readPTag(this);
        setMetaData(meta);
        
        // Calculate the corrected energy from raw energy.
        calculateCorrectedEnergy();
    }
    
    static private float emptyPos[] = new float[3]; 
    
    static void write(SimCalorimeterHit hit, SIOOutputStream out, int flags) throws IOException
    {
        long cellID = hit.getCellID();
        out.writeInt((int) cellID);
        if (LCIOUtil.bitTest(flags,LCIOConstants.CHBIT_ID1)) out.writeInt((int) (cellID>>32));
        out.writeFloat((float) hit.getRawEnergy());

        if ((flags & (1 << LCIOConstants.CHBIT_LONG)) != 0)
        {
            double[] pos = hit.getPosition();
            out.writeFloat((float) pos[0]);
            out.writeFloat((float) pos[1]);
            out.writeFloat((float) pos[2]);
        }

        boolean hasPDG = LCIOUtil.bitTest(flags,LCIOConstants.CHBIT_PDG);
        int n = hit.getMCParticleCount();
        out.writeInt(n);
        for (int i = 0; i < n; i++)
        {
            out.writePntr(hit.getMCParticle(i));
            out.writeFloat((float) hit.getContributedEnergy(i));
            out.writeFloat((float) hit.getContributedTime(i));
            if (hasPDG) 
            {
                out.writeInt(hit.getPDG(i));
                float[] st = hit.getStepPosition(i);
                if (st == null) st = emptyPos;
                out.writeFloat(st[0]);
                out.writeFloat(st[1]);
                out.writeFloat(st[2]);                
            }
        }
        out.writePTag(hit);
    }    
}
