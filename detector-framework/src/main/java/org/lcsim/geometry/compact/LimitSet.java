package org.lcsim.geometry.compact;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jdom.Element;

/**
 *
 * @author jeremym
 */
public class LimitSet 
{
    private Map<String, Limit> limits = new HashMap<String, Limit>();
    private String name;
    
    protected LimitSet(Element node)
    {
        name = node.getAttributeValue("name");
        addLimits(node);
    }
    
    public void addLimit(String name, Limit limit)
    {
        limits.put(name, limit);
    }
    
    private void addLimits(Element node)
    {
        for (Iterator i = node.getChildren("limit").iterator(); i.hasNext(); )
        {
            Element e = (Element) i.next();
            Limit l = new Limit(e);
            addLimit(l.getName(), l);
        }
    }
    
    public Limit getLimit(String name)
    {
        return limits.get(name);
    }
    
    public Map<String, Limit> getLimits()
    {
        return limits;
    }
    
    public String getName()
    {
        return name;
    }
}