package org.lcsim.recon.tracking.trfbase;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
/**This class allows multiple SimInteractors to be implemented
 *
 * @author Norman A. Graf
 * @version 1.0
 */

public class MultiSimInteractor extends SimInteractor
{
    
    private List _simints;
    
    //
    
    /**
     *default Constructor
     *
     */
    public MultiSimInteractor()
    {
        _simints = new ArrayList();
    }
    
    //
    
    /** Full constructor.
     * The MultiSimInteractor is constructed with
     * a List of concrete SimInteractors.
     * ex: the input might contain ThinCylMsSim and CylElossSim Interactors
     *
     * @param simints List of SimInteractors
     */
    public MultiSimInteractor(List simints)
    {
        _simints = new ArrayList(simints); //cng shallow copy
    }
    
    // .
    
    /** add a SimInteractor.
     *
     * @param simint SimInteractor to add
     */
    public void addSimInteractor(SimInteractor simint)
    {
        _simints.add(simint);
    }
    
    //
    
    /** interacting the VTrack
     *
     * @param vtrk VTrack to interact
     */
    public void interact(VTrack vtrk)
    {
        for(Iterator it = _simints.iterator(); it.hasNext(); )
        {
            ((SimInteractor)it.next()).interact(vtrk);
        }
    }
    
    // .
    
    
    /**
     *return the list of Interactors.
     *
     * @return a List of SimInteractors
     */
    public List getSimInteractors()
    {
        return new ArrayList(_simints); //cng shallow copy!
    }
    
    //
    
    /**
     *make a clone of this object
     *
     * @return new copy of this SimInteractor
     */
    public SimInteractor newCopy()
    {
        return new MultiSimInteractor(_simints); //cng shallow copy!
    }
    
    
    /**
     * String representation of the MultiSimInteractor
     *
     * @return String representation of the MultiSimInteractor
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        StringBuffer sb = new StringBuffer(className+" ");
        for(Iterator it = _simints.iterator(); it.hasNext(); )
        {
            sb.append("\n  "+ (SimInteractor) it.next());
        }
        return sb.toString();
    }
    
}
