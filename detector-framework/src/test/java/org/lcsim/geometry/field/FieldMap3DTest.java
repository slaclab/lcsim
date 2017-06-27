package org.lcsim.geometry.field;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import java.io.InputStream;
import java.util.Map;
import static junit.framework.Assert.assertEquals;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.FieldMap;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.geometry.compact.Field;

/**
 *
 * @author jeremym
 */
public class FieldMap3DTest extends FieldTest
{
    /** Creates a new instance of FieldMap3DTest */
    public FieldMap3DTest(String name)
    {
        super(name);
    }
       
    public void testRead() throws Exception
    {
        String[] files = {"/org/lcsim/geometry/field/FieldMap3DTest.xml", "/org/lcsim/geometry/field/FieldMap3DURLTest.xml"};
        for(int f=0; f<files.length; ++f)
        {
            System.out.println("testing "+files[f]);
        InputStream in = this.getClass().getResourceAsStream(files[f]); 
        GeometryReader reader = new GeometryReader();
        Detector det = reader.read(in);
        FieldMap map = det.getFieldMap();

        double[] off = {2.117, 0.0, 45.72};
        double[] fp = {-25.0, -8.9, -150.};
        double[] B = new double[3];

        // field at first map position should be zero since we offset the field
        testFieldAt(map, fp[0], fp[1], fp[2], 0, 0, 0);
        // field at first map position with offset included should equal first field values
        testFieldAt(map, fp[0] + off[0], fp[1] + off[1], fp[2] + off[2], 0, -1.9, 0);
        // field at the origin
        testFieldAt(map, 0.0 + off[0], -8.9 + off[1], 0.0 + off[2], 0., -500.6, 0.);
        // this field map is invariant in y, test this...
        testFieldAt(map, 0.0 + off[0], 0. + off[1], 0.0 + off[2], 0., -500.6, 0.);
        testFieldAt(map, 0.0 + off[0], 8.9 + off[1], 0.0 + off[2], 0., -500.6, 0.);

        //TODO check interpolation more rigorously
        //check all variations of accessor methods (why do we have SO many?!
        double[] p = {fp[0] + off[0], fp[1] + off[1], fp[2] + off[2]};
        Hep3Vector h3vP = new BasicHep3Vector(fp[0] + off[0], fp[1] + off[1], fp[2] + off[2]);

        map.getField(p, B);
        Hep3Vector h3vB = map.getField(h3vP);
        assertEquals(B[0], h3vB.x());
        assertEquals(B[1], h3vB.y());
        assertEquals(B[2], h3vB.z());

        double[] B2 = map.getField(p);
        for (int i = 0; i < 3; ++i) {
            assertEquals(B[i], B2[i]);
        }

        // test specifics of Cartesian3DMagneticFieldMap
        Map<String, Field> fields = det.getFields();
        assertEquals(fields.size(), 1);
        FieldMap3D cmap = (FieldMap3D) fields.get("FieldMap3DTest");
        double[] offsets = cmap.globalOffset();
        assertEquals(offsets[0], off[0]);
        assertEquals(offsets[1], off[1]);
        assertEquals(offsets[2], off[2]);             
        }
    }
}
