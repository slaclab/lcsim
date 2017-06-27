/*
 * PropDirected_Test.java
 *
 * Created on July 24, 2007, 12:06 PM
 *
 * $Id: PropDirected_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class PropDirected_Test extends TestCase        
{
    private boolean debug;
    /** Creates a new instance of PropDirected_Test */
    public void testPropDirected()
    {
       String ok_prefix = "PropDirected test (I): ";
        String error_prefix = "PropDirected test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "------- Testing component PropDirected. -------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructor."  );
        
        PropDirectedTest tprop = new PropDirectedTest();
        if(debug) System.out.println( tprop );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test backward VTrack propagation."  );
        tprop.direction(PropDir.BACKWARD);
        SurfTest tsrf1 = new SurfTest(1);
        VTrack trv1 = new VTrack( tsrf1.newPureSurface());
        SurfTest tsrf3 = new SurfTest(3);
        VTrack trv2 = new VTrack(trv1);
        VTrack trv3 = new VTrack(trv2);
        if(debug) System.out.println( trv3 );
        tprop.vecProp(trv3,tsrf3);
        if(debug) System.out.println( trv3 );
        Assert.assertTrue( trv3.surface().equals(tsrf3) );
        Assert.assertTrue( trv3.vector().get(1) < trv2.vector().get(1) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test backward ETrack propagation."  );
        tprop.direction(PropDir.BACKWARD);
        SurfTest tsrf2 = new SurfTest(2);
        ETrack tre2 = new ETrack(tsrf2.newPureSurface());
        TrackError err = new TrackError();
        err.set(0,0, 0.01);
        err.set(1,1, 0.02);
        err.set(2,2, 0.03);
        err.set(3,3, 0.04);
        err.set(4,4, 0.05);
        tre2.setError(err);
        ETrack tre3 = new ETrack(tre2);
        if(debug) System.out.println( tre3 );
        tprop.errProp(tre3,tsrf3);
        if(debug) System.out.println( tre3 );
        Assert.assertTrue( tre3.surface().equals(tsrf3) );
        Assert.assertTrue( tre3.vector().get(1) < tre2.vector().get(1) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        

    //********************************************************************        
    }
    
}
