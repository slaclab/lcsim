package org.lcsim.recon.tracking.trfbase;

import org.lcsim.recon.tracking.trfbase.PropDir;
import org.lcsim.recon.tracking.trfbase.PropDirected;
import org.lcsim.recon.tracking.trfbase.PropStat;
import org.lcsim.recon.tracking.trfbase.Propagator;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackDerivative;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;

// Concrete subclass for testing PropDirected.

public class PropDirectedTest extends  PropDirected
{
    
    // static methods
    
    // Return the type name.
    public static String typeName()
    { return "PropDirectedTest"; }
    
    // Return the type.
    public static String get_static_type()
    { return typeName(); }
    
    // methods
    
    // implementation of the propagation
    private PropStat
            myprop(VTrack trv,  Surface srf,  PropDir dir)
    {
        trv.setSurface( srf.newPureSurface() );
        PropStat pst = new PropStat();
        TrackVector vec = trv.vector();
        if ( dir.equals(PropDir.BACKWARD) )
        {
            vec.set(1, vec.get(1)-1.0);
            pst.setForward();
        }
        else
        {
            vec.set(1, vec.get(1)+1.0);
            pst.setBackward();
        }
        trv.setVector(vec);
        return pst;
    }
    
    // methods
    
    // Return the type.
    public String get_type()
    { return get_static_type(); };
    
    // clone
    public Propagator newPropagator()
    { return new PropDirectedTest(); };
    
    // propagate a track without error
    public PropStat vecDirProp(VTrack trv, Surface srf,
            PropDir dir, TrackDerivative deriv)
    {
        if ( deriv != null )
        {
            
            deriv.set(0,0 , 1.1);
            deriv.set(1,1 , 1.2);
            deriv.set(2,2 , 1.3);
            deriv.set(3,3 , 1.4);
            deriv.set(4,4 , 1.5);
        }
        return myprop(trv,srf,dir);
    }
    
    
    public String toString()
    {
        return  "Test propagator" ;
    }
}
