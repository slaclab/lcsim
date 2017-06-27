/*
 * Cell3D.java
 *
 * Created on April 4, 2006, 11:11 AM
 *
 * $Id: $
 */

package org.lcsim.recon.cluster.localequivalence;

import org.lcsim.event.CalorimeterHit;

/**
 * A Cell based on a CalorimeterHit
 * 
 * @author Norman Graf
 */
public class Cell3D extends Cell
{
    private double _value;
    private Cell _pointsTo;
    private Cell _pointedTo;
    private CalorimeterHit _calhit;
    
    
    /** Basic Constructor
     * @param value The value of this Cell.
     */
    public Cell3D(double value)
    {
        _value = value;
        _pointsTo = this;
        _pointedTo = this;
        _calhit = null;
    }
    
    /**
     * @param value The value of this Cell.
     * @param hit   The CalorimeterHit on which this Cell is based.
     */
    public Cell3D(double value, CalorimeterHit hit)
    {
        _value = value;
        _pointsTo = this;
        _pointedTo = this;
        _calhit = hit;
    }
    
    /**
     *
     * @param hit Assign the CalorimeterHit to this Cell
     */
    public Cell3D(CalorimeterHit hit)
    {
        _value = hit.getCorrectedEnergy();
        _pointsTo = this;
        _pointedTo = this;
        _calhit = hit;
    }    
    
    public double value()
    {
        return _value;
    }
    
    public Cell pointsTo()
    {
        return _pointsTo;
    }
    
    public void pointsTo(Cell pointsto)
    {
        _pointsTo = pointsto;
    }
    
    public void pointedTo(Cell cell)
    {
        _pointedTo = cell;
    }

    public long cellID()
    {
        return _calhit.getCellID();
    }
    
    public Cell pointedTo()
    {
        return _pointedTo;
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer(" Cell3D "+cellID()+" with value "+_value+"\n");
        sb.append("pointed to by: "+_pointedTo.cellID()+"\n");
        sb.append("points to: "+_pointsTo.cellID()+"\n");
        return sb.toString();
   }
    
    // Comparable interface
    public int compareTo(Object o)
    {
        double value = ( (Cell3D) o)._value;
        return (_value < value ? -1 : (_value == value ? 0 : 1));
    }
    
    /**
     *
     * @return The CalorimeterHit on which this Cell is based.
     */
    public CalorimeterHit getCalorimeterHit()
    {
        return _calhit;
    }
    
}