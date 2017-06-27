package org.lcsim.util.swim;

import org.lcsim.spacegeom.SpacePoint;
import org.lcsim.spacegeom.SpaceVector;

import hep.physics.vec.Hep3Vector;

/**
 * A particle trajectory (either a Helix or a Line)
 * @author tonyj
 * @version $Id: Trajectory.java,v 1.8 2007/08/16 21:46:45 jstrube Exp $
 */
public interface Trajectory
{
   /**
    * Gets a point after traveling distance alpha from the origin along the trajectory
    */
   SpacePoint getPointAtDistance(double alpha);
  /**
    * Calculates the distance at which the trajectory first reaches radius R.
    * Returns Double.NaN if the trajectory does not intercept the cylinder.
    * In principle there could be multiple solutions, so this method should
    * always return the minimum <b>positive</b> solution.
    */
   
   public double getDistanceToInfiniteCylinder(double r);
  /** 
    * Calculate the distance along the trajectory to reach a given Z plane.
    * Note distance may be negative.
    */
   public double getDistanceToZPlane(double z);
   
  /**
    * Obtain the point of closest approach (POCA)
    * of the trajectory to a point. The return value is the distance
    * parameter s, such that getPointAtDistance(s) is the POCA.
    * @param point Point in space to swim to
    * @return The length parameter s
    */
   // FIXME this should be a point rather than a vector
   public double getDistanceToPoint(Hep3Vector point);
   
   /**
    * Returns the momentum at a given distance from the origin
    * @param alpha The length along the trajectory from the origin
    * @return The momentum at the given distance
    */
   public SpaceVector getUnitTangentAtLength(double alpha);
}
