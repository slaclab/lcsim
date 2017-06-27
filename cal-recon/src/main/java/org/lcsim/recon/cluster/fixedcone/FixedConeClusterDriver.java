package org.lcsim.recon.cluster.fixedcone;

import java.util.List;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;
import org.lcsim.event.EventHeader;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.util.Driver;
import org.lcsim.recon.util.CalorimeterInformation;
import org.lcsim.geometry.Calorimeter.CalorimeterType;

/**
 * FixedConeClusterer implements a
 * <font face="symbol">q-f </font> cone clustering algorithm
 * that assigns all neighboring hits to the same cluster if they fall
 * within a radius R of the cluster axis. The axis is originally defined
 * by a seed cell, and is iteratively updated as cells are added.
 * This version of the ClusterBuilder splits overlapping clusters
 * by assigning cells in the overlap region to the nearest cluster axis.
 * By default only the "EcalBarrHits" collection is clustered and written
 * to event with the name "EcalBarrHitsFixedConeClusters"
 *
 * @author Norman A. Graf
 * @version 1.0
 */

public class FixedConeClusterDriver extends Driver
{
    private double _radius;
    private double _seedEnergy;
    private double _minEnergy;
    private FixedConeClusterer _clusterer;
    private String[] _collNames;
    private boolean _doall;
    private String _nameExt;
    
    IDDecoder _decoder;
    
    /**
     * Constructor
     *
     * @param   radius The cone radius in <font face="symbol">q-f </font> space
     * @param   seed   The minimum energy for a cone seed cell (in GeV)
     * @param   minE   The minimum energy for a cluster (in GeV)
     */
    public FixedConeClusterDriver(double radius, double seed, double minE)
    {
        _radius = radius;
        // overwrite later with sampling fraction correction
        _seedEnergy = seed;
        _minEnergy = minE;
        _clusterer = new FixedConeClusterer(_radius, _seedEnergy, _minEnergy);
        _doall = false;
        _nameExt = "FixedConeClusters";
    }
    
    
    /**
     * Processes an Event to find CalorimeterClusters
     *
     * @param   event  The Event to process
     */
    protected void process(EventHeader event)
    {
        if(_collNames == null)
        {
            CalorimeterInformation ci = CalorimeterInformation.instance();
            _collNames = new String[1];
            _collNames[0] = ci.getCollectionName(CalorimeterType.EM_BARREL);
        }
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
            List<Cluster> clusters = _clusterer.createClusters(collection);
//        System.out.println("found "+clusters.size()+" clusters");
            if (clusters.size() > 0) event.put(name+_nameExt,clusters);
        }
      }
    }
    
  /**
   * Set the extension of the hit collection name to use
   * when writing the cluster collection to event
   *
   * @param   ext - the extension to add to the hit collection name
   */
   public void setClusterNameExtension(String ext)
   {
       _nameExt = ext;
   }
  /**
   * Set the names of the CalorimeterHit collections to cluster
   *
   * @param   names - an array of Strings containing the names of the
   *                  hit collections to cluster
   */
   public void setCollectionNames(String[] names)
   {
       _collNames = names;
       _doall = false;
   }
  /**
   * Set a flag to cluster all CalorimeterHit collections
   *
   */
   public void setClusterAllCollections()
   {
       _doall = true;
   }
    public String toString()
    {
        return "FixedConeClusterDriver with clusterer "+_clusterer;
    }
}
