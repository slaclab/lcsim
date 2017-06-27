/*
 * MaterialCnvTest.java
 *
 * Created on June 29, 2005, 2:27 PM
 */

package org.lcsim.material;

import java.io.IOException;
import java.net.URL;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.JDOMException;

/**
 *
 * @author jeremym
 */
public class MaterialFromGDMLCnvTest extends TestCase
{
    public static URL defURL = MaterialFromGDMLCnvTest.class.getResource("MaterialFromGDMLCnvTest.xml");
    
    /** Creates a new instance of MaterialCnvTest */
    public MaterialFromGDMLCnvTest()
    {}
    
    public MaterialFromGDMLCnvTest(String testName)
    {
        super(testName);
    }
    
    public static Test suite()
    {
        return new TestSuite(MaterialFromGDMLCnvTest.class);
    }
    
    public void test_cnvNoLCDD() throws IOException, JDOMException
    {
        System.out.println("Test removed for now!");
        /*
        //MaterialElementData med = new MaterialElementData();
        MaterialManager.instance();
        //XMLMaterialManager f = XMLMaterialManager.create(MaterialFromGDMLCnvTest.class.getResourceAsStream("MaterialFromGDMLCnvTest.xml"));
        XMLMaterialManager xmat = new XMLMaterialManager(MaterialFromGDMLCnvTest.class.getResourceAsStream("MaterialFromGDMLCnvTest.xml"));
        
        //for ( MaterialElement me : MaterialManager.elements().values() )
        //{
        //    System.out.println("defined elem: " + me.name());
        //}
        
        xmat.makeMaterials(null);
        
        Material testFractionMat = MaterialManager.instance().getMaterial("TestFraction");
        
        //assertTrue(testFractionMat != null);
        
        //System.out.println("testFractionMat ncomp, ncompmax, nelem: " + testFractionMat.getNComponents() + " " + testFractionMat.getNComponentsMax() + " " + testFractionMat.getNElements() );        
        //assertEquals( testFractionMat.getMassFractions().get(0), 0.25);
        //assertEquals( testFractionMat.getMassFractions().get(1), 0.25);
        //assertEquals( testFractionMat.getMassFractions().get(2), 0.5);         
         */
    }
}
