package org.lcsim.detector.solids;

import hep.physics.vec.BasicHep3Vector;
import java.util.Random;
import junit.framework.TestCase;
import static java.lang.Math.abs;
import static java.lang.Math.PI;

/**
 *
 * @author Norman A. Graf
 *
 * @version $Id: RightRegularPolyhedronTest.java,v 1.2 2010/04/12 18:38:15 ngraf Exp $
 */
public class RightRegularPolyhedronTest extends TestCase
{

    private boolean debug = false;
    
    public RightRegularPolyhedronTest(String testName) {
        super(testName);
    }

    public void testRightRegularPolyhedron()
    {
        String name = "testSolid";
        for (int nsides = 4; nsides < 20; ++nsides)
        {
            double r1 = 1.0;
            double r2 = 2.0;
            double z1 = -1.;
            double z2 = 1.0;

//            RightRegularPolyhedron poly = new RightRegularPolyhedron(nsides, r2, z1, z2);
//
//            if(debug) System.out.println(poly);


            RightRegularPolyhedron p = new RightRegularPolyhedron(name, nsides, r1, r2, z1, z2);

            if(debug) System.out.println(p);

            //
            // coverage test
            // generate random points in the circumscribed sphere
            // ratio of inside to outside should equal the ratio of volumes
            //
            double sphereVolume = 4 * PI * r2 * r2 * r2 / 3.;
            double polyVolume = p.volume();

            int nmax = 1000000;
            int nIn = 0;
            Random ran = new Random();
            int i = 0;
            while (i < nmax)
            {

                double x = r2 * (2. * ran.nextDouble() - 1.);
                double y = r2 * (2. * ran.nextDouble() - 1.);
                double z = r2 * (2. * ran.nextDouble() - 1.);

                if ((x * x + y * y + z * z) < r2 * r2)
                {
                    ++i;
                    BasicHep3Vector pos = new BasicHep3Vector(x, y, z);
                    Inside in = p.inside(pos);
                    if (Inside.INSIDE.compareTo(in) == 0)
                        ++nIn;
                }
            }
            double meas = (double) nIn / nmax;
            double pred = polyVolume / sphereVolume;
            if (debug)
                System.out.println("nmax= " + nmax + " nIn= " + nIn + " ratio= " + meas);
            if (debug)
                System.out.println("volume ratio= " + pred);
            double err = (meas - pred) / pred;
            if (debug)
                System.out.println("(meas-pred)/pred= " + err);
            // 10% uncertainty
            assertTrue(abs(err) < .1);

        }
    }
}
