package org.lcsim.recon.tracking.trfbase;

import java.util.*;

import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.HitTest;

public class ClusterTest extends Cluster
{
    // surface
    private  SurfTest _stst;
    
    // # predictions to generate
    private int _npred;
    
    public String toString()
    {
        return "Dummy hit (" + _npred + " predictions)";
    }
    public boolean equal(Cluster clus)
    { return _npred == ((ClusterTest) clus)._npred;
    }
    
    public List predict(ETrack tre)
    {
        List hits = new ArrayList();
        for ( int ipred=0; ipred<_npred; ++ipred )
            hits.add( new HitTest(ipred) );
        // don't set cluster association anywhere?
        return hits;
    }
    
    // static methods
    // Return the type name.
        /*public static String get_type_name()
        {
                return "ClusterTest";
        }
         */
        /*
        // Return the type.
        public static String get_static_type()
        {
                return get_type_name();
        }
         */
    
    public ClusterTest( SurfTest srf,int npred)
    {
        _stst  = srf;
        _npred = npred;
    }
    
    public ClusterTest( ClusterTest clus)
    {
        _stst  = clus._stst;
        _npred = clus._npred;
    }
    
    public String type()
    {
        return staticType();
    }
    
    //public SurfTest get_surface()
    public Surface surface()
    {
        return _stst;
    }
}
