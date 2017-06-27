package org.lcsim;

import java.io.InputStream;

import junit.framework.TestCase;

import org.lcsim.geometry.compact.CompactReader;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.material.MaterialManager;

/**
 * @author jeremym
 *
 */
public class DetectorMaterialTest extends TestCase
{   
    Detector det = null;
    
    protected void setUp() throws Exception
    {
        InputStream in = 
            getClass().getResourceAsStream("/org/lcsim/geometry/compact/sidloi3.xml");
        CompactReader reader = new CompactReader();
        det = reader.read(in);
    }
        
    public void testPrint()
    {
        MaterialManager.instance().printElements(System.out);
        MaterialManager.instance().printMaterials(System.out);
    }
}
