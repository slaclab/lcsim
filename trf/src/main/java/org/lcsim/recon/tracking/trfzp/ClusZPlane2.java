package  org.lcsim.recon.tracking.trfzp;

import java.util.List;
import java.util.ArrayList;

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.McCluster;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.HitVector;
import org.lcsim.recon.tracking.trfbase.HitError;

/**
 * Describes a cluster which measures (x,y) on a ZPlane.
 *
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class ClusZPlane2 extends McCluster
{
    
    // static methods
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     *
     */
    public static String typeName()
    { return "ClusZPlane2";
    }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     *
     */
    public static String staticType()
    { return typeName();
    }
    
    
    // cluster parameter indices
    
    public static final int IX=0;
    public static final int IY=1;
    
    // attributes
    
    // the surface
    private SurfZPlane _szp;
    
    // measurement
    private double _x,_y;
    
    // the error matrix for the measurement
    private double _dx2;
    private double _dy2;
    private double _dxdy;
    
    // methods
    
    //
    
    /**
     * Test equality.
     *
     * @param   clus The Cluster to test against.
     * @return true if the Clusters are the same.
     */
    public boolean equal( Cluster clus)
    {
        Assert.assertTrue( type().equals(clus.type()) );
        ClusZPlane2 ccp = (ClusZPlane2) clus;
        return ( _x == ccp._x ) &&
                ( _y == ccp._y ) &&
                ( _dx2 == ccp._dx2 ) &&
                ( _dy2 == ccp._dy2 ) &&
                ( _dxdy == ccp._dxdy ) &&
                ( _szp.equals(ccp._szp) );
    }
    
    //
    
    /**
     *Generate the first (and only) track prediction.
     *
     * @param   tre The ETrack for which to generate the prediction.
     * @return A list of hits for this Track.
     */
    public  List predict( ETrack tre)
    {
        List hits = new ArrayList();
        
        TrackVector  vec = tre.vector();
        TrackError  err = tre.error();
        
        hits.add(  new
                HitZPlane2( vec.get(SurfZPlane.IX),vec.get(SurfZPlane.IY),
                err.get(SurfZPlane.IX,SurfZPlane.IX),
                err.get(SurfZPlane.IY,SurfZPlane.IY),
                err.get(SurfZPlane.IX,SurfZPlane.IY)
                ) );
        
        return hits;
    }
    
    // methods
    //
    
    
    /**
     *Construct an instance from the z plane position, the hit measurement,
     *and the hit measurement uncertainty.
     *
     * @param   zpos The z position of the plane.
     * @param   hm   The two dimensional HitVector representing the x,y measurement and its correlation.
     * @param   dhm  The two dimensional HitError representing the x,y measurement uncertainty and its correlation.
     */
    public ClusZPlane2(double zpos,
            HitVector hm,
            HitError dhm )
    {
        
        _szp = new SurfZPlane(zpos);
        // check that determinant of _dhm is positive
        
        _x = hm.get(IX);
        _y = hm.get(IY);
        _dx2 = dhm.get(IX,IX);
        _dy2 = dhm.get(IY,IY);
        _dxdy = dhm.get(IX,IY);
        
        Assert.assertTrue( _dx2 >= 0.0 && _dy2 >=0. );
        
        // check that determinant of _dhm is positive
        Assert.assertTrue( _dx2*_dy2 - _dxdy*_dxdy >= 0.0);
        
    }
    
    
    /**
     *Construct an instance from the z plane position, the x and y measurements,
     *and the hit measurement uncertainty.
     *
     * @param   zpos The z position of the plane.
     * @param   x The x measurement.
     * @param   y The y measurement.
     * @param   dhm  The two dimensional HitError representing the x,y measurement uncertainty and its correlation.
     */
    public ClusZPlane2(double zpos,
            double x, double y,
            HitError dhm )
    {
        _szp = new SurfZPlane(zpos);
        _x = x;
        _y = y;
        _dx2 = dhm.get(IX,IX);
        _dy2 = dhm.get(IY,IY);
        _dxdy = dhm.get(IX,IY);
        
        Assert.assertTrue( _dx2 >= 0.0 && _dy2 >=0. );
        
        // check that determinant of _dhm is positive
        Assert.assertTrue( _dx2*_dy2 - _dxdy*_dxdy >= 0.0);
    }
    
    
    //
    
    
    /**
     *Construct an instance from the z plane position, the x and y measurements,
     *and the hit measurement uncertainty.
     *
     * @param   zpos The z position of the plane.
     * @param   x The x measurement.
     * @param   y The y measurement.
     * @param   dx2 The (squared) error on the x measurement.
     * @param   dy2 The (squared) error on the y measurement.
     * @param   dxdy The xy covariance term.
     */
    public  ClusZPlane2(double zpos, double x, double y, double dx2, double dy2, double dxdy)
    {
        _szp = new SurfZPlane(zpos);
        _x = x;
        _y = y;
        _dx2 = dx2;
        _dy2 = dy2;
        _dxdy = dxdy;
        
        Assert.assertTrue( _dx2 >= 0.0 && _dy2 >=0. );
        
        // check that determinant of _dhm is positive
        Assert.assertTrue( _dx2*_dy2 - _dxdy*_dxdy >= 0.0);
    }
    
    //Constructor from single MC Id
    
    /**
     *Construct an instance from the z plane position, the x and y measurements,
     *the hit measurement uncertainty and the MC ID associated with this cluster.
     *
     * @param   zpos The z position of the plane.
     * @param   x The x measurement.
     * @param   y The y measurement.
     * @param   dx2 The (squared) error on the x measurement.
     * @param   dy2 The (squared) error on the y measurement.
     * @param   dxdy The xy covariance term.
     * @param   mcid The MC ID for the track creating this cluster.
     */
    public  ClusZPlane2(double zpos, double x, double y, double dx2, double dy2, double dxdy, int mcid)
    {
        super(mcid);
        _szp = new SurfZPlane(zpos);
        _x = x;
        _y = y;
        _dx2 = dx2;
        _dy2 = dy2;
        _dxdy = dxdy;
        
        Assert.assertTrue( _dx2 >= 0.0 && _dy2 >=0. );
        
        // check that determinant of _dhm is positive
        Assert.assertTrue( _dx2*_dy2 - _dxdy*_dxdy >= 0.0);
    }
    
    //
    
    /**
     *Construct an instance from the z plane position, the x and y measurements,
     *the hit measurement uncertainty and the MC ID associated with this cluster.
     *
     * @param   zpos The z position of the plane.
     * @param   x The x measurement.
     * @param   y The y measurement.
     * @param   dx2 The (squared) error on the x measurement.
     * @param   dy2 The (squared) error on the y measurement.
     * @param   dxdy The xy covariance term.
     * @param   mcids The list of MC IDs for the tracks contributing to this cluster.
     */
    public  ClusZPlane2(double zpos, double x, double y, double dx2, double dy2, double dxdy, List mcids)
    {
        super(mcids);
        _szp = new SurfZPlane(zpos);
        _x = x;
        _y = y;
        _dx2 = dx2;
        _dy2 = dy2;
        _dxdy = dxdy;
        
        Assert.assertTrue( _dx2 >= 0.0 && _dy2 >=0. );
        
        // check that determinant of _dhm is positive
        Assert.assertTrue( _dx2*_dy2 - _dxdy*_dxdy >= 0.0);
    }
    
    //
    
    /**
     *Construct an instance replicating the ClusZPlane2 ( copy constructor ).
     *
     * @param   clus The Cluster to replicate.
     */
    public  ClusZPlane2( ClusZPlane2 clus)
    {
        super(clus);
        _szp = new SurfZPlane(clus._szp);
        _x = clus._x;
        _y = clus._y;
        _dx2 = clus._dx2;
        _dy2 = clus._dy2;
        _dxdy = clus._dxdy;
    }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     *
     */
    public  String type()
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
        return _szp;
    }
    
    //
    
    /**
     *There are no more predictions.
     *
     * @return null.
     */
    public  Hit newNextPrediction()
    {
        return null;
    }
    
    
    /**
     * Return the measured x position.
     *
     * @return The measured x position.
     */
    public double x()
    {
        return _x;
    }
    
    /**
     * Return the measured y position.
     *
     * @return The measured y position.
     */
    public double y()
    {
        return _y;
    }
    
    /**
     * Return the error matrix element for x.
     * This is the square of the uncertainty on the x measurement.
     *
     * @return The square of the uncertainty on the x measurement.
     */
    public double dX2()
    {
        return _dx2;
    }
    
    /**
     * Return the error matrix element for y.
     * This is the square of the uncertainty on the y measurement.
     *
     * @return The square of the uncertainty on the y measurement.
     */
    public double dY2()
    {
        return _dy2;
    }
    
    /**
     *Return the error matrix covariance element for xy.
     *
     * @return The error matrix covariance element for xy.
     */
    public double dXdY()
    {
        return _dxdy;
    }
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        return " z " + _szp.z()
        + ": xy =  [" + _x + " , " + _y + " ] +/- " +
                "[ " + _dx2 + " , " + _dy2 +" , " + _dxdy + " ] ";
    }
    
}

