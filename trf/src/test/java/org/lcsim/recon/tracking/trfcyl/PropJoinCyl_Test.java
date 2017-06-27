/*
 * PropJoinCyl_Test.java
 *
 * Created on July 24, 2007, 8:29 PM
 *
 * $Id: PropJoinCyl_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfcyl;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.PropDirectedTest;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class PropJoinCyl_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of PropJoinCyl_Test */
    public void testPropJoinCyl()
    {
        String ok_prefix = "PropJoinCyl (I): ";
        String error_prefix = "PropJoinCyl test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component PropJoinCyl. --------" );
        
        if(debug) System.out.println( ok_prefix + "Test constructor." );
        Propagator ptest = new PropDirectedTest();
        PropJoinCyl prop = new PropJoinCyl(0.1,1.0,ptest,ptest);
        if(debug) System.out.println( prop );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Propagate a VTrack." );
        {
            Surface srf1 = new SurfTest(1);
            Surface srf2 =  new SurfTest(2);
            TrackVector vec = new TrackVector();
            VTrack trv = new VTrack(srf1,vec);
            PropStat pstat = prop.vecDirProp(trv,srf2,PropDir.FORWARD);
            if(debug) System.out.println( trv );
            
            Assert.assertTrue( pstat.success() );
            // Two propagations ==> vec(1) = 2.
            Assert.assertTrue( trv.vector().get(1) == 2.0 );
            
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Propagate an ETrack." );
        {
            Surface srf1 = new SurfTest(1);
            Surface srf2 = new SurfTest(2);
            TrackVector vec = new TrackVector();
            vec.set(1, 3.0);
            TrackError err = new TrackError();
            err.set(0,0, 0.01);
            err.set(1,1, 0.02);
            err.set(2,2, 0.03);
            err.set(3,3, 0.04);
            err.set(4,4, 0.05);
            ETrack tre = new ETrack(srf1,vec,err);
            tre.setForward();
            if(debug) System.out.println( "track before propagation\n"+tre );
            PropStat pstat = prop.errDirProp(tre,srf2,PropDir.BACKWARD);
            if(debug) System.out.println( "track after propagation\n"+tre );
            Assert.assertTrue( pstat.success() );
            // Two propagations ==> vec(1) = 3 + 1 - 1 = 3.
            Assert.assertTrue( tre.vector().get(1) == 3.0 );
        }
        
        //********************************************************************
        
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        
        //********************************************************************
                
    }
    
}
