/*
 * ClusterFinder_Test.java
 *
 * Created on July 24, 2007, 4:33 PM
 *
 * $Id: ClusterFinder_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trflayer;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class ClusterFinder_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of ClusterFinder_Test */
    public void testClusterFinder()
    {
         String component = "ClusterFinder";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println(ok_prefix
                + "-------- Testing component " + component
                + ". --------" );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test constructor." );
        ClusterFinderTest find = new ClusterFinderTest(3.0);
        if(debug) System.out.println(find );
        Assert.assertTrue( find.surface().parameter(0) == 3.0 );
        Assert.assertTrue( find.clusters().size() == 0 );
        ETrack tre = new ETrack( new SurfTest(3.0) );
        Assert.assertTrue( find.clusters(tre).size() == 0 );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************       
    }
    
}
