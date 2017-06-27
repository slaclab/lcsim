package org.lcsim.material;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is the repository for Material and MaterialElement objects loaded into LCSim. 
 * It is a singleton with only one existing per process.
 * @author jeremym
 * @version $Id: MaterialManager.java,v 1.14 2011/03/11 19:22:20 jeremy Exp $
 */
// TODO Figure out best place to reset when new Detector is defined (geometry.Detector?  conditions?  event?)
public class MaterialManager
{
    // The global manager instanec.
    private static MaterialManager _instance = null;

    // FIXME These should not be static. Use mgr.instance() instead.
    private static Map<String, Material> _materials = new HashMap<String, Material>();
    private static Map<String, MaterialElement> _elements = new HashMap<String, MaterialElement>();

    /** 
     * Direct instantiation is not allowed.  Use the {@link instance()} method. 
     **/
    private MaterialManager()
    {}

    /**
     * Get manager and create if necessary.
     * @return The global MaterialManager.
     */
    public static MaterialManager instance()
    {
        if (_instance == null)
        {
            _instance = new MaterialManager();
        }

        return _instance;
    }
    
    public List<String> getMaterialNames()
    {
        // TODO Make list immutable.
        return new ArrayList<String>(_materials.keySet());
    }
    
    public List<String> getElementNames()
    {
        // TODO Make list immutable.
        return new ArrayList<String>(_elements.keySet());
    }

    // FIXME Remove this method.  Use getMaterialNames() with getMaterial() instead.
    public Map<String, Material> materials()
    {
        return _materials;
    }
  
    // FIXME Remove this method.  Use getElementNames() with getElement() instead.
    public static Map<String, MaterialElement> elements()
    {
        return _elements;
    }
  
    protected void addMaterial(Material material)
    {
        if (material == null)
        {
            throw new IllegalArgumentException("Argument points to null.");
        }

        if (getMaterial(material.getName()) == null)
        {
            _materials.put(material.getName(), material);
        }
        else
        {
            System.err.println("Material with name " + material.getName() + " already exists so addMaterial is ignored!");
        }
    }

    /**
     * Get a material from the manager.
     * @param materialName
     * @return The material with name materialName. 
     */
    public Material getMaterial(String materialName)
    {
        return _materials.get(materialName);
    }

    public MaterialElement getElement(String elementName)
    {
        return _elements.get(elementName);
    }

    protected void addElement(MaterialElement me)
    {
        if (MaterialManager.instance().getElement(me.getName()) == null)        
        {
            _elements.put(me.getName(), me);
        }
        else
        {
            // throw new RuntimeException("MaterialElement already exists: " + me.getName());
            System.err.println("Element " + me.getName() + " already exists so addElement is ignored!");
        }
    }
    
    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        for (MaterialElement me : _elements.values())
        {
            buff.append(me.toString());
        }
        for (Material m : _materials.values())
        {
            buff.append(m.toString());
        }
        return buff.toString();
    }

    public void printMaterials(PrintStream ps)
    {
        for (Material m : materials().values())
        {
            ps.println(m.toString());
        }
    }

    public void printElements(PrintStream ps)
    {
        for (MaterialElement me : elements().values())
        {
            ps.println(me.toString());
        }
    }
    
    /**
     * Reset the MaterialManager instance between runs.  This is currently
     * called from XMLMaterialManager only.
     */
    void clear()
    {
        // Reset maps.
        _materials = new HashMap<String, Material>();
        _elements = new HashMap<String, MaterialElement>();
        
        // Set instance to null;
        _instance = null;
    }
}