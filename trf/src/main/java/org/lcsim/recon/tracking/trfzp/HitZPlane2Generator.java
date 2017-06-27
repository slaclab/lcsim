package  org.lcsim.recon.tracking.trfzp;

// Generator for ClusZPlane2 objects.

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.HitGenerator;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.HitError;
/**
 * Generates two dimensional (x,y) hits on a z plane with a gaussian spread
 * in the measurements correlated according to
 * the covariance matrix.
 *
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class HitZPlane2Generator extends HitGenerator
{
    
    // attributes
    private static final int IX = ClusZPlane2.IX;
    private static final int IY = ClusZPlane2.IY;
    
    // the surface
    private SurfZPlane _szp;
    
    // the error matrix for the measurement (x,y)
    private HitError _dhm;
    
    
    
    //
    
    /**
     *Construct an instance  from the z plane position and measurement 
     * position uncertainty.
     *
     * @param   zpos The z position of the plane.
     * @param   dhm  The hit error covariance matrix.
     */
    public HitZPlane2Generator(double zpos, HitError dhm)
    {
        super();
        _szp = new SurfZPlane(zpos);
        _dhm = dhm;
        Assert.assertTrue( _dhm.get(IX,IX) >= 0.0 && _dhm.get(IY,IY) >= 0.0);
        
        // check that determinant of _dhm is positive
        Assert.assertTrue( _dhm.get(IX,IX)*_dhm.get(IY,IY) - _dhm.get(IX,IY)*_dhm.get(IX,IY) >= 0.0);
        
        Assert.assertTrue( _dhm.size() == 2);
    }
    
    //
    
    /**
     *Construct an instance from the z plane position the measurement position uncertainty
     * and random number seed.
     *
     * @param   zpos The z position of the plane.
     * @param   dhm  The hit error covariance matrix.
     * @param   iseed The seed for the random number used by the HitGenerator.
     */
    public HitZPlane2Generator(double zpos, HitError dhm, long iseed)
    {
        super(iseed);
        _szp = new SurfZPlane(zpos);
        _dhm = dhm;
        Assert.assertTrue( _dhm.get(IX,IX) >= 0.0 && _dhm.get(IY,IY) >= 0.0);
        
        // check that determinant of _dhm is positive
        Assert.assertTrue( _dhm.get(IX,IX)*_dhm.get(IY,IY) - _dhm.get(IX,IY)*_dhm.get(IX,IY) >= 0.0);
        
        Assert.assertTrue( _dhm.size() == 2);  }
    
    //
    
   /**
     *Construct an instance replicating the HitZPlane2Generator ( copy constructor ).
     *
     * @param  gen The HitZPlane2Generator to replicate.
     */
    public HitZPlane2Generator( HitZPlane2Generator gen)
    {
        super(gen);
        _szp = new SurfZPlane(gen._szp);
        _dhm = (gen._dhm);
        Assert.assertTrue( _dhm.get(IX,IX) >= 0.0 && _dhm.get(IY,IY) >= 0.0 );
        
        // check that determinant of _dhm is positive
        Assert.assertTrue( _dhm.get(IX,IX)*_dhm.get(IY,IY) - _dhm.get(IX,IY)*_dhm.get(IX,IY) >= 0.0);
        
        Assert.assertTrue( _dhm.size() == 2 );
    }
    
    //
    
    /**
     *Return the surface associated with this HitZPlane2Generator.
     *
     * @return The surface associated with this HitZPlane2Generator.
     */
    public  Surface surface()
    { return _szp; }
    
    //
    
    /**
     *Generate a new cluster.
     * Return null for failure.
     *
     * @param   trv The VTrack for which to generate a cluster at this surface.
     * @return   The cluster for this track at this surface, null for failure.
     */
    public Cluster newCluster( VTrack trv )
    {
        return newCluster(trv, 0);
    }
    
    /**
     *Generate a new cluster with the specified Monte Carlo track ID.
     * Return null for failure.
     *
    * @param   trv The VTrack for which to generate a cluster at this surface.
     * @param   mcid The MC ID to associate with this cluster.
     * @return   The cluster for this track at this surface, null for failure.
     */
    public Cluster newCluster( VTrack trv, int mcid)
    {
        // Check track has been propagated to the surface.
        Assert.assertTrue( _szp.pureEqual( trv.surface() ) );
        if ( ! _szp.pureEqual( trv.surface() ) ) return null;
        
        // calculate axy.
        double x_track = trv.vector().get(SurfZPlane.IX);
        double y_track = trv.vector().get(SurfZPlane.IY);
        double a1  = _dhm.get(IX,IX);
        double a2  = _dhm.get(IY,IY);
        double a12 = _dhm.get(IX,IY);
        double phi;
        if(a12 == 0.)
        {
            phi = 0.;
        }
        else
        {
            if(a1 == a2 )
            {
                phi = Math.PI/4.;
            }
            else
            {
                phi = 0.5*Math.atan(2*a12/(a1-a2));
            }
        }
        double cphi= Math.cos(phi);
        double sphi= Math.sin(phi);
        double xx = cphi*cphi*a1 + sphi*sphi*a2 + 2*cphi*sphi*a12;
        double yy = cphi*cphi*a2 + sphi*sphi*a1 - 2*cphi*sphi*a12;
        
        // simple check. To be removed later
        
        Assert.assertTrue( Math.abs(cphi*cphi*a12 - sphi*sphi*a12 + cphi*sphi*(a2-a1)) < 1.e-10);
        Assert.assertTrue( Math.abs(xx*yy - (a1*a2 - a12*a12)) < 1.e-10 );
        
        // Very important test
        
        Assert.assertTrue( xx > 0.);
        Assert.assertTrue( yy > 0.);
        
        double dxx = gauss()/Math.sqrt(xx);
        double dyy = gauss()/Math.sqrt(yy);
        double b1 = cphi*dxx - sphi*dyy;
        double b2 = cphi*dyy + sphi*dxx;
        
        double ax  = x_track + (a1*b1+a12*b2);
        double ay  = y_track + (a2*b2+a12*b1);
        
        // construct cluster
        double zpos = _szp.parameter(SurfZPlane.ZPOS);
        return new ClusZPlane2(zpos,ax,ay,_dhm.get(IX,IX), _dhm.get(IY,IY), _dhm.get(IX,IY), mcid );
    }
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        return  "Surface: " + _szp
        + "\n Measurement error (dhm): " + _dhm ;
    }
}

