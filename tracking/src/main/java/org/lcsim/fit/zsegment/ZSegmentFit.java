/*
 * ZSegmentFit.java
 *
 * Created on August 1, 2007, 9:46 AM
 *
 */

package org.lcsim.fit.zsegment;

import java.util.List;
import hep.physics.matrix.SymmetricMatrix;

/**
 * Encapsulates the constraints in the z0 - tan(lambda) track parameters
 * due to a track passing through a set of detectors segmented in z.
 * @author Richard Partridge
 * @version 1.0
 */
public class ZSegmentFit {
    
    private List<double[]> _polygon;
    private double[] _centroid;
    private SymmetricMatrix _covariance;
    
    /**
     * Create a new instance of ZSegmentFit
     * @param polygon List of vertices of the polygon describing the allowed region in the z0 - tan(lambda) plane.
     * Each vertex is a double[2] with the first element being the z0 coordinate and the second
     * the tan(lambda) coordinate.
     * @param centroid Centroid of the polygon, where centroid[0] is the z0 coordinate, centroid[1] is the tan(lambda) coordinate
     * @param covariance Covariance matrix calculated assuming equal probability for all points in the polygonal allowed region
     */
    public ZSegmentFit(List<double[]> polygon, double[] centroid, SymmetricMatrix covariance) {
        _polygon = polygon;
        _centroid = centroid;
        _covariance = covariance;
    }
    
    /**
     * Return the polygon that specifies the allowed region in z0 - tan(lambda) space
     * @return List of vertices for the polygon.  Each vertex is returned as a double[2] with the first element
     * giving the z0 coordinate and the second element the tan(lambda) coordinate
     */
    public List<double[]> getPolygon() {
        return _polygon;
    }
    
    /**
     * Return the centroid (i.e., center of gravity) of the allowed region in the z0-tan(lambda) plane
     * @return Centroid of the polygon, with the first element giving the z0 coordinate and the second element
     * giving the tan(lambda) coordinate
     */
    public double[] getCentroid() {
        return _centroid;
    }
    
    /**
     * Return the covariance matrix for the allowed region in the z0 - tan(lambda) plane
     * @return Covariance matrix
     */
    public SymmetricMatrix getCovariance() {
        return _covariance;
    }
    
    /**
     * Return a string containing properties of this ZSegmentFit
     * @return String containing ZSegmentFit properties
     */
    public String toString() {
        String nl = System.getProperty("line.separator");
        StringBuffer output = new StringBuffer("ZSegmentFit: Centroid (z0, tan(lambda)) = ("+_centroid[0]+", "+_centroid[1]+")"+nl);
        output.append("             Covariance matrix ("+_covariance.e(0,0)+", "+_covariance.e(1,0)+", "+_covariance.e(1,1)+")"+nl);
        output.append("             Allowed region is a "+_polygon.size()+" sided polygon with vertices:"+nl);
        for (double[] vert : _polygon) {
            output.append("                 ("+vert[0]+", "+vert[1]+")"+nl);
        }
        return new String(output);
    }
    
}
