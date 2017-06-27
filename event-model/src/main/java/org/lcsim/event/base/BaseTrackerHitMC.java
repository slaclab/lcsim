/*
 * BaseTrackerHitMC.java
 *
 * Created on March 24, 2006, 9:46 AM
 *
 * $Id: BaseTrackerHitMC.java,v 1.3 2006/03/30 18:33:13 ngraf Exp $
 */

package org.lcsim.event.base;

import java.util.ArrayList;
import java.util.List;
import org.lcsim.event.MCParticle;
import org.lcsim.event.SimTrackerHit;

/**
 * A BaseTrackerHit which includes information about the Monte Carlo particles contributing to it.
 * @author Norman Graf
 */
public class BaseTrackerHitMC extends BaseTrackerHit
{
    // TODO should this be a Set so we have no duplicates?
    protected List<MCParticle> _mcparticles = new ArrayList<MCParticle>();
    protected List<SimTrackerHit> _simHits = new ArrayList<SimTrackerHit>();

    /**
     * fully qualified constructor
     * @param pos the position of this hit (x,y,z) in mm
     * @param cov the covariance matrix for the position measurement, packed as 6 elements.
     * @param t the time for this measurement in ns
     * @param e the energy deposit associated with this measurement, in GeV
     * @param type the type of this measurement. not yet defined.
     * @param mcparticle The monte carlo particle contributing to this measurement.
     */
    public BaseTrackerHitMC(double[] pos, double[] cov, double t, double e, int type, MCParticle mcparticle)
    {
        super(pos, cov, t, e, type);
        _mcparticles.add(mcparticle);
    }
    
    /**
     * fully qualified constructor
     * @param pos the position of this hit (x,y,z) in mm
     * @param cov the covariance matrix for the position measurement, packed as 6 elements.
     * @param t the time for this measurement in ns
     * @param e the energy deposit associated with this measurement, in GeV
     * @param type the type of this measurement. not yet defined.
     * @param simHits The list of SimTrackerHit(s) contributing to this measurement.
     */
    public BaseTrackerHitMC(double[] pos, double[] cov, double t, double e, int type, List<SimTrackerHit> simHits)
    {
        super(pos, cov, t, e, type);
        for(SimTrackerHit hit : simHits)
        {
            MCParticle mcp = hit.getMCParticle();
            if(!_mcparticles.contains(mcp)) _mcparticles.add(mcp);
            if(!_simHits.contains(hit)) _simHits.add(hit);
        }
    }
    
    /**
     * Add an MCParticle which contributed to this hit.
     * @param mcp The MCParticle to associate with this hit.
     */
    public void addMCParticle(MCParticle mcp)
    {
        if(!_mcparticles.contains(mcp)) _mcparticles.add(mcp);
    }
    /**
     * The list of monte carlo particles contributing to this measurement.
     * @return The list of monte carlo particles contributing to this measurement.
     */
    public List<MCParticle> mcParticles()
    {
        return _mcparticles;
    }
    
    /**
     * The MC tracker hits contributing to this hit.
     * @return the list of SimTrackerHit which contribute to this hit.
     */
    public List<SimTrackerHit> getSimHits()
    {
        return _simHits;
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(" with "+_mcparticles.size()+" MCParticle"+(_mcparticles.size()==1?"":"s")+"\n");
        for(MCParticle mcp : _mcparticles)
        {
            sb.append(mcp+"\n");
        }
        return sb.toString();
    }
    
}
