/*
 * Detector_Test.java
 *
 * Created on July 24, 2007, 4:31 PM
 *
 * $Id: Detector_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trflayer;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class Detector_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of Detector_Test */
    public void testDetector()
    {
         String component = "Detector";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println(ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test subclass constructor." );
        double x1 = 5.0;
        Layer lyr1 = new LayerTest(x1);
        Detector det1 = new OneLayerDetectorTest("Layer One", lyr1);
        if(debug) System.out.println(det1 );
        Assert.assertTrue( det1.isOk() );
        Assert.assertTrue( det1.layerNames().size() == 1 );
        Assert.assertTrue( det1.layerNames().get(0) == "Layer One" );
        Assert.assertTrue( det1.isAssigned( "Layer One" ) );
        Assert.assertTrue( ! det1.isAssigned( "Layer 1" ) );
        Assert.assertTrue( det1.layer("Layer One").typeName().equals(lyr1.typeName()) );
        
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test composite detector." );
        double x2 = 2.0;
        double x3 = 3.0;
        Layer lyr2  = new LayerTest(x2);
        Layer lyr3  = new LayerTest(x3);
        Detector  det2  = new TwoLayerDetectorTest("layer 2", lyr2, "layer 3", lyr3);
        if(debug) System.out.println(det2 );
        TwoSubDetectorTest det3 = new TwoSubDetectorTest(det1,det2);
        if(debug) System.out.println(det3 );
        Assert.assertTrue( det3.isOk() );
        Assert.assertTrue( det3.layerNames().size() == 3 );
        Assert.assertTrue( det3.layerNames().get(0) == "Layer One" );
        Assert.assertTrue( det3.layerNames().get(det3.layerNames().size()-1) == "layer 3" );
        Assert.assertTrue( det3.isAssigned( "Layer One" ) );
        Assert.assertTrue( det3.isAssigned( "layer 2" ) );
        Assert.assertTrue( det3.isAssigned( "layer 3" ) );
        Assert.assertTrue( ! det1.isAssigned( "Layer 1" ) );
        Assert.assertTrue( det3.layer("Layer One").typeName().equals(lyr1.typeName()) );
        Assert.assertTrue( det3.layer("layer 2").typeName().equals(lyr2.typeName()) );
        
        //********************************************************************
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************       
    }
    
}
