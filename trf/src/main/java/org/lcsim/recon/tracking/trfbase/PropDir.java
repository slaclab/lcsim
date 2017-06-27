package org.lcsim.recon.tracking.trfbase;
/** An Enumeration class for Propagation Directions
 *
 *@author Norman A. Graf
 *@version 1.0
 */
public class PropDir
{
    private String _propdir;
    // Direction for a propagator to go.
    // XXX_MOVE means to go to the next crossing if the original and
    // destinataion surfaces are the same.
    private PropDir( String propdir)
    {
        _propdir = propdir;
    }
    
    /**
     * @return String representation of this class
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        return className+_propdir;
    }
    public final static PropDir NEAREST = new PropDir("NEAREST");
    public final static PropDir FORWARD = new PropDir("FORWARD");
    public final static PropDir BACKWARD = new PropDir("BACKWARD");
    public final static PropDir NEAREST_MOVE = new PropDir("NEAREST_MOVE");
    public final static PropDir FORWARD_MOVE= new PropDir("FORWARD_MOVE");
    public final static PropDir BACKWARD_MOVE = new PropDir("BACKWARD_MOVE");
    
}