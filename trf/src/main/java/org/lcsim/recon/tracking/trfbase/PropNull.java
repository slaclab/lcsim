package org.lcsim.recon.tracking.trfbase;

/** PropNull propagates a track to itself.
 *
 * @author Norman A. Graf
 * @version 1.0
 */
public class PropNull extends Propagator
{
    
    // static methods
    
    //
    
    /**
     *Return the type name.
     *
     * @return  String representation of this class type
     *Included for completeness with C++ version
     */
    public static String typeName()
    { return "PropNull";
    }
    
    //
    
    /**
     *Return the type.
     *
     * @return  String representation of this class type
     *Included for completeness with C++ version
     */
    public static String staticType()
    { return typeName();
    }
    
    // object methods
    
    //
    
    /**
     *constructor
     *
     */
    public PropNull()
    {
    }
    
    //
    
    /**
     *Return the type.
     *
     * @return String representation of this class type
     *Included for completeness with C++ version
     */
    public String  type()
    { return staticType();
    }
    
    //
    
    /**
     *Clone
     *
     * @return new copy of this Propagator
     */
    public Propagator newPropagator()
    {
        return new PropNull();
    }
    
    //
    
    /**
     *propagate a track without error
     *
     * @param   trv VTrack to propagate
     * @param   srf Surface to which to propagate
     * @return  propagation status
     */
    public PropStat vecProp(VTrack trv, Surface srf)
    {
        TrackDerivative deriv = null;
        return vecProp(trv, srf, deriv);
    }
    
    
    /**
     *propagate a track without error
     *
     * @param   trv VTrack to propagate
     * @param   srf Surface to which to propagate
     * @param   deriv TrackDerivative to update at srf
     * @return  propagation status
     */
    public PropStat vecProp(VTrack trv,  Surface srf,
            TrackDerivative deriv)
    {
        PropStat pstat = new PropStat();
        if ( trv.surface().pureEqual(srf) ) pstat.setSame();
        if ( deriv != null )
        {
            deriv.setIdentity();
        }
        return pstat;
    }
    
    //
    
    /**
     *propagate a track without error in the specified direction
     *
     * @param   trv VTrack to propagate
     * @param   srf Surface to which to propagate
     * @param   dir direction in which to propagate
     * @return propagation status
     */
    public PropStat vecDirProp(VTrack trv, Surface srf, PropDir dir)
    {
        TrackDerivative deriv = null;
        return vecDirProp(trv, srf, dir, deriv);
    }
    
    /**
     *propagate a track without error in the specified direction
     *
     * @param   trv VTrack to propagate
     * @param   srf Surface to which to propagate
     * @param   dir direction in which to propagate
     * @param   deriv TrackDerivative to update at srf
     * @return propagation status
     */
    public PropStat vecDirProp(VTrack trv, Surface srf, PropDir dir,
            TrackDerivative deriv)
    {
        PropStat pstat = new PropStat();
        if ( trv.surface().pureEqual(srf) ) pstat.setSame();
        if ( deriv != null )
        {
            deriv.setIdentity();
        }
        return pstat;
    }
    
    //
    
    /**
     *propagate a track with error
     *
     * @param   tre ETrack to propagate
     * @param   srf Surface to which to propagate
     * @return  propagation status
     */
    public PropStat errProp(ETrack tre, Surface srf)
    {
        TrackDerivative deriv = null;
        return errProp(tre, srf, deriv);
    }
    
    /**
     *propagate a track with error
     *
     * @param   tre ETrack to propagate
     * @param   srf Surface to which to propagate
     * @param   deriv TrackDerivative to update at srf
     * @return  propagation status
     */
    public PropStat errProp(ETrack tre, Surface srf,
            TrackDerivative deriv)
    {
        PropStat pstat = new PropStat();
        if ( tre.surface().pureEqual(srf) ) pstat.setSame();
        if ( deriv != null )
        {
            deriv.setIdentity();
        }
        return pstat;
    }
    
    //
    
    /**
     *propagate a track with error in the specified direction
     *
     * @param   tre ETrack to propagate
     * @param   srf Surface to which to propagate
     * @param   dir direction in which to propagate
     * @return  propagation status
     */
    public PropStat errDirProp(ETrack tre, Surface srf, PropDir dir)
    {
        TrackDerivative deriv = null;
        return errDirProp(tre, srf, dir, deriv);
    }
    
    /**
     *propagate a track with error in the specified direction
     *
     * @param   tre ETrack to propagate
     * @param   srf Surface to which to propagate
     * @param   dir direction in which to propagate
     * @param   deriv TrackDerivative to update at srf
     * @return  propagation status
     */
    public PropStat errDirProp(ETrack tre,  Surface srf, PropDir dir,
            TrackDerivative deriv )
    {
        PropStat pstat = new PropStat();
        if ( tre.surface().pureEqual(srf) ) pstat.setSame();
        if ( deriv != null )
        {
            deriv.setIdentity();
        }
        return pstat;
    }
    
    
    
    /**
     *output stream
     *
     * @return String representation of this class
     */
    public String toString()
    {
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        
        return className;
    }
}
