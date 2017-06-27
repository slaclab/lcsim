package org.lcsim.recon.tracking.trfcylplane;

import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.PropDirected;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.TrackDerivative;
import org.lcsim.recon.tracking.trfzp.PropZZ;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfutil.TRFMath;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfxyp.SurfXYPlane;
import org.lcsim.recon.tracking.trfzp.SurfZPlane;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.ETrack;

/**
 * Propagates tracks from an XYPlane to a ZPlane in a constant magnetic field.
 *<p>
 * Propagation will fail if either the origin is not an XYPlane
 * or destination is not a ZPlane.
 * Propagator works incorrectly for tracks with very small curvatures.
 *<p>
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class PropXYZ extends PropDirected
{
    
    // attributes
    
    private double _bfac;
    private PropZZ _propzz;
    
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
    
    // static methods
    
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "PropXYZ";
    }
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName();
    }
    
    /**
     *Construct an instance from a constant solenoidal magnetic field in Tesla.
     *
     * @param   bfield The magnetic field strength in Tesla.
     */
    public PropXYZ(double bfield)
    {
        _bfac = TRFMath.BFAC*bfield;
        _propzz = new PropZZ(bfield);
    }
    
    
    /**
     *Clone an instance.
     *
     * @return A Clone of this instance.
     */
    public Propagator newPropagator( )
    {
        return new PropXYZ( bField() );
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
    public  PropStat vecDirProp( VTrack trv, Surface srf,
            PropDir dir,TrackDerivative deriv)
    {
        PropStat pstat = new PropStat();
        Propagator.reduceDirection(dir);
        
        // Check destination is a ZPlane.
        Assert.assertTrue( srf.pureType().equals(SurfZPlane.staticType()) );
        if ( !srf.pureType( ).equals(SurfZPlane.staticType()) )
            return pstat;
        
        // Fetch phi of the destination plane
        
        VTrack trv0 = trv; // we wish to modify this track!
        TrackDerivative deriv1 = new TrackDerivative();
        TrackDerivative deriv2 = new TrackDerivative();
        
        if ( deriv != null )  pstat = vecTransformXYZ( _bfac, trv0, dir, deriv1 );
        else pstat = vecTransformXYZ( _bfac, trv0, dir, deriv);
        
        if ( ! pstat.success() ) return pstat;
        
        if ( deriv != null ) pstat = _propzz.vecDirProp(trv0,srf,dir, deriv2);
        else pstat = _propzz.vecDirProp(trv0,srf,dir, deriv);
        
        if ( pstat.success() )
        {
            //trv = trv0;
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
    public  PropStat vecDirProp( VTrack trv, Surface srf,
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
        return "XYPlane-ZPlane propagation with constant "
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
    // If deriv is nonzero, return the derivative matrix there.
    
    private PropStat
            vecTransformXYZ( double B, VTrack trv,
            PropDir dir,
            TrackDerivative deriv)
    {
        
        // construct return status
        PropStat pstat = new PropStat();
        
        // fetch the originating surface and vector
        Surface srf1 = trv.surface();
        // TrackVector vec1 = trv.get_vector();
        
        // Check origin is a XYPlane.
        Assert.assertTrue( srf1.pureType().equals(SurfXYPlane.staticType()) );
        if ( !srf1.pureType( ).equals(SurfXYPlane.staticType()) )
            return pstat;
        SurfXYPlane sxyp1 = (SurfXYPlane) srf1;
        
        // Fetch the dist and phi of the plane and the starting track vector.
        int iphi  = SurfXYPlane.NORMPHI;
        int idist = SurfXYPlane.DISTNORM;
        double sphi = sxyp1.parameter(iphi);
        double    u = sxyp1.parameter(idist);
        TrackVector vec = trv.vector();
        double b1 = vec.get(IV);                  // v
        double b2 = vec.get(IZ);                  // z
        double b3 = vec.get(IDVDU);               // dv/du
        double b4 = vec.get(IDZDU);               // dz/du
        double b5 = vec.get(IQP_XY);              // q/p
        
        // check that dz != 0
        if(b4 == 0.) return pstat;
        
        double zpos = b2;
        double cos_sphi = Math.cos(sphi);
        double sin_sphi = Math.sin(sphi);
        
        double a1 = u*cos_sphi - b1*sin_sphi;
        double a2 = b1*cos_sphi + u*sin_sphi;
        double a3 = cos_sphi/b4 - b3/b4*sin_sphi;
        double a4 = b3/b4*cos_sphi + sin_sphi/b4;
        double a5 = b5;
        
        
        // if dz/du*du/dt > 0 and forward : dz>0; dz/du*du/dt < 0 dz<0
        // if dz/du*du/dt < 0 and backward : dz>0; dz/du*du/dt > 0 dz<0
        
        int sign_du = 0;
        if(trv.isForward())  sign_du =  1;
        if(trv.isBackward()) sign_du = -1;
        if(sign_du == 0)
        {
            System.out.println("PropXYZ._vec_propagate: Unknown direction of a track ");
            System.exit(1);
        }
        
        int sign_dz = 0;
        if(b4*sign_du > 0) sign_dz =  1;
        if(b4*sign_du < 0) sign_dz = -1;
        
        vec.set(IX     , a1);
        vec.set(IY     , a2);
        vec.set(IDXDZ  , a3);
        vec.set(IDYDZ  , a4);
        vec.set(IQP_Z  , a5);
        
        // Update trv
        SurfZPlane szp = new SurfZPlane(zpos);
        trv.setSurface(szp.newPureSurface());
        trv.setVector(vec);
        
        // set new direction of the track
        if(sign_dz ==  1) trv.setForward();
        if(sign_dz == -1) trv.setBackward();
        
        // Set the return status.
        pstat.setSame();
        
        // exit now if user did not ask for error matrix.
        if ( deriv == null ) return pstat;
        
        double a34_hat = sign_dz*Math.sqrt(a3*a3 + a4*a4);
        double a34_hat2 = a34_hat*a34_hat;
        
        double Rcos_phi = a3/Math.sqrt(1.+a34_hat2)*sign_dz;
        double Rsin_phi = a4/Math.sqrt(1.+a34_hat2)*sign_dz;
        
        // ddphi / db
        
        double ddphi_db2 = -B*a5*Math.sqrt(a34_hat2+1.)*sign_dz;
        double ddphi_db2_nob = -Math.sqrt(a34_hat2+1.)*sign_dz;
        
        // da3 / db
        
        double da3_db3 = -sin_sphi/b4;
        double da3_db4 = (-cos_sphi + b3*sin_sphi)/(b4*b4);
        
        // da4 / db
        
        double da4_db3 =  cos_sphi/b4;
        double da4_db4 = (- sin_sphi - b3*cos_sphi)/(b4*b4);
        
        // da1/ db
        
        double da1n_db1 = -sin_sphi;
        double da1n_db2 = Rcos_phi*ddphi_db2_nob;
        double da1n_db3 = 0.;
        double da1n_db4 = 0.;
        double da1n_db5 = 0.;
        
        // da2/ db
        
        double da2n_db1 = cos_sphi;
        double da2n_db2 = Rsin_phi*ddphi_db2_nob;
        double da2n_db3 = 0.;
        double da2n_db4 = 0.;
        double da2n_db5 = 0.;
        
        // da3/ db
        
        double da3n_db1 = 0.;
        double da3n_db2 = -a4*ddphi_db2;
        double da3n_db3 = da3_db3;
        double da3n_db4 = da3_db4;
        double da3n_db5 = 0.;
        
        // da4/ db
        
        double da4n_db1 = 0.;
        double da4n_db2 = a3*ddphi_db2;
        double da4n_db3 = da4_db3;
        double da4n_db4 = da4_db4;
        double da4n_db5 = 0.;
        
        // da5n
        
        double da5n_db1 = 0.;
        double da5n_db2 = 0.;
        double da5n_db3 = 0.;
        double da5n_db4 = 0.;
        double da5n_db5 = 1.;
        
        deriv.set(IX,IV        , da1n_db1);
        deriv.set(IX,IZ        , da1n_db2);
        deriv.set(IX,IDVDU     , da1n_db3);
        deriv.set(IX,IDZDU     , da1n_db4);
        deriv.set(IX,IQP_XY    , da1n_db5);
        deriv.set(IY,IV        , da2n_db1);
        deriv.set(IY,IZ        , da2n_db2);
        deriv.set(IY,IDVDU     , da2n_db3);
        deriv.set(IY,IDZDU     , da2n_db4);
        deriv.set(IY,IQP_XY    , da2n_db5);
        deriv.set(IDXDZ,IV     , da3n_db1);
        deriv.set(IDXDZ,IZ     , da3n_db2);
        deriv.set(IDXDZ,IDVDU  , da3n_db3);
        deriv.set(IDXDZ,IDZDU  , da3n_db4);
        deriv.set(IDXDZ,IQP_XY , da3n_db5);
        deriv.set(IDYDZ,IV     , da4n_db1);
        deriv.set(IDYDZ,IZ     , da4n_db2);
        deriv.set(IDYDZ,IDVDU  , da4n_db3);
        deriv.set(IDYDZ,IDZDU  , da4n_db4);
        deriv.set(IDYDZ,IQP_XY , da4n_db5);
        deriv.set(IQP_Z,IV     , da5n_db1);
        deriv.set(IQP_Z,IZ     , da5n_db2);
        deriv.set(IQP_Z,IDVDU  , da5n_db3);
        deriv.set(IQP_Z,IDZDU  , da5n_db4);
        deriv.set(IQP_Z,IQP_XY , da5n_db5);
        
        return pstat;
    }
    
}

