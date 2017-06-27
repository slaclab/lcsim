package org.lcsim.detector.identifier;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Tests for {@link IdentifierDictionary}.
 * 
 * @author jeremym
 * @version $Id: IdentifierDictionaryTest.java,v 1.1 2011/03/11 19:22:21 jeremy Exp $
 */
public class IdentifierDictionaryTest extends TestCase
{
    /**
     * Some "sanity checks" of IdentifierDictionary.
     */
    public void testBasicIdDict()
    { 
        List<IIdentifierField> fields = new ArrayList<IIdentifierField>();
        IIdentifierField f1 = new IdentifierField("f1", 6, 0, false);
        IIdentifierField f2 = new IdentifierField("f2", 6, 6, false);
        IIdentifierField f3 = new IdentifierField("f3", 6, 12, false);
        fields.add(f1);
        fields.add(f2);
        fields.add(f3);
        IdentifierDictionary dict = new IdentifierDictionary("testDict", fields);
        
        assertEquals(dict.getNumberOfFields(), 3);
        
        assertTrue(dict.hasField("f1"));
        assertTrue(dict.hasField("f2"));
        assertTrue(dict.hasField("f3"));
        
        assertEquals(dict.getFieldIndex("f1"), 0);
        assertEquals(dict.getFieldIndex("f2"), 1);
        assertEquals(dict.getFieldIndex("f3"), 2);
        
        assertEquals(dict.getField("f1"), f1);
        assertEquals(dict.getField("f2"), f2);
        assertEquals(dict.getField("f3"), f3);
        
        assertEquals(dict.getField(0), f1);
        assertEquals(dict.getField(1), f2);
        assertEquals(dict.getField(2), f3);
    }

}
