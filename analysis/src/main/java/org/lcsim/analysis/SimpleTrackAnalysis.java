package org.lcsim.analysis;

import hep.aida.IHistogram1D;

import org.lcsim.event.EventHeader;
import org.lcsim.event.Track;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;

/**
 * This is a very simple analysis driver used for grid production tests on LCIO data.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: SimpleTrackAnalysis.java,v 1.2 2013/04/26 22:44:38 jeremy Exp $
 */
public class SimpleTrackAnalysis extends Driver {
	AIDA aida = AIDA.defaultInstance();
	IHistogram1D h = aida.histogram1D("Track Momentum", 100, 0., 100.);
	public void process(EventHeader event) {
		for (Track track : event.get(Track.class, "Tracks")) {
			double[] p = track.getTrackStates().get(0).getMomentum();
			h.fill(computeMomentum(p));
		}
	}	
	private double computeMomentum(double[] p) {
		return Math.sqrt(p[0]*p[0]+p[1]*p[1]+p[2]*p[2]);
	}
}