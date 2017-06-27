package org.lcsim.mc.fast.util;

import static java.lang.Math.abs;

import hep.physics.particle.properties.ParticlePropertyManager;
import hep.physics.particle.properties.ParticlePropertyProvider;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;
import org.lcsim.event.ReconstructedParticle;
import org.lcsim.util.Driver;

public class MonitorStdhep extends Driver {

    final int nEventSkip = 0;
    // final int nEventSkip=95;
    // final int nEventSkip=233;
    // final int nEventSkip=684;
    final int nprintMax = 20;
    // final int nprintMax=Integer.MAX_VALUE;
    final int nCloudMax = Integer.MAX_VALUE;
    int ncnt = 0;
    int ncnt_in = 0;
    int ncnt_out = 0;
    int eventNumber;
    float eventWeight;
    final ParticlePropertyProvider dPPP;
    List<ReconstructedParticle> recoParticles;
    int idrup;

    int m_eventCount;

    int ievt;
    int nmax = 1000000;

    public MonitorStdhep() throws IOException {

        m_eventCount = 0;
        dPPP = ParticlePropertyManager.getParticlePropertyProvider();
        System.out.println(" MonitorStdhep constructor ");
    }

    protected void process(EventHeader event) {
        // super.process(event);
        ncnt_in++;
        if (ncnt_in <= nEventSkip) {
            // System.out.println(" event.getEventNumber= "+event.getEventNumber());
            throw new Driver.NextEventException();
        } else {
            eventNumber = event.getEventNumber();
            ncnt++;
            if (ncnt <= nprintMax || ncnt % 100 == 0)
                System.out.println(" ncnt_in= " + ncnt_in + " ncnt= " + ncnt + " eventNumber= " + eventNumber);
            // super.process(event);
            // idrup=event.getIntegerParameters().get("idrup")[0];

            eventWeight = event.getWeight();
            if (ncnt <= nprintMax) {
                Map<String, float[]> headerFloatMap = event.getFloatParameters();
                Map<String, int[]> headerIntMap = event.getIntegerParameters();
                for (String headerFloatName : headerFloatMap.keySet())
                    System.out.println(" headerFloatName= " + headerFloatName + " value= " + headerFloatMap.get(headerFloatName)[0]);
                for (String headerIntName : headerIntMap.keySet())
                    System.out.println(" headerIntName= " + headerIntName + " value= " + headerIntMap.get(headerIntName)[0]);
                System.out.println(" idrup= " + idrup);
                long eventTimeStamp = event.getTimeStamp();
                System.out.println(" eventWeight= " + eventWeight);
            }
            List<MCParticle> particles = event.get(MCParticle.class, event.MC_PARTICLES);
            for (MCParticle particle : particles) {
                int iPdgId = abs(particle.getPDGID());
                if (ncnt <= nprintMax)
                    System.out.println(" iPdgId= " + iPdgId);
            }
        }
    }
}
