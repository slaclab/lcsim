package org.lcsim.recon.cat.util;

/**
 * Exception to be thrown by <tt>set(name,value)</tt> methods of various classes.
 *
 * @author D. Onoprienko
 * @version $Id: NoSuchParameterException.java,v 1.1 2007/04/06 21:48:15 onoprien Exp $
 */
public class NoSuchParameterException extends RuntimeException {
  
// -- Constructors :  ----------------------------------------------------------
  
  public NoSuchParameterException() {
    super("No such parameter name\n Method: set(name,value)");
  }
  
  public NoSuchParameterException(String parName) {
    super("No such parameter name: " + parName + "\n Method: set(name,value)");
  }
  
  public NoSuchParameterException(String parName, Class cl) {
    super("No such parameter name: " + parName + "\n Method: set(name,value)\n Class: " + cl.getName());
  }
  
}
