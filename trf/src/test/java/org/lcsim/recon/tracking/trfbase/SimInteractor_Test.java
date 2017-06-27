/*
 * SimInteractor_Test.java
 *
 * Created on July 24, 2007, 11:59 AM
 *
 * $Id: SimInteractor_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class SimInteractor_Test extends TestCase       
{
    private boolean debug;
    /** Creates a new instance of SimInteractor_Test */
    public void testSimInteractor()
    {
       String component = "SimInteractor";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println("---------- Testing component " + component
                + ". ----------" );
        
        SimInteractorTest inter = new SimInteractorTest( 1.0, 3.0 );
        Assert.assertTrue( inter.get_rad_length() == 1.0 );
        
        VTrack vtrk = new VTrack();
        TrackVector trv = new TrackVector();
        trv.set(0,2);
        trv.set(1,3);
        if(debug) System.out.println(  "TrackVector trv before interacting..." + trv );
        vtrk.setVector( trv );
        if(debug) System.out.println(  "Track vtrk before interacting..." + vtrk );
        inter.interact( vtrk );
        if(debug) System.out.println(  "Track vtrk after interacting..." + vtrk );
        
        TrackVector trvprime = vtrk.vector();
        Assert.assertTrue( trvprime.get(0) == 3.0*trv.get(0) );
        Assert.assertTrue( trvprime.get(1) == 3.0*trv.get(1) );        
    }
    
}
