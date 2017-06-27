package org.lcsim.util.swim;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

import org.lcsim.spacegeom.CartesianPoint;
import org.lcsim.spacegeom.CartesianVector;
import org.lcsim.spacegeom.SpacePoint;
import org.lcsim.spacegeom.SpaceVector;

/**
 * A straight line
 * @author tonyj
 * @version $Id: Line.java,v 1.12 2007/08/16 21:46:45 jstrube Exp $
 */
public class Line implements Trajectory
{
   private double sinPhi;
   private double cosPhi;
   private double sinLambda;
   private double cosLambda;
   private Hep3Vector origin;
   
   /**
     * Defines a line in space. The direction of the line is defined
     * by the angles phi and lambda, such that the Cartesian unit vector is
     * (cos(lambda)*cos(phi), cos(lambda)*sin(phi), sin(lambda)).
     * @param origin A point on the line
     */
   public Line(Hep3Vector origin, double phi, double lambda)
   {
      this.origin = origin;
      sinPhi = Math.sin(phi);
      cosPhi = Math.cos(phi);
      sinLambda = Math.sin(lambda);
      cosLambda = Math.cos(lambda);
   }

   /**
     * Gets a point after traveling distance alpha from the trajectory's
     * origin point along the trajectory
     */
   public SpacePoint getPointAtDistance(double alpha)
   {
      double z = origin.z() + alpha*sinLambda;
      double dr = alpha*cosLambda;
      double x = origin.x() + dr*cosPhi;
      double y = origin.y() + dr*sinPhi;
      return new CartesianPoint(x,y,z);
   }

  /**
    * Calculates the distance at which the trajectory first reaches radius R.
    * Returns Double.NaN if the trajectory does not intercept the cylinder.
    * In principle there could be multiple solutions, so this method should
    * always return the minimum <b>positive</b> solution.
    */
   public double getDistanceToInfiniteCylinder(double r)
   {
       if (cosLambda == 0.0) {
           // This is a special case where the line is parallel to the z-axis.
           // There can be no well-defined answer in this case
           return Double.NaN;
       }
       // Find the intercepts on the cylinder (Cartesian):
       double[] distances = this.findInterceptsOnCylinder(r);
       // Which is the best?
       double bestDistance = -1.0;
       if (distances != null)
       {
           for (int i=0; i<distances.length; i++) {
               if (distances[i] >= 0.0) {
                   // Potentially valid -- is it the best so far?
                   if (bestDistance<0.0 || distances[i]<bestDistance) {
                       // Yes!
                       bestDistance = distances[i];
                   }
               }
           }
       }
       if (bestDistance < 0.0) {
           // There was no valid (positive) solution
           return Double.NaN;
       }
       return bestDistance;
   }

  /** 
    * Calculate the distance along the trajectory to reach a given Z plane.
    * Note distance may be negative.
    */
   public double getDistanceToZPlane(double z)
   {
      return (z-origin.z())/sinLambda;
   }
   
   /**
    * Obtain the point of closest approach (POCA)
    * of the line to a point. The return value is the distance
    * parameter s, such that getPointAtDistance(s) is the POCA.
    * @param point Point in space to swim to
    * @return The length parameter s
    */
   public double getDistanceToPoint(Hep3Vector point) {
       return findPOCAToPoint(point);
   }

   /**
    * An internal utility routine. This finds all intercepts of the
    * line on a infinite cylinder of radius r. The cylinder's axis
    * is the same as the z-axis. The return value is an array of
    * signed distances from the origin point to the intercept point.
    * If there are no solutions, the return value is null.
    */
   protected double[] findInterceptsOnCylinder(double r)
   {   
       if (cosLambda == 0.0) { 
           throw new AssertionError("Don't call this private routine with a line that's parallel to the z-axis!");
       }

       // Treat this first as a problem in the XY plane, then think about Z later.
       // Track is described as
       //    position(t) = (x0, y0) + t(vx, vy)
       // i.e.
       //    x = x0 + t(vx)
       //    y = y0 + t(vy) = y0 + ((x-x0)/vx)(vy)
       //                   = [y0 - x0.vy/vx] + [x.vy/vx]
       //                   = c + m.x
       // Circle is described as
       //    x*x + y*y = r*r
       // Solve for x and y:
       //    x*x + (c+mx)*(c+mx) = r*r
       //    x*x + c*c + 2*c*m*x + m*m*x*x = r*r
       //    (m*m + 1).x^2 + (2*c*m).x + (c*c - r*r) = 0
       // Thus, solving the quadratic,
       //    x = [ -(2*c*m) +- sqrt( (2*c*m)^2 - 4*(m*m+1)*(c*c-r*r) ) ] / [2*(m*m+1)]

       double x0 = origin.x();
       double y0 = origin.y();
       double vx = cosLambda*cosPhi;
       double vy = cosLambda*sinPhi;

       double c = y0 - (x0*vy)/vx;
       double m = vy/vx;
       
       if (vx == 0.0) {
           // This is a special case where the line is perpendicular to the x-axis.
           // Handle this by flipping the x and y coordinates.
           x0 = origin.y();
           y0 = origin.x();
           vx = cosLambda*sinPhi;
           vy = cosLambda*cosPhi;
           c  = y0 - (x0*vy)/vx;
           m  = vy/vx;
       }

       // Solve the quadratic...
       double termA = (m*m + 1.0);
       double termB = (2.0*c*m);
       double termC = (c*c - r*r);       
       double sqrtTerm = termB*termB - 4.0*termA*termC;

       double[] xSolutions = null; // This will hold the co-ordinates of the solutions

       if (sqrtTerm>0.0) {
           // Two solutions.
           xSolutions = new double[2];
           xSolutions[0] = ( -termB + Math.sqrt(sqrtTerm) ) / (2.0 * termA);
           xSolutions[1] = ( -termB - Math.sqrt(sqrtTerm) ) / (2.0 * termA);
       } else if (sqrtTerm==0.0) {
           // One solution
           xSolutions = new double[1];
           xSolutions[0] = ( -termB ) / (2.0 * termA);
       } else if (sqrtTerm < 0.0) {
           // No solutions
       }

       double[] distances = null; // The array of signed distances we'll return
       if (xSolutions == null) {
           // No solutions -- distances stays null
       } else {
           // Unit vector is (cosLambda*cosPhi, cosLambda*sinPhi, sinLambda), so
           // we can calculate the distances easily...
           distances = new double[xSolutions.length];
           for (int i=0; i<distances.length; i++) {
               // Calculate in a way that remains correct even if we flipped x and y
               // co-ordinates:
               distances[i] = (xSolutions[i] - x0) / vx;
           }
       }
       
       return distances;
   }

    /**
     * An internal utility routine to find the distance of closest
     * approach (DOCA) of the line to a point. This is a simple 2D
     * vector calculation:
     *   * Let the displacement vector from the origin to point be d.
     *   * Decompose d into a component parallel to the line (d_parallel)
     *     and a component perpendicular to the line (d_perp):
     *      d = d_parallel + d_perp
     *   * Take the dot product with the unit vector v along the line to
     *     obtain and use the parallel/perpendicular properties to obtain:
     *        |d.v| = |d_parallel|
     *   * Hence the DOCA, |d_perp|, is given by
     *        |d_perp|^2 = |d|^2 - |d_parallel|^2
     *                   = |d|^2 - |d.v|^2
     */
    protected double findDOCAToPoint(Hep3Vector point) 
    {
	// The first line is kind of ugly.
	Hep3Vector displacement = new BasicHep3Vector(point.x() - origin.x(), point.y() - origin.y(), point.z() - origin.z());
	Hep3Vector lineDirection = this.getUnitVector();
	double dotProduct = VecOp.dot(displacement, lineDirection);
	double doca = Math.sqrt(displacement.magnitudeSquared() - dotProduct*dotProduct);
	return doca;
    }

    /**
     * An internal utility routine to obtain the point of closest approach
     * (POCA) of the line to a point. The return value is the distance
     * parameter s, such that getPointAtDistance(s) is the POCA.
     * The vector calculation is as follows:
     *   Let the origin point be x and the unit vector along the line be v.
     *   Let the point we're trying to find the POCA to be p.
     *   Suppose the POCA is at
     *     x' = x + sv
     *   such that the scalar distance parameter we want is s.
     *   Then the displacement vector from the POCA to p is
     *     d = x' - p
     *   But x' is the POCA, so d is perpendicular to v:
     *     0 = d.v
     *       = (x' - p).v
     *       = (x + sv - p).v
     *   So
     *     (x-p).v + sv.v = 0
     *   But v is a unit vector, so
     *     s = (p-x).v
     */
    protected double findPOCAToPoint(Hep3Vector point) 
    {
	// Find (p-x)
	Hep3Vector originToPoint = new BasicHep3Vector(point.x()-origin.x(), point.y() - origin.y(), point.z() - origin.z());
	Hep3Vector lineDirection = this.getUnitVector();
	double dotProduct = VecOp.dot(originToPoint, lineDirection);
	return dotProduct;
    }

    /**
     * Internal utility routine to return the unit vector of the
     * line's direction.
     */
    protected Hep3Vector getUnitVector()
    {
	Hep3Vector lineDirection = new BasicHep3Vector(cosLambda*cosPhi, cosLambda*sinPhi, sinLambda);
	return lineDirection;
    }

    /**
     * Return the direction of the line. Marked Deprecated since this should
     * really be replaced by a general method for the Trajectory interface,
     * perhaps getDirection(double alpha) for distance parameter alpha.
     */
    @Deprecated public Hep3Vector getDirection()
    {
        return this.getUnitVector();
    }

    /**
     * Routine to find where two lines meet.
     * This will probably be replaced by a more general vertexing solution
     * at some point, but for now it works.
     */
    @Deprecated static public double[] getPOCAOfLines(Line line1, Line line2)
    {
	double[] output = line1.findTrackToTrackPOCA(line2);
	return output;
    }

    /**
     * Internal utility routine to find where two lines meet.
     * This will probably be replaced by a more general vertexing solution
     * at some point, but for now it works.
     */
    @Deprecated private double[] findTrackToTrackPOCA(Line otherLine)
    {
	Line line1 = this;
	Line line2 = otherLine;

	// Unit vectors of the two lines:
	Hep3Vector v1 = line1.getUnitVector();
	Hep3Vector v2 = line2.getUnitVector();
	// Origin points on the two lines:
	Hep3Vector x1 = line1.origin;
	Hep3Vector x2 = line2.origin;

        // Find the common perpendicular:
	Hep3Vector perp = VecOp.cross(v1, v2);
	if (perp.magnitude() == 0.0) {
	    // Lines are parallel!
            return null;
	} else {
	    // Let the origin point along the lines be x1 and x2.
	    // Let the unit vectors along the lines be v1 and v2.
            // Suppose that the points of closest approach are at:
            //    x1' = x1 + a(v1)
            //    x2' = x2 + b(v2)
            // Construct a vector p1 which is perpendicular to perp and to v1:
            //    p1 = v1 x perp = v1 x (v1 x v2) = v1(v1.v2) - v2(v1.v1)
            //    p2 = v2 x perp = v2 x (v1 x v2) = v1(v2.v2) - v2(v1.v2)
            // Then v1.p1 = (v1.v1)(v1.v2) - (v1.v2)(v1.v1) = 0
            //      v1.p2 = (v1.v1)(v2.v2) - (v1.v2)(v1.v2) = L
            //      v2.p1 = (v2.v1)(v1.v2) - (v2.v2)(v1.v1) = -L
            //      v2.p2 = (v2.v1)(v2.v2) - (v2.v2)(v1.v2) = 0
            // Thus,
            //    (x2'-x1').p1 = (x2 + b(v2) - x1 -a(v1)).p1
            //                 = x2.p1 + b(v2.p1) - x1.p1 - a(v1.p1)
            //                 = x2.p1 - bL - x1.p1
            //  and similarly,
            //    (x2'-x1').p2 = x2.p2 -x1.p2 - aL
            //  But p1 and p2 are perpendicular to perp, and (x2'-x1') is parallel
            //  to perp for x1' and x2' the closest points. So
            //    (x2'-x1').p1 = x2.p1 - bL - x1.p1 = 0
            //    (x2'-x1').p2 = x2.p2 - x1.p2 - aL = 0
            //  Hence,
            //    -bL = -x2.p1 + x1.p1 => b = (x2-x1).p1 / L
            //    -aL = -x2.p2 + x1.p2 => a = (x2-x1).p2 / L
            //  and so the POCA is at
            //    (x1' + x2')/2 = (x1 + a(v1) + x2 + b(v2)) / 2
            //                  = (x1 + [(x2-x1).p2/L]v1 + x2 + [(x2-x1).p1/L]v2) / 2

	    // p1 = v1 x perp  (and similarly p2)
	    Hep3Vector p1 = VecOp.cross(v1, perp);
	    Hep3Vector p2 = VecOp.cross(v2, perp);

	    // L = (v1.v1)(v2.v2) - (v1.v2)(v1.v2) = 1 - (v1.v2)^2
	    double L = 1.0 - (VecOp.dot(v1,v2) * VecOp.dot(v1,v2));

	    // a = (x2-x1).p2 / L
	    // b = (x2-x1).p1 / L
	    double a = VecOp.dot(VecOp.sub(x2,x1), p2) / L;
	    double b = VecOp.dot(VecOp.sub(x2,x1), p1) / L;

	    double[] output = new double[2];
	    output[0] = a;
	    output[1] = b;
	    return output;
	}
    }

   /**
     * Defines a line in space. The line is defined by a point and a
     * direction vector. The magnitude of the direction vector is ignored.
     * @param origin A point on the line
     * @param dir The signed direction of the line
     */
    public Line(Hep3Vector origin, Hep3Vector dir)
    {
	// The easy bit: 
	this.origin = origin;

	// The hard bit: find the direction as (cosPhi, sinPhi, cosLambda, sinLambda)
        double normalization = dir.magnitude();
        Hep3Vector dirNormalized = hep.physics.vec.VecOp.unit(dir);

	// cosPhi, sinPhi, sinLambda are fairly easy.
	// It helps that the signed unit vector is (cos(lambda)*cos(phi), cos(lambda)*sin(phi), sin(lambda)).
        double phi = hep.physics.vec.VecOp.phi(dirNormalized);
	cosPhi = Math.cos(phi);
	sinPhi = Math.sin(phi);
	sinLambda = dirNormalized.z();

	// cosLambda is harder. First find the absolute value:
        double cosLambdaSquared = (dirNormalized.x()*dirNormalized.x() + dirNormalized.y()*dirNormalized.y());
	if (cosLambdaSquared == 0.0) {
	    // Easy case
            cosLambda = 0.0;
	} else {
	    // Need to tease it apart using either X or Y. Choose the one with the bigger lever arm...
	    boolean useX = (Math.abs(dir.x()) > Math.abs(dir.y()));

	    // We will use one of the relations:
	    //    vx = cosLambda * cosPhi
	    //    vy = cosLambda * sinPhi
	    // where the unit direction vector is (vx, vy, vz)
            if (useX) {
                cosLambda = dirNormalized.x() / cosPhi;
            } else {
                cosLambda = dirNormalized.y() / sinPhi;
            }
        }
    }
    
    
    /**
     * Calculates the <em>unit vector</em> of the momentum at a certain distance from the origin.
     * Since the momentum is constant for a straight line,
     * @param alpha is ignored
     * @return The unit direction of the line, since there is now geometric way to obtain the momentum along a straight line.
     */
    public SpaceVector getUnitTangentAtLength(double alpha) {
        SpaceVector lineDirection = new CartesianVector(cosLambda * cosPhi, cosLambda * sinPhi, sinLambda);
        return lineDirection;
    }
}
