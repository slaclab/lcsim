package org.lcsim.event.util;

import hep.physics.event.generator.GeneratorFactory;
import hep.physics.event.generator.MCEvent;
import hep.physics.particle.BasicParticle;
import hep.physics.particle.Particle;
import hep.physics.particle.properties.ParticleType;
import hep.physics.particle.properties.UnknownParticleIDException;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.HepLorentzVector;

import org.apache.commons.lang3.NotImplementedException;
import org.lcsim.event.MCParticle;
import org.lcsim.event.MCParticle.SimulatorStatus;
import org.lcsim.event.base.BaseLCSimEvent;

public class LCSimFactory extends GeneratorFactory
{
    private String detectorName;
    private static SimulatorStatus emptySimulatorStatus = new Status();
    public LCSimFactory(String detectorName)
    {
        this.detectorName = detectorName;
    }

    public MCEvent createEvent(int run, int event)
    {
        return new BaseLCSimEvent(run,event,detectorName);
    }

    public BasicParticle createParticle(Hep3Vector origin, HepLorentzVector p, ParticleType ptype, int status, double time)
    {
        return new GeneratorParticle(origin,p,ptype,status,time);
    }

    private static class GeneratorParticle extends BasicParticle implements MCParticle
    {
        float[] spin = new float[3];
        int[] colorFlow = new int[2];

        GeneratorParticle(Hep3Vector origin,HepLorentzVector p,ParticleType ptype,int status, double time)
        {
            super(origin,p,ptype,status,time);
        }

        public SimulatorStatus getSimulatorStatus()
        {
            return emptySimulatorStatus;
        }

        public Hep3Vector getEndPoint()
        {
            if (getDaughters().isEmpty()) throw new RuntimeException("MCParticle end point not available");
            return ((Particle) getDaughters().get(0)).getOrigin();
        }
        // FixMe: Ugly workaround for particle 92 (pythia string) problem
        public double getCharge()
        {
            try
            {
                return super.getCharge();
            }
            catch (UnknownParticleIDException x)
            {
                if (x.getPDGID() == 92) return 0;
                else throw x;
            }
        }

        public float[] getSpin()
        {
            return spin;
        }

        public int[] getColorFlow()
        {
            return colorFlow;
        }
        
        public float[] getMomentumAtEndpoint() 
        {
            throw new NotImplementedException("The getMomentumAtEndpoint method is not implemented for this class.");
        }
    }
    private static class Status implements SimulatorStatus
    {
        public boolean vertexIsNotEndpointOfParent()
        {
            return false;
        }

        public boolean isStopped()
        {
            return false;
        }

        public boolean isDecayedInTracker()
        {
            return false;
        }

        public boolean isDecayedInCalorimeter()
        {
            return false;
        }

        public boolean isCreatedInSimulation()
        {
            return false;
        }

        public boolean isBackscatter()
        {
            return false;
        }

        public boolean hasLeftDetector()
        {
            return false;
        }

        public int getValue()
        {
            return 0;
        }
    }
}