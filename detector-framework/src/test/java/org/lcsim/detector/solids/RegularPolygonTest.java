
package org.lcsim.detector.solids;

import hep.physics.vec.BasicHep3Vector;
import java.util.Random;
import junit.framework.TestCase;
import static java.lang.Math.PI;
import static java.lang.Math.abs;

/**
 *
 * @author Norman A. Graf
 *
 * @version $Id: RegularPolygonTest.java,v 1.1 2010/04/09 22:53:24 ngraf Exp $
 */
public class RegularPolygonTest extends TestCase
{

    private boolean debug = false;

    public RegularPolygonTest(String testName)
    {
        super(testName);
    }

    public void testRegularPolygon()
    {
        // coverage test
        // generate random points in the circumscribed circle
        // ratio of inside to outside should equal the ratio of areas
        //
        double radius = 1;
        double circleArea = PI * radius * radius;
        int nmax = 1000000;
        int nIn = 0;
        Random rr = new Random();
        // test a common range of polygons...
        for (int np = 3; np < 21; ++np)
        {
            int nsides = np;
            if (debug)
                System.out.println("   ");
            if (debug)
                System.out.println("Testing nsides= " + nsides);
            RegularPolygon p = new RegularPolygon(nsides, radius);

            int i = 0;
            nIn = 0;
            while (i < nmax)
            {
                double x = radius*(2.*rr.nextDouble()-1.);
                double y = radius*(2.*rr.nextDouble()-1.);
                if ((x * x + y * y) < radius * radius)
                {
                    ++i;
                    BasicHep3Vector pos = new BasicHep3Vector(x, y, 0.);
                    Inside in = p.inside(pos);
                    if (Inside.INSIDE.compareTo(in) == 0)
                        ++nIn;
                }
            }
            double meas = (double) nIn / nmax;
            double pred = p.area() / circleArea;
            if (debug)
                System.out.println("nmax= " + nmax + " nIn= " + nIn + " ratio= " + meas);
            if (debug)
                System.out.println("area ratio= " + pred);
            double err = (meas - pred) / pred;
            if (debug)
                System.out.println("(meas-pred)/pred= " + err);
            // .5% uncertainty
            assertTrue(abs(err) < .005);
        }
    }
}
