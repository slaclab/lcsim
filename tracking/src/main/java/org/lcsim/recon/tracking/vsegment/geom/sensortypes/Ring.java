package org.lcsim.recon.tracking.vsegment.geom.sensortypes;

import java.util.*;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

/**
 * Class to represent a disk sensor divided into rectangular strips or pixels.
 * Reference frame origin is at the center of the disk. 
 *
 * {@link #getChannelID(Hep3Vector)} method returns <tt>-1</tt> if and only if the 
 * center of the rectangle (strip) to which the point belongs is ouside the ring.
 *
 * @author D.Onoprienko
 * @version $Id: Ring.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public class Ring extends Rectangle {
  
// -- Constructors :  ----------------------------------------------------------
  
  /**
   * Create <tt>Ring</tt> instance.
   *
   * @param radiusMin   Inside radius of the ring.
   * @param radiusMax   Outside radius of the ring.
   * @param pitch       Width of a strip.
   * @param length      Length of a strip.
   * @param thickness   Thickness of the sensor.
   */
  public Ring(double radiusMin, double radiusMax, double pitch, double length, double thickness) {
    super(pitch*Math.ceil(2.*radiusMax/pitch), length*Math.ceil(2.*radiusMax/length), thickness, pitch, length);
    _rMin = radiusMin;
    _rMax = radiusMax;
  }
  
// -----------------------------------------------------------------------------

  /**
   * Converts a point in local sensor coordinates to channel ID.
   * Returns -1 if the point is outside of sensor sensitive area.
   */
  public int getChannelID(Hep3Vector point) {
    int channel = super.getChannelID(point);
    return isValidChannelID(channel) ? channel : -1;
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
    int neighbor = super.getNeighbor(channelID, shiftU, shiftV);
    return isValidChannelID(neighbor) ? neighbor : -1;
  }
  
  /** Returns array of IDs of all immediate neighbor channels. */
  public List<Integer> getNeighbors(int channelID) {
    List<Integer> raw = super.getNeighbors(channelID);
    List<Integer> out = new ArrayList<Integer>(raw.size());
    for (int neighbor : raw) {
      if (isValidChannelID(neighbor)) out.add(neighbor);
    }
    return out;
  }
  
// -- Helper methods :  --------------------------------------------------------

  /**
   * Returns <tt>true</tt> if the center of the channel is inside the ring.
   */
  public boolean isValidChannelID(int channelID) {
    if (!super.isValidChannelID(channelID)) return false;
    double r = getChannelPosition(channelID).magnitude();
    return r > _rMin && r < _rMax;
  }

// -- Private parts :  ---------------------------------------------------------
  
  protected double _rMin;
  protected double _rMax;
  
}
