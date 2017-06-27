package org.lcsim.recon.cat;

import java.util.*;

import org.lcsim.event.*;
import org.lcsim.util.Driver;
import org.lcsim.geometry.Subdetector;

import org.lcsim.recon.cat.util.Const;
import org.lcsim.recon.cat.util.NoSuchParameterException;

/**
 * This processor converts tracker hits into GarfieldHit objects.
 * It lets the user to specify collections to be included, or ignore the hits
 * picked up by previously run tracking algorithms.  It also gets rid of
 * multiple adjacent hits due to delta electrons.
 * <p>
 * Most of this machinery will become unnecessary once proper digitization
 * packages are available, and standard two-dimensional tracker hit interface is
 * specified.
 *
 * @author  E. von Toerne
 * @author D. Onoprienko
 * @version $Id: GarfieldHitConverter.java,v 1.1 2007/04/06 21:48:14 onoprien Exp $
 */
final public class GarfieldHitConverter extends Driver {
  
// --  Constructors :  ---------------------------------------------------------
  
  public GarfieldHitConverter() {}
  
// -- Setters :  ---------------------------------------------------------------

  /** 
   * Set any <tt>String</tt> parameter. 
   * The following parameters can be set with this method:<br>
   * <tt>"OUTPUT_COLLECTION_NAME"</tt> - name of output <tt>TrackerHit</tt> collection. Default: "GarfieldHits"<br>
   * <tt>"INCLIDE_HIT_COLLECTION"</tt> - name of input <tt>SimTrackerHit</tt> collection to be included in processing.<br>
   * @param name   Name of parameter to be set. Case is ignored.
   * @param value  Value to be assigned to the parameter.
   * @throws NoSuchParameterException Thrown if the supplied parameter name is unknown.
   *         Subclasses may catch this exception after a call to <tt>super.set()</tt>
   *         and set their own parameters.
   */
  public void set(String name, String value) {
    if (name.equalsIgnoreCase("OUTPUT_COLLECTION_NAME")) {
      outputCollectionName = value;
    } else if (name.equalsIgnoreCase("INCLIDE_HIT_COLLECTION")) {
      inputCollectionNames.add(value);
    } else {
      throw new NoSuchParameterException(name, this.getClass());
    }
  }
  
  /** 
   * Set any <tt>boolean</tt> parameter. 
   * The following parameters can be set with this method:<br>
   * <tt>"INCLUDE_VXD"</tt> - Process <tt>SimTrackerHit</tt>s from VXD. Default: <tt>true</tt><br>
   * <tt>"OUTSIDE_IN"</tt> - sort hits for outside-in processing. Default: <tt>true</tt><br>
   * @param name   Name of parameter to be set. Case is ignored.
   * @param value  Value to be assigned to the parameter.
   * @throws NoSuchParameterException Thrown if the supplied parameter name is unknown.
   *         Subclasses may catch this exception after a call to <tt>super.set()</tt>
   *         and set their own parameters.
   */
  public void set(String name, boolean value) {
    if (name.equalsIgnoreCase("INCLUDE_VXD")) {
      modeUseVXD = value;
    } else if (name.equalsIgnoreCase("OUTSIDE_IN")) {
      modeFromOutsideIn = value;
    } else {
      throw new NoSuchParameterException(name, this.getClass());
    }
  }
  
  /** 
   * Set any <tt>boolean</tt> parameter to <tt>true</tt>.
   * See {@link #set(String, boolean)} for a list of parameters that can be set with this method.
   * @param name   Name of parameter to be set
   * @throws NoSuchParameterException Thrown if the supplied parameter name is unknown.
   *         Subclasses may catch this exception after a call to <tt>super.set()</tt>
   *         and set their own parameters.
   */
  public void set(String name) {set(name, true);}
  
  /** 
   * Set any <tt>double</tt> parameter. 
   * The following parameters can be set with this method:<br>
   * <tt>"DISTANCE_CUT"</tt> - Combine <tt>SimTrackerHit</tt>s that are less than this distance apart. Default: 200 mkm.<br>
   * <tt>"BARREL_TILING"</tt> - Default: 10 cm.<br>
   * <tt>"ENDCAP_TILING"</tt> - Default: 10 cm.<br>
   * <tt>"SIMPLE_ERROR"</tt> - Position error to be assigned to hits. Default: 50 mkm.<br>
   * @param name   Name of parameter to be set
   * @param value  Value to be assigned to the parameter.
   * @throws NoSuchParameterException Thrown if the supplied parameter name is unknown.
   *         Subclasses may catch this exception after a call to <tt>super.set()</tt>
   *         and set their own parameters.
   */
  public void set(String name, double value) {
    if (name.equalsIgnoreCase("DISTANCE_CUT")) {
      distanceCut = value;
    } else if (name.equalsIgnoreCase("BARREL_TILING")) {
      barrelTiling = value;
    } else if (name.equalsIgnoreCase("ENDCAP_TILING")) {
      endcapTiling = value;
    } else if (name.equalsIgnoreCase("SIMPLE_ERROR")) {
      simpleError = value;
    } else {
      throw new NoSuchParameterException(name, this.getClass());
    }
  }
  
// -- Processing event :  ------------------------------------------------------
  
  /**
   * Process event, and put created list of <tt>GarfieldHit</tt>s into the event.
   */
  public void process(EventHeader event) {
    
    // Convert SimTrackerHit to GarfieldHit
    
    // endcap tracker:  a simplified geometry:
    // even layers go along y, odd layers along x direction.
    // xxx this needs to be made more realistic
    
    ArrayList<GarfieldHit> listGarfieldHits = new ArrayList<GarfieldHit>();
    
    double[] xx0 = new double[]{0.,0.,0.};
    double[] xx1 = new double[]{0.,0.,0.};
    int id = 0;
    
    List<List<SimTrackerHit>> collections = event.get(SimTrackerHit.class);
    for (List<SimTrackerHit> collection : collections) {
      String collectionName = event.getMetaData(collection).getName();
      if (inputCollectionNames.isEmpty() || inputCollectionNames.contains(collectionName)) {
        for (SimTrackerHit hit: collection){
          
          if ( (!modeUseVXD) && isVXD(hit)) continue;
          
          if (findAdjacentHit(hit, listGarfieldHits)) continue; // Skip if hit is too close to one of
          // GarfieldHits that have been already created
          
          double[] p=hit.getPoint();
          int hLayer = hit.getLayer();
          int newLayerID = getGarfieldLayerID(hit);
          
          boolean isInEndCap = hit.getSubdetector().isEndcap();
          
          GarfieldHit gh;
          if (is3D(hit)){                                           // vertex detector
            gh = new GarfieldHit(p, simpleError, newLayerID, id);
          } else if (isInEndCap && isEven(hLayer)){             // even endcap layer
            xx0[0]=p[0];
            xx0[1]=Math.ceil(p[1]/endcapTiling)*endcapTiling;
            xx0[2]=p[2];
            xx1[0]=p[0];
            xx1[1]=(Math.ceil(p[1]/endcapTiling)-1)*endcapTiling;
            xx1[2]=p[2];
            gh = new GarfieldHit(xx0, xx1, simpleError, newLayerID, id);
            gh.setEndcap(true);
          } else if (isInEndCap && !isEven(hLayer)){             // odd endcap layer
            xx0[0]=Math.ceil(p[0]/endcapTiling)*endcapTiling;
            xx0[1]=p[1];
            xx0[2]=p[2];
            xx1[0]=(Math.ceil(p[0]/endcapTiling)-1)*endcapTiling;
            xx1[1]=p[1];
            xx1[2]=p[2];
            gh = new GarfieldHit(xx0, xx1, simpleError, newLayerID, id);
            gh.setEndcap(true);
          } else {                                                 // barrel tracker
            xx0[0]=p[0];
            xx0[1]=p[1];
            xx0[2]=barrelTrackerZ(p[2],0,hit.getLayer());
            xx1[0]=p[0];
            xx1[1]=p[1];
            xx1[2]=barrelTrackerZ(p[2],1,hit.getLayer());
            gh = new GarfieldHit(xx0, xx1, simpleError, newLayerID, id);
          }
          gh.addRawHit(hit);
          listGarfieldHits.add(gh);
          id++;
        }
      }
    }
    
    // Sort the list of Garfield hits by layer number
    
    if (modeFromOutsideIn){
      Collections.sort(listGarfieldHits, new Comparator() {
        public int compare(Object o1, Object o2) {
          return ((GarfieldHit)o2).getLayer() - ((GarfieldHit)o1).getLayer();
        }
      });
    } else{
      Collections.sort(listGarfieldHits, new Comparator() {
        public int compare(Object o1, Object o2) {
          return ((GarfieldHit)o1).getLayer() - ((GarfieldHit)o2).getLayer();
        }
      });
    }
    // Return Garfield hits collection (and put it into the event)
    
    listGarfieldHits.trimToSize();
    event.put(outputCollectionName, listGarfieldHits);    
  }
  
// -- Private helper methods :  ------------------------------------------------
  
  /**
   * Returns true if a GarfieldHit exists in the supplied <code>oldHitList</code>
   * that is too close to the <code>newHit</code>, so the two should be merged.
   * <code>newHit</code> is then added to the list of raw hits associated with that
   * GarfieldHit.
   *
   * This is a temporary solution to get rid of multiple adjacent hits due to delta rays -
   * should be replaced by proper digitization later.
   */
  private boolean findAdjacentHit(SimTrackerHit newHit, ArrayList<GarfieldHit> oldHitList) {
    boolean tooClose = false;
    for (GarfieldHit gHit : oldHitList) {
      List<SimTrackerHit> rawHits = (List<SimTrackerHit>)gHit.getRawHits();
      for (SimTrackerHit oldHit : rawHits) {
        if ( (newHit.getLayer() == oldHit.getLayer()) && (oldHit.getSubdetector().getSystemID() == newHit.getSubdetector().getSystemID()) ) {
          double[] p1 = newHit.getPoint();
          double[] p2 = oldHit.getPoint();
          double alphaZ = 1.;
          if (oldHit.getSubdetector().isBarrel() && !isVXD(oldHit)) alphaZ = 0.0001; // Do we need this ?
          double d = Math.sqrt((p1[0]-p2[0])*(p1[0]-p2[0])+(p1[1]-p2[1])*(p1[1]-p2[1])+alphaZ*(p1[2]-p2[2])*(p1[2]-p2[2]));
          tooClose = (d < this.distanceCut);
        }
        if (tooClose) {
          gHit.addRawHit(newHit);
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * Garfield-specific tracking layer numbering:
   * from innermost VXD layer to outermost Tracker layer, barrels first.
   */
  private int getGarfieldLayerID(SimTrackerHit hit) {
    int layer = hit.getLayer();
    Subdetector sys = hit.getSubdetector();
    if (sys == Const.det().VXD_BARREL.subdetector() || sys == Const.det().VXD_ENDCAP.subdetector()) {
      layer = layer; //  EvT changed that on Sep 29th + Const.nLayers[Const.codeVXDBarrel];
    } else if (sys == Const.det().TRACKER_BARREL.subdetector()) {
      layer = layer + Const.det().VXD_BARREL.nLayers(); // EVT + Const.nLayers[Const.codeVXDEndcap];
    } else if (sys == Const.det().TRACKER_ENDCAP.subdetector() || sys == Const.det().TRACKER_FORWARD.subdetector()) {
      layer = layer + Const.det().VXD_BARREL.nLayers()
              + Const.det().TRACKER_BARREL.nLayers(); //  EVT + Const.nLayers[Const.codeVXDEndcap]
      // if this changes, change also GarfieldTrack::initialize
    }
    return layer;
  }
  
  /** Very black magic */
  private double barrelTrackerZ(double z, int end, int layer){
    // layer as in trackerHit 0-4
    double offset;
    if (isEven(layer) || barrelTiling > 50.*Const.cm) offset = 0.;
    else offset = 0.5*barrelTiling;
    double zx = ((int) ((z-offset)/barrelTiling))*barrelTiling+offset;
    double zy;
    if (zx<z) zy = zx+ barrelTiling;
    else zy = zx - barrelTiling;
    if (end == 0) return zx;
    else return zy;
  }
  
  /**
   * Returns true if hit supplied as an argument has information about
   * all three coordinates (this implementation is very detector and
   * package specific).
   */
  private boolean is3D(SimTrackerHit hit){
    return Const.det().is3D(hit.getSubdetector());
  }
  
  private boolean isEven(int i){return ((i % 2) == 0);}
  
  /** Returns true if the hit is in vertex detector. */
  private boolean isVXD(SimTrackerHit hit) {
    return Const.det().isVXD(hit.getSubdetector());
  }
  
// -- Private data :  ----------------------------------------------------------
  
  List<String> inputCollectionNames = new ArrayList<String>(4);  // Names of hit collections to be processed
  String outputCollectionName = "GarfieldHits";
  
  double distanceCut = 200. * Const.micrometer;  // Hits closer to each other than this distance will be merged.
  
  private double barrelTiling = 10. * Const.cm;
  private double endcapTiling = 10. * Const.cm;
  
  private double simpleError = 0.005 * Const.cm; // 50 micron simple error for first tests
  
  private boolean modeFromOutsideIn = true;
  private boolean modeUseVXD = true;
  
}
