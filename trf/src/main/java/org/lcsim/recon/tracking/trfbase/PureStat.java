package org.lcsim.recon.tracking.trfbase;
/** An Enumeration class for Pure Status
 *
 *@author Norman A. Graf
 *@version 1.0
 */
public class PureStat
{
    String _stat;
    
    private PureStat(String stat)
    {
        _stat = stat;
    }
    
    /** output
     * @return String representation of this class
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        return className+_stat;
    }
    
    public final static PureStat AT = new PureStat("AT");
    public final static PureStat ON = new PureStat("ON");
    public final static PureStat INSIDE = new PureStat("INSIDE");
    public final static PureStat OUTSIDE = new PureStat("OUTSIDE");
    
}