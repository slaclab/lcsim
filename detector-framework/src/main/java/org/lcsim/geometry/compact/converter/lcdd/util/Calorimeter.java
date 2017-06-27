package org.lcsim.geometry.compact.converter.lcdd.util;

/**
 * 
 * @author tonyj
 */
public class Calorimeter extends SensitiveDetector {

    public Calorimeter(String name) {
        super("calorimeter", name);
    }

    /**
     * Constructor for subclasses with a different XML element name.
     * 
     * @param elementName The name of the element.
     * @param name The unique name of the calorimeter.
     */
    protected Calorimeter(String elementName, String name) {
        super(elementName, name);
    }

    public void setSegmentation(Segmentation segmentation) {
        addContent(segmentation);
    }
}
