package org.lcsim.geometry.segmentation;

import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;

import java.util.List;

import org.jdom.DataConversionException;
import org.jdom.Element;
import org.lcsim.detector.identifier.IIdentifierDictionary;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.geometry.Subdetector;
import org.lcsim.geometry.layer.Layer;
import org.lcsim.geometry.layer.LayerStack;
import org.lcsim.geometry.subdetector.CylindricalCalorimeter;
import org.lcsim.geometry.util.IDDescriptor;
import org.lcsim.geometry.util.IDEncoder;

/**
 * Cartesian XYZ grid segmentation, primarily used for readout of planes like
 * those in a Polyhedra Calorimeter with Trapezoidal staves. This segmentation
 * is based on a local coordinate system.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @author Guilherme Lima
 * @version $Id: GridXYZ.java,v 1.32 2013/01/24 22:24:20 jeremy Exp $
 */
// FIXME This class needs to be refactored.
public class GridXYZ extends SegmentationBase
{
    private double gridSizeX = 0;
    private double gridSizeY = 0;
    private double gridSizeZ = 0;

    private int xIndex = -1;
    private int yIndex = -1;
    private int zIndex = -1;
    private int barrelIndex = -1;
    private int systemIndex = -1;

    private double[] localPosition = { 0, 0, 0 };

    private double[] globalPosition = { 0, 0, 0 };
    
    private static final String fieldNames[] = {"x", "y", "z"};

    /** Creates a new instance of GridXYZ */
    public GridXYZ(Element node) throws DataConversionException
    {
        super(node);

        this.cellSizes.clear();
        
        if (node.getAttribute("gridSizeX") != null)
        {
            gridSizeX = node.getAttribute("gridSizeX").getDoubleValue();
            this.cellSizes.add(gridSizeX);
        }

        if (node.getAttribute("gridSizeY") != null)
        {
            gridSizeY = node.getAttribute("gridSizeY").getDoubleValue();
            this.cellSizes.add(gridSizeY);
        }

        if (node.getAttribute("gridSizeZ") != null)
        {
            gridSizeZ = node.getAttribute("gridSizeZ").getDoubleValue();
            this.cellSizes.add(gridSizeZ);
        }
    }
    
    public String[] getSegmentationFieldNames() {
        return fieldNames;
    }

    public boolean supportsNeighbours()
    {
        return true;
    }

    /**
     * Find neighbouring cells to the current cell. Cell neighbors are found
     * based on the direction (theta,phi) of the reference cell w.r.t the
     * origin.
     * 
     * @return array of cellIDs for the neighbouring cells
     */
    // FIXME: This method is overly complicated given that the grid will be symmetric 
    //        about 0,0 through all layers for all the subdetectors that we define.  
    //        Should just return same u,v and layer +-1.
    public long[] getNeighbourIDs(int layerRange, int xRange, int yRange)
    {
        // System.out.println("Nonproj neighbs: "+layerRange+" "+xRange+"
        // "+yRange);
        IDEncoder encoder = new IDEncoder(descriptor);
        encoder.setValues(values);
        long saveID = encoder.getID();

        //		int klay = values[layerIndex];
        //		int kx = values[xIndex];
        //		int ky = values[yIndex];
        //		System.out.println("Ref.ID: ref="+klay+" "+kx+" "+ky
        //		+" (hex "+Long.toHexString(saveID)+")");
        //		long system = (saveID>>7) & 0x1ff;
        //		System.out.println("hit pos: x="+getX()+", y="+getY()
        //		+", z="+getZ()+", system="+system);

        int nMax = (2 * layerRange + 1) * (2 * xRange + 1) * (2 * yRange + 1) - 1;
        long[] result = new long[nMax];

        // theta and phi are used to find central neighbors in other layers
        double theta = getTheta();
        double phi = getPhi();

        // this is not general, but specific to endcaps
        double dx = gridSizeX;
        double dy = gridSizeY;
        int zBins = detector.getLayering().getLayerCount();
        double Rmax = ((CylindricalCalorimeter) detector).getOuterRadius();
        double Rmin = this.getRMin();

        int size = 0;
        int refLayer = values[layerIndex];
        boolean isNegativeZ = getZ() < 0 ? true : false;
        for (int i = -layerRange; i <= layerRange; ++i)
        {
            int ilay = refLayer + i;

            if (ilay < 0 || ilay >= zBins)
                continue;
            encoder.setValue(layerIndex, ilay);

            int xBins = (int) Math.ceil(2 * Rmax / dx);
            int yBins = xBins;
            int rBinMin = (int) Math.floor(Rmin / dx); // Inner radius, in units of cell width
            int rBinMax = (int) Math.ceil(Rmax / dx); // Outer radius, in units of cell width

            // project theta,phi to sensitive middle of current layer
            double z = getDepthSensitiveMid(ilay);
            if (isNegativeZ)
                z = -z;
            double rho = z * Math.tan(theta);
            double x = rho * Math.cos(phi);
            double y = rho * Math.sin(phi);
            // System.out.println("layer "+ilay+", cylR="+rho
            // +", theta="+(theta*180./Math.PI)
            // +", phi="+(phi*180./Math.PI)
            // +", rvec=( "+x+", "+y+", "+z+")");

            long id = this.findCellContainingXYZ(x, y, z);
            if (id == 0)
                continue;

            this.setID(id);
            // System.out.println("from findXYZ: x="+getX()+", y="+getY()+",
            // z="+getZ());
            for (int j = -xRange; j <= xRange; ++j)
            {
                int ix = values[xIndex] + j;

                if (ix < -xBins / 2 || ix >= xBins / 2)
                    continue;
                encoder.setValue(xIndex, ix);

                for (int k = -yRange; k <= yRange; ++k)
                {
                    // skip reference cell
                    if (i == 0 && j == 0 && k == 0)
                        continue;

                    int iy = values[yIndex] + k;
                    if (iy < -yBins / 2 || iy >= yBins / 2)
                        continue;

                    //					System.out.println("Adding neighbor: "+ilay+" "+ix+" "
                    //					+iy+", total="+ (size+1));
                    long candidate = encoder.setValue(yIndex, iy);
                    if (ix * ix + iy * iy >= (rBinMax - 1) * (rBinMax - 1) || ix * ix + iy * iy <= (rBinMin + 1) * (rBinMin + 1))
                    {
                        // We're near the edge. Verify that this is actually a legal value:
                        this.setID(candidate);
                        double candAbsZ = Math.abs(this.getZ());
                        double candRhoSq = (this.getX() * this.getX() + this.getY() * this.getY());
                        boolean candValid = (candAbsZ >= getZMin() && candAbsZ <= getZMax() && candRhoSq >= (getRMin() * getRMin()) && candRhoSq <= (getRMax() * getRMax()));
                        if (candValid)
                        {
                            // OK -- neighbour is at a legal spacepoint
                            result[size++] = candidate;
                        }
                        else
                        {
                            // Failure -- this can happen if the "neighbour" was just us wandering off the grid
                            // Not serious -- don't add the candidate to the output, but don't panic either.
                        }
                        // Reset ID to previous value
                        this.setID(id);
                    }
                    else
                    {
                        // We're far from the edge. Go ahead and add it without spending CPU time on a check
                        result[size++] = candidate;
                    }
                }
            }
        }

        // reset encoder and decoder
        this.setID(saveID);
        if (size < result.length)
        {
            long[] temp = new long[size];
            System.arraycopy(result, 0, temp, 0, size);
            result = temp;
        }

        return result;
    }

    // Not for public use, this is needed to calculate positions
    // and number of cells, etc.

    // FIXME: Following four methods use the specific subdetector type CylindricalCalorimeter
    //        but should NOT.

    public double getZMin()
    {
        return ((CylindricalCalorimeter) detector).getZMin();
    }

    public double getZMax()
    {
        return ((CylindricalCalorimeter) detector).getZMax();
    }

    public double getRMin()
    {
        return ((CylindricalCalorimeter) detector).getInnerRadius();
    }

    public double getRMax()
    {
        return ((CylindricalCalorimeter) detector).getOuterRadius();
    }

    // FIXME: Doesn't belong here, as it is always computable from x and y.

    public double getPhi()
    {
        double phi = atan2(getY(), getX());

        if (phi < 0)
        {
            phi += 2 * PI;
        }

        return phi;
    }

    // FIXME: Doesn't belong here, as it is always computable given x and y.

    public double getTheta()
    {
        double theta = atan(getCylindricalRadiusFromPosition() / getZ());

        if (theta < 0)
        {
            theta += PI;
        }

        return theta;
    }

    public double getX()
    {
        return getPosition()[0];
    }

    public double getY()
    {
        return getPosition()[1];
    }

    public double getZ()
    {
        return getPosition()[2];
    }

    public double[] getPosition()
    {
        return globalPosition;
    }

    private void computePosition()
    {
        computeLocalPosition();
        computeGlobalPosition();
    }

    private void computeLocalPosition()
    {
        localPosition[0] = localPosition[1] = localPosition[2] = 0.0;
        computeLocalX();
        computeLocalY();
        computeLocalZ();
    }

    List<Integer> geomFields;
    IIdentifierHelper helper;
    IIdentifierDictionary iddict;

    private void computeGlobalPosition()
    {
        globalPosition = transformLocalToGlobal(localPosition);
    }

    public void setSubdetector(Subdetector subdet)
    {
        super.setSubdetector(subdet);
    }

    private void computeLocalX()
    {
        if (xIndex != -1)
        {
            localPosition[0] = (((double) getValue(xIndex)) + 0.5) * gridSizeX;
        }
    }

    private void computeLocalY()
    {
        if (yIndex != -1)
        {
            localPosition[1] = (((double) getValue(yIndex)) + 0.5) * gridSizeY;
        }
    }

    private void computeLocalZ()
    {
        if (zIndex != -1)
        {
            localPosition[2] = (((double) getValue(zIndex)) + 0.5) * gridSizeZ;
        }
    }

    /**
     * Overridden to cache the global position.
     */
    public void setID(long id)
    {
        super.setID(id);
        computePosition();
    }

    public double getCylindricalRadiusFromPosition()
    {
        return sqrt(getX() * getX() + getY() * getY());
    }

    /**
     * Check for a valid grid size for each of XYZ. If the grid is not valid,
     * then leave those indices flagged with invalid values. It is actually okay
     * to have all 0's, because this will return the center of the volume, at
     * least in analogous Geant4 impl of this class.
     */
    public void setIDDescription(IDDescriptor id)
    {
        super.setIDDescription(id);

        if (gridSizeX != 0)
        {
            xIndex = id.indexOf("x");
        }

        if (gridSizeY != 0)
        {
            yIndex = id.indexOf("y");
        }

        if (gridSizeZ != 0)
        {
            zIndex = id.indexOf("z");
        }

        try
        {
            barrelIndex = id.indexOf("barrel");
        }
        catch (IllegalArgumentException x)
        {
            barrelIndex = -1;
        }
        systemIndex = id.indexOf("system");
    }

    /**
     * Returns positive distance from IR to center of sensitive slice of any
     * layer
     * 
     * @param layer The layer index.
     */
    private double getDepthSensitiveMid(int ilay)
    {
        LayerStack stack = detector.getLayering().getLayers();
        Layer layer = stack.getLayer(ilay);

        double preLayers = 0;
        if (ilay > 0)
            preLayers = stack.getSectionThickness(0, ilay - 1);

        return this.getZMin() + preLayers + layer.getThicknessToSensitiveMid();
    }

    /**
     * Return the cell which contains a given point (x,y,z). If point is not
     * contained in this component, zero is returned.
     * 
     * @param x Cartesian X coordinate.
     * @param y Cartesian Y coordinate.
     * @param z Cartesian Z coordinate.
     * 
     * @return ID of cell containing the point (maybe either in absorber or live
     *         material)
     */
    public long findCellContainingXYZ(double x, double y, double z)
    {
        // validate point
        double absz = Math.abs(z);
        if (absz < getZMin())
            return 0;
        if (absz > getZMax())
            return 0;
        double rho = Math.sqrt(x * x + y * y);
        if (rho < getRMin())
            return 0;
        if (rho > getRMax())
            return 0;

        // ok, point is valid, so a valid ID should be returned
        int ix = getXBin(x);
        int iy = getYBin(y);
        int ilay = getLayerBin(absz);
        int ibar = (z < 0 ? 2 : 1);
        int system = detector.getSystemID();

        IDEncoder enc = new IDEncoder(descriptor);
        enc.setValue(layerIndex, ilay);
        enc.setValue(xIndex, ix);
        enc.setValue(yIndex, iy);
        enc.setValue(barrelIndex, ibar);
        enc.setValue(systemIndex, system);
        long resultID = enc.getID();
        return resultID;
    }

    public int getLayerBin(double z)
    {
        // In order to be general, we should not assume that all
        // layers have the same thickness. Therefore, one has to
        // guess the starting layer (based on average thickness), and
        // then navigate through layers until one finds the right one
        double depth = z - getZMin();
        double mean_t = (getZMax() - getZMin()) / getNumberOfLayers();

        int ilay = (int) Math.floor(depth / mean_t);
        LayerStack stack = getLayering().getLayers();
        Layer layer = stack.getLayer(ilay);
        double depHi = stack.getThicknessToLayerBack(ilay);
        double depLo = depHi - layer.getThickness();
        for (;;)
        {
            if (depth > depLo && depth <= depHi)
                return ilay;
            if (depth <= depLo)
            {
                --ilay;
                depHi = depLo;
                layer = stack.getLayer(ilay);
                depLo -= layer.getThickness();
            }
            if (depth > depHi)
            {
                ++ilay;
                depLo = depHi;
                layer = stack.getLayer(ilay);
                depHi += layer.getThickness();
            }
        }
    }

    public int getXBin(double x)
    {
        return getBin(x, gridSizeX);
    }

    public int getYBin(double y)
    {
        return getBin(y, gridSizeY);
    }

    public int getBin(double u, double gridSizeU)
    {
        //int numBins = (int) Math.floor(2 * getRMax() / gridSizeU);
        double u0 = gridSizeU / 2;
        int iu = (int) Math.floor((u - u0) / gridSizeU + 0.5);
        return iu;
    }

    public double getGridSizeX()
    {
        return gridSizeX;
    }

    public double getGridSizeY()
    {
        return gridSizeY;
    }

    public double getGridSizeZ()
    {
        return gridSizeZ;
    }

}
