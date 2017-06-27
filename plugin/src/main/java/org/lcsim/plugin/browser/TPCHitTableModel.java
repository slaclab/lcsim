package org.lcsim.plugin.browser;
import org.lcsim.event.TPCHit;


/**
 *
 * @author tonyj
 */
class TPCHitTableModel extends GenericTableModel
{
   private static final String[] columns = {"Time","CellID","Charge","Quality"};
   private static Class klass = TPCHit.class;

   TPCHitTableModel()
   {
      super(klass,columns);
   }
}
