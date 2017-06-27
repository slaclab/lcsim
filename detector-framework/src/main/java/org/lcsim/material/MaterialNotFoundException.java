package org.lcsim.material;

/**
 *
 * @author jeremym
 * @version $Id: MaterialNotFoundException.java,v 1.3 2011/03/11 19:22:20 jeremy Exp $
 */
public final class MaterialNotFoundException extends Exception
{
    MaterialNotFoundException(String materialName)
    {
        super("Material was not found: " + materialName);                
    }
}