package org.lcsim.cal.calib;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsManager.ConditionsSetNotFoundException;
import org.lcsim.conditions.ConditionsSet;
import org.lcsim.geometry.Calorimeter;
import org.lcsim.geometry.Calorimeter.CalorimeterType;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.compact.converter.pandora.CalorimeterConditions;
import org.lcsim.geometry.compact.converter.pandora.SamplingLayerRange;
import org.lcsim.geometry.compact.converter.pandora.SamplingLayers;
import org.lcsim.geometry.util.DetectorLocator;

/**
 * Load CalorimeterConditions data for 'sidloi3' and print it out.
 * @author Jeremy McCormick
 */
public class CalorimeterConditionsTest extends TestCase 
{
	public void testLoadCalorimeterConditions() throws Exception 
	{	
		// Get detector.
		Detector detector = DetectorLocator.findDetector("sidloi3");
		
		// Get CalorimeterCalibration conditions.
		ConditionsManager mgr = ConditionsManager.defaultInstance();
		ConditionsSet cs = null;
        try
        {
            cs = mgr.getConditions("CalorimeterCalibration");
        }
        catch(ConditionsSetNotFoundException e)
        {
            System.out.println("ConditionSet CalorimeterCalibration not found for detector "+ mgr.getDetector());
            System.out.println("Please check that the file CaloriemterCalibration.properties exists in this detector's conditions zip file.");
        }

        // Add CalorimeterTypes to check.
		List<Calorimeter.CalorimeterType> calTypes = new ArrayList<Calorimeter.CalorimeterType>();
		calTypes.add(CalorimeterType.EM_BARREL);
		calTypes.add(CalorimeterType.EM_ENDCAP);
		calTypes.add(CalorimeterType.HAD_BARREL);
		calTypes.add(CalorimeterType.HAD_ENDCAP);
		calTypes.add(CalorimeterType.MUON_BARREL);
		calTypes.add(CalorimeterType.MUON_ENDCAP);
		
		// Make conditions object for CalorimeterTypes. 
		for (Calorimeter.CalorimeterType calType : calTypes) 
		{
			System.out.println("Reading CalorimeterConditions for " + calType.toString() + " ... ");
			Calorimeter cal = detector.getCalorimeterByType(calType);
			CalorimeterConditions calCond = new CalorimeterConditions(cal, cs);
			System.out.println("mipCut = " + calCond.getMipCut());
			System.out.println("mipEnergy = " + calCond.getMipEnergy());
			System.out.println("mipSigma = " + calCond.getMipSigma());
			System.out.println("timeCut = " + calCond.getTimeCut());
			SamplingLayers layers = calCond.getSamplingLayers();
			for (SamplingLayerRange range: layers)
			{
				System.out.println("layers " + range.getLowerLayer() + " - " + range.getUpperLayer() + "; EM = " + range.getEMSampling() + "; HAD = " + range.getHADSampling());
			} 
			System.out.println("Done reading Calorimeter " + cal.getName());
			System.out.println("-----------------------");
		}
	}
}
