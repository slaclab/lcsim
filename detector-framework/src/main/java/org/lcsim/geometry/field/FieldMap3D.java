package org.lcsim.geometry.field;

import static java.lang.Math.sqrt;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.StringTokenizer;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.util.cache.FileCache;

/**
 *
 * @author Norman A Graf
 *
 * @version $Id:
 */
public class FieldMap3D extends AbstractFieldMap {
    private double[][][] xField;
    private double[][][] yField;
    private double[][][] zField;
    // The dimensions of the table
    private int nx, ny, nz;
    // The physical limits of the defined region
    private double minx, maxx, miny, maxy, minz, maxz;
    // The physical extent of the defined region
    private double dx, dy, dz;
    // Offsets if field map is not in global coordinates
    private double xOffset;
    private double yOffset;
    private double zOffset;
    // maximum field strength
    private double bMax;
    private double[] bfield = new double[3];
    String filename;
    URL url;

    boolean debug = false;

    public FieldMap3D(Element node) throws JDOMException {
        super(node);

        if (System.getProperty("org.lcsim.geometry.field.debug") != null) {
            this.debug = true;
        }

        xOffset = node.getAttribute("xoffset").getDoubleValue();
        yOffset = node.getAttribute("yoffset").getDoubleValue();
        zOffset = node.getAttribute("zoffset").getDoubleValue();

        filename = node.getAttributeValue("filename");

        if (node.getAttribute("url") != null) {
            try {
                url = new URL(node.getAttribute("url").getValue());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            setup();
        } catch (Exception e) {
            throw new RuntimeException("Error reading the field map.", e);
        }
    }

    private static int BUFFER_SIZE = 1024;

    private static void untar(InputStream is, File cacheDir) throws IOException {
        GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(is);
        File destFile = null;
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                int count;
                byte data[] = new byte[BUFFER_SIZE];
                destFile = new File(Paths.get(cacheDir.getAbsolutePath(), entry.getName()).toString());
                FileOutputStream fos = new FileOutputStream(destFile, false);
                try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
                    while ((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
                        dest.write(data, 0, count);
                    }
                    dest.close();
                }
                break;
            }
            tarIn.close();
        }
    }

    private static File getFieldMapFile(File file, File cacheDir) throws IOException {
        GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(new FileInputStream(file));
        File destFile = null;
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                destFile = new File(Paths.get(cacheDir.getAbsolutePath(), entry.getName()).toString());
                break;
            }
            tarIn.close();
        }
        return destFile;
    }


    private void setup() throws IOException {

        if (debug) {
            System.out.println("-----------------------------------------------------------");
            System.out.println("FieldMap3D ");
            System.out.println("-----------------------------------------------------------");
        }

        InputStream fis;
        BufferedReader br;
        String line;
        File file  = new File(filename);
        if (!file.exists() && url != null) {
            if (debug) {
                System.out.println("Using field map URL '" + url.toString() + "'");
            }
            file = cacheFile();
        } else {
            if (debug) {
                System.out.println("Using field map local file '" + file.getPath() + "'");
            }
        }

        fis = new FileInputStream(file);

        if (debug) {
            System.out.println("Reading the field grid from '" + file.getPath() + "' ... ");
        }

        br = new BufferedReader(new InputStreamReader(fis));
        // ignore the first blank line
        line = br.readLine();
        // next line has table dimensions
        line = br.readLine();
        // read in the table dimensions of the file
        StringTokenizer st = new StringTokenizer(line, " ");
        nx = Integer.parseInt(st.nextToken());
        ny = Integer.parseInt(st.nextToken());
        nz = Integer.parseInt(st.nextToken());

        // Set up storage space for table
        xField = new double[nx + 1][ny + 1][nz + 1];
        yField = new double[nx + 1][ny + 1][nz + 1];
        zField = new double[nx + 1][ny + 1][nz + 1];

        // Ignore other header information
        // The first line whose second character is '0' is considered to
        // be the last line of the header.
        do {
            line = br.readLine();
            if (debug) {
                System.out.println(line);
            }
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
        for (ix = 0; ix < nx; ix++) {
            for (iy = 0; iy < ny; iy++) {
                for (iz = 0; iz < nz; iz++) {
                    line = br.readLine();
                    st = new StringTokenizer(line, " ");
                    xval = Double.parseDouble(st.nextToken());
                    yval = Double.parseDouble(st.nextToken());
                    zval = Double.parseDouble(st.nextToken());
                    bx = Double.parseDouble(st.nextToken())*conversionFactor;
                    by = Double.parseDouble(st.nextToken())*conversionFactor;
                    bz = Double.parseDouble(st.nextToken())*conversionFactor;
                    if (ix == 0 && iy == 0 && iz == 0) {
                        minx = xval;
                        miny = yval;
                        minz = zval;
                    }
                    xField[ix][iy][iz] = bx;
                    yField[ix][iy][iz] = by;
                    zField[ix][iy][iz] = bz;
                    double b = bx * bx + by * by + bz * bz;
                    if (b > bMax) {
                        bMax = b;
                    }
                }
            }
        }
        bMax = sqrt(bMax);

        maxx = xval;
        maxy = yval;
        maxz = zval;

        if (debug) {
            System.out.println("\n ---> ... done reading ");
            System.out.println(" ---> assumed the order:  x, y, z, Bx, By, Bz "
                    + "\n ---> Min values x,y,z: "
                    + minx + " " + miny + " " + minz
                    + "\n ---> Max values x,y,z: "
                    + maxx + " " + maxy + " " + maxz
                    + "\n Maximum Field strength: " + bMax + " "
                    + "\n ---> The field will be offset by " + xOffset + " " + yOffset + " " + zOffset);
        }

        dx = maxx - minx;
        dy = maxy - miny;
        dz = maxz - minz;
        if (debug) {
            System.out.println("\n ---> Range of values x,y,z: "
                    + dx + " " + dy + " " + dz
                    + "\n-----------------------------------------------------------");
        }

        br.close();
    }

    private File cacheFile() throws IOException, FileNotFoundException {
        File file;
        FileCache cache = new FileCache();
        cache.setPrintStream(null); /* Turn off progress print out */
        File cacheFile = cache.getCachedFile(url);
        if (cacheFile.getAbsolutePath().endsWith(".tar.gz")) {
            file = getFieldMapFile(cacheFile, cache.getCacheDirectory());
            if (!file.exists()) {
                untar(new FileInputStream(cacheFile), cache.getCacheDirectory());
            }
        } else {
            file = cacheFile;
        }
        return file;
    }

    @Override
    public void getField(double[] position, double[] b) {
        getField(position[0], position[1], position[2]);
        System.arraycopy(bfield, 0, b, 0, 3);
    }

    @Override
    public Hep3Vector getField(Hep3Vector position) {
        getField(position.x(), position.y(), position.z());
        return new BasicHep3Vector(bfield[0], bfield[1], bfield[2]);
    }

    @Override
    public double[] getField(double[] position) {
        getField(position[0], position[1], position[2]);
        double[] field = {bfield[0], bfield[1], bfield[2]};
        return field;
    }

    @Override
    void getField(double x, double y, double z, BasicHep3Vector field) {
        getField(x, y, z);
        field.setV(bfield[0], bfield[1], bfield[2]);
    }

    public double[] globalOffset() {
        return new double[]{xOffset, yOffset, zOffset};
    }

    private void getField(double x, double y, double z) {
        // allow for offsets
        x -= xOffset;
        y -= yOffset;
        z -= zOffset;
        // Check that the point is within the defined region
        if (x >= minx && x <= maxx
                && y >= miny && y <= maxy
                && z >= minz && z <= maxz) {

            // Position of given point within region, normalized to the range
            // [0,1]
            double xfraction = (x - minx) / dx;
            double yfraction = (y - miny) / dy;
            double zfraction = (z - minz) / dz;

            //double xdindex, ydindex, zdindex;
            // Position of the point within the cuboid defined by the
            // nearest surrounding tabulated points
            double[] xmodf = modf(xfraction * (nx - 1));
            double[] ymodf = modf(yfraction * (ny - 1));
            double[] zmodf = modf(zfraction * (nz - 1));

            // The indices of the nearest tabulated point whose coordinates
            // are all less than those of the given point
            int xindex = (int) xmodf[0];
            int yindex = (int) ymodf[0];
            int zindex = (int) zmodf[0];
            double xlocal = xmodf[1];
            double ylocal = ymodf[1];
            double zlocal = zmodf[1];
            // bilinear interpolation
            bfield[0]
                    = xField[xindex][yindex][zindex] * (1 - xlocal) * (1 - ylocal) * (1 - zlocal)
                    + xField[xindex][yindex][zindex + 1] * (1 - xlocal) * (1 - ylocal) * zlocal
                    + xField[xindex][yindex + 1][zindex] * (1 - xlocal) * ylocal * (1 - zlocal)
                    + xField[xindex][yindex + 1][zindex + 1] * (1 - xlocal) * ylocal * zlocal
                    + xField[xindex + 1][yindex][zindex] * xlocal * (1 - ylocal) * (1 - zlocal)
                    + xField[xindex + 1][yindex][zindex + 1] * xlocal * (1 - ylocal) * zlocal
                    + xField[xindex + 1][yindex + 1][zindex] * xlocal * ylocal * (1 - zlocal)
                    + xField[xindex + 1][yindex + 1][zindex + 1] * xlocal * ylocal * zlocal;
            bfield[1]
                    = yField[xindex][yindex][zindex] * (1 - xlocal) * (1 - ylocal) * (1 - zlocal)
                    + yField[xindex][yindex][zindex + 1] * (1 - xlocal) * (1 - ylocal) * zlocal
                    + yField[xindex][yindex + 1][zindex] * (1 - xlocal) * ylocal * (1 - zlocal)
                    + yField[xindex][yindex + 1][zindex + 1] * (1 - xlocal) * ylocal * zlocal
                    + yField[xindex + 1][yindex][zindex] * xlocal * (1 - ylocal) * (1 - zlocal)
                    + yField[xindex + 1][yindex][zindex + 1] * xlocal * (1 - ylocal) * zlocal
                    + yField[xindex + 1][yindex + 1][zindex] * xlocal * ylocal * (1 - zlocal)
                    + yField[xindex + 1][yindex + 1][zindex + 1] * xlocal * ylocal * zlocal;
            bfield[2]
                    = zField[xindex][yindex][zindex] * (1 - xlocal) * (1 - ylocal) * (1 - zlocal)
                    + zField[xindex][yindex][zindex + 1] * (1 - xlocal) * (1 - ylocal) * zlocal
                    + zField[xindex][yindex + 1][zindex] * (1 - xlocal) * ylocal * (1 - zlocal)
                    + zField[xindex][yindex + 1][zindex + 1] * (1 - xlocal) * ylocal * zlocal
                    + zField[xindex + 1][yindex][zindex] * xlocal * (1 - ylocal) * (1 - zlocal)
                    + zField[xindex + 1][yindex][zindex + 1] * xlocal * (1 - ylocal) * zlocal
                    + zField[xindex + 1][yindex + 1][zindex] * xlocal * ylocal * (1 - zlocal)
                    + zField[xindex + 1][yindex + 1][zindex + 1] * xlocal * ylocal * zlocal;

        } else {
            bfield[0] = 0.0;
            bfield[1] = 0.0;
            bfield[2] = 0.0;
        }
    }

    //TODO pass double[] as argument to minimize internal array creation
    private double[] modf(double fullDouble) {
        int intVal = (int) fullDouble;
        double remainder = fullDouble - intVal;

        double[] retVal = new double[2];
        retVal[0] = intVal;
        retVal[1] = remainder;

        return retVal;
    }
}
