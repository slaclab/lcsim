package org.lcsim.geometry.subdetector;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.CylindricalSubdetector;

/**
 * @author Tony Johnson
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: CylindricalCalorimeter.java,v 1.18 2010/11/30 00:16:29 jeremy Exp $
 * 
 * FIXME: This is public only because changing to protected would break a bunch
 *        of stuff, mostly in digisim. F
 * FIXME: This should be called AbstractCylindricalCalorimeter, as it can't be instantiated, 
 *        but doing this breaks a bunch of digisim stuff.
 */
public abstract class CylindricalCalorimeter extends AbstractCalorimeter implements CylindricalSubdetector
{
    protected double innerR;
    protected double outerR;
    protected double minZ;
    protected double maxZ;

    public CylindricalCalorimeter( Element node ) throws JDOMException
    {
        super( node );
        build( node );
    }

    private void build( Element node ) throws JDOMException
    {
        Element dimensions = node.getChild( "dimensions" );
        innerR = dimensions.getAttribute( "inner_r" ).getDoubleValue();
    }

    public double getZMin()
    {
        return minZ;
    }

    public double getZMax()
    {
        return maxZ;
    }

    public double getOuterZ()
    {
        return getZMax();
    }

    public double getInnerZ()
    {
        return getZMin();
    }

    public double getOuterRadius()
    {
        return outerR;
    }

    public double getInnerRadius()
    {
        return innerR;
    }

    public int getNumberOfSides()
    {
        return 0;
    }

    public double getSectionPhi()
    {
        return Math.PI * 2;
    }
}