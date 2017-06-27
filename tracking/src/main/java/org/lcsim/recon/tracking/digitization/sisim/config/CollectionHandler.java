package org.lcsim.recon.tracking.digitization.sisim.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lcsim.util.Driver;

public abstract class CollectionHandler extends Driver
{
    protected Set<String> collections = new HashSet<String>();
    
    public CollectionHandler()
    {}
        
    public CollectionHandler(List<String> collectionNames)
    {        
        for (String collection : collectionNames)
        {
            this.collections.add(collection);
        }
    }
    
    public CollectionHandler(String[] collectionNames)
    {        
        for (String collection : collectionNames)
        {
            this.collections.add(collection);
        }
    }
    
    public void setCollections(String[] collectionNames)
    {
    	collections.addAll(Arrays.asList(collectionNames));
    }
    
    public void setCollection(String collection)
    {
    	collections.add(collection);
    }
 
    /**
     * Return whether this CollectionHandler can handle the collection called 
     * <code>collectionName</code>.  
     * @param collectionName
     * @return True if <code>collections</code> is empty or if 
     *         collections contains </code>collectionName</code>; 
     *         false if does not contain </code>collectionName</code>.
     */
    public boolean canHandle(String collectionName)
    {
    	if (collections.size() == 0) return true;
        return collections.contains(collectionName);
    }
}
