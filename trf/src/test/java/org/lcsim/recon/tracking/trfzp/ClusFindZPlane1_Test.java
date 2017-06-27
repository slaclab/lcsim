/*
 * ClusFindZPlane1_Test.java
 *
 * Created on July 24, 2007, 11:09 PM
 *
 * $Id: ClusFindZPlane1_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfzp;

import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class ClusFindZPlane1_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of ClusFindZPlane1_Test */
    public void testClusFindZPlane1()
    {
        String component = "ClusFindZPlane1";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component " + component
                + ". --------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructor." );
        double zpos1 = 10.0;
        ClusFindZPlane1 find1 = new ClusFindZPlane1(zpos1,0.01,0.02,10.0);
        if(debug) System.out.println( find1 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check the surface." );
        SurfZPlane srf1 = new SurfZPlane(zpos1);
        Assert.assertTrue( srf1.equals(find1.surface()) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Construct an axy finder." );
        double zpos2 = 20.0;
        double wx2 = 0.1;
        double wy2 = 0.2;
        ClusFindZPlane1 find2 = new ClusFindZPlane1(zpos2,wx2,wy2,10.0);
        if(debug) System.out.println( find2 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Add axy clusters to finder." );
        Cluster clu21 = new ClusZPlane1(zpos2,wx2,wy2,3.004,0.02);
        Cluster clu22 = new ClusZPlane1(zpos2,wx2,wy2,3.071,0.02); //wrong
        Cluster clu23 = new ClusZPlane1(zpos2,wx2,wy2,2.935,0.02);
        Cluster clu24 = new ClusZPlane1(zpos2,wx2,wy2,2.929,0.02); //wrong
        Cluster clu25 = new ClusZPlane1(zpos2,wx2,wy2,3.065,0.02);
        find2.addCluster(clu21);
        find2.addCluster(clu22);
        find2.addCluster(clu23);
        find2.addCluster(clu24);
        int stat = find2.addCluster(clu25);
        Assert.assertTrue( stat == 0 );
        Assert.assertTrue( find2.clusters().size() == 5 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Find nearby axy clusters." );
        SurfZPlane srf2 = new SurfZPlane(zpos2);
        TrackVector vec = new TrackVector();
        TrackError err = new TrackError();
        err.set(SurfZPlane.IX,SurfZPlane.IX, 0.001);
        err.set(SurfZPlane.IY,SurfZPlane.IY, 0.001);
        err.set(SurfZPlane.IDXDZ,SurfZPlane.IDXDZ, 0.01);
        err.set(SurfZPlane.IDYDZ,SurfZPlane.IDYDZ, 0.01);
        err.set(SurfZPlane.IQP,SurfZPlane.IQP, 0.01);
        vec.set(SurfZPlane.IY, 35.0);
        vec.set(SurfZPlane.IX, (3.0 - wy2*vec.get(SurfZPlane.IY))/wx2);
        ETrack tre2 = new ETrack(srf2.newPureSurface(),vec,err);
        if(debug) System.out.println( tre2 );
        
        List nearby_clusters = find2.clusters(tre2);
        Assert.assertTrue( nearby_clusters.size() == 3 );
        if(debug) System.out.println( nearby_clusters.get(0) );
        if(debug) System.out.println( nearby_clusters.get(nearby_clusters.size()-1) );
        Assert.assertTrue( nearby_clusters.get(0).equals(clu23) );
        Assert.assertTrue( nearby_clusters.get(nearby_clusters.size()-1).equals(clu25) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Drop clusters." );
        find2.dropClusters();
        Assert.assertTrue( find2.clusters().size() == 0 );
        Assert.assertTrue( find2.clusters(tre2).size() == 0 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Find a single cluster." );
        find2.addCluster(clu23);
        Assert.assertTrue( find2.clusters().size() == 1 );
        Assert.assertTrue( find2.clusters(tre2).size() == 1 );
        
        
        //********************************************************************
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
        
    }
    
}
