/*
 * BSurfXYPlane_Test.java
 *
 * Created on July 24, 2007, 10:34 PM
 *
 * $Id: BSurfXYPlane_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfxyp;

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
public class BSurfXYPlane_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of BSurfXYPlane_Test */
    public void testBSurfXYPlane()
    {
        String ok_prefix = "BSurfXYPlane test (I): ";
        String error_prefix = "BSurfXYPlane test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "------ Testing component BSurfXYPlane. ------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructor and get routines." );
        BSurfXYPlane bxyp = new BSurfXYPlane( 10.0, Math.PI/3., -20.0, 30.0, -120.0, 130.0 );
        if(debug) System.out.println( bxyp );
        Assert.assertTrue(  bxyp.parameter(SurfXYPlane.DISTNORM) == 10.0   );
        Assert.assertTrue(  bxyp.parameter(SurfXYPlane.NORMPHI) == Math.PI/3.   );
        Assert.assertTrue(  bxyp.vMin() == -20.0  );
        Assert.assertTrue(  bxyp.vMax() == 30.0   );
        Assert.assertTrue(  bxyp.zMin() == -120.0  );
        Assert.assertTrue(  bxyp.zMax() == 130.0   );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Equality tests varying parameters." );
        BSurfXYPlane bxyp1 = new BSurfXYPlane( 10.0, Math.PI/3., -20.0, 30.0, -120.0, 130.0 );
        BSurfXYPlane bxyp2 = new BSurfXYPlane( 10.0, Math.PI/3.,  20.0, 30.0, -120.0, 130.0 );
        BSurfXYPlane bxyp3 = new BSurfXYPlane( 10.0, Math.PI/3., -20.0, 20.0, -120.0, 130.0 );
        BSurfXYPlane bxyp4 = new BSurfXYPlane( 20.0, Math.PI/3., -20.0, 30.0, -120.0, 130.0 );
        BSurfXYPlane bxyp5 = new BSurfXYPlane( 10.0, Math.PI/5., -20.0, 30.0, -120.0, 130.0 );
        BSurfXYPlane bxyp6 = new BSurfXYPlane( 10.0, Math.PI/3., -20.0, 30.0,  121.0, 130.0 );
        BSurfXYPlane bxyp7 = new BSurfXYPlane( 10.0, Math.PI/3., -20.0, 30.0, -120.0, 131.0 );
        Assert.assertTrue( bxyp.boundEqual(bxyp) );
        Assert.assertTrue( bxyp.boundEqual(bxyp1) );
        Assert.assertTrue( ! bxyp.boundEqual(bxyp2) );
        Assert.assertTrue( ! bxyp.boundEqual(bxyp3) );
        Assert.assertTrue( ! bxyp.boundEqual(bxyp4) );
        Assert.assertTrue( ! bxyp.boundEqual(bxyp5) );
        Assert.assertTrue( ! bxyp.boundEqual(bxyp6) );
        Assert.assertTrue( ! bxyp.boundEqual(bxyp7) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Equality tests using base class." );
        SurfXYPlane sxyp = new SurfXYPlane(10.0, Math.PI/3.);
        SurfXYPlane psxyp = bxyp;
        Assert.assertTrue( bxyp.boundEqual(psxyp) );
        Assert.assertTrue( psxyp.boundEqual(bxyp) );
        Assert.assertTrue( ! psxyp.boundEqual(bxyp2) );
        Assert.assertTrue( ! bxyp.boundEqual(sxyp) );
        Assert.assertTrue( bxyp.pureEqual(sxyp) );
        Assert.assertTrue( sxyp.pureEqual(bxyp) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test type." );
        Assert.assertTrue( bxyp.type() != null );
        Assert.assertTrue( !bxyp.type().equals(sxyp.type()) );
        Assert.assertTrue( bxyp.type().equals(BSurfXYPlane.staticType()) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test purity." );
        Assert.assertTrue( ! bxyp.isPure() );
        Assert.assertTrue( sxyp.isPure() );
        Assert.assertTrue( ! psxyp.isPure() );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test crossing status." );
        if(debug) System.out.println( bxyp );
        TrackVector tvec = new TrackVector();
        TrackError terr = new TrackError();
        terr.set(SurfXYPlane.IV,SurfXYPlane.IV, 4.0);
        terr.set(SurfXYPlane.IZ,SurfXYPlane.IZ,  4.0);
        
        // Checking V component
        
        tvec.set(SurfXYPlane.IZ, 0.0);
        
        // completely inside
        tvec.set(SurfXYPlane.IV, 15.0);
        ETrack trk1= new ETrack(bxyp.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk1 );
        CrossStat xs = bxyp.status(trk1);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( !xs.outOfBounds() );
        // inside overlapping out
        tvec.set(SurfXYPlane.IV, 25.0);
        ETrack trk2= new ETrack(bxyp.newPureSurface(),tvec,terr);
        xs = bxyp.status(trk2);
        if(debug) System.out.println( trk2 );
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds());
        Assert.assertTrue( xs.outOfBounds());
        // outside overlapping in
        tvec.set(SurfXYPlane.IV, 35.0);
        ETrack trk3= new ETrack(bxyp.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk3 );
        xs = bxyp.status(trk3);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds());
        Assert.assertTrue( xs.outOfBounds());
        // way outside
        tvec.set(SurfXYPlane.IV, 45.0);
        ETrack trk4= new ETrack(bxyp.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk4 );
        xs = bxyp.status(trk4);
        if(debug) System.out.println( xs );
        Assert.assertTrue( !xs.inBounds());
        Assert.assertTrue( xs.outOfBounds());
        // inside overlapping out
        tvec.set(SurfXYPlane.IV, -15.0);
        ETrack trk5= new ETrack(bxyp.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk5 );
        xs = bxyp.status(trk5);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds());
        Assert.assertTrue( xs.outOfBounds());
        // outside overlapping in
        tvec.set(SurfXYPlane.IV, -25.0);
        ETrack trk6= new ETrack(bxyp.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk6 );
        xs = bxyp.status(trk6);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // way outside
        tvec.set(SurfXYPlane.IV, -35.0);
        ETrack trk7= new ETrack(bxyp.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk7 );
        xs = bxyp.status(trk7);
        if(debug) System.out.println( xs );
        Assert.assertTrue( !xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        
        
        // Checking Z component
        
        tvec.set(SurfXYPlane.IV, 0.0);
        
        // completely inside
        tvec.set(SurfXYPlane.IZ, 115.0);
        ETrack trk11= new ETrack(bxyp.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk11 );
        xs = bxyp.status(trk11);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds());
        Assert.assertTrue( !xs.outOfBounds());
        // inside overlapping out
        tvec.set(SurfXYPlane.IZ, 125.0);
        ETrack trk12= new ETrack(bxyp.newPureSurface(),tvec,terr);
        xs = bxyp.status(trk12);
        if(debug) System.out.println( trk12 );
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds());
        // outside overlapping in
        tvec.set(SurfXYPlane.IZ, 135.0);
        ETrack trk13= new ETrack(bxyp.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk13 );
        xs = bxyp.status(trk13);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // way outside
        tvec.set(SurfXYPlane.IZ, 145.0);
        ETrack trk14= new ETrack(bxyp.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk14 );
        xs = bxyp.status(trk14);
        if(debug) System.out.println( xs );
        Assert.assertTrue( !xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // inside overlapping out
        tvec.set(SurfXYPlane.IZ, -115.0);
        ETrack trk15= new ETrack(bxyp.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk15 );
        xs = bxyp.status(trk15);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // outside overlapping in
        tvec.set(SurfXYPlane.IZ, -125.0);
        ETrack trk16= new ETrack(bxyp.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk16 );
        xs = bxyp.status(trk16);
        if(debug) System.out.println( xs );
        Assert.assertTrue( xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        // way outside
        tvec.set(SurfXYPlane.IZ, -135.0);
        ETrack trk17= new ETrack(bxyp.newPureSurface(),tvec,terr);
        if(debug) System.out.println( trk17 );
        xs = bxyp.status(trk17);
        if(debug) System.out.println( xs );
        Assert.assertTrue( !xs.inBounds() );
        Assert.assertTrue( xs.outOfBounds() );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check cloning." );
        Surface psrf = bxyp.newSurface();
        Assert.assertTrue( psrf.type().equals(BSurfXYPlane.staticType()) );
        
        //********************************************************************
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
        
    }
    
}
