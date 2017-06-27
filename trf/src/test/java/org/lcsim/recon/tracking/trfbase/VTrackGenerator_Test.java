/*
 * VTrackGenerator_Test.java
 *
 * Created on July 24, 2007, 11:54 AM
 *
 * $Id: VTrackGenerator_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class VTrackGenerator_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of VTrackGenerator_Test */
    public void testVTrackGenerator()
    {
                String component = "VTrackGenerator";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        
        // Verify that each generated track is different and is in range.
        if(debug) System.out.println( ok_prefix + "Test default sequence." );
        SurfTest stest = new SurfTest(1);
        TrackVector min = new TrackVector();
        TrackVector max = new TrackVector();
        min.set(0, 0.0);
        max.set(0, 1.0);
        min.set(1, 1.0);
        max.set(1, 2.0);
        min.set(2, -3.0);
        max.set(2, -2.0);
        min.set(3, 3.0);
        max.set(3, 4.0);
        min.set(4, -4.0);
        max.set(4, 5.0);
        VTrackGenerator gen = new VTrackGenerator(stest,min,max);
        if(debug) System.out.println( gen );
        Assert.assertTrue( stest.pureEqual( gen.surface() ) );
        int ntest = 20;
        int itest;
        List values = new ArrayList();
        if(debug) System.out.println( "************" );
        for ( itest=0; itest<ntest; ++itest )
        {
            VTrack trk = gen.newTrack();
            if(debug) System.out.println( trk );
            if(debug) System.out.println( "************" );
            for ( Iterator ival=values.iterator(); ival.hasNext(); )
            {
                Assert.assertTrue( !trk.equals( (VTrack) ival.next()) );
            }
            Assert.assertTrue( stest.pureEqual( trk.surface() ) );
            for ( int i=0; i<5; ++i )
            {
                Assert.assertTrue( trk.vector().get(i) >= min.get(i) );
                Assert.assertTrue( trk.vector().get(i) <= max.get(i) );
            }
            values.add(trk);
        }
        
        //********************************************************************
        
        // Verify that two generators starting with the same seed give the
        // same values.
        if(debug) System.out.println( ok_prefix + "Test sequence with seed." );
        long seed = 246813579L;
        gen.setSeed(seed);
        VTrackGenerator gen2 = new VTrackGenerator(stest,min,max);
        gen2.setSeed(seed);
        for ( itest=0; itest<ntest; ++itest )
        {
            VTrack trk = gen.newTrack();
            VTrack trk2 = gen2.newTrack();
            if(debug) System.out.println( trk );
            if(debug) System.out.println( "*****" );
            if(debug) System.out.println( trk2 );
            if(debug) System.out.println( "************" );
            Assert.assertTrue( trk.equals(trk2) );
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test copy constructor." );
        VTrackGenerator gen3 = new VTrackGenerator(gen);
        for ( itest=0; itest<ntest; ++itest )
        {
            VTrack trk = gen.newTrack();
            VTrack trk2 = gen3.newTrack();
            if(debug) System.out.println( trk );
            if(debug) System.out.println( "*****" );
            if(debug) System.out.println( trk2 );
            if(debug) System.out.println( "************" );
            Assert.assertTrue( trk.equals(trk2) );
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
    }
    
}
