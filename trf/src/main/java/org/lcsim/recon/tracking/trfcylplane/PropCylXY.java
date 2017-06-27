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
import org.lcsim.recon.tracking.trfxyp.SurfXYPlane;
import org.lcsim.recon.tracking.trfxyp.PropXYXY;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.ETrack;

/**
 * Propagates tracks from a Cylinder to a XYPlane in a constant field.
 *<p>
 * Propagation will fail if either the origin is not a Cylinder
 * or destination is not a XYPlane.
 * Propagator works incorrectly for tracks with very small curvatures.
 *<p>
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public class PropCylXY extends PropDirected
{
    
    // Assign track parameter indices.
    
    private static final int IV = SurfXYPlane.IV;
    private static final int IZC   = SurfXYPlane.IZ;
    private static final int IDVDU = SurfXYPlane.IDVDU;
    private static final int IDZDU = SurfXYPlane.IDZDU;
    private static final int IQP_XY  = SurfXYPlane.IQP;
    
    private static final int IPHI = SurfCylinder.IPHI;
    private static final int IZ   = SurfCylinder.IZ;
    private static final int IALF = SurfCylinder.IALF;
    private static final int ITLM = SurfCylinder.ITLM;
    private static final int IQPT  = SurfCylinder.IQPT;
    
    
    // attributes
    
    private double _bfac;
    private PropXYXY _propxyxy;
    
    // static methods
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "PropCylXY"; }
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName(); }
    
    
    
    /**
     *Construct an instance from a constant solenoidal magnetic field in Tesla.
     *
     * @param   bfield The magnetic field strength in Tesla.
     */
    public PropCylXY(double bfield)
    {
        _bfac = TRFMath.BFAC*bfield;
        _propxyxy = new PropXYXY(bfield);
        
    }
    
    /**
     *Clone an instance.
     *
     * @return A Clone of this instance.
     */
    public Propagator newPropagator( )
    {
        return new PropCylXY( bField() );
        
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
        VTrack trv0 = trv; // want to change this vector.
        TrackDerivative deriv1 = new TrackDerivative();
        TrackDerivative deriv2 = new TrackDerivative();
        
        if ( deriv != null ) pstat = vecTransformCylXY( _bfac, trv0, phi_n, dir,deriv1 );
        else pstat = vecTransformCylXY( _bfac, trv0, phi_n, dir, deriv );
        
        if ( ! pstat.success() ) return pstat;
        
        if ( deriv != null ) pstat = _propxyxy.vecDirProp(trv0,srf,dir,deriv2);
        else  pstat = _propxyxy.vecDirProp(trv0,srf,dir, deriv);
        
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
    { return staticType(); }
    
    /**
     *output stream
     * @return  A String representation of this instance.
     */
    public String toString()
    {
        return "Cylinder-XYPlane propagation with constant "
                + bField() + " Tesla field";
        
    }
    
    //**********************************************************************
    
    // Private function to propagate a track without error
    // The corresponding track parameters are:
    // On Cylinder:
    // r (cm) is fixed
    // 0 - phi
    // 1 - z (cm)
    // 2 - alpha = phi_dir - phi; tan(alpha) = r*dphi/dr
    // 3 - sin(lambda) = dz/ds; tan(lambda) = dz/dsT
    // 4 - q/pT (1/GeV/c) (pT is component of p parallel to cylinder)
    // On XYPlane:
    // u (cm) is fixed
    // 0 - v (cm)
    // 1 - z (cm)
    // 2 - dv/du
    // 3 - dz/du
    // 4 - q/p   p is momentum of a track, q is its charge
    // If pderiv is nonzero, return the derivative matrix there.
    
    PropStat
            vecTransformCylXY( double B, VTrack trv, double phi_n,
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
        double c2 = vec.get(IZ);                   // z
        double c3 = vec.get(IALF);                 // alpha
        double c4 = vec.get(ITLM);                 // tan(lambda)
        double c5 = vec.get(IQPT);                 // q/pt
        
        // rotate coordinate system on phi_n
        
        c1 -= phi_n;
        if(c1 < 0.) c1 += TRFMath.TWOPI;
        
        double cos_c1 = Math.cos(c1);
        
        double u = Rcyl*cos_c1;
        if( u < 0. )
        {
            u = - u;
            cos_c1 = - cos_c1;
            c1 -= Math.PI;
            if(c1 < 0.) c1 += TRFMath.TWOPI;
            phi_n = phi_n + Math.PI;
            if( phi_n >= TRFMath.TWOPI) phi_n -= TRFMath.TWOPI;
        }
        
        double sin_c1  = Math.sin(c1);
        double cos_dir = Math.cos(c1+c3);
        double sin_dir = Math.sin(c1+c3);
        double c4_hat2 = 1+c4*c4;
        double c4_hat  = Math.sqrt(c4_hat2);
        
        // check if du == 0 ( that is track moves parallel to the destination plane )
        // du = pt*cos_dir
        if(cos_dir/c5 == 0.) return pstat;
        
        double tan_dir = sin_dir/cos_dir;
        
        double b1 = Rcyl*sin_c1;
        double b2 = c2;
        double b3 = tan_dir;
        double b4 = c4/cos_dir;
        double b5 = c5/c4_hat;
        
        int sign_du = 0;
        if(cos_dir > 0) sign_du =  1;
        if(cos_dir < 0) sign_du = -1;
        
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
        double invert_rsinphi =  (b5*B)*sign_du*b34_hat;
        
        
        // du_dc
        
        double du_dc1 = -Rcyl*sin_c1;
        
        // db1_dc
        
        double db1_dc1 = Rcyl*cos_c1;
        
        // db2_dc
        
        double db2_dc2 = 1.;
        
        // db3_dc
        
        double db3_dc1 = 1./(cos_dir*cos_dir);
        double db3_dc3 = 1./(cos_dir*cos_dir);
        
        // db4_dc
        
        double db4_dc1 = b4*tan_dir;
        double db4_dc3 = b4*tan_dir;
        double db4_dc4 = 1/cos_dir;
        
        // db5_dc
        
        double db5_dc4 = -c4*c5/(c4_hat*c4_hat2);
        double db5_dc5 = 1./c4_hat;
        
        // db3_n_db
        
        double db3_n_du  = -(1. + b3*b3)*invert_rsinphi;
        
        // db4_n_db
        
        double db4_n_du  = -b4*b3*invert_rsinphi;
        
        
        // db1_n_dc
        
        double db1_n_dc1 = db1_dc1 - b3 * du_dc1;
        double db1_n_dc2 = 0.;
        double db1_n_dc3 = 0.;
        double db1_n_dc4 = 0.;
        double db1_n_dc5 = 0.;
        
        // db2_n_dc
        
        double db2_n_dc1 = -b4 * du_dc1;
        double db2_n_dc2 = db2_dc2;
        double db2_n_dc3 = 0.;
        double db2_n_dc4 = 0.;
        double db2_n_dc5 = 0.;
        
        // db3_n_dc
        
        double db3_n_dc1 = db3_dc1 + db3_n_du * du_dc1;
        double db3_n_dc2 = 0.;
        double db3_n_dc3 = db3_dc3;
        double db3_n_dc4 = 0.;
        double db3_n_dc5 = 0.;
        
        // db4_n_dc
        
        double db4_n_dc1 = db4_dc1 + db4_n_du * du_dc1;
        double db4_n_dc2 = 0.;
        double db4_n_dc3 = db4_dc3;
        double db4_n_dc4 = db4_dc4;
        double db4_n_dc5 = 0.;
        
        // db5_n_dc
        
        double db5_n_dc1 = 0.;
        double db5_n_dc2 = 0.;
        double db5_n_dc3 = 0.;
        double db5_n_dc4 = db5_dc4;
        double db5_n_dc5 = db5_dc5;
        
        deriv.set(IV,IPHI      , db1_n_dc1);
        deriv.set(IV,IZ        , db1_n_dc2);
        deriv.set(IV,IALF      , db1_n_dc3);
        deriv.set(IV,ITLM      , db1_n_dc4);
        deriv.set(IV,IQPT      , db1_n_dc5);
        deriv.set(IZ,IPHI      , db2_n_dc1);
        deriv.set(IZ,IZ        , db2_n_dc2);
        deriv.set(IZ,IALF      , db2_n_dc3);
        deriv.set(IZ,ITLM      , db2_n_dc4);
        deriv.set(IZ,IQPT      , db2_n_dc5);
        deriv.set(IDVDU,IPHI   , db3_n_dc1);
        deriv.set(IDVDU,IZ     , db3_n_dc2);
        deriv.set(IDVDU,IALF   , db3_n_dc3);
        deriv.set(IDVDU,ITLM   , db3_n_dc4);
        deriv.set(IDVDU,IQPT   , db3_n_dc5);
        deriv.set(IDZDU,IPHI   , db4_n_dc1);
        deriv.set(IDZDU,IZ     , db4_n_dc2);
        deriv.set(IDZDU,IALF   , db4_n_dc3);
        deriv.set(IDZDU,ITLM   , db4_n_dc4);
        deriv.set(IDZDU,IQPT   , db4_n_dc5);
        deriv.set(IQP_XY,IPHI  , db5_n_dc1);
        deriv.set(IQP_XY,IZ    , db5_n_dc2);
        deriv.set(IQP_XY,IALF  , db5_n_dc3);
        deriv.set(IQP_XY,ITLM  , db5_n_dc4);
        deriv.set(IQP_XY,IQPT  , db5_n_dc5);
        
        return pstat;
    }
    
    
    
}
