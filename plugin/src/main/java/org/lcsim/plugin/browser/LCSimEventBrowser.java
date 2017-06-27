package org.lcsim.plugin.browser;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import org.freehep.util.commanddispatcher.CommandProcessor;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import org.freehep.application.studio.Studio;
import org.freehep.record.loop.LoopEvent;
import org.freehep.record.loop.LoopListener;
import org.freehep.record.loop.RecordLoop;
import org.freehep.record.source.RecordSource;
import org.freehep.util.FreeHEPLookup;
import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;



/**
 * A browser for viewing LCIO events in org.lcsim
 * @author tonyj
 * @author gaede
 */
public class LCSimEventBrowser extends JPanel implements LoopListener, TreeSelectionListener, ListSelectionListener, TreeWillExpandListener
{
   private static final String noEvent = "No Event";
   private static final TreeModel m_empty = new DefaultTreeModel(new DefaultMutableTreeNode(noEvent));
   private JLabel m_label = new JLabel(noEvent);
   private JTree m_tree = new JTree(m_empty);
   
   private EventHeaderPanel m_headerPanel = new EventHeaderPanel();
   private LCSimEventTree m_eventTree = new LCSimEventTree();
   private CollectionTable m_table;
   private CardLayout m_cardLayout = new CardLayout();
   private JPanel m_switchPanel = new JPanel(m_cardLayout);
   private RecordLoop m_loop;
   private EventHeader m_lce;
   private String m_selectedNode;
   private Commands m_commands = new Commands();
   private Studio app;
   
   public static void registerTableModels(FreeHEPLookup lookup)
   {
      lookup.add(new CalorimeterHitTableModel());
      lookup.add(new SimCalorimeterHitTableModel());
      lookup.add(new ClusterTableModel());
      lookup.add(new MCParticleTableModel());
      lookup.add(new SimTrackerHitTableModel());
      lookup.add(new RawCalorimeterHitTableModel());
      lookup.add(new RawTrackerHitTableModel());
      lookup.add(new TPCHitTableModel());
      lookup.add(new TrackerHitTableModel());
      lookup.add(new LCRelationTableModel());
      lookup.add(new TrackTableModel());
      lookup.add(new ReconstructedParticleTableModel());
      lookup.add(new LCGenericObjectTableModel());
      lookup.add(new Hep3VectorTableModel());
      lookup.add(new TrackerPulseTableModel());
      lookup.add(new TrackerDataTableModel());
      lookup.add(new VertexTableModel());
      lookup.add(new FloatVecTableModel());
      lookup.add(new IntVecTableModel());
      lookup.add(new StringVecTableModel());
   }
   
   public LCSimEventBrowser(Studio app, RecordLoop loop)
   {
      super(new BorderLayout());
      this.app = app;
      m_tree.addTreeSelectionListener(this);
      m_tree.addTreeWillExpandListener(this);
      m_tree.setMinimumSize(new Dimension(100,100));
      
      m_table = new CollectionTable(app);
      m_switchPanel.add(m_table,"Collection");
      m_switchPanel.add(m_headerPanel,"Event");
      m_switchPanel.add(m_eventTree,"Tree");      
      JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,new JScrollPane(m_tree),m_switchPanel);
      add(pane,BorderLayout.CENTER);
      add(m_label,BorderLayout.NORTH);
      m_loop = loop;
      
      Font oldFont = m_table.getFont() ;
      Font newFont = new Font( "Monospaced", oldFont.getStyle() , oldFont.getSize() ) ;
      m_table.setFont( newFont ) ;
   }
   
   public void addNotify()
   {
      RecordSource source = m_loop.getRecordSource();
      setSource(source);
      m_loop.addLoopListener(this);
      super.addNotify();
   }
   public void removeNotify()
   {
      super.removeNotify();
      m_loop.removeLoopListener(this);
   }
   public CommandProcessor getCommands()
   {
      return m_commands;
   }
   private void setSource(RecordSource source)
   {
      try
      {
         setEvent(source.getCurrentRecord());
      }
      catch (Throwable t)
      {
         setEvent(t);
      }
   }
   private void setEvent(Object event)
   {
      if (event == null)
      {
         m_tree.setModel(m_empty);
         m_label.setText(noEvent);
         m_headerPanel.setData(null);
         m_eventTree.setEvent(null);
         m_table.setEvent(null);
         m_lce = null;
      }
      else if (event instanceof EventHeader)
      {
         m_lce = (EventHeader) event;
         m_table.setEvent(m_lce);
         DefaultMutableTreeNode root = new DefaultMutableTreeNode("Event");
         List<String> collections = new ArrayList<String>(m_lce.keys());
         Collections.sort(collections);
         int selectedRow = -1;
         int i = 0;
         for (String name : collections)
         {
            if (name.equals(m_selectedNode)) selectedRow = i;
            root.add(new DefaultMutableTreeNode(name));
            i++;
         }
         if (m_lce.hasCollection(MCParticle.class, EventHeader.MC_PARTICLES))
         {
            if ("MCParticleTree".equals(m_selectedNode)) selectedRow = i;
            root.add(new DefaultMutableTreeNode("MCParticleTree"));
         }
         
         m_tree.setModel(new DefaultTreeModel(root));
         m_label.setText("Run:"+m_lce.getRunNumber()+"  Event: "+m_lce.getEventNumber());
         
         m_eventTree.setEvent(m_lce);
         
         // Try to find selected node
         if (selectedRow >= 0) m_tree.setSelectionRow(selectedRow+1);
         else m_tree.setSelectionRow(0);
         m_tree.expandRow(0); // Make sure root is expanded
      }
      else
      {
         m_lce = null;
         m_tree.setModel(m_empty);
         m_label.setText(event.toString());
         m_eventTree.setEvent(null);
      }
      m_commands.setChanged();
   }

   public void process(LoopEvent event) {
    switch (event.getEventType()) {
      case SUSPEND:
      case RESET:
        RecordLoop loop = event.getSource();
        setSource(loop.getRecordSource());
      default:
    }
   }
   
   public void valueChanged(TreeSelectionEvent e)
   {
      try
      {
         TreePath path = m_tree.getSelectionPath();
         if (path != null && m_lce != null)
         {
            m_selectedNode = path.getLastPathComponent().toString();
            if (m_selectedNode.equals("Event"))
            {
               m_headerPanel.setData(m_lce);
               m_cardLayout.show(m_switchPanel,"Event");
            }
            else if (m_selectedNode.equals("MCParticleTree"))
            {
               m_cardLayout.show(m_switchPanel,"Tree");               
            }
            else
            {
               List coll = m_lce.get(Object.class,m_selectedNode);
               m_table.setCollection(coll);
               m_cardLayout.show(m_switchPanel,"Collection");
            }
         }
         else
         {
            m_table.setCollection(null);
         }
      }
      catch (Exception x)
      {
         m_table.setCollection(x);
      }
      m_commands.setChanged();
   }
   
   public void valueChanged(ListSelectionEvent e)
   {
      m_commands.setChanged();
   }

   public void treeWillCollapse(TreeExpansionEvent treeExpansionEvent) throws ExpandVetoException
   {
      // Don't allow the root node to be collapsed
      if (treeExpansionEvent.getPath().getPathCount()==1) throw new ExpandVetoException(treeExpansionEvent);
   }

   public void treeWillExpand(TreeExpansionEvent treeExpansionEvent) throws ExpandVetoException
   {
   }
   
   private class Commands extends CommandProcessor
   {
      //      public void onPrintPreview()
      //      {
      //         Studio app = getApplication();
      //         TableModel model = table.getModel();
      //         Font headerFont = new Font("Serif", Font.BOLD, 12);
      //         Font cellFont = new Font("SansSerif", Font.PLAIN, 10);
      //         PrinterJob pj = PrinterJob.getPrinterJob();
      //         PageFormat pf = pj.defaultPage();
      //         pj.cancel();
      //         // FIXME: the frc and pf should depend on the current default printer
      //         FontRenderContext frc = ((Graphics2D) app.getGraphics()).getFontRenderContext();
      //         TablePrinter printer = new TablePrinter(model,"Table Test", pf, headerFont, cellFont,  frc);
      //         PrintPreview pp = app.createPrintPreview();
      //         pp.setPageable(printer);
      //         JDialog dlg = pp.createDialog(app,"Print Preview...");
      //         app.showDialog(dlg,"tableTestPrintPreview");
      //      }
//      public void enableCopy(CommandState state)
//      {
//         state.setEnabled(m_table.getSelectionModel().getLeadSelectionIndex() >= 0);
//      }
//      public void onCopy()
//      {
//         StringBuffer buf = new StringBuffer();
//         for (int col=0; col<m_table.getColumnCount();)
//         {
//            buf.append(m_table.getColumnName(col));
//            if (++col == m_table.getColumnCount()) break;
//            buf.append('\t');
//         }
//         buf.append('\n');
//         
//         int[] rows = m_table.getSelectedRows();
//         for (int r=0; r<rows.length; r++)
//         {
//            int row = rows[r];
//            for (int col=0; col<m_table.getColumnCount();)
//            {
//               buf.append(m_table.getValueAt(row,col));
//               if (++col == m_table.getColumnCount()) break;
//               buf.append('\t');
//            }
//            buf.append('\n');
//         }
//         Clipboard cb = getToolkit().getSystemClipboard();
//         StringSelection sel = new StringSelection(buf.toString());
//         cb.setContents(sel,sel);
//      }
   }
}
