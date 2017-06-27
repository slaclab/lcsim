/*
 * RandomGenerator_Test.java
 *
 * Created on July 24, 2007, 11:21 AM
 *
 * $Id: RandomGenerator_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfutil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Norman Graf
 */
public class RandomGenerator_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of RandomGenerator_Test */
    public void testRandomGenerator()
    {
        String component = "RandomGenerator";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        
        // Verify that each generated value is different and is in range.
        if(debug) System.out.println( ok_prefix + "Test default sequence." );
        double min = 2.0;
        double max = 5.0;
        long seed = 246813579L;
        RandomGeneratorTest rgen = new RandomGeneratorTest(min,max);
        if(debug) System.out.println( rgen );
        int ntest = 20;
        int itest;
        List values = new ArrayList();
        Iterator ival;
        for ( itest=0; itest<ntest; ++itest )
        {
            double newval;
            if ( itest == 4 || itest == 6 || itest == 7 ) newval = rgen.gauss();
            else newval = rgen.flat();
            if(debug) System.out.println( newval );
            for ( ival=values.iterator(); ival.hasNext();)
            {
                Assert.assertTrue( newval != ((Double) ival.next()).doubleValue() );
            }
            if ( itest != 4 && itest != 6 && itest != 7 )
            {
                Assert.assertTrue( newval >= min );
                Assert.assertTrue( newval <= max );
            }
            values.add(new Double(newval));
        }
        
        //********************************************************************
        
        // Verify that two generators starting with the same seed give the
        // same values.
        if(debug) System.out.println( ok_prefix + "Test sequence with seed." );
        rgen.setSeed(seed);
        RandomGeneratorTest rgen2 = new RandomGeneratorTest(min,max,seed);
        for ( itest=0; itest<ntest; ++itest )
        {
            RandomGeneratorTest rgen3 = new RandomGeneratorTest(rgen);
            
            double newval;
            double newval2;
            double newval3;
            if ( itest == 4 || itest == 6 || itest == 7 )
            {
                newval = rgen.gauss();
                newval2 = rgen2.gauss();
                newval3 = rgen3.gauss();
            }
            else
            {
                newval = rgen.flat();
                newval2 = rgen2.flat();
                newval3 = rgen3.flat();
            }
            if(debug) System.out.println( newval + " " + newval2 + " " + newval3 );
            Assert.assertTrue( newval == newval2 );
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test copy constructor." );
        // Note that rgen is not at the beginning of its sequence.
        // Copying the state should copy the seed and invoke the
        // appropriate number of calls so the next value will be
        // the same for both objects.
        RandomGeneratorTest rgen3 = new RandomGeneratorTest(rgen);
        for ( itest=0; itest<ntest; ++itest )
        {
            double newval;
            double newval2;
            if ( itest == 4 || itest == 6 || itest == 7 )
            {
                newval = rgen.gauss();
                newval2 = rgen3.gauss();
            }
            else
            {
                newval = rgen.flat();
                newval2 = rgen3.flat();
            }
            if(debug) System.out.println( newval + " " + newval2 );
            Assert.assertTrue( newval == newval2 );
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test copying state." );
        rgen.setSeed(seed);
        RandomGeneratorTest rgen4 = new RandomGeneratorTest(min,max);
        rgen4.setState(rgen);
        for ( itest=0; itest<ntest; ++itest )
        {
            double newval;
            double newval2;
            if ( itest == 4 || itest == 6 || itest == 7 )
            {
                newval = rgen.gauss();
                newval2 = rgen4.gauss();
            }
            else
            {
                newval = rgen.flat();
                newval2 = rgen4.flat();
            }
            if(debug) System.out.println( newval + ' ' + newval2 );
            Assert.assertTrue( newval == newval2 );
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        //********************************************************************
    }
    
}
