package org.lcsim.detector;

import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.solids.ISolid;

/**
 * An interface to logical volume information.
 * 
 * This interface borrows concepts from the Geant4 class G4LogicalVolume,
 * but does not include regions, sensitive detectors, or the magnetic
 * field.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 *
 */
public interface ILogicalVolume 
{
	public String getName();
	public ISolid getSolid();
	public IMaterial getMaterial();
	public IPhysicalVolumeContainer getDaughters();
	public int getNumberOfDaughters();
	public void addDaughter(IPhysicalVolume physvol); 
	public IPhysicalVolume getDaughter(int i);
	public boolean isDaughter(IPhysicalVolume physvol);
	public boolean isAncestor(IPhysicalVolume physvol);
}