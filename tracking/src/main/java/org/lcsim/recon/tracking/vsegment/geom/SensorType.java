package org.lcsim.recon.tracking.vsegment.geom;

import java.util.List;

import hep.physics.vec.Hep3Vector;

/**
 * Any class that implements this interface defines a particular shape of silicon
 * sensor and its segmentation into strips or pixels.
 * Current design assumes that all strips are parallel to each other, each strip or 
 * pixel has no more than one neighbor in each direction. Reference frame on a sensor: 
 * (U, V, W). U is the measurement direction (in the sensor plane, perpendicular to 
 * strips), V is along the strips, W forms right-nahded system.
 *
 * @author D.Onoprienko
 * @version $Id: SensorType.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public interface SensorType {

  /**
   * Converts a point in local sensor coordinates to channel ID.
   * Returns -1 if the point is outside of sensor sensitive area.
   */
  public int getChannelID(Hep3Vector point);
  
  /**
   * Returns position of the center of a given channel, in local sensor coordinates.
   */
  public Hep3Vector getChannelPosition(int channelID);
  
  /** Returns maximum channel ID on this sensor. */
  public int getMaxChannelID();
  
  /** Returns <tt>true</tt> if channel with this ID exists on this sensor. */
  public boolean isValidChannelID(int channelID);
  
  /** Returns dimensions of a given channel along U, V, W. */
  public Hep3Vector getChannelDimensions(int channelID);
  
  /**
   * Returns the dimension of a measurement by this type of sensor
   * (1 for strips, 2 for pixels, etc). Note that algorithms can ignore this method
   * and use {@link #getChannelDimensions(int)} to decide how to treat measurements
   * by the given sensor. However, {@link #getNeighbors(int)} should only look for 
   * neighbors in <tt>U</tt> direction if this method returns <tt>1</tt>.
   */
  public int getHitDimension();
  
  /**
   * Returns ID of a channel obtained by shifting the given channel by the given
   * number of channels in the given direction along the local reference frame axis. 
   * Returns <tt>-1</tt> if shifting puts the point outside of the sensor boundaries.
   * Throws <tt>IllegalArgumentException</tt> if this type of sensor does not have a
   * concept of a neighbor in the given direction. 
   *
   * @param channelID  ID of the original channel
   * @param shiftV     move in <tt>V</tt> direction by <tt>shiftV</tt> channels
   * @param shiftU     move in <tt>U</tt> direction by <tt>shiftU</tt> channels
   */
  public int getNeighbor(int channelID, int shiftU, int shiftV);
  
  /**
   * Returns a list of IDs of all immediate neighbor channels.
   */
  public List<Integer> getNeighbors(int channelID);
  
}
