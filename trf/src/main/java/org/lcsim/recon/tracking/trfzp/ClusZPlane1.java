package  org.lcsim.recon.tracking.trfzp;

import java.util.List;
import java.util.ArrayList;

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.McCluster;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.Hit;

/**
 * Describes a cluster which measures x-y on a ZPlane.
 * axy = wx*x + wy*y
 *
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */

//**********************************************************************

public class ClusZPlane1 extends McCluster
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
    { return "ClusZPlane1"; }
    
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public static String staticType()
    { return typeName(); }
    
    // attributes
    
    // the surface
    private SurfZPlane _szp;
    
    // the x axis weight
    private double _wx;
    
    // the y axis weight
    private double _wy;
    
    // measurement
    private double _axy;
    
    // the error (standard deviation) for the measurement
    private double _daxy;
    
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
        Assert.assertTrue(  type().equals(clus.type()) );
        ClusZPlane1 ccp = (ClusZPlane1) clus;
        return ( _wx == ccp._wx )
        && ( _wy == ccp._wy )
        && ( _axy == ccp._axy )
        && ( _daxy == ccp._daxy )
        && ( _szp.equals(ccp._szp) );
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
        List hits =  new ArrayList();
        double x_track = tre.vector().get(SurfZPlane.IX);
        double y_track = tre.vector().get(SurfZPlane.IY);
        double exx_track = tre.error().get(SurfZPlane.IX,SurfZPlane.IX);
        double exy_track = tre.error().get(SurfZPlane.IX,SurfZPlane.IY);
        double eyy_track = tre.error().get(SurfZPlane.IY,SurfZPlane.IY);
        
        double axy  = _wx*x_track + _wy*y_track;
        double eaxy = exx_track*_wx*_wx + 2.*exy_track*_wx*_wy + eyy_track*_wy*_wy;
        
        hits.add( new HitZPlane1( axy, eaxy ) );
        return hits;
    }
    
    // methods
    
    //
    
    /**
     *Construct an instance from the z plane position, mixing and
     * measurement uncertainty as a gaussian sigma.
     *
     * @param   zpos  The z position of the plane.
     * @param   wx  The stereo angle in the x direction.
     * @param   wy  The stereo angle in the y direction.
     * @param   axy The mixing between a and y.
     * @param   daxy  The gaussian sigma for the xy measurement uncertainty.
     */
    public ClusZPlane1(double zpos, double wx, double wy, double axy,double daxy)
    {
        _szp = new SurfZPlane(zpos);
        _wx = wx;
        _wy = wy;
        _axy = axy;
        _daxy = daxy;
        Assert.assertTrue( _daxy >= 0.0 );
    }
    
    //
    
    /**
     *Construct an instance from the z plane position, mixing,
     * the measurement uncertainty as a gaussian sigma and the MC ID associated with this cluster.
     *
     * @param   zpos  The z position of the plane.
     * @param   wx  The stereo angle in the x direction.
     * @param   wy  The stereo angle in the y direction.
     * @param   axy The mixing between a and y.
     * @param   daxy  The gaussian sigma for the xy measurement uncertainty.
     * @param   mcid   The MC ID for the track creating this cluster.
     */
    public ClusZPlane1(double zpos, double wx, double wy, double axy,double daxy, int mcid )
    {
        super(mcid);
        _szp = new SurfZPlane(zpos);
        _wx = wx;
        _wy = wy;
        _axy = axy;
        _daxy = daxy;
        Assert.assertTrue( _daxy >= 0.0 );
    }
    
    //
    
    /**
     *Construct an instance from the z plane position, mixing,
     * the measurement uncertainty as a gaussian sigma and a list
     * of MC IDs contributing to this cluster.
     *
     * @param   zpos  The z position of the plane.
     * @param   wx  The stereo angle in the x direction.
     * @param   wy  The stereo angle in the y direction.
     * @param   axy The mixing between a and y.
     * @param   daxy  The gaussian sigma for the xy measurement uncertainty.
     * @param   mcids   The list of MC IDs for the tracks contributing to this cluster.
     */
    public ClusZPlane1(double zpos, double wx, double wy, double axy,double daxy, List mcids )
    {
        super(mcids);
        _szp = new SurfZPlane(zpos);
        _wx = wx;
        _wy = wy;
        _axy = axy;
        _daxy = daxy;
        Assert.assertTrue( _daxy >= 0.0 );
    }
    
    //
    
    /**
     *Construct an instance replicating the ClusZPlane1 ( copy constructor ).
     *
     * @param   clus The Cluster to replicate.
     */
    public ClusZPlane1( ClusZPlane1 clus)
    {
        super(clus);
        _szp = new SurfZPlane(clus._szp);
        _wx = clus._wx;
        _wy = clus._wy;
        _axy = clus._axy;
        _daxy = clus._daxy;
    }
    
    //
    
    /**
     *Return a String representation of the class' type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' type name.
     */
    public String type()
    { return staticType();  }
    
    
    //
    
    /**
     *Return the surface at which this cluster is measured.
     *
     * @return The surface of this cluster.
     */
    public Surface surface()
    { return _szp; }
    
    //
    
    /**
     *Return the wx pitch.
     *
     * @return The wx pitch.
     */
    public double wX()
    { return _wx; }
    
    //
    
    /**
     *Return the wy pitch.
     *
     * @return The wy pitch.
     */
    public double wY()
    { return _wy; }
    
    //
    
    /**
     *Return the mixing in x and y axy.
     *
     * @return The mixing in x and y.
     */
    public double aXY()
    { return _axy; }
    
    //
    
    /**
     *Return the uncertainty in the xy measurement.
     *
     * @return The uncertainty in the xy measurement.
     */
    public double daXY()
    { return _daxy; }
    
    //
    
    /**
     *There are no more predictions.
     *
     * @return null
     */
    public Hit newNextPrediction()
    { return null; }
    
    
    /**
     *output stream
     *
     * @return A String representation of this instance.
     */
    public String toString()
    {
        return "axy " + _szp + " and x weight " + _wx
                + " and y weight " + _wy
                + ": axy = " + _axy + " +/- " + _daxy;
    }
    
    
}
