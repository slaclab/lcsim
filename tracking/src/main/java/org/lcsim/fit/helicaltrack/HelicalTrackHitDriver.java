/*
 * HelicalTrackHitDriver.java
 *
 * Created on November 30, 2007, 3:50 PM
 *
 */

package org.lcsim.fit.helicaltrack;

import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.solids.LineSegment3D;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.detector.tracker.silicon.SiTrackerModule;
import org.lcsim.event.base.MyLCRelation;
import org.lcsim.event.EventHeader;
import org.lcsim.event.LCRelation;
import org.lcsim.event.MCParticle;
import org.lcsim.event.RawTrackerHit;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.event.TrackerHit;
import org.lcsim.event.base.BaseTrackerHitMC;
import org.lcsim.event.base.BaseHit;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;
import org.lcsim.recon.tracking.digitization.sisim.SiTrackerHit;
import org.lcsim.recon.tracking.digitization.sisim.SiTrackerHitStrip1D;
import org.lcsim.recon.tracking.digitization.sisim.TrackerHitType.CoordinateSystem;
import org.lcsim.recon.tracking.vsegment.geom.SegmentationManager;
import org.lcsim.recon.tracking.vsegment.geom.Sensor;
import org.lcsim.recon.tracking.vsegment.geom.SensorType;
import org.lcsim.recon.tracking.vsegment.geom.sensortypes.Cylinder;
import org.lcsim.recon.tracking.vsegment.hit.DigiTrackerHit;
import org.lcsim.recon.tracking.vsegment.hit.TrackerCluster;
import org.lcsim.spacegeom.SpacePointVector;
import org.lcsim.util.Driver;

/**
 * Create the appropriate HelicalTrackHits for the specified TrackerHit
 * collections.  The resulting HelicalTrackHits encapsulate the information
 * needed to perform a helical track hit for either a segmented strip
 * detector, a pixel detector, or cross hits from a stereo detector.
 *
 * At this time, this driver supports the virtual segmentation hits
 * produced by the packages in contrib.onoprien.tracking.  Additional
 * coding needs to be done to support fully segmented detectors.
 *
 * The list of hit collections to be converted must be specified
 * before the process method is executed.
 *
 * Currently,
 * @author Richard Partridge
 * @version 1.0
 */
public class HelicalTrackHitDriver extends Driver {
    /**
     * Type of hits to be converted/
     */
    public enum HitType {
        /**
         * Anything that uses BaseTrackerHit or BaseTrackerHitMC, for examples
         * hits created by PixSim or TrackerHitCheater
         */
        Base,
        /**
         * Virtual segmentation (OldTrackerHit) hits.
         */
        VirtualSegmentation,
        /**
         *
         *  Digitized (SiTrackerHit)
         */
        Digitized
    }
    protected StereoHitMaker _crosser = new StereoHitMaker(2., 10.);
    protected HitIdentifier _ID = new HitIdentifier();
    private SegmentationManager _segman;
    private List<String> _vscol = new ArrayList<String>();
    private List<String> _bscol = new ArrayList<String>();
    private List<String> _digcol = new ArrayList<String>();
    protected String _outname = "HelicalTrackHits";
    protected String _hitrelname = "HelicalTrackHitRelations";
    protected String _mcrelname = "HelicalTrackMCRelations";
    private Hep3Vector _uloc = new BasicHep3Vector(1., 0., 0.);
    private Hep3Vector _vloc = new BasicHep3Vector(0., 1., 0.);
    private Hep3Vector _zhat = new BasicHep3Vector(0., 0., 1.);
    protected Hep3Vector _orgloc = new BasicHep3Vector(0., 0., 0.);
    private double _eps = 1.0e-6;
    
    /** Creates a new instance of HelicalTrackHitDriver */
    public HelicalTrackHitDriver() {
    }
    
    /**
     * Create the HelicalTrackHits for the specified hit collections.
     * @param event EventHeader of the event to be processed
     */
    @Override
    public void process(EventHeader event) {
        super.process(event);

        //  Initialize the list of HelicalTrackHits and vector with local z direction
        List<HelicalTrackHit> helhits = new ArrayList<HelicalTrackHit>();
        Hep3Vector lz = new BasicHep3Vector(0., 0., 1.);
        
        //  Create a List of LCRelations to relate HelicalTrackHits to the original hits
        List<LCRelation> hitrelations = new ArrayList<LCRelation>();
        
        for (String colname : _bscol) {
            List<TrackerHit> hitlist = (List<TrackerHit>) event.get(colname);
            for (TrackerHit hit : hitlist) {
                Hep3Vector pos = new BasicHep3Vector(hit.getPosition());
                SymmetricMatrix cov = new SymmetricMatrix(3, hit.getCovMatrix(), true);
                TrackerHit ohit = hit;
                
//                try {
                    //create a BaseTrackerHitMC from a BaseTrackerHit if necessary
                    if (!(hit instanceof BaseTrackerHitMC)){
                        List<SimTrackerHit> thesehits = new ArrayList<SimTrackerHit>();
                        List<RawTrackerHit> rawhits = (List<RawTrackerHit>)hit.getRawHits();
                        for (RawTrackerHit raw : rawhits) {
                            if(raw.getSimTrackerHits() != null)
                             thesehits.addAll(raw.getSimTrackerHits());
                        }
                        hit = new BaseTrackerHitMC(hit.getPosition(), hit.getCovMatrix(),
                                hit.getTime(), hit.getdEdx(), hit.getType(), thesehits);
                        ((BaseTrackerHitMC)hit).addRawTrackerHits(rawhits);
                        
                    if(thesehits.size() != 0)
                    {
                    SimTrackerHit simhit = ((BaseTrackerHitMC)hit).getSimHits().get(0);
                    HelicalTrackHit hthit = new HelicalTrack3DHit(pos, cov, hit.getdEdx(), hit.getTime(), hit.getRawHits(),
                                _ID.getName(simhit), _ID.getLayer(simhit), _ID.getBarrelEndcapFlag(simhit));
//                    IDetectorElementContainer cont = DetectorElementStore.getInstance().find(simhit.getIdentifier());
//                    if (cont.isEmpty()) {
//                        throw new RuntimeException("Detector Container is empty!");
//                    } else {
//                        IDetectorElement de = cont.get(0);
                        
                    for (MCParticle p : ((BaseTrackerHitMC)hit).mcParticles()) hthit.addMCParticle(p);
                    hitrelations.add(new MyLCRelation(hthit, ohit));
                    helhits.add(hthit);
                    }
                    else
                    {
                     if(rawhits.size() != 0)
                     {
                      IDetectorElement de = ((BaseHit) rawhits.get(0)).getDetectorElement();
                    HelicalTrackHit hthit = new HelicalTrack3DHit(pos, cov, hit.getdEdx(), hit.getTime(), hit.getRawHits(),
                                _ID.getName(de), _ID.getLayer(de), _ID.getBarrelEndcapFlag(de));
                     hitrelations.add(new MyLCRelation(hthit, ohit));
                     helhits.add(hthit);
      
                     }
                     else throw new RuntimeException("No way to identify de");
                    } 
                   }
//                }
//
//                } catch(Exception e) {
//                    System.out.println("Warning, could not complete Identification for smeared hits. Reason: "+e.getMessage());
//                    HelicalTrackHit hthit = new HelicalTrack3DHit(pos, cov, hit.getdEdx(), hit.getTime(),
//                            hit.getRawHits(), "Unknown", 0, BarrelEndcapFlag.BARREL);
//                    hitrelations.add(new MyLCRelation(hthit, ohit));
//                    helhits.add(hthit);
//                }
            }
        }
        
        //  Loop over the collections of hits with virtual segmentation
        for (String colname : _vscol) {

            //  Get the segmentation manager if we haven't already done so
            if (_segman == null) _segman = (SegmentationManager) event.get("SegmentationManager");
            
            //  Make a mapping between the stereo strips and corresponding hits
            Map<HelicalTrackStrip, org.lcsim.recon.tracking.vsegment.hit.TrackerHit> stripmap =
                    new HashMap<HelicalTrackStrip, org.lcsim.recon.tracking.vsegment.hit.TrackerHit>();
            
            //  Get the hit map that gives the list of hits on a particular sensor
            Map<Sensor, List<org.lcsim.recon.tracking.vsegment.hit.TrackerHit>> hitmap =
                    (Map<Sensor, List<org.lcsim.recon.tracking.vsegment.hit.TrackerHit>>) event.get(colname);
            
            //  Loop over sensors in the hit map
            for (Sensor sensor : hitmap.keySet()) {
                //  Get a list of hits for this sensor and loop over the hits
                List<org.lcsim.recon.tracking.vsegment.hit.TrackerHit> sensorhits = hitmap.get(sensor);
                for (org.lcsim.recon.tracking.vsegment.hit.TrackerHit hit : sensorhits) {
                    //  Convert to an old tracker hit for reference
                    ArrayList<TrackerCluster> parents = new ArrayList<TrackerCluster>(1);
                    parents.add(hit.getCluster());
                    //  Check if we have a strip or pixel hit
                    if (sensor.getType().getHitDimension() == 1) {
                        //  Strip hit - now check if this hit is part of a stereo pair
                        if (_segman.getStereoPartners(sensor) == null) {
                            //  Isolated axial hit - convert it and add to the hit list
                            HelicalTrackHit axialhit = MakeAxialHit(hit);
                            if (axialhit != null) {
                                hitrelations.add(new MyLCRelation(axialhit, hit));
                                helhits.add(axialhit);
                            }
                        } else {
                            //  Stereo hit - convert it and add it to the list of stereo strips
                            HelicalTrackStrip strip = MakeStrip(hit);
                            stripmap.put(strip, hit);
                        }
                    } else {
                        //  Pixel hit - convert it and add to the hit list
                        HelicalTrackHit pixelhit = MakePixelHit(hit);
                        hitrelations.add(new MyLCRelation(pixelhit, hit));
                        helhits.add(pixelhit);
                    }
                }
            }
            
            //  Make the stereo hits
            List<HelicalTrackStrip> striplist = new ArrayList<HelicalTrackStrip>(stripmap.keySet());
            List<HelicalTrackCross> stereohits = _crosser.MakeHits(striplist);
            for (HelicalTrackCross hit : stereohits) {
                for (HelicalTrackStrip strip : hit.getStrips()) {
                    hitrelations.add(new MyLCRelation(hit, stripmap.get(strip)));
                }
                helhits.add(hit);
            }
        }

        //  Loop over the collections of hits produced by the sisim digitization code
        for (String colname : _digcol) {

            //  Get the list of SiTrackerHits for this collection
            List<SiTrackerHit> hitlist = (List<SiTrackerHit>) event.get(colname);

            //  Create collections for modules, strip hits by sensor, and hit cross references
            Set<SiTrackerModule> modules = new HashSet<SiTrackerModule>();
            Map<SiSensor, List<HelicalTrackStrip>> sensormap = new HashMap<SiSensor, List<HelicalTrackStrip>>();
            Map<HelicalTrackStrip, SiTrackerHitStrip1D> stripmap = new HashMap<HelicalTrackStrip, SiTrackerHitStrip1D>();

            //  Loop over the SiTrackerHits in this collection
            for (SiTrackerHit hit : hitlist) {
                
                if (hit instanceof SiTrackerHitStrip1D) {
                    //determine if the hit is stereoed or not
                    SiTrackerHitStrip1D h = (SiTrackerHitStrip1D) hit;
                    
                    //  Get the sensor and parent modules
                    SiSensor sensor = h.getSensor();
                    SiTrackerModule m = (SiTrackerModule) sensor.getParent();

                    //  Check if we have a stereo hit (i.e., the module has 2 children)
                    if (m.getChildren().size()==2) {
                        
                        //  Add the module to the set of modules containing stereo hits
                        modules.add(m);

                        //  Create a HelicalTrackStrip for this hit
                        HelicalTrackStrip strip = makeDigiStrip(h);

                        //  Get the list of strips for this module - create a new list if one doesn't already exist
                        List<HelicalTrackStrip> modhits = sensormap.get(sensor);
                        if (modhits == null) {
                            modhits = new ArrayList<HelicalTrackStrip>();
                            sensormap.put(sensor, modhits);
                        }
                        
                        //  Add the strip to the list of strips on this sensor
                        modhits.add(strip);
                        
                        //  Map a reference back to the hit needed to create the stereo hit LC relations
                        stripmap.put(strip, h);

                    } else {
                        //System.out.println("trying to make an axial hit???");
                        HelicalTrackHit dah = makeDigiAxialHit(h);
                        helhits.add(dah); //isolated hit
                        hitrelations.add(new MyLCRelation(dah,hit));
                    }
                }
                //for other types, make 3d hits
                else {
                    HelicalTrackHit hit3d = makeDigi3DHit(hit);
                    helhits.add(hit3d);
                    hitrelations.add(new MyLCRelation(hit3d, hit));
                }
            }
            //  Now create the stereo hits
            //  Create a list of stereo hits
            List<HelicalTrackCross> stereohits = new ArrayList<HelicalTrackCross>();

            //  Loop over the modules with hits
            for (SiTrackerModule m : modules) {

                //  Make sure we have 2 sensors, and get the sensors for this module
                if (m.getChildren().size() != 2) continue;
                SiSensor sensor1 = (SiSensor) m.getChildren().get(0);
                SiSensor sensor2 = (SiSensor) m.getChildren().get(1);

                //  Form the stereo hits and add them to our hit list
                stereohits.addAll(_crosser.MakeHits(sensormap.get(sensor1), sensormap.get(sensor2)));
            }

            helhits.addAll(stereohits);
            
            //add LCRelation for strip hits
            for (HelicalTrackCross cross : stereohits) {
                for (HelicalTrackStrip strip : cross.getStrips()) {
                    hitrelations.add(new MyLCRelation(cross,stripmap.get(strip)));
                }
            }
            
        }
        
        //  Create the LCRelations between HelicalTrackHits and MC particles
        List<LCRelation> mcrelations = new ArrayList<LCRelation>();
        for (HelicalTrackHit hit : helhits) {
            for (MCParticle mcp : hit.getMCParticles()) {
                mcrelations.add(new MyLCRelation(hit, mcp));
            }
        }
        
        //  Put the HelicalTrackHits back into the event
        event.put(_outname, helhits, HelicalTrackHit.class, 0);
        event.put(_hitrelname, hitrelations, LCRelation.class, 0);
        event.put(_mcrelname, mcrelations, LCRelation.class, 0);
        return;
    }
    
    /**
     * Add a TrackerHit collection to be processed.
     * @param name Name of the hit collection
     * @param type Type of collection
     */
    public void addCollection(String name, HitType type) {
        if (type == HitType.VirtualSegmentation) {
            _vscol.add(name);
        } else if (type == HitType.Base) {
            _bscol.add(name);
        } else if (type == HitType.Digitized)
            _digcol.add(name);
        return;
    }
    
    public void setDigiCollectionName(String name)
    {
    	_digcol.add(name);
    }
    
    public void setDigiCollectionNames(String names[])
    {
    	_digcol.addAll(Arrays.asList(names));
    }
    
    public void setVirtualSegmentationCollectionName(String name)
    {
    	_vscol.add(name);
    }
    
    public void setVirtualSegmentationCollectionNames(String names[])
    {
    	_vscol.addAll(Arrays.asList(names));
    }
    
    public void setBaseCollectionName(String name)
    {
    	_bscol.add(name);
    }
    
    public void setBaseCollectionNames(String names[])
    {
    	_bscol.addAll(Arrays.asList(names));
    }
            
    /**
     * Name of the HelicalTrackHit collection to be put back in the event.
     * @param outname Name to use for the HelicalTrackHit collection
     */
    public void OutputCollection(String outname) {
        _outname = outname;
        return;
    }
    
    public void setOutputCollectionName(String outname) {
        _outname = outname;
        return;
    }
    
    public void HitRelationName(String hitrelname) {
        _hitrelname = hitrelname;
        return;
    }
    
    public void MCRelationName(String mcrelname) {
        _mcrelname = mcrelname;
        return;
    }

    public void setMaxSeperation(double maxsep) {
        _crosser.setMaxSeparation(maxsep);
        return;
    }

    public void setTolerance(double tolerance) {
        _crosser.setTolerance(tolerance);
        return;
    }
    
    private HelicalTrackHit MakeAxialHit(org.lcsim.recon.tracking.vsegment.hit.TrackerHit hit) {
        HelicalTrackStrip strip = MakeStrip(hit);
        if (VecOp.cross(strip.v(), _zhat).magnitude() > _eps) return null;
        double zmin = VecOp.add(HitUtils.StripCenter(strip), VecOp.mult(strip.vmin(), strip.v())).z();
        double zmax = VecOp.add(HitUtils.StripCenter(strip), VecOp.mult(strip.vmax(), strip.v())).z();
        HelicalTrackHit axialhit = new HelicalTrack2DHit(strip.origin(), HitUtils.StripCov(strip),
                strip.dEdx(), strip.time(), strip.rawhits(), strip.detector(), strip.layer(), strip.BarrelEndcapFlag(),
                zmin, zmax);
        List<MCParticle> mcplist = getMCParticles(hit.getCluster());
        for (MCParticle mcp : mcplist) {
            axialhit.addMCParticle(mcp);
        }
        return axialhit;
    }
    
    private HelicalTrackStrip MakeStrip(org.lcsim.recon.tracking.vsegment.hit.TrackerHit hit) {
        Hep3Vector u;
        Hep3Vector v;
        Hep3Vector org;
        double umeas;
        double du;
        double vmin;
        double vmax;
        Sensor s = hit.getSensor();
        SensorType stype = s.getType();
        if  (stype instanceof Cylinder) {
            SpacePointVector seg = hit.getSegment();
            v = VecOp.unit(seg.getDirection());
            Hep3Vector r = new BasicHep3Vector(hit.getPosition().x(), hit.getPosition().y(), 0.0);
            u = VecOp.unit(VecOp.cross(v, r));
            umeas = 0.;
            du = r.magnitude() * Math.sqrt(hit.getLocalCovMatrix().diagonal(0));
            org = VecOp.mult(0.5, VecOp.add(seg.getStartPoint(), seg.getEndPoint()));
            vmax = seg.getDirection().magnitude() / 2.;
            vmin = -vmax;
        } else {
            org = s.localToGlobal(_orgloc);
            u = VecOp.sub(s.localToGlobal(_uloc), org);
            v = VecOp.sub(s.localToGlobal(_vloc), org);
            umeas = hit.getLocalPosition().x();
            du = Math.sqrt(hit.getLocalCovMatrix().diagonal(0));
            vmin = hit.getLocalSegment().getStartPoint().y();
            vmax = hit.getLocalSegment().getEndPoint().y();
        }
        
        double dEdx = hit.getSignal();
        double time = hit.getTime();
        IDetectorElement de = s.getDetectorElement();
        String det = _ID.getName(de);
        int lyr = _ID.getLayer(de);
        if (_segman.getStereoPartners(s) != null) lyr = lyr / 2;
        BarrelEndcapFlag beflag = _ID.getBarrelEndcapFlag(de);
        
        HelicalTrackStrip strip = new HelicalTrackStrip(org, u, v, umeas, du, vmin, vmax, dEdx, time, null,
                det, lyr, beflag);
        List<MCParticle> mcplist = getMCParticles(hit.getCluster());
        for (MCParticle mcp : mcplist) {
            strip.addMCParticle(mcp);
        }
        return strip;
    }
    
    private HelicalTrackHit MakePixelHit(org.lcsim.recon.tracking.vsegment.hit.TrackerHit hit) {
        IDetectorElement de = hit.getSensor().getDetectorElement();
        HelicalTrackHit pixel = new HelicalTrack3DHit(hit.getPosition(), hit.getCovMatrix(), hit.getSignal(),
                hit.getTime(), null, _ID.getName(de), _ID.getLayer(de), _ID.getBarrelEndcapFlag(de));
        List<MCParticle> mcplist = getMCParticles(hit.getCluster());
        for (MCParticle mcp : mcplist) {
            pixel.addMCParticle(mcp);
        }
        
        return pixel;
    }
    
    private List<MCParticle> getMCParticles(TrackerCluster cluster) {
        List<MCParticle> mcplist = new ArrayList<MCParticle>();
        for (DigiTrackerHit dhit : cluster.getDigiHits()) {
            //  Get the elemental hits - not sure what the dif is...
            for (DigiTrackerHit dhit2 : dhit.getElementalHits()) {
                //  Get the MCParticle and add it to the hit
                MCParticle mcp = dhit2.getMCParticle();
                if (mcp != null) mcplist.add(mcp);
            }
        }
        return mcplist;
    }
    
    protected HelicalTrackHit makeDigi3DHit(SiTrackerHit h) {
        
        IDetectorElement de = h.getSensor();
        int lyr = _ID.getLayer(de);
        BarrelEndcapFlag be = _ID.getBarrelEndcapFlag(de);
        
        HelicalTrackHit hit = new HelicalTrack3DHit(h.getPositionAsVector(),
                h.getCovarianceAsMatrix(), h.getdEdx(), h.getTime(),
                h.getRawHits(), _ID.getName(de), lyr, be);
        
        for (MCParticle p : h.getMCParticles()) hit.addMCParticle(p);
        
        return hit;
        
    }
    
    private HelicalTrackHit makeDigiAxialHit(SiTrackerHitStrip1D h){
        
        double z1 = h.getHitSegment().getEndPoint().z();
        double z2 = h.getHitSegment().getStartPoint().z();
        double zmin = Math.min(z1,z2);
        double zmax = Math.max(z1,z2);
        IDetectorElement de = h.getSensor();
        
        HelicalTrackHit hit = new HelicalTrack2DHit(h.getPositionAsVector(),
                h.getCovarianceAsMatrix(), h.getdEdx(), h.getTime(),
                h.getRawHits(), _ID.getName(de), _ID.getLayer(de),
                _ID.getBarrelEndcapFlag(de), zmin, zmax);
        
        for (MCParticle p : h.getMCParticles()) hit.addMCParticle(p);
        return hit;
    }
    
    
    private HelicalTrackStrip makeDigiStrip(SiTrackerHitStrip1D h){

        //  Get the tranform from global coordinates to electrode coordinates
        //  We will use this to get the local hit coordinates and measurement error
        ITransform3D trans = h.getReadoutElectrodes().getGlobalToLocal();

        //  Get the global coordinates of the electrode origin
        Hep3Vector org = h.getReadoutElectrodes().getLocalToGlobal().transformed(_orgloc);

        //  Get the unit vectors for the measured and unmeasured directions
        Hep3Vector u = h.getMeasuredCoordinate();
        Hep3Vector v = h.getUnmeasuredCoordinate();

        //  Get the measured coordinate (i.e., the distance from the origin along the u direction)
        double umeas = trans.transformed(h.getPositionAsVector()).x();

        //  Get the limits on the unmeasured coordinate
        LineSegment3D vseg = h.getHitSegment();
        double vmin = trans.transformed(vseg.getStartPoint()).y();
        double vmax = trans.transformed(vseg.getEndPoint()).y();

        //  Get the error on the measured coordinate
        double du = Math.sqrt(trans.rotated(h.getCovarianceAsMatrix()).diagonal(0));

        //  Get the identifier information
        IDetectorElement de = h.getSensor();
        String det = _ID.getName(de);
        int lyr = _ID.getLayer(de);
        BarrelEndcapFlag be = _ID.getBarrelEndcapFlag(de);

        //  Get the energy deposit, time, and raw hits from the SiTrackerHitStrip1D
        double dEdx = h.getdEdx();
        double time = h.getTime();
        List<RawTrackerHit> rawhits = h.getRawHits();

        //  Create a new HelicalTrackStrip
        HelicalTrackStrip strip = new HelicalTrackStrip(org, u, v, umeas, du,
                vmin, vmax, dEdx, time, rawhits, det, lyr, be);

        //  Save the MCParticles associated with this hit
        for (MCParticle p : h.getMCParticles()) strip.addMCParticle(p);

        //  Done!
        return strip;
    }
    
}
