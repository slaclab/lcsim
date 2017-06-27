package org.lcsim.event;

import java.util.Comparator;

/**
 * <p>
 * This class represents a hit in a calorimeter detector which has an energy 
 * deposition in GeV, a time in nanoseconds, and an ID identifying its cell
 * or channel in the detector.
 * <p>
 * The super-interface gives access to subdetector, identifiers, iddecoder, etc.
 * @see org.lcsim.event.Hit
 * 
 * @author Tony Johnson <tonyj@slac.stanford.edu>
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public interface CalorimeterHit extends Hit {
    
   /**
    * Get the raw energy deposition in GeV.
    * @return The raw energy deposition.
    */
   public double getRawEnergy();
   
   /**
    * Get the corrected energy deposition in GeV.
    * @return The corrected energy deposition
    */
   public double getCorrectedEnergy();
   
   /**
    * Get the energy error.  (Units???)
    * @return The energy error.
    */
   public double getEnergyError();
   
   /**
    * Get the ID of the cell or channel. 
    * This can be converted to a physical position using a IDDecoder object obtained from the hit
    * or from the collection's MetaData object.
    * @return The cell or channel ID of the hit.
    */
   public long getCellID();
   
   /**
    * Get the time of the <b>earliest</b> energy contribution to this hit in nanoseconds.
    * @return The hit time in nanoseconds.
    */
   public double getTime();

   /**
    * Get the position of the hit in millimeters with the global coordinate system. 
    * If the hit position is stored in the LCIO data, this will be returned directly. 
    * Otherwise the IDDecoder is used to get the hit position from the hit ID.
    * @return The position in millimeters as a double array of length 3.
    */
   public double[] getPosition();

   /**
    * Get the type of the hit.
    * Mapping of integer types to type names through collection parameters
    * "CalorimeterHitTypeNames" and "CalorimeterHitTypeValues".
    * @return The type of the hit.
    */
   public int getType();
   
   /**
    * CalorimeterHit corrected energy comparator.
    */
   public static class CorrectedEnergyComparator implements Comparator<CalorimeterHit> {
       /**
        * Compare the corrected energy of the hits using the <code>Double.compare</code> method.
        * @return -1 if o1's energy is less than o2's, 1 if o1's is greater than o2's, and 0 if equal.
        */
       @Override
       public int compare(CalorimeterHit o1, CalorimeterHit o2) {
           return Double.compare(o1.getCorrectedEnergy(), o2.getCorrectedEnergy());
       }
   }   
   
   /**
    * CalorimeterHit raw energy comparator.
    */
   public static class RawEnergyComparator implements Comparator<CalorimeterHit> {
       /**
        * Compare the raw energy of the hits using the <code>Double.compare</code> method.
        * @return -1 if o1's energy is less than o2's, 1 if o1's is greater than o2's, and 0 if equal.
        */
       @Override
       public int compare(CalorimeterHit o1, CalorimeterHit o2) {
           return Double.compare(o1.getRawEnergy(), o2.getRawEnergy());
       }
   }   
   
   /**
    * CalorimeterHit time comparator.
    */
   static class TimeComparator implements Comparator<CalorimeterHit> {
       /**
        * Compare the time of the hits using the <code>Double.compare</code> method.
        * @return -1 if o1's time is less than o2's, 1 if o1's is greater than o2's, and 0 if equal.
        */
       @Override
       public int compare(CalorimeterHit o1, CalorimeterHit o2) {
           return Double.compare(o1.getTime(), o2.getTime());
       }
   }
   
}