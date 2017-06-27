package org.lcsim.plugin.browser;

import hep.physics.vec.Hep3Vector;


/**
 *
 * @author tonyj
 */
class Hep3VectorTableModel extends GenericTableModel
{
   private static final String[] columns = {"X","Y","Z"};
   private static Class klass = Hep3Vector.class;

   Hep3VectorTableModel()
   {
      super(klass,columns);
   }
   public Object getValueAt(int row, int column)
   {
      Hep3Vector vec = (Hep3Vector) getData(row);
      switch (column) 
      {
         case 0: return vec.x();
         case 1: return vec.y();
         case 2: return vec.z();
         default: return 0;
      }
   }
   
   public Class getColumnClass(int column)
   {
      return Double.class;
   }
}
