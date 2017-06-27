/*
 * SiliconRawHitMC.java
 *
 * Created on February 21, 2006, 5:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization;

import java.util.ArrayList;
import java.util.List;
import org.lcsim.event.MCParticle;
import org.lcsim.event.SimTrackerHit;

/**
 * A SiliconRawHit with information about the Monte Carlo particles 
 * which contributed to this hit.
 * @author ngraf
 */
public class SiliconRawHitMC extends SiliconRawHit
{
    private List<MCParticle> _mcParticles = new ArrayList<MCParticle>();
    private List<SimTrackerHit> _mcHits = new ArrayList<SimTrackerHit>();
    
    /** Creates a new instance of SiliconRawHitMC */
    public SiliconRawHitMC(int cellID0, int cellID1, int timeStamp, int adcCounts, MCParticle mcParticle)
    {
        super(cellID0, cellID1, timeStamp, adcCounts);
        _mcParticles.add(mcParticle);
    }
    
    public List<MCParticle> mcParticles()
    {
        return _mcParticles;
    }
    
    public void addHit(int timeStamp, int adcCounts, SimTrackerHit hit)
    {
        addHit(timeStamp, adcCounts);
        _mcParticles.add(hit.getMCParticle());
        _mcHits.add(hit);
    }
    
    public void addHit(SiliconRawHitMC hit)
    {
        addHit( hit.getTimeStamp(), hit.getADCCounts());
        _mcParticles.addAll(hit.mcParticles());
        _mcHits.addAll(hit.simTrackerHits());
    }

    public List<SimTrackerHit> simTrackerHits()
    {
        return _mcHits;
    }
}
