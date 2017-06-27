/*
 * LayerSimulator_Test.java
 *
 * Created on July 24, 2007, 4:26 PM
 *
 * $Id: LayerSimulator_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trflayer;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class LayerSimulator_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of LayerSimulator_Test */
    public void testLayerSimulator()
    {
         String component = "LayerSimulator";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        
        
        if(debug) System.out.println( ok_prefix + "Check constructor." );
        Layer  lyr = new LayerTest(5.0);
        LayerSimulatorTest lsim = new LayerSimulatorTest(lyr);
        if(debug) System.out.println( lsim );
        Assert.assertTrue( lyr.equals(lsim.layer()) );
        Assert.assertTrue( lsim.generators().size() == 1 );
        VTrack trv = new VTrack( (new SurfTest(5.0)) );
        int mcid = 137;
        lsim.addClusters(trv, 137);
        lsim.dropClusters();
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        
        //********************************************************************       
    }
    
}
