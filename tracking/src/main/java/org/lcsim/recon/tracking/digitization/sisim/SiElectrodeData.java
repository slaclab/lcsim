/*
 * SiElectrodeData.java
 *
 * Created on May 11, 2007, 1:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import java.util.ArrayList;
import java.util.HashSet;
import org.lcsim.event.SimTrackerHit;

import java.util.List;
import java.util.Set;

/**
 *
 * @author tknelson
 */
public class SiElectrodeData
{
    int _charge = 0;
    Set<SimTrackerHit> _simulated_hits = new HashSet<SimTrackerHit>();
    
    /** Creates a new instance of SiElectrodeData */
    public SiElectrodeData()
    {
    }
    
    public SiElectrodeData(int charge)
    {
        _charge = charge;
    }
    
    public SiElectrodeData(int charge, SimTrackerHit simulated_hit)
    {
        _charge = charge;
        _simulated_hits.add(simulated_hit);
    }
    
    public SiElectrodeData(int charge, Set<SimTrackerHit> simulated_hits)
    {
        _charge = charge;
        _simulated_hits = simulated_hits;
    }
    
    public boolean isValid()
    {
        return (getCharge() != 0);
    }
    
    public int getCharge()
    {
        return _charge;
    }
    
    public Set<SimTrackerHit> getSimulatedHits()
    {
        return _simulated_hits;
    }
    
    public SiElectrodeData add(SiElectrodeData electrode_data)
    {
        this.add(electrode_data.getCharge(),electrode_data.getSimulatedHits());
        return this;
    }
    
    public SiElectrodeData add(int charge, Set<SimTrackerHit> simulated_hits)
    {
        this.addCharge(charge);
        for (SimTrackerHit hit : simulated_hits)
        {
            this.addSimulatedHit(hit);
        }
        return this;
    }
    
    public SiElectrodeData addCharge(int charge)
    {
        _charge += charge;
        return this;
    }
    
    public SiElectrodeData addSimulatedHit(SimTrackerHit simulated_hit)
    {
        _simulated_hits.add(simulated_hit);
        return this;
    }    
   
}
