package org.lcsim.geometry;

import hep.physics.vec.Hep3Vector;

import org.lcsim.geometry.subdetector.BarrelEndcapFlag;
import org.lcsim.geometry.util.IDDescriptor;

/**
 * 
 * The compact detector description's interface for identifier decoding.
 * 
 * @author tonyj
 * @author jeremym
 */
// List of TODOs
// -------------
// TODO: The getX/Y/Z methods should be removed. Instead use getPosition()[i] or
// getPositionVec.x/y/z().
// TODO: The findCellContainingXYZ methods probably don't belong here. In general, hard to
// implement and not always implemented by subclasses.
// This functionality should reside in a geometer class.
// TODO: Do NOT need three similar versions of findCellContainingXYZ() in the interface,
// because it is confusing. One should be good enough.
// TODO: The methods related to getting ID meta information should be left in
// IDDescriptor. No need to push these methods up to here and clutter the interface.
// TODO: The class hierarchy of sub-classes far too complicated. (CalorimeterIDDecoder,
// geometry.util, segmentation, BaseIDDecoder, etc.)
// TODO: Methods like getSystemID() should use getValue("system") instead. Same for
// getLayer().
// TODO: The methods getPhi() and getTheta() can be easily computed from position or
// retrieved from position vector.
// TODO: The method getSubdetector() needs to be revisted. This should instead be
// retrieved from the hit's metadata.
// TODO: In general, the fact that the decoder holds "state" of a current ID is not good
// and could potentially lead to some odd behaviors. A better
// method signature would be getFieldValue("field", int id). Then there is no implied
// state in the decoder or interface.
public interface IDDecoder
{
    /**
     * Constant that flags an invalid index, i.e. for a field that does not exist.
     */
    public static final int INVALID_INDEX = -1;

    /**
     * Load the decoder with a 64-bit id value from the hit.
     */
    public void setID( long id );

    /**
     * Get the value of the given field.
     * 
     * @param field The name of the field.
     * @return The integer value of the field.
     */
    public int getValue( String field );

    /**
     * Get the value of a field by index.
     * 
     * @param index The index into the id.
     * @return The integer value of the field.
     */
    public int getValue( int index );

    /**
     * Get all decoded field values.
     * 
     * @param buffer The buffer to receive values.
     * @return The buffer with field values.
     */
    public int[] getValues(int[] buffer);
    
    /**
     * Get the number of fields in this id description.
     * 
     * @return The number of fields.
     */
    public int getFieldCount();

    /**
     * Get field name by index.
     * 
     * @param index The index of the field.
     * @return The field name.
     */
    public String getFieldName( int index );

    /**
     * Get field index by name.
     * 
     * @param name The name of the field.
     * @return The index of the field.
     */
    public int getFieldIndex( String name );

    /**
     * Set the ID description of this decoder.
     * 
     * @param id The ID description.
     */
    public void setIDDescription( IDDescriptor id );

    /**
     * Get the ID description of this decoder.
     * 
     * @return The ID description.
     */
    public IDDescriptor getIDDescription();

    /**
     * Get the layer number for the current id.
     * 
     * @return layer number
     */
    public int getLayer();

    /**
     * Get the layer number, possibly adjusted for topologies such as EcalBarrel. The
     * default implementation returns the value of {{@link #getLayer()}.
     * 
     * @return The pseudo layer number.
     */
    public int getVLayer();

    /**
     * @return Hep3Vector representing the position of the current ID.
     */
    public Hep3Vector getPositionVector();

    /**
     * @return position as double array of length 3
     */
    public double[] getPosition();

    /**
     * @return X coordinate
     */
    public double getX();

    /**
     * @return Y coordinate
     */
    public double getY();

    /**
     * @return Z coordinate
     */
    public double getZ();

    /**
     * @return phi angle
     */
    public double getPhi();

    /**
     * The decoded theta angle of this id.
     * 
     * @return The theta angle.
     */
    public double getTheta();

    /**
     * Locate a cell from a global position.
     * 
     * @param pos The position.
     * @return The cell id.
     */
    public long findCellContainingXYZ( Hep3Vector pos );

    /**
     * Locate a cell from a global position.
     * 
     * @param pos The position.
     * @return The cell id.
     */
    public long findCellContainingXYZ( double[] pos );

    // Removed. --JM
    // public long findCellContainingXYZ(double x, double y, double z);

    /**
     * Get the flag that indicates barrel or endcap, i.e. the "barrel" field.
     */
    public BarrelEndcapFlag getBarrelEndcapFlag();

    /**
     * Get the system ID, i.e. the "system" field.
     */
    public int getSystemID();

    /**
     * @deprecated Use {@link #getSystemID()} instead
     */
    public int getSystemNumber();

    /**
     * Get the Subdetector associated with this IDDecoder, or null if not applicable.
     */
    public Subdetector getSubdetector();

    /**
     * Does this Decoder support cell neighboring?
     * 
     * @return True if Decoder supports neighbors; false if not.
     */
    public boolean supportsNeighbours();

    /**
     * Get the current cell's neighbors using default neighboring parameters (usually
     * 1,1,1).
     * 
     * @return The cell neighbors.
     */
    public long[] getNeighbourIDs();

    /**
     * Get the current cell's neighbors using fully specified neighboring parameters.
     * 
     * @param deltaLayer The number of layers to neighbor (plus or minus).
     * @param deltaTheta The number of cells in theta to neighbor (plus or minus).
     * @param deltaPhi The number of cells in phi to neighbor (plus or minus).
     * @return
     */
    public long[] getNeighbourIDs( int deltaLayer, int deltaTheta, int deltaPhi );
}