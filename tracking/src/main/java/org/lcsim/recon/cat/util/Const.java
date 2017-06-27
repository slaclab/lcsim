package org.lcsim.recon.cat.util;

import java.util.*;

import org.lcsim.conditions.ConditionsListener;
import org.lcsim.conditions.ConditionsEvent;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsManager.ConditionsSetNotFoundException;
import org.lcsim.event.*;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.geometry.*;
import org.lcsim.util.Driver;

/**
 * The class provides access to general use constants and SiD-specific detector information.
 * <p>
 * Units, physics constants and frequently used particles PGD encodings are defined
 * as static fields. Default units (those equal to 1.0) are millimeter, second, Tesla, and GeV.
 * <p>
 * Detector-specific constants are accessible through an instance of <tt>Const</tt>
 * that can be obtained with a call to <tt>Const.det()</tt>.
 * These constants are initialized automatically once the detector information becomes 
 * available, and updated if the detector changes. An attempt to call {@link #det()} 
 * before the detector classes have been initialized by the framework (usually just 
 * before the first event processing starts) will throw <tt>NullPointerException</tt>.
 *
 * @author D. Onoprienko
 * @version $Id: Const.java,v 1.1 2007/04/06 21:48:15 onoprien Exp $
 */
public class Const implements ConditionsListener {
  
// --- Units :  ----------------------------------------------------------------
  
  public static final double mm = 1.;
  public static final double millimeter = mm;
  public static final double meter = 1000. * mm;
  public static final double m = 1000. * mm;
  public static final double centimeter = 10. * mm;
  public static final double cm = 10. * mm;
  public static final double micrometer = 0.001 * mm;
  
  public static final double second = 1.;
  public static final double sec = second;
  public static final double millisecond = 1.e-3 * second;
  public static final double microsecond = 1.e-6 * second;
  public static final double nanosecond = 1.e-9 * second;
  
  public static final double Tesla = 1.;
  
  public static final double GeV = 1.;
  public static final double TeV = 1.e3 * GeV;
  public static final double MeV = 1.e-3 * GeV;
  public static final double eV = 1.e-9 * GeV;
  
  public static final double degree = Math.PI / 180.;
  
// -- Physics constants :  -----------------------------------------------------
  
  public static final double SPEED_OF_LIGHT = 2.99792458e8 * (meter/second);
  
// -- Frequently used particles :  ---------------------------------------------
  
  public enum Particle {
    
    PI_PLUS(211),
    PI_MINUS(-211),
    PI_0(111),
    K_SHORT(310),
    LAMBDA(3122),
    SIGMA_PLUS(3222),
    SIGMA_MINUS(3112),
    SIGMA_0(3212);
    
    public final int PDGID;    
    private Particle(int pdgID) {PDGID = pdgID;}
  }
  
// -- Detector parameters :  ---------------------------------------------------
  
  /** Returns an instance of <tt>Const</tt> that can be used to retrieve various detector parameters. */
  static public Const det() {return _det;}
  
  public final SubDet VXD_BARREL;
  public final SubDet VXD_ENDCAP;
  public final SubDet TRACKER_BARREL;
  public final SubDet TRACKER_ENDCAP;
  public final SubDet TRACKER_FORWARD;
  
  /** 
   * Returns <tt>true</tt> if tracking subdetector supplied as an argument is an endcap.
   */
  public boolean isEndcap(Subdetector sub) {
    return get(sub).isEndcap();
  }
  /**
   * Returns <tt>true</tt> if tracking subdetector with system ID supplied as an
   * argument is an endcap.
   */
  public boolean isEndcap(int sysID) {
    return get(sysID).isEndcap();
  }
  
  /** 
   * Returns <tt>true</tt> if tracking subdetector supplied as an argument 
   * is a part of the vertex detector.
   */
  public boolean isVXD(Subdetector sub) {
    return get(sub).isVXD();
  }
  /**
   * Returns <tt>true</tt> if tracking subdetector with system ID supplied as an
   * argument is a part of the vertex detector.
   */
  public boolean isVXD(int sysID) {
    return get(sysID).isVXD();
  }
  
  /** 
   * Returns <tt>true</tt> if tracking subdetector supplied as an argument provides
   * 3-demensional measurement (silicon pixels).
   */
  public boolean is3D(Subdetector sub) {
    return get(sub).is3D();
  }
  /**
   * Returns <tt>true</tt> if tracking subdetector with system ID supplied as an
   * argument provides 3-demensional measurement (silicon pixels).
   */
  public boolean is3D(int sysID) {
    return get(sysID).is3D();
  }
  
  /** 
   * Returns number of layers in the tracking subdetector supplied as an argument.
   */
  public int nLayers(Subdetector sub) {
    return get(sub).nLayers();
  }
  /**
   * Returns number of layers in the tracking subdetector with system ID supplied as an argument.
   */
  public int nLayers(int sysID) {
    return get(sysID).nLayers();
  }
  
  /** Helper class that contains parameters of one of the tracking subdetectors. */
  public class SubDet {
    
    public String name() {return _name;}
    public Subdetector subdetector() {return _subd;}
    public int sysID() {return _sysID;}
    public int nLayers() {return _nLayers;}
    public boolean isEndcap() {return _isEndcap;}
    public boolean isVXD() {return _isVXD;}
    public boolean is3D() {return _is3D;}
    
    private SubDet(String name, boolean isEndcap, boolean isVXD, boolean is3D) {
      _name = name;
      _isEndcap = isEndcap;
      _isVXD = isVXD;
      _is3D = is3D;
      _subDetList.add(this);
    }

    private void initialize() {
      _subd = _detector.getSubdetector(_name);
      if (_subd == null) {
        _sysID = -1;
        _nLayers = 0;
      } else {
        _sysID = _subd.getSystemID();
        _nLayers = _subd.getLayering().getNumberOfLayers();
      }
    }

    private String _name;
    private Subdetector _subd;
    private int _sysID;
    private int _nLayers;
    private boolean _isEndcap;
    private boolean _isVXD;
    private boolean _is3D;
  }

  // -- Detector-dependent initialization :  -----------------------------------
  
  static private Const _det = new Const();

  private Const() {
    _subDetList = new ArrayList<SubDet>(5);
    VXD_BARREL =      new SubDet("VertexBarrel",  false,  true,  true);
    VXD_ENDCAP =      new SubDet("VertexEndcap",   true,  true,  true);
    TRACKER_BARREL =  new SubDet("TrackerBarrel", false, false, false);
    TRACKER_ENDCAP =  new SubDet("TrackerEndcap",  true, false, false);
    TRACKER_FORWARD = new SubDet("TrackerForward", true, false, false);
    ConditionsManager.defaultInstance().addConditionsListener(this);
    conditionsChanged(null);
  }
  
  /** Called by the framework when the detector information changes. */
  public void conditionsChanged(ConditionsEvent event) {
    ConditionsManager conMan = (event == null) ? ConditionsManager.defaultInstance() : event.getConditionsManager();
    try {
      Detector det = conMan.getCachedConditions(Detector.class,"compact.xml").getCachedData();
      if (det != _detector) {
        _detector = det;
        _bField = _detector.getFieldMap().getField(new double[]{0.,0.,0.})[2];
        for (SubDet sd : _subDetList) sd.initialize();
      }
    } catch (ConditionsSetNotFoundException x) {}
  }
  
  // -- Private parts :  -------------------------------------------------------

  private SubDet get(Subdetector subdetector) {
    for (SubDet sd : _subDetList) if (sd._subd == subdetector) return sd;
    return null;
  }
  private SubDet get(int sysID) {
    for (SubDet sd : _subDetList) if (sd._sysID == sysID) return sd;
    return null;
  }
  
  private Detector _detector;
  private ArrayList<SubDet> _subDetList;
  private double _bField;

}
