/*
 * ForwardDetectorCnvTest.java
 *
 * Created on June 16, 2005, 3:23 PM
 */

package org.lcsim.geometry.compact.converter.lcdd;

import java.io.InputStream;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author jeremym
 */
public class ForwardDetectorCnvTest extends TestCase
{
    
    /** Creates a new instance of ForwardDetectorCnvTest */
    public ForwardDetectorCnvTest()
    {
    }
    
    public static TestSuite suite()
    {
        return new TestSuite(ForwardDetectorCnvTest.class);
    }
    
    public void testForwardDetectorCnv() throws Exception
    {
      InputStream in = ForwardDetectorCnvTest.class.getResourceAsStream("/org/lcsim/geometry/subdetector/ForwardDetectorTest.xml");
      new Main().convert("ForwardDetectorTest",in,null);      
    }  
}
