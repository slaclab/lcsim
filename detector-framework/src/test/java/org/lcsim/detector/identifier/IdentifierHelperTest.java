package org.lcsim.detector.identifier;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * 
 * This is an an integration test for the {@link org.lcsim.detector.identifier} 
 * package using methods in {@link IIdentifierHelper}.
 * 
 * @author Jeremy McCormick
 * @version $Id: IdentifierHelperTest.java,v 1.11 2011/02/25 03:09:38 jeremy Exp $
 */

public class IdentifierHelperTest extends TestCase
{
    public IdentifierHelperTest(String name)
    {
        super(name);
    }

    public static junit.framework.Test suite()
    {
        return new TestSuite(IdentifierHelperTest.class);
    }

    public void testFieldPack() throws Exception
    {
        List<IIdentifierField> fields = new ArrayList<IIdentifierField>();
        fields.add(new IdentifierField("field1", 16, 0, false));
        IIdentifierDictionary iddict = new IdentifierDictionary("test3", fields);
        IdentifierHelper helper = new IdentifierHelper(iddict);
        IIdentifier check = helper.pack(new ExpandedIdentifier("/10"));

        IIdentifier id = new Identifier();
        iddict.getField(0).pack(10, id);

        assertEquals("Field packed id does not match compact!", id, check);
    }

    public void testIdContext()
    {
        int i1[] = new int[3];
        i1[0] = 2;
        i1[1] = 3;
        i1[2] = 4;

        IdentifierContext c1 = new IdentifierContext(i1);
        assertTrue(c1.isRange());
        assertTrue(c1.getStartIndex() == 2);
        assertTrue(c1.getEndIndex() == 4);
        assertTrue(c1.isValidIndex(2));
        assertTrue(c1.isValidIndex(3));
        assertTrue(c1.isValidIndex(4));
        assertTrue(!c1.isValidIndex(0));
        assertTrue(!c1.isValidIndex(5));
        assertTrue(c1.getNumberOfIndices() == 3);

        IdentifierContext c2 = new IdentifierContext(2, 4);
        assertTrue(c2.isRange());
        assertTrue(c2.getStartIndex() == 2);
        assertTrue(c2.getEndIndex() == 4);
        assertTrue(c2.isValidIndex(2));
        assertTrue(c2.isValidIndex(3));
        assertTrue(c2.isValidIndex(4));
        assertTrue(!c2.isValidIndex(0));
        assertTrue(!c2.isValidIndex(5));
        assertTrue(c2.getNumberOfIndices() == 3);

        int i2[] = new int[3];
        i2[0] = 0;
        i2[1] = 2;
        i2[2] = 4;
        IdentifierContext c3 = new IdentifierContext(i2);
        assertTrue(!c3.isRange());
        assertTrue(c3.getStartIndex() == 0);
        assertTrue(c3.getEndIndex() == 4);
        assertTrue(c3.isValidIndex(0));
        assertTrue(c3.isValidIndex(2));
        assertTrue(c3.isValidIndex(4));
        assertTrue(!c3.isValidIndex(5));
        assertTrue(!c3.isValidIndex(1));
        assertTrue(!c3.isValidIndex(3));
        assertTrue(c3.getNumberOfIndices() == 3);
    }

    public void testUnpack() throws Exception
    {
        List<IIdentifierField> fields = new ArrayList<IIdentifierField>();
        fields.add(new IdentifierField("field1", 8, 0, false));
        fields.add(new IdentifierField("field2", 8, 8, true));
        fields.add(new IdentifierField("field3", 8, 16, false));
        IIdentifierDictionary iddict = new IdentifierDictionary("test2", fields);
        IdentifierHelper helper = new IdentifierHelper(iddict);

        IExpandedIdentifier expId = new ExpandedIdentifier();
        expId.addValue(1);
        expId.addValue(-1);
        expId.addValue(2);

        IIdentifier id = helper.pack(expId);

        IExpandedIdentifier expIdCheck1 = helper.unpack(id);
        assertTrue(expIdCheck1.toString().equals("/1/-1/2"));

        /*
        IExpandedIdentifier expIdCheck2 = helper.getIdentifierDictionary().unpack(id, 1);
        assertTrue(expIdCheck2.toString().equals("/0/-1/2"));

        IExpandedIdentifier expIdCheck3 = helper.getIdentifierDictionary().unpack(id, 0, 1);
        assertTrue(expIdCheck3.toString().equals("/1/-1/0"));
        */
    }

    public void testPack() throws Exception
    {
        List<IIdentifierField> fields = new ArrayList<IIdentifierField>();
        fields.add(new IdentifierField("field1", 8, 0, false));
        fields.add(new IdentifierField("field2", 8, 8, true));
        IIdentifierDictionary iddict = new IdentifierDictionary("test3", fields);
        IIdentifierHelper helper = new IdentifierHelper(iddict);

        IExpandedIdentifier expId = new ExpandedIdentifier();
        expId.addValue(10);
        expId.addValue(-11);

        IIdentifier compactId = helper.pack(expId);

        IExpandedIdentifier expIdCheck = helper.unpack(compactId);

        assertTrue("ExpandedIdentifiers do not match!", expId.equals(expIdCheck));

        expId.clear();
        expId.addValue(10);
        expId.addValue(11);

        compactId = helper.pack(expId);
        expIdCheck = helper.unpack(compactId);

        assertTrue("ExpandedIdentifiers do not match!", expId.equals(expIdCheck));
    }
}