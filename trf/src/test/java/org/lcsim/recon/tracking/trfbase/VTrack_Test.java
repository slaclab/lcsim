/*
 * VTrack_Test.java
 *
 * Created on July 24, 2007, 11:52 AM
 *
 * $Id: VTrack_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class VTrack_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of VTrack_Test */
    public void testVTrack()
    {
         String ok_prefix = "VTrack test (I): ";
        String error_prefix = "VTrack test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "------- Testing component VTrack. -------" );
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructors." );
        VTrack trv0 = new VTrack();
        if(debug) System.out.println( trv0 );
        
        Surface srf1 =  new SurfTest(-123);
        VTrack trv1 = new VTrack(srf1);
        trv1.setForward();
        if(debug) System.out.println( trv1 );
        TrackVector vec = new TrackVector();
        vec.set(0, 1.0);
        vec.set(1, 2.0);
        vec.set(2, 3.0);
        vec.set(3, 4.0);
        vec.set(4, 5.0);
        VTrack trv2 = new VTrack(srf1,vec);
        trv2.setBackward();
        if(debug) System.out.println( trv2 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test copy constructor." );
        VTrack trv2c = new VTrack(trv2);
        if(debug) System.out.println( trv2c );
        VTrack trv3 = new VTrack(trv1);
        VTrack trv4 = new VTrack(trv3);
        
        if(debug) System.out.println( trv1 );
        if(debug) System.out.println( trv4 );
        Assert.assertTrue( trv1.equals(trv4) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test assignment.\n" );
        
        Assert.assertTrue( ! trv0.isValid() );
        trv0 = trv1;
        Assert.assertTrue( trv0.isValid() );
        if(debug) System.out.println( trv0 );
        Assert.assertTrue( trv0 == trv1 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test surface access.\n" );
        Surface srf2 = new SurfTest(246) ;
        trv2 = new VTrack(trv1);
        trv2.setSurface(srf2);
        if(debug) System.out.println( srf2 );
        if(debug) System.out.println( trv2.surface() );
        if(debug) System.out.println( trv2 );
        Assert.assertTrue( srf2.equals(trv2.surface()) );
        Assert.assertTrue( trv1.notEquals(trv2) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test vector access.\n" );
        trv2 = new VTrack(trv1);
        trv2.setVector(vec);
        if(debug) System.out.println( vec );
        if(debug) System.out.println( trv2.vector() );
        if(debug) System.out.println( trv2 );
        Assert.assertTrue( vec.equals(trv2.vector()) );
        Assert.assertTrue( trv1.notEquals(trv2) );
        for ( int i=0; i<5; ++i ) Assert.assertTrue( trv2.vector().get(i) == vec.get(i) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test direction.\n" );
        Assert.assertTrue( trv1.isForward() );
        Assert.assertTrue( ! trv1.isBackward() );
        trv1.setBackward();
        Assert.assertTrue( ! trv1.isForward() );
        Assert.assertTrue( trv1.isBackward() );
        trv1.setForward();
        Assert.assertTrue( trv1.isForward() );
        Assert.assertTrue( ! trv1.isBackward() );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );       
    }
    
}
