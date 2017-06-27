package org.lcsim.geometry.compact;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.geometry.util.BaseIDDecoder;
import org.lcsim.geometry.util.IDDescriptor;
import org.lcsim.geometry.util.IDDescriptor.IDException;

/**
 * This class implements the compact readout for subdetectors.
 * @author tonyj
 * @author Jeremy McCormick
 * @version $Id: $
 */
public class Readout implements org.lcsim.geometry.Readout {
    
    private String name;
    private IDDecoder decoder;
    private IDDescriptor desc;
    private boolean hasSegmentation;
    private List<String> processorNames = new ArrayList<String>();
    private List<String> collectionNames = new ArrayList<String>();

    protected Readout(Element node) throws JDOMException {
        name = node.getAttributeValue("name");
        setupIDDescriptor(node);
        
        /* Add list of processor names. */
        addProcessorNames(node);
        
        /* Add list of collection names. */
        addCollectionNames(node);
    }

    private void addProcessorNames(Element node) {
        List processors = node.getChildren("processor");
        Iterator iterator = processors.iterator();
        while (iterator.hasNext()) {
            Element processor = (Element) iterator.next();
            processorNames.add(new String(processor.getAttributeValue("type")));
        }
    }
    
    private void addCollectionNames(Element node) {
        List processors = node.getChildren("collection");
        Iterator iterator = processors.iterator();
        while (iterator.hasNext()) {
            Element collection = (Element) iterator.next();
            collectionNames.add(new String(collection.getAttributeValue("name")));
        }
    }

    /** Create a new IDDescriptor by parsing the CDATA from the <id> tag. */
    private void setupIDDescriptor(Element node) throws JDOMException {
        try {
            desc = new IDDescriptor(node.getChild("id").getTextTrim());
        } catch (IDException x) {
            throw new JDOMException("Invalid ID", x);
        }
    }

    public Segmentation getSegmentation() {
        return hasSegmentation ? (Segmentation) decoder : null;
    }

    public void setSegmentation(Segmentation segmentation) {
        setIDDecoder(segmentation);
        hasSegmentation = true;
    }

    public String getName() {
        return name;
    }

    public org.lcsim.geometry.IDDecoder getIDDecoder() {
        return decoder;
    }

    protected void setIDDecoder(IDDecoder d) {
        if (d == null)
            throw new IllegalArgumentException("IDDecoder was null.");
        decoder = d;
        ((BaseIDDecoder) decoder).setIDDescription(getIDDescriptor());
    }

    public IDDescriptor getIDDescriptor() {
        return desc;
    }
    
    public List<String> getProcessorNames() {
        return processorNames;
    }
    
    public List<String> getCollectionNames() {
        return collectionNames;
    }    
}