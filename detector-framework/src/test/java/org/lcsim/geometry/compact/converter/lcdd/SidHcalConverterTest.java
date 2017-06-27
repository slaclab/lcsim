package org.lcsim.geometry.compact.converter.lcdd;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.lcsim.util.test.TestUtil.TestOutputFile;

/**
 * This test generates an LCDD file for a test SidHcal.
 *
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: SidHcalConverterTest.java,v 1.2 2010/12/07 20:15:45 jeremy Exp $
 */
public class SidHcalConverterTest extends TestCase
{                
    public void testSiDHcalConverter() throws Exception
    {
        InputStream in = 
                PolyhedraBarrelCalorimeterConverterTest.class.
                getResourceAsStream("/org/lcsim/geometry/subdetector/SiDHcalTest.xml");
        OutputStream out = new BufferedOutputStream(new FileOutputStream(new TestOutputFile("SiDHcalTest.lcdd")));
        new Main().convert("SiDHcalTest",in,out);
    }
}
