package org.lcsim.detector;

import hep.physics.vec.Hep3Vector;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.detector.identifier.Identifier;
import org.lcsim.detector.solids.Inside;

/**
 * Implementation of {@link IDetectorElement}.
 * 
 * @author Jeremy McCormick
 * @version $Id: DetectorElement.java,v 1.48 2011/02/25 03:09:38 jeremy Exp $
 */
public class DetectorElement implements IDetectorElement
{
    private IDetectorElementContainer children;
    private IGeometryInfo geometry;
    private IDetectorElement parent;
    private IIdentifier id;
    private IIdentifierHelper helper;
    private IParameters parameters;
    private IReadout readout;
    private String name;

    /**
     * For subclasses.
     */
    protected DetectorElement()
    {
    }

    /**
     * Creates a DetectorElement with complete arguments, including a parent,
     * geometry support as a String, and an
     * {@link org.lcsim.detector.IIdentifier}.
     * 
     * @param name The name of this DetectorElement.
     * @param parent The parent DetectorElement.
     * @param support The geometry support as a "/" delimited String.
     * @param id The DetectorElement's identifier.
     */
    public DetectorElement(String name, IDetectorElement parent, IPhysicalVolumePath support, IIdentifier id)
    {
        setup(name, parent, support, id);
        register();
    }

    /**
     * Create a DE with a parent and support in the geometry tree.
     * 
     * @param name
     * @param parent
     * @param support
     */
    public DetectorElement(String name, IDetectorElement parent, IPhysicalVolumePath support)
    {
        setup(name, parent, support, null);
        register();
    }

    /**
     * Create a DE with complete arguments, including a parent DE, geometry
     * support, and an id.
     * 
     * @param name
     * @param parent
     * @param support
     */
    public DetectorElement(String name, IDetectorElement parent, String support)
    {
        setup(name, parent, support, id);
        register();
    }

    /**
     * Create a DE with complete arguments, including a parent DE, string of
     * path, and an id.
     * 
     * @param name
     * @param parent
     * @param support
     */
    public DetectorElement(String name, IDetectorElement parent, String support, IIdentifier id)
    {
        setup(name, parent, support, id);
        register();
    }

    /**
     * Create a DE with a parent but no support in the geometry, e.g. a ghost
     * volume.
     * 
     * @param name
     * @param parent
     */
    public DetectorElement(String name, IDetectorElement parent)
    {
        setup(name, parent, (IPhysicalVolumePath) null, null);
        register();
    }

    /**
     * Create with a name, parent, and identifier.
     */
    public DetectorElement(String name, IDetectorElement parent, IIdentifier id)
    {
        setup(name, parent, (IPhysicalVolumePath) null, id);
        register();
    }

    /**
     * Create a DE with no parent and no support. If this constructor is used,
     * then an external routine must setup the parent, support, and/or id later.
     * 
     * @param name
     */
    public DetectorElement(String name)
    {
        this.name = name;
        register();
    }

    public String getName()
    {
        return name;
    }

    /**
     * Set the parent IDetectorElement. Once this has been set, additional calls
     * to this method will cause a RuntimeException.
     * 
     * @param parent The parent IDetectorElement.
     * @throws RuntimeException If the parent IDetectorElement is already set.
     * @throws IllegalArgumentException If @param parent is null.
     */
    public void setParent(IDetectorElement parent)
    {
        if (this.parent != null)
        {
            throw new RuntimeException("The IDetectorElement <" + getName() + "> already has a parent!");
        }

        if (parent == null)
        {
            throw new IllegalArgumentException("The parent IDetectorElement is null!");
        }

        this.parent = parent;
        ((DetectorElement) parent).addChild(this);
    }

    /**
     * Register this IDetectorElement with the DetectorElementStore.
     */
    private void register()
    {
        if (!DetectorElementStore.getInstance().contains(this))
        {
            DetectorElementStore.getInstance().add(this);
        }
    }

    private void checkName(String name)
    {
        if (name == null)
            throw new IllegalArgumentException("Name argument is null!");

        if (name.length() == 0)
            throw new IllegalArgumentException("Name is zero length!");

        if (name.trim().length() == 0)
            throw new IllegalArgumentException("Name contains only white space!");

        if (name.contains("/"))
            throw new IllegalArgumentException("Name contains a '/', which is not a legal character!");
    }

    private void setup(String name, IDetectorElement parent, IPhysicalVolumePath support, IIdentifier id)
    {
        this.name = name;

        checkName(this.name);

        if (parent != null)
        {
            setParent(parent);
        }

        if (support != null)
        {
            setSupport(support);
        }

        if (id != null)
        {
            setIdentifier(id);
        }
    }

    private void setup(String name, IDetectorElement parent, String support, IIdentifier id)
    {
        this.name = name;

        checkName(this.name);

        if (parent != null)
        {
            setParent(parent);
        }

        if (support != null)
        {
            setSupport(support);
        }

        if (id != null)
        {
            setIdentifier(id);
        }
    }

    public void setSupport(IPhysicalVolumePath support)
    {
        createGeometryInfo(support);
    }

    public void setSupport(String path)
    {
        createGeometryInfo(path);
    }

    private void createGeometryInfo(IPhysicalVolumePath path)
    {
        geometry = new GeometryInfo(this, path);
    }

    private void createGeometryInfo(String path)
    {
        IPhysicalVolumeNavigator nav = PhysicalVolumeNavigatorStore.getInstance().getDefaultNavigator();
        geometry = new GeometryInfo(this, nav.getPath(path));
    }

    protected void addChild(IDetectorElement child)
    {
        if (children == null)
        {
            children = new DetectorElementContainer();
        }
        children.add(child);
    }

    public IDetectorElementContainer getChildren()
    {
        if (children == null)
        {
            children = new DetectorElementContainer();
        }
        return children;
    }

    public boolean hasChildren()
    {
        if (children == null)
        {
            return false;
        }
        return children.size() != 0;
    }

    public IGeometryInfo getGeometry()
    {
        return geometry;
    }

    public IDetectorElement getParent()
    {
        return parent;
    }

    public IExpandedIdentifier getExpandedIdentifier()
    {
        try
        {
            return getIdentifierHelper().unpack(id);
        }
        catch (Exception x)
        {
            throw new RuntimeException(x);
        }
    }

    public IIdentifier getIdentifier()
    {
        // FIXME Should return null if no Id.
        if (id == null)
        {
            id = new Identifier();
        }
        return id;
    }

    public void setIdentifier(IIdentifier id)
    {
        this.id = id;
    }

    public boolean hasGeometryInfo()
    {
        return geometry != null;
    }

    public IDetectorElement findDetectorElement(Hep3Vector globalPoint)
    {
        IDetectorElement srch = null;

        if (hasGeometryInfo())
        {
            Inside inside = getGeometry().inside(globalPoint);
            if (inside == Inside.INSIDE)
            {
                srch = this;
            }
        }

        // Look recursively through the children.
        if ((!hasGeometryInfo() || srch != null) && hasChildren())
        {
            for (IDetectorElement child : getChildren())
            {
                IDetectorElement childSrch = child.findDetectorElement(globalPoint);
                if (childSrch != null)
                {
                    srch = childSrch;
                    break;
                }
            }
        }

        return srch;
    }

    public IReadout getReadout()
    {
        if (readout == null)
        {
            readout = createReadout();
        }
        return readout;
    }

    public void setReadout(IReadout readout)
    {
        this.readout = readout;
    }

    public IReadout createReadout()
    {
        return new Readout();
    }

    public boolean hasReadout()
    {
        return this.readout != null;
    }

    public IParameters getParameters()
    {
        return parameters;
    }

    public IDetectorElementContainer getAncestry()
    {
        IDetectorElementContainer parents = new DetectorElementContainer();

        parents.add(this);

        IDetectorElement par = this.getParent();

        while (par != null)
        {
            parents.add(par);
            par = par.getParent();
        }

        java.util.Collections.reverse(parents);

        return parents;
    }

    public void clearReadouts()
    {
        if (hasReadout())
        {
            readout.clear();
        }

        if (hasChildren())
        {
            for (IDetectorElement child : getChildren())
            {
                child.clearReadouts();
            }
        }
    }

    public boolean isDescendant(IDetectorElement de)
    {
        boolean isDesc = false;
        if (hasChildren())
        {
            if (children.contains(de))
            {
                return isDesc = true;
            }
            else
            {
                for (IDetectorElement child : children)
                {
                    isDesc = child.isDescendant(de);
                    if (isDesc)
                        break;
                }
            }
        }
        return isDesc;
    }

    /**
     * Set the {@link IIdentifierHelper} for encoding and decoding identifiers
     * of this DetectorElement.
     * 
     * @param helper The IdentifierHelper to be assigned.
     */
    public void setIdentifierHelper(IIdentifierHelper helper)
    {
        this.helper = helper;
    }

    public IIdentifierHelper getIdentifierHelper()
    {
        if (helper == null)
        {
            IDetectorElement search = getParent();
            while (search != null)
            {
                if (search.getIdentifierHelper() != null)
                    return search.getIdentifierHelper();
                search = search.getParent();
            }
        }

        // Might return null if no parent has a helper.
        return helper;
    }

    public void traverseDescendantsPreOrder(IDetectorElementVisitor visitor)
    {
        traversePreOrder(this, visitor);
    }

    public void traverseDescendantsPostOrder(IDetectorElementVisitor visitor)
    {
        traversePostOrder(this, visitor);
    }

    private static final void traversePreOrder(IDetectorElement detectorElement, IDetectorElementVisitor visitor)
    {
        // Return if done.
        if (visitor.isDone())
        {
            return;
        }

        // Visit this node.
        visitor.visit(detectorElement);

        // Recursively traverse the daughters.
        if (detectorElement.hasChildren())
        {
            for (IDetectorElement child : detectorElement.getChildren())
            {
                traversePreOrder(child, visitor);
            }
        }
    }

    private static final void traversePostOrder(IDetectorElement detectorElement, IDetectorElementVisitor visitor)
    {
        // Return if done.
        if (visitor.isDone())
        {
            return;
        }

        // Recursively traverse the daughters.
        if (detectorElement.hasChildren())
        {
            for (IDetectorElement child : detectorElement.getChildren())
            {
                traversePreOrder(child, visitor);
            }
        }

        // Visit this node.
        visitor.visit(detectorElement);
    }

    public void traverseAncestors(IDetectorElementVisitor visitor)
    {
        IDetectorElement detelem = this;
        while (detelem != null)
        {
            visitor.visit(detelem);
            detelem = detelem.getParent();
        }
    }

    private class TypeSearch<T extends IDetectorElement> implements IDetectorElementVisitor
    {
        Class<T> klass;
        List<T> results = new ArrayList<T>();

        TypeSearch(Class<T> klass)
        {
            this.klass = klass;
        }

        public void visit(IDetectorElement detectorElement)
        {
            if (klass.isInstance(detectorElement))
            {
                results.add(klass.cast(detectorElement));
            }
        }

        public List<T> getResult()
        {
            return results;
        }

        public boolean isDone()
        {
            return false;
        }
    }

    public <T extends IDetectorElement> List<T> findAncestors(Class<T> klass)
    {
        TypeSearch<T> search = new TypeSearch<T>(klass);
        traverseAncestors(search);
        return search.getResult();
    }

    public <T extends IDetectorElement> List<T> findDescendants(Class<T> klass)
    {
        TypeSearch<T> search = new TypeSearch<T>(klass);
        traverseDescendantsPreOrder(search);
        return search.getResult();
    }

    public boolean isAncestor(IDetectorElement de)
    {
        return getAncestry().contains(de);
    }

    public void setParameters(IParameters parameters)
    {
        if (parameters == null)
        {
            throw new IllegalArgumentException("The parameters object is null.");
        }
        this.parameters = parameters;
    }

    /**
     * Default implementation of
     * {@link IDetectorElement#findDetectorElement(IIdentifier)}. Specific types
     * of {@link IDetectorElement}s can override the default scheme.
     */
    public IDetectorElementContainer findDetectorElement(IIdentifier id)
    {
        return DetectorElementStore.getInstance().find(id);
    }

    public IDetectorElement findDetectorElement(String pathString)
    {
        return findDetectorElement(pathString.split("/"));
    }

    public IDetectorElement findDetectorElement(String[] path)
    {
        IDetectorElement de = this;
        for (int i = 0; i < path.length; i++)
        {
            //System.out.println("looking up path component <" + path[i] + ">");

            if (!de.hasChildren() && i != path.length)
                throw new RuntimeException("Not enough child DetectorElements for path argument.");

            IDetectorElementContainer children = de.getChildren();

            IDetectorElementContainer matches = children.find(path[i]);
            if (matches.size() > 1)
                throw new RuntimeException("Found more than one match for path component <" + path[i] + ">.");

            // Check if no matches were found.
            // FIXME Should this throw an exception or just return null?
            if (matches.size() == 0)
            {
                throw new RuntimeException("A DetectorElement for the path component <" + path[i] + "> was not found.");
            }

            de = matches.get(0);
        }
        return de;
    }

    public IDetectorElement getTop()
    {
        IDetectorElement top;
        if (this.getParent() == null)
            return this;
        else
            top = this.getParent();

        while (true)
        {
            if (top.getParent() == null)
                break;
            top = top.getParent();
        }
        return top;
    }

    public boolean isSensitive()
    {
        if (!hasGeometryInfo())
            return false;
        IPhysicalVolume pv = getGeometry().getPhysicalVolume();
        if (pv != null)
            return pv.isSensitive();
        else
            return false;
    }
    
    public void initialize() {
    }
}
