/*
 * NNCluster.java
 *
 * Created on April 4, 2006, 11:16 AM
 *
 * $Id: $
 */

package org.lcsim.recon.cluster.localequivalence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.lcsim.event.CalorimeterHit;

/**
 * A simple Cluster which contains a list of constituent CalorimeterHits
 * @author Norman Graf
 */
public class NNCluster implements Comparable
{
    // attributes
    private SortedSet _cells;
    private double _value = 0;
    private List<CalorimeterHit> _hits = new ArrayList<CalorimeterHit>();
    
    // methods
    
    //Constructor
    
    public NNCluster()
    {
        _cells = new TreeSet( new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                // sort first on the value of the cell...
                double value1 = ((Cell)o1).value();
                double value2 = ((Cell)o2).value();
                if(value1!=value2) return (value1< (value2) ? -1 : (value1 == (value2) ? 0 : 1));
                // if both cells have the same value, let the cellID decide
                long ndx1 = ((Cell)o1).cellID();
                long ndx2 = ((Cell)o2).cellID();
                return (ndx1<ndx2 ? -1 : ((ndx1==ndx2) ? 0 : 1));
                
            }
        }
        );
    }
    
    public SortedSet cells()
    {
        return _cells;
    }
    
    public int size()
    {
        return _cells.size();
    }
    
    public void addCell(Cell cell)
    {
        _cells.add(cell);
        _value+=cell.value();
        Cell3D c3d = (Cell3D) cell;
        _hits.add(c3d.getCalorimeterHit());
    }
    
    public List<CalorimeterHit> hits()
    {
        return _hits;
    }
    
    public Cell highestCell()
    {
        return (Cell)_cells.last();
    }
    
    public double value()
    {
        return _value;
    }
    
    // Comparable interface
    public int compareTo(Object o)
    {
        double value = ( (NNCluster) o)._value;
//        System.out.println("in compareTo this.value= "+_value+" that.value = "+value);
//        System.out.println(_value < value ? -1 : (_value == value ? 0 : 1));
        return (_value < value ? -1 : (_value == value ? 0 : 1));
    }    
    
    public String toString()
    {
        return "\n NNCluster with "+size()+" cells, centered at "+highestCell().cellID()+" value= "+_value+"\n"+_cells;
    }
}
