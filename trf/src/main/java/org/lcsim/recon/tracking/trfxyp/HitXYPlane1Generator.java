package org.lcsim.recon.tracking.trfxyp;
// Generator for ClusXYPlane1 objects.

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.HitGenerator;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.Cluster;

/**
 * Generates one dimensional xy hits on an xy plane with a gaussian spread in the measurement.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class HitXYPlane1Generator extends HitGenerator
{
    
    // attributes
    
    // the surface
    private SurfXYPlane _sxyp;
    
    // the v axis weight
    private double _wv;
    
    // the z axis weight
    private double _wz;
    
    // the error (standard deviation) for the measurement avz = wv*v + wz*z
    private double _davz;
    
    
    /**
     *Construct an instance from the xy plane disance and angle, mixing and measurement uncertainty as a gaussian sigma.
     *
     * @param dist The shortest distance to the plane from the z axis.
     * @param phi The angle of the normal to the plane with respect to the x axis.
     * @param wv The stereo angle in the v direction.
     * @param wz The stereo angle in the z direction.
     * @param davz The gaussian sigma for the vz measurement uncertainty.
     */
    public HitXYPlane1Generator(double dist, double phi,
            double wv,   double wz,
            double davz)
    {
        super();
        _sxyp = new SurfXYPlane(dist,phi);
        _wv = wv;
        _wz = wz;
        _davz = davz;
        Assert.assertTrue( _davz >= 0.0 );
    }
    
    /**
     *Construct an instance from the xy plane disance and angle, mixing, measurement
     * uncertainty as a gaussian sigma and random number seed.
     *
     * @param dist The shortest distance to the plane from the z axis.
     * @param phi The angle of the normal to the plane with respect to the x axis.
     * @param wv The stereo angle in the v direction.
     * @param wz The stereo angle in the z direction.
     * @param davz The gaussian sigma for the vz measurement uncertainty.
     * @param iseed  The seed for the random number used by the HitGenerator.
     */
    public HitXYPlane1Generator(double dist, double phi,
            double wv,   double wz,
            double davz, long iseed)
    {
        super(iseed);
        _sxyp = new SurfXYPlane(dist,phi);
        _wv = wv;
        _wz = wz;
        _davz = davz;
        Assert.assertTrue( _davz >= 0.0 );
    }
    
    /**
     *Construct an instance replicating the HitZPlane1Generator ( copy constructor ).
     *
     * @param  gen The HitZPlane1Generator to replicate.
     */
    public HitXYPlane1Generator( HitXYPlane1Generator gen)
    {
        super(gen);
        _sxyp =  new SurfXYPlane(gen._sxyp);
        _wv = gen._wv;
        _wz = gen._wz;
        _davz =  gen._davz;
        Assert.assertTrue( _davz >= 0.0 );
    }
    
    /**
     *Return the surface associated with this HitXYPlane1Generator.
     *
     * @return The surface associated with this HitXYPlane1Generator.
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
        
        // calculate avz.
        double v_track = trv.vector().get(SurfXYPlane.IV);
        double z_track = trv.vector().get(SurfXYPlane.IZ);
        double avz = _wv*v_track + _wz*z_track
                + _davz*gauss();
        
        // construct cluster
        double dist = _sxyp.parameter(SurfXYPlane.DISTNORM);
        double  phi = _sxyp.parameter(SurfXYPlane.NORMPHI);
        return new ClusXYPlane1(dist, phi, _wv, _wz, avz, _davz, mcid );
        
    }
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        return "Surface: " + _sxyp +
                "\n V slope: " + _wv +
                "\n Z slope: " + _wz+
                "\n Measurement error (davz): " + _davz;
    }
    
}

