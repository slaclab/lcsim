package org.lcsim.detector.identifier;

import java.util.List;

/**
 * Holds an ordered and mapped list of {@link IIdentifierField} objects
 * that define the fields of an {@link IIdentifier}.
 *
 * @author Jeremy McCormick
 * @version $Id: IIdentifierDictionary.java,v 1.12 2013/01/24 22:26:35 jeremy Exp $
 */
public interface IIdentifierDictionary
{
    /**
     * Get the name of this dictionary.    
     * @return The name of this IIdentifierDictionary.
     */
    String getName();
    
    /**
     * Get a field by name.     
     * @param fieldName The field.
     * @return The field.
     */
    IIdentifierField getField(String fieldName);
    
    /**
     * True if this <code>IdentifierDictionary</code> contains the field; false if not.
     * @param fieldName The name of the field.
     */
    boolean hasField(String fieldName);

    /**
     * Get the {@link IIdentifierField} at specified index.  
     * @return The field at position <code>index</code> in the field array.
     * @throws ArrayIndexOutOfBoundsException if the index is not in bounds.
     */
    IIdentifierField getField(int index);
    
    /**
     * Get the index of a named field.     
     * @param fieldName The name of the field.
     * @return The index index of the field.
     */
    int getFieldIndex(String fieldName);

    /**
     * Get the list of field names.    
     * @return The field names contained by this dictionary.
     */    
    List<String> getFieldNames();
    
    /**
     * Get the list of fields.
     * @return The list of fields.
     */
    List<IIdentifierField> getFieldList();
    
    /**
     * Get the number of fields in this dictionary.     
     * @return The number of fields in the dictionary.
     */
    int getNumberOfFields();
    
    /**
     * Get the max index in the field array.
     * @return The max index in the field array.
     */
    int getMaxIndex();
            
    /**
     * Pack an expanded id .
     * @param id The expanded id.
     * @return The packed id.
     */
    IIdentifier pack(IExpandedIdentifier id);
    
    /**
     * Unpack a packed id into an expanded id.
     * @param compact The packed id.
     * @return The expanded id.
     */
    IExpandedIdentifier unpack(IIdentifier compact);
    
    /**
     * Unpack id, only including fields with indices in list.
     * @param compact The packed id
     * @param indices The indices of the fields to unpack.
     * @return The expanded id.
     */
    IExpandedIdentifier unpack(IIdentifier compact, List<Integer> indices);

    /**
     * Get the value of a field extracted from a packed id by name. 
     * @param compact The packed id.
     * @param field The field name.
     * @return The value of the field.
     */
    int getFieldValue(IIdentifier compact, String field);
    
    /**
     * Get the value of a field extracted from a packed id by index. 
     * @param compact The packed id.
     * @param field The field name.
     * @return The value of the field.
     */
    int getFieldValue(IIdentifier compact, int idx);
    
    /**
     * Check whether an expanded identifier is valid for this dictionary.
     * This includes checking number of fields and the field values.
     * @return True if id is valid; false if not.
     */
    boolean isValid(IExpandedIdentifier id);
}