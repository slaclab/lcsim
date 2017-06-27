package org.lcsim.recon.tracking.magfield;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.lcsim.recon.tracking.spacegeom.CartesianPointVector;
import org.lcsim.recon.tracking.spacegeom.SpacePoint;
import org.lcsim.recon.tracking.spacegeom.SpacePointTensor;
import org.lcsim.recon.tracking.spacegeom.SpacePointVector;

/**
 *
 * @author Norman A Graf
 *
 * @version $Id:
 */
public class Cartesian3DMagneticFieldMap extends AbstractMagneticField
{
    // Storage space for the table

    private double[][][] _xField;
    private double[][][] _yField;
    private double[][][] _zField;
    // The dimensions of the table
    private int _nx, _ny, _nz;
    // The physical limits of the defined region
    private double _minx, _maxx, _miny, _maxy, _minz, _maxz;
    // The physical extent of the defined region
    private double _dx, _dy, _dz;
    // Offsets if field map is not in global coordinates
    private double _xOffset;
    private double _yOffset;
    private double _zOffset;

    public Cartesian3DMagneticFieldMap(InputStream is, double xOffset, double yOffset, double zOffset)
    {
        _xOffset = xOffset;
        _yOffset = yOffset;
        _zOffset = zOffset;

        System.out.println("\n-----------------------------------------------------------"
                + "\n      Reading Magnetic Field map"
                + "\n-----------------------------------------------------------");

        try {
            BufferedReader myInput = new BufferedReader(new InputStreamReader(new BufferedInputStream(is)));

            String thisLine;
            // ignore the first blank line
            thisLine = myInput.readLine();
            // next line has table dimensions
            thisLine = myInput.readLine();
            // read in the table dimensions of the file
            StringTokenizer st = new StringTokenizer(thisLine, " ");
            _nx = Integer.parseInt(st.nextToken());
            _ny = Integer.parseInt(st.nextToken());
            _nz = Integer.parseInt(st.nextToken());


            // Set up storage space for table
            _xField = new double[_nx + 1][_ny + 1][_nz + 1];
            _yField = new double[_nx + 1][_ny + 1][_nz + 1];
            _zField = new double[_nx + 1][_ny + 1][_nz + 1];

            // Ignore other header information    
            // The first line whose second character is '0' is considered to
            // be the last line of the header.
            do {
                thisLine = myInput.readLine();
                st = new StringTokenizer(thisLine, " ");
            } while (!st.nextToken().trim().equals("0"));

            // now ready to read in the values in the table
            // format is:
            // x y z Bx By Bz
            //
            int ix, iy, iz;
            double xval = 0.;
            double yval = 0.;
            double zval = 0.;
            double bx, by, bz;
            for (ix = 0; ix < _nx; ix++) {
                for (iy = 0; iy < _ny; iy++) {
                    for (iz = 0; iz < _nz; iz++) {
                        thisLine = myInput.readLine();
                        st = new StringTokenizer(thisLine, " ");
                        xval = Double.parseDouble(st.nextToken());
                        yval = Double.parseDouble(st.nextToken());
                        zval = Double.parseDouble(st.nextToken());
                        bx = Double.parseDouble(st.nextToken());
                        by = Double.parseDouble(st.nextToken());
                        bz = Double.parseDouble(st.nextToken());
                        if (ix == 0 && iy == 0 && iz == 0) {
                            _minx = xval;
                            _miny = yval;
                            _minz = zval;
                        }
                        _xField[ix][iy][iz] = bx;
                        _yField[ix][iy][iz] = by;
                        _zField[ix][iy][iz] = bz;
                    }
                }
            }

            _maxx = xval;
            _maxy = yval;
            _maxz = zval;

            System.out.println("\n ---> ... done reading ");
            System.out.println(" ---> assumed the order:  x, y, z, Bx, By, Bz "
                    + "\n ---> Min values x,y,z: "
                    + _minx + " " + _miny + " " + _minz + " cm "
                    + "\n ---> Max values x,y,z: "
                    + _maxx + " " + _maxy + " " + _maxz + " cm "
                    + "\n ---> The field will be offset by " + _xOffset + " " + _yOffset + " " + _zOffset + " cm ");

            _dx = _maxx - _minx;
            _dy = _maxy - _miny;
            _dz = _maxz - _minz;
            System.out.println("\n ---> Range of values x,y,z: "
                    + _dx + " " + _dy + " " + _dz + " cm in z "
                    + "\n-----------------------------------------------------------");

            myInput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public SpacePointVector field(SpacePoint p)
    {
        double x = p.x() + _xOffset;
        double y = p.y() + _yOffset;
        double z = p.z() + _zOffset;
        double[] Bfield = new double[3];
        // Check that the point is within the defined region 
        if (x >= _minx && x <= _maxx
                && y >= _miny && y <= _maxy
                && z >= _minz && z <= _maxz) {

            // Position of given point within region, normalized to the range
            // [0,1]
            double xfraction = (x - _minx) / _dx;
            double yfraction = (y - _miny) / _dy;
            double zfraction = (z - _minz) / _dz;

            double xdindex, ydindex, zdindex;

            // Position of the point within the cuboid defined by the
            // nearest surrounding tabulated points
            double[] xmodf = modf(xfraction * (_nx - 1));
            double[] ymodf = modf(yfraction * (_ny - 1));
            double[] zmodf = modf(zfraction * (_nz - 1));

            // The indices of the nearest tabulated point whose coordinates
            // are all less than those of the given point

            int xindex = (int) xmodf[0];
            int yindex = (int) ymodf[0];
            int zindex = (int) zmodf[0];
            double xlocal = xmodf[1];
            double ylocal = ymodf[1];
            double zlocal = zmodf[1];
            // bilinear interpolation
            Bfield[0] =
                    _xField[xindex][yindex][zindex] * (1 - xlocal) * (1 - ylocal) * (1 - zlocal)
                    + _xField[xindex][yindex][zindex + 1] * (1 - xlocal) * (1 - ylocal) * zlocal
                    + _xField[xindex][yindex + 1][zindex] * (1 - xlocal) * ylocal * (1 - zlocal)
                    + _xField[xindex][yindex + 1][zindex + 1] * (1 - xlocal) * ylocal * zlocal
                    + _xField[xindex + 1][yindex][zindex] * xlocal * (1 - ylocal) * (1 - zlocal)
                    + _xField[xindex + 1][yindex][zindex + 1] * xlocal * (1 - ylocal) * zlocal
                    + _xField[xindex + 1][yindex + 1][zindex] * xlocal * ylocal * (1 - zlocal)
                    + _xField[xindex + 1][yindex + 1][zindex + 1] * xlocal * ylocal * zlocal;
            Bfield[1] =
                    _yField[xindex][yindex][zindex] * (1 - xlocal) * (1 - ylocal) * (1 - zlocal)
                    + _yField[xindex][yindex][zindex + 1] * (1 - xlocal) * (1 - ylocal) * zlocal
                    + _yField[xindex][yindex + 1][zindex] * (1 - xlocal) * ylocal * (1 - zlocal)
                    + _yField[xindex][yindex + 1][zindex + 1] * (1 - xlocal) * ylocal * zlocal
                    + _yField[xindex + 1][yindex][zindex] * xlocal * (1 - ylocal) * (1 - zlocal)
                    + _yField[xindex + 1][yindex][zindex + 1] * xlocal * (1 - ylocal) * zlocal
                    + _yField[xindex + 1][yindex + 1][zindex] * xlocal * ylocal * (1 - zlocal)
                    + _yField[xindex + 1][yindex + 1][zindex + 1] * xlocal * ylocal * zlocal;
            Bfield[2] =
                    _zField[xindex][yindex][zindex] * (1 - xlocal) * (1 - ylocal) * (1 - zlocal)
                    + _zField[xindex][yindex][zindex + 1] * (1 - xlocal) * (1 - ylocal) * zlocal
                    + _zField[xindex][yindex + 1][zindex] * (1 - xlocal) * ylocal * (1 - zlocal)
                    + _zField[xindex][yindex + 1][zindex + 1] * (1 - xlocal) * ylocal * zlocal
                    + _zField[xindex + 1][yindex][zindex] * xlocal * (1 - ylocal) * (1 - zlocal)
                    + _zField[xindex + 1][yindex][zindex + 1] * xlocal * (1 - ylocal) * zlocal
                    + _zField[xindex + 1][yindex + 1][zindex] * xlocal * ylocal * (1 - zlocal)
                    + _zField[xindex + 1][yindex + 1][zindex + 1] * xlocal * ylocal * zlocal;

        } else {
            Bfield[0] = 0.0;
            Bfield[1] = 0.0;
            Bfield[2] = 0.0;
        }

        return new CartesianPointVector(p, Bfield[0], Bfield[1], Bfield[2]);
    }

    @Override
    public SpacePointVector field(SpacePoint p, SpacePointTensor g)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double minX()
    {
        return _minx + _xOffset;
    }

    public double minY()
    {
        return _miny + _yOffset;
    }

    public double minZ()
    {
        return _minz + _zOffset;
    }
    
   public double maxX()
    {
        return _maxx + _xOffset;
    }

    public double maxY()
    {
        return _maxy + _yOffset;
    }

    public double maxZ()
    {
        return _maxz + _zOffset;
    }    
    

    private double[] modf(double fullDouble)
    {
        int intVal = (int) fullDouble;
        double remainder = fullDouble - intVal;

        double[] retVal = new double[2];
        retVal[0] = intVal;
        retVal[1] = remainder;

        return retVal;
    }
}
