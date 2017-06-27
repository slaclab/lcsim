package org.lcsim.event;


/** A generic TPCHit for real data. Optionaly contains an integer array
 *  with raw data that is implementation dependent, e.g. the FADC read out 
 *  of the corresponding bump. 
 *  If generic TrackerHits from reconstruction want to point back to
 *  TPCHit objects, these have to be stored in a collection
 *  with flag( bit LCIO::TPCBIT_PTR)==1.
 * 
 * @author gaede
 * @version $Id: TPCHit.java,v 1.3 2008/05/23 06:53:35 jeremy Exp $
 */

public interface TPCHit 
{
    /** Returns the detector specific cell id.
     */
    public int getCellID();

    /** Returns the  time of the hit.
     */
    public double getTime();

    /** Returns the integrated charge of the hit.
     */
    public double getCharge();

    /** Returns a quality flag for the hit.
     */
    public int getQuality();

    /** Return the number of raw data (32-bit) words stored for the hit.
     *  Check the flag word (bit TPCBIT_RAW) of the collection if raw data is
     *  stored at all.
     */
    public int getNRawDataWords();

    /** Return the raw data (32-bit) word at i.
     *  Check the flag word (bit TPCBIT_RAW) of the collection if raw data is
     *  stored at all.
     */
    public int getRawDataWord(int i);
} // class or interface

