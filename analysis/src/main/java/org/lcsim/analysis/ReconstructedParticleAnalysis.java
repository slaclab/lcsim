package org.lcsim.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;
import org.lcsim.event.EventHeader;
import org.lcsim.event.LCRelation;
import org.lcsim.event.MCParticle;
import org.lcsim.event.ReconstructedParticle;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.event.base.BaseLCRelation;
import org.lcsim.util.Driver;
import static org.lcsim.util.Driver.HLEVEL_FULL;
import static org.lcsim.util.Driver.HLEVEL_NORMAL;
import org.lcsim.util.aida.AIDA;

/**
 *
 * @author Norman A Graf
 *
 * @version $Id:
 */
public class ReconstructedParticleAnalysis extends Driver
{

    private AIDA aida = AIDA.defaultInstance();

    @Override
    protected void process(EventHeader event)
    {
        // The Monte carlo Particles
        List<MCParticle> mcparts = event.getMCParticles();
        for (MCParticle mcp : mcparts) {
            // only book final state particles here...
            if (mcp.getGeneratorStatus() == MCParticle.FINAL_STATE) {
                aida.cloud1D("MC final state PDG ID").fill(mcp.getPDGID());
            }
        }
        // need to set  up the relation between CalorimeterHit ans SimCalorimeterHit
        List<LCRelation> caloHitSimHitRelation = event.get(LCRelation.class, "CalorimeterHitRelations");
        Map<CalorimeterHit, SimCalorimeterHit> simhitmap = new HashMap<CalorimeterHit, SimCalorimeterHit>();
        for (LCRelation relation : caloHitSimHitRelation) {
            CalorimeterHit digiHit = (CalorimeterHit) relation.getFrom();
            SimCalorimeterHit simHit = (SimCalorimeterHit) relation.getTo();
            simhitmap.put(digiHit, simHit);
        }

        // the ReconstructedParticles
        List<ReconstructedParticle> rplist = event.get(ReconstructedParticle.class, "PandoraPFOCollection");
        aida.cloud1D("Number of ReconstructedParticles found").fill(rplist.size());
        double eventEnergy = 0.;
        for (ReconstructedParticle rp : rplist) {
            eventEnergy += rp.getEnergy();
            aida.cloud1D("Energy").fill(rp.getEnergy());
            int id = rp.getType();
            aida.cloud1D("Cluster Energy pid= " + id).fill(rp.getEnergy());
            List<Cluster> clusters = rp.getClusters();
            for (Cluster clus : clusters) {
                List<CalorimeterHit> hits = clus.getCalorimeterHits();
                for (CalorimeterHit hit : hits) {
                    //get the SimCalorimeterHit that corresponds to this hit
                    SimCalorimeterHit simHit = simhitmap.get(hit);
                    // TODO: flesh out the analysis here...
                }
            }
        }
        aida.cloud1D("Event Energy").fill(eventEnergy);
    }
}
