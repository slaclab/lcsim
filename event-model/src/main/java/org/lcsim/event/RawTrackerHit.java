package org.lcsim.event;

import java.util.List;

/**
 * A raw tracker hit, as expected in raw data from a real detector.
 * 
 * @author tonyj
 * @version $Id: RawTrackerHit.java,v 1.10 2008/08/20 01:34:21 jeremy Exp $
 */
public interface RawTrackerHit extends Hit {
    
   /**
    *  Returns a time measurement associated with the adc values.
    *  E.g. the t0 of the spectrum for the TPC. Subdetector dependent.  
    */
    int getTime();

    /**
     * Returns the array of ADCValues. 
     * The array may be of length 1 if this detector only reads out a single value per cell.
     * The value may also need decoding (for example the KPiX chip uses one bit as a
     * range indicator).
     */ 
    short[] getADCValues();    
    
    /** 
     * Returns the detector specific cell id.
     */
    long getCellID();
    
    /** 
     * Returns the associated SimTrackerHit. Note this may be <code>null</code>
     * if there is no associated SimTrackerHit (for example because this is a noise 
     * hit, or because there is no MC information.)
     */  
    List<SimTrackerHit> getSimTrackerHits();
}
