/*
 * StereoHitMaker.java
 *
 * Created on June 22, 2008, 9:45 PM
 *
 */
package org.lcsim.fit.helicaltrack;

import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.event.MCParticle;

/**
 * This class forms stereo hits (HelicalTrackCross) from pairs of hits in
 * parallel sensor layers.
 *
 * @author Richard Partridge
 * @version $Id: StereoHitMaker.java,v 1.14 2013/02/09 00:41:09 omoreno Exp $
 */
public class StereoHitMaker {
    static final boolean debug=false;
    private double _tolerance;
    private double _maxsep;
    private double _epsParallel = 1.0e-2;
    private double _epsStereoAngle = 1.0e-2;

    /**
     * Creates a new instance of StereoHitMaker with default parameters:
     *
     * MaxSeparation = 10 mm
     * Tolerance = 2
     */
    public StereoHitMaker() {
        this(2., 10.);
    }

    /**
     * Fully qualified constructor for StereoHitMaker.
     *
     * The tolerance is a dimensionless parameter characterizes the maximum
     * angle between a track and the sensor for high efficiency.  A tolerance
     * near unity will allow hits to be found even for steeply inclined tracks.
     *
     * The maximum separation sets the maximum distance between sensor planes
     * when forming paired hits.
     *
     * @param tolerance dimensionless tolerance parameter
     * @param maxsep maximum separation between sensor planes (units are mm)
     */
    public StereoHitMaker(double tolerance, double maxsep) {
        _tolerance = tolerance;
        _maxsep = maxsep;
    }

    /**
     * Create cross hits from two lists of HelicalTrackStrip hits.  The cross
     * hits are found by making pairs of hits, taking one hit from each list,
     * that are compatible with forming crosses.
     *
     * @param slist1 list of HelicalTrackStrips in first sensor plane
     * @param slist2 list of HelicalTrackStrips in second sensor plane
     * @return list of HelicalTrackCross hits
     */
    public List<HelicalTrackCross> MakeHits(List<HelicalTrackStrip> slist1, List<HelicalTrackStrip> slist2) {

        //  Make a list of the cross hits that are found
        List<HelicalTrackCross> crosscol = new ArrayList<HelicalTrackCross>();

        if (slist1 == null || slist2 == null) return crosscol;

        //  Loop over pairs of hits taking one hit from each collection
        for (HelicalTrackStrip strip1 : slist1) {
            for (HelicalTrackStrip strip2 : slist2) {

                //  Try to make a cross - if successful, add it to the collection
                HelicalTrackCross cross = MakeHit(strip1, strip2);
                if (cross != null) crosscol.add(cross);
            }
        }

        //  Done making cross hits
        return crosscol;
    }

    /**
     * Create cross hits from a list of HelicalTrackStrip hits.  Each pair of
     * hits is checked for consistency with forming a cross hit.  Pairs are
     * only formed from strips with the same detector name, layer number, and
     * BarrelEndcapFlag.
     *
     * @param stripcol list of HelicalTrackStrips
     * @return list of HelicalTrackCross hits
     */
    public List<HelicalTrackCross> MakeHits(List<HelicalTrackStrip> stripcol) {

        //  Make a list of the cross hits that are found
        List<HelicalTrackCross> crosscol = new ArrayList<HelicalTrackCross>();

        //  Make sure we have at least 2 strips
        int nstrip = stripcol.size();
        if (nstrip < 2) return crosscol;

        //  Loop over all pairs of strips
        for (int i = 0; i < nstrip - 1; i++)
            for (int j = i + 1; j < nstrip; j++) {
                HelicalTrackStrip strip1 = stripcol.get(i);
                HelicalTrackStrip strip2 = stripcol.get(j);

                //  Check that these strips are in the same detector and layer
                if (strip1.layer() != strip2.layer()) continue;
                if (strip1.BarrelEndcapFlag() != strip2.BarrelEndcapFlag()) continue;
                if (!strip1.detector().equals(strip2.detector())) continue;

                //  Try to make a cross - if successful, add it to the collection
                HelicalTrackCross cross = MakeHit(strip1, strip2);
                if (cross != null) crosscol.add(cross);
            }

        //  Done making crosses
        return crosscol;
    }

    /**
     * Set the maximum separation between sensor planes in the direction normal
     * to the sensor.
     *
     * @param maxsep maximum sensor seperation (units are mm)
     */
    public void setMaxSeparation(double maxsep) {
        _maxsep = maxsep;
    }

    /**
     * Set the tolerance parameter, which is a dimensionless parameter
     * that characterizes the maximum angle between a track and the sensor for
     * high efficiency.  A tolerance near unity will allow hits to be found
     * even for steeply inclined tracks.
     *
     * @param tolerance tolerance parameter
     */
    public void setTolerance(double tolerance) {
        _tolerance = tolerance;
    }

    /**
     * Try to form a cross hit from two HelicalTrackStrips.  Returns null
     * if these two strips don't form a valid cross.
     * 
     * @param strip1 first strip
     * @param strip2 second strip
     * @return cross hit (or null if a valid cross hit cannot be made)
     */
    private HelicalTrackCross MakeHit(HelicalTrackStrip strip1, HelicalTrackStrip strip2) {

        //  Check if we can make a valid hit from this cross
        if (!CheckCross(strip1, strip2)) return null;

        //  Strip pair passes all requirements, make a new cross
        //HelicalTrackCross cross = new HelicalTrackCross(strip1, strip2);
        HelicalTrackCross cross = new HelicalTrackCross();
        if(debug)
            System.out.println("in MakeHit...epsParallel=" + Double.toString(_epsParallel));
        
        cross.setEpsParallel(_epsParallel);
        cross.setEpsStereoAngle(_epsStereoAngle);

        if(debug)
            System.out.println("in MakeHit2...epsParallel=" + Double.toString(cross.getEpsParallel()));
        
        
        cross.init(strip1, strip2);

        // Add any matching MC hits to the cross
        for (MCParticle mcp : strip1.MCParticles()) {
            if (strip2.MCParticles().contains(mcp)) cross.addMCParticle(mcp);
        }

        //  Done making a new cross hit
        return cross;
    }

    /**
     * Check a pair of hits to see if we can make a valid cross hit.  Checks
     * include requiring the sensors to be parallel with a separation less than
     * the maximum separation, that the two strips are not collinear, and
     * that the intersection assuming a normal trajectory gives unmeasured
     * coordinates that are consistent with the strip limits (or if outside
     * the strip limits, are within the allowed tolerance).
     *
     * @param strip1 first strip
     * @param strip2 second strip
     * @return
     */
    private boolean CheckCross(HelicalTrackStrip strip1, HelicalTrackStrip strip2) {
        if(debug)
           System.out.println("in CheckCross...");
        if(debug)
           System.out.println("Strip1 origin : "+strip1.origin().toString()+"; Strip2 origin :"+strip2.origin().toString());
            
        
        //  Check that the strips have the same BarrelEndcap Flag
        if (strip1.BarrelEndcapFlag() != strip2.BarrelEndcapFlag()) return false;
        if(debug)
           System.out.println("in CheckCross...pass BarrelEndcapFlag");
        //  Check that the sensors planes are parallel to each other
       if(debug)
           System.out.println("Strip1 w : "+strip1.w().toString()+"; Strip2 w :"+strip2.w().toString());
        if (Math.abs(VecOp.cross(strip1.w(), strip2.w()).magnitude()) > _epsParallel) { 
            if(debug){
            	System.out.println("Failed!  Planes aren't parallel"+VecOp.cross(strip1.w(), strip2.w()).magnitude());
            }
        	return false;
        }
        if(debug)
           System.out.println("in CheckCross...planes are parallel");
        //  Check that the strips aren't colinear
        double salpha = VecOp.dot(strip1.v(), strip2.u());
        if (Math.abs(salpha) < _epsStereoAngle) return false;
        if(debug)
           System.out.println("in CheckCross...strips aren't colinear");
        //  Locate the center of the hit strips and the difference in these positions
        Hep3Vector p1 = VecOp.add(strip1.origin(), VecOp.mult(strip1.umeas(), strip1.u()));
        Hep3Vector p2 = VecOp.add(strip2.origin(), VecOp.mult(strip2.umeas(), strip2.u()));
        Hep3Vector dp = VecOp.sub(p1, p2);

        //  Check that the sensor separation meets requirements
        double separation = Math.abs(VecOp.dot(dp, strip1.w()));
        if (separation > _maxsep) return false;
          if(debug)
           System.out.println("in CheckCross...strips pass maxsep test");
        //  Check if we can form a cross within tolerances
        double mytolerance = _tolerance;
        if (Math.abs(salpha) > 0.99) mytolerance = 0.01;  //strips are almost perp...don't allow much tolerance
        double v1 = VecOp.dot(dp, strip2.u()) / salpha;
        double vtol = Math.abs(separation * mytolerance / salpha);
        if (v1 > strip1.vmax() + vtol) return false;
        if (v1 < strip1.vmin() - vtol) return false;
          if(debug)
           System.out.println("in CheckCross...strip1 within tolerence");
        double v2 = VecOp.dot(dp, strip1.u()) / salpha;
        if (v2 > strip2.vmax() + vtol) return false;
        if (v2 < strip2.vmin() - vtol) return false;
         if(debug)
           System.out.println("in CheckCross...strip2 within tolerence");
        //  Passed all tests - OK to make stereo hit from this strip pair
        return true;
    }

    public void setEpsParallel(double _epsParallel) {
        this._epsParallel = _epsParallel;
    }

    public void setEpsStereoAngle(double _epsStereoAngle) {
        this._epsStereoAngle = _epsStereoAngle;
    }
    
}
