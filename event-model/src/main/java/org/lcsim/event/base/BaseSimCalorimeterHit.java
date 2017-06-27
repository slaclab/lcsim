package org.lcsim.event.base;

import hep.io.sio.SIORef;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.MCParticle;
import org.lcsim.event.SimCalorimeterHit;

public class BaseSimCalorimeterHit extends BaseCalorimeterHit implements SimCalorimeterHit {
    protected int nContributions;
    protected Object[] particle;
    protected float[] energyContrib;
    protected float[] times;
    protected int[] pdg;
    protected List<float[]> steps;

    protected BaseSimCalorimeterHit() {
    }

    public BaseSimCalorimeterHit(long id, 
            double rawEnergy, 
            double time, 
            Object[] mcparts, 
            float[] energies, 
            float[] times, 
            int[] pdgs,
            LCMetaData meta) {
        // Base class fields.
        super.id = id;
        super.rawEnergy = rawEnergy;
        super.time = time;
        super.positionVec = null;

        // MCParticle contributions.
        this.nContributions = mcparts.length;
        this.particle = mcparts;
        this.energyContrib = energies;
        this.times = times;
        this.pdg = pdgs;
        this.steps = new ArrayList(nContributions);
        
        setMetaData(meta);
        
        // Calculate corrected energy from raw energy.
        calculateCorrectedEnergy();
    }

    /**
     * New ctor with step positions.
     */
    public BaseSimCalorimeterHit(long id, 
            double rawEnergy, 
            double time, 
            Object[] mcparts, 
            float[] energies, 
            float[] times, 
            int[] pdgs, 
            List<float[]> steps,
            LCMetaData meta) {
        // Base class fields.
        super.id = id;
        super.rawEnergy = rawEnergy;
        super.time = time;
        super.positionVec = null;

        // MCParticle contributions.
        this.nContributions = mcparts.length;
        this.particle = mcparts;
        this.energyContrib = energies;
        this.times = times;
        this.pdg = pdgs;
        this.steps = steps;
        
        setMetaData(meta);
    }

    public void shiftTime(double time) {
        super.time = this.getTime() + time;
        for (int i = 0; i < times.length; i++) {
            times[i] += time;
        }
    }

    public double getTime() {
        // First check for explicit value that has been set.
        if (super.time != 0)
            return super.time;

        // If the times array is empty, then there is no valid time to find.
        if (times.length == 0)
            return 0;

        // Find the earliest time from the array.
        double t = times[0];
        for (int i = 1; i < times.length; i++) {
            t = Math.min(t, times[i]);
        }

        // Cache the minimum time value to avoid repeating the above calculation.
        super.time = t;

        return t;
    }

    public MCParticle getMCParticle(int index) {
        Object p = particle[index];
        if (p instanceof SIORef)
            p = ((SIORef) p).getObject();
        return (MCParticle) p;
    }

    public double getContributedEnergy(int index) {
        return energyContrib[index];
    }

    public int getPDG(int index) {
        return pdg[index];
    }

    public double getContributedTime(int index) {
        return times[index];
    }

    public float[] getStepPosition(int index) {
        return steps.get(index);
    }

    public int getMCParticleCount() {
        return particle.length;
    }
}
