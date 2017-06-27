package org.lcsim.recon.tracking.seedtracker;

import hep.physics.vec.Hep3Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.detector.IPhysicalVolumeNavigator;
import org.lcsim.detector.IPhysicalVolumePath;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.PhysicalVolumeNavigator;
import org.lcsim.detector.PhysicalVolumeNavigatorStore;
import org.lcsim.detector.PhysicalVolumePath;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.solids.Box;
import org.lcsim.detector.solids.ISolid;
import org.lcsim.detector.solids.Point3D;
import org.lcsim.detector.solids.Polycone;
import org.lcsim.detector.solids.Polycone.ZPlane;
import org.lcsim.detector.solids.Trd;
import org.lcsim.detector.solids.Tube;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.Subdetector;
import org.lcsim.geometry.subdetector.DiskTracker;
import org.lcsim.geometry.subdetector.MultiLayerTracker;
import org.lcsim.geometry.subdetector.PolyconeSupport;
import org.lcsim.geometry.subdetector.SiTrackerBarrel;
import org.lcsim.geometry.subdetector.SiTrackerEndcap;
import org.lcsim.geometry.subdetector.SiTrackerEndcap2;
import org.lcsim.geometry.subdetector.SiTrackerFixedTarget2;
import org.lcsim.geometry.subdetector.SiTrackerSpectrometer;

/**
 * Rewrite and refactor of Rich's {@link MaterialManager} class to handle Subdetector types. This class should now group
 * together SiTrackerEndcap2 layers correctly.
 *
 * @author Rich Partridge
 * @author Jeremy McCormick
 * @author Matt Graham
 * @version $Id: MaterialManager.java,v 1.21 2013/07/12 20:47:35 phansson Exp $
 */
public class MaterialManager {

    /**
     * Get the path groupings for barrel Subdetectors with physical layers one level below top. This will handle
     * SiTrackerBarrel and MultiLayerTracker Subdetector types.
     */
    public static final class BarrelLayerVolumeGroup implements SubdetectorVolumeGrouper {

        @Override
        public List<List<String>> getPathGroups(final Subdetector subdet, final IPhysicalVolume topVol) {
            final List<List<String>> pathGroups = new ArrayList<List<String>>();
            for (final IDetectorElement layer : subdet.getDetectorElement().getChildren()) {
                final List<String> layerPaths = new ArrayList<String>();
                final String path = "";
                PhysicalVolumeNavigator.getLeafPaths(layerPaths, layer.getGeometry().getPhysicalVolume(), path);
                pathGroups.add(layerPaths);
            }
            return pathGroups;
        }
    }

    /**
     * Default VolumeGroup for endcaps with physical layers.
     */
    public static final class EndcapVolumeGrouper implements SubdetectorVolumeGrouper {

        @Override
        public List<List<String>> getPathGroups(final Subdetector subdet, final IPhysicalVolume topVol) {
            final List<List<String>> pathGroups = new ArrayList<List<String>>();
            // Positive and negative endcap loop.
            for (final IDetectorElement endcaps : subdet.getDetectorElement().getChildren()) {
                // Layer loop.
                for (final IDetectorElement layer : endcaps.getChildren()) {
                    final List<String> layerPaths = new ArrayList<String>();
                    final String path = "";
                    PhysicalVolumeNavigator.getLeafPaths(layerPaths, layer.getGeometry().getPhysicalVolume(), path);
                    pathGroups.add(layerPaths);
                }
            }
            return pathGroups;
        }

    }

    /**
     * Get the path groups for a PolyconeSupport, which is a single path.
     */
    public static final class PolyconeSupportVolumeGrouper implements SubdetectorVolumeGrouper {

        @Override
        public List<List<String>> getPathGroups(final Subdetector subdet, final IPhysicalVolume topVol) {
            final List<List<String>> pathGroups = new ArrayList<List<String>>();
            final String path = "";
            final List<String> supportPath = new ArrayList<String>();
            final IPhysicalVolume supportPV = subdet.getDetectorElement().getChildren().get(0).getGeometry()
                    .getPhysicalVolume();
            PhysicalVolumeNavigator.getLeafPaths(supportPath, supportPV, path);
            pathGroups.add(supportPath);
            return pathGroups;
        }
    }

    /**
     * Get the path groups for SiTrackerEndcap2, which has modules placed directly in the tracking volume.
     */
    public static final class SiTrackerEndap2VolumeGrouper implements SubdetectorVolumeGrouper {

        @Override
        public List<List<String>> getPathGroups(final Subdetector subdet, final IPhysicalVolume topVol) {
            final List<List<String>> pathGroups = new ArrayList<List<String>>();
            // Positive and negative endcap loop.
            for (final IDetectorElement endcaps : subdet.getDetectorElement().getChildren()) {
                // Layer loop.
                for (final IDetectorElement layer : endcaps.getChildren()) {
                    final List<String> modulePaths = new ArrayList<String>();

                    // Module loop.
                    for (final IDetectorElement module : layer.getChildren()) {
                        final String path = "";
                        PhysicalVolumeNavigator.getLeafPaths(modulePaths, module.getGeometry().getPhysicalVolume(),
                                path);
                    }

                    // for (String p : modulePaths) {
                    // System.out.println("adding path: " + p);
                    // }

                    // Add module paths to this layer.
                    pathGroups.add(modulePaths);
                }
            }
            return pathGroups;
        }
    }

    /**
     * Get the path groups for SiTrackerFixedTarget2
     */
    public static final class SiTrackerFixedTarget2VolumeGrouper implements SubdetectorVolumeGrouper {

        @Override
        public List<List<String>> getPathGroups(final Subdetector subdet, final IPhysicalVolume topVol) {
            final List<List<String>> pathGroups = new ArrayList<List<String>>();
            // Positive and negative endcap loop.
            for (final IDetectorElement endcaps : subdet.getDetectorElement().getChildren()) {
                // Layer loop.
                for (final IDetectorElement layer : endcaps.getChildren()) {
                    final List<String> modulePaths = new ArrayList<String>();
                    // System.out.println(layer.getName());

                    // Module loop.
                    for (final IDetectorElement module : layer.getChildren()) {
                        final String path = "";
                        PhysicalVolumeNavigator.getLeafPaths(modulePaths, module.getGeometry().getPhysicalVolume(),
                                path);
                    }
                    // Add module paths to this layer.
                    pathGroups.add(modulePaths);
                }
            }
            return pathGroups;
        }
    }

    /**
     * Interface for getting the path groupings for different Subdetector types.
     */
    public interface SubdetectorVolumeGrouper {

        List<List<String>> getPathGroups(Subdetector subdet, IPhysicalVolume topVol);
    }

    /**
     * A UniquePV is a wrapper around IPhysicalVolumePath which provides some convenience methods and caches
     * transformations.
     */
    static class UniquePV {

        IPhysicalVolumeNavigator nav;
        IPhysicalVolumePath path;
        ITransform3D transform = null;

        /**
         * Generates a top-level UniquePV.
         *
         * @param root The top-level IPhysicalVolume
         * @param navigator The IPhysicalVolumeNavigator associated with the detector
         */
        public UniquePV(final IPhysicalVolume root, final IPhysicalVolumeNavigator navigator) {
            path = new PhysicalVolumePath();
            nav = navigator;
            path.add(root);
        }

        /**
         * Generates a UniquePV from a path. (Shallow copy of path)
         *
         * @param path
         * @param navigator
         */
        public UniquePV(final IPhysicalVolumePath path, final IPhysicalVolumeNavigator navigator) {
            this.path = path;
            nav = navigator;
        }

        /**
         * Creates a UniquePV that is a daughter of the current UniquePV (deep copy made)
         *
         * @param daughter
         * @return
         */
        public UniquePV createDaughterUniquePV(final IPhysicalVolume daughter) {
            final IPhysicalVolumePath np = new PhysicalVolumePath();
            np.addAll(path);
            np.add(daughter);
            return new UniquePV(np, nav);
        }

        /**
         * Returns the local-to-global transform
         *
         * @return an ITransform3D from local coordinates to global coordinates.
         */
        public ITransform3D getLtoGTransform() {
            if (transform == null) {
                transform = nav.getTransform(path);
            }
            return transform;
        }

        /**
         * Returns the IPhysicalVolume (the last element of the path)
         */
        public IPhysicalVolume getPV() {
            return path.getLeafVolume();
        }

        /**
         * Returns the solid associated with the physical volume.
         *
         * @return
         */
        public ISolid getSolid() {
            return this.getPV().getLogicalVolume().getSolid();
        }

        /**
         * Transforms the given vector from local to global coords.
         *
         * @param v the untransformed local Hep3Vector
         * @return the transformed global Hep3Vector
         */
        public Hep3Vector localToGlobal(final Hep3Vector v) {

            return this.getLtoGTransform().transformed(v);
        }

        @Override
        public String toString() {
            return path.toString();
        }
    }

    /**
     * A "struct" holding geometry information about lists of physical volumes
     */
    class VolumeGroupInfo {

        double rmax = 0.0;
        double rmin = 1.e10;
        double vtot = 0.0;
        double vtot_tube_only = 0.;
        double weighted_r = 0.0;
        double weighted_y = 0.0;
        double weighted_z = 0.0;
        double X0 = 0.0;
        double xmax = -1.e10;
        double xmin = 1.e10;
        double ymax = -1.e10;
        // mg 3/14/11 MaterialXPlane info
        double ymin = 1.e10;
        double zmax = -1.e10;
        double zmin = 1.e10;

    }

    /**
     * A "struct" holding geometry information about a single physical volume
     */
    class VolumeInfo {

        double rmax = 0.0;
        double rmin = 1.e10;
        double xmax = -1.e10;
        double xmin = 1.e10;
        double ymax = -1.e10;
        // mg 3/14/11 MaterialXPlane info
        double ymin = 1.e10;
        double zmax = -1.e10;
        double zmin = 1.e10;
    }

    private static ITransform3D _detToTrk;
    private static double _rmax;

    private static double _zmax = 1800.;

    private static final boolean TUBE_ONLY = false; // only use Tube elements

    public static double getRMax() {
        return _rmax;
    }

    public static double getZMax() {
        return _zmax;
    }

    private static List<UniquePV> makeUniquePVList(final IPhysicalVolumeNavigator nav,
            final IPhysicalVolume trackingVol, final List<String> paths) {
        final List<UniquePV> uniqPVs = new ArrayList<UniquePV>();
        for (final String path : paths) {
            /**
             * Create the path object, prepending tracking volume name, as the paths are relative to Subdetector.
             */
            final IPhysicalVolumePath pvPath = nav.getPath("/" + trackingVol.getName() + path);

            /**
             * Create the UniquePV for MaterialManager.
             */
            uniqPVs.add(new UniquePV(pvPath, nav));
        }
        return uniqPVs;
    }

    private final List<MaterialCylinder> _matcyl = new ArrayList<MaterialCylinder>();

    private final List<MaterialDisk> _matdsk = new ArrayList<MaterialDisk>();

    // for calculating volume.
    private final List<MaterialPolyconeSegment> _matpc = new ArrayList<MaterialPolyconeSegment>();

    private final List<MaterialXPlane> _matxpl = new ArrayList<MaterialXPlane>();

    private boolean _useDefaultXPlanes = true;

    // Variables from original MaterialManager class.
    protected boolean DEBUG = false; // enable debug output to System.out

    private final HashMap<ISolid, Double> solid_vol_map = new HashMap<ISolid, Double>(400);

    /**
     * VolumeGroup handlers for Subdetector types.
     */
    protected final Map<Class, SubdetectorVolumeGrouper> subdetGroups = new HashMap<Class, SubdetectorVolumeGrouper>();

    /**
     * Creates a new instance of MaterialManager
     */
    public MaterialManager() {
        // Barrels.
        final SubdetectorVolumeGrouper barrelGrouper = new BarrelLayerVolumeGroup();
        subdetGroups.put(SiTrackerBarrel.class, barrelGrouper);
        subdetGroups.put(MultiLayerTracker.class, barrelGrouper);

        // Endcaps.
        final SubdetectorVolumeGrouper endcapGrouper = new EndcapVolumeGrouper();
        subdetGroups.put(SiTrackerEndcap.class, endcapGrouper);
        subdetGroups.put(DiskTracker.class, endcapGrouper);

        // SiTrackerEndcap2.
        final SubdetectorVolumeGrouper endcap2Grouper = new SiTrackerEndap2VolumeGrouper();
        subdetGroups.put(SiTrackerEndcap2.class, endcap2Grouper);
        subdetGroups.put(SiTrackerSpectrometer.class, endcap2Grouper);

        // SiTrackerFixedTarget2.
        subdetGroups.put(SiTrackerFixedTarget2.class, new SiTrackerFixedTarget2VolumeGrouper());

        // PolyconeSupport.
        subdetGroups.put(PolyconeSupport.class, new PolyconeSupportVolumeGrouper());
    }

    /**
     * Calculates the VolumeGroupInfo for a set of {@link UniquePV} objects.
     *
     * @param uniqPVs
     * @param vgi
     */
    private void addVolumeGroupInfo(final List<UniquePV> uniqPVs, final VolumeGroupInfo vgi) {
        double vtot;
        if (TUBE_ONLY) {
            vtot = vgi.vtot_tube_only;
        } else {
            vtot = vgi.vtot;
        }

        // Handle Polycone.
        if (uniqPVs.get(0).getPV().getLogicalVolume().getSolid() instanceof Polycone) {
            this.handlePolycone(uniqPVs.get(0).getPV());
        }

        if (vtot > 0.) {

            // Calculate the average radiation length for this volume

            // Determine if this volume should be modeled as barrel or disk
            if (this.isXPlane(vgi.xmin, vgi.xmax)) {
                // Calculate the weighted radius of the elements
                final double zlen = vgi.zmax - vgi.zmin;
                final double ylen = vgi.ymax - vgi.ymin;
                final double thickness = vtot / (ylen * zlen * vgi.X0);
                final double x = (vgi.xmax + vgi.xmin) / 2;

                if (DEBUG) {
                    System.out.println("Treating as a XPlane...x0: " + vgi.X0 + "| zmin: " + vgi.zmin + "| zmax: "
                            + vgi.zmax + "| vtot: " + vtot + "| thickness: " + thickness + "| rmin: " + vgi.rmin
                            + "| rmax: " + vgi.rmax + "| xmin: " + vgi.xmin + "| xmax: " + vgi.xmax);
                    System.out.println();
                }
                if (!_useDefaultXPlanes) {
                    if (x > 0.1) {
                        _matxpl.add(new MaterialXPlane(vgi.ymin, vgi.ymax, vgi.zmin, vgi.zmax, x, thickness));
                    }
                } else {
                    _matxpl.add(new MaterialXPlane(vgi.ymin, vgi.ymax, vgi.zmin, vgi.zmax, x, thickness));
                }
            } else if (this.isCylinder(vgi.rmin, vgi.rmax, vgi.zmin, vgi.zmax)) {
                // Calculate the weighted radius of the elements
                final double zlen = vgi.zmax - vgi.zmin;
                final double thickness = vtot / (2. * Math.PI * vgi.weighted_r * zlen * vgi.X0);

                if (DEBUG) {
                    System.out.println("Treating as a Cylinder...x0: " + vgi.X0 + "| zmin: " + vgi.zmin + "| zmax: "
                            + vgi.zmax + "| vtot: " + vtot + "| thickness: " + thickness + "| rmin: " + vgi.rmin
                            + "| rmax: " + vgi.rmax);
                    System.out.println();
                }

                _matcyl.add(new MaterialCylinder(null, vgi.weighted_r, vgi.zmin, vgi.zmax, thickness));
            } else {

                final double thickness = vtot / (Math.PI * (vgi.rmax * vgi.rmax - vgi.rmin * vgi.rmin) * vgi.X0);

                if (DEBUG) {
                    System.out.println("x0: " + vgi.X0 + "| zmin: " + vgi.zmin + "| zmax: " + vgi.zmax + "| vtot: "
                            + vtot + "| thickness: " + thickness + "| rmin: " + vgi.rmin + "| rmax: " + vgi.rmax);
                    System.out.println();
                }

                _matdsk.add(new MaterialDisk(null, vgi.rmin, vgi.rmax, vgi.weighted_z, thickness));
            }
        }
    }

    /**
     * Build model using new VolumeGroup interface for each Subdetector type.
     */
    public void buildModel(final Detector det) {
        // Get the default navigator.
        final IPhysicalVolumeNavigator nav = PhysicalVolumeNavigatorStore.getInstance().getDefaultNavigator();

        // Get the tracking volume.
        final IPhysicalVolume trackingVol = det.getTrackingVolume();

        // Loop over subdetectors.
        for (final Subdetector subdet : det.getSubdetectorList()) {
            // Only look at Subdetectors in the tracking region.
            if (subdet.isInsideTrackingVolume()) {
                if (DEBUG) {
                    System.out.println();
                    System.out.println(">>>> " + subdet.getName() + " >>>>");
                }

                // Get the VolumeGrouper for this type.
                final SubdetectorVolumeGrouper subdetGrouper = subdetGroups.get(subdet.getClass());

                // Can't handle this type.
                if (subdetGrouper == null) {
                    System.out.println("WARNING: Can't handle Subdetector of type <"
                            + subdet.getClass().getCanonicalName() + ">.");
                } else {
                    if (DEBUG) {
                        System.out.println("Found VolumeGrouper <" + subdetGrouper.getClass().getName() + ">.");
                    }

                    // Make the list of path groups for this Subdetector.
                    final List<List<String>> pathGroups = subdetGrouper.getPathGroups(subdet, trackingVol);

                    if (DEBUG) {
                        System.out.println("Got " + pathGroups.size() + " path groups.");
                    }

                    // Loop over path groups.
                    for (final List<String> pathGroup : pathGroups) {
                        if (DEBUG) {
                            System.out.println("Adding next " + pathGroup.size() + " paths.");
                        }

                        // Make the UniquePV list expected by MaterialManager.
                        final List<UniquePV> uniqPVs = makeUniquePVList(nav, trackingVol, pathGroup);

                        // Calculate VolumeGroupInfo for this path group.
                        final VolumeGroupInfo vgi = this.performVolumeGroupCalculations(uniqPVs);

                        // Debug print.
                        if (DEBUG) {
                            System.out.println("VolumeGroupInfo ...");
                            System.out.println("    rmax = " + vgi.rmax);
                            System.out.println("    rmin = " + vgi.rmin);
                            System.out.println("    xmin = " + vgi.xmin);
                            System.out.println("    xmax = " + vgi.xmax);
                            System.out.println("    ymin = " + vgi.ymin);
                            System.out.println("    ymax = " + vgi.ymax);
                            System.out.println("    zmin = " + vgi.zmin);
                            System.out.println("    zmax = " + vgi.zmax);
                            System.out.println("    X0 = " + vgi.X0);
                            System.out.println("    weighted_r = " + vgi.weighted_r);
                            System.out.println("    weighted_z = " + vgi.weighted_z);
                            System.out.println("    weighted_z = " + vgi.weighted_y);
                            System.out.println("    vtot_tube_only = " + vgi.vtot_tube_only);
                            System.out.println("    vtot = " + vgi.vtot);
                        }

                        // Add the VolumeGroupInfo, which will setup the
                        // material representation for this set of volumes.
                        this.addVolumeGroupInfo(uniqPVs, vgi);
                    }
                }
            }
        }

        // Setup the tracking volume.
        this.setupTrackingVolume(det);
    }

    public List<MaterialCylinder> getMaterialCylinders() {
        return _matcyl;
    }

    public List<MaterialDisk> getMaterialDisks() {
        return _matdsk;
    }

    public List<MaterialPolyconeSegment> getMaterialPolyconeSegments() {
        return _matpc;
    }

    public List<MaterialXPlane> getMaterialXPlanes() {
        return _matxpl;
    }

    private double getVolumeOfSolid(final ISolid solid) {
        if (solid_vol_map.containsKey(solid)) {
            return solid_vol_map.get(solid).doubleValue();
        } else {
            double vol;
            try {
                vol = solid.getCubicVolume();
            } catch (final Exception e) {
                vol = 0.0;
            }

            solid_vol_map.put(solid, vol);
            return vol;
        }

    }

    // special handling for Polycone...
    private void handlePolycone(final IPhysicalVolume pv) {
        final Polycone pc = (Polycone) pv.getLogicalVolume().getSolid();
        final IMaterial mat = pv.getLogicalVolume().getMaterial();

        // Loop through each segment
        for (int i = 0; i < pc.getNumberOfZPlanes() - 1; i++) {
            final ZPlane zp1 = pc.getZPlane(i);
            final ZPlane zp2 = pc.getZPlane(i + 1);

            final double z1 = zp1.getZ();
            final double z2 = zp2.getZ();
            final double vol = Polycone.getSegmentVolume(zp1, zp2);
            final double zlen = Math.abs(z2 - z1);
            final double ravg = 0.25 * (zp1.getRMax() + zp1.getRMin() + zp2.getRMax() + zp2.getRMin());
            final double ang = Math.atan2(0.5 * (zp1.getRMax() + zp1.getRMin() - zp2.getRMax() - zp2.getRMin()), zlen);
            final double X0 = 10 * mat.getRadiationLength() / mat.getDensity();
            final double thickness = Math.cos(ang) * vol / (2 * Math.PI * ravg * zlen * X0);

            // This is a cylinder
            if (zp1.getRMax() == zp2.getRMax() && zp1.getRMin() == zp2.getRMin()) {
                _matcyl.add(new MaterialCylinder(pv, ravg, Math.min(z1, z2), Math.max(z1, z2), thickness));
                if (DEBUG) {
                    System.out.println("Cylindrical segment of " + pv.getName());
                    System.out.println("zmin = " + z1 + "| zmax = " + z2 + "| ravg = " + ravg + "| thickness = "
                            + thickness);
                }

            } // Otherwise this is a non-cylindrical polycone segment
            else {
                _matpc.add(new MaterialPolyconeSegment(pv, zp1, zp2, thickness, ang));
                if (DEBUG) {
                    System.out.println("Non-Cylindrical segment of " + pv.getName());
                    System.out.println("ZPlane 1: " + zp1.toString() + "| ZPlane 2: " + zp2.toString()
                            + "| thickness = " + thickness);
                }

            }
        }
    }

    private boolean isCylinder(final double rmin, final double rmax, final double zmin, final double zmax) {
        return (rmax - rmin) * Math.abs(zmax + zmin) < (zmax - zmin) * (rmax + rmin);
    }

    private boolean isXPlane(final double xmin, final double xmax) {
        if (!_useDefaultXPlanes) {
            if (xmax - xmin < 0) {
                return false; // be default xmax is negative, xmin is positive
            }
            if (xmax + xmin < 50) {
                return false; // catch short modules...
            }
            return xmax - xmin < 50.0;// 5cm...
        } else {
            return xmax - xmin < 1.0;// 1mm
        }
    }

    private VolumeInfo performVolumeCalculations(final UniquePV pv) {

        final VolumeInfo vi = new VolumeInfo();
        final ISolid solid = pv.getSolid();

        // ASSUMPTION: tube is along z-axis and has center at r = 0
        if (solid instanceof Tube) {
            final Tube tube = (Tube) solid;
            final double z0 = pv.getLtoGTransform().getTranslation().z();
            vi.zmax = z0 + tube.getZHalfLength();
            vi.zmin = z0 - tube.getZHalfLength();
            vi.rmin = tube.getInnerRadius();
            vi.rmax = tube.getOuterRadius();
        } else if (solid instanceof Box) {
            final Box box = (Box) solid;
            for (final Point3D p : box.getVertices()) {
                final Hep3Vector transformed = pv.localToGlobal(p.getHep3Vector());
                if (_detToTrk != null) {
                    _detToTrk.transform(transformed);
                }
                vi.zmin = Math.min(transformed.z(), vi.zmin);
                vi.zmax = Math.max(transformed.z(), vi.zmax);
                final double r = Math.sqrt(transformed.x() * transformed.x() + transformed.y() * transformed.y());
                vi.rmin = Math.min(vi.rmin, r);
                vi.rmax = Math.max(vi.rmax, r);
                // mg 1/23/12 also store ymin,ymax
                vi.ymin = Math.min(transformed.y(), vi.ymin);
                vi.ymax = Math.max(transformed.y(), vi.ymax);
                vi.xmin = Math.min(transformed.x(), vi.xmin);
                vi.xmax = Math.max(transformed.x(), vi.xmax);

            }

        } else if (solid instanceof Trd) {
            final Trd box = (Trd) solid;
            for (final Point3D p : box.getVertices()) {
                final Hep3Vector transformed = pv.localToGlobal(p.getHep3Vector());
                if (_detToTrk != null) {
                    _detToTrk.transform(transformed);
                }
                vi.zmin = Math.min(transformed.z(), vi.zmin);
                vi.zmax = Math.max(transformed.z(), vi.zmax);
                final double r = Math.sqrt(transformed.x() * transformed.x() + transformed.y() * transformed.y());
                vi.rmin = Math.min(vi.rmin, r);
                vi.rmax = Math.max(vi.rmax, r);
                // mg 3/14/11 also store ymin,ymax
                vi.ymin = Math.min(transformed.y(), vi.ymin);
                vi.ymax = Math.max(transformed.y(), vi.ymax);
                vi.xmin = Math.min(transformed.x(), vi.xmin);
                vi.xmax = Math.max(transformed.x(), vi.xmax);
            }
        } // Note: this information will NOT be used most of the time...
        // Polycones that are top-level elements (e.g. the beampipe) are
        // handled specially (since the radiation length is a function of z).
        // The information here will only be used in case a top-level element
        // has a subelement that is a Polycone, in which case it'll be
        // approximated as the smallest possible cylinder.
        else if (solid instanceof Polycone) {
            final Polycone pc = (Polycone) solid;
            final List<Polycone.ZPlane> zplanes = pc.getZPlanes();

            // For now, just take the minimum rmin and rmax of the polycone
            for (final Polycone.ZPlane z : zplanes) {
                if (z.getRMax() > 0 && z.getRMin() > 0) {
                    vi.rmin = Math.min(vi.rmin, z.getRMin());
                    vi.rmax = vi.rmax > 0. ? Math.min(vi.rmax, z.getRMax()) : z.getRMax();
                }

            }

            vi.zmin = pc.getZPlanes().get(0).getZ();
            vi.zmax = pc.getZPlanes().get(pc.getZPlanes().size() - 1).getZ();

            // check for wrong order
            if (vi.zmin > vi.zmax) {
                final double temp = vi.zmin;
                vi.zmin = vi.zmax;
                vi.zmax = temp;
            }

        }

        return vi;
    }

    // This function performs all the calculations on lists of physical volumes
    private VolumeGroupInfo performVolumeGroupCalculations(final List<UniquePV> volgroup) {

        final VolumeGroupInfo vgi = new VolumeGroupInfo();

        // If we have a top-level polycone, don't bother doing anything, because
        // it'll be handled specially
        if (volgroup.size() == 1 && volgroup.get(0).getSolid() instanceof Polycone) {
            return vgi;
        }

        // The normal case
        double totwgt = 0.0;
        if (DEBUG && volgroup.isEmpty()) {
            System.out.println("Empty volume group...");
        }
        for (final UniquePV pv : volgroup) {

            // increment total volume
            final double vol = this.getVolumeOfSolid(pv.getSolid());
            if (pv.getSolid() instanceof Tube) {
                vgi.vtot_tube_only += vol;
            }
            vgi.vtot += vol;
            // calculate weighted R / Z / Radiation Length
            final VolumeInfo vi = this.performVolumeCalculations(pv);
            final IMaterial mat = pv.getPV().getLogicalVolume().getMaterial();
            final double matX0 = 10.0 * mat.getRadiationLength() / mat.getDensity();
            final double wgt = vol / matX0;
            final double z0 = pv.getLtoGTransform().getTranslation().z();
            vgi.weighted_r += 0.5 * (vi.rmin + vi.rmax) * wgt;
            vgi.weighted_z += z0 * wgt;
            totwgt += wgt;

            // grab (z/r)(mins/maxes)
            vgi.zmin = Math.min(vi.zmin, vgi.zmin);
            vgi.zmax = Math.max(vi.zmax, vgi.zmax);
            vgi.rmin = Math.min(vi.rmin, vgi.rmin);
            vgi.rmax = Math.max(vi.rmax, vgi.rmax);
            // mg 3/14/11 also store y,x information
            vgi.ymin = Math.min(vi.ymin, vgi.ymin);
            vgi.ymax = Math.max(vi.ymax, vgi.ymax);
            final double y0 = pv.getLtoGTransform().getTranslation().y();
            vgi.weighted_y += y0 * wgt;
            vgi.xmin = Math.min(vi.xmin, vgi.xmin);
            vgi.xmax = Math.max(vi.xmax, vgi.xmax);
        }

        // finish weighted R/Z calculations + perform X0 calculation
        if (totwgt > 0.) {
            vgi.weighted_r /= totwgt;
            vgi.weighted_z /= totwgt;
            vgi.X0 = vgi.vtot / totwgt;
            // mg 3/14/11 also y info
            vgi.weighted_y /= totwgt;
        }

        return vgi;
    }

    /**
     * Turn on/off debugging output.
     *
     * @param debug True if debugging should be enabled; false if not.
     */
    public void setDebug(final boolean debug) {
        this.DEBUG = debug;
    }

    public void setDefaultXPlaneUsage(final boolean useDefault) {
        _useDefaultXPlanes = useDefault;
    }

    public void setTransform(final ITransform3D transform) {
        _detToTrk = transform;
    }

    /**
     * Setup tracking volume parameters.
     *
     * @param det The Detector.
     */
    private void setupTrackingVolume(final Detector det) {
        // Find the envelope of the tracking volume
        final ISolid trkvol = det.getTrackingVolume().getLogicalVolume().getSolid();
        if (trkvol instanceof Tube) {
            final Tube trktube = (Tube) trkvol;
            _rmax = trktube.getOuterRadius();
            _zmax = trktube.getZHalfLength();
            if (DEBUG) {
                System.out.println("Ecal radius = " + _rmax);
                System.out.println("ECal inner Z = " + _zmax);
            }
        }
    }
}
