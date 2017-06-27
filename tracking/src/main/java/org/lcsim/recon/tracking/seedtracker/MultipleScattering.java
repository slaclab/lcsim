/*
 * MultipleScattering.java
 *
 * Created on January 23, 2008, 2:53 PM
 *
 */

package org.lcsim.recon.tracking.seedtracker;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.lcsim.fit.helicaltrack.HelicalTrackFit;
import org.lcsim.fit.helicaltrack.HelicalTrackHit;
import org.lcsim.fit.helicaltrack.HelixUtils;
import org.lcsim.fit.helicaltrack.MultipleScatter;

/**
 * Calculate the multiple scattering contribution to the hit position
 * error.
 * @author Richard Partridge
 * @version 1.0
 */
public class MultipleScattering {
    protected MaterialManager _materialmanager;
    protected double _bfield = 0.;
    private int _mxint = 10;
    protected boolean _debug = false;

    /**
     * Creates a new instance of MultipleScattering
     * @param materialmanager MaterialManager provides access to the scattering material
     */
    public MultipleScattering(MaterialManager materialmanager) {
        _materialmanager = materialmanager;
    }

    /**
     * Find the path lengths and multiple scattering angles for each
     * intersection of the helix with tracker material
     * @param helix HelicalTrackFit for this helix
     * @return List of path lengths and scattering angles
     */
    public List<ScatterAngle> FindScatters(HelicalTrackFit helix) {
        
        //  Check that B Field is set
        if (_bfield == 0.) throw new RuntimeException("B Field must be set before calling FindScatters method");

        //  Create a new list to contain the mutliple scatters
        List<ScatterAngle> scatters = new ArrayList<ScatterAngle>();

        //  Retrieve the cylinder and disk material models from the material manager
        List<MaterialCylinder> matcyl = _materialmanager.getMaterialCylinders();
        List<MaterialDisk> matdsk = _materialmanager.getMaterialDisks();
        List<MaterialXPlane> matxpl = _materialmanager.getMaterialXPlanes();

        //  Find the largest path length to a hit
        double smax = 9999.;

        //  We can't go further than the ECal, however
        double rmax = _materialmanager.getRMax();
        List<Double> slist = HelixUtils.PathToCylinder(helix, rmax, smax, 1);
        if (slist.size() > 0) smax = Math.min(smax, slist.get(0));
        double zmax = _materialmanager.getZMax();
        if (helix.slope() < 0.) zmax = -zmax;
        smax = Math.min(smax, HelixUtils.PathToZPlane(helix, zmax));

        for (MaterialDisk disk : matdsk) {
            double s = HelixUtils.PathToZPlane(helix, disk.z());
            if (s > 0. && s < smax) {
                Hep3Vector pos = HelixUtils.PointOnHelix(helix, s);
                double r = Math.sqrt(Math.pow(pos.x(), 2) + Math.pow(pos.y(),2));
                if (r >= disk.rmin() && r <= disk.rmax()) {
                    double cth = Math.abs(helix.cth());
                    double radlen = disk.ThicknessInRL() / Math.max(cth, .001);
                    double angle = msangle(helix.p(_bfield), radlen);
                    scatters.add(new ScatterAngle(s, angle));
                }
            }
        }

        for (MaterialCylinder cyl : matcyl) {
            double r = cyl.radius();
            double scmin = HelixUtils.PathToZPlane(helix, cyl.zmin());
            double scmax = HelixUtils.PathToZPlane(helix, cyl.zmax());
            if (scmin > scmax) {
                double temp = scmin;
                scmin = scmax;
                scmax = temp;
            }
            List<Double> pathlist = HelixUtils.PathToCylinder(helix, r, smax, _mxint);
            for (Double s : pathlist) {
                if (s > scmin && s < scmax) {
                    Hep3Vector dir = HelixUtils.Direction(helix, s);
                    Hep3Vector pos = HelixUtils.PointOnHelix(helix, s);
                    Hep3Vector rhat = VecOp.unit(new BasicHep3Vector(pos.x(), pos.y(), 0.));
                    double cth = Math.abs(VecOp.dot(dir, rhat));
                    double radlen = cyl.ThicknessInRL() / Math.max(cth, 0.001);
                    double angle = msangle(helix.p(_bfield), radlen);
                    scatters.add(new ScatterAngle(s, angle));
                }
        }
        }
        //mg 3/14/11  add in XPlanes
        for (MaterialXPlane xpl : matxpl) {          
            List<Double> pathlist = HelixUtils.PathToXPlane(helix, xpl.x(), smax, _mxint);
            for (Double s : pathlist) {
                Hep3Vector dir = HelixUtils.Direction(helix, s);
                Hep3Vector pos = HelixUtils.PointOnHelix(helix, s);
                double y=pos.y();
                double z=pos.z();
                if (y >= xpl.ymin() && y <= xpl.ymax()&&z >= xpl.zmin() && z <= xpl.zmax()) {
                    Hep3Vector xhat = VecOp.unit(new BasicHep3Vector(pos.x(),0. , 0.));
                    double cth = Math.abs(VecOp.dot(dir, xhat));
                    double radlen = xpl.ThicknessInRL() / Math.max(cth, .001);
                    double angle = msangle(helix.p(_bfield), radlen);
                    scatters.add(new ScatterAngle(s, angle));
                }
            }
        }
        //  Sort the multiple scatters by their path length
        Collections.sort(scatters);
        return scatters;
    }

    public static MultipleScatter CalculateScatter(HelicalTrackHit hit, HelicalTrackFit helix, List<ScatterAngle> scatters) {

        //  Retreive the x-y path length and calculate sin^2(theta) for this helix
        double sth2 = Math.pow(helix.sth(), 2);
        
        //  Make sure the hit has an x-y path lengths.  Hits added since the last fit
        //  won't have path lengths, so estimate the path length measured from the DCA
        Map<HelicalTrackHit, Double> pathmap = helix.PathMap();
        if (!pathmap.containsKey(hit)) {
            pathmap.put(hit, (Double) HelixUtils.PathLength(helix, hit));
        }

        double hitpath = pathmap.get(hit);

        //  Loop over scattering points and sum in quadrature ms errors for this hit.
        //  It is assumed that we can ignore ms correlations during the track-finding stage.
        double rphi_ms2 = 0.;
        double z_ms2 = 0.;
        for (ScatterAngle scat : scatters) {

            //  Find the x-y path length to this scatter
            double scatpath = scat.PathLen();
            
            //  If the scatter is before the hit, calculate the ms errors for this scatter
            if (scatpath > hitpath) break;

            //  Get the multiple scattering plane angle for this scatter
            double angle = scat.Angle();

            //  Sum in quadrature the r-phi ms errors.  It is assumed that we
            //  can ignore track curvature in calculating these errors during
            //  the track-finding stage.
            rphi_ms2 += Math.pow((hitpath - scatpath) * angle, 2);

            //  Sum in quadrature the z ms errors assuming a barrel geometry where
            //  the path length is fixed.  While z is fixed for disk detectors, we
            //  still do a z vs s fit by assuming the track direction is reasonably
            //  well known and converting the radial measurement error into an effective
            //  z coordinate error.
            z_ms2 += Math.pow((hitpath - scatpath) * angle, 2)/sth2;
        }
        
        //  Return the requested MultipleScatter
        return new MultipleScatter(Math.sqrt(rphi_ms2), Math.sqrt(z_ms2));
    }

    public void setBField(double bfield) {
        _bfield = bfield;
    }

//  Calculate the multiple scattering angle for a given momentum and thickness
    public double msangle(double p, double radlength) {
        double angle = (0.0136 / p) * Math.sqrt(radlength) * (1.0 + 0.038 * Math.log(radlength));
        return angle;
    }

    public void setDebug(boolean debug) {
        _debug = debug;
    }
    }
