package org.lcsim.geometry.compact.converter.lcdd;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.Box;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.Material;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.Position;
import org.lcsim.geometry.compact.converter.lcdd.util.Rotation;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Solids;
import org.lcsim.geometry.compact.converter.lcdd.util.Structure;
import org.lcsim.geometry.compact.converter.lcdd.util.Tube;
import org.lcsim.geometry.compact.converter.lcdd.util.VisAttributes;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;

/**
 * 
 * Convert an SiTrackerBarrel subdetector to the LCDD format.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @author Tim Nelson <tknelson@slac.stanford.edu>
 *
 */
public class SiTrackerBarrel extends LCDDSubdetector
{
	Map<String, Volume> modules = new HashMap<String, Volume>();

	public SiTrackerBarrel(Element node) throws JDOMException
	{
		super(node);
	}

	/**
	 * Build the LCDD for the subdetector.
	 * @param lcdd The LCDD file being created.
	 * @param sens The SD for this subdetector.
	 */
	public void addToLCDD(LCDD lcdd, SensitiveDetector sens) throws JDOMException
	{
		// ID of the detector.
		int id = this.node.getAttribute("id").getIntValue();

		// Name of the detector.
		String detector_name = this.node.getAttributeValue("name");

		// Get air from LCDD materials DB.
		Material air = lcdd.getMaterial("Air");

		// Get solids collection from LCDD. 
		Solids solids = lcdd.getSolids();

		// Get structure collection from LCDD.
		Structure structure = lcdd.getStructure();

		// Pick the mother volume (tracking volume).
		Volume tracking_volume = lcdd.pickMotherVolume(this);
		
		VisAttributes vis = null;
		if (node.getAttribute("vis") != null)
		    vis = lcdd.getVisAttributes(node.getAttributeValue("vis"));

		// Loop over the modules and put them into a map for lookup later.
		for (Iterator i = node.getChildren("module").iterator(); i.hasNext();)
		{
			Element module = (Element) i.next();
			String module_name = module.getAttributeValue("name");
			Volume module_envelope;
			try
			{
				module_envelope = buildModule(node, module_name, lcdd, sens, vis);
				modules.put(module_name, module_envelope);
			}
			catch (Exception x)
			{
				throw new RuntimeException(x);
			}
		}

		// Build the layers.
		for (Iterator i = node.getChildren("layer").iterator(); i.hasNext();)
		{
			// Get the next layer element.
			Element layer_element = (Element) i.next();
			
			int layern = layer_element.getAttribute("id").getIntValue();

			// Get the reference to the module from the layer.
			String module_name = layer_element.getAttributeValue("module");

			// Get the logical volume for the module.
			Volume module_envelope = modules.get(module_name);

			// Get the barrel_envelope for this layer.
			Element barrel_envelope = layer_element.getChild("barrel_envelope");

			// Inner radius of layer.
			double ir = barrel_envelope.getAttribute("inner_r").getDoubleValue();

			// Outer radius of layer.
			double or = barrel_envelope.getAttribute("outer_r").getDoubleValue();

			// Full length in z of layer.
			double oz = barrel_envelope.getAttribute("z_length").getDoubleValue();

			// Name of this layer including layer number.
			String layer_name = detector_name + "_layer" + layern;

			//System.out.println("layer_name=" + layer_name);

			// Create the layer tube solid.
			Tube layer_tube = new Tube(layer_name + "_tube");
			layer_tube.setRMin(ir);
			layer_tube.setRMax(or);
			layer_tube.setZ(oz);
			solids.addContent(layer_tube);

			// Create the layer envelope volume.
			Volume layer_volume = new Volume(layer_name);
			layer_volume.setMaterial(air);
			layer_volume.setSolid(layer_tube);

			// Get the rphi_layout element.
			Element rphi_layout = layer_element.getChild("rphi_layout");

			// Starting phi of first module.
			double phi0 = rphi_layout.getAttribute("phi0").getDoubleValue();

			// Number of modules in phi.
			int nphi = rphi_layout.getAttribute("nphi").getIntValue();
			assert (nphi > 0);

			// Phi tilt of a module.
			double phi_tilt = rphi_layout.getAttribute("phi_tilt").getDoubleValue();

			// Radius of the module center.
			double rc = rphi_layout.getAttribute("rc").getDoubleValue();

			// The delta radius of every other module.
			double rphi_dr = 0.0;
			if (rphi_layout.getAttribute("dr") != null)
			{
				rphi_dr = rphi_layout.getAttribute("dr").getDoubleValue();
			}

			// Phi increment for one module.
			double phi_incr = (Math.PI * 2) / nphi;

			// Phi of the module center.
			double phic = 0;
			phic += phi0;

			// Get the <z_layout> element.
			Element z_layout = layer_element.getChild("z_layout");

			// Z position of first module in phi.
			double z0 = z_layout.getAttribute("z0").getDoubleValue();

			// Number of modules to place in z.
			double nz = z_layout.getAttribute("nz").getIntValue();
			assert (nz > 0);

			// Radial displacement parameter, of every other module.
			double z_dr = z_layout.getAttribute("dr").getDoubleValue();

			// Z increment for module placement along Z axis.
			// Adjust for z0 at center of module rather than
			// the end of cylindrical envelope.
			double z_incr = (2.0 * z0) / (nz - 1);

			// Starting z for module placement along Z axis.
			double module_z = -z0;

			// DEBUG 
			//System.out.println("layer ir=" + ir);
			//System.out.println("layer or=" + or);
			//System.out.println("layer oz=" + oz);
			//System.out.println("phi_tilt=" + phi_tilt);
			//System.out.println("rc=" + rc);
			//System.out.println("phi0=" + phi0);
			//System.out.println("module z_incr=" + z_incr);
			//System.out.println("module z0=" + z0);
			//System.out.println("module nz=" + nz);
			//System.out.println("module dr=" + dr);
			//

			//String module_lkp_name = layer.getAttributeValue("module");
            
            int module = 0;
            
			// Loop over the number of modules in phi.
			for (int ii = 0; ii < nphi; ii++)
			{
				// Delta x of module position.
				double dx = z_dr * cos(phic + phi_tilt);

				// Delta y of module position.
				double dy = z_dr * sin(phic + phi_tilt);

				// Basic x module position.
				double x = rc * cos(phic);

				// Basic y module position.
				double y = rc * sin(phic);

				// Loop over the number of modules in z.
				for (int j = 0; j < nz; j++)
				{
					// Create a unique name for the module in this logical volume, layer, phi, and z.
					String module_place_name = detector_name + "_layer" + layern + "_phi" + ii + "_z" + j;

					double z = module_z;

					// DEBUG
					//System.out.println("module build...");
					//System.out.println("module nphi=" + ii);
					//System.out.println("module nz" + j);
					//System.out.println("module x=" + x);
					//System.out.println("module y=" + y);
					//System.out.println("module z=" + z);
					// DEBUG

					// Position of module.
					Position module_position = new Position(module_place_name + "_position");
					module_position.setX(x);
					module_position.setY(y);
					module_position.setZ(z);
					lcdd.getDefine().addPosition(module_position);

					// Rotation of module.
					Rotation module_rotation = new Rotation(module_place_name + "_rotation");
					double rotx = Math.PI / 2;
					double roty = -((Math.PI / 2) - phic - phi_tilt);
					double rotz = 0;
					module_rotation.setX(rotx);
					module_rotation.setY(roty);
					module_rotation.setZ(rotz);
					lcdd.getDefine().addRotation(module_rotation);

					//System.out.println("module rotx=" + rotx);
					////System.out.println("module roty=" + roty);
					//System.out.println("module rotz=" + rotz);

					// Module PhysicalVolume.
					PhysVol module_physvol = new PhysVol(module_envelope);
					module_physvol.setPosition(module_position);
					module_physvol.setRotation(module_rotation);
					//module_physvol.addPhysVolID("phi", ii);
					//module_physvol.addPhysVolID("z", j);
                    module_physvol.addPhysVolID("module", module);
                    ++module;
					layer_volume.addPhysVol(module_physvol);

					// Adjust the x and y coordinates of the module.
					x += dx;
					y += dy;

					// Flip sign of x and y adjustments.
					dx *= -1;
					dy *= -1;

					// Add z increment to get next z placement pos.
					module_z += z_incr;

					//System.out.println();
				}

				// Increment the phi placement of module.
				phic += phi_incr;

				// Increment the center radius according to dr parameter.
				rc += rphi_dr;

				// Flip sign of dr parameter.
				rphi_dr *= -1;

				// Reset the Z placement parameter for module.
				module_z = -z0;
			}

            setRegion(lcdd, layer_element, layer_volume);
            setLimitSet(lcdd, layer_element, layer_volume);
			//setVisAttributes(lcdd, layer_element, layer_volume);
            
			// Set the layer envelope to invisible to help Geant4 visualization.
			//if (layer_element.getAttribute("vis") == null)
			//{
			//	if (lcdd.getVisAttributes("InvisibleWithDaughters") != null)
			//	{
			//		layer_volume.setVisAttributes(lcdd.getVisAttributes("InvisibleWithDaughters"));
			//	}
			//}
            
			// Add the layer volume to LCDD.
			structure.addVolume(layer_volume);

			// Create the PhysicalVolume for the layer.
			PhysVol layer_envelope_physvol = new PhysVol(layer_volume);

			// Set the subdetector system ID.
			layer_envelope_physvol.addPhysVolID("system", id);

			// Flag this as a barrel subdetector.
			layer_envelope_physvol.addPhysVolID("barrel", 0);

			// Set the layer ID.
			layer_envelope_physvol.addPhysVolID("layer", layern);

			// Put the layer into the mother volume.
			tracking_volume.addPhysVol(layer_envelope_physvol);
		}
	}

	/**
	 * Build a module logical volume.
	 * @param detector The detector XML node.
	 * @param name The name of the module for lookup.
	 * @param lcdd The LCDD being processed.
	 * @return
	 * @throws Exception
	 */
	Volume buildModule(Element detector, String name, LCDD lcdd, SensitiveDetector sens, VisAttributes vis) throws Exception
	{
		String detector_name = detector.getAttributeValue("name");
		Volume module_volume = null;

		int sensor_number = 0;

		VisAttributes visOff = lcdd.getVisAttributes("InvisibleNoDaughters");
		
		// Search for module.
		for (Iterator i = detector.getChildren("module").iterator(); i.hasNext();)
		{
			Element module_element = (Element) i.next();

			if (module_element.getAttributeValue("name").compareTo(name) == 0)
			{
				Element module_envelope_element = module_element.getChild("module_envelope");

				String module_name = detector_name + "_" + module_element.getAttributeValue("name");

				// Create the module box.
				double module_length = module_envelope_element.getAttribute("length").getDoubleValue();
				double module_width = module_envelope_element.getAttribute("width").getDoubleValue();
				double module_thickness = module_envelope_element.getAttribute("thickness").getDoubleValue();
				Box module_box = new Box(module_name + "_box");
				module_box.setX(module_width);
				module_box.setY(module_length);
				module_box.setZ(module_thickness);
				lcdd.getSolids().addSolid(module_box);

				// Create the module logical volume.
				module_volume = new Volume(module_name);
				module_volume.setMaterial(lcdd.getMaterial("Air"));
				module_volume.setSolid(module_box);

				// Build module components.
				int ncomponents = 0;
				for (Iterator j = module_element.getChildren("module_component").iterator(); j.hasNext(); ++ncomponents)
				{
					Element component_element = (Element) j.next();

					boolean sensitive = ((component_element.getAttribute("sensitive") == null) ? false : component_element.getAttribute("sensitive").getBooleanValue());

					String component_name = module_name + "_component" + ncomponents;

					// DEBUG
					//System.out.println("component_name=" + component_name);
					//System.out.println("build component="+ncomponents);
					//

					// Create the box solid for the module component.
					double component_length = component_element.getAttribute("length").getDoubleValue();
					double component_width = component_element.getAttribute("width").getDoubleValue();
					double component_thickness = component_element.getAttribute("thickness").getDoubleValue();
					Box component_box = new Box(component_name + "_box");
					component_box.setX(component_width);
					component_box.setY(component_length);
					component_box.setZ(component_thickness);
					lcdd.getSolids().addSolid(component_box);

					// Create the volume for the module component.
					Volume component_volume = new Volume(component_name);
					Material component_material = lcdd.getMaterial(component_element.getAttributeValue("material"));
					component_volume.setMaterial(component_material);
					component_volume.setSolid(component_box);
						
					lcdd.getStructure().addVolume(component_volume);                    
                    
					PhysVol component_physvol = new PhysVol(component_volume);

					// Set component position.
					// FIXME: Processing of positions should be generalized in compact description.					
					if (component_element.getChild("position") != null)
					{
						Element pos_elem = component_element.getChild("position");

						Position component_position = new Position(component_name + "_position");

						if (pos_elem.getAttribute("x") != null)
						{
							component_position.setX(pos_elem.getAttribute("x").getDoubleValue());
						}

						if (pos_elem.getAttribute("y") != null)
						{
							component_position.setY(pos_elem.getAttribute("y").getDoubleValue());
						}

						if (pos_elem.getAttribute("z") != null)
						{
							component_position.setZ(pos_elem.getAttribute("z").getDoubleValue());
						}

						lcdd.getDefine().addPosition(component_position);
						component_physvol.setPosition(component_position);
					}

					// Set component rotation.
					// FIXME: Processing of rotations should be generalized in compact description.
					if (component_element.getChild("rotation") != null)
					{
						Element rot_elem = component_element.getChild("rotation");

						Rotation component_rotation = new Rotation(component_name + "_rotation");

						if (rot_elem.getAttribute("x") != null)
						{
							component_rotation.setX(rot_elem.getAttribute("x").getDoubleValue());
						}

						if (rot_elem.getAttribute("y") != null)
						{
							component_rotation.setY(rot_elem.getAttribute("y").getDoubleValue());
						}

						if (rot_elem.getAttribute("z") != null)
						{
							component_rotation.setZ(rot_elem.getAttribute("z").getDoubleValue());
						}

						component_physvol.setRotation(component_rotation);

						lcdd.getDefine().addRotation(component_rotation);
					}

					if (sensitive)
					{
						component_volume.setSensitiveDetector(sens);
						component_physvol.addPhysVolID("sensor", sensor_number);
						++sensor_number;
					}
                    
                    setRegion(lcdd, component_element, component_volume);
                    setLimitSet(lcdd, component_element, component_volume);
                    //setVisAttributes(lcdd, component_element, component_volume);
                    component_volume.setVisAttributes(visOff);

					module_volume.addPhysVol(component_physvol);
				}
				//setVisAttributes(lcdd, module_element, module_volume);
				if (vis != null)
				    module_volume.setVisAttributes(vis);
				break;
			}
		}

		if (module_volume == null)
		{
			throw new RuntimeException("Failed to find module " + name);
		}
        
		// Add module logical volume to LCDD.
		lcdd.getStructure().addVolume(module_volume);

		return module_volume;
	}

	public boolean isTracker()
	{
		return true;
	}
}