package org.lcsim.recon.tracking.digitization.sisim.config;

import hep.physics.matrix.BasicMatrix;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.VecOp;

import java.util.List;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IRotation3D;
import org.lcsim.detector.ITranslation3D;
import org.lcsim.detector.RotationPassiveXYZ;
import org.lcsim.detector.Transform3D;
import org.lcsim.detector.Translation3D;
import org.lcsim.detector.solids.IPolyhedron;
import org.lcsim.detector.solids.Polygon3D;
import org.lcsim.detector.tracker.silicon.ChargeCarrier;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.detector.tracker.silicon.SiSensorElectrodes;
import org.lcsim.detector.tracker.silicon.SiStrips;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.subdetector.SiTrackerBarrel;
import org.lcsim.util.Driver;

public class SiTrackerBarrelSensorSetup extends Driver 
{
	String subdetectorName;
	
	double readoutElectrodesPitch = 0.050;
	double senseElectrodesPitch = 0.025;
	double transferEfficiencies[] = {0.986,0.419};

	public SiTrackerBarrelSensorSetup()
	{}
	
	public SiTrackerBarrelSensorSetup(String subdetectorName)
	{
		this.subdetectorName = subdetectorName;
	}
	
	public void setSubdetectorName(String subdetectorName)
	{
		this.subdetectorName = subdetectorName;
	}
	
	public void detectorChanged(Detector detector)
	{
		if (subdetectorName == null)
			throw new RuntimeException("The subdetectorName was not set.");
		
		Subdetector subdetector = detector.getSubdetector(subdetectorName);
		if (subdetector instanceof SiTrackerBarrel)
			setupSensorDetectorElements(subdetector);
		else
			throw new RuntimeException("The subdetector " + subdetectorName + " is not an instance of SiTrackerBarrel.");
	}
	
	public void setReadoutElectrodesPitch(double readoutElectrodesPitch)
	{
		this.readoutElectrodesPitch = readoutElectrodesPitch;
	}
	
	public void setSenseElectrodesPitch(double senseElectrodesPitch)
	{
		this.senseElectrodesPitch = senseElectrodesPitch;
	}
	
	public void setTransferEfficiencies(double transferEfficiencies[])
	{
		if (transferEfficiencies.length < 2)
		{
			throw new IllegalArgumentException("Not enough values in transferEfficiencies array.");
		}
		this.transferEfficiencies[0] = transferEfficiencies[0];
		this.transferEfficiencies[1] = transferEfficiencies[1];
	}

	private void setupSensorDetectorElements(Subdetector subdet)
	{		
		for ( IDetectorElement layer : subdet.getDetectorElement().getChildren() )
		{
			for ( IDetectorElement module : layer.getChildren() )
			{
					List<SiSensor> sensors = module.findDescendants(SiSensor.class);

					if (sensors.size() == 0)
						throw new RuntimeException("No sensors found in module " + module.getName() + ".");

					SiSensor sensor = sensors.get(0);

					// Set up SiStrips for the sensors
					IPolyhedron sensor_solid = (IPolyhedron)sensor.getGeometry().getLogicalVolume().getSolid();

					// Bias the sensor
					Polygon3D p_side = sensor_solid.getFacesNormalTo(new BasicHep3Vector(0,0,1)).get(0);

					Polygon3D n_side = sensor_solid.getFacesNormalTo(new BasicHep3Vector(0,0,-1)).get(0);

					sensor.setBiasSurface(ChargeCarrier.HOLE,p_side);
					sensor.setBiasSurface(ChargeCarrier.ELECTRON,n_side);

					// Add sense and readout electrodes
					ITranslation3D electrodes_position = new Translation3D(VecOp.mult(-p_side.getDistance(),p_side.getNormal()));  // translate to p_side
					IRotation3D electrodes_rotation = new RotationPassiveXYZ(0.0,0.0,0.0);                                      // no rotation (global x-y = local x-y for axial strips)
					Transform3D electrodes_transform = new Transform3D(electrodes_position, electrodes_rotation);

					// Free calculation of readout electrodes, sense electrodes determined thereon
					SiSensorElectrodes readout_electrodes = new SiStrips(ChargeCarrier.HOLE,this.readoutElectrodesPitch,sensor,electrodes_transform);
					SiSensorElectrodes sense_electrodes = new SiStrips(ChargeCarrier.HOLE,senseElectrodesPitch,(readout_electrodes.getNCells()*2-1),sensor,electrodes_transform);

					sensor.setSenseElectrodes(sense_electrodes);
					sensor.setReadoutElectrodes(readout_electrodes);

					double[][] transferEfficienciesMatrix = { transferEfficiencies };
					sensor.setTransferEfficiencies(ChargeCarrier.HOLE,new BasicMatrix(transferEfficienciesMatrix));                        
			}                
		}        
	}
}
