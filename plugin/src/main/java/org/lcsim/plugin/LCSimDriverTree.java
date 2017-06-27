package org.lcsim.plugin;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.TreePath;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.freehep.application.studio.Studio;
import org.freehep.record.loop.LoopEvent;
import org.freehep.record.loop.LoopListener;
import org.freehep.record.loop.RecordLoop;
import org.freehep.swing.treetable.AbstractTreeTableModel;
import org.freehep.swing.treetable.JTreeTable;
import org.freehep.swing.treetable.TreeTableModel;
import org.lcsim.util.Driver;
import org.lcsim.util.DriverAdapter;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;



/**
 * A browser for viewing driver statistics
 * @author tonyj
 * @author gaede
 */
public class LCSimDriverTree extends JPanel implements LoopListener, LookupListener
{
   private Studio app;
   private RecordLoop loop;
   private Result result;
   private List<Driver> drivers = new ArrayList<Driver>();
   private DriverTreeModel model;
   
   public LCSimDriverTree(Studio app, RecordLoop loop)
   {
      super(new BorderLayout());
      this.app = app;
      this.loop = loop;
      JTreeTable table = new JTreeTable();
      model = new DriverTreeModel(drivers);
      table.setModel(model);
      add(new JScrollPane(table),BorderLayout.CENTER);
   }
   public void addNotify()
   {
      loop.addLoopListener(this);
      
      // Listen for record sources
      // TODO: We use a "wild-card" template to work around a bug in Lookup.
      //Lookup.Template template = new Lookup.Template(SequentialRecord.class);
      Template template = new Template();
      result = app.getLookup().lookup(template);
      drivers.clear();
      resultChanged(null);
      result.addLookupListener(this);
      
      super.addNotify();
   }
   public void removeNotify()
   {
      super.removeNotify();
      loop.removeLoopListener(this);
      result.removeLookupListener(this);
   }

   public void process(LoopEvent event) {}
   
   public class DriverTreeModel extends AbstractTreeTableModel
   {
      private String[] columns = { "Driver", "Events", "Time", "% of parent" };
      DriverTreeModel(List<Driver> drivers)
      {
         super(drivers);
      }

      public Class getColumnClass(int i)
      {
         return i == 0 ? TreeTableModel.class : Number.class;
      }

      public int getColumnCount()
      {
         return columns.length;
      }

      public String getColumnName(int i)
      {
         return columns[i];
      }

      public Object getChild(Object parent, int index)
      {
         if (parent instanceof List)
         {
            return ((List) parent).get(index);
         }
         else 
         {
            return ((Driver) parent).drivers().get(index);
         }
      }

      public int getChildCount(Object parent)
      {
         if (parent instanceof List)
         {
            return ((List) parent).size();
         }
         else 
         {
            return ((Driver) parent).drivers().size();
         }
      }

      public Object getValueAt(Object object, int i)
      {

         if (i == 0)
         {
            if (object instanceof Driver) return ((Driver) object).getName();
            else return object.toString();
         }
         else return "list";

      }

      protected void fireTreeStructureChanged(Object source, TreePath path, int[] childIndices, Object[] children)
      {
         super.fireTreeStructureChanged(source, path, childIndices, children);
      }

      protected void fireTreeNodesRemoved(Object source, TreePath path, int[] childIndices, Object[] children)
      {
         super.fireTreeNodesRemoved(source, path, childIndices, children);
      }

      protected void fireTreeNodesInserted(Object source, TreePath path, int[] childIndices, Object[] children)
      {
         super.fireTreeNodesInserted(source, path, childIndices, children);
      }

      protected void fireTreeNodesChanged(Object source, TreePath path, int[] childIndices, Object[] children)
      {
         super.fireTreeNodesChanged(source, path, childIndices, children);
      }
    
   }
   public void resultChanged(LookupEvent lookupEvent)
   {
      for (Object item : result.allInstances())
      {
         if (item instanceof DriverAdapter)
         {
            Driver driver = ((DriverAdapter) item).getDriver();
            if (!drivers.contains(driver))
            {
               int i[] = { drivers.size() };
               drivers.add(driver);
               model.fireTreeNodesInserted(this,new TreePath(drivers),i,null);
               
            }
         }
      }
   }
}