package org.lcsim.recon.tracking.trfbase;

import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackDerivative;
import org.lcsim.recon.tracking.trfbase.VTrack;



public class SimplePropTest extends Propagator
{
    
    private int _flag;
    
    
    // Return the type name.
    public static String typeName()
    { return "SimplePropTest"; }
    // Return the type.
    private static String get_static_type()
    { return typeName(); }
    
    public String toString()
    {
        return  "SimplePropTest propagator.";
    }
    
    
    public SimplePropTest()
    { _flag = 0; }
    // return the type
    public String get_type()
    { return get_static_type(); }
    
    public int get_flag()
    { return _flag; }
    public Propagator newPropagator()
    { return new SimplePropTest(); }
    
    public PropStat vecProp(VTrack trv, Surface srf)
    {
        TrackDerivative der = null;
        return vecProp(trv,srf,der);
    }
    public PropStat vecProp(VTrack trv, Surface srf,
            TrackDerivative der)
    {
        _flag = 1;
        return new PropStat();
    }
    
    public PropStat errProp(ETrack tre, Surface srf)
    {
        TrackDerivative der = null;
        return errProp(tre,srf,der);
    }
    
    public PropStat errProp(ETrack tre, Surface srf,
            TrackDerivative der)
    {
        _flag = 2;
        return new PropStat();
    }
    
    public PropStat vecDirProp(VTrack trv, Surface srf,
            PropDir dir)
    {
        TrackDerivative der = null;
        return vecDirProp(trv,srf,dir,der);
    }
    
    public PropStat vecDirProp(VTrack trv, Surface srf,
            PropDir dir, TrackDerivative der)
    {
        _flag = 3;
        return new PropStat();
    }
    
    public PropStat errDirProp(ETrack tre, Surface srf,
            PropDir dir)
    {
        TrackDerivative der = null;
        return errDirProp(tre,srf,dir,der);
    }
    
    public PropStat errDirProp(ETrack tre, Surface srf,
            PropDir dir, TrackDerivative der)
    {
        _flag = 4;
        return new PropStat();
    }
}
