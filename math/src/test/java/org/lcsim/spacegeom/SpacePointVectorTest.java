package org.lcsim.spacegeom;

import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;
import junit.framework.TestCase;

public class SpacePointVectorTest extends TestCase {
    boolean debug = false;
    public static boolean myEqual(Hep3Vector x1, Hep3Vector x2)
    {
        return VecOp.sub(x1, x2).magnitude() < 1.e-10;
    }
    
    private SpacePointVector thisPath;
    protected void setUp() throws Exception {
        super.setUp();
        thisPath = new SpacePointVector(new SpacePoint(), new CartesianPoint(1, 1, 1));
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'org.lcsim.spacegeom.SpacePath.getPointAtLength(double)'
     */
    public void testGetPointAtLength() {
        if (debug) {
            System.out.printf("To be equal: %s and %s\n\n", thisPath.getPointAtLength(0), thisPath.getStartPoint());
        }
        assertTrue(myEqual(thisPath.getPointAtLength(0), thisPath.getStartPoint()));
        if (debug) {
            System.out.printf("To be equal: %s and %s\n\n", thisPath.getPointAtLength(1), thisPath.getEndPoint());
        }
        assertTrue(myEqual(thisPath.getPointAtLength(1), thisPath.getEndPoint()));
        assertFalse(thisPath.getPointAtLength(1).equals(thisPath.getStartPoint()));
        SpacePoint farPoint = new CartesianPoint(2, 2, 2);
        assertTrue(thisPath.getPointAtLength(2).equals(farPoint));
        SpacePoint halfPoint = new CartesianPoint(0.5, 0.5, 0.5);
        assertTrue(thisPath.getPointAtLength(0.5).equals(halfPoint));
        SpacePoint negPoint = new CartesianPoint(-1, -1, -1);
        assertTrue(thisPath.getPointAtLength(-1).equals(negPoint));
    }

}
