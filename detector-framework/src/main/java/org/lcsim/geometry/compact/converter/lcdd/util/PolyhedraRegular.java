/*
 * PolyhedraRegular.java
 *
 * Created on August 24, 2005, 10:34 PM
 *
 */

package org.lcsim.geometry.compact.converter.lcdd.util;

/**
 *
 * @author jeremym
 *
 * This class represents a regular polyhedra solid, e.g. one without any slope to the sides.
 * Thus, it does not require user definition of any zplanes, unlike Polycone.
 */
public class PolyhedraRegular extends Solid
{
    double rmin;
    double rmax;
    
    public PolyhedraRegular(String name, int nsides, double rmin, double rmax, double zlength)
    {
        super("polyhedra", name);
        
        setAttribute("startphi", String.valueOf(0));
        setAttribute("deltaphi", String.valueOf(Math.PI * 2) );
        setAttribute("numsides", String.valueOf(nsides));
        
        if ( rmin < 0 || rmin > rmax )
        {
            throw new IllegalArgumentException("rmin <" + rmin + "> is invalid.");
        }
        
        if ( rmax < 0 )
        {
            throw new IllegalArgumentException("rmax <" + rmax + "> is invalid.");
        }
        
        ZPlane zplane1 = new ZPlane(rmin,  rmax, -zlength/2);
        ZPlane zplane2 = new ZPlane(rmin, rmax, zlength/2);
        
        addContent(zplane1);
        addContent(zplane2);
    }
}