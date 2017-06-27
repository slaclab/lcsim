package org.lcsim.mc.fast.cluster.ronan;

import org.lcsim.util.aida.AIDA;
import hep.physics.particle.Particle;
import java.util.Random;

public class ReconHADCluster extends ReconCluster {
    ReconHADCluster(ClusterResolutionTables parm, Random rand, Particle mcp, boolean hist) {
        super(parm, rand, mcp, hist);

        a = parm.getHADResolution();
        b = parm.getHADConstantTerm();
        c = parm.getHADPositionError();
        d = parm.getHADAlignmentError();

        smear(rand, hist);
    }

    protected void smearPosition(Random rand, double E, boolean hist) {
        double transSigma = c / Math.sqrt(E) + d;
        transDist = transSigma * rand.nextGaussian();
        if (hist)
            AIDA.defaultInstance().cloud1D("HAD: transDist").fill(transDist);
        smearPosition(rand);
    }
}
