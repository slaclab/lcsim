package org.lcsim.recon.tracking.trfbase;
import org.lcsim.recon.tracking.trfbase.Cluster;
import org.lcsim.recon.tracking.trfbase.HitGenerator;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.VTrack;


public class HitGeneratorTest extends HitGenerator {

  private SurfTest _srf;
  private double _min;
  private double _max;
  

  public HitGeneratorTest( SurfTest srf) 
  {
  _srf = srf;
  }
  
  public HitGeneratorTest( SurfTest srf,long seed)
  
  {
  super(seed);
   _srf = srf;
   }
   
  public  Surface surface()
  { 
  return _srf; 
  }
  
  public Cluster newCluster( VTrack trv, int mcid) 
  {
    int npred = (int) flat(1.0,10.0) ;
    return new ClusterTest(_srf,npred);
  }
  
}

