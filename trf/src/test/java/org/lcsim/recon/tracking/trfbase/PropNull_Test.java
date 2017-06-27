/*
 * PropNull_Test.java
 *
 * Created on July 24, 2007, 12:04 PM
 *
 * $Id: PropNull_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class PropNull_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of PropNull_Test */
    public void testPropNull()
    {
        String ok_prefix = "PropNull test (I): ";
        String error_prefix = "PropNull test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "------- Testing component PropNull  -----------" );
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Construct propagator."  );
        PropNull prop = new PropNull();
        if(debug) System.out.println( prop );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Construct surfaces and tracks."  );
        Surface srf1 = new SurfTest(1);
        Surface srf2 = new SurfTest(2);
        Surface bsrf1 = new BSurfTest(1,2);
        VTrack trv1 = new VTrack( new SurfTest(1) );
        VTrack trv2 = new VTrack(srf2);
        ETrack tre1 = new ETrack(srf1);
        ETrack tre2 = new ETrack(srf2);
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Propagate to same surface."  );
        PropStat pstat = new PropStat();
        pstat = prop.vecProp(trv1,srf1);
        if(debug) System.out.println( pstat );
        Assert.assertTrue( pstat.success() );
        pstat = prop.vecDirProp(trv1, srf1,PropDir.FORWARD);
        if(debug) System.out.println( pstat );
        Assert.assertTrue( pstat.success() );
        pstat = prop.errProp(tre1,srf1);
        if(debug) System.out.println( pstat );
        Assert.assertTrue( pstat.success() );
        pstat = prop.errDirProp(tre1,srf1,PropDir.FORWARD);
        if(debug) System.out.println( pstat );
        Assert.assertTrue( pstat.success() );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Propagate to same bounded surface."  );
        pstat = prop.vecProp(trv1,bsrf1);
        if(debug) System.out.println( pstat );
        Assert.assertTrue( pstat.success() );
        pstat = prop.vecDirProp(trv1,bsrf1,PropDir.FORWARD);
        if(debug) System.out.println( pstat );
        Assert.assertTrue( pstat.success() );
        pstat = prop.errProp(tre1,bsrf1);
        if(debug) System.out.println( pstat );
        Assert.assertTrue( pstat.success() );
        pstat = prop.errDirProp(tre1,bsrf1,PropDir.FORWARD);
        if(debug) System.out.println( pstat );
        Assert.assertTrue( pstat.success() );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Propagate to different surface."  );
        pstat = prop.vecProp(trv1,srf2);
        if(debug) System.out.println( pstat );
        Assert.assertTrue( ! pstat.success() );
        pstat = prop.vecDirProp(trv1,srf2,PropDir.FORWARD);
        if(debug) System.out.println( pstat );
        Assert.assertTrue( ! pstat.success() );
        pstat = prop.errProp(tre1,srf2);
        if(debug) System.out.println( pstat );
        Assert.assertTrue( ! pstat.success() );
        pstat = prop.errDirProp(tre1,srf2,PropDir.FORWARD);
        if(debug) System.out.println( pstat );
        Assert.assertTrue( ! pstat.success() );
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************        
    }
    
}
