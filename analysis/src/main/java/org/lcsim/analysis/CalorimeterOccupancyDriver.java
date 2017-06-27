package org.lcsim.analysis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsSet;
import org.lcsim.event.EventHeader;
import org.lcsim.event.SimCalorimeterHit;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;

/**
 *
 * @author Norman A Graf
 *
 * @version $Id:
 */
public class CalorimeterOccupancyDriver extends Driver
{

    private boolean _debug = true;
    private Set<String> collections = new HashSet<String>();
    private Map<String, Map<Long, Integer>> cellCountMaps = new HashMap<String, Map<Long, Integer>>();
    private Map<String, IDDecoder> _idDecoders = new HashMap<String, IDDecoder>();
    private AIDA aida = AIDA.defaultInstance();

    private ConditionsSet _cond;
    private double _ECalMipCut;

    public CalorimeterOccupancyDriver()
    {

    }

    @Override
    protected void detectorChanged(Detector detector)
    {
        ConditionsManager mgr = ConditionsManager.defaultInstance();
        try {
            _cond = mgr.getConditions("CalorimeterCalibration");
            System.out.println("found conditions for " + detector.getName());
            _ECalMipCut = _cond.getDouble("ECalMip_Cut");
            System.out.println("_ECalMipCut = "+_ECalMipCut);
        } catch (ConditionsManager.ConditionsSetNotFoundException e) {
            System.out.println("ConditionSet CalorimeterCalibration not found for detector " + mgr.getDetector());
            System.out.println("Please check that this properties file exists for this detector ");
        }
    }

    @Override
    protected void process(EventHeader event)
    {
        // loop over all of the collections
        for (String collectionName : collections) {
            // fetch the SimCalorimeterHits
            List<SimCalorimeterHit> hits = event.get(SimCalorimeterHit.class, collectionName);
            log("There are " + hits.size() + " " + collectionName);
            // get the right Map to populate
            Map<Long, Integer> map = cellCountMaps.get(collectionName);
            // loop over all of the hits
            for (SimCalorimeterHit hit : hits) {
                if (!_idDecoders.containsKey(collectionName)) {
                    _idDecoders.put(collectionName, hit.getIDDecoder());
                }
                double rawEnergy = hit.getRawEnergy();
                aida.cloud1D(collectionName + " hit Energy").fill(rawEnergy);
                if (rawEnergy > _ECalMipCut) {
                    aida.cloud1D(collectionName + " hit Energy after cut").fill(rawEnergy);
                    long cellId = hit.getCellID();
                    // and update the occupancy of this address
                    if (map.containsKey(cellId)) {
//                    System.out.println("id: "+cellId+" now has "+(map.get(cellId) + 1)+ " hits.");
                        map.put(cellId, map.get(cellId) + 1);
                    } else {
                        map.put(cellId, 1);
                    }
                }
            }
        }
    }

    @Override
    protected void endOfData()
    {
        // quick analysis...
        // loop over all of the calorimeters
        for (String collectionName : collections) {
            // get the right Map to analyze
            System.out.println(collectionName);
            Map<Long, Integer> map = cellCountMaps.get(collectionName);
            // get the IDDecoder
            IDDecoder idDecoder = _idDecoders.get(collectionName);
            //get its keys
            Set<Long> keys = map.keySet();
            // loop over all of the hits
            for (Long key : keys) {
                idDecoder.setID(key);
                int layer = idDecoder.getLayer();
                double[] pos = idDecoder.getPosition();
                Integer hitCount = map.get(key);
                // and fill the histogram
                if (hitCount > 3) {
                    System.out.println(collectionName + " id " + key + " has " + hitCount + " hits.");
                }
                aida.histogram1D(collectionName + "layer " + layer + " occupancy rates", 100, 0., 100.).fill(hitCount);
                aida.cloud2D(collectionName + "layer " + layer + " occupancy rates vs position").fill(pos[0],pos[1],hitCount);
                
            }
        }
    }

    public void setCollectionNames(String[] collectionNames)
    {
        System.out.println("there are " + collectionNames.length+ " collections to process: ");
        collections.addAll(Arrays.asList(collectionNames));
        System.out.println("processing: ");
        for (String collectionName : collections) {
            System.out.println(collectionName);
            cellCountMaps.put(collectionName, new HashMap<Long, Integer>());
        }
    }

    private void log(String s)
    {
        if (_debug) {
            System.out.println(s);
        }
    }
}
