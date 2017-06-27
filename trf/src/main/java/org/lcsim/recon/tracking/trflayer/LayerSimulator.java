package org.lcsim.recon.tracking.trflayer;
import org.lcsim.recon.tracking.trfutil.RandomSimulator;
import org.lcsim.recon.tracking.trfbase.VTrack;

/**
 * A layer simulator generates hits for a layer using a track (VTrack)
 * as input.  It is a simulator (i.e. inherits from class RandomSimulator)
 * so it must return a list of the generators (class RandomGenerator) used
 * to generate the hits.
 *<p>
 * This class is abstract.  A typical implementation would include the
 * layer, hit generators for each active surface in the layer and a
 * propagator.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 **/

public abstract class LayerSimulator extends RandomSimulator
{
    
    // attibutes
    
    // layer
    private Layer _lyr;
    
    // methods
    
    // constructor from the layer
    protected LayerSimulator(Layer lyr)
    {
        _lyr = lyr;
    }
    
    // methods
    
    //
    
    /**
     *Return the layer associated with this simulator.
     *
     * @return Layer for this simulator
     */
    public Layer layer()
    {
        return _lyr;
    }
    
    //
    
    /**
     *Generate clusters from a track and add them to the layer.
     *
     * @param   trv VTrack to simulate interactions
     * @param   mcid MC track ID to asociate with VTrack
     */
    public abstract void addClusters( VTrack trv, int mcid);
    
    //
    
    /**
     *drop the clusters from the layer
     * The default method here invokes the layer drop_clusters method().
     * In unusual cases subclass may want to override.
     *
     */
    public void dropClusters()
    {
        layer().dropClusters();
    }
    
}
