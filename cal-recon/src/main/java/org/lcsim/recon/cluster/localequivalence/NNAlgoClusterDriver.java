/*
 * NNAlgoClusterDriver.java
 *
 * Created on April 4, 2006, 2:37 PM
 *
 * $Id: $
 */

package org.lcsim.recon.cluster.localequivalence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;
import org.lcsim.event.EventHeader;
import org.lcsim.util.Driver;
import org.lcsim.event.base.BaseCluster;
import org.lcsim.lcio.LCIOConstants;

/**
 *
 * @author Norman Graf
 */
public class NNAlgoClusterDriver extends Driver
{
    // the neighborhood in u to search
    private int _dU;
    // the neighborhood in v to search
    private int _dV;
    // the neighborood in layers to search
    private int _dLayer;
    // energy threshold
    private double _thresh;
    
    private NNAlgo _clusterer;
    
    private boolean _doall;
    private String[] _collNames;
    private String _nameExt;
   
    private boolean debug;
    
    /** Creates a new instance of NNAlgoClusterDriver */
    // JAS needs default constructor
    public NNAlgoClusterDriver()
    {
        this(3, 3, 5, .1); // why was this 15?
    }
    // fully qualified constructor
    public NNAlgoClusterDriver(int dU, int dV, int dLayer, double threshold)
    {
        _dU = dU;
        _dV = dV;
        _dLayer = dLayer;
        _thresh = threshold;
        _doall = true;
        _collNames = new String[4];
        _collNames[0] = "EcalBarrelHits";
        _collNames[1] = "EcalEndcapHits";
        _collNames[2] = "BeamCalHits";
        _collNames[3] = "LumiCalHits";
        _nameExt = "EMClusters";
        _clusterer = new NNAlgo(_thresh, _dLayer, _dU, _dV);
    }
    
    protected void process(EventHeader event)
    {
        //First look for clusters in individual collections
        
        List<List<CalorimeterHit>> collections = event.get(CalorimeterHit.class);
        for (List<CalorimeterHit> collection: collections)
        {
            String name = event.getMetaData(collection).getName();
            boolean doit = false;
            if(_doall)
            {
                doit = true;
            }
            else
            {
                for(int i=0;i<_collNames.length;i++)
                {
                    if(name.compareTo(_collNames[i]) == 0)
                    {
                        doit = true;
                        break;
                    }
                }
            }
            if(doit)
            {
                Map<Long, CalorimeterHit> hitmap = new HashMap<Long, CalorimeterHit>();
                for(CalorimeterHit hit : collection)
                {
                    hitmap.put(hit.getCellID(), hit);
                }
                List<NNCluster> clusters = _clusterer.cluster(hitmap);
                
                List<BaseCluster> bclus = new ArrayList<BaseCluster>();
                for(NNCluster clus : clusters)
                {
                    if (clus.size()>5) // TODO fix this hard-coded cut
                    {
                        BaseCluster bc = new BaseCluster();
                        List<CalorimeterHit> hits = clus.hits();
                        for(CalorimeterHit hit: hits)
                        {
                            bc.addHit(hit);
                        }
                        bclus.add(bc);
                    }
                }
                if (bclus.size() > 0)
                {
                    int flag = 1 << LCIOConstants.CLBIT_HITS;
                    event.put(name+_nameExt,bclus,Cluster.class,(1<<31));
                }
            }
        }
    }
}