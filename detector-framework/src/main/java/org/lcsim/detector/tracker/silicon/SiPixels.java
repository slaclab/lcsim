/*
 * SiPixels.java
 *
 * Created on May 10, 2008, 4:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.lcsim.detector.tracker.silicon;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.Transform3D;
import org.lcsim.detector.solids.GeomOp2D;
import org.lcsim.detector.solids.GeomOp3D;
import org.lcsim.detector.solids.Point3D;
import org.lcsim.detector.solids.Polygon3D;
import org.lcsim.util.probability.BivariateDistribution;

/**
 *
 * @author tknelson
 */
public class SiPixels implements SiSensorElectrodes {

    private ChargeCarrier _carrier; // charge carrier collected
    private int _nrows; // number of rows - row index measures x
    private int _ncols; // number of columns - column index measures y
    protected double _row_pitch; // row pitch
    protected double _col_pitch; // column pitch
    private double _window_size = 3.0; // # sigma for computing electrode data
    private double _capacitance = 0.1;  // capacitance of a pixel in pF

    // cached for convenience
    protected double _row_offset; // row offset
    protected double _col_offset; // column offset
    private IDetectorElement _detector; // associated detector element
    private ITransform3D _parent_to_local; // parent to local transform
    private ITransform3D _local_to_global; // transformation to global coordinates
    private ITransform3D _global_to_local; // transformation from global coordinates
    private Polygon3D _geometry; // region in which strips are defined

    /** Creates a new instance of SiPixels */
    public SiPixels(ChargeCarrier carrier, double row_pitch, double col_pitch,
            IDetectorElement detector, ITransform3D parent_to_local) {

    //        System.out.println("Plane of polygon in sensor coordinates has... ");
    //        System.out.println("                        normal: "+((SiSensor)detector).getBiasSurface(carrier).getNormal());
    //        System.out.println("                        distance: "+((SiSensor)detector).getBiasSurface(carrier).getDistance());

        setCarrier(carrier);
        setRowPitch(row_pitch);
        setColumnPitch(col_pitch);
        setGeometry(((SiSensor) detector).getBiasSurface(carrier).transformed(parent_to_local));
        setPixelNumbering();
        setDetectorElement(detector);
        setParentToLocal(parent_to_local);
        setGlobalToLocal(Transform3D.multiply(parent_to_local, detector.getGeometry().getGlobalToLocal()));
        setLocalToGlobal(getGlobalToLocal().inverse());
    }

    // Cell shape, assumed to be strips or rectancular pixels
    public int getNAxes() {
        return 2;
    }

    // Get Detector element for associated sensor
    public IDetectorElement getDetectorElement() {
        return _detector;
    }

    // Transformation from sensor coordinates to electrode coordinates
    public ITransform3D getParentToLocal() {
        return _parent_to_local;
    }

    // Transformation from electrode coordinates to global coordinates
    public ITransform3D getLocalToGlobal() {
        return _local_to_global;
    }

    // Transformation from gloabl coordinates to electrode coordinates
    public ITransform3D getGlobalToLocal() {
        return _global_to_local;
    }

    // Polygon on which electrodes are defined
    public Polygon3D getGeometry() {
        return _geometry;
    }

    // Direction of each measured coordinate
    public Hep3Vector getMeasuredCoordinate(int axis) {
        if (axis == 0) return new BasicHep3Vector(1.0, 0.0, 0.0);
        else if (axis == 1) return new BasicHep3Vector(0.0, 1.0, 0.0);
        else return null;
    }

    // Direction of each non-measured coordinate (i.e. strip axis for strips)
    public Hep3Vector getUnmeasuredCoordinate(int axis) {
        if (axis == 0) return new BasicHep3Vector(0.0, 1.0, 0.0);
        if (axis == 1) return new BasicHep3Vector(1.0, 0.0, 0.0);
        else return null;
    }

    // Neigbor ncells away along each axis
    public int getNeighborCell(int cell_id, int ncells_row, int ncells_col) {

        //  Get the column and row numbers of the neighbor cell
        int nbrcol = getColumnNumber(cell_id) + ncells_col;
        int nbrrow = getRowNumber(cell_id) + ncells_row;

        //  Get teh neighbor cell ID and check that it's valid
        int nbrcell = getCellID(nbrrow, nbrcol);
        if (nbrcell < 0) return nbrcell;

        //  Check that the cell is valid (needed for non-rectangular geometries)
        if (!isValidCell(cell_id)) return -1;

        //  Valid neighbor - return the cell ID
        return nbrcell;
    }

    // Get all nearest neighbor cells
    public Set<Integer> getNearestNeighborCells(int cell_id) {
        Set<Integer> neighbors = new HashSet<Integer>();

        //  Loop over cells in 3x3 array around cell_id
        for (int irow = -1; irow <= 1; irow++) {
            for (int icol = - 1; icol <= 1; icol++) {
                
                //  Exclude the starting cell
                if (irow == 0 && icol == 0) continue;

                //  Get the neighbor cell and add it to the neighbor set if valid
                int neighbor = getNeighborCell(cell_id, irow, icol);
                if (neighbor >= 0) neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    // Cell number is valid
    public boolean isValidCell(int cell_id) {
        return GeomOp3D.intersects(new Point3D(getCellPosition(cell_id)), _geometry);  // FIXME: should cell position be a Point3D??
    }

    // Number of cells (strips or pixels)
    public int getNCells() {
        return _nrows * _ncols;
    }

    // Number of cells along each axis
    public int getNCells(int axis) {
        if (axis == 0) return _nrows;
        else if (axis == 1) return _ncols;
        else return 0;
    }

    // Size of a cell (strip or pixel pitch)
    public double getPitch(int axis) {
        if (axis == 0) return _row_pitch;
        else if (axis == 1) return _col_pitch;
        else return 0;
    }

    // ID of cell at a given position (cell number)
    public int getCellID(Hep3Vector position) {
        return getCellID(getRowNumber(position), getColumnNumber(position));
    }

    // Row number of cell at given position
    public int getRowNumber(Hep3Vector position) {
        int row = (int) Math.round((position.x() + _row_offset) / _row_pitch);
        if (row < 0) row = 0;
        if (row >= _nrows) row = _nrows - 1;
        return row;
    }

    // Column number of cell at given position
    public int getColumnNumber(Hep3Vector position) {
        int col = (int) Math.round((position.y() + _col_offset) / _col_pitch);
        if (col < 0) col = 0;
        if (col >= _ncols) col = _ncols - 1;
        return col;
    }

    // ID of cell from row and column number
    public int getCellID(int row_number, int column_number) {
        if (row_number < 0 || row_number >=_nrows) return -1;
        if (column_number < 0 || column_number >= _ncols) return -1;
        return row_number * _ncols + column_number;
    }

    // Row number of cell from ID
    public int getRowNumber(int cell_id) {
        return cell_id / _ncols;
    }

    // Column number of cell from ID
    public int getColumnNumber(int cell_id) {
        return cell_id - getRowNumber(cell_id) * _ncols;
    }

    // Location of given position within a cell
    public Hep3Vector getPositionInCell(Hep3Vector position) {
        return VecOp.sub(position, getCellPosition(getCellID(position)));
    }

    // Position of a particular cell (by cell number)
    public Hep3Vector getCellPosition(int cell_id) {
        return new BasicHep3Vector(getRowNumber(cell_id) * _row_pitch - _row_offset, getColumnNumber(cell_id) * _col_pitch - _col_offset, 0.0);
    }

    // Charge carrier
    public ChargeCarrier getChargeCarrier() {
        return _carrier;
    }

    /**
     * Returns the capacitance of a pixel in units of pF.  For SiPixels
     * the capacitance of all pixels are taken to be the same.
     *
     * @param cell_id
     * @return
     */
    public double getCapacitance(int cell_id) {
        return getCapacitance();
    }

    /**
     * Nominal pixel capacitance in units of pF.
     *
     * @return
     */
    public double getCapacitance() {
        return _capacitance;
    }

    /**
     * Set the pixel capacitance.  Currently, all pixels are assumed to have
     * identical capacitance.  Note that the pixel capacitance is used in the
     * readout noise calculation.  Units are pF.
     *
     * @param capacitance
     */
    public void setCapacitance(double capacitance) {
        _capacitance = capacitance;
    }

    /**
     * Sets the size of the window used to distribute charge over.  The window
     * size is specified in units the Gaussian RMS along the measurement axes.
     * For example, setWindowSize(3.0) will result in the computeElectrodeData
     * method calculating the charge deposition for all pixel cells that are
     * fully or partially contained within a +-3 sigma window in the x and y
     * directions about the mean of the charge distribution.
     *
     * @param window_size window size in units of sigma
     */
    public void setWindowSize(double window_size) {
        _window_size = Math.abs(window_size);
    }

    /**
     * Integrate a 2D Gaussian charge distribution over the electrodes for this
     * pixel sensor.  The charge distribution argument must be an instance of
     * the GaussianDistribution2D, class, which provides the access to the
     * parameters of the bivariate charge distribution.  The method uses the
     * BivariateDistribution class to perform the integration.  The size of
     * the pixel window can be set using the setWindowSize method.
     *
     * @param distribution charge distribution
     * @return Map containing pixel charges keyed by pixel number
     */
    public SortedMap<Integer, Integer> computeElectrodeData(ChargeDistribution distribution) {

        //  Check to make sure we have a 2D charge distribution
        if (!(distribution instanceof GaussianDistribution2D))
            throw new RuntimeException("Electrode charge distribution not recognized");
        GaussianDistribution2D gdistribution = (GaussianDistribution2D) distribution;

        //  Create a map to store the electrode data
        SortedMap<Integer, Integer> electrode_data = new TreeMap<Integer, Integer>();

        //  Instantiate the bivariate probability distribution
        BivariateDistribution bivariate = new BivariateDistribution();

        //  Find the center of the charge distribution
        Hep3Vector gmean = gdistribution.getMean();
        double x0 = gmean.x();
        double y0 = gmean.y();
        //System.out.println("Charge distribution center: x0: " + x0 + " y0: " + y0); 

        //  Get the measurement axes - axis 0 is the x axis and axis 1 is the y axis
        Hep3Vector xaxis = getMeasuredCoordinate(0);
        Hep3Vector yaxis = getMeasuredCoordinate(1);
        //System.out.println("x axis: " + xaxis.toString()); 
        //System.out.println("y axis: " + yaxis.toString()); 

        //  Get the x, y widths and correlation coeficient for the charge distribution
        double xsig = gdistribution.sigma1D(xaxis);
        double ysig = gdistribution.sigma1D(yaxis);
        double rho = gdistribution.covxy(xaxis, yaxis) / (xsig * ysig);

        //  Get the x and y pitches
        double xpitch = getPitch(0);
        double ypitch = getPitch(1);
        //System.out.println("Pitch: x: " + xpitch + " y: " + ypitch); 

        //  Find the window of cells that contain measurable charge by finding
        //  the corner cells for a window around the base pixel
        double xmin0 = x0 - _window_size * xsig;
        double ymin0 = y0 - _window_size * ysig;
        int cell1 = getCellID(new BasicHep3Vector(xmin0, ymin0, 0.));
        double xmax0 = x0 + _window_size * xsig;
        double ymax0 = y0 + _window_size * ysig;
        int cell2 = getCellID(new BasicHep3Vector(xmax0, ymax0, 0.));
        
        //System.out.println("xmin0: " + xmin0 + " xmax0: " + xmax0); 
        //System.out.println("ymin0: " + ymin0 + " ymax0: " + ymax0);
        //System.out.println("cell1: " + cell1);  
        //System.out.println("cell2: " + cell2);  


        //  Get the row and column indices for the corner cells
        int ixmin = getRowNumber(cell1);
        int iymin = getColumnNumber(cell1);
        int ixmax = getRowNumber(cell2);
        int iymax = getColumnNumber(cell2);

        //System.out.println("min row number x: " + ixmin + " min column number y: " + iymin);  
        //System.out.println("max row number x: " + ixmax + " max column number y: " + iymax);  

        //  Establish the x, y binning
        Hep3Vector corner1 = getCellPosition(cell1);
        double xmin = corner1.x() - 0.5 * xpitch;
        double ymin = corner1.y() - 0.5 * ypitch;
        int nxbins = ixmax - ixmin + 1;
        int nybins = iymax - iymin + 1;
        if (nxbins < 1) {
            nxbins = 1;
        }
        
        if (nybins < 1) {
            nybins = 1;
        }

        bivariate.xBins(nxbins, xmin, xpitch);
        bivariate.yBins(nybins, ymin, ypitch);

        //  Calculate the probability distribution for these bins
        double[][] prob = bivariate.Calculate(x0, y0, xsig, ysig, rho);

        //  Get the total charge in the distribution
        double normalization = gdistribution.getNormalization();

        //  Loop over the probability distribution bins
        for (int ix = 0; ix < nxbins; ix++) {
            for (int iy = 0; iy < nybins; iy++) {

                //  Find the pixel corresponding to this bin
                int ipixel = getCellID(ixmin + ix, iymin + iy);
                //System.out.println("ix: " + ix + " iy: " + iy + " ipixel: " + ipixel);  

                //  Make sure we have a valid pixel
                if (isValidCell(ipixel)) {
                    
                    //System.out.println("Valid pixel found.");
                    //System.out.println("Prob: " + prob[ix][iy]); 
                    //System.out.println("Normalization: " + normalization);  
                    //  Calculate the charge in this pixel
                    int pixel_charge = (int) Math.round(normalization * prob[ix][iy]);
                    
                    //System.out.println("Pixel charge: " + pixel_charge); 

                    //  Store the pixel charge in the electrode data map
                    if (pixel_charge != 0) {
                        electrode_data.put(ipixel, pixel_charge);
                    }
                }
            }
        }
        return electrode_data;
    }

    // Private setters
    //==================
    public void setCarrier(ChargeCarrier carrier) {
        _carrier = carrier;
    }

    public void setGeometry(Polygon3D geometry) {
//        System.out.println("Plane of polygon has... ");
//        System.out.println("                        normal: "+geometry.getNormal());
//        System.out.println("                        distance: "+geometry.getDistance());
//
//        System.out.println("Working plane has... ");
//        System.out.println("                        normal: "+GeomOp2D.PLANE.getNormal());
//        System.out.println("                        distance: "+GeomOp2D.PLANE.getDistance());

        if (GeomOp3D.equals(geometry.getPlane(), GeomOp2D.PLANE)) {
            _geometry = geometry;
        } else {
            throw new RuntimeException("Electrode geometry must be defined in x-y plane!!");
        }
    }

    private void setPixelNumbering() {
        double xmin = Double.MAX_VALUE;
        double xmax = Double.MIN_VALUE;
        double ymin = Double.MAX_VALUE;
        double ymax = Double.MIN_VALUE;
        for (Point3D vertex : _geometry.getVertices()) {
            xmin = Math.min(xmin, vertex.x());
            xmax = Math.max(xmax, vertex.x());
            ymin = Math.min(ymin, vertex.y());
            ymax = Math.max(ymax, vertex.y());
        }

        setNRows((int) Math.ceil((xmax - xmin) / getPitch(0)));
        setNColumns((int) Math.ceil((ymax - ymin) / getPitch(1)));

    }

    private void setNRows(int nrows) {
        _nrows = nrows;
        setRowOffset();
    }

    private void setNColumns(int ncolumns) {
        _ncols = ncolumns;
        setColumnOffset();
    }

    private void setRowOffset() {
        double xmin = Double.MAX_VALUE;
        double xmax = Double.MIN_VALUE;
        for (Point3D vertex : _geometry.getVertices()) {
            xmin = Math.min(xmin, vertex.x());
            xmax = Math.max(xmax, vertex.x());
        }
        //System.out.println("Row Offset: xmin: " + xmin + " xmax: " + xmax); 

        double row_center = (xmin + xmax) / 2;
        //System.out.println("Row Offset: center: " + row_center); 

        _row_offset = ((_nrows - 1) * _row_pitch) / 2 - row_center;
        
        //System.out.println("Row Offset: nrows: " + _nrows); 
        //System.out.println("Row Offset: pitch: " + _row_pitch); 
        //System.out.println("Row Offset: offset: " + _row_offset); 

    }

    private void setColumnOffset() {
        double ymin = Double.MAX_VALUE;
        double ymax = Double.MIN_VALUE;
        for (Point3D vertex : _geometry.getVertices()) {
            ymin = Math.min(ymin, vertex.y());
            ymax = Math.max(ymax, vertex.y());
        }
        //System.out.println("Column Offset: ymin: " + ymin + " ymax: " + ymax); 

        double column_center = (ymin + ymax) / 2;
        //System.out.println("Column Offset: center: " + column_center); 

        _col_offset = ((_ncols - 1) * _col_pitch) / 2 - column_center;


        //System.out.println("Column Offset: ncols: " + _ncols); 
        //System.out.println("Column Offset: pitch: " + _col_pitch); 
        //System.out.println("Column Offset: offset: " + _col_offset); 


    }

    private void setRowPitch(double row_pitch) {
        _row_pitch = row_pitch;
    }

    private void setColumnPitch(double col_pitch) {
        _col_pitch = col_pitch;
    }

    private void setDetectorElement(IDetectorElement detector) {
        _detector = detector;
    }

    private void setParentToLocal(ITransform3D parent_to_local) {
        _parent_to_local = parent_to_local;
    }

    private void setLocalToGlobal(ITransform3D local_to_global) {
        _local_to_global = local_to_global;
    }

    private void setGlobalToLocal(ITransform3D global_to_local) {
        _global_to_local = global_to_local;
    }
}
