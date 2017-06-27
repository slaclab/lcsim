package org.lcsim.recon.cat;

//import hep.physics.*;
import java.util.*;

/**
 * Simple circle from 2D point calculation for helix estimates.
 *
 * @author  E. von Toerne
 * @version $Id: CircleFromPoints.java,v 1.1 2007/04/06 21:48:14 onoprien Exp $
 */
final public class CircleFromPoints {
  public double[] a,b,c;
  private double[] ab,ac,cb,caperp;
  private double sin_a, sin_ab;
  public double[] center;
  public double kappa;
  public double[] dirAtC;
  // constructors
  
  /**
   * empty constructor
   */
  CircleFromPoints() {
    a=new double[]{0.,0.};
    b=new double[]{0.,0.};
    c=new double[]{0.,0.};
    ab=new double[]{0.,0.};
    ac=new double[]{0.,0.};
    cb=new double[]{0.,0.};
    caperp=new double[]{0.,0.};
    center=new double[]{0.,0.};
    dirAtC=new double[]{0.,0.};
  }
  
  public void calculate(double ax, double ay, double bx, double by, double cx, double cy) {
    int i;
    a[0]=ax;
    a[1]=ay;
    b[0]=bx;
    b[1]=by;
    c[0]=cx;
    c[1]=cy;
    for (i=0;i<2;i++){
      ab[i]=b[i]-a[i];
      ac[i]=c[i]-a[i];
      cb[i]=c[i]-b[i];
    }
    
    double lencb=leng(cb);
    double lenac=leng(ac);
    if (lenac<1.E-5){
      kappa = 0.;
      System.out.println("Problem in CircleFromPoints"+lencb);
      return;
    }
    if (lencb<1.E-5){
      kappa = 0.;
      dirAtC[0]=(c[0]-a[0])/lenac;
      dirAtC[1]=(c[1]-a[1])/lenac;
      //System.out.println("Problem in CircleFromPoints"+lencb);
      return;
    }
    
    caperp[0]=ac[1]/lenac;
    caperp[1]=-ac[0]/lenac;
    
    double sin_a=Math.abs(cdot(ab,cb)/leng(ab)/lencb);
    double sin_b=Math.abs(cdot(ac,cb)/leng(ac)/lencb);
    double cos_a=1.-sqr(sin_a);
    cos_a = cos_a >0.? Math.sqrt(cos_a) : 0.;
    cos_a = cos_a <1.? cos_a : 1.;
    double cos_b=1.-sqr(sin_b);
    cos_b = cos_b >0.? Math.sqrt(cos_b) : 0.;
    cos_b = cos_b<1.? cos_b : 1.;
    double sin_ab=sin_a * cos_b + sin_b * cos_a;
    double theSign=1.;
    if (cdot(caperp,cb)<0.) theSign = -1.;
    
    for (i=0;i<2;i++) center[i]=0.5*(a[i]+c[i])+theSign*0.5*sin_a/sin_ab*lencb*caperp[i];
    kappa=1./Math.sqrt(sqr(center[0]-a[0])+sqr(center[1]-a[1]));
    dirAtC[0]=(center[1]-c[1])*kappa;
    dirAtC[1]=-(center[0]-c[0])*kappa;
    if (dirAtC[0]*(c[0]-b[0])+dirAtC[1]*(c[1]-b[1])<0.){
      dirAtC[0]=-dirAtC[0];
      dirAtC[1]=-dirAtC[1];
    } else { kappa=-kappa;}
    
  }
  
  private double sqr(double x){return x*x;}
  private double cdot(double[] a1, double[] b1){return a1[0]*b1[0]+a1[1]*b1[1];}
  private double leng(double[] a1){return Math.sqrt(a1[0]*a1[0]+a1[1]*a1[1]);}
  public double getCenter(int i){return center[i];}
  public double getKappa(){return kappa;}
  public double getDirAtC(int i){return dirAtC[i];}
  public void debug(){
    double[] disa=new double[]{center[0]-a[0],center[1]-a[1]};
    double[] disb=new double[]{center[0]-b[0],center[1]-b[1]};
    double[] disc=new double[]{center[0]-c[0],center[1]-c[1]};
    System.out.println("a="+a[0]+" "+a[1]);
    System.out.println("b="+b[0]+" "+b[1]);
    System.out.println("c="+c[0]+" "+c[1]);
    System.out.println("center="+center[0]+" "+center[1]);
    System.out.println("kappa="+kappa+" Radius="+1./kappa);
    System.out.println("distance point A to center="+leng(disa));
    System.out.println("distance point B to center="+leng(disb));
    System.out.println("distance point C to center="+leng(disc));
  }
}
