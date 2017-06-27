package org.lcsim.plugin.browser;

import java.util.Arrays;
import org.lcsim.event.StringVec;


/**
 *
 * @author tonyj
 */
class StringVecTableModel extends GenericTableModel
{
   private static final String[] columns = {"I","Size","Data"};
   private static Class klass = StringVec.class;

   StringVecTableModel()
   {
      super(klass,columns);
   }
   public Object getValueAt(int row, int column)
   {
      StringVec vec = (StringVec) getData(row);
      switch (column) {
          case 0: return row;
          case 1: return vec.size();
          case 2: return Arrays.toString(vec.toStringArray());
          default: return null;
      }
   }
   
   public Class getColumnClass(int column)
   {
      return column==2 ? String.class : Integer.class;
   }
}
