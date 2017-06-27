package org.lcsim.plugin.browser;
import org.lcsim.event.TrackerPulse;


/**
 *
 * @author tonyj
 */
class TrackerPulseTableModel extends GenericTableModel
{
   private static final String[] columns = {"CellID","Time","Charge","Quality"}; //,"TrackerData"};
   private static Class klass = TrackerPulse.class;

   TrackerPulseTableModel()
   {
      super(klass,columns);
   }
}
