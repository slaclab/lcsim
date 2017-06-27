package  org.lcsim.recon.tracking.trfzp;
// Generator for ClusZPlane1 objects.

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.HitGenerator;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.Cluster;

/**
 * Generates one dimensional xy hits on a z plane with a gaussian spread in the measurement.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class HitZPlane1Generator extends HitGenerator
{
    
    // attributes
    
    // the surface
    private SurfZPlane _szp;
    
    // the x axis weight
    private double _wx;
    
    // the y axis weight
    private double _wy;
    
    // the error (standard deviation) for the measurement axy = wx*x + wy*y
    private double _daxy;
    
    
    //
    
    /**
     *Construct an instance  from the z plane position, mixing and measurement uncertainty as a gaussian sigma.
     *
     * @param   zpos  The z position of the plane.
     * @param   wx  The stereo angle in the x direction.
     * @param   wy  The stereo angle in the y direction.
     * @param   daxy  The gaussian sigma for the xy measurement uncertainty.
     */
    public HitZPlane1Generator(double zpos, double wx, double wy,double daxy)
    {
        super();
        _szp = new SurfZPlane(zpos);
        _wx = wx;
        _wy = wy;
        _daxy = daxy;
        Assert.assertTrue( _daxy >= 0.0 );
    }
    
    //
    
    /**
     *Construct an instance  from the z plane position, mixing,
     * measurement uncertainty as a gaussian sigma and random number seed.
     *
     * @param   zpos  The z position of the plane.
     * @param   wx  The stereo angle in the x direction.
     * @param   wy  The stereo angle in the y direction.
     * @param   daxy  The gaussian sigma for the xy measurement uncertainty.
     * @param   iseed The seed for the random number used by the HitGenerator.
     */
    public HitZPlane1Generator(double zpos, double wx, double wy,
    double daxy, long iseed)
    {
        super(iseed);
        _szp = new SurfZPlane(zpos);
        _wx = wx;
        _wy = wy;
        _daxy = daxy;
        Assert.assertTrue( _daxy >= 0.0 );
    }
    
    //
    
    /**
     *Construct an instance replicating the HitZPlane1Generator ( copy constructor ).
     *
     * @param  gen The HitZPlane1Generator to replicate.
     */
    public HitZPlane1Generator( HitZPlane1Generator gen)
    {
        super(gen);
        _szp = new SurfZPlane(gen._szp);
        _wx = gen._wx;
        _wy = gen._wy;
        _daxy = gen._daxy;
        Assert.assertTrue( _daxy >= 0.0 );
    }
    
    //
    
    /**
     *Return the surface associated with this HitZPlane1Generator.
     *
     * @return The surface associated with this HitZPlane1Generator.
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
        Assert.assertTrue( _szp.pureEqual( trv.surface() ) );
        if ( ! _szp.pureEqual( trv.surface() ) ) return null;
        
        // calculate axy.
        double x_track = trv.vector().get(SurfZPlane.IX);
        double y_track = trv.vector().get(SurfZPlane.IY);
        double axy = _wx*x_track + _wy*y_track
        + _daxy*gauss();
        
        // construct cluster
        double zpos = _szp.parameter(SurfZPlane.ZPOS);
        return new ClusZPlane1(zpos, _wx, _wy, axy, _daxy, mcid );
    }
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        return  "Surface: " + _szp
        + "\n X slope: " + _wx +" Y slope: " + _wy
        + "\n Measurement error (daxy): " + _daxy ;
    }
    
}

