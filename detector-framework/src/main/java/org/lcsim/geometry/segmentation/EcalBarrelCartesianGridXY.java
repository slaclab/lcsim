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
import org.lcsim.detector.solids.Box;
import org.lcsim.geometry.util.IDDescriptor;
import org.lcsim.geometry.util.BaseIDDecoder;
import org.lcsim.geometry.util.IDEncoder;
import org.lcsim.geometry.subdetector.AbstractPolyhedraCalorimeter;

/**
 * XY Cartesian grid segmentation for overlapping staves.
 * 
 * @author cassell
 */
public class EcalBarrelCartesianGridXY extends CartesianGridXY
{
    private double gridSizeX = 0;
    private double gridSizeY = 0;

    private int xIndex = -1;
    private int yIndex = -1;
    private int moduleIndex = -1;
    private int nmodules = 12;

    private int[] validXplusG;
    private int[] validXminusG;
    private int[] validXplusP;
    private int[] validXminusP;
    private int validZplus;
    private int validZminus;

    private double xc0 = 0.;
    private double[] yc;

    private double sinth = 0.;
    private double costh = 0.;
    private double tanth = 0.;
    private double cotth = 0.;
    private double secth = 0.;
    private double cscth = 0.;

    private int nlayers = 0;
    
    private static final String[] fieldNames = {"x", "y"};

    public EcalBarrelCartesianGridXY(Element node) throws DataConversionException
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

        if (node.getAttribute("gridSizeY") != null)
        {
            gridSizeY = node.getAttribute("gridSizeY").getDoubleValue();
            cellSizes.add(1, gridSizeY);
        }
        else
        {
            throw new RuntimeException("Missing gridSizeY parameter.");
        }
    }
    
    public String[] getSegmentationFieldNames() {
        return fieldNames;
    }

    public long[] getNeighbourIDs(int layerRange, int xRange, int yRange)
    {
        if (this.getDecoder().getID() == 0)
            throw new RuntimeException("No current ID is set.");
        if (sensitiveSlices == null)
            initSensitiveSlices();
        IDEncoder gnEncoder = new IDEncoder(descriptor);
        BaseIDDecoder gnDecoder = new BaseIDDecoder(descriptor);
        gnEncoder.setValues(values);
        gnDecoder.setID(gnEncoder.getID());

        // Check if we need initialization
        if (validXplusP == null)
        {
            initializeMappings();
        }
        // Set values for current id.
        int currLayer = gnDecoder.getValue(layerIndex);
        int currX = gnDecoder.getValue(xIndex);
        int currY = gnDecoder.getValue(yIndex);
        int currModule = gnDecoder.getValue(moduleIndex);

        // Find the neighbors within the current module
        List<Long> nl = getNeighbourIDs(layerRange, xRange, yRange, gnEncoder, currLayer, currX, currY);
        // Check for border problems
        boolean bp1 = false;
        boolean bp2 = false;
        // If the layer range includes layer 0 or negative, and the
        // X bin exceeds the maximum X bin in a "pseudo" geometry
        // then there is possible neighbors in module +1
        if (((currLayer - layerRange) < 1) && ((currX + xRange) > validXplusP[0]))
            bp1 = true;
        // If the X bin can be less than the minimum valid X bin for
        // the minimum layer then there is possible neighbors in module
        // -1
        else if ((currX - xRange) <= validXminusG[Math.min(currLayer + layerRange, nlayers - 1)])
            bp2 = true;
        // otherwise no border problem and just return the neighbors in
        // the current module.
        else
        {
            long result[] = new long[nl.size()];
            int i = 0;
            for (Long id : nl)
            {
                result[i] = id;
                i++;
            }
            return result;
        }
        if (bp1)
        {
            // Layer range includes l<=0, xbin large enough to overlap
            // with module m+1. Neighbors in module m already added.
            // Now find the neighbors in module m+1. The neighbor
            // volume is a rectangle, so find cells is in module m+1
            // cotained by the rectangle.
            //
            // Convert the corners of the rectangle layer, xbin to
            // actual coordinates.
            double lsep = yc[1] - yc[0];
            // The maximum y coordinate is the layer 0 y coordinate +
            // half the layer separation.
            double ycmax = yc[0] + lsep / 2.;
            // The minimum y coordinate is the layer 0 y coordinate -
            // half the layer separation - the layer separation*# negative
            // layers.
            double ycmin = yc[0] - lsep / 2. + lsep * (currLayer - layerRange);
            // The maximum/minimum X coordinate is the maximum/minimum
            // Xbin+-.5 times the grid size, + x0
            double xcmax = xc0 + gridSizeX * (currX + xRange + .5);
            double xcmin = xc0 + gridSizeX * (currX - xRange - .5);
            // rotate the coordinate system to module +1, and find the layer
            // range in that module.
            double minyp = ycmin * costh + xcmin * sinth;
            double maxyp = ycmax * costh + xcmax * sinth;
            int minl = 0;
            int maxl = 0;
            for (int il = 0; il < nlayers; il++)
            {
                if (yc[il] < minyp)
                    minl++;
                if (yc[il] > maxyp)
                    break;
                maxl++;
            }
            // Set the module
            int neighborModule = (currModule + 1) % nmodules;
            gnEncoder.setValue(moduleIndex, neighborModule);
            // Loop over the layer range
            for (int neighborLayer = minl; neighborLayer < maxl; neighborLayer++)
            {
                gnEncoder.setValue(layerIndex, neighborLayer);
                // In this module's coordinate system, calculate the
                // intersection of y=yc(layer) with the box to find the
                // X coordinate range inside the box.
                double xpmax = Math
                        .min(xcmax * secth - yc[neighborLayer] * tanth, yc[neighborLayer] * cotth - ycmin * cscth);
                double xpmin = Math
                        .max(xcmin * secth - yc[neighborLayer] * tanth, yc[neighborLayer] * cotth - ycmax * cscth);
                // Convert X coordinate to X bin.
                int xbmin = (int) ((xpmin - xc0 + gridSizeX * .5) / gridSizeX);
                int xbmax = (int) ((xpmax - xc0 - gridSizeX * .5) / gridSizeX);
                // Loop over X bins
                for (int neighborX = xbmin; neighborX <= xbmax; neighborX++)
                {
                    gnEncoder.setValue(xIndex, neighborX);
                    // Loop over the Z range
                    for (int iy = -yRange; iy <= yRange; iy++)
                    {
                        // Compute y value.
                        int neighborY = currY + iy;
                        gnEncoder.setValue(yIndex, neighborY);
                        if (sliceIndex >= 0)
                        {
                            // Loop over sensitive slices
                            for (int is = 0; is < sensitiveSlices[neighborLayer].size(); is++)
                            {
                                // Set the slic field.
                                gnEncoder.setValue(sliceIndex, ((Integer) (sensitiveSlices[neighborLayer].get(is)))
                                        .intValue());
                                ;
                                // Add the neighbor
                                nl.add(gnEncoder.getID());
                            }
                        }
                        else
                            nl.add(gnEncoder.getID());

                    }
                }
            }
            long result[] = new long[nl.size()];
            int i = 0;
            for (Long id : nl)
            {
                result[i] = id;
                i++;
            }
            return result;
        }
        // To get here we have overlap with module m-1. Add the
        // neighbors in module m-1.
        int neighborModule = (nmodules + currModule - 1) % nmodules;
        gnEncoder.setValue(moduleIndex, neighborModule);
        double ycmax = 0.;
        double ycmin = 0.;
        // The layer separation depends on where we are
        if (currLayer - layerRange < 1)
        {
            double lsep = yc[1] - yc[0];
            ycmin = yc[0] + (currLayer - layerRange) * lsep - lsep / 2.;
        }
        else
        {
            double lsep = yc[currLayer - layerRange] - yc[currLayer - layerRange - 1];
            ycmin = yc[currLayer - layerRange] - lsep / 2.;
        }
        if (currLayer + layerRange > nlayers - 2)
        {
            double lsep = yc[nlayers - 1] - yc[nlayers - 2];
            ycmax = yc[nlayers - 1] + lsep / 2.;
        }
        else
        {
            double lsep = yc[currLayer + layerRange + 1] - yc[currLayer + layerRange];
            ycmax = yc[currLayer + layerRange] + lsep / 2.;
        }
        // Find the coordinates of the box in module m-1
        double xcmax = xc0 + gridSizeX * (currX + xRange + .5);
        double xcmin = xc0 + gridSizeX * (currX - xRange - .5);
        double minyp = ycmin * costh - xcmax * sinth;
        double maxyp = ycmax * costh - xcmin * sinth;
        int minl = 0;
        int maxl = 0;
        for (int il = 0; il < nlayers; il++)
        {
            if (yc[il] < minyp)
                minl++;
            if (yc[il] > maxyp)
                break;
            maxl++;
        }
        // Loop over layer range
        for (int neighborLayer = minl; neighborLayer < maxl; neighborLayer++)
        {
            gnEncoder.setValue(layerIndex, neighborLayer);
            // Find intersection of y=yc[layer] with box to determine
            // X range
            double xpmax = Math
                    .min(xcmax * secth + yc[neighborLayer] * tanth, -yc[neighborLayer] * cotth + ycmax * cscth);
            double xpmin = Math
                    .max(xcmin * secth + yc[neighborLayer] * tanth, -yc[neighborLayer] * cotth + ycmin * cscth);
            int xbmin = (int) ((xpmin - xc0 + gridSizeX * .5) / gridSizeX);
            int xbmax = (int) ((xpmax - xc0 - gridSizeX * .5) / gridSizeX);
            xbmax = Math.min(xbmax, validXplusG[neighborLayer]);
            // Loop and add neighbors
            for (int neighborX = xbmin; neighborX <= xbmax; neighborX++)
            {
                gnEncoder.setValue(xIndex, neighborX);
                for (int neighborY = Math.max(validZminus, currY - yRange); neighborY <= Math
                        .min(validZplus, currY + yRange); neighborY++)
                {
                    gnEncoder.setValue(yIndex, neighborY);
                    if (sliceIndex >= 0)
                    {
                        // Loop over sensitive slices
                        for (int is = 0; is < sensitiveSlices[neighborLayer].size(); is++)
                        {
                            // Set the slic field.
                            gnEncoder.setValue(sliceIndex, ((Integer) (sensitiveSlices[neighborLayer].get(is)))
                                    .intValue());
                            ;
                            nl.add(gnEncoder.getID());
                        }
                    }
                    else
                        nl.add(gnEncoder.getID());
                }
            }
        }
        long result[] = new long[nl.size()];
        int i = 0;
        for (Long id : nl)
        {
            result[i] = id;
            i++;
        }
        return result;
    }

    protected List<Long> getNeighbourIDs(int layerRange, int xRange, int yRange, IDEncoder gnEncoder, int currLayer, int currX, int currY)
    {
        // Find neighbors within the currect module
        List<Long> rl = new ArrayList<Long>();
        for (int neighborLayer = Math.max(0, currLayer - layerRange); neighborLayer <= Math
                .min(nlayers - 1, currLayer + layerRange); neighborLayer++)
        {
            gnEncoder.setValue(layerIndex, neighborLayer);
            for (int neighborX = Math.max(validXminusG[neighborLayer], currX - xRange); neighborX <= Math
                    .min(validXplusG[neighborLayer], currX + xRange); neighborX++)
            {
                gnEncoder.setValue(xIndex, neighborX);
                for (int neighborY = Math.max(validZminus, currY - yRange); neighborY <= Math
                        .min(validZplus, currY + yRange); neighborY++)
                {
                    gnEncoder.setValue(yIndex, neighborY);
                    if (sliceIndex >= 0)
                    {
                        // Loop over sensitive slices
                        for (int is = 0; is < sensitiveSlices[neighborLayer].size(); is++)
                        {
                            // Set the slic field.
                            gnEncoder.setValue(sliceIndex, ((Integer) (sensitiveSlices[neighborLayer].get(is)))
                                    .intValue());
                            ;
                            if (this.getDecoder().getID() != gnEncoder.getID())
                                rl.add(gnEncoder.getID());
                        }
                    }
                    else if (this.getDecoder().getID() != gnEncoder.getID())
                        rl.add(gnEncoder.getID());
                }
            }
        }
        return rl;
    }

    public void setupGeomFields(IDDescriptor id)
    {
        super.setupGeomFields(id);
        xIndex = id.indexOf("x");
        yIndex = id.indexOf("y");
        moduleIndex = id.indexOf("module");
        layerIndex = id.indexOf("layer");
    }

    protected void initializeMappings()
    {
        nmodules = ((AbstractPolyhedraCalorimeter) getSubdetector()).getNumberOfSides();
        // Initialize all the needed parameters for the neighbor
        // finding across borders
        // Get number of layers.
        nlayers = this.getNumberOfLayers();
        // The valid X bins vary by layer
        validXplusP = new int[this.getNumberOfLayers()];
        validXminusP = new int[this.getNumberOfLayers()];
        validXplusG = new int[this.getNumberOfLayers()];
        validXminusG = new int[this.getNumberOfLayers()];
        // Calculate the trig functions needed for the rotations
        sinth = Math.sin(2. * Math.PI / nmodules);
        costh = Math.cos(2. * Math.PI / nmodules);
        tanth = sinth / costh;
        cotth = costh / sinth;
        secth = 1. / costh;
        cscth = 1. / sinth;
        // Get the helper and identifiers needed to manipulate the id
        IIdentifierHelper helper = detector.getDetectorElement().getIdentifierHelper();
        IIdentifier currId = new Identifier(this.getDecoder().getID());
        IExpandedIdentifier thisId = helper.unpack(currId);
        ExpandedIdentifier pseudoId = new ExpandedIdentifier(thisId);
        // Set the module and xbin to 0 to find pseudo-lengths and offsets
        pseudoId.setValue(moduleIndex, 0);
        pseudoId.setValue(xIndex, 0);
        pseudoId.setValue(layerIndex, 0);
        pseudoId.setValue(sliceIndex, 0);
        // Save the current ID
        long save = this.getDecoder().getID();
        this.getDecoder().setID(helper.pack(pseudoId).getValue());
        this.computeGlobalPosition();
        // xc0 is the global x coordinate or the center of the module
        xc0 = this.globalPosition[0] - gridSizeX / 2.;
        // yc is the global y coordinate of each layer
        yc = new double[nlayers];
        // xhlp is the half length of each layer
        double[] xhlp = new double[nlayers];
        for (int i = 0; i < nlayers; i++)
        {
            // Find the half lengths per layer and convert to x index.
            pseudoId.setValue(layerIndex, i);
            if (i == 1)
                pseudoId.setValue(sliceIndex, 2);
            this.getDecoder().setID(helper.pack(pseudoId).getValue());
            this.computeGlobalPosition();
            yc[i] = this.globalPosition[1];
            // Find the box containing this cell
            IIdentifier geomId = makeGeometryIdentifier(helper.pack(pseudoId).getValue());
            IDetectorElementContainer deSrch = getSubdetector().getDetectorElement().findDetectorElement(geomId);
            IDetectorElement de = deSrch.get(0);
            if (de.getGeometry().getLogicalVolume().getSolid() instanceof Box)
            {
                Box sensorBox = (Box) de.getGeometry().getLogicalVolume().getSolid();
                // Assume the Z extent is the same for all layers
                if (i == 0)
                {
                    double yhl = sensorBox.getYHalfLength();
                    // Convert half length to valid index
                    int nvalidy = (int) (yhl / gridSizeY) + 1;
                    validZplus = nvalidy - 1;
                    validZminus = -nvalidy;
                }
                // Convert X half length to valid X bins
                double xhl = sensorBox.getXHalfLength();
                int nvalidx = (int) (xhl / gridSizeX) + 1;
                validXplusG[i] = nvalidx - 1;
                validXminusG[i] = -nvalidx;
                // Compute valid X indices contained in a pseudo
                // geometry of a regular N-gon
                xhlp[i] = yc[i] * Math.tan(Math.PI / nmodules);
                validXplusP[i] = (int) ((xhlp[i] - xc0) / gridSizeX);
                validXminusP[i] = -(int) ((xhlp[i] + xc0) / gridSizeX) - 1;
            }
            else
            {
                throw new RuntimeException("Don't know how to bounds check solid " + de.getGeometry()
                        .getLogicalVolume().getSolid().getName() + ".");
            }
        }
        // Put back the cellID
        this.getDecoder().setID(save);
        this.computeGlobalPosition();
    }

    public int getVLayer()
    {
        if (validXplusP == null)
        {
            initializeMappings();
        }
        // Get the helper and identifiers needed to manipulate the id
        IIdentifierHelper helper = detector.getDetectorElement().getIdentifierHelper();
        IIdentifier currId = new Identifier(this.getDecoder().getID());
        IExpandedIdentifier thisId = helper.unpack(currId);
        int xbin = thisId.getValue(xIndex);
        int layer = thisId.getValue(layerIndex);
        if (xbin > validXplusP[layer])
        {
            double xc = xc0 + gridSizeX * (xbin + .5);
            double yp = yc[layer] * costh + xc * sinth;
            double dely = yp - yc[layer];
            int vl = layer;
            for (int il = layer; il < nlayers - 1; il++)
            {
                if (dely < (yc[il + 1] - yc[il]) / 2.)
                    break;
                vl++;
                dely -= yc[il + 1] - yc[il];
            }
            return vl;
        }
        return layer;
    }
}
