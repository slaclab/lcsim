package org.lcsim.geometry.compact.converter.lcdd;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

import java.util.Iterator;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.Box;
import org.lcsim.geometry.compact.converter.lcdd.util.Define;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.Material;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.PolyhedraRegular;
import org.lcsim.geometry.compact.converter.lcdd.util.Position;
import org.lcsim.geometry.compact.converter.lcdd.util.Rotation;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Solids;
import org.lcsim.geometry.compact.converter.lcdd.util.Structure;
import org.lcsim.geometry.compact.converter.lcdd.util.Trapezoid;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;
import org.lcsim.geometry.layer.Layer;
import org.lcsim.geometry.layer.Layering;

/**
 *
 * Convert a PolyhedraBarrelCalorimeter to LCDD format.
 *
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class PolyhedraBarrelCalorimeter2 extends LCDDSubdetector
{
    private Element node;   
        
    public PolyhedraBarrelCalorimeter2(Element node) throws JDOMException
    {
        super(node);
        this.node = node;
    }

    public void addToLCDD(LCDD lcdd, SensitiveDetector sens) throws JDOMException
    {
        // Get some important LCDD references.
        Solids solids = lcdd.getSolids();
        Structure structure = lcdd.getStructure();
        Volume motherVolume = lcdd.pickMotherVolume(this);
        Material air = lcdd.getMaterial("Air");
        Define define = lcdd.getDefine();
        Material gapMaterial = air;
        if (node.getAttribute("material") != null)
            gapMaterial = lcdd.getMaterial(node.getAttribute("material").getValue());

        // Subdetector name and ID.
        String detName = node.getAttributeValue("name");
        int id = node.getAttribute("id").getIntValue();
        
        Element staves = node.getChild("staves");
        
        double gap = 0.0;
        if (node.getAttribute("gap") != null)
        {
            gap = node.getAttribute("gap").getDoubleValue();
        }
                
        // Subdetector envelope dimensions.
        Element dimensions = node.getChild("dimensions");
        double detZ = dimensions.getAttribute("z").getDoubleValue();
        double rmin = dimensions.getAttribute("rmin").getDoubleValue();
        int numsides = dimensions.getAttribute("numsides").getIntValue();

        // Rotation of the envelope to make flat side down.
        double zrot = Math.PI / numsides;
        Rotation rot = new Rotation(detName + "_rotation");
        rot.setZ(zrot);
        define.addRotation(rot);

        // Create layering object for this subdetector.
        Layering layering = Layering.makeLayering(this.node);

        // Total thickness of subdetector.
        double total_thickness = layering.getLayerStack().getTotalThickness();
        
        int total_repeat = 0;
        int total_slices = 0;
        for (Iterator i = node.getChildren("layer").iterator(); i.hasNext();)
        {
            Element layer_element = (Element) i.next();
            int repeat = (int)layer_element.getAttribute("repeat").getDoubleValue();
            total_repeat += repeat;         
            total_slices += (layer_element.getChildren("slice").size() * total_repeat);         
        }
                                                                
        // Envelope volume for subdetector.
        PolyhedraRegular polyhedra = new PolyhedraRegular(
                detName + "_polyhedra", 
                numsides, 
                rmin, 
                rmin + total_thickness, 
                detZ);
        solids.addSolid(polyhedra);

        Volume envelopeVolume = new Volume(detName + "_envelope");
        envelopeVolume.setSolid(polyhedra);
        envelopeVolume.setMaterial(air);
        
        PhysVol envelopePhysvol = new PhysVol(envelopeVolume);
        envelopePhysvol.setRotation(rot);
        envelopePhysvol.addPhysVolID("system", id);
        envelopePhysvol.addPhysVolID("barrel", 0);
        motherVolume.addPhysVol(envelopePhysvol);

        // A trapezoid stave.
        double innerAngle = Math.PI * 2 / numsides;
        double halfInnerAngle = innerAngle / 2;
        double innerFaceLength = rmin * tan(halfInnerAngle) * 2;
        double rmax = rmin + total_thickness;
        double outerFaceLength = rmax * tan(halfInnerAngle) * 2;        
        double stave_thickness = total_thickness;
        
        // Make trap outer stave.
        Trapezoid staveTrdOuter = new Trapezoid(detName + "_stave_trapezoid_outer");
        staveTrdOuter.setY2(detZ/2);
        staveTrdOuter.setY1(detZ/2);
        staveTrdOuter.setZ(stave_thickness/2);
        staveTrdOuter.setX1(innerFaceLength/2);
        staveTrdOuter.setX2(outerFaceLength/2);
        solids.addSolid(staveTrdOuter);
                
        // Make trap outer volume.
        Volume staveOuterVolume = new Volume(detName + "_stave_outer");
        staveOuterVolume.setSolid(staveTrdOuter);
        staveOuterVolume.setMaterial(gapMaterial);        
        
        // Make trap inner stave.
        Trapezoid staveTrdInner = new Trapezoid(detName + "_stave_trapezoid_inner");
        staveTrdInner.setY2(detZ/2);
        staveTrdInner.setY1(detZ/2);
        staveTrdInner.setZ(stave_thickness/2);
        staveTrdInner.setX1(innerFaceLength/2 - gap);
        staveTrdInner.setX2(outerFaceLength/2 - gap);
        solids.addSolid(staveTrdInner);
        
        // Make trap outer volume.
        Volume staveInnerVolume = new Volume(detName + "_stave_inner");
        staveInnerVolume.setMaterial(air);
        staveInnerVolume.setSolid(staveTrdInner);
               
        // Create layers.               
        double layerOuterAngle = (PI - innerAngle) / 2;
        double layerInnerAngle = (PI / 2 - layerOuterAngle);        
        int layer_number = 0;
        double layer_position_z = -(stave_thickness / 2);                        
        double layer_dim_x = innerFaceLength - gap * 2;
        
        for (Iterator i = node.getChildren("layer").iterator(); i.hasNext();)
        {
            Element layer_element = (Element) i.next();

            // Get the layer from the layering engine.
            Layer layer = layering.getLayer(layer_number);

            // Get number of times to repeat this layer.
            int repeat = (int)layer_element.getAttribute("repeat").getDoubleValue();

            // Loop over repeats for this layer.
            for (int j = 0; j < repeat; j++)
            {                
                // Name of the layer.
                String layer_name = detName + "_stave_layer" + layer_number;

                // Layer thickness.
                double layer_thickness = layer.getThickness();              

                int nslices = layer_element.getChildren("slices").size();
                                                                
                // Layer position in Z within the stave.
                layer_position_z += layer_thickness / 2;
                                                                                            
                // Position of layer.
                Position layer_position = new Position(layer_name + "_position");
                layer_position.setZ(layer_position_z);
                define.addPosition(layer_position);

                // Layer box.
                Box layer_box = new Box(layer_name + "_box");
                layer_box.setX(layer_dim_x);
                layer_box.setY(detZ);
                layer_box.setZ(layer_thickness);
                solids.addSolid(layer_box);

                // Layer volume. 
                Volume layer_volume = new Volume(layer_name);
                layer_volume.setSolid(layer_box);
                layer_volume.setMaterial(air);

                // Create the slices (sublayers) within the layer.
                double slice_position_z = -(layer_thickness / 2);
                                                                
                int slice_number = 0;
                for (Iterator k = layer_element.getChildren("slice").iterator(); k.hasNext();)
                {
                    Element slice_element = (Element) k.next();

                    String slice_name = layer_name + "_slice" + slice_number;

                    Attribute s = slice_element.getAttribute("sensitive");
                    boolean sensitive = s != null && s.getBooleanValue();

                    double slice_thickness = slice_element.getAttribute("thickness").getDoubleValue();

                    slice_position_z += slice_thickness / 2;
                    
                    Material slice_material = lcdd.getMaterial(slice_element.getAttributeValue("material"));

                    // Slice Position.
                    Position slice_position = new Position(slice_name + "_position");
                    slice_position.setZ(slice_position_z);
                    define.addPosition(slice_position);

                    // Slice box. 
                    Box slice_box = new Box(slice_name + "_box");
                    slice_box.setX(layer_dim_x);
                    slice_box.setY(detZ);
                    slice_box.setZ(slice_thickness);
                    solids.addSolid(slice_box);

                    // Slice volume.
                    Volume slice_volume = new Volume(slice_name);
                    slice_volume.setSolid(slice_box);
                    slice_volume.setMaterial(slice_material);
                    if (sensitive)
                        slice_volume.setSensitiveDetector(sens);
                    structure.addVolume(slice_volume);

                    // Set region, limitset, and vis.
                    setRegion(lcdd, slice_element, slice_volume);
                    setLimitSet(lcdd, slice_element, slice_volume);
                    setVisAttributes(lcdd, slice_element, slice_volume);

                    // slice PhysVol
                    PhysVol slice_physvol = new PhysVol(slice_volume);
                    slice_physvol.setPosition(slice_position);
                    slice_physvol.addPhysVolID("slice", slice_number);
                    layer_volume.addPhysVol(slice_physvol);

                    // Increment Z position for next slice.
                    slice_position_z += slice_thickness / 2;

                    // Increment slice number.
                    ++slice_number;             
                }

                // Set region, limitset, and vis.
                setRegion(lcdd, layer_element, layer_volume);
                setLimitSet(lcdd, layer_element, layer_volume);
                setVisAttributes(lcdd, layer_element, layer_volume);
                
                // Add the layer logical volume to the structure.
                structure.addVolume(layer_volume);

                // Layer physical volume.
                PhysVol layer_physvol = new PhysVol(layer_volume);
                layer_physvol.setPosition(layer_position);
                layer_physvol.addPhysVolID("layer", layer_number);
                staveInnerVolume.addPhysVol(layer_physvol);

                // Increment the layer X dimension.
                layer_dim_x += layer_thickness * tan(layerInnerAngle) * 2;

                // Increment the layer Z position.
                layer_position_z += layer_thickness / 2;

                // Increment the layer number.
                ++layer_number;         
            }
        }
                        
        // Make stave inner physical volume.
        PhysVol staveInnerPhysVol = new PhysVol(staveInnerVolume);
        
        // Add stave inner physical volume to outer stave volume.
        staveOuterVolume.addPhysVol(staveInnerPhysVol);
        
        // Set the vis attributes of the outer stave section.
        if (staves != null)
        {
            if (staves.getAttribute("vis") != null)
            {
                setVisAttributes(lcdd,staves,staveOuterVolume);
            }
        }
        
        // Add the stave inner volume to the structure.
        structure.addVolume(staveInnerVolume);
        
        // Add the stave outer volume to the structure.
        structure.addVolume(staveOuterVolume);

        // Place the staves.
        placeStaves(define, detName, rmin, numsides, total_thickness, envelopeVolume, innerAngle, staveOuterVolume);
        
        // Set envelope volume attributes.
        setAttributes(lcdd, node, envelopeVolume);
        
        // Add the subdetector envelope to the structure.
        structure.addVolume(envelopeVolume);
    }

    private void placeStaves(Define define, String detName, double rmin, int numsides, double total_thickness, Volume envelopeVolume, double innerAngle, Volume sectVolume)
    {
        // Place the staves.
        double innerRotation = innerAngle;
        double offsetRotation= -innerRotation / 2;
        double sectCenterRadius = rmin + total_thickness / 2;
        double rotY = -offsetRotation;
        double rotX = PI / 2;
        double posX = -sectCenterRadius * sin(rotY);
        double posY = sectCenterRadius * cos(rotY);
        
        for (int i = 0; i < numsides; i++)
        {
            int moduleNumber = i;

            Position position = new Position(detName + "_stave0_module" + moduleNumber + "_position");
            position.setX(posX);
            position.setY(posY);

            Rotation rotation = new Rotation(detName + "_stave0_module" + moduleNumber + "_rotation");
            rotation.setX(rotX);
            rotation.setY(rotY);

            define.addPosition(position);
            define.addRotation(rotation);

            PhysVol sectPhysVol = new PhysVol(sectVolume);
            sectPhysVol.setPosition(position);
            sectPhysVol.setRotation(rotation);

            envelopeVolume.addPhysVol(sectPhysVol);
            sectPhysVol.addPhysVolID("stave", 0);
            sectPhysVol.addPhysVolID("module", moduleNumber);

            rotY -= innerRotation;
            posX = -sectCenterRadius * sin(rotY);
            posY = sectCenterRadius * cos(rotY);            
        }
    }

    public boolean isCalorimeter()
    {
        return true;
    }
}