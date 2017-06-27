package org.lcsim.event;
    
/** TrackerData contains the corrected (calibrated) raw tracker data.
 *  @see TrackerRawData
 *  @see TrackerPulse
 * 
 * @author gaede
 * @version $Id: TrackerData.java,v 1.2 2008/05/23 06:53:35 jeremy Exp $
 */

public interface TrackerData 
{
    /** Returns the first detector specific (geometrical) cell id.
     */
    long getCellID();

    /** Returns a time measurement associated with the adc values, e.g. the 
     *  t0 of the spectrum for the TPC. Subdetector dependent.
     */
    double getTime();

    /** The corrected (calibrated) FADC spectrum. 
     */
    double[] getChargeValues();
} // class or interface
