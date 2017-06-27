/*
 * Miss_Test.java
 *
 * Created on July 24, 2007, 12:12 PM
 *
 * $Id: Miss_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class Miss_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of Miss_Test */
    public void testMiss()
    {
         String component = "Miss";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println(ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test constructor." );
        MissTest miss = new MissTest(1,0.5);
        if(debug) System.out.println(miss );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test cloning." );
        Miss miss2 = miss.newCopy();
        if(debug) System.out.println(miss2 );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test access." );
        SurfTest srf = new SurfTest(1);
        Assert.assertTrue( miss2.surface().pureEqual(srf) );
        Assert.assertTrue( miss2.likelihood() == 0.5 );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix + "Test update." );
        ETrack tre = new ETrack( srf.newPureSurface() );
        miss2.update(tre);
        Assert.assertTrue( miss.likelihood() == 0.5 );
        Assert.assertTrue( miss2.likelihood() == 0.5*0.9 );
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************       
    }
    
}
