/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker;

import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.solids.Polycone;
import org.lcsim.detector.solids.Polycone.ZPlane;

/**
 * This class represents a non-cylindrical portion of a polycone. It is assumed
 * that (rmax-rmin) ~ constant throughout the Polycone so that the Polycone 
 * segment's thickness is always close to its thickness at its average radius.
 * 
 *  
 * @author cozzy
 */
public class MaterialPolyconeSegment {
    IPhysicalVolume _pv;  
    ZPlane _zp1;
    ZPlane _zp2; 
    double _thickness;
    Polycone _pc; 
    double _angle; 
    
    /**
     * Constructs a new Polycone segment. It is assumed that the inner and outer
     * surfaces are roughly parallel. 
     * 
     * @param pv The associated physical volume. 
     * @param zp1 One of the bounding ZPlanes
     * @param zp2 The other bounding ZPlane
     * @param thickness The thickness normal to planes.  
     * @param angle The z-r angle of the planes w.r.t to r=0
     */
    public MaterialPolyconeSegment(IPhysicalVolume pv, ZPlane zp1, ZPlane zp2, double thickness, double angle){
        
        if(!(pv.getLogicalVolume().getSolid() instanceof Polycone)){
            throw new RuntimeException("Non-Polycone physical volume in MaterialPolycone constructor");
        }
        _pc = (Polycone) pv.getLogicalVolume().getSolid(); 
        _pv = pv; 
        if (zp1.getZ()<zp2.getZ()) {
            _zp1 = zp1; 
            _zp2 = zp2; 
        }
        else {
            _zp1 = zp2; 
            _zp2 = zp1; 
        }
        _thickness = thickness; 
        _angle = angle; 
        
    }
    
    public double zMin(){
        return _zp1.getZ(); 
    }
    
    public double zMax(){
        return _zp2.getZ(); 
    }
    
    /**
     * Returns the average of the inner and outer radii at the given z. A  
     * RuntimException is thrown if the requested z is outside the bounds. 
     * @param z a z-coordinate in mm
     * @return the average of the inner and outer radii in mm
     */
    public double rAvgAtZ(double z){
        if (z<zMin() || z > zMax()) {
            throw new RuntimeException("Requested Z Value outside segment bounds");  
        }
        return 0.5*(_pc.getOuterRadiusAtZ(z) + _pc.getInnerRadiusAtZ(z));          
    }
    
    /**
     * Returns the thickness of the polycone in Radiation Lengths. Note that currently 
     * this value is only accurate if the inner and outer surfaces are close to 
     * parallel (i.e. rmax - rmin ~ constant throughout the polycone segment). 
     * This is the thickness measured normal to the plane of the polycone. 
     *
     * @return The thickness in radiation lengths
     */
    public double getThicknessInRL(){
        return _thickness; 
    }
    
    /**
     * Get the z-r angle of the of the plane of the polycone segment from horizontal (r=0)
     * This is calculated using the average r-values at the end points. 
     * @return the angle in radians
     */
    public double getAngle(){
        return _angle; 
    }
    
    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer("Tracker MaterialPolycone "+_pv.getName()+"\n");
        sb.append("Segment from z = "+_zp1.getZ()+" to "+_zp2.getZ()+"\n"); 
        return sb.toString();
    }
}
