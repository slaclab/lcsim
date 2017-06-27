package org.lcsim.recon.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;
import org.lcsim.event.EventHeader;
import org.lcsim.event.ReconstructedParticle;
import org.lcsim.event.Track;
import org.lcsim.geometry.Calorimeter;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.Calorimeter.CalorimeterType;
import org.lcsim.spacegeom.CartesianPoint;
import org.lcsim.spacegeom.CartesianVector;
import org.lcsim.spacegeom.SpacePoint;
import org.lcsim.spacegeom.SpaceVector;
import org.lcsim.util.Driver;
import org.lcsim.lcio.LCIOConstants;
import org.lcsim.lcio.LCIOUtil;
import org.lcsim.util.swim.HelixSwimmer;

/**
 * A driver to select ReconstructedParticles from a collection
 * based on their track and cluster times. This is used to
 * estimate the capability of rejecting machine induced
 * backgrounds by timing information, i.e. at CLIC.
 * <p>
 * A large number of selection cuts can be adjusted to control
 * the selection behavior for charged particles, neutral hadrons
 * and photons. PFOs going into the far forward region of the
 * detector are treated separately, due to the expected higher
 * occupancy in that region.
 * <p>
 * This is an adapted version of the Marlin processor
 * <i>CLICPfoSelector</i>, written by Mark Thomson.
 * 
 * @author <a href="mailto:christian.grefe@cern.ch">Christian Grefe</a>
 */
public class PfoSelector extends Driver {

	// collections
	protected String inputPfoCollection = "PandoraPFOCollection";
	protected String outputPfoCollection = "SelectedPFOCollection";
	
	// switches
	protected boolean correctHitTimesForTimeOfFlight = true;
	protected boolean checkProtonCorrection = false;
	protected boolean checkKaonCorrection = false;
	protected boolean keepKShorts = true;
	protected boolean useNeutronTiming = false;
	protected boolean useClusterLessPfos = true;
	protected double pfoEnergyToDisplay = 1.0;
	
	// cut variables
	protected double minimumEnergyForNeutronTiming = 1.0;
	protected double forwardCosThetaForHighEnergyNeutralHadrons = 0.95;
	protected double forwardHighEnergyNeutralHadronsEnergy = 10.0;
	protected double farForwardCosTheta = 0.975;
	protected double ptCutForTightTiming = 0.75;
	protected double photonPtCut = 0.0;
	protected double photonPtCutForLooseTiming = 4.0;
	protected double photonLooseTimingCut = 2.0;
	protected double photonTightTimingCut = 1.0;
	protected double chargedPfoPtCut = 0.0;
	protected double chargedPfoPtCutForLooseTiming = 4.0;
	protected double chargedPfoLooseTimingCut = 3.0;
	protected double chargedPfoTightTimingCut = 1.5;
	protected double chargedPfoNegativeLooseTimingCut = -1.0;
	protected double chargedPfoNegativeTightTimingCut = -0.5;
	protected double neutralHadronPtCut = 0.0;
	protected double neutralHadronPtCutForLooseTiming = 8.0;
	protected double neutralHadronLooseTimingCut = 2.5;
	protected double neutralHadronTightTimingCut = 1.5;
	protected double neutralFarForwardLooseTimingCut = 2.0;
	protected double neutralFarForwardTightTimingCut = 1.0;
	protected double photonFarForwardLooseTimingCut = 2.0;
	protected double photonFarForwardTightTimingCut = 1.0;
	protected double hCalBarrelLooseTimingCut = 20.0;
	protected double hCalBarrelTightTimingCut = 10.0;
	protected double hCalEndCapTimingFactor = 1.0;
	protected double neutralHadronBarrelPtCutForLooseTiming = 3.5;
	protected int minECalHitsForTiming = 5;
	protected int minHCalEndCapHitsForTiming = 5;
	protected double minMomentumForClusterLessPfos = 0.5;
	protected double maxMomentumForClusterLessPfos = 2.0;
	protected double minPtForClusterLessPfos = 0.5;
	protected double clusterLessPfoTrackTimeCut = 10.0;
	
	// internal variables
	protected double ecalRadius;
	protected double ecalZ;
	protected double ecalPhi0;
	protected int ecalNumSides;
	protected HelixSwimmer helixSwimmer;
	protected List<CalorimeterType> emCalorimeters;
	
	

	// -------------------- Constructors --------------------
	
	public PfoSelector() {
		emCalorimeters = new ArrayList<CalorimeterType>();
		emCalorimeters.add(CalorimeterType.EM_BARREL);
		emCalorimeters.add(CalorimeterType.EM_ENDCAP);
		emCalorimeters.add(CalorimeterType.BEAM);
		emCalorimeters.add(CalorimeterType.LUMI);
	}
	
	
	
	// -------------------- Driver Interface Methods --------------------
	
	@Override
	protected void detectorChanged(Detector detector) {
		// Get calorimeter parameters from Detector.
        Calorimeter ecalBarrel = null;
        Calorimeter ecalEndcap = null;

        // Get the EM Barrel.
        ecalBarrel = detector.getCalorimeterByType(CalorimeterType.EM_BARREL);
        if (ecalBarrel == null)
            throw new RuntimeException("Missing EM_BARREL subdetector in compact description.");

        // Get the EM Endcap.
        ecalEndcap = detector.getCalorimeterByType(CalorimeterType.EM_ENDCAP);
        if (ecalEndcap == null)
            throw new RuntimeException("Missing EM_ENDCAP subdetector in compact description.");
        
        ecalRadius = ecalBarrel.getInnerRadius();
        ecalZ = ecalEndcap.getInnerZ();
        ecalPhi0 = ecalBarrel.getSectionPhi();
        ecalNumSides = ecalBarrel.getNumberOfSides();
        
        // Get the magnetic field and initialize the helix swimmer
        SpacePoint iP = new CartesianPoint(0.0, 0.0, 0.0);
        double bField = detector.getFieldMap().getField(iP).z();
        helixSwimmer = new HelixSwimmer(bField);

	}
	
	@Override
	protected void process(EventHeader event) {
		
		// First get the reconstructed particles from the event
		List<ReconstructedParticle> PFOs = new ArrayList<ReconstructedParticle>();
		try {
			PFOs = event.get(ReconstructedParticle.class, inputPfoCollection);
		} catch (IllegalArgumentException e) {
			print(HLEVEL_DEFAULT, e.getMessage(), true);
		}

		// Sort the PFOs by type and then by energy
		Collections.sort(PFOs, new pfoComparator());
		print(HLEVEL_NORMAL, "Number of input PFOs: "+PFOs.size());
		print(HLEVEL_HIGH, "   Type     E     Pt  cosTheta  Tracks time  Clusters time ");
		
		// Store selected PFOs and their energy
		List<ReconstructedParticle> selectedPFOs = new ArrayList<ReconstructedParticle>();
		double eTotalInput = 0.0;
		double eTotalOutput = 0.0;
		
		for (ReconstructedParticle pfo : PFOs) {
			boolean passPfoSelection = true;
			
			int type = pfo.getType();
			SpaceVector momentum = new SpaceVector(pfo.getMomentum());
			double pT = momentum.rxy();
			double p = momentum.rxyz();
			double cosTheta = Math.abs(momentum.cosTheta());
			double energy = pfo.getEnergy();
			eTotalInput += energy;
			List<Cluster> clusters = pfo.getClusters();
			List<Track> tracks = pfo.getTracks();
			
			double trackTime = Double.MAX_VALUE;
			double clusterTime = 999.9;
			double clusterTimeEcal = 999.9;
			double clusterTimeHcalEndcap = 999.9;
			int nEcalHits = 0;
			int nHcalHits = 0;
			int nCaloHits = 0;
			double tProton = 0.;
			double tKaon = 0.;
			
			// Find earliest track in PFO
			for (Track track : tracks) {
				SpaceVector trackMomentum = new CartesianVector(track.getMomentum());
				TravelTime tT = timeAtEcal(track);
				if (Math.abs(tT.minimumTime) < trackTime) {
					trackTime = tT.minimumTime;
					double cProton = Math.sqrt((trackMomentum.magnitudeSquared()+0.94*0.94)/(trackMomentum.magnitudeSquared()+0.14*0.14));
					double cKaon = Math.sqrt((trackMomentum.magnitudeSquared()+0.49*0.49)/(trackMomentum.magnitudeSquared()+0.14*0.14));
					tProton = (trackTime+tT.timeOfFlight)*(cProton-1);
					tKaon = (trackTime+tT.timeOfFlight)*(cKaon-1);
				}
			}
			
			// Find earliest cluster in PFO
			for (Cluster cluster : clusters) {
				ClusterTimes cT = getClusterTimes(cluster);
				if (!tracks.isEmpty()) {
					cT.meanTime -= trackTime;
					cT.meanTimeEcal -= trackTime;
					cT.meanTimeHcalEndcap -= trackTime;
				}
				if (Math.abs(cT.meanTime) < clusterTime) {
					clusterTime = cT.meanTime;
					nCaloHits = cT.nCaloHits;
				}
				if (Math.abs(cT.meanTimeEcal) < clusterTimeEcal) {
					clusterTimeEcal = cT.meanTimeEcal;
					nEcalHits = cT.nEcalHits;
				}
				if (Math.abs(cT.meanTimeHcalEndcap) < clusterTimeHcalEndcap) {
					clusterTimeHcalEndcap = cT.meanTimeHcalEndcap;
					nHcalHits = cT.nHcalHits;
				}
			}
			
			boolean isFarForward = (cosTheta > farForwardCosTheta);
			
			// Fill selection cuts based on particle type
			double ptCut = neutralHadronPtCut;
			double ptCutForLooseTiming = neutralHadronPtCutForLooseTiming;
			double timingCutLow = 0.0;
			double timingCutHigh = neutralHadronLooseTimingCut;
			double hCalBarrelTimingCut = hCalBarrelLooseTimingCut;
			if (isFarForward) timingCutHigh = neutralFarForwardLooseTimingCut;
			
			// Neutral hadron cuts
			if (pT <= ptCutForTightTiming) {
				timingCutHigh = neutralHadronTightTimingCut;
				hCalBarrelTimingCut = hCalBarrelTightTimingCut;
				if (isFarForward) timingCutHigh = neutralFarForwardTightTimingCut;
			}
			
			// Photon cuts
			if (type == 22) {
				ptCut = photonPtCut;
				ptCutForLooseTiming = photonLooseTimingCut;
				if (isFarForward) timingCutHigh = photonFarForwardLooseTimingCut;
				if (pT <= ptCutForTightTiming) {
					timingCutHigh = photonTightTimingCut;
					if (isFarForward) timingCutHigh = photonFarForwardTightTimingCut;
				}
			}
			
			// Charged PFO cuts
			if (!tracks.isEmpty()) {
				ptCut = chargedPfoPtCut;
				ptCutForLooseTiming = chargedPfoPtCutForLooseTiming;
				timingCutLow = chargedPfoNegativeLooseTimingCut;
				timingCutHigh = chargedPfoLooseTimingCut;
				if (pT <= ptCutForTightTiming) {
					timingCutLow = chargedPfoNegativeTightTimingCut;
					timingCutHigh = chargedPfoTightTimingCut;
				}
			}
			
			// Reject low pt PFOs (by default this cut is set to zero)
			if (pT < ptCut) passPfoSelection = false;
			
			// Reject out of time clusterless tracks
			if (clusters.isEmpty() && Math.abs(trackTime) > clusterLessPfoTrackTimeCut) passPfoSelection = false;
			
			// Only apply cuts to low pt PFOs and very forward neutral hadrons
			boolean isForwardNeutron = cosTheta > forwardCosThetaForHighEnergyNeutralHadrons && type == 2112;
			boolean applyTimingCuts =  pT < ptCutForLooseTiming || isForwardNeutron;
			boolean useHcalTimingOnly = energy > forwardHighEnergyNeutralHadronsEnergy && isForwardNeutron;
			
			if (passPfoSelection && applyTimingCuts) {
				boolean selectPfo = false;
				
				// Require any cluster to be "in time" to slected PFO 
				if (!clusters.isEmpty()) {
					if (!useHcalTimingOnly && (nEcalHits > minECalHitsForTiming || nEcalHits >= nCaloHits/2.0)) {
						if (clusterTimeEcal >= timingCutLow && clusterTimeEcal <=timingCutHigh) selectPfo = true;
					} else if (type == 22) {
						if (clusterTime >= timingCutLow &&  clusterTime <= timingCutHigh) selectPfo = true;
					} else if (nHcalHits >= minHCalEndCapHitsForTiming || nHcalHits >= nCaloHits/2.0) {
						if (clusterTimeHcalEndcap >= timingCutLow && clusterTimeHcalEndcap <= hCalEndCapTimingFactor * timingCutHigh) selectPfo = true;
					} else {
						if (clusterTime >= timingCutLow && clusterTime < hCalBarrelTimingCut) selectPfo = true;
						if (tracks.isEmpty() && pT > neutralHadronBarrelPtCutForLooseTiming) selectPfo = true;
					}
					
					// keep KShorts
					if (keepKShorts && type == 310) {
						if (!selectPfo) print(HLEVEL_HIGH, "Recovered K0s: "+energy);
						selectPfo = true;
					}
					
					// check kaon and proton hypothesis
					if (nEcalHits > minECalHitsForTiming) {
						if (checkProtonCorrection && clusterTimeEcal-tProton >= timingCutLow && clusterTimeEcal-tProton <= timingCutHigh) {
							if (!selectPfo) print(HLEVEL_HIGH, "Recovered proton: "+energy);
							selectPfo = true;
						}
						if (checkKaonCorrection && clusterTimeEcal-tKaon >= timingCutLow && clusterTimeEcal-tKaon <= timingCutHigh) {
							if (!selectPfo) print(HLEVEL_HIGH, "Recovered kaon: "+energy);
							selectPfo = true;
						}
					}
				} else {
					if (p > minMomentumForClusterLessPfos && p < maxMomentumForClusterLessPfos && pT > minPtForClusterLessPfos) {
						selectPfo = useClusterLessPfos;
					}
				}
				if (!selectPfo) {
					passPfoSelection = false;
				}
			}
			
			// Print some diagnostic which PFOs are selected and which are rejected
			if (getHistogramLevel() >= HLEVEL_HIGH && energy > pfoEnergyToDisplay) {
				String line = "";
				String format = "%5d %6.2f %6.2f %6.5f %4d %6.1f %4d %6.1f %6.1f %6.1f";
				if (passPfoSelection) line += "Selected PFO: ";
				if (!passPfoSelection) line += "Rejected PFO: ";
				if (clusters.isEmpty()) line += String.format(format, type, energy, pT, cosTheta, tracks.size(), trackTime, 0, 0., 0., 0.);
				if (tracks.isEmpty()) line += String.format(format, type, energy, pT, cosTheta, 0, 0., clusters.size(), clusterTime, clusterTimeEcal, clusterTimeHcalEndcap);
				if (!tracks.isEmpty() && !clusters.isEmpty()) line += String.format(format, type, energy, pT, cosTheta, tracks.size(), trackTime, clusters.size(), clusterTime, clusterTimeEcal, clusterTimeHcalEndcap);
				System.out.println(line);
			}
			
			// Fill the list of selected PFOs
			if (passPfoSelection) {
				eTotalOutput += energy;
				selectedPFOs.add(pfo);
			}
		}
		
		print(HLEVEL_NORMAL, String.format("Total PFO energy in  : %6.2f GeV", eTotalInput));
		print(HLEVEL_NORMAL, String.format("Total PFO energy out : %6.2f GeV", eTotalOutput));
		
		int flags = event.getMetaData(PFOs).getFlags();
		flags = LCIOUtil.bitSet(flags, LCIOConstants.BITSubset, true);
		event.put(outputPfoCollection, selectedPFOs, ReconstructedParticle.class, flags);
		print(HLEVEL_NORMAL, "Added PFO selection \""+outputPfoCollection+"\" with "+selectedPFOs.size()+" PFOs to the event.");
	}
	
	
	
	// -------------------- Setter Methods --------------------

	public void setInputPfoCollection(String inputPfoCollection) {
		this.inputPfoCollection = inputPfoCollection;
	}
	
	public void setOutputPfoCollection(String outputPfoCollection) {
		this.outputPfoCollection = outputPfoCollection;
	}
	
	public void setCorrectHitTimesForTimeOfFlight( boolean correctHitTimesForTimeOfFlight) {
		this.correctHitTimesForTimeOfFlight = correctHitTimesForTimeOfFlight;
	}
	
	public void setCheckKaonCorrection(boolean checkKaonCorrection) {
		this.checkKaonCorrection = checkKaonCorrection;
	}
	
	public void setCheckProtonCorrection(boolean checkProtonCorrection) {
		this.checkProtonCorrection = checkProtonCorrection;
	}
	
	public void setKeepKShorts(boolean keepKShorts) {
		this.keepKShorts = keepKShorts;
	}
	
	public void setUseNeutronTiming(boolean useNeutronTiming) {
		this.useNeutronTiming = useNeutronTiming;
	}
	
	public void setUseClusterLessPfos(boolean useClusterLessPfos) {
		this.useClusterLessPfos = useClusterLessPfos;
	}
	
	public void setPfoEnergyToDisplay(double pfoEnergyToDisplay) {
		this.pfoEnergyToDisplay = pfoEnergyToDisplay;
	}
	
	public void setMinimumEnergyForNeutronTiming(double minimumEnergyForNeutronTiming) {
		this.minimumEnergyForNeutronTiming = minimumEnergyForNeutronTiming;
	}
	
	public void setForwardCosThetaForHighEnergyNeutralHadrons(double forwardCosThetaForHighEnergyNeutralHadrons) {
		this.forwardCosThetaForHighEnergyNeutralHadrons = forwardCosThetaForHighEnergyNeutralHadrons;
	}
	
	public void setForwardHighEnergyNeutralHadronsEnergy(double forwardHighEnergyNeutralHadronsEnergy) {
		this.forwardHighEnergyNeutralHadronsEnergy = forwardHighEnergyNeutralHadronsEnergy;
	}
	
	public void setFarForwardCosTheta(double farForwardCosTheta) {
		this.farForwardCosTheta = farForwardCosTheta;
	}
	
	public void setPtCutForTightTiming(double ptCutForTightTiming) {
		this.ptCutForTightTiming = ptCutForTightTiming;
	}
	
	public void setPhotonPtCut(double photonPtCut) {
		this.photonPtCut = photonPtCut;
	}
	
	public void setPhotonPtCutForLooseTiming(double photonPtCutForLooseTiming) {
		this.photonPtCutForLooseTiming = photonPtCutForLooseTiming;
	}
	
	public void setPhotonLooseTimingCut(double photonLooseTimingCut) {
		this.photonLooseTimingCut = photonLooseTimingCut;
	}
	
	public void setPhotonTightTimingCut(double photonTightTimingCut) {
		this.photonTightTimingCut = photonTightTimingCut;
	}
	
	public void setChargedPfoPtCut(double chargedPfoPtCut) {
		this.chargedPfoPtCut = chargedPfoPtCut;
	}
	
	public void setChargedPfoPtCutForLooseTiming(double chargedPfoPtCutForLooseTiming) {
		this.chargedPfoPtCutForLooseTiming = chargedPfoPtCutForLooseTiming;
	}
	
	public void setChargedPfoLooseTimingCut(double chargedPfoLooseTimingCut) {
		this.chargedPfoLooseTimingCut = chargedPfoLooseTimingCut;
	}
	
	public void setChargedPfoTightTimingCut(double chargedPfoTightTimingCut) {
		this.chargedPfoTightTimingCut = chargedPfoTightTimingCut;
	}
	
	public void setChargedPfoNegativeLooseTimingCut(double chargedPfoNegativeLooseTimingCut) {
		this.chargedPfoNegativeLooseTimingCut = chargedPfoNegativeLooseTimingCut;
	}
	
	public void setChargedPfoNegativeTightTimingCut(double chargedPfoNegativeTightTimingCut) {
		this.chargedPfoNegativeTightTimingCut = chargedPfoNegativeTightTimingCut;
	}
	
	public void setNeutralHadronPtCut(double neutralHadronPtCut) {
		this.neutralHadronPtCut = neutralHadronPtCut;
	}
	
	public void setNeutralHadronPtCutForLooseTiming(double neutralHadronPtCutForLooseTiming) {
		this.neutralHadronPtCutForLooseTiming = neutralHadronPtCutForLooseTiming;
	}
	
	public void setNeutralHadronLooseTimingCut(double neutralHadronLooseTimingCut) {
		this.neutralHadronLooseTimingCut = neutralHadronLooseTimingCut;
	}
	
	public void setNeutralHadronTightTimingCut(double neutralHadronTightTimingCut) {
		this.neutralHadronTightTimingCut = neutralHadronTightTimingCut;
	}
	
	public void setNeutralFarForwardLooseTimingCut(double neutralFarForwardLooseTimingCut) {
		this.neutralFarForwardLooseTimingCut = neutralFarForwardLooseTimingCut;
	}
	
	public void setNeutralFarForwardTightTimingCut(double neutralFarForwardTightTimingCut) {
		this.neutralFarForwardTightTimingCut = neutralFarForwardTightTimingCut;
	}
	
	public void setPhotonFarForwardLooseTimingCut(double photonFarForwardLooseTimingCut) {
		this.photonFarForwardLooseTimingCut = photonFarForwardLooseTimingCut;
	}
	
	public void setPhotonFarForwardTightTimingCut(double photonFarForwardTightTimingCut) {
		this.photonFarForwardTightTimingCut = photonFarForwardTightTimingCut;
	}
	
	public void setHCalBarrelLooseTimingCut(double hCalBarrelLooseTimingCut) {
		this.hCalBarrelLooseTimingCut = hCalBarrelLooseTimingCut;
	}
	
	public void setHCalBarrelTightTimingCut(double hCalBarrelTightTimingCut) {
		this.hCalBarrelTightTimingCut = hCalBarrelTightTimingCut;
	}
	
	public void setHCalEndCapTimingFactor(double hCalEndCapTimingFactor) {
		this.hCalEndCapTimingFactor = hCalEndCapTimingFactor;
	}
	
	public void setNeutralHadronBarrelPtCutForLooseTiming(double neutralHadronBarrelPtCutForLooseTiming) {
		this.neutralHadronBarrelPtCutForLooseTiming = neutralHadronBarrelPtCutForLooseTiming;
	}
	
	public void setMinECalHitsForTiming(int minECalHitsForTiming) {
		this.minECalHitsForTiming = minECalHitsForTiming;
	}
	
	public void setMinHCalEndCapHitsForTiming(int minHCalEndCapHitsForTiming) {
		this.minHCalEndCapHitsForTiming = minHCalEndCapHitsForTiming;
	}
	
	public void setMinMomentumForClusterLessPfos(double minMomentumForClusterLessPfos) {
		this.minMomentumForClusterLessPfos = minMomentumForClusterLessPfos;
	}
	
	public void setMaxMomentumForClusterLessPfos(double maxMomentumForClusterLessPfos) {
		this.maxMomentumForClusterLessPfos = maxMomentumForClusterLessPfos;
	}
	
	public void setMinPtForClusterLessPfos(double minPtForClusterLessPfos) {
		this.minPtForClusterLessPfos = minPtForClusterLessPfos;
	}
	
	public void setClusterLessPfoTrackTimeCut(double clusterLessPfoTrackTimeCut) {
		this.clusterLessPfoTrackTimeCut = clusterLessPfoTrackTimeCut;
	}
	
	
	
	// -------------------- Protected Methods --------------------

	/**
	 * Calculates the time it took the track to reach the face
	 * of the calorimeter. It calculates the time of flight along
	 * the line of sight between the origin and the impact point
	 * on at the calorimeter. It also calculates the minimum time
	 * it took to travel along the helix assuming its mass to be
	 * a charged pion.
	 * @param track The track which travel time should be calculated
	 * @return The minimum time along the helix and the time of flight along the line of sight
	 */
	protected TravelTime timeAtEcal(Track track) {
		helixSwimmer.setTrack(track);
		
		double s = 0.0;
		double sZ = helixSwimmer.getDistanceToZ(ecalZ);
        double sR = helixSwimmer.getDistanceToPolyhedra(ecalRadius, ecalNumSides);
        if (Double.isNaN(sR)) {
        	// helix never reaches barrel, so must hit endcap
            s = sZ;
        } else if (Double.isNaN(sZ)) {
        	// helix never reaches endcap, so must be barrel
            s = sR;
        } else {
        	// helix can reach endcap and barrel, which is closer?
            s = Math.min(sZ, sR);
        }
        
        // time of flight along line of sight from IP to point of impact at the ECal
        SpacePoint ecalPos = helixSwimmer.getPointAtLength(s);
        double tof = ecalPos.magnitude()/300.0;
        
        // time of flight along the helix, assuming particle to be pion
        SpaceVector p = new CartesianVector(track.getMomentum());
        double E = Math.sqrt(p.magnitudeSquared()+0.139*0.139);
        double minTime = s/(300.*p.magnitude())*E-tof;
		
		return new TravelTime(minTime, tof);
	}
	
	/**
	 * Calculates an energy weighted mean time of the cluster
	 * and the number of hits in the cluster. Also calculates
	 * the mean time of the cluster fractions present in the
	 * ECal and the HCal endcap. 
	 * @param cluster The cluster to be analysed
	 * @return The calculated cluster times
	 */
	protected ClusterTimes getClusterTimes(Cluster cluster) {
		
		double sumTimeEnergy = 0.0;
		double sumEnergy = 0.0;
		double sumEnergyEcal = 0.0;
		double sumTimeEnergyEcal = 0.0;
		double sumEnergyHcalEndcap = 0.0;
		double sumTimeEnergyHcalEndcap = 0.0;
		
		ClusterTimes cT = new ClusterTimes(Double.MAX_VALUE, 0, Double.MAX_VALUE, 0, Double.MAX_VALUE, 0);
		
		List<CalorimeterHit> hits = cluster.getCalorimeterHits();
		List<Double> hitTimes = new ArrayList<Double>();
		Map<CalorimeterHit, Double> tofCorrections = new HashMap<CalorimeterHit, Double>();
		List<Double> deltaTimes = new ArrayList<Double>();
		
		// Get the times of all hits (corrected for time of flight along the line of sight if necessary).
		for (CalorimeterHit hit : hits) {
			if (correctHitTimesForTimeOfFlight) {
				double tof = hit.getPositionVec().magnitude()/300.0;
				tofCorrections.put(hit, tof);
				hitTimes.add(hit.getTime() - tof);
			} else {
				hitTimes.add(hit.getTime());
			}
		}
		
		// Sort the hits to calculate the median time of the cluster
		Collections.sort(hitTimes);		
		int iMedian = (int) (hits.size()/2.0);
		double medianTime = hitTimes.get(iMedian);
		
		for (Double hittime : hitTimes) {
			deltaTimes.add(Math.abs(hittime - medianTime));
		}
		
		// Calculate the width of the shower in time (cutting of the tails)
		Collections.sort(deltaTimes);
		int ihit90 = (int)(hits.size() * 9.0/10.0);
		if (ihit90 >= hits.size() - 1) ihit90 = hits.size() - 2;
		if (ihit90 < 0) ihit90 = 0;
		double deltaMedian = deltaTimes.get(ihit90) + 0.1;
		
		// The actual calculation of energy weighted cluster times
		for (CalorimeterHit hit : hits) {
			double hitTime = hit.getTime();
			// Do not forget time of flight correction if needed
			if (correctHitTimesForTimeOfFlight) hitTime -= tofCorrections.get(hit);
			// Only use hits which are around the median time of the shower
			if ((hitTime - medianTime) < deltaMedian ) {
				double hitEnergy = hit.getCorrectedEnergy();
				sumEnergy += hitEnergy;
				sumTimeEnergy += hitEnergy*hitTime;
				cT.nCaloHits++;
				
				Calorimeter calorimeter = (Calorimeter)hit.getSubdetector();
				// Get the ECal part of the shower
				if (emCalorimeters.contains(calorimeter.getCalorimeterType())) {
					cT.nEcalHits++;
					sumEnergyEcal += hitEnergy;
					sumTimeEnergyEcal += hitEnergy*hitTime;
				} else {
					// Not ECal and not barrel -> HCal endcap
					// TODO Should this check for HAD_BARREL type instead?
					if (!calorimeter.isBarrel()) {
						cT.nHcalHits++;
						sumEnergyHcalEndcap += hitEnergy;
						sumTimeEnergyHcalEndcap += hitEnergy*hitTime;
					}
				}
			}
		}
		
		// complete the energy weighting of the mean times
		if (sumEnergy > 0) cT.meanTime = sumTimeEnergy/sumEnergy;
		if (sumEnergyEcal > 0) cT.meanTimeEcal = sumTimeEnergyEcal/sumEnergyEcal;
		if (sumEnergyHcalEndcap > 0) cT.meanTimeHcalEndcap = sumTimeEnergyHcalEndcap/sumEnergyHcalEndcap;
		
		return cT;
	}
	
	
	// -------------------- Static Methods --------------------
	// TODO These can most likely move to a more general class
	
	/**
	 * Helper method to write a message to the output stream if the
	 * histogram level set for the driver is equal or higher than
	 * the given value.
	 * @param histogramLevel The level at which the message is printed
	 * @param message The message, which will be printed to the stream
	 */
	protected void print(int histogramLevel, String message) {
		print(histogramLevel, message, false);
	}
	
	/**
	 * Helper method to write a message to the output stream if the
	 * histogram level set for the driver is equal or higher than
	 * the given value.
	 * @param histogramLevel The level at which the message is printed
	 * @param message The message, which will be printed to the stream
	 * @param error If true, writes to error stream instead of standard
	 */
	protected void print(int histogramLevel, String message, boolean error) {
		if (getHistogramLevel() >= histogramLevel) {
			message = getName()+": "+message;
			if (error) {
				System.err.println(message);
			} else System.out.println(message);
		}
	}
	
	/**
	 * A container to hold the traveling time of a particle.
	 * It contains the time of flight along the line of sight
	 * as well as the mass corrected minimum time along its
	 * helix.
	 */
	private class TravelTime {
		
		protected double minimumTime;
		protected double timeOfFlight;
		
		public TravelTime(double minimumTime, double timeOfFlight) {
			this.minimumTime = minimumTime;
			this.timeOfFlight = timeOfFlight;
		}
	}
	
	/**
	 * A container to hold results from the cluster times calculation.
	 * It contains the total mean time of the cluster and its total
	 * number of hits. It also contains the number of hits in the ECal
	 * and their mean time, as well as the number of hits in the HCal
	 * endcap and their mean time. 
	 */
	private class ClusterTimes {
		
		protected double meanTime;
		protected int nCaloHits;
		protected double meanTimeEcal;
		protected int nEcalHits;
		protected double meanTimeHcalEndcap;
		protected int nHcalHits;
		
		public ClusterTimes(double meanTime, int nCaloHits, double meanTimeEcal, int nEcalHits, double meanTimeHcalEndcap, int nHcalHits) {
			this.meanTime = meanTime;
			this.nCaloHits = nCaloHits;
			this.meanTimeEcal = meanTimeEcal;
			this.nEcalHits = nEcalHits;
			this.meanTimeHcalEndcap = meanTimeHcalEndcap;
			this.nHcalHits = nHcalHits;
		}	
	}
	
	/**
	 * Class to compare ReconstructedParticles first based on
	 * their type and then on their energy.
	 */
	public class pfoComparator implements Comparator<ReconstructedParticle> {
		
		public int compare(ReconstructedParticle lhs, ReconstructedParticle rhs) {
			
			double lhs_energy = lhs.getEnergy();
			double rhs_energy = rhs.getEnergy();
			int lhs_value = getValue(lhs);
			int rhs_value = getValue(rhs);
			
			if (lhs_value == rhs_value) {
				if (lhs_energy > rhs_energy) return 1;
				else if (lhs_energy < rhs_energy) return -1;
				else return 0;
			} else {
				if (lhs_value < rhs_value) return 1;
				else return -1;
			}
		}
		
		private int getValue(ReconstructedParticle pfo) {
			int value = 0;
			int pdgid = Math.abs(pfo.getType());
			if (pfo.getClusters().size() == 0) value = 1;
			if (pdgid == 22) value = 10;
			if (pdgid == 2112) value = 20;
			return value;
		}
	}
}

