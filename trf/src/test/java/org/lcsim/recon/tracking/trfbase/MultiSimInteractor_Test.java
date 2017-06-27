/*
 * MultiSimInteractor_Test.java
 *
 * Created on July 24, 2007, 12:10 PM
 *
 * $Id: MultiSimInteractor_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class MultiSimInteractor_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of MultiSimInteractor_Test */
    public void testMultiSimInteractor()
    {
         String component = "MultiSimInteractor";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        if(debug) System.out.println("Test default constructor."  );
        MultiSimInteractor mint0 = new MultiSimInteractor();
        Assert.assertTrue(mint0.getSimInteractors().size() == 0);
        
        
        SimInteractor inter1 = new SimInteractorTest(0.1,3.0);
        SimInteractor inter2 = new SimInteractorTest(0.1,5.0);
        
        if(debug) System.out.println("Add SimInteractors..."  );
        mint0.addSimInteractor(inter1);
        Assert.assertTrue(mint0.getSimInteractors().size() == 1);
        mint0.addSimInteractor(inter2);
        if(debug) System.out.println("Fetch SimInteractors..."  );
        List list = mint0.getSimInteractors();
        Assert.assertTrue(list.get(list.size()-1).equals(inter2));
        
        List multi_simint = new ArrayList();
        
        multi_simint.add(inter1);
        multi_simint.add(inter2);
        
        if(debug) System.out.println("Test full constructor."  );
        
        MultiSimInteractor mint = new MultiSimInteractor(multi_simint);
        
        if(debug) System.out.println(mint);
        
        if(debug) System.out.println("Fetch the Siminteractors..."  );
        List ints = mint.getSimInteractors();
        Assert.assertTrue( ints.size()==2);
        Assert.assertTrue( ints.get(0).equals(inter1));
        Assert.assertTrue( ints.get(1).equals(inter2));
        
        //**********************************************************************
        
        if(debug) System.out.println("Interact some tracks..."  );
        // first... refer to tracks passed through MultiSimInteractor
        // second... refer to tracks passed through siminter1 and siminter2
        Surface srf = new SurfTest(5.);
        VTrack vtrk = new VTrack(srf);
        TrackVector trv = new TrackVector();
        trv.set(0,2);
        trv.set(1,3);
        if(debug) System.out.println(  "TrackVector trv before interacting..." + trv );
        vtrk.setVector( trv );
        vtrk.setForward();
        if(debug) System.out.println(  "Track vtrk before interacting..." + vtrk );
        inter1.interact( vtrk );
        if(debug) System.out.println(  "Track vtrk after interacting once..." + vtrk );
        
        TrackVector trvprime = vtrk.vector();
        Assert.assertTrue( trvprime.get(0) == 3.0*trv.get(0) );
        Assert.assertTrue( trvprime.get(1) == 3.0*trv.get(1) );
        inter2.interact( vtrk );
        trvprime = vtrk.vector();
        if(debug) System.out.println(  "Track vtrk after interacting twice..." + vtrk );
        Assert.assertTrue( trvprime.get(0) == 15.0*trv.get(0) );
        Assert.assertTrue( trvprime.get(1) == 15.0*trv.get(1) );
        
        VTrack vtrk2 = new VTrack(srf);
        vtrk2.setVector( trv );
        vtrk2.setForward();
        mint.interact(vtrk2);
        if(debug) System.out.println(  "Track vtrk after multiinteracting..." + vtrk2 );
        
        Assert.assertTrue( vtrk2.equals(vtrk) );
        
        //**********************************************************
        
        if(debug) System.out.println(ok_prefix
                + "------------- All tests passed. -------------" );       
    }
    
}
