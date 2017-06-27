/*
 * LimitSet.java
 *
 * Created on October 26, 2005, 5:30 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.lcsim.geometry.compact.converter.lcdd.util;

/**
 *
 * @author jeremym
 */
public class LimitSet extends RefElement
{
    public LimitSet(String name)
    {
        super("limitset",name);
    }
    
    public void addLimit(Limit limit)
    {
        addContent(limit);
    }
}