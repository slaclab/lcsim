/*
 * ReadoutChip.java
 *
 * Created on April 20, 2007, 9:46 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import java.util.List;
import org.lcsim.detector.tracker.silicon.SiSensorElectrodes;
import java.util.SortedMap;
import org.lcsim.event.RawTrackerHit;

/**
 *
 * @author tknelson
 */
public interface ReadoutChip
{
    public SortedMap<Integer,List<Integer>> readout(SiElectrodeDataCollection data, SiSensorElectrodes electrodes);

    public ReadoutChannel getChannel(int channel_number);
    
    public double decodeCharge(RawTrackerHit hit);

    public int decodeTime(RawTrackerHit hit);
    
    public interface ReadoutChannel
    {
        public double computeNoise(double capacitance);
    }
    
}
