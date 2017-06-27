package org.lcsim.geometry.util;

import hep.physics.vec.Hep3Vector;
import hep.physics.vec.BasicHep3Vector;

import org.lcsim.geometry.Subdetector;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;

/**
 * A basic implementation of org.lcsim.geometry.IDDecoder
 * for others to extend.  It uses the org.lcsim.geometry.util
 * classes for functionality.
 *
 * @author jeremym
 * @version $Id: BaseIDDecoder.java,v 1.19 2011/04/28 01:12:32 jeremy Exp $
 */
public class BaseIDDecoder
implements org.lcsim.geometry.IDDecoder
{
    protected org.lcsim.geometry.util.IDDecoder decoder;
    protected IDDescriptor descriptor;
    protected int[] values;
    protected boolean valid = false;
    protected Subdetector detector;
    protected int layerIndex = -1;

    public BaseIDDecoder()
    {}

    public BaseIDDecoder(IDDescriptor id)
    {
        setIDDescription(id);
    }
    
    public org.lcsim.geometry.util.IDDecoder getDecoder()
    {
    	return decoder;
    }

    public double getX()
    {
        return 0;
    }

    public double getY()
    {
        return 0;
    }

    public double getZ()
    {
        return 0;
    }

    public double getPhi()
    {
        return 0;
    }

    public double getTheta()
    {
        return 0;
    }

    public double[] getPosition()
    {
        return new double[] { getX(), getY(), getZ() };
    }

    public void setID(long id)
    {
        decoder.setID(id);
        decoder.getValues(values);
        valid = true;
    }

    // FIXME: dup of util.IDDecoder method
    public int getValue(String field)
    {
        return decoder.getValue(field);
    }

    // FIXME: dup of util.IDDecoder method
    public int getValue(int index)
    {
        return decoder.getValue(index);
    }
    
    public int[] getValues(int[] buffer)
    {
        return decoder.getValues(buffer);
    }

    // FIXME: dup of util.IDDecoder method
    public String getFieldName(int index)
    {
        return decoder.getFieldName(index);
    }

    // FIXME: dup of util.IDDecoder method
    public int getFieldIndex(String name)
    {
        return decoder.getFieldIndex(name);
    }

    // FIXME: dup of util.IDDecoder method
    public int getFieldCount()
    {
        return values.length;
    }

    public void setIDDescription(IDDescriptor id)
    {
        descriptor = id;
        decoder = new org.lcsim.geometry.util.IDDecoder(id);
        values = new int[id.fieldCount()];

        // FIXME: doesn't belong here
        setLayerIndex(id);
    }

    public IDDescriptor getIDDescription()
    {
        return descriptor;
    }

    // FIXME: dup of util.IDDecoder method
    public String toString()
    {
        return decoder == null ? "NoDecoder" : decoder.toString();
    }

    public boolean isValid()
    {
        return valid;
    }

    public BarrelEndcapFlag getBarrelEndcapFlag()
    {
        return BarrelEndcapFlag.createBarrelEndcapFlag(decoder.getValue("barrel"));
    }

    public void setSubdetector(Subdetector d)
    {
        detector = d;
    }

    public Subdetector getSubdetector()
    {
        return detector;
    }

    private void setLayerIndex(IDDescriptor id)
    {
    	try {
    		layerIndex = id.indexOf("layer");
    	}
    	catch (IllegalArgumentException x)
    	{
    		System.err.println("WARNING: The layer field does not exist in this IDDecoder!");
    	}
    }

    public int getLayer()
    {
        return values[layerIndex];
    }
    
    public int getVLayer()
    {
        return getLayer();
    }

    public int getSystemID()
    {
        int sysid = -1;
        if ( getSubdetector() != null )
        {
            sysid = getSubdetector().getSystemID();
        }
        else 
        {
            sysid = decoder.getValue("system");
        }
        return sysid;
    }

    public int getSystemNumber()
    {
        return getSystemID();
    }

    public long[] getNeighbourIDs(int deltaLayer, int deltaTheta, int deltaPhi)
    {
        long[] dummyNeighbours = {0, 0, 0};
        return dummyNeighbours;
    }

    public boolean supportsNeighbours()
    {
        return false;
    }

    public long[] getNeighbourIDs()
    {
        return getNeighbourIDs(1,1,1);
    }

    public long findCellContainingXYZ(Hep3Vector pos)
    {
        return 0;
    }

    public long findCellContainingXYZ(double[] pos)
    {
        return 0;
    }

    //public long findCellContainingXYZ(double x, double y, double z)
    //{
    //    return 0;
    //}
    
    public final Hep3Vector getPositionVector()
    {
        return new BasicHep3Vector(getPosition());
    }
}
