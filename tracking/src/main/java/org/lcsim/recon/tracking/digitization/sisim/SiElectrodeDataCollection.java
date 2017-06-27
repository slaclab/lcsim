/*
 * SiElectrodeDataCollection.java
 *
 * Created on May 23, 2007, 10:56 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import org.lcsim.event.SimTrackerHit;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author tknelson
 */
public class SiElectrodeDataCollection extends TreeMap<Integer,SiElectrodeData>
{
    
    /** Creates a new instance of SiElectrodeDataCollection */
    public SiElectrodeDataCollection()
    {
    }
    
    // Create a deep copy of this collection
    public SiElectrodeDataCollection(SiElectrodeDataCollection electrode_data)
    {
        for (Integer cellid : electrode_data.keySet())
        {
            this.add(cellid,electrode_data.get(cellid));
        }
    }
    
    // Create from a map of electrode charges for a single SimTrackerHit
    public SiElectrodeDataCollection(SortedMap<Integer,Integer> electrode_charge, SimTrackerHit hit)
    {
        for (Integer cellid : electrode_charge.keySet())
        {
            this.put(cellid,new SiElectrodeData(electrode_charge.get(cellid),hit));
        }
    }
    
    public SortedMap<Integer,Integer> getChargeMap()
    {
        SortedMap<Integer,Integer> charge_map = new TreeMap<Integer,Integer>();
        for (Integer cellid : this.keySet())
        {
            charge_map.put(cellid,this.get(cellid).getCharge());
        }
        return charge_map;
    }
    
    public void add(SortedMap<Integer,SiElectrodeData> electrode_data_collection)
    {
        for (int cellid : electrode_data_collection.keySet())
        {
            this.add(cellid,electrode_data_collection.get(cellid));
        }
    }
    
    public void add(int cellid, SiElectrodeData electrode_data)
    {
        if (electrode_data.isValid())
        {
            if (this.containsKey(cellid))
            {
                this.put(cellid,this.get(cellid).add(electrode_data));
            }
            else
            {
                this.put(cellid,electrode_data);
            }
        }
    }
    
}
