package org.lcsim.detector;

import hep.physics.vec.Hep3Vector;

import org.lcsim.detector.solids.Inside;

/**
 * IGeometryInfo provides geometry information about an {@link IDetectorElement}.
 *
 * These are some of the important methods on {@link IGeometryInfo}.
 * 
 * <ul>
 * <li>{@link #getLogicalVolume()} - associated {@link ILogicalVolume}</li>
 * <li>{@link #getGlobalToLocal()} - global to local {@link Transform3D}</li>
 * <li>{@link #getLocalToGlobal()} - local to global {@link Transform3D}</li>
 * <li>{@link #getParentToLocal()} - parent to local {@link Transform3D}</li>
 * <li>{@link #getPath()} - {@link PhysicalVolumePath} associated with this {@link DetectorElement}</li>
 * <li>{@link #isInside(Hep3Vector)} - check if a global point is inside this geometry</li>
 * </ul>
 *
 * An {@link IDetectorElement} may have an {@link IGeometryInfo}, which means that it has a
 * corresponding node in the geometry tree.  Those DetectorElements without IGeometryInfo 
 * are ghost volumes that have no geometric correspondence, but may fill a logical roll 
 * in the detector hierarchy.
 *
 * @author Jeremy McCormick 
 * @author Tim Nelson  
 * @version $Id: IGeometryInfo.java,v 1.15 2007/08/06 19:09:31 jeremy Exp $
 */
public interface IGeometryInfo 
{
	/**
	 * Get an {@link IGeometryInfoContainer} with the child DetectorElement's IGeometryInfo objects.
	 * @return Container of IGeometryInfos from the child DetectorElements.
	 */
	public IGeometryInfoContainer getChildGeometries();

	/**
	 * Get the {@link IPhysicalVolumePath} at a global point.
	 * @param globalPoint A point in global coordinates.
	 * @return A path of physical volumes at the global point.
	 */
	public IPhysicalVolumePath getPath(Hep3Vector globalPoint);

	/**
	 * Get the associated {@link ILogicalVolume}.
	 * @return A LogicalVolume associated with this geometry.
	 */
	public ILogicalVolume getLogicalVolume();

	/**
	 * Get the PhysicalVolume at the global point
	 * using the IPhysicalVolumePath of this IGeometryInfo.
     *
	 * @param globalPoint A point in global coordinates.
	 * @return The PhysicalVolume at the point.
	 */
	public IPhysicalVolume getPhysicalVolume(Hep3Vector globalPoint);
    
    /**
     * Get the leaf PhysicalVolume in this 
     */

	/**
	 * Get the center position of the DetectorElement in global coordinates.
	 * @return Center position of DetectorElement in global coordinates.
	 */
	public Hep3Vector getPosition();

	/**
	 * Get the {@link IPhysicalVolumePath} assigned to this GeometryInfo.
	 * This path points to a unique node in the geometry tree
	 * and determines the basic global to local transform
	 * of the DetectorElement.
	 * 
	 * @return The path of this DetectorElement.
	 */
	public IPhysicalVolumePath getPath();
    
    /**
     * Get the {@link IPhysicalVolumePath} from {@link #getPath}
     * as a {@link String}.
     *  
     * @return The path string.
     */
    public String getPathString();

	/**
	 * Get the combined global to local transform.
     *
	 * @return The global to local transform.
	 */
	public ITransform3D getGlobalToLocal();

	/**
	 * Transform the global point from global coordinates to local.
	 * 
	 * @param globalPoint 
	 * @return Global point transformed to local.
	 */
	public Hep3Vector transformGlobalToLocal(Hep3Vector globalPoint);

	/**
	 * True if this IGeometryInfo has an associated LogicalVolume.
	 * @return True if the IGeomInfo has a non-<code>null</code> LogicalVolume.
	 */
	public boolean hasLogicalVolume();

	/**
	 * True if this IGeometryInfo has an associated full path
	 * or geometry node.
	 * 
	 * @return True if this IGeometryInfo has an IPhysicalVolumePath.
	 */
	public boolean hasPath();

	/**
	 * True if the global point is inside this DetectorElement
     * or any of its descendants.
	 * @param globalPoint A global point.
	 * @return True if the global point is inside this DetectorElement
     *         or any of its descendants.
	 */
	public Inside inside(Hep3Vector globalPoint);

	/**
	 * Get the combined local to global transform.
     * @see ITransform3D
	 * @return The local to global transform.
	 */
	public ITransform3D getLocalToGlobal();

	/**
	 * Get the transformation from parent geometry 
	 * into local geometry.
	 * @return The parent to local transform.
	 */
	public ITransform3D getParentToLocal();
	
	/**
	 * Transform a local point into global coordinates.
	 * @param localPoint A local point.
	 * @return Point transformed into global coordinate system.
	 */
	public Hep3Vector transformLocalToGlobal(Hep3Vector localPoint);

	/**
	 * Get the parent DetectorElement's IGeometryInfo.
	 * @return The parent's IGeometryInfo or <code>null</code>
     *         if none exists.
	 */
	public IGeometryInfo parentGeometry();	

	/**
	 * Transform point in parent geometry to local coordinate system. 
	 * 
	 * @param parentPoint A point in the parent coordinate system.
	 * @return Point transformed from parent to local coordinates.
	 */
	public Hep3Vector transformParentToLocal(Hep3Vector parentPoint);
	     
    /**
     * Return the associated {@link DetectorElement}.
     * 
     * @return The associated DetectorElement.
     */
    public IDetectorElement getDetectorElement();
    
    /**
     * Get the associated {@link IPhysicalVolume} or
     * <code>null</code> if none exists.
     */
    public IPhysicalVolume getPhysicalVolume();
}