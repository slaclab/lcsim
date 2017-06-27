package org.lcsim.recon.tracking.trfbase;
/** An Enumeration class for Track Surface Direction
 *
 *@author Norman A. Graf
 *@version 1.0
 */
public class TrackSurfaceDirection
{
    private String _dir;
    // Direction of a track relative to the surface direction.
    private TrackSurfaceDirection(String dir)
    {
        _dir = dir;
    }
    
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        return className+" "+_dir;
    }
    public final static TrackSurfaceDirection TSD_FORWARD = new TrackSurfaceDirection("TSD_FORWARD");
    public final static TrackSurfaceDirection TSD_BACKWARD = new TrackSurfaceDirection("TSD_BACKWARD");
    public final static TrackSurfaceDirection TSD_UNDEFINED = new TrackSurfaceDirection("TSD_UNDEFINED");
    
}
