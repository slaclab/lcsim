package org.lcsim.recon.tracking.spacegeom;

import junit.framework.TestCase;

/**
 *
 * @author Norman A. Graf
 *
 * @version $Id:
 */
public class SpacePointTensorTest extends TestCase
{
    boolean debug = false;
    public void testSpacePointTensor()
    {
        SpacePointTensor spt = new SpacePointTensor();
        if(debug) System.out.println(spt);
        //TODO should check that everything is initialized to zero
        // trust Java for now.
    }
}
