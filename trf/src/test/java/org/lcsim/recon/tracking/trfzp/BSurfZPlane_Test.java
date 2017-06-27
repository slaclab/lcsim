/*
 * BSurfZPlane_Test.java
 *
 * Created on July 24, 2007, 11:10 PM
 *
 * $Id: BSurfZPlane_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfzp;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.CrossStat;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class BSurfZPlane_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of BSurfZPlane_Test */
    public void testBSurfZPlane()
    {
        String ok_prefix = "BSurfZPlane test (I): ";
        String error_prefix = "BSurfZPlane test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "------ Testing component BSurfZPlane. ------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructor and get routines." );
        BSurfZPlane bzp = new BSurfZPlane( 10.0, -20.0, 30.0, -120.0, 130.0 );
        if(debug) System.out.println( bzp );
        Assert.assertTrue(  bzp.z() == 10.0  );
        Assert.assertTrue(  bzp.xMin() == -20.0  );
        Assert.assertTrue(  bzp.xMax() == 30.0  );
        Assert.assertTrue(  bzp.yMin() == -120.0  );
        Assert.assertTrue(  bzp.yMax() == 130.0  );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test type." );
        Assert.assertTrue( bzp.type() != null );
        Assert.assertTrue( bzp.type() == BSurfZPlane.staticType() );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check cloning." );
        Surface srf = ( bzp.newSurface());
        Surface srf_pure = (bzp.newPureSurface());
        Assert.assertTrue( srf.type().equals(BSurfZPlane.staticType()) );
        Assert.assertTrue( srf_pure.type().equals(SurfZPlane.staticType()) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Equality tests varying parameters." );
        Surface srf1 = new BSurfZPlane( 10.0, -20.0, 30.0, -120.0, 130.0 );
        Surface srf2 = new BSurfZPlane( 10.0,  20.0, 30.0, -120.0, 130.0 );
        Surface srf3 = new BSurfZPlane( 10.0, -20.0, 20.0, -120.0, 130.0 );
        Surface srf4 = new BSurfZPlane( 20.0, -20.0, 30.0, -120.0, 130.0 );
        Surface srf5 = new BSurfZPlane( 10.0, -20.0, 30.0,  121.0, 130.0 );
        Surface srf6 = new BSurfZPlane( 10.0, -20.0, 30.0, -120.0, 131.0 );
        Assert.assertTrue( srf.boundEqual(srf) );
        Assert.assertTrue( srf.boundEqual(srf1) );
        Assert.assertTrue( ! srf.boundEqual(srf2) );
        Assert.assertTrue( ! srf.boundEqual(srf3) );
        Assert.assertTrue( ! srf.boundEqual(srf4) );
        Assert.assertTrue( ! srf.boundEqual(srf5) );
        Assert.assertTrue( ! srf.boundEqual(srf6) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test purity." );
        Assert.assertTrue( ! bzp.isPure() );
        Assert.assertTrue( srf_pure.isPure() );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test crossing status." );
        if(debug) System.out.println( bzp );
        TrackVector tvec = new TrackVector();
        TrackError terr = new TrackError();
        terr.set(SurfZPlane.IX,SurfZPlane.IX, 4.0);
        terr.set(SurfZPlane.IY,SurfZPlane.IY, 4.0);
        
        // Checking X component
        
        tvec.set(SurfZPlane.IY, 0.0);
        
        // completely inside
        tvec.set(SurfZPlane.IX, 15.0);
        ETrack trk1 = new ETrack(srf,tvec,terr);
        if(debug) System.out.println( trk1 );
        CrossStat xs = srf.status(trk1);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( !xs.outOfBounds() );
        // inside overlapping out
        tvec.set(SurfZPlane.IX, 25.0);
        ETrack trk2 = new ETrack(srf,tvec,terr);
        xs = srf.status(trk2);
        if(debug) System.out.println( trk2 );
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // outside overlapping in
        tvec.set(SurfZPlane.IX, 35.0);
        ETrack trk3 = new ETrack(srf,tvec,terr);
        if(debug) System.out.println( trk3 );
        xs = srf.status(trk3);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // way outside
        tvec.set(SurfZPlane.IX, 45.0);
        ETrack trk4 = new ETrack(srf,tvec,terr);
        if(debug) System.out.println( trk4 );
        xs = srf.status(trk4);
        if(debug) System.out.println( xs );
        Assert.assertTrue( !xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // inside overlapping out
        tvec.set(SurfZPlane.IX, -15.0);
        ETrack trk5 = new ETrack(srf,tvec,terr);
        if(debug) System.out.println( trk5 );
        xs = srf.status(trk5);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // outside overlapping in
        tvec.set(SurfZPlane.IX, -25.0);
        ETrack trk6 = new ETrack(srf,tvec,terr);
        if(debug) System.out.println( trk6 );
        xs = srf.status(trk6);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // way outside
        tvec.set(SurfZPlane.IX, -35.0);
        ETrack trk7 = new ETrack(srf,tvec,terr);
        if(debug) System.out.println( trk7 );
        xs = srf.status(trk7);
        if(debug) System.out.println( xs );
        Assert.assertTrue( !xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        
        
        // Checking Y component
        
        tvec.set(SurfZPlane.IX, 0.0);
        
        // completely inside
        tvec.set(SurfZPlane.IY, 115.0);
        ETrack trk11 = new ETrack(srf,tvec,terr);
        if(debug) System.out.println( trk11 );
        xs = srf.status(trk11);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( !xs.outOfBounds() );
        // inside overlapping out
        tvec.set(SurfZPlane.IY, 125.0);
        ETrack trk12 = new ETrack(srf,tvec,terr);
        xs = srf.status(trk12);
        if(debug) System.out.println( trk12 );
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // outside overlapping in
        tvec.set(SurfZPlane.IY, 135.0);
        ETrack trk13 = new ETrack(srf,tvec,terr);
        if(debug) System.out.println( trk13 );
        xs = srf.status(trk13);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // way outside
        tvec.set(SurfZPlane.IY, 145.0);
        ETrack trk14 = new ETrack(srf,tvec,terr);
        if(debug) System.out.println( trk14 );
        xs = srf.status(trk14);
        if(debug) System.out.println( xs );
        Assert.assertTrue( !xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // inside overlapping out
        tvec.set(SurfZPlane.IY, -115.0);
        ETrack trk15 = new ETrack(srf,tvec,terr);
        if(debug) System.out.println( trk15 );
        xs = srf.status(trk15);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // outside overlapping in
        tvec.set(SurfZPlane.IY, -125.0);
        ETrack trk16 = new ETrack(srf,tvec,terr);
        if(debug) System.out.println( trk16 );
        xs = srf.status(trk16);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // way outside
        tvec.set(SurfZPlane.IY, -135.0);
        ETrack trk17 = new ETrack(srf,tvec,terr);
        if(debug) System.out.println( trk17 );
        xs = srf.status(trk17);
        if(debug) System.out.println( xs );
        Assert.assertTrue( !xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
        
    }
    
}
