package org.lcsim.recon.tracking.seedtracker.trackingdrivers.sidloi2;

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
import org.lcsim.detector.tracker.silicon.SiPixels;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.detector.tracker.silicon.SiSensorElectrodes;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.subdetector.SiTrackerBarrel;
import org.lcsim.util.Driver;

public class SiVertexBarrelSensorSetup extends Driver
{
	String subdetectorName;
	
    // Sets pixel size to x=0.05 and y=0.25 (mm)
	double readoutPitchX = 0.025;
	double readoutPitchY = 0.025;
	double sensePitchX = 0.025;
	double sensePitchY = 0.025;
	double transferEfficiency = 1.0;

	public SiVertexBarrelSensorSetup()
	{}
	
	public SiVertexBarrelSensorSetup(String subdetectorName)
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
	
	public void setReadoutPitchX(double x)
	{
		this.readoutPitchX = x;
	}
	
	public void setReadoutPitchY(double y)
	{
		this.readoutPitchY = y;
	}
	
	public void setSensePitchX(double x)
	{
		this.sensePitchX = x;
	}
	
	public void setSensePitchY(double y)
	{
		this.sensePitchY = y;
	}
	
	public void setTransferEfficiency(double t)
	{
		this.transferEfficiency = t;
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
					IPolyhedron sensor_solid = (IPolyhedron) sensor.getGeometry().getLogicalVolume().getSolid();
					
                    Polygon3D top_side = sensor_solid.getFacesNormalTo(new BasicHep3Vector(0, 0, 1)).get(0);
                    Polygon3D bot_side = sensor_solid.getFacesNormalTo(new BasicHep3Vector(0, 0, -1)).get(0);
                   
                    // collect electrons on the top side
                    sensor.setBiasSurface(ChargeCarrier.HOLE, top_side);
                    sensor.setBiasSurface(ChargeCarrier.ELECTRON, bot_side);

                    // Add sense and readout electrodes
                    ITranslation3D electrodes_position = new Translation3D(VecOp.mult(-top_side.getDistance(), top_side.getNormal()));  // translate to p_side
                    IRotation3D electrodes_rotation = new RotationPassiveXYZ(0.0, 0.0, 0.0);                     
                    // no rotation (global x-y = local x-y for axial strips)

                    Transform3D electrodes_transform = new Transform3D(electrodes_position, electrodes_rotation);

                    //  Define the pixel electrodes...collecting holes;
                    SiSensorElectrodes readout_electrodes = new SiPixels(ChargeCarrier.HOLE, this.readoutPitchX, this.readoutPitchY, sensor, electrodes_transform);
                    SiSensorElectrodes sense_electrodes = new SiPixels(ChargeCarrier.HOLE, this.sensePitchX, this.sensePitchY, sensor, electrodes_transform);

                    //  Tell the sensor about the electrodes
                    sensor.setSenseElectrodes(sense_electrodes);
                    sensor.setReadoutElectrodes(readout_electrodes);

                    //  Define the transfer efficiency from sense electrodes to readout electrodes
                    //  For pixels, we do a direct transfer of charge from sense electrodes to readout electrodes
                    double[][] transfer_efficiencies = {{this.transferEfficiency}};
                    sensor.setTransferEfficiencies(ChargeCarrier.HOLE, new BasicMatrix(transfer_efficiencies));
			}
		}
	}
}
