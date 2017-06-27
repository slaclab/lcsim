/*
 * Segmentation.java Created on June 1, 2005, 2:05 PM
 */

package org.lcsim.geometry.segmentation;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.geometry.Layered;
import org.jdom.Element;
import org.lcsim.geometry.layer.Layering;
import org.lcsim.geometry.CylindricalSubdetector;
import hep.physics.vec.Hep3Vector;

/**
 * @author jeremym Base implementation of Segmentation.
 */
public abstract class SegmentationBase extends org.lcsim.geometry.compact.Segmentation
{
    
    public SegmentationBase(Element e)
    {
        super(e);
    }
    
    public abstract String[] getSegmentationFieldNames();

    /**
     * Returns the cell which contains a given point (x,y,z).
     * 
     * @param x Cartesian X coordinate.
     * @param y Cartesian Y coordinate.
     * @param z Cartesian Z coordinate.     
     *
     * @return ID of cell containing the point (maybe either in absorber or live
     *         material), or <em>zero</em> if the point is not inside this
     *         component.
     */
    abstract public long findCellContainingXYZ(double x, double y, double z);

    /**
     * Returns the cell which contains a given point (x,y,z).
     * 
     * @param pos
     *            3-dim array with cartesian coordinates of the point
     * @return ID of cell containing the point (maybe either in absorber or live
     *         material), or <em>zero</em> if the point is not inside this
     *         component.
     */
    public long findCellContainingXYZ(double[] pos)
    {
        return findCellContainingXYZ(pos[0], pos[1], pos[2]);
    }

    /**
     * Returns the cell which contains a given point (x,y,z).
     * 
     * @param pos
     *            Hep3Vector with cartesian coordinates of the point
     * @return ID of cell containing the point (maybe either in absorber or live
     *         material), or <em>zero</em> if the point is not inside this
     *         component.
     */
    public long findCellContainingXYZ(Hep3Vector pos)
    {
        return findCellContainingXYZ(pos.x(), pos.y(), pos.z());
    }

    /**
     * @deprecated
     */
    protected int getNumberOfLayers()
    {
        return ((Layered) detector).getLayering().getLayerCount();
    }

    protected double[] transformLocalToGlobal(double[] localPos)
    {
        return detector.transformLocalToGlobal(localPos);
    }

    /**
     * @deprecated
     */
    protected Layering getLayering()
    {
        return ((Layered) detector).getLayering();
    }

    /**
     * @deprecated
     */
    protected double getZMin()
    {
        return ((CylindricalSubdetector) detector).getZMin();
    }

    /**
     * @deprecated
     */
    protected double getZMax()
    {
        return ((CylindricalSubdetector) detector).getZMax();
    }

    /**
     * @deprecated
     */
    protected double getRMin()
    {
        return ((CylindricalSubdetector) detector).getInnerRadius();
    }

    /**
     * @deprecated
     */
    protected double getRMax()
    {
        return ((CylindricalSubdetector) detector).getOuterRadius();
    }
    
    /**
     * @deprecated
     */
    public double getDistanceToSensitive(int layer)
    {
        return ((Layered) detector).getLayering().getDistanceToLayerSensorMid(layer);
    }
    
	public boolean supportsNeighbours()
	{
		return true;
	}
	
	public long getID()
	{
		return this.getDecoder().getID();
	}	
}