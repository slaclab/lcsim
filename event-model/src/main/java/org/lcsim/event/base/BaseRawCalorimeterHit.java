package org.lcsim.event.base;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.identifier.Identifier;
import org.lcsim.event.RawCalorimeterHit;

/**
 * This is a basic implementation of {@link org.lcsim.event.RawCalorimeterHit}.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class BaseRawCalorimeterHit extends BaseHit implements RawCalorimeterHit {

    long id;
    int amplitude;
    int timestamp;

    protected BaseRawCalorimeterHit() {
    }

    public BaseRawCalorimeterHit(long id, int amplitude, int timestamp) {
        this.id = id;
        this.amplitude = amplitude;
        this.timestamp = timestamp;
        this.packedID = new Identifier(id);
    }

    public BaseRawCalorimeterHit(long id, int amplitude, int timestamp, IDetectorElement de) {
        this.id = id;
        this.amplitude = amplitude;
        this.timestamp = timestamp;
        this.detectorElement = de;
    }

    public long getCellID() {
        return id;
    }

    public int getAmplitude() {
        return amplitude;
    }

    public int getTimeStamp() {
        return timestamp;
    }

    /**
     * The default position is that of the associated DetectorElement.
     * @return The position as a size 3 double array.
     */
    public double[] getPosition() {
        return getDetectorElement().getGeometry().getPosition().v();
    }
}