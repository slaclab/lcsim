package org.lcsim.geometry;

/**
 * The interface for a generic Calorimeter detector, providing access to basic envelope
 * parameters, calculations of layer parameters such as interaction and radiation lengths,
 * as well as some methods that eliminate the need for complicated method chaining to access
 * important information.
 * 
 * @author Jeremy McCormick
 * @version $Id: Calorimeter.java,v 1.20 2011/01/04 21:58:51 jeremy Exp $      
 * 
 */
public interface Calorimeter extends Subdetector
{
    /**
     * The CalorimeterType is an enum describing the type of Calorimeter. Values are based
     * on common subsystems in an ILC detector. This enum is designed such that only one
     * subdetector should have a given type in the compact description except for UNKNOWN,
     * which not be explicitly used in the calorimeterType field.    
     */
    enum CalorimeterType
    {
        UNKNOWN, 
        HAD_BARREL, 
        HAD_ENDCAP, 
        EM_BARREL, 
        EM_ENDCAP, 
        MUON_BARREL, 
        MUON_ENDCAP, 
        LUMI, 
        BEAM;
        
        /**
         * Convert to CalorimeterType from a String.
         * @param s The String.
         * @return The CalorimeterType.
         */
        public static CalorimeterType fromString( final String s )
        {
            if ( s.equals( "HAD_BARREL" ) )
                return HAD_BARREL;
            if ( s.equals( "HAD_ENDCAP" ) )
                return HAD_ENDCAP;
            else if ( s.equals( "EM_BARREL" ) )
                return EM_BARREL;
            else if ( s.equals( "EM_ENDCAP" ) )
                return EM_ENDCAP;
            else if ( s.equals( "MUON_BARREL" ) )
                return MUON_BARREL;
            else if ( s.equals( "MUON_ENDCAP" ) )
                return MUON_ENDCAP;
            else if ( s.equals( "LUMI" ) )
                return LUMI;
            else if ( s.equals( "BEAM" ) )
                return BEAM;
            else
                return UNKNOWN;
        }

        /**
         * Convert from CalorimeterType to String.
         * @param c The CalorimeterType.
         * @return The String.
         */
        public static String toString( CalorimeterType c )
        {
            if ( c.equals( HAD_BARREL ) )
                return "HAD_BARREL";
            else if ( c.equals( HAD_ENDCAP ) )
                return "HAD_ENDCAP";
            else if ( c.equals( EM_BARREL ) )
                return "EM_BARREL";
            else if ( c.equals( EM_ENDCAP ) )
                return "EM_ENDCAP";
            else if ( c.equals( MUON_BARREL ) )
                return "MUON_BARREL";
            else if ( c.equals( MUON_ENDCAP ) )
                return "MUON_ENDCAP";
            else if ( c.equals( LUMI ) )
                return "LUMI";
            else if ( c.equals( BEAM ) )
                return "BEAM";
            else
                return "UNKNOWN";
        }
    }

    /**
     * Get the CalorimeterType of this Calorimeter.
     * 
     * @return The CalorimeterType.
     */
    public CalorimeterType getCalorimeterType();

    /**
     * Get the innerRadius of this Calorimeter or 0 if NA.
     * 
     * @return The innerRadius.
     */
    public double getInnerRadius();

    /**
     * Get the outerRadius of this Calorimeter or 0 if NA.
     * 
     * @return The outerRadius.
     */
    public double getOuterRadius();

    /**
     * Get the innerZ of this Calorimeter or 0 if NA.
     * 
     * @return The innerZ.
     */
    public double getInnerZ();

    /**
     * Get the outerZ of this Calorimeter or 0 if NA.
     * 
     * @return The outerZ.
     */
    public double getOuterZ();

    /**
     * Get the Calorimeter's Z length.
     * 
     * @return The Calorimeter's Z length.
     */
    public double getZLength();

    /**
     * Get the inner phi angle subtended by one calorimeter section or 0 if NA.
     * 
     * @return The phi angle of one section.
     */
    public double getSectionPhi();

    /**
     * Get the number of sides of this calorimeter or 0 if NA.     
     * @return The inner number of sides.
     */
    public int getNumberOfSides();

    /**
     * Get the number of layers in the Calorimeter. 
     * @return The number of layers.
     */
    public int getNumberOfLayers();

    /**
     * Get the distance to the layer from the IP in mm.
     * 
     * @param layerNumber The layer index.
     * @return The distance to the layer.
     */
    public double getDistanceToLayer( int layerNumber );

    /**
     * Get the distance to the sensor from the IP in mm.     
     * @param The layer number.
     * @return The distance to the sensor in mm.
     */
    public double getDistanceToSensor( int layerNumber );

    /**
     * Get the total layer thickness in mm.     
     * @return The total layer thickness for the given layer.
     */
    public double getLayerThickness( int layern );
    
    /**
     * Get the total thickness of the calorimeter.
     * @return The calorimeter's thickness.
     */
    public double getTotalThickness();

    /**
     * Get the sensor thickness in mm. 
     * @return The sensor thicknes for the given layer.
     */
    public double getSensorThickness( int layern );

    /**
     * Get the number of interaction lengths in the layer.     
     * @param layern The layer number.
     * @return The number of interaction lengths for the given layer.
     */
    public double getInteractionLengths( int layern );

    /**
     * Get the number of radiation lengths in the layer.    
     * @param layern The layer number.
     * @return The number of radiation lengths for the given layer.
     */
    public double getRadiationLengths( int layern );
    
    /**
     * Get the number of interaction lengths in the layers.     
     * @param layern The layer number.
     * @return The number of interaction lengths for the given layer.
     */
    public double getInteractionLengths();

    /**
     * Get the number of radiation lengths in the layers.    
     * @param layern The layer number.
     * @return The number of radiation lengths for the given layer.
     */
    public double getRadiationLengths();

    /**
     * Get MIP energy loss in GeV in this layer.     
     * @param layern The layer number.
     * @return MIP energy loss for the given layer.
     */
    public double getDe( int layern );

    /**
     * Get the cell U dimension.
     * @return The cell U dimension.
     */
    public double getCellSizeU();

    /**
     * Get the cell V dimension.
     * @return The cell V dimension.
     */
    public double getCellSizeV();
        
    // This would be useful but requires access to the conditions system.
    // May require using a set method to decouple from conditions package.
    // public boolean isDigital();
}
