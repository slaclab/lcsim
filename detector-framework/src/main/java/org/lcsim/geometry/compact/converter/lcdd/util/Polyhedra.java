 package org.lcsim.geometry.compact.converter.lcdd.util;

import java.util.List;

public class Polyhedra extends Solid
{   
    public Polyhedra(String name, int nsides, List<ZPlane> zplanes)
    {
        super("polyhedra", name);
        
        setAttribute("startphi", String.valueOf(0));
        setAttribute("deltaphi", String.valueOf(Math.PI * 2) );
        setAttribute("numsides", String.valueOf(nsides));
                
        for (ZPlane zplane : zplanes)
        {
            addContent(zplane);
        }
    }
}