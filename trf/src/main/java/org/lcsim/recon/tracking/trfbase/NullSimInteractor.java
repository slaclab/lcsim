package org.lcsim.recon.tracking.trfbase;
/** NullSimInteractor does not modify the track when called.
 *
 * @author Norman A. Graf
 * @version 1.0
 */
public class NullSimInteractor extends SimInteractor
{
    
    // Static methods
    
    //
    
    /**
     *Return the type name.
     *
     * @return String representation of class type
     *Included only for completeness with C++ version
     */
    public static String typeName()
    { return "NullSimInteractor"; }
    
    //
    /**
     *Return the type.
     *
     *
     * @return String representation of class type
     *Included only for completeness with C++ version
     */
    public static String staticType()
    { return typeName(); }
    
    // Non-static methods
    
    //
    
    
    /**
     *Constructor
     *
     */
    public NullSimInteractor()
    {}
    
    //
    
    /**
     *Return the generic type.
     *This is only needed at this level.
     *
     * @return String representation of class type
     *Included only for completeness with C++ version
     */
    public String genericType()
    { return staticType(); }
    
    //
    
    /**
     *Return the type.
     *
     * @return String representation of class type
     *Included only for completeness with C++ version
     */
    public String type()
    { return staticType(); }
    
    
    //
    
    /**
     *Interact: modify the track vector
     *
     * @param   vtrk VTrack to interact
     */
    public void interact( VTrack vtrk)
    {
    }
    
    //
    
    /**
     *make a clone
     *
     * @return new copy of this Interactor
     */
    public SimInteractor newCopy()
    {
        return new NullSimInteractor();
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







