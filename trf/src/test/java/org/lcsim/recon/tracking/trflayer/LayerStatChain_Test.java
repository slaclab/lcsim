/*
 * LayerStatChain_Test.java
 *
 * Created on July 24, 2007, 4:25 PM
 *
 * $Id: LayerStatChain_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trflayer;

import junit.framework.TestCase;

/**
 *
 * @author Norman Graf
 */
public class LayerStatChain_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of LayerStatChain_Test */
    public void testLayerStatChain()
    {
        String component = "LayerStat";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------");
        
        //********************************************************************
        
        LayerStatChain lsc = new LayerStatChain();
        if(debug) System.out.println(lsc);
        
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------");
        if(debug) System.out.println("This is too difficult to test here. \n We test with LTrack");
        
        //********************************************************************        
    }
    
}
