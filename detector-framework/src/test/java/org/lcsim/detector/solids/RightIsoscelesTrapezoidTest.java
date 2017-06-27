package org.lcsim.detector.solids;

import hep.physics.vec.BasicHep3Vector;
import java.util.Random;
import junit.framework.TestCase;
import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.IPhysicalVolumeNavigator;
import org.lcsim.detector.IRotation3D;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.ITranslation3D;
import org.lcsim.detector.LogicalVolume;
import org.lcsim.detector.PhysicalVolume;
import org.lcsim.detector.PhysicalVolumeNavigatorStore;
import org.lcsim.detector.RotationGeant;
import org.lcsim.detector.Transform3D;
import org.lcsim.detector.Translation3D;
import org.lcsim.detector.converter.heprep.DetectorElementToHepRepConverter;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialElement;
import org.lcsim.util.test.TestUtil.TestOutputFile;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.cos;

import static org.lcsim.units.clhep.SystemOfUnits.m;

/**
 *
 * @author Norman A. Graf
 *
 * @version $Id:
 */
public class RightIsoscelesTrapezoidTest extends TestCase
{

    private boolean debug = false;
    private boolean writeHeprep = false;
    private static IMaterial dummymat = new MaterialElement("dummymat", 1, 1, 1.0);
    IPhysicalVolumeNavigator nav;
    IPhysicalVolume world;

    public RightIsoscelesTrapezoidTest(String testName)
    {
        super(testName);
    }

    public void testRightIsoscelesTrapezoid() throws Exception
    {
        String name = "testSolid";
        // start with a box
        double b = 1.0;
        double t = 1.0;
        double height = 1.0;
        double thickness = 1.0;

        RightIsoscelesTrapezoid rit1 = new RightIsoscelesTrapezoid(name, b, t, height, thickness);

        // should be equivalent to the following box

        Box box = new Box(name, b, height, thickness);
        if (debug)
            System.out.println(rit1);

        assertEquals(rit1.getCubicVolume(), box.getCubicVolume());

        //
        // coverage test
        // generate random points in the circumscribed sphere
        // ratio of inside to outside should equal the ratio of volumes
        //

        // pick a suitable large sphere...
        double r = 5.;

        double sphereVolume = 4 * PI * r * r * r / 3.;
        double polyVolume = rit1.getCubicVolume();

        int nmax = 100000;
        int nIn = 0;
        Random ran = new Random();
        int i = 0;
        long startTime = System.nanoTime();
        long endTime;

        try
        {
            while (i < nmax)
            {
                double x = r * (2. * ran.nextDouble() - 1.);
                double y = r * (2. * ran.nextDouble() - 1.);
                double z = r * (2. * ran.nextDouble() - 1.);

                if ((x * x + y * y + z * z) < r * r)
                {
                    ++i;
                    BasicHep3Vector pos = new BasicHep3Vector(x, y, z);
                    Inside in = rit1.inside(pos);
                    Inside inBox = box.inside(pos);
                    assertTrue(in.equals(inBox));
                    if (Inside.INSIDE.compareTo(in) == 0)
                        ++nIn;
                }
            }
        } finally
        {
            endTime = System.nanoTime();
        }
        long duration = endTime - startTime;
        if (debug)
            System.out.println("duration= " + duration);
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


        // now try a wedge...
        RightIsoscelesTrapezoid rit2 = new RightIsoscelesTrapezoid(name, b, 2. * t, height, thickness);
        if (debug)
            System.out.println(rit2);

        nmax = 100000;
        nIn = 0;
        i = 0;
        startTime = System.nanoTime();

        try
        {
            while (i < nmax)
            {
                double x = r * (2. * ran.nextDouble() - 1.);
                double y = r * (2. * ran.nextDouble() - 1.);
                double z = r * (2. * ran.nextDouble() - 1.);

                if ((x * x + y * y + z * z) < r * r)
                {
                    ++i;
                    BasicHep3Vector pos = new BasicHep3Vector(x, y, z);
                    Inside in = rit1.inside(pos);
                    if (Inside.INSIDE.compareTo(in) == 0)
                        ++nIn;
                }
            }
        } finally
        {
            endTime = System.nanoTime();
        }
        duration = endTime - startTime;
        if (debug)
            System.out.println("duration= " + duration);
        meas = (double) nIn / nmax;
        pred = polyVolume / sphereVolume;
        if (debug)
            System.out.println("nmax= " + nmax + " nIn= " + nIn + " ratio= " + meas);
        if (debug)
            System.out.println("volume ratio= " + pred);
        err = (meas - pred) / pred;
        if (debug)
            System.out.println("(meas-pred)/pred= " + err);
        // 10% uncertainty
        assertTrue(abs(err) < .1);

        // now try a wedge with a "skin" on the sides (simulates an HCal segment...
        // input is the thickness of the side...
        double skinThickness = .1*b; // start with 10% of the bottom thickness...
        double phi = atan((t-b)/2*height);
        double l = skinThickness/cos(phi);
        double newTop = 2.*t-l;
        double newBottom = b-l;
        RightIsoscelesTrapezoid rit3 = new RightIsoscelesTrapezoid(name, newBottom, newTop, height, thickness);


        // test the heprep creation
        if (writeHeprep)
        {
            world = createWorld();
            nav = PhysicalVolumeNavigatorStore.getInstance().createDefault(world);
            // Unrotated
            IRotation3D rotation = new RotationGeant(0, 0, 0);
            ITranslation3D translation = new Translation3D(0, 0, 0);
            ITransform3D transform = new Transform3D(translation, rotation);
            LogicalVolume lvTest = new LogicalVolume("lvtest", rit2, dummymat);
            new PhysicalVolume(
                    transform,
                    "pvtest",
                    lvTest,
                    world.getLogicalVolume(),
                    0);
            new DetectorElement("detest", null, "/pvtest");


            LogicalVolume lvTest2 = new LogicalVolume("lvtest2", rit3, dummymat);
            new PhysicalVolume(
                    transform,
                    "pvtest2",
                    lvTest2,
                    world.getLogicalVolume(),
                    0);
            new DetectorElement("detest2", null, "/pvtest2");

            

            // write this out
            DetectorElementToHepRepConverter.writeHepRep(new TestOutputFile("RightIsoscelesTrapezoidTest.heprep").getAbsolutePath());

        }

    }

    final IPhysicalVolume createWorld()
    {
        Box boxWorld = new Box(
                "world_box",
                10.0 * m,
                10.0 * m,
                10.0 * m);

        LogicalVolume lvWorld =
                new LogicalVolume(
                "world",
                boxWorld,
                dummymat);

        IPhysicalVolume pvTop =
                new PhysicalVolume(
                null,
                "world",
                lvWorld,
                null,
                0);

        return pvTop;
    }
}
