package org.lcsim.plugin.browser;

import org.lcsim.event.RawCalorimeterHit;

/**
 *
 * @author tonyj
 */
public class RawCalorimeterHitTableModel extends GenericTableModel
{
   private static final String[] columns = {"CellID","Amplitude","TimeStamp"};
   private static Class klass = RawCalorimeterHit.class;

   RawCalorimeterHitTableModel()
   {
      super(klass,columns);
   }
   
}
