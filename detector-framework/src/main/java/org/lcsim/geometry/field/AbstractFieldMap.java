package org.lcsim.geometry.field;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import org.jdom.Element;
import org.lcsim.geometry.FieldMap;
import org.lcsim.geometry.compact.Field;

/**
 * Base class for field map implementations. Subclasses should override
 * getXField, getYField, getZField as appropriate (the default implementation
 * returns 0).
 * @author tonyj
 */
abstract class AbstractFieldMap extends Field implements FieldMap
{
   AbstractFieldMap(Element node)
   {
      super(node);
   }
   abstract void getField(double x, double y, double z, BasicHep3Vector field);
   
   public Hep3Vector getField(Hep3Vector position, BasicHep3Vector field)
   {
      if (field == null) field = new BasicHep3Vector();
      getField(position.x(),position.y(),position.z(),field);
      return field;
   }
   
   public Hep3Vector getField(Hep3Vector position)
   {
      return getField(position,null);
   }
   
   public void getField(double[] position, double[] b)
   {
      BasicHep3Vector field = new BasicHep3Vector();
      getField(position[0],position[1],position[2],field);
      b[0] = field.x();
      b[1] = field.y();
      b[2] = field.z();
   }
   
   public double[] getField(double[] position)
   {
      BasicHep3Vector field = new BasicHep3Vector();
      getField(position[0],position[1],position[2],field);
      return field.v();
   }
}
