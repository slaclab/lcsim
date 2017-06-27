/*
 * SimInteractorRegistry_Test.java
 *
 * Created on July 24, 2007, 12:00 PM
 *
 * $Id: SimInteractorRegistry_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class SimInteractorRegistry_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of SimInteractorRegistry_Test */
    public void testSimInteractorRegistry()
    {
                String component = "SimInteractorRegistry";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        // make some surfaces:
        Surface srf1 = new BSurfTest( 10.0, 1 );
        Surface srf2 = new BSurfTest( 20.0, 2 );
        Surface srf3 = new BSurfTest( 30.0, 3 );
        Surface srf4 = new BSurfTest( 40.0, 4 );
        
        TrackVector trkv = new TrackVector();
        trkv.set(0, 2);
        trkv.set(1, 3);
        
        VTrack vtrk1 = new VTrack( srf1, trkv );
        VTrack vtrk2 = new VTrack( srf2, trkv );
        VTrack vtrk3 = new VTrack( srf3, trkv );
        VTrack vtrk4 = new VTrack( srf4, trkv );
        
        // make some interactors:
        SimInteractor int1 = new SimInteractorTest( 0.001, 1.0 );
        SimInteractor int2 = new SimInteractorTest( 0.001, 2.0 );
        SimInteractor int3 = new SimInteractorTest( 0.001, 3.0 );
        
        /// make the surface registry:
        if(debug) System.out.println("Test Constructor");
        SimInteractorRegistry regInter = new SimInteractorRegistry();
        // register some surfaces
        if(debug) System.out.println("Register some Interactors");
        regInter.registerInteractor( srf1, int1 );
        regInter.registerInteractor( srf2, int2 );
        regInter.registerInteractor( srf3, int3 );
        if(debug) System.out.println("Here's the Registry \n"+regInter);
        
        // get back a bounded surface
        if(debug) System.out.println("fetch a bounded surface");
        Surface bsurftest = regInter.bsurf(vtrk1);
        Assert.assertTrue( bsurftest.equals(srf1) );
        Assert.assertTrue( bsurftest instanceof BSurfTest );
        
        
        if(debug) System.out.println("Interact some tracks");
        if(debug) System.out.println( "Before scattering " + vtrk1 );
        regInter.interact( vtrk1 );
        if(debug) System.out.println( "After scattering " + vtrk1 );
        
        regInter.interact( vtrk2 );
        regInter.interact( vtrk3 );
        
        TrackVector vtrk1_p = vtrk1.vector();
        TrackVector vtrk2_p = vtrk2.vector();
        TrackVector vtrk3_p = vtrk3.vector();
        
        Assert.assertTrue( vtrk1_p.get(0) == trkv.get(0) );
        Assert.assertTrue( vtrk1_p.get(1) == trkv.get(1) );
        Assert.assertTrue( vtrk2_p.get(0) == 2*trkv.get(0) );
        Assert.assertTrue( vtrk2_p.get(1) == 2*trkv.get(1) );
        Assert.assertTrue( vtrk3_p.get(0) == 3*trkv.get(0) );
        Assert.assertTrue( vtrk3_p.get(1) == 3*trkv.get(1) );
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
    }
    
}
