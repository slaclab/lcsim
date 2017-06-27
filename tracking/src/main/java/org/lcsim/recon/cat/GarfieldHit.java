package org.lcsim.recon.cat;

import java.util.*;
import org.lcsim.event.*;

/**
 * Simple data format for digitized (aka smeared, taking segmentation into account) 
 * hits, either 2D or 3D, used within the Garfield track finding package for
 * calorimeter assisted tracking. This class will have to be re-written once 
 * proper digitization package is available (or at least  
 * the standard interface for 2-dimensional hits is defined).
 *
 * EVT 03/23/05 added new routines for faster pattern recognition
 * declared trackerhit private
 * decalred x0(0 , x1() "final"
 *
 * @see GarfieldTrackFinder
 *
 * @author  E. von Toerne
 * @author  D. Onoprienko
 * @version $Id: GarfieldHit.java,v 1.4 2011/08/24 18:51:17 jeremy Exp $
 */
final public class GarfieldHit implements TrackerHit {


    // -- Private parts :  -------------------------------------------------------

    private String hitStatus;
    private boolean isendcap=false;
    private boolean is3d;
    private boolean hasZ;
    private int layer;
    private double[] pos; // Position of the hit
    private double[] x0; // 2D hits are a line, this is it's one end
    private double[] x1; // 2D hits are a line, this is it's other end
    private double length2D;  //length of 2D line in xy plane
    private double simpleError;
    private int id;
    private double phi;

    private ArrayList<SimTrackerHit> rawHitList = new ArrayList<SimTrackerHit>(1);

    // -- Constructors :  --------------------------------------------------------

    /**
     * 2D hit constructor.
     */
    GarfieldHit(double[] x0, double[] x1, double error, int layer, int id) {
        this.layer = layer;
        this.id = id;
        this.hitStatus = "new";
        this.is3d = false;
        this.x0 = new double[3]; System.arraycopy(x0, 0, this.x0, 0, 3);
        this.x1 = new double[3]; System.arraycopy(x1, 0, this.x1, 0, 3);
        this.pos = new double[]{0.5*(x0[0]+x1[0]), 0.5*(x0[1]+x1[1]), 0.5*(x0[2]+x1[2])}; // middle of strip
        this.phi = Math.atan2(pos[1],pos[0]);
        this.hasZ = (Math.abs(x0[2]-x1[2])<1.E-2);
        this.simpleError = error;
        this.length2D=Math.sqrt((x0[0]-x1[0])*(x0[0]-x1[0])+(x0[1]-x1[1])*(x0[1]-x1[1]));
    }

    /**
     * Generic 3D point. Any getters requiring access to the original hit
     * cannot be called for an object created with this constructor.
     */
    GarfieldHit(double[] point, double error, int layer, int id) {
        this.layer = layer;
        this.id = id;
        this.hitStatus = "new";
        this.is3d = true;
        this.hasZ=true;
        this.pos = new double[3]; System.arraycopy(point, 0, this.pos, 0, 3);
        this.phi = Math.atan2(pos[1],pos[0]);
        this.x0 = null;
        this.x1 = null;
        this.simpleError = error;
        this.length2D = -1.;
    }

    // -- Setters :  -------------------------------------------------------------

    /**
     * Set status string of the hit.
     */
    public void setStatus(String stat){hitStatus=stat;}
    public void setEndcap(boolean enc){isendcap=enc;}

    /** Add a hit to the list of simulated hits that contributed to this "digitized" hit. */
    public void addRawHit(SimTrackerHit rawHit) {rawHitList.add(rawHit);}

    // -- Getters :  -------------------------------------------------------------

    /**
     * Returns <code>true</code> if all three coordinates of the hit are known.
     */
    public boolean is3D(){return is3d;}

    /**
     * Returns <code>true</code> if Z coordinate of the hit is known.
     */
    public boolean hasZ(){return hasZ;}

    /**
     * Returns <code>true</code> if the hit is in endcap.
     */
    public boolean isEndcap(){return isendcap;}

    public double getTime() {return rawHitList.isEmpty() ? 0. : rawHitList.get(0).getTime();}
    public double[] getCovMatrix() {return new double[]{0.,0.,0.,0.,0.,0.,0.,0.,0.};}
    public int getType() {return 0;}

    public List getRawHits() {return rawHitList;}

    /**
     * Get status string of the hit.
     */
    public String getStatus(){return hitStatus;}

    /**
     * Get simple error assigned to the hit.
     */
    public double getError(){return simpleError;}

    /**
     * Get Garfield layer number: numbers start at the innermost layer of vertex
     * detector and increase to the outer layer of the tracker.
     */

    public int getLayer(){return layer;}

    /**
     * Get Phi angle of the hit.
     */
    public double getPhi(){return phi;}

    /**
     * Get hit position.
     */
    public double[] getPoint(){return new double[]{pos[0],pos[1],pos[2]};}

    /**
     * Get hit position.
     */
    public double[] getPosition(){return new double[]{pos[0],pos[1],pos[2]};}

    /**
     * Get i-th coordinate of the point corresponding to the start of the line defining 2D hit.
     */
    public final double x0(int i){return x0[i];}

    /**
     * Get i-th coordinate of the point corresponding to the end of the line defining 2D hit.
     */
    public final double x1(int i){return x1[i];}

    /**
     * Get length of line in xy plane for the line defining 2D hit.
     */
    public final double getLength2D(){return length2D;}

    /**
     * Get i-th coordinate of the position of the hit.
     */
    double getPoint(int i){return pos[i];}

    /**
     * Get hit's distance from the detector axis.
     */
    public double getRxy(){
        return Math.sqrt(pos[0]*pos[0]+pos[1]*pos[1]);
    }

    public double getEdepError()
    {
        return 0;
    }
    
    public int getQuality()
    {
        return 0;
    }

    /**
     *  absolute value of a double
     */
    private final static double myAbs(double ka){return ka>0.? ka : -ka;}

    /**
     * calculate distance of the 2d hit as projected into the xy plane to a 3d postion
     * works best for a 2D hit that lies completely in the xy plane
     * created 11/05/04 for implementation of 2D endcap hits
     * E. von Toerne
     */
    public final double distanceToLine2D(double px, double py) {
        if (is3d) return -1.;
        double xx = x1[0]-x0[0];
        double xy = x1[1]-x0[1];
        double lx = px - x0[0];
        double ly = py - x0[1];
        double ll = lx*lx+ly*ly;
        double lDotX=lx*xx+ly*xy;
        if (lDotX<0.) return Math.sqrt(ll);
        if (lDotX > length2D*length2D) return Math.sqrt((px - x1[0])*(px - x1[0])+(py - x1[1])*(py - x1[1]));
        return Math.sqrt(ll-(lDotX/length2D)*(lDotX/length2D));
    }

    //public double sigmaFromHit(GarfieldHelix hel){ return distanceToHit(hel)/getError(); }

    private final double sqr(double x){return x*x;}

    public final double distanceToHit(GarfieldHelix hel, boolean useXYZ) {
        if (is3d) {
            if (myAbs(pos[2])>1000. && myAbs(hel.dir(2))>0.1) hel.setPointOnHelixWithZ(pos[2]);
            else hel.setPointOnHelixWithXY(pos[0],pos[1]);
            if (useXYZ) return Math.sqrt(sqr(pos[0]- hel.getPointOnHelix(0))+
                    sqr(pos[1]- hel.getPointOnHelix(1))+
                    sqr(pos[2]- hel.getPointOnHelix(2)));
            else return Math.sqrt(sqr(pos[0]- hel.getPointOnHelix(0))+
                    sqr(pos[1]- hel.getPointOnHelix(1)));

        } else if (hasZ){
            if (myAbs(hel.dir(2))>0.01) hel.setPointOnHelixWithZ(pos[2]);
            else hel.setPointOnHelixWithXY(pos[0],pos[1]);
            return distanceToLine2D(hel.getPointOnHelix(0),hel.getPointOnHelix(1));
        } else{
            hel.setPointOnHelixWithXY(pos[0],pos[1]);
            return Math.sqrt(sqr(pos[0]-hel.getPointOnHelix(0))+
                    sqr(pos[1]-hel.getPointOnHelix(1))+
                    sqr(distanceToInterval(x0[2],x1[2],hel.getPointOnHelix(2))));
        }
    }

    /**
     * returns distance to that interval a,b (0 or the distance to the edge)
     * allows for arbitrary ordering (a<b or a>b)
     */
    final private double distanceToInterval(double a, double b, double c) {
        if (a<b){
            if (c<a) return a-c;
            else if (c>b) return c-b;
            else return 0.;
        } else{
            if (c<b) return b-c;
            else if (c>a) return c-a;
            else return 0.;
        }
    }

    /**
     * Hit reporting
     */
    public void debug(){
        double[] ph = getPosition();
        String endCapString = " barrel";
        if (isEndcap()) endCapString = " EndCap";
        String string3d = " ";
        if (is3D()) string3d=" 3D";
        String stringHasZ = " ";
        if (hasZ()) stringHasZ=" hasZ";
        System.out.println("  hit ID="+getID()+" layer="+getLayer()+" "+
                ph[0]+" "+ph[1]+" "+ph[2]+
                endCapString+string3d+stringHasZ);
    }
    /**
     * The system which contains the hit.
     */
    public int getSystem(){return rawHitList.isEmpty() ? -1 : rawHitList.get(0).getSubdetector().getSystemID();}

    /**
     * Get hit ID.
     */
    public int getID(){return this.id;}

    /**
     * The MCParticle which created the hit. Returns null for the moment
     */
    //public MCParticle getMCParticle() {return null;}

    /**
     * Returns dE/dx energy deposition (forwarded from the first simulated hit in the list).
     */
    public double getdEdx() {return rawHitList.isEmpty() ? 0. : rawHitList.get(0).getdEdx();}
    
    public long getCellID()
    {
        if (true) throw new UnsupportedOperationException("This method is not implemented in this class.");
        return 0;
    }    
}
