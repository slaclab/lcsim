package org.lcsim.detector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhysicalVolumeNavigatorStore 
extends ArrayList<IPhysicalVolumeNavigator>
implements IPhysicalVolumeNavigatorStore
{
    Map<String,IPhysicalVolumeNavigator> navigators = new HashMap<String,IPhysicalVolumeNavigator>();    
    private static PhysicalVolumeNavigatorStore store;
    private String defaultNavigator="default";
    
    public static IPhysicalVolumeNavigatorStore getInstance()
    {
        if ( store == null )
        {
            store = new PhysicalVolumeNavigatorStore();
        }
        return store;        
    }
    
    public boolean add(IPhysicalVolumeNavigator nav)
    {
        add(nav,nav.getName());
        return true;
    }
    
    public void add(IPhysicalVolumeNavigator nav, String name)
    {   
        navigators.put(name, nav);
    }
    
    public IPhysicalVolumeNavigator createDefault(IPhysicalVolume topVolume)
    {
        //System.out.println("PhysicalVolumeNavigatorStore.createDefault");
        return create("default",topVolume);
    }
    
    public IPhysicalVolumeNavigator create(String name, IPhysicalVolume topVolume)
    {
        //System.out.println("PhysicalVolumeNavigatorStore.create - " + name);
        IPhysicalVolumeNavigator nav = new PhysicalVolumeNavigator(name, topVolume);
        add(nav, name);
        return nav;
    }
    
    public IPhysicalVolumeNavigator get(String name)
    {
        return navigators.get(name);
    }
    
    public IPhysicalVolumeNavigator get(IPhysicalVolume world)
    {
        List<IPhysicalVolumeNavigator> search = find(world);
        IPhysicalVolumeNavigator nav;
        if ( search.size() == 0 )
        {
            nav = new PhysicalVolumeNavigator(defaultNavigator, world);
        }
        else {
            nav = search.get(0);
        }                
        return nav;
    }
    
    public List<IPhysicalVolumeNavigator> find(IPhysicalVolume world)
    {
        List<IPhysicalVolumeNavigator> navList = new ArrayList<IPhysicalVolumeNavigator>();
        for ( IPhysicalVolumeNavigator nav : navList )
        {
            if ( nav.getTopPhysicalVolume() == world )
            {
                navList.add(nav);
            }
        }
        return navList;
    }
    
    public IPhysicalVolumeNavigator getDefaultNavigator()
    {
        if ( !navigators.containsKey(defaultNavigator) )
        {
            throw new RuntimeException("No default navigator was found!");
        }
        else {
            return navigators.get(defaultNavigator);
        }
    }           
    
    public void reset()
    {
        this.clear();
        navigators = new HashMap<String,IPhysicalVolumeNavigator>();        
    }
}