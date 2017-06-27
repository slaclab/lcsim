package org.lcsim.mc.fast.tracking;

import Jama.util.Maths;
import hep.physics.matrix.SymmetricMatrix;
import Jama.Matrix;
import hep.physics.particle.Particle;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;
import org.lcsim.constants.Constants;
import org.lcsim.event.MCParticle;

/**
 * Holds DOCA parameters and error matrix of track. Can be initialized with a MC truth particle. <br>
 *
 * @author Tony Johnson, Wolfgang Walkowiak
 * @version $Id: DocaTrackParameters.java,v 1.9 2007/10/16 18:16:50 cassell Exp $
 */
public class DocaTrackParameters implements TrackParameters {
    private Hep3Vector m_pdoca_ref = null;
    private Hep3Vector m_xdoca_ref = null;
    private Hep3Vector m_xref = null;
    private SymmetricMatrix m_err = new SymmetricMatrix(5);
    private double[] m_parm = new double[5];
    private double m_Bz = 0.;
    private double m_chi2 = -1.;
    private double m_l0 = 0.;
    private double m_l_ref = 0.;
    private int m_ndf = 5;

    // ====================================================
    //
    // Constructors
    //
    // ====================================================
    public DocaTrackParameters(double bField) {
        reset();
        m_Bz = bField;
    }

    public DocaTrackParameters(MCParticle p, double bField) {
        this(bField, p.getMomentum(), p.getOrigin(), p.getCharge());
    }

    public DocaTrackParameters(double bField, Particle p) {
        this(bField, p.getMomentum(), p.getOrigin(), p.getType().getCharge());
    }

    public DocaTrackParameters(double bField, Hep3Vector momentum, Hep3Vector x, double q) {
        reset();
        m_Bz = bField;
        calculateDoca(momentum, x, q);
    }

    public DocaTrackParameters(double bField, double[] momentum, double[] x, double q) {
        reset();
        m_Bz = bField;
        calculateDoca(momentum, x, q);
    }

    public DocaTrackParameters(double bField, double[] momentum, double[] x, double q, SymmetricMatrix errorMatrix) {
        this(bField, momentum, x, q);
        this.m_err = errorMatrix;
    }

    public DocaTrackParameters(double bField, double[] parameters) {
        m_Bz = bField;
        m_parm = parameters;
    }

    public DocaTrackParameters(double bField, double[] parameters, SymmetricMatrix errorMatrix) {
        this(bField, parameters);
        this.m_err = errorMatrix;
    }

    public DocaTrackParameters(double bField, double[] parameters, SymmetricMatrix errorMatrix, double chi2) {
        this(bField, parameters);
        this.m_err = errorMatrix;
        setChi2(chi2);
    }

    public DocaTrackParameters(double bField, double[] parameters, SymmetricMatrix errorMatrix, double chi2, int ndf) {
        this(bField, parameters);
        this.m_err = errorMatrix;
        setChi2(chi2);
        setNDF(ndf);
    }

    public double getD0() {
        return m_parm[0];
    }

    /**
     * Get the error matrix as a 2-D array
     * @see #getTrackParameter
     */
    public SymmetricMatrix getErrorMatrix() {
        return m_err;
    }

    /**
     * Get momentum at DOCA.
     */
    public double[] getMomentum() {
        return new double[] { getPX(), getPY(), getPZ() };
    }

    public double getOmega() {
        return m_parm[2];
    }

    public double getPX() {
        return getPt() * Math.cos(m_parm[1]);
    }

    public double getPY() {
        return getPt() * Math.sin(m_parm[1]);
    }

    public double getPZ() {
        return getPt() * m_parm[4];
    }

    public double getPhi0() {
        return m_parm[1];
    }

    /**
     * Get total momentum at DOCA.
     */
    public double getPtot() {
        return Math.sqrt((getPX() * getPX()) + (getPY() * getPY()) + (getPZ() * getPZ()));
    }

    public double getTanL() {
        return m_parm[4];
    }

    /**
     * Get an individual track parameter. <br>
     *
     * The track parameters for LCD are defined as follows
     * <table>
     * <tr>
     * <th>Index</th>
     * <th>Meaning</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>d0 = XY impact parameter</td>
     * <tr>
     * <tr>
     * <td>1</td>
     * <td>phi0</td>
     * <tr>
     * </td>
     * <tr>
     * <tr>
     * <td>2</td>
     * <td>omega = 1/curv.radius (negative for negative tracks)</td>
     * <tr>
     * <tr>
     * <td>3</td>
     * <td>z0 = z of track (z impact parameter)</td>
     * <tr>
     * <tr>
     * <td>4</td>
     * <td>s = tan lambda</td>
     * <tr>
     * </table>
     * @param i The index of the track parameter
     * @return The track parameter with the specified index
     *
     *         All parameters are given at the DOCA.
     */
    public double getTrackParameter(int i) {
        return m_parm[i];
    }

    /**
     * Get the track parameters as an array
     * @see #getTrackParameter
     */
    public double[] getTrackParameters() {
        return m_parm;
    }

    /**
     * Get the unit charge, ie +1, 0, -1.
     */
    public int getUnitCharge() {
        if (m_Bz != 0) {
            return (int) Math.signum(m_parm[2]);
        } else {
            return 0;
        }
    }

    public double getZ0() {
        return m_parm[3];
    }

    /**
     * Get cos(Theta) as calculated from the momentum vector at the DOCA. <br>
     *
     * Note: This is the same as getCosTheta()
     */

    public double magnitude() {
        return Math.sqrt(VecOp.dot(getDocaVec(), getDocaVec()));
    }

    public double magnitudeSquared() {
        return VecOp.dot(getDocaVec(), getDocaVec());
    }

    public double[] v() {
        return getDoca();
    }

    // IHep3Vector methods
    public double x() {
        return getDocaX();
    }

    public double y() {
        return getDocaY();
    }

    public double z() {
        return getDocaZ();
    }

    /**
     * Store the chi2.
     */
    void setChi2(double chi2) {
        m_chi2 = chi2;
    }

    /**
     * Return the chi2 from smearing. <br>
     *
     * Note: The chi2 is to be calculated and stored by the smearing routine using setChi2().
     */
    double getChi2() {
        return m_chi2;
    }

    /**
     * get cos(theta) at DOCA.
     */
    double getCosTheta() {
        return getPZ() / getPtot();
    }

    /**
     * Get coordinates of DOCA.
     */
    double[] getDoca() {
        return new double[] { getDocaX(), getDocaY(), getDocaZ() };
    }

    double[] getDocaMomentum(double[] refPoint) {
        return getDocaMomentumVec(refPoint).v();
    }

    /**
     * Calculate and get Doca momentum on track with respect to any space point.
     */
    Hep3Vector getDocaMomentumVec(Hep3Vector refPoint) {
        if ((refPoint.x() != 0.) || (refPoint.y() != 0.)) {
            checkCalcDoca(refPoint);

            return m_pdoca_ref;
        } else {
            return getMomentumVec();
        }
    }

    Hep3Vector getDocaMomentumVec(double[] refPoint) {
        return getDocaMomentumVec(new BasicHep3Vector(refPoint[0], refPoint[1], refPoint[2]));
    }

    double[] getDocaPosition(double[] refPoint) {
        return getDocaPositionVec(refPoint).v();
    }

    // ====================================================
    //
    // methods
    //
    // ====================================================

    /**
     * Calculate and get Doca position on track with respect to any space point.
     */
    Hep3Vector getDocaPositionVec(Hep3Vector refPoint) {
        if ((refPoint.x() != 0.) || (refPoint.y() != 0.)) {
            checkCalcDoca(refPoint);

            return m_xdoca_ref;
        } else {
            return getDocaVec();
        }
    }

    Hep3Vector getDocaPositionVec(double[] refPoint) {
        return getDocaPositionVec(new BasicHep3Vector(refPoint[0], refPoint[1], refPoint[2]));
    }

    /**
     * Calculate and get path length on track for a doca to any space point in respect to the track defining doca (with respect to the origin). The length l is given in the transverse plane. <br>
     * Use L = l*tan(lambda) to convert.
     */
    double getDocaTransversePathLength(Hep3Vector refPoint) {
        if ((refPoint.x() != 0.) || (refPoint.y() != 0)) {
            checkCalcDoca(refPoint);

            return m_l_ref;
        } else {
            return 0.;
        }
    }

    double getDocaTransversePathLength(double[] refPoint) {
        return getDocaTransversePathLength(new BasicHep3Vector(refPoint[0], refPoint[1], refPoint[2]));
    }

    /**
     * Get coordinates of DOCA.
     */
    Hep3Vector getDocaVec() {
        return new BasicHep3Vector(getDocaX(), getDocaY(), getDocaZ());
    }

    double getDocaX() {
        return (-m_parm[0] * Math.sin(m_parm[1]));
    }

    double getDocaY() {
        return (m_parm[0] * Math.cos(m_parm[1]));
    }

    double getDocaZ() {
        return (m_parm[3]);
    }

    /**
     * Set the (transverse) path length l0 to original track vertex.
     */
    void setL0(double l0) {
        m_l0 = l0;
    }

    /**
     * Get the (transverse) path length l0 to original track vertex.
     */
    double getL0() {
        return m_l0;
    }

    double[] getMomentum(double l) {
        return getMomentumVec(l).v();
    }

    /**
     * Calculate and get momentum on track with respect to any path length l on track (l in xy plane).
     */
    Hep3Vector getMomentumVec(double l) {
        double phi0 = m_parm[1];
        double omega = m_parm[2];
        double tanl = m_parm[4];

        int iq = getUnitCharge();

        double phi = phi0 + (omega * l);
        double pt = Constants.fieldConversion * iq * m_Bz / omega;

        double px = pt * Math.cos(phi);
        double py = pt * Math.sin(phi);
        double pz = pt * tanl;

        // System.out.println("l: "+l+" p: ("+px+", "+py+", "+pz+")");
        return new BasicHep3Vector(px, py, pz);
    }

    Hep3Vector getMomentumVec() {
        return new BasicHep3Vector(getPX(), getPY(), getPZ());
    }

    /**
     * Change the number degrees of freedom.
     */
    void setNDF(int ndf) {
        m_ndf = ndf;
    }

    /**
     * Get the number degrees of freedom.
     *
     * Default is 5 unless changed with setNDF().
     */
    int getNDF() {
        return m_ndf;
    }

    double[] getPosition(double l) {
        return getPositionVec(l).v();
    }

    /**
     * Calculate and get position on track with respect to any path length l on track (l in xy plane).
     */
    Hep3Vector getPositionVec(double l) {
        double d0 = m_parm[0];
        double phi0 = m_parm[1];
        double omega = m_parm[2];
        double z0 = m_parm[3];
        double tanl = m_parm[4];

        double phi = phi0 + l * omega;
        double rho = 1 / omega;

        double x = (rho * Math.sin(phi)) - ((rho + d0) * Math.sin(phi0));
        double y = (-rho * Math.cos(phi)) + ((rho + d0) * Math.cos(phi0));
        double z = z0 + (l * tanl);

        return new BasicHep3Vector(x, y, z);
    }

    /**
     * Get transverse momentum at DOCA.
     */
    double getPt() {
        if (m_parm[2] != 0.) {
            return Math.abs(Constants.fieldConversion * m_Bz / m_parm[2]);
        } else {
            return 0.;
        }
    }

    /**
     * Get theta angle at DOCA.
     */
    double getTheta() {
        return Math.atan2(getPt(), getPZ());
    }

    /**
     * Calculate the error matrix for the momentum for a point on the track specified by l. Result is given as a 3x3 array for the matrix.
     */
    SymmetricMatrix calcMomentumErrorMatrix(double l) {
        double rho = 1. / getOmega();
        double phi = getPhi0() + (getOmega() * l);
        double tanl = getTanL();
        double c = Constants.fieldConversion * Math.abs(m_Bz);
        double sphi = Math.sin(phi);
        double cphi = Math.cos(phi);

        Matrix tMatrix = new Matrix(5, 3, 0.);
        tMatrix.set(1, 0, -c * rho * sphi);
        tMatrix.set(1, 1, c * rho * cphi);
        tMatrix.set(2, 0, (-c * rho * rho * cphi) - (c * rho * l * sphi));
        tMatrix.set(2, 1, (-c * rho * rho * sphi) + (c * rho * l * cphi));
        tMatrix.set(2, 2, -c * rho * rho * tanl);
        tMatrix.set(4, 2, c * rho);

        Matrix errorMatrix = Maths.toJamaMatrix(getErrorMatrix());
        Matrix pErrorMatrix = tMatrix.transpose().times(errorMatrix.times(tMatrix));

        return new SymmetricMatrix(Maths.fromJamaMatrix(pErrorMatrix));
    }

    /**
     * Calculate the error matrix for the position coordinates for a point on the track specified by l. Result is given as a 3x3 array for the matrix.
     */
    double[][] calcPositionErrorMatrix(double l) {
        double d0 = getD0();
        double rho = 1. / getOmega();
        double phi = getPhi0() + (getOmega() * l);
        double sphi0 = Math.sin(getPhi0());
        double cphi0 = Math.cos(getPhi0());
        double sphi = Math.sin(phi);
        double cphi = Math.cos(phi);

        Matrix tMatrix = new Matrix(5, 3, 0.);
        tMatrix.set(0, 0, -sphi0);
        tMatrix.set(0, 1, cphi0);
        tMatrix.set(1, 0, (rho * cphi) - ((rho + d0) * cphi0));
        tMatrix.set(1, 1, (rho * sphi) - ((rho + d0) * sphi0));
        tMatrix.set(2, 0, (rho * l * cphi) - (rho * rho * (sphi - sphi0)));
        tMatrix.set(2, 1, (rho * l * sphi) + (rho * rho * (cphi - cphi0)));
        tMatrix.set(3, 2, 1.);
        tMatrix.set(4, 2, l);

        Matrix errorMatrix = Maths.toJamaMatrix(getErrorMatrix());

        // MyContext.println(MyContext.getHeader());
        // MyContext.printMatrix("Error matrix:",errorMatrix,10,15);
        // MyContext.printMatrix("Transf matrix:",tMatrix,10,15);
        Matrix xErrorMatrix = tMatrix.transpose().times(errorMatrix.times(tMatrix));

        return xErrorMatrix.getArrayCopy();
    }

    // ====================================================
    //
    // private methods
    //
    // ====================================================

    /*
     * Calculate the DOCA for a set of parameters with respect to the origin.
     */
    private void calculateDoca(double[] momentum, double[] trackPoint, double q) {
        Hep3Vector p = new BasicHep3Vector(momentum[0], momentum[1], momentum[2]);
        Hep3Vector x = new BasicHep3Vector(trackPoint[0], trackPoint[1], trackPoint[2]);
        calculateDoca(p, x, q);
    }

    /*
     * Calculate the DOCA for a set of parameters in vectors with respect to the origin.
     */
    private void calculateDoca(Hep3Vector momentum, Hep3Vector trackPoint, double charge) {
        reset();

        Hep3Vector xOrigin = new BasicHep3Vector(0., 0., 0.);

        Hep3Vector[] result = calculateDoca(momentum, trackPoint, charge, xOrigin);
        Hep3Vector xdoca = result[0];
        Hep3Vector pdoca = result[1];
        Hep3Vector dphdl = result[2];

        int iq = (int) (charge / Math.abs(charge));
        double pt = Math.sqrt((pdoca.x() * pdoca.x()) + (pdoca.y() * pdoca.y()));

        // now calculate track parameters
        double d0 = Math.sqrt((xdoca.x() * xdoca.x()) + (xdoca.y() * xdoca.y()));
        if (VecOp.cross(xdoca, pdoca).z() > 0) {
            d0 = -d0;
        }

        double phi0 = Math.atan2(pdoca.y(), pdoca.x());
        if (phi0 > Math.PI) {
            phi0 -= (2 * Math.PI);
        }
        if (phi0 < -Math.PI) {
            phi0 += (2 * Math.PI);
        }

        double omega = Constants.fieldConversion * iq * m_Bz / pt;
        double tanl = pdoca.z() / pt;
        double z0 = xdoca.z();

        // now fill trackparameters
        m_parm[0] = d0;
        m_parm[1] = phi0;
        m_parm[2] = omega;
        m_parm[3] = z0;
        m_parm[4] = tanl;

        // save the distance to orignial track vertex
        m_l0 = -dphdl.y();

        // System.out.println("DocaTrackParameters: xdoca = ("+
        // xdoca.x()+", "+xdoca.y()+", "+xdoca.z()+")");
        // System.out.println("DocaTrackParameters: pdoca = ("+
        // pdoca.x()+", "+pdoca.y()+", "+pdoca.z()+")");
        // System.out.println("DocaTrackParameters: d0: "+m_parm[0]+
        // " phi0: "+m_parm[1]+" omega: "+m_parm[2]+
        // " z0: "+m_parm[3]+" tanl: "+m_parm[4]);
        // System.out.println("DocaTrackParameters: m_l0 = "+m_l0);
    }

    /*
     * Calculate DOCA position and momentum vectors with respect to any space point.
     */
    private Hep3Vector[] calculateDoca(Hep3Vector momentum, Hep3Vector trackPoint, double charge, Hep3Vector refPoint) {
        // subtract refPoint
        Hep3Vector xp = VecOp.sub(trackPoint, refPoint);

        int iq = (int) (charge / Math.abs(charge));
        double pt = Math.sqrt((momentum.x() * momentum.x()) + (momentum.y() * momentum.y()));
        double tanl = momentum.z() / pt;
        double rho = pt / (m_Bz * Constants.fieldConversion);

        BasicHep3Vector xdoca;
        Hep3Vector pdoca;
        Hep3Vector dphdl;

        // System.out.println("calculateDoca: m_flip: "+m_flip+" iq: "+iq);
        if (xp.magnitude() > 0.) // no need for calculation if xp = (0,0,0) !
        {
            // calculate position and momentum at doca
            Hep3Vector nzv = new BasicHep3Vector(0., 0., iq);
            Hep3Vector xxc = new BasicHep3Vector(VecOp.cross(momentum, nzv).x(), VecOp.cross(momentum, nzv).y(), 0.);
            Hep3Vector nxxc = VecOp.unit(xxc);
            BasicHep3Vector xc = (BasicHep3Vector) VecOp.add(xp, VecOp.mult(rho, nxxc));
            xc.setV(xc.x(), xc.y(), 0.);

            Hep3Vector nxc = VecOp.unit(xc);

            BasicHep3Vector catMC = (BasicHep3Vector) VecOp.cross(nzv, nxc);
            catMC.setV(catMC.x(), catMC.y(), 0.);

            Hep3Vector ncatMC = VecOp.unit(catMC);

            pdoca = new BasicHep3Vector(pt * ncatMC.x(), pt * ncatMC.y(), momentum.z());

            double dphi = Math.asin(VecOp.cross(nxxc, nxc).z());
            double dl = -dphi * rho * iq;

            xdoca = (BasicHep3Vector) VecOp.add(xc, VecOp.mult(-rho, nxc));
            xdoca.setV(xdoca.x(), xdoca.y(), xp.z() + (dl * tanl));

            // save dphi and dl
            dphdl = new BasicHep3Vector(dphi, dl, 0.);
        } else {
            xdoca = (BasicHep3Vector) xp;
            pdoca = momentum;
            dphdl = new BasicHep3Vector();
        }

        // add refPoint back in again
        xdoca = (BasicHep3Vector) VecOp.add(xdoca, refPoint);

        return new Hep3Vector[] { xdoca, pdoca, dphdl };
    }

    /*
     * Calculate Doca for this track with respect to any space point, if this has not been done before.
     */
    private void checkCalcDoca(Hep3Vector refPoint) {
        // hassle with calculation only, if not done before yet!
        if ((m_xref == null) || (refPoint.x() != m_xref.x()) || (refPoint.y() != m_xref.y()) || (refPoint.z() != m_xref.z())) {
            m_xref = refPoint;

            // find doca vectors
            Hep3Vector xdoca = getDocaVec();
            Hep3Vector pdoca = getMomentumVec();
            int iq = getUnitCharge();

            // calculate doca to refPoint
            Hep3Vector[] result = calculateDoca(pdoca, xdoca, (double) iq, refPoint);
            m_xdoca_ref = result[0];
            m_pdoca_ref = result[1];

            // distance along track to original point
            m_l_ref = result[2].y();
        }
    }

    private void reset() {
        for (int i = 0; i < 5; i++) {
            m_parm[i] = 0;
            for (int j = 0; j <= i; j++) {
                m_err.setElement(i, j, 0);
            }
        }
        m_l0 = 0.;
    }

    void setErrorMatrix(SymmetricMatrix error) {
        m_err = error;
    }
}
