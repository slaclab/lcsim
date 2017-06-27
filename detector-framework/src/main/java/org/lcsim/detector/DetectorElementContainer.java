package org.lcsim.detector;

import java.util.ArrayList;

import org.lcsim.detector.identifier.IIdentifier;

/**
 * Implementation of {@link IDetectorElementContainer}.
 */
public class DetectorElementContainer
extends ArrayList<IDetectorElement>
implements IDetectorElementContainer
{       
    public IDetectorElementContainer find(String name)
    {
        IDetectorElementContainer matches = new DetectorElementContainer();
        for ( IDetectorElement de : this )
        {
            if (de.getName().equals(name))
            {
                matches.add(de);
            }
        }
        return matches;
    }   
    
    public IDetectorElementContainer find(IIdentifier id)
    {
        IDetectorElementContainer matches = new DetectorElementContainer();
        if (id != null) 
        {
            for (IDetectorElement de : this)
            {
                if (de.getIdentifier().isValid())
                {
                    if (de.getIdentifier().equals(id))
                    {
                        matches.add(de);
                    }
                }
            }
        }
        return matches;
    }
                
    public IDetectorElementContainer find(Class<? extends IDetectorElement> klass)
    {
        DetectorElementContainer matches = new DetectorElementContainer();
        for (IDetectorElement de : this)
        {
            for (Class checkClass : de.getClass().getClasses() )
            {
                if (checkClass.equals(klass))
                {
                    matches.add(de);
                }
            }
        }
        return matches;
    }   
    
    /**
     * Current implementation does not allow duplicate name, id, or object.
     */
    // FIXME: Good checks but very slow!!!
    /*
    public boolean add(IDetectorElement de)
    {                          
        if (de == null)
        {
            throw new IllegalArgumentException("The DetectorElement argument points to null!");
        }

        if (contains(de))
        {
            throw new IllegalArgumentException("The DetectorElement called <"+de.getName()+"> has already been registered.");            
        }        

        if (find(de.getName()).size() > 0)
        {
            throw new IllegalArgumentException("Duplicate DetectorElement name <"+de.getName()+">.");
        }
        
        return super.add(de);
    }
    */
    
    public String toString()
    {
    	StringBuffer buff = new StringBuffer();
    	for (IDetectorElement de : this)
    	{
    		buff.append(de.getName()+'\n');    		
    	}
    	return buff.toString();
    }
}
