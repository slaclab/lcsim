package org.lcsim.recon.tracking.trfxyp;

import java.util.List;
import java.util.ArrayList;

import org.lcsim.recon.tracking.trfutil.Assert;

import org.lcsim.recon.tracking.trfbase.McCluster;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.TrackError;

/** Describes a (v,z) measurement on a XYPlane.
 *<p>
 * This is a very simple hit.  It produces one prediction with fixed
 * measurement which is simply the (v,z) of the track.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class ClusXYPlane2 extends McCluster
{
    
    // static methods
    
    // Return the type name.
    public static String typeName()
    { return "ClusXYPlane2"; }
    
    // Return the type.
    public static String staticType()
    { return typeName(); }
    
    // cluster parameter indices
    
    public static final int IV=0;
    public static final int IZ=1;
    
    // attributes
    
    // the surface
    private SurfXYPlane _sxyp;
    
    // measurement
    private double _v,_z;
    
    // the error matrix for the measurement
    private double _dv2;
    private double _dz2;
    private double _dvdz;
    
    // methods
    
    // equality
    public boolean equal( Cluster clus)
    {
        Assert.assertTrue( type().equals(clus.type()) );
        ClusXYPlane2 ccp = (ClusXYPlane2) clus;
        return ( _v == ccp._v ) &&
                ( _z == ccp._z ) &&
                ( _dv2 == ccp._dv2 ) &&
                ( _dz2 == ccp._dz2 ) &&
                ( _dvdz == ccp._dvdz ) &&
                ( _sxyp.equals(ccp._sxyp) );
    }
    
    // generate first (and only) track prediction
    public List predict( ETrack tre)
    {
        List hits =  new ArrayList();
        TrackVector  vec = tre.vector();
        TrackError  err = tre.error();
        
        hits.add(  new
                HitXYPlane2( vec.get(SurfXYPlane.IV),vec.get(SurfXYPlane.IZ),
                err.get(SurfXYPlane.IV,SurfXYPlane.IV),
                err.get(SurfXYPlane.IZ,SurfXYPlane.IZ),
                err.get(SurfXYPlane.IV,SurfXYPlane.IZ)
                ) );
        return hits;
    }
    
    // methods
    
    // constructor
    public ClusXYPlane2(double dist,double phi, double v, double z,
            double dv2, double dz2, double dvdz )
    {
        _sxyp = new SurfXYPlane(dist,phi);
        _v = v;
        _z=  z;
        _dv2 = dv2;
        _dz2 = dz2;
        _dvdz = dvdz;
        
        Assert.assertTrue( _dv2 >= 0.0 && _dz2 >=0. );
        
        // check that determinant of _dhm is positive
        Assert.assertTrue( _dv2*_dz2 - _dvdz*_dvdz >= 0.0);
        
    }
    
    // constructor from a single mc id
    
    public ClusXYPlane2(double dist,double phi, double v, double z,
            double dv2, double dz2, double dvdz, int mcid )
    {
        super(mcid);
        _sxyp = new SurfXYPlane(dist,phi);
        _v = v;
        _z=  z;
        _dv2 = dv2;
        _dz2 = dz2;
        _dvdz = dvdz;
        
        Assert.assertTrue( _dv2 >= 0.0 && _dz2 >=0. );
        
        // check that determinant of _dhm is positive
        Assert.assertTrue( _dv2*_dz2 - _dvdz*_dvdz >= 0.0);
        
    }
    
    // constructor from a list of mc ids
    
    public ClusXYPlane2(double dist,double phi, double v, double z,
            double dv2, double dz2, double dvdz, List mcids )
    {
        super(mcids);
        _sxyp = new SurfXYPlane(dist,phi);
        _v = v;
        _z=  z;
        _dv2 = dv2;
        _dz2 = dz2;
        _dvdz = dvdz;
        
        Assert.assertTrue( _dv2 >= 0.0 && _dz2 >=0. );
        
        // check that determinant of _dhm is positive
        Assert.assertTrue( _dv2*_dz2 - _dvdz*_dvdz >= 0.0);
        
    }
    
    // copy constructor
    public ClusXYPlane2( ClusXYPlane2 clus)
    {
        super(clus);
        _sxyp = clus._sxyp;
        _v = clus.
                _v;_z = clus._z;
                _dv2 = clus._dv2;
                _dz2 = clus._dz2;
                _dvdz = clus._dvdz;
    }
    
    // return the type
    public String type()
    { return staticType();  };
    
    // return the surface
    public Surface surface()
    { return _sxyp; };
    public double v()
    { return _v; }
    public double z()
    { return _z; }
    public double dV2()
    { return _dv2; }
    public double dZ2()
    { return _dz2; }
    public double dVdZ()
    { return _dvdz; }
    
    // there are no more predictions.
    public Hit newNextPrediction()
    { return null; };
    
    public String toString()
    {
        return " vz " + _sxyp
                + ": vz =  [" + _v + " , " + _z + " ] +/- " +
                "[ " + _dv2 + " , " + _dz2 +" , " + _dvdz + " ] ";
    }
    
}
