package org.lcsim.recon.tracking.magfield;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.spacegeom.CartesianPoint;
import org.lcsim.recon.tracking.spacegeom.SpacePoint;
import org.lcsim.recon.tracking.spacegeom.SpacePointVector;
import static java.lang.Math.abs;

/**
 *
 * @author Norman A Graf
 *
 * @version $Id:
 */
public class Cartesian3DMagneticFieldMapTest extends TestCase
{

    private boolean debug = false;

    public void testCartesian3DMagneticFieldMap() throws Exception
    {
        InputStream is = this.getClass().getResourceAsStream("ThreeDFieldMap.dat");
        double xOff = 0.0;
        double yOff = 0.0;
        double zOff = 0.0;
        Cartesian3DMagneticFieldMap map = new Cartesian3DMagneticFieldMap(is, xOff, yOff, zOff);

        is.close();
        // now read in field map again, and check that we get the correct values at the
        // measured points
        // does not test the interpolation per se but is a check.
        BufferedReader myInput = new BufferedReader(new InputStreamReader(new BufferedInputStream(this.getClass().getResourceAsStream("ThreeDFieldMap.dat"))));
        String thisLine;
        int nx = 0;
        int ny = 0;
        int nz = 0;
        //skip the first nine lines of metadata
        for (int i = 0; i < 9; ++i) {
            thisLine = myInput.readLine();
            if (i == 1) // parse for number of grid point in x, y,x
            {
                StringTokenizer st = new StringTokenizer(thisLine, " ");
                nx = Integer.parseInt(st.nextToken());
                ny = Integer.parseInt(st.nextToken());
                nz = Integer.parseInt(st.nextToken());
            }
        }
        assertEquals(nx, 6);
        assertEquals(ny, 6);
        assertEquals(nz, 10);

        int nlines = nx * ny * nz;
        // loop over the field points

        for (int i = 0; i < nlines; ++i) {
            thisLine = myInput.readLine();
            StringTokenizer st = new StringTokenizer(thisLine, " ");
            double x = Double.parseDouble(st.nextToken());
            double y = Double.parseDouble(st.nextToken());
            double z = Double.parseDouble(st.nextToken());
            double bx = Double.parseDouble(st.nextToken());
            double by = Double.parseDouble(st.nextToken());
            double bz = Double.parseDouble(st.nextToken());
            SpacePointVector spv = map.field(new CartesianPoint(x+xOff, y+yOff, z+zOff));
            assertEquals(bx, spv.v_x(), abs(.001 * bx));
            assertEquals(by, spv.v_y(), abs(.001 * by));
            assertEquals(bz, spv.v_z(), abs(.001 * bz));
        }
    }
}
