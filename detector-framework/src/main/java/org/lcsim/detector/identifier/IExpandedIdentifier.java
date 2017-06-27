package org.lcsim.detector.identifier;

import java.util.List;

/**
 * An ID that has been unpacked from an {@link IIdentifier}
 * so that individual field values are available by index.
 *
 * Use the associated {@link IIdentifierDictionary} to
 * see which indices correspond to which field labels. 
 * 
 * @author  jeremym
 * @version $Id: IExpandedIdentifier.java,v 1.13 2007/11/26 18:34:27 jeremy Exp $
 */

public interface IExpandedIdentifier
{
    /**
     * Push a value onto the end of this identifier.
     */
    public void addValue(int value);

    /**
     * Get the field value at an index.
     */
    public int getValue(int index);

    /**
     * Set the field value at an index.
     */
    public void setValue(int index, int value);

    /**
     * Return the field values as a list. 
     */
    public List<Integer> getValues();

    /**
     * Get the number of fields in this identifier.
     */
    public int size();

    /**
     * Get the maximum index which is {@link #size} - 1 .
     */
    public int getMaxIndex();
    
    /**
     * True if the index is valid.
     * @param i The index.
     * @return True if index is valid.
     */
    public boolean isValidIndex(int i);

    /**
     * Clear the identifier of all field values.
     * The {@link #size} method returns 0 after
     * this method is called.
     */
    public void clear();

    /**
     * True if the {@link IExpandedIdentifier} is in
     * a valid state with 1 or more field values;
     * False if there are no values when size is 0. 
     */
    public boolean isValid();
        
    /**
     * Compares identifiers on prefix fields.
     * The smaller of the two maximum indices is
     * used for the field range.
     */
    public int match(IExpandedIdentifier id);
    
    /**
     * Compare a single field value.
     * @param id
     * @param idx
     * @return
     */
    public int compareField( IExpandedIdentifier id, int idx );
}
