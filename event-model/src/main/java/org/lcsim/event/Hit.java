package org.lcsim.event;

import org.lcsim.detector.DetectorIdentifierHelper;
import org.lcsim.detector.HasDetectorElement;
import org.lcsim.detector.identifier.Identifiable;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.geometry.Subdetector;
import hep.physics.vec.Hep3Vector;

/**
 * This is a common API for hit objects in events such
 * as CalorimeterHit, etc.
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public interface Hit extends HasDetectorElement, Identifiable {

    /**
     * Get the position vector of the hit.
     * @return The position of the hit as a vector.
     */
    Hep3Vector getPositionVec();

    /**
     * Get the position of the hit as a double array of length 3.
     * @return The position of the hit.
     */
    double[] getPosition();
	
    /**
     * Convenience method to get the Subdetector's IdentifierHelper.
     * @return The subdetector identifier helper.
     */
    DetectorIdentifierHelper getDetectorIdentifierHelper();
    
    /**
     * Get the collection meta data reference for this object.
     * @return The collection meta data.
     */
    LCMetaData getMetaData();
    
    /**
     * Set the collection meta data of this object.
     */
    void setMetaData(LCMetaData meta);
   
    /**
     * Get the subdetector of this hit.
     * @return The subdetector of the hit.
     */
    Subdetector getSubdetector();
    
    /**
     * Get the IDDecoder for the hit.
     * @return The IDDecoder for the hit.
     */
    IDDecoder getIDDecoder();
    
    /**
     * Get the subdetector's system ID.
     * @return The subdetector's system ID.
     */
    int getSystemId();
	
    /**
     * Get the barrel flag (collider-detector specific).
     * @return The barrel flag.
     */
    BarrelEndcapFlag getBarrelEndcapFlag();
	
    /**
     * Get the layer number of the hit.
     * @return The layer number.
     */
    int getLayerNumber();
	
    /**
     * Get a field value using the helper.
     * @param field The name of the field.
     * @return A field value.
     */
    int getIdentifierFieldValue(String field);
}
