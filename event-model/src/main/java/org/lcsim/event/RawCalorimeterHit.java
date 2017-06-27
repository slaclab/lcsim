package org.lcsim.event;
//import java.util.*;

/**
 * Represents a generic calorimeter hit for real data (or simulation
 * thereof).  In order to reduce the file size you can suppress a
 * (32-bit) pointer tag stored with every hit by setting the
 * flag(LCIO::RCHBIT_NO_PTR)==1. <br>
 *
 * <b>NB: If you apply this flag to reduce the file size you won't be
 * able to point/refer to the RawCalorimeterHits, e.g. from an
 * LCRelation object.</b>
 * 
 * @author Guilherme Lima
 * @version $Id: RawCalorimeterHit.java,v 1.2 2005/04/28 23:27:25 tonyj Exp $
 */
public interface RawCalorimeterHit {

    /** Returns the detector specific (geometrical) cell id.
     */
    public long getCellID();

    /** Returns the amplitude of the hit in ADC counts.
     */
    public int getAmplitude();

    /** Returns a time stamp for the hit. Optional, check/set 
     *  flag(LCIO::RCHBIT_TIME)==1.
     */
    public int getTimeStamp();
} // class or interface
