package org.lcsim.geometry;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.geometry.compact.VisAttributes;
import org.lcsim.geometry.layer.Layering;

/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @author Tony Johnson <tony_johnson@slac.stanford.edu>
 */
public interface Subdetector
{
    public String getName();

    public int getSystemID();

    public IDDecoder getIDDecoder();

    public String getHitsCollectionName();

    public String getDigiHitsCollectionName();

    /**
     * @deprecated Use functionality provided by
     *             {@link org.lcsim.detector.IDetectorElement}.
     * @return The layering.
     */
    @Deprecated
    public Layering getLayering();
       
    public VisAttributes getVisAttributes();

    public boolean isTracker();

    public boolean isCalorimeter();

    public boolean isBarrel();

    public boolean isEndcap();
    
    /**
     * 
     * @param localPos
     * @deprecated Use {@link org.lcsim.detector.IGeometryInfo#getLocalToGlobal()}
     */
    @Deprecated
    public double[] transformLocalToGlobal( double[] localPos );

    public IDetectorElement getDetectorElement();

    public boolean isInsideTrackingVolume();
    
    public boolean getReflect();
}