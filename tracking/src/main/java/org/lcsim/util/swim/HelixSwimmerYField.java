package org.lcsim.util.swim;

import static java.lang.Math.atan;
import static java.lang.Math.sqrt;
import static org.lcsim.event.LCIOParameters.ParameterName.omega;
import static org.lcsim.event.LCIOParameters.ParameterName.phi0;
import static org.lcsim.event.LCIOParameters.ParameterName.tanLambda;

import org.lcsim.event.LCIOParameters;
import org.lcsim.event.Track;
import org.lcsim.spacegeom.CartesianPoint;
import org.lcsim.spacegeom.CartesianVector;
import org.lcsim.spacegeom.SpacePoint;

/**
 * Swim Track assuming B-field in Y direction.
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class HelixSwimmerYField extends HelixSwimmer {
    
    public HelixSwimmerYField(double B) {
        super(B);
    }
    
    public void setTrack(Track t) {
        double pt = sqrt(t.getPX() * t.getPX() + t.getPY() * t.getPY());
        LCIOParameters parameters = new LCIOParameters(t.getTrackParameters(), pt);
        SpacePoint ref = new CartesianPoint(t.getReferencePoint());
        SpacePoint origin = LCIOParameters.Parameters2Position(parameters, ref);
        _trajectory = new HelixYField(origin, 1 / parameters.get(omega), parameters
                .get(phi0), atan(parameters.get(tanLambda)));
        _momentum = new CartesianVector(LCIOParameters.Parameters2Momentum(parameters).v());
    }        
}
