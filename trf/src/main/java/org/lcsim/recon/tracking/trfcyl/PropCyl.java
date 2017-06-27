package org.lcsim.recon.tracking.trfcyl;
import org.lcsim.recon.tracking.trfutil.TRFMath;
import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.TrackDerivative;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.PropDirected;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.ETrack;
/**
 * Propagates tracks from one cylinder to another in a constant solenoidal magnetic field.
 *<p>
 * Propagation will fail if either the origin or destination is
 * not a cylinder.
 *<p>
 * Propagation more than halfway around one loop is not allowed and
 * will result in failure if attempted.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class PropCyl extends PropDirected
{
    
    private static final int IPHI = SurfCylinder.IPHI;
    private static final int IZ   = SurfCylinder.IZ;
    private static final int IALF = SurfCylinder.IALF;
    private static final int ITLM = SurfCylinder.ITLM;
    private static final int IQPT  = SurfCylinder.IQPT;
    
    // attributes
    // Factor connecting curvature to momentum:
    // C = 1/Rc = BFAC * B * q/pT
    // where B = magnetic field in Tesla, q = charge in natural units
    // (electron has -1) and pT = transverse momentum in GeV/c.
    
    // BFAC * bfield
    private double _bfac;
    
    //
    
    /**
     *Construct an instance from a constant solenoidal magnetic field in Tesla.
     *
     * @param   bfield The magnetic field strength in Tesla.
     */
    public PropCyl(double bfield)
    {
        _bfac = TRFMath.BFAC*bfield;
    }
    
    //
    
    
    /**
     *Clone an instance.
     *
     * @return A Clone of this instance.
     */
    public Propagator newPropagator()
    {
        return new PropCyl( bField() );
    }
    
    // print
    
    /**
     *output stream
     *
     * @return  A String representation of this instance.
     */
    public String toString()
    {
        return "Cylinder propagation with constant "
                + bField() + " Tesla field \n";
    }
    //
    //
    
    /**
     *Propagate a track without error in the specified direction.
     *
     * The track parameters for a cylinder are:
     * phi z alpha tan(lambda) curvature
     *
     * @param   trv The VTrack to propagate.
     * @param   srf The Surface to which to propagate.
     * @param   dir The direction in which to propagate.
     * @return The propagation status.
     */
    public PropStat vecDirProp(VTrack trv, Surface srf, PropDir dir)
    {
        TrackDerivative deriv = null;
        return vecDirProp(trv, srf, dir, deriv);
    }
    
    
    /**
     *Propagate a track without error in the specified direction
     *and return the derivative matrix in deriv.
     *
     * The track parameters for a cylinder are:
     * phi z alpha tan(lambda) curvature
     *
     * @param   trv The VTrack to propagate.
     * @param   srf The Surface to which to propagate.
     * @param   dir The direction in which to propagate.
     * @param   deriv The track derivatives to update at the surface srf.
     * @return The propagation status.
     */
    public PropStat vecDirProp(VTrack trv, Surface srf, PropDir dir, TrackDerivative deriv)
    {
        
        // construct return status
        PropStat pstat = new PropStat();
        
        // fetch the originating surface and vector
        Surface srf1 = trv.surface();
        TrackVector vec1 = trv.vector();
        
        // Check origin is a cylinder.
        //need checks here
        Assert.assertTrue( srf1.pureType().equals(SurfCylinder.staticType()) );
        if ( !srf1.pureType( ).equals(SurfCylinder.staticType()) )
            return pstat;
        SurfCylinder scy1 = (SurfCylinder) srf1;
        // Check destination is a cylinder.
        Assert.assertTrue( srf.pureType().equals(SurfCylinder.staticType()) );
        if (! srf.pureType( ).equals(SurfCylinder.staticType()) )
            return pstat;
        SurfCylinder scy2 = ( SurfCylinder) srf;
        // Separate the move status from the direction.
        //need a clone here...
        //PropDir rdir = new PropDir(dir);
        PropDir rdir = dir;
        boolean move = reduceDirection(rdir);
        if(move) rdir = reduce(rdir);
        // If surfaces are the same, we can return now.
        if ( srf.pureEqual(srf1) && !move )
        {
            
            if ( deriv != null )
            {
                
                deriv.setIdentity();
                
            }
            
            pstat.setSame();
            return pstat;
        }
        
        // Fetch the radii and the starting track vector.
        int irad = SurfCylinder.RADIUS;
        double r1 = scy1.parameter(irad);
        double r2 = scy2.parameter(irad);
        TrackVector vec = trv.vector();
        double phi1 = vec.get(SurfCylinder.IPHI);               // phi position
        double z1 = vec.get(SurfCylinder.IZ);                   // z
        double alf1 = vec.get(SurfCylinder.IALF);               // phi_dir - phi_pos
        double tlam1 = vec.get(SurfCylinder.ITLM);              // tan(lambda)
        double qpt1 = vec.get(SurfCylinder.IQPT);               // q/pT
        
        // Check alpha range.
        alf1 = TRFMath.fmod2( alf1, TRFMath.TWOPI );
        Assert.assertTrue( Math.abs(alf1) <= Math.PI );
        if ( trv.isForward() ) Assert.assertTrue( Math.abs(alf1) <= TRFMath.PI2 );
        else Assert.assertTrue( Math.abs(alf1) > TRFMath.PI2 );
        
        // Calculate the cosine of lambda.
        double clam1 = 1.0/Math.sqrt(1+tlam1*tlam1);
        
        // Calculate the curvature = _bfac*(q/pt)
        double dcrv1_dqpt1 = _bfac;
        double crv1 = dcrv1_dqpt1*qpt1;
        double dcrv1_dtlam1 = 0.0;
        
        // Evaluate the new track vector.
        
        // lambda and curvature do not change
        double tlam2 = tlam1;
        double crv2 = crv1;
        double qpt2 = qpt1;
        
        // We can evaluate sin(alf2), leaving two possibilities for alf2
        // 1st solution: alf21, phi21, phid21, tht21
        // 2nd solution: alf22, phi22, phid22, tht22
        // evaluate phi2 to choose
        double salf1 = Math.sin( alf1 );
        double calf1 = Math.cos( alf1 );
        double salf2 = r1/r2*salf1 + 0.5*crv1/r2*(r2*r2-r1*r1);
        // If salf2 is close to 1 or -1, set it to that value.
        double diff = Math.abs( Math.abs(salf2) - 1.0 );
        if ( diff < 1.e-10 ) salf2 = salf2>0 ? 1.0 : -1.0;
        // if salf2 > 1, track does not cross cylinder
        if ( Math.abs(salf2) > 1.0 ) return pstat;
        double alf21 = Math.asin( salf2 );
        double alf22 = alf21>0 ? Math.PI-alf21 : -Math.PI-alf21;
        double calf21 = Math.cos( alf21 );
        double calf22 = Math.cos( alf22 );
        double phi20 = phi1 + Math.atan2( salf1-r1*crv1, calf1 );
        double phi21 = phi20 - Math.atan2( salf2-r2*crv2, calf21 );   // phi position
        double phi22 = phi20 - Math.atan2( salf2-r2*crv2, calf22 );
        // Construct an sT object for each solution.
        STCalc sto1 = new STCalc(r1,phi1,alf1,crv1,r2,phi21,alf21);
        STCalc sto2 = new STCalc(r1,phi1,alf1,crv1,r2,phi22,alf22);
        // Check the two solutions are nonzero and have opposite sign
        // or at least one is nonzero.
        
        // Extract the two sT-values.
        double st1 = sto1.st();
        double st2 = sto2.st();
        double ast1 = Math.abs(st1);
        double ast2 = Math.abs(st2);
        
        // If both solutions have the same sign, then the most distant one
        // should actually go the other way along a full cicle minus the
        // calculated value of s.  We discard this solution by setting it
        // to zero.
        if ( st1*st2 > 0.0 )
        {
            if ( ast1 > ast2 )
            {
                st1 = 0.0;
            }
            else
            {
                st2 = 0.0;
            }
        }
        // Choose the correct solution
        boolean use_first_solution;
        
        if (rdir.equals(PropDir.NEAREST))
        {
            use_first_solution = st1!=0 && (st2==0.0 || ast1<ast2);
        }
        else if (rdir.equals(PropDir.FORWARD))
        {
            if ( st1 > 0.0 )
            {
                use_first_solution = true;
            }
            else if ( st2 > 0.0 )
            {
                use_first_solution = false;
            }
            else
            {
                return pstat;
            }
        }
        else if (rdir.equals(PropDir.BACKWARD))
        {
            if ( st1 < 0.0 )
            {
                use_first_solution = true;
            }
            else if ( st2 < 0.0 )
            {
                use_first_solution = false;
            }
            else
            {
                return pstat;
            }
        }
        else
        {
            throw new IllegalArgumentException("PropCyl._vec_propagate: Unknown direction.");
        }
        // Assign phi2, alf2 and sto2 for the chosen solution.
        double phi2, alf2;
        STCalc sto;
        double calf2;
        if ( use_first_solution )
        {
            sto = sto1;
            phi2 = phi21;
            alf2 = alf21;
            calf2 = calf21;
        }
        else
        {
            sto = sto2;
            phi2 = phi22;
            alf2 = alf22;
            calf2 = calf22;
        }
        
        // fetch sT.
        double st = sto.st();
        
        if ( st == 0.0 )
        {
            return pstat;
        }
        
        // use sT to evaluate z2
        double z2 = z1 + tlam1*st;
        
        // Check alpha range.
        Assert.assertTrue( Math.abs(alf2) <= Math.PI );
        
        // put new values in vec
        vec.set(SurfCylinder.IPHI, phi2);
        vec.set(SurfCylinder.IZ,   z2);
        vec.set(SurfCylinder.IALF, alf2);
        vec.set(SurfCylinder.ITLM, tlam2);
        vec.set(SurfCylinder.IQPT, qpt2);
        
        // Update trv
        trv.setSurface( srf );
        trv.setVector(vec);
        if ( Math.abs(alf2) <= TRFMath.PI2 ) trv.setForward();
        else trv.setBackward();
        
        // Set the return status.
        //st > 0 ? pstat.set_forward() : pstat.set_backward();
        double s = st/clam1;
        pstat.setPathDistance(s);
        
        // exit now if user did not ask for error matrix.
        if ( deriv == null ) return pstat;
        
        // Calculate derivatives.
        
        // alpha_2
        double da2da1 = r1*calf1/r2/calf2;
        double da2dc1 = (r2*r2-r1*r1)*0.5/r2/calf2;
        
        // phi2
        double rcsal1 = r1*crv1*salf1;
        double rcsal2 = r2*crv2*salf2;
        double den1 = 1.0 + r1*r1*crv1*crv1 - 2.0*rcsal1;
        double den2 = 1.0 + r2*r2*crv2*crv2 - 2.0*rcsal2;
        double dp2dp1 = 1.0;
        double dp2da1 = (1.0-rcsal1)/den1 - (1.0-rcsal2)/den2*da2da1;
        double dp2dc1 = -r1*calf1/den1 + r2*calf2/den2
                - (1.0-rcsal2)/den2*da2dc1;
        
        // z2
        double dz2dz1 = 1.0;
        double dz2dl1 = st;
        double dz2da1 = tlam1*sto.d_st_dalf1(dp2da1, da2da1);
        double dz2dc1 = tlam1*sto.d_st_dcrv1(dp2dc1, da2dc1);
        
        // Build derivative matrix.
        
        deriv.set(IPHI, IPHI ,  dp2dp1);
        deriv.set(IPHI, IALF ,  dp2da1);
        deriv.set(IPHI, ITLM ,  dp2dc1*dcrv1_dtlam1);
        deriv.set(IPHI, IQPT ,  dp2dc1*dcrv1_dqpt1);
        deriv.set(IZ, IZ   ,  dz2dz1);
        deriv.set(IZ, IALF ,  dz2da1);
        deriv.set(IZ, ITLM ,  dz2dl1 + dz2dc1*dcrv1_dtlam1);
        deriv.set(IZ, IQPT ,  dz2dc1*dcrv1_dqpt1);
        deriv.set(IALF, IALF ,  da2da1);
        deriv.set(IALF, ITLM ,  da2dc1*dcrv1_dtlam1);
        deriv.set(IALF, IQPT ,  da2dc1*dcrv1_dqpt1);
        deriv.set(ITLM, ITLM ,  1.0);
        deriv.set(IQPT, IQPT ,  1.0);
        return pstat;
    }
    
    
    
    //
    
    /**
     *Return the strength of the magnetic field in Tesla.
     *
     * @return The strength of the magnetic field in Tesla.
     */
    public double bField()
    {
        return _bfac/TRFMath.BFAC;
    }
    
    // Private class STCalc.
    //
    // An STCalc object calculates sT (the signed transverse path length)
    // and its derivatives w.r.t. alf1 and crv1.  It is constructed from
    // the starting (r1, phi1, alf1, crv1) and final track parameters
    // (r2, phi2, alf2) assuming these are consistent.  Methods are
    // provided to retrieve sT and the two derivatives.
    //
    class STCalc
    {
        
        private boolean _big_crv;
        private double _st;
        private double _dst_dphi21;
        private double _dst_dcrv1;
        public double _crv1;
        
        // constructor
        public STCalc()
        {
        }
        public STCalc(double r1, double phi1, double alf1, double crv1,
                double r2, double phi2, double alf2)
        {
            _crv1 = crv1;
            Assert.assertTrue( r1 > 0.0 );
            Assert.assertTrue( r2 > 0.0 );
            double rmax = r1+r2;
            
            // Calculate the change in xy direction
            double phi_dir_diff = TRFMath.fmod2(phi2+alf2-phi1-alf1,TRFMath.TWOPI);
            Assert.assertTrue( Math.abs(phi_dir_diff) <= Math.PI );
            
            // Evaluate whether |C| is" big"
            _big_crv = rmax*Math.abs(crv1) > 0.001;
            
            // If the curvature is big we can use
            // sT = (phi_dir2 - phi_dir1)/crv1
            if ( _big_crv )
            {
                Assert.assertTrue( crv1 != 0.0 );
                _st = phi_dir_diff/crv1;
            }
            
            // Otherwise, we calculate the straight-line distance
            // between the points and use an approximate correction
            // for the (small) curvature.
            else
            {
                
                // evaluate the distance
                double d = Math.sqrt( r1*r1 + r2*r2 - 2.0*r1*r2*Math.cos(phi2-phi1) );
                double arg = 0.5*d*crv1;
                double arg2 = arg*arg;
                double st_minus_d = d*arg2*( 1.0/6.0 + 3.0/40.0*arg2 );
                _st = d + st_minus_d;
                
                // evaluate the sign
                // We define a metric xsign = abs( (dphid-d*C)/(d*C) ).
                // Because sT*C = dphid and d = abs(sT):
                // xsign = 0 for sT > 0
                // xsign = 2 for sT < 0
                // Numerical roundoff will smear these predictions.
                double sign = 0.0;
                if ( crv1!= 0. )
                {
                    double xsign = Math.abs( (phi_dir_diff - _st*crv1) / (_st*crv1) );
                    if ( xsign < 0.5 ) sign = 1.0;
                    if ( xsign > 1.5  &&  xsign < 3.0 ) sign = -1.0;
                }
                // If the above is indeterminate, assume zero curvature.
                // In this case abs(alpha) decreases monotonically
                // with sT.  Track passing through origin has alpha = 0 on one
                // side and alpha = +/-pi on the other.  If both points are on
                // the same side, we use dr/ds > 0 for |alpha|<pi/2.
                if ( sign==0. )
                {
                    sign = 1.0;
                    if ( Math.abs(alf2) > Math.abs(alf1) ) sign = -1.0;
                    if ( Math.abs(alf2) == Math.abs(alf1) )
                    {
                        if ( Math.abs(alf2) < TRFMath.PI2 )
                        {
                            if ( r2 < r1 ) sign = -1.0;
                        }
                        else
                        {
                            if ( r2 > r1 ) sign = -1.0;
                        }
                    }
                }
                
                // Correct _st using the above sign.
                Assert.assertTrue( Math.abs(sign) == 1.0 );
                _st = sign*_st;
                
                // save derivatives
                _dst_dcrv1 = sign*d*d*arg*( 1.0/6.0 + 3.0/20.0*arg2);
                double root = (1.0 + 0.5*arg*arg + 3.0/8.0*arg*arg*arg*arg );
                _dst_dphi21 = sign*(r1*r2*Math.sin(phi2-phi1))*root/d;
                
            }
            
        }
        
        public double st()
        { return _st;
        }
        
        public double d_st_dalf1(double dphi2_dalf1, double dalf2_dalf1 )
        {
            if ( _big_crv ) return ( dphi2_dalf1 + dalf2_dalf1 - 1.0 ) / _crv1;
            else return _dst_dphi21 * dphi2_dalf1;
        }
        
        public double d_st_dcrv1(double dphi2_dcrv1, double dalf2_dcrv1 )
        {
            if ( _big_crv ) return ( dphi2_dcrv1 + dalf2_dcrv1 - _st ) / _crv1;
            else return _dst_dcrv1 + _dst_dphi21*dphi2_dcrv1;
        }
        
    }
    
    
    
    
}
