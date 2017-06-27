package org.lcsim.recon.tracking.vsegment.hitmaking;

import java.util.*;

import hep.aida.*;
import org.lcsim.util.aida.AIDA;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;
import org.lcsim.event.EventHeader;
import org.lcsim.recon.cat.util.NoSuchParameterException;
import org.lcsim.spacegeom.SpacePointVector;
import org.lcsim.util.Driver;

import org.lcsim.recon.tracking.vsegment.geom.RegionSegmenter;
import org.lcsim.recon.tracking.vsegment.geom.SegmentationManager;
import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.geom.SensorType;
import org.lcsim.recon.tracking.vsegment.hit.TrackerCluster;
import org.lcsim.recon.tracking.vsegment.hit.TrackerHit;
import org.lcsim.recon.tracking.vsegment.hitmaking.hitmakers.TrackerHitMakerBasic;

/**
 * Driver that converts a collection of {@link TrackerCluster} or {@link TrackerHit}
 * objects into a collection of org.lcsim.event.{@link TrackerHit} objects.
 * <p>
 * See the description of {@link #set} method for details on how to specify input and
 * output collections. If this Driver is run on <tt>TrackerClusters</tt>, it first generates
 * <tt>TrackerHits</tt> internally, using the {@link TrackerHitMaker} set through a call to 
 * <tt>set("HIT_MAKER", hitMaker)</tt>, and then converts them into <tt>OldTrackerHits</tt>.
 * <p>
 * Creation of <tt>OldTrackerHit</tt> objects is done either by {@link #convert(TrackerHit hit)}
 * (for pixels and sensors for which <tt>getStereoPartners(Sensor)</tt> method of the 
 * corresponding <tt>Segmenter</tt> returns <tt>null</tt>) or by
 * {@link #cross(TrackerHit hit1, TrackerHit hit2)} method. A user can override either of these
 * methods to control what position and covariance matrix are assigned to newly created
 * <tt>OldTrackerHits</tt>.
 * <p>
 * If <tt>getStereoPartners(Sensor)</tt> method returns an empty list (default behavior
 * of {@link RegionSegmenter}) for a given <tt>Sensor</tt> object, and that Sensor contains
 * strips, no <tt>OldTrackerHits</tt> will be produced from clusters on that sensor.
 *
 * @author D. Onoprienko
 * @version $Id: TrackerHitConverter.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public class TrackerHitConverter extends Driver {
  
// -- Constructors :  ----------------------------------------------------------
  
  public TrackerHitConverter() {
    _notInit = true;
    _hitMapName = null;
    _clusterMapName = "TrackerClusters";
    _outListName = "StandardTrackerHits";
  }
  
  private void init(EventHeader event) {
    _notInit = false;
    if (_segMan == null) _segMan = (SegmentationManager) event.get("SegmentationManager");
    if (_clusterMapName != null) {
      List<Driver> drivers = drivers();
      ListIterator<Driver> it = drivers.listIterator();
      while (it.hasNext()) {
        if (it.next() instanceof HitMakingDriver) it.remove();
      }
      if (_hitMaker == null) _hitMaker = new TrackerHitMakerBasic();
      HitMakingDriver hitMakingDriver = new HitMakingDriver(_hitMaker);
      hitMakingDriver.set("INPUT_MAP_NAME",_clusterMapName);
      hitMakingDriver.set("OUTPUT_MAP_NAME", (_hitMapName == null) ? "_temporary_" : _hitMapName);
      add(hitMakingDriver);
    }
    
  }

// -- Setters :  ---------------------------------------------------------------

  /**
   * Set any <tt>String</tt> parameter. 
   * The following parameters can be set with this method:
   * <p><dl>
   * <dt>"INPUT_HIT_MAP_NAME"</dt> <dd>Name of input collection of tracker hits
   *             (type <tt>HashMap&lt;Sensor, ArrayList&lt;TrackerHit&gt;&gt;</tt>). 
   *             <br>Default: not set - clusters will be used as input.</dd>
   * <dt>"INPUT_CLUSTER_MAP_NAME"</dt> <dd>Name of input collection of tracker clusters
   *             (type <tt>HashMap&lt;Sensor, ArrayList&lt;TrackerCluster&gt;&gt;</tt>).
   *             If both input hit map name and input cluster map name are set, 
   *             a map of <tt>TrackerHits</tt> will be produced and saved into the event
   *             under the given name.
   *             <br>Default: "TrackerClusters".</dd>
   * <dt>"OUTPUT_HIT_LIST_NAME"</dt> <dd>Name of output collection of tracker hits
   *             (type <tt>List&lt;org.lcsim.event.TrackerHit&gt;</tt>). 
   *             <br>Default: "StandardTrackerHits".</dd>
   * <dt>"HIT_MAKER"</dt> <dd>{@link TrackerHitMaker} object to be used by this converter. 
   *             <br>Default: New instance of {@link TrackerHitMakerBasic} will be used
   *             if the hit maker is not set explicitly.</dd></dl>
   * 
   * @param name   Name of parameter to be set. Case is ignored.
   * @param value  Value to be assigned to the parameter.
   * @throws NoSuchParameterException Thrown if the supplied parameter name is unknown.
   *         Subclasses may catch this exception after a call to <tt>super.set()</tt>
   *         and set their own parameters.
   */
  public void set(String name, Object value) {
    try {
      if (name.equalsIgnoreCase("INPUT_HIT_MAP_NAME")) {
        _hitMapName = (String) value;
      } else if (name.equalsIgnoreCase("INPUT_CLUSTER_MAP_NAME")) {
        _clusterMapName = (String) value;
      } else if (name.equalsIgnoreCase("OUTPUT_HIT_LIST_NAME")) {
        _outListName = (String) value;
      } else if (name.equalsIgnoreCase("HIT_MAKER")) {
        _hitMaker = (TrackerHitMaker) value;
      } else {
        throw new NoSuchParameterException(name, this.getClass());
      }
    } catch (ClassCastException x) {
      throw new IllegalArgumentException("Value of incompatible type", x);
    }
  }
  
// -- Event processing :  ------------------------------------------------------
  
  public void process(EventHeader event) {
    
    if (_notInit) init(event);
    
//    System.out.println(" ");
//    System.out.println("Starting conversion of TrackerHits into TrackerHits");
    
    super.process(event);

    String hitMapName = (_hitMapName == null) ? "_temporary_" : _hitMapName;
    HashMap<Sensor, ArrayList<TrackerHit>> hitMap = (HashMap<Sensor, ArrayList<TrackerHit>>) event.get(hitMapName);
    ArrayList<OldTrackerHit> out = new ArrayList<OldTrackerHit>(1000);
    ArrayList<Sensor> processedSensors = new ArrayList<Sensor>(hitMap.size());
    
    for (Sensor sensor : hitMap.keySet()) {
      
      List<TrackerHit> hitsOnSensor = hitMap.get(sensor);
      int hitDimension = sensor.getType().getHitDimension();
      
      if (hitDimension == 1) { // Strips
        List<Sensor> partners = _segMan.getStereoPartners(sensor);
        if (partners == null) {
          for (TrackerHit hit : hitsOnSensor) {
            OldTrackerHit oHit = convert(hit);
            if (oHit != null) out.add(oHit);
          }
        } else {
          for (Sensor partner : partners) {
            Hep3Vector shift = VecOp.sub(sensor.getTranslation(), partner.getTranslation());
            if (! processedSensors.contains(partner)) {
              List<TrackerHit>  hitsOnPartner = hitMap.get(partner);
              if (hitsOnPartner != null) {
                for (TrackerHit hitOnSensor : hitsOnSensor) {
                  for (TrackerHit hitOnPartner : hitsOnPartner) {
                    OldTrackerHit hit = cross(hitOnSensor, hitOnPartner);
                    if (hit != null) out.add(hit);
                  }
                }
              }
            }
          }
          processedSensors.add(sensor);
        }
      } else if (hitDimension == 2) { // Pixels
        for (TrackerHit hit : hitsOnSensor) {
          OldTrackerHit oHit = convert(hit);
          if (oHit != null) out.add(oHit);
        }
      } else {
        throw new RuntimeException("Unknown hit dimension " + hitDimension);
      }
    }
    
    out.trimToSize();
    if (_hitMapName == null) event.remove("_temporary_");
    event.put(_outListName, out);
  }
  
  /**
   * Creates a new <tt>OldTrackerHit</tt> from a pair of <tt>TrackerHits</tt> in stereo layers.
   * <p>
   * The implementation assumes that hits provided as arguments are both segment-like, and 
   * belong to parallel <tt>Sensors</tt> (local reference frame W axes parallel) 
   * with non-parallel strips (U axes are not parallel). If any of these assumptions is
   * not true, <tt>null</tt> will be returned and error message printed. Otherwise, the
   * method will try to find an intersection in U,V plane (ignoring W difference between 
   * the sensors). If successful, the returned <tt>OldTrackerHit</tt> will have U and V
   * coordinates corresponding to that intersection, and W coordinate will be half way
   * between sensor planes. If hits do not cross, the method returns <tt>null</tt>.
   */
  protected OldTrackerHit cross(TrackerHit hit1, TrackerHit hit2) {
    
    Sensor sensor1 = hit1.getSensor();
    double tolerance = _stereoTolerance * hit2.getLength();
    
    SpacePointVector globSegment2 = hit2.getSegment();
    Hep3Vector locStart2 = sensor1.globalToLocal(globSegment2.getStartPoint());
    Hep3Vector locEnd2 = sensor1.globalToLocal(globSegment2.getEndPoint());
    SpacePointVector locSegment1 = hit1.getLocalSegment();
    Hep3Vector locStart1 = locSegment1.getStartPoint();
    Hep3Vector locEnd1 = locSegment1.getEndPoint();
    
    double du = locEnd2.x() - locStart2.x();
    double dw = Math.abs(locEnd2.z() - locStart2.z());
    if (Math.abs(du) < tolerance) {
      System.out.println("Shallow stereo angle");
      return null;
    } else if (dw > tolerance ) {
      System.out.println("Non-parallel stereo partners");
      return null;
    }
    double u = locStart1.x();
    double v = locStart2.y() - ((locStart2.x()-u) * (locEnd2.y()-locStart2.y())) / du;
    
    if (((v - locStart1.y()) * (v - locEnd1.y())) > 0.) return null;  // not intersecting
   
    Hep3Vector locPos = new BasicHep3Vector(u, v, (locEnd2.z() + locEnd1.z())/2.);
    
    Hep3Vector locUnitU2 = sensor1.globalToLocal(hit2.getSensor().localToGlobal(unitU));
    double a = locUnitU2.x();
    double b = locUnitU2.y();
    double sigma1 = hit1.getCovMatrix().diagonal(0);
    double sigma2 = hit2.getCovMatrix().diagonal(0);
    double[] cov = new double[]{sigma1+a*a*sigma2, a*b*sigma2, b*b*sigma2, 0., 0., _errFlat*dw};
    double s1 = hit1.getSignal();
    double s2 = hit2.getSignal();
    double signal = s1 + s2;
    double time = (s1*hit1.getTime() + s2*hit2.getTime()) / signal;
    ArrayList<TrackerCluster> parents = new ArrayList<TrackerCluster>(2);
    parents.add(hit1.getCluster());
    parents.add(hit2.getCluster());
    
    return new OldTrackerHit(sensor1.localToGlobal(locPos), new SymmetricMatrix(3,cov,true), signal, time, 0, parents);
  }
  
  /**
   * Creates a new <tt>OldTrackerHit</tt> given <tt>TrackerHit</tt>.
   * Position and covariance matrix of the created <tt>OldTrackerHit</tt> are
   * global position and covariance matrix of the <tt>TrackerHit</tt> (if the 
   * supplied <tt>TrackerHit</tt> is segment-like, that places the newly created
   * <tt>TrackerHit</tt> at the center of the segment. 
   */
  protected OldTrackerHit convert(TrackerHit hit) {
    ArrayList<TrackerCluster> parents = new ArrayList<TrackerCluster>(1);
    parents.add(hit.getCluster());
    return new OldTrackerHit(hit.getPosition(), hit.getCovMatrix(), hit.getSignal(), hit.getTime(), 0, parents);
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  private boolean _notInit;

  private String _hitMapName;
  private String _clusterMapName;
  private String _outListName;
  
  protected SegmentationManager _segMan;
  private TrackerHitMaker _hitMaker;
  
  private double _stereoTolerance = 0.01;
  
  private double _errFlat = 1./Math.sqrt(12.);
  private Hep3Vector unitU = new BasicHep3Vector(1., 0., 0.);  
}
