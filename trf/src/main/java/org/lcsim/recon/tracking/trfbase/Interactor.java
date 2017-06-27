package org.lcsim.recon.tracking.trfbase;
/** An interface base class for interactors, objects that modify
 * an ETrack.  These can account for dE/dx, MS.. etc.
 *
 * @author Norman A. Graf
 * @version 1.0
 */
public abstract class Interactor
{
    
    //
    
    /**
     *Interact: modify the track vector and error matrix.
     *
     * @param   tre ETrack to interact
     */
    public abstract void interact(ETrack tre);
    
    //
    
    /**
     *Make a clone of this object.
     *
     * @return new copy of this Interactor
     */
    public abstract Interactor newCopy();
    
}
