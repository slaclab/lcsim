/*
 * InteractorRegistry_Test.java
 *
 * Created on July 24, 2007, 12:16 PM
 *
 * $Id: InteractorRegistry_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class InteractorRegistry_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of InteractorRegistry_Test */
    public void testInteractorRegistry()
    {
        String component = "InteractorRegistry";
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
        
        TrackError err = new TrackError();
        err.setIdentity();
        
        VTrack vtrk1 = new VTrack( srf1, trkv );
        VTrack vtrk2 = new VTrack( srf2, trkv );
        VTrack vtrk3 = new VTrack( srf3, trkv );
        VTrack vtrk4 = new VTrack( srf4, trkv );
        
        ETrack etrk1 = new ETrack( vtrk1, err );
        ETrack etrk2 = new ETrack( vtrk2, err );
        ETrack etrk3 = new ETrack( vtrk3, err );
        ETrack etrk4 = new ETrack( vtrk4, err );
        
        // make some interactors:
        Interactor int1 = new InteractorTest( 1.0 );
        Interactor int2 = new InteractorTest( 2.0 );
        Interactor int3 = new InteractorTest( 3.0 );
        
        /// make the surface registry:
        if(debug) System.out.println("Test Constructor");
        InteractorRegistry regInter = new InteractorRegistry();
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
        regInter.interact( etrk1 );
        
        if(debug) System.out.println( "Before interacting " + etrk2 );
        regInter.interact( etrk2 );
        if(debug) System.out.println( "After interacting " + etrk2 );
        
        regInter.interact( etrk3 );
        
        TrackVector vtrk1_p = etrk1.vector();
        TrackVector vtrk2_p = etrk2.vector();
        TrackVector vtrk3_p = etrk3.vector();
        
        // Track vector should not change...
        Assert.assertTrue( vtrk1_p.equals(trkv) );
        
        TrackError terr1 = etrk1.error();
        TrackError terr2 = etrk2.error();
        TrackError terr3 = etrk3.error();
        
        // Track error(0,0) should change
        // by amount specified in InteractorTest constructor..
        Assert.assertTrue( terr1.get(0,0) == err.get(0,0) );
        Assert.assertTrue( terr2.get(0,0) == 2.*err.get(0,0) );
        // other elements should not change...
        Assert.assertTrue( terr2.get(1,1) == err.get(1,1) );
        Assert.assertTrue( terr2.get(2,2) == err.get(2,2) );
        Assert.assertTrue( terr2.get(3,3) == err.get(3,3) );
        Assert.assertTrue( terr2.get(4,4) == err.get(4,4) );
        
        Assert.assertTrue( terr3.get(0,0) == 3.*err.get(0,0) );
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );        
    }
    
}
