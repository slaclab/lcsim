package org.lcsim.event;

/** Tracker pulses as computed from  TrackerData objects or as directly measured by a specific
 *  subdetector. Typically TrackerHIts are created from TrackerPulses by some sort of clustering.
 *  @see TrackerRawData
 *  @see TrackerData
 * 
 * @author gaede
 * @version $Id: TrackerPulse.java,v 1.2 2008/05/23 06:53:35 jeremy Exp $
 */

public interface TrackerPulse 
{
    /** Returns the first detector specific (geometrical) cell id.
     */
    long getCellID();

    /** The time of the pulse - arbitrary units.
     */
    double getTime();

    /** The integrated charge of the pulse - arbitrary units.
     */
    double getCharge();

    /** The quality bit flag of the pulse - check/set collection parameters 
     *  TrackerPulseQualityNames and TrackerPulseQualityNamesValues.
     */
    int getQuality();

    /** Optionally the TrackerData that has been used to create the pulse
     *  can be stored with the pulse - NULL if none. Check the quality bits for reason
     *  why the spectrum has been stored for the pulse.
     */
    TrackerData getTrackerData();
} // class or interface
