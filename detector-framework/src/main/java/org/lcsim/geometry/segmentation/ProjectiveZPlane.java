package org.lcsim.geometry.segmentation;

import static java.lang.Math.PI;
import static java.lang.Math.tan;

import org.jdom.DataConversionException;
import org.jdom.Element;
import org.lcsim.geometry.layer.Layer;
import org.lcsim.geometry.layer.LayerStack;
import org.lcsim.geometry.util.IDDescriptor;
import org.lcsim.geometry.util.IDEncoder;

/**
 * This segmentation represents a projective Z plane.  It is 
 * most appropriately used for cylindrical endcap calorimeter components.
 * 
 * @author Tony Johnson <tonyj@slac.stanford.edu>
 */
public class ProjectiveZPlane extends SegmentationBase
{
    private int thetaBins;
    private int phiBins;

    private int thetaIndex;
    private int phiIndex;
    private int systemIndex = -1;
    private int barrelIndex = -1;

    private double thetaDim = 0;
    private double phiDim = 0;
    
    private static final String fieldNames[] = {"phi", "theta"};
    
    ProjectiveZPlane(Element node) throws DataConversionException
    {
        super(node);
        thetaBins = node.getAttribute("thetaBins").getIntValue();
        phiBins = node.getAttribute("phiBins").getIntValue();
    }
    
    public String[] getSegmentationFieldNames() {
        return fieldNames;
    }
    
    public double getCellSizeU()
    {
        return thetaDim;
    }
    
    public double getCellSizeV()
    {
        return phiDim;
    }

    public int getThetaBins()
    {
        return thetaBins;
    }

    public int getPhiBins()
    {
        return phiBins;
    }

    public double getPhi()
    {
        double phi = 2 * PI * ((getValue(phiIndex) + 0.5) / phiBins);
        return phi;
    }

    public double getTheta()
    {
        return PI * ((getValue(thetaIndex) + 0.5) / thetaBins);
    }

    public double getX()
    {
        return getSphericalRadius() * Math.cos(getPhi());
    }

    public double getY()
    {
        return getSphericalRadius() * Math.sin(getPhi());
    }

    public double getZ()
    {
        return -Math.signum(getTheta() - PI / 2) * getDistanceToSensitive(getLayer());
    }

    private double getSphericalRadius()
    {
        return getZ() * tan(getTheta());
    }

    public void setIDDescription(IDDescriptor id)
    {
        super.setIDDescription(id);

        phiIndex = id.indexOf("phi");
        thetaIndex = id.indexOf("theta");
        barrelIndex = id.indexOf("barrel");
        systemIndex = id.indexOf("system");
    }

    public long[] getNeighbourIDs(int deltaLayer, int deltaTheta, int deltaPhi)
    {
        IDEncoder encoder = new IDEncoder(descriptor);
        encoder.setValues(values);

        int nMax = (2 * deltaLayer + 1) * (2 * deltaTheta + 1) * (2 * deltaPhi + 1) - 1;
        int size = 0;
        long[] result = new long[nMax];
        for (int i = -deltaLayer; i <= deltaLayer; i++)
        {
            int l = values[layerIndex] + i;

            if (l < 0 || l >= getNumberOfLayers())
                continue;
            encoder.setValue(layerIndex, l);

            for (int j = -deltaTheta; j <= deltaTheta; j++)
            {
                int t = values[thetaIndex] + j;

                if (t < 0 || t >= thetaBins)
                    continue;
                encoder.setValue(thetaIndex, t);

                for (int k = -deltaPhi; k <= deltaPhi; k++)
                {
                    if (i == 0 && j == 0 && k == 0)
                        continue;

                    int p = values[phiIndex] + k;
                    if(p<0) p+=phiBins;
                    if(p>=phiBins) p-=phiBins;

                    result[size++] = encoder.setValue(phiIndex, p);
                }
            }
        }
        if (size < result.length)
        {
            long[] temp = new long[size];
            System.arraycopy(result, 0, temp, 0, size);
            result = temp;
        }
        return result;
    }

    public boolean supportsNeighbours()
    {
        return true;
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
        if(Math.abs(z) < getZMin()) return 0;
        if(Math.abs(z) > getZMax()) return 0;
        double rho = Math.sqrt(x * x + y * y);
        if(rho < getRMin()) return 0;
        if(rho > getRMax()) return 0;

        // ok, point is valid, so a valid ID should be returned
        int ilay = getLayerBin(z);

        double phi = Math.atan2(y, x);
        if(phi < 0) phi += 2 * PI;
        int iphi = (int) ((phi * phiBins)/(2*PI) - 0.5);

        double theta = Math.atan2(rho, z);
        if(theta < 0)
        {
            // If never prints out, the whole if can be dropped
            System.out.println("ProjCylinder: Is this really needed?!?");
            theta += 2. * PI;
        }
        int itheta = (int) ((theta * thetaBins)/PI - 0.5);
	int ibar = ( z<0 ? 2 : 1 );
	int system = detector.getSystemID();

        IDEncoder enc = new IDEncoder(descriptor);
        enc.setValue(layerIndex, ilay);
        enc.setValue(thetaIndex, itheta);
        enc.setValue(phiIndex, iphi);
	enc.setValue(barrelIndex, ibar);
	enc.setValue(systemIndex, system);
        long resultID = enc.getID();

        return resultID;
    }

    /**
     * Return the layer number based on the z-coordinate
     *
     * @param z
     *            z-coordinate
     * @return layer number of layer corresponding to that distance (may be
     *         either in absorber or live material)
     * @throws RuntimeException
     *             if abs(z)<zMin
     */
    public int getLayerBin(double z)
    {
        // In order to be general, we should not assume that all
        // layers have the same thickness. Therefore, one has to
        // guess the starting layer (based on average thickness), and
        // then navigate through layers until one finds the right one
        double depth = Math.abs(z) - getZMin();
        if (depth < 0)
            throw new RuntimeException("ProjectiveZPlane: Error: z < zMin, z=" + z + ", zMin=" + this.getZMin());

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
}
