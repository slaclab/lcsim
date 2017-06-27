package org.lcsim.geometry.field;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import junit.framework.*;
import org.lcsim.geometry.FieldMap;

/**
 *
 * @author tonyj
 */
// Note this class should be abstract, but maven won't let me make it so.
public /* abstract */ class FieldTest extends TestCase
{
   public FieldTest(String testName)
   {
      super(testName);
   }
   
   void testFieldAt(FieldMap map, double x, double y, double z, double bx, double by, double bz)
   {
      testFieldMethod1(map,x,y,z,bx,by,bz);
      testFieldMethod2(map,x,y,z,bx,by,bz);
      testFieldMethod3(map,x,y,z,bx,by,bz);
      testFieldMethod4(map,x,y,z,bx,by,bz);                  
   }
   private void testFieldMethod1(FieldMap map, double x, double y, double z, double bx, double by, double bz)
   {
      double[] field = map.getField(new double[]{x,y,z});
      assertEquals("Method 1, x",bx,field[0],1e-16);
      assertEquals("Method 1, y",by,field[1],1e-16);
      assertEquals("Method 1, z",bz,field[2],1e-16);
   }
   private void testFieldMethod2(FieldMap map, double x, double y, double z, double bx, double by, double bz)
   {
      Hep3Vector fieldVector = map.getField(new BasicHep3Vector(x,y,z));
      assertEquals("Method 2, x",bx,fieldVector.x(),1e-16);
      assertEquals("Method 2, y",by,fieldVector.y(),1e-16);
      assertEquals("Method 2, z",bz,fieldVector.z(),1e-16); 
   }
   private void testFieldMethod3(FieldMap map, double x, double y, double z, double bx, double by, double bz)
   {
      double[] field = { 9,9,9 }; 
      map.getField(new double[]{x,y,z},field);
      assertEquals("Method 3, x",bx,field[0],1e-16);
      assertEquals("Method 3, y",by,field[1],1e-16);
      assertEquals("Method 3, z",bz,field[2],1e-16);
   }
   private void testFieldMethod4(FieldMap map, double x, double y, double z, double bx, double by, double bz)
   {
      BasicHep3Vector fieldVector = new BasicHep3Vector(9,9,9);
      map.getField(new BasicHep3Vector(x,y,z),fieldVector);
      assertEquals("Method 4, x",bx,fieldVector.x(),1e-16);
      assertEquals("Method 4, y",by,fieldVector.y(),1e-16);
      assertEquals("Method 4, z",bz,fieldVector.z(),1e-16); 
   }
   
   public void testNothing()
   {
      // Doesn't do anything, just keeps maven happy
   }
}
