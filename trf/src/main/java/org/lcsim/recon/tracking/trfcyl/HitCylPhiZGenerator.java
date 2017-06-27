package org.lcsim.recon.tracking.trfcyl;
// Generator for ClusCylPhiZ objects.
import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.HitGenerator;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.CrossStat;
/**
 * Generates HitCylPhiZ hits on a cylindrical surface with a gaussian spread in the measurement.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class HitCylPhiZGenerator extends HitGenerator
{
    
    // attributes
    
    // the surface
    private Surface _srf;
    
    // the mixing parameter stereo: phiz = phi + stereo*z;
    private double _stereo;
    
    // Error (standard deviation) for the measurement.
    private double _dphiz;
    
    //
    
    /**
     *Construct an instance from a cylinder surface, the stereo angle and the measurement uncertainty.
     * The cylinder is cloned so user can pass subclass.
     *
     * @param   srf The cylindrical surface.
     * @param   stereo The stereo angle.
     * @param   dphiz The gaussian sigma for the phi-z stereo measurement  uncertainty.
     */
    public HitCylPhiZGenerator(  SurfCylinder srf, double stereo,
    double dphiz)
    {
        super();
        //   _srf = srf.new_surface();
        _srf = new SurfCylinder(srf);
        _stereo = stereo;
        _dphiz =dphiz;
        Assert.assertTrue( _dphiz >= 0.0 );
    }
    
    //
    /**
     *Construct an instance from a cylinder surface, the stereo angle, the measurement uncertainty
     and a random number seed.
     * The cylinder is cloned so user can pass subclass.
     *
     * @param   srf The cylindrical surface.
     * @param   stereo The stereo angle.
     * @param   dphiz The gaussian sigma for the phi-z stereo measurement uncertainty.
     * @param   iseed The seed for the random number used by the HitGenerator.
     */
    public HitCylPhiZGenerator(  SurfCylinder srf, double stereo,
    double dphiz, long iseed)
    {
        super(iseed);
        _srf = new SurfCylinder(srf);
        _stereo = stereo;
        _dphiz =dphiz;
        Assert.assertTrue( _dphiz >= 0.0 );
    }
    
    //
    
     /**
      *Construct an instance replicating the HitCylPhiZGenerator ( copy constructor ).
     *
     * @param   hgen The HitCylPhiZGenerator to replicate.
     */
    public HitCylPhiZGenerator(  HitCylPhiZGenerator hgen)
    {
        super(hgen);
        _srf = hgen._srf;
        _stereo = hgen._stereo;
        _dphiz = hgen._dphiz;
        Assert.assertTrue( _dphiz >= 0.0 );
        
        
    }
    
    //
    
    /**
     *Return the surface associated with this HitCylPhiZGenerator.
     *
     * @return The surface associated with this HitCylPhiZGenerator.
     */
    public  Surface surface()
    { return _srf;
    }
    
    //
    
   /**
     *Generate a new cluster.
     * Return null for failure.
     *
     * @param   trv The VTrack for which to generate a cluster at this surface.
     * @return   The cluster for this track at this surface, null for failure.
     */
    public Cluster newCluster(  VTrack trv )
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
    public Cluster newCluster(  VTrack trv, int mcid )
    {
        Cluster clu = null;
        // Check track has been propagated to the surface.
        Assert.assertTrue( _srf.pureEqual( trv.surface() ) );
        if ( ! _srf.pureEqual( trv.surface() ) ) return clu;
        
        // Require that track is in bounds if surface is bounded.
        CrossStat xstat = _srf.status(trv);
        if ( (! _srf.isPure()) && (! xstat.inBounds()) ) return clu;
        
        // calculate phiz.
        double phiz = trv.vector().get(0) + _stereo*trv.vector().get(1)
        + _dphiz*gauss();
        
        // construct cluster
        double radius = _srf.parameter(SurfCylinder.RADIUS);
        clu = new ClusCylPhiZ( radius, phiz, _dphiz, _stereo, mcid );
        
        return clu;
        
    }
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("HitCylPhiZ Generator at \n" + surface()
        + "\nStereo slope: " + _stereo );
        sb.append("\nMeasurement error (dphiz): " + _dphiz);
        sb.append(super.toString());
        return sb.toString();
        
    }
    
}

