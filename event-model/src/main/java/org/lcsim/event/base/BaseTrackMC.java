/*
 * BaseTrackMC.java
 *
 * Created on February 9, 2007, 10:50 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.lcsim.event.base;
import org.lcsim.event.MCParticle;
/**
 *
 * @author cassell
 */
public class BaseTrackMC extends BaseTrack
{
    MCParticle _p;
    /** Creates a new instance of BaseTrackMC */
    public BaseTrackMC(MCParticle p)
    {
        super();
        _p = p;
    }
    public MCParticle getMCParticle()
    {
        return _p;
    }
}
