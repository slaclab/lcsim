/*
 * ProcessHitsDriver.java
 *
 * Created on May 22, 2008, 12:10 PM
 *
 * $Id: ProcessHitsDriver.java,v 1.2 2008/06/06 14:58:29 ngraf Exp $
 */

package org.lcsim.cal.calib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsManager.ConditionsSetNotFoundException;
import org.lcsim.conditions.ConditionsSet;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.EventHeader;
import org.lcsim.event.base.BaseCalorimeterHit;
import org.lcsim.util.Driver;

/**
 *
 * @author Norman Graf
 */
public class ProcessHitsDriver extends Driver
{
    private ConditionsSet _cond;
    private boolean _initialized;
    
    private String[] _hitCollectionNames;
    private List<String> _emCalNames = new ArrayList<String>();
    private List<String> _hadCalNames = new ArrayList<String>();
    
    private double _timeCut;
//    private int _isolationMinNeighbors;
    private int _hadCalorimeterMaxLayer;
    
    private double _ECalMipCut;
    private double _HCalMipCut;
    private double[] _rawMipCut;
    
    private CollectionManager _collectionmanager = CollectionManager.defaultInstance();
    
    /** Creates a new instance of ProcessHitsDriver */
    public ProcessHitsDriver()
    {
    }
    
    protected void process(EventHeader event)
    {
        super.process(event);
        
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
            
            
            // The SimCalorimeterHit collections to process...
            String names = _cond.getString("BaseHitCollectionNames");
            
            _hitCollectionNames = names.split(",\\s");
            
            names = _cond.getString("HadCalorimeterCollections");
            String[] hadCalCollectionNames = names.split(",\\s");
            for(int i=0; i<hadCalCollectionNames.length; ++i)
            {
                _hadCalNames.add(hadCalCollectionNames[i]);
            }
            
            //TODO allow eachhad calorimeter to have its own max layer.
            // may want to study barrel and endcap separately.
            _hadCalorimeterMaxLayer = _cond.getInt("HadCalorimeterMaxLayer");
            
            names = _cond.getString("EMCalorimeterCollections");
            String[] emCalCollectionNames = names.split(",\\s");
            for(int i=0; i<emCalCollectionNames.length; ++i)
            {
                _emCalNames.add(emCalCollectionNames[i]);
            }
            
            // cut on late times
            _timeCut = _cond.getDouble("timeCut");
            
            // cut on small energy depositions
            _ECalMipCut = _cond.getDouble("ECalMip_Cut");
            _HCalMipCut = _cond.getDouble("HCalMip_Cut");
            
//            // isolation cut on number of neighbors
//            _isolationMinNeighbors = _cond.getInt("isolationMinNeighbors");
            
            // use same values for barrel and endcap
            _rawMipCut = new double[_hitCollectionNames.length];
            for(int i=0; i<_rawMipCut.length; ++i)
            {
                if(_emCalNames.contains(_hitCollectionNames[i]))
                {
                    _rawMipCut[i] = _ECalMipCut;
                }
                else if (_hadCalNames.contains(_hitCollectionNames[i]))
                {
                    _rawMipCut[i] = _HCalMipCut;
                }
            }
            _initialized = true;
        }
        
        // All the calorimeter hits keyed by their ID
//        Map<Long, CalorimeterHit> allHitsMap = new HashMap<Long, CalorimeterHit>();
//        for(String name : _hitCollectionNames)
//        {
//            List<CalorimeterHit> hits = event.get(CalorimeterHit.class, name);
//            for(CalorimeterHit hit : hits)
//            {
//                long id = hit.getCellID();
//                allHitsMap.put(id, hit);
//            }
//        }
        
        // The list of all of the calorimeter hits after cuts...
        List<CalorimeterHit> hitsToCluster = new ArrayList<CalorimeterHit>();
        int i = 0;
        
        for(String name : _hitCollectionNames)
        {
            boolean isHadCal = _hadCalNames.contains(name);
            List<CalorimeterHit> hits = event.get(CalorimeterHit.class, name);
            // let's look at the hits and see if we need to cut on energy or time...
            // continue out of the loop to fail fast
            for(CalorimeterHit hit: hits)
            {
                BaseCalorimeterHit h = (BaseCalorimeterHit) hit;
                
                //throw out low energy hits
                if(h.getRawEnergy() < _rawMipCut[i]) continue;
                
                // throw out late hits
                if(h.getTime() > _timeCut) continue;
                
                // special studies of hcal depth require the following
                if(isHadCal && h.getLayerNumber() > _hadCalorimeterMaxLayer) continue;
                
//                if(_isolationMinNeighbors !=0)
//                {
//                    if (isIsolated(hit, allHitsMap)) continue;
//                }
                // passed all the cuts, add to list
                hitsToCluster.add(hit);
            }
            ++i;
        }
        // add the processed hits back to the event
        // here we use the collection manager to handle these instead of the event
        String outputCollectionName = _cond.getString("ProcessedHitsCollectionName");
        
        _collectionmanager.addList(outputCollectionName, hitsToCluster);
    }
    
//    private boolean isIsolated(CalorimeterHit h, Map<Long, CalorimeterHit> allHitsMap)
//    {
//        return false;
//    }
}