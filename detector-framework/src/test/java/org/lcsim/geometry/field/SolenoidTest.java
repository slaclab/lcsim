package org.lcsim.geometry.field;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.FieldMap;

/**
 *
 * @author tonyj
 */
public class SolenoidTest extends FieldTest
{
   public SolenoidTest(String testName)
   {
      super(testName);
   }
   
   FieldMap createMap() throws JDOMException
   {
      Element element = new Element("solenoid");
      element.setAttribute("inner_field","5.0");
      element.setAttribute("outer_field","-1.0");
      element.setAttribute("zmax","100");
      element.setAttribute("outer_radius","50");
      return new Solenoid(element);
   }
   void checkMap(FieldMap map)
   {
      testFieldAt(map, 0,0,0,0,0,5);
      testFieldAt(map, 40,40,0,0,0,-1);
      testFieldAt(map, 0,0,-150,0,0,0);
      testFieldAt(map, 40,40,-150,0,0,0);      
   }
   public void testSolenoid() throws JDOMException
   {
      checkMap(createMap());
   }
}