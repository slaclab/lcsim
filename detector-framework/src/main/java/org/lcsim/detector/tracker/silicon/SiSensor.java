package org.lcsim.detector.tracker.silicon;
/*
 * SiSensor.java
 *
 * Created on July 20, 2005, 5:55 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

import hep.physics.matrix.BasicMatrix;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;
import java.util.Collection;

import java.util.EnumMap;
import java.util.Map;

import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.converter.compact.DeDetector;
import org.lcsim.detector.identifier.ExpandedIdentifier;
import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierDictionary;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.detector.solids.GeomOp3D;
import org.lcsim.detector.solids.Point3D;
import org.lcsim.detector.solids.Polygon3D;

/**
 *
 * @author tknelson
 */
public class SiSensor extends DetectorElement
{
    
    // Enumerated types
    //=================
    
    // Fields
    //=======
    // Static defaults - actual values user modifiable
    private static double _DEPLETION_VOLTAGE_DEFAULT = 100;// * volt;
    private static double _BIAS_VOLTAGE_DEFAULT = 110;// * volt;
    
    // primary properties
    //-------------------
    
    // Sensor ID
    private int _sensorid;
    
    // biasing and electrodes
    private Map<ChargeCarrier, Polygon3D> _bias_surfaces = new EnumMap<ChargeCarrier,Polygon3D>(ChargeCarrier.class);
    private Map<ChargeCarrier, SiSensorElectrodes> _sense_electrodes = new EnumMap<ChargeCarrier,SiSensorElectrodes>(ChargeCarrier.class);
    private Map<ChargeCarrier, SiSensorElectrodes> _readout_electrodes = new EnumMap<ChargeCarrier,SiSensorElectrodes>(ChargeCarrier.class);
    private Map<ChargeCarrier, BasicMatrix> _transfer_efficiencies = new EnumMap<ChargeCarrier,BasicMatrix>(ChargeCarrier.class);
    
    // bulk - propoerties of the bulk
    private DopedSilicon _bulk;
    private double _thickness;  // accessed often, cached here for speed
    
    // operating point
    private double _depletion_voltage;
    private double _bias_voltage;
    
    // Constructors
    //=============
    
    // Default defines everything but electrode configuration
    public SiSensor(
            int sensorid,
            String name,
            IDetectorElement parent,
            String support,
            IIdentifier id
            )
    {
        super(name,parent,support,id);
        setSensorID(sensorid);
        setBulk(new DopedSilicon());
        setDepletionVoltage(SiSensor._DEPLETION_VOLTAGE_DEFAULT);
        setBiasVoltage(SiSensor._BIAS_VOLTAGE_DEFAULT);
    }
    
    public SiSensor(
            int sensorid,
            String name,
            IDetectorElement parent,
            String support
            )
    {
        super(name,parent,support);
        setSensorID(sensorid);
        setBulk(new DopedSilicon());
        setDepletionVoltage(SiSensor._DEPLETION_VOLTAGE_DEFAULT);
        setBiasVoltage(SiSensor._BIAS_VOLTAGE_DEFAULT);
    }
    
    
    // Accessors
    //==========
    
    // Setters
    //--------
    public void setSensorID(int sensorid)
    {
        _sensorid = sensorid;
    }
    
    public void setSenseElectrodes(SiSensorElectrodes sense_electrodes)
    {
        _sense_electrodes.put(sense_electrodes.getChargeCarrier(),sense_electrodes);
    }
    
    public void setReadoutElectrodes(SiSensorElectrodes readout_electrodes)
    {
        _readout_electrodes.put(readout_electrodes.getChargeCarrier(),readout_electrodes);
    }
    
    public void setBiasSurface(ChargeCarrier carrier, Polygon3D bias_surface)
    {
        _bias_surfaces.put(carrier,bias_surface);
    }
    
    public void setTransferEfficiencies(ChargeCarrier carrier, BasicMatrix transfer_efficiencies)
    {
        _transfer_efficiencies.put(carrier,transfer_efficiencies);
    }
    
    public void setBulk(DopedSilicon bulk)
    {
        _bulk = bulk;
    }
    
    public void setDepletionVoltage(double depletion_voltage)
    {
        _depletion_voltage = depletion_voltage;
    }
    
    public void setBiasVoltage(double bias_voltage)
    {
        _bias_voltage = bias_voltage;
    }
    
    // Getters
    public int getSensorID()
    {
        return _sensorid;
    }
    
    public Collection<SiSensorElectrodes> getSenseElectrodes()
    {
        return _sense_electrodes.values();
    }
    
    public SiSensorElectrodes getSenseElectrodes(ChargeCarrier carrier)
    {
        return _sense_electrodes.get(carrier);
    }
    
    public Collection<SiSensorElectrodes> getReadoutElectrodes()
    {
        return _readout_electrodes.values();
    }
    
    public SiSensorElectrodes getReadoutElectrodes(ChargeCarrier carrier)
    {
        return _readout_electrodes.get(carrier);
    }
    
    public Polygon3D getBiasSurface(ChargeCarrier carrier)
    {
        return _bias_surfaces.get(carrier);
    }
    
    public BasicMatrix getTransferEfficiencies(ChargeCarrier carrier)
    {
        return _transfer_efficiencies.get(carrier);
    }
    
    public DopedSilicon getBulk()
    {
        return _bulk;
    }
    
    public double getThickness()
    {
        if (_thickness == 0)
        {
            
            
//            System.out.println("Hole plane normal: "+_bias_surfaces.get(ChargeCarrier.HOLE).getPlane().getNormal());
//            System.out.println("Hole plane direction: "+_bias_surfaces.get(ChargeCarrier.HOLE).getPlane().getDistance());
//
//            System.out.println("Electron plane normal: "+_bias_surfaces.get(ChargeCarrier.ELECTRON).getPlane().getNormal());
//            System.out.println("Electron plane direction: "+_bias_surfaces.get(ChargeCarrier.ELECTRON).getPlane().getDistance());
            
            _thickness = Math.abs(GeomOp3D.distanceBetween( _bias_surfaces.get(ChargeCarrier.HOLE).getPlane(), _bias_surfaces.get(ChargeCarrier.ELECTRON).getPlane()));
            
//            System.out.println("Calculated thickness: "+_thickness);
        }
        return _thickness;
    }
    
    public double getDepletionVoltage()
    {
        return _depletion_voltage;
    }
    
    public double getBiasVoltage()
    {
        return _bias_voltage;
    }
    
    public Hep3Vector getBField(Hep3Vector local_position)
    {
        
        IDetectorElement ancestor = this.getParent();
        while (!(ancestor instanceof DeDetector) && !(ancestor==null))
        {
            ancestor = ancestor.getParent();
        }
        if (ancestor == null) throw new RuntimeException("SiSensor.getBField CANNOT FIND DETECTOR!!");
        
        Hep3Vector global_position = getGeometry().getLocalToGlobal().transformed(local_position);
        Hep3Vector field_global = ((DeDetector)ancestor).getBField(global_position);
//        Hep3Vector field_global = new BasicHep3Vector(0,0,5);
        
        return getGeometry().getGlobalToLocal().rotated(field_global);
        
    }
    
    // Operators
    //==========
//    public void initialize()
//    {
//        // Cache thickness of bulk
//        _thickness = Math.abs(GeomOp3D.distanceBetween( _bias_surfaces.get(ChargeCarrier.HOLE).getPlane(), _bias_surfaces.get(ChargeCarrier.HOLE).getPlane()));
//
//    }
    
    public boolean isACCoupled(ChargeCarrier carrier)
    {
        return (this._readout_electrodes.get(carrier) != null);
    }
    
    public boolean isDoubleSided()
    {
        boolean double_sided = true;
        for (ChargeCarrier carrier : ChargeCarrier.values())
        {
            double_sided = double_sided && hasElectrodesOnSide(carrier);
        }
        return double_sided;
    }
    
    public boolean hasStrips()
    {
        boolean has_strips = false;
        for (SiSensorElectrodes electrodes : getReadoutElectrodes())
        {
            has_strips = has_strips || (electrodes instanceof SiStrips);
        }
        return has_strips;
    }
    
    public boolean hasPixels()
    {
        boolean has_pixels = false;
        for (SiSensorElectrodes electrodes : getReadoutElectrodes())
        {
            has_pixels = has_pixels || (electrodes instanceof SiPixels);
        }
        return has_pixels;
    }
    
    public double distanceFromSide(Hep3Vector point, ChargeCarrier carrier)
    {
//        System.out.println("distanceFromSide: "+-GeomOp3D.distanceBetween(new Point3D(point),_bias_surfaces.get(carrier).getPlane()));
        
        
        return -GeomOp3D.distanceBetween(new Point3D(point),_bias_surfaces.get(carrier).getPlane());
//        ITransform3D electrode_transform = _sense_electrodes.get(carrier).getParentToLocal().inverse();
//        Plane3D electrode_plane = _sense_electrodes.get(carrier).getGeometry().transformed(electrode_transform).getPlane();
//
//        return GeomOp3D.distanceBetween(new Point3D(point),electrode_plane);
    }
    
    public boolean hasElectrodesOnSide(ChargeCarrier carrier)
    {
        if(_sense_electrodes.get(carrier) == null) return false;
        else return true;
    }
    
    public Hep3Vector electricField(Hep3Vector position)
    {
        
        // See Gorelov
        double electric_field_magnitude = (_bias_voltage-_depletion_voltage)/getThickness() +
                (2.0*_depletion_voltage)/getThickness() * (1.0 - this.distanceFromSide(position,ChargeCarrier.ELECTRON));
//        System.out.println("Electric field strength: "+electric_field_magnitude);
//        System.out.println("Distance from side: "+this.distanceFromSide(position,ChargeCarrier.ELECTRON));
//        System.out.println("Thickness: "+getThickness());
        
        Hep3Vector electric_field_direction = _bias_surfaces.get(ChargeCarrier.HOLE).getNormal();
        
//        System.out.println("Electric field direction: "+electric_field_direction);
        
        return VecOp.mult(electric_field_magnitude,electric_field_direction);
        
        
//        Hep3Vector electric_field_direction = null;
//        for (ChargeCarrier carrier : ChargeCarrier.values())
//        {
//            if (hasElectrodesOnSide(carrier))
//            {
//                ITransform3D electrode_transform = _sense_electrodes.get(carrier).getParentToLocal().inverse();
//                Plane3D electrode_plane = _sense_electrodes.get(carrier).getGeometry().transformed(electrode_transform).getPlane();
//                electric_field_direction = VecOp.mult(carrier.charge(),electrode_plane.getNormal());
//                if (!(electric_field_direction == null)) break;
//            }
//        }
//
////        Hep3Vector electric_field_direction = (this._orientation == Orientation.PSIDE_POSITIVE_Z) ?
////            new BasicHep3Vector(0.0,0.0,1.0) : new BasicHep3Vector(0.0,0.0,-1.0);
//
//        double electric_field_magnitude = (_bias_voltage-_depletion_voltage)/_thickness +
//                (2.0*_depletion_voltage)/_thickness * (1.0 - this.distanceFromSide(position,ChargeCarrier.ELECTRON));
//
//        return VecOp.mult(electric_field_magnitude,electric_field_direction);
        
    }
    
    public String toString()
    {
        String newline = System.getProperty("line.separator");
        String output = "SiSensor Object: "+newline+
                "   Property 1";
        return output;
    }
    
    /**
     * Make an {@link IIdentifier} for a given strip number and side number.
     *
     * @param stripNumber The strip number, which should be from 1 to nstrips - 1.
     * @param sideNumber  The side number, which should be 1 or -1.
     * @return A 64-bit   A packed id for the strip.
     */
    public IIdentifier makeStripId( int stripNumber, int sideNumber )
    {
        // Copy the DetectorElement's identifier, which will leave blank fields
        // for side and strip.
        IExpandedIdentifier id = new ExpandedIdentifier(getExpandedIdentifier());
        
        // Get the helper and dictionary.
        IIdentifierHelper helper = getIdentifierHelper();
        IIdentifierDictionary dict = helper.getIdentifierDictionary();
        
        // Fill in the side and strip numbers.
        id.setValue(dict.getFieldIndex("side"), sideNumber);
        id.setValue(dict.getFieldIndex("strip"), stripNumber);
        
        // Pack and return the id.
        return helper.pack( id );
    }
}