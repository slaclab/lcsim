/*
 * TRFMath_Test.java
 *
 * Created on July 24, 2007, 11:34 AM
 *
 * $Id: TRFMath_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfutil;

import junit.framework.TestCase;

/**
 *
 * @author Norman Graf
 */
public class TRFMath_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of TRFMath_Test */
    public void testTRFMath()
    {
        String ok_prefix = "TRFMath (I): ";
        String error_prefix = "TRFMath test (E): ";
        
        if(debug) System.out.println("-------- Testing component TRFMath. --------" );
        
        //********************************************************************
        
        if(debug) System.out.println("Test mathematical constants." );
        if(debug) System.out.println("   pi: " + Math.PI );
        if(debug) System.out.println(" 2*pi: " + TRFMath.TWOPI );
        if(debug) System.out.println(" pi/2: " + TRFMath.PI2 );
        Assert.assertTrue( TRFMath.TWOPI == 2.0*Math.PI );
        Assert.assertTrue( TRFMath.PI2 == 0.5*Math.PI );
        
        //********************************************************************
        
        if(debug) System.out.println("Test physical constants." );
        if(debug) System.out.println("c (cm/sec): " + TRFMath.CLIGHT );
        if(debug) System.out.println("pT/(q*B*Rc): " + TRFMath.BFAC );
        Assert.assertTrue( TRFMath.CLIGHT > 0.0 );
        Assert.assertTrue( TRFMath.BFAC > 0.0 );
        Assert.assertTrue( Math.abs( (TRFMath.CLIGHT - 3.e10) / TRFMath.CLIGHT ) < 0.01 );
        Assert.assertTrue( Math.abs( (TRFMath.BFAC - 0.003) / TRFMath.BFAC ) < 0.01 );
        
        //********************************************************************
        
        if(debug) System.out.println("Test fmod1 and fmod2." );
        double val[] =  { -2.7, -1.7, -0.7, 0.3,  1.3, 2.3, 200.3 };
        double val2[] = { -0.7,  0.3, -0.7, 0.3, -0.7, 0.3,   0.3 };
        double range1 = 1.0;
        double range2 = 2.0;
        double maxdiff = 1.e-10;
        for ( int i=0; i<7; ++i )
        {
            double x1 = TRFMath.fmod1( val[i], range1 );
            double x2 = TRFMath.fmod2( val[i], range2 );
            if(debug) System.out.println(x1 + ' ' + x2 );
            Assert.assertTrue( Math.abs(x1-0.3) < maxdiff );
            Assert.assertTrue( Math.abs(x2-val2[i]) < maxdiff );
            double x1m = TRFMath.fmod1( val[i], -range1 );
            double x2m = TRFMath.fmod2( val[i], -range2 );
            if(debug) System.out.println(x1m + ' ' + x2m );
            Assert.assertTrue( Math.abs(x1m-x1) < maxdiff );
            Assert.assertTrue( Math.abs(x2m-x2) < maxdiff );
            if(debug) System.out.println("----------------" );
        }
        
        //********************************************************************
        
        //********************************************************************
        
        if(debug) System.out.println("Test asinrat." );
        {
            Assert.assertTrue( TRFMath.asinrat(0.0) == 1.0 );
            double[] vals = {1.e-15, 0.00004, 0.003, 1.0, -.137 };
            double close = 1.e-12;
            for ( int i = 0; i<vals.length; ++i)
            {
                double x = vals[i];
                double asx1 = Math.asin(x);
                double asx2 = TRFMath.asinrat(x)*x;
                double dif = Math.abs(asx2-asx1);
                if(debug) System.out.println(x + " " + asx1 + " " + asx2 + " " + dif );
                Assert.assertTrue( dif < close );
            }
            
            //********************************************************************
            
            if(debug) System.out.println("------------- All tests passed. -------------" );
        }
    }
    
}
