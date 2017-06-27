/*
 * CollectionManager.java
 *
 * Created on May 22, 2008, 6:46 PM
 *
 * $Id: CollectionManager.java,v 1.2 2008/06/06 15:45:47 ngraf Exp $
 */

package org.lcsim.cal.calib;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lcsim.event.CalorimeterHit;

/**
 *
 * @author Norman Graf
 */
public class CollectionManager
{
    private Map<String, List<CalorimeterHit>> _listMap = new HashMap<String, List<CalorimeterHit>>();
    private boolean _debug = false;

    private static CollectionManager _theCollectionManager;
    /** Creates a new instance of CollectionManager */
    protected CollectionManager()
    {
    }
    
    public static CollectionManager defaultInstance()
    {
        if(_theCollectionManager == null) _theCollectionManager = new CollectionManager();
        return _theCollectionManager;      
    }
    
    public void addList(String name, List<CalorimeterHit> hits)
    {
        if(_debug) System.out.println("Adding "+name+ " to manager");
        _listMap.put(name, hits);
    }
    
    public List<CalorimeterHit> getList( String name)
    {
        return _listMap.get(name);
    }
    
}
