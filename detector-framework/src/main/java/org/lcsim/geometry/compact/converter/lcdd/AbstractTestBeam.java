package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.Box;
import org.lcsim.geometry.compact.converter.lcdd.util.Define;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.Material;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.Position;
import org.lcsim.geometry.compact.converter.lcdd.util.Rotation;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Solids;
import org.lcsim.geometry.compact.converter.lcdd.util.Structure;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;
import org.lcsim.geometry.layer.LayerFromCompactCnv;

/**
 * 
 * @author Jeremy McCormick
 */
abstract class AbstractTestBeam extends LCDDSubdetector {

    AbstractTestBeam(Element node) throws JDOMException {
        super(node);
    }

    public void addToLCDD(LCDD lcdd, SensitiveDetector sens) throws JDOMException {
        String detectorName = node.getAttributeValue("name");
        int id = node.getAttribute("id").getIntValue();

        Material air = lcdd.getMaterial("Air");
        Solids solids = lcdd.getSolids();
        Structure structure = lcdd.getStructure();
        Volume motherVolume = lcdd.pickMotherVolume(this);
        Define define = lcdd.getDefine();

        Element dimensions = node.getChild("dimensions");
        double xdim = dimensions.getAttribute("x").getDoubleValue();
        double ydim = dimensions.getAttribute("y").getDoubleValue();
        double zdim = LayerFromCompactCnv.computeDetectorTotalThickness(node);

        Box envelopeBox = new Box(detectorName + "_box");
        envelopeBox.setX(xdim);
        envelopeBox.setY(ydim);
        envelopeBox.setZ(zdim);
        solids.addSolid(envelopeBox);
        Volume envelopeVolume = new Volume(detectorName + "_envelope");
        envelopeVolume.setSolid(envelopeBox);
        envelopeVolume.setMaterial(air);

        /* Create the position for the envelope volume. */
        Position envelopePosition = createPosition(detectorName);
        define.addPosition(envelopePosition);

        /* Create the rotation for the envelope volume. */
        Rotation envelopeRotation = createRotation(detectorName);
        if (envelopeRotation != null) {
            define.addRotation(envelopeRotation);
        }

        double layerZPos = -zdim / 2;

        int layerCount = 0;
        int layerTypeCount = 0;
        for (Object lo : node.getChildren("layer")) {
            Element layer = (Element) lo;

            double layerX = xdim;
            Attribute xattrib = layer.getAttribute("x");
            if (xattrib != null) {
                layerX = xattrib.getDoubleValue();
            }

            double layerY = ydim;
            Attribute yattrib = layer.getAttribute("y");
            if (yattrib != null) {
                layerY = yattrib.getDoubleValue();
            }

            double layerZ = LayerFromCompactCnv.computeSingleLayerThickness(layer);

            int repeat = 1;
            Attribute repeatAttrib = layer.getAttribute("repeat");
            if (repeatAttrib != null) {
                repeat = repeatAttrib.getIntValue();
            }

            String layerVolumeName = detectorName + "_layerType" + layerTypeCount;
            Box layerBox = new Box(layerVolumeName + "_box");
            layerBox.setX(layerX);
            layerBox.setY(layerY);
            layerBox.setZ(layerZ);
            solids.addSolid(layerBox);

            Volume layerVolume = new Volume(layerVolumeName);
            layerVolume.setMaterial(air);
            layerVolume.setSolid(layerBox);

            int sliceCount = 0;
            double slicePosZ = -layerZ / 2;

            for (Object so : layer.getChildren("slice")) {
                Element slice = (Element) so;
                double sliceX = layerX;
                double sliceY = layerY;
                xattrib = slice.getAttribute("x");
                if (xattrib != null) {
                    sliceX = xattrib.getDoubleValue();
                }

                yattrib = slice.getAttribute("y");
                if (yattrib != null) {
                    sliceY = yattrib.getDoubleValue();
                }
                double sliceZ = slice.getAttribute("thickness").getDoubleValue();

                Attribute s = slice.getAttribute("sensitive");
                boolean sensitive = s != null && s.getBooleanValue();

                String sliceName = layerVolumeName + "_slice" + sliceCount;

                Box sliceBox = new Box(sliceName + "_box");
                sliceBox.setX(sliceX);
                sliceBox.setY(sliceY);
                sliceBox.setZ(sliceZ);
                solids.addSolid(sliceBox);

                Volume sliceVolume = new Volume(sliceName);
                sliceVolume.setSolid(sliceBox);
                Material sliceMaterial = lcdd.getMaterial(slice.getAttributeValue("material"));
                sliceVolume.setMaterial(sliceMaterial);
                if (sensitive)
                    sliceVolume.setSensitiveDetector(sens);

                setRegion(lcdd, slice, sliceVolume);
                setLimitSet(lcdd, slice, sliceVolume);
                setVisAttributes(lcdd, slice, sliceVolume);

                structure.addVolume(sliceVolume);

                PhysVol slicePhysVol = new PhysVol(sliceVolume);
                slicePhysVol.addPhysVolID("slice", sliceCount);
                Position slicePosition = new Position(sliceName + "_position");
                slicePosZ += sliceZ / 2;

                slicePosition.setZ(slicePosZ);
                slicePosZ += sliceZ / 2;
                define.addPosition(slicePosition);
                slicePhysVol.setPosition(slicePosition);
                layerVolume.addPhysVol(slicePhysVol);

                ++sliceCount;
            }

            setVisAttributes(lcdd, layer, layerVolume);
            structure.addVolume(layerVolume);

            for (int i = 0; i < repeat; i++) {
                String layerPhysVolName = detectorName + "_layer" + layerCount;

                PhysVol layerPhysVol = new PhysVol(layerVolume);
                layerPhysVol.addPhysVolID("layer", layerCount);

                layerZPos += layerZ / 2;
                Position layerPosition = new Position(layerPhysVolName + "_position");
                define.addPosition(layerPosition);
                layerPosition.setZ(layerZPos);
                layerPhysVol.setPosition(layerPosition);
                layerZPos += layerZ / 2;

                envelopeVolume.addPhysVol(layerPhysVol);

                ++layerCount;
            }
            ++layerTypeCount;
        }
        setVisAttributes(lcdd, node, envelopeVolume);
        structure.addVolume(envelopeVolume);
        PhysVol envelopePhysVol = new PhysVol(envelopeVolume);
        envelopePhysVol.addPhysVolID("system", id);
        envelopePhysVol.setPosition(envelopePosition);
        if (envelopeRotation != null)
            envelopePhysVol.setRotation(envelopeRotation);
        motherVolume.addPhysVol(envelopePhysVol);
    }

    private Position createPosition(String detectorName) throws DataConversionException {
        double xpos = 0;
        double ypos = 0;
        double zpos = 0;

        Element positionElement = node.getChild("position");

        if (positionElement != null) {
            Attribute posAttribute = positionElement.getAttribute("x");
            if (posAttribute != null) {
                xpos = posAttribute.getDoubleValue();
            }

            posAttribute = positionElement.getAttribute("y");
            if (posAttribute != null) {
                ypos = posAttribute.getDoubleValue();
            }

            posAttribute = positionElement.getAttribute("z");
            if (posAttribute != null) {
                zpos = posAttribute.getDoubleValue();
            }
        }
        Position envelopePosition = new Position(detectorName + "_position");
        envelopePosition.setX(xpos);
        envelopePosition.setY(ypos);
        envelopePosition.setZ(zpos);
        return envelopePosition;
    }

    private Rotation createRotation(String detectorName) throws DataConversionException {
        double rx, ry, rz;
        rx = ry = rz = 0;
        Element rotationElement = node.getChild("rotation");
        Rotation envelopeRotation = null;
        if (rotationElement != null) {
            Attribute rotationAttribute = rotationElement.getAttribute("x");
            if (rotationAttribute != null) {
                rx = rotationAttribute.getDoubleValue();
            }
            rotationAttribute = rotationElement.getAttribute("y");
            if (rotationAttribute != null) {
                ry = rotationAttribute.getDoubleValue();
            }
            rotationAttribute = rotationElement.getAttribute("z");
            if (rotationAttribute != null) {
                rz = rotationAttribute.getDoubleValue();
            }
            envelopeRotation = new Rotation(detectorName + "_rotation");
            envelopeRotation.setX(rx);
            envelopeRotation.setY(ry);
            envelopeRotation.setZ(rz);
        }
        return envelopeRotation;
    }
}
