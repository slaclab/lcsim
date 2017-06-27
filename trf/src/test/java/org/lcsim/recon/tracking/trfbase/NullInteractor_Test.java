/*
 * NullInteractor_Test.java
 *
 * Created on July 24, 2007, 12:08 PM
 *
 * $Id: NullInteractor_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class NullInteractor_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of NullInteractor_Test */
    public void testNullInteractor()
    {
          String component = "NullInteractor";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Create interactor." );
        NullInteractor inter = new NullInteractor();
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Interact a track." );
        Surface srf = new SurfTest(10);
        ETrack tre = new ETrack(srf);
        ETrack tre0 = new ETrack(tre);
        Assert.assertTrue( tre.equals(tre0) );
        inter.interact(tre);
        Assert.assertTrue( tre.equals(tre0) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Clone." );
        {
            Interactor inter2 = inter.newCopy();
            Assert.assertTrue( tre.equals(tre0) );
            inter2.interact(tre);
            Assert.assertTrue( tre.equals(tre0) );
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        
        //********************************************************************      
    }
    
}
