package org.lcsim.recon.tracking.vsegment.digitization;

import java.util.*;

import org.lcsim.event.EventHeader;
import org.lcsim.recon.cat.util.NoSuchParameterException;
import org.lcsim.util.Driver;

import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.hit.DigiTrackerHit;
import org.lcsim.recon.tracking.vsegment.hit.base.DigiTrackerHitComposite;

/**
 * Driver that merges several collections of {@link DigiTrackerHit} objects into one,
 * combining hits in the same channels.
 * <p>
 * Input collections (either <tt>HashMap&lt;Sensor, ArrayList&lt;DigiTrackerHit&gt;&gt;</tt> or
 * <tt>Collection&lt;DigiTrackerHit&gt;</tt>) are fetched from the event usung name supplied
 * through calls to <tt>set("ADD_INPUT_COLLECTION", name)</tt>, and output collection
 * (type <tt>HashMap&lt;Sensor, ArrayList&lt;DigiTrackerHit&gt;&gt;</tt>) is attached
 * to the event with the name set by a call to <tt>set("OUTPUT_MAP_NAME", name)</tt>.
 * <p>
 * Whenever input collections contain hits in the same channels, new <tt>DigiTrackerHit</tt>
 * objects of type {@link DigiTrackerHitComposite} are created and stored in the output
 * map. These new objects reference all the original hits as their constituents.
 * <p>
 * {@link #merge} methods can also be called directly by other classes.
 *
 * @author D. Onoprienko
 * @version $Id: MergeDriver.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public class MergeDriver extends Driver {
  
// -- Constructors :  ----------------------------------------------------------
  
  /** Default constructor. */
  public MergeDriver() {}
  
// -- Setters :  ---------------------------------------------------------------

  /**
   * Set parameters. 
   * The following parameters can be set with this method:
   * <dl>
   * <dt>"ADD_INPUT_COLLECTION"</dt> <dd>Name of input collection of digitized hits
   *                 (type <tt>HashMap<Sensor, ArrayList<DigiTrackerHit>></tt>).<br>
   *                 No default.</dd>
   * <dt>"OUTPUT_MAP_NAME"</dt> <dd>Name of output collection of clusters
   *                 (type <tt>HashMap<Sensor, ArrayList<DigiTrackerHit>></tt>).<br>
   *                 No default.</dd></dl>
   * 
   * @param name   Name of parameter to be set. Case is ignored.
   * @param value  Value to be assigned to the parameter.
   * @throws NoSuchParameterException Thrown if the supplied parameter name is unknown.
   *         Subclasses may catch this exception after a call to <tt>super.set()</tt>
   *         and set their own parameters.
   */
  public void set(String name, Object value) {
    try {
      if (name.equalsIgnoreCase("ADD_INPUT_COLLECTION")) {
        _inMapNames.add((String)value);
      } else if (name.equalsIgnoreCase("OUTPUT_MAP_NAME")) {
        _outMapName = (String)value;
      } else {
        throw new NoSuchParameterException(name, this.getClass());
      }
    } catch (ClassCastException x) {
      throw new IllegalArgumentException("Value of incompatible type", x);
    }
 }
  
// -- Event processing :  ------------------------------------------------------
  
  /** Called by framework to process event. */
  public void process(EventHeader event) {
    
    if (_inMapNames == null) throw new RuntimeException("No input collection names provided");
    if (_outMapName == null) throw new RuntimeException("No output collection name provided");
    
    ArrayList<HashMap<Sensor, ArrayList<DigiTrackerHit>>> inMaps = 
            new ArrayList<HashMap<Sensor, ArrayList<DigiTrackerHit>>>(_inMapNames.size());
    for (String name : _inMapNames) {
      inMaps.add((HashMap<Sensor, ArrayList<DigiTrackerHit>>) event.get(name));
    }
    
    HashMap<Sensor, ArrayList<DigiTrackerHit>> outMap = merge(inMaps);
    event.put(_outMapName, outMap);
  }
  
// -- Merging :  ---------------------------------------------------------------
  
  /**
   * Returns the result of merging <tt>DigiTrackerHits</tt> from several maps.
   */
  public HashMap<Sensor, ArrayList<DigiTrackerHit>> merge(Collection<HashMap<Sensor, ArrayList<DigiTrackerHit>>> hitMaps) {
    HashMap<Sensor, ArrayList<DigiTrackerHit>> outMap = new HashMap<Sensor, ArrayList<DigiTrackerHit>>();
    for (HashMap<Sensor, ArrayList<DigiTrackerHit>> inMap : hitMaps) {
      for (Sensor sensor : inMap.keySet()) {
        ArrayList<DigiTrackerHit> inList = inMap.get(sensor);
        ArrayList<DigiTrackerHit> outList = outMap.get(sensor);
        if (outList == null) {
          outList = new ArrayList<DigiTrackerHit>(inList.size());
          outMap.put(sensor, outList);
        }
        outList.addAll(inList);
      }
    }
    for (Sensor sensor : outMap.keySet()) {
      outMap.put(sensor, merge(outMap.get(sensor)));
    }
    return outMap;
  }
  
  /**
   * Returns a list of hits produced by merging hits in the list supplied as an argument.
   * The output list is sorted by channel ID.
   */
  public ArrayList<DigiTrackerHit> merge(ArrayList<DigiTrackerHit> hitList) {
    Collections.sort(hitList);
    ArrayList<DigiTrackerHit> outList = new ArrayList<DigiTrackerHit>(hitList.size());
    int channel = -1;
    ArrayList<DigiTrackerHit> buffer = new ArrayList<DigiTrackerHit>();
    for (DigiTrackerHit hit : hitList) {
      int newChannel = hit.getChannel();
      if (buffer.isEmpty()) {
        buffer.add(hit);
        channel = newChannel;
      } else if (channel == newChannel) {
        buffer.add(hit);
      } else {
        if (buffer.size() == 1) {
          outList.add(buffer.get(0));
        } else {
          outList.add(new DigiTrackerHitComposite(buffer));
        }
        buffer.clear();
        buffer.add(hit);
        channel = newChannel;
      }
    }
    if (! buffer.isEmpty()) {
      if (buffer.size() == 1) {
        outList.add(buffer.get(0));
      } else {
        outList.add(new DigiTrackerHitComposite(buffer));
      }
    }
    outList.trimToSize();
    return outList;
  }

// -- Private parts :  ---------------------------------------------------------

  ArrayList<String> _inMapNames;
  String _outMapName;
}
