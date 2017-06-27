package org.lcsim.geometry.subdetector;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.CylindricalSubdetector;

/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: AbstractCylindricalTracker.java,v 1.3 2012/01/19 12:43:54 grefe Exp $
 */
abstract class AbstractCylindricalTracker extends AbstractTracker implements CylindricalSubdetector
{
    private double zmax;
    private double zmin;
    private double rmin;
    private double rmax;

    public AbstractCylindricalTracker( Element node ) throws JDOMException
    {
        super( node );
        build( node );
    }

    private void build( Element node ) throws JDOMException
    {
        Element dimensions = node.getChild( "dimensions" );

        if ( dimensions == null )
        {
            throw new JDOMException( "Missing dimensions element." );
        }

        rmin = dimensions.getAttribute( "inner_r" ).getDoubleValue();
        zmax = dimensions.getAttribute( "outer_z" ).getDoubleValue();
        if ( dimensions.getAttribute( "inner_z" ) != null )
        {
            zmin = dimensions.getAttribute( "inner_z" ).getDoubleValue();
        }
        else
        {
        	zmin = -zmax;
        }
        rmax = rmin + getLayering().getLayers().getTotalThickness();
        getLayering().setOffset( rmin );
    }

    public double getZMax()
    {
        return zmax;
    }

    public double getZMin()
    {
        return zmin;
    }

    public double getOuterRadius()
    {
        return rmax;
    }

    public double getInnerRadius()
    {
        return rmin;
    }
}