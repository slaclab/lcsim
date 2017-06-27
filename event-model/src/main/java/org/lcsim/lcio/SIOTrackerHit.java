package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import hep.io.sio.SIORef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.lcsim.event.TrackerHit;

/**
 *
 * @author Tony Johnson
 * @author Jeremy McCormick
 * @version $Id: SIOTrackerHit.java,v 1.4 2011/08/24 18:51:18 jeremy Exp $
 */
class SIOTrackerHit implements TrackerHit
{
    private List<SIORef> tempHits;
    private List rawHits;
    private int type;
    private double[] position = new double[3];
    private double[] covMatrix = new double[6];
    private float dEdx;    
    private float time;
    private float edepError;
    private int quality;
    private long id;

    SIOTrackerHit(SIOInputStream in, int flags, int version) throws IOException
    {
        // Cell ID.
        if(version >= 1060)
        {
            int cellID0 = in.readInt();
            int cellID1 = 0;
            if( (flags & (1 << LCIOConstants.THBIT_ID1)) != 0 )
            {
                cellID1 = in.readInt();
            }
            id = ((long) cellID1) << 32 | cellID0;
        }        

        type = in.readInt();
        for (int i = 0; i < 3; i++)
            position[i] = in.readDouble();
        
        for (int i = 0; i < 6; i++)
            covMatrix[i] = in.readFloat();

        dEdx = in.readFloat();

        if ( version > 1012 )
        {
            edepError = in.readFloat();
        }

        time = in.readFloat();
        
        if ( version > 1011 )
        {            
            quality = in.readInt();
        }

        int nRawHits = 1 ;
        if( version > 1002)
        {
            nRawHits = in.readInt() ;
        }

        tempHits = new ArrayList<SIORef>(nRawHits) ;
        for (int i = 0; i < nRawHits ; i++)
        {
            tempHits.add(in.readPntr());
        }
        rawHits = null;

        in.readPTag(this);
    }
    
    static void write(TrackerHit hit, SIOOutputStream out, int flags) throws IOException
    {
        // Cell ID.
        long cellID = hit.getCellID();
        out.writeInt((int) cellID);
        if (LCIOUtil.bitTest(flags, LCIOConstants.THBIT_ID1))
        {
            out.writeInt((int) (cellID >> 32));
        }
        
        out.writeInt(hit.getType());
        
        double[] pos = hit.getPosition();        
        for (int i = 0; i < 3; i++)
            out.writeDouble(pos[i]);

        double[] matrix = hit.getCovMatrix();
        for (int i = 0; i < 6; i++)
            out.writeFloat((float) matrix[i]);
        
        out.writeFloat((float) hit.getdEdx());        
        out.writeFloat((float) hit.getEdepError());        
        out.writeFloat((float) hit.getTime());        
        out.writeInt((int) hit.getQuality());

        List rawHits = hit.getRawHits() ;
        out.writeInt( rawHits.size()) ;
        for (int i = 0; i < rawHits.size() ; i++)
        {
            out.writePntr( rawHits.get(i) );
        }

        out.writePTag(hit);
    }   
        
    public List getRawHits()
    {
        if (rawHits == null && tempHits != null)
        {
            rawHits = new ArrayList(tempHits.size());
            for (SIORef ref : tempHits)
            {
                rawHits.add(ref.getObject());
            }
            tempHits = null;
        }
        return rawHits;
    }
    
    public long getCellID()
    {
        return id;
    }

    public double getdEdx()
    {
        return dEdx;
    }

    public int getType()
    {
        return type;
    }

    public double getTime()
    {
        return time;
    }

    public double[] getPosition()
    {
        return position;
    }

    public double[] getCovMatrix()
    {
        return covMatrix;
    }
    
    public double getEdepError()
    {
        return edepError;
    }
    
    public int getQuality()
    {
        return quality;
    }    
 }
