package org.lcsim.geometry.compact.converter.lcdd;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import static java.lang.Math.toDegrees;

import java.util.Iterator;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.Box;
import org.lcsim.geometry.compact.converter.lcdd.util.Define;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDDFactory;
import org.lcsim.geometry.compact.converter.lcdd.util.Material;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.PolyhedraRegular;
import org.lcsim.geometry.compact.converter.lcdd.util.Position;
import org.lcsim.geometry.compact.converter.lcdd.util.Rotation;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Trapezoid;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;
import org.lcsim.geometry.layer.LayerFromCompactCnv;
import org.lcsim.geometry.layer.LayerStack;
import org.lcsim.geometry.layer.Layering;

/*
 * Class to convert an EcalBarrel subdetector to the LCDD format.
 * This subdetector constructs barrel staves in a pinwheel arrangement
 * that is similar to the "ecal02" subdetector in the Mokka database.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: EcalBarrel.java,v 1.22 2009/11/02 20:39:42 jeremy Exp $
 */
public class EcalBarrel extends LCDDSubdetector
{
	// Change to true if debugging.
	private boolean _debug = false;
	
	// 1 micron adjustment for geometric tolerances.
	// This is used to downsize the stave.  It is
	// also applied to the box dimensions of the
	// layers and slices.  Because these are half
	// measurements it results in a 2 micron tolerance
	// for these components.
	//private double tolerance=0.001;
	private double tolerance=0.0;	

	EcalBarrel(Element node) throws JDOMException
	{
		super(node);
	}

	/** Add the EcalBarrel geometry to an LCDD instance. */
	public void addToLCDD(LCDD lcdd, SensitiveDetector sens)
			throws JDOMException
	{	
		// Get the define block of LCDD.
		Define define = lcdd.getDefine();
		
		// The name of the detector.
		String name = node.getAttributeValue("name");

		// The subdetector ID.
		int id = node.getAttribute("id").getIntValue();
		
		// Dimensions element.
		Element dimensions = node.getChild("dimensions");
		
		// Optional staves element.
		Element staves = node.getChild("staves");

		// Check for required attributes.
		assert (dimensions != null);
		assert (dimensions.getAttribute("numsides") != null);
		assert (dimensions.getAttribute("rmin") != null);
		assert (dimensions.getAttribute("z") != null);

		// Number of sides.
		int nsides = dimensions.getAttribute("numsides").getIntValue();

		// Inner radius to front surface of barrel stave.
		double inner_radius = dimensions.getAttribute("rmin")
				.getDoubleValue();
		
		// The stave's Z dimension, which sets the Z of the subdetector.
		double module_y1 = dimensions.getAttribute("z").getDoubleValue();
		double module_y2 = module_y1;
				
		// Compute the delta phi per section.
		double dphi = PI * 2.0 / nsides;
		double hphi = dphi / 2;

		// Compute the total thickness of the subdetector.
		double module_z = LayerFromCompactCnv
				.computeDetectorTotalThickness(node);
			
		// Compute the center Y offset of a single module.
		double module_y_offset = inner_radius + module_z / 2;

		// Get the mother volume.
		Volume motherVolume = lcdd.pickMotherVolume(this);
		
        double totalThickness = org.lcsim.geometry.layer.LayerFromCompactCnv.computeDetectorTotalThickness(node);
        
        // Create the polyhedra envelope for the subdetector.
        PolyhedraRegular polyhedra = new PolyhedraRegular(
                name + "_polyhedra",
                nsides, inner_radius, inner_radius+totalThickness+tolerance*2.0, module_y1);
        lcdd.getSolids().addSolid(polyhedra);
        
        // Create the volume for the envelope.
        Volume envelopeVolume = new Volume(name + "_envelope");
        envelopeVolume.setSolid(polyhedra);
        Material air = lcdd.getMaterial("Air");
        envelopeVolume.setMaterial(air);
                
        // Set the rotation to make a side lay "flat".
        double zrot = Math.PI / nsides;
        Rotation rot = new Rotation(name + "_rotation");
        rot.setZ(zrot);
        define.addRotation(rot);
        
        // Create the physical volume of the subdetector.
        PhysVol envelopePhysvol = new PhysVol(envelopeVolume);
        envelopePhysvol.setRotation(rot);
        envelopePhysvol.addPhysVolID("system",id);
        envelopePhysvol.addPhysVolID("barrel",0);
        motherVolume.addPhysVol(envelopePhysvol);
		
		// Compute the outer radius.
		double outer_radius = inner_radius + module_z;

		// Compute trapezoid measurements.
		double bo = tan(hphi) * outer_radius;
		double bi = tan(hphi) * inner_radius;

		// Compute the dx per layer, using side 
		// triangle calculations (from Norman Graf).
		double gamma = (PI * 2) / nsides;
		double dx = module_z / sin(gamma);

		// The offset of a stave, derived from the dx term.
		double module_x_offset = dx / 2.0;

		// Compute the top and bottom face measurements.
		double module_x2 = 2 * bo - dx;
		double module_x1 = 2 * bi + dx;
		
		// Create the trapezoid for the stave.
		Trapezoid module_trd = LCDDFactory.createTrapezoid(
				name + "_module_trd", 
				module_x1/2-tolerance, // Outer side, i.e. the "short" X side.
				module_x2/2-tolerance, // Inner side, i.e. the "long" X side.
				module_y1/2-tolerance, // Corresponds to subdetector (or module) Z.
				module_y2/2-tolerance, // "
				module_z/2-tolerance); // Thickness, in Y for top stave, when rotated.
		lcdd.add(module_trd);

		// Create the logical volume for the stave.
		Volume module_volume = LCDDFactory.createVolume(name + "_module", lcdd
				.getMaterial("Air"), module_trd);

		// DEBUG prints
		if (_debug)
		{
			System.out.println("name=" + name);
			System.out.println("nsides=" + nsides);
			System.out.println("inner_radius=" + inner_radius);
			System.out.println("module_y1=" + module_y1);
			System.out.println("module_y2=" + module_y2);
			System.out.println("module_z=" + module_z);
			System.out.println("module_y_offset=" + module_y_offset);
			System.out.println("module_x_offset=" + module_x_offset);
			System.out.println("gamma=" + gamma);
			System.out.println("dx=" + dx);
			System.out.println("bi=" + bi);
			System.out.println("bo=" + bo);
			System.out.println("");
		}

		// Build the stave logical volume.
		try
		{
			buildBarrelStave(lcdd, sens, module_volume);
		} 
		catch (Exception e)
		{
			throw new RuntimeException("Failed to build layers into "
					+ module_volume.getVolumeName(), e);
		}
				
		// Set stave visualization.
		if (staves != null)
		{
			if (staves.getAttribute("vis") != null)
			{
				module_volume.setVisAttributes(lcdd.getVisAttributes(staves.getAttributeValue("vis")));
			}
		}
		
		// Add the stave volume to LCDD.				
		lcdd.add(module_volume);

		// Phi start for a stave.
		double phi = ((PI) / nsides);

		// Create nsides staves.
		for (int i = 0; i < nsides; i++)
		{			
			// Module number.
			int module_number = i;

			// Rotation of the module.			
			Rotation rotation = LCDDFactory.createRotation(name + "_module"
					+ module_number + "_rotation", PI * 0.5, phi, 0);
			lcdd.add(rotation);
		
			// Compute the stave position; derived from calculations in Mokka 
			// Geometry/Tesla/Ecal02.cc
			double module_position_x = module_x_offset * cos(phi) - module_y_offset * sin(phi);
			double module_position_y = module_x_offset * sin(phi) + module_y_offset * cos(phi);
			double module_position_z = 0;

			Position position = LCDDFactory.createPosition(
					name + "_module" + module_number + "_position", 
					module_position_x, module_position_y, module_position_z);
			lcdd.add(position);

			// Place this module.
			PhysVol pv = LCDDFactory.createPhysVol(module_volume, position,
					rotation, null);
			pv.addPhysVolID("module", module_number);

			// FIXME: put these ids on subdetector envelope when have it
			pv.addPhysVolID("system", node.getAttribute("id").getIntValue());
			pv.addPhysVolID("barrel", 0);

			envelopeVolume.addPhysVol(pv);

			// increment phi
			phi -= dphi;
		}
		        
        // Set envelope volume attributes.
        setAttributes(lcdd, node, envelopeVolume);
		
		lcdd.getStructure().addVolume(envelopeVolume);
	}

	/**
	 * Build the barrel stave logical volume for this component.
	 * @param lcdd The LCDD file being created.
	 * @param subdetector The current EcalBarrel subdetector.
	 * @param sensitiveDetector The sensitive detector of the subdetector.
	 * @param container The trapezoid volume of the stave, to be filled with layers.
	 */
	private void buildBarrelStave(LCDD lcdd, /*LCDDSubdetector subdetector,*/
			SensitiveDetector sensitiveDetector, Volume container)
			throws Exception
	{
		Trapezoid trd = (Trapezoid) lcdd.getSolid(container.getSolidRef());

		Element node = getElement();

		if (trd == null)
		{
			throw new IllegalArgumentException("Volume " + container.getName()
					+ " is not a trapezoid.");
		}

		double nsides = getElement().getChild("dimensions")
				.getAttribute("numsides").getDoubleValue();

		Rotation irot = lcdd.getDefine().getRotation("identity_rot");

		double z = trd.y1();
		double trd_z = trd.z();

		// ------
		// Parameters for computing the layer X dimension, 
		// e.g. trapezoid's X1 value.
		// ------
		
		// Adjacent angle of triangle.
		double adj = (trd.x1() - trd.x2()) / 2;
		
		// Hypotenuse of triangle.
		double hyp = sqrt(trd_z * trd_z + adj * adj);
		
		// Lower-right angle of triangle.
		double beta = acos(adj / hyp);
		
		// Primary coefficient for figuring X.
		double tan_beta = tan(beta); 
		
		double subdetector_thickness = LayerFromCompactCnv
				.computeDetectorTotalThickness(node);

		double layer_position_z = -(subdetector_thickness / 2);

		String subdetector_name = getName();

		// Delta phi.
		double dphi = PI * 2.0 / nsides;

		// Half delta phi.
		double hphi = dphi / 2;

		// Starting X dimension for the layer.
		double layer_dim_x = trd.x1();		

		if (_debug)
		{
			System.out.println("slice start posZ=" + layer_position_z);
			System.out.println("dphi=" + toDegrees(dphi));
			System.out.println("hphi=" + toDegrees(hphi));
			System.out.println("starting slice X=" + layer_dim_x);
			System.out.println("adj=" + adj);
			System.out.println("beta=" + toDegrees(beta));
			System.out.println("");
		}
		
		Layering layering = Layering.makeLayering(node);
		
		LayerStack layers = layering.getLayerStack();
		
		// Loop over the sets of layer elements in the detector.
		int layer_number = 0;
		for (Iterator i = getElement().getChildren("layer")
				.iterator(); i.hasNext();)
		{
			Element layer_element = (Element) i.next();
			int repeat = (int)layer_element.getAttribute("repeat").getDoubleValue();

			// Loop over number of repeats for this layer.
			for (int j=0; j<repeat; j++)
			{
				// Compute this layer's thickness.
				double layer_thickness = layers.getLayer(layer_number).getThickness();
				
				// Increment the Z position to place this layer.
				layer_position_z += layer_thickness / 2;
				
				// Name of the layer.
				String layer_name = subdetector_name + "_layer" + layer_number;
				
				// Position of the layer.
				Position layer_position = LCDDFactory.createPosition(
						layer_name + "_position", 0, 0, layer_position_z);
				lcdd.add(layer_position);
	
				// Compute the X dimension for this layer.
				double xcut = (layer_thickness / tan_beta);
				layer_dim_x -= xcut; 
								
				// Box of the layer.				
				Box layer_box = LCDDFactory.createBox(layer_name + "_box",
						layer_dim_x*2 - tolerance, 
						z*2 - tolerance, 
						layer_thickness - tolerance);
				lcdd.add(layer_box);
				
				// Volume of the layer.
				Volume layer_volume = LCDDFactory.createVolume(layer_name,
						lcdd.getMaterial("Air"), layer_box);

				// Loop over the sublayers or slices for this layer.
				int slice_number = 0;
				double slice_position_z = -(layer_thickness / 2);
				for (Iterator k = layer_element.getChildren("slice").iterator(); k.hasNext();)
				{
					// XML element of slice.
					Element slice_element = (Element) k.next();
					
					// Name of the slice.
					String slice_name = layer_name + "_slice" + slice_number;
					
					// Sensitivity.
					Attribute s = slice_element.getAttribute("sensitive");
					boolean sensitive = s != null && s.getBooleanValue();

					// Thickness of slice.
					double slice_thickness = slice_element.getAttribute("thickness").getDoubleValue();

					// Increment Z position of slice.
					slice_position_z += slice_thickness / 2;
									
					// Position of slice.
					Position slice_position = LCDDFactory.createPosition(
							slice_name + "_position", 0, 0, slice_position_z);
					lcdd.add(slice_position);
					
					// Box of slice.
					Box slice_box = LCDDFactory.createBox(slice_name + "_box",
							layer_dim_x*2 - tolerance, 
							z*2 - tolerance, 
							slice_thickness - tolerance);

					lcdd.add(slice_box);

					// material of slice
					Material sliceMaterial = lcdd.getMaterial(slice_element
							.getAttributeValue("material"));

					// volume of slice
					Volume slice_volume = LCDDFactory.createVolume(slice_name,
							sliceMaterial, slice_box);
					if (sensitive)
					{
						slice_volume.setSensitiveDetector(sensitiveDetector);
					}				
					
					setRegion(lcdd, slice_element, slice_volume);
					setLimitSet(lcdd, slice_element, slice_volume);
					setVisAttributes(lcdd, slice_element, slice_volume);
					
					// Add slice volume to LCDD.
					lcdd.add(slice_volume);

					// Slice placement.
					PhysVol slice_physvol = LCDDFactory.createPhysVol(
							slice_volume, slice_position, irot);
					slice_physvol.addPhysVolID("layer", layer_number);
					slice_physvol.addPhysVolID("slice", slice_number);
					layer_volume.addPhysVol(slice_physvol);					
					
					// Increment Z position of slice.
					slice_position_z += slice_thickness / 2;
					
					// Increment slice number.
					++slice_number;
				}
				
                // Set region, limitset, and vis of layer.
                setRegion(lcdd, layer_element, layer_volume);
                setLimitSet(lcdd, layer_element, layer_volume);
                setVisAttributes(lcdd, layer_element, layer_volume);
                
				lcdd.add(layer_volume);
				
				// Place the layer.
				PhysVol layer_physvol = LCDDFactory.createPhysVol(
						layer_volume, layer_position, irot);
				layer_physvol.addPhysVolID("layer", layer_number);
				container.addPhysVol(layer_physvol);

				// Increment to next layer Z position.
				layer_position_z += layer_thickness / 2;
				
				// Increment layer number.
				++layer_number;
			}
		}			
		
	}

	public boolean isCalorimeter()
	{
		return true;
	}

}

// parameters from Mokka's ecal02 DB
//
// int nsides = 8;
// double inner_radius = 1700.0;
// double module_x_offset = 131.522;
// double module_y_offset = 1792.0;
// double module_x1=832.271;
// double module_x2=648.271;
// double module_y1=546.0;
// double module_y2=546.0;
// double module_z=92.0;
