/*
 * CylindricalBarrelCalorimeterTest.java
 *
 * Created on June 15, 2005, 12:00 PM
 */

package org.lcsim.geometry.subdetector;

import hep.graphics.heprep.HepRepProvider;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.JDOMException;
import org.lcsim.geometry.Calorimeter;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.util.xml.ElementFactory.ElementCreationException;

import org.lcsim.geometry.layer.Layering;

/**
 *
 * @author jeremym
 */
public class CylindricalCalorimeterTest extends TestCase
{
    Detector detector;
    CylindricalBarrelCalorimeter barrel;
    CylindricalEndcapCalorimeter endcap;
    
    /** Creates a new instance of CylindricalBarrelCalorimeterTest */
    public CylindricalCalorimeterTest()
    {
    }
    
    protected void setUp() throws java.lang.Exception
    {
        InputStream in = this.getClass().getResourceAsStream("/org/lcsim/geometry/subdetector/CylindricalCalorimeterTest.xml");
        
        GeometryReader reader = new GeometryReader();
        Detector det = reader.read(in);
        
        assertTrue( det.getSubdetectors().get("EMBarrel") != null );
        assertTrue( det.getSubdetectors().get("EMEndcap") != null );
        
        org.lcsim.geometry.compact.Subdetector subdetEMBarrel = det.getSubdetectors().get("EMBarrel");
        org.lcsim.geometry.compact.Subdetector subdetEMEndcap = det.getSubdetectors().get("EMEndcap");
        
        //System.out.println("barr class:" + subdetEMBarrel.getClass() );
       
        if ( subdetEMBarrel == null )
        {
            throw new RuntimeException("EMBarrel failed cast to Subdetector.");
        }
        
        if (!( subdetEMBarrel instanceof CylindricalBarrelCalorimeter ))
        {
            throw new RuntimeException("EMBarrel is not an instance of CylindricalBarrelCalorimeter.");
        }
        
        if ( subdetEMEndcap == null )
        {
            throw new RuntimeException("EMEndcap failed cast to Subdetector.");
        }                
        
        if (!(subdetEMEndcap instanceof CylindricalEndcapCalorimeter ))
        {
            throw new RuntimeException("EMBarrel is not an instance of CylindricalBarrelCalorimeter.");            
        }
        
        barrel = (org.lcsim.geometry.subdetector.CylindricalBarrelCalorimeter) subdetEMBarrel;
        
        if ( barrel == null )
        {
            throw new RuntimeException("Failed cast to CylindricalBarrelCalorimeter.");
        }
        
        endcap = (org.lcsim.geometry.subdetector.CylindricalEndcapCalorimeter) subdetEMEndcap;
        
        if ( endcap == null )
        {
            throw new RuntimeException("Failed cast to CylindricalEndcapCalorimeter.");
        }
        
        return;
    }
    
    public static junit.framework.Test suite()
    {
        return new TestSuite(CylindricalCalorimeterTest.class);
    }
    
    public void testBarrel()
    {
        double zmax = barrel.getZMax();
        double zmin = barrel.getZMin();
        double orad = barrel.getOuterRadius();
        double irad = barrel.getInnerRadius();
        
        assertEquals(zmax,1795.0);
        assertEquals(zmin,-1795.0);        
        assertEquals(irad,1270.0);
        assertEquals(orad,1382.5);          
        assertEquals(barrel.getLayering().getLayers().getTotalThickness(), orad - irad);
    }
    
    public void testEndcap()
    {
        double zmax = endcap.getZMax();
        double zmin = endcap.getZMin();
        double orad = endcap.getOuterRadius();
        double irad = endcap.getInnerRadius();                
        
        assertEquals(zmax,1792.5);
        assertEquals(zmin,1680.0);        
        assertEquals(orad,1250.0);
        assertEquals(irad,200.0);            
        assertEquals(endcap.getLayering().getLayers().getTotalThickness(), zmax - zmin);
        
        //System.out.println("zmax zmin orad irad: " + zmax + " " + zmin + " " + orad + " " + irad);                
    }
}
