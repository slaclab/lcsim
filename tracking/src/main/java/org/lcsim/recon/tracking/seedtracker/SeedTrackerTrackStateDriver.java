package org.lcsim.recon.tracking.seedtracker;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.event.EventHeader;
import org.lcsim.event.GenericObject;
import org.lcsim.event.Track;
import org.lcsim.event.base.BaseTrack;
import org.lcsim.event.base.BaseTrackState;
import org.lcsim.fit.helicaltrack.HelicalTrackCross;
import org.lcsim.fit.helicaltrack.HelicalTrackFit;
import org.lcsim.fit.helicaltrack.HelicalTrackHit;
import org.lcsim.fit.helicaltrack.HelixUtils;
import org.lcsim.fit.helicaltrack.TrackDirection;
import org.lcsim.geometry.Calorimeter;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.Calorimeter.CalorimeterType;
import org.lcsim.spacegeom.SpacePoint;
import org.lcsim.spacegeom.SpaceVector;
import org.lcsim.util.Driver;
import org.lcsim.util.swim.HelixSwimmer;

/**
 * This Driver creates the three TrackState collections expected by slicPandora
 * by using information from the SeedTracker objects. It must be run in the same
 * job as SeedTracker, NOT by reading in a persisted LCIO file, which will not
 * work due to lost data and type information. And it must be run the
 * SeedTracker Driver.
 * 
 * @author Rich Partridge
 * @author Jeremy McCormick
 * @author Norman Graf
 * 
 * @version $Id: SeedTrackerTrackStateDriver.java,v 1.9 2012/06/18 23:02:14 jeremy Exp $
 */
public class SeedTrackerTrackStateDriver extends Driver
{
    // protected CalorimeterInformation calInfo;

    protected double ecalRadius;
    protected int ecalNumSides;
    protected double ecalZ;
    protected double magField;
    protected HelixSwimmer swimmer;

    public SeedTrackerTrackStateDriver()
    {
    }

    protected void detectorChanged(Detector detector)
    {
        // get the magnetic field and set up the helix swimmer
        magField = detector.getFieldMap().getField(new BasicHep3Vector(0, 0, 0)).magnitude();
        swimmer = new HelixSwimmer(magField);

        // Get calorimeter parameters from Detector.
        Calorimeter ecalBarrel = null;
        Calorimeter ecalEndcap = null;

        // Get the EM Barrel.
        ecalBarrel = detector.getCalorimeterByType(CalorimeterType.EM_BARREL);
        if (ecalBarrel == null)
            throw new RuntimeException("Missing EM_BARREL subdetector in compact description.");

        // Get the EM Endcap.
        ecalEndcap = detector.getCalorimeterByType(CalorimeterType.EM_ENDCAP);
        if (ecalEndcap == null)
            throw new RuntimeException("Missing EM_ENDCAP subdetector in compact description.");

        ecalRadius = ecalBarrel.getInnerRadius();
        ecalNumSides = ecalBarrel.getNumberOfSides();
        ecalZ = ecalEndcap.getInnerZ();

        //System.out.println( "ECAL: radius " + ecalRadius + " nsides " + ecalNumSides + " z " + ecalZ );
    }

    public void process(EventHeader event)
    {
        // Get the Tracks from the event. Collection name is SeedTracker's default.
        List<Track> tracks = event.get(Track.class, "Tracks");

        // TrackState lists.
        List<GenericObject> startObjs = new ArrayList<GenericObject>();
        List<GenericObject> endObjs = new ArrayList<GenericObject>();
        List<GenericObject> ecalObjs = new ArrayList<GenericObject>();

        // Loop over all the Tracks in the collection.
        for (Track track : tracks)
        {
            SeedTrack strk = (SeedTrack) track;
            SeedCandidate scand = strk.getSeedCandidate();
            HelicalTrackFit helix = scand.getHelix();
            Hep3Vector ptrk = new BasicHep3Vector(strk.getMomentum());
            double smax = -9999.;
            HelicalTrackHit last = null;
            for (HelicalTrackHit hit : scand.getHits())
            {
                double s = helix.PathMap().get(hit);
                if (s > smax)
                {
                    smax = s;
                    last = hit;
                }
            }
            TrackDirection trkdir = HelixUtils.CalculateTrackDirection(helix, smax);
            Hep3Vector dir = trkdir.Direction();
            Hep3Vector pos = null;
            if (last instanceof HelicalTrackCross)
            {
                HelicalTrackCross cross = (HelicalTrackCross) last;
                cross.setTrackDirection(trkdir, helix.covariance());
                pos = cross.getCorrectedPosition();
            }
            else
            {
                pos = last.getCorrectedPosition();
            }

            // Set the End TrackState from the outermost hit on the track.
            Hep3Vector xmomentum = VecOp.mult(ptrk.magnitude(), dir);

            // Set the End TrackState to the point of closest approach to the last hit
            swimmer.setTrack(track);
            double s = swimmer.getDistanceToPoint(pos);
            Hep3Vector corPos = swimmer.getPointAtLength(s);

            TrackState endState = new TrackState(corPos, xmomentum);
            endObjs.add(endState);

            // Calculate the Start state and add to list.
            double[] smom = ptrk.v();
            double[] spos = HelixUtils.PointOnHelix(helix, 0.).v();
            
            TrackState startState = new TrackState(spos[0], spos[1], spos[2], smom[0], smom[1], smom[2]);
            startObjs.add(startState);

            // TODO Should set the ECal TrackState by propagating from the outermost hit on the track.
            double sZ = swimmer.getDistanceToZ(ecalZ);
            double sR = swimmer.getDistanceToPolyhedra(ecalRadius, ecalNumSides);
            if (Double.isNaN(sR))
            {
                s = sZ;
            }
            else if (Double.isNaN(sZ))
            {
                s = sR;
            }
            else
            {
                s = Math.min(swimmer.getDistanceToZ(ecalZ), swimmer.getDistanceToPolyhedra(ecalRadius, ecalNumSides));
            }
            SpacePoint ecalPos = swimmer.getPointAtLength(s);
            SpaceVector ecalMom = swimmer.getMomentumAtLength(s);

            TrackState ecalState = new TrackState(ecalPos.x(), ecalPos.y(), ecalPos.z(), ecalMom.x(), ecalMom.y(), ecalMom.z());
            ecalObjs.add(ecalState);

        }

        // Add the lists to the event.
        event.put("StateAtStart", startObjs, GenericObject.class, 0);
        event.put("StateAtECal", ecalObjs, GenericObject.class, 0);
        event.put("StateAtEnd", endObjs, GenericObject.class, 0);
    }
    
    private void setEcalTrackState(Track track)
    {
        SeedTrack strk = (SeedTrack) track;
        SeedCandidate scand = strk.getSeedCandidate();
        HelicalTrackFit helix = scand.getHelix();
        Hep3Vector ptrk = new BasicHep3Vector(strk.getMomentum());
        double smax = -9999.;
        HelicalTrackHit last = null;
        
        for (HelicalTrackHit hit : scand.getHits())
        {
            double s = helix.PathMap().get(hit);
            if (s > smax)
            {
                smax = s;
                last = hit;
            }
        }
        
        TrackDirection trkdir = HelixUtils.CalculateTrackDirection(helix, smax);
        
        Hep3Vector dir = trkdir.Direction();
        Hep3Vector pos = null;
            
        if (last instanceof HelicalTrackCross)    
        {
            HelicalTrackCross cross = (HelicalTrackCross) last;
            cross.setTrackDirection(trkdir, helix.covariance());
            pos = cross.getCorrectedPosition();
        }
        else
        {
            pos = last.getCorrectedPosition();
        }

        // Set the End TrackState from the outermost hit on the track.
        Hep3Vector xmomentum = VecOp.mult(ptrk.magnitude(), dir);

        // Set the End TrackState to the point of closest approach to the last hit.
        swimmer.setTrack(track);
        double s = swimmer.getDistanceToPoint(pos);
        Hep3Vector corPos = swimmer.getPointAtLength(s);

        // FIXME Set AtLastHit LCIO v2 TrackState here.
        TrackState endState = new TrackState(corPos, xmomentum);

        // Don't need start state anymore?????
        // Calculate the Start state and add to list.
        //double[] smom = ptrk.v();
        //double[] spos = HelixUtils.PointOnHelix(helix, 0.).v();
        //TrackState startState = new TrackState(spos[0], spos[1], spos[2], smom[0], smom[1], smom[2]);

        // TODO Should set the ECal TrackState by propagating from the outermost hit on the track.
        double sZ = swimmer.getDistanceToZ(ecalZ);
        double sR = swimmer.getDistanceToPolyhedra(ecalRadius, ecalNumSides);
        if (Double.isNaN(sR))
        {
            s = sZ;
        }
        else if (Double.isNaN(sZ))
        {
            s = sR;
        }
        else
        {
            s = Math.min(swimmer.getDistanceToZ(ecalZ), swimmer.getDistanceToPolyhedra(ecalRadius, ecalNumSides));
            // Should this be changed to...
            // s = Math.min(sZ, sR);   ?????
        }
        SpacePoint ecalPos = swimmer.getPointAtLength(s);
        SpaceVector ecalMom = swimmer.getMomentumAtLength(s);

        // FIXME Set AtCalorimeter state here.
        TrackState ecalState = new TrackState(ecalPos.x(), ecalPos.y(), ecalPos.z(), ecalMom.x(), ecalMom.y(), ecalMom.z());
        // Hmmm...now to recover the track parameters here!
    }
    
    void setTrackStates(Track track)
    {
        // we need to populate track states at the following four
        // canonical locations:
        // AtIP
        // AtFirstHit
        // AtLastHit
        // AtCalorimeter
        //
        List<BaseTrackState> trackStates = new ArrayList<BaseTrackState>();
        SeedTrack strk = (SeedTrack) track;
        SeedCandidate scand = strk.getSeedCandidate();
        HelicalTrackFit helixfit = scand.getHelix();
        
        // AtIP should be where this track is already defined, but double-check anyway
        
        int trackStateLocation = BaseTrackState.AtIP;
        double[] parameters = strk.getTrackParameters();
        double[] covMatrix = strk.getErrorMatrix().asPackedArray(true);
        double[] referencePoint = strk.getReferencePoint();
        
        trackStates.add(new BaseTrackState(parameters, covMatrix, referencePoint, trackStateLocation));
        
        // at first and last hits...
        
        double smax = -9999.;
        double smin = 9999.;
        HelicalTrackHit lastHit = null;
        HelicalTrackHit firstHit = null;
        
        //loop over this hits on this track and get the first and last
        for (HelicalTrackHit hit : scand.getHits())
        {
            double s = helixfit.PathMap().get(hit);
            if (s > smax)
            {
                smax = s;
                lastHit = hit;
            }
            if(s < smin)
            {
                smin = s;
                firstHit = hit;
            }
        }
        
        // AtFirstHit
        trackStateLocation = BaseTrackState.AtFirstHit;    
        referencePoint = HelixUtils.PointOnHelix(helixfit, smin).v();
        // by definition, z0 and d0 are zero at this point
        parameters[BaseTrack.D0] = 0.;
        parameters[BaseTrack.Z0] = 0.;
        // This is the helical track fit
        // There is no energy loss so omega does not change with state location
        parameters[BaseTrack.OMEGA] = strk.getTrackParameter(BaseTrack.OMEGA);
        // There is no multiple scattering, so tan(lambda) does not change
        parameters[BaseTrack.TANLAMBDA] = strk.getTrackParameter(BaseTrack.TANLAMBDA);
        // only phi changes as we progress along the helix
        parameters[BaseTrack.PHI] = helixfit.phi0()-smin/helixfit.R();
        
        // TODO calculate correct covariance matrix at this point...
        double[] emptyCovarianceMatrix = new double[15];
        trackStates.add(new BaseTrackState(parameters, emptyCovarianceMatrix, referencePoint, trackStateLocation));
        
        
        //AtlastHit
        trackStateLocation = BaseTrackState.AtLastHit;    
        referencePoint = HelixUtils.PointOnHelix(helixfit, smax).v();   
        // only phi changes as we progress along the helix
        parameters[BaseTrack.PHI] = helixfit.phi0()-smax/helixfit.R();
        
        // TODO calculate correct covariance matrix at this point...
        trackStates.add(new BaseTrackState(parameters, emptyCovarianceMatrix, referencePoint, trackStateLocation));
        
        
        //AtCalorimeter
        trackStateLocation = BaseTrackState.AtCalorimeter; 
        
        // start at the last hit position and extrapolate to the
        // face of the ECal.
        
        TrackDirection trkdir = HelixUtils.CalculateTrackDirection(helixfit, smax);
        Hep3Vector lastHitPosition = null;
            
        if (lastHit instanceof HelicalTrackCross)    
        {
            HelicalTrackCross cross = (HelicalTrackCross) lastHit;
            cross.setTrackDirection(trkdir, helixfit.covariance());
            lastHitPosition = cross.getCorrectedPosition();
        }
        else
        {
            lastHitPosition = lastHit.getCorrectedPosition();
        }

        // Set the End TrackState to the point of closest approach to the last hit.
        swimmer.setTrack(track);
        double s = swimmer.getDistanceToPoint(lastHitPosition);
        Hep3Vector corPos = swimmer.getPointAtLength(s);

        // Set the ECal TrackState by propagating from the point on the helix
        // closest to the outermost hit on the fitted track.
        double sZ = swimmer.getDistanceToZ(ecalZ);
        double sR = swimmer.getDistanceToPolyhedra(ecalRadius, ecalNumSides);
        if (Double.isNaN(sR))
        {
            s = sZ;
        }
        else if (Double.isNaN(sZ))
        {
            s = sR;
        }
        else
        {
            s = Math.min(swimmer.getDistanceToZ(ecalZ), swimmer.getDistanceToPolyhedra(ecalRadius, ecalNumSides));
            // Should this be changed to...
            // s = Math.min(sZ, sR);   ?????
        }
        referencePoint = HelixUtils.PointOnHelix(helixfit, s).v();   
        // only phi changes as we progress along the helix
        parameters[BaseTrack.PHI] = helixfit.phi0()-s/helixfit.R();
        
        // TODO calculate correct covariance matrix at this point...
        trackStates.add(new BaseTrackState(parameters, emptyCovarianceMatrix, referencePoint, trackStateLocation));
                
    }
}
