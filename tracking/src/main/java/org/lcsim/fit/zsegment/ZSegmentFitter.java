/*
 * ZSegmentFitter.java
 *
 * Created on July 29, 2007, 9:43 PM
 *
 */

package org.lcsim.fit.zsegment;

import hep.physics.matrix.BasicMatrix;
import hep.physics.matrix.Matrix;
import hep.physics.matrix.MatrixOp;
import hep.physics.matrix.MutableMatrix;
import hep.physics.matrix.SymmetricMatrix;

import java.util.ArrayList;
import java.util.List;

/**
 * Find the allowed region in z0 - tan(lambda) track parameter space for a track that
 * passes through a set of detectors segmented in the z coordinate.  The z coordinates
 * for each measurement are treated as being bounded but unmeasured.  The allowed region
 * is a polygon in z0-tan(lambda) space with 3 or more vertices.  The centroid and
 * covariance matrix for the allowed region are also calculated.
 * @author Richard Partridge
 * @version 1.0
 */
public class ZSegmentFitter {
    
    private double[] _s;
    private double[] _zmin;
    private double[] _zmax;
    private List<double[]> _polygon;
    private double _area;
    private double[] _centroid = new double[2];
    private SymmetricMatrix _covariance;
    private double _eps = 1e-6;
    
    /**
     * Create a new instance of ZSegmentFitter
     */
    public ZSegmentFitter() {
        
    }
    
    /**
     * Find the allowed region in z0-tan(lambda) space and determine the centroid and
     * covariance matrix for this region.
     * @param s Array specifying the arc length in the x-y plane for each hit
     * @param zmin Array specifying the minimum z coordinate for each hit
     * @param zmax Array specifying the maximum z coordinate for each hit
     * @return True if the specified hits are consistent with a straight line in the s-z plane
     */
    public boolean fit(double[] s, double[] zmin, double[] zmax) {
//  Save the fit input values and initialize the fit results now in case we return without finding a successful fit
        _s = s;
        _zmin = zmin;
        _zmax = zmax;
        _polygon = new ArrayList<double[]>();
        _centroid[0] = 0.;
        _centroid[1] = 0.;
        _area = 0.;
        _covariance = null;
//  Check that the input values are sensible
        if (_s.length!=_zmin.length) return false;
        if (_s.length!=_zmax.length) return false;
        if (_s.length<2) return false;
        
//  Each z segment specfies an allowed band in the z0-tan(lambda) plane given by
//  the constraint zmin < z0 + s*tan(lambda) < zmax.  The edges of each band are
//  bounded by the lines zmin = z0 + s*tan(lambda) and zmax = z0 + s*tan(lambda).
//  For each pair of measurements, the intersection of the corresponding bands is
//  a parallelogram in z0-tan(lambda) space.  The four vertices of the parallelogram
//  are found by intersecting the lines specifying the band edges given above.
//  The allowed region in z0-tan(lambda) space will be a convex polygon whose vertices
//  are a subset of all parallelogram vertices found in considering all layer pairs.
//  Each parallelogram vertex specifies a specific point in z0-tan(lambda) space
//  that may or may not be consistent with the full set of z segments.  Those
//  parallelogram vertices that are consistent with all z segments are retained
//  as polygon vertices that specify the allowed region in z-tan(lambda) space.
        for (int i=0; i<_s.length-1; i++) {
            for (int j=i+1; j<_s.length; j++) {
                IntersectLines(_s[i],_zmin[i],_s[j],_zmin[j]);
                IntersectLines(_s[i],_zmin[i],_s[j],_zmax[j]);
                IntersectLines(_s[i],_zmax[i],_s[j],_zmin[j]);
                IntersectLines(_s[i],_zmax[i],_s[j],_zmax[j]);
            }
        }
        
//  Check that we have at least 3 polygon vertices - fewer vertices indicates that the
//  specified z segments are not consistent with a straight line track in s-z space.
        int nv = _polygon.size();
        if (nv<3) return false;
        
//  Order the vertices so that adjacent vertices describe a line segment in the
//  polygon
        OrderVertices();
        
//  Find the area and centroid of the polygon (see, for example, http://local.wasp.uwa.edu.au/~pbourke/geometry/polyarea/)
        for (int i=0; i<nv; i++) {
            int j = (i+1) % nv;
            double[] p0 = _polygon.get(i);
            double[] p1 = _polygon.get(j);
            double darea = 0.5*(p0[0]*p1[1] - p1[0]*p0[1]);
            _area += darea;
            _centroid[0] += (p0[0]+p1[0]) * darea;
            _centroid[1] += (p0[1]+p1[1]) * darea;
        }
        _centroid[0] /= 3*_area;
        _centroid[1] /= 3*_area;
        _area = Math.abs(_area);
        
//  Calculate the covariance matrix for this polygon
        _covariance = PolygonCovariance();
        
        return true;
    }
    
    private void IntersectLines(double s1, double o1, double s2, double o2) {
//  Find the intersection of the lines z0 = 01 - s1*tan(lambda) and z0 = o2 - s2*tan(lambda)
        double[] cross = new double[2];
        if (s1==s2) return;
        cross[0] = (o1*s2 - o2*s1) / (s2 - s1);
        cross[1] = (o2 - o1) / (s2 - s1);
        
//  See if this intersection is consistent with all z segments
        for (int i=0; i<_s.length; i++) {
            double zpred = cross[0] + _s[i]*cross[1];
            if (zpred<_zmin[i]-_eps || zpred>_zmax[i]+_eps ) return;
        }
        
//  See if this intersection duplicates one we have previously found
        for (double[] old_cross : _polygon) {
            if (Math.pow(cross[0]-old_cross[0],2)+Math.pow(cross[1]-old_cross[1],2)<Math.pow(_eps,2)) return;
        }
        
//  Add this intersection to our polygon
        _polygon.add(cross);
    }
    
    private void OrderVertices() {
//  Take as an origin a point within the polygon and order the polygon vertices according to their azimuthal angle
        double[] pcent = PseudoCentroid();
        int nv = _polygon.size();
        for (int i=0; i<nv-1; i++) {
            for (int j=i+1; j<nv; j++) {
                //  phi1 calcuation must stay inside loop because of possible re-ordering
                double phi1 = Math.atan2(_polygon.get(i)[1]-pcent[1],_polygon.get(i)[0]-pcent[0]);
                double phi2 = Math.atan2(_polygon.get(j)[1]-pcent[1],_polygon.get(j)[0]-pcent[0]);
                if (phi1>phi2) {
                    double[] temp = _polygon.get(j);
                    _polygon.set(j,_polygon.get(i));
                    _polygon.set(i,temp);
                }
            }
        }
    }
    
    private double[] PseudoCentroid() {
        // Find a point within the convex polygon by averaging the coordinates of all vertices
        double[] pcent = {0.,0.};
        int nv = _polygon.size();
        for (double[] point : _polygon) {
            pcent[0] += point[0]/ nv;
            pcent[1] += point[1]/ nv;
        }
        return pcent;
    }
    
    private SymmetricMatrix PolygonCovariance() {
//  Calculate the covariance matrix for a convex polygon assuming all points in the polygon are equally likely.
//  The algorithm used here is to break the polygon into triangles, each of which contains the polygon centroid,
//  and calculate the contribution to the covariance matrix from each such triangle.
        
        int nv = _polygon.size();
        double cxx = 0.;
        double cxy = 0.;
        double cyy = 0.;
        for (int i=0; i<nv; i++) {
            int j = (i+1) % nv;
//  Find the triangle vertices in a coordinate system whose origin is at the polygon centroid.  Store the
//  two vertices as two columns in a 2x2 matrix (we ignore the 3rd triangle vertex located at the centroid)
            BasicMatrix vertices = new BasicMatrix(2,2);
            vertices.setElement(0,0,_polygon.get(i)[0]-_centroid[0]);
            vertices.setElement(1,0,_polygon.get(i)[1]-_centroid[1]);
            vertices.setElement(0,1,_polygon.get(j)[0]-_centroid[0]);
            vertices.setElement(1,1,_polygon.get(j)[1]-_centroid[1]);
            
//  Rotate these vertices to a new x',y' coordinate system where vertex 2 is on the x' axis
            double phi = Math.atan2(vertices.e(1,1),vertices.e(0,1));
            BasicMatrix rotmat = new BasicMatrix(2,2);
            rotmat.setElement(0,0,Math.cos(phi));
            rotmat.setElement(0,1,Math.sin(phi));
            rotmat.setElement(1,0,-rotmat.e(0,1));
            rotmat.setElement(1,1,rotmat.e(0,0));
            Matrix rotvert = MatrixOp.mult(rotmat,vertices);
            
//  Find the contributions to the covariance matrix for the triangle in the x',y' coordinate system.  If
//  the apex of the triangle is at (x'1,y'1), and the base of the triangle is at (0,0) and (x'2,0), then:
//      A = 1/2 y'1 * x'2
//      Ix'x' = integral x'*x'*dA = A * (x'1**2 + x'1*x'2 + x'2**2) / 6
//      Ix'y' = integral x'*y'*dA = A * y'1 * (2*x'1 + x'2) / 12
//      Iy'y' = integral y'*y'*dA = A * y'1**2 / 6
            double darea = Math.abs(0.5*rotvert.e(1,0)*rotvert.e(0,1));
            SymmetricMatrix cov_loc = new SymmetricMatrix(2);
            cov_loc.setElement(0,0,darea*(Math.pow(rotvert.e(0,0),2)+rotvert.e(0,0)*rotvert.e(0,1)+Math.pow(rotvert.e(0,1),2))/6.);
            cov_loc.setElement(1,0,darea*rotvert.e(1,0)*(2.*rotvert.e(0,0)+rotvert.e(0,1))/12);
            cov_loc.setElement(1,1,darea*Math.pow(rotvert.e(1,0),2)/6);
            
//  Rotate the 2nd rank tensor back to the local coordinate system: v vT = RT * v' v'T R where R is the rotation matrix,
//  v / v' are the (x,y) / (x',y') column vectors, and T indicates the transpose operator.  Note that v' = R v and R RT = RT R = 1.
            MutableMatrix rotbar = new BasicMatrix(2,2);
            MatrixOp.transposed(rotmat,rotbar);
            Matrix cov_glb = MatrixOp.mult(rotbar,MatrixOp.mult(cov_loc,rotmat));
            
//  Add the covariance contribution for this triangle to the polygon sum (note - we need a matrix add operation in freehep !!)
            cxx += cov_glb.e(0,0);
            cxy += cov_glb.e(1,0);
            cyy += cov_glb.e(1,1);
        }
        
//  Create the covariance matrix by dividing out the polygon area: cov_xx = integral x*x*dA / integral dA
        SymmetricMatrix cov = new SymmetricMatrix(2);
        cov.setElement(0,0,cxx/_area);
        cov.setElement(1,0,cxy/_area);
        cov.setElement(1,1,cyy/_area);
        
        return cov;
    }
    
    /**
     * Return the resutls of the fit as a ZSegmentFit object
     * @return ZSegmentFitter fit result
     */
    public ZSegmentFit getFit() {
        return new ZSegmentFit(_polygon, _centroid, _covariance);
    }
}