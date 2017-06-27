package org.lcsim.geometry.subdetector;

import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * 
 * @author tonyj
 */
public class CylindricalEndcapCalorimeter2 extends CylindricalCalorimeter
{
    CylindricalEndcapCalorimeter2( Element node ) throws JDOMException
    {
        super( node );
        build( node );
    }

    public boolean isEndcap()
    {
        return true;
    }

    private void build( Element node ) throws JDOMException
    {
    }

    public double getZLength()
    {
        return this.maxZ - this.minZ;
    }
}