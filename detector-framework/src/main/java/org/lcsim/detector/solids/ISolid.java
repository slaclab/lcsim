package org.lcsim.detector.solids;

import hep.physics.vec.Hep3Vector;

public interface ISolid 
{
	public String getName();
	public Inside inside(Hep3Vector position);
	public double getCubicVolume();
}