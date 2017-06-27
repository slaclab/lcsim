package org.lcsim.recon.tracking.trfcylplane;


import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.PropDirected;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.TrackDerivative;
import org.lcsim.recon.tracking.trfxyp.PropXYXY;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfutil.TRFMath;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfxyp.SurfXYPlane;
import org.lcsim.recon.tracking.trfzp.SurfZPlane;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.ETrack;

/**
 * Propagates tracks from a ZPlane to an XYPlane in a constant magnetic field.
 *<p>
 * Propagation will fail if either the origin is not a ZPlane
 * or destination is not a XYPlane.
 * Propagator works incorrectly for tracks with very small curvatures
 *<p>
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public class PropZXY extends PropDirected
{
    
    // Assign track parameter indices.
    
    private static final int IV = SurfXYPlane.IV;
    private static final int   IZ   = SurfXYPlane.IZ;
    private static final int   IDVDU = SurfXYPlane.IDVDU;
    private static final int   IDZDU = SurfXYPlane.IDZDU;
    private static final int   IQP_XY  = SurfXYPlane.IQP;
    
    private static final int   IX = SurfZPlane.IX;
    private static final int   IY   = SurfZPlane.IY;
    private static final int   IDXDZ = SurfZPlane.IDXDZ;
    private static final int   IDYDZ = SurfZPlane.IDYDZ;
    private static final int   IQP_Z  = SurfZPlane.IQP;
    
    
    // attributes
    
    private double _bfac;
    private PropXYXY _propxyxy;
    
    // static methods
    
    // Return the type name.
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "PropZXY";
    }
    
    // Return the type.
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName();
    }
    
    
    
    // constructor from magnetic field in Tesla.
    /**
     *Construct an instance from a constant solenoidal magnetic field in Tesla.
     *
     * @param   bfield The magnetic field strength in Tesla.
     */
    public PropZXY(double bfield)
    {
        _bfac = TRFMath.BFAC*bfield;
        _propxyxy = new PropXYXY(bfield);
    }
    
    
    // Clone.
    /**
     *Clone an instance.
     *
     * @return A Clone of this instance.
     */
    public Propagator newPropagator( )
    {
        return new PropZXY(bField());
        
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
    public PropStat vecDirProp( VTrack trv, Surface srf,
            PropDir dir, TrackDerivative deriv )
    {
        
        PropStat pstat = new PropStat();
        Propagator.reduceDirection(dir);
        
        // Check destination is a XYPlane.
        Assert.assertTrue( srf.pureType().equals(SurfXYPlane.staticType()) );
        if ( !srf.pureType( ).equals(SurfXYPlane.staticType()) )
            return pstat;
        SurfXYPlane sxyp2 = ( SurfXYPlane ) srf;
        
        // Fetch phi of the destination plane
        
        int iphi  = SurfXYPlane.NORMPHI;
        double phi_n = sxyp2.parameter(iphi);
        VTrack trv0 = trv; // we want to change this vector!
        TrackDerivative deriv1 = new TrackDerivative();
        TrackDerivative deriv2 = new TrackDerivative();
        TrackDerivative lderiv = deriv;
        if ( deriv != null ) pstat = vecTransformZXY( _bfac, trv0, phi_n, dir,deriv1 );
        else pstat = vecTransformZXY( _bfac, trv0, phi_n, dir, lderiv );
        if ( ! pstat.success() ) return pstat;
        
        if ( deriv != null ) pstat = _propxyxy.vecDirProp(trv0,srf,dir,deriv2);
        else  pstat = _propxyxy.vecDirProp(trv0,srf,dir, lderiv);
        if ( pstat.success() )
        {
            trv = trv0;
            if ( deriv != null )  deriv.set(deriv2.times(deriv1));
        }
        return pstat;
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
    public PropStat vecDirProp( VTrack trv, Surface srf,
            PropDir dir)
    {
        TrackDerivative deriv = null;
        return vecDirProp(trv, srf, dir, deriv);
        
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
    
    // Return the type.
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
     *output stream
     * @return  A String representation of this instance.
     */
    public String toString()
    {
        return "ZPlane-XYPlane propagation with constant "
                + bField() + " Tesla field";
        
    }
    
    //**********************************************************************
    
    // Private function to propagate a track without error
    // The corresponding track parameters are:
    // On ZPlane:
    // z (cm) is fixed
    // 0 - x (cm)
    // 1 - y (cm)
    // 2 - dx/dz
    // 3 - dy/dz
    // 4 - q/p   p is momentum of a track, q is its charge
    // On XYPlane:
    // u (cm) is fixed
    // 0 - v (cm)
    // 1 - z (cm)
    // 2 - dv/du
    // 3 - dz/du
    // 4 - q/p   p is momentum of a track, q is its charge
    // If pderiv is nonzero, return the derivative matrix there.
    
    private PropStat
            vecTransformZXY( double B, VTrack trv, double phi_n,
            PropDir dir,
            TrackDerivative deriv )
    {
        
        // construct return status
        PropStat pstat = new PropStat();
        
        // fetch the originating surface and vector
        Surface srf1 = trv.surface();
        // TrackVector vec1 = trv.get_vector();
        
        // Check origin is a ZPlane.
        Assert.assertTrue( srf1.pureType().equals(SurfZPlane.staticType()) );
        if ( !srf1.pureType( ).equals(SurfZPlane.staticType()) )
            return pstat;
        SurfZPlane szp1 = ( SurfZPlane ) srf1;
        
        
        // Fetch the zpos's of the planes and the starting track vector.
        int izpos  = SurfZPlane.ZPOS;
        
        double zpos_0 = szp1.parameter(izpos);
        
        TrackVector vec = trv.vector();
        double a1 = vec.get(IX);                  // x
        double a2 = vec.get(IY);                  // y
        double a3 = vec.get(IDXDZ);               // dx/dz
        double a4 = vec.get(IDYDZ);               // dy/dz
        double a5 = vec.get(IQP_Z);               // q/p
        
        int sign_dz = 0;
        if(trv.isForward())  sign_dz =  1;
        if(trv.isBackward()) sign_dz = -1;
        if(sign_dz == 0)
        {
            System.out.println("PropZXY._vec_propagate: Unknown direction of a track ");
            System.exit(1);
        }
        
        double sinphi_n = Math.sin(phi_n);
        double cosphi_n = Math.cos(phi_n);
        
        double  u =  a1*cosphi_n + a2*sinphi_n;
        if( u < 0. )
        {
            u = - u;
            sinphi_n = - sinphi_n;
            cosphi_n = - cosphi_n;
            phi_n = phi_n + Math.PI;
            if( phi_n >= TRFMath.TWOPI) phi_n -= TRFMath.TWOPI;
        }
        // check if du == 0 ( that is track moves parallel to the destination plane )
        double du_dz = a3*cosphi_n + a4*sinphi_n;
        if(du_dz == 0.) return pstat;
        
        double a_hat_phin = 1./du_dz;
        double a_hat_phin2 = a_hat_phin*a_hat_phin;
        
        // double  u =  a1*cosphi_n + a2*sinphi_n;
        double b1 = -a1*sinphi_n + a2*cosphi_n;
        double b2 = zpos_0;
        double b3 = (a4*cosphi_n - a3*sinphi_n)*a_hat_phin;
        double b4 = a_hat_phin;
        double b5 = a5;
        
        int sign_du = 0;
        if(b4*sign_dz > 0) sign_du =  1;
        if(b4*sign_dz < 0) sign_du = -1;
        
        vec.set(IV     , b1);
        vec.set(IZ     , b2);
        vec.set(IDVDU  , b3);
        vec.set(IDZDU  , b4);
        vec.set(IQP_XY , b5);
        
        // Update trv
        SurfXYPlane sxyp = new SurfXYPlane(u,phi_n);
        trv.setSurface(sxyp.newPureSurface());
        trv.setVector(vec);
        
        // set new direction of the track
        if(sign_du ==  1) trv.setForward();
        if(sign_du == -1) trv.setBackward();
        
        // Set the return status.
        pstat.setSame();
        
        // exit now if user did not ask for error matrix.
        if ( deriv == null ) return pstat;
        
        double b34_hat = Math.sqrt(1 + b3*b3 + b4*b4);
        double rsinphi =  (b5*B)*sign_du*b34_hat;
        
        // du_da
        
        double du_da1 = cosphi_n;
        double du_da2 = sinphi_n;
        
        // db1_da
        
        double db1_da1 = - sinphi_n;
        double db1_da2 =   cosphi_n;
        
        // db3_da
        
        double db3_da3 = -a4*a_hat_phin2;
        double db3_da4 =  a3*a_hat_phin2;
        
        // db4_da
        
        double db4_da3 = - cosphi_n*a_hat_phin2;
        double db4_da4 = - sinphi_n*a_hat_phin2;
        
        
        // db3_n_db
        
        double db3_n_du  = -(1. + b3*b3)*rsinphi;
        
        // db4_n_db
        
        double db4_n_du  = -b4*b3*rsinphi;
        
        // db5_n_db
        
        
        // db1_n_da
        
        double db1_n_da1 = -b3 * du_da1 + db1_da1;
        double db1_n_da2 = -b3 * du_da2 + db1_da2;
        double db1_n_da3 = 0.;
        double db1_n_da4 = 0.;
        double db1_n_da5 = 0.;
        
        // db2_n_da
        
        double db2_n_da1 = -b4 * du_da1;
        double db2_n_da2 = -b4 * du_da2;
        double db2_n_da3 = 0.;
        double db2_n_da4 = 0.;
        double db2_n_da5 = 0.;
        
        // db3_n_da
        
        double db3_n_da1 = db3_n_du * du_da1;
        double db3_n_da2 = db3_n_du * du_da2;
        double db3_n_da3 = db3_da3;
        double db3_n_da4 = db3_da4;
        double db3_n_da5 = 0.;
        
        // db4_n_da
        
        double db4_n_da1 = db4_n_du * du_da1;
        double db4_n_da2 = db4_n_du * du_da2;
        double db4_n_da3 = db4_da3;
        double db4_n_da4 = db4_da4;
        double db4_n_da5 = 0.;
        
        // db5_n_da
        
        double db5_n_da1 = 0.;
        double db5_n_da2 = 0.;
        double db5_n_da3 = 0.;
        double db5_n_da4 = 0.;
        double db5_n_da5 = 1.;
        
        deriv.set(IV,IX        , db1_n_da1);
        deriv.set(IV,IY        , db1_n_da2);
        deriv.set(IV,IDXDZ     , db1_n_da3);
        deriv.set(IV,IDYDZ     , db1_n_da4);
        deriv.set(IV,IQP_Z     , db1_n_da5);
        deriv.set(IZ,IX        , db2_n_da1);
        deriv.set(IZ,IY        , db2_n_da2);
        deriv.set(IZ,IDXDZ     , db2_n_da3);
        deriv.set(IZ,IDYDZ     , db2_n_da4);
        deriv.set(IZ,IQP_Z     , db2_n_da5);
        deriv.set(IDVDU,IX     , db3_n_da1);
        deriv.set(IDVDU,IY     , db3_n_da2);
        deriv.set(IDVDU,IDXDZ  , db3_n_da3);
        deriv.set(IDVDU,IDYDZ  , db3_n_da4);
        deriv.set(IDVDU,IQP_Z  , db3_n_da5);
        deriv.set(IDZDU,IX     , db4_n_da1);
        deriv.set(IDZDU,IY     , db4_n_da2);
        deriv.set(IDZDU,IDXDZ  , db4_n_da3);
        deriv.set(IDZDU,IDYDZ  , db4_n_da4);
        deriv.set(IDZDU,IQP_Z  , db4_n_da5);
        deriv.set(IQP_XY,IX    , db5_n_da1);
        deriv.set(IQP_XY,IY    , db5_n_da2);
        deriv.set(IQP_XY,IDXDZ , db5_n_da3);
        deriv.set(IQP_XY,IDYDZ , db5_n_da4);
        deriv.set(IQP_XY,IQP_Z , db5_n_da5);
        
        return pstat;
    }
    
    
}

