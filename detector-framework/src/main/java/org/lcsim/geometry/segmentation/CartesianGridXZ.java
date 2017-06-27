package org.lcsim.geometry.segmentation;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import java.util.ArrayList;

import org.jdom.DataConversionException;
import org.jdom.Element;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IDetectorElementContainer;
import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.detector.identifier.Identifier;
import org.lcsim.detector.solids.Inside;
import org.lcsim.detector.solids.Trd;
import org.lcsim.geometry.util.IDDescriptor;

/**
 * XZ Cartesian grid segmentation. 
 * 
 * @author jeremym
 */
public class CartesianGridXZ extends AbstractCartesianGrid
{	
	private int xIndex = -1;
	private int zIndex = -1;
	private static final String[] fieldNames = {"x", "z"};
	
	public CartesianGridXZ(Element node) throws DataConversionException
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
		return getNeighbourIDs(layerRange, xRange, zRange, xIndex, zIndex);	
	}
	 	
	protected void setSegmentationValues(IExpandedIdentifier geomId, Hep3Vector localPositionVec)
	{
		geomId.setValue(xIndex, getXBin(localPositionVec.x()));
		geomId.setValue(zIndex, getZBin(localPositionVec.z()));
	}

	public int getXBin(double x)
	{
		return getBin(x, gridSizeX);
	}

	public int getZBin(double z)
	{
		return getBin(z, gridSizeZ);
	}
			
	public boolean boundsCheck(long rawId)
	{
		IIdentifier geomId = makeGeometryIdentifier(rawId);
		IIdentifierHelper helper = getSubdetector().getDetectorElement().getIdentifierHelper();
		IIdentifier id = new Identifier(rawId);
		int xVal = helper.getValue(id, xIndex);
		int zVal = helper.getValue(id, zIndex);
		IDetectorElementContainer deSrch = getSubdetector().getDetectorElement().findDetectorElement(geomId);
		if (deSrch == null || deSrch.size() == 0)
		{
			return false;
		}
		IDetectorElement de = deSrch.get(0);
		double xPos = computeCoordinate(xVal, gridSizeX);
		double zPos = computeCoordinate(zVal, gridSizeZ);
		if (de.getGeometry().getLogicalVolume().getSolid() instanceof Trd)
		{
			Trd sensorTrd = (Trd)de.getGeometry().getLogicalVolume().getSolid();
			// Check coordinate values against trd bounds.
			if (sensorTrd.inside(new BasicHep3Vector(xPos, 0, zPos)) == Inside.INSIDE)
			{
				return true;
			}
			// TODO: Handle edge case with cells that have centers slightly outside readout volume.
			else
			{
				return false;
			}
		}
		else
		{
			throw new RuntimeException("Don't know how to bounds check solid " + de.getGeometry().getLogicalVolume().getSolid().getName() + ".");
		}
	}			
	
	protected void setupGeomFields(IDDescriptor id)
	{
	    if (geomFields == null)
	    {
	        geomFields = new ArrayList<Integer>();

	        xIndex = id.indexOf("x");
	        zIndex = id.indexOf("z");
	        layerIndex = id.indexOf("layer");
	        try {
	            sliceIndex = id.indexOf("slice");
	        }
	        catch (IllegalArgumentException x)
	        {
	            System.err.println("WARNING: The slice field does not exist in this IDDecoder!");
	            sliceIndex = -1;
	        }


	        // Set geometry field list.
	        for (int i=0; i < id.fieldCount(); i++)
	        {
	            String fname = id.fieldName(i);
	            if (!fname.equals("x") && !fname.equals("z"))
	            {
	                geomFields.add(i);
	            }
	        }
	    }
	}
		
	private void computeLocalX()
	{	
		localPosition[0] = (((double) getValue(xIndex)) + 0.5) * gridSizeX;	
	}

	private void computeLocalZ()
	{
		localPosition[2] = (((double) getValue(zIndex)) + 0.5) * gridSizeZ;
	}
	
	protected void computeLocalPosition()
	{
		localPosition[0] = localPosition[1] = localPosition[2] = 0.0;
		computeLocalX();
		computeLocalZ();
	}	
}