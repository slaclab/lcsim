package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import hep.io.sio.SIORef;
import hep.physics.particle.properties.ParticlePropertyManager;
import hep.physics.particle.properties.ParticleType;
import hep.physics.particle.properties.UnknownParticleIDException;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.BasicHepLorentzVector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.HepLorentzVector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.lcsim.event.MCParticle;

/**
 * SIO implementation of {@link org.lcsim.event.MCParticle}.
 * @author Tony Johnson
 * @author Jeremy McCormick
 * @version $Id: SIOMCParticle.java,v 1.13 2011/08/24 18:51:18 jeremy Exp $
 */
public class SIOMCParticle implements MCParticle
{
    private int pdg;
    private int generatorStatus;
    private int simulatorStatus;
    private Hep3Vector vertex;
    private Hep3Vector endPoint;
    private HepLorentzVector p;
    private float mass;
    private float charge;
    private float time;
    private List<MCParticle> daughters;
    private List<MCParticle> parents;
    private SimulatorStatus simStatus = new Status();
    protected float[] spin = new float[3];
    protected int[] colorFlow = new int[2];
    protected float[] momentumAtEndpoint = new float[3];

    private List temp = new ArrayList();

    SIOMCParticle(SIOInputStream in, int flags, int version) throws IOException
    {
        boolean hasEndPoint = false;
        in.readPTag(this);

        if (version == 8)
        {
            in.readPntr();
            in.readPntr();

            int nDaughters = in.readInt();
            for (int i = 0; i < nDaughters; i++)
                temp.add(in.readPntr());

            hasEndPoint = (nDaughters == 0);
        }
        else
        {
            int nParents = in.readInt();
            for (int i = 0; i < nParents; i++)
            {
                temp.add(in.readPntr());
            }
        }
        pdg = in.readInt();
        generatorStatus = in.readInt();
        if (version != 8)
        {
            simulatorStatus = in.readInt();
            hasEndPoint = LCIOUtil.bitTest(simulatorStatus, Status.BITEndpoint);
            simulatorStatus = LCIOUtil.bitSet(simulatorStatus, Status.BITEndpoint, false);
        }
        vertex = new BasicHep3Vector(in.readDouble(), in.readDouble(), in.readDouble());

        if (version > 1002)
            time = in.readFloat();

        Hep3Vector momentum = new BasicHep3Vector(in.readFloat(), in.readFloat(), in.readFloat());
        mass = in.readFloat();
        double e = Math.sqrt(mass * mass + momentum.magnitudeSquared());
        p = new BasicHepLorentzVector(e, momentum);
        charge = in.readFloat();
        if (hasEndPoint)
        {
            endPoint = new BasicHep3Vector(in.readDouble(), in.readDouble(), in.readDouble());
            
            // Read endpoint momentum for versions 2.6 and greater.
            if (version > 2006) {
                momentumAtEndpoint[0] = in.readFloat();
                momentumAtEndpoint[1] = in.readFloat();
                momentumAtEndpoint[2] = in.readFloat();
            }
        }

        // Spin and colorflow for versions 1.60 and greater.
        if (version >= 1060)
        {
            spin[0] = in.readFloat();
            spin[1] = in.readFloat();
            spin[2] = in.readFloat();

            colorFlow[0] = in.readInt();
            colorFlow[1] = in.readInt();
        }
    }

    void resolve(int version)
    {
        if (version == 8)
        {
            for (int i = 0; i < temp.size(); i++)
            {
                SIOMCParticle daughter = (SIOMCParticle) ((SIORef) temp.get(i)).getObject();
                this.addDaughter(daughter);
            }
        }
        else
        {
            for (int i = 0; i < temp.size(); i++)
            {
                SIOMCParticle parent = (SIOMCParticle) ((SIORef) temp.get(i)).getObject();
                parent.addDaughter(this);
            }
        }
        temp = null;
    }

    private void addDaughter(SIOMCParticle child)
    {
        if (daughters == null)
            daughters = new ArrayList<MCParticle>();
        daughters.add(child);
        child.addParent(this);
    }

    private void addParent(SIOMCParticle parent)
    {
        if (parents == null)
            parents = new ArrayList<MCParticle>();
        parents.add(parent);
    }

    public void setTime(double newTime)
    {
        time = (float) newTime;
    }

    public ParticleType getType()
    {
        return ParticlePropertyManager.getParticlePropertyProvider().get(pdg);
    }

    public double getProductionTime()
    {
        return time;
    }

    public List<MCParticle> getParents()
    {
        return parents == null ? Collections.EMPTY_LIST : parents;
    }

    public double getCharge()
    {
        return charge;
    }

    public List<MCParticle> getDaughters()
    {
        return daughters == null ? Collections.EMPTY_LIST : daughters;
    }

    public double getEnergy()
    {
        return p.t();
    }

    public int getGeneratorStatus()
    {
        return generatorStatus;
    }

    public double getMass()
    {
        return mass;
    }

    public Hep3Vector getMomentum()
    {
        return p.v3();
    }

    public Hep3Vector getOrigin()
    {
        return vertex;
    }

    public double getOriginX()
    {
        return vertex.x();
    }

    public double getOriginY()
    {
        return vertex.y();
    }

    public double getOriginZ()
    {
        return vertex.z();
    }

    public double getPX()
    {
        return p.v3().x();
    }

    public double getPY()
    {
        return p.v3().y();
    }

    public double getPZ()
    {
        return p.v3().z();
    }

    public Hep3Vector getEndPoint()
    {
        if (endPoint == null)
        {
            for (MCParticle daughter : getDaughters())
            {
                if (!daughter.getSimulatorStatus().vertexIsNotEndpointOfParent())
                {
                    return daughter.getOrigin();
                }
            }
            throw new RuntimeException("MCParticle end point not available");
        }
        else
            return endPoint;
    }

    public SimulatorStatus getSimulatorStatus()
    {
        return simStatus;
    }

    static void write(MCParticle particle, SIOOutputStream out, int flags) throws IOException
    {
        out.writePTag(particle);
        List<MCParticle> parents = particle.getParents();
        out.writeInt(parents.size());

        for (MCParticle parent : parents)
        {
            out.writePntr(parent);
        }

        out.writeInt(particle.getType().getPDGID());
        out.writeInt(particle.getGeneratorStatus());
        boolean shouldExplicityWriteOutEndPoint = true;
        for (MCParticle daughter : particle.getDaughters())
        {
            if (!daughter.getSimulatorStatus().vertexIsNotEndpointOfParent())
            {
                shouldExplicityWriteOutEndPoint = false;
            }
        }
        Hep3Vector endPoint = null;
        if (shouldExplicityWriteOutEndPoint)
        {
            try
            {
                endPoint = particle.getEndPoint();
            }
            catch (Exception x)
            {
                shouldExplicityWriteOutEndPoint = false;
            }
        }
        int simStatus = particle.getSimulatorStatus().getValue();
        simStatus = LCIOUtil.bitSet(simStatus, Status.BITEndpoint, shouldExplicityWriteOutEndPoint);
        out.writeInt(simStatus);

        out.writeDouble(particle.getOriginX());
        out.writeDouble(particle.getOriginY());
        out.writeDouble(particle.getOriginZ());

        out.writeFloat((float) particle.getProductionTime());

        out.writeFloat((float) particle.getPX());
        out.writeFloat((float) particle.getPY());
        out.writeFloat((float) particle.getPZ());
        out.writeFloat((float) particle.getMass());
        try
        {
            out.writeFloat((float) particle.getCharge());
        }
        catch (UnknownParticleIDException x)
        {
            out.writeFloat(0);
        }
        if (shouldExplicityWriteOutEndPoint)
        {
            out.writeDouble(endPoint.x());
            out.writeDouble(endPoint.y());
            out.writeDouble(endPoint.z());
            
            // Write momentum at endpoint supported in LCIO version 2.6 and greater.
            out.writeFloat(particle.getMomentumAtEndpoint()[0]);
            out.writeFloat(particle.getMomentumAtEndpoint()[1]);
            out.writeFloat(particle.getMomentumAtEndpoint()[2]);
        }

        // Spin.
        float[] spin = particle.getSpin();
        out.writeFloat((float) spin[0]);
        out.writeFloat((float) spin[1]);
        out.writeFloat((float) spin[2]);

        // Color flow.
        int[] colorFlow = particle.getColorFlow();
        out.writeInt(colorFlow[0]);
        out.writeInt(colorFlow[1]);
    }

    public int getPDGID()
    {
        return pdg;
    }

    public HepLorentzVector asFourVector()
    {
        return p;
    }

    public float[] getSpin()
    {
        return spin;
    }

    public int[] getColorFlow()
    {
        return colorFlow;
    }
    
    public float[] getMomentumAtEndpoint() {
        return momentumAtEndpoint;
    }

    private class Status implements SimulatorStatus
    {
        public boolean vertexIsNotEndpointOfParent()
        {
            return (simulatorStatus & (1 << BITVertexIsNotEndpointOfParent)) != 0;
        }

        public boolean isStopped()
        {
            return (simulatorStatus & (1 << BITStopped)) != 0;
        }

        public boolean isDecayedInTracker()
        {
            return (simulatorStatus & (1 << BITDecayedInTracker)) != 0;
        }

        public boolean isDecayedInCalorimeter()
        {
            return (simulatorStatus & (1 << BITDecayedInCalorimeter)) != 0;
        }

        public boolean isCreatedInSimulation()
        {
            return (simulatorStatus & (1 << BITCreatedInSimulation)) != 0;
        }

        public boolean isBackscatter()
        {
            return (simulatorStatus & (1 << BITBackscatter)) != 0;
        }

        public boolean hasLeftDetector()
        {
            return (simulatorStatus & (1 << BITLeftDetector)) != 0;
        }

        public int getValue()
        {
            return simulatorStatus;
        }

        // define the bit positions for the simulation flag
        private final static int BITEndpoint = 31;
        private final static int BITCreatedInSimulation = 30;
        private final static int BITBackscatter = 29;
        private final static int BITVertexIsNotEndpointOfParent = 28;
        private final static int BITDecayedInTracker = 27;
        private final static int BITDecayedInCalorimeter = 26;
        private final static int BITLeftDetector = 25;
        private final static int BITStopped = 24;
    }
}
