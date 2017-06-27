/*
 * PropDispatch_Test.java
 *
 * Created on July 24, 2007, 12:05 PM
 *
 * $Id: PropDispatch_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class PropDispatch_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of PropDispatch_Test */
    public void testPropDispatch()
    {
        String ok_prefix = "PropDispatch test (I): ";
        String error_prefix = "PropDispatch test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "------- Testing component PropDispatch. -------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructor."  );
        
        SimplePropTest ptest_prop0 = new SimplePropTest();
        
        if(debug) System.out.println( ptest_prop0 );
        
        PropDispatch tprop = new PropDispatch();
        Surface surf = new SurfTest(3.0);
        String stype = surf.pureType();
        
        int stat = tprop.addPropagator(stype,stype,ptest_prop0);
        Assert.assertTrue( stat == 0 );
        stat = tprop.addPropagator(stype,stype,ptest_prop0);
        Assert.assertTrue( stat != 0 );
        if(debug) System.out.println( "***tprop= \n"+tprop );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test propagation."  );
        Surface tsrf1 = new SurfTest(1.0);
        VTrack trv1 = new VTrack(  tsrf1.newPureSurface()  );
        ETrack tre1 = new ETrack(  tsrf1.newPureSurface()  );
        Surface tsrf2= new SurfTest(2.0);
        
        tprop.vecProp(trv1,tsrf2);
        if(debug) System.out.println(ptest_prop0.get_flag());
        Assert.assertTrue( ptest_prop0.get_flag() == 1 );
        tprop.errProp(tre1,tsrf2);
        Assert.assertTrue( ptest_prop0.get_flag() == 2 );
        tprop.vecDirProp(trv1,tsrf2,PropDir.NEAREST);
        Assert.assertTrue( ptest_prop0.get_flag() == 3 );
        tprop.errDirProp(tre1,tsrf2,PropDir.NEAREST);
        Assert.assertTrue( ptest_prop0.get_flag() == 4 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        
        //********************************************************************        
    }
    
}
