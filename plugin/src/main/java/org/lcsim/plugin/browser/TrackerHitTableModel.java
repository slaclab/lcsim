package org.lcsim.plugin.browser;
import org.lcsim.event.TrackerHit;


/**
 *
 * @author tonyj
 */
class TrackerHitTableModel extends GenericTableModel
{
   private static final String[] columns = {"Position","CovMatrix","dEdx","Time","Type"};
   private static Class klass = TrackerHit.class;

   TrackerHitTableModel()
   {
      super(klass,columns);
   }
}
