/*
 * FullFitter_Test.java
 *
 * Created on July 24, 2007, 4:59 PM
 *
 * $Id: FullFitter_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trffit;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class FullFitter_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of FullFitter_Test */
    public void testFullFitter()
    {
         String component = "FullFitter";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component " + component  );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Define track."  );
        SurfTest srf = new SurfTest(10);
        ETrack tre = new ETrack( srf.newPureSurface() );
        HTrack trh = new HTrack(tre);
        
        //********************************************************************
        
        if(debug) System.out.println(  ok_prefix + "Test constructor." );
        int ival = 123;
        FullFitterTest fit = new FullFitterTest(ival);
        if(debug) System.out.println( fit );
        Assert.assertTrue( fit.fit(trh) == ival );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        
        //********************************************************************       
    }
    
}
