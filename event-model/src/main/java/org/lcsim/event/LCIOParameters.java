/**
 * @author jstrube
 * @version $Id: LCIOParameters.java,v 1.1 2007/11/29 02:25:55 jstrube Exp $ 
 */
package org.lcsim.event;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static org.lcsim.constants.Constants.fieldConversion;
import static org.lcsim.event.LCIOParameters.ParameterName.d0;
import static org.lcsim.event.LCIOParameters.ParameterName.omega;
import static org.lcsim.event.LCIOParameters.ParameterName.phi0;
import static org.lcsim.event.LCIOParameters.ParameterName.tanLambda;
import static org.lcsim.event.LCIOParameters.ParameterName.z0;
import hep.physics.vec.Hep3Vector;

import org.lcsim.spacegeom.CartesianPoint;
import org.lcsim.spacegeom.CartesianVector;
import org.lcsim.spacegeom.SpacePoint;
import org.lcsim.spacegeom.SpaceVector;

public class LCIOParameters {
    public enum ParameterName {
        d0, phi0, omega, z0, tanLambda
    }
    
    /**
     * Computes the momentum vector from a given parameter set
     * 
     * @param parameters
     *                The Parameter object
     * @return The momentum vector corresponding to the given parameters
     */
    public static Hep3Vector Parameters2Momentum(LCIOParameters parameters) {
        double pt = parameters.pt;

        return new CartesianVector(pt * cos(parameters.get(phi0)), pt
                * sin(parameters.get(phi0)), pt * parameters.get(tanLambda));
    }

    /**
     * Computes the point of closest approach on the track to the given
     * reference point. Note that this function does not do any swimming. It
     * merely returns a different representation of the given parameters.
     * This is meaningless without the reference point however. In order to
     * prevent the user from having to know the implementation details, the
     * reference point is made an explicit parameter.
     * 
     * @param parameters
     *                The Parameter object
     * @param refPoint
     *                The reference point
     * @return The point of closest approach on the track to the reference
     *         point
     */
    public static SpacePoint Parameters2Position(LCIOParameters parameters,
            SpacePoint refPoint) {
        double d_0 = parameters.get(d0);
        double z_0 = parameters.get(z0);
        double phi_0 = parameters.get(phi0);

        double x = refPoint.x() - d_0 * sin(phi_0);
        double y = refPoint.y() + d_0 * cos(phi_0);
        double z = refPoint.z() + z_0;

        return new CartesianPoint(x, y, z);
    }

    /**
     * Calculates the parameters of the Track under the assumption that the
     * space-momentum representation is given at the POCA to the reference
     * point.
     * 
     * @param pos
     *                The point of closest approach on the track to the
     *                reference point
     * @param mom
     *                The momentum vector at
     * @see pos
     * @param ref
     *                The reference point
     * @param charge
     *                The charge of the particle
     * @param Bz
     *                The z component of the magnetic field. Assuming a
     *                homogeneous field parallel to z
     * @return The Parameter object corresponding to the arguments
     */
    public static LCIOParameters SpaceMomentum2Parameters(SpacePoint pos,
            Hep3Vector p, SpacePoint ref, int charge, double field_z) {
        // Hep3Vector is stupid
        SpaceVector mom = new CartesianVector(p.v());
        LCIOParameters result = new LCIOParameters();
        double aqBz = charge * field_z * fieldConversion;
        double x = pos.x() - ref.x();
        double y = pos.y() - ref.y();
        double z_0 = pos.z() - ref.z();
        double phi_0 = mom.phi();
        double pt = mom.rxy();

        result.set(d0, -x * sin(phi_0) + y * cos(phi_0));
        result.set(phi0, phi_0);
        result.set(omega, aqBz / pt);
        result.set(z0, z_0);
        result.set(tanLambda, mom.z() / pt);
        result.setPt(pt);
        return result;
    }

    double[] values;
    double pt;

    public LCIOParameters(LCIOParameters parameters) {
        values = parameters.values.clone();
        pt = parameters.pt;
    }

    LCIOParameters() {
        values = new double[5];
        pt = 0;
    }

    public LCIOParameters(double[] vals, double p_t) {
        values = vals;
        pt = p_t;
    }

    public double get(ParameterName name) {
        return values[name.ordinal()];
    }

    public double getPt() {
        return pt;
    }

    public double[] getValues() {
        return values;
    }

    public void set(ParameterName name, double val) {
        values[name.ordinal()] = val;
    }

    void setPt(double p_t) {
        pt = p_t;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Parameters:\n"));
        for (ParameterName p : ParameterName.values()) {
            sb.append(String.format("%10s: %g\n", p.name(), values[p
                    .ordinal()]));
        }
        sb.append(String.format("%10s: %g\n", "pt", pt));
        return sb.toString();
    }
}