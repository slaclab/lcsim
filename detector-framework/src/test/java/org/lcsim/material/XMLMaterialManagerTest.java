package org.lcsim.material;

import junit.framework.TestCase;

/**
 * This is a very basic integration test of the XMLMaterialManager, making
 * sure that it loads a few expected materials and elements into the MaterialManager.
 * @author jeremym
 */
public class XMLMaterialManagerTest extends TestCase
{
    static XMLMaterialManager xmgr;
        
    public void setUp()
    {
        if (xmgr == null)
        {
            XMLMaterialManager.setup();
            xmgr = XMLMaterialManager.getDefaultMaterialManager();
        }
    }
       
    public void testElements()
    {
        assertTrue(MaterialManager.instance().getElement("N") != null);
        assertTrue(MaterialManager.instance().getMaterial("Nitrogen") != null);
    }
    
    public void testMaterials()
    {
        assertTrue(MaterialManager.instance().getMaterial("Polystyrene") != null);
    }         
}