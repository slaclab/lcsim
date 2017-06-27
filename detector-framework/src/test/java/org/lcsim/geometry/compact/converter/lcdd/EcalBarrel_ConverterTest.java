package org.lcsim.geometry.compact.converter.lcdd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.lcsim.util.test.TestUtil.TestOutputFile;

/**
 *
 * @author jeremym
 */
public class EcalBarrel_ConverterTest extends TestCase
{       
    public static TestSuite suite()
    {
        return new TestSuite(EcalBarrel_ConverterTest.class);
    }
    
    public void testEcalBarrel_Converter() throws Exception
    {
      InputStream in = EcalBarrel_ConverterTest.class.getResourceAsStream("/org/lcsim/geometry/subdetector/EcalBarrelTest.xml");
      File file = new TestOutputFile("EcalBarrel_ConverterTest.lcdd");
      OutputStream out = new FileOutputStream(file);
      new Main().convert("EcalBarrel_ConverterTest",in,out);            
    }  
}
