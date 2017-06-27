package org.lcsim.plugin.browser;

import hep.physics.particle.properties.UnknownParticleIDException;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.MCParticle;
import org.lcsim.event.MCParticle.SimulatorStatus;

/**
 *
 * @author Tony Johnson
 * @author Jeremy McCormick
 */
class MCParticleTableModel extends AbstractTableModel implements EventBrowserTableModel
{
   private List particles;
   private static Class floatArrayClass = new float[0].getClass();
   private static Class doubleArrayClass = new double[0].getClass();
   private static Class intArrayClass = new int[0].getClass();
   private static final String[] columns = {"N","PDG ID","Type","Generator Status","Simulator Status","Parent","Energy","Momentum","Start","End","Mass","Charge","Time","Spin","Color Flow"};
   private static final Class[] classes = {Integer.class, Integer.class, Integer.class, String.class, String.class, String.class, Double.class, floatArrayClass, doubleArrayClass, doubleArrayClass, Float.class, Float.class, Float.class, floatArrayClass, intArrayClass};

   public boolean canDisplay(Class c)
   {
      return MCParticle.class.isAssignableFrom(c);
   }
   public void setData(LCMetaData meta, List particles)
   {
      this.particles = particles;
      fireTableDataChanged();
   }
   public int getRowCount()
   {
      return particles == null ? 0 : particles.size();
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
      return classes[index];
   }
   public Object getValueAt(int row, int column)
   {
      MCParticle p = (MCParticle) particles.get(row);
      try
      {
         switch (column)
         {
            case 0: return row;
            case 1: return p.getPDGID();
            case 2: return p.getType().getName();
            case 3: return convert(p.getGeneratorStatus());
            case 4: return convert(p.getSimulatorStatus());
            case 5: return parents(p);
            case 6: return p.getEnergy();
            case 7: return p.getMomentum().v();
            case 8: return p.getOrigin().v();
            case 9: 
               try
               {
                  return p.getEndPoint().v();
               }
               catch (Exception x) { return null; }
            case 10: return p.getMass();
            case 11: return p.getCharge();
            case 12: return p.getProductionTime();
            case 13: return p.getSpin();
            case 14: return p.getColorFlow();
            default: return " ";
         }
      }
      catch (UnknownParticleIDException x)
      {
         return "id="+x.getPDGID()+"?";
      }
   }
   static String convert(SimulatorStatus status)
   {
	   List<String> s = new ArrayList<String>();
	   if (status.hasLeftDetector())
	   {
		   s.add("Left");
	   }
	   if (status.isBackscatter())
	   {
		   s.add("Backscatter");
	   }
	   if (status.isCreatedInSimulation())
	   {
		   s.add("Created In Simulation");
	   }
	   if (status.isDecayedInCalorimeter())
	   {
		   s.add("Decayed In Calorimeter");
	   }
	   if (status.isDecayedInTracker())
	   {
		   s.add("Decayed In Tracker");
	   }
	   if (status.isStopped())
	   {
		   s.add("Stopped");
	   }
	   StringBuffer buff = new StringBuffer();
	   for (int i=0; i<s.size(); i++)
	   {		   
		   buff.append(s.get(i)+",");		   
	   }
	   buff.setLength(Math.max(0,buff.length()-1));
	   return buff.toString();
   }
   static String convert(int status)
   {
      switch (status)
      {
         case 1: return "Final State";
         case 2: return "Intermediate";
         case 3: return "Documentation";
         default: return "Other ("+status+")";
      }
   }
   private String parents(MCParticle p)
   {
      StringBuffer buf = new StringBuffer();
      List<MCParticle> parents = p.getParents();
      for (MCParticle parent : parents)
      {
         buf.append(particles.indexOf(parent));
         buf.append(',');
      }
      buf.setLength(Math.max(0,buf.length()-1));
      return buf.toString();
   }
}
