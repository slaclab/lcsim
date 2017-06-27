import org.lcsim.util.hitmap.*;
import org.lcsim.util.*;
import org.lcsim.event.*;
import org.lcsim.event.util.*;
import java.util.*;

import org.lcsim.recon.cluster.util.CalHitMapDriver;
import org.lcsim.digisim.DigiSimDriver;
import org.lcsim.digisim.CalorimeterHitsDriver;
import org.lcsim.util.hitmap.HitListToHitMapDriver;
import org.lcsim.util.hitmap.HitMapToHitListDriver;
import org.lcsim.event.util.CreateFinalStateMCParticleList;
import org.lcsim.recon.cluster.cheat.PerfectClusterer;
import org.lcsim.recon.pfa.cheat.PerfectIdentifier;
import org.lcsim.recon.pfa.output.EnergySumPlotter;
import org.lcsim.recon.pfa.output.CorrectedEnergySumPlotter;

/**
 * A very simple cheating PFA, to serve as a worked example.
 *
 * @version $Id: TrivialPFA.java,v 1.1 2008/10/30 23:38:19 jeremy Exp $
 */
public class TrivialPFA extends Driver
{
    /**
     *  Constructor. This sets up all of the daughter drivers.
     */
  public TrivialPFA()
  {
    // Set up and run DigiSim, based on org.lcsim.plugin.web.examples.DigiSimExample:
    // CalHitMapDriver is needed by DigiSim
    add(new org.lcsim.recon.cluster.util.CalHitMapDriver());
    // DigiSim: SimCalHits -> RawCalHits
    org.lcsim.digisim.DigiSimDriver digi = new org.lcsim.digisim.DigiSimDriver();
    add(digi);
    // RawCalHits -> SimCalorimeterHits
    add( new org.lcsim.digisim.SimCalorimeterHitsDriver() );

    // Set up a hitmap for the raw hits
    HitListToHitMapDriver rawHitMap = new HitListToHitMapDriver();
    rawHitMap.addInputList("EcalBarrHits");
    rawHitMap.addInputList("EcalEndcapHits");
    rawHitMap.addInputList("HcalBarrHits");
    rawHitMap.addInputList("HcalEndcapHits");
    rawHitMap.setOutput("raw hitmap");
    add(rawHitMap);

    // Set up a hitmap for the digisim output hits
    HitListToHitMapDriver digiHitMap = new HitListToHitMapDriver();
    digiHitMap.addInputList("EcalBarrDigiHits");
    digiHitMap.addInputList("EcalEndcapDigiHits");
    digiHitMap.addInputList("HcalBarrDigiHits");
    digiHitMap.addInputList("HcalEndcapDigiHits");
    digiHitMap.setOutput("digi hitmap");
    add(digiHitMap);
    
    // Do hit map -> hit list conversion
    HitMapToHitListDriver rawConverterDriver = new HitMapToHitListDriver();
    rawConverterDriver.setInputHitMap("raw hitmap");
    rawConverterDriver.setOutputList("raw hits (displayable)");
    add(rawConverterDriver);
    HitMapToHitListDriver digiConverterDriver = new HitMapToHitListDriver();
    digiConverterDriver.setInputHitMap("digi hitmap");
    digiConverterDriver.setOutputList("digi hits (displayable)");
    add(digiConverterDriver);

    // Set up the MC list
    CreateFinalStateMCParticleList mcListMakerGen = new CreateFinalStateMCParticleList("Gen");
    CreateFinalStateMCParticleList mcListMakerSim = new CreateFinalStateMCParticleList("Sim");
    add(mcListMakerGen);
    add(mcListMakerSim);
    String mcListGen = "GenFinalStateParticles";
    String mcListSim = "SimFinalStateParticles";
    String mcList = mcListGen; // Can choose the Gen or Sim list here

    // Cluster the hits (perfect pattern recognition)
    PerfectClusterer clusterer = new PerfectClusterer();
    clusterer.setInputHitMap("digi hitmap");
    clusterer.setOutputHitMap("leftover hits");
    clusterer.setOutputClusterList("perfect clusters");
    clusterer.setMCParticleList(mcList);
    add(clusterer);

    // Find tracks
    // Output: List<Track> saved as EventHeader.TRACKS
    add (new org.lcsim.mc.fast.tracking.MCFastTracking());

    // ID the clusters and create reconstructed particles
    PerfectIdentifier id = new PerfectIdentifier();
    id.setInputClusterList("perfect clusters");
    id.setOutputParticleList("perfect particles");
    id.setMCParticleList(mcList);
    id.setInputTrackList(EventHeader.TRACKS);
    add(id);

    // Plot the total energy (using only particles with
    // clusters in the calorimeter)
    add(new EnergySumPlotter("perfect particles", "perfect.aida"));

    // Plot the total energy (correcting for particles which didn't
    // leave any hits in the calorimeter)
    add(new CorrectedEnergySumPlotter("digi hitmap", "perfect particles", mcList, "corrected.aida"));
  }
}
