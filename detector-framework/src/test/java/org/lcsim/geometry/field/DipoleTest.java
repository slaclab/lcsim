package org.lcsim.geometry.field;

import org.jdom.Element;

/**
 *
 * @author tonyj
 */
public class DipoleTest extends FieldTest
{
   private Dipole dipole;
   public DipoleTest(String testName)
   {
      super(testName);
   }
   
   protected void setUp() throws Exception
   {
      Element element = new Element("dipole");
      element.setAttribute("zmin","10");
      element.setAttribute("zmax","20");
      element.setAttribute("rmax","100");
      for (int i=0; i<3; i++)
      {
         Element coeff = new Element("dipoleCoeff");
         coeff.setAttribute("value",String.valueOf(5-i));
         element.addContent(coeff);
      }
      dipole = new Dipole(element);
   }
   
   public void testDipole()
   {
      testFieldAt(dipole,0,0,0,0,0,0);
      testFieldAt(dipole,0,0,40,0,0,0);
      testFieldAt(dipole,80,80,15,0,0,0);
      // FixMe: This is not correct 
      testFieldAt(dipole,0,0,15,740,0,0);
   }

}
