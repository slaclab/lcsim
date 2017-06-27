package org.lcsim.detector;

import hep.physics.vec.Hep3Vector;

/**
 * 
 * Interface to placements of {@link org.lcsim.detector.ILogicalVolume}
 * objects in the geometry tree.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public interface IPhysicalVolume 
{
	public String getName();
	
	/**
	 * Get the associated LogicalVolume.
	 * @return The associated LogicalVolume.
	 */
	public ILogicalVolume getLogicalVolume();	
	
	/**
	 * Get the mother's LogicalVolume.
	 * @return The mother's LogicalVolume.
	 */
	public ILogicalVolume getMotherLogicalVolume();
	
	/**
	 * Get the transformation from mother's coordinate system.
	 * @return The transformation from mother's coordinate system.
	 */
	public ITransform3D getTransform();
	
	/**
	 * Get the translation component of the coordinate transform.
	 * @return The translation component of the coordinate transform.
	 */
	public Hep3Vector getTranslation();
	
	/**
	 * Get the rotation component of the coordinate transform.
	 * @return The rotation component of the coordinate transform.
	 */
	public IRotation3D getRotation();
	
	/**
	 * Get the copy number.
	 * @return The copy number of this volume.
	 */
	public int getCopyNumber();
	
	/**
	 * Transform a point from parent to this volume.
	 * @param point A point in parent coordinate system.
	 * @return Point transformed to local coordinate system.
	 */
	public Hep3Vector transformParentToLocal(Hep3Vector point);
    
    /**
     * True if this volume is a sensitive component;
     * False if this volume is not a sensitive component.
     */
    // FIXME: Move to LogicalVolume or DetectorElement?   Geant4 uses LogicalVolume for this.
    public boolean isSensitive();
}
