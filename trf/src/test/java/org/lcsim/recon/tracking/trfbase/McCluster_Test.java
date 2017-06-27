/*
 * McCluster_Test.java
 *
 * Created on July 24, 2007, 12:14 PM
 *
 * $Id: McCluster_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class McCluster_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of McCluster_Test */
    public void testMcCluster()
    {
         String component = "McCluster";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println("-------- Testing component " + component
                + ". --------" );
        
        //********************************************************************
        
        if(debug) System.out.println("Check MC ID's." );
        SurfTest stest = new SurfTest(3);
        int[] ids =  {1, 22, 333};
        
        Cluster pclu1 = new McClusterTest(stest,ids);
        {
            List oids = pclu1.mcIds();
            int[] oida = pclu1.mcIdArray();
            //			Assert.assertTrue( oids.size() == 3 );
            Assert.assertTrue( oida.length == 3 );
            //			Assert.assertTrue( ((Integer) oids.get(0)).valueOf() == 1 );
            Assert.assertTrue( oida[0] == 1 );
            Assert.assertTrue( oida[1] == 22 );
            Assert.assertTrue( oida[2] == 333 );
        }
        
        if(debug) System.out.println("Constructor from list of Ids");
        List mcids = new ArrayList();
        for(int i = 0; i<ids.length; ++i)
        {
            mcids.add(new Integer( ids[i]) );
        }
        Cluster clus2 = new McClusterTest(stest, mcids);
        int[] tstids = clus2.mcIdArray();
        Assert.assertTrue( tstids.length == 3 );
        Assert.assertTrue( tstids[0] == 1 );
        Assert.assertTrue( tstids[1] == 22 );
        Assert.assertTrue( tstids[2] == 333 );
        
        if(debug) System.out.println("Constructor from single Id");
        int mcid = 137;
        Cluster clus3 = new McClusterTest(stest, mcid);
        tstids = clus3.mcIdArray();
        Assert.assertTrue( tstids.length == 1 );
        Assert.assertTrue( tstids[0] == 137 );
        
        //********************************************************************
        
        if(debug) System.out.println("Copy." );
        Cluster pclu2 = new McClusterTest((McClusterTest)pclu1);
        
        {
            int[] oids = pclu2.mcIdArray();
            Assert.assertTrue( oids.length == 3 );
            Assert.assertTrue( oids[0] == 1 );
            Assert.assertTrue( oids[1] == 22 );
            Assert.assertTrue( oids[2] == 333 );
        }
        
        
        //********************************************************************
        
        if(debug) System.out.println( component+" ------------- All tests passed. -------------" );
        
        
        //********************************************************************       
    }
    
}
