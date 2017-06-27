package org.lcsim.recon.tracking.vsegment.geom;

/**
 * Direction along the track - {@link #OUT} means from the center of the detector to the 
 * periphery (for most tracks, this is the direction the particle moves in), {@link #IN}
 * means from outside into the detector.
 *
 * @author D.Onoprienko
 * @version $Id: Direction.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
public enum Direction {

  /** Outside-to-inside direction. */
  IN(-1), 
  
  /** Inside-to-outside direction. */
  OUT(1);
  
  /**
   * Integer coefficient associated with the direction: <tt>-1</tt> for {@link #IN}, <tt>1</tt> for {@link #OUT}.
   */
  public final int k;
  
  Direction(int k) {this.k = k;}
  
}
