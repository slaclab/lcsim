package org.lcsim.material;

import junit.framework.TestCase;

/**
 * This is a test of radiation length values calculated by the {@link MaterialManager}.
 * Answer key is taken from values at 
 * {@link http://pdg.lbl.gov/2010/AtomicNuclearProperties}.
 * 
 * @author jeremym
 * @version $Id: RadiationLengthTest.java,v 1.3 2011/03/12 00:37:28 jeremy Exp $
 */
public class RadiationLengthTest extends TestCase
{
    static final boolean DEBUG = false;
    String materialLookup[] = {"Tungsten", "Lead", "Silicon", "Boron", "Hydrogen"};    
    double radLenDensKey[] = {0.3504, 0.5612, 9.370, 22.23, 7.527e5};
    double radLenKey[] = {6.76, 6.37, 21.82, 52.69, 63.04};
    
    public void testRadLen()
    {        
        XMLMaterialManager.setup(); // bootstrap materials db
        //XMLMaterialManager.getDefaultMaterialManager();
        MaterialManager mgr = MaterialManager.instance();
        
        System.out.println("Material radLen, radLenDens");
        for (int i=0, n=materialLookup.length; i<n; i++)
        {
            Material material = mgr.getMaterial(materialLookup[i]);
            String materialName = material.getName();
            if (DEBUG)
            {
                System.out.println(materialName + " " 
                        + material.getRadiationLength() + ", " 
                        + material.getRadiationLengthWithDensity());
                System.out.println("  key: " + radLenKey[i] + ", " + radLenDensKey[i]);
                System.out.println("--");
            }
            
            // X0 computation tolerance for high values should be bigger, e.g. Hydrogen.
            //double tolerance = 0.1;
            //if (radLenKey[i] > 10000)
            //{
            //    tolerance = 100;
            //}
                
            // Test that the radiation length is within tolerance of answer key.
            //assertEquals(materialName, material.getRadiationLength(), radLenKey[i], tolerance);
            
            // Test the the radiation length times density is within tolerance of answer key.
            //assertEquals(materialName, material.getRadiationLengthWithDensity(), radLenDensKey[i], 0.01);
        }
    }
}
