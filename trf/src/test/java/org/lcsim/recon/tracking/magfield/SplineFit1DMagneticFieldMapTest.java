/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lcsim.recon.tracking.magfield;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.spacegeom.CartesianPoint;
import org.lcsim.recon.tracking.spacegeom.SpacePoint;
import org.lcsim.recon.tracking.spacegeom.SpacePointVector;

/**
 *
 * @author ngraf
 */
public class SplineFit1DMagneticFieldMapTest extends TestCase
{

    private boolean debug = false;

    /**
     * Test of field method, of class SplineFit1DMagneticFieldMap.
     */
    public void testSplineFit1DMagneticFieldMap()
    {
        List<Double> pos = new ArrayList<Double>();
        List<Double> bval = new ArrayList<Double>();

        InputStream is = this.getClass().getResourceAsStream("HPS_b18d36_By_0_0_z.dat");
        try {
            BufferedReader myInput = new BufferedReader(new InputStreamReader(new BufferedInputStream(is)));

            String thisLine;

            while ((thisLine = myInput.readLine()) != null) {
                //System.out.println(thisLine);
                StringTokenizer st = new StringTokenizer(thisLine, " ");
                pos.add(Double.parseDouble(st.nextToken()));
                bval.add(Double.parseDouble(st.nextToken()));
            }
            myInput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int size = pos.size();
        double[] z = new double[size];
        double[] By = new double[size];
        for (int i = 0; i < size; ++i) {
            z[i] = pos.get(i);
            By[i] = bval.get(i);
        }
        double scale = 0.5;
        SplineFit1DMagneticFieldMap map = new SplineFit1DMagneticFieldMap(SplineFit1DMagneticFieldMap.COORDINATE.Z, z, SplineFit1DMagneticFieldMap.BVAL.BY, By, scale);
        if (debug) {
            System.out.println(map);
        }
        for (int i = 0; i < size; ++i) {
            SpacePoint p = new CartesianPoint(0., 0., z[i]);
            SpacePointVector result = map.field(p);
            if (debug) {
                System.out.println("File z: " + z[i] + " By: " + By[i] + " map By: " + result.v_y());
            }
            assertEquals(scale*By[i], result.v_y());
        }
        if (debug) {
            double min = z[0];
            double max = z[size - 1];
            double du = (max - min) / (10. * (size - 1));
            System.out.println("min: " + min + " max: " + max + " size: " + size + " du: " + du);
            for (double u = min; u <= max; u += du) {
                SpacePoint p = new CartesianPoint(0., 0., u);
                SpacePointVector result = map.field(p);
                System.out.println(u + " " + result.v_y());
            }
        }
    }
}
