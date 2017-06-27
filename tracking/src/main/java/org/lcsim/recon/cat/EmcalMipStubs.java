package org.lcsim.recon.cat;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;
import org.lcsim.event.EventHeader;
import org.lcsim.geometry.IDDecoder;
import org.lcsim.recon.cat.util.Const;
import org.lcsim.util.Driver;
import org.lcsim.util.aida.AIDA;

/**
 * creates a list of <code>MipStub</code> which is
 * filled into the event. Name of event entry is in <code>emcalMipStubsListName</code>.
 * The default is <code>EmcalMipStubs</code>.
 *
 * @see MipStub
 * @see GarfieldTrackFinder
 *
 * @author  E. von Toerne
 * @author  K. Yamanaka
 * @version $Id: EmcalMipStubs.java,v 1.2 2009/09/15 23:23:21 jeremy Exp $
 */
final public class EmcalMipStubs extends Driver {
  
  private AIDA aida = AIDA.defaultInstance();
  private int debugLevel;
  private int minEcalHits=2;
  private int evtNo=0;
  private boolean useSecondMipStub=false;
  private boolean useThirdMipStub=false;
  private double[] tempBase;
  private double[] tempDir;
  private double[] strtP;
  private double[] mdlP;
  private double[] endP;
  private double lmax;
  private double lmin;
  private double deltaR;
  private int minLayer=0;
  private CircleFromPoints cir;
  static final double k_cm= Const.cm;
  static final double barrelRadius = 126.0 * k_cm;
  static final double rotationalKappaCompensation = 0.0 / k_cm;
  private int minLayerCut = 3; // used in isMipStub()
  boolean createStubTrackList = false;  //EVTFIXME
  
  static final double cosThetaEndcap = 0.8;
  static final double alphaFactor = 0.015 / k_cm;
  static String emcalMipStubsListName = "EmcalMipStubs";
  /**
   * @param debugL debugLevel. =0 not debug Output, =3 some output
   */
  public EmcalMipStubs(String listName, int debugL) {
    emcalMipStubsListName = listName;
    this.debugLevel = debugL;
    if (debugLevel>=1) System.out.println("created EmcalMipStubs object");
    this.tempBase  = new double[]{0.,0.,0.};
    this.tempDir   = new double[]{0.,0.,0.};
    this.strtP = new double[]{0.,0.,0.};
    this.mdlP = new double[]{0.,0.,0.};
    this.endP  = new double[]{0.,0.,0.};
    this.cir = new CircleFromPoints();
  }
  /** Minimum number of ECAL hits needed to accept a cluster as a MipStub candidate. */
  public void setMinEcalHits(int i){ minEcalHits =i;}
  /** controls amount of debug text output and test histograms, =0 not output, >0 debug output  */
  public void setDebugLevel(int i){ debugLevel = i;}
  
  /**
   * returns true if c is deemed to contain a MipStub
   */
  public boolean isMipStub(EventHeader event, Cluster c){
    if (minLayer > minLayerCut) return false;
    if (c.getCalorimeterHits().size()<minEcalHits) return false;
    return true;
  }
  
  public boolean isEndcap(Cluster c){
    // xxx getITheta() is not filled yet...
    //if (Math.abs(Math.cos(c.getITheta()))>0.8) return true;
    double[] p=c.getPosition();
    return (Math.abs(p[2])/Math.sqrt(p[0]*p[0]+p[1]*p[1]+p[2]*p[2])>0.8);
  }
  
  /**
   * process event and create list of Mipstubs.
   * The list is stored in the event record
   */
  public void process( EventHeader event) {
    evtNo++;
    // list of clusters
    List distClusters = new ArrayList();
    List<MipStub> listStubs = new ArrayList();
    int cnum=0;
    List<List<Cluster>> clusterSets = event.get(Cluster.class);
    List<CalorimeterHit> hitsBar = event.get(CalorimeterHit.class,"EcalBarrHits");
    IDDecoder theCell = event.getMetaData(hitsBar).getIDDecoder();
    
    for (List<Cluster> clusters : clusterSets) {
      //CalorimeterIDDecoder theCell = (CalorimeterIDDecoder) event.getMetaData(clusters).getIDDecoder();
      String name = event.getMetaData(clusters).getName();
      if (name.indexOf("Ecal")<0) continue;
      if (debugLevel >=1) System.out.println("name of Calorimeter"+name);
      //if (event.get(Cluster.class, name+"NNClusters")==null) continue;
      //List<Cluster> clusters =event.get(Cluster.class, name+"NNClusters");
      for (Cluster c : clusters){
        cnum++;
        List cHits = c.getCalorimeterHits();
        MipStub stub1=null,stub2=null,stub3=null;
        setStartEndPoint(theCell, c, isEndcap(c));
        if (debugLevel>=1){
        }
        if (isMipStub(event,c)){
          stub1 = createMipStubFromFit(theCell, c);
	  //
	  if (useSecondMipStub) stub2 = createMipStubStraight(theCell, c); 
	  else stub2=null;
	  //
	  if (useThirdMipStub || isEndcap(c)) stub3 = createMipStub(theCell, c); 
	  else stub3=null;
	  //
          if (stub1 != null){
            //stub1.setMCParticles(c.getMCParticles());
            listStubs.add(stub1);
            if (debugLevel>=4 && stub1 != null ) stub1.debug();
          }
          if (stub2 != null){
            //stub2.setMCParticles(c.getMCParticles());
            listStubs.add(stub2);
            if (debugLevel>=4 && stub2 != null ) stub2.debug();
          }
          if (stub3 != null){
            //stub3.setMCParticles(c.getMCParticles());
            listStubs.add(stub3);
            if (debugLevel>=4 && stub3 != null ) stub3.debug();
          }
        }
      }
    }
    if (debugLevel >=3) System.out.println("EmcalMipStubs n="+listStubs.size());
    event.put(emcalMipStubsListName,listStubs);
    
    if (createStubTrackList){
      List<GarfieldTrack> listStubTracks = new ArrayList<GarfieldTrack>();
      for (MipStub mStub : listStubs) {
        GarfieldTrack newTrack = new GarfieldTrack(mStub, debugLevel);
        newTrack.setTrackParameters();
        listStubTracks.add(newTrack);
      }
      event.put("GarfieldStubTracks",listStubTracks);
    }
  }
  
  /** Interface to U. Iowa MipStub algorithm. */
  public MipStub createUIowaMipstub(IDDecoder theCell, Cluster c) {
    // needs to be implemented
    return createMipStubSimple(theCell, c);
  }
  
  public MipStub createMipStubFromFit(IDDecoder theCell, Cluster c) {
    int i;
    int nHit = c.getCalorimeterHits().size();
    cir.calculate(0.,0.,strtP[0],strtP[1],endP[0],endP[1]);
    GarfieldTrack newTrk = new GarfieldTrack();
    newTrk.setDebugLevel(debugLevel-2);
    double err = 0.2 * k_cm;
    for (i=0;i<=2;i++) tempBase[i]=0;
    newTrk.addHit( new GarfieldHit(endP,err,4,4));
    newTrk.addHit( new GarfieldHit(mdlP,5.*err,3,3));
    newTrk.addHit( new GarfieldHit(strtP,err,2,2));
    newTrk.addHit( new GarfieldHit(tempBase,10.*err,1,1));
    
    newTrk.setHelix(strtP,endP);
    newTrk.hel.setKappa(cir.getKappa());
    newTrk.calculateChi2();
    //newTrk.debug();
    newTrk.fullChi2Fit(0.1,2);
    if (strtP[0]*strtP[0]+strtP[1]*strtP[1]>2500.*k_cm*k_cm){
      newTrk.hel.setPointOnHelixWithXY(strtP[0],strtP[1]);} else {newTrk.hel.setPointOnHelixWithZ(strtP[2]);}
    //newTrk.debug();
    for (i=0;i<=2;i++){
      tempBase[i]=newTrk.hel.getPointOnHelix(i);
      tempDir[i]=newTrk.hel.dirAtPoint(i);
    }
    return new MipStub(tempBase,tempDir,newTrk.hel.kappa(),nHit, isEndcap(c),this.minLayer, c);
  }
  
  public MipStub createMipStubKyoko(IDDecoder theCell, Cluster c) {
    double[] base = new double[]{0,0,0};
    double[] dir = new double[]{0,0,0};
    double[] ddir = new double[]{0,0,0}; // added this on July 2004
    double kappa = 0.;
    int nHit = c.getCalorimeterHits().size();
    int iCount = 0;
    double[] p;
    double Rmax = -10.;
    double RRmax = -10.; // added this on July 2004
    double Rmin = 1000000.;
    double R;
    List<CalorimeterHit> cHits = c.getCalorimeterHits();
    int n = 0;
    double sigmaxy = 0.;
    double sigmax = 0.;
    double sigmay =0.;
    double sigmaxx =0.;
    double sigmayy = 0.;
    double sigmaxx2;
    double[] b = new double[100]; //xxx check!
    double[] rr = new double[100]; // Correlation coefficient
    double [] x = new double[100];
    double [] y = new double[100];
    double [] z = new double[100];
    double avex, avey; // Average
    double ssxx, ssyy,ssxy; // Sum of squares
    // loop over hits in cluster
    for (CalorimeterHit ch: cHits) {
      iCount++;
      theCell.setID(ch.getCellID());
      p = ch.getPosition();
      R = Math.sqrt(p[0]*p[0]+p[1]*p[1]+p[2]*p[2]);
      if (debugLevel>=5) System.out.println("EmcalMipStub pos="+p[0]+" "+p[1]+" "+p[2]);
      if (R>RRmax){ //The direction of the furthest point is used in a shower
        RRmax = R;
        ddir=p;
      }
      if (R<Rmin){ //The nearest point is Base point
        Rmin = R;
        base=p;
      }
      
      // The following was changed on July 23rd-30th, 2004
      // Least squares fitting on xy-plane
      //(http://mathworld.wolfram.com/LeastSquaresFitting.html)
      // Z direction is from the nearest point to the furthest point
      n = iCount;
      x[n] = p[0];
      y[n] = p[1];
      z[n] = p[2];
    }
    dir[0]=ddir[0]-base[0];
    dir[1]=ddir[1]-base[1];
    dir[2]=ddir[2]-base[2];
    double cotTheta= dir[2]/Math.sqrt(dir[0]*dir[0]+dir[1]*dir[1]);
    
    sort(x,y,z,n);
    
    for(int i=1;i<=n;i++){
      sigmaxy += x[i]*y[i];
      sigmax += x[i];
      sigmay += y[i];
      sigmaxx += x[i]*x[i];
      sigmayy += y[i]*y[i];
      sigmaxx2 = sigmax*sigmax;
      //Calculate the correlation coefficient on xy-plane
      avex = sigmax/i;
      avey = sigmay/i;
      ssxx = sigmaxx-i*avex*avex;
      ssyy = sigmayy-i*avey*avey;
      ssxy = sigmaxy-i*avex*avey;
      if(ssxx==0){
        b[i] = 1.E15;
        rr[i] = 1.;
      } else if(ssyy==0){
        b[i] = (i*sigmaxy-sigmax*sigmay)/(i*sigmaxx-sigmaxx2);
        rr[i] = 1.;
      } else{
        b[i] = (i*sigmaxy-sigmax*sigmay)/(i*sigmaxx-sigmaxx2);
        rr[i] = ssxy*ssxy/ssxx/ssyy;
      }
      //If there are more than six points, find the best correlation coefficient
      R = Math.sqrt(x[i]*x[i]+y[i]*y[i]+z[i]*z[i]);
      if (i<6 && R>Rmax){ //This 6 may be changed
        Rmax = R;
        dir[0] = x[i];
        dir[1] = y[i];
        dir[2] = z[i];
      } else{
        if(rr[i-1]>rr[i]){
          rr[i] = rr[i-1];
          b[i] = b[i-1];
          x[i] = x[i-1];
          y[i] = y[i-1];
          z[i] = z[i-1];
        }
        dir[0] = x[i];
        dir[1] = y[i];
        dir[2] = z[i];
      }
    }
    System.out.println("The correlation coefficient is "+rr[n]);
    
    R = norm(dir);
    if(R>Rmax)	Rmax = R;
    
    //When the correlation coefficient is more than 0.7, that is, there are few shower,
    //use the least square fitting
    if(rr[n]>0.7){ //This 0.7 may be changed
      //Find z-direction vector
      dir[0] = dir[0] - base[0];
      dir[1] = dir[1] - base[1];
      dir[2] = dir[2] - base[2];
      // Find x and y-direction vectors and normalize them
      if(dir[0]>0){
        dir[0] = 1/Math.sqrt(1+b[n]*b[n]);
        dir[1] = b[n]/Math.sqrt(1+b[n]*b[n]);
      } else{
        dir[0] = -1/Math.sqrt(1+b[n]*b[n]);
        dir[1] = -b[n]/Math.sqrt(1+b[n]*b[n]);
      }
      dir[2] = cotTheta*Math.sqrt(dir[0]*dir[0]+dir[1]*dir[1]);
    } else{ // When some shower exist, use the furthest point for finding direction vector
      dir[0] = ddir[0] - base[0];
      dir[1] = ddir[1] - base[1];
      dir[2] = ddir[2] - base[2];
    }
    // temporary mipstub
    double angle = getAngleBaseDir(base,dir);
    // calculate kappa with getAngleBaseDir()
    if (!isEndcap(c) && Math.abs(angle)>Math.PI/2.){
      kappa = 0.;
      dir[0]=base[0];
      dir[1]=base[1];
      dir[2]=base[2];
    } else{
      kappa= 0.02*angle;
      double maxKappaValue =1.2 *(1./( 0.5 * Math.sqrt(base[0]*base[0]+base[1]*base[1])));
      if (kappa < -maxKappaValue) kappa = -maxKappaValue;
      if (kappa > maxKappaValue) kappa = maxKappaValue;
    }
    return new MipStub(base,dir,kappa,nHit, isEndcap(c), this.minLayer);
  }
  
  
  /** KSU default algorithm to create a MipStub from an ECAL cluster. */
  public MipStub createMipStub(IDDecoder theCell, Cluster c) {
    double kappa   = 0.;
    int nHit = c.getCalorimeterHits().size();
    int iCount = 0;
    double[] p;
    double lngth;
    if (lmax-lmin<1.E-1){
      return new MipStub(strtP,strtP,kappa,nHit, isEndcap(c), this.minLayer);
    }
    
    int nBase = 0;
    int nEnd = 0;
    tempBase[0]=0.;
    tempBase[1]=0.;
    tempBase[2]=0.;
    tempDir[0]=0.;
    tempDir[1]=0.;
    tempDir[2]=0.;
    List<CalorimeterHit> cHits = c.getCalorimeterHits();
    for (CalorimeterHit ch: cHits) {
      theCell.setID(ch.getCellID());
      p = ch.getPosition();
      if (isEndcap(c)) lngth = Math.abs(p[2]);
      else lngth = Math.sqrt(p[0]*p[0]+p[1]*p[1]);
      //System.out.println(lngth+" "+lmin+" "+lmax);
      if (lngth<lmin+ 2. * k_cm) {
        nBase++;
        tempBase[0]+=p[0];
        tempBase[1]+=p[1];
        tempBase[2]+=p[2];
      } else if (lngth < lmin + 0.5 * (lmax-lmin)) {
        nEnd++;
        tempDir[0]+=p[0];
        tempDir[1]+=p[1];
        tempDir[2]+=p[2];
      }
    }
    //System.out.println("nend nBase "+nEnd+" "+nBase+" "+lmin+" "+lmax);
    if (nEnd==0 || nBase==0){
      tempBase[0]=strtP[0];
      tempBase[1]=strtP[1];
      tempBase[2]=strtP[2];
      tempDir[0]=endP[0]-strtP[0];
      tempDir[1]=endP[1]-strtP[1];
      tempDir[2]=endP[2]-strtP[2];
    } else{
      tempBase[0]=tempBase[0]/(double) nBase;
      tempBase[1]=tempBase[1]/(double) nBase;
      tempBase[2]=tempBase[2]/(double) nBase;
      tempDir[0]=(tempDir[0]/(double) nEnd)-tempBase[0];
      tempDir[1]=(tempDir[1]/(double) nEnd)-tempBase[1];
      tempDir[2]=(tempDir[2]/(double) nEnd)-tempBase[2];
    }
    double angle = getAngleBaseDir(tempBase,tempDir);
    if (!isEndcap(c) && Math.abs(angle)>Math.PI/2.){
      kappa = 0.;
      tempDir[0]=tempBase[0];
      tempDir[1]=tempBase[1];
      tempDir[2]=tempBase[2];
    } else{
      kappa= alphaFactor*angle*barrelRadius/Math.sqrt(tempBase[0]*tempBase[0]+
              tempBase[1]*tempBase[1]);
      //kappa= alphaFactor*angle;
      double maxKappaValue =1.2 *(1./( 0.5 * Math.sqrt(tempBase[0]*tempBase[0]+
              tempBase[1]*tempBase[1])));
      if (kappa < -maxKappaValue) kappa = -maxKappaValue;
      if (kappa > maxKappaValue) kappa = maxKappaValue;
      double phiRot = -rotationalKappaCompensation*kappa * Math.abs(deltaR);
      double dirx=Math.cos(phiRot)*tempDir[0]-Math.sin(phiRot)*tempDir[1];
      double diry=Math.sin(phiRot)*tempDir[0]+Math.cos(phiRot)*tempDir[1];
      tempDir[0]=dirx;
      tempDir[1]=diry;
    }
    return new MipStub(tempBase,tempDir,kappa,nHit, isEndcap(c), this.minLayer);
  }
  
  /** simple approach to create a MipStub from an ECAL cluster uses Start and Endpoint of cluster */
  public MipStub createMipStubSimple(IDDecoder theCell, Cluster c) {
    double kappa = 0.;
    int nHit = c.getCalorimeterHits().size();
    int iCount = 0;
    this.lmax = -10.;
    this.lmin = 1000000.;
    double lngth;
    double rxy;
    List<CalorimeterHit> cHits = c.getCalorimeterHits();
    if (lmax-lmin<1.E-1){return new MipStub(strtP,strtP,0.,nHit,isEndcap(c), this.minLayer);} else{
      tempDir[0]=endP[0]-strtP[0];
      tempDir[1]=endP[1]-strtP[1];
      tempDir[2]=endP[2]-strtP[2];
      MipStub stub = new MipStub(strtP,tempDir,kappa,nHit,isEndcap(c), this.minLayer);
      kappa = alphaFactor* stub.getAngleBaseDir()*barrelRadius/Math.sqrt(strtP[0]*strtP[0]+strtP[1]*strtP[1]);
      //kappa = alphaFactor* stub.getAngleBaseDir();
      double maxKappaValue =1.2 *(1./( 0.5 * Math.sqrt(strtP[0]*strtP[0]+
              strtP[1]*strtP[1])));
      if (kappa < -maxKappaValue) kappa = -maxKappaValue;
      if (kappa > maxKappaValue) kappa = maxKappaValue;
      return new MipStub(strtP,tempDir,kappa,nHit,isEndcap(c), this.minLayer);
    }
  }
  
  public MipStub createMipStubStraight(IDDecoder theCell, Cluster c) {
    double kappa = 0.;
    int nHit = c.getCalorimeterHits().size();
    int iCount = 0;
    this.lmax = -10.;
    this.lmin = 1000000.;
    double lngth;
    double rxy;
    List<CalorimeterHit> cHits = c.getCalorimeterHits();
    if (lmax-lmin<1.E-1){return null;} else{
      tempDir[0]=endP[0]-strtP[0];
      tempDir[1]=endP[1]-strtP[1];
      tempDir[2]=endP[2]-strtP[2];
      return new MipStub(strtP,tempDir,kappa,nHit,isEndcap(c), this.minLayer);
    }
  }

  private void setStartEndPoint(IDDecoder theCell, Cluster c,  boolean isEndcap){
    int i;
    List<CalorimeterHit> cHits = c.getCalorimeterHits();
    double lngth;
    double rxy;
    double rxyMin=-10.;
    double rxyMax=1000000;
    this.minLayer = 1000000;
    this.lmax= -1.E10;
    this.lmin = 1.E10;
    for (CalorimeterHit ch: cHits) {
      int iCount = 0;
      iCount++;
      theCell.setID(ch.getCellID());
      double[] p = ch.getPosition();
      if (isEndcap) lngth = Math.abs(p[2]);
      else lngth = Math.sqrt(p[0]*p[0]+p[1]*p[1]);
      rxy = Math.sqrt(p[0]*p[0]+p[1]*p[1]);
      if (debugLevel>=5) System.out.println("EmcalMipStub pos="+p[0]+" "+p[1]+" "+p[2]);
      if (lngth>lmax) {
        this.lmax = lngth;
        rxyMax = rxy;
        for (i=0;i<=2;i++) endP[i]=p[i];
      }
      if (lngth<lmin) {
        this.lmin = lngth;
        rxyMin = rxy;
        for (i=0;i<=2;i++) strtP[i]=p[i];
        int theLayer =  theCell.getLayer();
        if (theLayer < minLayer) this.minLayer = theLayer;
      }
    }
    
    this.deltaR = Math.abs(rxyMax - rxyMin);
    for (i=0;i<=2;i++) mdlP[i]=0.;
    int nmdl=0;
    List<CalorimeterHit> cHits2 = c.getCalorimeterHits();
    for (CalorimeterHit ch: cHits2) {
      //int iCount = 0;
      //iCount++;
      theCell.setID(ch.getCellID());
      double[] p = ch.getPosition();
      if (isEndcap) lngth = Math.abs(p[2]);
      else lngth = Math.sqrt(p[0]*p[0]+p[1]*p[1]);
      if (Math.abs(lngth-0.5*(lmax+lmin))<0.15*(lmax-lmin)){
        for (i=0;i<=2;i++) mdlP[i]=mdlP[i]+p[i];
        nmdl++;
      }
    }
    if (norm(mdlP)<1.*k_cm){
      for (i=0;i<=2;i++) mdlP[i]=0.5*(strtP[i]+endP[i]);} else{ for (i=0;i<=2;i++) mdlP[i]=mdlP[i]/nmdl;}
  }
  
  // simple helper functions
  private double norm(double[]x){
    return Math.sqrt(x[0]*x[0]+x[1]*x[1]+x[2]*x[2]);
  }
  //Sort the points
  public static void sort(double[]x,double[]y,double[]z, int n){
    double[] RR = new double[100];
    for(int i=1;i<=n;i++){
      RR[i] = Math.sqrt(x[i]*x[i]+y[i]*y[i]+z[i]*z[i]);
    }
    for(int i=1;i<=n;i++){
      int min = i;
      for(int j=i;j<=n;j++){
        if(RR[j]<RR[i]) min=j;
      }
      double tmpx,tmpy,tmpz;
      tmpx = x[i];
      tmpy = y[i];
      tmpz = z[i];
      x[i] = x[min];
      y[i] = y[min];
      z[i] = z[min];
      x[min] = tmpx;
      y[min] = tmpy;
      z[min] = tmpz;
    }
  }
  
  private double getAngleBaseDir(double[] base, double[] dir) {
    double angleBaseDir;
    double lBase = Math.sqrt(base[0]*base[0]+base[1]*base[1]);
    double lDir = Math.sqrt(dir[0]*dir[0]+dir[1]*dir[1]);
    if (lBase*lDir>1.E-9){
      double c=(base[0]*dir[0]+base[1]*dir[1])/(lBase*lDir);
      if (Math.abs(c)>1.){ angleBaseDir = 0.;} else {angleBaseDir = Math.acos(c);}
      if (base[0]*dir[1]-base[1]*dir[0]<0.) angleBaseDir=-angleBaseDir;
    } else angleBaseDir = 0.;
    return angleBaseDir;
  }

  public void setMinLayerCut(int val){minLayerCut =val;}
  public void setCreateStubTrackList(boolean val){createStubTrackList =val;}
  public void setUseSecondMipStub(boolean val){useSecondMipStub = val;}
  public void setUseThirdMipStub(boolean val){useThirdMipStub   = val;}
    
}



