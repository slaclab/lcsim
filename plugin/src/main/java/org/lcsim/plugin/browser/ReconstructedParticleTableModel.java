package org.lcsim.plugin.browser;
import org.lcsim.event.ReconstructedParticle;


/**
 *
 * @author tonyj
 */
class ReconstructedParticleTableModel extends GenericTableModel
{
   private static final String[] columns = {"Type","Momentum","Energy","Mass","Charge","ReferencePoint", "ParticleIDs"};
   private static Class klass = ReconstructedParticle.class;

   ReconstructedParticleTableModel()
   {
      super(klass,columns);
   }



   /* We will have a string for the 6th column*/
   @Override
   public Class getColumnClass(int index) {
       if (index == 6)
           return String.class;
       else return super.getColumnClass(index);
   }
                   

   /* For the 6th column, build up a string from the list of particle types*/
   @Override
   public Object getValueAt(int r, int c) {
       if (c!=6)
           return super.getValueAt(r, c);

       ReconstructedParticle p = (ReconstructedParticle) getData(r);

       String str = "[";
       int size = p.getParticleIDs().size();

       for (int i = 0; i < size; i++) {

           str+= " " + p.getParticleIDs().get(i).getPDG() + " ";
           if  (i < size-1)
               str+=",";
       }

       str+="]";

       return str; 
   }
 
}
