package org.lcsim.plugin.browser;

import java.util.Arrays;
import org.lcsim.event.IntVec;


/**
 *
 * @author tonyj
 */
class IntVecTableModel extends GenericTableModel
{
   private static final String[] columns = {"I","Size","Data"};
   private static Class klass = IntVec.class;

   IntVecTableModel()
   {
      super(klass,columns);
   }
   public Object getValueAt(int row, int column)
   {
      IntVec vec = (IntVec) getData(row);
      switch (column) {
          case 0: return row;
          case 1: return vec.size();
          case 2: return Arrays.toString(vec.toIntArray());
          default: return null;
      }
   }
   
   public Class getColumnClass(int column)
   {
      return column==2 ? String.class : Integer.class;
   }
}
