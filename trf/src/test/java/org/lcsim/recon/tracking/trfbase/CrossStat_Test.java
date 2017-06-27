/*
 * CrossStat_t.java
 *
 * Created on July 24, 2007, 11:48 AM
 *
 * $Id: CrossStat_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class CrossStat_Test extends TestCase
{
    private boolean debug;
    //**********************************************************************
    
    // Routine to check crossing status.
    static boolean check_xs(CrossStat xstat, boolean at, boolean on,
            boolean inside, boolean outside, boolean inb, boolean outb)
    {
        boolean bad = false;
        if ( xstat.at() != at ) return bad;
        if ( xstat.on() != on ) return bad;
        if ( xstat.inside() != inside ) return bad;
        if ( xstat.outside() != outside ) return bad;
        if ( xstat.inBounds() != inb ) return bad;
        if ( xstat.outOfBounds() != outb ) return bad;
        return ! bad;
    }    
    /** Creates a new instance of CrossStat_t */
    public void testCrossStat()
    {
        String ok_prefix = "CrossStat test (I): ";
        String error_prefix = "CrossStat test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "------- Testing component CrossStat. -------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Testing pure constructor.\n");
        
        CrossStat xstat1 = new CrossStat(PureStat.AT);
        if(debug) System.out.println( xstat1 );
        Assert.assertTrue( check_xs(xstat1, true, true, false, false, false, false) );
        
        CrossStat xstat2 = new CrossStat(PureStat.ON);
        if(debug) System.out.println( xstat2 );
        Assert.assertTrue( check_xs(xstat2, false, true, false, false, false, false) );
        CrossStat xstat3 = new CrossStat(PureStat.INSIDE);
        if(debug) System.out.println( xstat3 );
        Assert.assertTrue( check_xs(xstat3, false, false, true, false, false, false) );
        CrossStat xstat4 = new CrossStat(PureStat.OUTSIDE);
        if(debug) System.out.println( xstat4 );
        Assert.assertTrue( check_xs(xstat4, false, false, false, true, false, false) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Testing bound constructor.\n");
        CrossStat xstat5 = new CrossStat(BoundedStat.UNDEFINED_BOUNDS);
        if(debug) System.out.println( xstat5 );
        Assert.assertTrue( check_xs(xstat5, true, true, false, false, false, false) );
        CrossStat xstat6 = new CrossStat(BoundedStat.IN_BOUNDS);
        if(debug) System.out.println( xstat6 );
        Assert.assertTrue( check_xs(xstat6, true, true, false, false, true, false) );
        CrossStat xstat7 = new CrossStat(BoundedStat.OUT_OF_BOUNDS);
        if(debug) System.out.println( xstat7 );
        Assert.assertTrue( check_xs(xstat7, true, true, false, false, false, true) );
        CrossStat xstat8 = new CrossStat(BoundedStat.BOTH_BOUNDS);
        if(debug) System.out.println( xstat8 );
        Assert.assertTrue( check_xs(xstat8, true, true, false, false, true, true) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check parameters." );
        Assert.assertTrue( xstat1.nSigma() == 5.0 );
        Assert.assertTrue( xstat2.precision() == 1.e-14 );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check boundsChecked method." );
        Assert.assertTrue( ! xstat1.boundsChecked() );
        Assert.assertTrue( ! xstat2.boundsChecked() );
        Assert.assertTrue( ! xstat3.boundsChecked() );
        Assert.assertTrue( ! xstat4.boundsChecked() );
        Assert.assertTrue( ! xstat5.boundsChecked() );
        Assert.assertTrue( xstat6.boundsChecked() );
        Assert.assertTrue( xstat7.boundsChecked() );
        Assert.assertTrue( xstat8.boundsChecked() );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check assignment." );
        xstat3 = xstat8;
        Assert.assertTrue( check_xs(xstat3, true, true, false, false, true, true) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************  
    }
    
}
