package org.lcsim.mc.fast.tracking;

import Jama.*;
import hep.physics.matrix.SymmetricMatrix;
import org.lcsim.util.aida.AIDA;
import hep.physics.particle.Particle;
import hep.physics.vec.Hep3Vector;
import org.lcsim.event.Track;
import org.lcsim.event.TrackState;
import org.lcsim.event.base.BaseTrackState;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.lcsim.mc.fast.tracking.SimpleTables;

/**
 * Provides MC smeared track. <br>
 * Handles to smeared and not-smeared set of DocaTrackParameters are provided. <br>
 *
 * @author Tony Johnson, Wolfgang Walkowiak
 * @version $Id: ReconTrack.java,v 1.12 2012/07/26 16:46:14 grefe Exp $
 */
public class ReconTrack implements Track {
    private static final String[][] matrixI = { { "(1,1):", "(1,2):", "(1,3):", "(1,4):", "(1,5):" }, { "(2,1):", "(2,2):", "(2,3):", "(2,4):", "(2,5):" }, { "(3,1):", "(3,2):", "(3,3):", "(3,4):", "(3,5):" }, { "(4,1):", "(4,2):", "(4,3):", "(4,4):", "(4,5):" }, { "(5,1):", "(5,2):", "(5,3):", "(5,4):", "(5,5):" } };

    // =======================================================================
    //
    // private members
    //
    // =======================================================================
    private DocaTrackParameters m_nosmear = null;
    private DocaTrackParameters m_smear = null;
    transient private Particle mc;
    private int m_tcharge;
    private double[] _refpoint = { 0, 0, 0 };
    private List<TrackState> _trackStates;

    ReconTrack(double bField, TrackResolutionTables parm, SimpleTables SmTbl, Random rand, Particle mc, boolean hist, boolean simple) {
        this.mc = mc;

        // get original momentum from MCParticle
        // convert to helical parameters
        m_nosmear = new DocaTrackParameters(bField, mc);

        double pt = m_nosmear.getPt();

        if (hist) {

            double r = Math.abs(m_nosmear.getD0());

            AIDA aida = AIDA.defaultInstance();
            aida.cloud1D("ptsqr").fill(pt * pt);
            aida.cloud1D("pt").fill(pt);
            aida.cloud1D("phi").fill(m_nosmear.getPhi0());
            aida.cloud1D("theta").fill(m_nosmear.getTheta());
            aida.cloud1D("tanL").fill(m_nosmear.getTanL());
            aida.cloud1D("r").fill(r);
            aida.cloud1D("z").fill(mc.getOriginZ());
        }
        // get appropriate resolution table
        double abscth = Math.abs(m_nosmear.getCosTheta());
        double ptot = m_nosmear.getPtot();
        ResolutionTable table = (abscth < parm.getPolarInner()) ? parm.getBarrelTable() : parm.getEndcapTable();

        // get resolution values from interpolation and fill error matrix
        m_nosmear.setErrorMatrix(getErrorMatrixFromTable(table, abscth, ptot));

        // smear tracks according to error matrix
        if (simple == true) {
            m_smear = (DocaTrackParameters) SmearTrackSimple.smearTrackSimple(bField, m_nosmear, rand, SmTbl, pt, hist);
            // double[] slice = {0, 0, 1, 0, 0};
            // m_smear = (DocaTrackParameters) SmearTrackSimpleII.SmearTrackSimpleII(bField, m_nosmear, rand, SmTbl, slice, hist);
        } else {
            m_smear = (DocaTrackParameters) SmearTrack.smearTrack(bField, m_nosmear, rand);
        }

        if (hist) {
            AIDA aida = AIDA.defaultInstance();
            aida.cloud1D("ptNew").fill(m_smear.getPt());
            aida.cloud1D("tanLNew").fill(m_smear.getTanL());
            aida.cloud1D("rNew").fill(Math.abs(m_smear.getD0()));
            aida.cloud1D("phiNew").fill(m_smear.getPhi0());
            aida.cloud1D("zNew").fill(m_smear.getZ0());
        }
        m_tcharge = (int) (m_smear.getUnitCharge() * Math.abs(mc.getType().getCharge()));
        _trackStates = new ArrayList<TrackState>();
        _trackStates.add(new BaseTrackState(m_smear.getTrackParameters(), bField));
    }

    /**
     * Get the full charge.
     */
    public int getCharge() {
        return m_tcharge;
    }

    /**
     * Get the chi2 from smearing.
     */
    public double getChi2() {
        return m_smear.getChi2();
    }

    /**
     * Get DOCA (2-dim) of smeared track. <br>
     *
     * Note: Use #getNotSmearedTrack().getDOCA() to access parameters of the not smeared track.
     */
    public double[] getDoca() {
        return m_smear.getDoca();
    }

    public double[] getDocaMomentum(double[] refPoint) {
        return m_smear.getDocaMomentum(refPoint);
    }

    /**
     * Get momentum at DOCA (2-dim) of smeared track. <br>
     *
     * Note: Use #getNotSmearedTrack().getMomentum() to access parameters of the not smeared track.
     */
    public double[] getDocaMomentum() {
        return m_smear.getMomentum();
    }

    public Hep3Vector getDocaMomentumVec(Hep3Vector refPoint) {
        return m_smear.getDocaMomentumVec(refPoint);
    }

    /*
     * Calculate and get Doca momentum on smeared track with respect to any space point.
     */
    public Hep3Vector getDocaMomentumVec(double[] refPoint) {
        return m_smear.getDocaMomentumVec(refPoint);
    }

    /**
     * Get x coordinate of momentum of the smeared track at DOCA.
     */
    public double getDocaMomentumX() {
        return m_smear.getPX();
    }

    /**
     * Get y coordinate of momentum of the smeared track at DOCA.
     */
    public double getDocaMomentumY() {
        return m_smear.getPY();
    }

    /**
     * Get z coordinate of momentum of the smeared track at DOCA.
     */
    public double getDocaMomentumZ() {
        return m_smear.getPZ();
    }

    public double[] getDocaPosition(double[] refPoint) {
        return m_smear.getDocaPosition(refPoint);
    }

    /**
     * Calculate and get Doca position on the smeared track with respect to any space point.
     */
    public Hep3Vector getDocaPositionVec(Hep3Vector refPoint) {
        return m_smear.getDocaPositionVec(refPoint);
    }

    public Hep3Vector getDocaPositionVec(double[] refPoint) {
        return m_smear.getDocaPositionVec(refPoint);
    }

    /**
     * Get transverse momentum of the smeared track at DOCA.
     */
    public double getDocaPt() {
        return m_smear.getPt();
    }

    /**
     * Calculate and get path length on the smeared track for a doca to any space point in respect to the track defining doca (with respect to the origin). The length l is given in the transverse
     * plane. <br>
     * Use L = l*tan(lambda) to convert.
     */
    public double getDocaTransversePathLength(Hep3Vector refPoint) {
        return m_smear.getDocaTransversePathLength(refPoint);
    }

    public double getDocaTransversePathLength(double[] refPoint) {
        return m_smear.getDocaTransversePathLength(refPoint);
    }

    /**
     * Get x coordinate of DOCA of smeared track.
     */
    public double getDocaX() {
        return m_smear.getDocaX();
    }

    /**
     * Get y coordinate of DOCA of smeared track.
     */
    public double getDocaY() {
        return m_smear.getDocaY();
    }

    /**
     * Get z coordinate of DOCA of smeared track.
     */
    public double getDocaZ() {
        return m_smear.getDocaZ();
    }

    /**
     * Get the full error matrix.
     * @see #getTrackParameter
     */
    public SymmetricMatrix getErrorMatrix() {
        return m_smear.getErrorMatrix();
    }

    /**
     * Get the MC particle for this track.
     */
    public Particle getMCParticle() {
        return mc;
    }

    public double[] getMomentum(double l) {
        return m_smear.getMomentum(l);
    }

    /**
     * Get momentum of smeared track at original vertex point. <br>
     *
     * Note: Use #getNotSmearedTrack().getMomentum() to access parameters of the not smeared track.
     */
    public double[] getMomentum() {
        return m_smear.getMomentum(m_smear.getL0());
    }

    /**
     * Calculate and get momentum on track with respect to any path length l on track (l in xy plane).
     */
    public Hep3Vector getMomentumVec(double l) {
        return m_smear.getMomentumVec(l);
    }

    public Hep3Vector getMomentumVec() {
        return m_smear.getMomentumVec(m_smear.getL0());
    }

    /**
     * Get x coordinate of momentum of the smeared track at original vertex.
     */
    public double getMomentumX() {
        return m_smear.getMomentum(m_smear.getL0())[0];
    }

    /**
     * Get y coordinate of momentum of the smeared track at original vertex.
     */
    public double getMomentumY() {
        return m_smear.getMomentum(m_smear.getL0())[1];
    }

    /**
     * Get z coordinate of momentum of the smeared track at original vertex.
     */
    public double getMomentumZ() {
        return m_smear.getMomentum(m_smear.getL0())[2];
    }

    /**
     * Get the number degrees of freedom.
     */
    public int getNDF() {
        return m_smear.getNDF();
    }

    /**
     * Get the complete parameter set for the not smeared track.
     */
    public DocaTrackParameters getNotSmearedTrack() {
        return m_nosmear;
    }

    /**
     * Get x coordinate of momentum of the smeared track at original vertex.
     */
    public double getPX() {
        return getMomentumX();
    }

    /**
     * Get y coordinate of momentum of the smeared track at original vertex.
     */
    public double getPY() {
        return getMomentumY();
    }

    /**
     * Get z coordinate of momentum of the smeared track at original vertex.
     */
    public double getPZ() {
        return getMomentumZ();
    }

    public double[] getPosition(double l) {
        return m_smear.getPosition(l);
    }

    /**
     * Calculate and get position on track with respect to any path length l on track (l in xy plane).
     */
    public Hep3Vector getPositionVec(double l) {
        return m_smear.getPositionVec(l);
    }

    /**
     * Get transverse momentum of the smeared track at original vertex.
     */
    public double getPt() {
        double[] p = getMomentum();

        return Math.sqrt((p[0] * p[0]) + (p[1] * p[1]));
    }

    public double getRadiusOfInnermostHit() {
        return 0; // FixMe:
    }

    /**
     * Get the original vertex point of smeared MC track.
     */

    // Get the reference point used for track parameter calculations
    //
    public double[] getReferencePoint() {
        return _refpoint;
    }

    public boolean isReferencePointPCA() {
        return true;
    }

    /**
     * Get x coordinate of the original vertex point of smeared MC track.
     */
    public double getReferencePointX() {
        return getReferencePoint()[0];
    }

    /**
     * Get y coordinate of the original vertex point of smeared MC track.
     */
    public double getReferencePointY() {
        return getReferencePoint()[1];
    }

    /**
     * Get z coordinate of the original vertex point of smeared MC track.
     */
    public double getReferencePointZ() {
        return getReferencePoint()[2];
    }

    /**
     * Get the complete parameter set for the smeared track.
     */
    public DocaTrackParameters getSmearedTrack() {
        return m_smear;
    }

    public int[] getSubdetectorHitNumbers() {
        return new int[0]; // FIXME
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
        return m_smear.getTrackParameter(i);
    }

    /**
     * Get the track parameters as an array
     * @see #getTrackParameter
     */
    public double[] getTrackParameters() {
        return m_smear.getTrackParameters();
    }

    public List getTrackerHits() {
        return Collections.EMPTY_LIST;
    }

    public List getTracks() {
        return Collections.EMPTY_LIST;
    }

    public int getType() {
        return 0; // FIXME:
    }

    /**
     * Calculate the error matrix for the momentum for a point on the smeared track specified by l.
     */
    public SymmetricMatrix calcMomentumErrorMatrix(double l) {
        return m_smear.calcMomentumErrorMatrix(l);
    }

    /**
     * Calculate the error matrix for the position coordinates for a point on the smeared track specified by l. Result is given as a 3x3 array for the matrix.
     */
    public double[][] calcPositionErrorMatrix(double l) {
        return m_smear.calcPositionErrorMatrix(l);
    }

    public boolean fitSuccess() {
        // FIXME Not implemented in fastMC
        return false;
    }

    public double getdEdx() {
        return 0; // FIXME
    }

    public double getdEdxError() {
        return 0; // FIXME
    }

    public String toString() {
        java.io.StringWriter buffer = new java.io.StringWriter();
        java.io.PrintWriter out = new java.io.PrintWriter(buffer);

        java.text.NumberFormat pf = java.text.NumberFormat.getInstance();
        pf.setMinimumFractionDigits(12);
        pf.setMaximumFractionDigits(12);
        pf.setMinimumIntegerDigits(1);
        pf.setMaximumIntegerDigits(3);
        pf.setGroupingUsed(false);

        Matrix mHlxPar = new Matrix(m_smear.getTrackParameters(), 1);

        out.println("ReconTrack Parameters:  d0          phi0          omega         z0        tan(lambda)");
        mHlxPar.print(out, pf, 16);
        out.println("Error Matrix:");
        out.println(m_smear.getErrorMatrix());

        return (buffer.toString());
    }

    private SymmetricMatrix getErrorMatrixFromTable(ResolutionTable table, double abscth, double ptot) {
        SymmetricMatrix errMatrix = new SymmetricMatrix(5);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j <= i; j++) {
                errMatrix.setElement(i, j, table.findTable(matrixI[i][j]).interpolateVal(abscth, ptot));
            }
        }
        return errMatrix;
    }

    public List<TrackState> getTrackStates() {
        return _trackStates;
    }
}