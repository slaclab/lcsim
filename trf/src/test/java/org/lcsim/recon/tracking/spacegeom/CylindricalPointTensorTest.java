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
public class CylindricalPointTensorTest extends TestCase
{
    boolean debug = false;
    public void testCylindricalPointTensor()
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
     // Construct tensor in Cylindrical coordinates.

      CylindricalPointTensor tensor1 = new CylindricalPointTensor (p[ip][0], p[ip][1], p[ip][2],
				     t[it][0][0], t[it][0][1], t[it][0][2],
				     t[it][1][0], t[it][1][1], t[it][1][2],
				     t[it][2][0], t[it][2][1], t[it][2][2]);
                if(debug) System.out.println("Cylindrical coordinates.");
                if(debug) System.out.println(tensor1);
      CylindricalPoint point1 = new CylindricalPoint(p[ip][0], p[ip][1], p[ip][2]);
      CylindricalPointTensor tensor1a = new CylindricalPointTensor (point1,
				      t[it][0][0], t[it][0][1], t[it][0][2],
				      t[it][1][0], t[it][1][1], t[it][1][2],
				      t[it][2][0], t[it][2][1], t[it][2][2]);
      Assert.assertTrue(tensor1.equals(tensor1a));
      SpacePointTensor tensor1b = new SpacePointTensor(tensor1);
      Assert.assertTrue(tensor1.equals(tensor1b));
      SpacePointTensor tensor0b = tensor1;
      Assert.assertTrue(tensor1.equals(tensor0b));

      // Test components.

      Assert.assertTrue(myequal(tensor1.t_rxy_rxy(), t[it][0][0]));
      Assert.assertTrue(myequal(tensor1.t_rxy_phi(), t[it][0][1]));
      Assert.assertTrue(myequal(tensor1.t_rxy_z(), t[it][0][2]));
      Assert.assertTrue(myequal(tensor1.t_phi_rxy(), t[it][1][0]));
      Assert.assertTrue(myequal(tensor1.t_phi_phi(), t[it][1][1]));
      Assert.assertTrue(myequal(tensor1.t_phi_z(), t[it][1][2]));
      Assert.assertTrue(myequal(tensor1.t_z_rxy(), t[it][2][0]));
      Assert.assertTrue(myequal(tensor1.t_z_phi(), t[it][2][1]));
      Assert.assertTrue(myequal(tensor1.t_z_z(), t[it][2][2]));

            }
        }
    }
    static boolean myequal(double x1, double x2)
    {
        return abs(x2 - x1) < 1.e-10;
    }
}
