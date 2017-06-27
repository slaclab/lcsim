package org.lcsim.recon.tracking.trfbase;
import org.lcsim.recon.tracking.trfutil.RandomGenerator;

/** Abtract interface for class to generate a cluster from a VTrack.
 * The track is already at the the appropriate surface.  Its position
 * is smeared to generate the hit.
 *
 * This class is used to generate simulated clusters which are used
 * to create hits.  Note that it inherits from RandomGenerator and
 * the member _rand may be used to generate random numbers (e.g.
 * _rand.gauss(mean)).
 *
 * @author Norman A. Graf
 * @version 1.0
 */

public abstract class HitGenerator extends RandomGenerator
{
    
    
    
    //
    
    /**
     *Constructor.
     *
     */
    public HitGenerator()
    {
        super();
    }
    
    //
    
    /**
     *Constructor from seed.
     *
     * @param   seed  Seed for this generator
     */
    public HitGenerator(long seed)
    {
        super(seed);
    }
    
    //
    
    /**
     *Copy constructor.
     *
     * @param   hg  HitGenerator to replicate
     */
    public HitGenerator( HitGenerator hg)
    {
        super(hg);
    }
    
    //
    
    /**
     *Return the surface to which track should be propagated.
     *
     * @return surface to which track should be propagated
     */
    public abstract Surface surface();
    
    //
    
    /**
     *Generate a new cluster from a track.
     * mcid identifies which particle created it
     * Null is returned if cluster was not created (e.g. if track is
     * not at surface).
     *
     * @param   trv VTrack for which to generate a Cluster
     * @param   mcid MC ID to associate with this Cluster
     * @return Cluster for this VTrack
     */
    public abstract Cluster newCluster( VTrack trv, int mcid );
    
}
