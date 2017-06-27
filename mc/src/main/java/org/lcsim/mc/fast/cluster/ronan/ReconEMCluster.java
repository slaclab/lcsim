package org.lcsim.mc.fast.cluster.ronan;

import org.lcsim.util.aida.AIDA;
import hep.physics.particle.Particle;
import java.util.Random;

public class ReconEMCluster extends ReconCluster {
    ReconEMCluster(ClusterResolutionTables parm, Random rand, Particle mcp, boolean hist) {
        super(parm, rand, mcp, hist);

        a = parm.getEMResolution();
        b = parm.getEMConstantTerm();
        c = parm.getEMPositionError();
        d = parm.getEMAlignmentError();

        smear(rand, hist);
    }

    protected void smearPosition(Random rand, double E, boolean hist) {
        double transSigma = c / Math.sqrt(E) + d;
        transDist = transSigma * rand.nextGaussian();
        if (hist)
            AIDA.defaultInstance().cloud1D("EM: transDist").fill(transDist);
        smearPosition(rand);
    }
}
