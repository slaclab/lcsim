package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import hep.io.sio.SIORef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.lcsim.event.Track;
import org.lcsim.event.TrackState;
import org.lcsim.event.TrackerHit;
import org.lcsim.event.base.BaseTrack;
import org.lcsim.event.base.BaseTrackState;

/**
 * 
 * @author Tony Johnson
 * @author Jeremy McCormick
 * @version $Id: SIOTrack.java,v 1.6 2012/06/18 23:02:14 jeremy Exp $
 */
class SIOTrack extends BaseTrack
{
    private List<SIORef> tempHits;
    private List<SIORef> tempTracks;

    SIOTrack(SIOInputStream in, int flag, int version, double bField) throws IOException
    {
        _type = in.readInt();

        // read TrackStates
        int nTrackStates = 1; // set to 1 per default for backwards compatibility

        if (version>=2000)
        {
            nTrackStates = in.readInt();
        }

        for (int i = 0; i<nTrackStates; i++)
        {
            // TODO put this code into SIOTrackState.java ?
            BaseTrackState ts = new BaseTrackState();

            if (version>=2000)
            {
                ts.setLocation(in.readInt());
            }

            ts.setD0(in.readFloat());
            ts.setPhi(in.readFloat());
            ts.setOmega(in.readFloat());
            ts.setZ0(in.readFloat());
            ts.setTanLambda(in.readFloat());
            
            // Compute the momentum while we have access to the B-field.
            ts.computeMomentum(bField);
            
            double[] covMatrix = new double[15]; // FIXME hardcoded 15
            for (int j = 0; j<covMatrix.length; j++)
                covMatrix[j] = in.readFloat();
            ts.setCovMatrix(covMatrix);
            double[] referencePoint = new double[3]; // FIXME hardcoded 3
            for (int j = 0; j<referencePoint.length; j++)
                referencePoint[j] = in.readFloat();
            ts.setReferencePoint(referencePoint);

            getTrackStates().add(ts);
        }

        _chi2 = in.readFloat();
        _ndf = in.readInt();
        _dEdx = in.readFloat();
        _dEdxErr = in.readFloat();
        _innermostHitRadius = in.readFloat();
        
        int nHitNumbers = in.readInt();
        _subdetId = new int[nHitNumbers];
        for (int i = 0; i<nHitNumbers; i++)
        {
            _subdetId[i] = in.readInt();
        }
        
        int nTracks = in.readInt();
        tempTracks = new ArrayList(nTracks);
        _tracks = null;
        for (int i = 0; i<nTracks; i++)
        {
            tempTracks.add(in.readPntr());
        }
        
        if (LCIOUtil.bitTest(flag, LCIOConstants.TRBIT_HITS))
        {
            int nHits = in.readInt();
            tempHits = new ArrayList(nHits);
            _hits = null;
            for (int i = 0; i<nHits; i++)
            {
                tempHits.add(in.readPntr());
            }
        }

        in.readPTag(this);
    }

    static void write(Track track, SIOOutputStream out, int flag) throws IOException
    {
        out.writeInt(track.getType());

        // write out TrackStates
        List<TrackState> trackstates = track.getTrackStates();
        out.writeInt(trackstates.size());

        for (Iterator it = trackstates.iterator(); it.hasNext();)
        {
            TrackState trackstate = (TrackState) it.next();

            // TODO put this code into SIOTrackState.java ?
            out.writeInt(trackstate.getLocation());
            out.writeFloat((float)trackstate.getD0());
            out.writeFloat((float)trackstate.getPhi());
            out.writeFloat((float)trackstate.getOmega());
            out.writeFloat((float)trackstate.getZ0());
            out.writeFloat((float)trackstate.getTanLambda());
            double[] covMatrix = trackstate.getCovMatrix();
            for (int i = 0; i<covMatrix.length; i++)
                out.writeFloat((float)covMatrix[i]);
            double[] referencePoint = trackstate.getReferencePoint();
            for (int i = 0; i<referencePoint.length; i++)
                out.writeFloat((float)referencePoint[i]);
        }

        out.writeFloat((float) track.getChi2());
        out.writeInt(track.getNDF());
        out.writeFloat((float) track.getdEdx());
        out.writeFloat((float) track.getdEdxError());
        out.writeFloat((float) track.getRadiusOfInnermostHit());
        
        int[] hitNumbers = track.getSubdetectorHitNumbers();
        out.writeInt(hitNumbers.length);
        for (int i = 0; i<hitNumbers.length; i++)
        {
            out.writeInt(hitNumbers[i]);
        }
        
        List<Track> tracks = track.getTracks();
        out.writeInt(tracks.size());
        for (Track t: tracks)
        {
            out.writePntr(t);
        }
        
        if (LCIOUtil.bitTest(flag, LCIOConstants.TRBIT_HITS))
        {
            List<TrackerHit> hits = track.getTrackerHits();
            out.writeInt(hits.size());
            for (TrackerHit hit: hits)
            {
                out.writePntr(hit);
            }
        }
        out.writePTag(track);
    }

    public List<TrackerHit> getTrackerHits()
    {
        if (_hits==null && tempHits!=null)
        {
            _hits = new ArrayList<TrackerHit>(tempHits.size());
            for (SIORef ref: tempHits)
            {
                _hits.add((TrackerHit) ref.getObject());
            }
            tempHits = null;
        }
        return _hits==null ? Collections.<TrackerHit> emptyList() : _hits;
    }

    public List<Track> getTracks()
    {
        if (_tracks==null && tempTracks!=null)
        {
            _tracks = new ArrayList<Track>(tempTracks.size());
            for (SIORef ref: tempTracks)
            {
                _tracks.add((Track) ref.getObject());
            }
            tempTracks = null;
        }
        return _tracks==null ? Collections.<Track> emptyList() : _tracks;
    }
}