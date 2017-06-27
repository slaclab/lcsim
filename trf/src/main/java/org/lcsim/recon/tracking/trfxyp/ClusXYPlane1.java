package org.lcsim.recon.tracking.trfxyp;


import java.util.List;
import java.util.ArrayList;

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.McCluster;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.Hit;

/**Describes a v-z measurement on a XYPlane.
 * avz = wv*v + wz*z
 * <p>
 * This is a very simple hit.  It produces one prediction with fixed
 * measurement which is simply the avz of the track.
 *
 *@author Norman A. Graf
 *@version 1.0
 */
public class ClusXYPlane1 extends McCluster
{
    
    // static methods
    
    // Return the type name.
    public static String typeName()
    { return "ClusXYPlane1"; }
    
    // Return the type.
    public static String staticType()
    { return typeName(); }
    
    // attributes
    
    // the surface
    private SurfXYPlane _sxyp;
    
    // the v axis weight
    private double _wv;
    
    // the z axis weight
    private double _wz;
    
    // measurement
    private double _avz;
    
    // the error (standard deviation) for the measurement
    private double _davz;
    
    // methods
    
    // equality
    public boolean equal(Cluster clus)
    {
        Assert.assertTrue( type().equals(clus.type()) );
        ClusXYPlane1 ccp = (  ClusXYPlane1 ) clus;
        return    ( _wv == ccp._wv )
        && ( _wz == ccp._wz )
        && ( _avz == ccp._avz )
        && ( _davz == ccp._davz )
        && ( _sxyp.equals(ccp._sxyp) );
    }
    
    // generate first (and only) track prediction
    public List predict( ETrack tre)
    {
        List hits =  new ArrayList();
        double v_track = tre.vector().get(SurfXYPlane.IV);
        double z_track = tre.vector().get(SurfXYPlane.IZ);
        double evv_track = tre.error().get(SurfXYPlane.IV,SurfXYPlane.IV);
        double evz_track = tre.error().get(SurfXYPlane.IV,SurfXYPlane.IZ);
        double ezz_track = tre.error().get(SurfXYPlane.IZ,SurfXYPlane.IZ);
        
        double avz  = _wv*v_track + _wz*z_track;
        double eavz = evv_track*_wv*_wv + 2.*evz_track*_wv*_wz + ezz_track*_wz*_wz;
        
        hits.add(  new HitXYPlane1( avz, eavz ) );
        return hits;
    }
    
    // methods
    
    // constructor
    public ClusXYPlane1(double dist, double phi,
            double wv, double wz, double avz,double davz)
    {
        _sxyp = new SurfXYPlane(dist,phi);
        _wv = wv;
        _wz = wz;
        _avz = avz;
        _davz = davz;
        Assert.assertTrue( _davz >= 0.0 );
    }
    
    // constructor from a single id
    public ClusXYPlane1(double dist, double phi,
            double wv, double wz, double avz,double davz,  int mcid)
    {
        super(mcid);
        _sxyp = new SurfXYPlane(dist,phi);
        _wv = wv;
        _wz = wz;
        _avz = avz;
        _davz = davz;
        
        Assert.assertTrue( _davz >= 0.0 );
    }
    
    // constructor from a list of ids
    public ClusXYPlane1(double dist, double phi,
            double wv, double wz, double avz,double davz,  List mcids)
    {
        super(mcids);
        _sxyp = new SurfXYPlane(dist,phi);
        _wv = wv;
        _wz = wz;
        _avz = avz;
        _davz = davz;
        
        Assert.assertTrue( _davz >= 0.0 );
    }
    
    // copy constructor
    public ClusXYPlane1( ClusXYPlane1 clus)
    {
        super(clus);
        _sxyp =  new SurfXYPlane(clus._sxyp);
        _wv = clus._wv;
        _wz = clus._wz;
        _avz = clus._avz;
        _davz = clus._davz;
    }
    
    // return the type
    public String type()
    { return staticType();  }
    
    // return the surface
    public  Surface surface()
    { return _sxyp; }
    
    // return the wv pitch
    public double wV()
    { return _wv; }
    
    // return the wz pitch
    public double wZ()
    { return _wz; }
    
    // return avz
    public double aVZ()
    { return _avz; }
    
    // return davz
    public double daVZ()
    { return _davz; }
    
    // there are no more predictions.
    public Hit newNextPrediction()
    { return null; }
    
    public String toString()
    {
        return "ClusXYPlane1 " + _sxyp + " and v weight " + _wv
                + " and z weight " + _wz
                + ": avz = " + _avz + " +/- " + _davz;
    }
    
}
