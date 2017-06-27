package org.lcsim.util.swim;

import static java.lang.Math.abs;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.signum;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

import org.lcsim.spacegeom.CartesianPoint;
import org.lcsim.spacegeom.CartesianVector;
import org.lcsim.spacegeom.SpacePoint;
import org.lcsim.spacegeom.SpaceVector;

/**
 * This class represents a helix with its axis aligned along Z. All quantities
 * in this class are dimensionless. It has no dependencies except for Hep3Vector
 * (which could easily be removed).
 * <p>
 * For more info on swimming see <a href="doc-files/transport.pdf">this paper</a>
 * by Paul Avery.
 * 
 * @author tonyj
 * @version $Id: Helix.java,v 1.30 2010/04/29 13:41:37 cassell Exp $
 */
public class Helix implements Trajectory {
    /**
     * Creates a new instance of Helix. Parameters according to <a
     * href="doc-files/L3_helix.pdf">the L3 conventions</a><br />
     * Please also have a look at <img src="doc-files/Helix-1.png"
     * alt="Helix-1.png"> <img src="doc-files/Helix-2.png" alt="Helix-2.png">
     * 
     * @param origin
     *                A point on the helix
     * @param radius
     *                The <em>signed</em> radius of curvature of the helix.
     *                The conventions is such that for <em>positive</em>
     *                radii, the direction is <em>clockwise</em>.
     * @param phi
     *                The polar angle of the helix <em>momentum</em> in x-y
     *                plane w/rt positive x-axis at the origin
     * @param lambda
     *                The dip angle w/rt positive part of the x-y plane
     */
    public Helix(Hep3Vector org, double r, double p, double lambda) {
        // if (abs(lambda) > PI/2.)
        // throw new IllegalArgumentException("lambda = " + lambda + " is
        // outside of -PI/2<lambda<PI/2");
        origin = org;
        radius = r;
        phi = p;

        // Calculate some useful quantities
        cosPhi = cos(phi);
        sinPhi = sin(phi);
        cosLambda = cos(lambda);
        sinLambda = sin(lambda);
        xCenter = origin.x() + radius * sinPhi;
        yCenter = origin.y() - radius * cosPhi;
        setSpatialParameters();
    }

    /**
     * returns a point on the Helix at a distance alpha from the origin along
     * the trajectory. alpha == 2*PI*radius/cos(lambda) is one rotation in the
     * x-y plane
     */
    public SpacePoint getPointAtDistance(double alpha) {
        double darg = alpha * cosLambda / radius - phi;
        double x = xCenter + radius * sin(darg);
        double y = yCenter + radius * cos(darg);
        double z = origin.z() + alpha * sinLambda;
        return new CartesianPoint(x, y, z);
    }

    public double getRadius() {
        return radius;
    }

    public Hep3Vector getCenterXY() {
        return new BasicHep3Vector(xCenter, yCenter, 0);
    }

    public double getDistanceToZPlane(double z) {
        return (z - origin.z()) / sinLambda;
    }

    /**
     * Calculates the distance along the Helix until it hits a cylinder centered
     * along z
     * 
     * @param r
     *                the radius of the cylinder
     * @return the distance along the trajectory or Double.NAN if the cylinder
     *         can never be hit
     */
    public double getDistanceToInfiniteCylinder(double r) {
        double phiToCenter = atan2(yCenter, xCenter);
        double radiusOfCenter = sqrt(xCenter * xCenter + yCenter * yCenter);
        // Negative radius doesn't make sense
        if (r < 0)
            throw new IllegalArgumentException("radius " + r + "<0");
        double darg = r * r / (2. * radius * radiusOfCenter) - radiusOfCenter
                / (2. * radius) - radius / (2. * radiusOfCenter);
        double deltaphi = phi - phiToCenter;
        if (deltaphi < -Math.PI)
            deltaphi += 2. * Math.PI;
        if (deltaphi > Math.PI)
            deltaphi -= 2. * Math.PI;
        double diff = asin(darg) + deltaphi;
        double result = (radius / cosLambda) * diff;
        while (result < 0)
            result += Math.abs(radius / cosLambda) * 2 * Math.PI;
        return result;
    }
    public double getDistanceToPolyhedra(double r, int nsides)
    {
        double mins = 9999999.;
        double period = Math.abs(2.*Math.PI*radius/cosLambda);
        for(int i=0;i<nsides;i++)
        {
            double dphi = i*2.*Math.PI/nsides;
            double beta = (r - Math.cos(dphi)*xCenter - Math.sin(dphi)*yCenter)/radius;
            if(Math.abs(beta) <= 1.)
            {
                double s1 = radius/cosLambda*(Math.asin(beta) - dphi + phi);
                double s2 = radius/cosLambda*(Math.PI - Math.asin(beta) - dphi + phi);
                while(s1 < 0.){s1 += period;}
                while(s2 < 0.){s2 += period;}
                s1 = s1%period;
                s2 = s2%period;
                double s = Math.min(s1,s2);
                if(s1 < mins)mins = s1;
            }
        }
        if(mins == 9999999.)return Double.NaN;
        return mins;
    }

    public double getDistanceToPoint(Hep3Vector point) {

        // Set starting position and direction unit vector
        Hep3Vector pos = new BasicHep3Vector(origin.v());
        Hep3Vector u = VecOp.unit(new BasicHep3Vector(px, py, pz));
        Hep3Vector zhat = new BasicHep3Vector(0., 0., 1.);

        //  First estimate distance using z coordinate of the point
        double s = 0.;
        if(Math.abs(sinLambda) > .00001)s = getDistanceToZPlane(point.z());
        double stot = s;

        //  Propagate to the estimated point
        Hep3Vector unew = propagateDirection(u, s);
        Hep3Vector posnew = propagatePosition(pos, u, s);
        u = unew;
        pos = posnew;
        
        //  Calculate how far we are from the desired point
        Hep3Vector delta = VecOp.sub(pos, point);

        int count = 0;
        int maxcount = 20;
        //  Iteratively close in on the point of closest approach
        while (delta.magnitude() > eps) {
            count++;
            //  Calculate the coefficients of the indicial equations a*s^2 + b*s + c = 0
            double c = VecOp.dot(u, delta);
            double b = 1. - rho * VecOp.dot(VecOp.cross(u, zhat), delta);
            double a = -0.5 * rho*rho * (c - delta.z()*u.z());

            //  Find the two solutions
            double arg = b*b - 4 * a * c;
            double s1 = (-b + Math.sqrt(arg)) / (2. * a);
            double s2 = (-b - Math.sqrt(arg)) / (2. * a);

            //  Find the position and distance from desired point for both solutions
            Hep3Vector pos1 = propagatePosition(pos, u, s1);
            double d1 = VecOp.sub(pos1, point).magnitude();
            Hep3Vector pos2 = propagatePosition(pos, u, s2);
            double d2 = VecOp.sub(pos2, point).magnitude();

            //  Pick the closest solution and update the position, direction unit vector, and
            //  path length.  If the change in position is small, we have converged.
            if (d1 < d2) {
                unew = propagateDirection(u, s1);
                u = unew;
                pos = pos1;
                stot += s1;
                if (Math.abs(s1) < eps) return stot;
            } else {
                unew = propagateDirection(u, s2);
                u = unew;
                pos = pos2;
                stot += s2;
                if (Math.abs(s2) < eps) return stot;
            }

            if(count > maxcount)return stot;
            //  Update how far we are from the specified point
            delta = VecOp.sub(pos, point);
        }

        //  If we get here, we found a point within the desired precision
        return stot;
    }

    /**
     * Swims the helix along its trajectory to the point of closest approach to
     * the given SpacePoint. For more info on swimming see <a
     * href=doc-files/fitting/transport.pdf> Paul Avery's excellent text</a>
     * 
     * @param point
     *                Point in Space to swim to.
     * @return the length Parameter alpha
     */
    public double getDistanceToXYPosition(Hep3Vector point) {
        double tanLambda = sinLambda / cosLambda;
        double pMag = sqrt(px * px + py * py + pz * pz);
        Hep3Vector p0 = new BasicHep3Vector(px, py, pz);
        Hep3Vector pCrossB = new BasicHep3Vector(py, -px, 0);

        // first, the point needs to be translated into the first period
        Hep3Vector xDiff = VecOp.sub(origin, point);
        int addedQuarterPeriods = 0;
        if (abs(tanLambda) > 1e-10) {
            double zPos = xDiff.z();
            while (abs(zPos) > abs(radius * tanLambda * Math.PI)) {
                zPos -= signum(zPos) * abs(radius * tanLambda * Math.PI);
                ++addedQuarterPeriods;
            }
            // Make sure the helix is in the right quadrant for the atan
            if (zPos > 0 && addedQuarterPeriods > 0)
                addedQuarterPeriods *= -1;
            if (addedQuarterPeriods % 2 != 0)
                addedQuarterPeriods += signum(addedQuarterPeriods);
            xDiff = new BasicHep3Vector(xDiff.x(), xDiff.y(), zPos);
        }
        double factorA1 = pMag - pz * pz / pMag - (VecOp.dot(xDiff, pCrossB))
                * rho;
        double factorA2 = (VecOp.dot(xDiff, p0) - xDiff.z() * pz) * rho;
        // System.err.print("addedQuarterPeriods: " + addedQuarterPeriods);
        // System.err.printf("result:%.3f + %.3f\n",
        // addedQuarterPeriods*(radius/cosLambda*Math.PI/2),
        // Math.atan(factorA2/factorA1) / -rho);
        return addedQuarterPeriods * abs(radius / cosLambda * Math.PI)
                - Math.atan2(factorA2, factorA1) / rho;
    }

    /**
     * Calculates the <em>signed</em> distance in mm between the Helix and an
     * arbitrary point in Space
     * 
     * @param point
     *                the point in space to calculate the distance to
     * @return the distance in mm between the point and the helix at the point
     *         of closest approach
     */
    public double getSignedClosestDifferenceToPoint(Hep3Vector point) {
        double tanLambda = sinLambda / cosLambda;
        Hep3Vector pCrossB = new BasicHep3Vector(py, -px, 0);
        Hep3Vector xDiff = VecOp.sub(origin, point);
        double pMag = sqrt(px * px + py * py + pz * pz);

        // translate along Z because algorithm can handle only numbers in the
        // first quadrant
        double zPos = xDiff.z();
        zPos = Math.IEEEremainder(zPos, abs(radius * tanLambda * Math.PI / 4));
        if (zPos < 0) zPos += abs(radius * tanLambda * Math.PI / 4);

        if (zPos/abs(radius * tanLambda * Math.PI / 4) < 0 || zPos/abs(radius * tanLambda * Math.PI / 4) > 1)
        {
            System.out.println("Valid range of zPos/abs(radius*tanLambda*Math.PI/4) [0 and 1], value is: "+zPos/abs(radius * tanLambda * Math.PI / 4));
        }
        
        xDiff = new BasicHep3Vector(xDiff.x(), xDiff.y(), zPos);

        double numerator = (-2 * VecOp.dot(xDiff, pCrossB) + pMag * rho
                * (xDiff.magnitudeSquared() - xDiff.z() * xDiff.z()))
                / radius;
        double denominator = 1 + sqrt(1 - 2 * rho * pMag
                * VecOp.dot(xDiff, pCrossB) / radius / radius + pMag * pMag
                * rho * rho
                * (xDiff.magnitudeSquared() - xDiff.z() * xDiff.z()) / radius
                / radius);
        return numerator / denominator;
    }

    // Returns the "momentum" at the length s from the starting point.
    // This uses the definition in
    // http://www.phys.ufl.edu/~avery/fitting/transport.pdf
    public Hep3Vector getTangentAtDistance(double alpha) {
        double p0x = px * cos(rho * alpha) - py * sin(rho * alpha);
        double p0y = py * cos(rho * alpha) + px * sin(rho * alpha);
        double p0z = pz;
        return new BasicHep3Vector(p0x, p0y, p0z);
    }

    // added by Nick Sinev
    public double getSecondDistanceToInfiniteCylinder(double r) {
        double result = getDistanceToInfiniteCylinder(r);
        SpacePoint first = getPointAtDistance(result);
        double angto0 = Math.atan2(-yCenter, -xCenter);
        double angtofirst = Math
                .atan2(first.y() - yCenter, first.x() - xCenter);
        double dang = angtofirst - angto0;
        if (dang < -Math.PI)
            dang += 2. * Math.PI;
        if (dang > Math.PI)
            dang -= 2. * Math.PI;
        double angofarc = 2. * (Math.PI - Math.abs(dang));
        double arc = Math.abs(radius) * angofarc / cosLambda;
        return result + arc;
    }

    public double getZPeriod() {
        return Math.PI * radius * 2. * sinLambda / cosLambda;
    }

    /**
     * Sets the parameterization in terms of "momentum" and charge
     * 
     */
    private void setSpatialParameters() {
        abs_r = abs(radius);
        px = abs_r * cosPhi;
        py = abs_r * sinPhi;
        pz = abs_r * sinLambda / cosLambda;
        rho = -cosLambda / radius;
    }

    /**
     * Propagates the direction unit vector a distance s along the helix
     * 
     * @param u initial direction unit vector
     * @param s distance to propagate
     * @return propagated direction unit vector
     */
    private Hep3Vector propagateDirection(Hep3Vector u, double s) {
        double ux = u.x();
        double uy = u.y();
        double uz = u.z();
        double carg = Math.cos(rho * s);
        double sarg = Math.sin(rho * s);
        double uxnew = ux * carg -uy * sarg;
        double uynew = uy * carg + ux * sarg;
        double uznew = uz;
        return new BasicHep3Vector (uxnew, uynew, uznew);
    }

    /**
     * Propagate a point on the helix by a specified distance
     *
     * @param pos starting position
     * @param u starting direction unit vector
     * @param s distance to propagate
     * @return propagated point on helix
     */
    private Hep3Vector propagatePosition(Hep3Vector pos, Hep3Vector u, double s) {
        double x = pos.x();
        double y = pos.y();
        double z = pos.z();
        double ux = u.x();
        double uy = u.y();
        double uz = u.z();
        double carg = Math.cos(rho * s);
        double sarg = Math.sin(rho * s);
        double xnew = x + ux * sarg / rho - uy * (1 - carg) / rho;
        double ynew = y + uy * sarg / rho + ux * (1 - carg) / rho;
        double znew = z + uz * s;
        return new BasicHep3Vector(xnew, ynew, znew);
    }

    /**
     * @param alpha
     *                The distance along the trajectory in the x-y plane
     * @return The unit vector of the momentum
     */
    public SpaceVector getUnitTangentAtLength(double alpha) {
        double angle = phi + alpha * rho;
        return new CartesianVector(cos(angle), sin(angle), sinLambda
                / cosLambda);
    }

    Hep3Vector origin;
    double xCenter;
    double yCenter;
    protected double radius;
    protected double sinLambda;
    protected double cosLambda;
    protected double sinPhi;
    protected double cosPhi;
    protected double phi;

    // parameterization in terms of 'momentum'
    // A helix is a mathematical object and doesn't have "momentum",
    // but unfortunately some of the used algorithms are expressed in terms of
    // it.
    // That's OK, it's a private variable.
    private double px;
    private double py;
    private double pz;
    private double abs_r;
    private double rho;

    //  Set the desired precision in finding the point closest to the track
    private double eps = 1.e-6;  //  1 nm ought to be good enough for government work!
    public void setExtrapToPointPrecision(double prec){eps = prec;}
}
