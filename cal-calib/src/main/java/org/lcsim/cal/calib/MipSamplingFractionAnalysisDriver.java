package org.lcsim.cal.calib;

import java.util.List;
import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;

/**
 *
 * @author Norman A. Graf
 *
 * @version $Id: MipSamplingFractionAnalysisDriver.java,v 1.2 2010/11/03 08:11:23 ngraf Exp $
 */
public class MipSamplingFractionAnalysisDriver extends Driver
{

    private boolean _debug = false;
    private AIDA aida = AIDA.defaultInstance();

    @Override
    protected void process(EventHeader event)
    {
        List<MCParticle> mcparts = event.getMCParticles();
        // for some reason, the MC hierarchy is inverted...
        MCParticle mcpart = mcparts.get(mcparts.size() - 1);
        String particleType = mcpart.getType().getName();
        log(particleType);
        if (particleType.equals("mu+") && mcpart.getSimulatorStatus().hasLeftDetector())
        {
            List<SimCalorimeterHit> ecalhits = event.get(SimCalorimeterHit.class, "EcalBarrelHits");
            log("There are " + ecalhits.size() + " EcalBarrelHits");
            for (SimCalorimeterHit hit : ecalhits)
            {
                long cellId = hit.getCellID();
                IDDecoder d = hit.getIDDecoder();
                d.setID(cellId);
                int layer = d.getLayer();
                int slice = d.getValue("slice");
                log(layer + " " + slice);
                aida.cloud1D("ECal layer" + layer + "_slice" + slice + " energy").fill(hit.getRawEnergy());
                if (layer > 0 && layer < 21)
                {
                    if (slice == 0)
                        aida.histogram1D("ECal thin layer absorber energy",100,0.,0.05).fill(hit.getRawEnergy());
                    if (slice == 2)
                        aida.histogram1D("ECal thin layer silicon energy",100,0.,0.001).fill(hit.getRawEnergy());
                }
                if (layer > 20)
                {
                    if (slice == 0)
                        aida.histogram1D("ECal thick layer absorber energy",100,0.,0.05).fill(hit.getRawEnergy());
                    if (slice == 2)
                        aida.histogram1D("ECal thick layer silicon energy",100,0.,0.001).fill(hit.getRawEnergy());
                }

            }
            List<SimCalorimeterHit> hcalhits = event.get(SimCalorimeterHit.class, "HcalBarrelHits");
            log("There are " + hcalhits.size() + " HcalBarrelHits");
            for (SimCalorimeterHit hit : hcalhits)
            {
                long cellId = hit.getCellID();
                IDDecoder d = hit.getIDDecoder();
                d.setID(cellId);
                int layer = d.getLayer();
                int slice = d.getValue("slice");
                log(layer + " " + slice);
                aida.cloud1D("HCal layer" + layer + "_slice" + slice + " energy").fill(hit.getRawEnergy());
//                if(slice == 0) aida.histogram1D("HCal absorber energy",100,0.,0.05).fill(hit.getRawEnergy());
//                if(slice == 1) aida.histogram1D("HCal scintillator energy",100,0.,0.004).fill(hit.getRawEnergy());
            }
        }
    }

    @Override
    protected void endOfData()
    {
        //TODO automate fits...
    }

    private void log(String s)
    {
        if (_debug)
            System.out.println(s);
    }
}
