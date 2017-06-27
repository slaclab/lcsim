package org.lcsim.detector.solids;

import hep.physics.vec.BasicHep3Vector;
import java.util.Random;
import junit.framework.TestCase;

import static java.lang.Math.abs;
import static java.lang.Math.PI;
import static java.lang.Math.atan;

/**
 *
 * @author Norman A. Graf
 *
 * @version $Id:
 */
public class IsoscelesTrapezoidTest extends TestCase
{

    private boolean debug = false;

    public IsoscelesTrapezoidTest(String testName)
    {
        super(testName);
    }

    public void testIsoscelesTrapezoid()
    {
        // start with a box
        double b = 1.0;
        double t = 1.0;
        double height = 1.0;
        IsoscelesTrapezoid trap1 = new IsoscelesTrapezoid(b, t, height);

        if (debug)
            System.out.println(trap1);

        assertEquals(trap1.area(), 4.0);
        
        if (debug)System.out.println(trap1.baseAngle());
        if (debug)System.out.println(trap1.topAngle());
        assertEquals(trap1.baseAngle(), trap1.topAngle());
        assertEquals(trap1.baseAngle(), PI/2);
        // coverage test
        // generate random points in the circumscribed circle
        // ratio of inside to outside should equal the ratio of areas
        //
        double radius = 10;
        double circleArea = PI * radius * radius;
        int nmax = 1000000;
        int nIn = 0;
        Random rr = new Random();
        // test a common range of polygons...

        int i = 0;
        nIn = 0;
        while (i < nmax)
        {
            double x = radius * (2. * rr.nextDouble() - 1.);
            double y = radius * (2. * rr.nextDouble() - 1.);
            if ((x * x + y * y) < radius * radius)
            {
                ++i;
                BasicHep3Vector pos = new BasicHep3Vector(x, y, 0.);
                Inside in = trap1.inside(pos);
                if (Inside.INSIDE.compareTo(in) == 0)
                    ++nIn;
            }
        }
        double meas = (double) nIn / nmax;
        double pred = trap1.area() / circleArea;
        if (debug)
            System.out.println("nmax= " + nmax + " nIn= " + nIn + " ratio= " + meas);
        if (debug)
            System.out.println("area ratio= " + pred);
        double err = (meas - pred) / pred;
        if (debug)
            System.out.println("(meas-pred)/pred= " + err);
        // 5% uncertainty
        assertTrue(abs(err) < .05);

        // now test a non-rectangular trapezoid...
        b = 1.0;
        t = 2.0;
        height = 1.0;
        IsoscelesTrapezoid trap2 = new IsoscelesTrapezoid(b, t, height);

        assertEquals(trap2.baseAngle(), (PI/2+atan(.5)));
        assertEquals(PI, trap2.baseAngle()+trap2.topAngle());

    }
}
