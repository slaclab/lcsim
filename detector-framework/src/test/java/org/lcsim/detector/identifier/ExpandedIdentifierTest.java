package org.lcsim.detector.identifier;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Simple tests of the {@link ExpandedIdentifier} class.
 *
 * @author Jeremy McCormick
 * @version $Id: ExpandedIdentifierTest.java,v 1.1 2007/11/20 20:30:04 jeremy Exp $
 */

public class ExpandedIdentifierTest
extends TestCase
{    
    public void testEquals()
    {        
        ExpandedIdentifier id1 = new ExpandedIdentifier();
        id1.addValue(1);
        id1.addValue(2);
        ExpandedIdentifier id2 = new ExpandedIdentifier();
        id2.addValue(1);
        id2.addValue(2);
        
        assertTrue(id1.equals(id2));
        assertTrue(id2.equals(id1));
        
        Map<ExpandedIdentifier,Integer> testMap = new HashMap<ExpandedIdentifier,Integer>();
        
        testMap.put(id1, 1);
        testMap.put(id2, 2);
        
        assertTrue(testMap.size() == 1);
        assertTrue(testMap.get(id2) == 2);
    }
    
    public void testNotEquals()
    {       
        ExpandedIdentifier id1 = new ExpandedIdentifier();
        id1.addValue(1);
        id1.addValue(2);
        ExpandedIdentifier id2 = new ExpandedIdentifier();
        id2.addValue(1);
        id2.addValue(2);
        id2.addValue(3);
        
        assertTrue(!id1.equals(id2));
        assertTrue(!id2.equals(id1));
        
        Map<ExpandedIdentifier,Integer> testMap = new HashMap<ExpandedIdentifier,Integer>();
        
        testMap.put(id1, 1);
        testMap.put(id2, 2);
        
        assertTrue(testMap.size() == 2);
        assertTrue(testMap.get(id1) == 1);
        assertTrue(testMap.get(id2) == 2);       
    }
}
