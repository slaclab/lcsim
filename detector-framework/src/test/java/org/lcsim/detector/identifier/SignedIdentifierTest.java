package org.lcsim.detector.identifier;

/**
 * Test of signed identifier encoding and decoding.
 *
 * @author Jeremy McCormick
 * @version $Id: SignedIdentifierTest.java,v 1.3 2011/02/25 03:09:38 jeremy Exp $
 */
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class SignedIdentifierTest extends TestCase
{    
    public void testSignedId() throws Exception
    {
        List<IIdentifierField> fields = new ArrayList<IIdentifierField>();
        fields.add(new IdentifierField("f1", 8,  0,  true));
        fields.add(new IdentifierField("f2", 8,  8,  true));
        fields.add(new IdentifierField("f3", 8,  16, true));
        fields.add(new IdentifierField("f4", 8,  24, true));
        fields.add(new IdentifierField("f5", 8,  32, true));
        fields.add(new IdentifierField("f6", 8,  40, true));
        fields.add(new IdentifierField("f7", 8,  48, true));
        
        IIdentifierDictionary iddict = new IdentifierDictionary("test4", fields);
        IIdentifierHelper helper = new IdentifierHelper(iddict);        
          

        
        IExpandedIdentifier testId = new ExpandedIdentifier();
        testId.addValue(8);
        testId.addValue(-8);
        testId.addValue(9);
        testId.addValue(-9);
        testId.addValue(10);
        testId.addValue(-10);
        testId.addValue(11);
                
        IIdentifier packed = helper.pack(testId);                       
        IExpandedIdentifier unpacked = helper.unpack(packed);
                
        assertEquals(unpacked.getValue(0), 8);
        assertEquals(unpacked.getValue(1), -8);
        assertEquals(unpacked.getValue(2), 9);
        assertEquals(unpacked.getValue(3), -9);
        assertEquals(unpacked.getValue(4), 10);      
        assertEquals(unpacked.getValue(5), -10);
        assertEquals(unpacked.getValue(6), 11);
    }
}
