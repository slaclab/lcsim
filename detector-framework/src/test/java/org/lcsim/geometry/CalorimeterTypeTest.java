package org.lcsim.geometry;

import static org.lcsim.geometry.Calorimeter.CalorimeterType.BEAM;
import static org.lcsim.geometry.Calorimeter.CalorimeterType.EM_BARREL;
import static org.lcsim.geometry.Calorimeter.CalorimeterType.EM_ENDCAP;
import static org.lcsim.geometry.Calorimeter.CalorimeterType.HAD_BARREL;
import static org.lcsim.geometry.Calorimeter.CalorimeterType.HAD_ENDCAP;
import static org.lcsim.geometry.Calorimeter.CalorimeterType.LUMI;
import static org.lcsim.geometry.Calorimeter.CalorimeterType.MUON_BARREL;
import static org.lcsim.geometry.Calorimeter.CalorimeterType.MUON_ENDCAP;

import java.io.InputStream;

import junit.framework.TestCase;

public class CalorimeterTypeTest extends TestCase 
{
    public void testCalorimeterType() throws Exception
    {        
        InputStream in = GeometryReaderTest.class.getResourceAsStream("sidloi3_compact.xml");
        GeometryReader reader = new GeometryReader();
        Detector detector = reader.read(in);
        
        for (Subdetector subdet : detector.getSubdetectors().values())
        {
            if (subdet.isCalorimeter())
            {
                Calorimeter.CalorimeterType calType = 
                    ((Calorimeter)subdet).getCalorimeterType();
                String detName = subdet.getName();
                
                if (calType == HAD_BARREL)
                {
                    assertEquals(detName,"HcalBarrel");
                }
                else if (calType == HAD_ENDCAP)
                {
                    assertEquals(detName,"HcalEndcap");
                }
                else if (calType == EM_BARREL)
                {
                    assertEquals(detName,"EcalBarrel");
                }
                else if (calType == EM_ENDCAP)
                {
                    assertEquals(detName,"EcalEndcap");
                }
                else if (calType == MUON_BARREL)
                {
                    assertEquals(detName,"MuonBarrel");
                }
                else if (calType == MUON_ENDCAP)
                {                    
                    assertEquals(detName,"MuonEndcap");
                }
                else if (calType == LUMI)
                {
                    assertEquals(detName,"LumiCal");
                }
                else if (calType == BEAM)
                {
                    assertEquals(detName,"BeamCal");
                }                
            }
        }        
    }
}
