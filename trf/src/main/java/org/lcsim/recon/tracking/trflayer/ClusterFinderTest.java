package org.lcsim.recon.tracking.trflayer;
// Dummy cluster finder.
import java.util.*;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trflayer.ClusterFinder;

public class ClusterFinderTest extends ClusterFinder
{
    
    // static methods
    
    // Return the type name.
    public static String typeName()
    { return "ClusterFinderTest"; }
    
    
    // Return the type.
    public static String staticType()
    { return typeName(); }
    
    // attributes
    
    // surface
    private SurfTest _srf;
    
    // methods
    
    // output stream
    public String toString()
    {return "Test cluster finder.";
    }
    
    // constructor from surface parameter
    public  ClusterFinderTest(double x)
    {
        _srf = new SurfTest(x) ;
    }
    
    // Return the type.
    public  String get_type()
    { return staticType(); }
    
    // return the surface
    public   Surface surface()
    { return _srf; }
    
    // return the list of clusters associated with the surface
    public  List clusters()
    { return new ArrayList(); }
    
    // return the clusters near the specified track
    public  List clusters( ETrack tre)
    { return new ArrayList(); }
    
}
