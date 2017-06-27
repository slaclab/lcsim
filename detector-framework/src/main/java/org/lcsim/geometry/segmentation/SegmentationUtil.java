/*
 * SegmentationUtil.java Created on September 26, 2005, 12:11 PM
 */

package org.lcsim.geometry.segmentation;

import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.PI;
import static java.lang.Math.sqrt;

/**
 * Utilities for segmentation classes.
 * 
 * @author jeremym
 */
public final class SegmentationUtil
{
    public static double getPhi(double x, double y)
    {
        double phi = atan2(x, y);

        if (phi < 0)
        {
            phi += 2 * PI;
        }

        return phi;
    }

    public static double getTheta(double x, double y, double z)
    {
        double theta = atan(getCylindricalRadius(x, y) / z);

        if (theta < 0)
        {
            theta += PI;
        }

        return theta;
    }

    public static double getCylindricalRadius(double x, double y)
    {
        return sqrt(x * x + y * y);
    }
}
