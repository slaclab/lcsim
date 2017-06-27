package org.lcsim.recon.tracking.trfutil;
/**
 * Returns an integer value for a Status object.
 *@version 1.0
 * @author  Norman A. graf
 */
public class StatusInt
{
    private int _status;
    private int _value;
    
    /** Creates a new instance of StatusDouble */
    public StatusInt(int status, int value)
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
    public int value()
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
