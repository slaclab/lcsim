/*
 * Main driver for setting up the hit digitization and clustering
 *
 */
package org.lcsim.recon.tracking.seedtracker.trackingdrivers.sidloi2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.detector.tracker.silicon.SiTrackerModule;
import org.lcsim.event.EventHeader;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.geometry.Detector;
import org.lcsim.recon.tracking.digitization.sisim.CDFSiSensorSim;
import org.lcsim.recon.tracking.digitization.sisim.GenericReadoutChip;
import org.lcsim.recon.tracking.digitization.sisim.NearestNeighbor;
import org.lcsim.recon.tracking.digitization.sisim.PixelHitMaker;
import org.lcsim.recon.tracking.digitization.sisim.RawTrackerHitMaker;
import org.lcsim.recon.tracking.digitization.sisim.SiDigitizer;
import org.lcsim.recon.tracking.digitization.sisim.SiSensorSim;
import org.lcsim.recon.tracking.digitization.sisim.SiTrackerHit;
import org.lcsim.recon.tracking.digitization.sisim.SiTrackerHitPixel;
import org.lcsim.recon.tracking.digitization.sisim.SiTrackerHitStrip1D;
import org.lcsim.recon.tracking.digitization.sisim.StripHitMaker;
import org.lcsim.recon.tracking.digitization.sisim.config.SimTrackerHitReadoutDriver;
import org.lcsim.util.Driver;
import org.lcsim.lcio.LCIOConstants;

/**
 *
 * @author Richard Partridge
 */
public class TrackerHitDriver_sidloi2 extends Driver {

    List<String> _readouts = new ArrayList<String>();
    List<String> _process_paths = new ArrayList<String>();
    List<IDetectorElement> _process_de = new ArrayList<IDetectorElement>();
    Set<SiSensor> _process_sensors = new HashSet<SiSensor>();
    Set<SiTrackerModule> _process_modules = new HashSet<SiTrackerModule>();
    SiDigitizer _strip_digitizer;
    SiDigitizer _pixel_digitizer;
    StripHitMaker _strip_clusterer;
    PixelHitMaker _pixel_clusterer;
    String _digitizer_name;
    int _nev = 0;

    /**
     * Creates a new instance of TrackerHitDriver
     */
    public TrackerHitDriver_sidloi2() {

        //  Instantiate the sensor simulation classes and set the thresholds
        SiSensorSim strip_simulation = new CDFSiSensorSim();
        SiSensorSim pixel_simulation = new CDFSiSensorSim();

        //  Instantiate the readout chips and set the noise parameters
        GenericReadoutChip strip_readout = new GenericReadoutChip();
        strip_readout.setNoiseIntercept(800.);
        strip_readout.setNoiseSlope(0.);
        strip_readout.setNoiseThreshold(4000.);
        strip_readout.setNeighborThreshold(4000.);
        GenericReadoutChip pixel_readout = new GenericReadoutChip();
        pixel_readout.setNoiseIntercept(80.);
        pixel_readout.setNoiseSlope(0.);
        pixel_readout.setNoiseThreshold(400.);
        pixel_readout.setNeighborThreshold(400.);

        //  Instantiate the digitizer that produces the raw hits
        _strip_digitizer = new RawTrackerHitMaker(strip_simulation, strip_readout);
        _pixel_digitizer = new RawTrackerHitMaker(pixel_simulation, pixel_readout);
        _digitizer_name = _strip_digitizer.getName();

        //  Instantiate a nearest neighbor clustering algorithm for the pixels
        NearestNeighbor strip_clustering = new NearestNeighbor();
        strip_clustering.setSeedThreshold(4000.);
        strip_clustering.setNeighborThreshold(2000.);

        //  Instantiate a nearest neighbor clustering algorithm for the pixels
        NearestNeighbor pixel_clustering = new NearestNeighbor();
        pixel_clustering.setSeedThreshold(400.);
        pixel_clustering.setNeighborThreshold(400.);

        //  Instantiate the clusterers and set hit-making parameters
        _strip_clusterer = new StripHitMaker(strip_simulation, strip_readout, strip_clustering);
        _strip_clusterer.setMaxClusterSize(10);
        _strip_clusterer.setCentralStripAveragingThreshold(4);
        _strip_clusterer.SetOneClusterErr(1 / Math.sqrt(12.));
        _strip_clusterer.SetTwoClusterErr(1 / 5.0);
        _strip_clusterer.SetThreeClusterErr(1 / 3.0);
        _strip_clusterer.SetFourClusterErr(1 / 2.0);
        _strip_clusterer.SetFiveClusterErr(1 / 1.0);
        
        _pixel_clusterer = new PixelHitMaker(pixel_simulation, pixel_readout, pixel_clustering);
        _pixel_clusterer.SetOneClusterErr(1 / Math.sqrt(12.));
        _pixel_clusterer.SetTwoClusterErr(1 / 5.0);
        _pixel_clusterer.SetThreeClusterErr(1 / 3.0);
        _pixel_clusterer.SetFourClusterErr(1 / 2.0);
        _pixel_clusterer.SetFiveClusterErr(1 / 1.0);

        //  Specify the readouts to process
        _readouts.add("SiVertexBarrelHits");
        _readouts.add("SiVertexEndcapHits");
        _readouts.add("SiTrackerBarrelHits");
        _readouts.add("SiTrackerEndcapHits");
        _readouts.add("SiTrackerForwardHits");

        //  Specify the detectors to process
        _process_paths.add("SiVertexBarrel");
        _process_paths.add("SiVertexEndcap");
        _process_paths.add("SiTrackerBarrel");
        _process_paths.add("SiTrackerEndcap");
        _process_paths.add("SiTrackerForward");

    }

    /**
     * Initialize whenever we have a new detector
     * 
     * @param detector
     */
    public void detectorChanged(Detector detector) {

        super.detectorChanged(detector);

        // Process detectors specified by path, otherwise process entire detector
        IDetectorElement detector_de = detector.getDetectorElement();
        for (String de_path : _process_paths) {
            _process_de.add(detector_de.findDetectorElement(de_path));
        }

        if (_process_de.size() == 0) {
            _process_de.add(detector_de);
        }

        for (IDetectorElement detector_element : _process_de) {
            _process_sensors.addAll(detector_element.findDescendants(SiSensor.class));
            _process_modules.addAll(detector_element.findDescendants(SiTrackerModule.class));
        }

    }

    /**
     * Setup readouts
     */
    public void startOfData() {
        // If readouts not already set, set them up
        if (_readouts.size() != 0) {
            super.add(new SimTrackerHitReadoutDriver(_readouts));
        }

        super.startOfData();
        _readouts.clear();
        _nev = 0;
    }

    /**
     * Main digitization driver.  Creates raw hits, forms clusters, and makes
     * tracker hits using the sisim package.
     *
     * @param event
     */
    public void process(EventHeader event) {
        super.process(event);

        //  Print out the event number
//        System.out.println("TrackerHitDriver processing event " + _nev);
        _nev++;

        // Lists of hits
        List<RawTrackerHit> raw_hits = new ArrayList<RawTrackerHit>();
        List<SiTrackerHit> hits_strip1D = new ArrayList<SiTrackerHit>();
        List<SiTrackerHit> hits_pixel = new ArrayList<SiTrackerHit>();

        for (SiSensor sensor : _process_sensors) {
 
            if (sensor.hasStrips()) {
                raw_hits.addAll(_strip_digitizer.makeHits(sensor));
                hits_strip1D.addAll(_strip_clusterer.makeHits(sensor));
            }


            if (sensor.hasPixels()) {
                raw_hits.addAll(_pixel_digitizer.makeHits(sensor));
                hits_pixel.addAll(_pixel_clusterer.makeHits(sensor));
            }

        }

        //int flag = (1 << LCIOConstants.RTHBIT_HITS | 1 << LCIOConstants.TRAWBIT_ID1); //correct flag for persistence 
        int flag = (1 << LCIOConstants.TRAWBIT_ID1); //correct flag for persistence 
        event.put(getRawHitsName(), raw_hits, RawTrackerHit.class, flag, toString());
        event.put(getStripHits1DName(), hits_strip1D, SiTrackerHitStrip1D.class, 0, toString());
        event.put(getPixelHitsName(), hits_pixel, SiTrackerHitPixel.class, 0, toString());

    }

    /**
     * Return the name of the raw hits collection
     *
     * @return name of raw hits collection
     */
    public String getRawHitsName() {
        return _digitizer_name + "_RawTrackerHits";
    }

    /**
     * Return the name of the strip hits collection
     *
     * @return name of strip hits collection
     */
    public String getStripHits1DName() {
        return _strip_clusterer.getName() + "_SiTrackerHitStrip1D";
    }

    /**
     * Return the name of the pixel hits collection
     *
     * @return name of pixel hits collection
     */
    public String getPixelHitsName() {
        return _pixel_clusterer.getName() + "_SiTrackerHitPixel";
    }
}
