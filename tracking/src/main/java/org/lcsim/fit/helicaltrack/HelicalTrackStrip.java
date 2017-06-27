/*
 * HelicalTrackStrip.java
 *
 * Created on May 1, 2008, 10:06 AM
 *
 */

package org.lcsim.fit.helicaltrack;

import java.util.ArrayList;
import java.util.List;

import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

import org.lcsim.event.MCParticle;
import org.lcsim.event.TrackerHit;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;

/**
 * Encapsulate strip information used to construct HelicalTrack2DHit and
 * HelicalTrackCross hits.  The hit position is given by:
 *
 * pos = origin + umeas * uhat + v * vhat
 *
 * where v is the unmeasured coordinate along the strip direction.
 * @author Richard Partridge
 * @version 1.0
 */
public class HelicalTrackStrip {
    private Hep3Vector _origin;
    protected Hep3Vector _u;
    protected Hep3Vector _v;
    protected Hep3Vector _w;
    private double _umeas;
    private double _du;
    private double _vmin;
    private double _vmax;
    private double _dEdx;
    private double _time;
    private List _rawhits;
    private String _detector;
    private int _layer;
    private BarrelEndcapFlag _beflag;
    private List<MCParticle> _mcplist;
    private double _eps = 1.0e-6;
    
    /**
     * Creates a new instance of HelicalTrackStripHit.
     * @param origin global position of local coordinate origin
     * @param u vector parallel to the measurement direction
     * @param v vector parallel to the strip direction
     * @param umeas measured coordinate
     * @param du uncertainty in the measured coordinate
     * @param vmin minimum value of the unmeasured coordinate
     * @param vmax maximum value of the unmeasured coordinate
     */
    public HelicalTrackStrip(Hep3Vector origin, Hep3Vector u, Hep3Vector v, double umeas, double du,
            double vmin, double vmax, double dEdx, double time, List rawhits, String detector,
            int layer, BarrelEndcapFlag beflag) {
        _origin = origin;
        _u = VecOp.unit(u);
        _v = VecOp.unit(v);
        _umeas = umeas;
        _du = du;
        _vmin = vmin;
        _vmax = vmax;
        _dEdx = dEdx;
        _time = time;
        _rawhits = rawhits;
        _detector = detector;
        _layer = layer;
        _beflag = beflag;
        _mcplist = new ArrayList<MCParticle>();
        
        //  Check if the origin is located at the center of the strip
        double vmiddle = 0.5 * (_vmin + _vmax);
        if (Math.abs(vmiddle) > _eps) {
            //  Relocate the origin to be at the center of the strip
            _origin = VecOp.add(origin, VecOp.mult(vmiddle, _v));
            _vmin -= vmiddle;
            _vmax -= vmiddle;
        }
        
        if(Math.abs(_vmin+_vmax) > 2*_eps)
            throw new RuntimeException("_vmin != -_vmax - vmin: "+vmin+" vmax: "+vmax);
                
        //  Make sure that vmin < vmax
        if (_vmin > _vmax) {
            double vtemp = _vmin;
            _vmin = _vmax;
            _vmax = vtemp;
        }

	//Find the sensor normal        
        initW();

    }
    
    protected void initW() {
        //  Find the sensor normal and make sure it is pointing in the "outgoing" direction
        _w = VecOp.cross(_u, _v);
        if (VecOp.dot(_w, _origin) < 0) {
            _v = VecOp.mult(-1., _v);
            _w = VecOp.cross(_u, _v);
        }
    }
    
    /**
     * Return the global position of the origin for the local strip coordinates.
     * @return origin of the local coordinates
     */
    public Hep3Vector origin() {
        return _origin;
    }
    
    /**
     * Return a unit vector parallel to the measurement direction for the strip.
     * @return measured direction unit vector
     */
    public Hep3Vector u() {
        return _u;
    }
    
    /**
     * Return a unit vector that is parallel to the strip.
     * @return unmeasured direction unit vector
     */
    public Hep3Vector v() {
        return _v;
    }
    
    /**
     * Return a unit vector that is normal to the sensor plane (u x v = w).
     * @return sensor normal unit vector
     */
    public Hep3Vector w() {
        return _w;
    }
    
    /**
     * Return the measured coordinate.
     * @return measured coordinate
     */
    public double umeas() {
        return _umeas;
    }
    
    /**
     * Return the uncertainty in the measured coordinate.
     * @return uncertainty in the measured coordinate
     */
    public double du() {
        return _du;
    }
    
    /**
     * Return the minimum value for the unmeasured coordinate.
     * @return minimum of the unmeasured coordinate
     */
    public double vmin() {
        return _vmin;
    }
    
    /**
     * Return the maximum value for the unmeasured coordinate.
     * @return maximum of the measured coordinate
     */
    public double vmax() {
        return _vmax;
    }
    
    public double dEdx() {
        return _dEdx;
    }
    
    public double time() {
        return _time;
    }
    
    public List rawhits() {
        return _rawhits;
    }
    
    public String detector() {
        return _detector;
    }
    
    public int layer() {
        return _layer;
    }
    
    public BarrelEndcapFlag BarrelEndcapFlag() {
        return _beflag;
    }
    public void addMCParticle(MCParticle mcp) {
        if (!_mcplist.contains(mcp)) _mcplist.add(mcp);
        return;
    }
    
    public List<MCParticle> MCParticles() {
        return _mcplist;
    }
    
    public String toString(){
        return ("Strip with u="+this.u().toString()+"\n v="+this.v().toString() + "\n vmin="+this.vmin() + "\n vmax="+this.vmax() + "\n umeas="+this.umeas()+"\n origin="+this.origin().toString()); 
    }
}
