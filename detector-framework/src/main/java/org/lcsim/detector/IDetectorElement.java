package org.lcsim.detector;

import hep.physics.vec.Hep3Vector;

import java.util.List;

import org.lcsim.detector.identifier.Identifiable;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierHelper;

/**
 * A class to represent a node in the detector hierarchy, based on Gaudi's IDetectorElement interface. The concept of DetectorElement is more abstract than physical geometry, as it may simply be a
 * container for other DetectorElements and need not have a physical representation in the geometry tree.
 * 
 * @see IDetectorElementStore
 * @see IDetectorElementContainer
 * @see IGeometryInfo
 * @see Identifiable
 * @see IIdentifier
 * @see IParameters
 * @see IReadout
 * 
 * @author jeremym
 * @author tknelson
 * @version $Id: IDetectorElement.java,v 1.25 2009/03/03 21:07:24 jeremy Exp $
 */
public interface IDetectorElement extends Identifiable {
    /**
     * Get the geometric information for this node, including the volume center position and global and local transformations.
     * 
     * @return An IGeometryInfo object with geometry information.
     */
    public IGeometryInfo getGeometry();

    /**
     * Get the parent element of this node. The top-level <code>DetectorElement</code> has no parent.
     * 
     * @return This node's parent or <code>null</code> if the DetectorElement has no parent.
     */
    public IDetectorElement getParent();

    /**
     * Get a container with the DetectorElement's children.
     * 
     * @return An IDetectorElementContainer containing this DE's children. This container will be empty if there are no children.
     */
    public IDetectorElementContainer getChildren();

    /**
     * True if the DetectorElement has children.
     * 
     * @return True if has children, else False.
     */
    public boolean hasChildren();

    /**
     * Get the name of this DetectorElement.
     * 
     * @return The name this DetectorElement.
     */
    public String getName();

    /**
     * Set the identifier of this DE.
     * 
     * @param id
     */
    public void setIdentifier(IIdentifier id);

    /**
     * Get the identifier of this DE.
     * 
     * @return The unique identifier of this DetectorElement.
     */
    public IIdentifier getIdentifier();

    public IIdentifierHelper getIdentifierHelper();

    /**
     * Set the parent DetectorElement. This method also calls addChild on the parent.
     * 
     * @param parent The parent DetectorElement.
     */
    public void setParent(IDetectorElement parent);

    /**
     * @return True if {@link IGeometryInfo} has been created; False if {@link IGeometryInfo} is <code>null</code>.
     */
    public boolean hasGeometryInfo();

    /**
     * Locate the deepest DetectorElement containing a global point starting with this DetectorElement and traversing into its children.
     *
     * This method can be used from {@link org.lcsim.geometry.Detector} to find the deepest node within the complete detector.
     * 
     * This method is not on {@link GeometryInfo}, because a DetectorElement is allowed to have a <code>null</code> GeometryInfo if it is a simple container without a geometry path, i.e. a ghost
     * volume.
     * 
     * @return The deepest IDetectorElement containing globalPoint or <code>null</code> if point is not contained within this DetectorElement or its children.
     */
    public IDetectorElement findDetectorElement(Hep3Vector globalPoint);

    /**
     * Locate a child detector element by {@link IIdentifier}.
     * @param id The <code>IIdentifier</code> of the <code>DetectorElement</code>.
     * @return The matching <code>IDetectorElement</code> to the ID.
     */
    public IDetectorElementContainer findDetectorElement(IIdentifier id);

    /**
     * Locate a child detector element given a slash-separated list of DetectorElement names.
     * @param pathString
     * @return
     */
    public IDetectorElement findDetectorElement(String pathString);

    /**
     * Locate a child detector element given an ordered list of DetectorElement names.
     * @param path
     * @return
     */
    public IDetectorElement findDetectorElement(String[] path);

    /**
     * Get the readout associated with this DetectorElement, or <code>null</code> if the DetectorElement has no associated readout.
     * 
     * @see org.lcsim.detector.IReadout
     * @see org.lcsim.detector.Readout
     * 
     * @return Associated IReadout object or <code>null</code> if Readout has been assigned to this DetectorElement.
     */
    public IReadout getReadout();

    /**
     * True if this DetectorElement has an {@link IReadout}; False if the {@link IReadout} is <code>null</code>.
     * 
     * @return True if this DetectorElement has a Readout; False if there is no Readout.
     */
    public boolean hasReadout();

    /**
     * The named parameters associated to this instance.
     * 
     * @see org.lcsim.detector.IParameters
     * 
     * @return An IParameters object containing the DetectorElement's named parameter set.
     */
    public IParameters getParameters();

    /**
     * A list of parents from the top to this one. The first member of the list will be the top {@link IDetectorElement} in the hierarchy. The last memory of the list will be this
     * {@link IDetectorElement}.
     * 
     * @see IDetectorElementContainer
     * 
     * @return A list of ancestors from the top to this node.
     */
    public IDetectorElementContainer getAncestry();

    /**
     * True if the DetectorElement is an ancestor of this one.
     */
    public boolean isAncestor(IDetectorElement de);

    /**
     * Clear the {@link IReadout} of this {@link DetectorElement} and recursively visit and clear the children..
     * 
     * @see IReadout
     * @see Readout
     */
    public void clearReadouts();

    /**
     * True if the DetectorElement is a descendant of this one.
     * 
     * @param de A DetectorElement to search for in descendants.
     * @see IDetectorElement
     * @see IDetectorElementContainer
     * @return True if DetectorElement is a descendant of this one.
     */
    public boolean isDescendant(IDetectorElement de);

    /**
     * Recursive pre-order tree traversal of this {@link IDetectorElement}.
     * @param visitor A visitor interface to perform some action on the node.
     */
    public void traverseDescendantsPreOrder(IDetectorElementVisitor visitor);

    /**
     * Recrusive post-order tree traversal of this {@link IDetectorElement}.
     * @param visitor A visitor interface to perform some action on the node.
     */
    public void traverseDescendantsPostOrder(IDetectorElementVisitor visitor);

    /**
     * Visit this and ancestors.
     * 
     * @param visitor
     */
    public void traverseAncestors(IDetectorElementVisitor visitor);

    /**
     * Find ancestors matching a class.
     * 
     * @param klass Class to match.
     */
    public <T extends IDetectorElement> List<T> findAncestors(Class<T> klass);

    /**
     * Find descendants matching a class.
     *
     * @param klass The class of the descendant DetectorElement.
     * @return Get a list of descendants with matching class.
     */
    public <T extends IDetectorElement> List<T> findDescendants(Class<T> klass);

    /**
     * True if the DetectorElement's PhysicalVolume is flagged as sensitive. False if the PhysicalVolume is not sensitive or the DetectorElement has no associated geometry.
     * @return True if sensitive; false if not sensitive.
     */
    public boolean isSensitive();
    
    /**
     * This method should be called externally to perform initialization after all child elements have been added.
     */
    public void initialize();
}
