package org.lcsim.recon.tracking.trfcyl;
// Generator for ClusCylPhiZ2D objects.
import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.HitGenerator;
import org.lcsim.recon.tracking.trfbase.HitError;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.CrossStat;
/**
 * Generates HitCylPhiZ2D hits on a cylindrical surface with a 
 * gaussian spread in the measurements correlated according to 
 * the covariance matrix.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class HitCylPhiZ2DGenerator extends HitGenerator
{
    
    // attributes
    
    // the surface
    private Surface _srf;
    
    // Error (standard deviation) for the phi measurement.
    private double _dphi;
    
    // Error for the z measurement
    private double _dz;
    
    // The correlation term in the covariance matrix
    private double _dphidz;
    
    //
    
    /**
     *Construct an instance from a cylinder surface and the measurement uncertainty.
     * The cylinder is cloned so user can pass subclass.
     *
     * @param   srf The cylindrical surface.
     * @param   dphi The gaussian sigma for the phi measurement uncertainty.
     * @param   dz  The gaussian sigma for the z measurement uncertainty.
     * @param   dphidz The covariance matrix for the phi an z measurements.
     */
    public HitCylPhiZ2DGenerator(  SurfCylinder srf,
    double dphi, double dz, double dphidz)
    {
        super();
        _srf = new SurfCylinder(srf);
        _dphi = dphi;
        _dz = dz;
        _dphidz = dphidz;
        Assert.assertTrue( _dphi >= 0.0 );
        Assert.assertTrue( _dz >= 0.0 );
        //check the determinant as well...
        Assert.assertTrue( _dphi*_dphi*_dz*dz - _dphidz*dphidz >= 0.0 );
    }
    
    //
    
     /**
     *Construct an instance from a cylinder surface and the measurement uncertainty.
     * The cylinder is cloned so user can pass subclass.
     *
     * @param   srf The cylindrical surface.
     * @param   dphi The gaussian sigma for the phi measurement uncertainty.
     * @param   dz  The gaussian sigma for the z measurement uncertainty.
     * @param   dphidz The covariance matrix for the phi an z measurements.
     * @param   iseed The seed for the random number used by the HitGenerator.
     */
    public HitCylPhiZ2DGenerator(  SurfCylinder srf,
    double dphi, double dz, double dphidz,
    long iseed)
    {
        super(iseed);
        _srf = new SurfCylinder(srf);
        _dphi = dphi;
        _dz = dz;
        _dphidz = dphidz;
        // Check covariance matrix
        Assert.assertTrue( _dphi >= 0.0 );
        Assert.assertTrue( _dz >= 0.0 );
        //check the determinant as well...
        Assert.assertTrue( _dphi*_dphi*_dz*dz - _dphidz*dphidz >= 0.0 );
    }
    //
    
    /**
      *Construct an instance replicating the HitCylPhiZ2DGenerator ( copy constructor ).
     *
     * @param   hgen The HitCylPhiZ2DGenerator to replicate.
     */
    public HitCylPhiZ2DGenerator(  HitCylPhiZ2DGenerator hgen)
    {
        super(hgen);
        _srf = hgen._srf;
        _dphi = hgen._dphi;
        _dz = hgen._dz;
        _dphidz = hgen._dphidz;
        Assert.assertTrue( _dphi >= 0.0 );
        Assert.assertTrue( _dz >= 0.0 );
        
    }
    
    //
    
     /**
     *Return the surface associated with this HitCylPhiZ2DGenerator.
     *
     * @return The surface associated with this HitCylPhiZ2DGenerator.
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
        
        // Covariantly smear phi and z...
        double ran1 = gauss();
        double ran2 = gauss();
        double a2 = 0;
        if ( _dphi != 0.0 ) a2 = _dphidz/_dphi;
        double b2 = _dz;
        if (a2 != 0.0 ) b2 = Math.sqrt( _dz*_dz - a2*a2);
        
        double phi = trv.vector().get(SurfCylinder.IPHI) + _dphi*ran1;
        double z = trv.vector().get(SurfCylinder.IZ) + a2*ran1 + b2*ran2;
        
        // construct cluster
        double radius = _srf.parameter(SurfCylinder.RADIUS);
        clu = new ClusCylPhiZ2D( radius, phi, _dphi, z, _dz, _dphidz, mcid );
        
        return clu;
        
    }
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("HitCylPhiZ2D Generator at \n" + surface());
        sb.append("\n dphi: " + _dphi);
        sb.append("\n dz: " + _dz);
        sb.append("\n dhidz: " + _dphidz);
        sb.append(super.toString());
        return sb.toString();
    }
    
}

