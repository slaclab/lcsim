package org.lcsim.detector.identifier;

/**
 * Represents a single field within a numerical identifier.
 *
 * @author Jeremy McCormick
 * @version $Id: IIdentifierField.java,v 1.8 2011/02/25 03:09:38 jeremy Exp $
 */
public interface IIdentifierField
{
    /**
     * Get the string label associated with this field.
     * @return The label.
     */
    public String getLabel();

    /**
     * Get the number of bits in this field. 
     * @return The number of bits in this field.
     */
    public int getNumberOfBits();

    /**
     * Get offset, or starting position, of this field.
     * @return The offset of the field.
     */
    public int getOffset();

    /**
     * Get mask on as long value.
     * @return The on mask as a long.
     */
    public long getLongMask();

    /**
     * Get mask on as int value.
     * @return The on mask on an int.
     */
    public int getIntegerMask();

    /**
     * True if field is capable of holding signed values.
     * @return True if field is signed.
     */
    public boolean isSigned();

    /**
     * Get the order of this field in the id, or -1 if the 
     * ordering is unknown.
     * 
     * @return The order this field in the id.
     */
    //public int getOrder();

    /**
     * Unpack and return the value of this field from a 64-bit {@link IIdentifier}.
     * 
     * @return Field's int value. 
     */
    public int unpack( IIdentifier id );

    /**
     * Unpack and return the value of this field from a raw long.
     * 
     * @return Field's int value. 
     */
    public int unpack( long value );

    /**
     * Pack a single value of this field into a {@link IIdentifier}.
     * 
     * @param value A value to pack into this field.
     * @return An identifier with the packed value.
     */
    public IIdentifier pack( int value );

    /**
     * Pack a single value this field into an existing {@link IIdentifier},
     * preserving the other field values that may be present.
     * 
     * @param value  The field value.
     * @param id     An Identifier.
     */
    public void pack( int value, IIdentifier id );

    /**
     * Get the field value from an {@link IExpandedIdentifier}.
     * 
     * @param id The <code>ExpandedIdentifier</code>.
     * @return The field value.
     * @throws IllegalArgumentException if field index is not valid for the id.
     */
    //public int getFieldValue( IExpandedIdentifier id );

    /**
     * Get the maximum inclusive value that can be stored in this field.
     * @return The maximum field value.
     */
    public int getMaxValue();

    /**
     * Get the minimum inclusive value that can be stored in this field.
     * @return The minimum field value.
     */
    public int getMinValue();

    /**
     * True if value is within valid range of field values; false if not.
     * @return True if value is valid; false if not.
     */
    public boolean isValidValue( int value );
}