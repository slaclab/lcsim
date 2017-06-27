/*
 * ClusterCalibrationAnalysisDriverTest.java
 * JUnit based test
 *
 * Created on May 22, 2008, 11:41 AM
 */

package org.lcsim.cal.calib;

import java.util.HashMap;
import java.util.Map;
import junit.framework.*;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsManager.ConditionsSetNotFoundException;
import org.lcsim.conditions.ConditionsSet;


import static java.lang.Math.sin;
import static java.lang.Math.PI;

/**
 *
 * @author ngraf
 */
public class ClusterCalibrationAnalysisDriverTest extends TestCase
{
    //TODO add some assertions to be able to fail if things go wrong
    public void testConditionsHandler() throws Exception
    {
        boolean debug = false;
        ConditionsManager mgr = ConditionsManager.defaultInstance();
        mgr.setDetector("sidloi3", 0);
        ConditionsSet cs = null;
        try
        {
            cs = mgr.getConditions("CalorimeterCalibration");
        }
        catch(ConditionsSetNotFoundException e)
        {
            System.out.println("ConditionSet CalorimeterCalibration not found for detector "+mgr.getDetector());
            System.out.println("Please check that this properties file exists for this detector ");
        }
        if(debug) System.out.println(" testing CalorimeterCalibration conditions");
        String collections = cs.getString("BaseHitCollectionNames");
        if(debug) System.out.println(collections);
        String[] collNames = collections.split(",\\s");
        if(debug)
        {
        for(int i=0; i<collNames.length; ++i)
        {
            System.out.println(collNames[i]);
        }
        System.out.println("ECalMip_Cut= "+cs.getDouble("ECalMip_Cut"));
        }
        
        // test sampling fraction handling...
        Map<String, Double> _fitParameters = new HashMap<String, Double>();
        // photons
        String photonFitParametersList = cs.getString("PhotonFitParameters");
        String[]  photonFitParameters = photonFitParametersList.split(",\\s");
        for(int i=0; i<photonFitParameters.length; ++i)
        {
            _fitParameters.put(photonFitParameters[i], cs.getDouble(photonFitParameters[i]));
        }
        // neutral hadrons
        String hadronFitParametersList = cs.getString("NeutralHadronFitParameters");
        String[]  hadronFitParameters = hadronFitParametersList.split(",\\s");
        for(int i=0; i<hadronFitParameters.length; ++i)
        {
            _fitParameters.put(hadronFitParameters[i], cs.getDouble(hadronFitParameters[i]));
        }
        
        double[] _ecalLayering = cs.getDoubleArray("ECalLayering");
        
        String[] types = {"gamma", "neutralHadron"};
        
        for(int i=0; i< types.length; ++i)
        {
            String type = types[i];
            
            // test the em barrel
            boolean isEM = true;
            boolean isEndcap = false;
            
            boolean useFirstLayer = cs.getDouble("IsFirstEmLayerSampling")==1.;
            if(useFirstLayer) if(debug) System.out.println("first layer is sampling");
            
            for (int layer = 0; layer<31; ++layer)
            {
                int caltype = 0;
                for(int j=1; j<_ecalLayering.length+1; ++j)
                {
                    if(layer >= _ecalLayering[j-1]) caltype = j-1;
                }
                
                if(debug) System.out.println("layer= "+layer+" caltype= "+caltype);
                String name = type + "_"+ (isEM ? "em"+caltype : "had") + (isEndcap ? "e" : "b");
                if(debug) System.out.println("fit parameter name "+name);
                
                double theta = PI/2.;
                // now calculate normal to the sampling plane
                if(isEndcap)
                {
                    theta -= PI/2.;
                }
                else
                {
                    theta -= PI;
                }
                double a = 0.;
                double b = 0.;
                if(caltype==0 && !useFirstLayer)
                {
                    a = 0.;
                    b = sin(theta);
                }
                else
                {
                    a = _fitParameters.get(name+"_0");
                    b = _fitParameters.get(name+"_1");
                }
                double correctionFactor = a + b*sin(theta);
                if(debug) System.out.println("theta= "+theta+" correction factor= "+correctionFactor);
                
            }
            
        } // end of loop over type

    }
}
