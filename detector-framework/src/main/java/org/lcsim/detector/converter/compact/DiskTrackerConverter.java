package org.lcsim.detector.converter.compact;

import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.DetectorIdentifierHelper;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.ILogicalVolume;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.IRotation3D;
import org.lcsim.detector.LogicalVolume;
import org.lcsim.detector.PhysicalVolume;
import org.lcsim.detector.RotationPassiveXYZ;
import org.lcsim.detector.Transform3D;
import org.lcsim.detector.Translation3D;
import org.lcsim.detector.identifier.ExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierDictionary;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.detector.identifier.IdentifierContext;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialStore;
import org.lcsim.detector.solids.Tube;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.layer.Layer;
import org.lcsim.geometry.layer.LayerSlice;
import org.lcsim.geometry.layer.Layering;
import org.lcsim.geometry.subdetector.DiskTracker;

/**
 * Convert a DiskTracker to the detailed geometry description.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * 
 */
public class DiskTrackerConverter extends AbstractSubdetectorConverter implements ISubdetectorConverter
{
    public void convert(Subdetector subdet, Detector detector)
    {
        ILogicalVolume mother = null;
        if (subdet.isInsideTrackingVolume())
        {
            mother = detector.getTrackingVolume().getLogicalVolume();
        }
        else
        {
            mother = detector.getWorldVolume().getLogicalVolume();
        }

        DiskTracker tracker = (DiskTracker)subdet;

        // Some DiskTracker subdetectors are used for support material
        // so need to make sure Readout is not null before using it.
        int systemNumber = 0;
        DetectorIdentifierHelper helper = null;
        if (subdet.getReadout() != null)
        {
            systemNumber = tracker.getSystemID();
            helper = (DetectorIdentifierHelper)tracker.getDetectorElement().getIdentifierHelper();
        }

        // Get the Subdetector name.
        String subdetName = tracker.getName();

        // Create the two endcap containers.
        DetectorElement endcapPos = new DetectorElement(subdetName + "_positive", tracker.getDetectorElement());
        DetectorElement endcapNeg = null;
        if (tracker.getReflect())
        {
            endcapNeg = new DetectorElement(subdetName + "_negative", tracker.getDetectorElement());
        }

        // Rotation for reflection.
        IRotation3D reflect = new RotationPassiveXYZ(0, Math.PI, 0);

        // Loop over the layers.
        // int sensorNum = 0;
        Layering layering = tracker.getLayering();
        for (int i = 0; i < layering.getNumberOfLayers(); i++ )
        {
            // Layer parameters.
            Layer layer = layering.getLayer(i);
            double layerInnerRadius = tracker.getInnerR()[i];
            double layerOuterRadius = tracker.getOuterR()[i];
            double layerInnerZ = tracker.getInnerZ()[i];

            // Layer names.
            String layerName = subdetName + "_layer" + i;
            String posLayerName = layerName + "_pos";
            String negLayerName = layerName + "_neg";

            // Create layer solid and LogicalVolume.
            Tube layerTube = new Tube(layerName, layerInnerRadius, layerOuterRadius, layer.getThickness() / 2);
            LogicalVolume layerLV =
                    new LogicalVolume(layerName + "_volume", layerTube, detector.getDetectorElement().getGeometry()
                            .getLogicalVolume().getMaterial());

            // Positive and negative PhysicalVolumes.
            IPhysicalVolume posLayerPV =
                    new PhysicalVolume(
                                       new Transform3D(new Translation3D(0, 0, layerInnerZ + layer.getThickness() / 2)),
                                       posLayerName, layerLV, mother, i);
            IPhysicalVolume negLayerPV = null;
            if (tracker.getReflect())
            {
                negLayerPV =
                        new PhysicalVolume(new Transform3D(new Translation3D(0., 0., -layerInnerZ
                                - layer.getThickness() / 2), reflect), negLayerName, layerLV, mother, i);

            }

            // Create the layer paths.
            String posLayerPath = "/";
            String negLayerPath = "/";
            if (subdet.isInsideTrackingVolume())
            {
                posLayerPath += detector.getTrackingVolume().getName() + "/";
                negLayerPath += detector.getTrackingVolume().getName() + "/";
            }
            posLayerPath += posLayerPV.getName();
            if (negLayerPV != null ) {
            	negLayerPath += negLayerPV.getName();
            }

            // Create the layer DetectorElements.
            IDetectorElement posLayerDE = new DetectorElement(posLayerPV.getName(), endcapPos, posLayerPath);
            IDetectorElement negLayerDE = null;
            if (negLayerPV != null)
            {
                negLayerDE = new DetectorElement(negLayerPV.getName(), endcapNeg, negLayerPath);
            }

            // Loop over the slices.
            double sliceZ = -layer.getThickness() / 2;
            for (int j = 0; j < layer.getNumberOfSlices(); j++ )
            {
                // Get slice parameters.
                LayerSlice slice = layer.getSlice(j);
                double sliceThickness = slice.getThickness();
                IMaterial sliceMaterial = MaterialStore.getInstance().get(slice.getMaterial().getName());
                String sliceName = layerName + "_slice" + j;

                // Create the solid and LogicalVolume.
                Tube sliceTube = new Tube(sliceName + "_tube", layerInnerRadius, layerOuterRadius, sliceThickness / 2);
                LogicalVolume sliceLV = new LogicalVolume(sliceName + "_volume", sliceTube, sliceMaterial);

                // Increment to correct slice Z for this placement.
                sliceZ += sliceThickness / 2;

                // Create the positive slice PhysicalVolume. Only one placement is necessary because
                // the negative and positive layers use the same LogicalVolume.
                PhysicalVolume slicePV =
                        new PhysicalVolume(new Transform3D(new Translation3D(0, 0, sliceZ)), sliceName, sliceLV,
                                           layerLV, j);

                // Increment slice Z for next placement.
                sliceZ += sliceThickness / 2;

                // Create Identifiers if this is a sensor.
                IIdentifier slicePosId = null;
                IIdentifier sliceNegId = null;
                if (slice.isSensitive())
                {
                    slicePV.setSensitive(true);
                    slicePosId = makeIdentifier(helper, systemNumber, helper.getEndcapPositiveValue(), i, j);
                    sliceNegId = makeIdentifier(helper, systemNumber, helper.getEndcapNegativeValue(), i, j);

                    // Create the DetectorElement paths for the slices.
                    String posPath = posLayerPath + "/" + sliceName;
                    String negPath = negLayerPath + "/" + sliceName;

                    // Make DetectorElements.
                    new DetectorElement(sliceName + "_pos", posLayerDE, posPath, slicePosId);
                    if (tracker.getReflect())
                    {
                        new DetectorElement(sliceName + "_neg", negLayerDE, negPath, sliceNegId);
                    }
                }

                // Increment sensor number if slice was sensitive.
                // if (slice.isSensitive())
                // {
                // ++sensorNum;
                // }
            }
        }
    }

    static IIdentifier makeIdentifier(IIdentifierHelper helper, int systemNumber, int barrel, int layer, int slice)
    {
        ExpandedIdentifier id = new ExpandedIdentifier(helper.getIdentifierDictionary().getNumberOfFields());
        try
        {
            id.setValue(helper.getFieldIndex("system"), systemNumber);
            id.setValue(helper.getFieldIndex("barrel"), barrel);
            id.setValue(helper.getFieldIndex("layer"), layer);
            if (helper.hasField("slice"))
                id.setValue(helper.getFieldIndex("slice"), slice);
            return helper.pack(id);
        }
        catch (Exception x)
        {
            throw new RuntimeException(x);
        }
    }

    static IIdentifier makeLayerIdentifier(IIdentifierHelper helper, int systemNumber, int barrel, int layer)
    {
        ExpandedIdentifier id = new ExpandedIdentifier(helper.getIdentifierDictionary().getNumberOfFields());
        try
        {
            id.setValue(helper.getFieldIndex("system"), systemNumber);
            id.setValue(helper.getFieldIndex("barrel"), barrel);
            id.setValue(helper.getFieldIndex("layer"), layer);
            return helper.pack(id);
        }
        catch (Exception x)
        {
            throw new RuntimeException(x);
        }
    }

    /*
    public void makeIdentifierContext(Subdetector subdet)
    {
        IIdentifierDictionary iddict = subdet.getDetectorElement().getIdentifierHelper().getIdentifierDictionary();

        int systemIndex = iddict.getFieldIndex("system");
        int barrelIndex = iddict.getFieldIndex("barrel");
        int layerIndex = iddict.getFieldIndex("layer");

        IdentifierContext systemContext = new IdentifierContext(new int[]{systemIndex});
        IdentifierContext subdetContext = new IdentifierContext(new int[]{systemIndex,barrelIndex});
        IdentifierContext layerContext = new IdentifierContext(new int[]{systemIndex,barrelIndex,layerIndex});

        iddict.addIdentifierContext("system", systemContext);
        iddict.addIdentifierContext("subdetector", subdetContext);
        iddict.addIdentifierContext("layer", layerContext);
    }
    */

    public Class getSubdetectorType()
    {
        return DiskTracker.class;
    }
}
