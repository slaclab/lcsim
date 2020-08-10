/*
 * HelicalTrackHit.java
 *
 * Created on November 13, 2007, 12:18 PM
 *
 */

package org.lcsim.fit.helicaltrack;

import java.util.ArrayList;
import java.util.List;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;

import org.lcsim.event.MCParticle;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.event.TrackerHit;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;

import org.apache.commons.math.util.FastMath;

/**
 * Encapsulate the hit information needed by HelicalTrackFitter.
 *
 * To account for situations where the best estimate of the hit position and
 * covariance matrix are subject to corrections, both nominal and corrected
 * values of these quantities are stored.  The nominal and corrected values are
 * initially identical, but the protected methods setCorrectedPosition and
 * setCorrectedCovMatrix can be used to update the corrected values.  Currently,
 * this feature is only used for stereo hits, where the hit position and covariance
 * matrix depend on the track direction.
 * 
 * @author Richard Partridge
 * @version $Id$
 * 
 */
public class HelicalTrackHit implements Comparable, TrackerHit {
    
    private double[] _pos;
    private Hep3Vector _poscor;
    private SymmetricMatrix _cov;
    private SymmetricMatrix _covcor;
    private double _dEdx;
    private double _time;
    private int _type;
    private List<RawTrackerHit> _rawhits;
    private String _detname;
    private int _layer;
    private BarrelEndcapFlag _beflag;
    private List<MCParticle> _mcplist;
    private double _r;
    private double _phi;
    private double _drphi;
    private double _dr;
    private double _chisq;
    protected double _eps = 1e-2;
    protected double _epsParallel;// = 1e-2;
    protected double _epsStereoAngle = 1e-2;
    protected long id; // FIXME: Dummy value that needs to be set from RawTrackerHit data.

    /**
     * Create a new instance of {@ HelicalTrackHit}
     */
    public HelicalTrackHit() {        
    }

    /**
     * Create a new instance of {@ HelicalTrackHit}
     */
    public HelicalTrackHit(Hep3Vector pos, SymmetricMatrix cov, double dEdx, double time, int type,
            List rawhits, String detname, int layer, BarrelEndcapFlag beflag) {
        
        init(pos, cov, dEdx, time, type, rawhits, detname, layer, beflag);
        
    }
    
    /**
     * Initialize the {@link HelicalTrackHit}.
     */
    public void init(Hep3Vector pos, SymmetricMatrix cov, double dEdx, double time, int type,
            List rawhits, String detname, int layer, BarrelEndcapFlag beflag) {
        _pos = pos.v();
        _poscor = pos;
        _cov = cov;
        _covcor = cov;
        _dEdx = dEdx;
        _time = time;
        _type = type;
        //  If we are passed a list of raw tracker hits use it, otherwise, create a new list
        if (rawhits != null) {
            _rawhits = rawhits;
        }
        else {
            _rawhits = new ArrayList<RawTrackerHit>();
        }
        _detname = detname;
        _layer = layer;
        _beflag = beflag;
        _mcplist = new ArrayList<MCParticle>();
        _chisq = 0.;
        setPolarVariables();
    }
    
    public void setEpsParallel(double _epsParallel) {
        this._epsParallel = _epsParallel;
    }
    
    public double getEpsParallel(){
        return _epsParallel;
    }
    
    public void setEpsStereoAngle(double _epsStereoAngle) {
        this._epsStereoAngle = _epsStereoAngle;
    }
    
    /**
     * Return the corrected x coordinate of the HelicalTrackHit
     * @return x coordinate
     */
    public double x() {
        return _poscor.x();
    }
    
    /**
     * Return the corrected y coordinate of the HelicalTrackHit
     * @return y coordinate
     */
    public double y() {
        return _poscor.y();
    }
    
    /**
     * Return the corrected z coordinate of the HelicalTrackHit
     * @return z coordinate
     */
    public double z() {
        return _poscor.z();
    }
    
    /**
     * Return the corrected radius of the hit (i.e., distance from the z axis)
     * @return radial coordinate
     */
    public double r() {
        return _r;
    }
    
    /**
     * Return the corrected azimuthal coordinate of the hit
     * @return azimuthal coordinate
     */
    public double phi() {
        return _phi;
    }
    
    /**
     * Return the corrected uncertainty in the azimuthal coordinate  r*phi
     * @return uncertainty in r*phi
     */
    public double drphi() {
        return _drphi;
    }
    
    /**
     * Return the corrected uncertainty in the radial coordinate r
     * @return uncertainty in r
     */
    public double dr() {
        return _dr;
    }
    
    /**
     * Return chi^2 penalty for the hit (used by cross hits when one or both of
     * the unmeasured coordinates is beyond its allowed range).
     * @return chi^2 penalty
     */
    public double chisq() {
        return _chisq;
    }
    
    /**
     * Return the BarrelEndcapFlag appropriate for this hit
     * @return BarrelEndcapFlag for this hit
     */
    public BarrelEndcapFlag BarrelEndcapFlag() {
        return _beflag;
    }
    
    /**
     * Implement comparable interface to allow hits to be sorted  by their corrected
     * z coordinate
     * @param hit2 HelicalTrackHit to be compared against this instance
     * @return 1 if the z for this hit is greater than for hit2
     */
    public int compareTo(Object hit2) {
        double zhit = ((HelicalTrackHit) hit2).z();
        if (_poscor.z() < zhit) return -1;
        if (_poscor.z() == zhit) return 0;
        return 1;
    }
    
    /**
     * Associate an MCParticle with this hit.
     * @param mcp MCParticle that is associated with this hit
     */
    public void addMCParticle(MCParticle mcp) {
        if (!_mcplist.contains(mcp)) _mcplist.add(mcp);
        return;
    }
    
    /**
     * Returns a list of MCParticles belonging to this hit.
     * null is returned if no list can be found.
     * @return A list of MCParticles, or null if none can be retrieved.
     */
    public List<MCParticle> getMCParticles(){
        return _mcplist;
    }
    
    /**
     * Return the nominal (uncorrected) hit position.
     * @return nominal hit position
     */
    public double[] getPosition() {
        return _pos;
    }
    
    /**
     * Return the corrected hit position.
     * @return Corrected hit position
     */
    public Hep3Vector getCorrectedPosition() {
        return _poscor;
    }
    
    /**
     * Return the nominal (uncorrected) covariance matrix.
     * @return nominal covariance matrix
     */
    public double[] getCovMatrix() {
        return _cov.asPackedArray(true);
    }
    
    /**
     * Return the corrected covariance matrix.
     * @return corrected covariance matrix
     */
    public SymmetricMatrix getCorrectedCovMatrix() {
        return _covcor;
    }
    
    /**
     * Return the energy deposit for this hit.
     * @return energy deposit
     */
    public double getdEdx() {
        return _dEdx;
    }
    
    /**
     * Return the time for this hit.
     * @return hit time
     */
    public double getTime() {
        return _time;
    }
    
    /**
     * Return the hit type.
     * @return hit type
     */
    public int getType() {
        return _type;
    }
    
    public int getQuality() 
    {
        return 0;
    }
    
    public double getEdepError()
    {
        return 0.;
    }
    
    /**
     * Return a list of raw hits for this hit.
     * @return raw hit list
     */
    public List getRawHits() {
        return _rawhits;
    }
    
    public String Detector() {
        return _detname;
    }
    
    public int Layer() {
        return _layer;
    }
    
    /**
     * Return the layer identifier string.
     * @return layer identifier
     */
    public String getLayerIdentifier() {
        return _detname+_layer+_beflag;
    }
    
    /**
     * Return a string describing the hit.
     * @return hit description
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("HelicalTrackHit: \n");
        sb.append("Layer Identifier= "+getLayerIdentifier()+"\n");
        sb.append("Position= "+_poscor.toString()+"\n");
        sb.append("Covariane= "+_covcor.toString()+"\n");
        sb.append("dE/dx= "+this.getdEdx()+"\n");
        sb.append("Time= "+this.getTime()+"\n");
        return sb.toString();
    }
    
    /**
     * Set the hit position.
     * @param position : hit position
     *  
     */
    public void setPosition(double[] position){
    	_pos = position;
    }

    /**
     * Set the corrected hit position.
     * @param poscor corrected hit position
     */
    protected void setCorrectedPosition(Hep3Vector poscor) {
        _poscor = poscor;
        setPolarVariables();
        return;
    }
    
    /**
     * Set the corrected covariance matrix.
     * @param covcor corrected covariance matrix
     */
    protected void setCorrectedCovMatrix(SymmetricMatrix covcor) {
        _covcor = covcor;
        setPolarVariables();
        return;
    }
    
    protected void addRawHit(RawTrackerHit rawhit) {
        if (!_rawhits.contains(rawhit)) _rawhits.add(rawhit);
        return;
    }
    
    /**
     * Set the chi^2 penalty for the hit (used by cross hits when one or both of
     * the unmeasured coordinates is beyond its allowed range).
     * @param chisq chi^2 penalty
     */
    protected void setChisq(double chisq) {
        _chisq = chisq;
        return;
    }
    
    protected double drphicalc(Hep3Vector pos, SymmetricMatrix cov) {
        double x = pos.x();
        double y = pos.y();
        double r2 = x*x + y*y;
        return Math.sqrt((y*y * cov.e(0,0) + x*x * cov.e(1,1) - 2. * x * y * cov.e(0,1)) / r2);
    }
    
    protected double drcalc(Hep3Vector pos, SymmetricMatrix cov) {
        double x = pos.x();
        double y = pos.y();
        double r2 = x*x + y*y;
        return Math.sqrt((x*x * cov.e(0,0) + y*y * cov.e(1,1) + 2. * x * y * cov.e(0,1)) / r2);
    }
    
    /**
     * Calculate the polar coordinates _r, _phi from the cartesian
     * coordinates _x, _y and cache them in this object since we
     * expect them to be used repeatedly by the track finding code.
     */
    private void setPolarVariables() {
        double x = _poscor.x();
        double y = _poscor.y();
        _r = Math.sqrt(x*x + y*y);
        _phi = FastMath.atan2(y, x);
        if (_phi < 0.) _phi += 2. * Math.PI;
        _drphi = drphicalc(_poscor, _covcor);
        _dr = drcalc(_poscor, _covcor);
        
        //do ! > because that'll handle the NaN case
        if (! (_dr>_eps))
            _dr = _eps;
        
        if (! (_drphi>_eps))
            _drphi = _eps;
        
        return;
    }
    
    public long getCellID()
    {
        return id;
    }
}
