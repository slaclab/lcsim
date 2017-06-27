/*
 * MissFixed_Test.java
 *
 * Created on July 24, 2007, 11:43 AM
 *
 * $Id: MissFixed_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class MissFixed_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of MissFixed_Test */
    public void testMissFixed()
    {
         String component = "MissFixed";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println(ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Construct miss." );
        Surface srf= new SurfTest(3);
        double like = 0.123;
        MissFixed miss = new MissFixed(srf,like);
        Assert.assertTrue( miss.surface().equals(srf) );
        Assert.assertTrue( miss.likelihood() == like );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Update." );
        Surface srf2 =  new SurfTest(2);
        TrackVector vec = new TrackVector();
        TrackError err = new TrackError();
        ETrack tre = new ETrack(srf2,vec,err);
        miss.update(tre);
        Assert.assertTrue( miss.surface().equals(srf) );
        Assert.assertTrue( miss.likelihood() == like );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Clone." );
        Miss pmiss2 = miss.newCopy();
        Assert.assertTrue( pmiss2.surface().equals(miss.surface()) );
        Assert.assertTrue( pmiss2.likelihood() == miss.likelihood() );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************       
    }
    
}
