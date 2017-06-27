package org.lcsim.geometry.subdetector;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.detector.IRotation3D;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.RotationPassiveXYZ;
import org.lcsim.detector.Transform3D;
import org.lcsim.detector.Translation3D;

/**
 * This class represents a test beam detector that can be positioned and rotated
 * in global coordinates.  There are seperate, concrete implementations for calorimeters
 * and trackers.
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: AbstractTestBeam.java,v 1.13 2013/02/13 10:47:50 grefe Exp $
 */
public abstract class AbstractTestBeam extends AbstractLayeredSubdetector {

    /* Envelope volume dimensions. */
    double dimensionX;
    double dimensionY;
    double dimensionZ;
    double radius;
    
    /* Default number of sides. */
    int DEFAULT_NSIDES = 4;

    /* The position and rotation in global space. */
    Hep3Vector position;    
    IRotation3D rotation;
    ITransform3D transform;

    /**
     * The constructor taking an XML element.
     * @param node The XML element for the detector in the compact description.
     * @throws JDOMException If XML parsing fails.
     */
    public AbstractTestBeam(Element node) throws JDOMException {
        super(node);
        build(node);
    }

    /**
     * Build the detector from XML.
     * @param node The XML node.
     * @throws JDOMException if there is a problem parsing parameters.
     */
    private void build(Element node) throws JDOMException {
        
        /* Get settings for detector dimensions. */
        Element dimensions = node.getChild("dimensions");

        /* Compute the dimensions of the detector. */
        computeDimensions(dimensions);

        /* Compute the outer radius from the dimensions. */
        radius = computeRadius();

        /* Set the Cartesian position of the test beam detector. */
        setPosition(node);
        
        /* Set the rotation of the detector. */
        setRotation(node);
        
        /* Set the transform combining rotation and position. */
        setTransform();
    }

    /**
     * Set the transform combining the position and rotation.
     */
    private void setTransform() {
        transform = new Transform3D(new Translation3D(position), rotation);
    }

    /**
     * Return the width in X.
     * @return width in X
     */
    public double getDimensionX() {
        return dimensionX;
    }

    /**
     * Return the width in Y.
     * @return width in Y
     */
    public double getDimensionY() {
        return dimensionY;
    }

    /**
     * Return the width in Z.
     * @return width in Z
     */
    public double getDimensionZ() {
        return dimensionZ;
    }

    /**
     * Return the position of the detector in global coordinates.
     * @return the position in global coordinates
     */
    public Hep3Vector getPosition() {
        return position;
    }
    
    /**
     * Return the rotation of the detector in global coordinates.
     * @return the rotation of the detector
     */
    public IRotation3D getRotation() {
        return rotation;
    }

    /**
     * Create an outline of the detector's envelope volume in HepRep format for display in Wired.
     */
    public void appendHepRep(HepRepFactory factory, HepRep heprep) {
        
        HepRepInstanceTree instanceTree = heprep.getInstanceTreeTop("Detector", "1.0");
        HepRepTypeTree typeTree = heprep.getTypeTree("DetectorType", "1.0");

        HepRepType ec = typeTree.getType("Endcap");
        HepRepType type = factory.createHepRepType(ec, getName());

        // Set color.
        type.addAttValue("color", getVisAttributes().getColor());

        // Set shape.
        type.addAttValue("drawAs", "Prism");

        HepRepInstance instance = factory.createHepRepInstance(instanceTree, type);
      
        /* x points */
        double x1 = getDimensionX() / 2;
        double x2 = -getDimensionX() / 2;

        /* y points */
        double y1 = getDimensionY() / 2;
        double y2 = -getDimensionY() / 2;

        /* z points */
        double z1 = getDimensionZ() / 2;
        double z2 = -getDimensionZ() / 2;

        /* Add HepRep points to the instance. */
        addHepRepPoint(factory, instance, x1, y1, z1);
        addHepRepPoint(factory, instance, x1, y2, z1);
        addHepRepPoint(factory, instance, x2, y2, z1);
        addHepRepPoint(factory, instance, x2, y1, z1);        
        addHepRepPoint(factory, instance, x1, y1, z2);
        addHepRepPoint(factory, instance, x1, y2, z2);
        addHepRepPoint(factory, instance, x2, y2, z2);
        addHepRepPoint(factory, instance, x2, y1, z2);              
    }
    
    /**
     * Get the number of sides.
     * @return the number of sides
     */
    public int getNumberOfSides() {
        return DEFAULT_NSIDES;
    }

    /**
     * Get the section phi, which is 0 because test beams do not have modules in phi.
     * @return The section phi which is 0 for test beams.
     */
    public double getSectionPhi() {
        return 0;
    }

    /**
     * This method is not implemented for this type.
     * @throw UnsupportedOperationException if this method is called
     */
    public double getInnerZ() {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    /**
     * This method is not implemented for this type.
     * @throw UnsupportedOperationException if this method is called
     */
    public double getOuterZ() {
        throw new UnsupportedOperationException("Method not implemented.");
    }

    /**
     * Get the outer radius of the envelope volume.     
     * @return The outer radius.
     */
    public double getOuterRadius() {
        return radius;
    }

    /**
     * Get the inner radius of the TestBeam, which is always 0.
     * @return The inner radius, which is 0.
     */
    public double getInnerRadius() {
        return 0;
    }

    /**
     * Get the z length of the TestBeam.
     * @return The z length.
     */
    public double getZLength() {
        return dimensionZ;
    }
    
    /**
     * Add a point to the HepRep output.
     * @param factory the HepRepFactory object
     * @param instance the HepRepInstance object
     * @param x the x value of the point
     * @param y the y value of the point
     * @param z the z value of the point
     */
    private void addHepRepPoint(HepRepFactory factory, HepRepInstance instance, double x, double y, double z) {        
        Hep3Vector point = new BasicHep3Vector(x, y, z);
        transform.transform(point);
        factory.createHepRepPoint(instance, point.x(), point.y(), point.z());
    }
    
    /**
     * Compute the dimensions of the detector.
     * @param dimensions The XML element containing the dimension settings.
     * @throws DataConversionException if there was a problem converting from XML values
     */
    private void computeDimensions(Element dimensions) throws DataConversionException {
        dimensionX = dimensions.getAttribute("x").getDoubleValue();
        dimensionY = dimensions.getAttribute("y").getDoubleValue();
        dimensionZ = getLayering().getThickness();
    }

    /**
     * Compute the maximum radius of the detector
     * @return the radius of the detector
     */
    private double computeRadius() {
        return Math.sqrt(Math.pow(dimensionX / 2, 2) + Math.pow(dimensionY / 2, 2));
    }

    /**
     * Set the position from XML data.
     * @param node the XML element
     * @throws DataConversionException if there is a problem converting XML parameters
     */
    private void setPosition(Element node) throws DataConversionException {
        Element element = node.getChild("position");

        double px = 0;
        double py = 0;
        double pz = 0;
        if (element != null) {
            Attribute attribute = element.getAttribute("x");
            if (attribute != null) {
                px = attribute.getDoubleValue();
            }
            attribute = element.getAttribute("y");
            if (attribute != null) {
                py = attribute.getDoubleValue();
            }
            attribute = element.getAttribute("z");
            if (attribute != null) {
                pz = attribute.getDoubleValue();
            }
        }
        this.position = new BasicHep3Vector(px, py, pz);
    }
    
    /**
     * Set the detector's rotation from XML data.
     * @param node the XML element
     * @throws DataConversionException if there is a problem converting XML parameters
     */
    private void setRotation(Element node) throws DataConversionException {
        Element element = node.getChild("rotation");
        double rx, ry, rz;
        rx = ry = rz = 0.;
        if (element != null) {
            if (element.getAttribute("x") != null) {
                rx = element.getAttribute("x").getDoubleValue();
            }
            if (element.getAttribute("y") != null) {
                ry = element.getAttribute("y").getDoubleValue();
            }
            if (element.getAttribute("z") != null) {
                rz = element.getAttribute("z").getDoubleValue();
            }
        }        
        this.rotation = new RotationPassiveXYZ(rx, ry, rz);
    }
}