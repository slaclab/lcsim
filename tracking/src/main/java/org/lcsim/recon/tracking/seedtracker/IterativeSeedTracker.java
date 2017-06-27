package org.lcsim.recon.tracking.seedtracker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;
import org.lcsim.fit.helicaltrack.HelicalTrackHit;

/**
 * This is a re-implementation of <code>SeedTracker</code> allowing consecutive track reconstruction with different startegy lists. 
 * Hits of successfully reconstructed tracks using one strategy list will be removed from the available hits before running the next strategy list.
 * @author cgrefe
 */
public class IterativeSeedTracker extends SeedTracker {
	
	protected List<List<SeedStrategy>> _strategyLists;
	
	public IterativeSeedTracker() {
		_strategyLists = new ArrayList<List<SeedStrategy>>();
		
        //  Instantiate the material manager
        // _materialmanager = new MaterialManager();

        //  Instantiate the hit manager
        _hitmanager = new HitManager();

        //  Initialize the detector sectoring
        _hitmanager.getSectorManager().setSectorParams(_strategylist, _bfield, _rtrk);

        //  Instantiate the helix finder
        _helixfitter = new HelixFitter(_materialmanager);

        //  Instantiate the Seed Finder
        _finder = new SeedTrackFinder(_hitmanager, _helixfitter);

        //  Instantiate the Track Maker
        _maketracks = new MakeTracks();
	}
	
	//--------------------------------------------------------------------------------
	// Override driver methods
	//--------------------------------------------------------------------------------
	
	@Override
	protected void startOfData() {
		
		// check if any strategy list has been added
		if (_strategyLists.size() == 0) {
			throw new RuntimeException("IterativeSeedTracker: no strategy file given!");
		}
		
		// get one of the strategies. This is just a workaround to not break the inherited methods. It does not affect actual reconstruction
		_strategylist = _strategyLists.get(0);
		
		super.startOfData();
	}
	
	@Override
	protected void process(EventHeader event) {
		
        //  Pass the event to the diagnostics package
        if (_diag != null) _diag.setEvent(event);

        //  Initialize timing
        long last_time = System.currentTimeMillis();
        long start_time = last_time;
        double dtime = 0.;

        //  Get the hit collection from the event
        List<HelicalTrackHit> hitcol = event.get(HelicalTrackHit.class, _inputCol);
        
        //  Prepare lists of seeded and confirmed MC Particles
        List<MCParticle> seededmcp = new ArrayList<MCParticle>();
        List<MCParticle> confirmedmcp = new ArrayList<MCParticle>();
        
        //  Prepare list of track seeds
        List<SeedCandidate> trackseeds = new ArrayList<SeedCandidate>();
        
        for (List<SeedStrategy> strategyList : _strategyLists) {
        	//  Sort the hits for this event
        	_hitmanager.setSectorParams(strategyList, _bfield, _rtrk);
            _hitmanager.OrganizeHits(hitcol);

            String listPrefix = "Strategy list "+ _strategyLists.indexOf(strategyList);
            
            //  Make the timing plots if requested
            start_time = System.currentTimeMillis();
            dtime = ((double) (start_time - last_time)) / 1000.;
            last_time = start_time;
            if (_timing) aida.cloud1D(listPrefix+"/Organize Hits").fill(dtime);

            //  Make sure that we have cleared the list of track seeds in the finder
            _finder.clearTrackSeedList();

            //  Loop over strategies and perform track finding
            for (SeedStrategy strategy : strategyList) {

                //  Set the strategy for the diagnostics
                if (_diag != null) _diag.fireStrategyChanged(strategy);

                //  Perform track finding under this strategy
                _finder.FindTracks(strategy, _bfield);

                //  Make the timing plots if requested
                long time = System.currentTimeMillis();
                dtime = ((double) (time - last_time)) / 1000.;
                last_time = time;
                if (_timing) aida.cloud1D(listPrefix+"/Tracking time for strategy "+strategy.getName()).fill(dtime);
            }

            //  Get the list of final list of SeedCandidates
            List<SeedCandidate> newTrackSeeds = _finder.getTrackSeeds();
            trackseeds.addAll(newTrackSeeds);
            
            //  Get the list of seeded MC Particles
            seededmcp.addAll(_finder.getSeededMCParticles());
            
            //  Get the list of confirmed MC Particles
            confirmedmcp.addAll(_finder.getConfirmedMCParticles());
            
            //  Find all the used hits and remove from the hit collection
            for (SeedCandidate trackCandidate : newTrackSeeds) {
            	for (HelicalTrackHit hit : trackCandidate.getHits()) {
            		hitcol.remove(hit);
            	}
            }
            
            //  Make plot of number of found tracks per strategy list if requested
            if (_timing) aida.cloud1D(listPrefix+"/Found Tracks").fill(newTrackSeeds.size());
            
            //  Clear the list of track seeds accumulated in the track finder
            _finder.clearTrackSeedList();
        }

        //  Make tracks from the final list of track seeds
        _maketracks.Process(event, trackseeds, _bfield);

        //  Save the MC Particles that have been seeded
        event.put("SeededMCParticles", seededmcp, MCParticle.class, 0);

        //  Save the MC Particles that have been confirmed
        event.put("ConfirmedMCParticles", confirmedmcp, MCParticle.class, 0);

        //  Make the total time plot if requested
        long end_time = System.currentTimeMillis();
        dtime = ((double) (end_time - start_time)) / 1000.;
        if (_timing) aida.cloud1D("Total tracking time").fill(dtime);

        return;
        
	}

	
	//--------------------------------------------------------------------------------
	// Adding strategy lists
	//--------------------------------------------------------------------------------
	
	public void setStrategyFile(String fileName) {
		setStrategyFile(new File(fileName));
	}
	
	public void setStrategyFile(File file) {
		_strategyLists.add(StrategyXMLUtils.getStrategyListFromFile(file));
	}
	
	public void setStrategyResource(String resource) {
		_strategyLists.add(StrategyXMLUtils.getStrategyListFromResource(resource));
	}

}
