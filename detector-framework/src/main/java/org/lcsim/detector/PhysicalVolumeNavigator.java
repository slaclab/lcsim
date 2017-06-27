package org.lcsim.detector;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import java.util.Collection;

import org.lcsim.detector.solids.Inside;

/**
 * Navigates from a top or "world" volume
 * into daughter volumes using String
 * paths of Physical Volume names.  The
 * returned information is a unique
 * stack of PhysicalVolume objects, called
 * an IPhysicalVolumePath.
 *  
 * String paths are a series of PhysicalVolume names.
 * 
 * "/a/b/c"
 * 
 * Paths do not include an explicit name of the top
 * volume.  The top volume can be addressed with
 * a single slash, "/", which the getPath method will
 * interpret to mean the top volume of this navigator.
 * 
 * The LogicalVolume class enforces unique naming of 
 * PhysicalVolume objects within its own daughter collection.
 * This allows unique addressing of children from a
 * given PhysicalVolume node.
 * 
 * Locates the deepest daughter volume containing a given 
 * global point within the top volume.
 * 
 * Computes the combined transform of IPhysicalVolumePaths.
 *
 * In theory, the top PhysicalVolume need not be the world
 * volume.  It is referred to as the "top" volume to avoid 
 * confusion.  
 *
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 *
 */
public class PhysicalVolumeNavigator 
implements IPhysicalVolumeNavigator
{
	String name;
	
	/**
	 * Find the full geometry path to the PhysicalVolume containing 
     * the global point @param globalPoint, relative to the world volume.
	 * 
	 * @param globalPoint Point in top geometry system.
	 * @param level Depth to descend.  -1 means to bottom.
     * @return An IPhysicalVolumePath containing the unique path to the containing sub-volume,
     *         or null if the @param globalPoint is outside the world volume.
	 */
	public IPhysicalVolumePath getPath(Hep3Vector globalPoint, int level) 
	{                
        // Path that will be returned to user.
        // Empty path means globalPoint is outside
        // the world volume.
		IPhysicalVolumePath path = new PhysicalVolumePath();
        
        // Get the top volume from this navigator.
		IPhysicalVolume world = getTopPhysicalVolume();
        
        // Start by looking in the world volume.
		ILogicalVolume lvCurr = world.getLogicalVolume();

		// First time, see if point is inside the world.
		if (lvCurr.getSolid().inside(globalPoint) == Inside.INSIDE)
		{
            // Add world to path.
			path.add(world);
		}		
        // The point is outside the world volume!
		else {            
			System.err.println("!!! Point " + globalPoint.v() + " is outside the top volume <"+world.getName()+">. !!!");
			
			// Return an empty path.
			return path;
		}

		// Current depth.
		int depth=0;

		// The current local point that is computed
        // by applying the containing daughter's transform
        // successively as the search is performed.
        Hep3Vector localPoint = new BasicHep3Vector(globalPoint.x(),globalPoint.y(),globalPoint.z());
        
        // Combined transform of path to current daughter.
        // If a daughter is found to contain the point,
        // its transform is applied to this.
        ITransform3D combinedTransform = new Transform3D();
        
        // Go into the geometry tree as long as there are 
        // daughter volumes in the current LogicalVolume.
		while(lvCurr.getNumberOfDaughters() != 0)
		{			
            // No daughter found yet.
			boolean inDau=false;
			
			// Loop over the daughters.
			for (IPhysicalVolume dau : lvCurr.getDaughters())
			{												
                // Transform the local point from parent
                // into the daughter's coordinate system.
                Hep3Vector checkLocalPoint =
                    dau.getTransform().inverse().transformed(localPoint);
                                						
                // Check if the point is inside this daughter's solid.
                if (dau.getLogicalVolume().getSolid().inside(checkLocalPoint) == Inside.INSIDE)
				{                    
                    // Found a containing daughter.
					inDau=true;
					
					// Add this daughter to the returned path.
					path.add(dau);

					// Traverse into the daughter.
					lvCurr = dau.getLogicalVolume();

					// Increment the current depth.
					++depth;
                    
                    // Add the daughter's transform to the combined transform.
                    combinedTransform.multiplyBy(dau.getTransform());
                    
                    // Set the current point to the daughter's local point.
                    localPoint = checkLocalPoint;
					
					// Stop looking at this volume's daughters.
					break;
				}			                
			}

			// If depth is past selected level or
            // no daughter was found, stop looking
            // and quit.  Current path will be returned.
			if ( level != -1 && depth >= level || !inDau)
			{
				break;
			}			
		}

		return path;
	}

	/**
	 * Get the IPhysicalVolumePath to the deepest PhysicalVolume
	 * in the tree containing the global point @param globalPoint,
	 * relative to the top volume.
	 */
	public IPhysicalVolumePath getPath(Hep3Vector globalPoint) 
	{
		return getPath(globalPoint,-1);
	}

	/**
	 * Get the combined transform from a String path.
	 * @param path A valid path into the geometry tree.
	 */
	public Transform3D getTransform(String path) 
	{
		return getTransform(getPath(path));
	}

	/**
	 * Compute the combined global to local transform from a path of PhysicalVolumes.
	 * 
	 * @param path The PhysicalVolumePath containing the unique geometry node.
	 */
	public Transform3D getTransform(IPhysicalVolumePath path) 
	{
		Transform3D theTransform = new Transform3D();
		for (IPhysicalVolume physvol : path)
		{
			theTransform.multiplyBy(physvol.getTransform());
		}
		return theTransform;
	}

	/**
	 * Utility method for dealing with strings
	 * containing slash-delimited volume names.
	 * Returns an array containing the name
	 * components.  The top volume name is not
	 * included.  Single leading and trailing
	 * slashes are discarded to avoid empty
	 * array entries when splitting the path.
	 *
	 * @param path
	 * @return
	 */
	private static String[] splitPath(String path)
	{
		if (path.equals("/"))
		{
			return new String[] {};
		}
		
		// Eat the first slash.
		if (path.startsWith("/"))
		{
			path = path.substring(1);
		}
		
		// Eat the last slash.
		if (path.endsWith("/"))
		{
			path = path.substring(0,(path.length()-1));
		}
		
		//System.out.println("path after eating slashes: "+path);

		// Split on remaining slashes.
		return path.split("/");
	}	
	
	/**
	 * 
	 * This is the primary method for navigating 
	 * using PhysicalVolume names.  It looks for
	 * a stack of volumes in the geometry tree with 
	 * names matching those in @param path.
	 * The match must be exact.  Failure
	 * to find a match throws a RuntimeException.
	 * The top volume name is not included
	 * in the search, but it is added to 
	 * the path so that the caller still has
	 * access to the complete geometric tree.
	 * 
	 * @param path
	 * @return The path associated with this array of volume names.
	 */
	public IPhysicalVolumePath getPath(String[] path)
	{
		// The path to be returned to user.
		IPhysicalVolumePath physvols = new PhysicalVolumePath();

		// The top physical volume.
		IPhysicalVolume pv = getTopPhysicalVolume();
		
		// Always add the top volume.
		physvols.add(pv);
		
		// Loop over the names in the path. 
		for (String name : path)		
		{									
			PhysicalVolumeContainer pvSearch = 
				pv.getLogicalVolume().getDaughters().findByName(name);
			
			if (pvSearch.size() > 1)
			{
				throw new RuntimeException("Got multiple matches on name <"+name+"> in mom <"+pv.getName() + ">.");
			}
			else if (pvSearch.size() == 0)
			{
				throw new RuntimeException("Path component <"+name+"> was not found!");
			}
			else {				
				IPhysicalVolume pvFound = pvSearch.get(0);
				//System.out.println("found match = " + pvFound.getName());
				physvols.add(pvFound);
				pv = pvSearch.get(0);
			}
		}
		return physvols;
	}		

	/**
	 * Get the IPhysicalVolumePath from a String @param path argument.
	 */
	public IPhysicalVolumePath getPath(String path) 
	{			
		return getPath(splitPath(path));
	}

	/**
	 * Get the top volume of this navigator.
	 * @return The top volume's PhysicalVolume.
	 */
	public IPhysicalVolume getTopPhysicalVolume() 
	{				
		return pvTop;
	}

	/**
	 * Set the top volume of this navigator.
	 * @param physvol A PhysicalVolume that cannot be null.
	 */
	public void setTopPhysicalVolume(IPhysicalVolume physvol) 
	{
		if (physvol == null)
		{
			throw new IllegalArgumentException("Top volume points to null!");
		}
		pvTop = physvol;		
	}
	
	/**
	 * Sets the top volume of this navigator 
	 * from the top node of an IPhysicalVolumePath.
	 * 
	 * @param path
	 */
	public void setTopPhysicalVolume(IPhysicalVolumePath path) 
	{
		if (path == null)
		{
			throw new IllegalArgumentException("The path cannot be null!");
		}
		
		if (path.isEmpty())
		{
			throw new IllegalArgumentException("The path is empty!");
		}
		
		setTopPhysicalVolume(path.getTopVolume());		
	}
		
	private IPhysicalVolume pvTop;

    public PhysicalVolumeNavigator(String name, IPhysicalVolume pvTop)
    {
        this.name = name;        
        setTopPhysicalVolume(pvTop);        
        PhysicalVolumeNavigatorStore.getInstance().add(this);
    }
    
    public PhysicalVolumeNavigator(String name, IPhysicalVolumePath path)
    {
        this.name = name;
        
        if ( path == null )
        {
            throw new IllegalArgumentException("The path is null!");
        }
        
        setTopPhysicalVolume(path);
        
        PhysicalVolumeNavigatorStore.getInstance().add(this);
    }       
    	
	public void traversePreOrder(
			IPhysicalVolumeVisitor visitor)
	{
		if ( visitor == null )
		{
			throw new IllegalArgumentException("The Visitor is null!");
		}
		
		traversePreOrder(getTopPhysicalVolume(), visitor);
	}
	
	/**
	 * Visit the PhysicalVolume recursively using preorder,
	 * calling the given IPhysicalVolumeVisitor's visit method
	 * for each node.
	 * 
	 * @param physicalVolume
	 * @param visitor
	 */
	protected void traversePreOrder(
			IPhysicalVolume physicalVolume, 
			IPhysicalVolumeVisitor visitor)
	{
		// Visit this node.
		visitor.visit(physicalVolume);
		
		// Recursively traverse the daughters.
		if ( physicalVolume.getLogicalVolume().getNumberOfDaughters() > 0 )
		{
			for ( IPhysicalVolume child : physicalVolume.getLogicalVolume().getDaughters())
			{
				traversePreOrder(child, visitor);
			}
		}		
	}
		
	/**
	 * Visit each PhysicalVolume recursively using postorder,
	 * calling the given IPhysicalVolumeVisitor's visit method
	 * for each node.
	 * 
	 * @param physicalVolume
	 * @param visitor
	 */
	protected void traversePostOrder(IPhysicalVolume physicalVolume, IPhysicalVolumeVisitor visitor)
	{		
		// Recursively traverse the daughters.
		if ( physicalVolume.getLogicalVolume().getNumberOfDaughters() > 0 )
		{
			for ( IPhysicalVolume child : physicalVolume.getLogicalVolume().getDaughters())
			{
				traversePostOrder(child, visitor);
			}
		}
		
		// Visit this node.
		visitor.visit(physicalVolume);		
	}	
	
	/**
	 * Visit the top volume recursively using postorder,
	 * calling the given IPhysicalVolumeVisitor's visit method
	 * for each node.
	 * 
	 * @param visitor A visitor to process the nodes.
	 */
	
	public void traversePostOrder(IPhysicalVolumeVisitor visitor)
	{
		traversePostOrder(getTopPhysicalVolume(), visitor);
	}	
	
	public String getName()
	{
		return name;
	}
	
	/**
	 * Utility method for getting String paths to leaf volumes.
	 * @param paths List of paths passed in by caller.
	 * @param node The top node to traverse.
	 * @param path The recursive path String.
	 */
	public static void getLeafPaths(Collection<String> paths, IPhysicalVolume node, String path)
    {
        if (node == null)
        {
            return;
        }
        if (node.getLogicalVolume().getDaughters().size() == 0)
        {
            paths.add(path + "/" + node.getName());
            return;
        }
        
        path += "/" + node.getName();
        for (IPhysicalVolume dau : node.getLogicalVolume().getDaughters())
        {
            getLeafPaths(paths, dau, path);
        }
    }
}