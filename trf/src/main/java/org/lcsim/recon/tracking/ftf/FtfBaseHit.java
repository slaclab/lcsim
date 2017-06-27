package org.lcsim.recon.tracking.ftf;

public class FtfBaseHit
{
    public   void         print( )
    {
        print(11);
    }
    public   void         print( int point_level )
    {
        //--
        //--     print hit info
        //
        
        if ( point_level > 9 )
            System.out.println( "hit Row    x      y     z" ) ;
        
        if ( point_level > 10 )
            System.out.println( id +" "+ row+" "+ x+" "+ y+ " "+ z ) ;
    }
    
    public  int          id ;
    public  short        row   ;         // Row #
    public  FtfTrack track;         // track to which the pnt was assigned
    public  FtfBaseHit nextTrackHit  ;// Next track hit
    public  FtfBaseHit nextMcTrackHit;// Next MC track hit
    public  float        xyChi2 ;        // Chi2 in xy
    public  float        szChi2 ;        // Chi2 in sz
    public   float       x    ;
    public  float        y    ;
    public  float        z    ;
    public  float        dx   ;          // error on the x coordinate
    public  float        dy   ;          // error on the y coordinate
    public  float        dz   ;          // error on the z coordinate
    public  float        q    ;          // total charge assigned to this point
    public  float        wxy  ;          // x-y weight x-y
    public  float        wz   ;          // z weight on z
    public  float        s    ;          // Track trajectory
    
}