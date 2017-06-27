/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lcsim.recon.tracking.trfzp;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.magfield.ConstantMagneticField;
import org.lcsim.recon.tracking.trfbase.*;

import static java.lang.Math.abs;
import static java.lang.Math.max;

/**
 *
 * @author ngraf
 */
public class PropZZRKTest extends TestCase
{

    private boolean debug = false;

    public void testPropZZRK()
    {
        ConstantMagneticField field = new ConstantMagneticField(0., 0., 2.);
        PropZZRK prop = new PropZZRK(field);
        if(debug) System.out.println(prop);

        // Construct equivalent PropZZ propagator.
        PropZZ prop0 = new PropZZ(2.0);
        if (debug) {
            System.out.println(prop0);
        }

        // Here we propagate some tracks both forward and backward and then
        // each back to the original track.  We check that the returned
        // track parameters match those of the original track.
        if(debug) System.out.println("Check reversibility.");
        double z1[] = {100.0, -100.0, 100.0, -100.0, 100.0, -100.0, 100.0, 100.0};
        double z2[] = {150.0, -50.0, 50.0, -150.0, 50.0, -50.0, 150.0, 50.0};
        double x[] = {10.0, 1.0, -1.0, 2.0, 2.0, -2.0, 3.0, 0.0};
        double xv[] = {0.5, 0.03, -0.03, 0.5, -0.5, 1.0, 1.0, 2.0};
        double crv[] = {0.1, -0.1, 0.1, 0.01, 0.01, -1.0, 1.0, 0.02};
        double y[] = {20.0, 0.0, 0.0, 0.0, 0.0, 0.0, 15.0, 0.0};
        double yv[] = {-0.5, 0.01, -0.02, 0.0, 0.0, 0.5, -0.5, 0.0};
        double fbdf[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        double bfdf[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        boolean forw[] = {true, false, false, true, false, true, true, true};
        double maxdiff = 1.e-7;
        int ntrk = 8;
        int i;


        for (i = 0; i < ntrk; ++i) {
            if(debug) System.out.println("********** Propagate track " + i + ". **********");
            PropStat pstat = new PropStat();
            SurfZPlane sz1 = new SurfZPlane(z1[i]);
            SurfZPlane sz2 = new SurfZPlane(z2[i]);
            TrackVector vec1 = new TrackVector();
            vec1.set(0, x[i]);    // x
            vec1.set(1, y[i]);    // y
            vec1.set(2, xv[i]);   // dx/dz
            vec1.set(3, yv[i]);   // dy/dz
            vec1.set(4, crv[i]);  // q/p
            TrackSurfaceDirection tdir;
            if (forw[i]) {
                tdir = TrackSurfaceDirection.TSD_FORWARD;
            } else {
                tdir = TrackSurfaceDirection.TSD_BACKWARD;
            }
            VTrack trv1 = new VTrack(sz1.newPureSurface(), vec1, tdir);
            if(debug) System.out.println(" starting: " + trv1);
            //
            // Find the direction that will propagate this track from z1 to z2.
            //
            PropDir dir = PropDir.FORWARD;
            PropDir rdir = PropDir.BACKWARD;
            if (z2[i] > z1[i] && tdir == TrackSurfaceDirection.TSD_BACKWARD
                    || z2[i] < z1[i] && tdir == TrackSurfaceDirection.TSD_FORWARD) {
                dir = PropDir.BACKWARD;
                rdir = PropDir.FORWARD;
            }

            //
            // Propagate.
            VTrack trv2f = trv1;
            pstat = prop.vecDirProp(trv2f, sz2, dir);
            assert (pstat.success());
            if(debug) System.out.println("  forward: " + trv2f);
            if(debug) System.out.println(pstat);
            if (dir == PropDir.FORWARD) {
                assert (pstat.forward());
            }
            if (dir == PropDir.BACKWARD) {
                assert (pstat.backward());
            }

            //
            // Propagate using PropZZ and check difference.
            VTrack trv2f0 = trv1;
            pstat = prop0.vecDirProp(trv2f0, sz2, dir);
            assert (pstat.success());
            if(debug) System.out.println("  forward: " + trv2f0);
            if(debug) System.out.println(pstat);
            double diff0 =
                    sz2.vecDiff(trv2f.vector(), trv2f0.vector()).amax();
            if(debug) System.out.println("diff: " + diff0);
            assert (diff0 < maxdiff);

            //
            // Propagate in reverse direction.
            VTrack trv2fb = trv2f;
            pstat = prop.vecDirProp(trv2fb, sz1, rdir);
            assert (pstat.success());
            if(debug) System.out.println(" f return: " + trv2fb);
            if(debug) System.out.println(pstat);
            if (rdir == PropDir.FORWARD) {
                assert (pstat.forward());
            }
            if (rdir == PropDir.BACKWARD) {
                assert (pstat.backward());
            }
            // Check the return differences.
            double difff =
                    sz1.vecDiff(trv2fb.vector(), trv1.vector()).amax();
            if(debug) System.out.println("diff: " + difff);
            assert (difff < maxdiff);
            //
            // Check no-move forward propagation to the same surface.
            VTrack trv1s = trv1;
            pstat = prop.vecDirProp(trv1s, sz1, PropDir.FORWARD);
            assert (pstat.success());
            if(debug) System.out.println(" same f: " + trv1s);
            if(debug) System.out.println(pstat);
            assert (pstat.same());
            assert (pstat.pathDistance() == 0);
            //
            // Check no-move backward propagation to the same surface.
            trv1s = trv1;
            pstat = prop.vecDirProp(trv1s, sz1, PropDir.BACKWARD);
            assert (pstat.success());
            if(debug) System.out.println(" same b: " + trv1s);
            if(debug) System.out.println(pstat);
            assert (pstat.same());
            assert (pstat.pathDistance() == 0);
            //
            // Check move propagation to the same surface.
            //
            // forward
            int successes = 0;
            trv1s = trv1;
            pstat = prop.vecDirProp(trv1s, sz1, PropDir.FORWARD_MOVE);
            if(debug) System.out.println(" forward move: " + trv1s);
            if(debug) System.out.println(pstat);
            if (pstat.forward()) {
                ++successes;
            }
            // backward
            trv1s = trv1;
            pstat = prop.vecDirProp(trv1s, sz1, PropDir.BACKWARD_MOVE);
            if(debug) System.out.println(" backward move: " + trv1s);
            if(debug) System.out.println(pstat);
            if (pstat.backward()) {
                ++successes;
            }
            // Neither of these should have succeeded.
            assert (successes == 0);
            //
            // nearest
            trv1s = trv1;
            pstat = prop.vecDirProp(trv1s, sz1, PropDir.NEAREST_MOVE);
            if(debug) System.out.println(" nearest move: " + trv1s);
            if(debug) System.out.println(pstat);
            assert (!pstat.success());


//               cng 120905 problems here... 
//            // Test derivatives numerically using uniform field. 
//            System.out.println("Testing derivatives with uniform field");
//            VTrack trv1a = trv1;
//            TrackDerivative deriv = new TrackDerivative();
//            pstat = prop.vecDirProp(trv1a, sz2, PropDir.NEAREST, deriv);
//            assert (pstat.success());
//            VTrack trv1a0 = trv1;
//            TrackDerivative deriv0 = new TrackDerivative();
//            pstat = prop0.vecDirProp(trv1a0, sz2, PropDir.NEAREST, deriv0);
//            assert (pstat.success());
//            for (int j = 0; j < 5; ++j) {
//                double d;
//                if (j < 2) {
//                    d = 0.25;
//                } else if (j < 4) {
//                    d = 0.01;
//                } else {
//                    d = 0.001;
//                }
//                TrackVector vec1b = vec1;
//                TrackVector vec1c = vec1;
//                vec1b.set(j, vec1.get(j) + d);
//                vec1c.set(j, vec1.get(j) - d);
//                VTrack trv1b = new VTrack(sz1.newPureSurface(), vec1b, tdir);
//                VTrack trv1c = new VTrack(sz1.newPureSurface(), vec1c, tdir);
//                pstat = prop.vecDirProp(trv1b, sz2, PropDir.NEAREST);
//                assert (pstat.success());
//                pstat = prop.vecDirProp(trv1c, sz2, PropDir.NEAREST);
//                assert (pstat.success());
//                System.out.println("Testing diffs");
//                System.out.println("ii, j \t dij deriv(ii,j) deriv0(ii,j)");
//                for (int ii = 0; ii < 5; ++ii) {
//                    double dij = (trv1b.vector(ii) - trv1c.vector(ii)) / (2. * d);
//                    System.out.println(ii + ", " + j + '\t' + dij + '\t' + deriv.get(ii, j) + '\t' + deriv0.get(ii, j));
//                    double scale = max(abs(deriv.get(ii, j)), 1.);
//                    assert (abs(deriv.get(ii, j) - deriv0.get(ii, j)) / scale < maxdiff);
//                    double tmp = abs(dij - deriv.get(ii, j)) / scale;
//                    System.out.println(tmp + " " + scale);
//                    assert (abs(dij - deriv.get(ii, j)) / scale < 1.e-3);
//                }
//            }

// following was commented out originally
//    /*
//    // Test derivatives numerically using non-uniform field.
//    System.out.println("Testing derivatives with non-uniform field");
//    VTrack trv1an = trv1;
//    TrackDerivative derivn;
//    pstat = propmc.vecDirProp(trv1an,sz2,PropDir.NEAREST, &derivn);
//    assert(pstat.success());
//    for(int j=0; j<5; ++j) {
//      double d;
//      if(j < 2)
//	d = 0.1;
//      else if(j < 4)
//	d = 0.01;
//      else
//	d = 0.001;
//      TrackVector vec1bn = vec1;
//      TrackVector vec1cn = vec1;
//      vec1bn(j, vec1(j) + d;
//      vec1cn(j, vec1(j) - d;
//      VTrack trv1bn(SurfacePtr(sz1.newPureSurface()), vec1bn, tdir);
//      VTrack trv1cn(SurfacePtr(sz1.newPureSurface()), vec1cn, tdir);
//      pstat = propmc.vecDirProp(trv1bn,sz2,PropDir.NEAREST);
//      assert(pstat.success());
//      pstat = propmc.vecDirProp(trv1cn,sz2,PropDir.NEAREST);
//      assert(pstat.success());
//      for(int i=0; i<5; ++i) {
//	double dijn = (trv1bn.vector(i) - trv1cn.vector(i))/(2.*d);
//	cout + i + ", " + j + '\t' +  dijn + '\t' + derivn(i,j) );
//	double scale = max(abs(derivn(i,j)), 1.);
//	assert(abs(dijn-derivn(i,j))/scale < 0.01);
//      }
//    }
//    */
        }

        //********************************************************************

        // Repeat the above with errors.
        if(debug) System.out.println("Check reversibility with errors.");
        double exx[] = {0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
        double exy[] = {0.05, -0.05, 0.05, -0.05, 0.05, -0.05, 0.05, 0.05};
        double eyy[] = {0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25, 0.25};
        double exxv[] = {0.004, -0.004, 0.004, -0.004, 0.004, -0.004, 0.004, 0.004};
        double eyxv[] = {0.02, -0.02, 0.02, -0.02, 0.02, -0.02, 0.02, 0.02};
        double exvxv[] = {0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01};
        double exyv[] = {0.004, -0.004, 0.004, -0.004, 0.004, -0.004, 0.004, 0.004};
        double eyyv[] = {0.04, -0.04, 0.04, -0.04, 0.04, -0.04, 0.04, 0.04};
        double exvyv[] = {0.004, -0.004, 0.004, -0.004, 0.004, -0.004, 0.004, 0.004};
        double eyvyv[] = {0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02, 0.02};
        double exc[] = {0.004, -0.004, 0.004, -0.004, 0.004, -0.004, 0.004, 0.004};
        double eyc[] = {0.004, -0.004, 0.004, -0.004, 0.004, -0.004, 0.004, 0.004};
        double exvc[] = {0.004, -0.004, 0.004, -0.004, 0.004, -0.004, 0.004, 0.004};
        double eyvc[] = {0.004, -0.004, 0.004, -0.004, 0.004, -0.004, 0.004, 0.004};
        double ecc[] = {0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01, 0.01};
        for (i = 0; i < ntrk; ++i) {
            if(debug) System.out.println("********** Propagate track " + i + ". **********");
            PropStat pstat = new PropStat();
            SurfZPlane dz1 = new SurfZPlane(z1[i]);
            SurfZPlane dz2 = new SurfZPlane(z2[i]);
            TrackVector vec1 = new TrackVector();
            vec1.set(0, x[i]);    // x
            vec1.set(1, y[i]);    // y
            vec1.set(2, xv[i]);   // dx/dz
            vec1.set(3, yv[i]);   // dy/dz
            vec1.set(4, crv[i]);  // curvature
            TrackError err1 = new TrackError();
            err1.set(0, 0, exx[i]);
            err1.set(0, 1, exy[i]);
            err1.set(1, 1, eyy[i]);
            err1.set(0, 2, exxv[i]);
            err1.set(1, 2, eyxv[i]);
            err1.set(2, 2, exvxv[i]);
            err1.set(0, 3, exyv[i]);
            err1.set(1, 3, eyyv[i]);
            err1.set(2, 3, exvyv[i]);
            err1.set(3, 3, eyvyv[i]);
            err1.set(0, 4, exc[i]);
            err1.set(1, 4, eyc[i]);
            err1.set(2, 4, exvc[i]);
            err1.set(3, 4, eyvc[i]);
            err1.set(4, 4, ecc[i]);
            TrackSurfaceDirection tdir;
            if (forw[i]) {
                tdir = TrackSurfaceDirection.TSD_FORWARD;
            } else {
                tdir = TrackSurfaceDirection.TSD_BACKWARD;
            }
            ETrack tre1 = new ETrack((dz1.newPureSurface()), vec1, err1, tdir);
            if(debug) System.out.println(" starting: " + tre1);
            //
            // Find the direction that will propagate this track from r1 to r2.
            //
            PropDir dir = PropDir.FORWARD;
            PropDir rdir = PropDir.BACKWARD;
            if (z2[i] > z1[i] && tdir == TrackSurfaceDirection.TSD_BACKWARD
                    || z2[i] < z1[i] && tdir == TrackSurfaceDirection.TSD_FORWARD) {
                dir = PropDir.BACKWARD;
                rdir = PropDir.FORWARD;
            }

            //
            // Propagate.
            ETrack tre2f = tre1;
            pstat = prop.errDirProp(tre2f, dz2, dir);
            assert (pstat.success());
            if(debug) System.out.println("  forward: " + tre2f);
            if(debug) System.out.println(pstat);
            if (dir == PropDir.FORWARD) {
                assert (pstat.forward());
            }
            if (dir == PropDir.BACKWARD) {
                assert (pstat.backward());
            }

            //
            // Propagate using PropZZ and check difference.
            ETrack tre2f0 = tre1;
            pstat = prop0.errDirProp(tre2f0, dz2, dir);
            assert (pstat.success());
            if(debug) System.out.println("  forward: " + tre2f0);
            if(debug) System.out.println(pstat);
            double vdiff0 =
                    dz2.vecDiff(tre2f.vector(), tre2f0.vector()).amax();
            if(debug) System.out.println("vec diff: " + vdiff0);
            assert (vdiff0 < maxdiff);
            TrackError df0 = tre2f.error().minus(tre2f0.error());
            double ediff0 = df0.amax();
            if(debug) System.out.println("err diff: " + ediff0);
            assert (ediff0 < maxdiff);

            //
            // Propagate backward
            ETrack tre2fb = tre2f;
            pstat = prop.errDirProp(tre2fb, dz1, rdir);
            assert (pstat.success());
            if(debug) System.out.println(" f return: " + tre2fb);
            if(debug) System.out.println(pstat);
            if (rdir == PropDir.FORWARD) {
                assert (pstat.forward());
            }
            if (rdir == PropDir.BACKWARD) {
                assert (pstat.backward());
            }
            double difff =
                    dz1.vecDiff(tre2fb.vector(), tre1.vector()).amax();
            if(debug) System.out.println("vec diff: " + difff);
            assert (difff < maxdiff);
            TrackError dfb = tre2fb.error().minus(tre1.error());
            double edifff = dfb.amax();
            if(debug) System.out.println("err diff: " + edifff);
            assert (edifff < maxdiff);
        }

    }
}
