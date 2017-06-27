package org.lcsim.geometry.segmentation;

import java.util.ArrayList;
import java.util.List;

import org.jdom.DataConversionException;
import org.jdom.Element;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IDetectorElementContainer;
import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.detector.identifier.ExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.detector.identifier.Identifier;
import org.lcsim.detector.solids.Trd;
import org.lcsim.geometry.util.IDDescriptor;
import org.lcsim.geometry.util.BaseIDDecoder;
import org.lcsim.geometry.util.IDEncoder;
import org.lcsim.geometry.subdetector.AbstractPolyhedraCalorimeter;

/**
 * XZ Cartesian grid segmentation.
 * 
 * @author cassell
 */
public class RegularNgonCartesianGridXZ extends CartesianGridXZ
{
    private double gridSizeX = 0;
    private double gridSizeZ = 0;
    private int nmodules = 12;

    private int xIndex = -1;
    private int zIndex = -1;
    private int moduleIndex = -1;
    private int barrelIndex = -1;

    private int nzbins = 0;
    private int zbinmax = 0;
    private int zbinmin = 0;
    private int[] nvalidx;
    private boolean[] borderCellIsDuplicate;

    private double xhl0 = 0.;
    private double xslope = 0.;

    private int ecsign = -1;
    
    private static final String fieldNames[] = {"x", "z"};

    public RegularNgonCartesianGridXZ(Element node) throws DataConversionException
    {
        super(node);

        if (node.getAttribute("gridSizeX") != null)
        {
            gridSizeX = node.getAttribute("gridSizeX").getDoubleValue();
            cellSizes.add(0, gridSizeX);
        }
        else
        {
            throw new RuntimeException("Missing gridSizeX parameter.");
        }

        if (node.getAttribute("gridSizeZ") != null)
        {
            gridSizeZ = node.getAttribute("gridSizeZ").getDoubleValue();
            cellSizes.add(1, gridSizeZ);
        }
        else
        {
            throw new RuntimeException("Missing gridSizeZ parameter.");
        }
    }
    
    public String[] getSegmentationFieldNames() {
        return fieldNames;
    }

    public long[] getNeighbourIDs(int layerRange, int xRange, int zRange)
    {
        if (this.getDecoder().getID() == 0)
            throw new RuntimeException("No current ID is set.");
        if (sensitiveSlices == null)
            initSensitiveSlices();
        IDEncoder gnEncoder = new IDEncoder(descriptor);
        BaseIDDecoder gnDecoder = new BaseIDDecoder(descriptor);
        gnEncoder.setValues(values);
        gnDecoder.setID(gnEncoder.getID());
        IDEncoder dupEncoder = new IDEncoder(descriptor);
        BaseIDDecoder dupDecoder = new BaseIDDecoder(descriptor);
        dupEncoder.setValues(values);
        dupDecoder.setID(dupEncoder.getID());

        // Get number of layers.
        int nlayers = this.getNumberOfLayers();

        // Initialize the border arrays the first time in
        if (nvalidx == null)
        {
            initializeBorders();
        }
        // Set values for current id.
        int currLayer = gnDecoder.getValue(layerIndex);
        int currX = gnDecoder.getValue(xIndex);
        int currZ = gnDecoder.getValue(zIndex);
        int currModule = gnDecoder.getValue(moduleIndex);
        // Create return array.
        List<Long> neighbors = new ArrayList<Long>();

        // Loop over Z range.
        for (int iz = -zRange; iz <= zRange; iz++)
        {
            // Compute y value.
            int neighborZ = currZ + iz;
            if (neighborZ >= zbinmin && neighborZ <= zbinmax)
            {
                gnEncoder.setValue(zIndex, neighborZ);
                int Zindex = neighborZ + nzbins / 2;
                // Loop over X range.
                for (int ix = -xRange; ix <= xRange; ix++)
                {
                    // Need to check border
                    int neighborX = currX + ix;
                    int neighborModule = currModule;
                    int dupX = neighborX;
                    int dupModule = neighborModule;
                    boolean dup = false;
                    if (neighborX < -nvalidx[Zindex] + 1)
                    {
                        if (neighborX == -nvalidx[Zindex])
                        {
                            if (borderCellIsDuplicate[Zindex])
                            {
                                dup = true;
                                dupX = -neighborX - 1;
                                dupModule = (nmodules + neighborModule - ecsign) % nmodules;
                            }
                        }
                        else
                        {
                            neighborModule = (nmodules + neighborModule - ecsign) % nmodules;
                            neighborX = 2 * nvalidx[Zindex] + neighborX;
                            if (borderCellIsDuplicate[Zindex])
                                neighborX--;
                        }
                    }
                    else if (neighborX > nvalidx[Zindex] - 2)
                    {
                        if (neighborX == nvalidx[Zindex] - 1)
                        {
                            if (borderCellIsDuplicate[Zindex])
                            {
                                dup = true;
                                dupX = -neighborX - 1;
                                dupModule = (nmodules + neighborModule + ecsign) % nmodules;
                            }
                        }
                        else
                        {
                            neighborModule = (nmodules + neighborModule + ecsign) % nmodules;
                            neighborX = -2 * nvalidx[Zindex] + neighborX;
                            if (borderCellIsDuplicate[Zindex])
                                neighborX++;
                        }
                    }
                    gnEncoder.setValue(xIndex, neighborX);
                    gnEncoder.setValue(moduleIndex, neighborModule);
                    // Loop over layer range.
                    for (int ilayer = -layerRange; ilayer <= layerRange; ilayer++)
                    {
                        // Compute layer value.
                        int neighborLayer = currLayer + ilayer;

                        if (neighborLayer >= 0 && neighborLayer < nlayers)
                        {

                            // Set the neighbor fields.
                            gnEncoder.setValue(layerIndex, neighborLayer);
                            // Add the neighbor id to the return array.
                            if (sliceIndex >= 0)
                            {
                                // Loop over sensitive slices
                                for (int is = 0; is < sensitiveSlices[neighborLayer].size(); is++)
                                {
                                    // Set the slic field.
                                    gnEncoder.setValue(sliceIndex, ((Integer) (sensitiveSlices[neighborLayer].get(is)))
                                            .intValue());

                                    if (this.getDecoder().getID() != gnEncoder.getID())
                                        neighbors.add(gnEncoder.getID());
                                    // If we have a duplicate cell, add it
                                    if (dup)
                                    {
                                        dupEncoder.setValue(zIndex, neighborZ);
                                        dupEncoder.setValue(xIndex, dupX);
                                        dupEncoder.setValue(layerIndex, neighborLayer);
                                        dupEncoder.setValue(moduleIndex, dupModule);
                                        dupEncoder.setValue(sliceIndex, ((Integer) (sensitiveSlices[neighborLayer]
                                                .get(is))).intValue());
                                        neighbors.add(dupEncoder.getID());
                                    }
                                }
                            }
                            else
                            {
                                if (this.getDecoder().getID() != gnEncoder.getID())
                                    neighbors.add(gnEncoder.getID());
                                // If we have a duplicate cell, add it
                                if (dup)
                                {
                                    dupEncoder.setValue(zIndex, neighborZ);
                                    dupEncoder.setValue(xIndex, dupX);
                                    dupEncoder.setValue(layerIndex, neighborLayer);
                                    dupEncoder.setValue(moduleIndex, dupModule);
                                    neighbors.add(dupEncoder.getID());
                                }

                            }
                        }
                    }
                }
            }
        }

        long result[] = new long[neighbors.size()];
        int i = 0;
        for (Long id : neighbors)
        {
            result[i] = id;
            i++;
        }
        return result;
    }

    protected void initializeBorders()
    {
        nmodules = ((AbstractPolyhedraCalorimeter) getSubdetector()).getNumberOfSides();
        IIdentifierHelper helper = detector.getDetectorElement().getIdentifierHelper();
        // Current packed id.
        IIdentifier currId = new Identifier(this.getDecoder().getID());
        // Create an ExpandedIdentifier for the current id.
        IExpandedIdentifier thisId = helper.unpack(currId);
        IIdentifier id = new Identifier(this.getDecoder().getID());
        ExpandedIdentifier testId = new ExpandedIdentifier(thisId);
        int barrelVal = helper.getValue(id, barrelIndex);
        ecsign = 1;
        if (barrelVal == 2)
            ecsign = -1;
        testId.setValue(xIndex, 0);
        IIdentifier geomId = makeGeometryIdentifier(helper.pack(testId).getValue());
        IDetectorElementContainer deSrch = getSubdetector().getDetectorElement().findDetectorElement(geomId);
        IDetectorElement de = deSrch.get(0);
        if (de.getGeometry().getLogicalVolume().getSolid() instanceof Trd)
        {
            Trd sensorTrd = (Trd) de.getGeometry().getLogicalVolume().getSolid();
            double zhl = sensorTrd.getZHalfLength();
            double xhl1 = sensorTrd.getXHalfLength1();
            double xhl2 = sensorTrd.getXHalfLength2();
            xhl0 = (xhl1 + xhl2) / 2.;
            xslope = (xhl2 - xhl0) * gridSizeZ / zhl;
            nzbins = 2 * ((int) (zhl / gridSizeZ) + 1);
            zbinmax = nzbins / 2 - 1;
            zbinmin = -nzbins / 2;
            nvalidx = new int[nzbins];
            borderCellIsDuplicate = new boolean[nzbins];
            for (int zi = 0; zi < nzbins; zi++)
            {
                int zbin = zi - nzbins / 2;
                double xhl = xhl0 + xslope * (zbin + .5);
                nvalidx[zi] = (int) (xhl / gridSizeX) + 1;
                double bcw = (nvalidx[zi] * gridSizeX - xhl) / gridSizeX;
                borderCellIsDuplicate[zi] = false;
                if (bcw < .5)
                    borderCellIsDuplicate[zi] = true;
            }
        }
        else
        {
            throw new RuntimeException("Don't know how to bounds check solid " + de.getGeometry().getLogicalVolume()
                    .getSolid().getName() + ".");
        }
    }

    protected void setupGeomFields(IDDescriptor id)
    {
        super.setupGeomFields(id);
        xIndex = id.indexOf("x");
        zIndex = id.indexOf("z");
        moduleIndex = id.indexOf("module");
        layerIndex = id.indexOf("layer");
        barrelIndex = id.indexOf("barrel");

    }

}