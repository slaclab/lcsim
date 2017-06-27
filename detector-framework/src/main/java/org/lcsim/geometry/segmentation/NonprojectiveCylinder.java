/*
 * NonprojectiveCylinder.java
 *
 * Created on March 30, 2005, 3:09 PM
 */
package org.lcsim.geometry.segmentation;

import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.floor;
import static java.lang.Math.PI;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.lcsim.geometry.layer.Layer;
import org.lcsim.geometry.layer.LayerStack;
import org.lcsim.geometry.util.BaseIDDecoder;
import org.lcsim.geometry.util.IDDescriptor;
import org.lcsim.geometry.util.IDEncoder;

/**
 * @author jeremym
 *
 * Nonprojective segmentation of a cylinder with delta z and phi as parameters.
 *
 */
public class NonprojectiveCylinder extends BarrelCylinderSegmentationBase
{
    private double gridSizePhi;
    private double gridSizeZ;

    private int zIndex;
    private int phiIndex;
    private int systemIndex;
    private int barrelIndex;
    
    private static final String fieldNames[] = {"gridSizePhi", "gridSizeZ"};

    /** Creates a new instance of NonprojectiveCylinder */
    public NonprojectiveCylinder(Element node) throws DataConversionException
    {
        super(node);

        gridSizePhi = node.getAttribute("gridSizePhi").getDoubleValue();
        gridSizeZ = node.getAttribute("gridSizeZ").getDoubleValue();
        
        this.cellSizes.add(0, gridSizePhi);
        this.cellSizes.add(1, gridSizeZ);
    }
    
    public String[] getSegmentationFieldNames() {
        return fieldNames;
    }
    
    public double getCellSizeU()
    {
        return gridSizeZ;
    }
    
    public double getCellSizeV()
    {
        return gridSizeZ;
    }

    void setGridSizePhi(double gsp)
    {
        gridSizePhi = gsp;
    }

    void setGridSizeZ(double gsz)
    {
        gridSizeZ = gsz;
    }

    public double getGridSizePhi()
    {
        return gridSizePhi;
    }

    public double getGridSizeZ()
    {
        return gridSizeZ;
    }

    public double getPhi()
    {
        return (((double) getValue(phiIndex)) + 0.5) * computeDeltaPhiForLayer();
    }

    public double getTheta()
    {
        double x = this.getX();
        double y = this.getY();
        double theta = atan(sqrt(x * x + y * y) / getZ());

        /** Normalize to positive theta. */
        if (theta < 0)
        {
            theta += PI;
        }

        return theta;
    }

    public double getX()
    {
        return getDistanceToSensitive( getLayer() ) * cos( getPhi() );
    }

    public double getY()
    {
        return getDistanceToSensitive(getLayer()) * sin(getPhi());
    }

    public double getZ()
    {
        return ((double) getValue(zIndex) + 0.5) * gridSizeZ;
    }

    public double computeDeltaPhiForLayer(int layer)
    {
        double circ = getDistanceToSensitive(layer) * (2 * PI);
        int nphi = (int) Math.floor(circ / gridSizePhi);
        double deltaPhi = (2 * PI) / nphi;
        return deltaPhi;
    }

    public double computeDeltaPhiForLayer()
    {
        return computeDeltaPhiForLayer(getLayer());
    }

    public void setIDDescription(IDDescriptor id)
    {
        super.setIDDescription(id);
        phiIndex = id.indexOf("phi");
        zIndex = id.indexOf("z");
        systemIndex = id.indexOf("system");
        barrelIndex = id.indexOf("barrel");
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
    public long[] getNeighbourIDs(int layerRange, int zRange, int phiRange)
    {
        IDEncoder gnEncoder = new IDEncoder(descriptor);
        BaseIDDecoder gnDecoder = new BaseIDDecoder(descriptor);
        gnEncoder.setValues(values);
        long origID = gnEncoder.getID();
        gnDecoder.setID(gnEncoder.getID());

        int nMax = (2*layerRange + 1)*(2*zRange + 1)*(2*phiRange + 1) - 1;
        long[] result = new long[nMax];

        // theta and phi are used to find central neighbors in other layers
        double theta = getTheta();
        double phi = getPhi();

        // Be careful with # of Z bins.
	// The rules are:
	//   * A cell is valid if its center has (z >= getZMin() && z <= getZMax())
	//   * The center of a bin is always at z = (integer + 0.5) * gridSizeZ
	//      => Bins are arranged about z=0, with a bin edge at z=0
	//      => If getZMin()==-getZMax() then there is always an even number of bins
	//
	// If things are nicely symmetric, i.e. getZMin()==-getZMax(), then this turns
	// into a very easy problem: if we have n bins ON EACH SIDE (for integer n) then
	//       n - 0.5 <= zMax/gridSizeZ < n + 0.5
	// and so we can work backwards (and add in the other half) to obtain the total
	// number of bins 2n as 2*Math.round((zMax-zMin)/(2.0*gridSizeZ)).
	//
	// If things are not symmetric but span the z=0 point then we're OK as long as
	// we're careful -- the easiest way to look at it is to do each half of the
	// detector separately:
	//    Number of bins in z>0:  Math.round((zMax - 0.0)/gridSizeZ)
	//    Number of bins in z<0:  Math.round((0.0 - zMin)/gridSizeZ)
	//
	// If things are asymmetric and do not include z=0 then things are more tricky again.
	// We'd have to work like this:
	//    a) find the first bin-center z1 after GetZMin()
	//    b) find the last bin-center z2 before GetZMax()
	//    c) count number of bins as Math.round((z2-z1)/gridSizeZ)
	// where 
	//   z1 = gridSizeZ * (Math.ceil ((zMin/gridSizeZ)+0.5) - 0.5)
	//   z2 = gridSizeZ * (Math.floor((zMax/gridSizeZ)-0.5) + 0.5)
	//
	// So the most general form for the number of bins should be
	//   Math.round( (gridSizeZ*(Math.floor((zMax/gridSizeZ)-0.5)+0.5) - gridSizeZ*(Math.floor((zMax/gridSizeZ)-0.5)+0.5) ) / gridSizeZ )
	//
	// ... but we will assume that the detector is symmetric here -- that is true
	// for any barrel calorimeter generated from a compact.xml geometry file.
	int zBins = 2 * (int) Math.round( (getZMax()-getZMin())/(2.0*getGridSizeZ()) );

        int size = 0;
        for (int i = -layerRange; i <= layerRange; ++i)
        {
            int ilay = values[layerIndex] + i;

            if (ilay < 0 || ilay >= getNumberOfLayers())
                continue;
            gnEncoder.setValue(layerIndex, ilay);

            double dphi = this.computeDeltaPhiForLayer(ilay);
            int phiBins = (int) Math.round(2 * Math.PI / dphi); // Use round() not floor() since a floor() was already applied in the definition of dphi

	    if (i != 0) {
                double cylR = getRadiusSensitiveMid(ilay);
                double x = cylR * Math.cos(phi);
                double y = cylR * Math.sin(phi);
                double z = cylR / Math.tan(theta);

                long id = this.findCellContainingXYZ(x,y,z);
                if(id==0) continue;
                // save indices in a new array, as values[] keeps the original ref
                gnDecoder.setID(id);
            } else {
		// This is the original layer => center cell is just the original cell
                gnDecoder.setID(origID);
            }

            for (int j = -zRange; j <= zRange; ++j)
            {
                int iz = gnDecoder.getValue(zIndex) + j;

                if (iz < -zBins / 2 || iz >= zBins / 2) {
		    // We make the implicit assumption that detector is symmetric, and so number 
		    // of bins is even and looks like [-n, +n-1]. For example, for the
		    // very simple case of two bins, the range of bin indices is {-1, 0}.
		    // In this instance we're outside the valid range, so skip this bin.
                    continue;
		}
                gnEncoder.setValue(zIndex, iz);

                for (int k = -phiRange; k <= phiRange; ++k)
                {
                    // skip reference cell (not a neighbor of its own)
                    if (i==0 && j==0 && k==0) continue;

                    int iphi = gnDecoder.getValue(phiIndex) + k;
                    // phi is cyclic
                    if (iphi < 0) iphi += phiBins;
                    if (iphi >= phiBins) iphi -= phiBins;

                    if (iphi < 0 || iphi >= phiBins) continue;
                    gnEncoder.setValue(phiIndex, iphi);
                    result[size++] = gnEncoder.getID();
                }
            }
        }

        // resize resulting array if necessary
        if (size < result.length)
        {
            long[] temp = new long[size];
            System.arraycopy(result, 0, temp, 0, size);
            result = temp;
        }

        return result;
    }

    /**
     * Return the cell which contains a given point (x,y,z), or zero.
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
        if (z < getZMin()) return 0;
        if (z > getZMax()) return 0;
        double rho = Math.sqrt(x * x + y * y);
        if (rho < getRMin()) return 0;
        if (rho > getRMax()) return 0;

        // ok, point is valid, so a valid ID should be returned
        int ilay = getLayerBin(rho);
        int iz = getZBin(z);

        double phi = Math.atan2(y, x);
        if (phi < 0) phi += 2 * Math.PI;
        int iphi = getPhiBin(ilay, phi);

        IDEncoder enc = new IDEncoder(descriptor);
        enc.setValues(values);
        enc.setValue(layerIndex, ilay);
        enc.setValue(zIndex, iz);
        enc.setValue(phiIndex, iphi);
	enc.setValue(barrelIndex,0);
	enc.setValue(systemIndex, detector.getSystemID());
        long resultID = enc.getID();

        return resultID;
    }

    public int getPhiBin(int ilay, double phi)
    {
        // phic is phi at center of cells with iphi=0
        double deltaPhi = this.computeDeltaPhiForLayer(ilay);
        double phic = deltaPhi / 2;
        int iphi = (int) Math.floor((phi - phic) / deltaPhi + 0.5);
        return iphi;
    }

    public int getZBin(double z)
    {
        // zc is z at center of cells with iz=0
        int numz = (int) Math.floor((getZMax() - getZMin()) / gridSizeZ);
        // double zc = 0; // for odd numz
        // if( numz%2 == 0 ) zc = gridSizeZ / 2;
        double zc = gridSizeZ / 2;
        int iz = (int) Math.floor((z - zc) / gridSizeZ + 0.5);
        return iz;
    }

    /**
     * Returns cylindrical radius to center of sensitive slice of any layer
     *
     * @param layer
     *            layer index
     */
    private double getRadiusSensitiveMid(int ilay)
    {
        LayerStack stack = getLayering().getLayers();
        Layer layer = stack.getLayer(ilay);

        double preLayers = 0;
        if (ilay > 0)
            preLayers = stack.getSectionThickness(0, ilay - 1);

        return this.getRMin() + preLayers + layer.getThicknessToSensitiveMid();
    }
}
