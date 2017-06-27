/*
 * RandomRegistry_Test.java
 *
 * Created on July 24, 2007, 11:25 AM
 *
 * $Id: RandomRegistry_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfutil;

import junit.framework.TestCase;

/**
 *
 * @author Norman Graf
 */
public class RandomRegistry_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of RandomRegistry_Test */
    public void testRandomRegistry()
    {
        String component = "RandomRegistry";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );;
                
                //********************************************************************
                
                if(debug) System.out.println( ok_prefix + "Create generators." );;
                double min = 2.0;
                double max = 5.0;
                long seed = 246813579L;
                RandomGeneratorTest rgen1 = new RandomGeneratorTest(min,max,seed);
                RandomGeneratorTest rgen2 = new RandomGeneratorTest(2*min,2*max,2*seed);
                RandomGeneratorTest rgen3 = new RandomGeneratorTest(3*min,3*max,3*seed);
                if(debug) System.out.println( rgen1 );
                if(debug) System.out.println( rgen2 );
                if(debug) System.out.println( rgen3 );
                
                //********************************************************************
                
                if(debug) System.out.println( ok_prefix + "Create registry." );
                RandomRegistry reg = new RandomRegistry();
                reg.addGenerator(rgen1);
                reg.addGenerator(rgen2);
                reg.addGenerator(rgen3);
                if(debug) System.out.println( reg );
                Assert.assertTrue( reg.generatorCount() == 3 );
                Assert.assertTrue( reg.stateCount() == 0 );
                Assert.assertTrue( reg.record() == 0 );
                Assert.assertTrue( reg.record() == 1 );
                Assert.assertTrue( reg.stateCount() == 2 );
                
                //********************************************************************
                
                if(debug) System.out.println( ok_prefix + "Check set." );
                double save1 = rgen2.flat();
                double save2 = rgen2.flat();
                rgen2.flat();
                rgen2.flat();
                Assert.assertTrue( rgen2.flat() != save1 );
                Assert.assertTrue( reg.record() == 2 );
                reg.set(1);
                Assert.assertTrue( rgen2.flat() == save1 );
                Assert.assertTrue( rgen2.flat() == save2 );
                reg.set(2);
                Assert.assertTrue( rgen2.flat() != save1 );
                
                //********************************************************************
                
                if(debug) System.out.println( ok_prefix + "Check set for gauss." );
                // Note odd number of gauss calls.
                rgen3.gauss();
                Assert.assertTrue( reg.record() == 3 );
                double gsave1 = rgen3.gauss();
                double gsave2 = rgen3.gauss();
                Assert.assertTrue( rgen3.gauss()!=gsave1 );
                reg.set(3);
                Assert.assertTrue(   rgen3.gauss() == gsave1  );
                Assert.assertTrue(   rgen3.gauss() == gsave2  );
                
                //********************************************************************
                
                if(debug) System.out.println( ok_prefix
                        + "------------- All tests passed. -------------" );
                
                //********************************************************************
                
    }
    
}
