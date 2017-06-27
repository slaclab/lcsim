/*
 * SiSensorElectrodes.java
 *
 * Created on April 11, 2007, 9:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.detector.tracker.silicon;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.ITransform3D;
import hep.physics.vec.Hep3Vector;
import java.util.SortedMap;
import java.util.Set;
import org.lcsim.detector.solids.Polygon3D;

/**
 *
 * @author tknelson
 */
public interface SiSensorElectrodes
{
    // Cell shape, assumed to be strips or rectancular pixels
    public int getNAxes();
    
    // Get Detector element for associated sensor
    public IDetectorElement getDetectorElement();
    
    // Transformation from sensor coordinates to electrode coordinates
    public ITransform3D getParentToLocal();    
    
    // Transformation from electrode coordinates to global coordinates
    public ITransform3D getLocalToGlobal();
    
    // Transformation from gloabl coordinates to electrode coordinates
    public ITransform3D getGlobalToLocal();
    
    // Polygon on which electrodes are defined
    public Polygon3D getGeometry();
    
    // Direction of each measured coordinate
    public Hep3Vector getMeasuredCoordinate(int axis);
    
    // Direction of each non-measured coordinate (i.e. strip axis for strips)
    public Hep3Vector getUnmeasuredCoordinate(int axis);
    
    // Neigbor ncells away along each axis
    public int getNeighborCell(int cell, int ncells_0, int ncells_1);
    
    // Get all nearest neighbor cells
    public Set<Integer> getNearestNeighborCells(int cell);
    
    // Cell number is valid
    public boolean isValidCell(int cell);
    
    // Number of cells (strips or pixels)
    public int getNCells();
    
    // Number of cells along each axis
    public int getNCells(int axis);
    
    // Size of a cell (strip or pixel pitch)
    public double getPitch(int axis);
    
    // Position of a particular cell (by cell number)
    public Hep3Vector getCellPosition(int cell_id);
    
    // ID of cell at a given position (cell number)
    public int getCellID(Hep3Vector position);
    
    // Column number of cell at given position
    public int getColumnNumber(Hep3Vector position);
    
    // Row number of cell at given position
    public int getRowNumber(Hep3Vector position);
    
    // ID of cell from row and column number
    public int getCellID(int row_number, int column_number);
    
    // Column number of cell from ID
    public int getColumnNumber(int cell_id);
    
    // Row number of cell from ID
    public int getRowNumber(int cell_id);
    
    // Location of given position within a cell
    public Hep3Vector getPositionInCell(Hep3Vector position);
    
    // Charge carrier
    public ChargeCarrier getChargeCarrier();
    
    // Capacitance of electrode
    public double getCapacitance(int cell_id);

    // Nominal capacitance (used for throwing random noise hits)
    public double getCapacitance();
    
    // Compute Gaussian-distributed charge on electrodes
    public SortedMap<Integer,Integer> computeElectrodeData(ChargeDistribution distribution);
    
}
