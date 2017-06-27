/*
 * Layer_Test.java
 *
 * Created on July 24, 2007, 3:10 PM
 *
 * $Id: Layer_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trflayer;

import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class Layer_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of Layer_Test */
    public void testLayer()
    {
                String component = "Layer";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println(ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test constructor." );
        LayerTest ltest1 = new LayerTest(10.,3);
        LayerTest ltest2 = new LayerTest(20.);
        if(debug) System.out.println(ltest1 );
        if(debug) System.out.println(ltest1.get_type() );
        if(debug) System.out.println(ltest2.get_type() );
        Assert.assertTrue( ltest1.get_type().equals(ltest2.get_type()) );
        Assert.assertTrue( ltest1.hasClusters() );
        Assert.assertTrue( ltest1.clusters().size() == 0 );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Construct propagator and track." );
        PropTest prop = new PropTest();
        if(debug) System.out.println(prop );
        SurfTest stest = new SurfTest(2);
        TrackVector vec = new TrackVector();
        TrackError err = new TrackError();
        ETrack tre = new ETrack( stest.newPureSurface(), vec, err );
        ETrack tre0 = new ETrack(tre);
        if(debug) System.out.println(tre );
        
        //********************************************************************
        // This needs to be changed to LTrack...
        if(debug) System.out.println(ok_prefix + "Propagate 1." );
        List ltracks1 = ltest1.propagate(tre,prop);
        Assert.assertTrue( ltracks1.size() == 1 );
        if(debug) System.out.println("ltracks1.get(0)= "+ltracks1.get(0) );
        //  const LayerStat& lstat1 = ltracks1.front().get_status();
        //  Assert.assertTrue( ! lstat1.at_exit() );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Propagate 2." );
        List ltracks2 = ltest1.propagate( ((LTrack)ltracks1.get(0)).track(),prop);
        Assert.assertTrue( ltracks2.size() == 1 );
        if(debug) System.out.println(ltracks2.get(0) );
        //  const LayerStat& lstat2 = ltracks2.front().get_status();
        //  Assert.assertTrue( ! lstat2.at_exit() );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Propagate 3." );
        List ltracks3 = ltest1.propagate( ((LTrack) ltracks2.get(0)).track(),prop);
        Assert.assertTrue( ltracks3.size() == 1 );
        if(debug) System.out.println(ltracks3.get(0) );
        //  const LayerStat& lstat3 = ltracks3.front().get_status();
        //  Assert.assertTrue( lstat3.at_exit() );
        
        
        if(debug) System.out.println(ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
        
    }
    
}
