package org.lcsim.recon.tracking.trfdca;


import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;
import org.lcsim.recon.tracking.trfbase.PropDirected;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.TrackDerivative;
import org.lcsim.recon.tracking.trfbase.TrackVector;

import org.lcsim.recon.tracking.trfcyl.SurfCylinder;

import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.PropDirected;
import org.lcsim.recon.tracking.trfbase.PropDir;

/**
 * Propagates tracks from a Cylinder to a DCA surface.
 *<p>
 * Propagation will fail if either the origin is not a cylinder,
 * or the destination is not a DCA surface.
 *
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class PropCylDCA extends PropDirected
{
    
    // static Attributes
    
    // Assign track parameter indices
    
    
    private static final int   IPHI     = SurfCylinder.IPHI;
    private static final int   IZ_CYL   = SurfCylinder.IZ;
    private static final int   IALF     = SurfCylinder.IALF;
    private static final int   ITLM_CYL = SurfCylinder.ITLM;
    private static final int   IQPT_CYL = SurfCylinder.IQPT;
    
    private static final int   IRSIGNED = SurfDCA.IRSIGNED;
    private static final int   IZ_DCA   = SurfDCA.IZ;
    private static final int   IPHID    = SurfDCA.IPHID;
    private static final int   ITLM_DCA = SurfDCA.ITLM;
    private static final int   IQPT_DCA = SurfDCA.IQPT;
    
    
    
    
    
    // Attributes   ***************************************************
    
    // bfield * BFAC
    private double _bfac;
    
    
    // Methods   ******************************************************
    
    // static methods
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public  static String typeName()
    { return "PropCylDCA";
    }
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public  String staticType()
    { return typeName();
    }
    
    /**
     *Construct an instance from a constant solenoidal magnetic field in Tesla.
     *
     * @param   bfield The magnetic field strength in Tesla.
     */
    public PropCylDCA(double bfield)
    {
        _bfac = bfield * TRFMath.BFAC;
    }
    
    /**
     *Clone an instance.
     *
     * @return A Clone of this instance.
     */
    public Propagator newPropagator()
    {
        return new PropCylDCA( bField() );
    }
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public String type()
    { return staticType();
    }
    
    
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
    public PropStat vecDirProp( VTrack trv,  Surface srf,
            PropDir dir)
    {
        TrackDerivative deriv = null;
        return cylDcaPropagate( _bfac, trv, srf, dir, deriv );
    }
    
    
    /**
     *Propagate a track without error in the specified direction.
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
    public PropStat vecDirProp( VTrack trv,  Surface srf,
            PropDir dir, TrackDerivative deriv)
    {
        return cylDcaPropagate( _bfac, trv, srf, dir, deriv );
    }
    //
    //  PropStat err_dir_prop( ETrack& tre, const Surface& srf,
    //                                            PropDir dir ) const;
    
    
    
    /**
     * Propagate a track with error in the specified direction.
     *
     * @param   _bfac Numerical factor times the magnetic field strength.
     *  @param   trv The VTrack to propagate.
     * @param   srf The Surface to which to propagate.
     * @param   dir The direction in which to propagate.
     * @param   deriv The track derivatives to update at the surface srf.
     * @return The propagation status.
     */
    public PropStat cylDcaPropagate(       double            _bfac,
            VTrack             trv,
            Surface            srf,
            PropDir            dir,
            TrackDerivative    deriv )
    {
        
        // construct return status
        PropStat pstat = new PropStat();
        
        // fetch the originating Surface and check it is a Cylinder
        Surface srf_scy = trv.surface();
        Assert.assertTrue( srf_scy.pureType().equals(SurfCylinder.staticType()) );
        if ( !srf_scy.pureType( ).equals(SurfCylinder.staticType()) )
            return pstat;
        
        // check that the destination surface is a DCA surface
        Assert.assertTrue( srf.pureType().equals(SurfDCA.staticType()) );
        if ( !srf.pureType( ).equals(SurfDCA.staticType()) )
            return pstat;
        SurfDCA srf_dca = ( SurfDCA ) srf;
        
        // Check that dca surface has zero tilt.
        
        boolean tilted = srf_dca.dXdZ() != 0 || srf_dca.dYdZ() != 0;
        Assert.assertTrue(!tilted);
        if(tilted) return pstat;
        
        // original coordinates are marked with prime ( phip,alfp,r1p)
        // coordinates centered at (xv,yv) are phi,alf,r1
        
        
        // fetch the originating radius
        double r1p    = srf_scy.parameter(SurfCylinder.RADIUS);
        double xv = srf_dca.x();
        double yv = srf_dca.y();
        
        // fetch the originating TrackVector
        TrackVector vec_scy = trv.vector();
        double phi1p  = vec_scy.get(SurfCylinder.IPHI);      // phi_position
        double z1    = vec_scy.get(SurfCylinder.IZ);        // z
        double alf1p  = vec_scy.get(SurfCylinder.IALF);      // phi_dir - phi_pos
        double tlam1 = vec_scy.get(SurfCylinder.ITLM);      // tan(lambda)
        double qpt1  = vec_scy.get(SurfCylinder.IQPT);      // q/pT
        
        double cosphi1p=Math.cos(phi1p);
        double sinphi1p=Math.sin(phi1p);
        
        double x1=r1p*cosphi1p-xv;
        double y1=r1p*sinphi1p-yv;
        
        double phi1 = Math.atan2(y1,x1);       // phi position in (xv,yv) coord system
        double r1 = Math.sqrt(x1*x1+y1*y1);    // r1 in (xv,yv) coord system
        double alf1 = alf1p + phi1p - phi1; // alf1 in (xv,yv) coord system
        
        
        // calculate salf1, calf1 and crv1
        double salf1 = Math.sin( alf1 );
        double calf1 = Math.cos( alf1 );
        double  crv1 = _bfac * qpt1;
        
        // calculate r2 and alf2 of the destination DCA Surface
        
        double r2;
        final double sign_alf1 = alf1 > 0.0 ? 1.0 : -1.0;
        double sign_alf2 = 1.0;
        
        double del = (r1*crv1 - 2.0*salf1);
        double rcdel = r1*crv1*del;
        double sroot = Math.sqrt(1.0 + rcdel);
        double sroot_minus_one = sroot - 1.0;
        if ( rcdel < 1.e-6 )
            sroot_minus_one = 0.5*rcdel - 0.125*rcdel*rcdel + 0.0625*rcdel*rcdel*rcdel;
        
        if ( Math.abs(del)<1e-14 )
        {
            sign_alf2 = sign_alf1;
            r2 = 0.0;
        }
        else if ( TRFMath.isZero(crv1) )
        {
            sign_alf2 = sign_alf1;
            r2 = r1 * Math.abs(salf1);
        }
        else
        {
            sign_alf2 = crv1*r1 > 2.0*salf1 ? -1.0 : 1.0;
            r2 = -sign_alf2*sroot_minus_one/crv1;
            Assert.assertTrue( r2 > 0.0 );
        }
/*
        if ( del == 0. )
        {
            sign_alf2 = sign_alf1;
            r2 = 0.0;
        }
        else if ( crv1 == 0. )
        {
            sign_alf2 = sign_alf1;
            r2 = r1p * Math.abs(salf1);
        }
        else
        {
            sign_alf2 = crv1*r1p > 2.0*salf1 ? -1.0 : 1.0;
            r2 = -sign_alf2*sroot_minus_one/crv1;
            Assert.assertTrue( r2 > 0.0 );
        }
 */
        
        double alf2 = sign_alf2 * TRFMath.PI2;
        
        // calculate phi2 of the destination DCA Surface
        double sign_crv = 1.;
        
        // calculate tlam2, qpt2 and crv2 of the destination DCA Surface
        double tlam2 = tlam1;
        double qpt2  = qpt1;
        double crv2  = crv1;
        
        
        double salf2 = sign_alf2 > 0 ? 1.: (sign_alf2 < 0 ? -1.: 0. );
        //double calf2 = 0.;
        double cnst = salf2-r2*crv2 > 0. ? TRFMath.PI2: (sign_alf2 == 0. ? 0. : -TRFMath.PI2);
        double phi2 = phi1p + Math.atan2( salf1-r1p*crv1, calf1 )
        - cnst;
        if(crv1 == 0. ) phi2= phi1p + alf1 - cnst;
        
        double phid2 = phi2 + sign_alf2 * TRFMath.PI2;
        phid2 = TRFMath.fmod2( phid2, TRFMath.TWOPI );
        
        // construct an object of ST_CylDCA
        ST_CylDCA sto = new ST_CylDCA(r1,phi1,alf1,crv1,r2,phi2,alf2);
        // fetch sT
        double st = sto.st();
        double s = st*Math.sqrt(1+tlam1*tlam1);
        
        // calculate z2 of the destination DCA Surface
        double z2 = z1 + st * tlam1;
        
        // construct the destination TrackVector
        TrackVector vec_dca = new TrackVector();
        vec_dca.set(IRSIGNED , sign_alf2 * r2);
        vec_dca.set(IZ_DCA   , z2);
/*
#ifdef TRF_PHIRANGE
  vec_dca(IPHID)    = fmod1(phid2, TWOPI);
#else
  vec_dca(IPHID)    = phid2;
#endif
 */
        vec_dca.set(IPHID    , phid2);
        vec_dca.set(ITLM_DCA , tlam2);
        vec_dca.set(IQPT_DCA , qpt2);
        
/*
// For axial tracks, zero z and tan(lambda).
 
  if(trv.is_axial()) {
    vec_dca(SurfDCA::IZ) = 0.;
    vec_dca(SurfDCA::ITLM) = 0.;
  }
 */
        
        // set the surface of trv to the destination DCA surface
        trv.setSurface( srf_dca.newPureSurface() );
        
        // set the vector of trv to the destination TrackVector (DCA coord.)
        trv.setVector(vec_dca);
        
        // set the direction of trv to the default value for VTrack on DCA
        trv.setForward();
        
        // set the return status
        pstat.setPathDistance(s);
        
        // exit now if user did not ask for error matrix
        if ( deriv == null ) return pstat;
        
        
        // calculate derivatives
        
        double dr2_dalf1_or = calf1/(sign_alf2-crv1*r2);
        double dr2_dalf1 = r1*dr2_dalf1_or;
        double dr2_dcrv1 = (r2*r2-r1*r1)/(2.0*(sign_alf2-crv1*r2));
        double dr2_dr1 = -sign_alf2/sroot*(r1*crv1 - salf1);
        
        double dphi2_dalf1 =  sign_crv*(1.0-r1*crv1*salf1)/(sroot*sroot);
        double dphi2_dalf1_m1_or = sign_crv*(-crv1*salf1-crv1*del)/(sroot*sroot);
        double dphi2_dcrv1 = -sign_crv*(r1*calf1)/(sroot*sroot);
        double dphi2_dr1 = -crv1*calf1/(1.+r1*crv1*r1*crv1-2.*salf1*r1*crv1);
        
        
        double dst_dalf1 = sto.d_st_dalf1(dr2_dalf1, dphi2_dalf1);
        double dst_dalf1_or = sto.d_st_dalf1_or(r1,dr2_dalf1_or, dphi2_dalf1_m1_or);
        double dst_dcrv1 = sto.d_st_dcrv1(dr2_dcrv1, dphi2_dcrv1);
        double dst_dr1 = sto.d_st_dr1();
        
        
        // derivatives between phi,alf and phip (prime) alfp
        double dphi1_dphi1p_tr = r1p*Math.cos(phi1p-phi1);
        double dalf1_dphi1p_tr = r1 - dphi1_dphi1p_tr;
        double dalf1_dalf1p = 1.;
        double dr1_dphi1p = r1p*Math.sin(phi1-phi1p);
        
        
        // derivatives to original coordinates
        
        double dr2_dphi1p = dr2_dalf1_or*dalf1_dphi1p_tr + dr2_dr1*dr1_dphi1p;
        double dr2_dalf1p = dr2_dalf1 * dalf1_dalf1p;
        
        double dst_dphi1p = dst_dalf1_or*dalf1_dphi1p_tr + dst_dr1*dr1_dphi1p;
        double dst_dalf1p = dst_dalf1*dalf1_dalf1p;
        
        double dphi2_dphi1p = -dphi1_dphi1p_tr*dphi2_dalf1_m1_or + dphi2_dr1*dr1_dphi1p + dphi2_dalf1;
        double dphi2_dalf1p = dphi2_dalf1*dalf1_dalf1p;
        
        
        // build the derivative matrix
        
        //  matrix<double>& deriv = *deriv;
        //  deriv.fill( 0.0 );
        
        deriv.set(IRSIGNED, IPHI     , sign_alf2 * dr2_dphi1p);
        deriv.set(IRSIGNED, IZ_CYL   , 0.0);
        deriv.set(IRSIGNED, IALF     , sign_alf2 * dr2_dalf1p);
        deriv.set(IRSIGNED, ITLM_CYL , 0.0);
        deriv.set(IRSIGNED, IQPT_CYL , sign_alf2 * _bfac * dr2_dcrv1);
        
        deriv.set(IZ_DCA,   IPHI     , tlam1 * dst_dphi1p);
        deriv.set(IZ_DCA,   IZ_CYL   , 1.0);
        deriv.set(IZ_DCA,   IALF     , tlam1 * dst_dalf1p);
        deriv.set(IZ_DCA,   ITLM_CYL , st);
        deriv.set(IZ_DCA,   IQPT_CYL , tlam1 * _bfac * dst_dcrv1);
        
        deriv.set(IPHID,    IPHI     , dphi2_dphi1p);
        deriv.set(IPHID,    IZ_CYL   , 0.0);
        deriv.set(IPHID,    IALF     , dphi2_dalf1p);
        deriv.set(IPHID,    ITLM_CYL , 0.0);
        deriv.set(IPHID,    IQPT_CYL , _bfac * dphi2_dcrv1);
        
        deriv.set(ITLM_DCA, IPHI     , 0.0);
        deriv.set(ITLM_DCA, IZ_CYL   , 0.0);
        deriv.set(ITLM_DCA, IALF     , 0.0);
        deriv.set(ITLM_DCA, ITLM_CYL , 1.0);
        deriv.set(ITLM_DCA, IQPT_CYL , 0.0);
        
        deriv.set(IQPT_DCA, IPHI     , 0.0);
        deriv.set(IQPT_DCA, IZ_CYL   , 0.0);
        deriv.set(IQPT_DCA, IALF     , 0.0);
        deriv.set(IQPT_DCA, ITLM_CYL , 0.0);
        deriv.set(IQPT_DCA, IQPT_CYL , 1.0);
        
        // For axial tracks, zero all derivatives of or with respect to z or
        // tan(lambda), that are not already zero.  This will force the error
        // matrix to have zero errors for z and tan(lambda).
        
/*
  if(trv.is_axial()) {
    deriv.set(IZ_DCA,   IPHI,     0.);
    deriv.set(IZ_DCA,   IZ_CYL,   0.);
    deriv.set(IZ_DCA,   IALF,     0.);
    deriv.set(IZ_DCA,   ITLM_CYL, 0.);
    deriv.set(IZ_DCA,   IQPT_CYL, 0.);
 
    deriv.set(ITLM_DCA, ITLM_CYL, 0.);
  }
 */
        
        return pstat;
        
    }
    
    
    /**
     *Return the strength of the magnetic field in Tesla.
     *
     * @return The strength of the magnetic field in Tesla.
     */
    public double bField()
    {
        return _bfac/TRFMath.BFAC;
    }
    
    
    /**
     *output stream
     * @return  A String representation of this instance.
     */
    public String toString()
    {
        return "Propagation from Cylinder to DCA with constant "
                + bField() + " Tesla field";
        
    }
    
    
    //**********************************************************************
    
    // Private class ST_CylDCA.
    //
    // An ST_CylDCA_ object calculates sT (the signed transverse path length)
    // and its derivatives w.r.t. alf1 and crv1.  It is constructed from
    // the starting (r1, phi1, alf1, crv1) and final track parameters
    // (r2, phi2, alf2) assuming these are consistent.  Methods are
    // provided to retrieve sT and the two derivatives.
    
    class ST_CylDCA
    {
        
        private boolean _big_crv;
        private double _st;
        private double _dst_dr2;
        private double _dst_dcrv1;
        private double _dst_dphi2;
        private double _dst_dr1;
        private double _dst_dphi2_or;
        private double _crv1;  // was public? need to check
        
        // constructor
        public ST_CylDCA()
        {
        }
        public ST_CylDCA(double r1, double phi1, double alf1, double crv1,
                double r2, double phi2, double alf2)
        {
            
            _crv1 = crv1;
            Assert.assertTrue( r1 >= 0.0 );
            Assert.assertTrue( r2 >= 0.0 );
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
                if ( crv1 != 0. )
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
                if (  sign == 0. )
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
                if(TRFMath.isZero(d))
                {
                    _dst_dcrv1 = 0.0;
                    _dst_dphi2 = sign*r1;
                    _dst_dphi2_or = sign;
                    _dst_dr2 =  0.0;
                }
                else
                {
                    _dst_dcrv1 = sign*d*d*arg*( 1.0/6.0 + 3.0/20.0*arg2);
                    double root = (1.0 + 0.5*arg*arg + 3.0/8.0*arg*arg*arg*arg );
                    _dst_dphi2_or = sign*(r2*Math.sin(phi2-phi1))*root/d;
                    _dst_dphi2 = r1*_dst_dphi2_or;
                    _dst_dr2 =   sign*(r2-r1*Math.cos(phi2-phi1))*root/d;
                }
                
            }
            _dst_dr1 = -Math.cos(alf1)/(1.+r1*crv1*r1*crv1-2.*Math.sin(alf1)*r1*crv1);
        }
        
        public  double st()
        {
            return _st;
        }
        
        public double d_st_dr1()
        {
            return _dst_dr1;
        }
        
        public double d_st_dalf1_or(double r1,double dr2_dalf1_or, double dphi2_dalf1_m1_or)
        {
            if ( _big_crv ) return ( dphi2_dalf1_m1_or ) / _crv1;
            else return  _dst_dr2 * dr2_dalf1_or + _dst_dphi2_or * (dphi2_dalf1_m1_or*r1+1.);
        }
        
        public  double d_st_dalf1( double d_r2_dalf1, double d_phi2_dalf1 )
        {
            if ( _big_crv ) return ( d_phi2_dalf1 - 1.0 ) / _crv1;
            else return  _dst_dr2 * d_r2_dalf1 + _dst_dphi2 * d_phi2_dalf1;
        }
        
        public  double d_st_dcrv1( double d_r2_dcrv1, double d_phi2_dcrv1 )
        {
            if ( _big_crv ) return ( d_phi2_dcrv1 - _st ) / _crv1;
            else return  _dst_dr2 * d_r2_dcrv1 + _dst_dphi2 * d_phi2_dcrv1+ _dst_dcrv1;
            
        }
        
        
    }
    
}