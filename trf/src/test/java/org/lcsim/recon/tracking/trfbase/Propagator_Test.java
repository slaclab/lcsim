/*
 * Propagator_Test.java
 *
 * Created on July 24, 2007, 12:01 PM
 *
 * $Id: Propagator_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class Propagator_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of Propagator_Test */
    public void testPropagator()
    {
        String ok_prefix = "Propagator test (I): ";
        String error_prefix = "Propagator test (E): ";
        
        if(debug) System.out.println("------- Testing component Propagator. -------" );
        
        //********************************************************************
        
        if(debug) System.out.println("Test constructor."  );
        
        TestProp tprop = new TestProp();
        if(debug) System.out.println(tprop );
        
        //********************************************************************
        
        if(debug) System.out.println("Test default VTrack propagation."  );
        Surface ptsrf1= new SurfTest(1) ;
        Surface ptsrf2= new SurfTest(2) ;
        VTrack trv1 = new VTrack(ptsrf1);
        VTrack trv2 = new VTrack(trv1);
        
        Assert.assertTrue( trv2.surface().equals(ptsrf1) );
        Assert.assertTrue(! trv2.surface().equals(ptsrf2) );
        Assert.assertTrue( trv2.vector().equals(trv1.vector()) );
        if(debug) System.out.println(trv2 );
        
        tprop.vecProp(trv2,ptsrf2);
        if(debug) System.out.println(trv2 );
        
        Assert.assertTrue( trv2.surface().equals(ptsrf2) );
        if(debug) System.out.println(trv2.vector().get(1)+", "+ trv1.vector().get(1));
        Assert.assertTrue( trv2.vector().get(1) > trv1.vector().get(1) );
        
        
        //********************************************************************
        
        if(debug) System.out.println("Test backward VTrack propagation."  );
        Surface ptsrf3 = new SurfTest(3);
        VTrack trv3 = new VTrack(trv2);
        if(debug) System.out.println(trv3 );
        
        tprop.vecDirProp(trv3,ptsrf3,PropDir.BACKWARD);
        if(debug) System.out.println(trv3 );
        
        Assert.assertTrue( trv3.surface().equals(ptsrf3) );
        Assert.assertTrue( trv3.vector().get(1) < trv2.vector().get(1) );
        
        
        //********************************************************************
        
        if(debug) System.out.println("Test default ETrack propagation."  );
        ETrack tre1 = new ETrack(ptsrf1);
        TrackError err = new TrackError();
        err.set(0,0,0.01);
        err.set(1,1,0.02);
        err.set(2,2,0.03);
        err.set(3,3,0.04);
        err.set(4,4,0.05);
        tre1.setError(err);
        ETrack tre2 = new ETrack(tre1);
        tre2.setError(err);
        
        Assert.assertTrue( tre2.surface().equals(ptsrf1) );
        Assert.assertTrue( !tre2.surface().equals(ptsrf2) );
        Assert.assertTrue( tre2.vector().equals(tre1.vector()) );
        if(debug) System.out.println(tre2 );
        tprop.errProp(tre2,ptsrf2);
        if(debug) System.out.println(tre2 );
        Assert.assertTrue( tre2.surface().equals(ptsrf2) );
        Assert.assertTrue( tre2.vector().get(1) > tre1.vector().get(1) );
        
        //********************************************************************
        
        if(debug) System.out.println("Test backward ETrack propagation."  );
        ETrack tre3 = new ETrack(tre2);
        if(debug) System.out.println(tre3 );
        tprop.errDirProp(tre3,ptsrf3,PropDir.BACKWARD);
        if(debug) System.out.println(tre3 );
        Assert.assertTrue( tre3.surface().equals(ptsrf3) );
        Assert.assertTrue( tre3.vector().get(1) < tre2.vector().get(1) );
        
        //********************************************************************
        
        if(debug) System.out.println("Test direction reduction." );
        boolean stat;
        PropDir dir = PropDir.FORWARD_MOVE;
        if(debug) System.out.println(dir);
        dir = Propagator.reduce(dir);
        if(debug) System.out.println(dir);
        //  Assert.assertTrue( Propagator.reduce_direction(dir) );
        //  if(debug) System.out.println(dir);
        Assert.assertTrue( dir.equals(PropDir.FORWARD) );
        //  Assert.assertTrue( ! Propagator.reduce_direction(dir) );
        Assert.assertTrue( dir.equals(PropDir.FORWARD) );
        dir = PropDir.BACKWARD_MOVE;
        Assert.assertTrue( Propagator.reduceDirection(dir) );
        dir = Propagator.reduce(dir);
        Assert.assertTrue( dir.equals(PropDir.BACKWARD) );
        Assert.assertTrue( ! Propagator.reduceDirection(dir) );
        Assert.assertTrue( dir.equals(PropDir.BACKWARD) );
        dir = PropDir.NEAREST_MOVE;
        Assert.assertTrue( Propagator.reduceDirection(dir) );
        dir = Propagator.reduce(dir);
        Assert.assertTrue( dir.equals(PropDir.NEAREST) );
        Assert.assertTrue( ! Propagator.reduceDirection(dir) );
        Assert.assertTrue( dir.equals(PropDir.NEAREST) );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix+ "------------- All tests passed. -------------" );
        
        //********************************************************************        
    }
    
}
