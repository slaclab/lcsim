/*
 * HelixFitter.java
 *
 * Created on January 22, 2008, 9:25 AM
 *
 */

package org.lcsim.recon.tracking.seedtracker;

import java.util.List;
import java.util.Map;

import org.lcsim.fit.circle.CircleFit;
import org.lcsim.fit.helicaltrack.*;
import org.lcsim.fit.helicaltrack.HelicalTrackFitter.FitStatus;
import org.lcsim.fit.line.SlopeInterceptLineFit;
import org.lcsim.recon.tracking.seedtracker.diagnostic.ISeedTrackerDiagnostics;
import org.lcsim.fit.zsegment.ZSegmentFit;

/**
 *
 * @author Richard Partridge
 * @version 1.0
 */
public class HelixFitter {
    private HelicalTrackFitter _fitter = new HelicalTrackFitter();
    protected MultipleScattering _scattering;
    private HelicalTrackFit _helix;
    private MaterialManager _materialmanager;
    private ConstrainHelix _constrain;
    private double _bfield = 0.;
    private CircleFit _circlefit;
    private SlopeInterceptLineFit _linefit;
    private ZSegmentFit _zsegmentfit;
    private FitStatus _status;
    private ISeedTrackerDiagnostics _diag = null;
    TrackCheck _trackCheck; // set by SeedTracker
    private boolean _debug = false;

    /**
     * Creates a new instance of HelixFitter
     */
    public HelixFitter(MaterialManager materialmanager) {
        _materialmanager = materialmanager;
        _scattering = new MultipleScattering(_materialmanager);
        _constrain = new ConstrainHelix();
    }
    
    public boolean FitCandidate(SeedCandidate seed, SeedStrategy strategy) {
        if(_debug) System.out.printf("%s: FitCandidate() with stategy: \"%s\"\n",this.getClass().getSimpleName(),strategy.getName());
        
        //  Initialize fit results to null objects
        _helix = null;
        
        //  Check that we have set the magnetic field
        if (_bfield == 0.) throw new RuntimeException("B Field must be set before calling the Helix fitter");
        
        //  Set the tolerance in the fitter
        _fitter.setTolerance(Math.sqrt(strategy.getMaxChisq()));
        
        //  Retrieve list of hits to be fit
        List<HelicalTrackHit> hitlist = seed.getHits();
        
        if(_debug) {
            System.out.println(this.getClass().getSimpleName()+": hitlist size " + hitlist.size() + ":");
            double z_prev = -99999999.9;
            for (HelicalTrackHit hit : hitlist) {
                System.out.printf("%s: (%.2f,%.2f,%.2f) corrected  %s\n",
                                    this.getClass().getSimpleName(),hit.getPosition()[0],hit.getPosition()[1],hit.getPosition()[2],
                                    hit.getCorrectedPosition().toString());
                if(z_prev<-99999999.0) z_prev=hit.getPosition()[2];
                else {
                    if(Math.signum(z_prev)!=Math.signum(hit.getPosition()[2])) {
                        System.out.printf("%s: topBotHits in event\n",this.getClass().getSimpleName());
                    }
                    z_prev = hit.getPosition()[2];
                }
            }
        }
        //  Retrieve the old helix
        HelicalTrackFit oldhelix = seed.getHelix();
        
        //  If this is the candidate's first helix fit, first do a fit without MS errors
        if (oldhelix == null) {
     
            if(_debug) 
                System.out.println(this.getClass().getSimpleName()+": no old helix do the fit without MS scattering map" );
            
            //  Reset the stereo hit positions to their nominal value
            for (HelicalTrackHit hit : hitlist) {
                if (hit instanceof HelicalTrackCross) ((HelicalTrackCross) hit).resetTrackDirection();
            }
            //  Do the fit
            _status = _fitter.fit(hitlist);
            SaveFit();
            
            //  Check for unrecoverable fit errors and call appropriate diagnostic
            if (_status != FitStatus.Success) {
                if(_diag!=null) _diag.fireHelixFitFailed(seed, _status, true);
                return false;
            }
            
            //  Retrieve the helix parameters from this fit and save them in the seed
            oldhelix = _fitter.getFit();
            seed.setHelix(oldhelix);

            if(_debug) System.out.printf("%s: fit succeeded, will be used as seed, with chi2=%.3f and helix:\n%s \n",this.getClass().getSimpleName(),oldhelix.chisqtot(),oldhelix.toString());

            //  Calculate the multiple scattering angles for this helix
            try {
               seed.setScatterAngles(_scattering.FindScatters(oldhelix)); 
            } catch (Exception e) {
               System.err.println(e);
               if(_debug)
               {
                   e.printStackTrace();
               }
               return false;
            }
            
            if(_debug) {
                System.out.printf("%s: after calculating the MS map it has %d size:\n",this.getClass().getSimpleName(),seed.getMSMap().size());
                for(Map.Entry<HelicalTrackHit, MultipleScatter> ms : seed.getMSMap().entrySet()) {
                    System.out.printf("%s: Hit at layer %d and position %s has MS drpdhi=%f and dz=%f\n",
                                    this.getClass().getSimpleName(),ms.getKey().Layer(),ms.getKey().getCorrectedPosition().toString(),ms.getValue().drphi(),ms.getValue().dz());
                }
            }
            
        }
        
        if(_debug) 
            System.out.printf("%s: update the stereo hit positions with the old helix:\n%s \n",this.getClass().getSimpleName(),oldhelix.toString());
        
        //  Update the stereo hit positions and covariance matrices
        CorrectStereoHits(hitlist, oldhelix);
        
        //  Do a helix fit including MS errors
        if(_debug) {
            System.out.printf("%s: do the helix fit including MS map this time: \n",this.getClass().getSimpleName());
            for(Map.Entry<HelicalTrackHit, MultipleScatter> ms : seed.getMSMap().entrySet()) {
                    System.out.printf("%s: Hit at layer %d and position %s has MS drpdhi=%f and dz=%f\n",
                                    this.getClass().getSimpleName(),ms.getKey().Layer(),ms.getKey().getCorrectedPosition().toString(),ms.getValue().drphi(),ms.getValue().dz());
            }
        }
        
        _status = _fitter.fit(hitlist, seed.getMSMap(), oldhelix);
        SaveFit();
        
        //  Check for unrecoverable fit errors and call appropriate diagnostic
        if (_status != FitStatus.Success) {
            if(_diag!=null) _diag.fireHelixFitFailed(seed, _status, false);
            return false;
        }
        
        //  Retrieve and save the new helix fit
        _helix = _fitter.getFit();
        seed.setHelix(_helix);
         if ((_trackCheck != null) && (! _trackCheck.checkSeed(seed))) {
            return false;
         }

        //  Set the non-holonomic constraint chi square
        _constrain.setConstraintChisq(strategy, _helix, hitlist);
        
        //  If the total chi square is below the cut, we have a successful fit
        boolean success = _helix.chisqtot() <= strategy.getMaxChisq();
        if (!success)
            if (_diag != null) _diag.fireFailedChisqCut(seed);

        //  If fit was successful, set the new multiple scattering angles
        if (success) {
                
            seed.setScatterAngles(_scattering.FindScatters(_helix));
            if(_debug) {
                System.out.printf("%s: this fit was successful, chi2=%f with resulting helix paramaters:\n%s\n",this.getClass().getSimpleName(),_helix.chisqtot(),_helix.toString());
                System.out.printf("%s: updated MS map before returning from fitCandidate():\n",this.getClass().getSimpleName());       
                for(Map.Entry<HelicalTrackHit, MultipleScatter> ms : seed.getMSMap().entrySet()) {
                    System.out.printf("%s: Hit at layer %d and position %s has MS drpdhi=%f and dz=%f\n",
                                    this.getClass().getSimpleName(),ms.getKey().Layer(),ms.getKey().getCorrectedPosition().toString(),ms.getValue().drphi(),ms.getValue().dz());
                }
            }
        } else {
            if(_debug) 
                System.out.printf("%s: this fit with chi2=%f failed!\n",this.getClass().getSimpleName(),_helix.chisqtot());
        }
        
        return success;
    }
    
    public void setDiagnostics(ISeedTrackerDiagnostics d) {
        _diag = d;
        return;
    }
    
    public HelicalTrackFit getHelix() {
        return _helix;
    }
    
    public FitStatus getFitStatus() {
        return _status;
    }
    
    public CircleFit getCircleFit() {
        return _circlefit;
    }
    
    public SlopeInterceptLineFit getLineFit() {
        return _linefit;
    }
    
    public ZSegmentFit getZSegmentFit() {
        return _zsegmentfit;
    }
    
    public void setBField(double bfield) {
        _bfield = bfield;
        _scattering.setBField(_bfield);
        _constrain.setBField(_bfield);
        return;
    }

     public void setReferencePoint(double x,double y) {
        _fitter.setReferencePoint(x, y);
        return;
    }

    private void SaveFit() {
        
        //  Default to no fit results when circle fit fails
        _circlefit = null;
        _linefit = null;
        _zsegmentfit = null;
        
        //  Save the circle fit results if they exist
        if (_status == FitStatus.CircleFitFailed) return;
        _circlefit = _fitter.getCircleFit();
        
        //  If we have a circle fit, try to save the line fit / zsegment fit results
        if (_status == FitStatus.InconsistentSeed) return;
        if (_status == FitStatus.LineFitFailed) return;
        if (_status == FitStatus.ZSegmentFitFailed) return;
        _linefit = _fitter.getLineFit();
        _zsegmentfit = _fitter.getZSegmentFit();
        
        return;
    }
    
    private void CorrectStereoHits(List<HelicalTrackHit> hitlist, HelicalTrackFit helix) {
        
        //  Get the PathMap - used to find the track direction at the hit location
        Map<HelicalTrackHit, Double> pathmap = helix.PathMap();
        
        //  Loop over the hits and look for stereo hits
        for (HelicalTrackHit hit : hitlist) {
            if (hit instanceof HelicalTrackCross) {
                
                //  Found a stereo hit - calculate the track direction and pass it to the hit
                ((HelicalTrackCross) hit).setTrackDirection(helix);
            }
        }
        return;
    }

    public void setDebug(boolean debug) {
        this._debug = debug;
    }
 }
