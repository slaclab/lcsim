package org.lcsim.recon.tracking.gtrbase;
/**
 * An enum to represent the status of a global track fit.
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class FitStatus
{
    private String _fitStatus;
    private FitStatus( String stat)
    {
        _fitStatus = stat;
    }
    public String toString()
    {
        return _fitStatus;
    }
    // Flag indicating the status of the track fit.
    // The first valid status should be recorded, e.g. if a fit is
    // COMPLETE, FORWARD and OPTIMAL, it is recorded as OPTIMAL.
    
    public final static FitStatus BADSTATE = new FitStatus("BADSTATE"); // Entire state is invalid
    public final static FitStatus INVALID = new FitStatus("INVALID");   // Fit parameters have no meaning
    public final static FitStatus OPTIMAL = new FitStatus("OPTIMAL");   // Optimal fit to all clusters (with smoothing)
    public final static FitStatus FORWARD = new FitStatus("FORWARD");   // Optimal fit with this and all preceeding clusters
    public final static FitStatus BACKWARD = new FitStatus("BACKWARD"); // Optimal fit with this and all following clusters
    public final static FitStatus PULL = new FitStatus("PULL");         // Optimal fit with all clusters except this.
    public final static FitStatus COMPLETE = new FitStatus("COMPLETE"); // Non-optimal fit to all clusters (e.g. no smoothing)
    public final static FitStatus PARTIAL = new FitStatus("PARTIAL");   // Fit with a subset of clusters
    
}
