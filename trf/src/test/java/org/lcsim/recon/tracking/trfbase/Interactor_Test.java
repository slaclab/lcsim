/*
 * Interactor_Test.java
 *
 * Created on July 24, 2007, 12:15 PM
 *
 * $Id: Interactor_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class Interactor_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of Interactor_Test */
    public void testInteractor()
    {
        String component = "Interactor";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Construct interactor." );
        double errfac = 3.0;
        InteractorTest inter = new InteractorTest(errfac);
        Assert.assertTrue( inter.get_errfac() == errfac );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Interact track." );
        ETrack tre = new ETrack();
        TrackError cleanError = tre.error();
        TrackError err = new TrackError();
        err.set(0,0, 4.0);
        tre.setError(err);
        Assert.assertTrue( tre.error().get(0,0) == 4.0 );
        inter.interact(tre);
        Assert.assertTrue( tre.error().get(0,0) == errfac*4.0 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************        
    }
    
}
