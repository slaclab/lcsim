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
import org.lcsim.detector.tracker.silicon.SiPixels;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.detector.tracker.silicon.SiSensorElectrodes;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.subdetector.SiTrackerEndcap;
import org.lcsim.geometry.subdetector.SiTrackerEndcap2;
import org.lcsim.util.Driver;

public class SiVertexEndcapSensorSetup extends Driver
{
	
	String subdetectorName;
	double readoutPitchX = 0.05;
	double readoutPitchY = 0.25;
	double sensePitchX = 0.05;
	double sensePitchY = 0.25;
	double transferEfficiency = 1.0;

	public SiVertexEndcapSensorSetup()
	{}
	
	public SiVertexEndcapSensorSetup(String subdetectorName)
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
		if (subdetector instanceof SiTrackerEndcap || subdetector instanceof SiTrackerEndcap2)
			setupSensorDetectorElements(subdetector);
		else
			throw new RuntimeException("The subdetector " + subdetectorName + " is not an instance of SiTrackerEndcap or SiTrackerEndcap2.");
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
        for (IDetectorElement endcap : subdet.getDetectorElement().getChildren()) {
            for (IDetectorElement layer : endcap.getChildren()) 
            {
                int nwedges = layer.getChildren().size();
                for (IDetectorElement wedge : layer.getChildren()) 
                {
                    for (IDetectorElement module : wedge.getChildren()) 
                    {	
                    	// find sensors on the module
                    	List<SiSensor> sensors = module.findDescendants(SiSensor.class);

                    	// require that sensors are found
    					if (sensors.size() == 0)
    						throw new RuntimeException("No sensors found in module.");

    					// loop over sensors (can be double-sided)
    					for (SiSensor sensor : sensors)
    					{
    						// get sensor field from id
    						int sensorId = sensor.getIdentifierHelper().getValue(sensor.getIdentifier(), "sensor");

    						// Get the sensor solid.
    						IPolyhedron sensor_solid = (IPolyhedron) sensor.getGeometry().getLogicalVolume().getSolid();

    						// Get solids for inner and outer surfaces.
    						Polygon3D inner_surface = sensor_solid.getFacesNormalTo(new BasicHep3Vector(0, -1, 0)).get(0);
    						Polygon3D outer_surface = sensor_solid.getFacesNormalTo(new BasicHep3Vector(0, 1, 0)).get(0);

    						//
    						// Determine p and n sides based on sensor id.
    						//
    						
    						Polygon3D p_side;
    						Polygon3D n_side;
    						int side;

    						if (sensorId == 0) // inner sensor
    						{
    							p_side = inner_surface;
    							n_side = outer_surface;
    							side = 1;
    						} 
    						else // outer sensor
    						{
    							p_side = outer_surface;
    							n_side = inner_surface;
    							side = -1;
    						}

    						double strip_angle = Math.PI / nwedges;

    						// Compute the geometric propertes of the electrodes.
    						ITranslation3D electrodes_position = new Translation3D(VecOp.mult(-p_side.getDistance(), new BasicHep3Vector(0, 0, 1)));  // translate to outside of polygon
    						IRotation3D electrodes_rotation = new RotationPassiveXYZ(side * (Math.PI / 2), 0, strip_angle);          //
    						Transform3D electrodes_transform = new Transform3D(electrodes_position, electrodes_rotation);

    						//
    						// Pixel-specific code starts here.
    						//
    						
    						// Set the bias surfaces.
    						sensor.setBiasSurface(ChargeCarrier.ELECTRON, p_side);
    						sensor.setBiasSurface(ChargeCarrier.HOLE, n_side);
            
    						//  Define the pixel electrodes.
    						SiSensorElectrodes readout_electrodes = new SiPixels(ChargeCarrier.ELECTRON, readoutPitchX, readoutPitchY, sensor, electrodes_transform);
    						SiSensorElectrodes sense_electrodes = new SiPixels(ChargeCarrier.ELECTRON, sensePitchX, sensePitchY, sensor, electrodes_transform);

    						//  Tell the sensor about the electrodes.
    						sensor.setSenseElectrodes(sense_electrodes);
    						sensor.setReadoutElectrodes(readout_electrodes);

    						//  Define the transfer efficiency from sense electrodes to readout electrodes.
    						//  For pixels, we do a direct transfer of charge from sense electrodes to readout electrodes.
    						double[][] transfer_efficiencies = {{transferEfficiency}};
    						sensor.setTransferEfficiencies(ChargeCarrier.ELECTRON, new BasicMatrix(transfer_efficiencies));
    					}
                    }
                }
            }
        }
	}
}