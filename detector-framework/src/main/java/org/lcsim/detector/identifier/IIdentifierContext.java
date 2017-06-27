package org.lcsim.detector.identifier;

/**
 * IIdentifierContext defines a set of Identifier field indices that are 
 * applicable in a given context, such as a certain level of the
 * detector hierarchy.  The simplest way to define it is with start and end indices.  
 * It can also be defined as a set of index values that do not constitute a range.
 * If a set of index values is found to constitute a range at construction
 * time, then it will be treated as such, and {@link #isRange()} will return
 * <code>true</code>.
 *
 * @author Jeremy McCormick
 * @version $Id: IIdentifierContext.java,v 1.2 2010/04/14 18:49:52 jeremy Exp $
 */
public interface IIdentifierContext 
{
	/**
	 * The set of discrete indices in this range.
	 * @return Int array of indices.
	 */
	int[] getIndices();
	
	/**
	 * The start index.
	 * @return The start index.
	 */
	int getStartIndex();
	
	/**
	 * The end index.
	 * @return The end index.
	 */
	int getEndIndex();
	
	/**
	 * Does this IdContext constitution a contiguous block of fields between
	 * the start and end indices?
	 * @return True if this is a range; false if not.
	 */
	boolean isRange();
	
	/**
	 * Check if <code>index</code> is within this context.
	 * @param index
	 * @return True if index is within this context; false if not.
	 */
	boolean isValidIndex(int index);
	
	/**
	 * The number of indices.
	 * @return The number of indices.
	 */
	int getNumberOfIndices();
	
	/**
	 * Get the index at position <code>i</code>.
	 * @param i
	 * @return The index at position <code>i</code>.
	 * @throws IllegalArgumentException if <code>i</code> is out of range.
	 */
	int getIndex(int i);
}