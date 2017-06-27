package org.lcsim.mc.fast.cluster.ronan;

import org.lcsim.mc.fast.MCFast;
import hep.physics.particle.Particle;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.lcsim.event.Cluster;

public abstract class ReconCluster implements Cluster {
    protected ClusterResolutionTables parm;
    protected Particle mcp;
    protected double a = 0;
    protected double b = 0;
    protected double c = 0;
    protected double d = 0;
    protected double energy;
    protected double energyError;
    protected double neg_energy;
    protected double sigma;
    protected double phi;
    protected double radius;
    protected double theta;
    protected double transDist;

    ReconCluster(ClusterResolutionTables parm, Random rand, Particle mcp, boolean hist) {
        this.parm = parm;
        this.mcp = mcp;
    }

    /** Best estimate for total energy of cluster */
    public double getEnergy() {
        return energy;
    }

    public double getEnergyError() {
        return energyError;
    }

    public void setEnergyError(double energyError) {
        this.energyError = energyError;
    }

    public double getNegEnergy() {
        return neg_energy;
    }

    public double getSigma() {
        return sigma;
    }

    public void adjustEnergy(double neg_energy_total, double pos_energy_weight_total) {
        MCFast.log.info(" min(sigma,energy)=" + Math.min(sigma, energy) + " ratio= " + (Math.min(sigma, energy) / pos_energy_weight_total) + " before adjust energy= " + energy);
        energy += neg_energy_total * Math.min(sigma, energy) / pos_energy_weight_total;

        if (energy <= mcp.getMass())
            energy = mcp.getMass() + Double.MIN_VALUE;

        MCFast.log.info(" neg_energy_total= " + neg_energy_total + " after adjust energy= " + energy);
    }

    protected void smear(Random rand, boolean hist) {
        // Get true energy from MCParticle
        double E = mcp.getEnergy();

        // Smear reconstructed energy

        smearEnergy(rand, E, hist);

        // Smear reconstructed position
        smearPosition(rand, E, hist);
    }

    protected void smearEnergy(Random rand, double E, boolean hist) {
        sigma = Math.sqrt(Math.pow(a, 2.) * E + Math.pow(b * E, 2.));

        energy = E + (sigma * rand.nextGaussian());
        if (energy <= mcp.getMass()) {
            neg_energy = energy - mcp.getMass();
            energy = mcp.getMass() + Double.MIN_VALUE;
        } else {
            neg_energy = 0.;
        }
    }

    protected void smearPosition(Random rand) {
        // Get true direction from MCParticle
        double Px = mcp.getPX();
        double Py = mcp.getPY();
        double Pz = mcp.getPZ();

        double P = Math.sqrt((Px * Px) + (Py * Py) + (Pz * Pz));
        double Phi = Math.atan2(Py, Px);
        if (Phi < 0) {
            Phi += (2 * Math.PI);
        }

        double Theta = Math.acos(Pz / P);

        // Simulate position smearing on a sphere of radius 2 meters
        radius = 2000.0;

        double x = (radius * Px) / P;
        double y = (radius * Py) / P;
        double z = (radius * Pz) / P;

        // these vectors vt and vs (orthonorm.) span a plane perpendicular to the momentum vector,
        // so smearing with a transdist will involve a lin. comb. of these
        double[] vt = { -Math.cos(Theta) * Math.cos(Phi), -Math.cos(Theta) * Math.sin(Phi), Math.sin(Theta) };
        double[] vs = { Math.sin(Phi), -Math.cos(Phi), 0 };

        // restricted to [0,PI] since transdist can be negative
        double alpha = rand.nextDouble() * Math.PI;
        x = x + transDist * (Math.cos(alpha) * vt[0] + Math.sin(alpha) * vs[0]);
        y = y + transDist * (Math.cos(alpha) * vt[1] + Math.sin(alpha) * vs[1]);
        z = z + transDist * (Math.cos(alpha) * vt[2] + Math.sin(alpha) * vs[2]);

        phi = Math.atan2(y, x);
        if (phi < 0) {
            phi += (2 * Math.PI);
        }
        theta = Math.acos(z / radius);
    }

    public Particle getMCParticle() {
        return mcp;
    }

    abstract void smearPosition(Random rand, double E, boolean hist);

    public double[] getHitContributions() {
        return null;
    }

    public List getClusters() {
        return Collections.EMPTY_LIST;
    }

    public double[] getSubdetectorEnergies() {
        return null;
    }

    public double[] getPositionError() {
        return null; // fixme:
    }

    public int getType() {
        return 0; // Fixme:
    }

    public double getITheta() {
        return 0; // Fixme:
    }

    public double getIPhi() {
        return 0; // Fixme:
    }

    public double[] getDirectionError() {
        return null; // Fixme:
    }

    public List getCalorimeterHits() {
        return Collections.EMPTY_LIST;
    }

    public double[] getShape() {
        return null;
    }

    public double[] getPosition() {
        double x = radius * Math.sin(theta) * Math.cos(phi);
        double y = radius * Math.sin(theta) * Math.sin(phi);
        double z = radius * Math.cos(theta);
        return new double[] { x, y, z };
    }

    public double[] getParticleType() {
        return null; // Fixme:
    }

    public int getParticleId() {
        return 0;
    }

    public int getSize() {
        return 0;
    }
}
