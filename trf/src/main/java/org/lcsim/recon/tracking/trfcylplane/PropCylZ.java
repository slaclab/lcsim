package org.lcsim.recon.tracking.trfcylplane;

import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.PropDirected;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.TrackDerivative;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfutil.TRFMath;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfcyl.SurfCylinder;
import org.lcsim.recon.tracking.trfzp.SurfZPlane;
import org.lcsim.recon.tracking.trfzp.PropZZ;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.ETrack;

/**
 * Propagates tracks from a Cylinder to a ZPlane in a constant magnetic field.
 *<p>
 * Propagation will fail if either the origin is not a Cylinder
 * or destination is not a ZPlane.
 * Propagator works incorrectly for tracks with very small curvatures.
 *<p>
 *@author Norman A. Graf
 *@version 1.0
 *
 **/

public class PropCylZ extends PropDirected
{
    
    // Assign track parameter indices.
    private static final int   IX = SurfZPlane.IX;
    private static final int   IY   = SurfZPlane.IY;
    private static final int   IDXDZ = SurfZPlane.IDXDZ;
    private static final int   IDYDZ = SurfZPlane.IDYDZ;
    private static final int   IQP_Z  = SurfZPlane.IQP;
    
    private static final int   IPHI = SurfCylinder.IPHI;
    private static final int   IZ_CYL= SurfCylinder.IZ;
    private static final int   IALF = SurfCylinder.IALF;
    private static final int   ITLM = SurfCylinder.ITLM;
    private static final int   IQPT  = SurfCylinder.IQPT;
    
    
    // attributes
    
    private double _bfac;
    private PropZZ _propzz;
    
    
    // static methods
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "PropCylZ";
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
    public PropCylZ(double bfield)
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
        return new PropCylZ( bField() );
        
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
            PropDir dir, TrackDerivative deriv)
    {
        PropStat pstat = new PropStat();
        Propagator.reduceDirection(dir);
        // Check destination is a ZPlane.
        Assert.assertTrue( srf.pureType().equals(SurfZPlane.staticType()) );
        if ( !srf.pureType( ).equals(SurfZPlane.staticType()) )
            return pstat;
        
        VTrack trv0 = trv; // want to change this vector
        TrackDerivative deriv1 = new TrackDerivative();
        TrackDerivative deriv2 = new TrackDerivative();
        
        if ( deriv != null ) pstat = vecTransformCylZ( _bfac, trv0, dir, deriv1 );
        else pstat = vecTransformCylZ( _bfac, trv0, dir, deriv );
        
        if ( ! pstat.success() ) return pstat;
        
        if ( deriv != null ) pstat = _propzz.vecDirProp(trv0,srf,dir,deriv2);
        else pstat = _propzz.vecDirProp(trv0,srf,dir, deriv);
        
        if ( pstat.success() )
        {
            trv = trv0;
            if ( deriv != null )  deriv.set(deriv2.times(deriv1));
        }
        
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
        return "Cylinder-ZPlane propagation with constant "
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
    // On Cylinder:
    // r (cm) is fixed
    // 0 - phi
    // 1 - z (cm)
    // 2 - alpha = phi_dir - phi; tan(alpha) = r*dphi/dr
    // 3 - sin(lambda) = dz/ds; tan(lambda) = dz/dsT
    // 4 - q/pT (1/GeV/c) (pT is component of p parallel to cylinder)
    // If deriviv is nonzero, return the derivative matrix there.
    
    private PropStat
            vecTransformCylZ( double B, VTrack trv,
            PropDir dir,
            TrackDerivative deriv )
    {
        
        // construct return status
        PropStat pstat = new PropStat();
        
        // fetch the originating surface and vector
        Surface srf1 = trv.surface();
        // TrackVector vec1 = trv.get_vector();
        
        // Check origin is a Cylinder.
        Assert.assertTrue( srf1.pureType().equals(SurfCylinder.staticType()) );
        if ( !srf1.pureType( ).equals(SurfCylinder.staticType()) )
            return pstat;
        SurfCylinder scy1 = ( SurfCylinder ) srf1;
        
        
        // Fetch the R of the cylinder and the starting track vector.
        int ir  = SurfCylinder.RADIUS;
        double Rcyl = scy1.parameter(ir);
        
        TrackVector vec = trv.vector();
        double c1 = vec.get(IPHI);                 // phi
        double c2 = vec.get(IZ_CYL);               // z
        double c3 = vec.get(IALF);                 // alpha
        double c4 = vec.get(ITLM);                 // tan(lambda)
        double c5 = vec.get(IQPT);                 // q/pt
        
        // check that dz != 0
        if(c4 == 0.) return pstat;
        
        double zpos = c2;
        double cos_c1 = Math.cos(c1);
        double sin_c1 = Math.sin(c1);
        double cos_dir = Math.cos(c1+c3);
        double sin_dir = Math.sin(c1+c3);
        double c4_hat2 = 1+c4*c4;
        double c4_hat  = Math.sqrt(c4_hat2);
        
        double a1 = Rcyl*cos_c1;
        double a2 = Rcyl*sin_c1;
        double a3 = cos_dir/c4;
        double a4 = sin_dir/c4;
        double a5 = c5/c4_hat;
        
        
        // if tan(lambda)=dz/dst > 0 : dz>0; dzdst < 0 dz<0
        
        int sign_dz = 0;
        if(c4 > 0) sign_dz =  1;
        if(c4 < 0) sign_dz = -1;
        
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
        // ddphi / dc
        
        double ddphi_dc2 = -B*a5*Math.sqrt(a34_hat2+1.)*sign_dz;
        double ddphi_dc2_nob = -Math.sqrt(a34_hat2+1.)*sign_dz;
        
        // da1 / dc
        
        double da1_dc1 = -Rcyl*sin_c1;
        
        // da2 / dc
        
        double da2_dc1 =  Rcyl*cos_c1;
        
        // da3 / dc
        
        double da3_dc1 = -sin_dir/c4;
        double da3_dc3 = -sin_dir/c4;
        double da3_dc4 = -cos_dir/(c4*c4);
        
        // da4 / dc
        
        double da4_dc1 =  cos_dir/c4;
        double da4_dc3 =  cos_dir/c4;
        double da4_dc4 = -sin_dir/(c4*c4);
        
        // da5 / dc
        
        double da5_dc4 =  -c5*c4/(c4_hat*c4_hat2);
        double da5_dc5 =   1./c4_hat;
        
        // da1n/ dc
        
        double da1n_dc1 = da1_dc1;
        double da1n_dc2 = Rcos_phi*ddphi_dc2_nob;
        double da1n_dc3 = 0.;
        double da1n_dc4 = 0.;
        double da1n_dc5 = 0.;
        
        // da2n/ dc
        
        double da2n_dc1 = da2_dc1;
        double da2n_dc2 = Rsin_phi*ddphi_dc2_nob;
        double da2n_dc3 = 0.;
        double da2n_dc4 = 0.;
        double da2n_dc5 = 0.;
        
        // da3n/ dc
        
        double da3n_dc1 = da3_dc1;
        double da3n_dc2 = - a4*ddphi_dc2;
        double da3n_dc3 = da3_dc3;
        double da3n_dc4 = da3_dc4;
        double da3n_dc5 = 0.;
        
        // da4n/ dc
        
        double da4n_dc1 = da4_dc1;
        double da4n_dc2 = a3*ddphi_dc2;
        double da4n_dc3 = da4_dc3;
        double da4n_dc4 = da4_dc4;
        double da4n_dc5 = 0.;
        
        // da5n
        
        double da5n_dc1 = 0.;
        double da5n_dc2 = 0.;
        double da5n_dc3 = 0.;
        double da5n_dc4 = da5_dc4;
        double da5n_dc5 = da5_dc5;
        
        deriv.set(IX,IPHI      , da1n_dc1);
        deriv.set(IX,IZ_CYL    , da1n_dc2);
        deriv.set(IX,IALF      , da1n_dc3);
        deriv.set(IX,ITLM      , da1n_dc4);
        deriv.set(IX,IQPT      , da1n_dc5);
        deriv.set(IY,IPHI      , da2n_dc1);
        deriv.set(IY,IZ_CYL    , da2n_dc2);
        deriv.set(IY,IALF      , da2n_dc3);
        deriv.set(IY,ITLM      , da2n_dc4);
        deriv.set(IY,IQPT      , da2n_dc5);
        deriv.set(IDXDZ,IPHI   , da3n_dc1);
        deriv.set(IDXDZ,IZ_CYL , da3n_dc2);
        deriv.set(IDXDZ,IALF   , da3n_dc3);
        deriv.set(IDXDZ,ITLM   , da3n_dc4);
        deriv.set(IDXDZ,IQPT   , da3n_dc5);
        deriv.set(IDYDZ,IPHI   , da4n_dc1);
        deriv.set(IDYDZ,IZ_CYL , da4n_dc2);
        deriv.set(IDYDZ,IALF   , da4n_dc3);
        deriv.set(IDYDZ,ITLM   , da4n_dc4);
        deriv.set(IDYDZ,IQPT   , da4n_dc5);
        deriv.set(IQP_Z,IPHI   , da5n_dc1);
        deriv.set(IQP_Z,IZ_CYL , da5n_dc2);
        deriv.set(IQP_Z,IALF   , da5n_dc3);
        deriv.set(IQP_Z,ITLM   , da5n_dc4);
        deriv.set(IQP_Z,IQPT   , da5n_dc5);
        
        return pstat;
    }
}