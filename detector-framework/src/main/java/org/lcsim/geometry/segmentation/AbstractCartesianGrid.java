package org.lcsim.geometry.segmentation;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.identifier.ExpandedIdentifier;
import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.Identifier;
import org.lcsim.geometry.util.BaseIDDecoder;
import org.lcsim.geometry.util.IDDescriptor;
import org.lcsim.geometry.util.IDEncoder;
import org.lcsim.geometry.layer.Layering;

/**
 * This is the abstract base class for Cartesian grid segmentation types. It can
 * compute positions in 3D space but only allows two fields that correspond to
 * dimensions.  The detailed Java geometry system is used to transform 2D coordinates 
 * into 3D points within the detector.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: AbstractCartesianGrid.java,v 1.14 2011/02/25 03:09:38 jeremy Exp $
 */
public abstract class AbstractCartesianGrid extends SegmentationBase
{
    // Cached local position array.
    double[] localPosition = { 0, 0, 0 };
    
    // Cached global position array.
    double[] globalPosition = { 0, 0, 0 };
    
    // Flag if positions need to be recomputed.
    boolean needsCompute = true;
    
    // Geometric field indices.
    List<Integer> geomFields;
    
    // The grid cell sizes, which may be zero.
    double gridSizeX, gridSizeY, gridSizeZ;
    
    // Cached sensitive slices (Ron, please remove and use Layering system.  --JM)
    List[] sensitiveSlices;
    protected int sliceIndex = -1;

    /**
     * This constructor accepts an XML element corresponding to the segmentation
     * but does nothing with it. Sub-types should set their grid size values from 
     * this.
     * 
     * @param segmentation The XML element for the segmentation.
     */
    protected AbstractCartesianGrid(Element segmentation)
    {
        super(segmentation);
    }

    /**
     * Using the current ID, compute the local position in the readout volume
     * and set the internal <code>localPosition</code> array.
     */
    abstract protected void computeLocalPosition();

    /**
     * Make a list of fields that are geometric by stripping out this
     * segmentation's fields from the given <code>IDDescriptor</code>.
     * 
     * @param id The description of the Id.
     */
    abstract protected void setupGeomFields(IDDescriptor id);

    /**
     * Check if the id is valid, e.g. if it is within the corresponding
     * readout volume.
     * 
     * @param rawId
     * @return True if id is valid for this segmentation type; False if not.
     */
    abstract public boolean boundsCheck(long rawId);

    /**
     * Set the segmentation field values on the given
     * <code>IExpandedIdentifier</code> given a local hit position.
     * 
     * @param geomId The expanded id.
     * @param localPositionVec The local position of the hit.
     */
    abstract protected void setSegmentationValues(IExpandedIdentifier geomId, Hep3Vector localPositionVec);

    /**
     * Compute the integer bin value given a coordinate and a grid size.
     * 
     * @param u The coordinate value.
     * @param gridSize The grid size.
     * @return The bin value.
     */
    public final int getBin(double u, double gridSize)
    {
        double u0 = gridSize / 2;
        int iu = (int) Math.floor((u - u0) / gridSize + 0.5);
        return iu;
    }

    /**
     * Compute the coordinate given a bin value.
     * 
     * @param binValue The bin value.
     * @param gridSize The grid size.
     * @return The coordinate value.
     */
    public final double computeCoordinate(int binValue, double gridSize)
    {
        return (((double) binValue) + 0.5) * gridSize;
    }

    /**
     * Generic computation of hit's global position. Sets the
     * <code>globalPosition</code> array.
     */
    protected void computeGlobalPosition()
    {
        // Make an id only containing geometric fields and no segmentation fields.
        // TODO This may affect overall performance time.
        IExpandedIdentifier geomIdExp = 
            detector.getDetectorElement()
                .getIdentifierHelper()
                .getIdentifierDictionary()
                .unpack(new Identifier(this.getDecoder().getID()), geomFields);
        IIdentifier geomId = detector.getDetectorElement().getIdentifierHelper().pack(geomIdExp);

        // Search for the the DetectorElement associated with the geometry id.
        List<IDetectorElement> deSearch = detector.getDetectorElement().findDetectorElement(geomId);

        // Check if the DetectorElement lookup failed.
        if (deSearch == null || deSearch.size() == 0)
        {
            throw new RuntimeException("Failed to find DetectorElement with geometry id <" + geomIdExp.toString() + "> !");
        }

        // Set the DetectorElement to use for the local to global transform.
        IDetectorElement sensor = deSearch.get(0);

        // Create a vector from the local position array.
        Hep3Vector posVecLocal = new BasicHep3Vector(localPosition[0], localPosition[1], localPosition[2]);

        // Compute the global position of the hit using the local position and
        // the DetectorElement.
        Hep3Vector posVecGlobal = sensor.getGeometry().transformLocalToGlobal(posVecLocal);

        // Set the internal global position array.
        globalPosition[0] = posVecGlobal.x();
        globalPosition[1] = posVecGlobal.y();
        globalPosition[2] = posVecGlobal.z();
    }

    /**
     * Find the readout cell given a global position.
     * 
     * @return The cell id for the position.
     */
    public long findCellContainingXYZ(double x, double y, double z)
    {
        Hep3Vector pos = new BasicHep3Vector(x, y, z);
        IDetectorElement de = getSubdetector().getDetectorElement().findDetectorElement(pos);
        if (de == null)
        {
            throw new RuntimeException("No DetectorElement found at " + pos + ".");
        }
        if (!de.getGeometry().getPhysicalVolume().isSensitive())
        {
            throw new RuntimeException("The volume " + de.getName() + " is not sensitive.");
        }
        Hep3Vector localPositionVec = de.getGeometry().transformGlobalToLocal(pos);
        ExpandedIdentifier geomId = new ExpandedIdentifier(de.getExpandedIdentifier());
        setSegmentationValues(geomId, localPositionVec);
        return getSubdetector().getDetectorElement().getIdentifierHelper().pack(geomId).getValue();
    }

    /**
     * Get the position from the current cell ID. Recomputes the position if
     * necessary.
     * 
     * @return The position of the current cell.
     */
    public final double[] getPosition()
    {
        if (needsCompute)
        {
            computePosition();
            needsCompute = false;
        }
        return globalPosition;
    }

    /**
     * Compute and cache the cell's global position.
     */
    protected void computePosition()
    {
        computeLocalPosition();
        computeGlobalPosition();
    }

    /**
     * Create an id with geometry fields only given an id including geometry and
     * segmentation fields.
     * 
     * @param id The cell id.
     * @return A cell id with values for geometry fields only.
     */
    protected final IIdentifier makeGeometryIdentifier(long id)
    {
        IExpandedIdentifier geomIdExp = 
            detector.getDetectorElement()
                    .getIdentifierHelper()
                    .getIdentifierDictionary()
                    .unpack(new Identifier(id), geomFields);
        return detector.getDetectorElement().getIdentifierHelper().pack(geomIdExp);
    }

    /**
     * This method is overridden in order to flag the decoder as dirty so that
     * internal position data can be recomputed the next time a position is
     * retrieved by the user.
     */
    public final void setID(long id)
    {
        super.setID(id);
        needsCompute = true;
    }

    /**
     * We override this method to cache the geometry field information.
     */
    public void setIDDescription(IDDescriptor id)
    {
        super.setIDDescription(id);
        setupGeomFields(id);
    }

    /**
     * All implementations must support neighbor finding.
     * 
     * @return True.
     */
    public final boolean supportsNeighbours()
    {
        return true;
    }

    /**
     * Get the X coordinate.
     * 
     * @return The X coordinate.
     */
    public final double getX()
    {
        return getPosition()[0];
    }

    /**
     * Get the Y coordinate.
     * 
     * @return The Y coordinate.
     */
    public final double getY()
    {
        return getPosition()[1];
    }

    /**
     * Get the Z coordinate.
     * 
     * @return The Z coordinate.
     */
    public final double getZ()
    {
        return getPosition()[2];
    }

    /**
     * Utility method for finding neighbors in a 2D readout grid with layers.
     * 
     * @param layerRange The layer range.
     * @param uRange The u range.
     * @param vRange The v range.
     * @param uIndex The u field index.
     * @param vIndex The v field index.
     * @return
     */
    protected final long[] getNeighbourIDs(int layerRange, int uRange, int vRange, int uIndex, int vIndex)
    {
        if (this.getDecoder().getID() == 0)
        {
            throw new RuntimeException("No current ID is set.");
        }
        if (sensitiveSlices == null)
        {
            initSensitiveSlices();
        }
        IDEncoder gnEncoder = new IDEncoder(descriptor);
        BaseIDDecoder gnDecoder = new BaseIDDecoder(descriptor);
        gnEncoder.setValues(values);
        gnDecoder.setID(gnEncoder.getID());

        // Get number of layers.
        int nlayers = this.getNumberOfLayers();

        // Set values for current id.
        int currLayer = values[layerIndex];
        int currU = gnDecoder.getValue(uIndex);
        int currV = gnDecoder.getValue(vIndex);

        // Create return array.
        List<Long> neighbors = new ArrayList<Long>();

        // Loop over layer range.
        for (int ilayer = -layerRange; ilayer <= layerRange; ilayer++)
        {
            // Compute layer value.
            int neighborLayer = currLayer + ilayer;

            if (neighborLayer >= 0 && neighborLayer < nlayers)
            {
                gnEncoder.setValue(layerIndex, neighborLayer);
                // Loop over X range.
                for (int iu = -uRange; iu <= uRange; iu++)
                {
                    // Compute x value.
                    int neighborU = currU + iu;
                    gnEncoder.setValue(uIndex, neighborU);

                    // Loop over Y range.
                    for (int iv = -vRange; iv <= vRange; iv++)
                    {
                        // Compute y value.
                        int neighborV = currV + iv;

                        // Set the neighbor fields.
                        gnEncoder.setValue(vIndex, neighborV);

                        if (sliceIndex >= 0)
                        {
                            // Ron, this is how to get the sensor indices. --JM
                            // List<Integer> sensorIndices =
                            // getSubdetector().getLayering().getLayer(neighborLayer).getSensorIndices();

                            // Loop over sensitive slices
                            for (int is = 0; is < sensitiveSlices[neighborLayer].size(); is++)
                            {
                                // Set the slice field.
                                gnEncoder.setValue(sliceIndex, ((Integer) (sensitiveSlices[neighborLayer].get(is)))
                                        .intValue());                                

                                // Add the neighbor id to the return array.
                                if (this.getDecoder().getID() != gnEncoder.getID())
                                {
                                    neighbors.add(gnEncoder.getID());
                                }
                            }
                        }
                        else
                        {
                            // Add the neighbor id to the return array.
                            if (this.getDecoder().getID() != gnEncoder.getID())
                            {
                                neighbors.add(gnEncoder.getID());
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

    protected void initSensitiveSlices()
    {
        Layering layering = this.getLayering();
        sensitiveSlices = new List[layering.getNumberOfLayers()];
        for (int i = 0; i < layering.getNumberOfLayers(); i++)
        {
            sensitiveSlices[i] = new ArrayList<Integer>();
            for (int j = 0; j < layering.getLayer(i).getNumberOfSlices(); j++)
            {
                if (layering.getLayer(i).getSlice(j).isSensitive())
                {
                    sensitiveSlices[i].add(new Integer(j));
                }
            }
        }

    }
}
