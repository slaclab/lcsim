package org.lcsim.detector.solids;

import hep.physics.vec.Hep3Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A port of Polycone that only maintains data members.
 * No solid functionality for now.
 * 
 * @author Cosmin Deaconu <cozzyd@stanford.edu>
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class Polycone 
extends AbstractSolid
{
    List<ZPlane> zplanes = new ArrayList<ZPlane>();
    private double zHalfLength = 0; 
    private double zMax; 
    private double zMin; 
    
    public Polycone(String name, List<ZPlane> zplanes)
    {
    	super(name);
    	for (ZPlane zplane : zplanes)
    	{
    		this.zplanes.add(zplane);
    	}
        
        //keep ZPlanes sorted in Z... otherwise things will get messed up
        Collections.sort(this.zplanes, new Comparator<ZPlane>(){
            
            public int compare(ZPlane a, ZPlane b){
                if (a.z<b.z) return -1;
                if (a.z>b.z) return 1; 
                return 0; 
            }
        });
        
        //calculate zmax,min,halflength
        if(zplanes.size()>0){
            zMax = zplanes.get(zplanes.size()-1).z;
            zMin = zplanes.get(0).z;
            zHalfLength = (zMax - zMin)/2.0;
            
        }
    }    
        
    public List<ZPlane> getZPlanes()
    {
        return zplanes;
    }
        
    public int getNumberOfZPlanes()
    {
        return zplanes.size();
    }
    
    public ZPlane getZPlane(int idx)
    {
        return zplanes.get(idx);
    }
    
 
    public double getZHalfLength(){
        return zHalfLength;
    }
    
    public static class ZPlane
    {
        double rmin, rmax, z;
        
        public ZPlane(double rmin, double rmax, double z)
        {
            this.rmin = rmin;
            this.rmax = rmax;
            this.z = z;
        }
        
        public double getRMin()
        {
            return rmin;
        }
        
        public double getRMax()
        {
            return rmax;
        }
        
        public double getZ()
        {
            return z;
        }
        
        @Override
        public String toString(){
            return "ZPlane w/ rmin = "+rmin+", rmax = "+rmax+", z = "+z;
        }
    }

    public double getCubicVolume() 
    {   
        if (zplanes.size()<2) throw new RuntimeException("Too few ZPlanes in PolyCone"); 
        double vol = 0.0; 
        for (int i = 1; i<zplanes.size(); i++) {
            vol+=getSegmentVolume(zplanes.get(i-1), zplanes.get(i)); 
        }
        return vol;
    }

    public Inside inside(Hep3Vector position) 
    {
        if (zplanes.size()<2) throw new RuntimeException("Too few ZPlanes in PolyCone"); 

        double r = Math.sqrt(position.x()*position.x() + position.y()*position.y());
        double z = position.z();

        for (int i = 1; i<zplanes.size(); i++) {

            ZPlane p1 = zplanes.get(i-1);
            ZPlane p2 = zplanes.get(i);

            //see if it's at the left or right edge
            if ((i==1 && z == p1.z && r<=p1.rmax && r>=p1.rmin) ||
                (i==zplanes.size()-1 && z == p2.z && r<=p2.rmax && r>=p2.rmin)) 
                return Inside.SURFACE;

            //this means we're in the right section...
            if ((z <= p2.z && z >= p1.z)) {

                double b = f(position.z(),OUTER,p1,p2);
                double a = f(position.z(),INNER,p1,p2);

                if (r==b || r==a) return Inside.SURFACE;
                if (r<b && r>a) return Inside.INSIDE;
            }
        }

        return Inside.OUTSIDE; 
    }           

    /**
    * Returns the inner radius of the polycone at the given z. If
    * no portion of the polycone exists for the given z, 0. is returned. 
    * @param z a z-coordinate in mm
    * @return the inner radius in mm at z
    */
    public double getInnerRadiusAtZ(double z) {
       return getRadiusAtZ(z,INNER);
    }

    /**
    * Returns the outer radius of the polycone at the given z. If
    * no portion of the polycone exists for the given z, 0. is returned. 
    * @param z a z-coordinate in mm
    * @return the outer radius in mm at z
    */
    public double getOuterRadiusAtZ(double z) {
       return getRadiusAtZ(z,OUTER);
    }

    /**
    * Returns the volume of the segment at the given value of z. 
    * 
    * <ul>
    *   <li>If there is no segment at that z, 0 is returned.
    *   <li>If z lies at the junction of the two segments, the earlier segment
    *       in the ZPlanes list will be used (should be the one at smaller z)
    * </ul>
    * @param z a z-coordinate in mm 
    * @return the volume of the segment at z in mm^3
    */
    public double getVolumeOfSegmentAtZ(double z){
       if (z<zMin || z>zMax) return 0; 
       for (int i = 1; i < zplanes.size(); i++) {
            if (z<zplanes.get(i).z){
                ZPlane p1 = zplanes.get(i-1); 
                ZPlane p2 = zplanes.get(i); 
                return getSegmentVolume(p1,p2); 
            }
       }
       return 0; 
    }

   /**
    * Returns the volume of a polycone segment defined by the zplanes p1 and p2
    * @param p1 A bounding ZPlane
    * @param p2 The other bounding ZPlane
    * @return the volume of the segment
    */
   public static double getSegmentVolume(ZPlane p1, ZPlane p2) {    
        return Math.PI/(3.0)*(p2.z-p1.z)*(p2.rmax*p2.rmax + p1.rmax*p2.rmax + p1.rmax*p1.rmax 
                        - p2.rmin*p2.rmin - p2.rmin*p1.rmin - p1.rmin*p1.rmin);           
   }

   private static final boolean INNER = true; 
   private static final boolean OUTER = false; 
   //Calculates the radius at any z of either the outer or inner part of the segment
   private double f(double z, boolean whichR, ZPlane p1, ZPlane p2){

        double z1 = p1.z;
        double z2 = p2.z;
        double x1 = whichR ? p1.rmin : p1.rmax;
        double x2 = whichR ? p2.rmin : p2.rmax;

        double m = (x2-x1)/(z2-z1);
        return x1 + m*(z-z1);
   }

   private double getRadiusAtZ(double z, boolean whichR){
        if (z<zMin || z>zMax) return 0; 
        for (int i = 1; i <zplanes.size(); i++){
            if (z<=zplanes.get(i).z){
                return f(z,whichR,zplanes.get(i-1),zplanes.get(i));
            }
        }
        return 0.; 
   }         
}