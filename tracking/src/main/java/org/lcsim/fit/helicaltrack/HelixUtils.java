/*
 * HelixUtils.java
 *
 * Created on February 1, 2008, 5:48 PM
 *
 */

package org.lcsim.fit.helicaltrack;

import org.apache.commons.math.util.FastMath;

import hep.physics.matrix.BasicMatrix;
import hep.physics.matrix.Matrix;
import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import java.util.ArrayList;
import java.util.List;
import org.lcsim.fit.circle.CircleFit;

/**
 * Assorted helix utilities that operate on HelicalTrackFits.
 * 
 * Since these utilities are typically called many times for a
 * given HelicalTrackFit (or in some cases for a given CircleFit),
 * a number of derived quantities used by the methods in this class
 * are cached to minimize re-computation.  Since a HelicalTrackFit
 * (or CircleFit) is immutable, these quantities only need to be
 * calculated once.
 * @author Richard Partridge
 * @version 1.0
 */
public class HelixUtils {
    private static double _minslope = 1.e-6;
	private static double _maxpath  = 2400; // todo, add a more "dynamic" maxpath
	private static double _epsilon  = 0.01; // value below which a z vector Or an angle is considered as null
        private static double _tol = 1.e-4; // Maximum aspect ratio of hit covariance terms for hit to measure x-y coord
    
    /**
     * Creates a new instance of HelixUtils.
     */
    public HelixUtils() {
    }
    
    /**
     * Return the x-y path length between two HelicalTrackHits.
     * @param cfit CircleFit to be used in calculating the path length
     * @param hit1 first hit
     * @param hit2 second hit
     * @return path length from hit1 to hit2
     */
    public static double PathLength(CircleFit cfit, HelicalTrackHit hit1, HelicalTrackHit hit2) {
        //  Find the position on the circle for the hits
        Hep3Vector pos1 = getPositionOnCircle(cfit, hit1);
        Hep3Vector pos2 = getPositionOnCircle(cfit, hit2);
        //  Return the path length between hit1 and hit2
        return PathCalc(xc(cfit), yc(cfit), RC(cfit), pos1.x(), pos1.y(), pos2.x(), pos2.y());
    }
    
    /**
     * Return the x-y path length from the DCA.
     * @param cfit CircleFit to be used in calculating the path length
     * @param hit hit to be used for the path length calculation
     * @return path length from the DCA to the hit
     */
    public static double PathLength(CircleFit cfit, HelicalTrackHit hit) {
        //  Find the position on the circle for the hit
        Hep3Vector pos = getPositionOnCircle(cfit, hit);
        //  Return the path length from the DCA
        return PathCalc(xc(cfit), yc(cfit), RC(cfit), x0(cfit), y0(cfit), pos.x(), pos.y());
    }
    
     /**
     * Return the x-y path length from the DCA to a HelicalTrackHit.
     * @param helix HelicalTrackFit to be used in calculating the path length
     * @param hit hit to be used for the path length calculation
     * @return path length from the DCA to the hit
     */
    public static double PathLength(HelicalTrackFit helix, HelicalTrackHit hit) {

        //  Calculate the shortest path length from the DCA to the hit x-y coordinates
        double path0 = PathCalc(helix.xc(), helix.yc(), helix.R(), helix.x0(), helix.y0(), hit.x(), hit.y());
        if (hit instanceof HelicalTrack2DHit) return path0;

        //  Find the closest 3D hit in the z coordinate (if there is one)
        HelicalTrackHit close = null;
        double zhit = hit.z();
        for (HelicalTrackHit fithit : helix.PathMap().keySet()) {
            if (!(fithit instanceof HelicalTrack2DHit)) {

                 if (close == null) {
                    //  If this is the first 3D hit, mark it as being closest
                    close = fithit;
                } else {
                    //  Check if the fithit is closer in z
                    if (Math.abs(zhit - fithit.z()) < Math.abs(zhit - close.z())) close = fithit;
                }
            }
        }

        //  If we didn't find another 3D hit, return the path from the DCA
        if (close == null) return path0;

        //  Find the path length for the closest hit assuming it hasn't looped
        double path = helix.PathMap().get(close) +
                PathCalc(helix.xc(), helix.yc(), helix.R(), close.x(), close.y(), hit.x(), hit.y());
        return path;
    }
    
    /**
     * Return the x-y path length to a z-plane.
     * @param helix HelicalTrackFit to be used in calculating the path length
     * @param z location of z-plane
     * @return path length from the DCA to the z-plane
     */
    public static double PathToZPlane(HelicalTrackFit helix, double z) {
        //  Find the z distace between the DCA and the z plane and calculate the x-y path length
        double zdist = z - helix.z0();
        double safeslope = helix.slope();
        if (Math.abs(safeslope) < _minslope) safeslope = _minslope * Math.signum(safeslope);
        return zdist / safeslope;
    }

        /**
     * Return the x-y path length to an x-plane.
     * @param helix HelicalTrackFit to be used in calculating the path length
     * @param x location of x-plane
     * @return path length from the DCA to the x-plane
     */
    public static List<Double>  PathToXPlane(HelicalTrackFit helix, double x,double smax, int mxint) {
        //  Create a list to hold the path lengths
        List<Double> pathlist = new ArrayList<Double>();
        //  Retrieve helix dca and RC
        double x0 = helix.x0();
        double y0 = helix.y0();

        double xc=helix.xc();
        double yc=helix.yc();
        double RC = helix.R();
        double y=yc+Math.signum(RC)*Math.sqrt(RC*RC-Math.pow(x-xc,2));

        double s=PathCalc(xc,yc,RC,x0,y0,x,y);

//        System.out.println("PathToXPlane :  s = "+s+"; sFromClass = "+sFromClass);
        /*
        while (s < smax && pathlist.size() < mxint) {
            //  Add this odd-numbered crossing to the list
            pathlist.add(s);
                //  Advance to the next even-numbered crossing
            s += 2. * (Math.PI - dphi) * Math.abs(RC);
            //  Check to see if we should add it
            if (s < smax && pathlist.size() < mxint) pathlist.add(s);
            //  Add this even-numbered crossing to the list
            s += 2. * dphi * Math.abs(RC);
        }*/
        pathlist.add(s);

        return pathlist;
    }


    /**
     * Return a list of x-y path lengths to a cylinder centered on the z axis.
     * @param helix HelicalTrackFit to be used in calculating the path length
     * @param r desired radius
     * @param smax maximum path length to be considered
     * @param mxint Maximum number of intersections
     * @return list of path lengths
     */
    public static List<Double> PathToCylinder(HelicalTrackFit helix, double r, double smax, int mxint) {
        //  Create a list to hold the path lengths
        List<Double> pathlist = new ArrayList<Double>();
        //  Retrieve helix dca and RC
        double dca = helix.dca();
        double RC = helix.R();
        //  Find cos(dphi) for dphi from the point of closest approach to the first crossing
        double cdphi = 1 - (r * r - dca * dca) / (2. * RC * (RC - dca));
        //  Make sure we have a physical intersection
        if (Math.abs(cdphi) < 1.) {
            //  Calculate dphi and the path length to the first crossing
            double dphi = Math.acos(cdphi);
            Double s = dphi * Math.abs(RC);
            //  Loop over crossings until we exceed one of the limits
            while (s < smax && pathlist.size() < mxint) {
                //  Add this odd-numbered crossing to the list
                pathlist.add(s);
                //  Advance to the next even-numbered crossing
                s += 2. * (Math.PI - dphi) * Math.abs(RC);
                //  Check to see if we should add it
                if (s < smax && pathlist.size() < mxint) pathlist.add(s);
                //  Add this even-numbered crossing to the list
                s += 2. * dphi * Math.abs(RC);
            }
        }
        return pathlist;
    }
    
    /**
     * Return a unit vector giving the track direction at a given point on
     * the helix.
     * @param helix HelicalTrackFit to be used in calculating direction
     * @param s path length to the desired point on helix
     * @return direction unit vector
     */
    public static Hep3Vector Direction(HelicalTrackFit helix, double s) {
        //  Calculate the azimuthal direction
        double phi = helix.phi0() - s / helix.R();
        double sth = helix.sth();
        //  Calculate the components of the direction unit vector
        double ux = Math.cos(phi) * helix.sth();
        double uy = Math.sin(phi) * helix.sth();
        double uz = helix.cth();
        //  Return the direction unit vector
        return new BasicHep3Vector(ux, uy, uz);
    }
    
    /**
     * Return the TrackDirection object for a given point on a helix.
     * This might seem like an odd place to put this code, but putting
     * it here allows the cached helix quantities to be used in constructin
     * the TrackDirection objects.
     * @param helix HelicalTrackFit to use in constructing the TrackDirection
     * @param s path length specifying location on helix
     * @return TrackDirection object for this point on the helix
     */
    public static TrackDirection CalculateTrackDirection(HelicalTrackFit helix, double s) {
        Hep3Vector dir = Direction(helix, s);
        Matrix deriv = DirectionDerivates(helix, dir, s);
        return new TrackDirection(dir, deriv);
    }
    
    /**
     * Return the location in space for a particular point on a helix.
     * @param helix HelicalTrackFit to be used
     * @param s path length
     * @return point in space corresponding to the given path length
     */
    public static Hep3Vector PointOnHelix(HelicalTrackFit helix, double s) {
        //  Find the azimuthal direction at this path length
        double RC = helix.R();
        double phi = helix.phi0() - s / RC;
        //  Calculate the position on the helix at this path length
        double x = helix.xc() - RC * Math.sin(phi);
        double y = helix.yc() + RC * Math.cos(phi);
        double z = helix.z0() + s * helix.slope();
        //  Return the position as a Hep3Vector
        return new BasicHep3Vector(x, y, z);
    }

    private static Hep3Vector getPositionOnCircle(CircleFit cfit, HelicalTrackHit hit) {

        //  Get hit position and errors
        Hep3Vector pos = hit.getCorrectedPosition();
        SymmetricMatrix cov = hit.getCorrectedCovMatrix();
        double dxdx = cov.diagonal(0);
        double dydy = cov.diagonal(1);
        if ((dxdx < _tol * dydy) && (dydy < _tol * dxdx)) return pos;

        //  Get circle parameters
        double x0 = xc(cfit);
        double y0 = yc(cfit);
        double R = Math.abs(RC(cfit));

        //  Get hit coordinates
        double x = pos.x();
        double y = pos.y();


        if (_tol * dxdx > dydy && Math.abs(y-y0) < R) {
            double xnew1 = Math.sqrt(R*R - (y-y0)*(y-y0)) + x0;
            double xnew2 = -xnew1 + 2. * x0;
            if (Math.abs(xnew1 - x) < Math.abs(xnew2 - x)) {
                x = xnew1;
            } else {
                x = xnew2;
            }
        }
        if (_tol * dydy > dxdx && Math.abs(x-x0) < R) {
            double ynew1 = Math.sqrt(R*R - (x-x0)*(x-x0)) + y0;
            double ynew2 = -ynew1 + 2. * y0;
            if (Math.abs(ynew1 - y) < Math.abs(ynew2 - y)) {
                y = ynew1;
            } else {
                y = ynew2;
            }
        }

        return new BasicHep3Vector(x, y, pos.z());
    }

    private static double PathCalc(double xc, double yc, double RC, double x1, double y1, double x2, double y2) {
        //  Find the angle between these points measured wrt the circle center
        double phi1 = FastMath.atan2(y1 - yc, x1 - xc);
        double phi2 = FastMath.atan2(y2 - yc, x2 - xc);
        double dphi = phi2 - phi1;
        //  Make sure dphi is in the valid range (-pi, pi)
        if (dphi >  Math.PI) dphi -= 2. * Math.PI;
        if (dphi < -Math.PI) dphi += 2. * Math.PI;
        //  Return the arc length
        return -RC * dphi;
    }

    /**
     * Return the derivatives of the momentum unit vector with respect to the
     * helix parameters.  The direction derivatives are returned in matrix
     * form, with the row giving the cartesian component of the momentum
     * vector and the column giving the helix parameter.
     * @param helix HelicalTrackFit to be used in calculating derivatives
     * @param s path length to the desired point on the helix
     * @return direction derivatives
     */
    private static Matrix DirectionDerivates(HelicalTrackFit helix, Hep3Vector u, double s) {
        //  Create the matrix that will hold the derivatives
        BasicMatrix deriv = new BasicMatrix(3,5);
        //  Retrieve some helix info
        double cphi0 = Math.cos(helix.phi0());
        double sphi0 = Math.sin(helix.phi0());
        double sth = helix.sth();
        double cth = helix.cth();
        double dca = helix.dca();
        double omega = helix.curvature();
        //  Calculate the non-zero derivatives of the direction with respect to the helix parameters
        deriv.setElement(0, HelicalTrackFit.curvatureIndex, (u.x() - cphi0 * sth) / omega);  // du_x / domega
        deriv.setElement(1, HelicalTrackFit.curvatureIndex, (u.y() - sphi0 * sth) / omega);  // du_y / domega
        deriv.setElement(0, HelicalTrackFit.dcaIndex, -omega * cphi0 * sth);  // du_x / ddca
        deriv.setElement(1, HelicalTrackFit.dcaIndex, -omega * sphi0 * sth);  // du_y / ddca
        deriv.setElement(0, HelicalTrackFit.phi0Index, -(1 - dca * omega) * sphi0 * sth);  // du_x / dphi0
        deriv.setElement(1, HelicalTrackFit.phi0Index,  (1 - dca * omega) * cphi0 * sth);  // du_y / dphi0
        deriv.setElement(0, HelicalTrackFit.slopeIndex, -u.x() * sth * cth);  // du_x / dslope
        deriv.setElement(1, HelicalTrackFit.slopeIndex, -u.y() * sth * cth);  // du_y / dslope
        deriv.setElement(2, HelicalTrackFit.slopeIndex, sth*sth*sth);  // du_z / dslope
        //  Return the derivative matrix
        return deriv;
    }
    /**
	 * return true if the helix is intercepting the bounded cylinder
     *
     * Check if at least one of the first ten intersection of the helix with the cylinder
     * is between zmin and zmax...there might be a better way to do this
	 * @param helix
	 * @param r rarius of the cylinder
	 * @param zmin lower bound of the cylinder
	 * @param zmax higher bound of the cylinder
	 * @return
	 */
	public static boolean isInterceptingBoundedCylinder(HelicalTrackFit helix,double r,double zmin,double zmax){
		double minpath = PathToZPlane(helix, zmin);//not sure it's very efficient to calculate the maximum path
		double maxpath = PathToZPlane(helix, zmax);
		double path = Math.max(minpath, maxpath);
		List<Double> pathlist = PathToCylinder(helix,r,path,10);
		for(double s:pathlist){
			double z = PointOnHelix(helix,s).z();
			if(z<zmax && z> zmin)
				return true;
		}
		return false;
	}
	/**
	 * return true if the helix is intercepting the given disk
	 * @param helix
	 * @param rmin
	 * @param rmax
	 * @param z
	 * @return
	 */
	public static boolean isInterceptingZDisk(HelicalTrackFit helix,double rmin,double rmax, double z ){
		double s = PathToZPlane(helix,z);
		Hep3Vector  point = PointOnHelix(helix,s);
		double x = point.x();
		double y = point.y();
		double r = Math.sqrt(x*x+y*y);
		if(r < rmax && r > rmin ){return true;}
		return false;
	}
  
	/**
	 * return true if the point on the helix at coord z is intercepting a given ZPolygon, the
	 * method don't check if the polygone is parallel to a z plane
     *
     * this method check if the private methode insidePolygone return a value a less than .5 deg from 2PI
     *
	 * @param helix
	 * @param z
	 * @param vertices
	 * @return true OR false wether the helix intercept or not the polygone
	 */
	public static boolean isInterceptingZpolygon(HelicalTrackFit helix,double z,List<Hep3Vector> vertices){
	//todo: check if all vertices are on same z
		double epsilon = Math.PI/360.;
		double s = PathToZPlane(helix,z);
		if(s<0){return false;}
		if(s>_maxpath){return false;}
		Hep3Vector point = PointOnHelix(helix,s);
		double angle = insidePolygon(point,vertices);
		if(Math.abs(angle-2*Math.PI)<epsilon)
			return true;
		return false;
	}



	/**
	 * Check if the given helix is intercepting XY plan
	 * note, the z coordinate of the XY plane should be 0, but due to numerical
	 * approximation, it accept all z value < _epsilon
	 * @param helix the Helix
	 * @param normal a unitary normal vector to the plan
	 * @param orig one point of the plane
	 * @return true if the helix intersect at least once with the plane
	 */
	public static boolean isInterceptingXYPlane(HelicalTrackFit helix, Hep3Vector normal,Hep3Vector orig){
		if(normal.z()>_epsilon)
			throw new UnsupportedOperationException("Not a XY plan !"+normal);
		double x = -helix.x0()+orig.x();
		double y = -helix.y0()+orig.y();
		double z = -helix.z0()+orig.z();
		double xn= normal.x();
		double yn= normal.y();
		double zn= normal.z();
		double Phip = FastMath.atan2(yn, xn);
		double dist = (xn*x+yn*y+zn*z);
		double verif = Math.sin(Phip-helix.phi0())+helix.curvature()*dist;
		if(normal.magnitude() < 0.99)
			throw new UnsupportedOperationException("normal vector not unitary :"+normal.magnitude());
		if(Math.abs(verif)>1)
			return false;
		else
			return true ;
	}

	/**
	 * Check if the given helix is intercepting an XY plane bouded by a
	 * list of nodes
	 * @param helix
	 * @param normal normal vector to the plane
	 * @param nodes
	 * @return true if th e helix intercept the bounded plane
	 **/
	public static boolean isInterceptingBoundedXYPlane(HelicalTrackFit helix,Hep3Vector normal,List<Hep3Vector> nodes){
		Hep3Vector orig = nodes.get(0);
		if(!isInterceptingXYPlane(helix,normal,orig)){return false;}
		double s = PathToXYPlan(helix, normal, orig);
		if(s<0)    {return false;}
		if(s>_maxpath){return false;}
		Hep3Vector point = PointOnHelix(helix, s);
		if(Math.abs(insidePolygon(point, nodes)-2*Math.PI)<_epsilon)
			return true;
		return false;
	}
	/**
	 * return one path length (projection on the XY plane) to an XY plane
	 * the methode is for now only used to check IF an helix is intercepting an
	 * XY plane
	 * note, the z coordinate of the XY plane should be 0, but due to numerical
	 * approximation, it accept all z value < _epsilon
	 * @param helix
	 * @param normal a UNITARY vector NORMAL to the plan
	 * @param origin one point of the plane
	 * @return path length
	 */
	public static double PathToXYPlan(HelicalTrackFit helix,Hep3Vector normal,Hep3Vector origin){
		if(normal.z()>_epsilon)
			throw new UnsupportedOperationException("Not a XY plan ! normal vector is "+normal);
		double x = -helix.x0()+origin.x();
		double y = -helix.y0()+origin.y();
		double z = -helix.z0()+origin.z();
		double xn= normal.x();
		double yn= normal.y();
		double zn= normal.z();
		double phip = FastMath.atan2(yn, xn);
		double dist = (xn*x+yn*y+zn*z);
		double sinPlan = Math.sin(phip-helix.phi0())+helix.curvature()*dist;
		double Cs = (Math.asin(sinPlan)-phip+helix.phi0());
		double s=dist/(Math.sqrt(xn*xn+yn*yn)*(2./Cs)*Math.sin(Cs/2.)*Math.cos(Cs/2+phip-helix.phi0())+zn*helix.slope());
		return s;
	}


	/**
	 * adapted from http://local.wasp.uwa.edu.au/~pbourke/geometry/insidepoly/
	 * find if a point is inside a given polygon in 3Dspace
	 * if the point is in the polygone the returned value is 2*PI
	 * if the point is outside of the polygone the returned value is 0
	 * if the point is not of the plan of the polygone the value is between 0 and 2PI
	 * The polygone HAVE to Be convex for the algorythme to work
	 *
	 * This fonction sum all the angle made by q and 2 consecutives point of the polygone
	 *
     * as this method is generic, it might be usefull to make it public,and mabe in another
     * file because it can be used for anything else than helix
     *
	 * @param q the point to determine if it is or not in the polygon
	 * @param p List of edges of the polygone
	 * @return 2PI if inside, 0 if outside,intermediate values if not on the plane
	 */
	private static double insidePolygon(Hep3Vector q,List<Hep3Vector> p)
	{
	   int i;
	   double epsilon = _epsilon;
	   double m1,m2;
	   double anglesum=0,costheta;
	   double x1,x2,y1,y2,z1,z2,m1m2;
       //might be usefull to check if all the p vector are on the same plane
       Hep3Vector p1;
	   Hep3Vector p2;
	   int n = p.size();
	   for (i=0;i<n;i++) {
		  p1 = p.get(i);
		  p2 = p.get((i+1)%n);  // the "last+1" vector is the first
		  x1 = (p1.x()-q.x());  // not optimal, operation can be made only once
		  y1 = (p1.y()-q.y());  //
		  z1 = (p1.z()-q.z());
		  x2 = (p2.x()-q.x());
		  y2 = (p2.y()-q.y());
		  z2 = (p2.z()-q.z());
		  m1m2 = Math.sqrt(x1*x1+y1*y1+z1*z1)*Math.sqrt(x2*x2+y2*y2+z2*z2);


		  if (m1m2 <= epsilon){
			  return 2*Math.PI; /* We are on a node, consider this inside */
		  }
		  else{
			 costheta = (x1*x2 + y1*y2 + z1*z2)/(m1m2);
			 anglesum += Math.acos(costheta);
		  }
	   }
	   return anglesum;
	}


    private static double RC(CircleFit cfit) {
        return 1.0 / cfit.curvature();
    }
    
    private static double xc(CircleFit cfit) {
        //Note that DCA for circle fit has opposite sign w.r.t. the standard L3 definition
        return cfit.xref() + (RC(cfit) - (-1*cfit.dca())) * Math.sin(cfit.phi());
        
    }
    
    private static double yc(CircleFit cfit) {
        //Note that DCA for circle fit has opposite sign w.r.t. the standard L3 definition
        return cfit.yref() - (RC(cfit) - (-1*cfit.dca())) * Math.cos(cfit.phi());
        
    }
    
    private static double x0(CircleFit cfit) {
        //Note that DCA for circle fit has opposite sign w.r.t. the standard L3 definition
        return cfit.xref() - (-1*cfit.dca()) * Math.sin(cfit.phi());
    }
    
    private static double y0(CircleFit cfit) {
        //Note that DCA for circle fit has opposite sign w.r.t. the standard L3 definition
        return cfit.yref() + (-1*cfit.dca()) * Math.cos(cfit.phi());
    }

}
