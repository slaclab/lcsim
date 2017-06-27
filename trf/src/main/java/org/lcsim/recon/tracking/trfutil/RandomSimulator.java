package org.lcsim.recon.tracking.trfutil;
import java.util.List;
import java.util.Iterator;

/**
 * This is an abtract class to be used as a base for any class which will
 * hold random generators.  It provides an interface for fetching all the
 * generators and for registering their states.
 *
 * @author Norman A. Graf
 * @version 1.0
 */
public abstract class RandomSimulator
{
    
    /**
     * Return the List of random generators.
     * This <b> must </b> be implemented in subclasses.
     *
     * @return  List of RandomGenerators.
     */
    public abstract List generators();
    /**
     * Register the generators.
     * This <b>should not</b> be overridden in subclasses.
     *
     * @param   reg RandomRegistry to be registered
     */
    public void registerGenerators(RandomRegistry reg)
    {
        
        // Fetch the list of generators.
        List gens = generators();
        
        // Register them.
        Iterator igen;
        for ( igen=gens.iterator(); igen.hasNext(); )
            reg.addGenerator( (RandomGenerator) igen.next() );
        
    }
    
    /**
     * String representation of this class
     * @return short description of this class
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        return className;
    }
    
}



