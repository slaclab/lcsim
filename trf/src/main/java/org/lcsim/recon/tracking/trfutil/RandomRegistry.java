package org.lcsim.recon.tracking.trfutil;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Maintains a list of pointers to RandomGenerator objects.
 * The method record is called to record the state of each
 * and set is called to reset the state of each to previously recorded
 * values.
 *
 * @author Norman A. Graf
 * @version 1.0
 */
public class RandomRegistry
{
    // attributes
    
    // Known generators.
    private List _gens = new ArrayList();
    
    // Array of states for each generator.
    private List _states = new ArrayList();
    
    // methods
    
    /**
     * Hide copy constructor.
     *
     * @param   rr
     */
    private RandomRegistry( RandomRegistry rr)
    {
    }
    
    // methods
    
    /**
     * Constructor.
     *
     */
    public RandomRegistry()
    {
    }
    
    /**
     * Register a generator.
     *
     * @param   gen  RandomGenerator to register.
     */
    public void addGenerator(RandomGenerator gen)
    {
        _gens.add( gen );
    }
    
    /**
     * Record the states.
     *
     * @return  A unique integer labeling the state.
     */
    public int record()
    {
        
        // Create a list of generators to record states.
        int istate = _states.size();
        _states.add( new ArrayList() );
        List gens = (List) _states.get(istate);
        
        // Loop over generators and fill list.
        Iterator igen;
        for ( igen=_gens.iterator(); igen.hasNext();)
            gens.add( new RandomGenerator( (RandomGenerator) igen.next() ) );
        
        return istate;
        
    }
    
    /**
     * Return the number of registered generators.
     *
     * @return   integer number of registered generators.
     */
    public int generatorCount()
    {
        return _gens.size();
    }
    
    /**
     * Return the number of recorded states.
     *
     * @return integer number of recorded states.
     */
    public int stateCount()
    {
        return _states.size();
    }
    
    /**
     * Reset the state.
     *
     * @param   istate integer state to be reset.
     * @return  Return nonzero integer for error (e.g. unknown state index).
     */
    public int set(int istate)
    {
        
        // Check that the state exists.
        if ( istate < 0 ) return 1;
        if ( istate >= stateCount() ) return 2;
        
        // Fetch the appropriate list of generators.
        List oldgens = (List) _states.get(istate);
        
        // Loop over generators and reset state.
        // The recorded list may be shorter than the maintained list.
        for ( int igen=0; igen<oldgens.size(); ++igen )
            ( (RandomGenerator) _gens.get(igen)).setState( (RandomGenerator) oldgens.get(igen) );
        
        return 0;
        
    }
    
    /**
     * String representation of RandomRegistry.
     *
     * @return String representation of RandomRegistry.
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        StringBuffer sb = new StringBuffer(className+" with " +  _gens.size() + " random generators: \n");
        Iterator igen;
        for ( igen=_gens.iterator(); igen.hasNext(); )
            sb.append(igen.next()+"\n");
        sb.append("\n"+stateCount() + " states recorded.");
        return sb.toString();
    }
}




