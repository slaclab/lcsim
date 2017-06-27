/*
 * CDFSiSensorSim.java
 *
 * Created on May 10, 2007, 3:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.digitization.sisim;

import org.lcsim.detector.solids.GeomOp3D;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.detector.tracker.silicon.SiSensorElectrodes;
import org.lcsim.detector.tracker.silicon.ChargeCarrier;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.IRotation3D;
import org.lcsim.detector.tracker.silicon.ChargeDistribution;
import org.lcsim.detector.tracker.silicon.GaussianDistribution2D;

import hep.physics.vec.Hep3Vector;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.matrix.BasicMatrix;
import hep.physics.vec.VecOp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.lcsim.detector.Transform3D;
import org.lcsim.detector.Translation3D;
import org.lcsim.detector.solids.Line3D;
import org.lcsim.detector.solids.Point3D;

/**
 *
 * @author tknelson
 */
public class CDFSiSensorSim implements SiSensorSim
{
    
    // Fields
    SiSensor _sensor = null;
//    ChargeTransferModel _transfer_model = null;
    Map<ChargeCarrier,Hep3Vector> _drift_direction = null;
    Map<ChargeCarrier,SiElectrodeDataCollection> _sense_data = null;
    Map<ChargeCarrier,SiElectrodeDataCollection> _readout_data = null;

    // Simple simulation of charge trapping, this is a temporary kludge.
    // Charge collection efficiency with linear drift distance dependence.
    // Input is fraction lost per 100um drift: 0.2 is typical for 1E15 NEQ.
    // FIXME: should be calculated from properties of DopedSilicon (radiation dose)

    double _trapping = 0.0;
    
//    SiElectrodeSim
    
    // Static parameters - not intended to be user modifiable
    private static double _DEPOSITION_GRANULARITY = 0.10; // 10% of pitch or depleted thickness
    private static final double DISTANCE_ERROR_THRESHOLD = 0.001; //This is the maximum distance outside of the sensor allowed before an error is thrown. 
    
    private final boolean debug = false;
    
    /**
     * Creates a new instance of CDFSiSensorSim
     */
    public CDFSiSensorSim()
    {
        _drift_direction = new EnumMap<ChargeCarrier,Hep3Vector>(ChargeCarrier.class);
        _sense_data = new EnumMap<ChargeCarrier,SiElectrodeDataCollection>(ChargeCarrier.class);
        _readout_data = new EnumMap<ChargeCarrier,SiElectrodeDataCollection>(ChargeCarrier.class);
        for (ChargeCarrier carrier : ChargeCarrier.values())
        {
            _sense_data.put(carrier,new SiElectrodeDataCollection());
            _readout_data.put(carrier,new SiElectrodeDataCollection());
        }
    }

    // Setters
    public void setTrapping(double trapping)
    {
        _trapping = trapping;
    }

    // Implementation of SiSensorSim interface
    //==================================
    
    // Get charge map on electrodes
    public SiElectrodeDataCollection getReadoutData(ChargeCarrier carrier)
    {
        return _readout_data.get(carrier);
    }
    
    // SiSensorSim interface
    //======================
    
    // Simulate charge deposition
    public void setSensor(SiSensor sensor)
    {
        _sensor = sensor;
    }
    
    public Map<ChargeCarrier,SiElectrodeDataCollection> computeElectrodeData()
    {
    	if(debug) {
    		System.out.printf("%s: computeElectrodeData for sensor %s\n", this.getClass().getSimpleName(),this._sensor.getName());
    		System.out.printf("%s: # Sense strips: %d\n", this.getClass().getSimpleName(),_sensor.getSenseElectrodes(ChargeCarrier.HOLE).getNCells(0));
    		System.out.printf("%s: # Readout strips: %d\n", this.getClass().getSimpleName(),_sensor.getReadoutElectrodes(ChargeCarrier.HOLE).getNCells(0));
    	}
        depositChargeOnSense();
        transferChargeToReadout();
        
        return _readout_data;
    }
    
    // Clear readout data
    public void clearReadout()
    {
        for (ChargeCarrier carrier : ChargeCarrier.values())
        {
            _readout_data.get(carrier).clear();
        }
    }
    
    public void lorentzCorrect(Hep3Vector position, ChargeCarrier carrier)
    {
        Line3D drift_line = new Line3D(new Point3D(position), driftDirection(carrier,new BasicHep3Vector(0,0,0))); // use drift direction at origin for now
        
        List<Point3D> intersection_points = new ArrayList<Point3D>();
        
        for (ChargeCarrier bias_carrier : ChargeCarrier.values())
        {
            intersection_points.add(GeomOp3D.intersection(drift_line,_sensor.getBiasSurface(bias_carrier).getPlane()));
        }
        
        Hep3Vector corrected_position = VecOp.mult(0.5,VecOp.add(intersection_points.get(0),intersection_points.get(1)));
        
        ITransform3D transform = new Transform3D(new Translation3D(VecOp.sub(corrected_position,position)));
        
        transform.transform(position);
    }
    
    // Private
    private void clearSense()
    {
        for (ChargeCarrier carrier : ChargeCarrier.values())
        {
            _sense_data.get(carrier).clear();
        }
    }
    
    private void depositChargeOnSense()
    {
        
        if(debug)
    		System.out.printf("%s: depositChargeOnSense for sensor %s\n", this.getClass().getSimpleName(),this._sensor.getName());

        // Set up drift directions // FIXME: put this in a setup method.
        for (ChargeCarrier carrier : ChargeCarrier.values())
        {
            if (_sensor.hasElectrodesOnSide(carrier))
            {
                _drift_direction.put( carrier, driftDirection(carrier,new BasicHep3Vector(0.0,0.0,0.0)) );
            }
        }
        
        ITransform3D global_to_sensor = _sensor.getGeometry().getGlobalToLocal();
        List<SimTrackerHit> hits = _sensor.getReadout().getHits(SimTrackerHit.class);
        
        for (SimTrackerHit hit : hits)
        {
            if(debug) 
            		System.out.printf("%s: depositChargeOnSense for sim tracker hit at %s \n", this.getClass().getSimpleName(),hit.getPositionVec().toString());

//            System.out.println("Hit point: " + "[" + hit.getPoint()[0] + "," + hit.getPoint()[1] + "," + hit.getPoint()[2] + "]");
//            System.out.println("Startpoint: " + "[" + hit.getStartPoint()[0] + "," + hit.getStartPoint()[1] + "," + hit.getStartPoint()[2] + "]");
//            System.out.println("Endpoint: " + "[" + hit.getEndPoint()[0] + "," + hit.getEndPoint()[1] + "," + hit.getEndPoint()[2] + "]");
//
//            System.out.println("\n"+"Startpoint is inside: " + _sensor.getGeometry().inside(new BasicHep3Vector(hit.getStartPoint())));
//            System.out.println("Endpoint is inside: " + _sensor.getGeometry().inside(new BasicHep3Vector(hit.getEndPoint())));
            
//            if (_sensor.getGeometry().inside(new BasicHep3Vector(hit.getStartPoint())) == Inside.OUTSIDE ||
//                _sensor.getGeometry().inside(new BasicHep3Vector(hit.getEndPoint())) == Inside.OUTSIDE )
//            {
//                throw new RuntimeException("Endpoints of SimTrackerHit are outside the sensor volume!!");
//            }
            
            
            TrackSegment track = new TrackSegment(hit);
            track.transform(global_to_sensor);
            
            int nsegments = 0;
            
            // Compute number of segments
            for (ChargeCarrier carrier : ChargeCarrier.values())
            {
                if (_sensor.hasElectrodesOnSide(carrier))
                {
//                    _drift_direction.put( carrier, this.driftDirection(carrier,new BasicHep3Vector(0.0,0.0,0.0)) );
                    nsegments = Math.max(nsegments,nSegments(track,carrier, _DEPOSITION_GRANULARITY));
                }
            }
            
//            System.out.println("Number of subsegments: " + nsegments);
            
            // Set up segments
//            double segment_length = hit.getPathLength()/nsegments;
//            double segment_charge = hit.getdEdx()/nsegments/_sensor.getBulk().ENERGY_EHPAIR;
            
//            Hep3Vector segment_step = VecOp.mult(segment_length,VecOp.unit( new BasicHep3Vector(hit.getMomentum()) ));
//            Hep3Vector segment_center = VecOp.add( new BasicHep3Vector(hit.getStartPoint()), VecOp.mult(0.5,segment_step) );
            
            
            
            // Set up segments
            double segment_length = track.getLength()/nsegments;
            double segment_charge = track.getEloss()/nsegments/_sensor.getBulk().ENERGY_EHPAIR;

//            System.out.println("length of subsegments: " + segment_length);
//            System.out.println("subsegment charge: " + segment_charge);
            
            Hep3Vector segment_step = VecOp.mult(segment_length,track.getDirection());
            Hep3Vector segment_center = VecOp.add( track.getP1(),VecOp.mult(0.5,segment_step) );
            
//            System.out.println("Segment step: " + segment_step);
            
            // Loop over segments
            for (int iseg = 0; iseg < nsegments; iseg++)
            {
//                System.out.println("Segment center: " + segment_center);
                
                // FIXME: Add correct straggling treatment for thin layers
                
                // loop over sides of detector
                for (ChargeCarrier carrier : ChargeCarrier.values())
                {
                    if (_sensor.hasElectrodesOnSide(carrier))
                    {
                        SiSensorElectrodes electrodes = _sensor.getSenseElectrodes(carrier);

                        // Apply collection inefficiency for charge trapping: require between 0 and 1
                        double collection_efficiency = 1.0 - 10*_trapping*
                                driftVector(segment_center,carrier).magnitude();
                        collection_efficiency = Math.max(0.0,Math.min(1.0,collection_efficiency));
                        segment_charge *= (collection_efficiency);
                        
                        ChargeDistribution charge_distribution = diffusionDistribution(segment_charge,segment_center,carrier);
                        charge_distribution.transform(electrodes.getParentToLocal());
                        
                        SortedMap<Integer,Integer> sense_charge = electrodes.computeElectrodeData(charge_distribution);
//                        System.out.println("Sense charge map: " + sense_charge);
                        
                        _sense_data.get(carrier).add(new SiElectrodeDataCollection(sense_charge,hit));
                    }
                }
                
                // step to next segment
                segment_center = VecOp.add(segment_center, segment_step);
            }
            
        }
   
        if(debug) { 
        	System.out.printf("%s: Final sense charge map:\n", this.getClass().getSimpleName());
        	for(Map.Entry<Integer, Integer> entry :  _sense_data.get(ChargeCarrier.HOLE).getChargeMap().entrySet()) {
        		System.out.printf("%s: cell %d -> %d \n", this.getClass().getSimpleName(),entry.getKey(),entry.getValue());
        	}
        }
        
    }
    
    private void transferChargeToReadout()
    {
        for (ChargeCarrier carrier : ChargeCarrier.values())
        {
            if (_sensor.hasElectrodesOnSide(carrier))
            {
                if (_sensor.isACCoupled(carrier))
                {
//                    System.out.println("Is AC coupled");
                    
                    SiSensorElectrodes sense_electrodes = _sensor.getSenseElectrodes(carrier);
                    SiSensorElectrodes readout_electrodes = _sensor.getReadoutElectrodes(carrier);
                    BasicMatrix transfer_efficiencies = _sensor.getTransferEfficiencies(carrier);
                    
                    SiElectrodeDataCollection sense_data = _sense_data.get(carrier);
                    SiElectrodeDataCollection readout_data = _readout_data.get(carrier);
                    
                    for (Integer sense_cell : sense_data.keySet())
                    {
                        
//                        System.out.println("Processing sense cell : "+sense_cell);
                        
                        SiElectrodeData sense_cell_data = sense_data.get(sense_cell);
                        
//                        System.out.println("Sense cell charge : "+sense_cell_data.getCharge());
                        
                        int sense_row = sense_electrodes.getRowNumber(sense_cell);
                        int sense_col = sense_electrodes.getColumnNumber(sense_cell);
                        int row_steps = transfer_efficiencies.getNRows()-1;
                        int col_steps = transfer_efficiencies.getNColumns()-1;
                        
//                        System.out.println("sense_row : "+sense_row);
//                        System.out.println("sense_col : "+sense_col);
//                        System.out.println("row_steps : "+row_steps);
//                        System.out.println("col_steps : "+col_steps);
                        
//                        System.out.println("transfer_efficiencies : "+transfer_efficiencies);

                        for (int irow = sense_row - row_steps; irow <= sense_row + row_steps; irow++)
                        {
                            
//                            System.out.println("irow : "+irow);
                            
                            for (int icol = sense_col - col_steps; icol <= sense_col + col_steps; icol++)
                            {
                                
//                                System.out.println("icol : "+icol);
                                
                                int sense_id = sense_electrodes.getCellID(irow,icol);
                                if (sense_id < 0) continue;
                                Hep3Vector sense_position = sense_electrodes.getCellPosition(sense_id);
                                int readout_cell = readout_electrodes.getCellID(sense_position);
                                
//                                System.out.println("sense_id : "+sense_id);
//                                System.out.println("sense_position: "+sense_position);
//                                System.out.println("readout_cell : "+readout_cell);
                                
//                                System.out.println("position_in_cell : "+readout_electrodes.getPositionInCell(sense_position));
//
//                                System.out.println("Sense position : "+sense_electrodes.getCellPosition(sense_id));
//                                System.out.println("Readout position : "+readout_electrodes.getCellPosition(readout_cell));
                                
                                if ( readout_electrodes.isValidCell(readout_cell) &&
                                        readout_electrodes.getPositionInCell(sense_position).x() == 0.0 &&
                                        readout_electrodes.getPositionInCell(sense_position).y() == 0.0 )
                                {
//                                    System.out.println("transferring...");
                                    double transfer_efficiency = transfer_efficiencies.e(Math.abs(irow-sense_row),Math.abs(icol-sense_col));
//                                    System.out.println("transfer efficiency: "+transfer_efficiency);
//                                    System.out.println("sense charge: "+sense_cell_data.getCharge());
                                    
                                    SiElectrodeData readout_datum  = new SiElectrodeData((int)Math.round(transfer_efficiency*sense_cell_data.getCharge()),
                                            sense_cell_data.getSimulatedHits());
                                    readout_data.add(readout_cell,readout_datum);
//                                    System.out.println("transferring 3...");
//                                    System.out.println("Current readout charge map: " + _readout_data.get(carrier).getChargeMap());
                                }
//                                System.out.println("4...");
                            }
//                            System.out.println("5...");
                        }
//                        System.out.println("6...");
                    }
//                    System.out.println("7...");
                }
                else
                {
                    _readout_data.put(carrier,_sense_data.get(carrier));
                }
                
//                System.out.println("Final readout charge map: " + _readout_data.get(carrier).getChargeMap());
            }
        }
        
        clearSense();
        
    }
    
    private int nSegments(TrackSegment track, ChargeCarrier carrier, double deposition_granularity)
    {
        // Decide how to cut track into pieces as a fraction of strip pitch
        int nsegments = 0;
        if (!_sensor.hasElectrodesOnSide(carrier)) return nsegments;
        SiSensorElectrodes electrodes = _sensor.getSenseElectrodes(carrier);
        
//        System.out.println("Track P1: " + track.getP1());
//        System.out.println("Track P2: " + track.getP2());
//        System.out.println("Drift Destination of P1: " + driftDestination(track.getP1(),carrier));
//        System.out.println("Drift Destination of P2: " + driftDestination(track.getP2(),carrier));
        
        nsegments = (int)Math.ceil(Math.abs(VecOp.dot(track.getVector(),electrodes.getGeometry().getNormal()))/(_sensor.getThickness()*deposition_granularity));
        
//        nsegments = (int)Math.ceil(track.getVector().z()/(_sensor.getThickness()*deposition_granularity));  // old way
        
        Hep3Vector deposition_line = VecOp.sub( driftDestination(track.getP2(),carrier),
                driftDestination(track.getP1(),carrier) );
        
        int naxes = electrodes.getNAxes();
        for (int iaxis = 0; iaxis < naxes; iaxis++)
        {
            IRotation3D electrode_to_sensor = electrodes.getParentToLocal().getRotation().inverse();
            Hep3Vector measured_coordinate = electrode_to_sensor.rotated(electrodes.getMeasuredCoordinate(iaxis));
            
            double projected_deposition_length = Math.abs(VecOp.dot(deposition_line,measured_coordinate));
            
//            double projected_deposition_length = Math.abs(VecOp.dot(deposition_line,_sensor.getMeasuredCoordinates(carrier)[iaxis]));
            
//            System.out.println("Projected deposition Length: " + projected_deposition_length);
            
            int required_segments = (int)Math.ceil(projected_deposition_length/
                    (deposition_granularity*electrodes.getPitch(iaxis)));
            nsegments = Math.max(nsegments,required_segments);
        }
        
//        if (nsegments > 1000)
//        {
//            System.out.println("Track P1: " + track.getP1());
//            System.out.println("Track P2: " + track.getP2());
//            System.out.println("Drift Destination of P1: " + driftDestination(track.getP1(),carrier));
//            System.out.println("Drift Destination of P2: " + driftDestination(track.getP2(),carrier));
//            
//            System.out.println("nsegments: " + nsegments);
//        }
        
        return nsegments;
    }
    
    
    private Hep3Vector driftDestination(Hep3Vector origin, ChargeCarrier carrier)
    {
//        System.out.println("Beginning driftDestination for origin: "+origin);
        return VecOp.add(origin,driftVector(origin, carrier));
    }
    
    private Hep3Vector driftVector(Hep3Vector origin, ChargeCarrier carrier)
    {
//        System.out.println("Beginning driftVector");
        
        double drift_vector_scale = _sensor.distanceFromSide(origin,carrier)/VecOp.dot(_drift_direction.get(carrier),_sensor.getBiasSurface(carrier).getNormal());
//        double drift_vector_scale = _sensor.distanceFromSide(origin,carrier)/_drift_direction.get(carrier).z();
        
        return VecOp.mult(drift_vector_scale,_drift_direction.get(carrier));
    }
    
    private Hep3Vector driftDirection(ChargeCarrier carrier, Hep3Vector local_position)
    {
//        System.out.println("Beginning driftDirection");
        
//        System.out.println("Position: "+local_position);
        
//        System.out.println("Carrier: "+carrier);

        //  Get the magnetic field - already in Si units (Tesla)
        Hep3Vector b_field = _sensor.getBField(local_position);
//        System.out.println("B field: "+b_field);

        //  Get the electric field and convert from V/mm to Si units (V/m)
        Hep3Vector e_field = VecOp.mult(1000., _sensor.electricField(local_position));
//        System.out.println("E field: "+e_field);

        //  Get the mobility and convert from cm^2/V/s to Si units of m^2/V/s
        double mobility = 1.0e-4 * _sensor.getBulk().mobility(carrier);

        //  Get the charge (to be consistent with mobility, this should be in units of e)
        double qmu =  carrier.charge() * mobility;
//        System.out.println("mobility: "+mobility+" charge: "+qmu/mobility;
        
        //  Calculate the velocity parallel to the E field vpar = q * mu * E
        Hep3Vector vpar = VecOp.mult(qmu, e_field);
//        System.out.println("vpar: "+vpar);

        //  Calculate the velocity perpendicular to the E field vperp = q^2 * mu^2 * E x B
        Hep3Vector vperp = VecOp.mult(qmu*qmu, VecOp.cross(e_field, b_field));
//        System.out.println("vperp: "+vperp);

        //  Calculate a unit vector in the drift direction
        Hep3Vector drift_direction = VecOp.unit(VecOp.add(vpar, vperp));
        
//        System.out.println("Drift direction: "+drift_direction);
        
        return drift_direction;
        
    }
    
    private ChargeDistribution diffusionDistribution(double segment_charge, Hep3Vector origin, ChargeCarrier carrier)
    {
        
//        System.out.println("\n"+"Calculating charge distribution for carrier: "+carrier);
        
        // Local variables and a quick check
        double distance = _sensor.distanceFromSide(origin,carrier);
        double thickness = _sensor.getThickness();
        
//        System.out.println("Distance from side: "+distance);
        
        
        if (distance < -DISTANCE_ERROR_THRESHOLD || distance > thickness + DISTANCE_ERROR_THRESHOLD){
            throw new RuntimeException("Distance is outside of sensor by more than "+DISTANCE_ERROR_THRESHOLD+". Distance = "+distance+
                    ". If this is an isolated event, then perhaps DISTANCE_ERROR_THRESHOLD must be increased in CDFSiSensorSim"); 
        }
        else if (distance < 0) distance = 0.;
        else if (distance > thickness) distance = thickness;
//        {
//            throw new RuntimeException("Attempting to drift charge from outside of sensor!");
//        }
        double bias_voltage = _sensor.getBiasVoltage();
        double depletion_voltage = _sensor.getDepletionVoltage();
        
        // Common factors
        double difference_V = bias_voltage - depletion_voltage;
        double sum_V = bias_voltage + depletion_voltage;
        double common_factor = 2.0 * distance * depletion_voltage / thickness;
        
//        System.out.println("Distance from side: "+_sensor.distanceFromSide(origin,carrier));
//        System.out.println("Origin: "+origin);
        
        // Calculate charge spreading without magnetic field
        double sigmasq = _sensor.getBulk().K_BOLTZMANN * _sensor.getBulk().getTemperature() *
                _sensor.getThickness()*_sensor.getThickness() / _sensor.getDepletionVoltage();
        if (_sensor.getBulk().isNtype() == (carrier==ChargeCarrier.HOLE))
        {
            sigmasq *= Math.log( sum_V / (sum_V - common_factor));
        }
        else
        {
            sigmasq *= Math.log( (difference_V + common_factor) / difference_V );
        }
        
        double sigma = Math.sqrt(sigmasq);
        
//        System.out.println("sigma: "+sigma);
        
        // Corrections for magnetic field -- this is an approximation, may have to be done better for high fields
        
        // Special case if field is parallel to drift direction
        Hep3Vector drift_direction = _drift_direction.get(carrier);
        Hep3Vector bias_surface_normal = _sensor.getBiasSurface(carrier).getNormal();
        
        Hep3Vector major_axis;
        Hep3Vector minor_axis;
        
        double major_axis_length;
        double minor_axis_length;
        
        if (VecOp.cross(drift_direction,bias_surface_normal).magnitude() < GeomOp3D.ANGULAR_TOLERANCE)
        {
//            System.out.println("Drift direction perpendicular to bias plane");
//            System.out.println("Drift direction: "+drift_direction);
//            System.out.println("bias_surface_normal: "+bias_surface_normal);
//            System.out.println("First edge of bias plane: "+_sensor.getBiasSurface(carrier).getEdges().get(0).getDirection());
            
            
            major_axis = VecOp.cross(bias_surface_normal,_sensor.getBiasSurface(carrier).getEdges().get(0).getDirection()); // arbitrary vector in plane
            minor_axis = VecOp.cross(bias_surface_normal,major_axis);
            
//            System.out.println("Major axis at initialization: "+major_axis);
//            System.out.println("Minor axis at initialization: "+minor_axis);
            
            major_axis_length = minor_axis_length = sigma;
            
        }
        else
        {
//            System.out.println("Drift direction NOT perpendicular to bias plane!!");
            
            major_axis = VecOp.unit(VecOp.cross(bias_surface_normal,VecOp.cross(_drift_direction.get(carrier),bias_surface_normal)));
            minor_axis = VecOp.cross(bias_surface_normal,major_axis);
            
//        double phi_lorentz = VecOp.phi(_drift_direction.get(carrier));  // FIXME: careful here... calculate axis directions to instantiate distribution.
            // Project-to-plane would definitely be convenient here!!!
            
            double cos_theta_lorentz = VecOp.dot(_drift_direction.get(carrier),_sensor.getBiasSurface(carrier).getNormal());
//        double cos_theta_lorentz = VecOp.cosTheta(_drift_direction.get(carrier));                                 // FIXME: careful with cosTheta here!
//        System.out.println("Cos theta lorentz: "+cos_theta_lorentz);
            
            minor_axis_length = sigma*(1.0/cos_theta_lorentz); // drift time correction
            major_axis_length = minor_axis_length*(1.0/cos_theta_lorentz); // + drift angle correction
        }
        
        
        major_axis = VecOp.mult(major_axis_length,major_axis);
        minor_axis = VecOp.mult(minor_axis_length,minor_axis);
        
//        System.out.println("Major axis: "+major_axis);
//        System.out.println("Minor axis: "+minor_axis);
        
        // FIXME: this has a Z component!!! (is that OK??  I think the whole thing transforms into the electrode coordinates before integrating charge.)
        Hep3Vector drift_destination = driftDestination(origin,carrier);
        
//        System.out.println("Drift destination: "+drift_destination);
        
        ChargeDistribution distribution = new GaussianDistribution2D(segment_charge, drift_destination,major_axis,minor_axis);
        
//        ChargeDistribution distribution = new GaussianDistribution2D(segment_charge, drift_destination,new BasicHep3Vector(major_axis_length,0.0,0.0),new BasicHep3Vector(0.0,minor_axis_length,0.0));
//
//        ITransform3D phi_rotation = new Transform3D(new RotationPassiveXYZ(0.0,0.0,-phi_lorentz));
//        distribution.transform(phi_rotation);
        
//        System.out.println("Done calculating charge distribution for carrier: "+carrier);
        
        return distribution;
        
    }
    
}


















