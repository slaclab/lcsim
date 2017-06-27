/**
 * @version $Id: FastMCTrackFactory.java,v 1.3 2011/04/02 16:35:41 jstrube Exp $
 */
package org.lcsim.mc.fast.tracking.fix;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static org.lcsim.event.LCIOParameters.ParameterName.omega;
import static org.lcsim.event.LCIOParameters.ParameterName.phi0;
import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;

import java.io.IOException;
import java.util.Random;

import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsSet;
import org.lcsim.event.EventHeader;
import org.lcsim.event.LCIOParameters;
import org.lcsim.event.MCParticle;
import org.lcsim.event.Track;
import org.lcsim.mc.fast.tracking.ResolutionTable;
import org.lcsim.mc.fast.tracking.SimpleTables;
import org.lcsim.mc.fast.tracking.TrackResolutionTables;
import org.lcsim.spacegeom.SpacePoint;
import org.lcsim.spacegeom.SpaceVector;
import org.lcsim.util.swim.HelixSwimmer;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;
import Jama.util.Maths;

/**
 * @author jstrube This class creates a new FastMC Track. It is used as the interface between the track measurement and the detector. Since Track doesn't know anything about the magnetic field, and
 *         the material, it cannot transport its own parameters. Changing the reference point of a track requires swimming; it is therefore done in this class.
 * 
 */
public class FastMCTrackFactory {
    private TrackResolutionTables _tables;
    private SimpleTables _simpleTables;
    private ConditionsManager _manager;
    private double _Bz;
    private HelixSwimmer _swimmer;
    private static Random rDummy = new Random();
    private static SpacePoint pDummy = new SpacePoint();

    /**
     * This constructor obtains the necessary information for construction like the field and the resolution tables from the event.
     * 
     * @param event The current event
     * @param beamConstraint A switch to obtain the resolution tables with or without beamconstraint
     */
    public FastMCTrackFactory(EventHeader event, boolean beamConstraint) {
        this(event.getDetectorName(), event.getDetector().getFieldMap().getField(new double[3])[2], beamConstraint);
    }

    /**
     * This constructor is only to be used by unit tests. It will instantiate the Factory with a detector name and a field.
     * 
     */
    public FastMCTrackFactory(String detectorName, double field, boolean beamConstraint) {
        _Bz = field;
        _manager = ConditionsManager.defaultInstance();
        try {
            // new detector, run 0
            _manager.setDetector(detectorName, 0);
        } catch (ConditionsManager.ConditionsNotFoundException e) {
            System.err.print("Conditions for detector " + detectorName + " not found!");
        }
        ConditionsSet trackParameters = _manager.getConditions("TrackParameters");
        ConditionsSet simpleTrack = _manager.getConditions("SimpleTrack");
        try {
            _tables = new TrackResolutionTables(trackParameters, beamConstraint);
            _simpleTables = new SimpleTables(simpleTrack);
        } catch (IOException e) {
        }
        _swimmer = new HelixSwimmer(field);
    }

    /**
     * Creates a track from an MCParticle
     * @param part The MCParticle that is transformed to a track
     * @return A FastMCTrack instance that contains information about the particle that was used as input
     */
    public Track getTrack(MCParticle part) {
        FastMCTrack t = (FastMCTrack) getTrack(part.getMomentum(), part.getOrigin(), (int) part.getCharge());
        t._particle = part;
        return t;
    }

    /**
     * Creates a track from an MCParticle without smearing the parameters
     * @param part The MCParticle that is transformed to a track
     * @return A FastMCTrack instance that contains information about the particle that was used as input
     */
    public Track getUnsmearedTrack(MCParticle part) {
        FastMCTrack t = (FastMCTrack) getTrack(new SpaceVector(part.getMomentum()), new SpacePoint(part.getOrigin()), pDummy, (int) part.getCharge(), rDummy, false);
        t._particle = part;
        return t;
    }

    /**
     * Creates a new Track with the given parameters. See #{@link #getTrack(SpacePoint, SpacePoint, SpacePoint, int, Random)} for details.
     * 
     * @param momentum The momentum at a given location
     * @param location The location where the momentum is measured
     * @param charge The charge of the Particle that created the Track
     * @return A new NewTFastMCTrackect with the desired properties
     */
    public Track getTrack(SpaceVector momentum, SpacePoint location, int charge) {
        return getTrack(momentum, location, pDummy, charge, rDummy);
    }

    /**
     * Creates a new Track with the given parameters. See #{@link #getTrack(SpacePoint, SpacePoint, SpacePoint, int, Random)} for details.
     * 
     * @param momentum The momentum at a given location
     * @param location The location where the momentum is measured
     * @param charge The charge of the Particle that created the Track
     * @param random A random generator instance
     * @return A new NewTrFastMCTrackct with the desired properties
     */
    public Track getTrack(SpaceVector momentum, SpacePoint location, int charge, Random random) {
        return getTrack(momentum, location, pDummy, charge, random);
    }

    /**
     * Creates a new Track with the given parameters. See #{@link #getTrack(SpacePoint, SpacePoint, SpacePoint, int, Random)} for details.
     * 
     * @param momentum The momentum at a given location
     * @param location The location where the momentum is measured
     * @param referencePoint The point with respect to which the parameters are measured
     * @param charge The charge of the Particle that created the Track
     * @return A new NewTrFastMCTrackct with the desired properties
     */
    public Track getTrack(SpaceVector momentum, SpacePoint location, SpacePoint referencePoint, int charge) {
        return getTrack(momentum, location, referencePoint, charge, rDummy);
    }

    /**
     * This version is only to be used in unit tests.
     * 
     * @param momentum The momentum at a given location
     * @param location The location where the momentum is measured
     * @param referencePoint The point with respect to which the parameters are measured
     * @param charge The charge of the Particle that created the Track
     * @param random A random generator instance
     * @param shouldISmear This parameter switches smearing on/off. It should always be true except in Unit tests.
     * @return A new NewTracFastMCTrack with the desired properties
     */
    public Track getTrack(SpaceVector momentum, SpacePoint location, SpacePoint referencePoint, int charge, Random random, boolean shouldISmear) {
        _swimmer.setTrack(momentum, location, charge);
        double alpha = _swimmer.getTrackLengthToPoint(referencePoint);
        SpacePoint poca = _swimmer.getPointAtLength(alpha);
        SpaceVector momentumAtPoca = _swimmer.getMomentumAtLength(alpha);
        LCIOParameters parameters = LCIOParameters.SpaceMomentum2Parameters(poca, momentumAtPoca, referencePoint, charge, _Bz);
        SymmetricMatrix errorMatrix = new SymmetricMatrix(5);
        // this sets the measurement error
        double cosTheta = abs(momentumAtPoca.cosTheta());
        double p_mag = momentumAtPoca.magnitude();
        ResolutionTable table = cosTheta < _tables.getPolarInner() ? _tables.getBarrelTable() : _tables.getEndcapTable();
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j <= i; j++) {
                double iVal = table.findTable(i, j).interpolateVal(cosTheta, p_mag);
                errorMatrix.setElement(i, j, iVal);
            }
        }
        // it's a bit inefficient to always have this condition here, although it's only used in tests.
        LCIOParameters smearParams = shouldISmear ? smearParameters(parameters, errorMatrix, random) : parameters;

        // System.out.println("Charge: " + charge);
        // System.out.println("TrackFactory: POCA " + poca);
        // System.out.println("TrackFactory: Momentum " + momentumAtPoca);
        // System.out.println("TrackFactory: Parameters: " + smearParams);
        // System.out.println("TrackFactory: POCA from parameters: " + LCIOTrackParameters.Parameters2Position(smearParams, referencePoint));
        // System.out.println("TrackFactory: Parameters from POCA: " + LCIOTrackParameters.SpaceMomentum2Parameters(poca, momentumAtPoca, referencePoint, charge, _Bz));
        return new FastMCTrack(referencePoint, smearParams, errorMatrix, charge);
    }

    /**
     * Returns a new Track object initialized with the given values, and with its parameters smeared according to the Tables that are read from the detector. This method can take a random seed
     * 
     * @param momentum The momentum at a given location
     * @param location The location where the momentum is measured
     * @param referencePoint The point with respect to which the parameters are measured
     * @param charge The charge of the Particle that created the Track
     * @param random A random generator instance
     * @return A new NewTraFastMCTrackt with the desired properties
     */
    public Track getTrack(SpaceVector momentum, SpacePoint location, SpacePoint referencePoint, int charge, Random random) {
        return getTrack(momentum, location, referencePoint, charge, random, true);
    }

    public Track getTrack(Hep3Vector momentum, Hep3Vector location, int charge) {
        return getTrack(new SpaceVector(momentum), new SpacePoint(location), pDummy, charge, rDummy);
    }

    /**
     * Swims the Track to a new reference point and calculates the parameters anew. It has to be done here, because it involves swimming, which has to be done outside the track
     * @param track The track to be swum
     * @param referencePoint The new reference point for the track to swim to
     */
    public void setNewReferencePoint(Track track, SpacePoint referencePoint) {
        _swimmer.setTrack(track);
        double alpha = _swimmer.getTrackLengthToPoint(referencePoint);

        // TODO this involves transportation of the full covariance matrix.
        // See Paul Avery's notes for details.
        throw new RuntimeException("not yet implemented !");
    }

    /**
     * Smears the measurement matrix with a Gaussian error
     * @param oldParams The unsmeared Parameters
     * @param errorMatrix The measurement error matrix
     * @param random A random generator
     * @return A new set of smeared parameters
     */
    private static LCIOParameters smearParameters(LCIOParameters oldParams, SymmetricMatrix sm, Random random) {
        Matrix errorMatrix = Maths.toJamaMatrix(sm);
        EigenvalueDecomposition eigen = errorMatrix.eig();
        double[] realEigen = eigen.getRealEigenvalues();
        double[] imaginaryEigen = eigen.getImagEigenvalues();
        Matrix eigenValues = eigen.getV();
        if (eigenValues.det() == 0) {
            throw new RuntimeException("ErrorMatrix does not have orthogonal basis");
        }
        for (int i = 0; i < imaginaryEigen.length; i++) {
            if (imaginaryEigen[i] != 0)
                throw new RuntimeException("ErrorMatrix has imaginary eigenvalues");
        }
        Matrix x = new Matrix(5, 1);
        for (int i = 0; i < 5; i++) {
            if (realEigen[i] <= 0)
                throw new RuntimeException("non-positive eigenvalue encountered");
            x.set(i, 0, sqrt(realEigen[i]) * random.nextGaussian());
        }
        Matrix shift = eigenValues.times(x);
        Matrix params = new Matrix(oldParams.getValues(), 5);
        // calculate the new parameters
        double[] parameters = params.plus(shift).getColumnPackedCopy();
        double pt = oldParams.getPt() * oldParams.get(omega) / parameters[omega.ordinal()];
        // adjust the new parameters if necessary
        if (parameters[phi0.ordinal()] > PI) {
            parameters[phi0.ordinal()] -= 2 * PI;
        } else if (parameters[phi0.ordinal()] < -PI) {
            parameters[phi0.ordinal()] += 2 * PI;
        }
        return new LCIOParameters(parameters, pt);
    }
}
