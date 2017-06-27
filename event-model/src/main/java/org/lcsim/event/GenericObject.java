package org.lcsim.event;

/** Simple interface to store generic user data.
 * To store your own classes they have to implement
 * this interface.
 * @version $Id: GenericObject.java,v 1.3 2006/06/28 04:48:31 jstrube Exp $
 */

public interface GenericObject
{
    /** Number of integer values stored in this object.
     */
    public int getNInt();
    
    /** Number of float values stored in this object.
     */
    public int getNFloat();
    
    /** Number of double values stored in this object.
     */
    public int getNDouble();
    
    /** Returns the integer value for the given index.
     */
    public int getIntVal(int index);
    
    /** Returns the float value for the given index.
     */
    public float getFloatVal(int index);
    
    /** Returns the double value for the given index.
     */
    public double getDoubleVal(int index);
    
    /** True if objects of the implementation class have a fixed size, i.e
     * getNInt, getNFloat and getNDouble will return values that are constant during
     * the lifetime of the object.
     */
    public boolean isFixedSize();

} // class or interface

