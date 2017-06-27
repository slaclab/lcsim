package org.lcsim.recon.tracking.trflayer;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.PropDirected;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackDerivative;
import org.lcsim.recon.tracking.trfbase.VTrack;
public class PropTest extends PropDirected
{
    // static methods
    // Return the type name.
    public static String typeName()
    { return "PropTest"; }
    // Return the type.
    public static String get_static_type()
    { return typeName(); }
    
    public String toString()
    {return "PropTest"; }
    
    
    // Return the type.
    public String get_type()
    { return get_static_type(); }
    
    public PropStat
            vecDirProp(VTrack trv, Surface srf, PropDir dir,
            TrackDerivative pder)
    {
        PropStat pstat = new PropStat();
        pstat.setPathDistance(1.);
        return pstat;
    }
    public PropStat
            vecDirProp(VTrack trv, Surface srf, PropDir dir)
    {
        TrackDerivative pder = null;
        return vecDirProp(trv, srf, dir, pder);
    }
    
    
    public PropStat
            errDirProp(ETrack tre, Surface srf, PropDir dir,
            TrackDerivative pder)
    {
        PropStat pstat = new PropStat();
        pstat.setPathDistance(1.);
        return pstat;
    }
    public PropStat
            errDirProp(ETrack tre, Surface srf, PropDir dir)
    {
        TrackDerivative pder = null;
        return errDirProp(tre, srf, dir, pder);
    }
    
    public Propagator newPropagator()
    { return new PropTest(); }
}
