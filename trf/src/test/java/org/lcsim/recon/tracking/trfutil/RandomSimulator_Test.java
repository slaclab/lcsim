/*
 * RandomSimulator_Test.java
 *
 * Created on July 24, 2007, 11:35 AM
 *
 * $Id: RandomSimulator_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfutil;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Norman Graf
 */
public class RandomSimulator_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of RandomSimulator_Test */
    public void testRandomSimulator()
    {
        String component = "RandomSimulator";
        String ok_prefix = component + " (I): ";
        String error_prefix = component + " test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "---------- Testing component " + component
                + ". ----------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Construct generators and simulator." );
        double min = 2.0;
        double max = 5.0;
        long seed1 = 246813579L;
        long seed2 = 987654321L;
        long seed3 = 135724689L;
        RandomGeneratorTest rgen1 = new RandomGeneratorTest(min,max,seed1);
        RandomGeneratorTest rgen2 = new RandomGeneratorTest(min,max,seed2);
        RandomGeneratorTest rgen3 = new RandomGeneratorTest(min,max,seed3);
        if(debug) System.out.println( "Generators:" );
        if(debug) System.out.println( rgen1 );
        if(debug) System.out.println( rgen2 );
        if(debug) System.out.println( rgen3 );
        RandomSimulatorTest rsim = new RandomSimulatorTest(rgen1,rgen2,rgen3);
        if(debug) System.out.println( "Simulator:" );
        if(debug) System.out.println( rsim );
        // save initial values
        List values0 = new ArrayList();
        values0.add( new Double( rgen1.flat()) );
        values0.add( new Double( rgen2.flat()) );
        values0.add( new Double( rgen3.flat()) );
        List values1 = new ArrayList();
        values1.add( new Double( rgen1.flat()) );
        values1.add( new Double( rgen2.flat()) );
        values1.add( new Double( rgen3.flat()) );
        int nval = 3;
        Assert.assertTrue( values0.size() == nval );
        Assert.assertTrue( values1.size() == nval );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Construct registry." );
        RandomRegistry reg = new RandomRegistry();
        rsim.registerGenerators(reg);
        int rec = reg.record();
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check initial generators." );
        List gens = rsim.generators();
        Assert.assertTrue( gens.size() == nval );
        List values = rsim.generate_values();
        int i;
        for ( i=0; i<nval; ++i )
        {
            if(debug) System.out.println( i + " " + values.get(i) + " " + values0.get(i) );
            if(debug) System.out.println( rsim );
            Assert.assertTrue( values.get(i).equals(values0.get(i)) );
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check non-initial states." );
        values = rsim.generate_values();
        for ( i=0; i<nval; ++i )
        {
            if(debug) System.out.println( i + " " + values.get(i) + " " + values0.get(i) + " "
                    + values1.get(i) );
            Assert.assertTrue( !values.get(i).equals(values0.get(i)) );
            Assert.assertTrue( values.get(i).equals(values1.get(i)) );
        }
        rsim.generate_values();
        rsim.generate_values();
        rsim.generate_values();
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Check reset states." );
        reg.set(rec);
        values = rsim.generate_values();
        for ( i=0; i<nval; ++i )
        {
            if(debug) System.out.println( values.get(i) + " " + values0.get(i) );
            Assert.assertTrue( values.get(i).equals(values0.get(i)) );
        }
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );
        
        //********************************************************************
    }
    
}
