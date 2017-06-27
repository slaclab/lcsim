/*
 * Cell.java
 *
 * Created on April 4, 2006, 11:09 AM
 *
 * $Id: $
 */
package org.lcsim.recon.cluster.localequivalence;

/**
 * A cell with properties adapted to a nearest-neighbor clustering algorithm
 * whose metric for "nearest" is based on value().
 *
 * @author Norman Graf
 */
public abstract class Cell implements Comparable
{
    /**
     *
     * @return A value for weighting multiple neighbors.
     */
    public abstract double value();

    /**
     *
     * @return A unique identifier associated with this cell
     */
    public abstract long cellID();

    /**
     *
     * @return A Cell to which this Cell points.
     */
    public abstract Cell pointsTo();

    /**
     *
     * @param pointsto Assign a Cell to which this Cell points.
     */
    public abstract void pointsTo(Cell pointsto);

    /**
     *
     * @param cell Assign a Cell which points to this Cell.
     */
    public abstract void pointedTo(Cell cell);

    /**
     *
     * @return A Cell which points to this Cell.
     */
    public abstract Cell pointedTo();

}