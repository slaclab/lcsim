package org.lcsim.recon.tracking.trfcyl;

import java.util.*;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfbase.McCluster;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Surface;

/**
 * Describes a cluster which measures phi on a cylinder.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class ClusCylPhi extends McCluster
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
    { return "ClusCylPhi";
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
    
    // the measurement
    private double _phi;
    
    // the error (standard deviation) for the measurement
    private double _dphi;
    
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
        ClusCylPhi ccp = ( ClusCylPhi ) clus;
        return ( _phi == ccp._phi ) && ( _dphi == ccp._dphi ) &&
                ( _scy.equals(ccp._scy) );
        
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
        hits.add(
                new HitCylPhi( tre.vector().get(0), tre.error().get(0,0) ) );
        return hits;
        
    }
    
    // methods
    
    //
    
    /**
     *Construct an instance from the radius of the measurement, the phi measurement,
     * and the uncertainty in the phi measurement.
     *
     * @param   radius The cylindrical radius of the measurement.
     * @param   phi    The phi value measurement.
     * @param   dphi   The uncertainty in the phi measurement.
     */
    public ClusCylPhi(double radius, double phi, double dphi)
    {
        _scy = new SurfCylinder(radius);
        _phi = phi;
        _dphi = dphi;
        Assert.assertTrue( _dphi >= 0.0 );
        
    }
    
    //
    
    /**
     *Construct an instance from the radius of the measurement, the phi measurement,
     * the uncertainty in the phi measurement and the MC ID associated with this cluster.
     *
     * @param   radius The cylindrical radius of the measurement.
     * @param   phi    The phi value measurement.
     * @param   dphi   The uncertainty in the phi measurement.
     * @param   mcid   The MC ID for the track creating this cluster.
     */
    public ClusCylPhi(double radius, double phi, double dphi, int mcid)
    {
        super(mcid);
        _scy = new SurfCylinder(radius);
        _phi = phi;
        _dphi = dphi;
        Assert.assertTrue( _dphi >= 0.0 );
    }
    
    //
    
    /**
     *Construct an instance from the radius of the measurement, the phi measurement,
     * the uncertainty in the phi measurement and a list of MC IDs contributing to this cluster.
     *
     * @param   radius The cylindrical radius of the measurement.
     * @param   phi    The phi value measurement.
     * @param   dphi   The uncertainty in the phi measurement.
     * @param   mcids   The list of MC IDs for the tracks contributing to this cluster.
     */
    public ClusCylPhi(double radius, double phi, double dphi, List mcids)
    {
        super(mcids);
        _scy = new SurfCylinder(radius);
        _phi = phi;
        _dphi = dphi;
        Assert.assertTrue( _dphi >= 0.0 );
    }
    
    //
    
    /**
     *Construct an instance replicating the ClusCylPhi ( copy constructor ).
     *
     * @param   ccp The Cluster to replicate.
     */
    public ClusCylPhi( ClusCylPhi ccp)
    {
        super(ccp);
        _scy = new SurfCylinder(ccp._scy);
        _phi = ccp._phi;
        _dphi = ccp._dphi;
    }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public String type()
    { return staticType();
    }
    
    //
    
    /**
     *Return the surface at which this cluster is measured.
     *
     * @return The surface of this cluster.
     */
    public  Surface surface()
    { return _scy;
    }
    
    //
    
    /**
     * Return the phi measurement of this cluster.
     *
     * @return The phi measurement.
     */
    public double phi()
    { return _phi;
    }
    
    //
    
    /**
     *Return the uncertainty in the phi measurement.
     *
     * @return The uncertainty in the phi measurement.
     */
    public double dPhi()
    { return _dphi;
    }
    
    //
    
    /**
     *Return the cylindrical radius of this cluster.
     *
     * @return The cylindrical radius of this cluster.
     */
    public double radius()
    { return _scy.radius();
    }
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("Hit at " + _scy + ": phi = " + _phi + " +/- " + _dphi);
        List mcids = mcIds();
        if ( mcids.size() > 0)
        {
            sb.append( "\n MC ID's:");
            for ( Iterator it=mcids.iterator(); it.hasNext();)
            {
                sb.append( "+ " + it.next());
            }
            sb.append("\n");
        }
        return sb.toString();
        
    }
    
}

