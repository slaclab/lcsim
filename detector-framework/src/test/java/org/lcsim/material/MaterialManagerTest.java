/*
 * MaterialManagerTest.java
 *
 * Created on July 1, 2005, 2:25 PM
 */

package org.lcsim.material;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author jeremym
 */
public class MaterialManagerTest extends TestCase
{
    MaterialManager mgr;
    
    /** Creates a new instance of MaterialManagerTest */
    public MaterialManagerTest()
    {}    
    
    public MaterialManagerTest(String testName)
    {
        super(testName);
    }
    
    public static TestSuite suite()
    {
        return new TestSuite(MaterialManagerTest.class);
    }
    
    protected void setUp()
    {}    
    
    public void test_materialsBasic()
    {
        MaterialManager mgr = MaterialManager.instance();
        //mgr.print();
    }
    /*
    String materialLookup[] = {"Tungsten", "Lead", "Silicon", "Iron"}; 
    public void testRadLen()
    {        
        MaterialManager mgr = MaterialManager.instance();
        System.out.println("Material radLen, radLenDens");
        for (String materialName : materialLookup)
        {
            Material material = mgr.getMaterial(materialName);
            System.out.println(materialName + " " 
                    + material.getRadiationLength() + ", " 
                    + material.getRadiationLengthWithDensity());
        }
    }
    */
}