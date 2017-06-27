package org.lcsim.recon.tracking.trfutil;
import java.util.*;
/**
 * Object-pair class modelled on C++ STL pair.
 * @author Norman A. Graf
 * @version 1.0
 */
public class Pair
{
    
    private Object _first;
    private Object _second;
    
    /**
     * Constructor.
     *
     * @param   first  First Object to associate.
     * @param   second Second Object to associate.
     */
    public Pair( Object first, Object second )
    {
        _first = first;
        _second = second;
    }
    
    /**
     * Fetch the first object.
     *
     * @return Return by reference since we can't guarantee that clone() does the right thing.
     */
    public Object first()
    {
        return _first;
    }
    
    
    /**
     * Fetch the second object.
     *
     * @return Return by reference since we can't guarantee that clone() does the right thing.
     */
    public Object second()
    {
        return _second;
    }
    
    /**
     * Equality test.
     *
     * @param   o Pair Object to test against.
     * @return  true if Pairs are the same using equals(), <b>not </b> ==.
     */
    public boolean equals( Object o )
    {
        if ( o != null && o.getClass().equals(getClass()) )
        {
            return _first.equals(((Pair) o).first()) && _second.equals(((Pair)o).second());
        }
        return false;
    }
    
    
    /**
     * Inequality test, convenience method.
     *
     * @param   o Pair Object to test against.
     * @return  true if Pairs are the <b>not</b> the same using equals(), <b>not </b> ==.
     */
    public boolean notEquals( Object o )
    {
        return !equals(o);
    }
    
    /**
     * Compute a hash code for the pair.
     *
     * @return integer hash.
     */
    public int hashCode()
    {
        int result = 17;
        result = 37*result + _first.hashCode();
        result = 37*result +_second.hashCode();
        return result;
    }
    
    // output stream
    
    /**
     * String representation of this Pair.
     *
     * @return   String representation of this Pair.
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        return className+" ( "+_first+", "+_second+" )";
    }
}
