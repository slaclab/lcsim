/*
 * ClusterCalibrationAnalysisDriver.java
 *
 * Created on May 22, 2008, 9:37 AM
 *
 * $Id: ClusterCalibrationAnalysisDriver.java,v 1.1 2008/05/27 18:12:21 ngraf Exp $
 */

package org.lcsim.cal.calib;

import hep.aida.ITree;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsManager.ConditionsSetNotFoundException;
import org.lcsim.conditions.ConditionsSet;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;
import org.lcsim.event.EventHeader;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.geometry.Subdetector;
import org.lcsim.recon.cluster.fixedcone.FixedConeClusterer;
import org.lcsim.recon.cluster.fixedcone.FixedConeClusterer.FixedConeDistanceMetric;
import org.lcsim.spacegeom.CartesianPoint;
import org.lcsim.spacegeom.SpacePoint;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;

import static java.lang.Math.sin;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
/**
 *
 * @author Norman Graf
 */
public class ClusterCalibrationAnalysisDriver extends Driver
{
    private ConditionsSet _cond;
    private FixedConeClusterer _fcc;
    private CollectionManager _collectionmanager = CollectionManager.defaultInstance();
    
    
    private AIDA aida = AIDA.defaultInstance();
    private ITree _tree;
    
    private double[] _ecalLayering;
    boolean _useFirstLayer;
    
    Map<String, Double> _fitParameters = new HashMap<String, Double>();
    
    private boolean _initialized;
    /** Creates a new instance of ClusterCalibrationAnalysisDriver */
    public ClusterCalibrationAnalysisDriver()
    {
        _tree = aida.tree();
    }
    
    protected void process(EventHeader event)
    {
        if(!_initialized)
        {
            ConditionsManager mgr = ConditionsManager.defaultInstance();
            try
            {
                _cond = mgr.getConditions("CalorimeterCalibration");
            }
            catch(ConditionsSetNotFoundException e)
            {
                System.out.println("ConditionSet CalorimeterCalibration not found for detector "+mgr.getDetector());
                System.out.println("Please check that this properties file exists for this detector ");
            }
            double radius = .5;
            double seed = 0.;//.1;
            double minE = .05; //.25;
            _fcc = new FixedConeClusterer(radius, seed, minE, FixedConeDistanceMetric.DPHIDTHETA);
            
            _ecalLayering = _cond.getDoubleArray("ECalLayering");
            _useFirstLayer = _cond.getDouble("IsFirstEmLayerSampling")==1.;
            
            // photons
            String photonFitParametersList = _cond.getString("PhotonFitParameters");
            String[]  photonFitParameters = photonFitParametersList.split(",\\s");
            for(int i=0; i<photonFitParameters.length; ++i)
            {
                _fitParameters.put(photonFitParameters[i], _cond.getDouble(photonFitParameters[i]));
            }
            // neutral hadrons
            String hadronFitParametersList = _cond.getString("NeutralHadronFitParameters");
            String[]  hadronFitParameters = hadronFitParametersList.split(",\\s");
            for(int i=0; i<hadronFitParameters.length; ++i)
            {
                _fitParameters.put(hadronFitParameters[i], _cond.getDouble(hadronFitParameters[i]));
            }
            
            _initialized = true;
        }
        
        String processedHitsName = _cond.getString("ProcessedHitsCollectionName");
        List<CalorimeterHit> hitsToCluster = _collectionmanager.getList(processedHitsName);
        // cluster the hits
        List<Cluster> clusters = _fcc.createClusters(hitsToCluster);
        String type = "gamma";
        for(Cluster c : clusters)
        {
            aida.cloud1D("uncorrected cluster energy for all clusters").fill(c.getEnergy());
            double e = correctClusterEnergy(c, type);
            aida.cloud1D("corrected cluster energy for all clusters").fill(e);
            SpacePoint p = new CartesianPoint(c.getPosition());
            double clusterTheta = p.theta();
            aida.cloud2D("uncorrected cluster energy for all clusters vs theta").fill(clusterTheta, c.getEnergy());
            aida.cloud2D("corrected cluster energy for all clusters vs theta").fill(clusterTheta, e);
            aida.cloud2D("raw-corrected cluster energy for all clusters vs theta").fill(clusterTheta, c.getEnergy()-e);         
        }        
    }
    
    private double correctClusterEnergy(Cluster c, String type)
    {
        double e = 0.;
        // brute force for the time being
        // in the future we will either move this to the sampling fraction corrections
        // or use the cluster direction for calculating the angle effect.
        List<CalorimeterHit> hits = c.getCalorimeterHits();
        for (CalorimeterHit hit : hits)
        {
            Subdetector det = hit.getSubdetector();
            String detName = det.getName();
//            System.out.println(detName);
            boolean isEM = detName.startsWith("EM");
            boolean isEndcap = det.isEndcap();
            IDDecoder decoder = hit.getIDDecoder();
            decoder.setID(hit.getCellID());
            int layer = decoder.getLayer();
            int caltype = 0;
            if(isEM)
            {
            for(int i=1; i<_ecalLayering.length+1; ++i)
            {
                if(layer >= _ecalLayering[i-1]) caltype = i-1;
            }
            }
            //TODO change this when had cal layering changes...
            else
            {
                caltype  = 3;
            }
//            System.out.println("layer= "+layer+" caltype= "+caltype);
            String name = type + "_" +(isEM ? "em"+caltype : "had") + (isEndcap ? "e" : "b");
//            System.out.println("fit parameter name "+name);
            SpacePoint p = new CartesianPoint(hit.getPosition());
            double hitTheta = p.theta();
            double normalTheta = hitTheta;
            // now calculate normal to the sampling plane
            if(isEndcap)
            {
                normalTheta -= PI/2.;
                normalTheta = abs(normalTheta);
            }
            else
            {
                normalTheta -= PI;
            }
            double a = 0.;
            double b = 0.;
            if(caltype==0 && !_useFirstLayer)
            {
                a = 0.;
                b = 1.;
            }
            else
            {
                a = _fitParameters.get(name+"_0");
                b = _fitParameters.get(name+"_1");
            }
            
            double correctionFactor = a + b*sin(normalTheta);
            aida.cloud2D(name+" "+layer+"hit Theta vs correction factor ").fill(hitTheta, correctionFactor);
            aida.cloud2D(name+" hit Theta vs correction factor ").fill(hitTheta, correctionFactor);
            if(isEM) aida.cloud2D("EM layer vs caltype").fill(layer, caltype);
            
            // now apply the correction
            
            e += hit.getRawEnergy()/correctionFactor;
        }
        return e;
    }
}
