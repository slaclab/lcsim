package org.lcsim.event;

import java.util.List;

/** 
 * A generic tracker hit to be used by pattern recognition.
 *
 * @author Tony Johnson
 * @author Jeremy McCormick
 * @version $Id: TrackerHit.java,v 1.8 2011/08/24 18:51:17 jeremy Exp $
 */

public interface TrackerHit
{
    /** 
     * The hit position [mm].
     */
    double[] getPosition();

    /**
     * Covariance of the position (x,y,z)
     */
    double[] getCovMatrix();

    /** The dE/dx of the hit [GeV].
     */
    double getdEdx();
    
    /**
     * The measured edep error [GeV].
     */
    double getEdepError();
    
    /**
     * The quality of the hit.
     */
    int getQuality();

    /** 
     * The  time of the hit [ns].
     */     
    double getTime();

    /** 
     * Type of hit. Mapping of integer types to type names
     * through collection parameters "TrackerHitTypeNames"
     * and "TrackerHitTypeValues".
     */
    int getType();

    /** The raw data hits.
     * Check getType() to get actual data type.
     */
    List getRawHits();
    
    /**
     * Get the cell ID.
     * @return The cell ID.
     */
    long getCellID();
}