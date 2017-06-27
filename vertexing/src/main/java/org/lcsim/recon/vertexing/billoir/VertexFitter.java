/*
 * VertexFitter.java
 *
 * Created on March 25, 2006, 4:54 PM
 *
 * @version $Id: VertexFitter.java,v 1.3 2006/05/22 07:56:43 jstrube Exp $
 */

package org.lcsim.recon.vertexing.billoir;

import java.util.List;
import org.lcsim.event.Track;
import org.lcsim.spacegeom.SpacePoint;

/**
 *
 * @author jstrube
 */
public interface VertexFitter {
    // better have an enumset of possible constraints
    // or better even some kind of map for the constraints
    public Vertex fit(List<Track> tracks, SpacePoint initialPosition, boolean withBeamConstraint);
}
