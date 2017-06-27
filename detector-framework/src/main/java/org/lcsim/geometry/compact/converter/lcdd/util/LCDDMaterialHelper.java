package org.lcsim.geometry.compact.converter.lcdd.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.material.XMLMaterialManager;

/**
 * This helper class is used to export data from an XMLMaterialManager to LCDD.
 * 
 * @author jeremym
 * @version $Id: LCDDMaterialHelper.java,v 1.2 2011/04/19 00:48:34 jeremy Exp $
 */
public class LCDDMaterialHelper
{
    XMLMaterialManager xmgr = null;
    
    /**
     * Ctor for helper which will use the given {@link org.lcsim.material.XMLMaterialManager}.
     * @param xmgr The XMLMaterialManager with material XML data.
     */
    public LCDDMaterialHelper(XMLMaterialManager xmgr)
    {
        this.xmgr = xmgr;
    }
    
    /**
     * Copy a MaterialElement node. 
     * @param e The XML element to copy.
     * @return The cloned XML element.
     * @throws JDOMException
     */
    private static org.jdom.Element makeMaterialElement(org.jdom.Element e)
    {
        org.jdom.Element ee = new org.jdom.Element("element");
        ee.addContent(e.cloneContent());
        ee.setAttribute("name", e.getAttributeValue("name"));
        ee.setAttribute("formula", e.getAttributeValue("formula"));
        ee.setAttribute("Z", e.getAttributeValue("Z"));
        return ee;
    }
    
    private static Material makeMaterial(org.jdom.Element m)
    {
        Material mat = new Material(m.getAttributeValue("name"));
        mat.addContent(m.cloneContent());
        return mat;
    }
           
    private void makeReferenceList(String matName, List refs)
    {
        if (refs == null)
        {
            throw new IllegalArgumentException("List cannot be null.");
        }

        org.jdom.Element m = xmgr.getMaterialXML(matName);

        if (m != null)
        {
            if ("material".equals(m.getName()))
            {
                List l = m.getChildren("composite");

                if (l.isEmpty())
                {
                    l = m.getChildren("fraction");
                }

                if (l.isEmpty())
                {
                    throw new RuntimeException("Material does not contain fraction or composite tags.");
                }

                for (org.jdom.Element ce : (List<org.jdom.Element>)l)
                {
                    String ref = ce.getAttributeValue("ref");

                    /* Add if element. */
                    org.jdom.Element fnd = xmgr.getMaterialElementXML(ref);
                    if (fnd != null)
                    {
                        if (!refs.contains(fnd))
                        {
                            refs.add(fnd);
                        }
                    }
                    /* Add if material. */
                    else
                    {
                        fnd = xmgr.getMaterialXML(ref);

                        /* Recursively add references of this material. */
                        if (fnd != null)
                        {
                            makeReferenceList(fnd.getAttributeValue("name"), refs);
                        }
                    }
                }

                /* Lastly, add the material that was passed in after its references are recursively resolved. */
                if (!refs.contains(m))
                {
                    refs.add(m);
                }
            }
            else
            {
                throw new IllegalArgumentException("makeReferenceList() - Material not found: " + matName);
            }
        }
    }
    
    // DEBUG
    private void printXMLMat()
    {
        System.out.println();
        System.out.println("XMLMatMgr mats ...");
        for (Object o : xmgr.getMaterialXMLMap().values())
        {                       
            org.jdom.Element e = (org.jdom.Element)o;
            System.out.println(e.getAttributeValue("name"));
        }
        System.out.println();
    }
    
    /** 
     * Copy all materials into an LCDD object for output.
     * @param lcdd The LCDD object. 
     */
    private void copyMaterialsToLCDD(LCDD lcdd) throws JDOMException
    {
        // DEBUG
        //printXMLMat();
        //
        
        for (Object o : xmgr.getMaterialXMLMap().values())
        {                       
            org.jdom.Element e = (org.jdom.Element)o;                      
            Material m = makeMaterial(e);
            lcdd.addMaterial(m);
        }
    }
    
    /**
     * Add references to materials from a compact document to this manager.
     * @param lccdd The compact detector.
     * @throws JDOMException
     */
    private void addReferencesFromCompact(org.jdom.Element lccdd)
    {
        // FIXME This fixes up problem with preloaded XMgr but is just kludge.
        this.xmgr.clearMaterialMap();
        
        if (lccdd == null)
        {
            throw new RuntimeException("The argument points to null.");
        }
        
        if (lccdd.getChild("materials") == null)
        {
            throw new RuntimeException("No materials section found in Element.");
        }
        
        org.jdom.Element materials = lccdd.getChild("materials");

        // FIXME Ugh!
        xmgr.setMaterialsRoot(materials);
                
        for (Iterator i = materials.getChildren().iterator(); i.hasNext();)
        {
            org.jdom.Element element = (org.jdom.Element)i.next();
            if ("material".equals(element.getName()))
            {
                addReferences(element);
            }
        }
    }
    
    /**
     * Recursively add the referenced materials or elements from a single material node. 
     * This method will also look in the parent XMLMaterialManager.  
     */
    // FIXME Affecting the state of XMatMgr with this method is a bad idea.
    private void addReferences(org.jdom.Element e, Document d)
    {
        if ("material".equals(e.getName()))
        {
            // Check for composite or fraction tags.
            List l = e.getChildren("composite");
            if (l.isEmpty())
            {
                l = e.getChildren("fraction");
            }
            
            if (l.isEmpty())
            {
                // Bad XML.  Required tags missing.
                throw new RuntimeException("Material does not contain required fraction or composite tags.");
            }

            for (org.jdom.Element ce : (List<org.jdom.Element>)l)
            {
                String ref = ce.getAttributeValue("ref");

                // Add if element.
                org.jdom.Element fnd = xmgr.findMaterialElementXML(ref, d);
                if (fnd != null)
                {
                    xmgr.addMaterialElementRef(fnd);
                }
                // Add if material.
                else
                {
                    fnd = xmgr.findMaterialXML(ref, d);

                    if (fnd != null)
                    {
                        // Recursively add references of this material.
                        addReferences(fnd, d);
                    }
                    else
                    {
                        throw new RuntimeException("Material or element reference not found: " + ref);
                    }
                }
            }
            
            // Add original material after refs resolved.
            xmgr.addMaterialRef(e);
        }       
    }

    /** 
     * Copy material data into LCDD.
     * @param lcdd The LCDD object. 
     **/
    public void copyToLCDD(LCDD lcdd) throws JDOMException
    {
        copyMaterialElementsToLCDD(lcdd);
        copyMaterialsToLCDD(lcdd);
    }
    
    /**
     * Copy material elements into LCDD.
     * @param lcdd The LCDD object.
     */
    private void copyMaterialElementsToLCDD(LCDD lcdd)
    {
        for (Object o : xmgr.getElementXMLMap().values())
        {
            org.jdom.Element e = (org.jdom.Element)o;
            lcdd.addElement((org.jdom.Element)e.clone());
        }
    }
    
    /**
     * Add material reference from a materials section.
     * @param e The element pointing to the materials section.
     */
    private void addReferences(org.jdom.Element e)
    {
        addReferences(e, xmgr.getCurrentDocument());
    }   
    
    /**
     * Load referenced material into this manager.
     * @param name The name of the material.
     * @param lcdd The LCDD object to update.
     */
    // FIXME This is called from LCDDDetector but not sure it should be.
    public void resolveLCDDMaterialReference(String name, LCDD lcdd)
    {
        List l = new ArrayList();

        makeReferenceList(name, l);

        for (Object o : l)
        {
            org.jdom.Element e = (org.jdom.Element)o;

            // Material.
            if ("material".equals(e.getName()))
            {
                Material m = makeMaterial(e);
                lcdd.addMaterial(m);
            }
            // Element.
            else
            {
                org.jdom.Element ee = makeMaterialElement(e);
                lcdd.addElement(ee);
            }
        }
    }
    
    public void copyToLCDD(Element compact, LCDD lcdd)
    {       
        try 
        {
            // Adds references from compact file to the XMLMaterialManager.
            addReferencesFromCompact(compact);
    
            // Copy materials used in the compact file to the LCDD file.
            copyToLCDD(lcdd);
        }
        catch (Exception x)
        {
            throw new RuntimeException(x);
        }
    }
}
