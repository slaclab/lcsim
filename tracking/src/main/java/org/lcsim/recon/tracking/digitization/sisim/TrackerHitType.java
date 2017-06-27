/*
 * TrackerHitType.java
 *
 * Created on December 4, 2007, 11:14 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import org.lcsim.detector.tracker.silicon.SiSensor;

/**
 *
 * @author tknelson
 */
public class TrackerHitType
{
    
    private CoordinateSystem _coordinate_system;
    private MeasurementType _measurement_type;
    
    public enum CoordinateSystem
    {
        GLOBAL,
        SENSOR,
        UNKNOWN;
    }
    
    public enum MeasurementType
    {
        STRIP_1D,
        STRIP_2D,
        PIXEL;
    }
    
    /** Creates a new instance of TrackerHitType */
    public TrackerHitType(CoordinateSystem coordinate_system, MeasurementType measurement_type)
    {
        _coordinate_system = coordinate_system;
        _measurement_type = measurement_type;
    }
    
    public CoordinateSystem getCoordinateSystem()
    {
        return _coordinate_system;
    }
    
    public MeasurementType getMeasurementType()
    {
        return _measurement_type;
    }
    
    private void setCoordinateSystem(CoordinateSystem coordinate_system)
    {
        _coordinate_system = coordinate_system;
    }
    
    private void setMeasurementType(MeasurementType measurement_type)
    {
        _measurement_type = measurement_type;
    }
    
    public static TrackerHitType decoded(int encoded_type)
    {
        return Decoder.decoded(encoded_type);
    }
    
    public static int encoded(TrackerHitType decoded_type)
    {
        return Decoder.encoded(decoded_type);
    }
    
    private static class Decoder
    {
        
        private static TrackerHitType decoded(int raw_type)
        {
            int measurement_type_index =    raw_type & 0x3    ;
            int coordinate_system_index =  (raw_type & 0x4) >> 2;
            
            MeasurementType measurement_type = MeasurementType.values()[measurement_type_index];
            CoordinateSystem coordinate_system = CoordinateSystem.values()[coordinate_system_index];
 
            return new TrackerHitType(coordinate_system,measurement_type);
        }
        
        private static int encoded(TrackerHitType type)
        {
            return (type.getCoordinateSystem().ordinal()<<2) | type.getMeasurementType().ordinal();
        }
        
    }
    
    
    
    
}
