/*
 * LayerStat_Test.java
 *
 * Created on July 24, 2007, 4:22 PM
 *
 * $Id: LayerStat_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trflayer;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.MissTest;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class LayerStat_Test extends TestCase
{
 private boolean debug;   
    /** Creates a new instance of LayerStat_Test */
    public void testLayerStat()
    {
        String component = "LayerStat";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Construct and fill status." );
        MissTest miss = new MissTest(2.0,0.4);
        ClusterFinderTest find = new ClusterFinderTest(2.0);
        Surface srf = find.surface();
        ETrack tre = new ETrack(srf.newPureSurface() );
        TLayer lyr = new TLayer(true,true,miss,find,5);
        LayerStat lstat = lyr.propagate();
        if(debug) System.out.println( lstat );
        lyr.check(lstat);
        Assert.assertTrue( lstat.atExit() );
        Assert.assertTrue( lstat.hasClusters() );
        Assert.assertTrue( lstat.clusters().size() == 0 );
        Assert.assertTrue( lstat.clusters(tre).size() == 0 );
        Assert.assertTrue( lstat.miss().likelihood() == 0.4 );
        Assert.assertTrue( lstat.layer() == lyr );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test copy." );
        LayerStat lstat2 = new LayerStat(lstat);
        if(debug) System.out.println( lstat2 );
        Assert.assertTrue( lstat2.atExit() );
        Assert.assertTrue( lstat2.hasClusters() );
        Assert.assertTrue( lstat2.clusters().size() == 0 );
        Assert.assertTrue( lstat2.clusters(tre).size() == 0 );
        Assert.assertTrue( lstat2.miss().likelihood() == 0.4 );
        Assert.assertTrue( lstat2.layer() == lyr );
        lyr.check(lstat2);
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************        
    }
    
}
