package org.lcsim.recon.tracking.trfbase;
//**********************************************************************
import org.lcsim.recon.tracking.trfbase.SurfTest;
import org.lcsim.recon.tracking.trfbase.BoundedStat;
import org.lcsim.recon.tracking.trfbase.CrossStat;
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.VTrack;


// Bounded surface

public class BSurfTest extends SurfTest {

// attributes

  // parameters specifying the bounds
  private double _bparam;

 // static methods

  // Return the type name.
  public static String typeName() { return "BSurfTest"; }
 
  // Return the type.
  public static String staticType() { return typeName(); }

  // methods

  // output stream
  public String toString()
  {
  return "BSurfTest "+super.toString()+" bound= "+_bparam +"\n";
  }

  // Equality comparing two bound surfaces of this type
  protected boolean safeBoundEqual( Surface srf)  {
    if ( ! pureEqual(srf) ) return false;
    return _bparam == ((BSurfTest) srf)._bparam;
  }

 // methods

  // Constructor
  public BSurfTest(double x, double bparam) 
  {
  super(x);
  _bparam= bparam;
  }
  
  // Return the type.
  public String type()  { return staticType(); }
 
  // return the full crossing status
  public CrossStat status( VTrack trv) 
    { return new CrossStat(BoundedStat.IN_BOUNDS); };
  public CrossStat status( ETrack tre) 
    { return new CrossStat(BoundedStat.BOTH_BOUNDS); };

  // clone
  public Surface newSurface()  {
    return new BSurfTest(parameter(0),_bparam);
  }

}






















 