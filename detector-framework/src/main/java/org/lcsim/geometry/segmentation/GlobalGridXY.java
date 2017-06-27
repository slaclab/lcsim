package org.lcsim.geometry.segmentation;

import hep.physics.vec.Hep3Vector;

import java.util.ArrayList;

import org.jdom.DataConversionException;
import org.jdom.Element;
import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.geometry.layer.Layer;
import org.lcsim.geometry.layer.Layering;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;
import org.lcsim.geometry.util.IDDescriptor;

/**
 * This segmentation is a global XY Cartesian grid. It is appropriate for
 * segmenting planar endcaps with a uniform grid in global XY coordinates.
 * 
 * @author jeremym
 */
public class GlobalGridXY extends AbstractCartesianGrid
{
    private int xIndex = -1;
    private int yIndex = -1;
    private static final String[] fieldNames = {"x", "y"};

    public GlobalGridXY(Element node) throws DataConversionException
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

    protected void computePosition()
    {
        computeGlobalX();
        computeGlobalY();
        computeGlobalZ();
    }

    private void computeGlobalX()
    {
        globalPosition[0] = (((double) getValue(xIndex)) + 0.5) * gridSizeX;
    }

    private void computeGlobalY()
    {
        globalPosition[1] = (((double) getValue(yIndex)) + 0.5) * gridSizeY;
    }

    /**
     * Compute and cache the global Z of the hit.
     */
    private void computeGlobalZ()
    {
        Layering layers = getSubdetector().getLayering();
        int sliceIdx = getValue("slice");
        int layerIdx = getValue("layer");
        Layer layer = layers.getLayer(layerIdx);

        globalPosition[2] = layers.getDistanceToLayer(layerIdx) + layer.computeDistanceToSliceMid(sliceIdx);

        // Flip sign to negative for south endcap.
        if (getBarrelEndcapFlag() == BarrelEndcapFlag.ENDCAP_SOUTH)
        {
            globalPosition[2] = -globalPosition[2];
        }
    }

    protected void setSegmentationValues(IExpandedIdentifier geomId, Hep3Vector positionVec)
    {
        geomId.setValue(xIndex, getXBin(positionVec.x()));
        geomId.setValue(yIndex, getYBin(positionVec.y()));
    }

    protected void setupGeomFields(IDDescriptor id)
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
                System.err.println("The slice field does not exist in this IDDecoder.");
                sliceIndex = -1;
            }

            // Exclude "x" and "y" fields in field list.
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

    public long[] getNeighbourIDs(int layerRange, int xRange, int yRange)
    {
        return getNeighbourIDs(layerRange, xRange, yRange, xIndex, yIndex);
    }

    public int getXBin(double x)
    {
        return getBin(x, gridSizeX);
    }

    public int getYBin(double y)
    {
        return getBin(y, gridSizeY);
    }

    /**
     * Similar to super-class implementation but doesn't need to do a global to
     * local transform into the target shape's coordinate system.
     */
    /*
    public long findCellContainingXYZ(double x, double y, double z) 
    {
        Hep3Vector pos = new BasicHep3Vector(x,y,z);
        IDetectorElement de = getSubdetector().getDetectorElement().findDetectorElement(pos);
        if (de == null)
        {
            throw new RuntimeException("No DetectorElement was found at " + pos + ".");
        }
        if (!de.getGeometry().getPhysicalVolume().isSensitive())
        {
            throw new RuntimeException("The volume " + de.getName() + " is not sensitive.");
        }        
        ExpandedIdentifier geomId = new ExpandedIdentifier(de.getExpandedIdentifier());
        setSegmentationValues(geomId, pos);
        return getSubdetector().getDetectorElement().getIdentifierHelper().pack(geomId).getValue();
    }    
     */

    protected void computeLocalPosition()
    {
    }

    // TODO: Implement this method.
    public boolean boundsCheck(long rawId)
    {
        return false;
    }

    /*
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

        double zPos = getSubdetector().getLayering().getDistanceToLayerSensorMid(getLayer());
        // Flip sign to negative for south endcap.
        if (getBarrelEndcapFlag() == BarrelEndcapFlag.ENDCAP_SOUTH)
        {
            zPos = -zPos;
        }        

        // Check that coordinate is inside sensor (doesn't check the z coordinate!).
        ISolid sensor = de.getGeometry().getLogicalVolume().getSolid();
        if (sensor.inside(new BasicHep3Vector(xPos, yPos, zPos)) == Inside.INSIDE)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
     */

    /*
    OLD METHOD
    private void computeGlobalZ()
    {
        // Make an id only containing geometric fields and no segmentation fields.
        IExpandedIdentifier geomIdExp = detector.getDetectorElement().getIdentifierHelper().unpack(new Identifier(this.getDecoder().getID()), geomFields);
        IIdentifier geomId = detector.getDetectorElement().getIdentifierHelper().pack(geomIdExp);

        // Search for the the DetectorElement associated with the geometry id.
        List<IDetectorElement> deSearch = detector.getDetectorElement().findDetectorElement(geomId);

        // Check if the lookup failed.
        if (deSearch == null || deSearch.size() == 0)
        {
            throw new RuntimeException("Failed to find DetectorElement with geometry id <" + geomIdExp.toString() + "> !");
        }

        // Set the DetectorElement to use for local to global transform.
        IDetectorElement sensor = deSearch.get(0);

        globalPosition[2] = sensor.getGeometry().getPosition().z();
    } 

     */
}