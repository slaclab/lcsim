package org.lcsim.recon.tracking.trfxyp;
/**
 * Propagates tracks from one XYPlane to another in a constant magnetic field.
 * in the x^ direction.
 *
 * Propagation will fail if either the origin or destination is
 * not an XYPlane.
 */
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
 * Propagates tracks from one XYPlane to another in a constant field.
 * Magnetic field is in x^ direction
 *<p>
 * Propagation will fail if either the origin or destination is
 * not an XYPlane.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */

public class PropXYXYBX extends PropDirected
{
    
    // attributes
    
    private double _bfac;
    
    private static final int    IV = SurfXYPlane.IV;
    private static final int   IZ   = SurfXYPlane.IZ;
    private static final int   IDVDU = SurfXYPlane.IDVDU;
    private static final int   IDZDU = SurfXYPlane.IDZDU;
    private static final int   IQP  = SurfXYPlane.IQP;
    
    
    private boolean debug;
    
    // static methods
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    {
        return "PropXYXYBX";
    }
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    {
        return typeName();
    }
    
    
    
    /**
     *Construct an instance from a constant solenoidal magnetic field in Tesla.
     *
     * @param   bfield The magnetic field strength in Tesla.
     */
    public PropXYXYBX(double bfield)
    {
        _bfac = TRFMath.BFAC*bfield;
    }
    
    /**
     *Clone an instance.
     *
     * @return A Clone of this instance.
     */
    public Propagator newPropagator( )
    {
        return new PropXYXYBX( bField() );
    }
    
    
    /**
     *Propagate a track without error in the specified direction
     *and return the derivative matrix in deriv.
     *
     * @param   trv The VTrack to propagate.
     * @param   srf The Surface to which to propagate.
     * @param   dir The direction in which to propagate.
     * @param   deriv The track derivatives to update at the surface srf.
     * @return The propagation status.
     */
    public PropStat vecDirProp( VTrack trv, Surface srf,
            PropDir dir,TrackDerivative deriv )
    {
        PropStat pstat = vecPropagateXYXYBX( _bfac, trv, srf, dir, deriv );
        return pstat;
    }
    
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
    {
        return staticType();
    }
    
    /**
     *output stream
     *
     * @return  A String representation of this instance.
     */
    public String toString()
    {
        return "XYPlane-XYPlane propagation with constant "
                + bField() + " Tesla field in x^ direction";
    }
    
    
    // function to find t along the trajectory . B^ || x^
    // Return -flag_forward if no solution can be found
    
    private double direction(int flag_forward,
            double w,
            double r_sinalf,double r_cosalf,
            double b1, double u_n, double u,
            int sign_du,
            double sinphi, double cosphi)
    {
        
        Assert.assertTrue(flag_forward == 1 || flag_forward == -1);
        
        // if( cosphi == 0. )  //cng 01/11/01
        if (Math.abs(cosphi)<1.e-14)
        {
            double a1 = u_n - (b1 - r_sinalf)*sinphi;
            double a2 = r_cosalf*sinphi;
            double a3 = r_sinalf*sinphi;
            if(w<0 )
            {
                w = -w; // w is always greater than 0
                a2 = -a2;
            }
            if(flag_forward < 0)
            {
                a2 = -a2;
            }
            // equation to solve is a1 - a2*sin(w*t) - a3*cos(w*t) = 0
            double b = Math.sqrt(a2*a2+a3*a3);
            double phi = Math.acos(-a3/b);
            Assert.assertTrue(phi<=Math.PI && phi>=0.);
            if(a2<0) phi = TRFMath.TWOPI - phi;
            // equation to solve is b*cos(w*t+phi)  = -a1
            if(Math.abs(a1)> b || b==0.)
            {
                // no solution
                return -1.*flag_forward;
            }
            double phi1 = Math.acos(-a1/b);
            Assert.assertTrue(phi1<=Math.PI && phi1>=0.);
            
            double t = (phi1-phi)/w;
            if(t > 0) return t*flag_forward;
            t = (TRFMath.TWOPI - phi1 - phi)/w;
            if(t > 0) return t*flag_forward;
            t = (TRFMath.TWOPI + phi1 - phi)/w;
            Assert.assertTrue(t>=0.);
            return t*flag_forward;
        }
        Assert.assertTrue(sign_du == 1 || sign_du == -1);
        Assert.assertTrue(flag_forward == 1 || flag_forward == -1);
        
        double eps = 1.e-10; // precision of the solution
        double x_start,x_var,x_fix;
        double f_start,f_var,f_fix,f_deriv;
        double t_start;
        
        // double next_zero(double);
        // double next_min(double);
        // double next_max(double);
        // double next_1(double,double,double);
        
        double a1 = (u_n - u*cosphi - (b1 - r_sinalf)*sinphi)/(cosphi*sign_du);
        double a2 = r_cosalf*sinphi / (cosphi*sign_du);
        double a3 = r_sinalf*sinphi / (cosphi*sign_du);
        
        
        if(flag_forward == -1)
        {
            a1 = -a1;
            a3 = -a3;
        }
        if(w<0 )
        {
            w = -w; // w is always greater than 0
            a2 = -a2;
        }
        // now equation to solve is t = f(t) = a1 - a2sin(wt) - a3cos(wt)
        
        double b = Math.sqrt(a2*a2+a3*a3);
        
        if(b==0.) return a1*flag_forward; // solution is very easy to find
        
        // now equation to solve is x = t - a1 = f(x) = bcos(w(x+a1) + phi)
        // f(-a1) = b*cos(phi)
        
        if( -a1 > b)
        {
            // no solution
            return -1.*flag_forward;
        }
        
        double phi = Math.acos(-a3/b);
        if(a2<0) phi = TRFMath.TWOPI - phi;
        Assert.assertTrue(phi<=TRFMath.TWOPI && phi>=0.);
        if( -a1 >= -b)
        {
            t_start = 0.;
            f_start = -a3;
            f_deriv = -a2*w;
        }
        else
        {
            t_start = a1 - b;
            
            phi = phi + w*t_start;
            
            for(;phi>=TRFMath.TWOPI;phi -= TRFMath.TWOPI);
            
            Assert.assertTrue(phi<=TRFMath.TWOPI && phi>=0.);
            f_start =  b*Math.cos(phi);
            f_deriv = -b*Math.sin(phi)*w;
        }
        
        // now we start at t = - b + a1 , f(-b) = bcos(w(x+b) + phi) = bcos(phi)
        
        x_start = t_start - a1;
        
        if( (f_start - x_start)<=0 )
        {
            double dphi = next1(phi,b,w);
            if(debug) System.out.println("dphi= "+dphi);
            if(dphi ==-1.) return -1.*flag_forward; //no solution
            x_var = x_start + dphi/w;
            f_var = b*Math.cos((x_var-x_start)*w+phi);
            x_fix = x_start;
            f_fix = f_start;
            if(f_deriv <= 1)
            {
                dphi  = next1(phi+dphi,b,w);
                
                x_fix = x_var + dphi/w;
                f_fix = b*Math.cos((x_fix-x_start)*w+phi);
            }
            if( (f_fix - x_fix)*(f_var-x_var)>0) return -1.*flag_forward; //no solution
        } // end of first case
        else
        {
            double dphi = next1(phi,b,w);
            if(debug) System.out.println("dphi= "+dphi);
            if(dphi == -1.)
            {
                x_var = x_start + nextMax(phi)/w;
                f_var = b;
                x_fix = x_start;
                f_fix = f_start;
            } // end of case where f_deriv < 1 always
            else
            {
                x_var = x_start + dphi/w;
                f_var = b*Math.cos((x_var-x_start)*w+phi);
                x_fix = x_start;
                f_fix = f_start;
                // if((f_deriv >= 1)||(f_fix - x_fix)*(f_var-x_var)>0) //cng 01/11/01
                double tol = f_deriv-1;
                if(debug) System.out.println("tol= "+tol);
                if(debug) System.out.println((f_fix - x_fix)*(f_var-x_var));
                if( (tol>-1e-10)||(f_fix - x_fix)*(f_var-x_var)>0)
                {
                    dphi  += next1(phi+dphi,b,w);
                    
                    x_fix = x_start + dphi/w;
                    f_fix = b*Math.cos((x_fix-x_start)*w+phi);
                }
                if( (f_fix - x_fix)*(f_var-x_var)>0)
                {
                    
                    dphi  += next1(phi+dphi,b,w);
                    x_var = x_start + dphi/w;
                    f_var = b*Math.cos((x_var-x_start)*w+phi);
                }
            } // end of case where f_deriv > 1 sometimes
            if(debug) System.out.println("f_fx "+(f_fix - x_fix)*(f_var-x_var));
            Assert.assertTrue( (f_fix - x_fix)*(f_var-x_var)<=0 ); //no solution
        } // end of second case
        int n = 0;
        double x_var_prev,f_var_prev;
        while ( Math.abs(f_var - x_var) > eps)
        {
            n++;
            Assert.assertTrue(n < 1000);
            x_var_prev = x_var;
            f_var_prev = f_var;
            Assert.assertTrue((x_fix-x_var+f_var-f_fix)!=0.);
            x_var = (f_var*x_fix - f_fix*x_var)/((x_fix-x_var)-(f_fix - f_var));
            f_var = b*Math.cos(w*(x_var-x_start)+phi);
            if((f_fix - x_fix)*(f_var-x_var)>0 )
            {
                x_fix = x_var_prev;
                f_fix = f_var_prev;
            }
            Assert.assertTrue((f_fix - x_fix)*(f_var-x_var)<=0 );
        }
        
        Assert.assertTrue(x_var >= x_start);
        // return t
        return (x_var + a1)*flag_forward;
    }
    
    
    // Return delta_phi to next min
    // -1 if something is wrong
    
    private double nextMin(double phi)
    {
        if(   (Math.PI - phi)> 0.) return (  Math.PI - phi);
        if( (3*Math.PI - phi)> 0.) return (3*Math.PI - phi);
        return -1.;
    }
    
    // Return delta_phi to next max.
    // -1 if something is wrong
    
    private double nextMax(double phi)
    {
        if((TRFMath.TWOPI - phi)> 0.) return ( TRFMath.TWOPI - phi);
        return -1.;
    }
    
    // Return delta_phi to next 0.
    // -1 if something is wrong
    
    private double nextZero(double phi)
    {
        if(   (Math.PI/2. - phi)> 0.) return (  Math.PI/2. - phi);
        if( (3*Math.PI/2. - phi)> 0.) return (3*Math.PI/2. - phi);
        if( (5*Math.PI/2. - phi)> 0.) return (5*Math.PI/2. - phi);
        return -1.;
    }
    
    // Return delta_phi to next point where first derivative is 1.
    // -1 if something is wrong
    
    private double next1(double phi,double amp,double w)
    {
        int n,si;
        if(Math.abs(amp*w)<1.) return -1.;
        if(debug) System.out.println("amp= "+amp+", w= "+w);
        double phi1 = -Math.asin(1/(amp*w));
        Assert.assertTrue(phi1>= -Math.PI/2. && phi1 <= Math.PI/2.);
        
        double delta = phi1-phi;
        if(debug) System.out.println("phi1= "+phi1+", phi= "+phi+", delta= "+delta);
                /* si = -1;
                // for(n=1,si=-1; delta <= 0;n++,si *=-1)
                for(n = 1; delta<=0; n++)
                {
                Assert.assertTrue(n<10);
                delta = n*Math.PI + si*phi1 - phi;
                if(debug) System.out.println("n= "+n+", si= "+si+", delta= "+delta);
                si *= -1;
                 
                }
                 */
        si = -1;
        n = 1;
        while (delta>0.)
        {
            Assert.assertTrue(n<10);
            delta = n*Math.PI + si*phi1 - phi;
            if(debug) System.out.println("n= "+n+", si= "+si+", delta= "+delta);
            n++;
            si *= -1;
            
        }
        if(debug) System.out.println("delta= "+delta);
        if(debug) System.out.println( "in next_1 "+Math.abs(-amp*Math.sin(phi+delta)*w ));
        if(debug) System.out.println( "in next_1 "+Math.abs(-amp*Math.sin(phi+delta)*w - 1));
        Assert.assertTrue( Math.abs(-amp*Math.sin(phi+delta)*w - 1) < 1.e-8);
        return delta;
    }
    
    
    
    // Private function to propagate a track without error
    // The corresponding track parameters are:
    // u (cm) is fixed
    // 0 - v (cm)
    // 1 - z (cm)
    // 2 - dv/du
    // 3 - dz/du
    // 4 - q/p   p is momentum of a track, q is its charge
    // If pderiv is nonzero, return the derivative matrix there.
    
    PropStat
            vecPropagateXYXYBX( double B, VTrack trv, Surface srf,
            PropDir dir,
            TrackDerivative deriv  )
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
        SurfXYPlane sxyp1 = ( SurfXYPlane ) srf1;
        
        // Check destination is a XYPlane.
        Assert.assertTrue( srf.pureType().equals(SurfXYPlane.staticType()) );
        if ( !srf.pureType( ).equals(SurfXYPlane.staticType()) )
            return pstat;
        SurfXYPlane sxyp2 = ( SurfXYPlane ) srf;
        
        // If surfaces are the same, we can return now.
        if ( srf.pureEqual(srf1) )
        {
            if (deriv != null)
            {
                deriv.setIdentity();
            }
            pstat.setSame();
            return pstat;
        }
        
        // Fetch the u's and phi's of the planes and the starting track vector.
        int iphi  = SurfXYPlane.NORMPHI;
        int idist = SurfXYPlane.DISTNORM;
        double phi_0 = sxyp1.parameter(iphi);
        
        
        double   u_0 = sxyp1.parameter(idist);
        double phi_n = sxyp2.parameter(iphi);
        double   u_n = sxyp2.parameter(idist);
        TrackVector vec = trv.vector();
        double b1_0 = vec.get(IV);                  // v
        double b2_0 = vec.get(IZ);                  // z
        double b3_0 = vec.get(IDVDU);               // dv/du
        double b4_0 = vec.get(IDZDU);               // dz/du
        double b5_0 = vec.get(IQP);                 // q/p
        
        // phi_x = 0 now
        // phi_x is an angle of r.f where B^ || x^
        
        double phi_x = 0.;
        
        // cos and sin of the destination plane in r.f. where B^ || x^
        // Now I think that I'm already in such r.f. (phi_0==0)
        
        double cosphi = Math.cos(phi_n - phi_x);
        double sinphi = Math.sin(phi_n - phi_x);
        
        double phi_u = phi_0 - phi_x;
        
        double cosphi_u = Math.cos(phi_u);
        double sinphi_u = Math.sin(phi_u);
        
        // check if du == 0 ( that is track moves parallel to the destination plane )
        double du_du_0 = cosphi_u - b3_0*sinphi_u;
        if(du_du_0==0.) return pstat;
        
        double a_hat_u = 1./du_du_0;
        
        double u  = u_0*cosphi_u - b1_0*sinphi_u;
        double b1 = b1_0*cosphi_u + u_0*sinphi_u;
        double b2 = b2_0;
        double b3 = (b3_0*cosphi_u + sinphi_u)*a_hat_u;
        double b4 = b4_0*a_hat_u;
        double b5 = b5_0;
        
        int sign_du0 = 0;
        if(trv.isForward())  sign_du0 =  1;
        if(trv.isBackward()) sign_du0 = -1;
        if(sign_du0 == 0)
        {
            System.out.println( "PropXYXYBX._vec_propagate: Unknown direction of a track ");
            System.exit(1);
        }
        int sign_du =0;
        if(du_du_0*sign_du0 > 0) sign_du =  1;
        if(du_du_0*sign_du0 < 0) sign_du = -1;
        
        
        // check that q/p != 0
        Assert.assertTrue(b5 !=0. );
        
        double c_hat_b = Math.sqrt(1 + b3*b3 + b4*b4);
        
        double w = B*b5*c_hat_b;
        double r_cosalf = 1/(B*b5)*b3*sign_du/c_hat_b;
        double r_sinalf = 1/(B*b5)*b4*sign_du/c_hat_b;
        
        int flag_forward = 0;
        double time = 0.;
        
        if( dir.equals(PropDir.NEAREST))
        {
            flag_forward = 1;
            double time2 =
                    direction(flag_forward,w,r_sinalf,r_cosalf,b1,u_n,u,sign_du, sinphi, cosphi);
            
            flag_forward = -1;
            double time1 =
                    direction(flag_forward,w,r_sinalf,r_cosalf,b1,u_n,u,sign_du, sinphi, cosphi);
            
            if(time2<-time1 && time2>0)
            {
                time = time2;
                flag_forward = 1;
            }
            else if(-time1<time2 && time1<0)
            {
                time = time1;
                flag_forward = -1;
            }
            else return pstat;
        }
        else if ( dir.equals(PropDir.FORWARD) )
        {
            flag_forward = 1;
            time =
                    direction(flag_forward,w,r_sinalf,r_cosalf,b1,u_n,u,sign_du, sinphi, cosphi);
            if(time*flag_forward < 0) return pstat;
        }
        else if ( dir.equals(PropDir.BACKWARD) )
        {
            flag_forward = -1;
            time =
                    direction(flag_forward,w,r_sinalf,r_cosalf,b1,u_n,u,sign_du, sinphi, cosphi);
            if(time*flag_forward < 0) return pstat;
        }
        else
        {
            System.out.println( "PropXYXYBX._vec_propagate: Unknown direction.");
            System.exit(1);
        }
        
        double coswt = Math.cos(w*time);
        double sinwt = Math.sin(w*time);
        
        double  u_p = u + time*sign_du;
        double b1_p = r_sinalf*(coswt-1.) + r_cosalf*sinwt + b1;
        double b2_p = r_sinalf*sinwt + r_cosalf*(1.-coswt) + b2;
        double b3_p = b3*coswt - b4*sinwt;
        double b4_p = b4*coswt + b3*sinwt;
        double b5_p = b5;
        
        double du_n_du = cosphi + b3_p*sinphi;
        
        if(du_n_du==0.) return pstat;
        
        double b1_n = b1_p*cosphi - u_p*sinphi;
        double b2_n = b2_p;
        double b3_n = (b3_p*cosphi - sinphi)/du_n_du;
        double b4_n = b4_p/du_n_du;
        double b5_n = b5_p;
        
        
        int sign_dun = 0;
        if(du_n_du*sign_du > 0) sign_dun =  1;
        if(du_n_du*sign_du < 0) sign_dun = -1;
        
        vec.set(IV     ,b1_n);
        vec.set(IZ     ,b2_n);
        vec.set(IDVDU  ,b3_n);
        vec.set(IDZDU  ,b4_n);
        vec.set(IQP    ,b5_n);
        
        // Update trv
        trv.setSurface( srf.newPureSurface() );
        trv.setVector(vec);
        
        // set new direction of the track
        if(sign_dun ==  1) trv.setForward();
        if(sign_dun == -1) trv.setBackward();
        
        
        // Calculate sT
        
        // initial and final position
        
        double cphi0 = Math.cos(phi_0);
        double sphi0 = Math.sin(phi_0);
        double y0 =  u_0*sphi0 + b1_0*cphi0;
        double cphin = Math.cos(phi_n);
        double sphin = Math.sin(phi_n);
        double yn =  u_n*sphin + b1_n*cphin;
        double dy = yn-y0;
        
        // track direction
        
        double du_ds = 1./Math.sqrt(1.+b3_n*b3_n+b4_n*b4_n);
        if ( trv.isBackward() ) du_ds *= -1.0;
        double dv_ds = b3_n*du_ds;
        double dy_ds =  du_ds*sphin + b3_n*cphin;
        double dz_ds = b4_n*du_ds;
        double dx_ds =  du_ds*cphin - dv_ds*sphin;
        double tlm2 = (dx_ds*dx_ds+dz_ds*dz_ds)/dy_ds/dy_ds;
        
        //  the distance
        
        double ds = ((double)flag_forward)*Math.abs(dy)*Math.sqrt(1+tlm2);
        
        // Set the return status.
        pstat.setPathDistance(ds);
        
        // exit now if user did not ask for error matrix.
        if ( deriv == null ) return pstat;
        
        // db1_n_db_p
        
        double db1_n_db1_p = cosphi;
        double db1_n_du_p  = -sinphi;
        
        // db2_n_db_p
        
        //double db2_n_db2_p = 1.;
        
        // db3_n_db_p
        
        double du_n_du_2 = du_n_du*du_n_du;
        
        double db3_n_db3_p = 1./du_n_du_2;
        
        // db4_n_db_p
        
        double db4_n_db3_p = - b4_p*sinphi/du_n_du_2;
        double db4_n_db4_p = 1./du_n_du;
        
        // db5_n_db_p
        
        //double db5_n_db5_p = 1.;
        
        // dw_db
        
        double dw_db3 = B*b5*b3/c_hat_b;
        double dw_db4 = B*b5*b4/c_hat_b;
        double dw_db5 = w/b5;
        
        // dr_cosalf_db
        
        double c_hat_b2 = c_hat_b*c_hat_b;
        
        double dr_cosalf_db3 =   sign_du/(b5*B)*(1 - b3*b3/c_hat_b2)/c_hat_b;
        double dr_cosalf_db4 = - sign_du/(b5*B)*b3*b4/c_hat_b2/c_hat_b;
        double dr_cosalf_db5 = - r_cosalf/b5;
        
        // dr_cosalf_db
        
        double dr_sinalf_db3 = - sign_du/(b5*B)*b3*b4/c_hat_b2/c_hat_b;
        double dr_sinalf_db4 =   sign_du/(b5*B)*(1 - b4*b4/c_hat_b2)/c_hat_b;
        double dr_sinalf_db5 = - r_sinalf/b5;
        
        // dtime_db
        
        double dtime_db1;
        double dtime_db3;
        double dtime_db4;
        double dtime_db5;
        double dtime_du;
        {
            double rsinalf_time = (coswt - 1.)*sinphi;
            double rcosalf_time = sinphi*sinwt;
            double w_time       = time*sinphi*(coswt*r_cosalf - sinwt*r_sinalf);
            double down = (r_sinalf*sinwt - r_cosalf*coswt)*w*sinphi - sign_du*cosphi;
            
            Assert.assertTrue(down!=0);
            dtime_db1 = sinphi / down;
            dtime_db3 = (  dr_sinalf_db3*rsinalf_time + dr_cosalf_db3*rcosalf_time
                    + dw_db3*w_time) / down;
            dtime_db4 = (  dr_sinalf_db4*rsinalf_time + dr_cosalf_db4*rcosalf_time
                    + dw_db4*w_time) / down;
            dtime_db5 = (  dr_sinalf_db5*rsinalf_time + dr_cosalf_db5*rcosalf_time
                    + dw_db5*w_time) / down;
            dtime_du  = cosphi / down;
        }
        
        // dcoswt_db
        
        double dcoswt_db1 = - sinwt* w*dtime_db1;
        double dcoswt_db3 = - sinwt*(w*dtime_db3 + time*dw_db3);
        double dcoswt_db4 = - sinwt*(w*dtime_db4 + time*dw_db4);
        double dcoswt_db5 = - sinwt*(w*dtime_db5 + time*dw_db5);
        double dcoswt_du  = - sinwt* w*dtime_du;
        
        // dsinwt_db
        
        double dsinwt_db1 = coswt* w*dtime_db1;
        double dsinwt_db3 = coswt*(w*dtime_db3 + time*dw_db3);
        double dsinwt_db4 = coswt*(w*dtime_db4 + time*dw_db4);
        double dsinwt_db5 = coswt*(w*dtime_db5 + time*dw_db5);
        double dsinwt_du  = coswt* w*dtime_du;
        
        // du_p_db
        
        double du_p_db1 = dtime_db1*sign_du;
        double du_p_db3 = dtime_db3*sign_du;
        double du_p_db4 = dtime_db4*sign_du;
        double du_p_db5 = dtime_db5*sign_du;
        double du_p_du  = dtime_du *sign_du + 1.;
        
        // db1_p_db
        
        double db1_p_db1 = 1. + r_sinalf*dcoswt_db1 + r_cosalf*dsinwt_db1;
        double db1_p_db3 =   (coswt - 1.)*dr_sinalf_db3 + r_sinalf*dcoswt_db3
                + sinwt*dr_cosalf_db3 + r_cosalf*dsinwt_db3;
        double db1_p_db4 =   (coswt - 1.)*dr_sinalf_db4 + r_sinalf*dcoswt_db4
                + sinwt*dr_cosalf_db4 + r_cosalf*dsinwt_db4;
        double db1_p_db5 =   (coswt - 1.)*dr_sinalf_db5 + r_sinalf*dcoswt_db5
                + sinwt*dr_cosalf_db5 + r_cosalf*dsinwt_db5;
        double db1_p_du  = r_sinalf*dcoswt_du + r_cosalf*dsinwt_du;
        
        // db2_p_db
        
        double db2_p_db1 = r_sinalf*dsinwt_db1 - r_cosalf*dcoswt_db1;
        //double db2_p_db2 = 1.;
        double db2_p_db3 =   r_sinalf*dsinwt_db3 + sinwt*dr_sinalf_db3
                - (coswt - 1.)*dr_cosalf_db3 - r_cosalf*dcoswt_db3;
        double db2_p_db4 =   r_sinalf*dsinwt_db4 + sinwt*dr_sinalf_db4
                - (coswt - 1.)*dr_cosalf_db4 - r_cosalf*dcoswt_db4;
        double db2_p_db5 =   r_sinalf*dsinwt_db5 + sinwt*dr_sinalf_db5
                - (coswt - 1.)*dr_cosalf_db5 - r_cosalf*dcoswt_db5;
        double db2_p_du  = r_sinalf*dsinwt_du - r_cosalf*dcoswt_du;
        
        // db3_p_db
        
        double db3_p_db1 = b3*dcoswt_db1 - b4*dsinwt_db1;
        double db3_p_db3 = b3*dcoswt_db3 - b4*dsinwt_db3 + coswt;
        double db3_p_db4 = b3*dcoswt_db4 - b4*dsinwt_db4 - sinwt;
        double db3_p_db5 = b3*dcoswt_db5 - b4*dsinwt_db5;
        double db3_p_du  = b3*dcoswt_du  - b4*dsinwt_du;
        
        // db4_p_db
        
        double db4_p_db1 = b4*dcoswt_db1 + b3*dsinwt_db1;
        double db4_p_db3 = b4*dcoswt_db3 + b3*dsinwt_db3 + sinwt;
        double db4_p_db4 = b4*dcoswt_db4 + b3*dsinwt_db4 + coswt;
        double db4_p_db5 = b4*dcoswt_db5 + b3*dsinwt_db5;
        double db4_p_du  = b4*dcoswt_du  + b3*dsinwt_du;
        
        // db5_p_db
        
        //double db5_p_db5 = 1.;
        
        // db1_n_db
        
        double db1_n_db1 = db1_n_db1_p*db1_p_db1 + db1_n_du_p*du_p_db1;
        double db1_n_db3 = db1_n_db1_p*db1_p_db3 + db1_n_du_p*du_p_db3;
        double db1_n_db4 = db1_n_db1_p*db1_p_db4 + db1_n_du_p*du_p_db4;
        double db1_n_db5 = db1_n_db1_p*db1_p_db5 + db1_n_du_p*du_p_db5;
        double db1_n_du  = db1_n_db1_p*db1_p_du  + db1_n_du_p*du_p_du;
        
        // db2_n_db
        
        double db2_n_db1 = db2_p_db1;
        // double db2_n_db2 = 1.;
        double db2_n_db3 = db2_p_db3;
        double db2_n_db4 = db2_p_db4;
        double db2_n_db5 = db2_p_db5;
        double db2_n_du  = db2_p_du;
        
        // db3_n_db
        
        double db3_n_db1 = db3_n_db3_p*db3_p_db1;
        double db3_n_db3 = db3_n_db3_p*db3_p_db3;
        double db3_n_db4 = db3_n_db3_p*db3_p_db4;
        double db3_n_db5 = db3_n_db3_p*db3_p_db5;
        double db3_n_du  = db3_n_db3_p*db3_p_du;
        
        // db4_n_db
        
        double db4_n_db1 = db4_n_db3_p*db3_p_db1 + db4_n_db4_p*db4_p_db1;
        double db4_n_db3 = db4_n_db3_p*db3_p_db3 + db4_n_db4_p*db4_p_db3;
        double db4_n_db4 = db4_n_db3_p*db3_p_db4 + db4_n_db4_p*db4_p_db4;
        double db4_n_db5 = db4_n_db3_p*db3_p_db5 + db4_n_db4_p*db4_p_db5;
        double db4_n_du  = db4_n_db3_p*db3_p_du  + db4_n_db4_p*db4_p_du;
        
        // db5_n_db
        
        //double db5n_db5 = 1.;
        
        // right now all these are 1
        
        // a_hat_u2 = 1/du_du_0^2 = 1/(cosphi_u - b3_0*sinphi_u)^2
        double a_hat_u2 = a_hat_u*a_hat_u;
        
        // db1_db_0
        
        double db1_db1_0 = cosphi_u;
        //double db1_du_0 = sinphi_u;
        
        // db2_db_0
        
        //double db2_db2_0 = 1.;
        
        // db3_db_0
        
        double db3_db3_0 = a_hat_u2;
        
        // db4_db_0
        
        double db4_db3_0 = b4_0*sinphi_u*a_hat_u2;
        double db4_db4_0 = a_hat_u;
        
        // db5_db_0
        
        //double db5_db5_0 = 1.;
        
        // du_db_0
        
        double du_db1_0 = - sinphi_u;
        //double du_du_0 = cosphi_u;
        
        // db1_n_db_0
        
        double db1_n_db1_0 = db1_n_db1*db1_db1_0 + db1_n_du*du_db1_0;
        double db1_n_db2_0 = 0.;
        double db1_n_db3_0 = db1_n_db3*db3_db3_0 + db1_n_db4*db4_db3_0;
        double db1_n_db4_0 = db1_n_db4*db4_db4_0;
        double db1_n_db5_0 = db1_n_db5;
        
        // db2_n_db_0
        
        double db2_n_db1_0 = db2_n_db1*db1_db1_0 + db2_n_du*du_db1_0;
        double db2_n_db2_0 = 1.;
        double db2_n_db3_0 = db2_n_db3*db3_db3_0 + db2_n_db4*db4_db3_0;
        double db2_n_db4_0 = db2_n_db4*db4_db4_0;
        double db2_n_db5_0 = db2_n_db5;
        
        // db3_n_db_0
        
        double db3_n_db1_0 = db3_n_db1*db1_db1_0 + db3_n_du*du_db1_0;
        double db3_n_db2_0 = 0.;
        double db3_n_db3_0 = db3_n_db3*db3_db3_0 + db3_n_db4*db4_db3_0;
        double db3_n_db4_0 = db3_n_db4*db4_db4_0;
        double db3_n_db5_0 = db3_n_db5;
        
        // db4_n_db_0
        
        double db4_n_db1_0 = db4_n_db1*db1_db1_0 + db4_n_du*du_db1_0;
        double db4_n_db2_0 = 0.;
        double db4_n_db3_0 = db4_n_db3*db3_db3_0 + db4_n_db4*db4_db3_0;
        double db4_n_db4_0 = db4_n_db4*db4_db4_0;
        double db4_n_db5_0 = db4_n_db5;
        
        // db5_n_db_0
        
        double db5_n_db1_0 = 0.;
        double db5_n_db2_0 = 0.;
        double db5_n_db3_0 = 0.;
        double db5_n_db4_0 = 0.;
        double db5_n_db5_0 = 1.;
        
        deriv.set(IV,IV       , db1_n_db1_0);
        deriv.set(IV,IZ       , db1_n_db2_0);
        deriv.set(IV,IDVDU    , db1_n_db3_0);
        deriv.set(IV,IDZDU    , db1_n_db4_0);
        deriv.set(IV,IQP      , db1_n_db5_0);
        deriv.set(IZ,IV       , db2_n_db1_0);
        deriv.set(IZ,IZ       , db2_n_db2_0);
        deriv.set(IZ,IDVDU    , db2_n_db3_0);
        deriv.set(IZ,IDZDU    , db2_n_db4_0);
        deriv.set(IZ,IQP      , db2_n_db5_0);
        deriv.set(IDVDU,IV    , db3_n_db1_0);
        deriv.set(IDVDU,IZ    , db3_n_db2_0);
        deriv.set(IDVDU,IDVDU , db3_n_db3_0);
        deriv.set(IDVDU,IDZDU , db3_n_db4_0);
        deriv.set(IDVDU,IQP   , db3_n_db5_0);
        deriv.set(IDZDU,IV    , db4_n_db1_0);
        deriv.set(IDZDU,IZ    , db4_n_db2_0);
        deriv.set(IDZDU,IDVDU , db4_n_db3_0);
        deriv.set(IDZDU,IDZDU , db4_n_db4_0);
        deriv.set(IDZDU,IQP   , db4_n_db5_0);
        deriv.set(IQP,IV      , db5_n_db1_0);
        deriv.set(IQP,IZ      , db5_n_db2_0);
        deriv.set(IQP,IDVDU   , db5_n_db3_0);
        deriv.set(IQP,IDZDU   , db5_n_db4_0);
        deriv.set(IQP,IQP     , db5_n_db5_0);
        
        return pstat;
    }
}

