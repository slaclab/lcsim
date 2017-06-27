package org.lcsim.recon.cat;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.BasicHepLorentzVector;
import org.lcsim.recon.cat.GarfieldTrack;
import java.util.*;
import java.lang.Math;
import org.lcsim.recon.cat.util.Const;
/**
 * Constrains two tracks to a common vertex. 
 * Starts with interesection in XY and constrains the z-info. 
 * Intended for tracks with insufficient z-measurement (barrel tracks without VXD)
 * <p>
 * It changes dir[2] of both track until the deltaZ 
 * at intersection point is Zero
 * This is done by minimizing 
 * Chi^2 = Chi^2(track1) + Chi^2(track1) + 10000 * (z1-z2)^2
 *
 * @author  E. von Toerne
 * @version $Id: SimpleConstrainedFit.java,v 1.1 2007/04/06 21:48:14 onoprien Exp $
 */
final public class SimpleConstrainedFit 
{
    private int nIter;
    private double chi2;
    private double bField=5.;  // please note that the momentum is only used
                               // for the vertex constrined, 
                               // a different bField does not affect the fit
    private int debugLevel;
    public GarfieldTrack track1;
    public GarfieldTrack track2;
    private double[] vtx2D;
    private double rXY;
    double[] bas1;
    double[] bas2;
    double[] dir1;
    double[] dir2;

    private BasicHepLorentzVector pp1;
    private BasicHepLorentzVector pp2;
    private BasicHepLorentzVector ppsum;

    private boolean usePrimaryVertexConstraint=true;

    public SimpleConstrainedFit(){
      nIter = 10;
      debugLevel =0;
      chi2=0.;
      debugLevel=0;
      vtx2D = new double[]{0.,0.};
      bas1 = new double[]{0.,0.,0.};
      bas2 = new double[]{0.,0.,0.};
      dir1 = new double[]{0.,0.,0.};
      dir2 = new double[]{0.,0.,0.};
      track1 = new GarfieldTrack();
      track2 = new GarfieldTrack();
      pp1=new BasicHepLorentzVector();
      pp2=new BasicHepLorentzVector();
      ppsum=new BasicHepLorentzVector();
      System.out.println("SimpleConstrainedFit constructor");
    }
    public void setDebugLevel(int i){ debugLevel = i;}
    public void setPrimaryVertexConstraint(boolean val){ usePrimaryVertexConstraint = val;}
    public void setNIter(int i){ nIter = i;}

    private void calculateChi2(){
      track1.hel.setPointOnHelixWithXY(vtx2D[0],vtx2D[1]);
      track2.hel.setPointOnHelixWithXY(vtx2D[0],vtx2D[1]);
      double z1= track1.hel.getPointOnHelix(2); 
      double z2= track2.hel.getPointOnHelix(2); 
      track1.calculateChi2();
      track2.calculateChi2();
      chi2 = track1.getChi2()+track2.getChi2()+(z1-z2)*(z1-z2)*1.E4;
      if (usePrimaryVertexConstraint &&
	  Math.abs(track1.getPara("kappa")) > 1.E-10 && 
	  Math.abs(track2.getPara("kappa")) > 1.E-10 ){
	  double dZ= 0.5*(z1+z2);
	  double tanVtx = dZ / rXY;
	  getTrack4MomentumAtRxy(track1, vtx2D[0],vtx2D[1], 0.139, pp1);
	  getTrack4MomentumAtRxy(track2, vtx2D[0],vtx2D[1], 0.139, pp2);
	  addLorentzVector(pp1,pp2,ppsum);
	  double tanK0s= ppsum.v3().z()/Math.sqrt(ppsum.v3().x()*ppsum.v3().x()+ppsum.v3().y()*ppsum.v3().y());
	  chi2=chi2+(tanVtx-tanK0s)*(tanVtx-tanK0s)*1.E10;
	  //System.out.println("SimpleC "+tanVtx+" "+tanK0s+" chi2="+chi2+" z1="+z1+" z2="+z2);
      }

    }

  private void getTrack4MomentumAtRxy(GarfieldTrack g, double xpos, double ypos, double mass, BasicHepLorentzVector pp) {
    double pt= g.getPt(bField);
    (g.hel).setPointOnHelixWithXY(xpos,ypos);
    double px    = pt*(g.hel).dirAtPoint(0);
    double py    = pt*(g.hel).dirAtPoint(1);
    double pz    = pt*g.getPara("lambda");
    double en    = Math.sqrt(mass*mass+px*px+py*py+pz*pz);
    pp.setV3(en, px, py, pz);
  }

    private void addLorentzVector(BasicHepLorentzVector a, BasicHepLorentzVector b , BasicHepLorentzVector sum){
    sum.setV3(component(a,0)+component(b,0),
    component(a,1)+component(b,1),
    component(a,2)+component(b,2),
    component(a,3)+component(b,3));
  }
    
    private double component(BasicHepLorentzVector a, int i) 
    {
	if (i==0) return a.t();
	else return (a.v3().v())[i-1];
    }

 
    /**
     * Constrains two tracks, t1,t2, in z to a common vertex. 
     * Fitted tracks are stored in this.track1 and this.track2.
     *
     * @param v  v[0]=x coordinate of vertex, v[1]=y coordinate
     * @param t1 input track1
     * @param t2 input track2
     */
    public boolean fitTwoTracksToVertexXY(double[] v, GarfieldTrack t1, GarfieldTrack t2){
      vtx2D[0]=v[0];
      vtx2D[1]=v[1];
      track1= new GarfieldTrack(t1);
      //track1.setHasZ(false); // added Nov 2006, EVT
      track1.calculateChi2();
      track1.setDebugLevel(debugLevel);
      track1.hel.setPointOnHelixWithXY(track1.getMipStubBase(0),track1.getMipStubBase(1));
      track1.hel.setDir(track1.hel.dirAtPoint(0),track1.hel.dirAtPoint(1),track1.hel.dirAtPoint(2)); 
      track1.hel.setBase(track1.hel.getPointOnHelix(0),track1.hel.getPointOnHelix(1),
			 track1.hel.getPointOnHelix(2));
      track2= new GarfieldTrack(t2);
      //track2.setHasZ(false); // added Nov 2006, EVT
      track2.setDebugLevel(debugLevel);
      track2.hel.setPointOnHelixWithXY(track2.getMipStubBase(0),track2.getMipStubBase(1));
      track2.hel.setDir(track2.hel.dirAtPoint(0),track2.hel.dirAtPoint(1),track2.hel.dirAtPoint(2)); 
      track2.hel.setBase(track2.hel.getPointOnHelix(0),track2.hel.getPointOnHelix(1),
			 track2.hel.getPointOnHelix(2));

      rXY=Math.sqrt(v[0]*v[0]+v[1]*v[1]);
      
      calculateChi2();
      double chi2Diff = track1.getChi2()+track2.getChi2()-t1.getChi2()+t2.getChi2();
      if (debugLevel>=2){
	  System.out.println("SimpleConstrainedFit FULLChi2Fit before"+chi2+" chi2Diff="+
	   chi2Diff);
      }
      if (chi2Diff>1.E5){
	  if (debugLevel>=1) System.out.println("SimpleConstrainedFit refitting track");
	  track1.fullChi2Fit(0.1,10);
	  track2.fullChi2Fit(0.1,10);
      }

      track1.hel.setPointOnHelixWithXY(v[0],v[1]);
      track2.hel.setPointOnHelixWithXY(v[0],v[1]);
      double z1= track1.hel.getPointOnHelix(2); 
      double z2= track2.hel.getPointOnHelix(2); 
      double zMin = Math.min(z1,z2);
      double zMax = Math.max(z1,z2);
      double z=zMin;
      double zStep=(Math.abs(zMax-zMin)/20.+1.*Const.mm); // this is a bug fix EVT, Nov 2006
      boolean done =false;
      double oldChi2=chi2;
      int oldLevel =debugLevel;
      this.debugLevel=0;
      int n=0;
      int nRounds = 0;
      while (nRounds < nIter && chi2 > 3.){	
	done=false;
	while (!done){
	  chi2FitIteration(zStep/rXY);
	  n++;
	  if (oldChi2<=chi2) done =true;
	  oldChi2=chi2;
	  if (n>4000){
	    this.debugLevel=oldLevel;
	    if (debugLevel>=2) System.out.println("GarfieldTrack FullChi2Fit runaway chi2="
						  +chi2+" zstep="+zStep);
	    this.track1.setTrackParameters();
	    this.track2.setTrackParameters();
	    if (chi2>1.E5) return false;
	    else return true;
	  }
	}
	zStep = zStep/4.;
	//System.out.println(" chi2  "+chi2+" n="+n+" zstep="+zStep+" nRounds"+nRounds);
	nRounds++;
      }
      this.debugLevel=oldLevel;
      if (debugLevel>=2) System.out.println("++FULLChi2Fit after"+chi2+" n="+n+" zstep="+zStep+" nRounds"+nRounds);
      this.track1.setTrackParameters();
      this.track2.setTrackParameters();
      if (debugLevel>=4) {
	  System.out.println("SimpleConstrainedFit first track");
	  t1.debug();
	  System.out.println("first track constrained");
	  this.track1.debug();
	  System.out.println("second track");
	  t2.debug();
	  System.out.println("second track constrained");
	  this.track2.debug();
      }
      if (chi2>1.E8) return false;
      return true;
    }    
    
    private void chi2FitIteration(double dirStep){
      // fast variation of dir 
      double deltaD = dirStep;
      calculateChi2();
      int nItera = 2;
      double oldChi2=chi2;
      dir1[0]=track1.hel.dir(0);
      dir1[1]=track1.hel.dir(1);
      dir1[2]=track1.hel.dir(2);
      dir2[0]=track2.hel.dir(0);
      dir2[1]=track2.hel.dir(1);
      dir2[2]=track2.hel.dir(2);
      
      bas1[0]=track1.hel.base(0);
      bas1[1]=track1.hel.base(1);
      bas1[2]=track1.hel.base(2);
      bas2[0]=track2.hel.base(0);
      bas2[1]=track2.hel.base(1);
      bas2[2]=track2.hel.base(2);
      
      double bestDirZtrack1=track1.hel.dir(2);
      double bestDirZtrack2=track2.hel.dir(2);
      double minChi2=chi2;
      
      for (int i1=-nItera; i1<=nItera;i1++){
	track1.hel.setDir(dir1[0],dir1[1],dir1[2]+deltaD*i1);
	for (int i2=-nItera; i2<=nItera;i2++){
	  track2.hel.setDir(dir2[0],dir2[1],dir2[2]+deltaD*i2);
	  calculateChi2();
	  if (chi2<minChi2){ 		  
	    bestDirZtrack1=track1.hel.dir(2);
	    bestDirZtrack2=track2.hel.dir(2);
	    minChi2=chi2;		  
	  }
	}
      }
      track1.hel.setDir(dir1[0],dir1[1],bestDirZtrack1);
      track2.hel.setDir(dir2[0],dir2[1],bestDirZtrack2);
      calculateChi2();
    }
}
