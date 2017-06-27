package org.lcsim.detector.identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link IIdentifierDictionaryManager}.
 *
 * @author Jeremy McCormick
 * @version $Id: IdentifierDictionaryManager.java,v 1.3 2007/05/11 00:21:09 jeremy Exp $
 */

public class IdentifierDictionaryManager
implements IIdentifierDictionaryManager
{
    Map<String,IIdentifierDictionary> dicts = new HashMap<String,IIdentifierDictionary>();
    private static final IIdentifierDictionaryManager instance = new IdentifierDictionaryManager();
    
    private IdentifierDictionaryManager()
    {}
    
    public void addIdentifierDictionary(IIdentifierDictionary dict)
    {
        dicts.put(dict.getName(), dict);
    }

    public IIdentifierDictionary getIdentifierDictionary(String name)
    {
        return dicts.get(name);        
    }       
    
    public static IIdentifierDictionaryManager getInstance()
    {
        return instance;
    } 

    public void clear()
    {
        dicts.clear();
    }
}
