package org.lcsim.recon.tracking.trfcyl;

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.HitGenerator;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.CrossStat;

/**
 * Generates HitCylPhi hits on a cylindrical surface with a gaussian spread in the measurement.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class HitCylPhiGenerator extends HitGenerator
{
    
    // attributes
    
    // the surface
    private  Surface  _srf;
    
    // Error (standard deviation) for the measurement.
    private double _dphi;
    
    //
    private boolean debug;
    
    /**
     *Construct an instance from a cylinder surface and the measurement uncertainty as a gaussian sigma.
     * The cylinder is cloned so user can pass subclass.
     *
     * @param   srf The cylindrical surface.
     * @param   dphi The gaussian sigma for the phi measurement uncertainty.
     */
    public HitCylPhiGenerator(  SurfCylinder srf, double dphi)
    {
        super();
        _srf = srf.newSurface();
        //_srf = new SurfCylinder(srf);
        _dphi =dphi;
        Assert.assertTrue( _dphi >= 0.0 );
    }
    
    //
    
    /**
     *Construct an instance from a cylinder surface, the measurement uncertainty
     *and a random number seed.
     * The cylinder is cloned so user can pass subclass.
     *
     * @param   srf The cylindrical surface.
     * @param   dphi The gaussian sigma for the phi measurement uncertainty.
     * @param   seed The seed for the random number used by the HitGenerator.
     */
    public HitCylPhiGenerator(  SurfCylinder srf, double dphi, long seed)
    {
        super(seed);
        _srf = srf.newSurface();
        // _srf = new SurfCylinder(srf);
        _dphi =dphi;
        Assert.assertTrue( _dphi >= 0.0 );
        
    }
    
    //
    
    /**
      *Construct an instance replicating the HitCylPhiGenerator ( copy constructor ).
     *
     * @param   hgen The HitCylPhiGenerator to replicate.
     */
    public HitCylPhiGenerator(  HitCylPhiGenerator hgen)
    {
        super(hgen);
        _srf = hgen._srf;
        _dphi = hgen._dphi;
        Assert.assertTrue( _dphi >= 0.0 );
        
    }
    
    //
    
    /**
     *Return the surface associated with this HitCylPhiGenerator.
     *
     * @return The surface associated with this HitCylPhiGenerator.
     */
    public Surface surface()
    { return _srf; }
    
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
        return newCluster( trv, 0 );
    }
    
    
    /**
     *Generate a new cluster with the specified Monte Carlo track ID.
     * Return null for failure.
     *
    * @param   trv The VTrack for which to generate a cluster at this surface.
     * @param   mcid The MC ID to associate with this cluster.
     * @return   The cluster for this track at this surface, null for failure.
     */
    public Cluster newCluster( VTrack trv, int mcid )
    {
        
        // Create null cluster.
        Cluster clu = null;
        
        // Check track has been propagated to the surface.
        Assert.assertTrue( _srf.pureEqual( trv.surface() ) );
        if ( ! _srf.pureEqual( trv.surface() ) ) return clu;
        
        // Require that track is in bounds if surface is bounded.
        CrossStat xstat =_srf.status(trv);
        if(debug) System.out.println("Surface: "+_srf + "\n Status: "+xstat);
        if ( (! _srf.isPure()) && (! xstat.inBounds()) ) return clu;
        
        // calculate phi.
        double phi = trv.vector().get(0) + _dphi*gauss();
        
        // construct cluster
        double radius = _srf.parameter(SurfCylinder.RADIUS);
        clu = new ClusCylPhi( radius, phi, _dphi, mcid );
        return clu;
        
    }
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("HitCylPhi Generator at \n" + surface()
        + "\nMeasurement error (dphi): " + _dphi);
        sb.append(super.toString());
        return sb.toString();
        
    }
}
