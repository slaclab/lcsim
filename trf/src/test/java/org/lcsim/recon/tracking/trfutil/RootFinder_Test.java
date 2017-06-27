/*
 * RootFinder_Test.java
 *
 * Created on July 24, 2007, 11:33 AM
 *
 * $Id: RootFinder_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfutil;

import junit.framework.TestCase;

/**
 *
 * @author Norman Graf
 */
public class RootFinder_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of RootFinder_Test */
    public void testRootFinder()
    {
        String component = "RootFinder";
        String ok_prefix = component+ " (I): ";
        String error_prefix = component+ " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "--------------- testing component" + component
                + ". -------------");
        //*******************************************
        if(debug) System.out.println( ok_prefix+" Test constructor");
        
        RootFinderTest rfind = new RootFinderTest();
        
        StatusDouble eval = rfind.evaluate(3.2);
        StatusDouble soln = rfind.solve(2.34,5.67);
        Assert.assertTrue( eval.status() == 1 );
        Assert.assertTrue( eval.value() == 3.2 );
        Assert.assertTrue( soln.status() == 2 );
        Assert.assertTrue( soln.value() == 2.34*5.67 );
        
        
        //*******************************************
        if(debug) System.out.println( ok_prefix
                + "-------------------- All tests passed. -------------------");
        //*******************************************
    }
    
}
