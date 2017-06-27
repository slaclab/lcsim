package org.lcsim.geometry.field;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import static java.lang.Math.sqrt;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.util.cache.FileCache;

/**
 *
 * @author Norman A Graf
 *
 * @version $Id:
 */
public class FieldMap3D extends AbstractFieldMap
{
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
    // maximum field strength
    private double _bMax;
    private double[] _Bfield = new double[3];
    String _filename;

    public FieldMap3D(Element node) throws JDOMException
    {
        super(node);
        _xOffset = node.getAttribute("xoffset").getDoubleValue();
        _yOffset = node.getAttribute("yoffset").getDoubleValue();
        _zOffset = node.getAttribute("zoffset").getDoubleValue();
        _filename = node.getAttributeValue("filename");
        System.out.println("Field Map location: " + _filename);
        try {
            setup(_filename);
        } catch (Exception e) {
            throw new RuntimeException("Error reading field map from " + _filename, e);
        }
    }

    private void setup(String filename) throws IOException
    {
        InputStream fis;
        BufferedReader br;
        String line;

        //FIXME Should specify either filename or url in the xml. Needs change to schema.
        if (filename.startsWith("http")) {
            FileCache cache = new FileCache();
            File file = cache.getCachedFile(new URL(filename));
            fis = new FileInputStream(file);
        } else {
            fis = new FileInputStream(filename);
        }

        System.out.println("-----------------------------------------------------------");
        System.out.println("FieldMap3D ");
        System.out.println("-----------------------------------------------------------");
        System.out.println("Reading the field grid from " + filename + " ... ");

        br = new BufferedReader(new InputStreamReader(fis));
        // ignore the first blank line
        line = br.readLine();
        // next line has table dimensions
        line = br.readLine();
        // read in the table dimensions of the file
        StringTokenizer st = new StringTokenizer(line, " ");
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
            line = br.readLine();
            System.out.println(line);
            st = new StringTokenizer(line, " ");
        } while (!st.nextToken().trim().equals("0"));

        // now ready to read in the values in the table
        // format is:
        // x y z Bx By Bz
        // Recall that in Geant4 internal units 1 Tesla is equal to 0.001 so convert
        //
        int conversionFactor = 1000;
        int ix, iy, iz;
        double xval = 0.;
        double yval = 0.;
        double zval = 0.;
        double bx, by, bz;
        for (ix = 0; ix < _nx; ix++) {
            for (iy = 0; iy < _ny; iy++) {
                for (iz = 0; iz < _nz; iz++) {
                    line = br.readLine();
                    st = new StringTokenizer(line, " ");
                    xval = Double.parseDouble(st.nextToken());
                    yval = Double.parseDouble(st.nextToken());
                    zval = Double.parseDouble(st.nextToken());
                    bx = Double.parseDouble(st.nextToken())*conversionFactor;
                    by = Double.parseDouble(st.nextToken())*conversionFactor;
                    bz = Double.parseDouble(st.nextToken())*conversionFactor;
                    if (ix == 0 && iy == 0 && iz == 0) {
                        _minx = xval;
                        _miny = yval;
                        _minz = zval;
                    }
                    _xField[ix][iy][iz] = bx;
                    _yField[ix][iy][iz] = by;
                    _zField[ix][iy][iz] = bz;
                    double b = bx * bx + by * by + bz * bz;
                    if (b > _bMax) {
                        _bMax = b;
                    }
                }
            }
        }
        _bMax = sqrt(_bMax);

        _maxx = xval;
        _maxy = yval;
        _maxz = zval;

        System.out.println("\n ---> ... done reading ");
        System.out.println(" ---> assumed the order:  x, y, z, Bx, By, Bz "
                + "\n ---> Min values x,y,z: "
                + _minx + " " + _miny + " " + _minz
                + "\n ---> Max values x,y,z: "
                + _maxx + " " + _maxy + " " + _maxz
                + "\n Maximum Field strength: " + _bMax + " "
                + "\n ---> The field will be offset by " + _xOffset + " " + _yOffset + " " + _zOffset);

        _dx = _maxx - _minx;
        _dy = _maxy - _miny;
        _dz = _maxz - _minz;
        System.out.println("\n ---> Range of values x,y,z: "
                + _dx + " " + _dy + " " + _dz
                + "\n-----------------------------------------------------------");

        br.close();
    }

    @Override
    public void getField(double[] position, double[] b)
    {
        getField(position[0], position[1], position[2]);
        System.arraycopy(_Bfield, 0, b, 0, 3);
    }

    @Override
    public Hep3Vector getField(Hep3Vector position)
    {
        getField(position.x(), position.y(), position.z());
        return new BasicHep3Vector(_Bfield[0], _Bfield[1], _Bfield[2]);
    }

    @Override
    public double[] getField(double[] position)
    {
        getField(position[0], position[1], position[2]);
        double[] field = {_Bfield[0], _Bfield[1], _Bfield[2]};
        return field;
    }

    @Override
    void getField(double x, double y, double z, BasicHep3Vector field)
    {
        getField(x, y, z);
        field.setV(_Bfield[0], _Bfield[1], _Bfield[2]);
    }

    public double[] globalOffset()
    {
        return new double[]{_xOffset, _yOffset, _zOffset};
    }

    private void getField(double x, double y, double z)
    {
        // allow for offsets
        x -= _xOffset;
        y -= _yOffset;
        z -= _zOffset;
        // Check that the point is within the defined region 
        if (x >= _minx && x <= _maxx
                && y >= _miny && y <= _maxy
                && z >= _minz && z <= _maxz) {

            // Position of given point within region, normalized to the range
            // [0,1]
            double xfraction = (x - _minx) / _dx;
            double yfraction = (y - _miny) / _dy;
            double zfraction = (z - _minz) / _dz;

            //double xdindex, ydindex, zdindex;
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
            _Bfield[0]
                    = _xField[xindex][yindex][zindex] * (1 - xlocal) * (1 - ylocal) * (1 - zlocal)
                    + _xField[xindex][yindex][zindex + 1] * (1 - xlocal) * (1 - ylocal) * zlocal
                    + _xField[xindex][yindex + 1][zindex] * (1 - xlocal) * ylocal * (1 - zlocal)
                    + _xField[xindex][yindex + 1][zindex + 1] * (1 - xlocal) * ylocal * zlocal
                    + _xField[xindex + 1][yindex][zindex] * xlocal * (1 - ylocal) * (1 - zlocal)
                    + _xField[xindex + 1][yindex][zindex + 1] * xlocal * (1 - ylocal) * zlocal
                    + _xField[xindex + 1][yindex + 1][zindex] * xlocal * ylocal * (1 - zlocal)
                    + _xField[xindex + 1][yindex + 1][zindex + 1] * xlocal * ylocal * zlocal;
            _Bfield[1]
                    = _yField[xindex][yindex][zindex] * (1 - xlocal) * (1 - ylocal) * (1 - zlocal)
                    + _yField[xindex][yindex][zindex + 1] * (1 - xlocal) * (1 - ylocal) * zlocal
                    + _yField[xindex][yindex + 1][zindex] * (1 - xlocal) * ylocal * (1 - zlocal)
                    + _yField[xindex][yindex + 1][zindex + 1] * (1 - xlocal) * ylocal * zlocal
                    + _yField[xindex + 1][yindex][zindex] * xlocal * (1 - ylocal) * (1 - zlocal)
                    + _yField[xindex + 1][yindex][zindex + 1] * xlocal * (1 - ylocal) * zlocal
                    + _yField[xindex + 1][yindex + 1][zindex] * xlocal * ylocal * (1 - zlocal)
                    + _yField[xindex + 1][yindex + 1][zindex + 1] * xlocal * ylocal * zlocal;
            _Bfield[2]
                    = _zField[xindex][yindex][zindex] * (1 - xlocal) * (1 - ylocal) * (1 - zlocal)
                    + _zField[xindex][yindex][zindex + 1] * (1 - xlocal) * (1 - ylocal) * zlocal
                    + _zField[xindex][yindex + 1][zindex] * (1 - xlocal) * ylocal * (1 - zlocal)
                    + _zField[xindex][yindex + 1][zindex + 1] * (1 - xlocal) * ylocal * zlocal
                    + _zField[xindex + 1][yindex][zindex] * xlocal * (1 - ylocal) * (1 - zlocal)
                    + _zField[xindex + 1][yindex][zindex + 1] * xlocal * (1 - ylocal) * zlocal
                    + _zField[xindex + 1][yindex + 1][zindex] * xlocal * ylocal * (1 - zlocal)
                    + _zField[xindex + 1][yindex + 1][zindex + 1] * xlocal * ylocal * zlocal;

        } else {
            _Bfield[0] = 0.0;
            _Bfield[1] = 0.0;
            _Bfield[2] = 0.0;
        }
    }

    //TODO pass double[] as argument to minimize internal array creation
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
