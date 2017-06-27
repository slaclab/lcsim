package org.lcsim.recon.tracking.trfdca;

import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfutil.StatusDouble;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;
import org.lcsim.recon.tracking.trfutil.RootFindLinear;
import org.lcsim.recon.tracking.trfcyl.SurfCylinder;

/**
 * DistSurface.java
 *This is a class for finding the propagation distance of a track
 *          to an arbitrary surface in a uniform magnetic field.
 *
 *          The constructor arguments include the initial track position
 *          (x, y, z), direction (phid, tlam), the helix rate or curvature
 *          wc (abs(wc) = cos(lambda) / radius), the destination surface
 *          and limits (smin, smax).  The limits are used to resolve multiple
 *          solution ambiguities (otherwise the nearest solution is used).
 *
 *          Return value status codes can be found in trfutil/RootFindLinear.h
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class DistSurface extends RootFindLinear
{
    // Attributes.
    
    private double _x;                   // Track x.
    private double _y;                   // Track y.
    private double _z;                   // Track z.
    private double _phid;                // Azimuth of track momentum vector.
    private double _tlam;                // Latitude of track momentum vector.
    private double _wc;                  // Parametric cyclotron frequency (rad/cm).
    private Surface _srf;                // Destination surface.
    private double _smin;                // Minimum allowed distance.
    private double _smax;                // Maximum allowed distance.
    private boolean _dca;                // DCA surface?
    
    // The following attributes are filled only in the case of a dca surface.
    
    private double _r;                   // Track radius (relative to dca surface).
    private double _phi;                 // Track phi (relative to dca surface).
    private double _slam;                // sin(lambda).
    private double _clam;                // cos(lambda).
    private double _b;                   // Total beam tilt.
    private double _phib;                // Phi of beam tilt.
    
    
    
    
    /**
     * Creates a new instance of DistSurface using the initial track parameters and the destination surface.
     *
     * @param x  track x position
     * @param y track y position
     * @param z track z position
     * @param phid Azimuth (phi) of track momentum vector.
     * @param tlam Latitude (tangent(lambda) of track momentum vector.
     * @param wc Parametric cyclotron frequency (rad/cm).
     * @param srf Destination surface.
     */
    public DistSurface(double x, double y, double z,
            double phid, double tlam,
            double wc, Surface srf)
    {
        this(x, y, z, phid, tlam, wc, srf, -1000., 1000.);
    }
    
    /**
     * Creates a new instance of DistSurface using the initial track parameters and the destination surface.
     *
     * @param x  track x position
     * @param y track y position
     * @param z track z position
     * @param phid Azimuth (phi) of track momentum vector.
     * @param tlam Latitude (tangent(lambda) of track momentum vector.
     * @param wc Parametric cyclotron frequency (rad/cm).
     * @param srf Destination surface.
     * @param smin Minimum allowed distance.
     * @param smax Maximum allowed distance.
     */
    public DistSurface(double x, double y, double z,
            double phid, double tlam,
            double wc, Surface srf,
            double smin, double smax)
    {
        _x = x;
        _y = y;
        _z = z;
        _phid = phid;
        _tlam = tlam;
        _wc = wc;
        _srf = srf;
        _smin = smin;
        _smax = smax;
        _dca = false;
        
        // Is this a DCA surface?
        
        if(srf.pureType().equals(SurfDCA.staticType()))
        {
            _dca = true;
            SurfDCA srfdca = (SurfDCA) srf;
            double bx = srfdca.dXdZ();
            double by = srfdca.dYdZ();
            _b = Math.sqrt(bx*bx + by*by);
            _phib = (_b!=0 ? Math.atan2(by, bx) : 0.);
            double xbeam = srfdca.x(z);
            double ybeam = srfdca.y(z);
            double dx = x - xbeam;
            double dy = y - ybeam;
            _r = Math.sqrt(dx*dx + dy*dy);
            _phi = Math.atan2(dy, dx);
            _clam = 1. / Math.sqrt(1. + tlam*tlam);
            _slam = _clam * tlam;
        }
        
    }
    
    
    /**
     * Find distance to the destination surface.
     * @return Distance with status.
     */
    public StatusDouble distance()
    {
        // DCA surface?
        
        if(_dca)
        {
            return distanceToTiltedDca();
        }
        if(_srf.pureType().equals(SurfCylinder.staticType()))
        {
            return distanceToCylinder();
        }
        System.out.println("DistSurface: Unsupported surface: " + _srf );
        Assert.assertTrue(false);
        return new StatusDouble(RootFindLinear.OUT_OF_RANGE, 0.);
    }
    
    
    
    /**
     * Evaluate function whose zero gives distance to tilted dca surface.
     * @param s Distance parameter.
     * @return Function value with success status.
     */
    public StatusDouble evaluate(double s)
    {
        Assert.assertTrue(_dca);
        double result = _r*Math.cos(_wc*s - _phid + _phi)
        - s*_b*_slam*Math.cos(_wc*s - _phid + _phib)
        + _clam*(_wc != 0. ? Math.sin(_wc*s)/_wc : s );
        return new StatusDouble(0,result);
    }
    
    /**
     * Calculate derivative wrt s of function calculated by method evaluate.
     * @param s Distance parameter.
     * @return Function derivative.
     */
    public double derivative(double s)
    {
        Assert.assertTrue(_dca);
        double result = -_r*_wc*Math.sin(_wc*s - _phid + _phi)
        - _b*_slam*Math.cos(_wc*s - _phid + _phib)
        + s*_b*_slam*_wc*Math.sin(_wc*s - _phid + _phib)
        + _clam*Math.cos(_wc*s);
        return result;
    }
    
    
    /**
     * Find distance to straight dca surface.
     * @return Distance with success status.
     */
    public StatusDouble distanceToStraightDca()
    {
        Assert.assertTrue(_dca);
        double result;
        double alf = _phid - _phi;
        double calf = Math.cos(alf);
        double salf = Math.sin(alf);
        if(_wc != 0)
            result = (-1. / _wc) * Math.atan(_wc*_r*calf / (_wc*_r*salf + _clam) );
        else
            result = -_r*calf / _clam;
        
        // Make sure we are on a branch of atan such that we are moving toward
        // smaller radius (the maximum distance is also a solution in the finite
        // momentum case).
        
        if(calf * result > 0.)
        {
            Assert.assertTrue(_wc != 0.);
            if(_wc * result > 0.)
                result -= Math.PI/_wc;
            else
                result += Math.PI/_wc;
        }
        Assert.assertTrue(calf*result <= 0.);
        
        // Check limits before returning.
        
        int status = 0;
        if(result < _smin || result > _smax)
            status = RootFindLinear.OUT_OF_RANGE;
        return new StatusDouble(status, result);
    }
    
    /**
     * Find distance to tilted dca surface.
     * @return  Distance with status.
     */
    public StatusDouble distanceToTiltedDca()
    {
        Assert.assertTrue(_dca);
        StatusDouble ss0 = distanceToStraightDca();
        if(_b == 0)
            return ss0;
        
        // Search for two values of s that bracket a zero of the function.
        
        int maxit = 100;
        double s0 = ss0.value();
        StatusDouble val0 = evaluate(s0);
        Assert.assertTrue(val0.status() == 0);
        
        // Use Newton's method to make an improved guess.
        
        double s1 = s0;
        double d = derivative(s0);
        StatusDouble val1 = new StatusDouble(val0.status(),val0.value());
        if(d != 0.)
        {
            s1 = s0 - val0.value()/d;
            val1 = evaluate(s1);
            Assert.assertTrue(val1.status() == 0);
        }
        
        // Find two arguments bracketing a zero.
        
        for(int i=0; i<maxit && val0.value()*val1.value() > 0.; ++i)
        {
            if(val0.value() == val1.value())
            {
                if(s1 >= 0)
                    s1 = 1.2*s1 + 0.1;
                else
                    s1 = 1.2*s1 - 0.1;
                val1 = evaluate(s1);
                Assert.assertTrue(val1.status() == 0);
            }
            else if(Math.abs(val0.value()) > Math.abs(val1.value()))
            {
                s0 = 2.2*s1 - 1.2*s0;
                val0 = evaluate(s0);
                Assert.assertTrue(val0.status() == 0);
            }
            else
            {
                s1 = 2.2*s0 - 1.2*s1;
                val1 = evaluate(s1);
                Assert.assertTrue(val1.status() == 0);
            }
        }
        
        // Return failure if we couldn't find bracketing values.
        
        if(val0.value()*val1.value() > 0.)
            return new StatusDouble(RootFindLinear.TOO_MANY_ITERATIONS, s1);
        
        // Solve.
        
        double smin = Math.max(_smin, Math.min(s0, s1));
        double smax = Math.min(_smax, Math.max(s0, s1));
        return solve(smin, smax);
    }
    
    /**
     * Return distance to cylinder surface.
     * @return Distance with status.
     */
    public StatusDouble distanceToCylinder()
    {
        Assert.assertTrue(_srf.pureType().equals(SurfCylinder.staticType()));
        SurfCylinder srfcyl = (SurfCylinder) _srf;
        double rcyl = srfcyl.radius();
        double cphid = Math.cos(_phid);
        double sphid = Math.sin(_phid);
        double rsalf = _x*sphid - _y*cphid;   // r*cos(alpha) = r*cos(phid - phi)
        double rcalf = _x*cphid + _y*sphid;   // r*sin(alpha) = r*sin(phid - phi)
        double clam = 1. / Math.sqrt(1. + _tlam*_tlam);
        double rsq = _x*_x + _y*_y;
        
        // Calculate quadratic equation coefficients.  The independent variable
        // is (1/wc)*tan(wc*s/2).
        
        double a = (rsq - rcyl*rcyl)*_wc*_wc + 4.*_wc*clam*rsalf + 4.*clam*clam;
        double b = 4.*clam*rcalf;
        double c = rsq - rcyl*rcyl;
        
        // If the quadratic equation can't be solved, return an error status.
        
        double dsq = b*b - 4.*a*c;
        if(dsq < 0.)
            return new StatusDouble(RootFindLinear.INVALID_FUNCTION_CALL, -b / (2.*a));
        
        // Find both quadratic roots.
        
        double d = Math.sqrt(dsq);
        double t1 = (-b + d)/(2.*a);
        double t2 = (-b - d)/(2.*a);
        double s1 = 2. * (_wc != 0 ? Math.atan(_wc*t1)/_wc : t1);
        double s2 = 2. * (_wc != 0 ? Math.atan(_wc*t2)/_wc : t2);
        
        // Choose the best (smallest) solution that is consistent with the
        // specified limits, jumping to another branch of atan if necessary
        // (should hardly ever be necessary).
        
        if(_wc != 0)
        {
            double sfull = TRFMath.TWOPI/Math.abs(_wc);
            if(s1 < _smin) s1 += sfull;
            if(s1 > _smax) s1 -= sfull;
            if(s2 < _smin) s2 += sfull;
            if(s2 > _smax) s2 -= sfull;
        }
        boolean s1ok = s1 >= _smin && s1 <= _smax;
        boolean s2ok = s2 >= _smin && s2 <= _smax;
        if(s1ok && s2ok)
        {
            s1ok = Math.abs(s1) < Math.abs(s2);
            s2ok = !s1ok;
        }
        Assert.assertTrue(!(s1ok && s2ok));
        if(s1ok)
            return new StatusDouble(0, s1);
        if(s2ok)
            return new StatusDouble(0, s2);
        if(Math.abs(s1) < Math.abs(s2))
            return new StatusDouble(RootFindLinear.OUT_OF_RANGE, s1);
        else
            return new StatusDouble(RootFindLinear.OUT_OF_RANGE, s2);
    }
    
    
    
    /**
     * String representation of this object.
     * @return String representation of this object.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("DistSurface: \n");
        sb.append("( "+_x+" "+_y+" "+_z+" ) "+_phid+" "+_tlam+" "+_wc+"\n");
        sb.append("at "+_srf+"\n");
        if(_dca)
        {
            sb.append(_r+" "+_phi+" "+_slam+" "+_clam+" "+_b+" "+_phib);
        }
        return sb.toString();
    }
    
}
