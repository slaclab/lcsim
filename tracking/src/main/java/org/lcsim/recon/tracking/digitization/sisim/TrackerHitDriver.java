/*
 * TrackerHitDriver.java
 *
 * Created on February 15, 2008, 7:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.detector.tracker.silicon.SiTrackerModule;
import org.lcsim.event.EventHeader;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.geometry.Detector;
import org.lcsim.recon.tracking.digitization.sisim.config.SimTrackerHitReadoutDriver;
import org.lcsim.util.Driver;
import org.lcsim.lcio.LCIOConstants;

/**
 *
 * @author tknelson
 */
public class TrackerHitDriver extends Driver
{
    
    List<String> _readouts = new ArrayList<String>();
    
    List<String> _process_paths = new ArrayList<String>();
    List<IDetectorElement> _process_de = new ArrayList<IDetectorElement>();
    Set<SiSensor> _process_sensors = new HashSet<SiSensor>();
    Set<SiTrackerModule> _process_modules = new HashSet<SiTrackerModule>();
    
    //  Algorithm classes responsible for making hits
    SiSensorSim _strip_simulation = new CDFSiSensorSim();
    ReadoutChip _strip_readout = new Kpix();

    SiSensorSim _pixel_simulation = new CDFSiSensorSim();
    ReadoutChip _pixel_readout = new Kpix();
    
    SiDigitizer _digitizer = new RawTrackerHitMaker(_strip_simulation,_strip_readout);
    Clusterer _strip_clusterer = new StripHitMaker(_strip_simulation,_strip_readout);
    Clusterer _pixel_clusterer = new PixelHitMaker(_pixel_simulation,_pixel_readout);
    StripHitCombiner _striphit_combiner = new StripHit2DMaker();
    
    /**
     * Creates a new instance of TrackerHitDriver
     */
    
    // Default constructor
    public TrackerHitDriver()
    {
        
    }
    
    // Construct your own
    public TrackerHitDriver(SiDigitizer digitizer, Clusterer strip_clusterer,
            Clusterer pixel_clusterer, StripHitCombiner striphit_combiner)
    {
        _digitizer = digitizer;
        _strip_clusterer = strip_clusterer;
        _pixel_clusterer = pixel_clusterer;
        _striphit_combiner = striphit_combiner;
    }
    
    // Change out a single component
    //------------------------------
    public void setDigitizer(SiDigitizer digitizer)
    {
        _digitizer = digitizer;
    }
    
    public void setStripClusterer(Clusterer strip_clusterer)
    {
        _strip_clusterer = strip_clusterer;
    }

    public void setPixelClusterer(Clusterer pixel_clusterer)
    {
        _pixel_clusterer = pixel_clusterer;
    }
    
    public void setStripHitCombiner(StripHitCombiner striphit_combiner)
    {
        _striphit_combiner = striphit_combiner;
    }
    
    // Access components
    //------------------
    public SiDigitizer getDigitizer()
    {
        return _digitizer;
    }
    
    public Clusterer getStripClusterer()
    {
        return _strip_clusterer;
    }
    
    public Clusterer getPixelClusterer()
    {
        return _pixel_clusterer;
    }
    
    public StripHitCombiner getStripHitCombiner()
    {
        return _striphit_combiner;
    }
    
    // Collection names
    //-----------------
    public String getRawHitsName()
    {
        return _digitizer.getName()+"_RawTrackerHits";
    }
    
    public String getStripHits1DName()
    {
        return _strip_clusterer.getName()+"_SiTrackerHitStrip1D";
    }
    
    String getPixelHitsName()
    {
        return _pixel_clusterer.getName()+"_SiTrackerHitPixel";
    }
    
    public String getStripHits2DName()
    {
        return _striphit_combiner.getName()+"_SiTrackerHitStrip2D";
    }
    
    
    // Define which hits to use and which detector elements to process
    //================================================================
    
    // Add a readout name for SimTrackerHits to use
    public void setReadout(String readout)
    {
        _readouts.add(readout);
    }
    
    public void setReadouts(String readout[])
    {
    	_readouts.addAll(Arrays.asList(readout));
    }
    
    public void setElementsToProcess(String de_paths[])
    {
    	_process_paths.addAll(Arrays.asList(de_paths));
    }
    
    // Add a detector to process
    public void addElementToProcess(String de_path)
    {
        _process_paths.add(de_path);
    }
    
    // Actions begins here
    //====================
    public void detectorChanged(Detector detector)
    {
         System.out.println(detector.getName());
        super.detectorChanged(detector);
        
        // Process detectors specified by path, otherwise process entire detector
        IDetectorElement detector_de = detector.getDetectorElement();
        System.out.println(detector_de.getName());
        for (String de_path : _process_paths)
        {
            _process_de.add(detector_de.findDetectorElement(de_path));
        }
        
        if (_process_de.size() == 0)
        {
            _process_de.add(detector_de);
        }
        
        for (IDetectorElement detector_element : _process_de)
        {
            _process_sensors.addAll(detector_element.findDescendants(SiSensor.class));
            _process_modules.addAll(detector_element.findDescendants(SiTrackerModule.class));
        }
        
    }
    
    public void startOfData()
    {        
        // If readouts not already set, set them up
        if (_readouts.size() != 0)
        {
            System.out.println("Adding SimTrackerHitIdentifierReadoutDriver with readouts: "+_readouts);
            super.add( new SimTrackerHitReadoutDriver( _readouts ) );        
        }
        
        // Call this after added above driver, so that subdriver's startOfData() method is called. --JM
        super.startOfData();

        // Only allow this once per job since readouts cannot be deleted for SimTrackerIdentifierReadoutDriver
        // FIXME: should be a robust system for changing readouts and eliminating duplicates in the readout driver
        _readouts.clear();

    }
    
    public void process(EventHeader event)
    {
        super.process(event);
        System.out.println("TrackerHitDriver processing event...");
        
        // Lists of hits
        List<RawTrackerHit> raw_hits = new ArrayList<RawTrackerHit>();

        List<SiTrackerHit> hits_strip1D = new ArrayList<SiTrackerHit>();
        List<SiTrackerHit> hits_pixel = new ArrayList<SiTrackerHit>();
        List<SiTrackerHit> hits_strip2D = new ArrayList<SiTrackerHit>();
        
//       for (IDetectorElement detector_element : _process_de)        {
//
        //System.out.println("Processing detector: "+detector_element.getName());
        
        for (SiSensor sensor : _process_sensors)
        {
 //            System.out.println("Processing "+sensor.getName());
//            if (sensor.getName().contains("Endcap"))
//            {
//                System.out.println("Processing sensor: "+sensor.getName());
//            }
            
            raw_hits.addAll(_digitizer.makeHits(sensor));
            
            if (sensor.hasStrips())
            {
                hits_strip1D.addAll(_strip_clusterer.makeHits(sensor));
            }

         
            //            if (sensor.hasPixels())
//            {
//                hits_pixel.addAll(_pixel_clusterer.makeHits(sensor));
//            }
            // When pixels are working, this should become an else if to pixel hitmaking
            if (sensor.isDoubleSided())
            {
              
//               hits_strip2D.addAll(_striphit_combiner.makeHits(sensor));
            }
        }
        
 
        
        for (SiTrackerModule module : _process_modules)
        {
  //          System.out.println("Combining double sided modules "+module.getName());
            if (module.isDoubleSided())
            {   
  //              System.out.println(" # Raw Hits:"+raw_hits.size());
  //              System.out.println("# Strip1D Hits:"+hits_strip1D.size());
//                hits_strip2D.addAll(_striphit_combiner.makeHits(module));
            }
        }
 //           System.out.println(" # Raw Hits:"+raw_hits.size());
  //          System.out.println("# Strip1D Hits:"+hits_strip1D.size());
//        }

        // FIXME
        // Take list of 2D hits
        // Loop through and make LCRelationalTable
        // Add table to event
        // Is that all there is?
        
//        System.out.println("# Strip2D Hits:"+hits_strip2D.size());
        //int flag = (1 << LCIOConstants.RTHBIT_HITS | 1 << LCIOConstants.TRAWBIT_ID1); //correct flag for persistence 
        int flag = (1 << LCIOConstants.TRAWBIT_ID1); //correct flag for persistence 
        event.put(getRawHitsName(),raw_hits,RawTrackerHit.class,flag,toString());
        event.put(getStripHits1DName(),hits_strip1D,SiTrackerHitStrip1D.class,0,toString());
        event.put(getPixelHitsName(),hits_pixel,SiTrackerHitPixel.class,0,toString());
        event.put(getStripHits2DName(),hits_strip2D,SiTrackerHitStrip2D.class,0,toString());

        
    }
    
}













//    public void makeHits(IDetectorElement detector)
//    {
//
//        List<SiSensor> sensors = detector.findDescendants(SiSensor.class);
//        List<SiTrackerModule> modules = detector.findDescendants(SiTrackerModule.class);
//
//        // clear hit lists
//        _hits_raw.clear();
//        _hits.clear();
//
//        // Loop over all sensors
//        for (SiSensor sensor : sensors)
//        {
//            // Make raw hits
//            _hits_raw.addAll(_raw_hitmaker.makeHits(sensor));
//
//            // Make Pixel or 1D hits - FIXME need appropriate switch and protection here
//            _hits.addAll(_strip_clusterer.makeHits(sensor));
//
//            // If double-sided strip sensors, make 2-d hits
//            if (sensor.isDoubleSided())
//            {
//                _hits.addAll(_2D_hitmaker.makeHits2D(sensor));
//            }
//        }
//
//        // Loop over all modules
//        for (SiTrackerModule module : modules)
//        {
//            if (module.isDoubleSided())
//            {
//                _hits.addAll(_2D_hitmaker.makeHits2D(module));
//            }
//        }
//
//    }


//    public List<RawTrackerHit> getRawHits()
//    {
//        return _hits_raw;
//    }


//    public List<TrackerHit> getHits()
//    {
//        return _hits;
//    }





//                for (SiSensorElectrodes electrodes : sensor.getReadoutElectrodes())
//                {
//                    if (electrodes instanceof SiStrips)
//                    {
//                        hits_strip1D.addAll(makeHits((SiStr);
//                    }
////                    else if (electrodes instanceof SiPixels)
////                    {
////                        hits.addAll(_pixel_clusterer.makeHits(electrodes));
////                    }
//                }
