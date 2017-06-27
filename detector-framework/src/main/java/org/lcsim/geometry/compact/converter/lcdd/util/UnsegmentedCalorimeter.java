package org.lcsim.geometry.compact.converter.lcdd.util;

/**
 * 
 * @author tonyj
 */
public class UnsegmentedCalorimeter extends Calorimeter
{
    public UnsegmentedCalorimeter( String name )
    {
        super( "unsegmented_calorimeter", name );
    }
    
    /*
    public void setSegmentation(Segmentation segmentation)
    {
       addContent(segmentation);
    }
    */
}