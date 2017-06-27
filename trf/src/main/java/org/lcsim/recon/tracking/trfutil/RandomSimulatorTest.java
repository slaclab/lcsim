package org.lcsim.recon.tracking.trfutil;
import java.util.*;

public class RandomSimulatorTest extends RandomSimulator
{
    
    // The generators.
    private RandomGeneratorTest _gen1;
    private RandomGeneratorTest _gen2;
    private RandomGeneratorTest _gen3;
    
    
    // constructor
    public RandomSimulatorTest(RandomGeneratorTest gen1, RandomGeneratorTest gen2,
            RandomGeneratorTest gen3)
    {
        _gen1 = new RandomGeneratorTest(gen1);
        _gen2 =  new RandomGeneratorTest(gen2);
        _gen3 = new RandomGeneratorTest(gen3);
    }
    
    // Return the list of generators.
    public List generators()
    {
        List gens = new ArrayList();
        gens.add(  _gen1  );
        gens.add(  _gen2  );
        gens.add(  _gen3  );
        return gens;
    };
    
    // Return a list of random numbers from the generators.
    public List generate_values()
    {
        List values = new ArrayList();
        values.add( new Double(_gen1.flat()) );
        values.add( new Double(_gen2.flat()) );
        values.add( new Double(_gen3.flat()) );
        return values;
    }
    
    public String toString()
    {
        return super.toString()+ " with generators: \n" +_gen1 + " \n"+ _gen2 +" \n"+ _gen3;
    }
    
}

