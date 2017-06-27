/*
 * ClusterFindCyl2D_Test.java
 *
 * Created on July 24, 2007, 8:59 PM
 *
 * $Id: ClusterFindCyl2D_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfcyl;

import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfutil.Assert;
import org.lcsim.recon.tracking.trfutil.TRFMath;

/**
 *
 * @author Norman Graf
 */
public class ClusterFindCyl2D_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of ClusterFindCyl2D_Test */
    public void testClusterFindCyl2D()
    {
        String component = "ClusterFindCyl2D";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component " + component
                + ". --------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructor." );
        double r1 = 10.0;
        ClusterFindCyl2D find1 = new ClusterFindCyl2D(r1,10.0);
        if(debug) System.out.println( find1 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check the surface." );
        SurfCylinder srf1 = new SurfCylinder(r1);
        Assert.assertTrue( srf1.equals(find1.surface()) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Add clusters to finder." );
        Cluster clu11 = new ClusCylPhiZ2D(r1,1.23,0.02, 0.0, 0.1, 0.0);   // should fail nearby phi
        Cluster clu12 = new ClusCylPhiZ2D(r1,5.29,0.02, 0.0, 0.1, 0.0);   // should fail nearby phi
        Cluster clu13 = new ClusCylPhiZ2D(r1,2.38+TRFMath.TWOPI,0.02, 0.0, 0.1, 0.0);
        Cluster clu14 = new ClusCylPhiZ2D(r1,2.36,0.02, 0.5, 0.1, 0.0);   // should fail nearby z
        Cluster clu15 = new ClusCylPhiZ2D(r1,2.34,0.02, 0.0, 0.1, 0.0);
        find1.addCluster(clu11);
        find1.addCluster(clu12);
        find1.addCluster(clu13);
        find1.addCluster(clu14);
        int stat = find1.addCluster(clu15);
        if(debug) System.out.println( find1 );
        Assert.assertTrue( stat == 0 );
        if(debug) System.out.println("size= "+find1.clusters().size());
        Assert.assertTrue( find1.clusters().size() == 5 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Find nearby clusters." );
        TrackVector vec = new TrackVector();
        vec.set(0, 2.36);
        TrackError err = new TrackError();
        err.set(0,0 , 0.0001);
        err.set(1,1 , 0.01);
        err.set(2,2 , 0.01);
        err.set(3,3 , 0.01);
        err.set(4,4 , 0.01);
        ETrack tre1 = new ETrack( srf1.newPureSurface(), vec, err );
        if(debug) System.out.println( tre1 );
        List nearby_clusters = find1.clusters(tre1);
        if(debug) System.out.println(nearby_clusters.size());
        Assert.assertTrue( nearby_clusters.size() == 2 );
        if(debug) System.out.println( nearby_clusters.get(0) );
        if(debug) System.out.println( nearby_clusters.get(nearby_clusters.size()-1) );
        Assert.assertTrue( nearby_clusters.get(0).equals(clu15) );
        Assert.assertTrue( nearby_clusters.get(nearby_clusters.size()-1).equals(clu13) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Drop clusters." );
        find1.dropClusters();
        Assert.assertTrue( find1.clusters().size() == 0 );
        Assert.assertTrue( find1.clusters(tre1).size() == 0 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Find a single cluster." );
        find1.addCluster(clu13);
        Assert.assertTrue( find1.clusters().size() == 1 );
        Assert.assertTrue( find1.clusters(tre1).size() == 1 );
        
        
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
        
    }
    
}
