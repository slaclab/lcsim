/*
 * LayerSimGeneric_Test.java
 *
 * Created on July 24, 2007, 4:27 PM
 *
 * $Id: LayerSimGeneric_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trflayer;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfbase.HitGenerator;
import org.lcsim.recon.tracking.trfbase.HitGeneratorTest;
import org.lcsim.recon.tracking.trfbase.PropDirectedTest;
import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class LayerSimGeneric_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of LayerSimGeneric_Test */
    public void testLayerSimGeneric()
    {
       String component = "LayerSimGeneric";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check constructor." );
        double spar = 5.0;
        SurfTest srf = new SurfTest(spar);
        HitGenerator hgen = new HitGeneratorTest(srf);
        Layer lyr = new LayerTest(spar);
        Propagator prop = new PropDirectedTest();
        LayerSimGeneric lsim = new LayerSimGeneric(lyr,hgen,prop);
        if(debug) System.out.println( lsim );
        Layer lyr1 = lsim.layer();
        if(debug) System.out.println( lyr );
        Assert.assertTrue(lyr1.equals(lyr));
        Assert.assertTrue( lsim.generators().size() == 1 );
        VTrack trv = new VTrack( srf.newPureSurface() );
        int mcid = 137;
        lsim.addClusters(trv,mcid);
        lsim.dropClusters();
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************        
    }
    
}
