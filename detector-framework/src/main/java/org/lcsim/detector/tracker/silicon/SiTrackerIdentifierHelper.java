package org.lcsim.detector.tracker.silicon;

import org.lcsim.detector.DetectorIdentifierHelper;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierDictionary;
import org.lcsim.detector.identifier.IIdentifierField;

/**
 * Decode identifier fields that are specific to the tracking sub-system.
 *
 * @author Jeremy McCormick
 * @version $Id: SiTrackerIdentifierHelper.java,v 1.6 2009/04/24 00:47:42 tknelson Exp $
 */
public class SiTrackerIdentifierHelper
extends DetectorIdentifierHelper
{    
    int moduleIdx=-1;
    int sensorIdx=-1;
    int sideIdx=-1;
    int electrodeIdx=-1;
    
    IIdentifierField moduleField = null;
    IIdentifierField sensorField = null;
    IIdentifierField sideField = null;
    IIdentifierField electrodeField = null;
    
    public SiTrackerIdentifierHelper(IDetectorElement subdetector, IIdentifierDictionary iddict, SystemMap sysMap)
    {
        super(subdetector, iddict, sysMap);

        moduleIdx = iddict.getFieldIndex("module");
        sensorIdx = iddict.getFieldIndex("sensor");
        sideIdx = iddict.getFieldIndex("side");
        electrodeIdx = iddict.getFieldIndex("strip");

        moduleField = iddict.getField(moduleIdx);
        sensorField = iddict.getField(sensorIdx);
        sideField = iddict.getField(sideIdx);
        electrodeField = iddict.getField(electrodeIdx);
    }

	/**
	 * The module number.
	 * @return The module number.
	 */
	public int getModuleValue(IIdentifier id)
    {
	    return moduleField.unpack(id);
    }
	
	/**
	 * The sensor number.
	 * @return The sensor number.
	 */
	public int getSensorValue(IIdentifier id)
    {
        return sensorField.unpack(id);  
    }
	
	/**
	 * The module side number.
	 * @return The module side number.
	 */
	public int getSideValue(IIdentifier id)
    {
        return sideField.unpack(id);   
    }
	   
	/**
	 * The electrode number.
	 * @return The electrode number.
	 */
	public int getElectrodeValue(IIdentifier id)
    {
        return electrodeField.unpack(id);
    }
        
    /**
     * The module number.
     * @return The module number.
     */
    public int getModuleValue(IExpandedIdentifier id)
    {
        return id.getValue(moduleIdx);
    }
    
    /**
     * The sensor number.
     * @return The sensor number.
     */
    public int getSensorValue(IExpandedIdentifier id)
    {
        return id.getValue(sensorIdx); 
    }
    
    /**
     * The module side number.
     * @return The module side number.
     */
    public int getSideValue(IExpandedIdentifier id)
    {
        return id.getValue(sideIdx);
    }
    
    /**
     * The electrode number.
     * @return The electrode number.
     */
    public int getElectrodeValue(IExpandedIdentifier id)
    {
        return id.getValue(electrodeIdx);
    }
    
    /**
     * Get the module field index.
     * @return The module field index.
     */
    public int getModuleIndex()
    {
        return moduleIdx;
    }
    
    /**
     * Get the sensor field index.
     * @return The sensor field index.
     */
    public int getSensorIndex()
    {
        return sensorIdx;
    }
    
    /**
     * Get the side field index.
     * @return side field index.
     */
    public int getSideIndex()
    {
        return sideIdx;
    }
    
    /**
     * Get the electrode field index.
     * @return The trip field index.
     */
    public int getElectrodeIndex()
    {
        return electrodeIdx;
    }        
}