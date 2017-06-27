package org.lcsim.geometry;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

/**
 * A field map, allows getting the field at a given point
 * 
 * @author tonyj
 */
public interface FieldMap
{
    /**
     * Get the field magnitude and direction at a particular point.
     * 
     * @param position The position at which the field is requested
     * @param b The field (the object is passed by reference and set to the correct field)
     */
    public void getField( double[] position, double[] b );

    /**
     * Get the field magnitude and direction at a particular point. This method requires
     * allocation of a new object on each call, and should therefore not be used if it may
     * be called many times.
     * 
     * @param position The position at which the field is requested
     * @return The field.
     */
    public double[] getField( double[] position );

    /**
     * Get the field magnitude and direction at a particular point.
     * 
     * @param position The position at which the field is requested.
     * @param field The field. If not <code>null</code> this is passed by reference and
     *            set to the correct field
     * @return The field. This will be the same object passed as field, unless field was
     *         <code>null</code>
     */
    public Hep3Vector getField( Hep3Vector position, BasicHep3Vector field );

    /**
     * Get the field magnitude and direction at a particular point. Equivalent to
     * <code>getField(position,null)</code>.
     */
    public Hep3Vector getField( Hep3Vector position );
}
