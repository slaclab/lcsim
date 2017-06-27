package org.lcsim.util.swim;

import hep.physics.vec.Hep3Vector;

import org.lcsim.event.LCIOParameters;
import org.lcsim.event.Track;
import org.lcsim.spacegeom.CartesianPoint;
import org.lcsim.spacegeom.CartesianVector;
import org.lcsim.spacegeom.SpacePoint;
import org.lcsim.spacegeom.SpaceVector;

import static java.lang.Math.atan;
import static java.lang.Math.sqrt;
import static org.lcsim.constants.Constants.fieldConversion;
import static org.lcsim.event.LCIOParameters.ParameterName.phi0;
import static org.lcsim.event.LCIOParameters.ParameterName.omega;
import static org.lcsim.event.LCIOParameters.ParameterName.tanLambda;

/**
 * A simple helix swimmer for use in org.lcsim. Uses standard lcsim units Tesla,
 * mm, GeV. This swimmer works for charged and neutral tracks.
 * <p>
 * For more info on swimming see <a href="doc-files/transport.pdf">this paper</a>
 * by Paul Avery.
 * 
 * @author jstrube
 * @version $Id: HelixSwimmer.java,v 1.22 2011/04/17 14:22:13 grefe Exp $
 */
public class HelixSwimmer {
    protected double field;
    protected Trajectory _trajectory;
    protected SpaceVector _momentum;
    protected double _charge;

    /**
     * Creates a new instance of HelixSwimmer
     * 
     * @param B
     *                field strength in Tesla; uniform, solenoidal, directed
     *                along z-axis
     */
    public HelixSwimmer(double B) {
        field = B * fieldConversion;
    }

    /**
     * Sets parameters for helix swimmmer.
     * 
     * @param p
     *                3-momentum (px,py,pz)
     * @param r0
     *                initial position(x0,y0,z0)
     * @param iq
     *                charge iq = q/|e| = +1/0/-1
     */
    public void setTrack(Hep3Vector p0, SpacePoint r0, int iq) {
        SpaceVector p = new CartesianVector(p0.v());
        double phi = Math.atan2(p.y(), p.x());
        double lambda = Math.atan2(p.z(), p.rxy());

        if (iq != 0 && field != 0) {
            double radius = p.rxy() / (iq * field);
            _trajectory = new Helix(r0, radius, phi, lambda);
        } else {
            _trajectory = new Line(r0, phi, lambda);
        }
        _momentum = p;
        _charge = iq;
    }
    
    /**
     * Sets parameters for helix swimmmer.
     * 
     * @param p
     *                3-momentum (px,py,pz)
     * @param r0
     *                initial position(x0,y0,z0)
     * @param q
     *                charge q = q/|e| = +1/0/-1
     */
    public void setTrack(Hep3Vector p0, SpacePoint r0, double q) {
        SpaceVector p = new CartesianVector(p0.v());
        double phi = Math.atan2(p.y(), p.x());
        double lambda = Math.atan2(p.z(), p.rxy());

        if (q != 0 && field != 0) {
            double radius = p.rxy() / (q * field);
            _trajectory = new Helix(r0, radius, phi, lambda);
        } else {
            _trajectory = new Line(r0, phi, lambda);
        }
        _momentum = p;
        _charge = q;
    }

    /**
     * Sets parameters for helix swimmmer.
     * 
     * @param p
     *                3-momentum (px,py,pz)
     * @param r0
     *                initial position(x0,y0,z0)
     * @param iq
     *                charge iq = q/|e| = +1/0/-1
     * @deprecated in favor of
     *             {@link setTrack(SpaceVector p, SpacePoint r0, int iq)}
     *             because this method has an ambiguous signature
     */
    @Deprecated
    public void setTrack(Hep3Vector p, Hep3Vector r0, int iq) {
        double pt = sqrt(p.x() * p.x() + p.y() * p.y());
        double phi = Math.atan2(p.y(), p.x());
        double lambda = Math.atan2(p.z(), pt);

        if (iq != 0 && field != 0) {
            double radius = pt / (iq * field);
            _trajectory = new Helix(r0, radius, phi, lambda);
        } else {
            _trajectory = new Line(r0, phi, lambda);
        }
        _momentum = new CartesianVector(p.v());
        _charge = iq;
    }

    /**
     * Sets the parameters for the helix swimmer. Uses the LCIOParameters class
     * for conversion between track parameters and space and momentum
     * representation
     * 
     * @param t
     *                The track to approximate with a helix
     */
    public void setTrack(Track t) {
        double pt = sqrt(t.getPX() * t.getPX() + t.getPY() * t.getPY());
        LCIOParameters parameters = new LCIOParameters(t.getTrackParameters(), pt);

        SpacePoint ref = new CartesianPoint(t.getReferencePoint());
        SpacePoint origin = LCIOParameters.Parameters2Position(parameters, ref);
        _trajectory = new Helix(origin, 1 / parameters.get(omega), parameters
                .get(phi0), atan(parameters.get(tanLambda)));
        // Hep3Vectors have too many shortcomings
        _momentum = new CartesianVector(LCIOParameters.Parameters2Momentum(parameters).v());
    }

    /**
     * 
     * @param alpha
     * @return a {@link SpacePoint} at the length alpha from the origin along
     *         the track
     * @deprecated in favor of {@link getPointAtLength}
     */
    @Deprecated
    public SpacePoint getPointAtDistance(double alpha) {
        return getPointAtLength(alpha);
    }

    /**
     * Returns a SpacePoint at the length alpha from the origin along the track
     * 
     * @param alpha
     * @return a {@link SpacePoint} at the length alpha from the origin along
     *         the track
     */
    public SpacePoint getPointAtLength(double alpha) {
        if (_trajectory == null) {
            throw new RuntimeException("Trajectory not set");
        }
        return _trajectory.getPointAtDistance(alpha);
    }

    public double getDistanceToRadius(double r) {
        if (_trajectory == null) {
            throw new RuntimeException("Trajectory not set");
        }
        return _trajectory.getDistanceToInfiniteCylinder(r);
    }
    public double getDistanceToPolyhedra(double r, int nsides)
    {
        if (_trajectory == null) {
            throw new RuntimeException("Trajectory not set");
        }
        double s = _trajectory.getDistanceToInfiniteCylinder(r);
        if(Double.isNaN(s))return s;
        return ((Helix) (_trajectory)).getDistanceToPolyhedra(r,nsides);
    }

    public double getDistanceToZ(double z) {
        if (_trajectory == null) {
            throw new RuntimeException("Trajectory not set");
        }
        double result = _trajectory.getDistanceToZPlane(z);
        if (result < 0) {
            result = _trajectory.getDistanceToZPlane(-z);
        }
        return result;
    }

    public double getDistanceToCylinder(double r, double z) {
        double x1 = getDistanceToRadius(r);
        double x2 = getDistanceToZ(z);
        return Double.isNaN(x1) ? x2 : Math.min(x1, x2);
    }

    /**
     * Returns the distance along the trajectory to get to the point of closest
     * approach
     * 
     * @param point
     *                The point to swim as close as possible to
     * @return the length parameter by how much the trajectory has to be swum
     */
    public double getTrackLengthToPoint(Hep3Vector point) {
        return _trajectory.getDistanceToPoint(point);
    }

    public double getDistanceToPoint(Hep3Vector point) {
        return getTrackLengthToPoint(point);
    }
    /**
     * Returns the momentum on a point on the track at a distance from the
     * origin
     * 
     * @param alpha
     *                The 2D distance from the origin along the track
     * @return The components of the momentum in a SpacePoint
     */
    public SpaceVector getMomentumAtLength(double alpha) {
        // the trajectory can only return the unit direction of the momentum
        SpaceVector unitDirection = _trajectory.getUnitTangentAtLength(alpha);
        double magnitude = _momentum.rxy();
        // System.out.println("HelixSwimmer: momentum.magnitude= "+magnitude);
        return VectorArithmetic.multiply(unitDirection, magnitude);
    }

    public Trajectory getTrajectory() {
        return _trajectory;
    }
}
