/*
 * BaseMCParticle.java
 *
 * Created on March 30, 2006, 8:58 AM
 *
 * $Id: BaseMCParticle.java,v 1.7 2012/07/10 07:20:27 grefe Exp $
 */

package org.lcsim.event.base;

import hep.physics.particle.BasicParticle;
import hep.physics.particle.properties.ParticleType;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.HepLorentzVector;

import org.apache.commons.lang3.NotImplementedException;
import org.lcsim.event.MCParticle;

/**
 * A base class implementatin of the MCParticle interface. 
 * Extends BasicParticle to fulfill the Particle interface. 
 * @author Norman Graf
 */
public class BaseMCParticle extends BasicParticle implements MCParticle
{
   
    protected Hep3Vector _endPoint = new BasicHep3Vector(0., 0., 0.);
    protected SimulatorStatus _status;
    protected double charge; 
    protected float[] spin = new float[3];
    protected int[] colorFlow = new int[2];
    protected float[] momentumAtEndpoint = new float[3];
    protected double time;
    
    /**
     * Creates a new instance of BaseMCParticle
     * @param origin The (x,y,z) point of origin for this particle in mm.
     * @param p The four momentum of this particle in GeV.
     * @param ptype The particle type.
     * @param status the integer status of this particle.
     * @param time the time of creation of this particle in ns.
     */
    public BaseMCParticle(Hep3Vector origin,HepLorentzVector p,ParticleType ptype,int status, double time)
    {
        super(origin, p, ptype, status, time);
        this.time = time;
    }
    
    /**
     * Overrides the charge from the particle type.
     */
    // FIXME Should be offered by BasicParticle instead.
    public void setCharge(double charge) {
		this.charge = charge;
	}
    
    @Override
    public double getCharge() {
    	return Double.isNaN(charge) ? super.getCharge() : charge;
    }
    
    /**
     * If this point has interacted, set its end point.
     * @param p The (x,y,z) end point in mm.
     */
    public void setEndPoint(Hep3Vector p)
    {
        _endPoint = p;
    }
    
    /**
     * @param status The Geant4 status of the particle
     */
    public void setSimulatorStatus(SimulatorStatus status) {
    	_status = status;
    }
    
    public void setSimulatorStatus(int value) {
    	_status = new Status(value);
    }
    
    public void setProductionTime(double time)
    {
    	this.time = time;
    }
    
    @Override
    public double getProductionTime() {
    	return time;
    }
    
    public String toString()
    {
        // TODO fix this to call super.toString() when BasicParticle implements toString()
        String className = getClass().getName();
        int lastDot = className.lastIndexOf('.');
        if(lastDot!=-1)className = className.substring(lastDot+1);
        StringBuffer sb = new StringBuffer(className+": "+getType().getName()+"\n");
        sb.append("pdgId: "+getPDGID()+"\n");
        sb.append("(x,y,z): ("+getOriginX()+", "+getOriginY()+", "+getOriginZ()+")\n");
        sb.append("(px,py,pz): ("+getPX()+", "+getPY()+", "+getPZ()+")\n");
        return sb.toString();
    }
    
// MCParticle interface
    
    /**
     * If this event has been simulated by Geant4 this method will return
     * the simulation status
     * @return 
     */
    public SimulatorStatus getSimulatorStatus()
    {
        return _status;
    }
    /**
     * The endpoint of the simulated track. Note this may not always be available,
     * in which case this method may throw an exception.
     * @return 
     */
    public Hep3Vector getEndPoint()
    {
        return _endPoint;
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
    
    public void setMomentumAtEndpoint(float[] momentumAtEndpoint) {
        this.momentumAtEndpoint = momentumAtEndpoint;
    }
        
    protected class Status implements SimulatorStatus {
    	int simulatorStatus;
    	public Status(int v) {
    		simulatorStatus = v;
    	}
        public boolean vertexIsNotEndpointOfParent()
        {
           return (simulatorStatus & (1<<BITVertexIsNotEndpointOfParent)) != 0;
        }
        
        public boolean isStopped()
        {
           return (simulatorStatus & (1<<BITStopped)) != 0;
        }
        
        public boolean isDecayedInTracker()
        {
           return (simulatorStatus & (1<<BITDecayedInTracker)) != 0;
        }
        
        public boolean isDecayedInCalorimeter()
        {
           return (simulatorStatus & (1<<BITDecayedInCalorimeter)) != 0;
        }
        
        public boolean isCreatedInSimulation()
        {
           return (simulatorStatus & (1<<BITCreatedInSimulation)) != 0;
        }
        
        public boolean isBackscatter()
        {
           return (simulatorStatus & (1<<BITBackscatter)) != 0;
        }
        
        public boolean hasLeftDetector()
        {
           return (simulatorStatus & (1<<BITLeftDetector)) != 0;
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
