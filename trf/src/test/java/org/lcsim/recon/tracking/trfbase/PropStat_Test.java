/*
 * PropStat_Test.java
 *
 * Created on July 24, 2007, 12:03 PM
 *
 * $Id: PropStat_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class PropStat_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of PropStat_Test */
    public void testPropStat()
    {
        String ok_prefix = "PropStat (I): ";
        String error_prefix = "PropStat test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "-------- Testing component PropStat. --------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructor produces failed propagation."
                );
        {
            PropStat pst = new PropStat();
            if(debug) System.out.println( pst );
            Assert.assertTrue( ! pst.success() );
            Assert.assertTrue( ! pst.forward() );
            Assert.assertTrue( ! pst.backward() );
            Assert.assertTrue( ! pst.same() );
        }
                
                //********************************************************************
                
                if(debug) System.out.println( ok_prefix + "Test forward propagation." );
                {
                    PropStat pst = new PropStat();
                    double dist = 123.56;
                    pst.setPathDistance(dist);
                    if(debug) System.out.println( pst );
                    Assert.assertTrue(   pst.success() );
                    Assert.assertTrue(   pst.forward() );
                    Assert.assertTrue( ! pst.backward() );
                    Assert.assertTrue( ! pst.same() );
                    Assert.assertTrue( pst.pathDistance() == dist );
                }
                
                //********************************************************************
                
                if(debug) System.out.println( ok_prefix + "Test backward propagation." );
                {
                    PropStat pst = new PropStat();
                    double dist = -123.56;
                    pst.setPathDistance(dist);
                    if(debug) System.out.println( pst );
                    Assert.assertTrue(   pst.success() );
                    Assert.assertTrue( ! pst.forward() );
                    Assert.assertTrue(   pst.backward() );
                    Assert.assertTrue( ! pst.same() );
                    Assert.assertTrue( pst.pathDistance() == dist );
                }
                
                //********************************************************************
                
                if(debug) System.out.println( ok_prefix + "Test same propagation." );
                {
                    PropStat pst = new PropStat();
                    pst.setSame();
                    double dist = 0.0;
                    pst.setPathDistance(dist);
                    if(debug) System.out.println( pst );
                    Assert.assertTrue(   pst.success() );
                    Assert.assertTrue( ! pst.forward() );
                    Assert.assertTrue( ! pst.backward() );
                    Assert.assertTrue(   pst.same() );
                    Assert.assertTrue( pst.pathDistance() == dist );
                }
                
                //********************************************************************
                
                if(debug) System.out.println( ok_prefix
                        + "------------- All tests passed. -------------" );
                
                
                //********************************************************************        
    }
    
}
