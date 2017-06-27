package org.lcsim.detector.solids;

import hep.physics.vec.Hep3Vector;

import static java.lang.Math.abs;
import static org.lcsim.detector.solids.Inside.INSIDE;
import static org.lcsim.detector.solids.Inside.OUTSIDE;
import static org.lcsim.detector.solids.Inside.SURFACE;
import static org.lcsim.detector.solids.Tolerance.TOLERANCE;
import static java.lang.Math.sqrt;

/**
 *
 * @author Tim Nelson <tknelson@slac.stanford.edu>
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: Tube.java,v 1.8 2009/03/18 02:51:54 jeremy Exp $
 */
public class Tube
extends AbstractSolid
{
	private double innerRadius, outerRadius, zHalfLength;
	
    public Tube(String name, double innerRadius, double outerRadius, double zHalfLength)
    {
    	super(name);
    	this.innerRadius = innerRadius;
    	this.outerRadius = outerRadius;
    	this.zHalfLength = zHalfLength;    	
    }
    
    /** 
	 * @see org.lcsim.detector.solids.ITube#getInnerRadius()
	 */
    public double getInnerRadius()
    {
    	return innerRadius;
    }
    
    /** 
	 * @see org.lcsim.detector.solids.ITube#getOuterRadius()
	 */
    public double getOuterRadius()
    {
    	return outerRadius;
    }
    
    /** 
	 * @see org.lcsim.detector.solids.ITube#getZHalfLength()
	 */
    public double getZHalfLength()
    {
    	return zHalfLength;
    }
        
    public double getCubicVolume()
    {
        return Math.PI * (outerRadius * outerRadius - 
        		innerRadius * innerRadius) * (zHalfLength * 2);
    }
    
    public Inside inside(Hep3Vector p)
    {
    	Inside inside = OUTSIDE;
    	
        double r_xy = sqrt(p.x()*p.x() + p.y()*p.y());
        
        // Inside.
        if ( r_xy > innerRadius - TOLERANCE*0.5 && 
             r_xy < outerRadius + TOLERANCE*0.5 && 
        	 abs(p.z()) < zHalfLength + TOLERANCE*0.5)
        {
        	inside = INSIDE;
        }
        else
        {
        	// Inner surface.
        	if (r_xy >= innerRadius - TOLERANCE*0.5
        			&& r_xy <= innerRadius + TOLERANCE*0.5
        			&& abs(p.z()) <= zHalfLength + TOLERANCE*0.5)        		
        	{
        		inside = SURFACE;
        	}
        	// Outer surface.
        	else if (r_xy >= outerRadius - TOLERANCE*0.5
        			&& r_xy <= outerRadius + TOLERANCE*0.5
        			&& abs(p.z()) <= zHalfLength + TOLERANCE*0.5)
        	{
        		inside = SURFACE;
        	}
        	// Z plane.
        	else if (abs(p.z()) >= zHalfLength - TOLERANCE*0.5 &&
        			abs(p.z()) <= zHalfLength + TOLERANCE*0.5 &&
        			r_xy >= innerRadius - TOLERANCE*0.5 && 
                    r_xy <= outerRadius + TOLERANCE*0.5)
        	{
        		inside = SURFACE;
        	}
        }
        
        return inside;
    }  
    
    public String toString()
    {
        return this.getClass().getSimpleName()+" "+name+" : innerRadius= "+innerRadius+ " outerRadius= "+outerRadius+" zHalfLength= "+zHalfLength;
    }
}