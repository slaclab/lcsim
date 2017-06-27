/*
 * NNAlgo.java
 *
 * Created on April 4, 2006, 11:16 AM
 *
 * $Id: $
 */

package org.lcsim.recon.cluster.localequivalence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import org.lcsim.event.CalorimeterHit;
import org.lcsim.geometry.IDDecoder;

/**
 * A nearest-neighbor clustering algorithm which associates hits with their
 * highest-energy neighboring hit
 * 
 * @author Norman Graf
 */
public class NNAlgo
{
    
//  miniumum cluster energy...
    private double _minValue = 0.;
    private int _deltaLayer = 1;
    private int _deltaTheta = 1;
    private int _deltaPhi = 1;
    
    public NNAlgo(double  min)
    {
        _minValue = min;
    }
    
    public NNAlgo(double  min, int deltaLayer, int deltaTheta, int deltaPhi)
    {
        _minValue = min;
        _deltaLayer = deltaLayer;
        _deltaTheta = deltaTheta;
        _deltaPhi = deltaPhi;
    }
    
    public void setMinValue(double min)
    {
        _minValue = min;
    }
    
    public List<NNCluster> cluster(Map<Long, CalorimeterHit> hitmap)
    {
        List<Cell> cells = new ArrayList<Cell>();
        Map<Long, Cell> cellmap = new HashMap<Long, Cell>();
        Collection<CalorimeterHit> hits = hitmap.values();
        for(CalorimeterHit hit : hits)
        {
            Cell3D cell = new Cell3D(hit);
            cells.add(cell);
            long key = hit.getCellID();
            cellmap.put(key, cell);
//            System.out.println(decodeCalHit(hit));
        }
        Collections.sort(cells);
        Collections.reverse(cells);
//        System.out.println(cells);
        // now loop over the energy-sorted list of cells...
        for(Cell cell : cells)
        {
            Cell3D c3d = (Cell3D) cell;
//            System.out.println(c3d);
            Cell pointsto = cell.pointsTo();
            double max = cell.value();
            CalorimeterHit c = c3d.getCalorimeterHit();
            IDDecoder decoder = c.getIDDecoder();
            decoder.setID(c.getCellID());
            // loop over neighbors...
            long[] neighbors = decoder.getNeighbourIDs(_deltaLayer, _deltaTheta, _deltaPhi);
//            System.out.println("");
//            System.out.println("");
//            System.out.println("  hit "+decodeCalHit(c));
            for (int j=0; j<neighbors.length; ++j)
            {
//                System.out.println("  j: "+j);
//                System.out.println("  "+decodeCellID(neighbors[j],c.getIDDecoder()));
//                System.out.println(c3d.cellID()+ " j= "+j+" neighbor= "+neighbors[j]);
                CalorimeterHit h = hitmap.get(neighbors[j]);
                // is the neighboring cell id hit?
                // if so, does it meet or exceed threshold?
                if (h != null)
                {
//                    System.out.println("   hit neighbor "+ decodeCalHit(h));
                    Cell neigh = cellmap.get(neighbors[j]);
                    // find highest neighbor to point to...
                    if (neigh.value() > max) //Note difference between > and >=
                    {
                        max = neigh.value();
                        pointsto = neigh;
                    }
                }
            } // end of loop over neighbors...
            // If cell does not point to itself set pointedto and pointsto
            if (cell != pointsto)
            {
                cell.pointsTo(pointsto);
                pointsto.pointedTo().pointsTo(cell);
                cell.pointedTo(pointsto.pointedTo());           
                pointsto.pointedTo(cell);
            }
        }
        
        // This is the end of the clustering algorithm
//        System.out.println("Done clustering! \n");
//        System.out.println(cells);
        // Build clusters here, with deletion of map entries
        
        // A collection to hold the clusters
        List<NNCluster> clusters = new ArrayList<NNCluster>();
//                // The set of linked cells in the map
        Set set = cellmap.entrySet();
//
        int cluster  = 0;
        int size = cellmap.size();
        Iterator it;
        while(size>0)
        {
            it = set.iterator();
            Map.Entry entry = (Map.Entry) it.next();
            Cell cell = (Cell) entry.getValue();
            Cell nextcell = cell.pointsTo();
            
            ++cluster;
            NNCluster clus = new NNCluster();
            // loop over all cells pointed to by this cell recursively
            while(cellmap.containsValue(cell))
            {
                clus.addCell(cell);
                cellmap.remove(cell.cellID());
//
                cell = nextcell;
                nextcell = cell.pointsTo();
            }
            // done with this cluster
            // If over threshold, add it to the list of clusters to return
            if(clus.value()>_minValue)
            {
                clusters.add(clus);
                SortedSet clusCells = clus.cells();
                for(Object o : clusCells)
                {
                    Cell3D c = (Cell3D) o;
                    // remove the associated calorimeter hit from the input map.
                    hitmap.remove(c.cellID());
                }
            }
            size = cellmap.size();
        } // end of clustering loop over map
        return clusters;
    }
    
    public String toString()
    {
        return "A Nearest Neighbor Clusterer with min value "+_minValue;
    }
    
    String decodeCalHit(CalorimeterHit hit)
    {
        StringBuffer sb = new StringBuffer();
        IDDecoder decoder = hit.getIDDecoder();
        decoder.setID(hit.getCellID());
        int nFields = decoder.getFieldCount();
        for(int i=0; i<nFields; ++i)
        {
            sb.append(" "+decoder.getFieldName(i)+" : "+decoder.getValue(i));
        }
        return sb.toString();
    }
    
    String decodeCellID(long key, IDDecoder decoder)
    {
        StringBuffer sb = new StringBuffer();
        decoder.setID(key);
        int nFields = decoder.getFieldCount();
        for(int i=0; i<nFields; ++i)
        {
            sb.append(" "+decoder.getFieldName(i)+" : "+decoder.getValue(i));
        }
        return sb.toString();
    }
}
