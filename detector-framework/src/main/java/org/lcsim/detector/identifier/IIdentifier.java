package org.lcsim.detector.identifier;

/**
 * An interface defining comparable 64-bit identifiers
 * using for uniquely defining components of the
 * detector. 
 * 
 * @author Jeremy McCormick
 * @version $Id: IIdentifier.java,v 1.5 2007/05/09 00:59:58 jeremy Exp $
 */
public interface IIdentifier
extends Comparable
{
    /**
     * Get the value of this identifier as a long.
     * @return The long value.
     */
    long getValue();
    
    /**
     * Set the value of this identifier from a long.
     * 
     * @param id The long value.
     */
    void setValue(long id);
          
    /**
     * Get a raw hex string representation.
     * 
     * @return The raw hex string.
     */
	public String toHexString();

    /**
     * Set the value from a raw hex string.
     * 
     * @param hexRep The hex representation.
     */
	public void fromHexString(String hexRep);
    
	/**
     * Clear the state of this identifier,
     * setting the value to 0. 
	 */
	public void clear();

	/**
     * True if this ID has been set; 
     * False if it has not been set
     * or was set and {@link #clear()}
     * was called. 
     *  
     * @return True if this ID is valid.
	 */
	public boolean isValid();
}