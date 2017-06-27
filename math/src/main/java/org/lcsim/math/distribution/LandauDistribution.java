/*
 * LandauDistribution.java
 * This corresponds to the CLHEP version, which
 * is based on the CERNLIB routine denlan (G110).
 * Note that there is some variation in whether
 * to divide by sigma, and whether the peak
 * coincides with the most probable value.
 *
 * Created on May 23, 2005, 4:21 PM
 */

package org.lcsim.math.distribution;

import static java.lang.Math.sqrt;
import static java.lang.Math.exp;
import static java.lang.Math.log;
/**
 *
 * @author ngraf
 */
public class LandauDistribution
{
    private double _peak;
    private double _width;
    
    /** Creates a new instance of LandauDistribution */
    public LandauDistribution(double peak, double width)
    {
        _peak = peak;
        _width = width;
    }
    
    public double peak()
    {
        return _peak;
    }
    
    public double width()
    {
        return _width;
    }
    
    public double value(double x)
    {
        double xs  = _peak + 0.222782*_width;
        return denlan((x-xs)/_width)/_width;
    }
    
    private double denlan(double x)
    {
        /* Initialized data */
        
        /* System generated locals */
        double ret_val, r__1;
        
        /* Local variables */
        double u, v;
        v = x;
        if (v < -5.5)
        {
            u = exp(v + 1.);
            ret_val = exp(-1 / u) / sqrt(u) * .3989422803 * ((a1[0] + (a1[
                    1] + a1[2] * u) * u) * u + 1);
        }
        else if (v < -1.)
        {
            u = exp(-v - 1);
            ret_val = exp(-u) * sqrt(u) * (p1[0] + (p1[1] + (p1[2] + (p1[3] + p1[
                    4] * v) * v) * v) * v) / (q1[0] + (q1[1] + (q1[2] + (q1[3] +
                    q1[4] * v) * v) * v) * v);
        }
        else if (v < 1.)
        {
            ret_val = (p2[0] + (p2[1] + (p2[2] + (p2[3] + p2[4] * v) * v) * v) *
                    v) / (q2[0] + (q2[1] + (q2[2] + (q2[3] + q2[4] * v) * v) * v)
                    * v);
        }
        else if (v < 5.)
        {
            ret_val = (p3[0] + (p3[1] + (p3[2] + (p3[3] + p3[4] * v) * v) * v) *
                    v) / (q3[0] + (q3[1] + (q3[2] + (q3[3] + q3[4] * v) * v) * v)
                    * v);
        }
        else if (v < 12.)
        {
            u = 1 / v;
            /* Computing 2nd power */
            r__1 = u;
            ret_val = r__1 * r__1 * (p4[0] + (p4[1] + (p4[2] + (p4[3] + p4[4] * u)
            * u) * u) * u) / (q4[0] + (q4[1] + (q4[2] + (q4[3] + q4[4] *
                    u) * u) * u) * u);
        }
        else if (v < 50.)
        {
            u = 1 / v;
            /* Computing 2nd power */
            r__1 = u;
            ret_val = r__1 * r__1 * (p5[0] + (p5[1] + (p5[2] + (p5[3] + p5[4] * u)
            * u) * u) * u) / (q5[0] + (q5[1] + (q5[2] + (q5[3] + q5[4] *
                    u) * u) * u) * u);
        }
        else if (v < 300.)
        {
            u = 1 / v;
            /* Computing 2nd power */
            r__1 = u;
            ret_val = r__1 * r__1 * (p6[0] + (p6[1] + (p6[2] + (p6[3] + p6[4] * u)
            * u) * u) * u) / (q6[0] + (q6[1] + (q6[2] + (q6[3] + q6[4] *
                    u) * u) * u) * u);
        }
        else
        {
            u = 1 / (v - v * log(v) / (v + 1));
            /* Computing 2nd power */
            r__1 = u;
            ret_val = r__1 * r__1 * ((a2[0] + a2[1] * u) * u + 1);
        }
        return ret_val;
        
    }
    
    static final double[] p1 = { .4259894875,-.124976255,.039842437,-.006298287635,.001511162253 };
    static final double[] q5 = { 1.,156.9424537,3745.310488,9834.698876,66924.28357 };
    static final double[] p6 = { 1.000827619,664.9143136,62972.92665,475554.6998,-5743609.109 };
    static final double[] q6 = { 1.,651.4101098,56974.73333,165917.4725,-2815759.939 };
    static final double[] a1 = { .04166666667,-.01996527778,.02709538966 };
    static final double[] a2 = { -1.84556867,-4.284640743 };
    static final double[] q1 = { 1.,-.3388260629,.09594393323,-.01608042283,.003778942063 };
    static final double[] p2 = { .1788541609,.1173957403,.01488850518,-.001394989411,1.283617211e-4 };
    static final double[] q2 = { 1.,.7428795082,.3153932961,.06694219548,.008790609714 };
    static final double[] p3 = { .1788544503,.09359161662,.006325387654,6.611667319e-5,-2.031049101e-6 };
    static final double[] q3 = { 1.,.6097809921,.2560616665,.04746722384,.006957301675 };
    static final double[] p4 = { .9874054407,118.6723273,849.279436,-743.7792444,427.0262186 };
    static final double[] q4 = { 1.,106.8615961,337.6496214,2016.712389,1597.063511 };
    static final double[] p5 = { 1.003675074,167.5702434,4789.711289,21217.86767,-22324.9491 };
    
}
