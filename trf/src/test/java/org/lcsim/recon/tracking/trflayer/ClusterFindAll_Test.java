/*
 * ClusterFindAll_Test.java
 *
 * Created on July 24, 2007, 4:35 PM
 *
 * $Id: ClusterFindAll_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trflayer;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.ClusterTest;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class ClusterFindAll_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of ClusterFindAll_Test */
    public void testClusterFindAll()
    {
         String component = "ClusterFindAll";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println(ok_prefix
                + "-------- Testing component " + component
                + ". --------" );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test constructor." );
        SurfTest psrf6 = new SurfTest(6.0);
        ClusterFindAll find = new ClusterFindAll(psrf6);
        if(debug) System.out.println(find );
        
        Assert.assertTrue( find.surface().parameter(0) == 6.0 );
        Assert.assertTrue( find.clusters().size() == 0 );
        ETrack tre = new ETrack(psrf6);
        Assert.assertTrue( find.clusters(tre).size() == 0 );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Add some clusters."  );
        find.addCluster( new ClusterTest(psrf6,1) );
        find.addCluster( new ClusterTest(psrf6,2) );
        find.addCluster( new ClusterTest(psrf6,3) );
        if(debug) System.out.println(find );
        Assert.assertTrue( find.clusters().size() == 3 );
        Assert.assertTrue( find.clusters(tre).size() == 3 );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Drop the clusters."  );
        find.dropClusters();
        if(debug) System.out.println(find );
        Assert.assertTrue( find.clusters().size() == 0 );
        Assert.assertTrue( find.clusters(tre).size() == 0 );
        
        //********************************************************************
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix
                + "------------- All tests passed. -------------" );
        
        
        //********************************************************************       
    }
    
}
