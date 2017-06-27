package org.lcsim.spacegeom;

import hep.physics.vec.Hep3Vector;

/**
 * A SpaceVector is the representation of a vector in 3D space, where a vector is defined to have a direction and a length.
 * SpaceVector objects know about their representation in cartesian, spherical and cylindrical coordinate systems.
 * In distinction to points in space, vectors are invariant under translation and can be multiplied by a scalar.
 * A scalar and a vector product is defined for vectors, and the difference between two SpacePoints is a SpaceVector.
 * For interoperability with Freehep classes, SpaceVector implements the Hep3Vector interface. 
 *
 *@version $Id: SpaceVector.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
 */
public class SpaceVector extends SpacePoint
{
    
    /**
     * A vector of zero length doesn't make sense
     */
    SpaceVector()
    {
    }
    
    /**
     * Constructor from a SpacePoint
     * Creates a vector from (0,0,0) to this point
     * @param spt SpacePoint to point to
     */
    public SpaceVector(Hep3Vector spt)
    {
        super(spt);
    }
    
    /**
     * Output Stream
     *
     * @return  String representation of object
     */
    public String toString()
    {
        return  _representation + " SpaceVector: " + "\n" +
                "    x: " + x()     + "\n" +
                "    y: " + y()     + "\n" +
                "    z: " + z()     + "\n" +
                "  rxy: " + rxy()   + "\n" +
                " rxyz: " + rxyz()  + "\n" +
                "  phi: " + phi()   + "\n" +
                "theta: " + theta() + "\n" ;
    }

}
