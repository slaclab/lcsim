package org.lcsim.recon.tracking.vsegment.geom.sensortypes;

import java.util.ArrayList;
import java.util.List;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import org.lcsim.recon.tracking.vsegment.geom.SensorType;

/**
 * This class represents a rectangular sensor with strips parallel to its side.
 * The reference frame origin is at the center of the sensor.
 *
 * @author D.Onoprienko
 * @version $Id: Rectangle.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public class Rectangle implements SensorType {
  
// -- Constructors :  ----------------------------------------------------------
  
  /** Default constructor for use by subclusses. No initialization. */
  public Rectangle() {}
  
  /**
   * Create an instance of <tt>Rectangle</tt>.
   * The <tt>center</tt> parameter controls how the local reference frame origin
   * is positioned with respect to the cuboid defining the sensor volume.
   *
   * @param width       Width of the sensor.
   * @param length      Length of the sensor (along strip direction).
   * @param thickness   Thickness of the sensor.
   * @param nWidth      Number of divisions across the sensor width.
   * @param nLength     Number of divisions along the sensor length.
   * @param center      Position of the cuboid center in the local reference frame
   */
  public Rectangle(double width, double length, double thickness, 
                   int nWidth, int nLength, Hep3Vector center) {
    if (nLength == 0) nLength = 1;
    _uLow = center.x() - width/2.;
    _vLow = center.y() - length/2.;
    _wCenter = center.z();
    _pitch = width/nWidth;
    _length = length/nLength;
    _thick = thickness;
    _hitDim = (_length/_pitch < 4.) ? 2 : 1;
    _nDivV = nLength;
    _nDivU = nWidth;
//    System.out.println("Rect w "+width+" len "+length+" thick "+thickness+" nW "+nWidth+" nL "+nLength+" center "+center);
//    System.out.println(" pitch "+_pitch+" length "+_length+" thick "+_thick+" hitDim "+_hitDim);
  }
  
  /**
   * Create <tt>Rectangle</tt> instance.
   * Reference frame origin will be in the center of the cuboid defining the sensor volume.
   *
   * @param width       Width of the sensor.
   * @param length      Length of the sensor (along strip direction).
   * @param thickness   Thickness of the sensor.
   * @param nWidth      Number of divisions across the sensor width.
   * @param nLength     Number of divisions along the sensor length.
   */
  public Rectangle(double width, double length, double thickness, int nWidth, int nLength) {
    this(width, length, thickness, nWidth, nLength, new BasicHep3Vector());
  }
  
  /**
   * Create <tt>Rectangle</tt> instance.
   * The <tt>center</tt> parameter controls how the local reference frame origin
   * is positioned with respect to the cuboid defining the sensor volume.
   * Strip width will be rounded to place integral number of strips on the rectangle.
   *
   * @param width       Width of the sensor.
   * @param length      Length of the sensor (along strip direction).
   * @param thickness   Thickness of the sensor.
   * @param stripPitch  Strip width.
   * @param nLength     Number of divisions along the sensor length.
   * @param center      Position of the cuboid center in the local reference frame
   */
  public Rectangle(double width, double length, double thickness, 
                   double stripPitch, int nLength, Hep3Vector center) {
    this(width, length, thickness, (int)Math.round(width/stripPitch), nLength, center);
  }
  
  /**
   * Create <tt>Rectangle</tt> instance.
   * Reference frame origin will be in the center of the cuboid defining the sensor volume.
   * Strip width will be rounded to place integral number of strips on the rectangle.
   *
   * @param width       Width of the sensor.
   * @param length      Length of the sensor (along strip direction).
   * @param thickness   Thickness of the sensor.
   * @param stripPitch  Strip width.
   * @param nLength     Number of divisions along the sensor length.
   */
  public Rectangle(double width, double length, double thickness, 
                   double stripPitch, int nLength) {
    this(width, length, thickness, (int)Math.round(width/stripPitch), nLength, new BasicHep3Vector());
  }
  
  /**
   * Create <tt>Rectangle</tt> instance.
   * The <tt>center</tt> parameter controls how the local reference frame origin
   * is positioned with respect to the cuboid defining the sensor volume.
   * Strip length will be rounded to place integral number of strips on the rectangle.
   *
   * @param width         Width of the sensor.
   * @param length        Length of the sensor (along strip direction).
   * @param thickness     Thickness of the sensor.
   * @param nWidth        Number of divisions across the sensor width.
   * @param stripLength   Strip length.
   * @param center        Position of the cuboid center in the local reference frame
   */
  public Rectangle(double width, double length, double thickness, 
                   int nWidth, double stripLength, Hep3Vector center) {
    this(width, length, thickness, nWidth, (int)Math.round(length/stripLength), center);
  }
  
  /**
   * Create <tt>Rectangle</tt> instance.
   * Reference frame origin will be in the center of the cuboid defining the sensor volume.
   * Strip length will be rounded to place integral number of strips on the rectangle.
   *
   * @param width         Width of the sensor.
   * @param length        Length of the sensor (along strip direction).
   * @param thickness     Thickness of the sensor.
   * @param nWidth        Number of divisions across the sensor width.
   * @param stripLength   Strip length.
   */
  public Rectangle(double width, double length, double thickness, 
                   int nWidth, double stripLength) {
    this(width, length, thickness, nWidth, (int)Math.round(length/stripLength), new BasicHep3Vector());
  }
  
  /**
   * Create <tt>Rectangle</tt> instance.
   * The <tt>center</tt> parameter controls how the local reference frame origin
   * is positioned with respect to the cuboid defining the sensor volume.
   * Strip width and length will be rounded to place integral number of strips on the rectangle.
   *
   * @param width         Width of the sensor.
   * @param length        Length of the sensor (along strip direction).
   * @param thickness     Thickness of the sensor.
   * @param stripPitch    Strip width.
   * @param stripLength   Strip length.
   * @param center        Position of the cuboid center in the local reference frame
   */
  public Rectangle(double width, double length, double thickness, 
                   double stripPitch, double stripLength, Hep3Vector center) {
    this(width, length, thickness, (int)Math.round(width/stripPitch), (int)Math.round(length/stripLength), center);
  }
  
  /**
   * Create <tt>Rectangle</tt> instance.
   * Reference frame origin will be in the center of the cuboid defining the sensor volume.
   * Strip width and length will be rounded to place integral number of strips on the rectangle.
   *
   * @param width         Width of the sensor.
   * @param length        Length of the sensor (along strip direction).
   * @param thickness     Thickness of the sensor.
   * @param stripPitch    Strip width.
   * @param stripLength   Strip length.
   */
  public Rectangle(double width, double length, double thickness, 
                   double stripPitch, double stripLength) {
    this(width, length, thickness, (int)Math.round(width/stripPitch), (int)Math.round(length/stripLength), new BasicHep3Vector());
  }

// -- Setters :  ---------------------------------------------------------------
  
  /**
   * Set the dimension of a measurement by this type of sensor (1 for strips, 2 for pixels, etc).
   */
  public void setHitDimension(int dim) {
    _hitDim = dim;
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
    
    int nU = (int) Math.floor((u-_uLow)/_pitch); 
    if ((nU < 0) || (nU >= _nDivU)) return -1;
    int nV = (int) Math.floor((v-_vLow)/_length);
    if ((nV < 0) || (nV >= _nDivV)) return -1;
    return nV*_nDivU + nU;
  }
  
  /**
   * Returns position of the center of the given channel, in local sensor coordinates.
   */
  public Hep3Vector getChannelPosition(int channelID) {
    int nU = channelID % _nDivU;
    int nV = channelID / _nDivU;
    double u = (nU + .5) * _pitch + _uLow;
    double v = (nV + .5) * _length + _vLow;
    return new BasicHep3Vector(u,v,_wCenter);
  }

  /** Returns maximum possible channel ID on this sensor. */
  public int getMaxChannelID() {
    return  _nDivU * _nDivV - 1;
  }
  
  /** Returns <tt>true</tt> if channel with this ID exists on this sensor. */
  public boolean isValidChannelID(int channelID) {
    return channelID > -1 && channelID < (_nDivU * _nDivV);
  }
  
  /**
   * Returns dimensions of the given channel along U, V, W.
   */
  public Hep3Vector getChannelDimensions(int channelID) {
    return new BasicHep3Vector(_pitch, _length, _thick);
  }
  
  /**
   * Returns the dimension of a measurement by this type of sensor
   * (1 for strips, 2 for pixels, etc). 
   * By default, this method returns <tt>1</tt> if strip length is at least 4 times
   * bigger than strip pitch. Alternatively, the return value can be set explicitly
   * through a call to {@link #setHitDimension(int)}.
   */
  public int getHitDimension() {
    return _hitDim;
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
    int nU = (channelID % _nDivU) + shiftU;
    if ((nU < 0) || (nU >= _nDivU)) return -1;
    int nV = (channelID / _nDivU) + shiftV;
    if ((nV < 0) || (nV >= _nDivV)) return -1;
    return nV*_nDivU + nU;
  }
  
  /** Returns array of IDs of all immediate neighbor channels. */
  public List<Integer> getNeighbors(int channelID) {
    int nU = channelID % _nDivU;
    int nV = channelID / _nDivU;
    ArrayList<Integer> out = new ArrayList<Integer>(8);
    int vDown = ((_hitDim == 2) && (nV > 0)) ? nV-1 : nV;
    int vUp = ((_hitDim == 2) && (nV < _nDivV-1)) ? nV+1 : nV;
    int uDown = (nU > 0) ? nU-1 : nU;
    int uUp = (nU < _nDivU-1) ? nU+1 : nU;
    for (int iV = vDown; iV < vUp; iV++) {
      for (int iU = uDown; iU < uUp; iU++) {
        out.add(nV*_nDivU + nU);
      }
    }
    return out;
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  protected double _uLow;
  protected double _vLow;
  protected double _wCenter;
  
  protected double _pitch;
  protected double _length;
  protected double _thick;
  
  protected int _hitDim;
  
  protected int _nDivU;
  protected int _nDivV;
}
