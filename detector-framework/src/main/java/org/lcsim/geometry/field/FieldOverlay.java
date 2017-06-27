package org.lcsim.geometry.field;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import java.util.ArrayList;
import java.util.List;

import org.lcsim.geometry.FieldMap;

/**
 * FieldOverlay is a FieldMap that itself
 * contains a list of references to other FieldMaps.
 * This setup supports the overlay of multiple
 * magnetic fields, such as the detector solenoid
 * and the DiD.
 *
 * @see org.lcsim.geometry.Detector
 *
 * @author Jeremy McCormick
 */
public class FieldOverlay implements FieldMap
{
   private List<FieldMap> fields = new ArrayList<FieldMap>();
   
   public FieldOverlay()
   {}
   
   public void addField(FieldMap field)
   {
      fields.add(field);
   }
   
   public void getField(double[] pos, double[] b)
   {
      int size = fields.size();
      if (size == 0)
      {
         b[0] = b[1] = b[2] = 0.;
      }
      else if (size == 1)
      {
         fields.get(0).getField(pos,b);
      }
      else
      {
         double[] temp = new double[3];
         b[0] = b[1] = b[2] = 0.;
         for (FieldMap field : fields)
         {
            field.getField(pos, temp);
            for (int i=0; i<3; i++) b[i] += temp[i];
         }
      }
   }
   
   public double[] getField(double[] pos)
   {
      double[] b = new double[3];
      getField(pos, b);
      return b;
   }
   
   public Hep3Vector getField(Hep3Vector position, BasicHep3Vector field)
   {
      if (field == null) field = new BasicHep3Vector();
      int size = fields.size();
      if (size == 0)
      {
         field.setV(0,0,0);
      }
      else if (size == 1)
      {
         fields.get(0).getField(position,field);
      }
      else
      {
         double bx = 0;
         double by = 0;
         double bz = 0;
         BasicHep3Vector temp = new BasicHep3Vector();
         for (FieldMap map : fields)
         {
            map.getField(position,temp);
            bx += temp.x();
            by += temp.y();
            bz += temp.z();
         }
         field.setV(bx,by,bz);
      }
      return field;
   }
   
   public Hep3Vector getField(Hep3Vector position)
   {
      return getField(position,null);
   }
}