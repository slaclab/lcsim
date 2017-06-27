package org.lcsim.recon.cat;

import java.util.*;
import org.lcsim.recon.cat.util.Const;
import org.lcsim.event.*;
import org.lcsim.util.Driver;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.util.aida.AIDA;

/**
 * Calorimeter-assisted tracking algorithm for the SiD.
 * This processor finds track with outside-to-inside pattern recognition.
 * Starting from Ecal clusters and their pointing direction, hits are
 * associated to a track.
 * <p>
 * See ExampleDriver.java on how to use it<br>
 * <p>
 * This org.lcsim version is based on a previous hep.lcd version.
 * known dependencies on org.lcsim: <br>
 * a few parameters are tuned specifically to SiD in org.lcsim, lines with
 * such dependencies are labeled with "FixMe".
 * <p>
 *
 * @see MipStub
 * @see GarfieldTrack
 * @see GarfieldHit
 *
 * @author  E. von Toerne
 * @author  D. Onoprienko
 * @version $Id: GarfieldTrackFinder.java,v 1.2 2007/12/08 02:39:11 jeremy Exp $
 */
final public class GarfieldTrackFinder extends Driver{
  
  // -- Named constants :  -----------------------------------------------------
  
  static final int APPEND = 0;
  static final int OVERWRITE = 1;
  static final int NONE = 2;
  
  static final int YES = 1;
  static final int NO = 0;
  
  // -- Private Data :  --------------------------------------------------------
  
  private List<GarfieldHit> listOfHits; // contains list of GarfieldHits of the event
  private int listOfHitsLowestLayer, listOfHitsHighestLayer;
  private List<GarfieldHit> selectedHits; //contains selected hits for attachment to track
  
  private GarfieldHitConverter hitConverter;
  
  // -- Cut values and other finder parameters :  ------------------------------
  
  static double k_cm = 10.;
  private int garfieldnTRKHitMin = 3; // minimum number of hits on a track
  private boolean refitDuringPatternReco = true;
  private double garfieldChi2ContribCut = 50.; // max. Chi^2 contrib. of a hit
  private double garfieldChi2Cut = 100.; // max. Chi^2/Hit value
  private double garfieldChi2Cut3Ndf=50.;
  private double garfieldKappaCut3Ndf=1./(50. * k_cm);
  private double scaleSearchRegion = 3.; // scale search region by this factor if first attempt fails
  private double maxDistanceFirstHitToBase = 40.*k_cm;
  private int nNewHitsMax=5; // maximum number of new hits
  private int nFitIterations=5; // precision of Chi2-fit
  
  // Finder modes. See setMode() for details.
  
  private int modeModifyTracklist = APPEND;
  private boolean modeUseVXD = true;
  private boolean modeBestTrackOnly = true;
  private boolean modeFromOutsideIn = true;
  private boolean modeUseAllMipStubs = true;
  private int pickEventNo=-1;
  private int pickMipStubNo=-1;
  private double pickMipStubAngle=-100000.;
  
  /**
   * Level of verbosity: 0 no output, 1 some, 3 lot, 5 lots and lots
   */
  private int debugLevel = 0;
  
  
  // -- Some variables to store intermediate results :  ------------------------
  
  private int issuedTrackIDValue; // last issued trackID
  private double minDistanceInHits;
  private GarfieldHit bestHit;
  private int bestTrackID;
  private double bestGrade;
  static int maxTrkNmbr=10000;
  static double bfield = 5.;
  //private ReconstructedTrack[] defTrks = new ReconstructedTrack[maxTrkNmbr];
  
  // -- Constructors :  --------------------------------------------------------
  
  /**
   * Construct <code>GarfieldTrackFinder</code> with default parameters
   * and no debugging output requested.
   */
  public GarfieldTrackFinder() {this(0);}
  
  /**
   * Construct <code>GarfieldTrackFinder</code> with default parameters.
   *
   * @param debugLevel   The higher the <code>debugLeve</code>, the more
   *                     debugging output and test histograms are produced.
   */
  public GarfieldTrackFinder(int debugLevel) {
    selectedHits = new ArrayList<GarfieldHit>();
    this.debugLevel = debugLevel;
    minDistanceInHits=0.;
    issuedTrackIDValue=0;
  }
  
  // -- Setters :  -------------------------------------------------------------
  
  /**
   * Set desired amount of debugging output and test histograms,
   * =0 no output, > 0 debug output
   */
  public void setDebugLevel(int i){ debugLevel = i;}
  
  /**
   * Set track finder's mode of operation.<p>
   *
   * @param  category  Mode category (what to set)
   * @param      mode  Requested mode
   *
   * List of categories and correspondin mode choices
   * (default mode is listed first) :
   * <P>
   * "Modify Event TrackList"
   * <UL>
   * <LI>APPEND :
   *      Append found tracks to the event's tracklist.
   * <LI>OVERWRITE :
   *      Replace the event's tracklist with the list of found tracks.
   * <LI>NONE :
   *      Do not modify event's tracklist.
   * <P>
   * "Use VXD Hits"
   * <UL>
   * <LI>YES :
   *      Extrapolate Garfield Tracks into VXD if qualifying hits are found there.
   * <LI>NO :
   *      Do not use VXD hits.
   * <P>
   * "Best Track Only"
   * <UL>
   * <LI>YES :
   *      Accept only one track per MipStub.
   * <LI>NO :
   *      Accept all tracks that pass the cuts.
   * <P>
   * "From Outside In"
   * <UL>
   * <LI>YES :
   *      Add hits to the track starting from outer layers and moving in.
   * <LI>NO :
   *      Add hits to the track starting with inner layers and moving out.
   * </UL>
   */
  public void setMode(String category, int mode) {
    
    if (category.equalsIgnoreCase("Modify Event TrackList")) {
      switch (mode) {
        case APPEND :
          modeModifyTracklist = APPEND; break;
        case OVERWRITE :
          modeModifyTracklist = OVERWRITE; break;
        case NONE :
          modeModifyTracklist = NONE; break;
        default :
          throw new IllegalArgumentException("Garfield track finder : Unknown mode");
      }
      
    } else if (category.equalsIgnoreCase("Use VXD Hits")) {
      modeUseVXD = (mode == YES);
      
    } else if (category.equalsIgnoreCase("Best Track Only")) {
      modeBestTrackOnly = (mode == YES);
      
    } else if (category.equalsIgnoreCase("From Outside In")) {
      modeFromOutsideIn = (mode == YES);
      
    } else if (category.equalsIgnoreCase("Refit During PatternReco")) {
      refitDuringPatternReco = (mode == YES);
      
    } else if (category.equalsIgnoreCase("Use All MipStubs")) {
      modeUseAllMipStubs = (mode == YES);
      
    } else {
      throw new IllegalArgumentException("Garfield track finder : Unknown mode category");
    }
  }
  
  
  public void setNNewHitsMax(int i){ nNewHitsMax= i;};
  public void setNTrkHitMin(int i){ garfieldnTRKHitMin = i;};
  public void setNFitIterations(int i){ nFitIterations=i;}
  public void setChi2ContribCut(double d){  garfieldChi2ContribCut = d;}
  public void setChi2Cut(double d){  garfieldChi2Cut = d;}
  public void setScaleSearchRegion(double d){ scaleSearchRegion=d;}
  /** allows processing of a single event. */
  public void setPickEventNo(int i){ pickEventNo=i;}
  /** allows processing of a single MipStub. */
  public void setPickMipStubNo(int i){ pickMipStubNo=i;}
  /** allows processing of a phi-region. */
  public void setPickMipStubAngle(double a){ pickMipStubAngle=a;}
  
  
  // -- Implementing AbstractProcessor :  --------------------------------------
  
  /**
   * Process event (called by the framework).
   **/
  public void process(EventHeader event) {
    
    // Array to hold found tracks :
    
    issuedTrackIDValue=0;
    ArrayList<GarfieldTrack> listGarfieldTracks = new ArrayList<GarfieldTrack>();
    
    // Debugging: process only specific event if requested
    
    int eventNumber = event.getEventNumber();
    if (debugLevel>=1) System.out.println("GarfieldTrackFinder ev="+eventNumber);
    if (pickEventNo>=0 && pickEventNo!=eventNumber) {
      event.put("GarfieldTracks", listGarfieldTracks);
      return;
    }
    
    // Prepare tracker hit and MipStub lists
    
    List<MipStub> listMipStub;
    if (modeUseAllMipStubs){
      listMipStub = event.get(MipStub.class,"GarfieldMipStubs");
    } else {
      listMipStub = event.get(MipStub.class,"UnassociatedGarfieldMipStubs");
    }
    
    listOfHits = event.get(GarfieldHit.class, "GarfieldHits");
    if (!listOfHits.isEmpty()) {
      listOfHitsLowestLayer = listOfHits.get(listOfHits.size()-1).getLayer();
      listOfHitsHighestLayer = listOfHits.get(0).getLayer();
      if (debugLevel>=2){
        System.out.println("GarfieldTrackFinder hit lowest/highest layer " 
                + listOfHitsLowestLayer + " " + listOfHitsHighestLayer);
        System.out.println("GarfieldTrackFinder barrelLayerVXD "+Const.det().VXD_BARREL.nLayers() + 
                           " tracker="+ Const.det().TRACKER_BARREL.nLayers() + 
                           " tracker endcap="+Const.det().TRACKER_ENDCAP.nLayers());
        
        for (GarfieldHit hh : listOfHits) { hh.debug();}
        
      }
      
    }
    
    // Loop over MIP stubs
    
    int iMipStub = 0;
    
    for (MipStub mStub : listMipStub) {
      
      iMipStub++;
      bestGrade = -1.E12;
      bestTrackID= -1;
      
      if ((pickMipStubNo>=0 && iMipStub!=pickMipStubNo) ||
              (pickMipStubAngle>-100. && Math.abs(pickMipStubAngle - mStub.getPhi())>10./180.*Math.PI)){
        continue;
      }
      
      List<GarfieldTrack> listTemporaryTracks = new ArrayList<GarfieldTrack>();
      GarfieldTrack newTrk = new GarfieldTrack(mStub, debugLevel);
      newTrk.setID(issueTrackID());
      listTemporaryTracks.add(newTrk);
      
      // start adding hits to the track and increasing
      // listTemporaryTracks while looping over it
      
      int ii = 0;
      boolean done = false;
      while(!done) { // loop over list of temporary tracks
        GarfieldTrack g = (GarfieldTrack) listTemporaryTracks.get(ii);
        if (debugLevel>=2){
          System.out.println("GarfieldTrackFinder, next track = "+g.getID());
          g.debug();
        }
        
        // compiling list selectedHits of eligible hits from the next layer
        
        getNextHits(g,1.);
        if (selectedHits.size()==0 && g.getEquivnHits()>0){
          getNextHits(g,scaleSearchRegion);
        }
        if (selectedHits.size()==0 && g.getEquivnHits()>1){
          g.fastChi2Fit(this.nFitIterations); // try if refitting helps us to find hits
          getNextHits(g,this.scaleSearchRegion);
        }
        
        // creating new track object for each of the eligible hits and adding it to
        // the listTemporaryTracks
        
        int nNewHits = selectedHits.size();
        if (nNewHits==0) {
          // no hits found, done !
          if (debugLevel>=2) System.out.println("GarfieldTrackFinder done with picking up hits track="+g.getID()+", nhit="+g.getnHits()+" helix base="+g.hel.base(0)+" "+g.hel.base(1)+"minD="+this.minDistanceInHits);
        } else if (nNewHits > nNewHitsMax){
          if (debugLevel >=2) System.out.println("GarfieldTrackFinder process: too many new hits nHNew="+nNewHits+" adding best Hit only");
          GarfieldTrack gNew = new GarfieldTrack(g);
          gNew.setID(issueTrackID());
          if (addHit(gNew,bestHit)){listTemporaryTracks.add(gNew);}
        } else {
          for (int j = 0 ; j<nNewHits ; j++){
            GarfieldHit h = (GarfieldHit) selectedHits.get(j);
            GarfieldTrack gNew = new GarfieldTrack(g);
            gNew.setID(issueTrackID());
            if (addHit(gNew,h)) listTemporaryTracks.add(gNew);
            if (listTemporaryTracks.size() > maxTrkNmbr){
              panicPurge(listTemporaryTracks,maxTrkNmbr); // panic, too many tracks
            }
          }
        }
        
        if (debugLevel>=3) System.out.println("GarfieldTrackFinder done with temporary track "+g.getID());
        ii++; // increase counter of tracks
        done = ii == listTemporaryTracks.size();
      } // loop over list of temporary tracks
      
      if (debugLevel>=2) System.out.println("GarfieldTrackFinder finished creating track seed using MipStub. nTracks="+listTemporaryTracks.size());
      int mipStubStartInTrackList = listGarfieldTracks.size();
      
      // Go through the list of temporary tracks, apply cuts, add good tracks
      // to the listGarfieldTracks
      
      ListIterator k = listTemporaryTracks.listIterator();
      while (k.hasNext()) {
        GarfieldTrack gg = (GarfieldTrack) k.next();
        if (gg.isRejected()) continue;
        if (gg.getEquivnHits()<garfieldnTRKHitMin) continue;
        //gg.purgeHits(garfieldChi2ContribCut, garfieldnTRKHitMin, nFitIterations);
        if (gg.getnHits()<garfieldnTRKHitMin) continue;
        if (gg.getNdf()<=3){ // special cuts for small number of degrees of freedom
          double kabs = Math.abs(gg.hel.kappa());
          if (gg.getChi2()/gg.getNdf()>this.garfieldChi2Cut3Ndf) continue; // harder cut on tracks with rely too much on MipStub
          if (kabs>this.garfieldKappaCut3Ndf) continue; // harder cut on tracks with rely too much on MipStub
        }
        if (gg.getNumberOfStepovers()>1) continue;
        gg.fastChi2Fit(this.nFitIterations*4);
        if (gg.getChi2()/gg.getNdf() < garfieldChi2Cut){
          gg.setTrackParameters();
          gg.setRadii();
          gg.setGrade(garfieldChi2ContribCut);
          gg.setDone();
          if (gg.getGrade()>bestGrade){
            bestTrackID =gg.getID();
            bestGrade =gg.getGrade();
          }
          listGarfieldTracks.add(gg);
          if (debugLevel >=2) System.out.println("*accepting track "+gg.getID()+", nH="+gg.getnHits()+" chi2="+gg.getChi2()+" ndf="+gg.getNdf()+" cutvalue="+this.garfieldChi2Cut);
        } else {
          if (debugLevel >=2) System.out.println("rejecting track "+gg.getID()+", nH="+gg.getnHits()+" chi2="+gg.getChi2()+" ndf="+gg.getNdf()+" cutvalue="+this.garfieldChi2Cut);
        }
        if (debugLevel >=3) gg.debug();
        if (debugLevel >=3) System.out.println("******************");
      }
      
      if (debugLevel>=3) System.out.println("clearing  listTemporaryTracks");
      
      // If modeBestTrackOnly is set, go back and purge all tracks to the
      // current MipStub which are not optimal. Added Aug 5th 2004 EvT.
      
      if (modeBestTrackOnly){
        for (int k1=mipStubStartInTrackList; k1<listGarfieldTracks.size(); k1++) {
          GarfieldTrack gg1 = (GarfieldTrack) listGarfieldTracks.get(k1);
          if (gg1.getID() != bestTrackID) gg1.purgeTrack();
        }
      }
      
      listTemporaryTracks.clear();
    } // End of loop over MIP stubs
    
    //  Purge duplicate tracks :
    
    if (debugLevel > 0) System.out.println("GarfieldTrackFinder purging tracks now");
    for (int k1=0;k1<listGarfieldTracks.size();k1++){
      GarfieldTrack gg1 = (GarfieldTrack) listGarfieldTracks.get(k1);
      if (gg1.isPurged()) continue;
      int nh1 = gg1.getnHits();
      for (int k2=k1+1;k2<listGarfieldTracks.size();k2++){
        GarfieldTrack gg2 = (GarfieldTrack) listGarfieldTracks.get(k2);
        if (gg2.isPurged()) continue;
        int nh2 = gg2.getnHits();
        double overLap=overlapRatio(gg1,gg2);
        if (overLap>0.35){ // set to 0.3 if problems with fake 3hit tracks occur
          // purge one track
          if (gg1.getGrade() < gg2.getGrade()) gg1.purgeTrack();
          else gg2.purgeTrack();
          if (debugLevel > 1) System.out.println("purged one of pair "+gg1.getID()+" "+gg2.getID());
        }
      }
    }
    
    // Fit and add unpurged tracks to the final list :
    
    int nPurged = 0;
    ArrayList<GarfieldTrack> listGarfieldTracksGood = new ArrayList();
    int NTrks=0;
    for (int k=0;k<listGarfieldTracks.size();k++){
      GarfieldTrack gg = (GarfieldTrack) listGarfieldTracks.get(k);
      if (! gg.isPurged()){
        gg.fullChi2Fit(0.05, this.nFitIterations);
        gg.setTrackParameters();
        gg.setRadii();
        gg.setGrade(garfieldChi2ContribCut);
        gg.setDone();
        listGarfieldTracksGood.add(gg);
        /*
        ReconstructedTrack track = new ReconstructedTrack(bfield,
        gg.getPara("d0"),
        gg.getPara("phi0"),
        gg.getPara("kappa"),
        gg.getPara("z0"),
        gg.getPara("lambda"));
        track.mcp = gg.getMCParticle();
         
        defTrks[NTrks] = track;
         */
        NTrks++;
      } else {
        nPurged++;
      }
    }
    
    if (debugLevel>=1){
      System.out.println("List of good GarfieldTracks");
      for (GarfieldTrack gd : listGarfieldTracksGood){
        gd.debug();
      }
    }
    if (debugLevel>=1) System.out.println("GarfieldTrackFinder purged "+nPurged+" tracks");
    if (debugLevel>=3) System.out.println("clearing listGarfieldTracks");
    
    listGarfieldTracks.clear();
    
    // Put list of tracks into the event :
    
    listGarfieldTracksGood.trimToSize();
    event.put("GarfieldTracks",listGarfieldTracksGood);
    System.out.println("GarfieldTrackfinder found Ntracks="+listGarfieldTracksGood.size());
    /*
    if (modeModifyTracklist == APPEND) {
      TrackList tlist = event.getTrackList();
      Enumeration e = tlist.getTracks();
      int lngth = NTrks + tlist.getNTracks();
      final Vector v = new Vector(lngth);
      while (e.hasMoreElements()){
        ReconstructedTrack t = (ReconstructedTrack) e.nextElement();
        v.add(t);
      }
      for(int i=0; i<NTrks; i++) v.add(defTrks[i]);
      event.put(event.TrackList,new ReconTrackList(v));
    } else if (modeModifyTracklist == OVERWRITE){
      final Vector v = new Vector(NTrks);
      for(int i=0; i<NTrks; i++) v.add(defTrks[i]);
      event.put(event.TrackList,new ReconTrackList(v));
    }
     */
  }
  
  // -- Helper methods implementing parts of tracking algorithm :  -------------
  
  /**
   * Compile a list of eligible hits in the next layer and put it into
   * <code>selectedHits</code> for pattern recognition code to use.
   */
  private void getNextHits(GarfieldTrack g, double scale) {
    
    selectedHits.clear();
    
    int lastLayer = g.lastLayer;
    if (modeFromOutsideIn && (lastLayer == listOfHitsLowestLayer)) return;
    
    int newLayer= -1;
    int diffLayer = 0;
    minDistanceInHits = 1000000.;
    double d;
    boolean hitsAreOnTrack = false;
    double phi1=0.;
    if (g.getnHits()>0){
      hitsAreOnTrack=true;
      phi1= ((GarfieldHit) g.hits.get(g.getnHits()-1)).getPhi();
    }
    double dMax = g.distanceCutValue()*scale;
    
    // Loop over hits
    
    for (GarfieldHit h : listOfHits) {
      
      int hLayer = h.getLayer();
      boolean good = false;
      
      if (modeFromOutsideIn){ // outSideIn Tracking
        diffLayer = lastLayer - hLayer;
        if (newLayer == -1 && hLayer < lastLayer) good=true;
        if (newLayer != -1 && hLayer == newLayer) good=true;
      }
      if (!modeFromOutsideIn){ // InSideOut Tracking
        diffLayer = hLayer - lastLayer;
        if (newLayer == -1 && hLayer > lastLayer) good=true;
        if (newLayer != -1 && hLayer == newLayer) good=true;
      }
      
      if ((newLayer != -1) && !(good)) break; // D.O.
      if (diffLayer > 6) break; // added July 25 to speed up code
      if (diffLayer > 1 && !hitsAreOnTrack ) break; // added July 25 to speed up code
      
      if (! good) continue;
      
      if (hitsAreOnTrack) {
        double phi2 = h.getPhi();
        double deltaPhi = Math.abs(phi1-phi2);
        deltaPhi = Math.min(deltaPhi,Math.abs(deltaPhi-2.*Math.PI));
        if (deltaPhi>Math.PI/2.) good=false;
      }
      if (! good) continue;
      
      d=h.distanceToHit(g.hel,g.hasZMeasurement());
      if (d<this.minDistanceInHits){
        this.minDistanceInHits=d;
        this.bestHit = h;
      }
      if (d<dMax){
        this.selectedHits.add(h);
        newLayer = hLayer;
      }
    }
    if (debugLevel>=2){
      if (selectedHits.size()>0) {
        System.out.println("GarfieldTrackFinder getNextHits T="
                +g.getID()+" found Nhits="+ selectedHits.size()+" in Layer "+newLayer+
                " minD="+minDistanceInHits);
      } else {
        System.out.println("GarfieldTrackFinder getNextHits T="+g.getID()+
                ", did not find hits. The lastLayer="+lastLayer+" minD="+minDistanceInHits);
      }
    }
  }
  
  /**
   * Add a hit to an existing track including decisions on how to update helix paramters.
   * Returns <code>true</code> if the hit is successfully added,
   * <code>false</code> if the hit has too large chi^2.
   */
  private boolean addHit(GarfieldTrack g, GarfieldHit h) {
    
    double distanceToBase = g.hel.distanceBaseToPoint(h);
    if ((g.getnHits() == 0 &&  distanceToBase > maxDistanceFirstHitToBase) ||
            (g.getnHits() > 0 &&  distanceToBase > 80.*k_cm)) {
      if (debugLevel>=2) System.out.println("routine addHit reject new hit T="+
              g.getID()+" h="+h.getID()+" dist="+distanceToBase);
      return false;
    }
    
    g.addHit(h);
    if (debugLevel>=2) System.out.println("Track "+g.getID()+" adding hit=" +
            h.getID()+ ",  Layer="+h.getLayer()+" new nHit="+g.getnHits());
    if ( g.getNumberOfStepovers()>1) return false;
    g.calculateHelixFromHits();
    
    if (this.refitDuringPatternReco && g.getEquivnHits() >= 3) {
      g.fastChi2Fit(nFitIterations);
      if (g.chi2Contrib(g.nHits-1)>200.0*this.garfieldChi2ContribCut ){
        g.dropHit(g.nHits-1); // drop last hit
        g.calculateHelixFromHits();
        if (debugLevel>=2) {
          System.out.println("rejecting Hit T="+g.getID()+" H="+h.getID()+" chi2ndf="+g.getChi2()/g.getNdf()+" stpOver="+g.getNumberOfStepovers());
          if (debugLevel>=4) g.debug();
        }
        return false;
      } else {
        g.setStatus("WITH_HITS_FITTED");
        return true;
      }
    } else{
      g.setStatus("WITH_HITS");
      return true;
    }
  }
  
  
  // -- Simple helper functions :  ---------------------------------------------
  
  private int issueTrackID() {return issuedTrackIDValue++;}
  
  private boolean isEven(int i){
    if (2*((int) (i/2))==i) return true;
    return false;
  }
  
  private double overlapRatio(GarfieldTrack g1, GarfieldTrack g2) {
    int nOverlap = 0;
    for (int j=0; j<g1.getnHits(); j++){
      for (int k=0; k<g2.getnHits(); k++){
        if (g1.getHitID(j)==g2.getHitID(k)) nOverlap++;
      }
    }
    int nMin = Math.min(g1.getnHits(),g2.getnHits());
    if (nMin<1) return -1.;
    double r = ((double) nOverlap)/((double) nMin);
    return r;
  }
  
  private void panicPurge(List lst, int ntrMax) {
    int ntr = lst.size();
    System.out.println("GarfieldTrackFinder PanicPurge size="+ntr+" max allowed "+ntrMax);
    System.out.println("GarfieldTrackFinder PanicPurge brutal removal of last couple of tracks");
    for (int j = ntr-1; j<ntrMax ; j--){
      lst.remove(j);
    }
  }
  
  
  // --  Plotting, Debugging, Testing, and Examples :  -------------------------
  
  public void testEvent(EventHeader event){
    int[] hitList = new int[]{0,1,2,3,4,5,6};
    int nHitInList =7;
    int mstubID = 0;
    List listMipStub = (List) event.get("GarfieldMipStubs");
    MipStub mStub = (MipStub) listMipStub.get(mstubID);
    GarfieldTrack newTrk = new GarfieldTrack(mStub, 0);
    newTrk.setID(this.issueTrackID());
    for (int i=0; i<nHitInList;i++){
      addHit(newTrk,(GarfieldHit) (this.listOfHits).get(hitList[i]));
      newTrk.debug();
    }
    newTrk.debug();
    newTrk.calculateChi2();
    newTrk.fullChi2Fit(0.1,this.nFitIterations);
    newTrk.debug();
    newTrk.purgeHits(this.garfieldChi2ContribCut,this.garfieldnTRKHitMin,this.nFitIterations);
    newTrk.debug();
    newTrk.setTrackParameters();
    newTrk.setRadii();
    newTrk.setDone();
    newTrk.debug();
  }
  
  /**
   * example for stand-alone tracking for Wolfgang M.
   */
  public void testGenericTrackFit(EventHeader event) {
    GarfieldTrack newTrk = new GarfieldTrack();
    newTrk.setDebugLevel(3);
    newTrk.setID(issueTrackID());
    double k_mm = 0.1;
    double err = 2.0 * k_mm; // global error on example hit is 2 millimeter
    int layer = 0; // just a dummy
    int id = 0;
    newTrk.addHit( new GarfieldHit(new double[]{0.0, 0.0, 0.0}, err, layer, id++));
    newTrk.addHit( new GarfieldHit(new double[]{0.2, 0.2, 1.0}, err, layer, id++));
    newTrk.addHit( new GarfieldHit(new double[]{0.2, 0.4, 2.0}, err, layer, id++));
    newTrk.addHit( new GarfieldHit(new double[]{0.4, 0.4, 3.0}, err, layer, id++));
    newTrk.addHit( new GarfieldHit(new double[]{0.6, 0.6, 4.0}, err, layer, id++));
    newTrk.addHit( new GarfieldHit(new double[]{0.6, 0.8, 4.0}, err, layer, id++));
    newTrk.addHit( new GarfieldHit(new double[]{0.8, 0.8, 5.0}, err, layer, id++));
    newTrk.addHit( new GarfieldHit(new double[]{1.0, 1.0, 6.0}, err, layer, id++));
    newTrk.addHit( new GarfieldHit(new double[]{1.0, 1.2, 7.0}, err, layer, id++));
    newTrk.debug();
    double p0[]=newTrk.getHitPoint(0);
    double p1[]=newTrk.getHitPoint(newTrk.getnHits()-1);
    newTrk.setHelix(p0,p1);
    newTrk.fullChi2Fit(0.1,20);  // first argument is first stepsize
    newTrk.debug();
    newTrk.setTrackParameters();
    newTrk.setRadii();
    newTrk.setDone();
    newTrk.debug();
    
    // plotting helix
    double step = 0.1;
    for (int i=-2 ; i<20;i++){
      newTrk.hel.setPointOnHelix(step * (double) i);
    }
    // plotting hits
    for (int k=0 ; k<newTrk.getnHits();k++){
      double[] ph = newTrk.getHitPoint(k);
    }
  }
  
  /**
   * another example on how to fit a track
   */
  public void testEv5(EventHeader event) {
    List listMipStub = (List) event.get("GarfieldMipStubs");
    MipStub mStub = (MipStub) listMipStub.get(0);
    GarfieldTrack newTrk = new GarfieldTrack(mStub, 0);
    newTrk.setID(this.issueTrackID());
    GarfieldHit ha = (GarfieldHit) (this.listOfHits).get(0);
    GarfieldHit hb = (GarfieldHit) (this.listOfHits).get(4);
    GarfieldHit hc = (GarfieldHit) (this.listOfHits).get(9);
    GarfieldHit hd = (GarfieldHit) (this.listOfHits).get(11);
    GarfieldHit he = (GarfieldHit) (this.listOfHits).get(14);
    addHit(newTrk,ha);
    addHit(newTrk,hb);
    addHit(newTrk,hc);
    addHit(newTrk,hd);
    addHit(newTrk,he);
    newTrk.debug();
    newTrk.calculateChi2();
    newTrk.setHelixBaseToPCA();
    newTrk.calculateChi2();
    newTrk.debug();
    newTrk.fastChi2Fit(5);
    newTrk.debug();
    newTrk.fastChi2Fit(5);
    newTrk.debug();
    newTrk.purgeHits(this.garfieldChi2ContribCut,this.garfieldnTRKHitMin,this.nFitIterations/2);
  }
  
}
