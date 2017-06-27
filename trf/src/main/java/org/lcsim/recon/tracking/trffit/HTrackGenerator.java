package org.lcsim.recon.tracking.trffit;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.lcsim.recon.tracking.trfutil.RandomGenerator;

import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.HitGenerator;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.TrackError;

/**
 * Generates an HTrack from a VTrack using its track error matrix
 * and a list of hit generators.  The VTrack is propagated to each
 * of the hit generator surfaces and used to generate a cluster which
 * is in turn used to generate a hit.  This is repeated until a hit is
 * succesfully produced at each surface.  The first hit from each
 * surface is assigned to the HTrack.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */


public class HTrackGenerator extends RandomGenerator
{
    
    // The Hit generators.
    private List _hgens;
    
    // The starting surface.
    private Surface _srf;
    
    // The track error matrix.
    private TrackError _terr;
    
    // Propagator.
    private Propagator _prop;
    
    /**
     *Construct an instance from a list of HitGenerators, a Propagator,
     * a starting Surface and a track error.
     *
     * @param   hgens The list of HitGenerators.
     * @param   prop  The Propagator for propagating the track.
     * @param   srf   The Surface at which to start the track.
     * @param   terr  The starting TrackError.
     */
    public HTrackGenerator(  List hgens,   Propagator prop,
            Surface srf,   TrackError terr)
    {
        _hgens = hgens;
        _terr = new TrackError(terr);
        _prop = prop.newPropagator();
        _srf = srf.newPureSurface();
    }
    
    //
    
    /**
     *Return a new HTrack.
     * Return null for failure.
     *
     * @param   trv The starting VTrack.
     * @return  The HTrack containing hits.
     */
    public HTrack newTrack(  VTrack trv)
    {
        HTrack trh = null;
        VTrack newtrv = new VTrack(trv);
        // Create new list of hits.
        List track_hits = new ArrayList();
        // Loop over hit surfaces.
        
        for ( Iterator ihgen=_hgens.iterator(); ihgen.hasNext(); )
        {
            HitGenerator hgen = (HitGenerator) ihgen.next();
            // Propagate to the surface.
            PropStat pstat = _prop.vecProp(newtrv,hgen.surface());
            if ( ! pstat.success() ) break;
            // Generate a cluster.
            Cluster clu = hgen.newCluster(newtrv,0);
            if ( clu == null ) break;
            // Generate a hit.
            List hits = clu.predict( new ETrack(newtrv,_terr), clu );
            if ( hits.size() == 0 ) break;
            track_hits.add( hits.get(0) );
        }
        // If the number of assigned does not equal the number of hit
        // generators, all hits were not found.  Exit with error.
        if ( track_hits.size() != _hgens.size() ) return trh;
        // Propagate to the surface.
        VTrack starttrv = new VTrack(trv);
        PropStat pstat = _prop.vecProp(starttrv,_srf);
        if ( ! pstat.success() ) return trh;
        ETrack tre = new ETrack(starttrv,_terr);
        trh = new HTrack(tre);
        
        for ( Iterator ihit=track_hits.iterator(); ihit.hasNext(); )
            trh.addHit((Hit) ihit.next());
        return trh;
    }
    
    
    /**
     * Output stream
     *
     * @return String representation of the class
     */
    public String toString()
    {
        return getClass().getName();
    }
    
}