package org.lcsim.recon.tracking.trfcyl;

import java.util.*;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfbase.McCluster;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Surface;
/**
 * Describes a cluster which measures phi and z on a cylinder.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class ClusCylPhiZ2D extends McCluster
{
    
    // static methods
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String typeName()
    { return "ClusCylPhiZ2D";
    }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName();
    }
    
    // attributes
    
    // the surface
    private SurfCylinder _scy;
    
    // the phi measurement
    private double _phi;
    
    // the error (standard deviation) for the phi measurement
    private double _dphi;
    
    // the z measurement
    private double _z;
    
    // the error (standard deviation) for the z measurement
    private double _dz;
    
    // the covariance term
    private double _dphidz;
    
    // methods
    
    //
    
    /**
     * Test equality.
     *
     * @param   clus The Cluster to test against.
     * @return true if the Clusters are the same.
     */
    public boolean equal(Cluster clus)
    {
        Assert.assertTrue( type().equals(clus.type()) );
        ClusCylPhiZ2D ccpz = ( ClusCylPhiZ2D ) clus;
        return ( _phi == ccpz._phi ) && ( _dphi == ccpz._dphi ) &&
                ( _z == ccpz._z ) && ( _dz == ccpz._dz ) &&
                ( _scy.equals(ccpz._scy) );
    }
    
    //
    
    /**
     *Generate the first (and only) track prediction.
     *
     * @param   tre The ETrack for which to generate the prediction.
     * @return A list of hits for this Track.
     */
    public List predict(ETrack tre)
    {
        List hits = new ArrayList();
        double phi = tre.vector().get(SurfCylinder.IPHI);
        double dphi = tre.error().get(SurfCylinder.IPHI, SurfCylinder.IPHI);
        double z = tre.vector().get(SurfCylinder.IZ);
        double dz = tre.error().get(SurfCylinder.IZ,SurfCylinder.IZ);
        double dphidz = 0.;
        hits.add( new HitCylPhiZ2D( phi, dphi, z, dz, dphidz ) );
        return hits;
    }
    
    // methods
    
    //
    
    /**
     *Construct an instance from the radius of the measurement, the phi measurement,
     * the uncertainty in the phi measurement, the z measurement, the uncertainty in the z measurement,
     * and the correlation between the phi and z measurements.
     *
     * @param   radius The cylindrical radius of the measurement.
     * @param   phi    The phi value measurement.
     * @param   dphi   The uncertainty in the phi measurement.
     * @param   z      The z value measurement.
     * @param   dz     The uncertainty in the z measurement.
     * @param   dphidz The correlation between the phi and z measurements.
     */
    public ClusCylPhiZ2D(double radius, double phi, double dphi, double z, double dz, double dphidz)
    {
        _scy = new SurfCylinder(radius);
        _phi = phi;
        _dphi = dphi;
        _z = z;
        _dz = dz;
        _dphidz = dphidz;
        Assert.assertTrue( _dphi >= 0.0 );
        Assert.assertTrue( _dz >= 0.0 );
    }
    
    //
    
    /**
     *Construct an instance from the radius of the measurement, the phi measurement,
     * the uncertainty in the phi measurement, the z measurement, the uncertainty in the z measurement,
     * the correlation between the phi and z measurements and the MC ID associated with this cluster.
     *
     * @param   radius The cylindrical radius of the measurement.
     * @param   phi    The phi value measurement.
     * @param   dphi   The uncertainty in the phi measurement.
     * @param   z      The z value measurement.
     * @param   dz     The uncertainty in the z measurement.
     * @param   dphidz The correlation between the phi and z measurements.
     * @param   mcid   The MC ID for the track creating this cluster.
     */
    public ClusCylPhiZ2D(double radius, double phi, double dphi, double z, double dz, double dphidz,
            int mcid)
    {
        super(mcid);
        _scy = new SurfCylinder(radius);
        _phi = phi;
        _dphi = dphi;
        _z = z;
        _dz = dz;
        _dphidz = dphidz;
        Assert.assertTrue( _dphi >= 0.0 );
        Assert.assertTrue( _dz >= 0.0 );
    }
    
    //
    
    /**
     *Construct an instance from the radius of the measurement, the phi measurement,
     * the uncertainty in the phi measurement, the z measurement, the uncertainty in the z measurement,
     * the correlation between the phi and z measurements and a list of MC IDs contributing to this cluster.
     *
     * @param   radius The cylindrical radius of the measurement.
     * @param   phi    The phi value measurement.
     * @param   dphi   The uncertainty in the phi measurement.
     * @param   z      The z value measurement.
     * @param   dz     The uncertainty in the z measurement.
     * @param   dphidz The correlation between the phi and z measurements.
     * @param   mcids  The list of MC IDs for the tracks contributing to this cluster.
     */
    public ClusCylPhiZ2D(double radius, double phi, double dphi, double z, double dz, double dphidz,
            List mcids)
    {
        super(mcids);
        _scy = new SurfCylinder(radius);
        _phi = phi;
        _dphi = dphi;
        _z = z;
        _dz = dz;
        _dphidz = dphidz;
        Assert.assertTrue( _dphi >= 0.0 );
        Assert.assertTrue( _dz >= 0.0 );
    }
    
    //
    
    /**
     *Construct an instance replicating the ClusCylPhiZ2D ( copy constructor ).
     *
     * @param   ccpz The Cluster to replicate.
     */
    public ClusCylPhiZ2D(ClusCylPhiZ2D ccpz)
    {
        super(ccpz);
        _scy = new SurfCylinder(ccpz._scy);
        _phi = ccpz._phi;
        _dphi = ccpz._dphi;
        _z = ccpz._z;
        _dz = ccpz._dz;
        _dphidz = ccpz._dphidz;
    }
    
    //
    
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
    
    //
    
    /**
     *Return the surface at which this cluster is measured.
     *
     * @return The surface of this cluster.
     */
    public  Surface surface()
    {
        return _scy;
    }
    
    //
    
    /**
     *Return the phi measurement of this cluster
     *
     * @return The phi measurement.
     */
    public double phi()
    {
        return _phi;
    }
    
    //
    
    /**
     *Return the uncertainty in the phi measurement.
     *
     * @return The uncertainty in the phi measurement.
     */
    public double dPhi()
    {
        return _dphi;
    }
    
    //
    
    /**
     *Return the z measurement of this cluster
     *
     * @return The z measurement.
     */
    public double z()
    {
        return _z;
    }
    
    // return dz
    
    /**
     *Return the uncertainty in the z measurement.
     *
     * @return The uncertainty in the z measurement.
     */
    public double dZ()
    {
        return _dz;
    }
    
    //
    
    /**
     *Return the covariance between the phi and z measurements.
     *
     * @return The covariance between the phi and z measurements.
     */
    public double dPhidZ()
    {
        return _dphidz;
    }
    
    //
    
    /**
     *Return the cylindrical radius of this cluster.
     *
     * @return The cylindrical radius of this cluster.
     */
    public double radius()
    {
        return _scy.radius();
    }
    
    // there are no more predictions.
    
    /**
     *Return the next prediction. There are none for this simple hit, so return null.
     *
     * @return null.
     */
    public Hit newNextPrediction()
    { return null;
    }
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("ClusCylPhiZ2D at " + _scy
                + "\n phi = " + _phi + " +/- " + _dphi
                + "\n z   = " + _z   + " +/- " + _dz);
        List mcids = mcIds();
        if ( mcids.size() > 0)
        {
            sb.append( "\n MC ID's:");
            for ( Iterator it=mcids.iterator(); it.hasNext(); )
            {
                sb.append( " " + it.next());
            }
            sb.append("\n");
        }
        return sb.toString();
        
    }
}


