/*
 * LTrack_Test.java
 *
 * Created on July 24, 2007, 4:29 PM
 *
 * $Id: LTrack_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
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
public class LTrack_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of LTrack_Test */
    public void testLTrack()
    {
        String component = "LTrack";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructor." );
        SurfTest stst = new SurfTest(1.0);
        ETrack tre = new ETrack( stst.newPureSurface() );
        if(debug) System.out.println("tre= "+tre);
        Layer ltst = new TLayer();
        LayerStat lstat = new LayerStat(ltst);
        lstat.setState(2);
        //  LTrack trl1 = new LTrack();
        LTrack trl1 = new LTrack(tre, lstat);
        if(debug) System.out.println("trl1= "+trl1);
        //fetch reference to the Etrack...
        ETrack tmp = trl1.track();
        if(debug) System.out.println("tmp= "+tmp);
        //Set this reference to a real Etrack...
        tmp = tre;
        if(debug) System.out.println("tmp= "+tmp);
        trl1.pushStatus(lstat);
        if(debug) System.out.println( "trl1= "+trl1 );
        // trl1 should now point to a real track... but it doesn't!
        LTrack trl2 = new LTrack(trl1);
        if(debug) System.out.println( "trl2= "+trl2 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test access." );
        if(debug) System.out.println("\n ***trl1.get_track()= \n"+trl1.track());
        if(debug) System.out.println("\n ***trl2.get_track()= \n"+trl2.track());
        Assert.assertTrue( trl1.track().equals(trl2.track()) );
        LayerStatChain lsc = trl1.statusChain();
        if(debug) System.out.println("***lsc= \n"+lsc);
        if(debug) System.out.println("\n "+trl1.status().state() );
        Assert.assertTrue( trl1.status().state() == 2 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test status manipulation." );
        Assert.assertTrue( trl1.atValidStatus() );
        Assert.assertTrue( trl1.status().state() == 2 );
        if(debug) System.out.println( trl1 );
        trl1.status().setState(1);
        if(debug) System.out.println( trl1 );
        Assert.assertTrue( trl1.status().state() == 1 );
        // insert 2nd status
        lstat.setState(2);
        trl1.pushStatus(lstat);
        if(debug) System.out.println( trl1 );
        Assert.assertTrue( trl1.status().state() == 2 );
        Assert.assertTrue( trl1.setPreviousStatus() );
        // insert 3rd status
        lstat.setState(3);
        trl1.pushStatus(lstat);
        Assert.assertTrue( ! trl1.atTopStatus() );
        Assert.assertTrue( trl1.status().state() == 3 );
        // step up to 2nd status
        Assert.assertTrue( trl1.setPreviousStatus() );
        if(debug) System.out.println(trl1.status().state());
        if(debug) System.out.println("lsc= "+trl1.statusChain());
        Assert.assertTrue( trl1.status().state() == 2 );
        // step up to 1st status
        Assert.assertTrue( trl1.setPreviousStatus() );
        Assert.assertTrue( ! trl1.setPreviousStatus() );
        Assert.assertTrue( trl1.atTopStatus() );
        Assert.assertTrue( trl1.status().state() == 1 );
        // step down
        Assert.assertTrue( trl1.setNextStatus() );
        if(debug) System.out.println("get_state()= "+trl1.status().state());
        Assert.assertTrue( trl1.status().state() == 2 );
        Assert.assertTrue( trl1.setNextStatus() );
        Assert.assertTrue( trl1.status().state() == 3 );
        Assert.assertTrue( ! trl1.setNextStatus() );
        // pop last status -- should leave us positioned at 2nd
        Assert.assertTrue( trl1.popStatus() );
        Assert.assertTrue( trl1.status().state() == 2 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test propagator status" );
        Assert.assertTrue( ! trl2.propStat().success() );
        trl2.propStat().setForward();
        Assert.assertTrue( trl2.propStat().success() );
        double s = 123.4;
        trl2.propStat().setPathDistance(s);
        Assert.assertTrue( trl2.propStat().pathDistance() == s );
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
    }
    
}
