package org.lcsim.detector;

import static org.lcsim.detector.solids.Inside.INSIDE;
import static org.lcsim.detector.solids.Inside.OUTSIDE;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import org.lcsim.detector.solids.Inside;

/**
 * IGeometryInfo provides a cache of detailed geometry
 * information for its associated DetectorElement,
 * including coordinate transformations and the 
 * center position in global coordinates.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @author Tim Nelson <tknelson@slac.stanford.edu>
 */
public class GeometryInfo 
implements IGeometryInfo
{
    IDetectorElement de;
    IGeometryInfoContainer childIGeometryInfos;
    IGeometryInfo parentIGeometryInfo;
    IPhysicalVolumePath path;
    ILogicalVolume logicalVolume;
    ITransform3D globalToLocal;
    ITransform3D localToGlobal;
    ITransform3D parentToLocal;
    Hep3Vector globalPosition;	

    /**
     * 
     * This method provides a single point of entry
     * for setting up a GeometryInfo object.  Any
     * of the arguments except for @param de are
     * allowed to be null.
     * 
     * @param de The DetectorElement that owns this GeometryInfo.
     * @param lv The associated LogicalVolume.
     * @param support The path into the geometry that is associated
     *                with this DetectorElement.
     */
    private void setup(
            IDetectorElement de,
            ILogicalVolume logicalVolume,
            IPhysicalVolumePath support)
    {        
        // The DetectorElement is not allowed to be null.
        if ( de == null )
        {
            throw new IllegalArgumentException("The IDetectorElement is null!");
        }
        
        // Set the DetectorElement.
        this.de = de;

        // Cache child IGeometryInfos.
        if ( de.getChildren() != null )
        {
            if ( de.getChildren().size() > 0 )
            {
                this.childIGeometryInfos = new GeometryInfoContainer();
                for (IDetectorElement child : de.getChildren())
                {
                    this.childIGeometryInfos.add(child.getGeometry());
                }
            }
        }

        // Set the parent IGeometryInfo ref.
        if (de.getParent() != null)
        {
            this.parentIGeometryInfo = de.getParent().getGeometry();            
        }
        
        // Set the LogicalVolume, if it is given explicitly.
        if ( logicalVolume != null )
        {
            this.logicalVolume = logicalVolume;
        }

        // If the DetectorElement has support in the geometry tree,
        // set the support variable and cache all the derivable geometry 
        // information.
        if ( support != null )
        {
            setupPath(support);
        }
    }
    
    private void setupPath(
            IPhysicalVolumePath path
            )
    {
        if ( this.path != null )
        {
            throw new RuntimeException("The DetectorElement already has support in the geometry!");
        }
        
        if ( path == null )
        {
            throw new IllegalArgumentException("The IPhysicalVolumePath is null!");
        }
                        
        // Set the support reference.
        this.path = path;

        // Get the current geometry navigator.
        IPhysicalVolumeNavigator navigator =
            PhysicalVolumeNavigatorStore.getInstance().getDefaultNavigator();        

        // Set the LogicalVolume from the path if it hasn't been setup already.
        if ( this.logicalVolume == null )
        {
            logicalVolume = this.path.getLeafVolume().getLogicalVolume();
        }

        // Cache the local to global transform.
        localToGlobal = navigator.getTransform(this.path);

        // Cache the global to local transform.
        globalToLocal = localToGlobal.inverse();

        // Cache the parent to global transform.
        if ( parentGeometry() != null ) {
            parentToLocal = Transform3D.multiply(
                    getGlobalToLocal(),
                    parentGeometry().getLocalToGlobal());
        }

        // Cache the global position.
        globalPosition = localToGlobal.transformed(new BasicHep3Vector(0.,0.,0.));        
    }

    /**
     * Creates a ghost volume with no support
     * in the geometry tree.
     * 
     * @param de The associated DetectorElement.
     */
    public GeometryInfo(
            IDetectorElement de)
    {
        setup(de,null,null);
    }
    
    /**
     * Creates an orphan volume with a LogicalVolume
     * but no support in the geometry tree.
     * 
     * @param de The associated DetectorElement.
     */
    public GeometryInfo(
            IDetectorElement de,
            ILogicalVolume lv)
    {
        setup(de,lv,null);
    }

    /**
     * This constructor associates this GeometryInfo 
     * with a node in the geometry tree.
     * 
     * @param de
     * @param support
     */
    public GeometryInfo(
            IDetectorElement de, 
            IPhysicalVolumePath support)
    {			        
        if ( support == null )
        {
            throw new IllegalArgumentException("The support cannot be null!");
        }

        if ( support.size() == 0)
        {
            throw new IllegalArgumentException("Support is empty!");
        }
        
        setup(de,null,support);
    }

    public IGeometryInfoContainer getChildGeometries() 
    {
        return childIGeometryInfos;
    }

    public IPhysicalVolumePath getPath(Hep3Vector globalPoint) 
    {
        return PhysicalVolumeNavigatorStore.getInstance().getDefaultNavigator().getPath(globalPoint);
    }

    public ILogicalVolume getLogicalVolume() 
    {
        return logicalVolume;
    }

    public IPhysicalVolume getPhysicalVolume(Hep3Vector globalPoint) 
    {
        return getPath(globalPoint).getLeafVolume();
    }
    
    public IPhysicalVolume getPhysicalVolume()
    {
        IPhysicalVolume pv = null;
        if ( hasPath() )
        {
            pv = path.getLeafVolume();
        }
        return pv;
    }

    public Hep3Vector getPosition() 
    {
        return globalPosition;
    }

    public IPhysicalVolumePath getPath() 
    {
        return path;
    }

    public ITransform3D getGlobalToLocal() 
    {
        return globalToLocal;
    }

    public Hep3Vector transformGlobalToLocal(Hep3Vector global_point) 
    {
        return globalToLocal.transformed(global_point);
    }

    /** 
     * 
     * Check if the global point is inside this volume
     * by transforming the point from global to local coordinates
     * and seeing if the resulting point is inside this DetectorElement's
     * solid. 
     * 
     * Check the daughters recursively if this GeometryInfo does not
     * have a corresponding node in the geometry tree, i.e. if it 
     * is a "ghost" that is just a container for other DetectorElements.
     */
    public Inside inside(Hep3Vector globalPoint) 
    {
        Inside inside=OUTSIDE;
        if ( hasPath() )
        {
            inside = getLogicalVolume().getSolid().inside(
            		getGlobalToLocal().transformed(globalPoint)
            );
        }   
        else {
            for ( IDetectorElement child : getDetectorElement().getChildren() )
            {                
                inside = child.getGeometry().inside(globalPoint);
                if (inside==INSIDE) 
                {
                    break;
                }
            }
        }
        return inside;        
    }

    public ITransform3D getLocalToGlobal() 
    {
        return localToGlobal;
    }

    public Hep3Vector transformLocalToGlobal(Hep3Vector local_point) 
    {
        return localToGlobal.transformed(local_point);
    }

    public IGeometryInfo parentGeometry() 
    {
        return parentIGeometryInfo;
    }

    public ITransform3D getParentToLocal() 
    {
        return parentToLocal;
    }

    public Hep3Vector transformParentToLocal(Hep3Vector parentPoint)
    {
        return parentToLocal.transformed(parentPoint);		
    }
    
    public boolean isOrphan()
    {
        return path == null && logicalVolume != null;
    }

    public IDetectorElement getDetectorElement()
    {
        return de;
    }
    
    public boolean hasLogicalVolume() 
    {
        return logicalVolume != null;
    }

    public boolean hasPath() 
    {
        return path != null;
    }   
    
    public String getPathString()
    {
        String pathString = null;
        if ( hasPath() )
        {
            pathString = path.toString();
        }
        return pathString;
    }
}
