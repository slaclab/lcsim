package org.lcsim.recon.tracking.ftf;


public class FtfHit extends FtfBaseHit
{
    
    public    void         printLinks( )
    {
        print(11);
    }
    public    void         printLinks( int point_level )
    {
        
        //--
        //--     print hit info
        //
        
        if ( point_level > 9 )
            System.out.println( "hit ir iphi ieta   phi   eta      x      y     z\n" ) ;
        
        if ( point_level%10 > 0 )
            System.out.printf( "%3d %3d %3d  %3d  %6.2f %5.2f  %6.2f %6.2f %6.2f \n",
                    (int)id, (int)row, (int)phiIndex, (int)etaIndex,
                    phi*FtfGeneral.toDeg, eta, x, y, z ) ;
        
        long vhit ;
        if ( nextVolumeHit != null ) vhit = ((FtfHit) nextVolumeHit).id ;
        else vhit = -1 ;
        long rhit ;
        if ( nextRowHit != null ) rhit = ((FtfHit)nextRowHit).id ;
        else rhit = -1 ;
        long thit ;
        if ( nextTrackHit != null ) thit = ((FtfBaseHit)nextTrackHit).id ;
        else thit = -1 ;
        long mhit ;
        if ( nextMcTrackHit != null ) mhit = ((FtfBaseHit)nextMcTrackHit).id ;
        else mhit = -1 ;
        
        if ( point_level%10 > 1 )
            System.out.printf( "pointers:vol,row,tr,mtr,mirror (%4d,%4d,%4d,%4d)\n ",
                    vhit, rhit, thit, mhit ) ;
        int tid ;
        if ( track != null ) tid = track.id ;
        else tid = -1 ;
        if ( point_level%10 > 2 )
            System.out.printf( "\n Tracks  :reco            (%4d) ", tid ) ;
        
    }
    
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //    This function assigns this hit and all its
    //    mirror hits to a given track
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    
    public    void         setStatus( FtfTrack this_track )
    {
        //
        //   Set the flag for this hit
        //
        //   track = (FtfBaseTrack *)this_track ;
        
    }
    
    public    long         id ;
    public   short        phiIndex ;        // Phi index
    public   short        etaIndex ;        // Eta index
    public    short        flags    ;        // various flags
    public     short        sector   ;        // various flags
    //
    public     FtfHit        nextVolumeHit ;  // Next volume hit
    public     FtfHit        nextRowHit    ;  // Next row hit
    public     float        r    ;            // radius
    public     float        phi  ;            // azimuthal angle
    public     float        dphi ;            // Error in phi
    public     float        eta  ;            // hit pseudorapidity
    public     float        xp   ;            // x conformal coordinate
    public     float        yp   ;            // y conformal coordinate
    public     short        buffer1 ;          //
    public     short        buffer2 ;
    public      short hardwareId ;
}

