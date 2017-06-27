package org.lcsim.detector;

import hep.physics.vec.Hep3Vector;

/**
 * This interface provides utilities for returning {@link org.lcsim.detector.IPhysicalVolumePath}
 * objects that represent a volume's unique position in the physical geometry tree with
 * an ordered list of physical volumes.  This class can calculate the combined geometric 
 * transform of an IPhysicalVolumePath.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: IPhysicalVolumeNavigator.java,v 1.11 2010/04/14 17:52:32 jeremy Exp $
 */
public interface IPhysicalVolumeNavigator 
{
	public String getName();
	
	/**
	 * Get the top physical volume assigned to
	 * this navigator.
	 * 
	 * @return The top or world volume.
	 */
	public IPhysicalVolume getTopPhysicalVolume();
	
	/**
	 * Set the top physical volume assigned to 
	 * this navigator.
	 * 
	 * @param physvol A top volume.
	 */
	public void setTopPhysicalVolume(IPhysicalVolume physvol);
	
	/**
	 * 
	 * Get the full stack of {@link IPhysicalVolume}s from a path string
	 * of names.
     *
	 * <code>
	 * getPath("/volume_name/subvolume_name")
	 * <code> 
     
	 * @param path A String with the path name.
	 * @return IPhysicalVolumePath corresponding to the name.
	 */
	public IPhysicalVolumePath getPath(String path);	

	/**
	 * 
	 * Get the full transformation from the origin of
	 * the coordinate system to the given path.
	 * 
	 * @param path The path of volumes.
	 * @return Transform from a path.
	 */
	public Transform3D getTransform(String path);
	
	/**
	 * Get the full transformation from the origin of
	 * the coordinate system from a stack of physical volumes.
	 * 
	 * @param path The path of volumes.
	 * @return The full transform from the path.
	 */
	Transform3D getTransform(IPhysicalVolumePath path);

	/**
	 * Given a global point, return the full path to
	 * deepest volume containing this point, not past
     * given depth.
	 * 
	 * @param globalPoint A point in the global coordinate system.
	 * @param level Max depth.  -1 will go to bottom.
	 * @return Path down to the given level.
	 */
	public IPhysicalVolumePath getPath(Hep3Vector globalPoint, int level);
	
	/**
	 * Same as {@link #getPath(Hep3Vector globalPoint, int level)}
     * with level set to -1.
	 * 
	 * @param globalPoint A point in the global coordinate system.
	 * @return Path at the point.
	 */
	public IPhysicalVolumePath getPath(Hep3Vector globalPoint);	
	 
	/**
	 * Traverse the tree using preorder, calling the visit method 
	 * of the {@link IPhysicalVolumeVisitor}.
	 * 
	 * @param visitor An IPhysicalVolumeVisitor that will be activated
	 *                at each PhysicalVolume in the tree.
	 */
	public void traversePreOrder(IPhysicalVolumeVisitor visitor);
	
	/**
	 * Traverse the tree using postorder, calling the visit method 
	 * of the {@link IPhysicalVolumeVisitor}.
	 * 
	 * @param visitor An IPhysicalVolumeVisitor that will be activated
	 *                at each PhysicalVolume in the tree.
	 */
	public void traversePostOrder(IPhysicalVolumeVisitor visitor);	
}
