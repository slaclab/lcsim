/*
 * Runem.java
 *
 * Created on May 22, 2008, 4:40 PM
 *
 * $Id: Runem.java,v 1.3 2008/07/15 01:31:55 ngraf Exp $
 */

package org.lcsim.cal.calib;

import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;

/**
 *
 * @author Norman Graf
 */
public class Runem extends Driver
{
    
    /** Creates a new instance of Runem */
    public Runem()
    {
        add(new ProcessHitsDriver());
        //add(new SamplingFractionAnalysisDriver());
        //add( new ClusterCalibrationAnalysisDriver());
        add(new ClusterEnergyAnalysis());
    }

}
