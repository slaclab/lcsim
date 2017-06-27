package org.lcsim.recon.tracking.vsegment.geom.sensortypes;

import java.util.*;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import org.lcsim.recon.tracking.vsegment.geom.SensorType;

/**
 *
 * @author D.Onoprienko
 * @version $Id: WedgeSideParallel.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public class WedgeSideParallel implements SensorType {
  
// -- Constructors :  ----------------------------------------------------------
  
  public WedgeSideParallel(boolean left, double shortBase, double longBase, double angle, double pitch, double thickness) {
    _left = left;
    _thickness = thickness;
    _pitch = pitch;
    _tan = Math.tan(angle/2.);
    _cotan2 = 1./Math.tan(angle);
    _side = ((longBase - shortBase)/2.)/Math.sin(angle/2.);
    _maxID = (int) Math.floor((longBase * Math.cos(angle/2.))/pitch);
    _uCorner = shortBase * Math.cos(angle/2.);
    _offset = (left) ? 0. : (_maxID * pitch) - _uCorner;
    _vConst = - (shortBase/2.) / Math.sin(angle/2.);
    
  }

  public WedgeSideParallel(double rMin, double side, double angle, double pitch, boolean left, double thickness) {
    _left = left;
    _thickness = thickness;
    _side = side;
    _tan = Math.tan(angle/2.);
    _cotan2 = 1./Math.tan(angle);
    _pitch = pitch;
    _maxID = (int) Math.floor(((rMin+side)*Math.sin(angle))/pitch);
    _uCorner = rMin * Math.sin(angle);
    _offset = (left) ? 0. : (_maxID * pitch) - _uCorner;
    _vConst = - rMin;
  }
// -----------------------------------------------------------------------------

  /**
   * Converts a point in local sensor coordinates to channel ID.
   * Returns -1 if the point is outside of sensor sensitive area.
   */
  public int getChannelID(Hep3Vector point) {
    double u = point.x();
    int channel = (int) Math.floor((u + _offset) / _pitch);
    return ((channel < 0) || (channel > _maxID)) ? -1 : channel;
  }
  
  /**
   * Returns position of the center of a given channel, in local sensor coordinates.
   */
  public Hep3Vector getChannelPosition(int channelID) {
    double u = ((channelID + .5) * _pitch) - _offset;
    double v;
    if (_left) {
      if (u < _uCorner) {
        v = _side/2. - u * _tan;
      } else {
        v = ((_side - u * _tan) + (u * _cotan2 + _vConst) )/2.;
      }
    } else {
      if (u < 0.) {
        v = (_side + u * _tan - u * _cotan2 )/2.;
      } else {
        v = _side/2. + u * _tan;
      }
    }
    return new BasicHep3Vector(u,v,0.);
  }
  
  /** Returns maximum channel ID on this sensor. */
  public int getMaxChannelID() {
    return _maxID;
  }
  
  /** Returns <tt>true</tt> if channel with this ID exists on this sensor. */
  public boolean isValidChannelID(int channelID) {
    return channelID > -1 && channelID <= _maxID;
  }
  
  /** Returns dimensions of a given channel along U, V, W. */
  public Hep3Vector getChannelDimensions(int channelID) {
    double u = ((channelID + .5) * _pitch) - _offset;
    double length;
    if (_left) {
      if (u < _uCorner) {
        length = _side;
      } else {
        length = (_side - u * _tan) - (u * _cotan2 + _vConst);
      }
    } else {
      if (u < 0.) {
        length = (_side + u * _tan) + u * _cotan2 ;
      } else {
        length = _side;
      }
    }
    if (length < 0.) System.out.println("U: "+u+" left "+_left);
    return new BasicHep3Vector(_pitch, length, _thickness);
    
  }
  
  /**
   * Returns the dimension of a measurement by this type of sensor (1 since 
   * <tt>WedgeSideParallel</tt> is always tiled with strips).
   */
  public int getHitDimension() {return 1;}
  
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
  public int getNeighbor(int channelID, int shiftU, int shiftV) {
    if (shiftV != 0) return -1;
    int channel = channelID + shiftU;
    return ((channel < 0) || (channel > _maxID)) ? -1 : channel;
  }
  
  /**
   * Returns array of IDs of all immediate neighbor channels.
   */
  public List<Integer> getNeighbors(int channelID) {
    ArrayList<Integer> out = new ArrayList<Integer>(2);
    if (channelID > 0) out.add(channelID - 1);
    if (channelID < _maxID) out.add(channelID + 1);
    return out;
  }
  
  /**
   * Returns an array of (U,V) coordinates of corners of the sensor.
   * Useful for event display. The dimensions of the returned array are [4][2].
   */
  public double[][] getCorners() {
    double[][] out = new double[4][2];
    out[0] = new double[]{0.,0.};
    if (_left) {
      out[1] = new double[]{0.,_side};
      double u = (_side - _vConst) * Math.sin(2.*Math.atan(_tan));
      out[2] = new double[]{u, _side - u * _tan};
      out[3] = new double[]{_uCorner, - _uCorner * _tan};
    } else {
      double u = - _side * Math.sin(2.*Math.atan(_tan));
      out[1] = new double[]{u, - u * _cotan2};
      out[2] = new double[]{_uCorner, _side + _uCorner * _tan};
      out[3] = new double[]{_uCorner, _uCorner * _tan};
    }
    return out;
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  private boolean _left;
  
  private int _maxID;
  private double _thickness;
  private double _offset;
  private double _pitch;
  private double _side;
  private double _tan;
  private double _cotan2;
  private double _uCorner;
  private double _vConst;
  
}
