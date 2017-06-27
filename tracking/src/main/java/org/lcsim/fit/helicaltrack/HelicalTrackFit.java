/*
 * HelicalTrackFit.java
 *
 * Created on March 25, 2006, 6:11 PM
 *
 * $Id: HelicalTrackFit.java,v 1.16 2008/10/13 01:05:58 partridge Exp $
 */

package org.lcsim.fit.helicaltrack;

import hep.physics.matrix.SymmetricMatrix;
import java.util.Map;
import org.lcsim.constants.Constants;

/**
 * Represents the result of a fit to a helical track.
 * @author Norman Graf
 * @version 2.0 (modified by R. Partridge)
 */
public class HelicalTrackFit {
    /**
     * Index of DCA element in parameter array and covariance matrix.
     */
    public static int dcaIndex = 0;
    /**
     * Index of phi0 element in parameter array and covariance matrix.
     */
    public static int phi0Index = 1;
    /**
     * Index of curvature element in the parameter array and covariance matrix.
     */
    public static int curvatureIndex = 2;
    /**
     * Index of the z0 coordinate in the parameter array and covariance matrix.
     */
    public static int z0Index = 3;
    /**
     * Index of the slope in the parameter array and covariance matrix.
     */
    public static int slopeIndex = 4;
    // fit independently to a circle in x-y and a line in s-z
    // first is circle, second is line
    private double[] _chisq = new double[2];
    private double _nhchisq;
    private int[] _ndf = new int[2];
    private double[] _parameters;
    private SymmetricMatrix _covmatrix;
    private Map<HelicalTrackHit, Double> _smap;
    private Map<HelicalTrackHit, MultipleScatter> _msmap;
     /**
     * Doubles used for error variables
     */
    //Omega error
    private double curveerror;
    //tanl(lambda) error
    private double slopeerror;
    //distance of closest approach error
    private double dcaerror;
    //azimuthal angle at DCA for momentum error
    private double phi0error;
    //z position when the particle is at the dca error
    private double z0error;
    
    /**
     * Creates a new instance of HelicalTrackFit
     * @param pars array of helix parameters
     * @param cov covariance matrix of helix fit
     * @param chisq chisq of the circle fit (chisq[0]) and s-z fit (chisq[1])
     * @param ndf dof for the circle fit (ndf[0]) and s-z fit (ndf[1])
     * @param smap map containing the x-y path lengths
     * @param msmap map containing the multiple scattering uncertainties
     */
    public HelicalTrackFit(double[]pars, SymmetricMatrix cov, double[] chisq, int[] ndf,
            Map<HelicalTrackHit, Double> smap, Map<HelicalTrackHit, MultipleScatter> msmap) {
        _parameters = pars;
        _covmatrix = cov;
        _chisq = chisq;
        _nhchisq = 0.;
        _ndf = ndf;
        _smap = smap;
        _msmap = msmap;
    }
    
    /**
     * Return the helix parameters as an array.
     * @return helix parameters
     */
    public double[] parameters() {
        return _parameters;
    }
    
    /**
     * Return the signed helix DCA.
     * @return DCA
     */
    public double dca() {
        return _parameters[dcaIndex];
    }
    
    /**
     * Return the azimuthal direction at the DCA
     * @return azimuthal direction
     */
    public double phi0() {
        return _parameters[phi0Index];
    }
    
    /**
     * Return the signed helix curvature.
     * @return helix curvature
     */
    public double curvature() {
        return _parameters[curvatureIndex];
    }
    
    /**
     * Return the z coordinate for the DCA.
     * @return z coordinate
     */
    public double z0() {
        return _parameters[z0Index];
    }
    
    /**
     * Return the helix slope tan(lambda).
     * @return slope
     */
    public double slope() {
        return _parameters[slopeIndex];
    }
    
    /**
     * Return the helix covariance matrix.
     * @return covariance matrix
     */
    public SymmetricMatrix covariance() {
        return _covmatrix;
    }
    
    /**
     * Return the helix fit chisqs.  chisq[0] is for the circle fit, chisq[1] is
     * for the s-z fit.
     * @return chisq array
     */
    public double[] chisq() {
        return _chisq;
    }
    
    /**
     * Set the chisq for non-holonomic constraints (e.g., pT > xx).
     * @param nhchisq non-holonomic constraint chisq
     */
    public void setnhchisq(double nhchisq) {
        _nhchisq = nhchisq;
        return;
    }
    
    /**
     * Return the non-holenomic constraint chisq.
     * @return non-holenomic constraint chisq
     */
    public double nhchisq() {
        return _nhchisq;
    }
    
    /**
     * Return the total chisq: chisq[0] + chisq[1] + nhchisq.
     * @return total chisq
     */
    public double chisqtot() {
        return _chisq[0]+_chisq[1]+_nhchisq;
    }
    
    /**
     * Return the degrees of freedom for the fits.  ndf[0] is for the circle fit
     * and ndf[1] is for the s-z fit.
     * @return dof array
     */
    public int[] ndf() {
        return _ndf;
    }
    
    /**
     * Return cos(theta).
     * @return cos(theta)
     */
    public double cth() {
        return slope() / Math.sqrt(1 + Math.pow(slope(), 2));
    }
    
    /**
     * Return sin(theta).
     * @return sin(theta)
     */
    public double sth() {
        return 1. / Math.sqrt(1 + Math.pow(slope(), 2));
    }
    
    /**
     * Return transverse momentum pT for the helix.
     * @param bfield magnetic field
     * @return pT
     */
    public double pT(double bfield) {
        return Constants.fieldConversion * bfield * Math.abs(R());
    }
    
    /**
     * Return the momentum.
     * @param bfield magnetic field
     * @return momentum
     */
    public double p(double bfield) {
        return pT(bfield) / sth();
    }
    
    /**
     * Return the radius of curvature for the helix.
     * @return radius of curvature
     */
    public double R() {
        return 1. / curvature();
    }
    
    /**
     * Return the x coordinate of the helix center/axis.
     * @return x coordinate of the helix axis
     */
    public double xc() {
        return (R() - dca()) * Math.sin(phi0());
    }
    
    /**
     * Return the y coordinate of the helix center/axis.
     * @return y coordinate of the helix axis
     */
    public double yc() {
        return -(R() - dca()) * Math.cos(phi0());
    }
    
    public double x0() {
        return -dca() * Math.sin(phi0());
    }
    
    public double y0() {
        return dca() * Math.cos(phi0());
    }
    
    /**
     * Return a map of x-y path lengths for the hits used in the helix fit.
     * @return path length map
     */
    public Map<HelicalTrackHit, Double> PathMap() {
        return _smap;
    }
    
    /**
     * Return a map of the MultipleScatter objects supplied for the fit.
     * @return map of multiple scattering uncertainties
     */
    public Map<HelicalTrackHit, MultipleScatter> ScatterMap() {
        return _msmap;
    }
     /**
     * Return the error for curvature, omega
     * @return a double curveerror
     */
    public double getCurveError()
    {
        curveerror = Math.sqrt(_covmatrix.e(HelicalTrackFit.curvatureIndex, HelicalTrackFit.curvatureIndex));
        return curveerror;
    }
    /**
     * Return the error for slope dz/ds, tan(lambda)
     * @return double a slopeerror
     */
    public double getSlopeError()
    {
        slopeerror = Math.sqrt(_covmatrix.e(HelicalTrackFit.slopeIndex, HelicalTrackFit.slopeIndex));
        return slopeerror;
    }
    /**
     * Return the error for distance of closest approach, dca
     * @return a double dcaerror
     */
    public double getDcaError()
    {
        dcaerror = Math.sqrt(_covmatrix.e(HelicalTrackFit.dcaIndex, HelicalTrackFit.dcaIndex));
        return dcaerror;
    }
    /**
     * Return the error for phi0, azimuthal angle of the momentum at the DCA ref. point
     * @return a double phi0error
     */
    public double getPhi0Error()
    {
        phi0error = Math.sqrt(_covmatrix.e(HelicalTrackFit.phi0Index, HelicalTrackFit.phi0Index));
        return phi0error;
    }
    /**
     * Return the error for z0, the z position of the particle at the DCA
     * @return a double z0error
     */
    public double getZ0Error()
    {
        z0error = Math.sqrt(_covmatrix.e(HelicalTrackFit.z0Index, HelicalTrackFit.z0Index));
        return z0error;
    }
    /**
     * Create a string with the helix parameters.
     * @return string containing the helix parameters
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("HelicalTrackFit: \n");
        sb.append("d0= "+dca()+"\n");
        sb.append("phi0= "+phi0()+"\n");
        sb.append("curvature: "+curvature()+"\n");
        sb.append("z0= "+z0()+"\n");
        sb.append("tanLambda= "+slope()+"\n");
        return sb.toString();
    }
}