/*
 * NullSimInteractor_Test.java
 *
 * Created on July 24, 2007, 12:07 PM
 *
 * $Id: NullSimInteractor_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class NullSimInteractor_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of NullSimInteractor_Test */
    public void testNullSimInteractor()
    {
       String component = "NullSimInteractor";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Create interactor." );
        NullSimInteractor inter = new NullSimInteractor();
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Interact a track." );
        Surface srf = new SurfTest(10);
        VTrack trv = new VTrack(srf);
        VTrack trv0 = new VTrack(trv);
        Assert.assertTrue( trv.equals(trv0) );
        inter.interact(trv);
        Assert.assertTrue( trv.equals(trv0) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Clone." );
        {
            SimInteractor inter2 = inter.newCopy();
            Assert.assertTrue(inter2!=inter);
            Assert.assertTrue( trv.equals(trv0) );
            inter2.interact(trv);
            Assert.assertTrue( trv.equals(trv0) );
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        
        //********************************************************************        
    }
    
}
