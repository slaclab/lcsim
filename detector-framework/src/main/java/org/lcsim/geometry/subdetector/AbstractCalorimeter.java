package org.lcsim.geometry.subdetector;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.Calorimeter;
import org.lcsim.geometry.compact.Segmentation;

/**
 * The common base class for Calorimeter subdetectors.     
 *
 * @see org.lcsim.geometry.Calorimeter;
 *                                      
 * @author Jeremy McCormick
 * @version $Id: AbstractCalorimeter.java,v 1.12 2011/01/04 21:58:51 jeremy Exp $
 */
abstract class AbstractCalorimeter extends AbstractLayeredSubdetector implements Calorimeter
{
    // Type of Calorimeter from CalorimeterType enum.
    CalorimeterType calorimeterType = CalorimeterType.UNKNOWN;
    
    // Parameters for methods defined in Calorimeter interface.
    protected double innerRadius;
    protected double outerRadius;
    protected double innerZ;
    protected double outerZ;
    protected double zlength;
    protected double sectionPhi;
    protected int nsides;

    public AbstractCalorimeter( Element node ) throws JDOMException
    {
        super( node );

        // Set the calorimeterType from compact description calorimeterType field.
        if ( node.getAttribute( "calorimeterType" ) != null )
        {
            calorimeterType = CalorimeterType.fromString( node.getAttributeValue( "calorimeterType" ) );
        }
    }

    /**
     * Get the CalorimeterType of this Calorimeter.
     * @return The CalorimeterType.
     */
    public CalorimeterType getCalorimeterType()
    {
        return calorimeterType;
    }

    /**
     * Implementation of Subdetector method.
     * @return True.
     */
    public boolean isCalorimeter()
    {
        return true;
    }
    
    public double getTotalThickness()
    {
        return layering.getThickness();
    }
    
    /**
     * Get the cell U dimension from the segmentation.
     * @return The cell U dimension.
     */
    public double getCellSizeU()
    {
        return ( ( Segmentation ) this.getIDDecoder() ).getCellSizeU();
    }

    /**
     * Get cell V dimension from the segmentation.
     * @return The cell V dimension.
     */
    public double getCellSizeV()
    {
        return ( ( Segmentation ) this.getIDDecoder() ).getCellSizeV();
    }
}