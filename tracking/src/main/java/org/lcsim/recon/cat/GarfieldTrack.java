package org.lcsim.recon.cat;

import java.util.*;

import hep.physics.matrix.SymmetricMatrix;
import org.lcsim.conditions.ConditionsListener;
import org.lcsim.conditions.ConditionsEvent;
import org.lcsim.conditions.ConditionsManager;
import org.lcsim.conditions.ConditionsManager.ConditionsSetNotFoundException;
import org.lcsim.event.*;
import org.lcsim.geometry.Detector;
import org.lcsim.util.Driver;

import org.lcsim.recon.cat.util.Const;


/**
 * Simple track format for linear collider detector.
 * Used for calorimeter assisted tracking with the SiD.
 * An example on how to create a track from 3D position measurements,
 * please see {@link GarfieldTrackFinder}::testGenericTrackFit()
 *
 * @see GarfieldHit
 * @see GarfieldHelix
 *
 * @author  E. von Toerne
 * @author  D. Onoprienko
 * @version $Id: GarfieldTrack.java,v 1.4 2012/07/30 15:35:34 grefe Exp $
 */
public class GarfieldTrack implements Track {
  
  // -- Data :  ----------------------------------------------------------------
  
  public ArrayList hits;
  public GarfieldHelix hel;
  private double[] trackPara;
  private double minRadius;
  private double maxRadius;
  private String status;
  private MipStub stub;
  private int ID=0;
  public int ndf;
  private double chi2;
  private double grade; // grade for track: high= good, low = bad
  public int lastLayer;
  public int nLayerMissed;
  private boolean isEndcap=false;
  private boolean hasZ = false;
  public int nHits;
  public int debugLevel;
  private boolean trackDone=false;
  
  private MCParticle mcParticle;
  private boolean mcParticleIsKnown;
  
  private List<TrackState> _trackStates;
  
  // Indexing array of parameters :
  
  static  int PARA_d0 = 0;
  static  int PARA_phi0 = 1;
  static  int PARA_kappa = 2;
  static  int PARA_z0 = 3;
  static  int PARA_lambda = 4;
  
  static int maxEndcapLayer;
  static int maxBarrelLayer;
  static double chi2NdfFitEnd = 5.;
  static double speedOfLight=0.299792458;
  
  static double emCalZ = 183.9*Const.cm; // EM Calorimeter endcap Z position;
  
  static ConditionsListener _conListener = new ConditionsListener() {
    public void conditionsChanged(ConditionsEvent event) {
      ConditionsManager conMan = (event == null) ? ConditionsManager.defaultInstance() : event.getConditionsManager();
      try {
        Detector det = conMan.getCachedConditions(Detector.class,"compact.xml").getCachedData();
        initialize(det);
      } catch (ConditionsSetNotFoundException x) {}
    }
  };
  static {
    ConditionsManager.defaultInstance().addConditionsListener(_conListener);
    _conListener.conditionsChanged(null);
  }
  
  
// -- Constructors and static initialization:  ---------------------------------
  
  /** 
   * Initialization of static members.
   * Runs automatically once {@link org.lcsim.contrib.garfield.util.Const} has been initialized.
   */
  public static void initialize(Detector detector) {
    maxBarrelLayer = Const.det().VXD_BARREL.nLayers() + Const.det().TRACKER_BARREL.nLayers() - 1;
    maxEndcapLayer = Const.det().VXD_BARREL.nLayers() + Const.det().TRACKER_ENDCAP.nLayers();
  }
  
  /**
   * Default constructor.
   */
  GarfieldTrack() {
    hits = new ArrayList();
    hel = new GarfieldHelix();
    trackPara= new double[]{ 0.,0.,0.,0.,0. };
    minRadius = 0.;
    maxRadius = 0.;
    status = "DUMMY";
    lastLayer=-1;
    nLayerMissed=0;
    stub = null;
    ndf = 0;
    chi2 = 0.;
    nHits=0;
    debugLevel = 0;
    mcParticleIsKnown = false;
    _trackStates = new ArrayList<TrackState>();
  }
  
  /**
   * Construct GarfieldTrack from a {@link MipStub}.
   */
  GarfieldTrack(MipStub mStub, int debugL) {
    hits = new ArrayList();
    hel = new GarfieldHelix(mStub.base, mStub.dir, mStub.kappa);
    hel.setDebugLevel(debugL);
    trackPara= new double[]{ 0.,0.,0.,0.,0. };
    minRadius = 0.;
    maxRadius = 0.;
    status = "MIPSTUB_START";
    stub = mStub;
    if (stub.isEndcap()){
      isEndcap = true;
      lastLayer=maxEndcapLayer+1;
    } else{
      isEndcap = false;
      lastLayer=maxBarrelLayer+1;
    }
    nLayerMissed=0;
    ndf = 0;
    chi2 = 0.;
    nHits=0;
    debugLevel = debugL;
    mcParticleIsKnown = false;
    _trackStates = new ArrayList<TrackState>();
  }
  
  /**
   * Construct GarfieldTrack from a standard hep.lcd Track.<br>
   * Since <code>hep.lcd.event.Track</code> is not linked to hits,
   * and lacks other information used by K0S finder, Monte Carlo pointers
   * are used to construct a list of hits, and a fake MipStub created to
   * support fitting. This needs to be done better to fully integrate
   * garfield tracking into the full tracking algorithm - plan to do
   * it later, and hopefully with a better framework (org.lcsim ?).
   * For now, any methods requiring access to individual hits might not
   * work correctly if called for <code>GarfieldTrack</code>
   * object created with this constructor.
   */
  GarfieldTrack(Track standardTrack, EventHeader event, int debugLevel) {
    
    mcParticle = null ;   // for some reason org.lcsim has getMCParticle commented out
    //mcParticle = standardTrack.getMCParticle();
    mcParticleIsKnown = (mcParticle != null);
    
    hits = new ArrayList();
    if (mcParticleIsKnown) {
      //Enumeration vxdHits = event.getVXDHits().getHits();
      //while (vxdHits.hasMoreElements()) {
      //TrackerHit h = (TrackerHit)vxdHits.nextElement();
      //if (h.getMCParticle() == mcParticle) {
      //  hits.add(new GarfieldHit(h, 0, 0, 0));
      //}
      //}
      //Enumeration trackerHits = event.getTrackerHits().getHits();
      //while (trackerHits.hasMoreElements()) {
      //TrackerHit h = (TrackerHit)trackerHits.nextElement();
      //if (h.getMCParticle() == mcParticle) {
      //  hits.add(new GarfieldHit(h, 0, 0, 0));
      //}
      //}
      nHits = hits.size();
      minRadius = 999999.;
      ListIterator it = hits.listIterator();
      while (it.hasNext()) {
        GarfieldHit gh = (GarfieldHit)it.next();
        minRadius = Math.min(gh.getRxy(), minRadius);
      }
    } else {
      nHits = ndf+3; // just a guess ...
      minRadius =  standardTrack.getRadiusOfInnermostHit();
      //Math.sqrt(standardTrack.getOriginX()*standardTrack.getOriginX() +
      //           standardTrack.getOriginY()*standardTrack.getOriginY());
      minRadius = minRadius > 1.2 * Const.cm ? minRadius : 1.2 * Const.cm;  // guessing innermost VXD layer
    }
    
    hel = new GarfieldHelix(standardTrack.getReferencePoint(), standardTrack.getMomentum(),
            standardTrack.getTrackParameter(2));
    hel.setDebugLevel(debugLevel);
    trackPara = new double[5];
    setTrackParameters();
    maxRadius = 125.*Const.cm;
    status = "FROM_STANDARD_TRACK";
    stub = null;
    ndf = (int)standardTrack.getNDF() > 0 ? (int)standardTrack.getNDF() : 1;
    chi2 = standardTrack.getChi2();
    grade = 99999;
    this.debugLevel = debugLevel;
    trackDone = true;
    isEndcap = false; // xxx just a kludge
    //(mcParticleIsKnown) ?
    //Math.abs(mcParticle.getCalorimeterEntryZ()) > emCalZ : false;
    lastLayer = ( isEndcap ? maxEndcapLayer : maxBarrelLayer) + 1;
    nLayerMissed = 0;
    
    stub = new MipStub(standardTrack.getReferencePoint(), standardTrack.getMomentum(),
            standardTrack.getTrackParameter(2), nHits, isEndcap, 0);
    hasZ = hasZMeasurement();
    _trackStates = new ArrayList<TrackState>();
  }
  
  /**
   * Copy constructor.
   */
  GarfieldTrack(GarfieldTrack g) {
    hits = (ArrayList)(g.hits.clone());
    hel = new GarfieldHelix(g.hel.base(),g.hel.dir(),g.hel.kappa());
    hel.setDebugLevel(g.debugLevel);
    trackPara = new double[5];
    System.arraycopy(g.trackPara, 0, trackPara, 0, 5);
    minRadius = g.minRadius;
    maxRadius = g.maxRadius;
    lastLayer= g.lastLayer;
    nLayerMissed= g.nLayerMissed;
    status = g.status;
    grade = g.grade;
    stub = g.stub;
    ndf = g.ndf;
    chi2 = g.chi2;
    nHits=g.nHits;
    isEndcap = g.isEndcap;
    debugLevel = g.debugLevel;
    trackDone = g.trackDone;
    if (debugLevel >=4) System.out.println("GarfieldTrack created copy of track with nHit="+g.hits.size());
    mcParticleIsKnown = false;
    hasZ = g.hasZ;  // fixed Sep 08 2005, evt
    _trackStates = g.getTrackStates();
  }
  
  // simple data retriever
  public int getID(){ return ID;}
  public double getGrade(){ return grade;}
  
  public int getHitID(int i){
    GarfieldHit gh = (GarfieldHit) hits.get(i);
    return gh.getID();
  }
  
  public double[] getHitPoint(int i){
    if (i<0 || i>nHits-1){
      System.out.println("GarfieldTrack getHitPoint problem index="+i);
      return new double[]{999.,999.,999.};
    }
    GarfieldHit gh = (GarfieldHit) hits.get(i);
    return gh.getPoint();
  }
  
  public int getNumberOfStepovers(){return nLayerMissed;}
  
  public int getHitLayer(int i){
    if (i<0 || i>nHits-1){
      System.out.println("GarfieldTrack getHitLayer problem index="+i);
      return -1;
    }
    GarfieldHit gh = (GarfieldHit) hits.get(i);
    return gh.getLayer();
  }
  
  /**
   * gets track parameters
   * parameters refer to point of closest approach (pca) in the xy plane
   * magnetic field points along z direction
   *
   * - getPara("kappa") gives curvature<br>
   * - d0: impact parameter in xy plane<br>
   * - z0: impact parameter in z direction<br>
   * - lambda: tan lambda (dip angle)<br>
   * - phi0: phi direction at point of closest approach<br>
   * <p>
   * how to get pca from track parameter<br>
   * x-coordinate  pca_x = -d0 * sin(phi0)<br>  // see also contrib/timb/mc/fast/tracking/DocaTrackParameters.java
   * x-coordinate  pca_y = d0 * cos(phi0)<br>
   * z-coordinate  pca_z = z0<br>
   * how to get helix direction at pca<br>
   * dir-x =cos(phi0)<br>
   * dir-y =sin(phi0)<br>
   * dir-z =lambda<br>
   */
  public double getPara(String name) {
    if (name == "kappa") return trackPara[PARA_kappa];
    if (name == "d0") return trackPara[PARA_d0];
    if (name == "z0") return trackPara[PARA_z0];
    if (name == "lambda") return trackPara[PARA_lambda]; // this is really tan lambda
    if (name == "phi0") return trackPara[PARA_phi0];
    System.out.println("GarfieldTrack getPara, severe error, unknown name"+name);
    return -999.;
  }
  
  public String getStatus(){return status;}
  public int getnHits(){return nHits;}
  public int getStubMinLayer(){return stub.getMinLayer();}
  public int getEquivnHits(){
    if (isEndcap()) return (int) nHits/2;
    else return nHits;
  }
  public double getMinR(){ return minRadius;}
  public double getMaxR(){ return maxRadius;}
  public double getChi2(){ return chi2;}
  public int getNdf(){ return ndf;}
  
  /**
   * transverse momentum Pt in GeV = 0.299.. * B (in Tesla) * R (in Meter)
   * @param bField B field in Tesla
   */
  public double getPt(double bField){
    return speedOfLight*bField/Math.abs(getPara("kappa") * Const.m)/Const.GeV; // this division makes the return value independent of the units for momentum
  }
  
  public double getP(double bField){
    double pt = getPt(bField);
    double pz = pt*getPara("lambda");
    return Math.sqrt(pt*pt+pz*pz);
  }
  
  public boolean hasZMeasurement(){
    if (status.equals("FROM_STANDARD_TRACK")) {
      return true;
    } else {
      for (int i=0;i<nHits;i++){
        if ( ((GarfieldHit) hits.get(i)).hasZ()) return true;
      }
    }
    return false;
  }
  
  public boolean isPurged(){return (status == "PURGED");}
  public boolean isRejected(){return (status=="REJECTED");}
  public boolean isDone(){return trackDone;}
  public boolean isEndcap(){ return isEndcap;}
  public int q() {
    if (trackPara[PARA_kappa] == 0.) return hel.q();
    else if (trackPara[PARA_kappa]<0.) return -1;
    else return 1;
  }
  public double getMipStubBase(int i){ return stub.base[i];}
  public double[] getMipStubBase(){ return stub.base;}
  public double getMipStubKappa(){ return stub.kappa();}
  public int getStubNHits(){return stub.getnHits();}
  
  // simple value setter
  public void setID(int id){ ID=id;}
  public void setHasZ(boolean val){ hasZ = val;}
  /** controls amount of debug text output and test histograms, =0 not output, >0 debug output  */
  public void setDebugLevel(int i){ debugLevel = i;}
  public void setLastLayer(int i){lastLayer=i;}
  public void setDone(){trackDone=true;}
  public void setStatus(String stat){status=stat;}
  public void setHelix(double[] pbase, double[] p2){
    hel.setBase(pbase[0],pbase[1],pbase[2]);
    hel.setDir(p2[0]-pbase[0],p2[1]-pbase[1],p2[2]-pbase[2]);
  }
  
  // -- Simple helper functions :  ---------------------------------------------
  
  public void rejectTrack(){
    status = "REJECTED";
    if (debugLevel>=3) debug();
  }
  
  public void purgeTrack(){
    status = "PURGED";
    if (debugLevel>=3) debug();
  }
  
  public void setRadii() {
    // xxx how to define minRadius with 2D hits i nthe endcap???
    double R;
    minRadius = 10000.;
    maxRadius= -10000.;
    for (int i= 0; i<nHits; i++){
      R=((GarfieldHit)hits.get(i)).getRxy();
      if (R<minRadius) minRadius = R;
      if (R>maxRadius) maxRadius = R;
    }
  }
  
  /** calculate grade of track */
  public double setGrade(double chi2Weight) {
    double val = (double) nHits;
    for (int i=0;i<nHits;i++){
      if (chi2Contrib(i)<25.) val +=1.; // less than 3 sigma
    }
    val -= chi2 / chi2Weight;
    val -= Math.abs(getPara("d0"))/Const.m;
    val -= Math.abs(getPara("kappa"))*Const.cm;
    val -= (double) getNumberOfStepovers()/2.;
    grade=val;
    return val;
  }
  /**
   * finds point of closest approach in x,y plane with respect to 0,0,0<br>
   * see also getPara()
   */
  public void setTrackParameters() {
    trackPara[PARA_kappa] = hel.kappa();
    trackPara[PARA_lambda] = hel.dir(2)/Math.sqrt(hel.dir(0)*hel.dir(0)+hel.dir(1)*hel.dir(1)); // tan Lambda = cot theta
    hel.setPointOnHelixWithXY(0.,0.);
    trackPara[PARA_d0] = Math.sqrt(hel.getPointOnHelix(0)*hel.getPointOnHelix(0)+
            hel.getPointOnHelix(1)*hel.getPointOnHelix(1));
    trackPara[PARA_phi0] = Math.atan2(hel.dirAtPoint(1),hel.dirAtPoint(0));
    if (hel.getPointOnHelix(0)*Math.sin(trackPara[PARA_phi0])<0.) trackPara[PARA_d0] = -trackPara[PARA_d0];
    if (debugLevel>=3) System.out.println("setTrackParameters "+trackPara[PARA_phi0]+" "+hel.dirAtPoint(0)+" "+hel.dirAtPoint(1));
    //
    trackPara[PARA_z0] = hel.getPointOnHelix(2);
  }
  
  /**
   * Throw avay hits that have worst chi2 until there are no hits left with contribution
   * to Chi2 above <code>cutValue</code>, or until the number of hits on the track
   * falls below <code>nHitMin</code>. Track is re-fit after removing each hit.
   */
  public void purgeHits(double cutValue, int nHitMin, int nIter){
    double c;
    double maxContrib;
    int maxI=-1;
    boolean done=false;
    fastChi2Fit(nIter+1);
    while (!done){
      maxContrib = -10.;
      maxI=-1;
      for (int i=0;i<nHits;i++){
        c=chi2Contrib(i);
        if (c>cutValue && c>maxContrib) {
          maxI=i;
          maxContrib = c;
        }
      }
      if (maxContrib>cutValue){
        dropHit(maxI);
        if (nHits >= nHitMin) fastChi2Fit(nIter);
      } else {done=true;}
      if (nHits < nHitMin) done=true;
    }
    fastChi2Fit(nIter+1);
    if (debugLevel>=2) System.out.println("GarfieldTrack purgeHits track="+getID()+" nHits="+nHits);
  }
  
  /**
   * Add a new hit to this track.<br>
   * Helix parameters are not changed, Chi2 is recalculated.
   */
  public void addHit(GarfieldHit h){
    boolean endcapBarrelTransition = (isEndcap() && !h.isEndcap() || (!isEndcap() && h.isEndcap()));
    boolean trackerVTXTransition = (hasZ && !h.is3D()) || (!hasZ && h.is3D()); // FixMe, this is very crude
    hasZ = hasZ || h.hasZ();
    int hlayer = h.getLayer();
    // calculate if we missed a layer
    // update nLayersmissed
    if (lastLayer - hlayer>1 &&
            !endcapBarrelTransition &&
            !trackerVTXTransition) nLayerMissed+=(lastLayer - hlayer)-1;
    
    lastLayer = hlayer;
    hits.add(h);
    nHits++;
    if (h.is3D()) {
      ndf+=2; // see also dropHit() if this line changes
    } else {
      ndf++; // see also dropHit() if this line changes
    }
    calculateChi2();
  }
  
  /**
   * Drop a hit from this track.<br>
   * Helix parameters are not changed, Chi2 is recalculated.
   */
  public void dropHit(int i){
    if (((GarfieldHit) hits.get(i)).is3D()) ndf-=2;// see also addHit() if this line changes
    else ndf--;// see also addHit() if this line changes
    if (i!=nHits-1) nLayerMissed++;
    hits.remove(i);
    nHits=hits.size();
    hasZ = hasZMeasurement();
    setRadii();
    calculateChi2();
  }
  
  public void setHelixBaseToPCA() {
    hel.setPointOnHelixWithXY(0.,0.);
    double bx=hel.getPointOnHelix(0);
    double by=hel.getPointOnHelix(1);
    double bz=hel.getPointOnHelix(2);
    double dx=hel.dirAtPoint(0);
    double dy=hel.dirAtPoint(1);
    double dz=hel.dirAtPoint(2);
    double d0=Math.sqrt(dx*dx+dy*dy);
    double signkappa=1.;
    if (hel.kappa()<0.) signkappa=-1.;
    hel.setBase(-d0*signkappa*dy,d0*signkappa*dx,bz);
    hel.setDir(dx,dy,dz);
  }
  
  public double distanceCutValue() {
    // xxx this function depends on hardcoded length constants
    // needs to be changed when we switch to millimeter
    if (nHits==0) return 3.0*stub.getBaseError(); // 3 sigma around base point
    else if (getEquivnHits()==1) {
      return Const.cm*Math.sqrt(400.+(stub.getDirXYError()*30.)*(stub.getDirXYError()*30.)+stub.kappa()*stub.kappa()*1.E6);
    } else if (getEquivnHits()==2) {
      return Const.cm*Math.sqrt(100.+400.*stub.getDirXYError()*stub.getDirXYError()+stub.kappa()*stub.kappa()*5.E6);
    } else if (getEquivnHits()==3) {
      return Const.cm*10.;
    } else {
      return 10.*Const.cm;
    }
  }
  
  /**
   * updated 03/23/05 to remove vcreation of new double,
   * and call to CircleFromPoints to calculate helix parameters for barrel tracks
   **/
  public void calculateHelixFromHits(){
    int nh = hits.size();
    if (nh==0) return;
    GarfieldHit lastHit = (GarfieldHit) hits.get(nh-1);
    if (lastHit.isEndcap() && Math.abs(hel.dir(2))>1.E-2) {
      hel.setPointOnHelixWithZ(lastHit.getPoint(2));
    } else {
      hel.setPointOnHelixWithXY(lastHit.getPoint(0),lastHit.getPoint(1));
    }
    hel.setDir(hel.dirAtPoint(0),hel.dirAtPoint(1),hel.dirAtPoint(2));
    hel.setBase(hel.getPointOnHelix(0),hel.getPointOnHelix(1),hel.getPointOnHelix(2));
    //if (debugLevel>=2) System.out.println("calculateHelixFromhits base="+hel.base(0)+" "+hel.base(1)+" "+hel.base(2));
    //if (debugLevel>=2) System.out.println("calculateHelixFromhits  dir="+hel.dir(0)+" "+hel.dir(1)+" "+hel.dir(2)+" kappa="+hel.kappa());
  }
  
  public void newCalculateHelixFromHits(){
    int nh = hits.size();
    if (nh==0) return;
    GarfieldHit lastHit = (GarfieldHit) hits.get(nh-1);
    if (lastHit.isEndcap() && Math.abs(hel.dir(2))>1.E-2) {
      hel.setPointOnHelixWithZ(lastHit.getPoint(2));
    } else {
      hel.setPointOnHelixWithXY(lastHit.getPoint(0),lastHit.getPoint(1));
    }
    hel.setDir(hel.dirAtPoint(0),hel.dirAtPoint(1),hel.dirAtPoint(2));
    hel.setBase(hel.getPointOnHelix(0),hel.getPointOnHelix(1),hel.getPointOnHelix(2));
    
    if (nh>=2 && !lastHit.isEndcap() && !((GarfieldHit) hits.get(0)).isEndcap()){
      CircleFromPoints cir =new CircleFromPoints();
      if (nh>2){
        cir.calculate(
                getPoint(nh-1,0),getPoint(nh-1, 1),
                getPoint(nh-2,0),getPoint(nh-2, 1),
                getPoint(nh-3,0),getPoint(nh-3, 1)
                );
        hel.setDir(cir.getDirAtC(0),cir.getDirAtC(1),hel.dirAtPoint(2));
        hel.setBase(getPoint(nh-1,0),getPoint(nh-1, 1),hel.getPointOnHelix(2));
      } else if (nh==2 && stub != null){
        cir.calculate(
                stub.base[0],stub.base[1],
                getPoint(nh-1,0),getPoint(nh-1, 1),
                getPoint(nh-2,0),getPoint(nh-2, 1)
                );
        hel.setDir(cir.getDirAtC(0),cir.getDirAtC(1),hel.dirAtPoint(2));
        hel.setBase(getPoint(nh-1,0),getPoint(nh-1, 1),hel.getPointOnHelix(2));
      }
    }
    //if (debugLevel>=2) System.out.println("calculateHelixFromhits base="+hel.base(0)+" "+hel.base(1)+" "+hel.base(2));
    //if (debugLevel>=2) System.out.println("calculateHelixFromhits  dir="+hel.dir(0)+" "+hel.dir(1)+" "+hel.dir(2)+" kappa="+hel.kappa());
  }
  public double getPoint(int i, int j) {
    return ((GarfieldHit) hits.get(i)).getPoint(j);
  }
  
  //
  // chi-square and fitting
  //
  
  //  public void calculateNdf(){ ndf=hits.size();}
  
  /**
   * Chi-square from hits and - if MipStub exists - from mipstub to track matching.
   */
  public void calculateChi2(){
    if (status == "FROM_STANDARD_TRACK") return;
    double saveErr=1.E-3; // 1 micrometer expressed in mm.
    double c;
    chi2=0.;
    // contribution from hits
    for (int i=0;i<nHits;i++){
      GarfieldHit h = (GarfieldHit) hits.get(i);
      c=h.distanceToHit(hel,hasZ)/(h.getError()+saveErr);
      //c=hel.distanceToHit(h)/(h.getError()+saveErr);
      chi2=chi2+c*c;
    }
    // contribution from mipStub
    if (stub != null){
      c=hel.distanceToHit3D(stub.base)/stub.getBaseError();
      chi2=chi2+c*c;
      if (!hasZ){
        hel.setPointOnHelixWithXY(stub.base[0],stub.base[1]);
        c=Math.abs(stub.dir[2]-hel.dirAtPoint(2))/stub.getDirZError();
        chi2=chi2+c*c;
        if (getEquivnHits()<3){
          double cosPhi=stub.dir[0]*hel.dirAtPoint(0)+stub.dir[1]*hel.dirAtPoint(1);
          if (Math.abs(cosPhi)>1.000001) System.out.println("Error !!! GarfieldTrack cosPhi="+cosPhi+" "+stub.dir[0]+" "+stub.dir[1]+" "+stub.dir[2]);
          if (cosPhi>1.) cosPhi=1.;
          if (cosPhi<-1.) cosPhi=-1.;
          c=Math.acos(cosPhi)/stub.getDirXYError();
          chi2=chi2+c*c;
          c = (hel.kappa()-stub.kappa)/stub.getKappaError();
          chi2=chi2+c*c;
        }
      }
    }
  }
  
  /**
   * hit chiSquare contribution
   * @param i hit number from 0 to nHits-1
   */
  public double chi2Contrib(int i) {
    double saveErr=1.E-3;
    GarfieldHit h = (GarfieldHit) hits.get(i);
    double c=h.distanceToHit(hel,hasZ)/(h.getError()+saveErr);
    //double c=hel.distanceToHit(h)/(h.getError()+saveErr);
    return c*c;
  }
  
  public boolean fullChi2Fit(double rangeMax, int nIter){
    return fastChi2Fit(nIter*4);
  }
  public boolean oldFullChi2Fit(double rangeMax, int nIter){
    calculateChi2();
    //calculateNdf();
    if (debugLevel>=2) System.out.println("++FULLChi2Fit before"+chi2);
    boolean done =false;
    double range = rangeMax;
    if (chi2/ndf<10. && rangeMax>0.5) range =  rangeMax/8.;
    double oldChi2=chi2;
    int oldLevel =debugLevel;
    this.debugLevel=0;
    int n=0;
    int nRounds = 0;
    while (nRounds < nIter){
      done=false;
      while (!done){
        chi2FitIteration(range,0);
        n++;
        if (oldChi2<=chi2) done =true;
        oldChi2=chi2;
        if (n>4000){
          this.debugLevel=oldLevel;
          if (debugLevel>=2) System.out.println("GarfieldTrack FullChi2Fit runaway chi2="+chi2);
          return false;
        }
      }
      range = range/4.;
      nRounds++;
    }
    this.debugLevel=oldLevel;
    if (debugLevel>=2) System.out.println("++FULLChi2Fit after"+chi2+" n="+n);
    return true;
  }
  
  public boolean fastChi2Fit(int scale){
    calculateChi2();
    double originalChi=chi2;
    boolean done =false;
    double range;
    double chi2ndf = chi2/ndf;
    int nStepMax;
    if (chi2ndf<2.) {nStepMax=3; range = 0.02;} 
    else if (chi2ndf<10.) {nStepMax=5; range = 0.2;} 
    else if (chi2ndf<100.) {nStepMax=9; range = 1.0;} 
    else if (chi2ndf<1000.) {nStepMax=12; range = 2.0;} 
    else if (chi2ndf<10000.) {nStepMax=20; range = 4.0;} 
    else if (chi2ndf<100000.) {nStepMax=25; range = 5.0;} 
    else if (chi2ndf<1000000.) {nStepMax=30; range = 6.0;} 
    else {nStepMax=40; range = 8.0;}
    nStepMax = nStepMax * scale;
    //System.out.println("fC Tr="+ID+" Chi2="+chi2+ "NstepMax="+nStepMax);
    double oldChi2=chi2;
    int oldLevel =debugLevel;
    //System.out.println("**FastChi2Fit before"+chi2+" "+chi2ndf+" scale"+scale);
    this.debugLevel=0;
    int n=0;
    int nRounds = 0;
    while (range > 0.02/scale ){
      done=false;
      while (!done){
        chi2FitIteration(range,1);
        n++;
        if (oldChi2<=chi2*1.05) done =true;
        oldChi2=chi2;
        if (n>nStepMax){
          //System.out.println("**FastChi2Fit runaway nHits="+getnHits()+" "+originalChi+" n="+n+" chi2="+chi2+" nstepmax "+nStepMax);
          this.debugLevel=oldLevel;
          return false;
        }
      }
      done=false;
      while (!done){
        chi2FitIteration(range,0);
        n++;
        if (oldChi2<=chi2*1.05) done =true;
        oldChi2=chi2;
        if (n>nStepMax){
          //System.out.println("**FastChi2Fit runaway nHits="+getnHits()+" "+originalChi+" n="+n+" chi2="+chi2+" nstepmax "+nStepMax);
          this.debugLevel=oldLevel;
          return false;
        }
      }
      //System.out.println("**FC n="+n+" range="+range);
      //debug();
      range = range/2.;
      nRounds++;
    }
    //System.out.println("**FastChi2Fit nHits="+getnHits()+" "+originalChi+" n="+n+" chi2="+chi2+" nstepmax "+nStepMax);
    this.debugLevel=oldLevel;
    return true;
  }
  
  public void chi2FitIteration(double range, int mode){
    // fast variation of dir and kappa
    // mode = 0 fit rphi and z
    // mode = 1 fit rphi only
    // mode = 2 fit z only
    calculateChi2();
    int nIter = 1;
    double oldChi2=chi2;
    double oldDir0=hel.dir(0);
    double oldDir1=hel.dir(1);
    double oldDir2=hel.dir(2);
    double bestDir0=hel.dir(0);
    double bestDir1=hel.dir(1);
    double bestDir2=hel.dir(2);
    double bestBas0=hel.base(0);
    double bestBas1=hel.base(1);
    double bestBas2=hel.base(2);
    double oldBas0=hel.base(0);
    double oldBas1=hel.base(1);
    double oldBas2=hel.base(2);
    double oldKappa=hel.kappa();
    double bestKappa=oldKappa;
    double minChi2=chi2;
    double cosphi;
    double sinphi;
    double phi;
    
    if (mode == 0 || mode ==2){
      for (int i=-nIter; i<=nIter;i++){
        double delta = 0.05*range*i;
        hel.setDir(oldDir0,oldDir1,oldDir2+delta);
        for (int j=-nIter; j<=nIter;j++){
          hel.setBase(oldBas0,oldBas1,oldBas2+5.0*range*j);
          calculateChi2();
          if (chi2<minChi2){
            bestDir0=hel.dir(0);
            bestDir1=hel.dir(1);
            bestDir2=hel.dir(2);
            bestBas2=hel.base(2);
            //if (debugLevel>=4) System.out.println(" Fit found new minimum Z-direction variation old="+minChi2+" new="+chi2+" dir0="+bestDir0+" dir1="+bestDir1+" dir2="+bestDir2);
            minChi2=chi2;
          }
        }
      }
      hel.setDir(bestDir0,bestDir1,bestDir2);
      hel.setBase(bestBas0,bestBas1,bestBas2);
      oldDir0=hel.dir(0);
      oldDir1=hel.dir(1);
      oldDir2=hel.dir(2);
      oldBas0=hel.base(0);
      oldBas1=hel.base(1);
      oldBas2=hel.base(2);
      calculateChi2();
      oldChi2=chi2;
      minChi2=chi2;
    }
    
    if (mode <=1){
      for (int i=-nIter; i<=nIter;i++){
        hel.setKappa(oldKappa+0.0001*i*range);
        for (int j=-nIter; j<=nIter;j++){
          phi = 0.05*range*j;
          cosphi=Math.cos(phi);
          sinphi=Math.sin(phi);
          hel.setDir(cosphi*oldDir0-sinphi*oldDir1,sinphi*oldDir0+cosphi*oldDir1,oldDir2);
          for (int k=-nIter; k<=nIter;k++){
            double eps=2.0*range*k/nIter;
            hel.setBase(oldBas0-oldDir1*eps,oldBas1+oldDir0*eps,oldBas2+oldDir2*eps);
            calculateChi2();
            if (chi2<minChi2){
              bestDir0=hel.dir(0);
              bestDir1=hel.dir(1);
              bestDir2=hel.dir(2);
              bestBas0=hel.base(0);
              bestBas1=hel.base(1);
              bestBas2=hel.base(2);
              bestKappa=hel.kappa();
              //if (debugLevel>=4) System.out.println(" Fit found new minimum old="+minChi2+" new="+chi2+" kappa="+bestKappa+" phi="+phi+" dir0="+bestDir0+" dir1="+bestDir1+" i,j,k="+i+" "+j+" "+k);
              minChi2=chi2;
            }
          }
        }
      }
    } // end of mode check
    
    hel.setKappa(bestKappa);
    hel.setDir(bestDir0,bestDir1,bestDir2);
    hel.setBase(bestBas0,bestBas1,bestBas2);
    calculateChi2();
    //if (debugLevel>=2) System.out.println("GarfieldTrack chi2FitIteration range="+range+" new chi2="+chi2+" old chi2="+oldChi2+" dir0="+hel.dir(1));
  }
  
  private double kappaError(){
    return 0.0002*Math.exp(-0.2*nHits);
  }
  //
  // testing debugging and plotting
  //
  public void debug() {
    if (debugLevel<1) return;
    System.out.println(" ");
    String hasZString=" ";
    if (hasZMeasurement()) hasZString="has Z";
    String endc="";
    if (isEndcap()) endc=" endcap";
    
    System.out.println("GarfieldTrack "+getID()+" REPORT nh="+hits.size()+" Status="+status+" Chi2="+chi2+" ndf="+ndf+" kappa="+hel.kappa());
    System.out.println("       "+hasZString+", "+endc+" Nstepover="+getNumberOfStepovers()+" Grade="+grade);
    if (stub != null){
      System.out.println("REPORT MipStub base"+stub.base[0]+" "+stub.base[1]+" "+stub.base[2]+" d="+hel.distanceToHit3D(stub.base)+" dErr="+stub.getBaseError());
      System.out.println("REPORT MipStub dir"+stub.dir[0]+" "+stub.dir[1]+" "+stub.dir[2]+" kappa="+stub.kappa+" kaErr="+stub.getKappaError());
      System.out.println("REPORT MipStub angle="+stub.angleBaseDir+" nH="+stub.getnHits());
    }
    System.out.println("REPORT HELIX base"+hel.base(0)+" "+hel.base(1)+" "+hel.base(2));
    System.out.println("REPORT HELIX dir"+hel.dir(0)+" "+hel.dir(1)+" "+hel.dir(2)+" kappa="+hel.kappa());
    
    for (int k=0; k<hits.size(); k++){
      double[] ph = getHitPoint(k);
      String endCapString = " barrel";
      if (((GarfieldHit) hits.get(k)).isEndcap()) endCapString = " EndCap";
      String string3d = " ";
      if (((GarfieldHit) hits.get(k)).is3D()) string3d=" 3D";
      String stringHasZ = " ";
      if (((GarfieldHit) hits.get(k)).hasZ()) stringHasZ=" hasZ";
      System.out.println("  hit ID="+getHitID(k)+" layer="+getHitLayer(k)+" "+ph[0]+" "+ph[1]+" "+ph[2]+" Chi2C="+chi2Contrib(k)+" d="+((GarfieldHit) hits.get(k)).distanceToHit(hel,hasZ)+endCapString+string3d+stringHasZ);
    }
    
    System.out.println("REPORT  kappa,phi,lam,d0,z0 = "+this.trackPara[PARA_kappa]+" "+
            this.trackPara[PARA_phi0]+" "+this.trackPara[PARA_lambda]+" "+this.trackPara[PARA_d0]+" "+this.trackPara[PARA_z0]);
    System.out.println("REPORT minR="+minRadius+" maxR="+maxRadius+ " q="+q());
    
  }
  
  /**
   * Returns MCParticle that produced tracker hits that are associated with
   * this track. Returns null if more than one MCParticle was involved.
   */
  public MCParticle getMCParticle() {
    if (!mcParticleIsKnown) findMCParticle();
    return mcParticle;
  }
  
  private void findMCParticle() {
    mcParticle = null;
    /* // xxx kludge
    ListIterator iter = hits.listIterator();
    while (iter.hasNext()) {
      MCParticle particle = (MCParticle)((GarfieldHit)iter.next()).getMCParticle();
      if (mcParticle == null) {
        mcParticle = particle;
      } else if (mcParticle != particle) {
        mcParticle = null;
        break;
      }
    }
    mcParticleIsKnown = true;
     */
  }
  
  // -- Implementing org.lcsim.event.Track :  ----------------------------------
  // -- (dummy implementations for some methods - this should be fixed later) --
  
  public double[] getTrackParameters() {return trackPara;} // FIXME : need conversion once Tony sorts
  // out conventions and WIRED swimming is fixed
  public double getTrackParameter(int param) {   // FIXME : same here
    if (param == PARA_kappa) {
      //return trackPara[param]*10.;
      return - trackPara[param];
    } else if (param == PARA_d0) {
      //return - trackPara[param] / 10.;
      return - trackPara[param];
    } else if (param == PARA_z0) {
      //return trackPara[param] / 10.;
      return trackPara[param];
    } else if (param == PARA_lambda) {
      return trackPara[param];
    } else {
      return trackPara[param];
    }
  }
  
  public double getdEdxError() {return 0.;} // FixMe
  
  public double getdEdx() {return 0.;} // FixMe
  
  public int getType() {return 0;} // FixMe
  
  public List<Track> getTracks() {return new ArrayList<Track>(1);}
  
  public List<TrackerHit> getTrackerHits() {return (List<TrackerHit>)hits;}
  
  public int[] getSubdetectorHitNumbers() {return new int[] {0};} //FixMe
  
  public boolean fitSuccess() {return true;}  // FixMe
  
  public int getCharge() {return q();}
  
  public SymmetricMatrix getErrorMatrix() {return new SymmetricMatrix(5);} // FixMe
  
  public double[] getMomentum() {return new double[] {0.,0., 0.};} //FixMe
  
  public int getNDF() {return ndf;}
  
  public double getPX() {return 0.;} //FixMe
  
  public double getPY() {return 0.;} //FixMe
  
  public double getPZ() {return 0.;} //FixMe
  
  public double getRadiusOfInnermostHit() {return minRadius;}
  
  public boolean isReferencePointPCA() {return false;}
  
  public double[] getReferencePoint() {
    setHelixBaseToPCA();
    return hel.base();
  }
  
  public double getReferencePointX() {
    setHelixBaseToPCA();
    return hel.base(0);
  }
  
  public double getReferencePointY() {
    setHelixBaseToPCA();
    return hel.base(1);
  }
  
  public double getReferencePointZ() {
    setHelixBaseToPCA();
    return hel.base(2);
  }
  
  public List<TrackState> getTrackStates()
  {
      return _trackStates;
  }
}





