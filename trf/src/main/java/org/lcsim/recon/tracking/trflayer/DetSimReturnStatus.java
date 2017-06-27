package org.lcsim.recon.tracking.trflayer;
/** Return status enum for adding layer simulators to a
* Detector Simulator
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 **/
public class DetSimReturnStatus
{
    private String _returnstatus;
    private DetSimReturnStatus(String returnstatus)
    {
        _returnstatus = returnstatus;
    }
    
    public String toString()
    {
        return getClass().getName()+" "+_returnstatus;
    }
    
    public final static DetSimReturnStatus OK = new DetSimReturnStatus("OK");
    public final static DetSimReturnStatus UNKNOWN_NAME = new DetSimReturnStatus("UNKNOWN_NAME");
    public final static DetSimReturnStatus LAYER_MISMATCH = new DetSimReturnStatus("LAYER_MISMATCH");
    
}