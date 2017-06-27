package org.lcsim.geometry.field;

import java.io.InputStream;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.FieldMap;
import org.lcsim.geometry.GeometryReader;

/**
 *
 * @author jeremym
 */
public class RZFieldMapTest extends FieldTest
{
    /** Creates a new instance of RZFieldMapTest */
    public RZFieldMapTest(String name)
    {
        super(name);
    }
       
    public void testRead() throws Exception
    {
        InputStream in = this.getClass().getResourceAsStream("/org/lcsim/geometry/field/RZFieldMapTest.xml");        
        GeometryReader reader = new GeometryReader();
        Detector det = reader.read(in);
        FieldMap map = det.getFieldMap();
        testFieldAt(map,0,0,0,0,0,5.0011);
        testFieldAt(map,0,0,1000,0,0,4.84980);
        testFieldAt(map,100,0,0,0.0003,0,5.0019);
        testFieldAt(map,0,100,0,0,0.0003,5.0019);
        testFieldAt(map,100,0,100,0.0015,0,5.00030);
        // Is it really correct that the radial field flips at negative z?
        testFieldAt(map,100,0,-100,-0.0015,0,5.00030);
    }    
}