package org.lcsim.geometry.layer;

import static java.lang.Math.abs;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Provides access to information of individual layers within a subdetector.
 *
 * @author Jeremy McCormick <jeremym@slac.stanford.edu> 
 */
public class Layer
{
    List<LayerSlice> slices;
    List<LayerSlice> sensors;
    List<Integer> sensorIndices;
    double preOffset = 0;
    double thickness = 0;    
    double thicknessToSensor = 0;
    double thicknessToSensorMid = 0;
    double sensorThickness = 0;
    int indexOfFirstSensor = -1;
    double thicknessWithPreOffset = 0;
    
    public Layer(List<LayerSlice> slices)
    {
    	this.slices = slices;
    	
    	// Cache list of sensor indices for fast access.
    	sensorIndices = new ArrayList<Integer>();
    	for (int i=0; i<slices.size(); i++)
    	{
    	    if (slices.get(i).isSensitive())
    	        sensorIndices.add(i);
    	}
    	
    	// Cache computed layer information.
    	computeThickness();
    	computeIndexOfFirstSensor();
    	computeSensorThickness();
    	computeThicknessToSensor();    	
    	computeThicknessToSensorMid();
    	computeThicknessWithPreOffset();
    }
    
    public List<LayerSlice> getSensors()
    {
        if (sensors == null)
        {
            sensors = new ArrayList<LayerSlice>();
            List<Integer> sensorIndices = getSensorIndices();
            for (int i : sensorIndices)
            {
                sensors.add(this.getSlice(i));
            }
        }
        return sensors;
    }
    
    public List<Integer> getSensorIndices()
    {
        return sensorIndices;
    }
    
    public double getThickness()
    {
    	return thickness;
    }
    
    public void setPreOffset(double preOffset)
    {
        if ( abs(preOffset) < 1E-7 )
        {
            preOffset = 0;
        }
               
        this.preOffset = preOffset;
    }
    
    public double getPreOffset()
    {
        return preOffset;
    }
                 
    public double getThicknessWithPreOffset()
    {
    	return thicknessWithPreOffset;
    }
    
    public double getThicknessToSensitiveMid()
    {
    	return thicknessToSensorMid;
    }
    
    public double getThicknessToSensitive()        
    {
    	return thicknessToSensor;
    }
    
    public LayerSlice getSlice(int idx)
    {        
        return slices.get(idx);
    }
    
    public List<LayerSlice> getSlices()
    {
        return slices;
    }
    
    public int getNumberOfSlices()
    {
        return slices.size();
    }
    
    public double getSensorThickness()
    {
    	return sensorThickness;
    }
    
    public int indexOfFirstSensor()
    {
    	return indexOfFirstSensor;
    }
    
    private void computeThicknessToSensor()
    {        
        int i = indexOfFirstSensor();
        
        if ( i != -1)
        {
            
            for ( int ii = 0; ii < i; ii++)
            {
                thicknessToSensor += slices.get(ii).getThickness();
            }
        }
    }
    
    private void computeThickness()
    {
    	thickness = 0;
    	for ( LayerSlice l : slices)
        {
            thickness += l.getThickness();
        }
    }
    
    private void computeSensorThickness()
    {    
    	if (indexOfFirstSensor() != -1)
    	{
    		sensorThickness = slices.get(indexOfFirstSensor()).getThickness();
    	}
    }
    
    private void computeIndexOfFirstSensor()
    {
        int i = 0;
        boolean fnd = false;
        for ( LayerSlice l : slices )
        {
            if ( l.isSensitive() )
            {
                fnd = true;
                break;
            }
            i++;
        }
        
        if (!fnd)
        {
            i = -1;
        }
        
        indexOfFirstSensor = i;
    }
    
    public double computeDistanceToSlice(int idx)
    {
        double d = 0;
        for (int i=0; i<idx; i++)
        {
            d += getSlice(i).getThickness();
        }
        return d;
    }
    
    public double computeDistanceToSliceMid(int idx)
    {
        double d = computeDistanceToSlice(idx);
        d += getSlice(idx).getThickness() / 2;
        return d;
    }
    
    private void computeThicknessToSensorMid()
    {
        int i = indexOfFirstSensor();
        
        if ( i != -1)
        {
            
            for ( int ii = 0; ii < i; ii++)
            {
            	thicknessToSensorMid += slices.get(ii).getThickness();
            }
            
            thicknessToSensorMid += slices.get(i).getThickness()/2;
        }
    }
    
    private void computeThicknessWithPreOffset()
    {
        thicknessWithPreOffset = getThickness() + getPreOffset();
    }
}