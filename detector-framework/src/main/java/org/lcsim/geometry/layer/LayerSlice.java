package org.lcsim.geometry.layer;

import org.lcsim.material.Material;
import org.lcsim.material.MaterialManager;
import org.lcsim.material.MaterialNotFoundException;

/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: LayerSlice.java,v 1.12 2011/03/11 19:22:20 jeremy Exp $
 */
public class LayerSlice
{
    private boolean sensitive;
    private Material material = null;
    private double thickness;

    public LayerSlice()
    {
        material = null;
        thickness = 0.0;
        sensitive = false;
    }

    public LayerSlice(String matName, double w, boolean sens)
    {

        material = MaterialManager.instance().getMaterial(matName);
        if (material == null)
        {
            throw new RuntimeException("The material " + matName + " was not found.");
        }

        thickness = w;
        sensitive = sens;
    }

    private LayerSlice(Material m, double w, boolean sens)
    {
        if (m == null)
        {
            throw new IllegalArgumentException("Material argument cannot be null.");
        }

        material = m;
        thickness = w;
        sensitive = sens;
    }

    public Material getMaterial()
    {
        return material;
    }

    public double getThickness()
    {
        return thickness;
    }

    public boolean isSensitive()
    {
        return sensitive;
    }

    public void setMaterial(Material m)
    {
        if (m == null)
        {
            throw new IllegalArgumentException("Material argument to LayerSlice is null!");
        }
        material = m;
    }

    public void setThickness(double t)
    {
        if (t < 0.0)
        {
            throw new IllegalArgumentException("Thickness cannot be < 0.0");
        }
        thickness = t;
    }

    public void setIsSensitive(boolean s)
    {
        sensitive = s;
    }
}
