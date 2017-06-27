package org.lcsim.plugin.browser;

import java.util.Arrays;
import org.lcsim.event.FloatVec;


/**
 *
 * @author tonyj
 */
class FloatVecTableModel extends GenericTableModel
{
   private static final String[] columns = {"I","Size","Data"};
   private static Class klass = FloatVec.class;

   FloatVecTableModel()
   {
      super(klass,columns);
   }
   public Object getValueAt(int row, int column)
   {
      FloatVec vec = (FloatVec) getData(row);
      switch (column) {
          case 0: return row;
          case 1: return vec.size();
          case 2: return Arrays.toString(vec.toFloatArray());
          default: return null;
      }
   }
   
   public Class getColumnClass(int column)
   {
      return column==2 ? String.class : Integer.class;
   }
}
