package org.lcsim.detector.converter.compact;

import org.lcsim.geometry.compact.Detector;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.subdetector.TubeSegment;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.ILogicalVolume;
import org.lcsim.detector.solids.Tube;
import org.lcsim.detector.PhysicalVolume;
import org.lcsim.detector.IPhysicalVolumeNavigator;
import org.lcsim.detector.PhysicalVolumeNavigatorStore;
import org.lcsim.detector.IPhysicalVolumePath;
import org.lcsim.detector.DetectorElement;
import org.lcsim.detector.LogicalVolume;

public class TubeSegmentConverter extends AbstractSubdetectorConverter implements ISubdetectorConverter
{

    public void convert( Subdetector subdet, Detector detector )
    {
        // Cast to subtype.
        TubeSegment tube = ( TubeSegment ) subdet;

        // Get the world volume.
        IPhysicalVolume world = detector.getWorldVolume();

        // Get the world volume fill material.
        IMaterial matWorld = world.getLogicalVolume().getMaterial();

        // Create the Subdetector's envelope LogicalVolume.
        ILogicalVolume envelope = buildEnvelope( tube, matWorld );

        // Create the PhysicalVolume.
        new PhysicalVolume( tube.getTransform(), tube.getName(), envelope, world.getLogicalVolume(), subdet
                .getSystemID() );

        // Setup the geometry.
        IPhysicalVolumeNavigator nav = PhysicalVolumeNavigatorStore.getInstance().getDefaultNavigator();
        IPhysicalVolumePath path = nav.getPath( tube.getName() );

        // Create the Subdetector's DetectorElement.
        if ( tube.getDetectorElement() != null )
        {
            ( ( DetectorElement ) tube.getDetectorElement() ).setSupport( path );
        }
    }

    private ILogicalVolume buildEnvelope( TubeSegment tubeSeg, IMaterial material )
    {
        Tube tube = new Tube( tubeSeg.getName() + "_envelope_tube",
                              tubeSeg.getInnerRadius(),
                              tubeSeg.getOuterRadius(),
                              tubeSeg.getZHalfLength() );

        LogicalVolume logvol = new LogicalVolume( tubeSeg.getName() + "_envelope", tube, material );

        return logvol;
    }

    public Class getSubdetectorType()
    {
        return TubeSegment.class;
    }
}
