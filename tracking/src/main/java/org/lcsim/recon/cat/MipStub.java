package org.lcsim.recon.cat;

import java.util.ArrayList;
import java.util.List;

import org.lcsim.event.CalorimeterHit;
import org.lcsim.event.Cluster;
import org.lcsim.event.MCParticle;

/**
 * Simple data format for Minimum ionizing particles in the ECAL.
 * MipStub is used within the Garfield track finding package for
 * calorimeter assisted tracking with the SiD
 *
 * @see EmcalMipStubs
 * @see GarfieldTrackFinder
 *
 * @author  E. von Toerne
 * @version $Id: MipStub.java,v 1.3 2012/07/23 10:56:43 grefe Exp $
 */
final public class MipStub implements Cluster {
  
  // -- Data :  ----------------------------------------------------------------
  
  public double[] base;
  public double[] dir;
  public double kappa;  // curvature estimate
  private double baseError;
  private double dirXYError;
  private double dirZError;
  private double kappaError;
  private boolean isEndcap = false;
  public double energy;  // energy estimate
  private double energyError;
  public double angleBaseDir;     // angle FROM BASE to DIR in xy
  //static double maxKappaValue = 1./50.;  // maximum allowed curvature estimate
  static final double k_cm = 10.;
  public int debugLevel;
  private int nHits=0;
  private int minLayer=-1;
  private MCParticle[] mcParticles = null;
  
  private Cluster cluster = null;
  
  // -- Constructors :  --------------------------------------------------------
  
  /**
   * Default constructor
   */
  public MipStub() {
    this.base = new double[]{0,0,0};
    this.dir = new double[]{0,0,0};
    this.kappa = 0.;
    this.energy = -1.;
    this.angleBaseDir=0.;
    nHits = 0;
    normalizeDir();
    setBaseError();
    setDirError();
    setKappaError();
  }
  
  /**
   * standard constructor
   * @param p         base, which is also the entrance point into the ECAL
   * @param d         direction of MIP at entrance point
   * @param ka        curvature (one over curvature radius)
   * @param nHit      number of hits in cluster
   * @param isNdCap   boolean, true if MipStub and its cluster is in Endcap
   * @param minLay    Minimum layer in ECAL (approx. 0 for MIPS)
   *
   */
  public MipStub(double[] p, double[] d, double ka, int nHit, boolean isNdCap, int minLay) {
    this.base = new double[]{p[0],p[1],p[2]};
    this.dir = new double[]{d[0],d[1],d[2]};
    this.kappa = ka;
    this.energy = -1.;
    this.nHits = nHit; //
    this.isEndcap = isNdCap;
    this.minLayer = minLay;
    normalizeDir();
    setAngleBaseDir();
    setBaseError();
    setDirError();
    setKappaError();
  }
  
  /**
   * Standard constructor that also takes a reference to the original cluster
   * (temporary, D.O.)
   *
   * @param p         base, which is also the entrance point into the ECAL
   * @param d         direction of MIP at entrance point
   * @param ka        curvature (one over curvature radius)
   * @param nHit      number of hits in cluster
   * @param isNdCap   boolean, true if MipStub and its cluster is in Endcap
   * @param minLay    Minimum layer in ECAL (approx. 0 for MIPS)
   * @param cluster   Cluster from which this MIP stub was created
   *
   */
  public MipStub(double[] p, double[] d, double ka, int nHit, boolean isNdCap, int minLay, Cluster cluster) {
    this.base = new double[]{p[0],p[1],p[2]};
    this.dir = new double[]{d[0],d[1],d[2]};
    this.kappa = ka;
    this.energy = -1.;
    this.nHits = nHit; //
    this.isEndcap = isNdCap;
    this.minLayer = minLay;
    normalizeDir();
    setAngleBaseDir();
    setBaseError();
    setDirError();
    setKappaError();
    this.cluster = cluster;
  }
  
  // -- Getters :  -------------------------------------------------------------
  
  public double kappa(){ return kappa;}
  public int getnHits(){ return nHits;}
  public double getPhi(){return Math.atan2(base[1],base[0]);}
  public double getAngleBaseDir(){ return angleBaseDir;}
  public double getDirXYError(){ return dirXYError;}
  public double getDirZError(){ return dirZError;}
  public double getBaseError(){return baseError;}
  public double getKappaError(){return kappaError;}
  public int getMinLayer(){return minLayer;}
  public boolean isEndcap(){return isEndcap;}
  public int charge() { return (kappa<0.) ? -1 : 1 ;}
  public MCParticle[] getMCParticles() {return mcParticles;}
  
  public double getEnergyError()
  {
      return energyError;
  }
  
  public void setEnergyError(double energyError)
  {
      this.energyError = energyError;
  }
  
  // -- Public setters :  ------------------------------------------------------
  
  public void setMCParticles(MCParticle[] mcParticles) {this.mcParticles = mcParticles;}
  public void setKappa(double ka){kappa=ka;}
  
  
  // -- Private helper functions :  ---------------------------------------------
  
  /**
   * normalizes the direction vector. convention here follows {@link GarfieldHelix}
   * xy-projection of dir is of length one.
   *
   * @see             GarfieldHelix
   */
  private void normalizeDir(){
    // normalization: xy direction has length one.
    double d=Math.sqrt(dir[0]*dir[0]+dir[1]*dir[1]);
    if (d>1.E-20){
      dir[0]=dir[0]/d;
      dir[1]=dir[1]/d;
      dir[2]=dir[2]/d;
    } else {
      dir[0]=1;
      dir[1]=0.;
      dir[2]=dir[2]*1.E10;
    }
  }
  
  /**
   * calculates the angle from base to the dir vector in the xy plane.
   */
  private void setAngleBaseDir() {
    double lBase = Math.sqrt(base[0]*base[0]+base[1]*base[1]);
    if (lBase>1.E-9){
      double c=(base[0]*dir[0]+base[1]*dir[1])/(lBase);  // please note that dir-xy is of norm one
      if (Math.abs(c)>1.){ angleBaseDir = 0.;} else {angleBaseDir = Math.acos(c);}
      if (base[0]*dir[1]-base[1]*dir[0]<0.) angleBaseDir=-angleBaseDir;
    } else angleBaseDir = 0.;
  }
  
  /**
   * calculates a rough error estimate for direction vector in XY plane
   * based on <code>nHits</code> only
   */
  private void setDirError(){
    if (nHits>=10) dirXYError=20.;
    else if (nHits >=5) dirXYError=25.;
    else dirXYError= 45.;
    dirXYError = Math.sqrt(dirXYError*dirXYError+900.*angleBaseDir*angleBaseDir);
    dirZError = dirXYError / 5. * k_cm; // set dirZError
    // conversion from degrees into rads
    dirXYError = dirXYError*Math.PI/180.;
    if (isEndcap()){
      dirZError = dirZError * 10.;
      dirXYError = dirXYError * 10.;
    }
  }
  
  private void setBaseError(){ baseError = 1.0 * k_cm;}
  
  private void setKappaError(){
    //kappaError = 0.001/k_cm;
    //kappaError = Math.sqrt(kappaError*kappaError+0.3*0.3*kappa*kappa);
    if (nHits>=15) kappaError=0.03;
    else if (nHits>=10) kappaError=0.035;
    else if (nHits>=5) kappaError=0.05;
    else kappaError=0.1;
    if (isEndcap()) kappaError = kappaError * 10.;
  }
  
  // -- Debugging :  -----------------------------------------------------------
  
  // plotting, testing and debugging
  void debug(){
    //if (debugLevel <1) return;
    System.out.println("MipStub base="+this.base[0]+" "+this.base[1]+" "+this.base[2]);
    System.out.println("MipStub dir="+this.dir[0]+" "+this.dir[1]+" "+this.dir[2]+" kappa="+this.kappa+" angle="+angleBaseDir);
  }
  
  /**
   * prepares graphic presentation of a mipstub by providing a list of positions
   * this plot routine uses quite a bit of memory.
   *
   */
  public List plot(){
    // creates a list of 3-D point that represent the MipStub and that that can be used in plotting of histograms
    List lst = new ArrayList();
    double s = 2.*k_cm; // step size for the plot
    for (int j=0;j<=5;j++){
      double[] pix=new double[]{0,0,0};
      pix[0]=base[0]+s*dir[0]*j;
      pix[1]=base[1]+s*dir[1]*j;
      pix[2]=base[2]+s*dir[2]*j;
      lst.add(pix);
    }
    GarfieldHelix hh = new GarfieldHelix(base,dir,kappa);
    for (int j=0;j<=10;j++){
      double[] pix=new double[]{0,0,0};
      hh.setPointOnHelix(-8.*k_cm*j);
      pix[0]=hh.getPointOnHelix(0);
      pix[1]=hh.getPointOnHelix(1);
      pix[2]=hh.getPointOnHelix(2);
      lst.add(pix);
    }
    return lst;
  }
  
  // -- Implementing org.lcsim.event.Cluster :  --------------------------------
  // -- (dummy implementations for some methods - this should be fixed later) --

  public int getType() {return 0;}  // FixMe

  public double[] getSubdetectorEnergies() {return new double[] {getEnergy()};}

  public double[] getShape() {return new double[6];}  // Meaning is undefined in LCIO

  // TODO return meaningful position error
  public double[] getPositionError() {return new double[] {baseError, baseError, baseError, baseError, baseError, baseError};}

  public double[] getPosition() {return base;}

  public double getITheta() {return 0.;}

  public double getIPhi() {return 0.;}

  public double[] getHitContributions() {
    if (cluster == null) {
      return new double[] {0.};
    } else {
      return cluster.getHitContributions();
    }
  }

  public double getEnergy() {return energy;}

  public double[] getDirectionError() {return new double[] {dirXYError, dirXYError, dirZError};}

  public List<Cluster> getClusters() {
    List<Cluster> list = new ArrayList<Cluster>(1);
    if (cluster != null) {
      list.add(cluster);
    }
    return list;
  }

  public List<CalorimeterHit> getCalorimeterHits() {
    if (cluster == null) {
      return new ArrayList<CalorimeterHit>(1);
    } else {
      return cluster.getCalorimeterHits();
    }
  }

  public int getSize() {return nHits;}

  public int getParticleId() {
      return 0;
  }
  
}

