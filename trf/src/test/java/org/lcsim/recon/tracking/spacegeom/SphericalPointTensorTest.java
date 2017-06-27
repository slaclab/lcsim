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
public class SphericalPointTensorTest extends TestCase
{
    boolean debug = false;
    public void testSphericalPointTensor()
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
     // Construct tensor in Spherical coordinates.

      SphericalPointTensor tensor2 = new SphericalPointTensor(p[ip][0], p[ip][1], p[ip][2],
				   t[it][0][0], t[it][0][1], t[it][0][2],
				   t[it][1][0], t[it][1][1], t[it][1][2],
				   t[it][2][0], t[it][2][1], t[it][2][2]);
                if(debug) System.out.println("Spherical coordinates.");
                if(debug) System.out.println(tensor2);
      SphericalPoint point2 = new SphericalPoint(p[ip][0], p[ip][1], p[ip][2]);
      SphericalPointTensor tensor2a = new SphericalPointTensor(point2,
				    t[it][0][0], t[it][0][1], t[it][0][2],
				    t[it][1][0], t[it][1][1], t[it][1][2],
				    t[it][2][0], t[it][2][1], t[it][2][2]);
      Assert.assertTrue(tensor2.equals(tensor2a));
      SpacePointTensor tensor2b = new SpacePointTensor(tensor2);
      Assert.assertTrue(tensor2.equals(tensor2b));
      SpacePointTensor tensor0b = tensor2;
      Assert.assertTrue(tensor2.equals(tensor0b));

      // Test components.

      Assert.assertTrue(myequal(tensor2.t_rxyz_rxyz(), t[it][0][0]));
      Assert.assertTrue(myequal(tensor2.t_rxyz_theta(), t[it][0][1]));
      Assert.assertTrue(myequal(tensor2.t_rxyz_phi(), t[it][0][2]));
      Assert.assertTrue(myequal(tensor2.t_theta_rxyz(), t[it][1][0]));
      Assert.assertTrue(myequal(tensor2.t_theta_theta(), t[it][1][1]));
      Assert.assertTrue(myequal(tensor2.t_theta_phi(), t[it][1][2]));
      Assert.assertTrue(myequal(tensor2.t_phi_rxyz(), t[it][2][0]));
      Assert.assertTrue(myequal(tensor2.t_phi_theta(), t[it][2][1]));
      Assert.assertTrue(myequal(tensor2.t_phi_phi(), t[it][2][2]));

            }
        }


    }
    static boolean myequal(double x1, double x2)
    {
        return abs(x2 - x1) < 1.e-10;
    }

}
