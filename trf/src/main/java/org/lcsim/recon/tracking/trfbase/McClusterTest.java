package org.lcsim.recon.tracking.trfbase;
// Dummy concrete cluster and hit classes.
// These are only used for testing.
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.McCluster;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.McHitTest;

// Cluster.
public class McClusterTest extends McCluster
{
    
    // surface
    public SurfTest _stst;
        /*
        // Return the type name.
        public  String get_type_name()
        { return "McClusterTest";
        }
         */
        /*
        // Return the type.
        public static String get_static_type()
        { return get_creator();
        }
         */
    public String toString()
    {
        return  "McCluster test.";
    }
    public boolean equal( Cluster clus)
    { List tmp1 = mcIds();
      List tmp2 = ((McClusterTest) clus).mcIds();
      if (tmp1.size() != tmp2.size()) return false;
      Iterator it2 = tmp2.iterator();
      for(Iterator it = tmp1.iterator() ; it.hasNext() ; )
      {
          if ( ((Cluster)it.next()).notEquals( ((Cluster)it2.next()) ) ) return false;
      }
      return true;
    }
    
    public List predict( ETrack tre)
    {
        List hits = new ArrayList();
        hits.add( (Hit) (new McHitTest()) );
        return hits;
    }
    
    public McClusterTest( SurfTest stst,  int mcid)
    {
        super(mcid);
        _stst=new SurfTest(stst);
    }
    
    public McClusterTest( SurfTest stst,  int[] mcids)
    {
        super(mcids);
        _stst=new SurfTest(stst);
    }
    
    public McClusterTest( SurfTest stst,  List mcids)
    {
        super(mcids);
        _stst=new SurfTest(stst);
    }
    
    public McClusterTest(McClusterTest mct)
    {
        super(mct.mcIdArray());
        _stst=new SurfTest(mct._stst);
    }
    
    public String type()
    { return staticType();
    }
    
    public Surface surface()
    { return _stst;
    }
}


