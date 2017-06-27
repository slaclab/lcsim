package org.lcsim.recon.tracking.vsegment.geom.sensortypes;

import java.util.ArrayList;
import java.util.List;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import org.lcsim.recon.tracking.vsegment.geom.SensorType;

/**
 * This class represents a cylindrical sensor with strips parallel to its axis.
 * The reference frame is cylindrical (U,V,W = Phi,Z,R), with the position of origin 
 * controled by <tt>Hep3Vector center</tt> parameter given to a constructor.
 * If <tt>center = (Phi0,Z0,R0)</tt>, then the sensor volume of the cylinder will be defined by 
 * <tt><nobr>Phi0-PI < u < Phi0+PI ,</nobr> 
 * <nobr>Z0-length < v < Z0+length ,</nobr> 
 * <nobr>R0-thickness/2 < w < R0+thickness/2</nobr></tt>. 
 * Constructors that do not require <tt>center</tt> parameter assume
 * <nobr><tt>center = (0.,0.,radius)</tt></nobr>,
 * placing the local reference frame origin in the center of the detector with 
 * <tt>phi</tt> ranging from <tt>-PI</tt> to <tt>PI</tt>.
 *
 * @author D.Onoprienko
 * @version $Id: Cylinder.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public class Cylinder extends Rectangle {
  
// -- Constructors :  ----------------------------------------------------------
  
  /**
   * Create <tt>Cylinder</tt> instance.
   * The <tt>center</tt> parameter controls offsets in the local reference frame.
   *
   * @param length      Length of the cylinder (along strip direction).
   * @param radius      Radius of the cylinder.
   * @param thickness   Thickness of the sensor.
   * @param nLength     Number of divisions along the cylinder length.
   * @param nPhi        Number of divisions in phi.
   * @param center      Controls definition of the local reference frame
   */
  public Cylinder(double radius, double length, double thickness, int nPhi, int nLength, Hep3Vector center) {
    super(TWOPI, length, thickness, nPhi, nLength, center);
    _hitDim = ((length/nLength)/(TWOPI*radius/nPhi) < 4.) ? 2 : 1;
  }
  
  /**
   * Create <tt>Cylinder</tt> instance.
   *
   * @param length      Length of the cylinder (along strip direction).
   * @param radius      Radius of the cylinder.
   * @param thickness   Thickness of the sensor.
   * @param nLength     Number of divisions along the cylinder length.
   * @param nPhi        Number of divisions in phi.
   */
  public Cylinder(double radius, double length, double thickness, int nPhi, int nLength) {
    this(radius, length, thickness, nPhi, nLength, new BasicHep3Vector(0.,0.,radius));
  }
  
  /**
   * Create <tt>Cylinder</tt> instance.
   * Strip width will be adjusted to make sure integral number of strips fits the 
   * circumference of the cylinder.
   *
   * @param length       Length of the cylinder (along strip direction).
   * @param radius       Radius of the cylinder.
   * @param thickness    Thickness of the sensor.
   * @param stripPitch   Strip width.
   * @param stripLength  Strip length.
   * @param center       Controls definition of the local reference frame
   */
  public Cylinder(double radius, double length, double thickness, double stripPitch, double stripLength, Hep3Vector center) {
    this(radius, length, thickness, (int) Math.round((TWOPI*radius)/stripPitch), (int) Math.round(length/stripLength), center);
  }
  
  /**
   * Create <tt>Cylinder</tt> instance.
   * Strip width will be adjusted to make sure integral number of strips fits the 
   * circumference of the cylinder.
   *
   * @param length       Length of the cylinder (along strip direction).
   * @param radius       Radius of the cylinder.
   * @param thickness    Thickness of the sensor.
   * @param stripPitch   Strip width.
   * @param stripLength  Strip length.
   */
  public Cylinder(double radius, double length, double thickness, double stripPitch, double stripLength) {
    this(radius, length, thickness, stripPitch, stripLength, new BasicHep3Vector(0.,0.,radius));
  }

// -----------------------------------------------------------------------------

  /**
   * Converts a point in local sensor coordinates to channel ID.
   * Returns -1 if the point is outside of sensor sensitive area.
   */
  public int getChannelID(Hep3Vector point) {
    
    if (Math.abs(point.z()-_wCenter) > _thick/2.) return -1;

    double u = point.x();
    double v = point.y();

    int nV = (int) Math.floor((v-_vLow)/_length);
    if ((nV < 0) || (nV >= _nDivV)) return -1;

    int nU = (int) Math.floor((u-_uLow)/_pitch); 
    while (nU < 0) nU += _nDivU;
    while (nU >= _nDivU) nU -= _nDivU;

    return nV*_nDivU + nU;
  }
  
  /**
   * Returns channel ID of a neighbor channel.
   * Returns -1 if the channel defined by shifts does not exist on this sensor.
   *
   * @param channelID  ID of the original channel
   * @param shiftV     move in <tt>V</tt> direction by <tt>shiftV</tt> channels
   * @param shiftU     move in <tt>U</tt> direction by <tt>shiftU</tt> channels
   */
  public int getNeighbor(int channelID, int shiftU, int shiftV) {
    int nV = (channelID / _nDivU) + shiftV;
    if (nV < 0 || nV >= _nDivV) return -1;
    int nU = (channelID % _nDivU) + shiftU;
    while (nU < 0) nU += _nDivU;
    while (nU >= _nDivU) nU -= _nDivU;
    return nV*_nDivU + nU;
  }
  
  /** 
   * Returns array of IDs of all immediate neighbor channels. 
   * For strips ({@link #getHitDimension()} returns 1), this method looks for neighbors
   * in U direction only. Therefore, each strip has 1 or 2 neighbors. For pixels
   * ({@link #getHitDimension()} returns 2), up to 8 neighbors can be found.
   */
  public List<Integer> getNeighbors(int channelID) {
    int nU = channelID % _nDivU;
    int nV = channelID / _nDivU;
    ArrayList<Integer> out = new ArrayList<Integer>(8);
    int vDown = ((_hitDim == 2) && (nV > 0)) ? nV-1 : nV;
    int vUp = ((_hitDim == 2) && (nV < _nDivV-1)) ? nV+1 : nV;
    for (int iV = vDown; iV < vUp; iV++) {
      for (int iU = nU-1; iU < nU+1; iU++) {
        while (nU < 0) nU += _nDivU;
        while (nU >= _nDivU) nU -= _nDivU;
        out.add(nV*_nDivU + nU);
      }
    }
    return out;
  }

// -- Private parts :  ---------------------------------------------------------
  
  static final double TWOPI = 2.*Math.PI;
}
