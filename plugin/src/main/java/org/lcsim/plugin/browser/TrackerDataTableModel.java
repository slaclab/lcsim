package org.lcsim.plugin.browser;
import org.lcsim.event.TrackerData;


/**
 *
 * @author tonyj
 */
class TrackerDataTableModel extends GenericTableModel
{
   private static final String[] columns = {"CellID","Time","ChargeValues"};
   private static Class klass = TrackerData.class;

   TrackerDataTableModel()
   {
      super(klass,columns);
   }
}
