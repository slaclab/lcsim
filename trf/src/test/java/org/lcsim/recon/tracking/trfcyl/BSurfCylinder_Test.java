/*
 * BSurfCylinder_Test.java
 *
 * Created on July 24, 2007, 9:00 PM
 *
 * $Id: BSurfCylinder_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfcyl;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.CrossStat;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class BSurfCylinder_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of BSurfCylinder_Test */
    public void testBSurfCylinder()
    {
        String ok_prefix = "BSurfCylinder test (I): ";
        String error_prefix = "BSurfCylinder test (E): ";
        
        if(debug) System.out.println("------ Testing component BSurfCylinder. ------" );
        
        if(debug) System.out.println("Test constructor and get routines." );
        BSurfCylinder bcy = new BSurfCylinder( 10.0, -20.0, 30.0 );
        if(debug) System.out.println( bcy );
        Assert.assertTrue( bcy.radius() == 10.0 );
        Assert.assertTrue( bcy.zMin() == -20.0 );
        Assert.assertTrue( bcy.zMax() == 30.0 );
        
        //********************************************************************
        
        if(debug) System.out.println("Equality tests varying parameters." );
        BSurfCylinder bcy1 = new BSurfCylinder( 10.0, -20.0, 30.0 );
        BSurfCylinder bcy2 = new BSurfCylinder( 10.0, 20.0, 30.0 );
        BSurfCylinder bcy3 = new BSurfCylinder( 10.0, -20.0, 20.0 );
        BSurfCylinder bcy4 = new BSurfCylinder( 20.0, -20.0, 30.0 );
        
        Assert.assertTrue( bcy.boundEqual(bcy) );
        Assert.assertTrue( bcy.boundEqual(bcy1) );
        Assert.assertTrue( ! bcy.boundEqual(bcy2) );
        Assert.assertTrue( ! bcy.boundEqual(bcy3) );
        Assert.assertTrue( ! bcy.boundEqual(bcy4) );
        
        //********************************************************************
        
        if(debug) System.out.println("Equality tests using base class." );
        SurfCylinder scy = new SurfCylinder(10.0);
        Assert.assertTrue( ! bcy.boundEqual(scy) );
        Assert.assertTrue( bcy.pureEqual(scy) );
        Assert.assertTrue( scy.pureEqual(bcy) );
        
        //********************************************************************
        
        if(debug) System.out.println("Test type." );
        Assert.assertTrue( bcy.type() != null );
        Assert.assertTrue( !bcy.type().equals(scy.type()) );
        Assert.assertTrue( bcy.type().equals(BSurfCylinder.staticType()) );
        
        //********************************************************************
        
        if(debug) System.out.println("Test purity." );
        Assert.assertTrue( ! bcy.isPure() );
        Assert.assertTrue( scy.isPure() );
        
        //********************************************************************
        
        if(debug) System.out.println("Test crossing status." );
        if(debug) System.out.println( bcy );
        TrackVector tvec = new TrackVector();
        TrackError terr = new TrackError();
        terr.set(1,1,4.0);
        // completely inside
        tvec.set(1,15.0);
        ETrack trk1 = new ETrack(bcy.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk1 );
        CrossStat xs = bcy.status(trk1);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( ! xs.outOfBounds() );
        // inside overlapping out
        tvec.set(1,25.0);
        ETrack trk2 = new ETrack(bcy.newPureSurface(),tvec,terr);
        xs = bcy.status(trk2);
        if(debug) System.out.println( trk2 );
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // outside overlapping in
        tvec.set(1,35.0);
        ETrack trk3 = new ETrack(bcy.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk3 );
        xs = bcy.status(trk3);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // way outside
        tvec.set(1,45.0);
        ETrack trk4 = new ETrack(bcy.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk4 );
        xs = bcy.status(trk4);
        if(debug) System.out.println( xs );
        Assert.assertTrue( ! xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // inside overlapping out
        tvec.set(1,-15.0);
        ETrack trk5 = new ETrack(bcy.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk5 );
        xs = bcy.status(trk5);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // outside overlapping in
        tvec.set(1,-25.0);
        ETrack trk6 = new ETrack(bcy.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk6 );
        xs = bcy.status(trk6);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // way outside
        tvec.set(1,-35.0);
        ETrack trk7 = new ETrack(bcy.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk7 );
        xs = bcy.status(trk7);
        if(debug) System.out.println( xs );
        Assert.assertTrue( ! xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        
        //********************************************************************
        
        // if(debug) System.out.println("Check cloning." );
        
        //********************************************************************
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix+"------------- All tests passed. -------------" );
        
        //********************************************************************
        
    }
    
}
