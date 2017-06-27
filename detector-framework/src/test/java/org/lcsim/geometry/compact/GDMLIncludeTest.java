package org.lcsim.geometry.compact;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.lcsim.geometry.compact.converter.lcdd.Main;
import org.lcsim.util.test.TestUtil.TestOutputFile;

public class GDMLIncludeTest extends TestCase
{        
    public void testMerge() throws Exception
    {
        InputStream in = GDMLIncludeTest.class.getResourceAsStream("/org/lcsim/geometry/compact/GDMLIncludeTest.xml");
        OutputStream out = new BufferedOutputStream(new FileOutputStream(new TestOutputFile("GDMLIncludeTest.lcdd")));
        new Main().convert("GDMLIncludeTest",in,out);        
    }    
}
