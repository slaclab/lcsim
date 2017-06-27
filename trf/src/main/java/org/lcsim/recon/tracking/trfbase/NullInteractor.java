package org.lcsim.recon.tracking.trfbase;
/** NullInteractor does not modify the track when called.
 *
 * @author Norman A. Graf
 * @version 1.0
 */
public class NullInteractor extends Interactor
{
    
    // Static methods
    
    //
    
    /**
     *Return the type name.
     *
     * @return String representation of this class type
     *Included for completeness with C++ version
     */
    public static String typeName()
    { return "NullInteractor"; }
    
    //
    
    /**
     *Return the type.
     *
     * @return String representation of this class type
     *Included for completeness with C++ version
     */
    public static String staticType()
    { return typeName(); }
    
    // Non-static methods
    
    //
    
    /**
     *Constructor
     *
     */
    
    public NullInteractor()
    {
    }
    
    //
    
    /**
     *Return the generic type.
     * This is only needed at this level.
     *
     * @return String representation of this class type
     *Included for completeness with C++ version
     */
    public String genericType()
    { return staticType(); }
    
    //
    
    /**
     *Return the type.
     *
     * @return String representation of this class type
     *Included for completeness with C++ version
     */
    public String type()
    { return staticType(); }
    
    //
    
    /**
     *Interact: modify the track vector and error matrix.
     *
     * @param   tre ETrack to interact
     */
    public void interact(ETrack tre)
    {
    }
    
    //
    
    /**
     *Make a clone of this object.
     *
     * @return new copy of this Interactor
     */
    public Interactor newCopy()
    {
        return new NullInteractor();
    }
    
    //
    
    /**
     *output stream
     *
     * @return String representation of this class
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        return className;
    }
    
}
