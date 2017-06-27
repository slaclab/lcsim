package org.lcsim.plugin.browser;

import hep.physics.vec.Hep3Vector;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.freehep.application.Application;
import org.freehep.application.studio.Studio;
import org.freehep.swing.table.TableColumnPacker;
import org.freehep.util.ScientificFormat;
import org.lcsim.event.EventHeader;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.plugin.browser.sort.DefaultSortableTableModel;
import org.lcsim.plugin.browser.sort.SortableTableModel;
import org.lcsim.plugin.browser.sort.TableSorter;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;

/**
 *
 * @author tonyj
 * @version $Id: CollectionTable.java,v 1.9 2007/06/02 00:05:51 tonyj Exp $
 */
class CollectionTable extends JPanel implements ActionListener
{
   private static final String noCollection = "No Collection";
   private static final TableModel emptyTable = new DefaultSortableTableModel(new DefaultTableModel());
   private JTextArea m_tableLabel = new JTextArea(noCollection);
   private JTable m_table = new JTable();
   private JButton errorButton = new JButton("An error occured, click for details...");
   private CardLayout cardLayout = new CardLayout();
   private JPanel panel = new JPanel(cardLayout);
   private EventHeader m_lce;
   private Studio m_app;
   private Throwable error;
   private Map<TableModel,SortableTableModel> sortedModels = new HashMap<TableModel,SortableTableModel>();
   private TableColumnPacker tableColumnPacker = new TableColumnPacker();
   
   CollectionTable(Studio app)
   {
      super(new BorderLayout());
      m_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      m_app = app;
      m_tableLabel.setEditable(false);
      add(m_tableLabel,BorderLayout.NORTH);
      panel.add(new JScrollPane(m_table),"first");
      panel.add(errorButton,"second");
      add(panel,BorderLayout.CENTER);
      
      errorButton.addActionListener(this);
      
      // Add some smart renderers
      
      m_table.setDefaultRenderer(Double.class,new ScientificRenderer());
      m_table.setDefaultRenderer(Float.class,new ScientificRenderer());
      m_table.setDefaultRenderer(new double[0].getClass(),new ArrayRenderer());
      m_table.setDefaultRenderer(new float[0].getClass(),new ArrayRenderer());
      m_table.setDefaultRenderer(new int[0].getClass(),new ArrayRenderer());
      m_table.setDefaultRenderer(new short[0].getClass(),new ArrayRenderer());
      m_table.setDefaultRenderer(new String[0].getClass(),new ArrayRenderer());
      m_table.setDefaultRenderer(Hep3Vector.class,new VectorRenderer());
      m_table.setDefaultRenderer(Object.class, new LCObjectRenderer());
      
      new TableSorter(m_table);
   }
   void setEvent(EventHeader event)
   {
      m_lce = event;
      m_table.setModel(emptyTable);
      m_tableLabel.setText(noCollection) ;
   }
   void setCollection(Object collection)
   {
      if (collection instanceof Throwable)
      {
         m_table.setModel(emptyTable);
         error = (Throwable) collection;
         cardLayout.last(panel);
      }
      else if (collection != null && collection instanceof List && m_lce != null)
      {
         List coll = (List) collection;
         LCMetaData meta = m_lce.getMetaData(coll);
         Class type = meta.getType();
         int flag = meta.getFlags();
         
         StringBuffer tableText = new StringBuffer();
         tableText.append("Collection: ").append(meta.getName());
         tableText.append(" size:").append(coll.size());
         tableText.append(" flags:").append(Integer.toHexString(flag));
         
         for (Map.Entry<String,int[]> entry : meta.getIntegerParameters().entrySet())
         {
            tableText.append( "\n    ").append(entry.getKey()).append(":\t");
            for(int v : entry.getValue())
            {
               tableText.append(v);
               tableText.append(", ");
            }
            if (entry.getValue().length > 0) tableText.setLength(tableText.length()-2);
         }
         for (Map.Entry<String,float[]> entry : meta.getFloatParameters().entrySet())
         {
            tableText.append( "\n    ").append(entry.getKey()).append(":\t");
            for(float v : entry.getValue())
            {
               tableText.append(v);
               tableText.append(", ");
            }
            if (entry.getValue().length > 0) tableText.setLength(tableText.length()-2);
         }
         for (Map.Entry<String,String[]> entry : meta.getStringParameters().entrySet())
         {
            tableText.append( "\n    ").append(entry.getKey()).append(":\t");
            for(String v : entry.getValue())
            {
               tableText.append(v);
               tableText.append(", ");
            }
            if (entry.getValue().length > 0) tableText.setLength(tableText.length()-2);
         }
         m_tableLabel.setText(tableText.toString()) ;
         
         try
         {
            Template template = new Template(EventBrowserTableModel.class);
            Result result = m_app.getLookup().lookup(template);
            Collection<EventBrowserTableModel> models = (Collection<EventBrowserTableModel>) result.allInstances();
            
            boolean ok = false;
            for (EventBrowserTableModel model : models)
            {
               if (model.canDisplay(type))
               {
                  model.setData(meta,coll);
                  SortableTableModel sortedModel = sortedModels.get(model);
                  if (sortedModel == null)
                  {
                     sortedModel = new DefaultSortableTableModel(model);
                     sortedModels.put(model,sortedModel);
                  }
                  m_table.setModel(sortedModel);
                  tableColumnPacker.packColumns(m_table);
                  ok = true;
                  break;
               }
            }
            if (!ok)
            {
               m_table.setModel(emptyTable);
               
            }
            cardLayout.first(panel);
         }
         catch (Exception x)
         {
            error = x;
            cardLayout.last(panel);
         }
      }
      else
      {
         m_table.setModel(emptyTable);
         m_tableLabel.setText(noCollection) ;
         cardLayout.first(panel);
      }
   }
   
   public void actionPerformed(ActionEvent actionEvent)
   {
      Application.error(this,"Error displaying collection", error);
   }
   private static class ScientificRenderer extends DefaultTableCellRenderer
   {
      private ScientificFormat format = new ScientificFormat();
      ScientificRenderer()
      {
         setHorizontalAlignment(SwingConstants.RIGHT);
      }
      
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
      {
         super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
         if (value instanceof Number) setText(format.format(((Number) value).doubleValue()));
         return this;
      }
   }
   private static class ArrayRenderer extends DefaultTableCellRenderer
   {
      private ScientificFormat format = new ScientificFormat();
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
      {
         super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
         if (value != null && value.getClass().isArray())
         {
            StringBuffer buffer = new StringBuffer("[");
            int ll = Array.getLength(value);
            for (int i=0; i<10;)
            {
               if (i >= ll) break;
               Object o = Array.get(value,i);
               if (o instanceof Double || o instanceof Float) buffer.append(format.format(((Number) o).doubleValue()));
               else buffer.append(o);
               if (++i >= ll) break;
               buffer.append(',');
            }
            if (ll > 10) buffer.append("...");
            buffer.append(']');
            setText(buffer.toString());
         }
         return this;
      }
   }
   private static class VectorRenderer extends DefaultTableCellRenderer
   {
      private ScientificFormat format = new ScientificFormat();
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
      {
         super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
         if (value instanceof Hep3Vector)
         {
            Hep3Vector vector = (Hep3Vector) value;
            StringBuffer buffer = new StringBuffer("[");
            buffer.append(format.format(vector.x()));
            buffer.append(',');
            buffer.append(format.format(vector.y()));
            buffer.append(',');
            buffer.append(format.format(vector.z()));
            buffer.append(']');
            setText(buffer.toString());
         }
         return this;
      }
   }
   private class LCObjectRenderer extends DefaultTableCellRenderer
   {
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
      {
         super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
         if (value instanceof String) return this;
         
         // Look for collections in the event which contain objects of this class
         List<List<Object>> collections = m_lce.get(Object.class);
         for (List<Object> collection : collections)
         {
            int index = collection.indexOf(value);
            if (index >= 0)
            {
               setText(m_lce.getMetaData(collection).getName()+"["+index+"]");
               return this;
            }
         }
         return this;
      }
   }
}