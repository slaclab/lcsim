package org.lcsim.plugin.browser;

import java.lang.reflect.Method;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import org.lcsim.event.EventHeader.LCMetaData;

/**
 *
 * @author tonyj
 */
abstract class GenericTableModel extends AbstractTableModel implements EventBrowserTableModel
{
   private List data;
   private String[] columns;
   private Class klass;

   GenericTableModel(Class beanClass, String[] columns)
   {
      this.columns =columns;
      this.klass = beanClass;
   }
   public boolean canDisplay(Class c)
   {
      return klass.isAssignableFrom(c);
   }
   public void setData(LCMetaData meta, List data)
   {
      this.data = data;
      fireTableDataChanged();
   }
   public int getRowCount()
   {
      return data == null ? 0 : data.size();
   }
   public int getColumnCount()
   {
      return columns.length;
   }
   public String getColumnName(int index)
   {
      return columns[index];
   }
   public Class getColumnClass(int index)
   {
      try
      {
         String name = columns[index];
         Method m = klass.getMethod("get"+name,(Class[]) null);
         Class returnType = m.getReturnType();
         if (returnType.isPrimitive())
         {
            if (returnType == Integer.TYPE) return Integer.class;
            if (returnType == Short.TYPE) return Short.class;
            if (returnType == Long.TYPE) return Long.class;
            if (returnType == Double.TYPE) return Double.class;
            if (returnType == Float.TYPE) return Float.class;
            if (returnType == Byte.TYPE) return Byte.class;
            if (returnType == Character.TYPE) return Character.class;
            if (returnType == Boolean.TYPE) return Boolean.class;
         }
         return returnType;
      }
      catch (Throwable t)
      {
         return Throwable.class;
      }
   }
   protected Object getData(int row)
   {
      return data.get(row);
   }
   public Object getValueAt(int row, int column)
   {
      try
      {
         Object bean = getData(row);
         String name = columns[column];
         Method m = klass.getMethod("get"+name,(Class[]) null);
         return m.invoke(bean,(Object[]) null);
      }
      catch (Throwable t)
      {
         return t;
      }
   }
}
