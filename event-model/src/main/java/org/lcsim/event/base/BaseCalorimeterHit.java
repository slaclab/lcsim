package org.lcsim.event.base;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.Identifier;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.geometry.IDDecoder;

/**
 * Base implementation of CalorimeterHit.
 * 
 * @author Tony Johnson <tonyj@slac.stanford.edu>
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class BaseCalorimeterHit extends BaseHit implements CalorimeterHit {

    protected double rawEnergy;
    protected double correctedEnergy;
    protected double energyError;
    protected double time;
    protected long id;
    protected int type;

    protected BaseCalorimeterHit() {
    }
      
    public BaseCalorimeterHit(
            double rawEnergy, 
            double correctedEnergy, 
            double energyError, 
            double time, 
            long id, 
            Hep3Vector positionVec, 
            int type,
            LCMetaData metaData) {
        
        this.rawEnergy = rawEnergy;
        this.correctedEnergy = correctedEnergy;
        this.energyError = energyError;
        this.time = time;
        this.id = id;
        this.positionVec = positionVec;
        this.type = type;
                        
        setMetaData(metaData);        
    }
    
    public BaseCalorimeterHit(BaseCalorimeterHit hit) {
        this.rawEnergy = hit.getRawEnergy();
        this.energyError = hit.getEnergyError();
        this.time = hit.getTime();
        this.type = hit.getType();
        this.id = hit.getCellID();
        if (hit.positionVec != null) {
            this.positionVec = new BasicHep3Vector(hit.getPositionVec().x(), hit.getPositionVec().y(), hit.getPositionVec().z());
        }
        if (hit.metaData != null) {
            setMetaData(hit.getMetaData());
        }
        try {
            this.correctedEnergy = hit.getCorrectedEnergy();
        } catch (Exception e) {            
            // This can conceivably fail sometimes without valid sampling fraction conditions.
        }
        if (hit.detectorElement != null) {
            this.detectorElement = hit.getDetectorElement();
        }
    }
    
    public BaseCalorimeterHit clone() {
        return new BaseCalorimeterHit(this);
    }

    public double getTime() {
        return time;
    }

    public int getType() {
        return type;
    }

    public double getRawEnergy() {
        return rawEnergy;
    }
    
    public double getCorrectedEnergy() {
        return correctedEnergy;
    }
      
    public long getCellID() {
        return id;
    }

    public int getLayerNumber() {
        IDDecoder decoder = getIDDecoder();
        decoder.setID(id);
        return decoder.getLayer();
    }

    public IIdentifier getIdentifier() {
        if (packedID == null) {
            packedID = new Identifier(id);
        }
        return packedID;
    }

    public double getEnergyError() {
        return energyError;
    }

    public void setTime(double time) { 
        this.time = time; 
    }
             
    protected void calculateCorrectedEnergy() {
        getIDDecoder().setID(id);
        correctedEnergy = SamplingFractionManager.defaultInstance().getCorrectedEnergy(rawEnergy, getIDDecoder().getLayer(), getSubdetector());
    }
                             
    /**
     * Conversion to String for printout.
     * @return String output.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("type: " + type + ", energy: " + correctedEnergy + ", energyError: " + energyError + ", ");
        sb.append("position: (" + positionVec.x() + ", " + positionVec.y() + ", " + positionVec.z() + "), ");
        sb.append("id: 0x" + getIdentifier().toHexString() + ", time: " + time);
        return sb.toString();
    }
}
