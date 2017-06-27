package org.lcsim.detector;

/**
 * Implementation of {@link IParametersStore}.
 * 
 * @author Jeremy McCormick
 * @version $Id: ParametersStore.java,v 1.2 2007/05/25 20:16:26 jeremy Exp $
 */
public class ParametersStore
extends ObjectStore<IParameters>
implements IParametersStore
{
    private static IParametersStore store;
    public static IParametersStore getInstance()
    {
        if (store == null)
        {
            store = new ParametersStore();
        }
        return store;
    }
    
    public IParameters get(String name)
    {
        for (IParameters p : this)
        {
            if (p.getName().equals(name))
            {
                return p;
            }
        }
        return null;
    }    
}