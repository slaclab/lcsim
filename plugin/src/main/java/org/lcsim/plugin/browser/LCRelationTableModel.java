package org.lcsim.plugin.browser;
import org.lcsim.event.LCRelation;

/**
 *
 * @author tonyj
 */
public class LCRelationTableModel extends GenericTableModel
{
   private static final String[] columns = {"From","To","Weight"};
   private static Class klass = LCRelation.class;

   LCRelationTableModel()
   {
      super(klass,columns);
   }
   
}
