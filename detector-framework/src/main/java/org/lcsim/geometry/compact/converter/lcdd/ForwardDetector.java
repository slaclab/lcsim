/*
 * ForwardDetector.java
 *
 * Created on June 16, 2005, 1:54 PM
 */

package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.JDOMException;
import org.jdom.Element;
import org.jdom.Attribute;
import org.lcsim.geometry.compact.converter.lcdd.util.Define;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.Material;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Solids;
import org.lcsim.geometry.compact.converter.lcdd.util.Structure;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;
import org.lcsim.geometry.compact.converter.lcdd.util.SubtractionSolid;
import org.lcsim.geometry.compact.converter.lcdd.util.Tube;
import org.lcsim.geometry.compact.converter.lcdd.util.Position;
import org.lcsim.geometry.compact.converter.lcdd.util.Rotation;
import org.lcsim.geometry.layer.LayerFromCompactCnv;

/**
 *
 * @author jeremym
 */
public class ForwardDetector extends LCDDSubdetector
{
    
    /** Creates a new instance of ForwardDetector */
    public ForwardDetector(Element node) throws JDOMException
    {
        super(node);
    }
    
    /** FIXME: This method is horrible spaghetti code.  --JM */
    public void addToLCDD(LCDD lcdd, SensitiveDetector sens) throws JDOMException
    {
        int id = -1;
        if (node.getAttribute("id") != null)
            id = node.getAttribute("id").getIntValue();
        String detectorName = node.getAttributeValue("name");        
        
        Material air = lcdd.getMaterial("Air");
        Solids solids = lcdd.getSolids();
        Structure structure = lcdd.getStructure();
        Volume motherVolume = lcdd.pickMotherVolume(this);
        
        Define defines = lcdd.getDefine();
        Rotation reflection = lcdd.getDefine().getRotation("reflect_rot");
        
        boolean reflect = node.getAttribute("reflect").getBooleanValue();
        
        /** Dimension parameters. */
        Element dimensions = node.getChild("dimensions");
        double zinner = dimensions.getAttribute("inner_z").getDoubleValue();
        double rmax = dimensions.getAttribute("outer_r").getDoubleValue();
        double rmin = dimensions.getAttribute("inner_r").getDoubleValue();
        
        /** Beampipe parameters. */
        Element beam = node.getChild("beampipe");
        double outgoingR = beam.getAttribute("outgoing_r").getDoubleValue();
        double incomingR = beam.getAttribute("incoming_r").getDoubleValue();
        double xangle = beam.getAttribute("crossing_angle").getDoubleValue();
        double xangleHalf = xangle / 2;
        
        /** Computed z values. */
        double thickness = layers.getLayerStack().getTotalThickness();
        double zpos = zinner + (thickness / 2);
        double zouter = zinner + thickness;
        
        /** Beampipe position in envelope. */
        double beamPosX = Math.tan(xangleHalf) * zpos;
        //System.out.println("xangleHalf: " + xangleHalf);
        //System.out.println("thickness: " + thickness);
        //System.out.println("zpos: " + zpos);
        //System.out.println("beamPosX: " + beamPosX);
        
        /** Detector envelope solid. */
        Tube envelopeTube = new Tube(detectorName + "_tube");
        envelopeTube.setRMax(rmax);
        envelopeTube.setRMin(rmin);
        envelopeTube.setZ(thickness);
        solids.addSolid(envelopeTube);
        
        /** Incoming beampipe solid. */
        Tube beamInTube = new Tube(detectorName + "_beampipe_incoming_tube");
        beamInTube.setZ(thickness * 2);
        beamInTube.setRMin(0);
        beamInTube.setRMax(outgoingR);
        solids.addSolid(beamInTube);
        
        /** Outgoing beampipe solid. */
        Tube beamOutTube = new Tube(detectorName + "_beampipe_outgoing_tube");
        beamOutTube.setZ(thickness * 2);
        beamOutTube.setRMin(0);
        beamOutTube.setRMax(incomingR);
        solids.addSolid(beamOutTube);
        
        /** /\/\/\/\ First envelope boolean subtraction of incoming beampipe. /\/\/\/\ */
        SubtractionSolid envelopeSubtraction1 =
                new SubtractionSolid(detectorName + "_subtraction1_tube");
        envelopeSubtraction1.setFirstSolid(envelopeTube);
        envelopeSubtraction1.setSecondSolid(beamInTube);
        solids.addSolid(envelopeSubtraction1);
        
        /** Position of incoming beampipe. */
        Position beamInPos = new Position(detectorName + "_subtraction1_tube_pos");
        defines.addPosition(beamInPos);
        beamInPos.setX(beamPosX);
        envelopeSubtraction1.setPosition(beamInPos);
        
        /** Rotation of incoming beampipe. */
        Rotation beamInRot = new Rotation(detectorName + "_subtraction1_tube_rot");
        defines.addRotation(beamInRot);
        beamInRot.setY(xangleHalf);
        envelopeSubtraction1.setRotation(beamInRot);
        
        /** /\/\/\/\ Second envelope boolean subtracion of outgoing beampipe. /\/\/\/\ */
        SubtractionSolid envelopeSubtraction2 =
                new SubtractionSolid(detectorName + "_subtraction2_tube");
        envelopeSubtraction2.setFirstSolid(envelopeSubtraction1);
        envelopeSubtraction2.setSecondSolid(beamOutTube);
        solids.addSolid(envelopeSubtraction2);
        
        /** Position of outgoing beampipe. */
        Position beamOutPos = new Position(detectorName + "_subtraction2_tube_pos");
        defines.addPosition(beamOutPos);
        beamOutPos.setX(-beamPosX);
        envelopeSubtraction2.setPosition(beamOutPos);
        
        /** Rotation of outgoing beampipe. */
        Rotation beamOutRot = new Rotation(detectorName + "_subtraction2_tube_rot");
        defines.addRotation(beamOutRot);
        beamOutRot.setY(-xangleHalf);
        envelopeSubtraction2.setRotation(beamOutRot);
        
        /** Final envelope boolean volume. */
        Volume envelopeVolume = new Volume(detectorName + "_envelope_volume");
        envelopeVolume.setSolid(envelopeSubtraction2);
        envelopeVolume.setMaterial(air);
        
        /** Process each layer element. */
        double layerPosZ = -thickness / 2;
        double layerDisplZ = 0;
        for (Object o : node.getChildren("layer") )
        {
            Element layerElem = (Element) o;
            
            int repeat = layerElem.getAttribute("repeat").getIntValue();
            
            double layerThickness = LayerFromCompactCnv.computeSingleLayerThickness(layerElem);
            
            /**
             * Create tube envelope for this layer, which can be reused in boolean definition
             * in the repeat loop below.
             */
            Tube layerTube = new Tube(detectorName + "_layer_tube");
            layerTube.setRMin(rmin);
            layerTube.setRMax(rmax);
            layerTube.setZ(layerThickness);
            solids.addSolid(layerTube);
            
            /** Layer x repeat loop. */
            for (int i=0; i < repeat; i++)
            {
                //System.out.println("proc layer repeat: " + i);
                
                String layerBasename = detectorName + "_layer" + i;
                
                /** Increment to new layer position. */
                layerDisplZ += layerThickness / 2;
                layerPosZ += layerThickness / 2;
                
                //System.out.println("layerPosZ: " + layerPosZ);
                
                /** First layer subtraction solid. */
                SubtractionSolid layerSubtraction1 =
                        new SubtractionSolid(layerBasename + "_subtraction1");
                layerSubtraction1.setFirstSolid(layerTube);
                layerSubtraction1.setSecondSolid(beamInTube);
                solids.addSolid(layerSubtraction1);
                
                Position layerSubtraction1Pos = new Position(layerBasename + "_subtraction1_pos");
                
                double layerGlobalZ = zinner + layerDisplZ;
//                System.out.println("layerGlobalZ: " + layerGlobalZ);
                double layerPosX = Math.tan(xangleHalf) * layerGlobalZ;
//                System.out.println("layerPosX: " + layerPosX);
                layerSubtraction1Pos.setX(layerPosX);
                defines.addPosition(layerSubtraction1Pos);
                
                layerSubtraction1.setPosition(layerSubtraction1Pos);
                layerSubtraction1.setRotation(beamInRot);
                
                /** Second layer subtraction solid. */
                SubtractionSolid layerSubtraction2 =
                        new SubtractionSolid(layerBasename + "_subtraction2");
                layerSubtraction2.setFirstSolid(layerSubtraction1);
                layerSubtraction2.setSecondSolid(beamOutTube);
                solids.addSolid(layerSubtraction2);
                
                Position layerSubtraction2Pos = new Position(layerBasename + "_subtraction2_pos");
                layerSubtraction2Pos.setX(-layerPosX);
                defines.addPosition(layerSubtraction2Pos);
                
                layerSubtraction2.setPosition(layerSubtraction2Pos);
                layerSubtraction2.setRotation(beamOutRot);
                
                /** Layer LV. */
                Volume layerVolume = new Volume(layerBasename + "_volume");
                layerVolume.setMaterial(air);
                layerVolume.setSolid(layerSubtraction2);
                
                /** Slice loop. */
                double slicePosZ = -layerThickness / 2;
                double sliceDisplZ = 0;
                int sliceCount = 0;
                for ( Object oo : layerElem.getChildren("slice"))
                {
                    Element sliceElem = (Element) oo;
                    
                    String sliceBasename = layerBasename + "_slice" + sliceCount;
                    
                    /** Get slice parameters. */
                    double sliceThickness = sliceElem.getAttribute("thickness").getDoubleValue();
                    Attribute s = sliceElem.getAttribute("sensitive");
                    boolean sensitive = ( s != null && s.getBooleanValue() );
                    String materialName = sliceElem.getAttributeValue("material");
                    
                    /** Go to mid of this slice. */
                    sliceDisplZ += sliceThickness / 2;
                    slicePosZ += sliceThickness / 2;
//                    System.out.println("sliceDisplZ: " + sliceDisplZ );
//                    System.out.println("slicePosZ: " + slicePosZ );
                    
                    /** Slice's basic tube. */
                    Tube sliceTube = new Tube(sliceBasename + "_tube");
                    sliceTube.setRMin(rmin);
                    sliceTube.setRMax(rmax);
                    sliceTube.setZ(sliceThickness);
                    solids.addSolid(sliceTube);
                    
                    /** First slice subtraction solid. */
                    SubtractionSolid sliceSubtraction1 =
                            new SubtractionSolid(sliceBasename + "_subtraction1");
                    sliceSubtraction1.setFirstSolid(sliceTube);
                    sliceSubtraction1.setSecondSolid(beamInTube);
                    solids.addSolid(sliceSubtraction1);
                    
                    Position sliceSubtraction1Pos = new Position(sliceBasename + "_subtraction1_pos");
                    double sliceGlobalZ = zinner + (layerDisplZ - layerThickness / 2) + sliceDisplZ;
                    
//                    System.out.println("sliceGlobalZ: " + sliceGlobalZ);
                    
                    double slicePosX = Math.tan(xangleHalf) * sliceGlobalZ;
                    
//                    System.out.println("slicePosX: " + slicePosX);
                    
                    sliceSubtraction1Pos.setX(slicePosX);
                    defines.addPosition(sliceSubtraction1Pos);
                    
                    sliceSubtraction1.setPosition(sliceSubtraction1Pos);
                    sliceSubtraction1.setRotation(beamInRot);
                    
                    /** Second slice subtraction solid. */
                    SubtractionSolid sliceSubtraction2 =
                            new SubtractionSolid(sliceBasename + "_subtraction2");
                    sliceSubtraction2.setFirstSolid(sliceSubtraction1);
                    sliceSubtraction2.setSecondSolid(beamOutTube);
                    solids.addSolid(sliceSubtraction2);
                    
                    Position sliceSubtraction2Pos = new Position(sliceBasename + "_subtraction2_pos");
                    sliceSubtraction2Pos.setX(-slicePosX);
                    defines.addPosition(sliceSubtraction2Pos);
                    
                    sliceSubtraction2.setPosition(sliceSubtraction2Pos);
                    sliceSubtraction2.setRotation(beamOutRot);
                    
                    /** Slice LV. */
                    Volume sliceVolume = new Volume(sliceBasename + "_volume");
                    sliceVolume.setMaterial(lcdd.getMaterial(sliceElem.getAttributeValue("material")));
                    sliceVolume.setSolid(sliceSubtraction2);
                    
                    if ( sensitive )
                    {
                        sliceVolume.setSensitiveDetector(sens);
                    }
                    
                    /* FIXME: these need to be called automatically whenever a new volume is created --JM */
                    setRegion(lcdd, sliceElem, sliceVolume);
                    setLimitSet(lcdd, sliceElem, sliceVolume);                    
                    
                    setVisAttributes(lcdd, node, sliceVolume);
                    
                    structure.addVolume(sliceVolume);
                    
                    /** Slice PV. */
                    PhysVol slicePV = new PhysVol(sliceVolume);
                    slicePV.setZ(slicePosZ);
                    layerVolume.addPhysVol(slicePV);
                    
                    /** Start of next slice. */
                    sliceDisplZ += sliceThickness / 2;
                    slicePosZ += sliceThickness / 2;
                    ++sliceCount;
                }
                
                setVisAttributes(lcdd, node, layerVolume);
                
                structure.addVolume(layerVolume);
                
                /** Layer PV. */
                PhysVol layerPV = new PhysVol(layerVolume);
                layerPV.setZ(layerPosZ);
                layerPV.addPhysVolID("layer", i);
                envelopeVolume.addPhysVol(layerPV);
                
                /** Increment to start of next layer. */
                layerDisplZ += layerThickness / 2;
                layerPosZ += layerThickness / 2;
            }
        }
        
        setVisAttributes(lcdd, node, envelopeVolume);
        
        /** Add envelope LV. */
        structure.addVolume(envelopeVolume);
                
        /** Add envelope PV. */
        PhysVol envelopePV = new PhysVol(envelopeVolume);
        envelopePV.setZ(zpos);
        envelopePV.addPhysVolID("system", id);
        envelopePV.addPhysVolID("barrel", 1);
        motherVolume.addPhysVol(envelopePV);
        
        /** Reflect it. */
        if ( reflect )
        {
            PhysVol envelopePV2 = new PhysVol(envelopeVolume);
            envelopePV2.setZ(-zpos);
            envelopePV2.setRotation(reflection);
            envelopePV2.addPhysVolID("system", id);
            envelopePV2.addPhysVolID("barrel", 2);
            motherVolume.addPhysVol(envelopePV2);
        }
    }
    
    public boolean isCalorimeter()
    {
        return true;
    }
}
