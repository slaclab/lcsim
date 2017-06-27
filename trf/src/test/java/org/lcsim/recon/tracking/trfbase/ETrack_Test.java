/*
 * ETrack_Test.java
 *
 * Created on July 24, 2007, 2:54 PM
 *
 * $Id: ETrack_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class ETrack_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of ETrack_Test */
    public void testETrack()
    {
         String ok_prefix = "ETrack test (I): ";
        String error_prefix = "ETrack test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "------- Testing component TrkVec. -------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructors." );
        ETrack tre0 = new ETrack();
        if(debug) System.out.println( tre0 );
        Surface srf1 = new SurfTest(123);
        ETrack tre1 = new ETrack(srf1);
        tre1.setForward();
        if(debug) System.out.println( tre1 );
        
        TrackVector vec = new TrackVector();
        vec.set(0, 1.0);
        vec.set(1, 2.0);
        vec.set(2, 3.0);
        vec.set(3, 4.0);
        vec.set(4, 5.0);
        TrackError err = new TrackError();
        err.set(0,0, 0.1);
        err.set(0,1, -0.021);
        err.set(1,1, 0.2);
        err.set(2,0, 0.031);
        err.set(2,1, 0.032);
        err.set(2,2, 0.3);
        err.set(0,3, -0.041);
        err.set(1,3, -0.042);
        err.set(2,3, -0.043);
        err.set(3,3, 0.4);
        err.set(4,0, 0.051);
        err.set(4,1, 0.052);
        err.set(4,2, 0.053);
        err.set(4,3, 0.054);
        err.set(4,4, 0.5);
        ETrack tre2 = new ETrack(srf1,vec,err,TrackSurfaceDirection.TSD_BACKWARD);
        if(debug) System.out.println( tre2 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test equality and inequality.\n" );
        Assert.assertTrue( tre2.notEquals(tre1) );
        Assert.assertTrue( tre1.equals(tre1) );
        Assert.assertTrue( tre2 .equals(tre2) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test copy constructor.\n" );
        ETrack tre3 = new ETrack(tre2);
        if(debug) System.out.println( tre2 );
        if(debug) System.out.println( tre3 );
        Assert.assertTrue( tre2.equals(tre3) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test VTrack constructor.\n" );
        VTrack trv5 = new VTrack(srf1,vec);
        ETrack tre5 = new ETrack(trv5,err);
        if(debug) System.out.println( tre5 );
        Assert.assertTrue( tre5.equals(tre2) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test assignment.\n" );
        tre1 = new ETrack(tre2);
        if(debug) System.out.println( tre2 );
        if(debug) System.out.println( tre1 );
        Assert.assertTrue( tre2 != tre1 );
        tre0 = tre1;
        if(debug) System.out.println( tre0 );
        Assert.assertTrue( tre0 == tre1 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test error access.\n" );
        TrackError err2 = tre1.error();
        if(debug) System.out.println( "err="+err );
        Assert.assertTrue( err2.equals(err) );
        int i, j;
        for (i=0; i<5; ++i) err2.set(i,i, 2.0*err2.get(i,i));
        ETrack tre4 = new ETrack(srf1,vec,err2);
        if(debug) System.out.println( "err2="+err2 );
        
        tre2 = new ETrack(tre1);
        if(debug) System.out.println( "tre2= "+tre2 );
        tre2.setError(err2);
        if(debug) System.out.println( "tre2= "+tre2 );
        if(debug) System.out.println( "tre1= "+tre1 );
        Assert.assertTrue( tre2.notEquals(tre1) );
        Assert.assertTrue( tre2.equals(tre4) );
        for (i=0; i<5; ++i)
            for (j=0; j<5; ++j)
                Assert.assertTrue( tre2.error(i,j) == err2.get(i,j) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test chi-square difference." );
        VTrack trv_no_err = new VTrack( tre1.surface(), tre1.vector() );
        tre2 = new ETrack(tre1);
        TrackVector vec2 = tre2.vector();
        for (j=0; j<5; ++j) vec2.set(j, vec2.get(j)+Math.sqrt(0.1*vec2.get(j)));
        tre2.setVector(vec2);
        if(debug) System.out.println( tre1 );
        if(debug) System.out.println( tre2 );
        
        double diff0 = ETrack.chisqDiff( tre1, tre1 );
        double diff1 = ETrack.chisqDiff( tre2, tre1 );
        double diff2 = ETrack.chisqDiff( tre2, trv_no_err );
        double diff3 = ETrack.chisqDiff( trv_no_err, tre2 );
        if(debug) System.out.println( "Both with error:  " + diff1 );
        if(debug) System.out.println( "With and without: " + diff2 );
        if(debug) System.out.println( "Without and with: " + diff3 );
        
        double diff00 = Math.abs(diff0);
        double diff12 = Math.abs(2.0*diff1-diff2);
        double diff23 = Math.abs(diff2-diff3);
        double eps = 1.e-10;
        Assert.assertTrue( diff00 < eps );
        Assert.assertTrue( diff12 < eps );
        Assert.assertTrue( diff23 < eps );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );       
    }
    
}
