package org.lcsim.recon.tracking.vsegment.transform;

/**
 *
 * @author D.Onoprienko
 * @version $Id: Axis.java,v 1.1 2008/12/06 21:53:44 onoprien Exp $
 */
public enum Axis {
  
  X(0),
  Y(1),
  Z(2);
  
  private int _index;
  
  private Axis(int index) {_index = index;}
  
  public int index() {return _index;}
 
}
