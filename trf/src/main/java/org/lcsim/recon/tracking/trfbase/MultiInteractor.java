package org.lcsim.recon.tracking.trfbase;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;
/** MultiInteractor handles cases where multiple Interactors
 * are to be associated with a layer.
 *
 * @author Norman A. Graf
 * @version 1.0
 */
public class MultiInteractor extends Interactor
{
    
    private List _ints;
    
    //
    
    /**
     *constructor.  The MultiInteractor is constructed with
     * a List of concrete Interactors.
     * ex: the input might contain ThinCylMS and CylEloss Interactors
     *
     * @param   ints  List of Interactors which comprise this MultiInteractor
     */
    public MultiInteractor(List ints)
    {
        _ints = new ArrayList(ints);
        //		Collections.copy(_ints, ints);
    }
    
    //
    
    /**
     *method for adding the interaction
     *
     * @param   tre ETrack to interact
     */
    public void interact(ETrack tre)
    {
        for(Iterator it = _ints.iterator(); it.hasNext(); )
        {
            ((Interactor)it.next()).interact(tre);
        }
    }
    
    //
    
    
    /**
     *return the list of Interactors..
     *
     * @return   List of Interactors
     */
    public List getInteractors()
    {
        return new ArrayList(_ints); //cng shallow copy!
    }
    
    //
    
    /**
     *make a clone of this object
     *
     * @return  new copy of this Interactor
     */
    public Interactor newCopy()
    {
        return new MultiInteractor(_ints); //cng shallow copy!
    }
    
    
    /**
     *output stream
     *
     * @return  String representation of this class
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        StringBuffer sb = new StringBuffer(className+" ");
        for(Iterator it = _ints.iterator(); it.hasNext(); )
        {
            sb.append("\n  "+ (Interactor) it.next());
        }
        return sb.toString();
    }
    
}
