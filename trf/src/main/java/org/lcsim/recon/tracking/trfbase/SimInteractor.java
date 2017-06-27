package org.lcsim.recon.tracking.trfbase;
import org.lcsim.recon.tracking.trfutil.RandomGenerator;
/**An interface base class for interactor Simulators.
 * The type of layer and simulation are supplied in the constructor.
 *
 * @author Norman A. Graf
 * @version 1.0
 */

public abstract class SimInteractor extends RandomGenerator
{
    //
    
    /** method for changing the track vector
     *
     * @param vtrk VTrack to interact
     */
    public abstract void interact( VTrack vtrk);
    
    //
    
    /**
     * Clone
     *
     * @return new SimInteractor
     */
    public abstract SimInteractor newCopy();
    
}