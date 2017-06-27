package  org.lcsim.recon.tracking.trfzp;

import org.lcsim.recon.tracking.trfutil.TRFMath;
import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.PropDirected;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.TrackDerivative;
/**
 * Propagates tracks from one ZPlane to another in a constant field.
 *<p>
 * Propagation will fail if either the origin or destination is
 * not a ZPlane.
 * Propagator works incorrectly for tracks with very small curvatures
 *
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class PropZZ extends PropDirected
{
    
    // attributes
    
    private double _bfac;
    
    // Assign track parameter indices.
    
    private static final int IX = SurfZPlane.IX;
    private static final int IY   = SurfZPlane.IY;
    private static final int IDXDZ = SurfZPlane.IDXDZ;
    private static final int IDYDZ = SurfZPlane.IDYDZ;
    private static final int IQP  = SurfZPlane.IQP;
    
    
    // static methods
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "PropZZ"; }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName(); }
    
    
    
    //
    
    /**
     *Construct an instance from a constant solenoidal magnetic field in Tesla.
     *
     * @param   bfield The magnetic field strength in Tesla.
     */
    public PropZZ(double bfield)
    {
        _bfac = TRFMath.BFAC*bfield;
    }
    
    //
    
     /**
     *Clone an instance.
     *
     * @return A Clone of this instance.
     */
    public Propagator newPropagator( )
    {
        return new PropZZ( bField() );
    }
    
    //
    
    /**
     *Propagate a track without error in the specified direction.
     *
     * @param   trv The VTrack to propagate.
     * @param   srf The Surface to which to propagate.
     * @param   dir The direction in which to propagate.
     * @return The propagation status.
     */
    public PropStat vecDirProp( VTrack trv, Surface srf,
    PropDir dir)
    {
        TrackDerivative pder = null;
        return vecDirProp(trv, srf, dir, pder);
        
    }
    
    
    /**
     *Propagate a track without error in the specified direction
     *and return the derivative matrix in deriv.
     *
     * @param   trv The VTrack to propagate.
     * @param   srf The Surface to which to propagate.
     * @param   dir The direction in which to propagate.
     * @param   pder The track derivatives to update at the surface srf.
     * @return The propagation status.
     */
    public PropStat vecDirProp( VTrack trv, Surface srf,
    PropDir dir, TrackDerivative pder )
    {
        return vec_propagatezz_( _bfac, trv, srf, dir , pder );
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
    
    //
    
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
     *
     * @return  A String representation of this instance.
     */
    public String toString()
    {
        return "ZPlane-ZPlane propagation with constant "
        +bField() +" Tesla field";
        
    }
    
    
    // Private function to propagate a track without error
    // The corresponding track parameters are:
    // z (cm) is fixed
    // 0 - x (cm)
    // 1 - y (cm)
    // 2 - dx/dz
    // 3 - dy/dz
    // 4 - q/p   p is momentum of a track, q is its charge
    // If pderiv is nonzero, return the derivative matrix there.
    
    PropStat
    vec_propagatezz_( double B, VTrack trv, Surface srf,
    PropDir dir1,
    TrackDerivative deriv )
    {
        
        // construct return status
        PropStat pstat = new PropStat();
        PropDir dir = dir1; //need to check this
        boolean move = Propagator.reduceDirection(dir);
        if(move) dir = reduce(dir);
        // fetch the originating surface and vector
        Surface srf1 = trv.surface();
        // TrackVector vec1 = trv.get_vector();
        
        // Check origin is a ZPlane.
        Assert.assertTrue( srf1.pureType().equals(SurfZPlane.staticType()) );
        if ( !srf1.pureType( ).equals(SurfZPlane.staticType()) )
            return pstat;
        
        SurfZPlane szp1 = (SurfZPlane) srf1;
        
        // Check destination is a ZPlane.
        Assert.assertTrue( srf.pureType().equals(SurfZPlane.staticType()) );
        if ( !srf.pureType( ).equals(SurfZPlane.staticType()) )
            return pstat;
        SurfZPlane szp2 = (SurfZPlane) srf;
        
        // If surfaces are the same, we can return now.
        if ( srf.pureEqual(srf1) )
        {
            // if XXX_MOVE was requested , return fail because track doesn't cross
            // Z place in more than one place
            if ( move ) return new PropStat();
            if(deriv!=null)
            {
                deriv.setIdentity();
            }
            pstat.setSame();
            return pstat;
        }
        
        if( Math.abs(B) < 1e-7 ) return _zeroBField(trv,szp1,szp2,dir1,deriv);
        
        // Fetch the zpos's of the planes and the starting track vector.
        int izpos  = SurfZPlane.ZPOS;
        
        double zpos_0 = szp1.parameter(izpos);
        double zpos_n = szp2.parameter(izpos);
        
        // Calculate difference in zpos
        
        double dz = zpos_n - zpos_0;
        
        TrackVector vec = trv.vector();
        double a1 = vec.get(IX);                  // x
        double a2 = vec.get(IY);                  // y
        double a3 = vec.get(IDXDZ);               // dx/dz
        double a4 = vec.get(IDYDZ);               // dy/dz
        double a5 = vec.get(IQP);                 // q/p
        
        int sign_dz = 0;
        if(trv.isForward())  sign_dz =  1;
        if(trv.isBackward()) sign_dz = -1;
        if(sign_dz == 0)
        {
            System.out.println("PropZZ._vec_propagate: Unknown direction of a track ");
            System.exit(1);
        }
        
        // if delta_z*dz/dt >0 and backward - fail
        // if delta_z*dz/dt <0 and forward  - fail
        
        int flag_forward = 0;
        
        if(dir.equals(PropDir.NEAREST))
        {
            if(sign_dz*dz>0.) flag_forward = 1;
            else flag_forward = -1;
        }
        
        else if (dir.equals(PropDir.FORWARD) )
        {
            
            //   z-z propagation failed
            if(sign_dz*dz<0.) return pstat;
            flag_forward =  1;
        }
        else if (dir.equals(PropDir.BACKWARD) )
        {
            
            //   z-z propagation failed
            if(sign_dz*dz>0.) return pstat;
            flag_forward = -1;
        }
        else
        {
            System.out.println( "PropZZ._vec_propagate: Unknown direction.");
            System.exit(1);
        }
        
        double a34_hat = sign_dz*Math.sqrt(a3*a3 + a4*a4);
        double a34_hat2 = a34_hat*a34_hat;
        double dphi = B*dz*a5*Math.sqrt(a34_hat2+1.)*sign_dz;
        double cos_dphi = Math.cos(dphi);
        double sin_dphi = Math.sin(dphi);
        
        
        double Rcos_phi = 1./(B*a5)*a3/Math.sqrt(1.+a34_hat2)*sign_dz;
        double Rsin_phi = 1./(B*a5)*a4/Math.sqrt(1.+a34_hat2)*sign_dz;
        
        double a1n = a1 + Rsin_phi*(cos_dphi - 1) + Rcos_phi*sin_dphi;
        double a2n = a2 - Rcos_phi*(cos_dphi - 1) + Rsin_phi*sin_dphi;
        double a3n =   a3*cos_dphi - a4*sin_dphi;
        double a4n =   a3*sin_dphi + a4*cos_dphi;
        double a5n = a5;
        
        vec.set(IX, a1n);
        vec.set(IY, a2n);
        vec.set(IDXDZ, a3n);
        vec.set(IDYDZ, a4n);
        vec.set(IQP, a5n);
        
        // Update trv
        trv.setSurface(srf.newPureSurface());
        trv.setVector(vec);
        
        // set new direction of the track
        
        if(sign_dz ==  1) trv.setForward();
        if(sign_dz == -1) trv.setBackward();
        
        // Calculate the path distance s.
        double ctlamsq = a3*a3 + a4*a4;
        double slam_inv = sign_dz*Math.sqrt(1.0+ctlamsq);
        double ds = dz*slam_inv;
        
        // Set the return status.
        //(flag_forward==1)?pstat.set_forward():pstat.set_backward();
        pstat.setPathDistance(ds);
        
        // Check the direction of propgation is consistent with the old
        // calculation.
        if ( flag_forward == 1 ) Assert.assertTrue( pstat.forward() );
        else Assert.assertTrue( pstat.backward() );
        
        // exit now if user did not ask for error matrix.
        if ( deriv == null ) return pstat;
        
        //da34_hat
        
        double da34_hat_da3 = 0.;
        double da34_hat_da4 = 0.;
        if(Math.abs(a3)>=Math.abs(a4))
        {
            int sign3=1;
            if(a3<0) sign3 = -1;
            if(a3 !=0.)
            {
                da34_hat_da3 = sign_dz*sign3/Math.sqrt(1.+(a4/a3)*(a4/a3) );
                da34_hat_da4 = sign_dz*(a4/Math.abs(a3))/Math.sqrt(1.+(a4/a3)*(a4/a3) );
            }
            else
            {
                da34_hat_da3 = 0.;
                da34_hat_da4 = 0.;
            }
        }
        if(Math.abs(a4)>Math.abs(a3))
        {
            int sign4=1;
            if(a4<0) sign4 = -1;
            if(a4 !=0.)
            {
                da34_hat_da3 = sign_dz*(a3/Math.abs(a4))/Math.sqrt(1.+(a3/a4)*(a3/a4) );
                da34_hat_da4 = sign_dz*sign4/Math.sqrt(1.+(a3/a4)*(a3/a4) );
            }
            else
            {
                da34_hat_da3 = 0.;
                da34_hat_da4 = 0.;
            }
        }
        
        // ddphi
        
        double ddphi_da3 = B*dz*a5*a34_hat*sign_dz/Math.sqrt(1.+a34_hat2)*da34_hat_da3;
        double ddphi_da4 = B*dz*a5*a34_hat*sign_dz/Math.sqrt(1.+a34_hat2)*da34_hat_da4;
        double ddphi_da5 = B*dz*Math.sqrt(a34_hat2+1.)*sign_dz;
        
        // dRsin_phi
        
        
        double dRsin_phi_da3 = -Rsin_phi*a34_hat/(1.+a34_hat2)*da34_hat_da3;
        double dRsin_phi_da4 = -Rsin_phi*a34_hat/(1.+a34_hat2)*da34_hat_da4 +
        sign_dz/(B*a5)/Math.sqrt(1.+a34_hat2);
        double dRsin_phi_da5 = -Rsin_phi/a5;
        
        // dRcos_phi
        
        
        double dRcos_phi_da3 = -Rcos_phi*a34_hat/(1.+a34_hat2)*da34_hat_da3 +
        sign_dz/(B*a5)/Math.sqrt(1.+a34_hat2);
        double dRcos_phi_da4 = -Rcos_phi*a34_hat/(1.+a34_hat2)*da34_hat_da4;
        double dRcos_phi_da5 = -Rcos_phi/a5;
        
        // da1n first two are simple because dR,dphi _da1,_da2 = 0.
        
        double da1n_da1 = 1.;
        double da1n_da2 = 0.;
        double da1n_da3 = dRsin_phi_da3*(cos_dphi-1.) + dRcos_phi_da3*sin_dphi
        - Rsin_phi*sin_dphi*ddphi_da3 + Rcos_phi*cos_dphi*ddphi_da3;
        double da1n_da4 = dRsin_phi_da4*(cos_dphi-1.) + dRcos_phi_da4*sin_dphi
        - Rsin_phi*sin_dphi*ddphi_da4 + Rcos_phi*cos_dphi*ddphi_da4;
        double da1n_da5 = dRsin_phi_da5*(cos_dphi-1.) + dRcos_phi_da5*sin_dphi
        - Rsin_phi*sin_dphi*ddphi_da5 + Rcos_phi*cos_dphi*ddphi_da5;
        
        // da2n first two are simple because dR,dphi _da1,_da2 = 0.
        
        double da2n_da1 = 0.;
        double da2n_da2 = 1.;
        double da2n_da3 = -dRcos_phi_da3*(cos_dphi-1.) + dRsin_phi_da3*sin_dphi
        + Rcos_phi*sin_dphi*ddphi_da3 + Rsin_phi*cos_dphi*ddphi_da3;
        double da2n_da4 = -dRcos_phi_da4*(cos_dphi-1.) + dRsin_phi_da4*sin_dphi
        + Rcos_phi*sin_dphi*ddphi_da4 + Rsin_phi*cos_dphi*ddphi_da4;
        double da2n_da5 = -dRcos_phi_da5*(cos_dphi-1.) + dRsin_phi_da5*sin_dphi
        + Rcos_phi*sin_dphi*ddphi_da5 + Rsin_phi*cos_dphi*ddphi_da5;
        
        // da3n first two are simple because dphi _da1,_da2 = 0.
        
        double da3n_da1 = 0.;
        double da3n_da2 = 0.;
        double da3n_da3 = cos_dphi - a3*sin_dphi*ddphi_da3 - a4*cos_dphi*ddphi_da3;
        double da3n_da4 = - sin_dphi - a3*sin_dphi*ddphi_da4 - a4*cos_dphi*ddphi_da4;
        double da3n_da5 = - a3*sin_dphi*ddphi_da5 - a4*cos_dphi*ddphi_da5;
        
        // da4n first two are simple because dphi _da1,_da2 = 0.
        
        double da4n_da1 = 0.;
        double da4n_da2 = 0.;
        double da4n_da3 =   sin_dphi + a3*cos_dphi*ddphi_da3 - a4*sin_dphi*ddphi_da3;
        double da4n_da4 =   cos_dphi + a3*cos_dphi*ddphi_da4 - a4*sin_dphi*ddphi_da4;
        double da4n_da5 =   a3*cos_dphi*ddphi_da5 - a4*sin_dphi*ddphi_da5;
        
        // da5n
        
        double da5n_da1 = 0.;
        double da5n_da2 = 0.;
        double da5n_da3 = 0.;
        double da5n_da4 = 0.;
        double da5n_da5 = 1.;
        
        deriv.set(IX,IX       ,da1n_da1);
        deriv.set(IX,IY       ,da1n_da2);
        deriv.set(IX,IDXDZ    ,da1n_da3);
        deriv.set(IX,IDYDZ    ,da1n_da4);
        deriv.set(IX,IQP      ,da1n_da5);
        deriv.set(IY,IX       ,da2n_da1);
        deriv.set(IY,IY       ,da2n_da2);
        deriv.set(IY,IDXDZ    ,da2n_da3);
        deriv.set(IY,IDYDZ    ,da2n_da4);
        deriv.set(IY,IQP      ,da2n_da5);
        deriv.set(IDXDZ,IX    ,da3n_da1);
        deriv.set(IDXDZ,IY    ,da3n_da2);
        deriv.set(IDXDZ,IDXDZ ,da3n_da3);
        deriv.set(IDXDZ,IDYDZ ,da3n_da4);
        deriv.set(IDXDZ,IQP   ,da3n_da5);
        deriv.set(IDYDZ,IX    ,da4n_da1);
        deriv.set(IDYDZ,IY    ,da4n_da2);
        deriv.set(IDYDZ,IDXDZ ,da4n_da3);
        deriv.set(IDYDZ,IDYDZ ,da4n_da4);
        deriv.set(IDYDZ,IQP   ,da4n_da5);
        deriv.set(IQP,IX      ,da5n_da1);
        deriv.set(IQP,IY      ,da5n_da2);
        deriv.set(IQP,IDXDZ   ,da5n_da3);
        deriv.set(IQP,IDYDZ   ,da5n_da4);
        deriv.set(IQP,IQP     ,da5n_da5);
        
        return pstat;
        
    }
    
    private PropStat _zeroBField( VTrack trv, SurfZPlane szp1,
    SurfZPlane szp2,PropDir dir1,
    TrackDerivative deriv)
    {
        PropStat pstat = new PropStat();
        PropDir dir = dir1; //need to check constness
        boolean move = Propagator.reduceDirection(dir);
        boolean same = szp2.pureEqual(szp1);
        
        // There is only one solution. Can't XXX_MOVE
        if ( same && move ) return pstat;
        
        if ( same )
        {
            if(deriv != null)
            {
                
                deriv.setIdentity();
            }
            pstat.setSame();
            return pstat;
        }
        
        TrackVector vec = trv.vector();
        double x0 = vec.get(IX);
        double y0 = vec.get(IY);
        double dxdz0 = vec.get(IDXDZ);
        double dydz0 = vec.get(IDYDZ);
        
        double dz0 =1.;
        if( trv.isBackward() ) dz0 = -1.;
        
        double z0 = szp1.parameter(SurfZPlane.ZPOS);
        
        double z1 = szp2.parameter(SurfZPlane.ZPOS);
        
        double a = dxdz0*dz0;
        double b = dydz0*dz0;
        double c = dz0;
        
        double ap = 0.;
        double bp = 0.;
        double cp = 1.0;
        
        double xp = 0.;
        double yp = 0.;
        double zp = z1;
        
        double denom = a*ap + b*bp + c*cp;
        
        if( denom == 0. ) return pstat;
        
        double S = ( (xp-x0)*ap + (yp-y0)*bp + (zp-z0)*cp )/denom;
        
        double x1 = x0 + S*a;
        double y1 = y0 + S*b;
        
        boolean forward = S > 0. ? true : false;
        
        if( dir == PropDir.FORWARD && !forward ) return pstat;
        if( dir == PropDir.BACKWARD && forward ) return pstat;
        
        
        double dxdz1 = (x1-x0)/(z1-z0);
        double dydz1 = (y1-y0)/(z1-z0);
        
        vec.set(IX, x1);
        vec.set(IY, y1);
        vec.set(IDXDZ, dxdz1);
        vec.set(IDYDZ, dydz1);
        
        trv.setSurface( szp2.newPureSurface());
        trv.setVector(vec);
        if( dz0 >0 ) trv.setForward();
        else trv.setBackward();
        
        double ds = S*Math.sqrt(a*a+b*b+c*c);
        
        pstat.setPathDistance(ds);
        
        if( deriv == null ) return pstat;
        
        
        // S= (zp-z0)/dz0
        // a(dxdz dz0) b(dydz dz0) c(dz0)
        
        
        double da_dxdz0 = dz0;
        double db_dydz0 = dz0;
        
        double dx1dx0 =1.;
        double dy1dy0 =1.;
        
        double dx1_dxdz0 = S*da_dxdz0;
        double dy1_dydz0 = S*db_dydz0;
        
        double dxdz1_dx0 = (dx1dx0 - 1.)/(z1-z0);
        double dydz1_dy0 = (dy1dy0 - 1.)/(z1-z0);
        
        double dxdz1_dxdz0 = dx1_dxdz0/(z1-z0);
        double dydz1_dydz0 = dy1_dydz0/(z1-z0);
        
        
        deriv.setIdentity();
        
        deriv.set(IX,IX, dx1dx0);
        deriv.set(IX,IDXDZ, dx1_dxdz0);
        
        deriv.set(IY,IY, dy1dy0);
        deriv.set(IY,IDYDZ, dy1_dydz0);
        
        deriv.set(IDXDZ,IX, dxdz1_dx0);
        deriv.set(IDXDZ,IDXDZ, dxdz1_dxdz0);
        
        deriv.set(IDYDZ,IY, dydz1_dy0);
        deriv.set(IDYDZ,IDYDZ, dydz1_dydz0);
        
        deriv.set(IQP,IQP, 1.0);
        
        return pstat;
        
    }
    
    
    
}
