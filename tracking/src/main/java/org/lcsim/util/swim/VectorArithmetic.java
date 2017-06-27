package org.lcsim.util.swim;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

// import org.lcsim.spacegeom.LorentzVector;
import org.lcsim.spacegeom.CartesianVector;
import org.lcsim.spacegeom.SpaceVector;
import org.lcsim.spacegeom.CartesianPoint;
import org.lcsim.spacegeom.SpacePath;
import org.lcsim.spacegeom.SpacePoint;
import org.lcsim.spacegeom.SpacePointVector;
import static java.lang.Math.sqrt;

/**
 * Class to perform basic vector arithmetic on Hep3Vectors
 * @author jstrube
 * @version $Id: VectorArithmetic.java,v 1.16 2007/11/29 02:25:58 jstrube Exp $
 */
final public class VectorArithmetic {

    /**
     * private constructor ensures it's never instantiated
     */
    private VectorArithmetic() {
    }

    /**
     * Calculates the inner product of two Hep3Vectors
     * @param a Hep3Vector 1
     * @param b Hep3Vector 2
     * @return the inner (dot) product.
     */
    public static double dot(Hep3Vector a, Hep3Vector b) {
        return a.x() * b.x() + a.y() * b.y() + a.z() * b.z();
    }

    public static double dot(double[] a, double[] b) {
        if (a.length != b.length)
            throw new IllegalArgumentException("dimensions don't match");
        double result = 0;
        for (int i=0; i<a.length; i++)
            result += a[i]*b[i];
        return result;
    }

    public static Hep3Vector cross(Hep3Vector a, Hep3Vector b) {
        double x = a.y() * b.z() - a.z() * b.y();
        double y = a.z() * b.x() - a.y() * b.x();
        double z = a.x() * b.y() - a.y() * b.x();
        return new BasicHep3Vector(x, y, z);
    }

    // inefficient implementation doesn't matter because of deprecation
    @Deprecated public static Hep3Vector cross(Hep3Vector a, double[] b) {
        return cross(a, new BasicHep3Vector(b[0], b[1], b[2]));
    }
    
    /**
     * Multiplies the vector with a scalar
     * @param vec vector object
     * @param a scalar factor
     * @return a new Hep3Vector object
     */
    public static SpacePoint multiply(Hep3Vector vec, double a) {
        return new CartesianPoint(vec.x() * a, vec.y() * a, vec.z() * a);
    }
    
    /**
     * Multiplies the vector with a scalar
     * @param vec vector object
     * @param a scalar factor
     * @return a new SpaceVector object
     */
    public static SpaceVector multiply(SpaceVector vec, double a) {
        return new CartesianVector(vec.x() * a, vec.y() * a, vec.z() * a);
    }

    
    /**
     * Divides the vector by a scalar
     * @param vec vector object
     * @param a scalar divisor
     * @return a new Hep3Vector object
     */
    public static Hep3Vector divide(Hep3Vector vec, double a) {
        return new BasicHep3Vector(vec.x() / a, vec.y() / a, vec.z() / a);
    }

    public static Hep3Vector subtract(Hep3Vector a, Hep3Vector b) {
        return new BasicHep3Vector(a.x() - b.x(), a.y() - b.y(), a.z() - b.z());
    }

    /**
     * Calculates the vectorial difference between two points in space
     * @param a SpacePoint 1
     * @param b SpacePoint 2
     * @return a-b
     */
    public static SpaceVector subtract(SpacePoint a, SpacePoint b) {
        return new CartesianVector(a.x() - b.x(), a.y() - b.y(), a.z() - b.z());
    }

    public static double[] subtract(double[] a, double[] b) {
        if (a.length != b.length)
            throw new IllegalArgumentException("dimensions do not match");
        double[] result = new double[a.length];
        for (int i=0; i<a.length; i++) {
            result[i] = a[i] - b[i];
        }
        return result;
    };

    /**
     * Calculates the sum of two points in space
     * @param a point 1
     * @param b point 2
     * @return a+b
     */
    public static Hep3Vector add(Hep3Vector a, Hep3Vector b) {
        return new BasicHep3Vector(a.x() + b.x(), a.y() + b.y(), a.z() + b.z());
    }

    /**
     * Calculates the sum of two points in space
     * @param a point 1
     * @param b point 2
     * @return a+b
     */
    public static SpacePoint add(SpacePoint a, SpacePoint b) {
        return new CartesianPoint(a.x() + b.x(), a.y() + b.y(), a.z() + b.z());
    }

    /**
     * Calculates the sum of two points in space
     * @param b point 2
     * @param a point 1
     * @return a+b
     */
    public static SpacePoint add(SpacePoint a, Hep3Vector b) {
        return new CartesianPoint(a.x() + b.x(), a.y() + b.y(), a.z() + b.z());
    }

    /**
     * Calculates the sum of two 3-vectors
     * @param a vector 1
     * @param b vector 2
     * @return the euclidian sum
     */
    public static Hep3Vector add(Hep3Vector a, double[] b) {
    	if (b.length != 3)
    		throw new IllegalArgumentException("b must have length 3");
    	double[] x = new double[] {a.x() + b[0], a.y() + b[1], a.z() + b[2]};
    	return new BasicHep3Vector(x);
    }
    
    public static double[] add(double[] a, double[] b) {
        if (a.length != b.length)
            throw new IllegalArgumentException("dimensions do not match");
        double[] result = new double[a.length];
        for (int i=0; i<a.length; i++) {
            result[i] = a[i] + b[i];
        }
        return result;
    }

    /**
     * Returns the distance between two space points.
     * @param spt1 SpacePoint 1
     * @param spt2 SpacePoint 2
     * @return the Euclidean distance between points
     */
    public static double distance(SpacePoint spt1, SpacePoint spt2) {
        double dx = spt2.x() - spt1.x();
        double dy = spt2.y() - spt1.y();
        double dz = spt2.z() - spt1.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    
    public static double distance(Hep3Vector spt1, Hep3Vector spt2) {
        double dx = spt2.x() - spt1.x();
        double dy = spt2.y() - spt1.y();
        double dz = spt2.z() - spt1.z();
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Returns a unit vector in a given direction
     * @param v the direction
     * @return a SpacePoint at the and of the unit vector
     */
    public static SpaceVector unit(Hep3Vector v) {
        double mag = v.magnitude();
        return new CartesianVector(v.x() / mag, v.y() / mag, v.z() / mag);
    }

    /**
     * Calculates the angle (in rad) between two different Hep3Vectors. Returns
     * a value between 0 and pi
     * @param vec1
     * @param vec2
     * @return the (absolute of the) angle in rad
     */
    public static double angle(Hep3Vector vec1, Hep3Vector vec2) {
        double result = Math.acos(dot(vec1, vec2) / vec1.magnitude()
                / vec2.magnitude());
        return result;
    }
    
    public static double angle(Hep3Vector vec, SpacePointVector path) {
        double result = Math.acos(dot(vec, path.getDirection()) / vec.magnitude()
                / path.magnitude());
        return result;
    }
    
    /**
     * Calculates the Euclidian norm of the vector x
     * @param x a vector
     * @return norm of x
     */
    @Deprecated public static double magnitude(double[] x) {
    	double norm = 0;
    	for (int i=0; i<x.length; ++i) {
    		norm += x[i]*x[i];
    	}
        return Math.sqrt(norm);
    }
    
    @Deprecated public static Hep3Vector unit(double[] x) {
        double y = magnitude(x);
        return new BasicHep3Vector(x[0]/y, x[1]/y, x[2]/y);
    }
    
    public static double getPt(double[] x) {
        assert x.length == 3;
        return sqrt(x[0]*x[0]+x[1]*x[1]);
    }
}
