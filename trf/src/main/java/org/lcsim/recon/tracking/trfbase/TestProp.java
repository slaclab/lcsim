package org.lcsim.recon.tracking.trfbase;


// Test concrete class.

public class TestProp extends Propagator
{
    // static methods
    // Return the type name.
    public static String typeName()
    { return "TestProp"; }
    // Return the type.
    public  String get_static_type()
    { return typeName(); }
    
    public String toString()
    {
        return "Test propagator";
    }
    
    public  PropStat
            myprop(VTrack trv, Surface asrf, PropDir dir)
    {
        Surface srf = asrf.newPureSurface();
        trv.setSurface(srf);
        PropStat pst = new PropStat();
        TrackVector vec = trv.vector();
        if ( dir.equals(PropDir.BACKWARD) )
        {
            vec.set(1,vec.get(1)-1.0);
        }
        else
        {
            vec.set(1, vec.get(1)+1.0);
        }
        trv.setVector(vec);
        return pst;
    }
    
    public String get_type()
    { return get_static_type(); }
    
    public Propagator newPropagator()
    {
        return new TestProp();
    }
    
    public PropStat vecProp(VTrack trv, Surface srf,
            TrackDerivative pder )
    {
        return myprop(trv,srf,PropDir.NEAREST);
    }
    
    public PropStat vecProp(VTrack trv, Surface srf)
    {
        TrackDerivative pder = null;
        return vecProp(trv, srf, pder);
    }
    
    public PropStat vecDirProp(VTrack trv, Surface srf,
            PropDir dir, TrackDerivative pder)
    {
        return myprop(trv,srf,dir);
    }
    
    public PropStat vecDirProp(VTrack trv, Surface srf,
            PropDir dir)
    {
        TrackDerivative pder = null;
        return vecDirProp(trv,srf,dir,pder);
    }
    
    public PropStat errProp(ETrack trv, Surface srf,
            TrackDerivative pder)
    {
        return myprop(trv,srf,PropDir.NEAREST);
    }
    
    public PropStat errProp(ETrack trv, Surface srf)
    {
        TrackDerivative pder = null;
        return errProp(trv,srf,pder);
    }
    
    public PropStat errDirProp(ETrack trv, Surface srf,
            PropDir dir, TrackDerivative pder)
    {
        return myprop(trv,srf,dir);
    }
    
    public PropStat errDirProp(ETrack trv, Surface srf,
            PropDir dir)
    {
        TrackDerivative pder = null;
        return errDirProp(trv,srf,dir, pder);
    }
}