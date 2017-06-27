package org.lcsim.geometry.segmentation;

import java.util.ArrayList;
import java.util.List;

import org.jdom.DataConversionException;
import org.jdom.Element;
import org.lcsim.detector.identifier.ExpandedIdentifier;
import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.detector.identifier.Identifier;
import org.lcsim.geometry.subdetector.AbstractPolyhedraCalorimeter;
import org.lcsim.geometry.util.BaseIDDecoder;
import org.lcsim.geometry.util.IDDescriptor;
import org.lcsim.geometry.util.IDEncoder;

/**
 * Extend XY Cartesian grid segmentation for neighboring across
 * module boundaries.
 * 
 * @author cassell
 */
public class RegularNgonCartesianGridXY extends CartesianGridXY
{	
	private double gridSizeX = 0;
	private double gridSizeY = 0;
    private int nmodules = 12;

	private int xIndex = -1;
	private int yIndex = -1;
	private int moduleIndex = -1;

    private int[] nvalidx;
    private boolean[] borderCellIsDuplicate;
    
    private static final String fieldNames[] = {"x", "y"};

	public RegularNgonCartesianGridXY(Element node) throws DataConversionException
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
                if(sensitiveSlices == null)initSensitiveSlices();
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

	// Set values for current id.
	int currLayer = values[layerIndex];
	int currX = gnDecoder.getValue(xIndex);
	int currY = gnDecoder.getValue(yIndex);
        int currModule = gnDecoder.getValue(moduleIndex);

        // Initialize the border arrays the first time in
        if (nvalidx == null)
        {
            initializeBorders();
        }
	// Create return array.
	List<Long> neighbors = new ArrayList<Long>();

	// Loop over layer range.
	for (int ilayer=-layerRange; ilayer<=layerRange; ilayer++)
	{
            // Compute layer value.
            int neighborLayer = currLayer + ilayer;

            if (neighborLayer >= 0 && neighborLayer < nlayers)
            {
                gnEncoder.setValue(layerIndex, neighborLayer);
                // Loop over X range.
                for (int ix=-xRange; ix<=xRange; ix++)
                {
                    // Need to check border
                    int neighborX = currX + ix;
                    int neighborModule = currModule;
                    int dupX = neighborX;
                    int dupModule = neighborModule;
                    boolean dup = false;
                    if(neighborX < -nvalidx[neighborLayer]+1)
                    {
                        if(neighborX == -nvalidx[neighborLayer])
                        {
                            if(borderCellIsDuplicate[neighborLayer])
                            {
                                dup = true;
                                dupX = -neighborX -1;
                                dupModule = (nmodules + neighborModule -1)%nmodules;
                            }
                        }
                        else
                        {
                            neighborModule = (nmodules + neighborModule -1)%nmodules;
                            neighborX = 2*nvalidx[neighborLayer] + neighborX;
                            if(borderCellIsDuplicate[neighborLayer])neighborX--;
                        }
                    }
                    else if(neighborX > nvalidx[neighborLayer]-2)
                    {
                        if(neighborX == nvalidx[neighborLayer]-1)
                        {
                            if(borderCellIsDuplicate[neighborLayer])
                            {
                                dup = true;
                                dupX = -neighborX -1;
                                dupModule = (neighborModule +1)%nmodules;
                            }
                        }
                        else
                        {
                            neighborModule = (neighborModule +1)%nmodules;
                            neighborX = -2*nvalidx[neighborLayer] + neighborX;
                            if(borderCellIsDuplicate[neighborLayer])neighborX++;
                        }
                    }
                    gnEncoder.setValue(xIndex, neighborX);
                    gnEncoder.setValue(moduleIndex, neighborModule);
                    // Loop over Y range.
                    for (int iy=-yRange; iy<=yRange; iy++)
                    {
                        // Compute y value.
			int neighborY = currY + iy;
			// Set the neighbor fields.
			gnEncoder.setValue(yIndex, neighborY);
			// Add the neighbor id to the return array.
                        if(sliceIndex >= 0)
                        {
                            // Loop over sensitive slices
                            for(int is = 0;is < sensitiveSlices[neighborLayer].size();is++)
                            {
                                // Set the slic field.
                                gnEncoder.setValue(sliceIndex, ((Integer) (sensitiveSlices[neighborLayer].get(is))).intValue());

                                if( this.getDecoder().getID()!= gnEncoder.getID())neighbors.add(gnEncoder.getID());
                                // If we have a duplicate cell, add it
                                if(dup)
                                {
                                    dupEncoder.setValue(yIndex, neighborY);
                                    dupEncoder.setValue(xIndex, dupX);
                                    dupEncoder.setValue(layerIndex, neighborLayer);
                                    dupEncoder.setValue(moduleIndex, dupModule);
                                    dupEncoder.setValue(sliceIndex, ((Integer) (sensitiveSlices[neighborLayer].get(is))).intValue());
                                    neighbors.add(dupEncoder.getID());
                                }
                            }
                        }
                        else
                        {
                            if( this.getDecoder().getID()!= gnEncoder.getID())neighbors.add(gnEncoder.getID());
                            // If we have a duplicate cell, add it
                            if(dup)
                            {
                                dupEncoder.setValue(yIndex, neighborY);
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
        nmodules = ((AbstractPolyhedraCalorimeter)getSubdetector()).getNumberOfSides();
		IIdentifierHelper helper = detector.getDetectorElement().getIdentifierHelper();
        // Current packed id.
        IIdentifier currId = new Identifier(this.getDecoder().getID());
        // Create an ExpandedIdentifier for the current id.
        IExpandedIdentifier thisId = helper.unpack(currId);
        // nvalidx is the number of valid xbins in the elements half
        // length per layer.
        nvalidx = new int[this.getNumberOfLayers()];
        // borderCellIsDuplicate - true if we treat the partial cell\
        // on each side of the boundary as a single cell
        borderCellIsDuplicate = new boolean[this.getNumberOfLayers()];
        ExpandedIdentifier testId = new ExpandedIdentifier(thisId);
        testId.setValue(moduleIndex,0);
        long save = this.getDecoder().getID();
        for(int layer=0;layer<this.getNumberOfLayers();layer++)
        {
			testId.setValue(layerIndex, layer);
            this.getDecoder().setID(helper.pack(testId).getValue());
            this.computeGlobalPosition();
            double yc = this.globalPosition[1];
            double xhl = yc*Math.tan(Math.PI/nmodules);
            nvalidx[layer] = (int)(xhl/gridSizeX) + 1;
            double bcw = (nvalidx[layer]*gridSizeX - xhl)/gridSizeX;
            borderCellIsDuplicate[layer] = false;
            if(bcw < .5)borderCellIsDuplicate[layer] = true;
        }
// Put back the cellID
        this.getDecoder().setID(save);
        this.computeGlobalPosition();
    }
	public void setupGeomFields(IDDescriptor id)
	{
        super.setupGeomFields(id);
        xIndex = id.indexOf("x");
		yIndex = id.indexOf("y");
		layerIndex = id.indexOf("layer");
		moduleIndex = id.indexOf("module");
	}
		
}