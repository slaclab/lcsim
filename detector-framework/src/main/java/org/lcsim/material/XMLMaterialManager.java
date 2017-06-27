package org.lcsim.material;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * This class loads GDML data from a materials XML section into the {@link MaterialManager}.  
 * 
 * It is also used to keep track of compact material data for the conversion to/from
 * compact descriptions.
 * 
 * To distinguish between XML and chemical "elements" in the code, I sometimes use the 
 * term "node" to refer to an XML element.
 * 
 * @author jeremym
 * @version $Id: XMLMaterialManager.java,v 1.23 2011/04/21 19:42:37 jeremy Exp $
 */
// TODO Refactor spaghetti/recursion code that does reference resolution.
// TODO Replace parent/child structure of managers with a single manager or utility class.
// TODO Move most of the logic for loading materials to the CompactReader class.
// TODO Simplify the find/get methods for looking up XML elements.  There are too many similar methods.
// TODO Where possible, move additional methods into LCDDMaterialHelper class.
// FIXME Should not keep references to XML nodes.  To copy from compact to LCDD,
// only the MaterialManager should be used to resolve references.  LCDD material XML 
// can be constructed from Material and MaterialElement objects on the fly rather than cloned.
// FIXME Deal with multiple unnecessary calls to setup() from clients.
public class XMLMaterialManager
{    
    // Location of elements resource in jar relative to org.lcsim.material pkg.
    private static final String elementResource = "elements.xml";

    /// Location of materials resource in jar relative to org.lcsim.material pkg.
    private static final String materialResource = "materials.xml";
    
    // Instance of MaterialManager.
    private static MaterialManager mgr;
            
    // Static instance containing elements data.
    private static XMLMaterialManager elements;

    // Static instance containing materials data.
    private static XMLMaterialManager materials;

    // Parent manager of this one. 
    private XMLMaterialManager parent;

    // Current XML document known by this manager.
    private Document currentDoc;

    // Map of names to material XML nodes known by this manager.
    private HashMap<String, org.jdom.Element> materialMap = new LinkedHashMap<String, Element>();

    // Map of names to material element XML nodes known by this manager.
    private HashMap<String, org.jdom.Element> elementMap = new LinkedHashMap<String, Element>();
    
    /**
     * Setup static material data.  This can be called to reset material data between runs
     * with different Detectors that are run in the same Java process, such as a TestCase.  
     * This method must be called before the XMLMaterialManager is created for a compact document.
     */    
    public static void setup()
    {        
        // Clear the global material store.
        MaterialManager.instance().clear();
        mgr = MaterialManager.instance();
                        
        // Rebuild the default material data stores.
        elements = createMaterialElements();
        materials = createMaterials();        
    }
    
    /**
     * Ctor for a manager with the given root XML node, probably pointing to the
     * materials section of a compact detector description.  Its parent is the default 
     * material manager.
     * @param materialsRoot The XML element pointing to a materials section.
     */
    public XMLMaterialManager(Element materialsRoot)
    {
        // Bootstrap if static data is not setup yet.
        if (elements == null)
        {
            setup();
        }
        parent = getDefaultMaterialManager();
        setMaterialsRoot(materialsRoot);
        loadMaterialsFromXML(materialsRoot);
    }
               
    /** 
     * Get manager with default materials defined in the embedded resource file. 
     * @return The manager with references to the default material manager.    
     **/        
    public static XMLMaterialManager getDefaultMaterialManager()
    {
        // Bootstrap if static data is not setup yet.
        if (elements == null)
        {
            setup();
        }
        return materials;
    }    
    
    /** 
     * Get the manager with chemical element data.
     * @return The manager with default chemical elements data.    
     **/        
    public static XMLMaterialManager getElementsManager()
    {
        // Bootstrap if static data is not setup yet.
        if (elements == null)
        {
            setup();
        }
        return elements;
    }
         
    /**
     * Get the map of XML material nodes known by this manager.
     * @return The map of names to material nodes.
     */
    public Map<String, Element> getMaterialXMLMap()
    {
        return materialMap;
    }
    
    /**
     * Get the map of XML chemical element nodes known by this manager.
     * @return The map of names to chemical element nodes.
     */
    public Map<String, Element> getElementXMLMap()
    {
        return elementMap;
    }
    
    /** 
     * Add a material XML node to the lookup map.  
     * Does not create a Material object.
     * @param e The XML node to add. 
     **/
    public void addMaterialRef(org.jdom.Element e)
    {
        materialMap.put(e.getAttributeValue("name"), e);     
    }

    /** 
     * Add a material element XML node to the lookup map.
     * Does not create a MaterialElement.
     * @param e The XML ndoe to add. 
     **/
    public void addMaterialElementRef(org.jdom.Element e)
    {
        elementMap.put(e.getAttributeValue("name"), e);
    }

    /** 
     * Find the XML element for a material.
     * @return The XML element with name <code>matName</code> or null if doesn't exist.    
     */
    // FIXME Move to LCDDMatHelp
    public org.jdom.Element getMaterialXML(String matName)
    {
        org.jdom.Element m = materialMap.get(matName);

        if (m == null)
        {
            if (hasParentManager())
            {
                m = parent.getMaterialXML(matName);
            }
        }

        return m;
    }    

    /** 
     * Find the XML node for a chemical element.
     * @param elemName The name of the chemical element.
     * @return The XML element with name <code>elemName</code> or null if doesn't exist.    
     */
    public org.jdom.Element getMaterialElementXML(String elemName)
    {
        org.jdom.Element e = elementMap.get(elemName);

        if (e == null)
        {
            if (hasParentManager())
            {
                e = parent.getMaterialElementXML(elemName);
            }
        }

        return e;
    }

    /** 
     * Find a chemical element in a document.
     * @return The XML node of the chemical element called <code>elemName</code>. 
     **/
    public Element findMaterialElementXML(String elemName, Document d)
    {        
        org.jdom.Element me = getMaterialsRoot(d);
        org.jdom.Element fnd = null;
        for (Object o : me.getChildren("element"))
        {
            org.jdom.Element curr = (org.jdom.Element)o;
            if (curr.getAttributeValue("name").equals(elemName))
            {
                fnd = curr;
                break;
            }
        }

        // Try the parent.
        if (fnd == null && hasParentManager())
        {
            fnd = parent.findMaterialElementXML(elemName);
        }

        return fnd;
    }
    
    /**
     *  Get the current XML document known by this manager.
     *  @return The current XML document. 
     **/
    public Document getCurrentDocument()
    {
        return currentDoc;
    }
    
    /** 
     * Find a material in an XML document.
     * @param matName The name of the material.
     * @param d The document to search.
     * @return The node for the material called <code>matName</code> or null if not found. 
     **/
    public org.jdom.Element findMaterialXML(String matName, Document d)
    {
        org.jdom.Element me = getMaterialsRoot(d);

        org.jdom.Element fnd = null;
        for (Object o : me.getChildren("material"))
        {
            org.jdom.Element curr = (org.jdom.Element)o;
            if (curr.getAttributeValue("name").equals(matName))
            {
                fnd = curr;
                break;
            }
        }

        // Look in parent manager.
        if (fnd == null && hasParentManager())
        {
            fnd = parent.findMaterialXML(matName);
        }

        return fnd;
    }
            
    /**
     * Create the static instance of the elements manager.
     * @return The manager containing elements data.
     */
    static private XMLMaterialManager createMaterialElements()
    {
        elements = new XMLMaterialManager();
        elements.loadElementsFromResource();
        return elements; 
    }
    
    /**
     * Create the static instance of the material manager.
     * @return The manager containing default materials data.
     */
    static private XMLMaterialManager createMaterials()
    {
        materials = new XMLMaterialManager(elements);
        materials.loadMaterialsFromResource(materialResource);
        return materials;
    }
              
    /**
     * The default ctor should not be used.  Any "user" managers
     * must have a parent and/or a node pointing to a materials section.
     */
    private XMLMaterialManager()
    {}
          
    /** 
     * Ctor for XMLMaterialFactory with given parent and no initial material data.
     * @param p The parent manager. 
     **/
    private XMLMaterialManager(XMLMaterialManager p)
    {
        parent = p;
    }
   
    /** 
     * Set current document source.
     * @param d The Document with a materials node as its child.
     **/    
    private void setCurrentDocument(Document d)
    {
        currentDoc = d;
    }

    /** 
     * Check if this object has a parent manager.
     * @return True if this manager has a parent; false if not. 
     **/
    protected boolean hasParentManager()
    {
        return (parent != null);
    }

    /**
     * Attempt to find the materials section in a document using two possible cases.
     * 
     * 1) The materials section is the root element. 
     * 2) The materials section is a child of the root element.
     * 
     * @param d Document to search for materials element.     
     */           
    // FIXME This should be removed.  Callers should have the materials node in hand.
    private static org.jdom.Element getMaterialsRoot(Document d)
    {        
        org.jdom.Element m = null;

        if (d.getRootElement().getName() == "materials")
        {
            m = d.getRootElement();
        }
        else
        {
            m = d.getRootElement().getChild("materials");
        }

        return m;
    }
 
    /**
     * Find the XML node for the material or element called <code>elemName</code>
     * in the current Document. 
     * @param elemName The name attribute value of the node.
     * @return The XML node with attribute name of <code>elemName</code> or null if not found.
     * @throws JDOMException
     */
    private org.jdom.Element findMaterialElementXML(String elemName)
    {
        return findMaterialElementXML(elemName, currentDoc);
    }
   
    /** 
     * Find a material in the manager's document.
     * @param matName The name of the material.
     * @return The node for the material called <code>matName</code> or null if not found. 
     **/
    private org.jdom.Element findMaterialXML(String matName)
    {
        return findMaterialXML(matName, currentDoc);
    } 
     
    /**
     * Create a new document from a materials section.
     * @param e The XML element of the materials section.
     * @return The document containing the materials section.
     */
    private Document cloneMaterialsRoot(org.jdom.Element e)
    {
        org.jdom.Element matRoot = new org.jdom.Element("materials");
        matRoot.setContent(e.cloneContent());
        Document d = new Document();
        d.setRootElement(matRoot);
        setCurrentDocument(d);
        return currentDoc;
    }

    /**
     * Set the XML element containing the materials section for this manager.
     * @param e The XML element pointing to a materials section.
     */
    public void setMaterialsRoot(org.jdom.Element e)
    {
        setCurrentDocument(cloneMaterialsRoot(e));
    }
               
    /**
     * Load the chemical elements data from the embedded resource file into this manager.
     */
    // FIXME: Duplicates some code for creating materials in makeMaterialFromXML().
    private void loadElementsFromResource()
    { 
        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        try
        {
            doc = builder.build(XMLMaterialManager.class.getResourceAsStream(elementResource));
            setCurrentDocument(doc);
        }
        catch (JDOMException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        Element root = doc.getRootElement();
        
        setMaterialsRoot(root);

        Map<String, Element> elementNodes = new HashMap();

        // Loop over materials and elements.
        for (Object x : root.getChildren())
        {
            Element node = (Element)x;

            // Don't actually build elements but keep references for corresponding materials.
            if (node.getName() == "element")
            {
                elementNodes.put(node.getAttributeValue("name"), node);
                
                // Add element to lookup map in this manager.
                addMaterialElementRef(node);
            }
            // This code block handles the simple case of a material element
            // with a corresponding material. 
            if (node.getName() == "material")
            {
                // Add material to lookup map.
                addMaterialRef(node);

                // FIXME: Rest of method duplicates code in makeMaterialFromXML().
                Element comp = node.getChild("composite");
                String elemRef = comp.getAttributeValue("ref");
                Element elemNode = elementNodes.get(elemRef);

                if (elemNode == null)
                    throw new RuntimeException("Could not find element " + elemRef + " in map.");

                String formula = elemNode.getAttributeValue("name");
                double Z = Double.valueOf(elemNode.getAttributeValue("Z"));
                Element anode = elemNode.getChild("atom");
                double A = Double.valueOf(anode.getAttributeValue("value"));

                Element radlenNode = node.getChild("RL");
                double radlen = Double.valueOf(radlenNode.getAttributeValue("value"));
                Element nilNode = node.getChild("NIL");
                double nil = Double.valueOf(nilNode.getAttributeValue("value"));

                Element densNode = node.getChild("D");
                double density = Double.valueOf(densNode.getAttributeValue("value"));

                String fullName = node.getAttributeValue("name");

                MaterialState state = MaterialState.fromString(node.getAttributeValue("state"));
 
                new org.lcsim.material.Material(fullName, formula, Z, A, density, radlen, nil, state);
            }
        }               
    }  
    
    /**
     * Load materials from an embedded resource in the jar file.
     * @param resource Resource location string.
     */
    private void loadMaterialsFromResource(String resource)
    {
        // Build the materials document.
        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        try
        {
            doc = builder.build(XMLMaterialManager.class.getResourceAsStream(resource));
        }
        catch (JDOMException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
        // Set the root materials element.
        Element root = doc.getRootElement();
        setMaterialsRoot(root);
        
        // Load the Materials from the materials section.
        loadMaterialsFromXML(root);
    }  
    
    /**    
     * Creates lcsim materials from an XML element pointing to a GDML materials section.      
     * @param materialsRoot The GDML materials section.
     */
    private void loadMaterialsFromXML(Element materialsRoot)
    {          
        for (Object o : materialsRoot.getChildren("material"))
        {
            makeMaterialFromXML((Element)o);
        }        
    }

    /**
     * Create an lcsim Material from XML.
     * @param matNode The XML Element pointing to a material node.
     */
    private void makeMaterialFromXML(Element matNode)
    {                
        // Get the name of the material.
        String name = matNode.getAttributeValue("name");
                                        
        // Check if material with this name already exists.
        if (mgr.getMaterial(name) != null)
        {                       
            // This could be okay, e.g. if a material was moved into default material file
            // and not removed from compact file.  Print a warning in case something strange is happening.
            System.out.println("WARNING: Ignoring attempt to add material " + name + " that already exists!");
            return;
            //throw new RuntimeException("Attempting to add material " + name + " that already exists!");
        }
        
        // Add material reference in this XMLMatMgr.
        addMaterialRef(matNode);
        
        // Get the density element and convert it.
        Element densNode = matNode.getChild("D");
        if (densNode == null)
        {
            throw new RuntimeException("Missing required D element in material " + name + ".");
        }        
        double density = Double.valueOf(densNode.getAttributeValue("value"));
        
        // Get the state element and convert it.
        MaterialState state = MaterialState.UNKNOWN;
        if (matNode.getAttribute("state") != null)
        {
            String stateStr = matNode.getAttributeValue("state");
            state = MaterialState.fromString(stateStr);
        }
        
        // Check for either list of mass fractions or composites.
        List fractions = matNode.getChildren("fraction");
        List composites = matNode.getChildren("composite");
        
        // Reference to new material that will be created.
        org.lcsim.material.Material mat = null;
        
        // Figure out number of components depending on the type of definition.
        int ncomp = 0;
        if (fractions.size() > 0 && composites.size() > 0)
        {
            throw new RuntimeException("Cannot mix fraction and composite tags in material " + name + ".");
        }
        else if (fractions.size() > 0)
        {
            ncomp = fractions.size();
        }
        else
        {
            ncomp = composites.size();
        }   
        
        // Define the new material.
        mat = new org.lcsim.material.Material(name, ncomp, density, state);
        
        // Defined as fractions of material adding to 1.0.
        if (matNode.getChildren("fraction").size() != 0)
        {                                                
            // Add the mass fractions to the Material.
            for (Object oo : fractions)
            {
                Element fracNode = (Element)oo;
                double frac = Double.valueOf(fracNode.getAttributeValue("n"));
                String ref = fracNode.getAttributeValue("ref");
                MaterialElement elem = mgr.getElement(ref);
                if (elem != null)
                {
                    mat.addElement(elem, frac);
                }
                else
                {
                    org.lcsim.material.Material matFrac = mgr.getMaterial(ref);
                    if (matFrac == null)
                    {
                        throw new RuntimeException("Could not resolve ref to " + ref + ".");
                    }
                    mat.addMaterial(matFrac, frac);
                }                        
            }
        }
        // Defined as composite by number of atoms of chemical elements.
        else if (matNode.getChildren("composite").size() != 0)
        {
            // Add the composites to the Material.
            for (Object oo : composites)
            {
                Element compNode = (Element)oo;
                int n = Integer.valueOf(compNode.getAttributeValue("n"));
                String ref = compNode.getAttributeValue("ref");
                MaterialElement elem = mgr.getElement(ref);
                if (elem == null)
                {
                    throw new RuntimeException("Could not find referenced element " + ref + ".");
                }
                mat.addElement(elem, n);
            }
        }
        // The Material is missing required fraction or composite elements.
        else
        {
            throw new RuntimeException("Missing at least one fraction or composite element in material " + name + ".");
        }
    }    

    // !!!!!!!!!!!
    public void clearMaterialMap()
    {
        this.materialMap.clear();
    }
}