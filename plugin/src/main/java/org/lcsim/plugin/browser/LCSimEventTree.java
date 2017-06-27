package org.lcsim.plugin.browser;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import org.freehep.util.ScientificFormat;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;

/**
 *
 * @author tonyj
 * @version $Id: LCSimEventTree.java,v 1.2 2007/04/09 05:11:11 tonyj Exp $
 */
class LCSimEventTree extends JComponent
{
   LCSimEventTree()
   {
      setLayout(new BorderLayout());
      m_root = new ParticleNode();
      m_tree = new JTree(m_root);
      m_tree.setRootVisible(false);
      add(new JScrollPane(m_tree), BorderLayout.CENTER);
   }

   void setEvent(EventHeader event)
   {
      m_root.clear();
      m_event = event;
         
      if (m_event != null && m_event.hasCollection(MCParticle.class, EventHeader.MC_PARTICLES))
      {
         // Any particle which has no parents should appear at the
         // root level
         // Note: Particles whose only parent is themselves are counted as having no
         // parents!
         // Note: Sometimes the parent/daughter information is inconsistent, so we 
         // use _only_ parent information to build the tree
         // Note: We only use the first parent, second parent info is ignored.
         
         Map<MCParticle,ParticleNode> temp = new HashMap<MCParticle,ParticleNode>();
         
         List<MCParticle> particles = event.get(MCParticle.class,EventHeader.MC_PARTICLES);
         for (MCParticle p : particles )
         {
            ParticleNode node = new ParticleNode(p);
            temp.put(p,node);
         }
         for (MCParticle p : particles )
         {
            List<MCParticle> parents = p.getParents();
            boolean hasParent = parents != null && !parents.isEmpty() && parents.get(0) != p;

            ParticleNode parentNode = hasParent ? temp.get(parents.get(0)) : m_root;
            parentNode.addChild((ParticleNode) temp.get(p));
         }
      }
      ((DefaultTreeModel) m_tree.getModel()).nodeStructureChanged(m_root);
   }
   
   private EventHeader m_event;
   private JTree m_tree;
   private ParticleNode m_root;
   private ScientificFormat format = new ScientificFormat();
   
   private class ParticleNode implements TreeNode
   {
      ParticleNode()
      {
      }
      ParticleNode(MCParticle mc)
      {
         this.particle = mc;
      }
      void addChild(ParticleNode child)
      {
         children.add(child);
         child.parent = this;
      }
      public int getIndex(TreeNode node)
      {
         return children.indexOf(node);
      }
      public TreeNode getChildAt(int index)
      {
         return (TreeNode) children.get(index);
      }
      public TreeNode getParent()
      {
         return parent;
      }
      public boolean getAllowsChildren()
      {
         return true;
      }
      public Enumeration children()
      {
         return Collections.enumeration(children);
      }
      public boolean isLeaf()
      {
         return children.isEmpty();
      }
      public int getChildCount()
      {
         return children.size();
      }
      void clear()
      {
         children.clear();
      }
      public String toString()
      {
         if (particle == null)
         {
            if (m_event == null) return "No Event";
            else return "Run "+m_event.getRunNumber()+" Event "+m_event.getEventNumber();
         }
         else
         {
            String result = String.valueOf(particle.getType().getName());
            result += "(E="+format.format(particle.getEnergy())+" status="+MCParticleTableModel.convert(particle.getGeneratorStatus())+")";
            return result;
         }
      }
      private MCParticle particle;
      private List children = new ArrayList();
      private TreeNode parent;
   }
}