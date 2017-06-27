/*
 * RootFindLinear_Test.java
 *
 * Created on July 24, 2007, 11:30 AM
 *
 * $Id: RootFindLinear_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfutil;

import junit.framework.TestCase;

/**
 *
 * @author Norman Graf
 */
public class RootFindLinear_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of RootFindLinear_Test */
    public void  testRootFindLinear()
    {
        String component = "RootFindLinear";
        String ok_prefix = component+ " (I): ";
        String error_prefix = component+ " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "--------------- testing component" + component
                + ". -------------");
        //*******************************************
        if(debug) System.out.println( ok_prefix+" Test constructor");
        
        Assert.assertTrue( RootFindSin.OK == 0 );
        double y0 = 0.345;
        double x0 = Math.asin(y0);
        RootFindSin sfind = new RootFindSin(y0);
        StatusDouble sd = sfind.solve(0.0,0.8);
        if(debug) System.out.println(sd);
        double xf = sd.value();
        double yf = Math.sin(xf);
        if(debug) System.out.println( "Predict:" + x0 + " " + y0);
        if(debug) System.out.println( "  Found:" + xf + " " + yf);
        Assert.assertTrue( sd.status() == RootFindSin.OK );
        double dif = Math.abs(xf - x0);
        Assert.assertTrue( dif < 1.e-10 );
        
        
        //*******************************************
        if(debug) System.out.println( ok_prefix
                + "-------------------- All tests passed. -------------------");
        //*******************************************
    }
    
    static class RootFindSin extends RootFindLinear
    {
        private double _val;
        
        public RootFindSin(double val)
        {
            _val = val;
        }
        
        public StatusDouble evaluate(double x)
        {
            return new StatusDouble(0,Math.sin(x)-_val);
        }
    }    
}


