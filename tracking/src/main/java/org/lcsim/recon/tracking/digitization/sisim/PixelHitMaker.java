/*
 * StripHitMaker.java
 *
 * Created on September 26, 2007, 1:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.lcsim.recon.tracking.digitization.sisim;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IReadout;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.tracker.silicon.ChargeCarrier;
import org.lcsim.detector.tracker.silicon.DopedSilicon;
import org.lcsim.detector.tracker.silicon.SiPixels;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.detector.tracker.silicon.SiSensorElectrodes;
import org.lcsim.detector.tracker.silicon.SiTrackerIdentifierHelper;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.event.SimTrackerHit;

/**
 *
 * @author tknelson
 * 
 * @version $Id:
 */
public class PixelHitMaker implements Clusterer {

    private static String _NAME = "PixelClusterer";
    // Clustering algorithm
    ClusteringAlgorithm _clustering;

    // Absolute maximum cluster size
    int _max_cluster_npixels = 10;
    // Readout chip needed to decode hit information
    ReadoutChip _readout_chip;
    // Sensor simulation needed to correct for Lorentz drift
    SiSensorSim _simulation;
    // Identifier helper (reset once per sensor)
    SiTrackerIdentifierHelper _sid_helper;
    // Temporary map connecting hits to pixel numbers for sake of speed (reset once per sensor)
    Map<RawTrackerHit, Integer> _pixel_map = new HashMap<RawTrackerHit, Integer>();
    double _oneClusterErr = 1 / Math.sqrt(12);
    double _twoClusterErr = 1 / 5;
    double _threeClusterErr = 1 / 3;
    double _fourClusterErr = 1 / 2;
    double _fiveClusterErr = 1;

    /**
     * Create a new instance of PixelHitMaker that uses the default clustering algorithm
     *
     * @param simulation charge collection simulation
     * @param readout_chip readout chip
     */
    public PixelHitMaker(SiSensorSim simulation, ReadoutChip readout_chip) {
        this(simulation, readout_chip, new NearestNeighbor());
    }

    /**
     * Fully qualified constructor for PixelHitMaker
     * 
     * @param simulation charge collection simulation
     * @param readout_chip readout chip
     * @param clustering clustering algorithm
     */
    public PixelHitMaker(SiSensorSim simulation, ReadoutChip readout_chip, ClusteringAlgorithm clustering) {
        _simulation = simulation;
        _readout_chip = readout_chip;
        _clustering = clustering;
    }

    public void setClusteringAlgorithm(ClusteringAlgorithm clustering_algorithm) {
        _clustering = clustering_algorithm;
    }

    public void setMaxClusterSize(int max_cluster_npixels) {
        _max_cluster_npixels = max_cluster_npixels;
    }

    public String getName() {
        return _NAME;
    }

    public void SetOneClusterErr(double err) {
        _oneClusterErr = err;
    }

    public void SetTwoClusterErr(double err) {
        _twoClusterErr = err;
    }

    public void SetThreeClusterErr(double err) {
        _threeClusterErr = err;
    }

    public void SetFourClusterErr(double err) {
        _fourClusterErr = err;
    }

    public void SetFiveClusterErr(double err) {
        _fiveClusterErr = err;
    }

    // Make hits for all sensors within a DetectorElement
    public List<SiTrackerHit> makeHits(IDetectorElement detector) {
        System.out.println("makeHits(IDetectorElement): " + detector.getName());
        List<SiTrackerHit> hits = new ArrayList<SiTrackerHit>();
        List<SiSensor> sensors = detector.findDescendants(SiSensor.class);

        // Loop over all sensors
        for (SiSensor sensor : sensors) {
            if (sensor.hasPixels()) {
                hits.addAll(makeHits(sensor));
            }
        }

        // Return hit list
        return hits;
    }

    // Make hits for a sensor
    public List<SiTrackerHit> makeHits(SiSensor sensor) {

        //System.out.println("makeHits: " + sensor.getName());

        List<SiTrackerHit> hits = new ArrayList<SiTrackerHit>();

        // Get SiTrackerIdentifierHelper for this sensor and refresh the pixel map used to increase speed
        _sid_helper = (SiTrackerIdentifierHelper) sensor.getIdentifierHelper();
        _pixel_map.clear();

        // Get hits for this sensor
        IReadout ro = sensor.getReadout();
        List<RawTrackerHit> raw_hits = ro.getHits(RawTrackerHit.class);

        Map<SiSensorElectrodes, List<RawTrackerHit>> electrode_hits = new HashMap<SiSensorElectrodes, List<RawTrackerHit>>();

        for (RawTrackerHit raw_hit : raw_hits) {

            // get id and create pixel map, get electrodes.
            IIdentifier id = raw_hit.getIdentifier();
            int pixel_number = _sid_helper.getElectrodeValue(id);

            //  Add this hit to the pixel maps
            _pixel_map.put(raw_hit, pixel_number);

            // Get electrodes and check that they are pixels
            //System.out.println("proc raw hit from: " + DetectorElementStore.getInstance().find(raw_hit.getIdentifier()).get(0).getName());
            ChargeCarrier carrier = ChargeCarrier.getCarrier(_sid_helper.getSideValue(id));
            SiSensorElectrodes electrodes = ((SiSensor) raw_hit.getDetectorElement()).getReadoutElectrodes(carrier);
            if (!(electrodes instanceof SiPixels)) {
                continue;
            }

            if (electrode_hits.get(electrodes) == null) {
                electrode_hits.put(electrodes, new ArrayList<RawTrackerHit>());
            }

            electrode_hits.get(electrodes).add(raw_hit);
        }

        for (Entry entry : electrode_hits.entrySet()) {
            hits.addAll(makeHits(sensor, (SiPixels) entry.getKey(), (List<RawTrackerHit>) entry.getValue()));
        }

        return hits;
    }

    // Private methods
    //=========================
    // Make hits for an electrode
    public List<SiTrackerHit> makeHits(SiSensor sensor, SiSensorElectrodes electrodes, List<RawTrackerHit> raw_hits) {

        //  Call the clustering algorithm to make clusters
        List<List<RawTrackerHit>> cluster_list = _clustering.findClusters(electrodes, _readout_chip, raw_hits);

        //  Create an empty list for the pixel hits to be formed from clusters
        List<SiTrackerHit> hits = new ArrayList<SiTrackerHit>();

        //  Make a pixel hit from this cluster
        for (List<RawTrackerHit> cluster : cluster_list) {

            // Make a TrackerHit from the cluster if it meets max cluster size requirement
            if (cluster.size() <= _max_cluster_npixels) {
                SiTrackerHitPixel hit = makeTrackerHit(cluster, electrodes);

                // Add to readout and to list of hits
                ((SiSensor) electrodes.getDetectorElement()).getReadout().addHit(hit);
                hits.add(hit);
            }
        }

        return hits;
    }

    //Make the hit
    private SiTrackerHitPixel makeTrackerHit(List<RawTrackerHit> cluster, SiSensorElectrodes electrodes) {
        Hep3Vector position = getPosition(cluster, electrodes);
        SymmetricMatrix covariance = getCovariance(cluster, electrodes);
        double time = getTime(cluster);
        double energy = getEnergy(cluster);
        TrackerHitType type = new TrackerHitType(TrackerHitType.CoordinateSystem.GLOBAL, TrackerHitType.MeasurementType.PIXEL);

        SiTrackerHitPixel hit = new SiTrackerHitPixel(position, covariance, energy, time, cluster, type);
        return hit;
    }

    private List<SimTrackerHit> getSimulatedHits(List<RawTrackerHit> cluster) {
        Set<SimTrackerHit> simulated_hits = new HashSet<SimTrackerHit>();
        for (RawTrackerHit hit : cluster) {
            simulated_hits.addAll(hit.getSimTrackerHits());
        }
        return new ArrayList<SimTrackerHit>(simulated_hits);
    }

    private Hep3Vector getPosition(List<RawTrackerHit> cluster, SiSensorElectrodes electrodes) {
        List<Double> signals = new ArrayList<Double>();
        List<Hep3Vector> positions = new ArrayList<Hep3Vector>();

        for (RawTrackerHit hit : cluster) {
            signals.add(_readout_chip.decodeCharge(hit));
            positions.add(electrodes.getCellPosition(_pixel_map.get(hit)));
        }

        double total_charge = 0;
        Hep3Vector position = new BasicHep3Vector(0, 0, 0);

        for (int ipixel = 0; ipixel < signals.size(); ipixel++) {
            double signal = signals.get(ipixel);

            total_charge += signal;
            position = VecOp.add(position, VecOp.mult(signal, positions.get(ipixel)));
        }
        position = VecOp.mult(1 / total_charge, position);

        // Put position in sensor coordinates
        electrodes.getParentToLocal().inverse().transform(position);

//        System.out.println("Position \n"+position);

        // Swim position back through lorentz drift direction to midpoint between bias surfaces
        _simulation.setSensor((SiSensor) electrodes.getDetectorElement());
        _simulation.lorentzCorrect(position, electrodes.getChargeCarrier());

//        System.out.println("Lorentz corrected position \n"+position);

        // return position in global coordinates
        return ((SiSensor) electrodes.getDetectorElement()).getGeometry().getLocalToGlobal().transformed(position);
//        return electrodes.getLocalToGlobal().transformed(position);
    }

    private double getTime(List<RawTrackerHit> cluster) {
        int time_sum = 0;
        int signal_sum = 0;

        for (RawTrackerHit hit : cluster) {

            int pixel_number = _pixel_map.get(hit);
            double signal = _readout_chip.decodeCharge(hit);
            double time = _readout_chip.decodeTime(hit);

            time_sum += time * signal;
            signal_sum += signal;

        }
        return (double) time_sum / (double) signal_sum;
    }

    private SymmetricMatrix getCovariance(List<RawTrackerHit> cluster, SiSensorElectrodes electrodes) {

        SymmetricMatrix covariance = new SymmetricMatrix(3);
        covariance.setElement(0, 0, Math.pow(getXResolution(cluster, electrodes), 2));
        covariance.setElement(1, 1, Math.pow(getYResolution(cluster, electrodes), 2));
        covariance.setElement(2, 2, 0.0);

        SymmetricMatrix covariance_global = electrodes.getLocalToGlobal().transformed(covariance);

//        System.out.println("Global covariance matrix: \n"+covariance_global);

        return covariance_global;

    }

    private double getXResolution(List<RawTrackerHit> cluster, SiSensorElectrodes electrodes) {

        double measured_resolution;

        Set<Integer> rows = new HashSet<Integer>();
        for (RawTrackerHit hit : cluster) {
            rows.add(electrodes.getRowNumber(_pixel_map.get(hit)));
        }

        int cluster_width = rows.size();
        double sense_pitch = ((SiSensor) electrodes.getDetectorElement()).getSenseElectrodes(electrodes.getChargeCarrier()).getPitch(0);

        if (cluster_width == 1) {
            measured_resolution = sense_pitch * _oneClusterErr;
        } else if (cluster_width == 2) {
            measured_resolution = sense_pitch * _twoClusterErr;
        } else if (cluster_width == 3) {
            measured_resolution = sense_pitch * _threeClusterErr;
        } else if (cluster_width == 4) {
            measured_resolution = sense_pitch * _fourClusterErr;
        } else {
            measured_resolution = sense_pitch * _fiveClusterErr;
        }

        return measured_resolution;

    }

    private double getYResolution(List<RawTrackerHit> cluster, SiSensorElectrodes electrodes) {

        double measured_resolution;

        Set<Integer> columns = new HashSet<Integer>();
        for (RawTrackerHit hit : cluster) {
            columns.add(electrodes.getColumnNumber(_pixel_map.get(hit)));
        }

        int cluster_width = columns.size();
        double sense_pitch = ((SiSensor) electrodes.getDetectorElement()).getSenseElectrodes(electrodes.getChargeCarrier()).getPitch(1);

        if (cluster_width == 1) {
            measured_resolution = sense_pitch * _oneClusterErr;
        } else if (cluster_width == 2) {
            measured_resolution = sense_pitch * _twoClusterErr;
        } else if (cluster_width == 3) {
            measured_resolution = sense_pitch * _threeClusterErr;
        } else if (cluster_width == 4) {
            measured_resolution = sense_pitch * _fourClusterErr;
        } else {
            measured_resolution = sense_pitch * _fiveClusterErr;
        }

        return measured_resolution;

    }

    private double getEnergy(List<RawTrackerHit> cluster) {
        double total_charge = 0.0;
        for (RawTrackerHit hit : cluster) {

            int pixel_number = _pixel_map.get(hit);
            double signal = _readout_chip.decodeCharge(hit);

            total_charge += signal;
        }
        return total_charge * DopedSilicon.ENERGY_EHPAIR;
    }
}
