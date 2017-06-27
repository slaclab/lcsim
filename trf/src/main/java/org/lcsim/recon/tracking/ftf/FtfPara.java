package org.lcsim.recon.tracking.ftf;
//
//           fft control parameters
//
public class FtfPara
{
    public     FtfPara( )
    { setDefaults() ; }
    public    void      setDefaults( )
    {
        /*  Define cuts - this should be obsolete */
        float toDeg = FtfGeneral.toDeg;
        modRow          = 1    ;
        infoLevel       = 0 ;
        hitChi2Cut      = 500.F  ;
        goodHitChi2     = 100.F ;
        trackChi2Cut    = 250.F ;
        maxChi2Primary  = 0.f ;
        segmentRowSearchRange = 1 ;
        trackRowSearchRange   = 3 ;
        dEdx              = 0     ;
        dEdxNTruncate     = 20    ;
        dphi              = 0.10F * modRow ;
        deta              = 0.10F * modRow ;
        dphiMerge         = 0.02F  ;
        detaMerge         = 0.02F  ;
        distanceMerge     = 2.f ;
        etaMin            = -2.5F  ;
        etaMinTrack       = -2.2F  ;
        etaMax            =  2.5F  ;
        etaMaxTrack       =  2.2F  ;
        eventReset        =  1     ;
        getErrors         =  false     ;
        fillTracks        =  true    ;
        primaries         = 1;
        ghostFlag         =  0     ;
        goBackwards       =  false     ;
        goodDistance      =  1.F * modRow ;
        init              =  0 ;
        mergePrimaries    =  1    ;
        parameterLocation =  1    ;
        phiMin            =  (float)(-0.000001/toDeg)  ;
        phiMinTrack       =  (float)(-0.000001/toDeg)  ;
        phiMax            = (float)(360.2/toDeg)  ;
        phiMaxTrack       = (float)(360.2/toDeg)  ;
        maxDistanceSegment = 100.F * modRow ;
        minHitsPerTrack   = 5      ;
        nHitsForSegment   = 2      ;
        nEta              = 60     ;
        nEtaTrack         = 60     ;
        nPhi              = 20     ;
        nPhiTrack         = 60     ;
        nPrimaryPasses    = 1      ;
        nSecondaryPasses  = 0      ;
        vertexConstrainedFit = false ;
        rowInnerMost      = 1      ;
        rowOuterMost      = 45     ;
        rowStart          = 45     ;
        rowEnd            =  1     ;
        segmentMaxAngle   = 10.F/toDeg ;
        szFitFlag         = 1      ;
        xyErrorScale      = 1.0F   ;
        szErrorScale      = 1.0F   ;
        bField            = 0.5F   ;
        phiShift          = 0.0f    ;
        
        ptMinHelixFit     = 0.F  ;
        rVertex           = 0.F    ;
        xVertex           = 0.F    ;
        yVertex           = 0.F    ;
        zVertex           = 0.F    ;
        dxVertex          = 0.005F ;
        dyVertex          = 0.005F ;
        phiVertex         = 0.F    ;
        maxTime           = 1.e18f ; // by default tracker can run as long as the age of the Universe
        
    }
    public    void      read  ( String inputFile )
    {
    }
    public    void      write( String outputFile )
    {
    }
    
    int       infoLevel;       // Level of information printed about progress
    int       segmentRowSearchRange;       // Row search range for segments
    int       trackRowSearchRange;         // Row search range for tracks
    int       dEdx  ;          // dEdx switch
    int       dEdxNTruncate ;  // # points to truncate in dEdx
    int       eventReset   ;   // Flag to reset event in fft
    boolean       getErrors    ;   // Flag to switch error calculation
    boolean       fillTracks   ;   // Flag to switch FtfTrack class filling
    int       ghostFlag    ;   // =1 when there are ghost hits
    boolean       goBackwards  ;   // Flag to go backwards at the end of track reco
    int       init;            // Control initialization
    int       mergePrimaries ; // Switch to control primary merging
    int       minHitsPerTrack; // Minimum # hits per track
    int       modRow;          // Modulo pad row number to use
    int       nHitsForSegment; // # hits in initial segments
    int       minHitsForFit;
    int       nEta;            // # volumes in eta
    int       nEtaTrack;       // # Track areas in eta
    int       nPhi;            // # volumes in nphi
    int       nPhiTrack;       // # Track areas in nphi
    int       nPrimaryPasses;  // # iterations looking for primaries
    int       nSecondaryPasses;// # iterations looking for secondaries
    boolean       vertexConstrainedFit; //
    int       parameterLocation; // 0=inner most point, 1=closest approach
    float     maxChi2Primary ; // maximum chi2 to be considered primary
    int       rowInnerMost;    // Row where end track search
    int       rowOuterMost;    // Outer most row to consider tin tracking
    int       rowStart;        // Row where start track search
    int       rowEnd  ;        // Row where end   track search
    int       szFitFlag;       // Switch for sz fit
    float     bField      ;    // Magnetic field
    float     hitChi2Cut;      // Maximum hit chi2
    float     goodHitChi2;     // Chi2 to stop looking for next hit
    float     trackChi2Cut;    // Maximum track chi2
    float     deta;            // Eta search range
    float     dphi;            // Phi search range
    float     detaMerge ;      // Eta difference for track merge
    float     dphiMerge ;      // Phi difference for track merge
    float     distanceMerge ;  // Maximum distance for reference point to merge secondaries
    float     etaMin;          // Min eta to consider
    float     etaMinTrack ;    // Track min eta to consider
    float     etaMax;          // Max eta to consider
    float     etaMaxTrack ;    // Track max eta to consider
    float     goodDistance ;   // In segment building
    // distance consider good enough
    float     phiMin;          // Min phi to consider
    float     phiMinTrack ;    // Track min phi to consider
    float     phiMax;          // Max phi to consider
    float     phiMaxTrack ;    // Track max phi to consider
    float     phiShift      ;  // Shift in phi when calculating phi
    float     ptMinHelixFit ;  // Minimum pt to apply helix fit
    float     maxDistanceSegment; // Maximum distance for segments
    float     segmentMaxAngle; // Maximum angle between to consecutive track pieces
    // when forming segments. A piece is the connection
    // two hits
    float     szErrorScale;    // sz error scale
    float     xyErrorScale;    // xy error scale
    float     xVertex      ;   // x position primary vertex
    float     yVertex      ;   // y position primary vertex
    float     dxVertex     ;
    float     dyVertex     ;
    float     zVertex      ;
    float     xyWeightVertex;  // Weight vertex in x-y
    float     phiVertex      ;
    float     rVertex        ;
    float     maxTime        ; // maxTime tracker can run
    int       phiClosed ;
    int       primaries  ;
    int       nRowsPlusOne, nPhiPlusOne   ; // Number volumes + 1
    int       nEtaPlusOne, nPhiEtaPlusOne ; // Number volumes + 1
    int       nPhiTrackPlusOne, nEtaTrackPlusOne ;
    float     phiSlice, etaSlice ;
    float     phiSliceTrack, etaSliceTrack ;
    int       trackDebug ;
    int       hitDebug ;
    int       debugLevel ;
}

