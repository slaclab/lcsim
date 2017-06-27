package org.lcsim.geometry.compact.converter.lcdd;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

import java.util.Iterator;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.Define;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.Material;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.Position;
import org.lcsim.geometry.compact.converter.lcdd.util.Rotation;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Solids;
import org.lcsim.geometry.compact.converter.lcdd.util.Structure;
import org.lcsim.geometry.compact.converter.lcdd.util.Trapezoid;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;
import org.lcsim.geometry.layer.LayerStack;
import org.lcsim.geometry.layer.Layering;

/**
 * Convert a PolyhedraEndcapCalorimeter to the LCDD format.
 *
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $id: $
 */
public class PolyhedraEndcapCalorimeter extends LCDDSubdetector
{
    private Element node;

    // Subtracted from stave envelope dimensions.
    //private final double STAVE_ENVELOPE_TOLERANCE=10.0;
    private final double STAVE_ENVELOPE_TOLERANCE=0.0;
    
    // Subtracted from layer envelope dimensions.
    //private final double LAYER_ENVELOPE_TOLERANCE=1.0;
    private final double LAYER_ENVELOPE_TOLERANCE=0.0;
    
    // Subtracted from slice dimensions.
    //private final double SLICE_ENVELOPE_TOLERANCE=1.0;
    private final double SLICE_ENVELOPE_TOLERANCE=0.0;
    
    // Small gap placed in front of each layer.
    //private final double INTER_LAYER_GAP=1.0;
    private final double INTER_LAYER_GAP=0.0;
    
    // Small subtraction from given thickness of slice.  
    //private final double SLICE_TOLERANCE=0.01;
    private final double SLICE_TOLERANCE=0.0;

    public PolyhedraEndcapCalorimeter(Element node) throws JDOMException
    {
        super(node);
        this.node = node;
    }

    public void addToLCDD(LCDD lcdd, SensitiveDetector sens) throws JDOMException
    {
        if ( sens == null)
        {
            throw new IllegalArgumentException("PolyhedraBarrelCalorimeter <" + getName() + " has null SD.");
        }

        // Get important LCDD objects.
        Solids solids = lcdd.getSolids();
        Structure structure = lcdd.getStructure();
        Volume motherVolume = lcdd.pickMotherVolume(this);
        Material air = lcdd.getMaterial("Air");
        Define define = lcdd.getDefine();
        Element staves = node.getChild("staves");

        // Subdetector name and ID.
        String detName = node.getAttributeValue("name");
        int id = node.getAttribute("id").getIntValue();

        // Subdetector envelope dimensions.
        Element dimensions = node.getChild("dimensions");
        double zmin = dimensions.getAttribute("zmin").getDoubleValue();
        double rmin = dimensions.getAttribute("rmin").getDoubleValue();
        double rmax = dimensions.getAttribute("rmax").getDoubleValue();
                
        int numsides = dimensions.getAttribute("numsides").getIntValue();
        
        // Geant4 interprets rmax as the distance to the polygonal flat.
        // we want to interpret this as the distance to the point.
        rmax = rmax*Math.cos(Math.PI/numsides);

        LayerStack layers = Layering.makeLayering(this.node).getLayerStack();
        
        rmax = rmin + layers.getTotalThickness();
        
        // Total thickness of the subdetector.
        double subdetectorThickness = org.lcsim.geometry.layer.LayerFromCompactCnv.computeDetectorTotalThickness(node);
        
        // Increase the thickness of the endcap to accomodate a gap between layers.
        subdetectorThickness += layers.getNumberOfLayers() * INTER_LAYER_GAP;
                
        // Compute the radial thickness from the user parameters. 
        double radialThickness = rmax - rmin;        

        /*
        // The detector envelope volume.
         * 
        double detZ = subdetector_thickness;
        PolyhedraRegular polyhedra = new PolyhedraRegular(
                detName + "_polyhedra",
                numsides, rmin, rmax, detZ);
        solids.addSolid(polyhedra);

        Volume envelopeVolume = new Volume(detName + "_envelope");
        envelopeVolume.setSolid(polyhedra);
        envelopeVolume.setMaterial(air);
        */

        // The stave's trapezoid.
        double innerAngle = Math.PI * 2 / numsides;
        double halfInnerAngle = innerAngle/2;
        double innerFaceLength = (rmin * tan(halfInnerAngle)) * 2;
        double outerFaceLength = (rmax * tan(halfInnerAngle)) * 2;

        // Apply tolerances to the computed dimensions.
        radialThickness -= STAVE_ENVELOPE_TOLERANCE;
        innerFaceLength -= STAVE_ENVELOPE_TOLERANCE;
        outerFaceLength -= STAVE_ENVELOPE_TOLERANCE;
                       
        //outerFaceLength = (innerFaceLength * outerFaceLength) / (innerFaceLength + STAVE_ENVELOPE_TOLERANCE);
        //System.out.println("outerFaceLength="+outerFaceLength);
        
        Trapezoid sectTrd = new Trapezoid(detName + "_stave_trapezoid");
        sectTrd.setY2(subdetectorThickness/2);
        sectTrd.setY1(subdetectorThickness/2);
        sectTrd.setZ(radialThickness/2);
        sectTrd.setX1(innerFaceLength/2);
        sectTrd.setX2(outerFaceLength/2);
        
        solids.addSolid(sectTrd);
        Volume sectVolume = new Volume(detName + "_stave");
        sectVolume.setMaterial(air);
        sectVolume.setSolid(sectTrd);

        // Build the layers.
        int layer_number = 0;
        double layerPositionY = subdetectorThickness / 2;
        
        // DEBUG
        layerPositionY -= INTER_LAYER_GAP;
        for (Iterator i = node.getChildren("layer").iterator(); i.hasNext();)
        {
            Element layerElement = (Element) i.next();

            int repeat = (int)layerElement.getAttribute("repeat").getDoubleValue();

            for ( int j=0; j<repeat; j++)
            {
            	String layerName = detName + "_stave_layer" + layer_number;;

            	double layerThickness = layers.getLayer(layer_number).getThickness();

            	layerPositionY -= layerThickness / 2;

            	// Layer position.
                Position layerPosition = new Position(layerName + "_position");
                layerPosition.setY(layerPositionY);
                define.addPosition(layerPosition);

                // Layer trapezoid.
                Trapezoid layerTrd = new Trapezoid(layerName + "_trapezoid");
                
                double layerInnerFaceLength=innerFaceLength-LAYER_ENVELOPE_TOLERANCE;
                double layerOuterFaceLength=outerFaceLength-LAYER_ENVELOPE_TOLERANCE;
                double layerRadialThickness=radialThickness-LAYER_ENVELOPE_TOLERANCE;
                
                layerTrd.setX1(layerInnerFaceLength/2);
                layerTrd.setX2(layerOuterFaceLength/2);
                layerTrd.setY1(layerThickness/2);
                layerTrd.setY2(layerThickness/2);
                layerTrd.setZ(layerRadialThickness/2);
                                
                solids.addSolid(layerTrd);

                Volume layerVolume = new Volume(layerName);
                layerVolume.setSolid(layerTrd);
                layerVolume.setMaterial(lcdd.getMaterial("Air"));

                int slice_number = 0;
                double slice_position_y = layerThickness / 2;
                for ( Iterator k = layerElement.getChildren("slice").iterator(); k.hasNext();)
                {
                	Element sliceElement = (Element)k.next();

                	String sliceName = layerName + "_slice" + slice_number;

                	Attribute s = sliceElement.getAttribute("sensitive");
                    boolean sensitive = s != null && s.getBooleanValue();

                    double slice_thickness = sliceElement.getAttribute("thickness").getDoubleValue();
                    
                    // Apply tolerance factor to given slice thickness.
                    //slice_thickness -= SLICE_TOLERANCE;
                    
                    slice_position_y -=  slice_thickness / 2;

                    Position slicePosition = new Position(sliceName + "_position");
                    slicePosition.setY(slice_position_y);
                    define.addPosition(slicePosition);

                    Trapezoid sliceTrd = new Trapezoid(sliceName + "_trapezoid");
                    
                    double sliceInnerFaceLength = layerInnerFaceLength - SLICE_ENVELOPE_TOLERANCE;
                    double sliceOuterFaceLength = layerOuterFaceLength - SLICE_ENVELOPE_TOLERANCE;
                    double sliceRadialThickness = layerRadialThickness - SLICE_ENVELOPE_TOLERANCE;
                    
                    sliceTrd.setX1(sliceInnerFaceLength/2);
                    sliceTrd.setX2(sliceOuterFaceLength/2);
                    sliceTrd.setY1((slice_thickness-SLICE_TOLERANCE)/2);  // Subtract tolerance from slice_thickness.
                    sliceTrd.setY2((slice_thickness-SLICE_TOLERANCE)/2);  // Subtract tolerance from slice_thickness.
                    sliceTrd.setZ(sliceRadialThickness/2);
                    
                    solids.addSolid(sliceTrd);

                    Volume sliceVolume = new Volume(sliceName);
                    sliceVolume.setSolid(sliceTrd);
                    sliceVolume.setMaterial(lcdd.getMaterial(sliceElement.getAttributeValue("material")));
                    
                    if ( sensitive ) sliceVolume.setSensitiveDetector(sens);
                    
                    if (sliceElement.getAttribute("vis") != null)
                    {
                        sliceVolume.setVisAttributes(lcdd.getVisAttributes(sliceElement.getAttributeValue("vis")));
                    }

                    setRegion(lcdd, sliceElement, sliceVolume);
                    setLimitSet(lcdd, sliceElement, sliceVolume);

                    //setVisAttributes(lcdd, node, sliceVolume);
                    structure.addVolume(sliceVolume);

                    PhysVol slicePhysVol = new PhysVol(sliceVolume);
                    slicePhysVol.setPosition(slicePosition);
                    slicePhysVol.addPhysVolID("slice", slice_number);
                    layerVolume.addPhysVol(slicePhysVol);

                    // The slice thickness is the original, NOT adjusted for tolerance,
                    // so that the center of the slice is in the right place with tolerance
                    // gaps on either side.
                    slice_position_y -= slice_thickness / 2;

                    // Increment the slice counter.
                    ++slice_number;
                }
                                
                lcdd.add(layerVolume);

                setRegion(lcdd, layerElement, layerVolume);
                setLimitSet(lcdd, layerElement, layerVolume);
                if (layerElement.getAttribute("vis") != null)
                {
                    layerVolume.setVisAttributes(lcdd.getVisAttributes(layerElement.getAttributeValue("vis")));
                }
                //setVisAttributes(lcdd, node, layerVolume);

                PhysVol layer_physvol = new PhysVol(layerVolume);
                layer_physvol.setPosition(layerPosition);
                layer_physvol.addPhysVolID("layer", layer_number);
                sectVolume.addPhysVol(layer_physvol);

                layerPositionY -= layerThickness / 2;
                
                //layerPositionY -= INTER_LAYER_GAP;

                ++layer_number;                    
            }
            // DEBUG - Uncomment to build only one layer. 
            //break;
        }        

        // Set the vis of the section.
        if (staves != null)
		{
        	if (staves.getAttribute("vis") != null)
        	{
        		setVisAttributes(lcdd,staves,sectVolume);
        	}
		}
        
        // Add the section volume after layers created.
        //if (node.getAttribute("vis") != null)
        //{
        //    sectVolume.setVisAttributes(lcdd.getVisAttributes(node.getAttributeValue("vis")));
        //}
        
        structure.addVolume(sectVolume);

        // Place the sections.
        double innerRotation = innerAngle;
        //double offsetRotation = -innerRotation / 2;
        //double offsetRotation = 0.;

        //System.out.println("radial_thickness: " + radial_thickness);

        double sectCenterRadius = rmin + radialThickness / 2;
        //System.out.println("sectCenterRadius: " + sectCenterRadius);
        //double rotY = -offsetRotation;
        double rotY = 0.;
        //double rotY = 0.;
        double rotX = PI / 2;
        //double rotX = PI / 4;
        double posX = -sectCenterRadius * sin(rotY);
        double sectPosY = sectCenterRadius * cos(rotY);
        //System.out.println("posX: " + posX);
        //System.out.println("sectPosY: " + sectPosY);
        for ( int i=0; i < numsides; i++)
        {
            int moduleNumber=i;

            //System.out.println("moduleNumber: " + moduleNumber);
            //System.out.println("posX: " + posX);
            //System.out.println("sectPosY: " + sectPosY);
            
            Position position = new Position(detName + "_stave0_module" + moduleNumber + "_position");
            position.setX(posX);
            position.setY(sectPosY);
            position.setZ(zmin + subdetectorThickness/2);
            
            Rotation rotation = new Rotation(detName + "_stave0_module" + moduleNumber + "_rotation");
            rotation.setX(rotX);
            rotation.setY(rotY);

            define.addPosition(position);
            define.addRotation(rotation);

            PhysVol sectPhysVol = new PhysVol(sectVolume);
            sectPhysVol.setPosition(position);
            sectPhysVol.setRotation(rotation);

            //envelopeVolume.addPhysVol(sectPhysVol);
            motherVolume.addPhysVol(sectPhysVol);
            sectPhysVol.addPhysVolID("system",id);
            sectPhysVol.addPhysVolID("barrel",1);
            sectPhysVol.addPhysVolID("stave",0);
            sectPhysVol.addPhysVolID("module",moduleNumber);
            
            // Place the reflected subdetector envelope.
            boolean reflect = true;
            if (node.getAttribute("reflect") != null)
                reflect = node.getAttribute("reflect").getBooleanValue();
            if (reflect)
            {
                Rotation envelopeRotationReflect = new Rotation(detName + "_stave0_module" + moduleNumber + "_reflect_rotation");
                
                //envelopeRotationReflect.setX(Math.PI);
                //envelopeRotationReflect.setY(rotY);
                //envelopeRotationReflect.setZ(zrot);
                
                envelopeRotationReflect.setX(Math.PI / 2);
                envelopeRotationReflect.setY(rotY);
                envelopeRotationReflect.setZ(Math.PI);
                
                //envelopeRotationReflect.setY(Math.PI);
                
                define.addRotation(envelopeRotationReflect);

                Position reflect_position = new Position(detName + "_stave0_module" + moduleNumber + "_reflect_position");
                reflect_position.setX(posX);
                reflect_position.setY(sectPosY);
                reflect_position.setZ(-zmin-subdetectorThickness/2);
                define.addPosition(reflect_position);
                
                PhysVol physvol2 = new PhysVol(sectVolume);
                physvol2.setPosition(reflect_position);
                physvol2.setRotation(envelopeRotationReflect);
                physvol2.addPhysVolID("system",id);
                physvol2.addPhysVolID("barrel",2);
                physvol2.addPhysVolID("stave",0);
                physvol2.addPhysVolID("module",moduleNumber);
                motherVolume.addPhysVol(physvol2);
            } 
            
            rotY -= innerRotation;
            posX = -sectCenterRadius * sin(rotY);
            sectPosY = sectCenterRadius * cos(rotY);

            //System.out.println();
            
            // DEBUG
            //if (i==1)
            //	break;
        }

        /*
        // Place the subdetector envelope.
        PhysVol envelopePhysvol = new PhysVol(envelopeVolume);
        envelopePhysvol.setZ(zmin + subdetector_thickness/2);
        envelopePhysvol.addPhysVolID("system",id);
        envelopePhysvol.setRotation(envelopeRotation);
        envelopePhysvol.addPhysVolID("barrel",1);

        motherVolume.addPhysVol(envelopePhysvol);

        // Place the reflected subdetector envelope.
        boolean reflect = node.getAttribute("reflect").getBooleanValue();
        if (reflect)
        {
        	Rotation envelopeRotationReflect = new Rotation(detName + "_reflect_rotation");
            envelopeRotationReflect.setX(Math.PI);
            envelopeRotationReflect.setZ(zrot);
            define.addRotation(envelopeRotationReflect);

            PhysVol physvol2 = new PhysVol(envelopeVolume);
            physvol2.setZ(-zmin-subdetector_thickness/2);
            physvol2.setRotation(envelopeRotationReflect);
            physvol2.addPhysVolID("system",id);
            physvol2.addPhysVolID("barrel",2);
            motherVolume.addPhysVol(physvol2);
        }

        // Add the envelope volume to LCDD once staves are all created.
        setVisAttributes(lcdd, node, envelopeVolume);
        structure.addVolume(envelopeVolume);
        */
    }

    public boolean isCalorimeter()
    {
        return true;
    }
}
