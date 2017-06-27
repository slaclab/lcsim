package org.lcsim.recon.tracking.vsegment.digitization;

import java.util.*;

import org.lcsim.detector.DetectorElementStore;
import org.lcsim.detector.IDetectorElementContainer;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.Identifier;
import org.lcsim.event.EventHeader;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.recon.cat.util.NoSuchParameterException;
import org.lcsim.units.clhep.SystemOfUnits;
import org.lcsim.util.Driver;

import org.lcsim.recon.tracking.vsegment.geom.SegmentationManager;
import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.hit.DigiTrackerHit;
import org.lcsim.recon.tracking.vsegment.hit.base.DigiTrackerHitComposite;
import org.lcsim.recon.tracking.vsegment.mctruth.MCTruth;

/**
 * Driver that handles conversion of {@link SimTrackerHit} objects collections
 * found in a simulated event into a collection of {@link DigiTrackerHit} objects. 
 * <p>
 * The actual conversion of a group of <tt>SimTrackerHits</tt> produced by a particle
 * crossing sensitive detector into a group <tt>DigiTrackerHits</tt> is done by the
 * {@link SimToDigiConverter} object supplied in the constructor. The driver handles reading and
 * writing collections, dividing <tt>SimTrackerHits</tt> into groups corresponding to 
 * single particle-sensor crossings, passing those groups to the <tt>SimToDigiConverter</tt>,
 * and combining <tt>DigiTrackerHits</tt> corresponding to the same channel.
 * <p>
 * On input, lists of <tt>SimTrackerHits</tt> with names added through calls to 
 * <tt>set("ADD_INPUT_LIST_NAME", name)</tt> will be processed. If no input list names have 
 * been added, all <tt>SimTrackerHit</tt> lists found in the event will be processed.
 * <p>
 * On output, a map of {@link Sensor} objects to lists of created <tt>DigiTrackerHits</tt>
 * is added to the event, with a name supplied through a call to <tt>set("OUTPUT_MAP_NAME", name)</tt>
 * (type <tt>HashMap&lt;Sensor, ArrayList&lt;DigiTrackerHit&gt;&gt;</tt>).
 * 
 * 
 * 
 * @author D.Onoprienko
 * @version $Id: SimToDigiDriver.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public class SimToDigiDriver extends Driver {
  
// -- Constructors :  ----------------------------------------------------------
  
  /**
   * Create digitization driver with the given <tt>SimToDigiConverter</tt> and the default
   * distance cut of 200 microns.
   */
  public SimToDigiDriver(SimToDigiConverter converter) {
    _converter = converter;
    set("DISTANCE_CUT", 200.*SystemOfUnits.micrometer);
  }
  
// -- Getters :  ---------------------------------------------------------------
  
  /** Returns <tt>SimToDigiConverter</tt> object used by this driver. */
  public SimToDigiConverter getConverter() {return _converter;}
  
// -- Setters :  ---------------------------------------------------------------
  
  /** Set <tt>SimToDigiConverter</tt> to be used by this driver. */
  public void setConverter(SimToDigiConverter converter) {_converter = converter;}
  
  /**
   * Set any <tt>double</tt> parameter.
   * The following parameters can be set with this method:
   * <p><dl>
   * <dt>"DISTANCE_CUT"</dt> <dd>Put <tt>SimTrackerHit</tt>s that are less than this
   *                           distance apart and produced by the same <tt>MCParticle</tt>
   *                           into a group supplied in a single call to <tt>SimToDigiConverter</tt>.
   *                           Default: 200 microns.</dd></dl>
   * 
   * 
   * @param name   Name of parameter to be set (case is ignored).
   * @param value  Value to be assigned to the parameter.
   * @throws NoSuchParameterException Thrown if the supplied parameter name is unknown.
   *         Subclasses may catch this exception after a call to <tt>super.set()</tt>
   *         and set their own parameters.
   */
  public void set(String name, double value) {
    if (name.equalsIgnoreCase("DISTANCE_CUT")) {
      _distCut2 = value*value;
    } else {
      throw new NoSuchParameterException(name, this.getClass());
    }
  }
  
  /** 
   * Set any <tt>String</tt> parameter. 
   * The following parameters can be set with this method:
   * <p><dl>
   * <dt>"ADD_INPUT_LIST_NAME"</dt> <dd>Add a name of input list of simulated hits
   *                 (type <tt>List<SimTrackerHit></tt>). 
   *                 Default: <tt>null</tt> (all lists will be processed).<dd>
   * <dt>"OUTPUT_MAP_NAME"</dt> <dd>Name of ouitput collection of hist
   *             (type <tt>HashMap<Sensor, ArrayList<DigiTrackerHit>></tt>). 
   *             Default: "DigiHits".<dd></dl>
   *
   * @param name   Name of parameter to be set. Case is ignored.
   * @param value  Value to be assigned to the parameter.
   * @throws NoSuchParameterException Thrown if the supplied parameter name is unknown.
   *         Subclasses may catch this exception after a call to <tt>super.set()</tt>
   *         and set their own parameters.
   */
  public void set(String name, String value) {
    if (name.equalsIgnoreCase("ADD_INPUT_LIST_NAME")) {
      if (_inListNames == null) _inListNames = new ArrayList<String>(5);
      _inListNames.add(value);
    } else if (name.equalsIgnoreCase("OUTPUT_MAP_NAME")) {
      _outMapName = value;
    } else {
      throw new NoSuchParameterException(name, this.getClass());
    }
  }
  
// -- Processing event :  ------------------------------------------------------ 

  public void process(EventHeader event) {
    
    // Process children if any
    
    super.process(event);
    
    // Fetch MCTruth object if present in the event:
    
    MCTruth mcTruth = null;
    try {
      mcTruth = (MCTruth) event.get("MCTruth");
    } catch (IllegalArgumentException x) {}
    
    // Fetch Segmentation manager:
    
    SegmentationManager segMan = (SegmentationManager) event.get("SegmentationManager");
    _converter.setSegmentationManager(segMan);
 
    // Make a list of SimTrackerHit collections to process :
    
    List<List<SimTrackerHit>> collections;

    if (_inListNames == null) {
      collections = event.get(SimTrackerHit.class);
    } else {
      collections = new ArrayList<List<SimTrackerHit>>(_inListNames.size());
      for (String name : _inListNames) {
        try {
          collections.add(event.get(SimTrackerHit.class, name));
        } catch (IllegalArgumentException x) {}
      }
    }
    
    // Create output map :
    
    HashMap<Sensor, ArrayList<DigiTrackerHit>> outMap = new HashMap<Sensor, ArrayList<DigiTrackerHit>>();

    // Loop over input collections :
    
    for (List<SimTrackerHit> hitList : collections) {
      
      // Split hit lists into groups produced by a single particle-sensor crossing
      
      LinkedList<LinkedList<SimTrackerHit>> groupList = new LinkedList<LinkedList<SimTrackerHit>>();
      for (SimTrackerHit hit : hitList) {
        
        // associate DetectorElements with SimTrackerHits
        if (hit.getDetectorElement() == null) {
          IIdentifier hitId = new Identifier(hit.getCellID());
          IDetectorElementContainer deHit = DetectorElementStore.getInstance().find(hitId);
          if (deHit.size() == 0) {
            throw new RuntimeException("No DetectorElement found for id <"+hitId.toString()+">.");
          }
          hit.setDetectorElement( deHit.get(0) );
        }

        boolean found = false;
        for (LinkedList<SimTrackerHit> group : groupList) {
          SimTrackerHit lastHit = group.getLast();
          if (hit.getMCParticle() == lastHit.getMCParticle() && hit.getLayer() == lastHit.getLayer()) {
            double[] p1 = lastHit.getPoint();
            double[] p2 = hit.getPoint();
            if ((p1[0]-p2[0])*(p1[0]-p2[0]) + (p1[1]-p2[1])*(p1[1]-p2[1]) + (p1[2]-p2[2])*(p1[2]-p2[2]) < _distCut2) {
              found = true;
              group.addLast(hit);
              break;
            }
          }
        }
        if (! found) {
          LinkedList<SimTrackerHit> newGroup = new LinkedList<SimTrackerHit>();
          newGroup.add(hit);
          groupList.addFirst(newGroup);
        }
      }
      
      // Call SimToDigiConverter for each group, add resulting digis to the output map
      
      for (LinkedList<SimTrackerHit> group : groupList) {
        
        List<DigiTrackerHit> digiGroup = _converter.convert(group);
        if (mcTruth != null) mcTruth.addSimGroup(group, digiGroup);
        
        Sensor prevSensor = null;
        ArrayList<DigiTrackerHit> digiList = null;
        for (DigiTrackerHit digi : digiGroup) {
          Sensor sensor = digi.getSensor();
          if (sensor != prevSensor) {
            prevSensor = sensor;
            digiList = outMap.get(sensor);
          }
          if (digiList == null) {
            digiList = new ArrayList<DigiTrackerHit>();
            outMap.put(sensor, digiList);
          }
          digiList.add(digi);
        }
      }

    } // end of loop over input collections
    
    // Combine and sort DigiTrackerHits in each list
    
    for (ArrayList<DigiTrackerHit> dList : outMap.values()) {
      Collections.sort(dList);
      DigiTrackerHit[] copy = dList.toArray(new DigiTrackerHit[dList.size()+1]);
      dList.clear();
      int previousChannel = -1;
      DigiTrackerHit previousHit = null;
      DigiTrackerHitComposite compHit = null;
      for (DigiTrackerHit hit : copy) {
        if (hit == null) {
          if (compHit == null) {
            if (previousHit != null) dList.add(previousHit);
          } else {
            compHit.trimToSize();
            dList.add(compHit);
          }
          break;
        }
        int channelID = hit.getChannel();
        if (channelID == previousChannel) {
          if (compHit == null) {
            compHit = new DigiTrackerHitComposite();
            compHit.addHit(previousHit);
          }
          compHit.addHit(hit);
        } else {
          if (compHit == null) {
            if (previousHit != null) dList.add(previousHit);
          } else {
            compHit.trimToSize();
            dList.add(compHit);
            compHit = null;
          }
        }
        previousHit = hit;
        previousChannel = channelID;
      }
      dList.trimToSize();
    }
    
    // Put the map back into the event
    
    event.put(_outMapName, outMap);
    
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  protected SimToDigiConverter _converter;
  
  protected ArrayList<String> _inListNames;
  protected String _outMapName;
  
  protected double _distCut2;
}
