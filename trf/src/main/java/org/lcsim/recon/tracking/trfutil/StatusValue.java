package org.lcsim.recon.tracking.trfutil;

/**
 *Class to encapsulate a value and an integer status.
 * Typically used to return a value of the specified type
 * where an error is indicated with a nonzero status.
 * @version 1.0
 * @author  Norman A. Graf
 */
public class StatusValue
{
    private int _status;
    private Object _value;
    /**
     * Creates a new instance of StatusValue
     * @param status The status of the Object value.
     * @param value  The value Object which has this status.
     */
    public StatusValue(int status, Object value)
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
    public Object value()
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
