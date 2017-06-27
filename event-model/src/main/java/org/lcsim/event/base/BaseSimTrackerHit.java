package org.lcsim.event.base;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.Identifier;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.MCParticle;
import org.lcsim.event.SimTrackerHit;

/**
 * A concrete implementation of SimTrackerHit.
 * 
 * @author Jeremy McCormick
 * @version $Id: BaseSimTrackerHit.java,v 1.19 2012/07/20 09:29:43 grefe Exp $
 */
public class BaseSimTrackerHit extends BaseHit implements SimTrackerHit {
    
    protected double[] position = new double[3];
    protected double[] momentum = new double[3];
    protected MCParticle mcparticle;
    protected double time;
    protected double dEdx;
    protected int cellID0;
    protected int cellID1;
    protected long id;
    protected double pathLength;
    private Hep3Vector startPoint, endPoint;

    /**
     * Constructor for subclasses
     */
    protected BaseSimTrackerHit() {
    }

    /**
     * Fully qualified constructor
     * 
     * @param position The center point of the hit in Cartesian coordinates (x,y,z).
     * @param momentum The momentum of the hit in GeV (px,py,pz).
     * @param mcparticle The associated MCParticle. (may be null)
     * @param time The time of the hit in nanoseconds.
     * @param dEdx The energy deposited over the path in GeV.
     * @param cellID The 32-bit identifier.
     * @param pathLength The path length from start to end point.
     * @param meta The LCMetaData associated to this hit. (may be null)
     * @param de The DetectorElement associated to this hit. (may be null)
     */
    public BaseSimTrackerHit(double[] position, double dEdx, double[] momentum, double pathLength, double time, int cellID, MCParticle mcparticle, LCMetaData meta, IDetectorElement de) {
        
        positionVec = new BasicHep3Vector(position[0], position[1], position[2]);

        if (position.length != 3)
            throw new IllegalArgumentException("The position array is of the wrong size!");
        for (int i = 0, n = this.position.length; i < n; i++) {
            this.position[i] = position[i];
        }

        if (momentum == null)
            throw new IllegalArgumentException("The momentum points to null!");
        if (momentum.length != 3)
            throw new IllegalArgumentException("The momentum array is of the wrong size!");
        for (int i = 0, n = this.momentum.length; i < n; i++) {
            this.momentum[i] = momentum[i];
        }

        this.mcparticle = mcparticle;
        this.time = time;
        this.dEdx = dEdx;
        this.cellID0 = cellID;
        // TODO need to properly set a 64bit ID
        this.id = (long) cellID;
        this.pathLength = pathLength;
        this.metaData = meta;
        this.detectorElement = de;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public int getLayer() {
        getIDDecoder().setID(getCellID());
        return getIDDecoder().getLayer();
    }

    public double[] getPoint() {
        return positionVec.v();
    }

    public double getTime() {
        return time;
    }

    public double getdEdx() {
        return dEdx;
    }

    public MCParticle getMCParticle() {
        return mcparticle;
    }

    // @Deprecated
    // Use {@link #getCellID64()} instead.
    public int getCellID() {
        return cellID0;
    }

    public long getCellID64() {
        return id;
    }

    public void setCellID64(long cellID) {
        id = cellID;
    }

    public double getPathLength() {
        return pathLength;
    }

    public double[] getMomentum() {
        return momentum;
    }

    public double[] getStartPoint() {
        if (startPoint == null)
            computePoints();
        return startPoint.v();
    }

    public double[] getEndPoint() {
        if (endPoint == null)
            computePoints();
        return endPoint.v();
    }

    private void computePoints() {
        Hep3Vector midpoint = new BasicHep3Vector(getPoint());
        Hep3Vector direction = VecOp.unit(new BasicHep3Vector(getMomentum()));
        Hep3Vector halfLength = VecOp.mult(getPathLength() / 2.0, direction);

        startPoint = VecOp.add(midpoint, VecOp.mult(-1.0, halfLength));
        endPoint = VecOp.add(midpoint, halfLength);
    }

    public IIdentifier getIdentifier() {
        if (packedID == null)
            packedID = new Identifier(id);
        return packedID;
    }    
    
    /**
     * Get the {@see org.lcsim.detector.IDetectorElement} associated with this hit.     
     * @return The IDetectorElement for this hit.
     */
    public IDetectorElement getDetectorElement() {        
        if (detectorElement == null) {
            // By default, use the hit's position to lookup the geometry.
            detectorElement = findDetectorElement(this.getPositionVec());
        }        
        return detectorElement;
    }
}
