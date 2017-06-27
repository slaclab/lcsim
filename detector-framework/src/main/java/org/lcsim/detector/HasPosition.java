package org.lcsim.detector;

import hep.physics.vec.Hep3Vector;

/**
 * Mixin interface for objects that have a Cartesian position.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public interface HasPosition
{
    double[] getPosition();
    Hep3Vector getPositionVec();
}
