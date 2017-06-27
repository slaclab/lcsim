package org.lcsim.recon.tracking.trfmaterial;

/**
 *
 * @author Norman A Graf
 *
 * @version $Id: Material.java 3377 2014-10-20 21:26:08Z ngraf $
 * 
 * =================================================================================================
 * Repository path: $HeadURL: svn://svn.freehep.org/lcdet/projects/lcsim/trunk/trf/src/main/java/org/lcsim/recon/tracking/trfmaterial/Material.java $ 
 * Last committed: $Revision: 3377 $ 
 * Last changed by: $Author: ngraf $ 
 * Last changed date: $Date: 2014-10-20 14:26:08 -0700 (Mon, 20 Oct 2014) $ 
 * ID: $Id: Material.java 3377 2014-10-20 21:26:08Z ngraf $
 * =================================================================================================
 *
 */
public class Material {
    double _thickness; // Length of the material [cm]
    double _radLength; // Radiation length [cm]
    double _density; // Density [g/cm^3]
    double _Z; // Atomic number
    double _A; // Atomic mass 
    String _name; // Name of material

    /*
     * @return thickness of the material
     */
    double thickness()
    {
        return _thickness;
    }

    /*
     * @return Radiation length
     */
    double radLength()
    {
        return _radLength;
    }

    /*
     * @return Density
     */
    double density()
    {
        return _density;
    }

    /*
     * @return Atomic number
     */
    double Z()
    {
        return _Z;
    }

    /*
     * @return Atomic mass
     */
    double A()
    {
        return _A;
    }

    String name()
    {
        return _name;
    }

    /*
     * Sets length of the material
     */
    void setThickness(double length)
    {
        _thickness = length;
    }

    /*
     * Sets radiation length of the material
     */
    void setRadLength(double rl)
    {
        _radLength = rl;
    }

    /*
     * Sets density
     */
    void setDensity(double rho)
    {
        _density = rho;
    }

    /*
     * Sets atomic number
     */
    void setZ(double Z)
    {
        _Z = Z;
    }

    /*
     * Sets atomic mass
     */
    void setA(double A)
    {
        _A = A;
    }

    void setName(String name)
    {
        _name = name;
    }

    /*
     * @return String representation of the class
     */
    public String toString()
    {
        StringBuffer ss = new StringBuffer();
        ss.append("MaterialInfo: length=" + _thickness + " rl=" + _radLength
                + " rho=" + _density + " Z=" + _Z + " A=" + _A //+ " zpos=" + fZpos
                + " name=" + _name + "\n");
        return ss.toString();
    }  
}
