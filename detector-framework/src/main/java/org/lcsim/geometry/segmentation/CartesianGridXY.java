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
import org.lcsim.detector.solids.Box;
import org.lcsim.detector.solids.Inside;
import org.lcsim.geometry.util.IDDescriptor;

/**
 * This segmentation represents an XY Cartesian grid.  It can be used 
 * to segment staves in a calorimeter that have box shaped layers. 
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: CartesianGridXY.java,v 1.13 2013/01/24 22:24:20 jeremy Exp $
 */
public class CartesianGridXY extends AbstractCartesianGrid
{
    private int xIndex = -1;
    private int yIndex = -1;
    
    private static final String fieldNames[] = {"x", "y"};

    public CartesianGridXY(Element node) throws DataConversionException
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

    public double getGridSizeX()
    {
        return gridSizeX;
    }

    public double getGridSizeY()
    {
        return gridSizeY;
    }

    public long[] getNeighbourIDs(int layerRange, int xRange, int yRange)
    {
        return getNeighbourIDs(layerRange, xRange, yRange, xIndex, yIndex);
    }

    protected void setSegmentationValues(IExpandedIdentifier geomId, Hep3Vector localPositionVec)
    {
        geomId.setValue(xIndex, getXBin(localPositionVec.x()));
        geomId.setValue(yIndex, getYBin(localPositionVec.y()));
    }

    public int getXBin(double x)
    {
        return getBin(x, gridSizeX);
    }

    public int getYBin(double y)
    {
        return getBin(y, gridSizeY);
    }

    public boolean boundsCheck(long rawId)
    {
        IIdentifier geomId = makeGeometryIdentifier(rawId);
        IIdentifierHelper helper = getSubdetector().getDetectorElement().getIdentifierHelper();
        IIdentifier id = new Identifier(rawId);
        int xVal = helper.getValue(id, xIndex);
        int yVal = helper.getValue(id, yIndex);
        IDetectorElementContainer deSrch = getSubdetector().getDetectorElement().findDetectorElement(geomId);
        if (deSrch == null || deSrch.size() == 0)
        {
            return false;
        }
        IDetectorElement de = deSrch.get(0);
        double xPos = computeCoordinate(xVal, gridSizeX);
        double yPos = computeCoordinate(yVal, gridSizeY);
        if (de.getGeometry().getLogicalVolume().getSolid() instanceof Box)
        {
            Box sensorBox = (Box) de.getGeometry().getLogicalVolume().getSolid();
            // Check coordinate values against box bounds.
            if (sensorBox.inside(new BasicHep3Vector(xPos, yPos, 0)) == Inside.INSIDE)
            {
                return true;
            }
            // TODO: Handle edge case with partial cells.  (How???)
            else
            {
                return false;
            }
        }
        else
        {
            throw new RuntimeException("Don't know how to bounds check solid " + de.getGeometry().getLogicalVolume()
                    .getSolid().getName() + ".");
        }
    }

    public void setupGeomFields(IDDescriptor id)
    {
        if (geomFields == null)
        {
            geomFields = new ArrayList<Integer>();

            xIndex = id.indexOf("x");
            yIndex = id.indexOf("y");
            layerIndex = id.indexOf("layer");
            try
            {
                sliceIndex = id.indexOf("slice");
            }
            catch (IllegalArgumentException x)
            {
                System.err.println("WARNING: The slice field does not exist in this IDDecoder!");
                sliceIndex = -1;
            }

            // Set geometry field list.
            for (int i = 0; i < id.fieldCount(); i++)
            {
                String fname = id.fieldName(i);
                if (!fname.equals("x") && !fname.equals("y"))
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

    private void computeLocalY()
    {
        localPosition[1] = (((double) getValue(yIndex)) + 0.5) * gridSizeY;
    }

    protected void computeLocalPosition()
    {
        localPosition[0] = localPosition[1] = localPosition[2] = 0.0;
        computeLocalX();
        computeLocalY();
    }
}