package org.lcsim.detector.identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link IIdentifierDictionary}. This class is declared final because overriding the methods could
 * cause serious bugs. To provide specialized id functionality, the class {@link IdentifierHelper} can be subclassed
 * instead.
 * 
 * @author Jeremy McCormick
 * @version $Id: IdentifierDictionary.java,v 1.9 2013/01/24 22:26:35 jeremy Exp $
 */
// TODO Add ctor checks on fields: no overlapping ranges, no overflow, w/in 64 bits, no fields crossing the 32-bit boundary, etc.
public final class IdentifierDictionary implements IIdentifierDictionary
{
    // An array of field objects for fast access by index.
    private IIdentifierField fieldArray[] = null;
    
    // A map of field names to field objects, maintaining creation order.
    private Map<String, IIdentifierField> fieldMap = new LinkedHashMap<String, IIdentifierField>();

    // A map of field names to their indices in the array.
    private Map<String, Integer> fieldIndices = new HashMap<String, Integer>();

    // The name of the dictionary.
    private final String name;

    // The number of fields inthe dictionary.
    protected int numberOfFields;
    
    // The maximum array index, which is cached here for performance.
    protected int maxIndex;

    /**
     * Create an identifier dictionary with fields from <code>fieldList</code>. A dictionary cannot be modified once it
     * is created.
     * @param name The name of the dictionary.
     * @param fieldList The list of fields.
     */
    public IdentifierDictionary(String name, List<IIdentifierField> fieldList)
    {
        // Set the dictionary name.
        this.name = name;

        // Add the fields to the dictionary.
        addFields(fieldList);

        // Set the number of fields.
        numberOfFields = fieldList.size();

        // Set the max index in the dictionary.
        maxIndex = numberOfFields - 1;
       
        // TODO Register with global IdDictManager.
    }

    /**
     * Get the name of this dictionary.
     * @return The dictionary's name.
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Get the list of fields.
     * @return The list of fields.s
     */
    public List<IIdentifierField> getFieldList() {
        return Arrays.asList(fieldArray);
    }

    /**
     * Get a field by name or <code>null</code> if it doesn't exist in this dictionary.
     * @return The field called <code>fieldName</code> or null if doesn't exist.
     */
    public IIdentifierField getField(String fieldName)
    {
        return fieldMap.get(fieldName);
    }

    /**
     * Get a field by index.
     * @return The field at <code>index</code> in the array.
     * @throws ArrayOutOfBoundsException if index is not valid.
     */
    public IIdentifierField getField(int index)
    {
        return fieldArray[index];
    }

    /**
     * Get a field's index in the array or <code>null</code> if it doesn't exist.
     * @return The field index for the field called <code>fieldName</code> or null if doesn't exist.
     */
    public int getFieldIndex(String fieldName)
    {
        return fieldIndices.get(fieldName);
    }

    /**
     * Get the list of fields in this dictionary, which is cloned in case the caller modifies it.
     * This means that callers should take care not to put this method inside a loop unnecessarily.
     * @return The list of fields.
     */
    public List<String> getFieldNames()
    {
        return new ArrayList<String>(fieldMap.keySet());
    }

    /**
     * Get the number of fields in the dictionary.
     * @return The number of fields.
     */
    public int getNumberOfFields()
    {
        return numberOfFields;
    }
    
    /**
     * Get the maximum index in the field array.
     * @return The max index.
     */
    public int getMaxIndex()
    {
        return maxIndex;
    }

    /**
     * True if the dictionary has a field called <code>fieldName</code>; false if not.
     * @return True if <code>fieldName</code> exists in this dictionary; false if not.
     */
    public boolean hasField(String fieldName)
    {
        return fieldMap.containsKey(fieldName);
    }

    /**
     * Unpack a packed id.
     * @return The expanded id, which is a list of field values.
     */
    public IExpandedIdentifier unpack(IIdentifier compact)
    {
        ExpandedIdentifier expId = new ExpandedIdentifier();
        long id = compact.getValue();

        for (int i = 0; i < numberOfFields; i++ )
        {
            IIdentifierField field = getField(i);

            int start = field.getOffset();
            int length = field.getNumberOfBits();
            int mask = field.getIntegerMask();

            int result = (int)((id >> start) & mask);
            if (field.isSigned())
            {
                int signBit = 1 << (length - 1);
                if ((result & signBit) != 0)
                    result -= (1 << length);
            }

            expId.addValue(result);
        }

        return expId;
    }
    
    public IExpandedIdentifier unpack(IIdentifier compact, List<Integer> indices)
    {        
        ExpandedIdentifier buffer = new ExpandedIdentifier();
        
        long id = compact.getValue();
                                               
        for ( int i=0; i<numberOfFields; i++)
        {            
            if (indices.contains(i))
            {                                                
                IIdentifierField field = getField(i);
                
                int start = field.getOffset();
                int length = field.getNumberOfBits();
                int mask = field.getIntegerMask();
                
                int result = (int) ((id >> start) & mask);
                if (field.isSigned())
                {
                    int signBit = 1<<(length-1);
                    if ((result & signBit) != 0) result -= (1<<length);
                }
                
                buffer.addValue(result);
            }
            else
            {
                buffer.addValue(0);
            }
        }
               
        return buffer;
    }
    
    

    /**
     * Pack an expanded id.
     * @return The packed id.
     */
    public IIdentifier pack(IExpandedIdentifier id)
    {
        long result = 0;

        for (int i = 0; i <= maxIndex; i++ )
        {
            IIdentifierField field = getField(i);

            int start = field.getOffset();
            long mask = field.getLongMask();
            result |= (mask & id.getValue(i)) << start;
        }

        return new Identifier(result);
    }
    
    /**
     * Extract value of a named field from packed id.
     * @param compact The packed id.
     * @param field The field name.
     * @return The field value.
     */
    public int getFieldValue(IIdentifier compact, String field)
    {
        return getField(field).unpack(compact);
    }
    
    /**
     * Extract value of a field by index.
     * @param compact The packed id.
     * @param field The field name.
     * @return The field value.
     */
    public int getFieldValue(IIdentifier compact, int idx)
    {
        return getField(idx).unpack(compact);
    }

    /**
     * Add a field.
     * @param field The field object.
     * @param index The field index.
     */
    private void addField(IIdentifierField field, int index)
    {
        // Check for duplicate name.
        if (fieldMap.containsKey(field.getLabel()))
        {
            throw new RuntimeException("Duplicate field name <" + field.getLabel() + "> in IdDict <" + getName() + ">.");
        }

        // Put into the field map.
        fieldMap.put(field.getLabel(), field);

        // Add field to array.
        fieldArray[index] = field;

        // Store the order of this field by name.
        fieldIndices.put(field.getLabel(), index);
    }

    /**
     * Add a list of fields.
     * @param fields The list of fields.
     */
    // TODO Add sanity checks.
    private void addFields(List<IIdentifierField> fields)
    {
        // Initialize the field array for fast indexed access.
        fieldArray = new IIdentifierField[fields.size()];

        // Add the fields to the dictionary.
        int fieldIndex = 0;
        for (int i = 0, n = fields.size(); i < n; i++ )
        {
            IIdentifierField field = fields.get(i);
            addField(field, i);
            fieldArray[fieldIndex] = field;
            ++fieldIndex;
        }
    }

    
    /**
     * Check that an {@link IExpandedIdentifier} is valid for this dictionary.
     * This method checks that the sizes are the same and that the values
     * are valid for their corresponding fields.
     * @return True if valid; false if not.
     */
    public boolean isValid(IExpandedIdentifier id)
    {
        int size = id.size();
        if (size != numberOfFields)
        {
            return false;
        }
        for (int i = 0, n = id.getMaxIndex(); i < n; i++ )
        {
            IIdentifierField field = getField(i);
            if (!field.isValidValue(id.getValue(i)))
                return false;
        }
        return true;
    }

    /**
     * Convert the dictionary to a String.
     * @return A string with the dictionary's field information.
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer();
        str.append(getName() + '\n');
        for (IIdentifierField field : fieldMap.values())
        {
            str.append("    " + field.toString());
        }
        return str.toString();
    }

}