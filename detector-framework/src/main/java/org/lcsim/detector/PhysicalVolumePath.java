package org.lcsim.detector;

import java.util.ArrayList;

/**
 * An ordered list of PhysicalVolume objects
 * corresponding to a unique path in a 
 * geometry of LogicalVolumes and
 * PhysicalVolumes.  The first volume
 * should be the world volume.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 *
 */
public class PhysicalVolumePath 
extends ArrayList<IPhysicalVolume>
implements IPhysicalVolumePath
{
	/**
	 * Get the top or first volume which is the world volume.
	 * @return The top volume.
	 */
	public IPhysicalVolume getTopVolume()
	{
		if (size()>0)
		{
			return this.get(0);
		}
		else {
			return null;
		}
	}	
	
	/**
	 * Get the bottom or last volume which is the leaf in this path.
	 * @return The leaf volume.
	 */
	public IPhysicalVolume getLeafVolume()
	{
		if (size()>0)
		{
			return this.get(size()-1);
		}
		else {
			return null;
		}
	}
	
	/**
	 * True if this IPhysicalVolumePath contains no PhysicalVolumes.
	 * @return True if empty.
	 */
	public boolean isEmpty()
	{
		return size()==0;
	}

	/**
	 * Compare with another IPhysicalVolumePath.
	 * 
	 * False if path is null, if path is not the same
	 * size as this one, or if any of the PhysicalVolumes
	 * are different PhysicalVolume objects.
	 * 
	 * @param path
	 * @return True if the the paths are equal.
	 */
	public boolean equals(IPhysicalVolumePath path)
	{
		if (path == null)
		{
			return false;
		}
		
		if (path.size() != this.size())
		{
			return false;
		}
		
		for (int i=0; i<this.size(); i++)
		{
			if (this.get(i) != path.get(i))
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Compare the smallest prefix set of PhysicalVolumes together. 
     * 
     * For instance, these two IDs would return true.
     * 
     * <code>
     * ExpandedIdentifier id1 = ExpandedIdentifier("/1/2/3");
     * ExpandedIdentifier id2 = ExpandedIdentifier("/1/2/");
     * </code>
	 * 
	 * @param path
	 * @return True if the set of prefix fields is equal.
	 */
	public boolean equalsPrefix(IPhysicalVolumePath path)
	{		
		if (path == null)
		{
			return false;
		}
		
		if (size() != 0 && path.size() == 0)
		{
			return false;
		}
		
		for (int i = (size() <= path.size() ? size() : path.size() ); i<size(); i++)
		{
			if (get(i) != path.get(i))
			{
				return false;
			}
		}
		return true;
	}
	
	public String toString()
	{		
		StringBuffer sb = new StringBuffer();

		if (getTopVolume() != null)
		{			
			if (size() == 1)
			{
				sb.append("/");				
			}
			else {
				for (int i=1; i<size(); i++)
				{
					sb.append("/"+get(i).getName());
				}
			}
		}
		else {
			sb.append("");
		}				
				
		return sb.toString();
	}
}