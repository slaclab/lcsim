package org.lcsim.detector.converter.compact;

import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.LogicalVolume;
import org.lcsim.detector.PhysicalVolume;
import org.lcsim.detector.identifier.ExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierDictionary;
import org.lcsim.detector.identifier.IdentifierDictionaryManager;
import org.lcsim.detector.identifier.IdentifierUtil;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialStore;
import org.lcsim.detector.solids.Tube;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.layer.Layer;
import org.lcsim.geometry.layer.LayerSlice;
import org.lcsim.geometry.layer.Layering;
import org.lcsim.geometry.subdetector.MultiLayerTracker;

/**
 * Convert a MultiLayerTracker into the org.lcsim.detector geometry representation.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class MultiLayerTrackerConverter extends AbstractSubdetectorConverter implements ISubdetectorConverter
{
    public void convert(Subdetector subdet, Detector detector)
    {
        IPhysicalVolume mother = null;
        if (subdet.isInsideTrackingVolume())
        {
            mother = detector.getTrackingVolume();
        }
        else
        {
            mother = detector.getWorldVolume();
        }

        MultiLayerTracker tracker = (MultiLayerTracker)subdet;
        Layering layering = tracker.getLayering();
        String subdetName = tracker.getName();

        // int layerNumber = 0;
        for (int i = 0; i < tracker.getLayering().getNumberOfLayers(); i++ )
        {
            String layerName = subdetName + "_layer" + i;
            Layer layer = layering.getLayer(i);
            double layerInnerR = tracker.getInnerR()[i];
            double layerOuterZ = tracker.getOuterZ()[i];
            double layerThickness = layer.getThickness();

            Tube layerTube = new Tube(layerName + "_tube", layerInnerR, layerInnerR + layerThickness, layerOuterZ);
            IMaterial layerMaterial = detector.getDetectorElement().getGeometry().getLogicalVolume().getMaterial();
            LogicalVolume layerLV = new LogicalVolume(layerName, layerTube, layerMaterial);
            new PhysicalVolume(null, layerName, layerLV, mother.getLogicalVolume(), i);
            double sliceInnerR = layerInnerR;

            // Make layer name and path to geometry.
            String layerPath = "";
            if (subdet.isInsideTrackingVolume())
            {
                layerPath += "/" + mother.getName();
            }
            layerPath += "/" + layerName;

            //System.out.println("MultiLayerTracker: " + layerPath);

            // Make the layer DetectorElement. (no id)
            IDetectorElement layerDe = new DetectorElement(layerName, tracker.getDetectorElement(), layerPath);

            for (int j = 0; j < layer.getNumberOfSlices(); j++ )
            {
                LayerSlice slice = layer.getSlice(j);
                double sliceThickness = slice.getThickness();
                String sliceName = subdetName + "layer" + i + "_slice" + j;

                Tube sliceTube = new Tube(sliceName + "_tube", sliceInnerR, sliceInnerR + sliceThickness, layerOuterZ);
                IMaterial sliceMaterial = MaterialStore.getInstance().get(slice.getMaterial().getName());
                LogicalVolume sliceLV = new LogicalVolume(sliceName, sliceTube, sliceMaterial);
                PhysicalVolume slicePV = new PhysicalVolume(null, sliceName, sliceLV, layerLV, j);

                // Create ID for sensors.
                IIdentifier id = null;
                if (slice.isSensitive())
                {
                    slicePV.setSensitive(true);

                    ExpandedIdentifier expid =
                            makeExpandedIdentifier(subdet.getIDDecoder(), tracker.getIDDecoder().getSystemNumber(), i);
                    IIdentifierDictionary iddict =
                            IdentifierDictionaryManager.getInstance().getIdentifierDictionary(
                                    subdet.getReadout().getName());
                    id = iddict.pack(expid);
                }

                String slicePath = layerPath + "/" + slicePV.getName();
                new DetectorElement(sliceName, layerDe, slicePath, id);

                sliceInnerR += sliceThickness;
            }
        }
    }

    private static ExpandedIdentifier makeExpandedIdentifier(IDDecoder decoder, int systemNumber, int layer)
    {
        ExpandedIdentifier id = new ExpandedIdentifier();
        for (int i = 0; i < decoder.getFieldCount(); i++ )
        {
            String fieldName = decoder.getFieldName(i);
            if (fieldName.equals("system"))
            {
                id.addValue(systemNumber);
            }
            else if (fieldName.equals("layer"))
            {
                id.addValue(layer);
            }
            else if (fieldName.equals("barrel"))
            {
                id.addValue(0);
            }
            else
            {
                id.addValue(0);
            }
        }
        return id;
    }

    public void makeIdentifierContext(Subdetector subdet)
    {
    /*
     * IIdentifierDictionary iddict = subdet.getDetectorElement().getIdentifierHelper().getIdentifierDictionary();
     * 
     * int systemIndex = iddict.getFieldIndex("system"); int barrelIndex = iddict.getFieldIndex("barrel"); int
     * layerIndex = iddict.getFieldIndex("layer");
     * 
     * IdentifierContext systemContext = new IdentifierContext(new int[] {systemIndex}); IdentifierContext subdetContext
     * = new IdentifierContext(new int[] {systemIndex,barrelIndex}); IdentifierContext layerContext = new
     * IdentifierContext(new int[] {systemIndex,barrelIndex,layerIndex});
     * 
     * iddict.addIdentifierContext("system", systemContext); iddict.addIdentifierContext("subdetector", subdetContext);
     * iddict.addIdentifierContext("layer", layerContext);
     */
    }

    public Class getSubdetectorType()
    {
        return MultiLayerTracker.class;
    }

    /*
     * public class MultiLayerTrackerSensorLayer extends DetectorElement { MultiLayerTrackerSensorLayer(String name,
     * IDetectorElement parent, String path, IIdentifier id) { super(name,parent,path,id); } }
     */
}
