package org.lcsim.event.base;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.DetectorElementStore;
import org.lcsim.detector.DetectorIdentifierHelper;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IDetectorElementContainer;
import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.Hit;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.geometry.Subdetector;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;

/**
 * This is a base class for hit-like objects in a detector.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public abstract class BaseHit implements Hit {
    
    protected LCMetaData metaData;
    protected IExpandedIdentifier expandedID;
    protected IIdentifier packedID;
    protected IDetectorElement detectorElement;
    protected Hep3Vector positionVec;

    /**
     * Get the metadata associated with this hit.     
     * @return The hit's metadata.
     */
    public LCMetaData getMetaData() {
        return metaData;
    }

    /**
     * Set the metadata associated with this hit.
     */
    public void setMetaData(LCMetaData meta) {
                
        this.metaData = meta;               
    }
    
    /**
     * Get the subdetector of this hit.     
     * @return The Subdetector of this hit.
     */
    public Subdetector getSubdetector() {
        return getIDDecoder().getSubdetector();
    }

    /**
     * Get the {@see org.lcsim.detector.IDetectorElement} associated with this hit.     
     * @return The IDetectorElement for this hit.
     */
    public IDetectorElement getDetectorElement() {
        if (detectorElement == null) {
            // Attempt to lookup the geometry using this exact ID.
            detectorElement = findDetectorElement(getIdentifier());
            if (detectorElement == null) {
                // Presumably if the ID lookup fails, this means it has non-geometric fields,
                // so use the global position instead.
                if (getPositionVec() != null) {
                    detectorElement = findDetectorElement(getPositionVec());
                }                
                // Just leave it as null!
            }
        }
        return detectorElement;
    }
    
    /**
     * Set the @see org.lcsim.detector.IDetectorElement of this hit. By default, this will not override an
     * existing value.
     */
    public void setDetectorElement(IDetectorElement de) {
        this.detectorElement = de;        
    }

    /**
     * Get the {@see org.lcsim.detector.identifier.IExpandedIdentifier} of this hit.     
     * @return The expanded identifier of this hit.
     */
    public IExpandedIdentifier getExpandedIdentifier() {
        if (expandedID == null) {
            expandedID = getIdentifierHelper().unpack(getIdentifier());
        }
        return expandedID;
    }

    /**
     * Get the identifier of this hit.     
     * @return The identifier of this hit.
     */
    public IIdentifier getIdentifier() {
        return packedID;
    }

    /**
     * Get the identifier helper of this hit.     
     * @return The hit's identifier helper.
     */
    public IIdentifierHelper getIdentifierHelper() {
        return getSubdetector().getDetectorElement().getIdentifierHelper();
    }

    /**
     * Get the detector identifier helper of this hit.   
     * @return The hit's detector identifier helper.
     */
    public DetectorIdentifierHelper getDetectorIdentifierHelper() {
        return (DetectorIdentifierHelper)getIdentifierHelper();
    }

    /**
     * Get the {@see org.lcsim.geometryIDDecoder} of this hit.     
     * @return The hit's id decoder.
     */
    public IDDecoder getIDDecoder() {
        return metaData.getIDDecoder();
    }

    /**
     * Get the layer number of this hit.    
     * @return The layer number of this hit.
     */
    public int getLayerNumber() {
        return getIdentifierHelper().getValue(getIdentifier(), "layer");
    }

    /**
     * Get the barrel flag of this hit.    
     * @return The barrel flag of this hit.
     */
    public BarrelEndcapFlag getBarrelEndcapFlag() {
        return BarrelEndcapFlag.createBarrelEndcapFlag(getIdentifierHelper().getValue(getIdentifier(), "barrel"));
    }

    /**
     * Get the system id of this hit.
     * @return The system id of this hit.
     */
    public int getSystemId() {
        return getIdentifierHelper().getValue(getIdentifier(), "system");
    }

    /**
     * Get a field value from the hit's identifier.     
     * @return The field value from the hit's identifier.
     */
    public int getIdentifierFieldValue(String field) {
        return getIdentifierHelper().getValue(getIdentifier(), field);
    }
                  
    /**    
     * Get the position of the hit.  This may be different than the position of the hit's DetectorElement.
     * @return The position of this hit as a double array of size 3.
     */
    public double[] getPosition() {
        if (positionVec == null) {
            calculatePosition();
        }
        return positionVec.v();
    }
    
    /**
     * Get the position of this hit in mm as a {@see hep.physics.vec.Hep3Vector}.
     * @return The position vector of the hit in mm.
     */
    public Hep3Vector getPositionVec() { 
        return positionVec;
    }       
    
    /**
     * Calculate the position of the hit, depending on what information is available on the object.
     */
    protected void calculatePosition() {
        // Is the position not set already?
        if (positionVec == null) {
            
            // Is the detector object not set yet?
            if (detectorElement == null) {
                // Try to find a detector object to link to this hit.
                detectorElement = findDetectorElement(getIdentifier());
            }
            
            // Does the hit ID match the DetectorElement exactly?
            if (detectorElement != null && detectorElement.getIdentifier().equals(getIdentifier())) {
                // Set the position from the DetectorElement.
                this.positionVec = detectorElement.getGeometry().getPosition();                
            } else {
                // This is what should happen for IDs with virtual segmentation values.
                // The IDDecoder needs to be used here if it is set on the hit.
                if (this.metaData != null) {
                    IDDecoder decoder = this.getIDDecoder();
                    if (decoder != null) {
                        decoder.setID(this.getIdentifier().getValue());
                        this.positionVec = new BasicHep3Vector(decoder.getPosition());
                    }
                }
            }
        }
    }
            
    /**
     * Find a DetectorElement by its identifier.
     * @param id The identifier.
     * @return The DetectorElement matching the identifier.
     */
    protected IDetectorElement findDetectorElement(IIdentifier id) {
        IDetectorElement mommy = null;
        if (this.metaData != null) {
            // Use subdetector's volume as top.
            mommy = this.getIDDecoder().getSubdetector().getDetectorElement();
        } 
        if (mommy == null) {
            // Use the world volume as top.
            mommy = ((DetectorElement) DetectorElementStore.getInstance().get(0)).getTop();
        }       
        
        // Find DetectorElements with exactly matching ID.
        IDetectorElementContainer detectorElements = mommy.findDetectorElement(id);
        
        // FIXME: Beware! The container itself can be null, which should be fixed (detector-framework).
        if (detectorElements != null && detectorElements.size() == 1) {
            return detectorElements.get(0); 
        } else {
            return null;
        }
    }
    
    /**
     * Find a DetectorElement by its global Cartesian position.
     * @param position The position.
     * @return The DetectorElement at the position.
     */
    protected IDetectorElement findDetectorElement(Hep3Vector position) {                                                                                     
        IDetectorElement mommy = null;                                                                                                                      
        if (this.metaData != null) {                                                                                                                        
            // Use subdetector's volume as top.                                                                                                             
            mommy = this.getIDDecoder().getSubdetector().getDetectorElement();                                                                              
        }                                                                                                                                                   
        if (mommy == null) {                                                                                                                                
            // Use the world volume.                                                                                                                        
            mommy = ((DetectorElement) DetectorElementStore.getInstance().get(0)).getTop().findDetectorElement(getPositionVec());                           
        }                                                                                                                                                   
        return mommy.findDetectorElement(this.positionVec);                                                                                                 
    }
}