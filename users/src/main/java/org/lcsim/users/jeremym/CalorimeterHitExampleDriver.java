package org.lcsim.users.jeremym;

import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.EventHeader;
import org.lcsim.util.aida.AIDA;

/**
 * This is an example Driver that sums hit energies in the ECAL barrel.
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class CalorimeterHitExampleDriver {
    
    static String collectionName = "EcalBarrelHits";
    AIDA aida = AIDA.defaultInstance();
        
    public void process(EventHeader event) {
        double totalEnergy = 0.;
        for (CalorimeterHit hit : event.get(CalorimeterHit.class, collectionName)) {
            totalEnergy += hit.getCorrectedEnergy();
            aida.cloud1D("EcalBarrel Energy").fill(totalEnergy);
        }        
    }
}
