package org.lcsim.recon.tracking.trfcyl;

import java.util.*;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfbase.McCluster;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Surface;

/**
 * Describes a cluster which measures phiz on a cylinder, where
 * phiz = phi + stereo*z.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class ClusCylPhiZ extends McCluster
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
    { return "ClusCylPhiZ";
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
    
    // the phi-z mixing
    private double _stereo;
    
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
        ClusCylPhiZ ccpz = ( ClusCylPhiZ ) clus;
        return ( _phi == ccpz._phi ) && ( _dphi == ccpz._dphi ) &&
                ( _stereo == ccpz._stereo ) && ( _scy.equals(ccpz._scy) );
        
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
        double phiz = tre.vector().get(0) + _stereo*tre.vector().get(1);
        double epp = tre.error().get(0,0) + 2.0*_stereo*tre.error().get(0,1) +
                _stereo*_stereo*tre.error().get(1,1);
        hits.add( new HitCylPhiZ( phiz, epp ) );
        return hits;
    }
    
    // methods
    
    //
    
    /**
     *Construct an instance from the radius of the measurement, the phi measurement,
     * the uncertainty in the phi measurement and the stereo angle (dphi/dz).
     *
     * @param   radius The cylindrical radius of the measurement.
     * @param   phi    The phi value measurement.
     * @param   dphi   The uncertainty in the phi measurement.
     * @param   stereo The stereo angle (dphi/dz) for the measurement.
     */
    public ClusCylPhiZ(double radius, double phi, double dphi, double stereo)
    {
        _scy = new SurfCylinder(radius);
        _phi = phi;
        _dphi = dphi;
        _stereo = stereo;
        Assert.assertTrue( _dphi >= 0.0 );
    }
    
    //
    
    /**
     *Construct an instance from the radius of the measurement, the phi measurement,
     * the uncertainty in the phi measurement, the stereo angle (dphi/dz)
     *and the MC ID associated with this cluster.
     *
     * @param   radius The cylindrical radius of the measurement.
     * @param   phi    The phi value measurement.
     * @param   dphi   The uncertainty in the phi measurement.
     * @param   stereo The stereo angle (dphi/dz) for the measurement.
     * @param   mcid   The MC ID for the track creating this cluster.
     */
    public ClusCylPhiZ(double radius, double phi, double dphi, double stereo, int mcid)
    {
        super(mcid);
        _scy = new SurfCylinder(radius);
        _phi = phi;
        _dphi = dphi;
        _stereo = stereo;
        Assert.assertTrue( _dphi >= 0.0 );
    }
    
    //
    
    /**
     *Construct an instance from the radius of the measurement, the phi measurement,
     * the uncertainty in the phi measurement, the stereo angle (dphi/dz)
     *and a list of MC IDs contributing to this cluster.
     *
     * @param   radius The cylindrical radius of the measurement.
     * @param   phi    The phi value measurement.
     * @param   dphi   The uncertainty in the phi measurement.
     * @param   stereo The stereo angle (dphi/dz) for the measurement.
     * @param   mcids  The list of MC IDs for the tracks contributing to this cluster.
     */
    public ClusCylPhiZ(double radius, double phi, double dphi, double stereo,
            List mcids)
    {
        super(mcids);
        _scy = new SurfCylinder(radius);
        _phi = phi;
        _dphi = dphi;
        _stereo = stereo;
        Assert.assertTrue( _dphi >= 0.0 );
    }
    
    //
    
    /**
     *Construct an instance replicating the ClusCylPhiZ ( copy constructor ).
     *
     * @param   ccpz The Cluster to replicate.
     */
    public ClusCylPhiZ(ClusCylPhiZ ccpz)
    {
        super(ccpz);
        _scy = new SurfCylinder(ccpz._scy);
        _phi = ccpz._phi;
        _dphi = ccpz._dphi;
        _stereo = ccpz._stereo;
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
     *Return the stereo pitch of the measurement (dphi/dz).
     *
     * @return The stereo angle dphi/dz.
     */
    public double stereo()
    { return _stereo;
    }
    
    //
    
    /**
     *Return the phiz measurement of this cluster
     *
     * @return The phiz measurement (phi + stereo*z).
     */
    public double phiZ()
    { return _phi;
    }
    
    //
    
    /**
     *Return the uncertainty of the phiz measurement of this cluster
     *
     * @return The uncertainty in the phiz measurement d(phi + stereo*z).
     */
    public double dPhiZ()
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
    
    //
    
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
        StringBuffer sb = new StringBuffer("Hit at " + _scy + " with mixing " + _stereo
                + ": phiz = " + _phi + " +/- " + _dphi);
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

