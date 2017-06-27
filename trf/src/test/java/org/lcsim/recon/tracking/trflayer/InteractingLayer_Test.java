/*
 * InteractingLayer_Test.java
 *
 * Created on July 24, 2007, 4:30 PM
 *
 * $Id: InteractingLayer_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trflayer;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Interactor;
import org.lcsim.recon.tracking.trfbase.InteractorTest;
import org.lcsim.recon.tracking.trfbase.PropDirectedTest;
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfbase.TrackError;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class InteractingLayer_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of InteractingLayer_Test */
    public void testInteractingLayer()
    {
         String component = "InteractingLayer";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructor." );
        Layer ltest1 = new LayerTest(10.,3);
        Layer ltest2 = new LayerTest(20.);
        Interactor inter = new InteractorTest(1.0);
        InteractingLayer intltest1= new InteractingLayer( ltest1, inter );
        InteractingLayer intltest2= new InteractingLayer( ltest2, inter );
        if(debug) System.out.println( intltest1 );
        if(debug) System.out.println( intltest1.type() );
        if(debug) System.out.println( intltest2.type() );
        Assert.assertTrue( intltest1.type().equals(intltest2.type()) );
        Assert.assertTrue( intltest1.hasClusters() );
        Assert.assertTrue( intltest1.clusters().size() == 0 );
        Assert.assertTrue( intltest1.layer().equals(ltest1) );
        Assert.assertTrue( intltest1.interactor().equals(inter) );
        
        //********************************************************************
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Construct propagator and track." );
        PropDirectedTest prop = new PropDirectedTest();
        if(debug) System.out.println( prop );
        SurfTest stest = new SurfTest(2);
        TrackVector vec = new TrackVector();
        TrackError err = new TrackError();
        err.set(0,0, 1.0);
        ETrack tre = new ETrack(stest.newPureSurface(), vec, err );
        ETrack tre0 = new ETrack(tre);
        if(debug) System.out.println( tre );
        
        //  if(debug) System.out.println( ok_prefix + "Test interaction" );
        //  intltest1.propagate(tre, prop);
        //  if(debug) System.out.println( tre );
        
        
        //********************************************************************
        
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
    
    //********************************************************************       
    }
    
}
