package org.lcsim.recon.tracking.vsegment.geom.sensortypes;

import java.util.ArrayList;
import java.util.List;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import org.lcsim.recon.tracking.vsegment.geom.SensorType;

/**
 * This class represents a hexagonal sensor with strips parallel to its side.
 * The reference frame origin is at the center of the sensor.
 *
 * @author D.Onoprienko
 * @version $Id: Hexagon.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public class Hexagon implements SensorType {
  
// -- Constructors :  ----------------------------------------------------------
  
  /**
   * Create <tt>Hexagon</tt> instance.
   *
   * @param sideLength      Length of the hexagon side.
   * @param thickness       Thickness of the sensor.
   * @param nU              Number of divisions along U (measurement direction, perpendicular to strips).
   */
  public Hexagon(double sideLength, double thickness, int nU) {
    _side = sideLength;
    _halfWidth = sideLength*COSPI6;
    _halfThick = thickness/2.;
    _nDivU = nU;
    _pitch = (2.*_halfWidth)/nU;
  }

// -----------------------------------------------------------------------------

  /**
   * Converts a point in local sensor coordinates to channel ID.
   * Returns -1 if the point is outside of sensor sensitive area.
   */
  public int getChannelID(Hep3Vector point) {
    
    if (Math.abs(point.z()) > _halfThick) return -1;

    double u = point.x();
    double v = point.y();
    if (Math.abs(u) > _halfWidth || Math.abs(v) > _side - Math.abs(u)*TANPI6) return -1;
    
    int nU = (int) Math.floor((u+_halfWidth)/_pitch); if (nU == _nDivU) nU--;
    return nU;
  }
  
  /**
   * Returns position of the center of the given channel, in local sensor coordinates.
   */
  public Hep3Vector getChannelPosition(int channelID) {
    double u = (channelID + .5) * _pitch - _halfWidth;
    return new BasicHep3Vector(u,0.,0.);
  }

  /** Returns maximum possible channel ID on this sensor. */
  public int getMaxChannelID() {
    return  _nDivU - 1;
  }
  
  /** Returns <tt>true</tt> if channel with this ID exists on this sensor. */
  public boolean isValidChannelID(int channelID) {
    return channelID > -1 && channelID < _nDivU;
  }
  
  /**
   * Returns dimensions of the given channel along U, V, W.
   */
  public Hep3Vector getChannelDimensions(int channelID) {
    double u = (channelID + .5) * _pitch - _halfWidth;
    double stripLength = 2.* (_side - Math.abs(u)*TANPI6);
    return new BasicHep3Vector(_pitch, stripLength, 2.*_halfThick);
  }
  
  /**
   * Returns the dimension of a measurement by this type of sensor
   * (1 for strips, 2 for pixels, etc). 
   * <tt>Hexagon</tt> always contain strips so this method always returns 1.
   */
  public int getHitDimension() {
    return 1;
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
    int nU = channelID + shiftU;
    return (nU < 0 || nU >= _nDivU || shiftV != 0) ? -1 : nU ;
  }
  
  /** Returns array of IDs of all immediate neighbor channels. */
  public List<Integer> getNeighbors(int channelID) {
    ArrayList<Integer> out = new ArrayList<Integer>(2);
    if (channelID > 0) out.add(channelID - 1);
    if (channelID < (_nDivU - 1)) out.add(channelID + 1);
    return out;
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  protected double _side;
  protected double _halfWidth;
  protected double _halfThick;
  
  protected double _pitch;
  
  protected int _nDivU;
  
  protected final double COSPI6 = Math.sqrt(3.)/2.;
  protected final double TANPI6 = 1./Math.sqrt(3.);
}
