package org.lcsim.recon.tracking.trffit;
import java.util.*;

import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfutil.Assert;

public class ClusTestFit1 extends Cluster
{
    
    // static methods
    
    // Return the type name.
    //public static String get_type_name() { return "ClusTestFit1"; }
    
    // Return the type.
    //public static String get_static_type() { return get_type_name(); }
    
    
    private SurfTest _stst;     // surface
    private double _msmt;               // measurement
    private double _emsmt;              // square of measured error
    public String toString()
    { return "ClusTestFit1 with measurement " + _msmt + " +/- "
              + Math.sqrt(_emsmt);
    }
    
    // Equality.
    public boolean equal(  Cluster rhs)
    {
        Assert.assertTrue( rhs.type() == type() );
        ClusTestFit1 clus = (  ClusTestFit1) rhs;
        return _msmt == clus._msmt && _emsmt == clus._emsmt;
    }
    
    // generate hit prediction ==> pass msmt and track
    // In this simple example, there is only one hit/cluster.
    public List predict(  ETrack tre)
    {
        List hits = new ArrayList();
        hits.add( new HitTestFit1(_msmt,_emsmt,tre) );
        return hits;
    }
    
    
    
    //  constructor from msmt and its error
    public ClusTestFit1(  SurfTest srf,double msmt,double emsmt)
    {
        _stst=srf;
        _msmt=msmt;
        _emsmt=emsmt;
    };
    
    // Return the type.
    public String type()
    { return staticType(); }
    
    // Return the surface.
    public Surface surface()
    { return _stst; };
    
}
