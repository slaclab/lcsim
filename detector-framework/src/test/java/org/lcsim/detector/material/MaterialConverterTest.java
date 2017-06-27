package org.lcsim.detector.material;

import java.io.InputStream;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.lcsim.detector.converter.lcdd.MaterialElementConverter;
import org.lcsim.detector.converter.lcdd.MaterialMixtureConverter;
import org.lcsim.geometry.compact.CompactReader;
import org.lcsim.material.MaterialManager;
import org.lcsim.material.XMLMaterialManager;
import org.lcsim.util.xml.JDOMExpressionFactory;

// TODO Add OneTimeDetectorSetup.
public class MaterialConverterTest
extends TestCase
{    
    public MaterialConverterTest(String name)
    {
        super(name);
    }
    
    public static junit.framework.Test suite()
    {
        return new TestSuite(MaterialConverterTest.class);
    }
    
    public void testDummy()
    {
        // dummy for now
    }
    
    // TODO These should be separate tests.
    /*    
    public void testReadElementXML() throws Exception
    {
    	InputStream in = 
    		MaterialElementConverter.class.
    		getResourceAsStream("/org/lcsim/material/elements.xml");    	
    	JDOMExpressionFactory eval = new JDOMExpressionFactory();
    	CompactReader.registerCLHEPConstants(eval);    	
    	SAXBuilder builder = new SAXBuilder();                  
    	builder.setFactory(eval);
        Document doc = builder.build(in);
        
        Element root = doc.getRootElement();
                
        // Test conversion of <element>.
        MaterialElementConverter cnv = new MaterialElementConverter();
        for ( Object child : root.getChildren("element") )
        {        	
        	Element element = (Element)child;
        	cnv.convert(element);        	
        }
        
        // Test conversion of <material>.
        MaterialMixtureConverter cnv2 = new MaterialMixtureConverter();
        for ( Object child : root.getChildren("material") )
        {            
        	Element element = (Element)child;
        	cnv2.convert(element);
        }
        
        InputStream in2 = 
    		MaterialElementConverter.class.
    		getResourceAsStream("/org/lcsim/material/materials.xml");    	
    	JDOMExpressionFactory eval2 = new JDOMExpressionFactory();
    	CompactReader.registerCLHEPConstants(eval);    	
    	SAXBuilder builder2 = new SAXBuilder();                  
    	builder2.setFactory(eval2);
        Document doc2 = builder.build(in2);
                
        //MaterialMixtureConverter cnv2 = new MaterialMixtureConverter();
        for ( Object child : doc2.getRootElement().getChildren("material") )
        {            
        	Element element = (Element)child;
        	cnv2.convert(element);
        }               
        
        XMLMaterialManager xmlMat 
        	= XMLMaterialManager.create(XMLMaterialManager.materials() );
        xmlMat.load("/org/lcsim/material/materials.xml");
        xmlMat.makeMaterials(null);
        
        // Compare to old materials database.
        for ( IMaterial material : MaterialStore.getInstance())
        {   
        	if ( material instanceof MaterialMixture )
        	{
        		org.lcsim.material.Material oldmaterial = 
        			MaterialManager.instance().getMaterial(material.getName());
               
        		assertEquals(oldmaterial.getAeff(), material.getA(), 1e-8 );
        		
        		assertEquals(oldmaterial.getZeff(), material.getZ(), 1e-8 );
        		
        		assertEquals(
                        oldmaterial.getNuclearInteractionLength(),
                        material.getNuclearInteractionLength(), 
                        1e-8 );
                
                assertEquals(
                        oldmaterial.getNuclearInteractionLengthWithDensity(),
                        material.getNuclearInteractionLengthWithDensity(),
                        1e-8 );
        		
        		assertTrue(
        				oldmaterial.getRadiationLength() -
        				material.getRadiationLength() <=
                        1E-5 );
                
                assertTrue(
                        oldmaterial.getRadiationLengthWithDensity() - 
                        material.getRadiationLengthWithDensity() <=
                        1E-5 );
                
                //assertTrue(
                //        oldmaterial.getMoliereRadius() - 
                //        material.getMoliereRadius() <=
                //        1e-4 );
                
                // Old material does not have this.
                //assertTrue(
                //        material.getEffectiveNumberOfNucleons() != 0
                //        );                
                
                if (material.getName().equals("Silicon"))
                {
                    System.out.println("old material ...");
                    System.out.println(oldmaterial.toString());
                    System.out.println("----");
                    System.out.println("new material ...");
                    System.out.println(material.toString());
                }
        	}
        }        
    }
    */
}