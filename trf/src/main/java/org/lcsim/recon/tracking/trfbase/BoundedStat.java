package org.lcsim.recon.tracking.trfbase;
/** An enumeration class providing BoundedStat enums
 *
 *@author Norman A. Graf
 *@version 1.0
 *
 */
public class BoundedStat
{
    private String _stat;
    
    private BoundedStat(String stat)
    {
        _stat = stat;
    }
    
    /** String representation of BoundedStat.
     * @return String representation of BoundedStat.
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        return className+" "+_stat;
    }
    
    /** Bounds are undefined.
     */
    public final static BoundedStat UNDEFINED_BOUNDS = new BoundedStat("UNDEFINED_BOUNDS");
    /** In bounds.
     */
    public final static BoundedStat IN_BOUNDS = new BoundedStat("IN_BOUNDS");
    /** Out of bounds.
     */
    public final static BoundedStat OUT_OF_BOUNDS = new BoundedStat("OUT_OF_BOUNDS");
    /** Both bounds.
     */
    public final static BoundedStat BOTH_BOUNDS = new BoundedStat("BOTH_BOUNDS");
    
}


