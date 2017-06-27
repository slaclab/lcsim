package org.lcsim.material;

import org.jdom.DataConversionException;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.Constant;

/**
 *
 * Convert a GDML material to org.lcsim.Material implementation class.
 * @author jeremym
 */
public class MaterialFromGDMLCnv
{
    LCDD _lcdd;

    /** Creates a new instance of MaterialCnv */
    public MaterialFromGDMLCnv(LCDD lcdd)
    {
        _lcdd = lcdd;
        MaterialManager.instance();
    }

    public MaterialFromGDMLCnv()
    {
        _lcdd = null;
        MaterialManager.instance();
    }

    public void setLCDD(LCDD lcdd)
    {
        _lcdd = lcdd;
    }

    /**
     * Make an org.lcsim.material.Material object from a GDML material tag.
     * 
     * Resolve references outside materials tag (e.g. within define tag) using the lcdd parameter (which is allowed to
     * be null).
     * 
     */
    public Material makeMaterial(org.jdom.Element materialNode, LCDD lcdd) throws JDOMException
    {
        //System.out.println("makeMaterial - " + materialNode.getName() + " : " + materialNode.getAttributeValue("name"));
        MaterialManager mgr = MaterialManager.instance();

        _lcdd = lcdd;

        Material material = null;
        String name = materialNode.getAttributeValue("name");
        //String formula = materialNode.getAttributeValue("formula");
        MaterialState state = MaterialState.fromString(materialNode.getAttributeValue("state"));

        double density;

        /// Allow the exception to propagate if D or Dref is not found.
        density = getDensity(materialNode);

        MaterialDefinitionType mdt = MaterialDefinitionType.getMaterialDefinitionType(materialNode);

        if (mdt == MaterialDefinitionType.INVALID)
        {
            throw new RuntimeException("Material definition type was not valid.");
        }

        String tagname = mdt.getTagName();
        
        // FIXME Is this needed?
        /*
         * if (mdt == MaterialDefinitionType.ATOM) { // System.out.println("atom def");
         * 
         * double A = materialNode.getChild(tagname).getAttribute("value").getDoubleValue(); double Z = 0; if
         * (materialNode.getAttribute("Z") != null) { Z = materialNode.getAttribute("Z").getDoubleValue(); } else {
         * throw new JDOMException("Required Z value missing for atom definition of material."); }
         * 
         * material = new Material(name, Z, A, density, state); } else {
         */
        // System.out.println("fraction or composite def");

        int nComponents = 0;

        // System.out.println("tagname: " + tagname);

        nComponents = materialNode.getChildren(tagname).size();

        // System.out.println("ncomp: " + nComponents);

        material = new Material(name, nComponents, density, state);

        /* Fill in composite. */
        if (mdt == MaterialDefinitionType.COMPOSITE)
        {
            // System.out.println("composite def");

            for (Object o : materialNode.getChildren(tagname))
            {
                org.jdom.Element compositeElement = (org.jdom.Element)o;
                int n = compositeElement.getAttribute("n").getIntValue();
                String elementName = compositeElement.getAttributeValue("ref");

                MaterialElement me = mgr.getElement(elementName);

                if (me != null)
                {
                    material.addElement(me, n);
                }
                else
                {
                    throw new RuntimeException("MaterialElement was not defined in MaterialManager: " + elementName);
                }
            }
        }
        /* Fill in mass fraction. */
        else
        {
            // System.out.println("fraction def");
            for (Object o : materialNode.getChildren(tagname))
            {
                org.jdom.Element fractionElement = (org.jdom.Element)o;

                // System.out.println(tagname + " " + fractionElement.getAttributeValue("ref"));

                String refName = fractionElement.getAttributeValue("ref");
                double f = fractionElement.getAttribute("n").getDoubleValue();

                MaterialElement me = mgr.getElement(refName);

                if (me != null)
                {
                    // System.out.println("adding element: " + refName);
                    material.addElement(me, f);
                }
                else
                {
                    Material m = MaterialManager.instance().getMaterial(refName);

                    if (m != null)
                    {
                        material.addMaterial(m, f);
                    }
                    else
                    {
                        throw new JDOMException("fraction ref is undefined: " + refName);
                    }
                }
            }
        }
        // }

        // material.setFormula(formula);

        return material;
    }

    /** Get density from D or Dref tag. */
    public double getDensity(org.jdom.Element materialElement) throws JDOMException
    {
        return getValueFromAttributeOrRef(materialElement, "D", _lcdd);
    }

    /** Get pressure from P of Pref tag. */
    public double getPressure(org.jdom.Element materialElement) throws JDOMException
    {
        return getValueFromAttributeOrRef(materialElement, "P", _lcdd);
    }

    /** Get temperature from T or Tref tag. */
    public double getTemperature(org.jdom.Element materialElement) throws JDOMException
    {
        return getValueFromAttributeOrRef(materialElement, "T", _lcdd);
    }

    public double getValueFromAttributeOrRef(org.jdom.Element materialElement, String tagname, LCDD lcdd) throws DataConversionException, JDOMException
    {
        boolean haveLCDD = (lcdd == null);

        org.jdom.Element tag = materialElement.getChild(tagname);

        double value = 0;

        /* FIXME: Use the unit attribute. */
        if (tag != null)
        {
            if (tag.getAttribute("value") != null)
            {
                value = tag.getAttribute("value").getDoubleValue();
            }
            else
            {
                throw new JDOMException("Missing value attribute for " + tagname + " in material " + materialElement.getAttributeValue("name"));
            }
        }
        else if (haveLCDD)
        {
            org.jdom.Element tagref = materialElement.getChild(tagname);

            if (tagref != null)
            {
                String ref = tagref.getAttributeValue("ref");

                Constant constant = lcdd.getDefine().getConstant(ref);

                if (constant != null)
                {
                    value = constant.getConstantValue();
                }
                else
                {
                    throw new JDOMException(tagname + "ref's ref attribute does not refer to a defined constant: " + ref);
                }
            }
            else
            {
                throw new JDOMException(tagname + " or " + tagname + "ref was not provided by the material: " + materialElement.getAttributeValue("name"));
            }
        }
        else
        {
            throw new JDOMException("No LCDD object to resolve the " + tagname + "ref.");
        }

        return value;
    }
}
