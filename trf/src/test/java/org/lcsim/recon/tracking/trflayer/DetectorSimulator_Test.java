/*
 * DetectorSimulator_Test.java
 *
 * Created on July 24, 2007, 4:32 PM
 *
 * $Id: DetectorSimulator_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trflayer;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class DetectorSimulator_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of DetectorSimulator_Test */
    public void testDetectorSimulator()
    {
         String component = "DetectorSimulator";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                +"---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix +"Check return status." );
        Assert.assertTrue( DetSimReturnStatus.OK != null);
        Assert.assertTrue( DetSimReturnStatus.UNKNOWN_NAME != DetSimReturnStatus.OK );
        Assert.assertTrue( DetSimReturnStatus.LAYER_MISMATCH != DetSimReturnStatus.OK );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix +"Check constructor." );
        Layer lyr1 = new LayerTest(1.0);
        Layer lyr2 = new LayerTest(2.0);
        Layer lyr3 = new LayerTest(3.0);
        LayerSimulator lsim1 = new LayerSimulatorTest(lyr1) ;
        LayerSimulator lsim2 = new LayerSimulatorTest(lyr2) ;
        LayerSimulator lsim3 = new LayerSimulatorTest(lyr3) ;
        
        Detector det12 = new TwoLayerDetectorTest("LYR1",lyr1,"LYR2",lyr2);
        Detector det3 = new OneLayerDetectorTest("LYR3",lyr3);
        if(debug) System.out.println( det12 );
        if(debug) System.out.println( det3 );
        TwoLayerDetectorSimulatorTest dsim12 = new TwoLayerDetectorSimulatorTest(det12,"LYR1",lsim1,"LYR2",lsim2);
        OneLayerDetectorSimulatorTest dsim3 = new OneLayerDetectorSimulatorTest(det3,"LYR3",lsim3);
        if(debug) System.out.println( dsim12 );
        if(debug) System.out.println( dsim3 );
        Assert.assertTrue( dsim12.generators().size() == 2 );
        Assert.assertTrue( dsim3.generators().size() == 1 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix +"Combine detector simulators." );
        Detector det123 = new TwoSubDetectorTest(det12,det3);
        if(debug) System.out.println( det123 );
        TwoSubDetectorSimulatorTest dsim123 = new TwoSubDetectorSimulatorTest(det123,dsim12,dsim3);
        Assert.assertTrue( dsim123.generators().size() == 3 );
        if(debug) System.out.println( dsim123 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                +"------------- All tests passed. -------------" );
        
        //********************************************************************       
    }
    
}
