package org.lcsim.recon.tracking.spacegeom;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

import static java.lang.Math.abs;

/**
 *
 * @author Norman A. Graf
 *
 * @version $Id:
 */
public class CartesianPointTensorTest extends TestCase
{
    boolean debug = false;
    public void testCartesianPointTensor()
    {
        int np = 10;   // Number of points data.
        int nt = 5;   // Number of tensors data.

        double p[][] =
        {
            {
                0., 0., 0.
            },
            {
                1., 0., 0.
            },
            {
                0., 1., 0.
            },
            {
                0., 0., 1.
            },
            {
                1., 1., 0.
            },
            {
                0., 1., 1.
            },
            {
                1., 0., 1.
            },
            {
                1., 2., 3.
            },
            {
                -2., -3., 1.
            },
            {
                4., -5., 3.
            }
        };

        double t[][][] =
        {
            {
                {
                    0., 0., 0.
                },
                {
                    0., 0., 0.
                },
                {
                    0., 0., 0.
                }
            },
            {
                {
                    1., 0., 0.
                },
                {
                    0., 1., 0.
                },
                {
                    0., 0., 1.
                }
            },
            {
                {
                    1., 1., 1.
                },
                {
                    1., 1., 1.
                },
                {
                    1., 1., 1.
                }
            },
            {
                {
                    3., -2., 4.
                },
                {
                    -3., 6., -5.
                },
                {
                    -1., 2., 1.
                }
            },
            {
                {
                    -2., 2., -1.
                },
                {
                    3., -4., 5.
                },
                {
                    1., -5., 3.
                }
            }
        };

        // Loop over points.

        for (int ip = 0; ip < np; ++ip)
        {
            // Loop over tensors.
            for (int it = 0; it < nt; ++it)
            {

                // Construct tensor in Cartesian coordinates.

                CartesianPointTensor tensor0 = new CartesianPointTensor(p[ip][0], p[ip][1], p[ip][2],
                        t[it][0][0], t[it][0][1], t[it][0][2],
                        t[it][1][0], t[it][1][1], t[it][1][2],
                        t[it][2][0], t[it][2][1], t[it][2][2]);
                if(debug) System.out.println("Cartesian coordinates.");
                if(debug) System.out.println(tensor0);
                CartesianPoint point0 = new CartesianPoint(p[ip][0], p[ip][1], p[ip][2]);
                CartesianPointTensor tensor0a = new CartesianPointTensor(point0,
                        t[it][0][0], t[it][0][1], t[it][0][2],
                        t[it][1][0], t[it][1][1], t[it][1][2],
                        t[it][2][0], t[it][2][1], t[it][2][2]);
                Assert.assertTrue(tensor0.equals(tensor0a));
                SpacePointTensor tensor0b = new SpacePointTensor(tensor0);
                if(debug) System.out.println("tensor0:  \n" + tensor0);
                if(debug) System.out.println("tensor0b:  \n" +tensor0b);
                Assert.assertTrue(tensor0.equals(tensor0b));
                tensor0b = tensor0;
                Assert.assertTrue(tensor0.equals(tensor0b));

                // Test components.

                Assert.assertTrue(myequal(tensor0.t_x_x(), t[it][0][0]));
                Assert.assertTrue(myequal(tensor0.t_x_y(), t[it][0][1]));
                Assert.assertTrue(myequal(tensor0.t_x_z(), t[it][0][2]));
                Assert.assertTrue(myequal(tensor0.t_y_x(), t[it][1][0]));
                Assert.assertTrue(myequal(tensor0.t_y_y(), t[it][1][1]));
                Assert.assertTrue(myequal(tensor0.t_y_z(), t[it][1][2]));
                Assert.assertTrue(myequal(tensor0.t_z_x(), t[it][2][0]));
                Assert.assertTrue(myequal(tensor0.t_z_y(), t[it][2][1]));
                Assert.assertTrue(myequal(tensor0.t_z_z(), t[it][2][2]));

            }
        }
    }

    static boolean myequal(double x1, double x2)
    {
        return abs(x2 - x1) < 1.e-10;
    }
}
