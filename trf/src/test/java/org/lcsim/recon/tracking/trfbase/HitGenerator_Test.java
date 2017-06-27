/*
 * HitGenerator_Test.java
 *
 * Created on July 24, 2007, 2:53 PM
 *
 * $Id: HitGenerator_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;

/**
 *
 * @author Norman Graf
 */
public class HitGenerator_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of HitGenerator_Test */
    public void testHitGenerator()
    {
         String component = "HitGenerator";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test virtual methods." );
        SurfTest stest = new SurfTest(1.0);
        long seed = 1111111111;
        HitGeneratorTest gen = new HitGeneratorTest(stest,seed);
        VTrack trv = new VTrack(stest.newPureSurface() );
        int mcid = 137;
        if(debug) System.out.println( gen.surface() );
        if(debug) System.out.println( gen.newCluster(trv, mcid) );
        if(debug) System.out.println( gen.newCluster(trv, mcid) );
        if(debug) System.out.println( gen.newCluster(trv, mcid) );
        if(debug) System.out.println( gen.newCluster(trv, mcid) );
        if(debug) System.out.println( gen.newCluster(trv, mcid) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************       
    }
    
}
