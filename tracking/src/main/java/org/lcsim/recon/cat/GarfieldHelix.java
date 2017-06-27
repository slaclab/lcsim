package org.lcsim.recon.cat;

//import hep.physics.*;
//import hep.lcd.event.*;
//import hep.lcd.mc.fast.*;
//import hep.lcd.util.driver.*;
//import hep.lcd.geometry.*;
import java.util.*;
import java.lang.Math;
//import hep.lcd.mc.smear.SmearDriver;
//import hep.lcd.recon.tracking.*;
//import hep.lcd.recon.cluster.simple.SimpleClusterBuilder;
//import hep.lcd.recon.cluster.radial.RadialClusterBuilder;

/**
 * simple helix format for general hits, either 2D or 3D
 * used within the Garfield track finding package for 
 * calorimeter assisted tracking with the SiD.
 * <p>
 * parametrization is redundant. A helix is defined by<br>
 * - a point on the helix (base)<br> 
 * - direction vector at this point (dir)<br> 
 * - curvature (kappa)<br>
 * dir is normalized such that the projection of dir into xy plane has length one.
 *
 * @see GarfieldTrackFinder 
 *
 * @author  E. von Toerne
 * @version $Id: GarfieldHelix.java,v 1.1 2007/04/06 21:48:14 onoprien Exp $
 */
final public class GarfieldHelix
{
  // definition of variables
  private static double k_cm = 10.;
  private double[] base;  // 
  private double[] dir;
  private double kappa;  // if viewing along negative z, positive kappa bends counterclockwise xxx check!!
  private double[] pointOnHelix; //last point calculated on the helix by routine pointOnHelix
  private double sSave; // value s associated with pointOnHelix[]
  private int debugLevel;

  // constructors

    /** 
     * empty constructor
     */
  public GarfieldHelix()
  {
    this.base = new double[]{0.,0.,0.};
    this.dir = new double[]{1.,0.,0.};
    this.pointOnHelix = new double[]{0.,0.,0.};
    this.kappa = 0.;
    this.sSave = 0.;
    normalizeDir();
    debugLevel=0;
  }
    /** 
     * @param b base (= point on helix)
     * @param d dir  (= helix direction vector at base point)
     * @param ka curvature 
     */
  public GarfieldHelix(double[] b, double[] d, double ka)
  {
      this.base = new double[]{b[0],b[1],b[2]};  // was =b which is very bad!!
    this.dir =  new double[]{d[0],d[1],d[2]};;
    this.pointOnHelix = new double[]{0.,0.,0.};
    normalizeDir();
    kappa = ka;
    this.sSave = 0.;
    debugLevel = 0;
  }

  // simple data retriever
  public int q()  {
    if (kappa<0.) return -1;
    return 1;
  }    
  public double sSave(){ return sSave;}
  public double dir(int i){ return dir[i];}
  public double base(int i){ return base[i];}
  public double[] dir(){ return dir;}
  public double[] base(){ return base;}
  public double kappa(){ return kappa;}
  final public double getPointOnHelix(int i){ return pointOnHelix[i];}

  // simple value setter
    /** controls amount of debug text output and test histograms, =0 not output, >0 debug output  */
  public void setDebugLevel(int i){ debugLevel = i;}

  public void setKappa(double ka){ kappa=ka;}

  public void setBase(double x,double y,double z)
  {
    base[0] = x;
    base[1] = y;
    base[2] = z;
  }

  public void setDir(double x,double y, double z)
  {
    dir[0] = x;
    dir[1] = y;
    dir[2] = z;
    normalizeDir();
  }

  // simple helper functions
  public double sqr(double x){ return x*x;}

    /**
     * normalizes the direction vector. 
     * xy-projection of dir is of length one.
     */
  public void normalizeDir(){
    double r=Math.sqrt(dir[0]*dir[0]+dir[1]*dir[1]);
    if (r>1.E-25){
      dir[0]=dir[0]/r;
      dir[1]=dir[1]/r;
      dir[2]=dir[2]/r;
    }
  }

  final public double distanceToHit3D(double[] pos)
  {
    // works well in endcaps, might not be great in VertexDetecto
    if (Math.abs(pos[2])>100.*k_cm && Math.abs(dir[2])>0.1) setPointOnHelixWithZ(pos[2]);
    else setPointOnHelixWithXY(pos[0],pos[1]);

    return Math.sqrt((pos[0]-pointOnHelix[0])*(pos[0]-pointOnHelix[0])+(pos[1]-pointOnHelix[1])*(pos[1]-pointOnHelix[1])+(pos[2]-pointOnHelix[2])*(pos[2]-pointOnHelix[2]));
  }

  final public double distanceBaseToPoint(double[] p){
      return Math.sqrt(sqr(p[0]-base[0])+sqr(p[1]-base[1]));
  }

    /** added for convenience and to avoid creating more double vectors **/
  final public double distanceBaseToPoint(GarfieldHit h){
      return Math.sqrt(sqr(base[0]-h.getPoint(0))+sqr(base[1]-h.getPoint(1)));
  }

    /**
     *  absolute value of a double
     */
  private final static double myAbs(double ka){return ka>0.? ka : -ka;}

    /**
     *  sinfrac(x, ka) = sin(x*ka) / ka
     */
  final private double sinfrac(double x, double ka){
    // sinfrac = sin(x*ka) / ka
    // sin p = p - 1/6 p^3 + 1/120. p^5 - ...
    // sin(x ka) /ka = x - 1/6 x^3 ka^2
    double kabs=ka>0.? ka : -ka;
    double xx=x*ka;
    double y= 0.;
    if (kabs > 1.E-3){
      y=Math.sin(xx)/ka;
    }
    else if (kabs > 1.E-5){
      y= x - (x*xx*xx)/6. + (x*xx*xx*xx*xx)/120.;
    }
    else if (kabs>1.E-7){
      y=  x - (x*xx*xx)/6.;
    }
    else{
       y=  x ;
    }
    return y;    
  }

    /**
     *  cosfrac(x, ka) = (cos(x*ka)-1) / ka
     */
  final private double cosfrac(double x, double ka){
    // cosfrac = (cos(x*ka)-1) / ka
    // cos p = 1 - 1/2 p^2 + 1/24 p^4
    // (cos(x ka) -1)/ka = -1/2 x^2 ka + 1/24 x^4 ka^3 
    double kabs=ka>0.? ka : -ka;
    double xx=x*ka;
    double y= 0.;
    if (kabs > 1.E-3){
      y=(Math.cos(xx)-1.)/ka;
    }
    else if (kabs > 1.E-5){
      y= -(x*xx)/2. + (x*xx*xx*xx)/24.;
    }
    else{
      y=  -(x*xx)/2.;
    }
    return y;
  }

    /**
     * returns distance to that interval a,b (0 or the distance to the edge)
     * allows for arbitrary ordering (a<b or a>b)
     */
  final private double distanceToInterval(double a, double b, double c)
    {
	if (a<b){
	    if (c<a) return Math.abs(a-c);
	    else if (c>b) return Math.abs(c-b);
	    else return 0.;
	}
	else{
	    if (c<b) return Math.abs(b-c);
	    else if (c>a) return Math.abs(c-a);
	    else return 0.;
	}
    }

    final public double getCenter(int i){
	if (i==0) return base[0]-dir[1]/kappa;
	if (i==1) return base[1]+dir[0]/kappa;
	return 0.;
    }

  //
  // functions that do something
  //

  public void setPointOnHelix(double s) {
    sSave = s;  // store s for savekeeping
    double cfrac=cosfrac(s,kappa);
    double sfrac=sinfrac(s,kappa);
    pointOnHelix[0]=base[0]+dir[1]*cfrac + dir[0]*sfrac;      
    pointOnHelix[1]=base[1]-dir[0]*cfrac + dir[1]*sfrac;
    pointOnHelix[2]=base[2]+dir[2]*s;
  }      
 
  public void setPointOnHelixWithXY(double x, double y) {
    //
    // sinfrac = \vec{Dxy} * \vec{P-B}xy  
      double kabs = kappa>0.? kappa : -kappa;
      if (kabs<1.E-7){
          double s=dir[0]*(x-base[0])+dir[1]*(y-base[1]);
	  setPointOnHelix(s);
      }
      else{
	  double d = Math.sqrt((x-base[0])*(x-base[0])+(y-base[1])*(y-base[1]));
	  if (d<1.E-12){
	      setPointOnHelix(0.);
	      return;
	  }
	  double cx=getCenter(0);
	  double cy=getCenter(1);
	  double l = Math.sqrt((x-cx)*(x-cx)+(y-cy)*(y-cy));
	  double R = 1./kabs;
	  double cosAngle = 0.5*(l/R+R/l-d*d/(l*R));
	  double s=0.;
	  if (cosAngle <=1. && cosAngle >= -1.) s=Math.acos(cosAngle)*R;
	  else if (debugLevel>=4){ 
	      System.out.println("GarfieldHelix problem in setXY"+cosAngle+" "+l+" "+R+" "+d);
	      System.out.println("  center x="+cx+" "+cy);
	  }
	  if (dir[0]*(x-base[0])+dir[1]*(y-base[1])>0.) setPointOnHelix(s);
	  else setPointOnHelix(-s);
      }
  }      
 
  public void setPointOnHelixWithZ(double z) {
    //pointOnHelix[2]=base[2]+dir[2]*s;
    // z = base_z + dir[2] * s  => s = (z - base_z)/dir[2]
    if (dir[2]>1.E-12 || dir[2]<-1.E-12){ 
	sSave = (z-base[2])/dir[2];
    }
    else{
	if (debugLevel>=4) {System.out.println("problem in GarfieldHelix setPointOnHelixWithZ, dir2="+dir[2]);}
	sSave= 10.;
    }
    double cfrac=cosfrac(sSave,kappa);
    double sfrac=sinfrac(sSave,kappa);
    pointOnHelix[0]=base[0]+dir[1]*cfrac + dir[0]*sfrac;      
    pointOnHelix[1]=base[1]-dir[0]*cfrac + dir[1]*sfrac;
    pointOnHelix[2]=z;
  }      
 
  public double dirAtPoint(int i)
  {
    double phi= sSave*kappa;
    if (i==2) return dir[2];
    else if (i==0) return dir[0]*Math.cos(phi)-dir[1]*Math.sin(phi); 
    else if (i==1) return dir[1]*Math.cos(phi)+dir[0]*Math.sin(phi); 
    else {
	System.out.println("GarfieldHelix dirAtPoint bad index i="+i);
	return -999.;
    }
  }
  

  public double distanceToLine2D(GarfieldHit h, double px, double py)
  {
      double x = h.getLength2D();
      double xx = h.x1(0)-h.x0(0);
      double xy = h.x1(1)-h.x0(1);
      double lx = px - h.x0(0);
      double ly = py - h.x0(1);
      double ll = lx*lx+ly*ly;
      double lDotX=lx*xx+ly*xy;
      if (lDotX<0.) return Math.sqrt(ll);
      if (lDotX > x*x) return Math.sqrt((px - h.x1(0))*(px - h.x1(0))+(py - h.x1(1))*(py - h.x1(1)));
      return Math.sqrt(ll-(lDotX/x)*(lDotX/x));
  }

    
  public double distanceToHit2D(GarfieldHit h)
  {
      double kabs = Math.abs(kappa);
      if (h.hasZ() && Math.abs(dir[2])>0.1){ // changed EVT - March 11th, for JonTrackFinder routines
	  setPointOnHelixWithZ(h.getPoint(2));    
	  return distanceToLine2D(h,pointOnHelix[0],pointOnHelix[1]);
      }
      else{
	  double x=h.getPoint(0);
	  double y=h.getPoint(1);
	  setPointOnHelixWithXY(x,y);
	  double eps = distanceToInterval(h.x0(2),h.x1(2),pointOnHelix[2]);
	  //if (debugLevel>=4){
	  //    System.out.println("distanceToHit2D d="+Math.sqrt((pos[0]-pointOnHelix[0])*(pos[0]-pointOnHelix[0])+(pos[1]-pointOnHelix[1])*(pos[1]-pointOnHelix[1])+eps*eps)+" eps="+eps);
	  //    System.out.println("   "+pointOnHelix[0]+" "+pointOnHelix[1]+" "+pointOnHelix[2]);
	  //    System.out.println("   "+pos[0]+" "+pos[1]+" "+pos[2]);
	  //}
          return Math.sqrt(sqr(x-pointOnHelix[0])+sqr(y-pointOnHelix[1]));
      }
  }

}





