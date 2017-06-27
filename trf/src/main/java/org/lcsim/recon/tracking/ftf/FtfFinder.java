package org.lcsim.recon.tracking.ftf;
public class FtfFinder
{
    private   FtfTrack currentTrack;
    
    
    public   FtfFinder( )
    {
    }
    
    //     Calculates deposited Energy
    public   void    dEdx                    ( )
    {
        for ( int i = 0 ; i<nTracks ; i++ )
        {
            track[i].dEdx( ) ;
        }
    }
    
    // Recontruct primary tracks
    public   int     getTracks               ( )
    {
        //
        //     Set conformal coordinates if we are working with primaries
        //
        int nHitsSegment   = (short)para.nHitsForSegment;
        if ( para.primaries!=0 )
        {
            setConformalCoordinates( ) ;
            para.minHitsForFit = 1 ;
            para.nHitsForSegment = Math.max(2,nHitsSegment);
        }
        else
        {
            para.minHitsForFit = 2 ;
            para.nHitsForSegment = Math.max(3,nHitsSegment);
        }
        //
        //               Loop over rows
        //
        for ( int ir = para.nRowsPlusOne - 1 ; ir>=para.minHitsPerTrack ; ir--)
        {
            //
            //           Loop over hits in this particular row
            //
            //cng      if ( rowC[ir].first &&  (((FtfHit)rowC[ir].first).row) < para.rowEnd ) break ;
            //    if ( (((FtfHit *)rowC[ir].first).row) < para.rowEnd ) break ;
            for ( FtfHit firstHit = (FtfHit)rowC[ir].first ;
            firstHit != null ;
            firstHit = (FtfHit )(firstHit.nextRowHit) )
            {
                //
                //     Check hit was not used before
                //
                if ( firstHit.track != null  ) continue ;
                //
                //     One more track
                //
                nTracks++ ;
                //
                //
                if ( nTracks > maxTracks )
                {
                    System.out.println("\n FtfFinder::getTracks: Max nr tracks reached !") ;
                    nTracks = maxTracks  ;
                    return 1 ;
                }
                //
                //     Initialize variables before going into track hit loop
                //
                FtfTrack thisTrack = track[nTracks-1];
                thisTrack.para     = para ;
                thisTrack.id       = nTracks ;
                thisTrack.firstHit = thisTrack.lastHit = firstHit ;
                thisTrack.innerMostRow = thisTrack.outerMostRow = firstHit.row ;
                thisTrack.xRefHit  = firstHit.x ;
                thisTrack.yRefHit  = firstHit.y ;
                thisTrack.xLastHit = firstHit.x ;
                thisTrack.yLastHit = firstHit.y ;
                // TRDEBUG
                //cng         thisTrack.debugNew ( ) ;
                //
                //
                //              Set fit parameters to zero
                //
                thisTrack.reset( ) ;
                //
                //      Go into hit looking loop
                //
                if ( thisTrack.buildTrack( firstHit, volumeC ) !=0 )
                {
                    //
                    //    Merge Tracks if requested
                    //
                    if ( para.primaries !=0 &&
                            para.mergePrimaries == 1 &&
                            para.fillTracks &&
                            thisTrack.mergePrimary( trackC )==0  )
                    {
                        nTracks-- ;
                        thisTrack.deleteCandidate( ) ;
                    }
                }
                else
                {
                    //
                    //      If track was not built delete candidate
                    //
                    thisTrack.deleteCandidate( ) ;
                    nTracks-- ;
                }
                //
                //       End loop over hits inside row
                //
            }
            //       End loop over rows
            //
            //    Check time
            //
            if ( CpuTime() - initialCpuTime > para.maxTime )
            {
                System.out.println( "FtfFinder::getTracks: tracker time out after \n"+ (CpuTime() - initialCpuTime) ) ;
                break ;
            }
        }
        //
        para.nHitsForSegment = nHitsSegment ;
        //
        return 0 ;
    }
    public   void    mergePrimaryTracks      ( )
    {
    }
    
    private static void log(String s)
    {
        System.out.println(s);
    }
    //      Steers the tracking
    public   double  process( )
    {
        //-----------------------------------------------------------------
        //        Make sure there is something to work with
        //------------------------------------------------------------------
        if ( nHits <= 0 )
        {
            if ( para.infoLevel > 2 )
                log( "fft: Hit structure is empty \n " ) ;
            return 1 ;
        }
        //
        initialCpuTime  = CpuTime( );
        initialRealTime = RealTime( );
        //
        //        General initialization
        //
        if ( para.init == 0 )
        {
            if ( reset( )!=0 ) return 1 ;
        }
        //
        //      Event reset and set pointers
        //
        if ( (para.eventReset!=0)  && (setPointers( )!=0) ) return 1 ;
        //
        //      Build primary tracks now
        //
        short i ;
        para.primaries = 1 ;
        for ( i = 0 ; i < para.nPrimaryPasses ; i++ )
            if ( getTracks( )!=0 ) break ;
        //
        //      Look for secondaries
        //
        para.primaries = 0 ;
        for ( i = 0 ; i < para.nSecondaryPasses ; i++ )
            if ( getTracks( )!=0 ) break ;
        
        //   if ( para.dEdx ) dEdx ( ) ;
        
        cpuTime  = CpuTime  ( ) - initialCpuTime  ;
        realTime = RealTime( ) - initialRealTime ;
        //DEBUG
        if ( para.infoLevel > 0 )
            log(  "FtfFinder::process: cpu "+cpuTime+" real "+realTime+" \n" ) ;
        //
        return cpuTime ;
    }
    
    
    public   int     reset                   ( )
    {
        return 0;
    }
    public   int     setConformalCoordinates( )
    {
        return 0;
    }
    public   int     setPointers             ( )
    {
        return 0;
    }
    public   double  CpuTime                 ( )
    {
        return 0.;
    }
    public   double  RealTime                ( )
    {
        return 0.;
    }
    //
    //
    public   int           nHits      ;
    public   int           nHitsOutOfRange ;
    public   int           maxHits    ;
    public   FtfHit        hit       ;
    public   int           nTracks    ;
    public   FtfTrack[]      track     ;
    public   FtfPara       para       ;
    public   int           maxTracks  ;
    public  int           nMcTracks  ;
    public   FtfMcTrack    mcTrack    ;
    public   FtfContainer  volumeC ;
    public   FtfContainer[]  rowC    ;
    public   FtfContainer  trackC  ;
    public  double        initialCpuTime ;
    public  double        initialRealTime ;
    public  double        cpuTime ;
    public   double        realTime ;
    
    
}