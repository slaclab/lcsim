package org.lcsim.recon.tracking.seedtracker;

import hep.physics.vec.Hep3Vector;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.event.GenericObject;

/**
 * Simple implementation of TrackState as an LCIO GenericObject containing 6
 * floats corresponding to position (x,y,z) and momentum (px,py,pz).
 * 
 * @author jeremym
 */
public class TrackState implements GenericObject
{
    List<Float> vals = new ArrayList<Float>();

    /**
     * Float constructor.
     * 
     * @param x
     * @param y
     * @param z
     * @param px
     * @param py
     * @param pz
     */
    public TrackState(float x, float y, float z, float px, float py, float pz)
    {
        vals.add(x);
        vals.add(y);
        vals.add(z);
        vals.add(px);
        vals.add(py);
        vals.add(pz);
    }

    /**
     * Double constructor.
     * 
     * @param x
     * @param y
     * @param z
     * @param px
     * @param py
     * @param pz
     */
    public TrackState(double x, double y, double z, double px, double py, double pz)
    {
        vals.add((float) x);
        vals.add((float) y);
        vals.add((float) z);
        vals.add((float) px);
        vals.add((float) py);
        vals.add((float) pz);
    }
    
    /**
     * Constructor using Hep3Vectors
     * 
     * @param pos position of the track state
     * @param p momentum at the position
     */
    public TrackState(Hep3Vector pos, Hep3Vector p) {
    	vals.add((float)pos.x());
    	vals.add((float)pos.y());
    	vals.add((float)pos.z());
    	vals.add((float)p.x());
    	vals.add((float)p.y());
    	vals.add((float)p.z());
    }

    public double getDoubleVal(int index)
    {
        return 0;
    }

    public float getFloatVal(int index)
    {
        return vals.get(index);
    }

    public int getIntVal(int index)
    {
        return 0;
    }

    public int getNDouble()
    {
        return 0;
    }

    public int getNFloat()
    {
        return 6;
    }

    public int getNInt()
    {
        return 0;
    }

    public boolean isFixedSize()
    {
        return true;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer(" TrackState: \n");
        sb.append(" global cartesian position: "+vals.get(0)+" "+vals.get(1)+" "+vals.get(2));
        sb.append(" momentum: "+vals.get(3)+" "+vals.get(4)+" "+vals.get(5));
        return sb.toString();
    }
}