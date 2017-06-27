package org.lcsim.plugin.browser;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.lcsim.event.EventHeader;
import org.lcsim.geometry.IDDecoder;

/**
 *
 * @author tonyj
 */
public abstract class CellIDTableModel extends AbstractTableModel implements EventBrowserTableModel 
{
    private List hits;
    private final String[] defaultColumns;
    private List<String> columns;
    private IDDecoder decoder;   
    
   /** Creates a new instance of CellIDTableModel */
   protected CellIDTableModel(String[] defaultColumns)
   {
      this.defaultColumns = defaultColumns;
   }


   public void setData(EventHeader.LCMetaData meta, List hits)
   {
      this.hits = hits;
      List oldColumns = columns;
      columns = new ArrayList<String>();
      
      try
      {
         this.decoder = meta.getIDDecoder();
         for (int i = 0; i < decoder.getFieldCount(); i++)columns.add("id: " + decoder.getIDDescription().fieldName(i));
         for (int i = 1; i < defaultColumns.length; i++)columns.add(defaultColumns[i]);
      } catch (Exception x)
      {
         System.err.println("Error accessing decoder "+x);
         decoder = null;
         for (int i = 0; i < defaultColumns.length; i++)columns.add(defaultColumns[i]);
      }
      
      if (columns.equals(oldColumns)) fireTableDataChanged();
      else super.fireTableStructureChanged();
   }


   public int getColumnCount()
   {
       return columns.size();
   }


   public String getColumnName(int index)
   {
       return columns.get(index);
   }


   public int getRowCount()
   {
       return hits == null ? 0 : hits.size();
   }


   protected int getFieldCount()
   {
      return decoder == null ? 1 : decoder.getFieldCount();
   }
   
   protected IDDecoder getIDDecoder()
   {
      return decoder;
   }
   
   protected Object getHit(int row)
   {
      return hits.get(row);
   }
}

