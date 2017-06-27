package org.lcsim.recon.tracking.trfxyp;

// Generator for ClusXYPlane2 objects.

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.HitGenerator;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.HitError;
/**
 * Generates two dimensional (v,z) hits on an xy plane with a gaussian spread
 * in the measurements correlated according to
 * the covariance matrix.
 *
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class HitXYPlane2Generator extends HitGenerator
{
    
    // attributes
    private static final int IV = ClusXYPlane2.IV;
    private static final int IZ = ClusXYPlane2.IZ;
    // the surface
    private SurfXYPlane _sxyp;
    
    // the error matrix for the measurement (v,z)
    private HitError _dhm;
    
    
    
    /**
     *Construct an instance from the xy plane disance and angle and
     *hit error covariance matrix.
     *
     * @param dist The shortest distance to the plane from the z axis.
     * @param phi The angle of the normal to the plane with respect to the x axis.
     * @param dhm The hit error covariance matrix.
     */
    public HitXYPlane2Generator(double dist, double phi,  HitError dhm)
    {
        super();
        _sxyp = new SurfXYPlane(dist,phi);
        _dhm = dhm;
        Assert.assertTrue( _dhm.get(IV,IV) >= 0.0 && _dhm.get(IZ,IZ) >=0. );
        
        // check that determinant of _dhm is positive
        Assert.assertTrue( _dhm.get(IV,IV)*_dhm.get(IZ,IZ) - _dhm.get(IV,IZ)*_dhm.get(IV,IZ) >= 0.0);
        
        Assert.assertTrue( _dhm.size() == 2);
    }
    /**
     *Construct an instance from the xy plane disance and angle,
     *hit error covariance matrix and random number seed.
     *
     * @param dist The shortest distance to the plane from the z axis.
     * @param phi The angle of the normal to the plane with respect to the x axis.
     * @param dhm The hit error covariance matrix.
     * @param iseed The seed for the random number used by the HitGenerator.
     */
    public HitXYPlane2Generator(double dist, double phi,
            HitError dhm, long iseed)
    {
        super(iseed);
        _sxyp = new SurfXYPlane(dist,phi);
        _dhm = dhm;
        Assert.assertTrue( _dhm.get(IV,IV) >= 0.0 && _dhm.get(IZ,IZ) >=0. );
        
        // check that determinant of _dhm is positive
        Assert.assertTrue( _dhm.get(IV,IV)*_dhm.get(IZ,IZ) - _dhm.get(IV,IZ)*_dhm.get(IV,IZ) >= 0.0);
        
        Assert.assertTrue( _dhm.size() == 2);
    }
    
    /**
     *Construct an instance replicating the HitXYPlane2Generator ( copy constructor ).
     *
     * @param  gen The HitXYPlane2Generator to replicate.
     */
    public HitXYPlane2Generator(  HitXYPlane2Generator gen)
    {
        super(gen);
        _sxyp = new SurfXYPlane(gen._sxyp);
        _dhm = gen._dhm;
        Assert.assertTrue( _dhm.get(IV,IV) >= 0.0 && _dhm.get(IZ,IZ) >=0. );
        
        // check that determinant of _dhm is positive
        Assert.assertTrue( _dhm.get(IV,IV)*_dhm.get(IZ,IZ) - _dhm.get(IV,IZ)*_dhm.get(IV,IZ) >= 0.0);
        
        Assert.assertTrue( _dhm.size() == 2 );
    }
    
    /**
     *Return the surface associated with this HitXYPlane2Generator.
     *
     * @return The surface associated with this HitXYPlane2Generator.
     */
    public  Surface surface()
    { return _sxyp; }
    
    /**
     *Generate a new cluster.
     * Return null for failure.
     *
     * @param   trv The VTrack for which to generate a cluster at this surface.
     * @return   The cluster for this track at this surface, null for failure.
     */
    public Cluster newCluster( VTrack trv)
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
        Assert.assertTrue( _sxyp.pureEqual( trv.surface() ) );
        if ( ! _sxyp.pureEqual( trv.surface() ) ) return null;
        
        // calculate axy.
        double v_track = trv.vector().get(SurfXYPlane.IV);
        double z_track = trv.vector().get(SurfXYPlane.IZ);
        double a1  = _dhm.get(IV,IV);
        double a2  = _dhm.get(IZ,IZ);
        double a12 = _dhm.get(IV,IZ);
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
        double vv = cphi*cphi*a1 + sphi*sphi*a2 + 2*cphi*sphi*a12;
        double zz = cphi*cphi*a2 + sphi*sphi*a1 - 2*cphi*sphi*a12;
        
        // simple check. To be removed later
        
        Assert.assertTrue( Math.abs(cphi*cphi*a12 - sphi*sphi*a12 + cphi*sphi*(a2-a1)) < 1.e-10);
        Assert.assertTrue( Math.abs(vv*zz - (a1*a2 - a12*a12)) < 1.e-10 );
        
        // Very important test
        
        Assert.assertTrue( vv > 0.);
        Assert.assertTrue( zz > 0.);
        
        double dvv = gauss()/Math.sqrt(vv);
        double dzz = gauss()/Math.sqrt(zz);
        double b1 = cphi*dvv - sphi*dzz;
        double b2 = cphi*dzz + sphi*dvv;
        
        double av  = v_track + (a1*b1+a12*b2);
        double az  = z_track + (a2*b2+a12*b1);
        
        // construct cluster
        double dist   = _sxyp.parameter(SurfXYPlane.DISTNORM);
        double phinor = _sxyp.parameter(SurfXYPlane.NORMPHI);
        return new ClusXYPlane2(dist,phinor,av,az,_dhm.get(IV,IV), _dhm.get(IZ,IZ), _dhm.get(IV, IZ), mcid );
        
    }
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        return "Surface: " +_sxyp +
                "\nMeasurement error (dhm): " + _dhm ;
    }
    
}

