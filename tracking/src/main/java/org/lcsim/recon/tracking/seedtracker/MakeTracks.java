/*
 * MakeTracks.java
 *
 * Created on February 6, 2008, 11:40 AM
 *
 */

package org.lcsim.recon.tracking.seedtracker;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.event.EventHeader;
import org.lcsim.event.Track;
import org.lcsim.event.TrackerHit;
import org.lcsim.fit.helicaltrack.HelicalTrackFit;
import org.lcsim.fit.helicaltrack.HelicalTrackHit;
import org.lcsim.lcio.LCIOConstants;

/**
 * Create a list of SeedTracks from a list of SeedCandidates that have passed
 * the SeedTracker algorithm and store these tracks back into the event.
 * @author Richard Partridge
 * @version 1.0
 */
public class MakeTracks {

    private String _TrkCollectionName = "Tracks";
    TrackCheck _trackCheck; // set by SeedTracker

    /**
     * Creates a new instance of MakeTracks.
     */
    public MakeTracks() {
    }
    
    /**
     * Process a list of SeedCandidates to make a list of SeedTracks and store
     * these tracks back into the event.
     * @param event event header
     * @param seedlist list of SeedCandidates that are to be turned into tracks
     * @param bfield magnetic field (used to turn curvature into momentum)
     */
    public void Process(EventHeader event, List<SeedCandidate> seedlist, double bfield) {
        
        //  Create a the track list
        List<Track> tracks = new ArrayList<Track>();
        
        //  Initialize the reference point to the origin
        double[] ref = new double[] {0., 0., 0.};
        
        //  Loop over the SeedCandidates that have survived
        for (SeedCandidate trackseed : seedlist) {
            
            //  Create a new SeedTrack (SeedTrack extends BaseTrack)
            SeedTrack trk = new SeedTrack();
            
            //  Add the hits to the track
            for (HelicalTrackHit hit : trackseed.getHits()) {
                trk.addHit((TrackerHit) hit);
            }
            
            //  Retrieve the helix and save the relevant bits of helix info
            HelicalTrackFit helix = trackseed.getHelix();
            trk.setTrackParameters(helix.parameters(), bfield); // Sets first TrackState.
            trk.setCovarianceMatrix(helix.covariance()); // Modifies first TrackState.
            trk.setChisq(helix.chisqtot());
            trk.setNDF(helix.ndf()[0]+helix.ndf()[1]);
            
            //  Flag that the fit was successful and set the reference point
            trk.setFitSuccess(true);
            trk.setReferencePoint(ref); // Modifies first TrackState.
            trk.setRefPointIsDCA(true);
            
            //  Set the strategy used to find this track
            trk.setStratetgy(trackseed.getSeedStrategy());
            
            //  Set the SeedCandidate this track is based on
            trk.setSeedCandidate(trackseed);

            // Check the track - hook for plugging in external constraint
            if ((_trackCheck != null) && (! _trackCheck.checkTrack(trk))) continue;

            //  Add the track to the list of tracks
            tracks.add((Track) trk);
        }
        
        // Put the tracks back into the event and exit
        int flag = 1<<LCIOConstants.TRBIT_HITS;
        event.put(_TrkCollectionName, tracks, Track.class, flag);
        
        return;
    }

    public void setTrkCollectionName(String name) {
        _TrkCollectionName = name;
    }
    public void setTrackCheck(TrackCheck trackCheck){
        _trackCheck=trackCheck;
    }
}
