package org.lcsim.recon.tracking.trfbase;
/** Derived from propagator and add a modifiable default direction.
 * We implement the methods that do not specify a direction using
 * those that do.
 *
 * @author Norman A. Graf
 * @version 1.0
 */
public abstract class PropDirected extends Propagator
{
    
    private PropDir _dir;
    
    //
    
    /**
     *constructor with default direction = NEAREST
     *
     */
    public PropDirected()
    {
        direction( PropDir.NEAREST );
    }
    
    //
    
    /**
     *constructor from direction
     *
     * @param   dir Direction in which to propagate
     */
    public PropDirected(PropDir dir)
    {
        direction( dir );
    }
    
    //
    
    /**
     *set the default direction
     *
     * @param   dir direction in which to propagate
     */
    public void direction(PropDir dir)
    {
        _dir = dir;
    }
    
    //
    
    /**
     *get the default direction
     *
     * @return the default direction in which to propagate
     */
    public PropDir direction()
    {
        return _dir;
    }
    
    //
    
    /**
     *Propagate a track without error in the default direction.
     *
     * @param   trv VTrack to propagate
     * @param   srf Surface to which to propagate
     * @param   tder TrackDerivative to update at srf
     * @return  propagation status
     */
    public PropStat vecProp(VTrack  trv,  Surface  srf,
            TrackDerivative  tder )
    {
        return vecDirProp(trv, srf, _dir, tder);
    }
    
    //
    
    /**
     *Propagate a track with error in the default direction.
     *
     * @param   trv ETrack to propagate
     * @param   srf Surface to which to propagate
     * @param   tder TrackDerivative to update at srf
     * @return  propagation status
     */
    public PropStat errProp( ETrack  trv,  Surface  srf,
            TrackDerivative  tder )
    {
        return errDirProp(trv, srf, _dir, tder);
    }
}
