/*
 * Pair_Test.java
 *
 * Created on July 24, 2007, 11:37 AM
 *
 * $Id: Pair_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfutil;

import junit.framework.TestCase;

/**
 *
 * @author Norman Graf
 */
public class Pair_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of Pair_Test */
    public void testPair()
    {
        String component = "Pair";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println("-------- Testing component " + component
                + ". --------" );
        
        //********************************************************************
        
        if(debug) System.out.println("Testing Constructor");
        
        Integer int1 = new Integer(1);
        Integer int2 = new Integer(2);
        
        Pair pair = new Pair(int1, int2);
        
        if(debug) System.out.println("pair= "+pair);
        
        Assert.assertTrue(pair.first().equals(int1));
        Assert.assertTrue(pair.second().equals(int2));
        
        Object obj1 = new Integer(1);
        Object obj2 = new Integer(2);
        Assert.assertTrue(obj1!=int1);
        Assert.assertTrue(obj2!=int2);
        Pair pair2 = new Pair(obj1, obj2);
        Assert.assertTrue(pair.equals(pair2));
        
        Integer int3 = new Integer(3);
        Pair pair3 = new Pair(int1, int3);
        Assert.assertTrue(pair.notEquals(pair3));
        
        //********************************************************************
        
        if(debug) System.out.println( "------------- All tests passed. -------------" );
        
        
        //********************************************************************
    }
    
}
