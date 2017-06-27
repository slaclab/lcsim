/*
 * BarrelCylinderSegmentationBase.java 20051124 - G.Lima - Created
 */

package org.lcsim.geometry.segmentation;

import org.jdom.Element;
import org.lcsim.geometry.layer.Layer;
import org.lcsim.geometry.layer.LayerStack;

/**
 * Base implementation of a barrel segmentation, it provides common code
 * implementation.
 * 
 * @author Guilherme Lima
 * @version $Id: BarrelCylinderSegmentationBase.java,v 1.1 2005/12/07 10:39:37
 *          lima Exp $
 */
// FIXME This can probably be folded into some other class.  It only has one method.
public abstract class BarrelCylinderSegmentationBase extends SegmentationBase
{
    public BarrelCylinderSegmentationBase(Element e)
    {
        super(e);
    }

    /**
     * Return the layer number based on the radial distance to cylinder axis.
     * 
     * @param r
     *            radial distance to cylinder axis
     * @return layer number of layer corresponding to that distance (may be
     *         either in absorber or live material)
     */
    public int getLayerBin(double r)
    {
        // In order to be general, we should not assume that all
        // layers have the same thickness. Therefore, one has to
        // guess the starting layer (based on average thickness), and
        // then navigate through layers until one finds the right one
        double depth = r - getRMin();
        double mean_t = (getRMax() - getRMin()) / getNumberOfLayers();

        int ilay = (int) Math.floor(depth / mean_t);
        LayerStack stack = getLayering().getLayers();
        Layer layer = stack.getLayer(ilay);
        double depHi = stack.getThicknessToLayerBack(ilay);
        double depLo = depHi - layer.getThickness();
        for (;;)
        {
            if (depth > depLo && depth <= depHi)
                return ilay;
            if (depth <= depLo)
            {
                --ilay;
                depHi = depLo;
                layer = stack.getLayer(ilay);
                depLo -= layer.getThickness();
            }
            if (depth > depHi)
            {
                ++ilay;
                depLo = depHi;
                layer = stack.getLayer(ilay);
                depHi += layer.getThickness();
            }
        }
    }
}
