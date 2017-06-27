package org.lcsim.recon.tracking.trfbase;
// SurfTest is a dummy concrete subclass for testing the abstract
// class Surface.
//
// It serves as a template for constructing pure and bound surface
// implementations.  Note however that the methods are chosen to
// simplify testing not to provide behavior characteristic of a real
// surface.  See trfcyl/SurfCylinder for a better example of a Surface
// subclass.
//
// SurfTest has two constructors: one from the x-coordinate and one
// the full SpacePath.
//
// We arbitrarily define the direction to be forward if x > 0.


import org.lcsim.recon.tracking.trfbase.CrossStat;
import org.lcsim.recon.tracking.trfbase.PureStat;
import org.lcsim.recon.tracking.trfbase.Surface;
import org.lcsim.recon.tracking.trfbase.TrackSurfaceDirection;
import org.lcsim.recon.tracking.trfbase.TrackVector;
import org.lcsim.recon.tracking.trfbase.VTrack;
import org.lcsim.recon.tracking.spacegeom.CartesianPath;
import org.lcsim.recon.tracking.spacegeom.SpacePath;
import org.lcsim.recon.tracking.spacegeom.SpacePoint;

// Pure surface

public class SurfTest extends Surface
{
    
    // attributes
    
    // Here the surface position and orientation are assumed to be
    // characterized by a single number.  A more complicated surface
    // might use more parameters or might define a space vector.
    protected double _x;
    
    // SpacePath for testing SurfDCA.
    protected SpacePath _spth;
    
    // methods
    
    public String toString()
    {
        return "Test surface with parameter " + _x + ".";
        
    }
    
    // equality comparing two pure surfaces
    protected boolean safePureEqual(Surface srf)
    { return _x == ((SurfTest) srf)._x;
    }
    
    // ordering surfaces
    protected boolean safePureLessThan(Surface srf)
    { return _x < ((SurfTest) srf)._x;
    }
    
    // static methods
    
    // Return the type name.
    public static String typeName()
    { return "SurfTest";
    }
    
    // Return the type.
    public static String staticType()
    { return typeName();
    }
    
    // methods
    
    // constructor
    public SurfTest(double x)
    {
        _x = x;
        _spth = new CartesianPath(_x,0,0,0,0,0);
    }
    
    // constructor from a space path
    public SurfTest(SpacePath spth)
    {
        _x = spth.x();
        _spth = new SpacePath(spth);
    }
    
    // copy constructor
    public SurfTest(SurfTest st)
    {
        _x = st._x;
        _spth = new SpacePath(st._spth);
    }
    
    // Return the type.
    public String type()
    { return staticType();
    }
    
    // Return the direction.
    // The direction is defined if x > 0 and set forward.
    // For x < 0 it is left undefined so the direction can
    // be specified in VTrack.
    public TrackSurfaceDirection direction( TrackVector vec)
    {
        if ( _x > 0.0 ) return TrackSurfaceDirection.TSD_FORWARD;
        else return TrackSurfaceDirection.TSD_UNDEFINED;
    }
    
    // return the pure type
    public String pureType()
    { return staticType();
    }
    
    // clone the pure surface
    public Surface newPureSurface( )
    { return new SurfTest(_spth);
    };
    
    // Return the parameter.
    public double parameter(int ipar)
    {
        if( ipar != 0 ) throw new IllegalArgumentException("Wrong Parameter!");
        return _x;
    }
    
    // Return the space vector specifying the position and orientation
    // of the surface.
    public SpacePath get_space_vector()
    { return new CartesianPath(_x, 0.0, 0.0, 1.0, 0.0, 0.0);
    }
    
    // Return the crossing status for a track without error.
    public CrossStat pureStatus( VTrack trv )
    { return new CrossStat(PureStat.AT);
    }
    
    // Return the difference between two track vectors.
    public TrackVector vecDiff( TrackVector vec1,
            TrackVector vec2 )
    { return vec1.minus(vec2);
    }
    
    // Return the space point for a track vector.
    public SpacePoint spacePoint( TrackVector vec )
    { return new SpacePoint();
    }
    
    // Return the space vector for a track vector.
    public SpacePath spacePath( TrackVector vec,
            TrackSurfaceDirection dir)
    { return _spth;
    }
    
}
