package org.lcsim.detector.identifier;

import java.util.Arrays;

/**
 * Implementation of {@link IIdentifierContext}.
 *
 * @author Jeremy McCormick
 * @version $Id: IdentifierContext.java,v 1.2 2011/02/25 03:09:38 jeremy Exp $
 */

public class IdentifierContext
implements IIdentifierContext
{
	int[] indices;
	int startIndex;
	int endIndex;
	boolean isRange;
	
	public IdentifierContext(int startIndex, int endIndex)
	{		
		if (startIndex > endIndex)
			throw new IllegalArgumentException("startIndex bigger than endIndex!");
		isRange = true;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		indices = new int[endIndex - startIndex + 1];
		int index = startIndex;
		for (int i=0; i<indices.length; i++)
		{
			indices[i] = index;
			++index;
		}
	}
	
	public IdentifierContext(int[] indices)
	{
		this.indices = new int[indices.length];
		System.arraycopy(indices, 0, this.indices, 0, indices.length);
		Arrays.sort(indices);
		startIndex = indices[0];
		endIndex = indices[indices.length - 1];
		isRange = true;
		for (int i=0; i<indices.length; i++)
		{
			if (i != indices.length - 1)
			{
				if (indices[i+1] != indices[i] + 1)
				{
					// This means the indices are not a continuous range.
					isRange = false;
					break;
				}
			}
		}
	}
	
	public int getEndIndex() 
	{
		return endIndex;
	}

	public int getStartIndex() 
	{
		return startIndex;
	}
	
	public int[] getIndices() 
	{
		return indices;
	}

	public boolean isRange() 
	{
		return isRange;
	}

	public boolean isValidIndex(int index) 
	{	
		return Arrays.binarySearch(indices, index) > -1;
	}
	
	public int getIndex(int i) 
	{
		if (i < 0 || i > getNumberOfIndices() - 1)
			throw new IllegalArgumentException("The index " + i + " is invalid!");
		return indices[i];
	}

	public int getNumberOfIndices() 
	{
		return indices.length;
	}
}