package org.lcsim.recon.tracking.trffit;

/**
 * Abstract base class FullFitter refits a track using all of its hits.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public abstract class FullFitter
{
    
    // static methods
    
    //
    
    /**
     *Return a String representation of the class' the type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' the type name.
     */
    public static String typeName()
    { return "FullFitter"; }
    
    //
    
    /**
     *Return a String representation of the class' the type name.
     *Included for completeness with the C++ version.
     *
     * @return   A String representation of the class' the type name.
     */
    public static String staticType()
    { return typeName(); }
    
    
    
    //
    
    /**
     *Construct a default instance of this class.
     *
     */
    public FullFitter()
    {
    }
    
    
    //
    
    /**
     *Return the generic type.
     * This is only needed at this level.
     *Included for completeness with the C++ version.
     *
     * @return  A String representation of the class' the type name.
     */
    public String genericType()
    { return staticType(); }
    
    //
    
    /**
     *Fit the specified track.
     *
     * @param   trh The HTrack to fit.
     * @return 0 if fit is successful.
     */
    public abstract int fit(HTrack trh);
    
}



