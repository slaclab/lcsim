/*
 * Surface_Test.java
 *
 * Created on July 24, 2007, 11:57 AM
 *
 * $Id: Surface_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class Surface_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of Surface_Test */
    public void testSurface()
    {
               String component = "Surface";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println("-------- Testing component " + component
                + ". --------" );
        
        //********************************************************************
        
        if(debug) System.out.println("Testing constructors." );
        SurfTest tst1 = new SurfTest(123.);
        if(debug) System.out.println( tst1 );
        SurfTest tst2 = new SurfTest(246.);
        if(debug) System.out.println( tst2 );
        BSurfTest btst1 = new BSurfTest(123.,456.);
        if(debug) System.out.println( btst1 );
        BSurfTest btst2 = new BSurfTest(246.,369.);
        if(debug) System.out.println( btst2 );
        
        //********************************************************************
        
        if(debug) System.out.println("Check SurfTest type." );
        if(debug) System.out.println( Surface.staticType());
        if(debug) System.out.println( SurfTest.staticType());
        if(debug) System.out.println( tst1.type());
        Assert.assertTrue( tst1.type().equals(SurfTest.staticType()) );
        if(debug) System.out.println( tst1.pureType());
        Assert.assertTrue( tst1.pureType().equals(SurfTest.staticType()) );
        if(debug) System.out.println( tst1.genericType());
        Assert.assertTrue( tst1.genericType().equals(Surface.staticType()) );
        
        //********************************************************************
        
        if(debug) System.out.println("Check BSurfTest type." );
        if(debug) System.out.println( BSurfTest.staticType());
        if(debug) System.out.println( btst1.type());
        Assert.assertTrue( btst1.type() == BSurfTest.staticType() );
        if(debug) System.out.println( btst1.pureType());
        Assert.assertTrue( btst1.pureType() == SurfTest.staticType() );
        if(debug) System.out.println( btst1.genericType());
        Assert.assertTrue( btst1.genericType() == Surface.staticType() );
        
        //********************************************************************
        
        if(debug) System.out.println("Testing purity." );
        if ( ! tst1.isPure() )
        {
            if(debug) System.out.println(error_prefix +"Pure test class is not pure." );
            System.exit(1);
        }
        if ( btst1.isPure() )
        {
            if(debug) System.out.println(error_prefix +"Bounded test class is pure." );
            System.exit(2);
        }
        
        //********************************************************************
        
        if(debug) System.out.println("Testing equality." );
        Assert.assertTrue( tst1.boundEqual(tst1) );
        Assert.assertTrue( tst1.equals(tst1) );
        Assert.assertTrue( ! tst1.boundEqual(tst2) );
        Assert.assertTrue( !tst1.equals(tst2) );
        Assert.assertTrue( ! tst1.boundEqual(btst1) );
        Assert.assertTrue( ! btst1.boundEqual(tst1) );
        if(debug) System.out.println(btst1);
        if(debug) System.out.println(tst1);
        Assert.assertTrue( tst1.pureEqual(btst1) );
        Assert.assertTrue( btst1.pureEqual(tst1) );
        
        //********************************************************************
        
        if(debug) System.out.println("Testing ordering." );
        Assert.assertTrue( ! tst1.pureLessThan(tst1) );
        Assert.assertTrue( tst1.pureLessThan(tst2) || tst2.pureLessThan(tst1) );
        Assert.assertTrue( ! (tst1.pureLessThan(tst2) && tst2.pureLessThan(tst1)) );
        Assert.assertTrue( ! btst1.pureLessThan(tst1) );
        Assert.assertTrue( ! btst1.pureLessThan(btst1) );
        Assert.assertTrue( btst1.pureLessThan(tst2) || tst2.pureLessThan(btst1) );
        
        //********************************************************************
        
        if(debug) System.out.println("Testing assignment." );
        SurfTest tst3 = tst1;
        if ( tst3 != tst1 )
        {
            if(debug) System.out.println(error_prefix + "Failure: tst3 != tst1." );
            System.exit(7);
        }
        BSurfTest btst3 = btst1;
        if ( btst3 != btst1 )
        {
            if(debug) System.out.println(error_prefix + "Failure: btst3 != btst1." );
            System.exit(8);
        }
        
        //********************************************************************
        
        if(debug) System.out.println("Testing types." );
        if(debug) System.out.println( tst1.type() );
        if(debug) System.out.println( tst2.type() );
        if(debug) System.out.println( btst1.type() );
        if(debug) System.out.println( btst2.type() );
        
        Assert.assertTrue( tst1.type() == tst2.type() );
        
        Assert.assertTrue( btst1.type() == btst2.type() );
        Assert.assertTrue( tst1.type() != btst1.type() );
        
        //********************************************************************
        
        if(debug) System.out.println("Testing pure types." );
        if(debug) System.out.println( tst1.pureType() );
        if(debug) System.out.println( btst1.pureType() );
        Assert.assertTrue( tst1.pureType().equals(tst1.type()) );
        Assert.assertTrue( tst1.pureType().equals(btst1.pureType()) );
        
        //********************************************************************
        
        if(debug) System.out.println("Test curvature." );
        TrackVector vec = new TrackVector();
        vec.set(4,0.0012345);
        if(debug) System.out.println( "expect " + vec.get(4) + "; found " + tst1.qOverP(vec) );
        Assert.assertTrue( tst1.qOverP(vec) == vec.get(4) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
    }
    
}
