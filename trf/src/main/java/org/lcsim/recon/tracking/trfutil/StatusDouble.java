package org.lcsim.recon.tracking.trfutil;
/**
 * Returns a double value for a Status object.
 *@version 1.0
 * @author  Norman A. graf
 */
public class StatusDouble
{
    private int _status;
    private double _value;
    
    /** Creates a new instance of StatusDouble */
    public StatusDouble(int status, double value)
    {
    _status = status;
    _value = value;
    }
    
   /**
     * Return the status for this Status-Value pair.
     * @return the integer status.
     */
    public int status()
    {
        return _status;
    }
    
    /**
     * Return the value for this Status-Value pair.
     * @return the Object value.
     */
    public double value()
    {
        return _value;
    }
    
    /**
     * String representation of this object.
     * @return a String representation of this object.
     */    
    public String toString()
    {
        return _value+" has status: "+_status;
    }    
    
    
}
