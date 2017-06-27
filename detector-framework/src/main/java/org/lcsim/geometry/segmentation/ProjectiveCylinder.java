package org.lcsim.geometry.segmentation;

import org.jdom.DataConversionException;
import org.jdom.Element;
import org.lcsim.geometry.util.IDDescriptor;
import org.lcsim.geometry.util.IDEncoder;

/**
 * This segmentation represents a projective cylinder.  It is most 
 * appropriately used in a cylindrical calorimeter barrel.
 * 
 * @author Tony Johnson <tonyj@slac.stanford.edu>
 */
public class ProjectiveCylinder extends BarrelCylinderSegmentationBase
{
    private int thetaBins;
    private int phiBins;

    private int thetaIndex;
    private int phiIndex;
    private int barrelIndex = -1;
    private int systemIndex = -1;
    
    private static final String fieldNames[] = {"phi", "theta"};

    ProjectiveCylinder(Element node) throws DataConversionException
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
        return getTheta();
    }
    
    public double getCellSizeV()
    {
        return getPhi();
    }

    /**
     * FIXME Cache this value in ctor.
     */
    public double getPhi()
    {
        return Math.PI * 2 * (getValue(phiIndex) + 0.5) / phiBins;
    }

    /**
     * FIXME Cache this value in ctor.
     */
    public double getTheta()
    {
        return Math.PI * (getValue(thetaIndex) + 0.5) / thetaBins;
    }

    public double getX()
    {
        return getDistanceToSensitive(getLayer()) * Math.cos(getPhi());
    }

    public double getY()
    {
        return getDistanceToSensitive(getLayer()) * Math.sin(getPhi());
    }

    public double getZ()
    {
        double cosTheta = Math.cos(getTheta());
        return getDistanceToSensitive(getLayer()) * cosTheta / Math.sqrt(1 - cosTheta * cosTheta);
    }

    public void setIDDescription(IDDescriptor id)
    {
        super.setIDDescription(id);

        phiIndex = id.indexOf("phi");
        thetaIndex = id.indexOf("theta");
        barrelIndex = id.indexOf("barrel");
        systemIndex = id.indexOf("system");
    }

    public boolean supportsNeighbours()
    {
        return true;
    }

    public long[] getNeighbourIDs(int deltaLayer, int deltaTheta, int deltaPhi)
    {
        IDEncoder encoder = new IDEncoder(descriptor);
        encoder.setValues(values);

        int nMax = (2*deltaLayer + 1)*(2*deltaTheta + 1)*(2*deltaPhi + 1) - 1;
        int size = 0;
        long[] result = new long[nMax];
        for (int i = -deltaLayer; i <= deltaLayer; i++)
        {
            int l = values[layerIndex] + i;

            if (l<0 || l>= getNumberOfLayers() ) continue;
            encoder.setValue(layerIndex,l);

            for (int j = -deltaTheta; j <= deltaTheta; j++)
            {
                int t = values[thetaIndex] + j;

                if (t<0 || t>=thetaBins) continue;
                encoder.setValue(thetaIndex,t);

                for (int k = -deltaPhi; k <= deltaPhi; k++)
                {
                    if (i==0 && j==0 && k==0) continue;

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

    public int getThetaBins()
    {
        return thetaBins;
    }

    public int getPhiBins()
    {
        return phiBins;
    }

    /**
     * Return the cell which contains a given point (x,y,z), or zero.
     *
     * @param x Cartesian X coordinate.
     * @param y Cartesian Y coordinate.
     * @param z Cartesian Z coordinate.     
     * 
     * @return ID of cell containing the point (maybe either in absorber
     *         or live material)
     */
    public long findCellContainingXYZ(double x, double y, double z)
    {

        // validate point
        if(z<getZMin()) return 0;
        if(z>getZMax()) return 0;
        double rho = Math.sqrt(x*x+y*y);
        if(rho<getRMin()) return 0;
        if(rho>getRMax()) return 0;

        // ok, point is valid, so a valid ID should be returned
        int ilay = getLayerBin(rho);

        double phi = Math.atan2(y, x);
        if (phi < 0)
            phi += 2 * Math.PI;
        int iphi = (int) (phi * phiBins / (2 * Math.PI));

        double theta = Math.atan2(rho, z);
        int itheta = (int) (theta * thetaBins / Math.PI);

        IDEncoder enc = new IDEncoder(descriptor);
        enc.setValue(layerIndex, ilay);
        enc.setValue(thetaIndex, itheta);
        enc.setValue(phiIndex, iphi);
        enc.setValue(barrelIndex, 0);
        enc.setValue(systemIndex, detector.getSystemID());
        long resultID = enc.getID();

        return resultID;
    }
}
