package org.lcsim.detector.converter.compact;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.DetectorIdentifierHelper.SystemMap;
import org.lcsim.detector.identifier.IIdentifierHelper;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.geometry.compact.Subdetector;

/**
 * Converts a {@link org.lcsim.geometry.Subdetector} into the detailed geometry
 * description defined by the {@link org.lcsim.detector} package.
 */
public interface ISubdetectorConverter
{
    /**
     * This is the primary conversion method for building the detailed geometry and the
     * hierarchy of DetectorElement nodes.
     * 
     * @param subdet
     * @param detector
     */
    public void convert( Subdetector subdet, Detector detector );

    /**
     * Get the type of Subdetector handled by this converter.
     * 
     * @return The type of Subdetector handled by this converter.
     */
    public Class getSubdetectorType();

    /**
     * Create the top-level {@link org.lcsim.detector.IDetectorElement} for this
     * Subdetector. This node will be assigned the
     * {@link org.lcsim.detector.identifier.IIdentifierHelper} created by
     * {@link #makeIdentifierHelper(Subdetector, SystemMap)}.
     * 
     * @param detector
     * @param subdetector
     * @return
     */
    public IDetectorElement makeSubdetectorDetectorElement( Detector detector, Subdetector subdetector );

    /**
     * This method is called after {@link #convert(Subdetector, Detector)} to define the
     * appropriate IdentifierContext objects in the IdentifierDictionary.
     * 
     * @param subdet The Subdetector.
     */
    //public void makeIdentifierContext( Subdetector subdet );

    /**
     * Create the appropriate type of
     * {@link org.lcsim.detector.identifier.IIdentifierHelper} for this subdetector type.
     * 
     * @param subdetector
     * @param systemMap
     * @return
     */
    public IIdentifierHelper makeIdentifierHelper( Subdetector subdetector, SystemMap systemMap );

    /**
     * Creates the hierarchy of Identifiers in this Subdetector.
     */
    public void makeIdentifiers( Subdetector subdetector );
}