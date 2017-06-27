package org.lcsim.detector.identifier;

/**
 * This class has a reference to a single {@link IIdentifierDictionary} that specifies the fields for a 64-bit
 * {@link IIdentifier}.
 * 
 * The {{@link #pack(IExpandedIdentifier)} method is used to turn an {@link IExpandedIdentifier} of field values into a
 * compact, or packed, identifier.
 * 
 * The {@link #unpack(IIdentifier)} method does the reverse, taking a packed {@link IIdentifier} and turning into a list
 * of field values.
 * 
 * The {@link #getValue(IIdentifier compact, IIdentifierField desc )} method is used to unpack a single field value.
 * There are similar, overloaded methods for using an index in the dictionary or a field label to retrieve a single
 * field value.
 * 
 * @see org.lcsim.detector.identifier
 * @see IIdentifier
 * @see IExpandedIdentifier
 * @see IIdentifierDictionary
 * 
 * @author Jeremy McCormick
 * @version $Id: IIdentifierHelper.java,v 1.11 2011/02/25 03:09:38 jeremy Exp $
 */

public interface IIdentifierHelper
{
    /**
     * Get the {@link IIdentifierDictionary} associated with this helper.
     * 
     * @return The IdentifierDictionary.
     */
    IIdentifierDictionary getIdentifierDictionary();

    /**
     * Pack an {@link IExpandedIdentifier} into a compact {@link IIdentifier}.
     * 
     * @param id The ExpandedIdentifier to be packed.
     * @return The compact identfier.
     */
    IIdentifier pack(IExpandedIdentifier id);
    
    /**
     * Upack the {@link IIdentifier} into an {@link IExpandedIdentifier} containing the field values in a list.
     * 
     * @param id The Identifier.
     * @return An ExpandedIdentifier.
     */
    public IExpandedIdentifier unpack(IIdentifier id);

    /**
     * Get a single field value by index.
     * 
     * @param compact The compact Identifier.
     * @param field The field index in the dictionary.
     * @return The field value.
     */
    int getValue(IIdentifier compact, int field);

    /**
     * Get a single field value by name.
     * 
     * @param compact The compact Identifier.
     * @param field The field index in the dictionary.
     * @return The field value.
     */
    int getValue(IIdentifier compact, String field);

    /**
     * Pushed up from {@link IIdentifierDictionary#hasField(String)}.
     * @param fieldName The field name.
     * @return True if this helper's dictionary has a field called <code>fieldName</code>; False if not.
     */
    boolean hasField(String fieldName);    
    
    /**
     * Wraps {@link IIdentifierDictionary#getFieldIndex(String)}.
     * 
     * @param fieldName
     * @return
     * @throws FieldNotFoundException
     */
    public int getFieldIndex(String fieldName);
    
    public IExpandedIdentifier createExpandedIdentifier();
    
    /**
     * Pack a subset of fields into an {@link IExpandedIdentifier}.
     * 
     * @param id An ExpandedIdentifier to pack.
     * @param start The start index in the ExpandedIdentifier.
     * @return An Identifier with the packed fields.
     */
    //public IIdentifier pack(IExpandedIdentifier id, int start);
    
    /**
     * Get a single field value using an {@link IIdentifierField}.
     * 
     * @param compact The compact Identifier.
     * @param desc The field description.
     * @return The field value.
     */
    //public int getValue(IIdentifier compact, IIdentifierField desc);
    
    /**
     * Pack a subset of fields into an {@link IExpandedIdentifier}.
     * 
     * @param id An ExpandedIdentifier to pack.
     * @param startIndex Starting index.
     * @param endIndex End index.
     * @return An Identifier with the packed fields.
     */
    //public IIdentifier pack(IExpandedIdentifier id, int startIndex, int endIndex);

    /**
     * 
     * Upack the {@link IIdentifier} into an {@link IExpandedIdentifier} containing the fields starting at index start
     * and unpacking nfields fields. The {@link IExpandedIdentifier} is padded with zero values to match the full
     * specification in the {@link IIdentifierDictionary}.
     * 
     * @param id The identifier.
     * @param startIndex The start index.
     * @param endIndex The end index.
     * @return An ExpandedIdentifier with the unpacked values.
     */
    //public IExpandedIdentifier unpack(IIdentifier id, int startIndex, int endIndex);

    /**
     * Unpack the {@link IIdentifier} into an {@link IExpandedIdentifier} containing the fields starting at index start
     * and unpacking all fields to the end. The {@link IExpandedIdentifier} is padded with zero values to match the full
     * specification in the {@link IIdentifierDictionary}.
     * 
     * @param id The identifier.
     * @param startIndex The start field index.
     * @return An ExpandedIdentifier with the unpacked values.
     */
    //public IExpandedIdentifier unpack(IIdentifier id, int startIndex);

    /**
     * Unpack {@link IIdentifier} into an {@link IExpandedIdentifier}, ignoring fields that are not in the list of
     * indices. Fields not in the list are given a value of 0.
     * @param iddict The IIdentifierDictionary.
     * @param compact The identifier.
     * @param indices The list of indices.
     * @return
     */
    //public IExpandedIdentifier unpack(IIdentifier compact, List<Integer> indices);

    /**
     * Set the single field value of an expanded identifier by named field.
     * @param expid The expanded identifier.
     * @param field The field name.
     * @param value The field value.
     */
    //public void setValue(IExpandedIdentifier expid, String field, int value);

    /**
     * Set the single field value of an expanded identifier by integer index.
     * @param expid The expanded identifier.
     * @param index The field index.
     * @param value The field value.
     */
    //public void setValue(IExpandedIdentifier expid, int index, int value);
}