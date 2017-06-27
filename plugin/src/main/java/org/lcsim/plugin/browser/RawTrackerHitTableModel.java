package org.lcsim.plugin.browser;
import org.lcsim.event.RawTrackerHit;


/**
 *
 * @author tonyj
 */
class RawTrackerHitTableModel extends GenericTableModel
{
   private static final String[] columns = {"Time","CellID","ADCValues"};
   private static Class klass = RawTrackerHit.class;

   RawTrackerHitTableModel()
   {
      super(klass,columns);
   }
}
