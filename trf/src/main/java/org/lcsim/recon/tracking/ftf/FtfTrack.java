package org.lcsim.recon.tracking.ftf;
public class FtfTrack extends FtfBaseTrack
{
    
    
    
    static int   USE_SEGMENT= 1 ;
    static int   USE_FOLLOW = 2 ;
    static int   GO_DOWN    =-1 ;
    static int   GO_UP      = 1 ;
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //    Add hits to track
    // Arguments:
    //        thisHit:  hit pointer
    //        way     :  >0 add at beginning, <0 at end
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    void      add                   ( FtfHit   thisHit, int way )
    {
        //
        //      Increment # hits in this track
        //
        nHits++ ;
        //
        //         Update pointers
        //
        if ( way < 0 || nHits == 1 )
        {
            if ( nHits > 1 ) lastHit.nextTrackHit = thisHit ;
            lastHit = thisHit ;
            innerMostRow = lastHit.row ;
            xLastHit = lastHit.x ;
            yLastHit = lastHit.y ;
        }
        else
        {
            thisHit.nextTrackHit = firstHit ;
            firstHit = thisHit ;
            outerMostRow = firstHit.row ;
        }
        //
        //        Declare hit as used and fill chi2
        //
        thisHit.setStatus( this ) ;
        //
        //    Check whether a fit update is needed
        //
        if ( nHits < getPara().minHitsForFit ) return ;
        //
        //    Include hit in xy fit parameter calculation
        //
        
        s11Xy = s11Xy + thisHit.wxy ;
        s12Xy = s12Xy + thisHit.wxy * thisHit.xp ;
        s22Xy = s22Xy + thisHit.wxy * (thisHit.xp*thisHit.xp) ;
        g1Xy  = g1Xy  + thisHit.wxy * thisHit.yp ;
        g2Xy  = g2Xy  + thisHit.wxy * thisHit.xp * thisHit.yp ;
        
        
        if ( nHits > getPara().minHitsForFit  )
        {
            ddXy  = s11Xy * s22Xy - ( s12Xy*s12Xy ) ;
            if ( ddXy != 0 )
            {
                a1Xy  = ( g1Xy * s22Xy - g2Xy * s12Xy ) / ddXy ;
                a2Xy  = ( g2Xy * s11Xy - g1Xy * s12Xy ) / ddXy ;
            }
            else
            {
                if ( getPara().infoLevel > 0 )
                {
                    System.out.println("FtfTrack:add: ddXy = 0 \n" ) ;
                }
            }
        }
        //
        //     Now in the sz plane
        //
        if ( getPara().szFitFlag == 1 )
        {
            s11Sz = s11Sz + thisHit.wz ;
            s12Sz = s12Sz + thisHit.wz * thisHit.s ;
            s22Sz = s22Sz + thisHit.wz * thisHit.s * thisHit.s ;
            g1Sz  = g1Sz  + thisHit.wz * thisHit.z ;
            g2Sz  = g2Sz  + thisHit.wz * thisHit.s * thisHit.z ;
            
            if ( nHits > getPara().minHitsForFit )
            {
                
                ddSz  = s11Sz * s22Sz -  s12Sz * s12Sz ;
                if ( ddSz != 0 )
                {
                    a1Sz  = ( g1Sz * s22Sz - g2Sz * s12Sz ) / ddSz ;
                    a2Sz  = ( g2Sz * s11Sz - g1Sz * s12Sz ) / ddSz ;
                }
                else
                {
                    if ( getPara().infoLevel > 0 )
                    {
                        System.out.println( "FtfTrack:add: ddSz = 0 \n" ) ;
                    }
                }
            }
        }
    }
    //****************************************************************************
    //   Fill track information tables
    //****************************************************************************
    
    void      add                   ( FtfTrack piece )
    {
        //
        //   Get circle parameters
        //
        s11Xy += piece.s11Xy  ;
        s12Xy += piece.s12Xy  ;
        s22Xy += piece.s22Xy  ;
        g1Xy  += piece.g1Xy   ;
        g2Xy  += piece.g2Xy   ;
        
        ddXy  =   s11Xy * s22Xy - ( s12Xy*s12Xy ) ;
        a1Xy  = ( g1Xy * s22Xy - g2Xy * s12Xy ) / ddXy ;
        a2Xy  = ( g2Xy * s11Xy - g1Xy * s12Xy ) / ddXy ;
        //
        //     Now in the sz plane
        //
        if ( getPara().szFitFlag == 1 )
        {
            double det1 = s11Sz * s22Sz - s12Sz * s12Sz ;
            dtanl = (double) ( s11Sz / det1 );
            dz0   = (double) ( s22Sz / det1 );
            
            double det2 = piece.s11Sz * piece.s22Sz - piece.s12Sz * piece.s12Sz ;
            piece.dtanl = (double) ( piece.s11Sz / det2 );
            piece.dz0   = (double) ( piece.s22Sz / det2 );
            
            double weight1 = 1./(dtanl*dtanl);
            double weight2 = 1./(piece.dtanl*piece.dtanl);
            double weight  = (weight1+weight2);
            tanl = ( weight1 * tanl + weight2 * piece.tanl ) / weight ;
            
            weight1 = 1./(dz0*dz0);
            weight2 = 1./(piece.dz0*piece.dz0);
            weight  = (weight1+weight2);
            z0   = ( weight1 * z0 + weight2 * piece.z0 ) / weight ;
        }
        
        //
        //  Add space points to first track
        //
        int counter ;
        if ( piece.outerMostRow < outerMostRow )
        {
            if ( lastHit != null )
            {
                counter = 0 ;
                for ( currentHit   = piece.firstHit ;
                currentHit != null && counter < piece.nHits ;
                currentHit  = currentHit.nextTrackHit  )
                {
                    currentHit.track = this   ;
                    counter++ ;
                }
                lastHit.nextTrackHit = piece.firstHit ;
                lastHit         = piece.lastHit ;
            }
            piece.firstHit = null ;
            innerMostRow = piece.innerMostRow ;
            xLastHit     = piece.xLastHit ;
            yLastHit     = piece.yLastHit ;
        }
        else
        {
            if ( piece.lastHit != null )
            {
                counter = 0 ;
                for ( currentHit   = piece.firstHit ;
                currentHit != null && counter < piece.nHits ;
                currentHit  = currentHit.nextTrackHit  )
                {
                    currentHit.track = this   ;
                    counter++;
                }
                piece.lastHit.nextTrackHit = firstHit ;
                firstHit               = piece.firstHit ;
            }
            outerMostRow = piece.outerMostRow ;
            piece.firstHit = null ;
        }
        //
        //
        
        nHits  += piece.nHits ;
        chi2[0] += piece.chi2[0] ;
        chi2[1] += piece.chi2[1] ;
        //
        //   Update track parameters
        //
        //
        getPara().szFitFlag = 0 ;
        if ( getPara().fillTracks ) fill( ) ;
        getPara().szFitFlag = 1 ;
        //
        //
        //   Declare track 2 not to be used
        //
        piece.flag    = -1 ;
        
    }
    
    //****************************************************************************
    //   Control how the track gets built
    //****************************************************************************
    
    int       buildTrack            ( FtfHit frstHit, FtfContainer volume )
    {
        
        //
        //   Add first hit to track
        //
        add( frstHit, GO_DOWN ) ;
        //
        //    Try to build a segment first
        //
        if ( !segment( volume, GO_DOWN ) ) return 0 ;
        //
        //    If segment build go for a real track with a fit
        //
        int rowToStop = getPara().rowInnerMost ;
        if ( !follow( volume, GO_DOWN, rowToStop ) ) return 0 ;
        //
        //    Now to extent track the other direction if requested
        //
        if ( getPara().goBackwards ) follow( volume, GO_UP, getPara().rowOuterMost ) ;
        //
        //  Fill tracks
        //
        if ( getPara().fillTracks ) fill( ) ;
        
        //   debugFill ( ) ;
        
        
        return 1 ;
    }
    //***************************************************************************
    //   Calculates dEdx
    //***************************************************************************
    
    void      dEdx                  ( )
    {
        int i, j ;
        FtfBaseHit nextHit ;
        int nTruncate = Math.max(1,
                getPara().dEdxNTruncate*nHits/100) ;
        nTruncate = Math.min(nHits/2,nTruncate) ;
        //
        //   Define array to keep largest de's
        //
        double[] de = new double[nTruncate] ;
        //
        //    Reset
        //
        dedx = 0.F ;
        //cng   memset ( de, 0, nTruncate*sizeof(double) ) ;
        //
        //
        //
        for  ( nextHit = firstHit ;
        nextHit != null ;
        nextHit = nextHit.nextTrackHit)
        {
            
            dedx += nextHit.q ;
            
            if ( nextHit.q < de[0] ) continue ;
            
            for ( i = nTruncate-1 ; i>=0 ; i-- )
            {
                if ( nextHit.q > de[i] )
                {
                    for ( j=0 ; j<i ; j++ ) de[j] = de[j+1] ;
                    de[i] = nextHit.q ;
                    break ;
                }
            }
        }
        //
        //    Subtract largest de
        //
        for ( i=0 ; i<nTruncate ; i++ ) dedx -= de[i] ;
        dedx = dedx / length ;
        //   End track in required volume condition
    }
    //***********************************************************************
    //   Delete track candidate
    //***********************************************************************
    
    void      deleteCandidate       ( )
    {
        FtfHit curentHit = (FtfHit)firstHit ;
        FtfHit nextHit ;
        
        //  debugDeleteCandidate ( ) ;
        
        while ( curentHit != null )
        {
            nextHit            = (FtfHit)curentHit.nextTrackHit;
            curentHit.nextTrackHit     =  null ;
            curentHit.xyChi2   =
                    curentHit.szChi2   =
                    curentHit.s        =  0.F ;
            
            curentHit.setStatus( null ) ;
            curentHit = nextHit;
        }
    }
    
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //    Fills track variables with or without fit
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    void      fill                  ( )
    {
        //
        //   Get circle parameters
        //
        double xc, yc ;
        double rc   = Math.sqrt( a2Xy * a2Xy + 1 ) / ( 2 * Math.abs(a1Xy) ) ;
        pt          = (double)(FtfGeneral.bFactor * getPara().bField * rc );
        double xParameters = 0.;
        double yParameters = 0.;
        //
        if ( pt > getPara().ptMinHelixFit )
        {
            double combinedChi2 = 0.5*(chi2[0]+chi2[1])/nHits ;
            if ( (getPara().primaries!=0) && (combinedChi2 < getPara().maxChi2Primary) )
                getPara().vertexConstrainedFit = true ;
            else
                getPara().vertexConstrainedFit = false ;
            
            fitHelix( ) ;
            
            if ( getPara().vertexConstrainedFit && (getPara().parameterLocation==0) )
            {
                updateToRadius( Math.sqrt(xLastHit*xLastHit+yLastHit*yLastHit) ) ;
            }
            else if ( !getPara().vertexConstrainedFit && !(getPara().parameterLocation==0) )
            {
                updateToClosestApproach( getPara().xVertex, getPara().yVertex ) ;
            }
        }
        else
        {
            if ( getPara().primaries!=0 )
            {
                double[] xyrc = new double[2];
                fillPrimary( xyrc, getPara().xVertex, getPara().yVertex ) ;
                xc = xyrc[0];
                yc = xyrc[1];
                rc = xyrc[2];
                if ( getPara().parameterLocation==1 )
                {// give track parameters at inner most point
                    updateToRadius( Math.sqrt(xLastHit*xLastHit+yLastHit*yLastHit) ) ;
                }
            }
            else
            { // Secondaries now
                xc = - a2Xy / ( 2. * a1Xy ) + xRefHit ;
                yc = - 1.   / ( 2. * a1Xy ) + yRefHit ;
                if ( getPara().parameterLocation==1 )
                { // give track parameters at inner most point
                    xParameters = xLastHit ;
                    yParameters = yLastHit ;
                }
                else
                { // give parameters at point of closest approach
                    double[] xyp = new double[2];
                    getClosest( getPara().xVertex, getPara().yVertex,
                            rc, xc, yc, xyp) ;
                    xParameters = xyp[0];
                    yParameters = xyp[1];
                }
                double[] xyc = new double[2];
                fillSecondary( xyc, xParameters, yParameters ) ;
                xc = xyc[0];
                yc = xyc[1];
            }
            //
            //    Get Errors
            //
            if ( getPara().getErrors )
            {
                getErrorsCircleFit(  (double)xc, (double)yc, (double)rc ) ;
                double det = s11Sz * s22Sz - s12Sz * s12Sz ;
                dtanl = (double) ( s11Sz / det );
                dz0   = (double) ( s22Sz / det );
            }
        }
    }
    //****************************************************************************
    //     Fill track information variables
    //****************************************************************************
    
    void fillPrimary( double[] xyrc, /*double &xc, double &yc, double &rc,*/
            double xPar, double yPar )
    {
        //
        //   Get circle parameters
        //
        double xc = getPara().xVertex - a2Xy / ( 2. * a1Xy ) ;
        double yc = getPara().yVertex - 1.   / ( 2. * a1Xy ) ;
        double rc = xyrc[2];
        
        //
        //   Get track parameters
        //
        double angle_vertex  = Math.atan2( yPar-yc, xPar-xc ) ;
        if ( angle_vertex < 0. ) angle_vertex = angle_vertex + 2.*Math.PI ;
        
        double dx_last    = xLastHit - xc ;
        double dy_last    = yLastHit - yc ;
        double angle_last = Math.atan2( dy_last, dx_last ) ;
        if ( angle_last < 0. ) angle_last = angle_last + 2.*Math.PI ;
        //
        //       Get the rotation
        //
        double d_angle = angle_last - angle_vertex ;
        // double d_angle = angle_vertex - angle_last ;
        
        // if ( d_angle >  pi ) d_angle -= twoPi  ;
        if ( d_angle < -Math.PI ) d_angle += 2.*Math.PI  ;
        
        q = (short) ( ( d_angle < 0 ) ? 1 : -1 ) ;
        r0   = Math.sqrt(xPar*xPar+yPar*yPar) ;
        phi0 = Math.atan2(yPar,xPar) ;
        if ( phi0 < 0 ) phi0 += 2. * Math.PI ;
        psi  = (double)(angle_vertex - q * 0.5F * Math.PI) ;
        if ( psi < 0     )  psi = (double)(psi + 2.*Math.PI );
        if ( psi > 2.*Math.PI )  psi = (double)(psi - 2.*Math.PI );
        //
        //      Get z parameters if needed
        //
        if ( getPara().szFitFlag == 1 )
        {
            tanl = -(double)a2Sz ;
            z0   =  (double)(a1Sz + a2Sz * ( length - rc * d_angle * q ) );
        }
        else if ( getPara().szFitFlag == 2 )
        {
            tanl = firstHit.z /
                    Math.sqrt( firstHit.x*firstHit.x + firstHit.y*firstHit.y ) ;
            z0      = 0.F ;
        }
        
        //
        //    Store some more track info
        //
        eta     = FtfGeneral.seta(1.f,(float)tanl )   ;
        //
        //   Set primary track
        //
        flag = 1 ;
        
    }
    //****************************************************************************
    //
    //   Fill track information tables
    //
    //****************************************************************************
    
    void      fillSecondary         ( double[] xyc, /*double &xc, double &yc,*/double xPar, double yPar )
    {
        double xc = xyc[0];
        double yc = xyc[1];
        double rc = xyc[2];
        double twoPi = 2.*Math.PI;
                /*--------------------------------------------------------------------------
                Get angles for initial and final points
                ------------------------------------------------------------------------------*/
        double dx1    = firstHit.x - xc ;
        double dy1    = firstHit.y - yc ;
        double angle1 = Math.atan2( dy1, dx1 ) ;
        if ( angle1 < 0. ) angle1 = angle1 + twoPi ;
        
        double dx2    = xLastHit - xc ;
        double dy2    = yLastHit - yc ;
        double angle2 = Math.atan2( dy2, dx2 ) ;
        if ( angle2 < 0. ) angle2 = angle2 + twoPi ;
                /*--------------------------------------------------------------------------
                Get the rotation
                ------------------------------------------------------------------------------*/
        
        double dangle = angle2 - angle1 ;
        //  if ( dangle >  pi ) dangle =   dangle - twoPi  ;
        if ( dangle < -Math.PI ) dangle =   dangle + twoPi  ;
        
        q    = (short) ( ( dangle > 0 ) ? 1 : -1 ) ;
        r0   = ((FtfHit)lastHit).r   ;
        phi0 = ((FtfHit)lastHit).phi ;
        psi  = (double)(angle2 - q * Math.PI/2. );
        if ( psi < 0     ) psi = (double)(psi + twoPi );
        //
        //      Get z parameters if needed
        //
        if ( getPara().szFitFlag !=0 )
        {
            tanl = -(double)a2Sz ;
            z0   =  (double)(a1Sz + a2Sz * length  );
        }
        else
        {
            tanl = firstHit.z /
                    Math.sqrt( firstHit.x*firstHit.x +
                    firstHit.y*firstHit.y ) ;
            z0      = 0.F ;
        }
        //
        //-.    Store some more track info
        //
        eta     = FtfGeneral.seta(1.f, (float)tanl )   ;
        //
        //    Set primary track flag
        //
        flag = 0 ;
        
    }
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //        Adds hits to a track chosing the closest to fit
    // Arguments:
    //              volume:	      volume pointer
    //              way   :       which way to procede in r (negative or positive)
    //              row_to_stop:  row index where to stop
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    boolean       follow                ( FtfContainer volume, int way, int rowToStop )
    {
        
        FtfHit nextHit = null;
        
        if ( way < 0 )
            nextHit = (FtfHit )lastHit ;
        else
            nextHit = (FtfHit )firstHit ;
        if (FtfGeneral.TRDEBUG)
        {
            if ( getPara().trackDebug==0 && getPara().debugLevel >= 2 )
                System.out.println( "FtfTrack::follow: ===> Going into Track extension <===\n" );
        }
        //
        //     Define variables to keep total chi2
        //
        double xyChi2 = chi2[0] ;
        double szChi2 = chi2[1] ;
        
        //
        //    Loop as long a a hit is found and the segment
        //    is shorter than n_hit_segm
        //
        while ( way * nextHit.row < way *rowToStop )
        {
            //
            //      Select next hit
            //
            chi2[0] = getPara().hitChi2Cut ;
            
            nextHit = seekNextHit( volume, nextHit, way*getPara().trackRowSearchRange, USE_FOLLOW ) ;
            
            if (FtfGeneral.TRDEBUG)
            {
                if ( getPara().trackDebug !=0 && getPara().debugLevel >= 1 )
                {
                    if ( nextHit != null )
                    {
                        System.out.println( "FtfTrack::follow: Search succesful, hit selected "+
                                nextHit.id );
                        //		    nextHit.Show ( getPara().color_track ) ;
                    }
                    else
                    {
                        System.out.println( "FtfTrack::follow: Search unsuccesful\n" );
                        if ( chi2[0]+chi2[1] > getPara().hitChi2Cut )
                            System.out.println( " hit chi2 "+chi2[0]+chi2[1]+" larger than cut" + getPara().hitChi2Cut ) ;
                    }
                    //cng         debugAsk () ;
                }
            }
            
            //
            //    Stop if nothing found
            //
            if ( nextHit == null ) break ;
            //
            //   Keep total chi2
            //
            double lxyChi2 = chi2[0]-chi2[1] ;
            xyChi2 += lxyChi2 ;
            nextHit.xyChi2 = (float) lxyChi2 ;
            //
            //   if sz fit update track length
            //
            if ( getPara().szFitFlag !=0  )
            {
                length = nextHit.s ;
                szChi2 += chi2[1]  ;
                nextHit.szChi2 = (float) chi2[1] ;
            }
            //
            //     Add hit to track
            //
            add( nextHit, way ) ;
            
        } // End while
        //
        //    Check # hits
        //
        if ( nHits < getPara().minHitsPerTrack ) return false ;
        //
        //   Store track chi2
        //
        chi2[0] = xyChi2 ;
        chi2[1] = szChi2 ;
        //
        //        Check total chi2
        //
        double normalized_chi2 = (chi2[0]+chi2[1])/nHits ;
        if ( normalized_chi2 > getPara().trackChi2Cut ) return false ;
        //
        return true;
    }
    /*******************************************************************************
     * Reconstructs tracks
     *********************************************************************************/
    
    int       followHitSelection    ( FtfHit baseHit, FtfHit candidateHit )
    {
        //
        double lszChi2 = 0 ;
        double lchi2 ;
        double slocal = 0.;
        double deta, dphi ;
        double dx, dy, dxy, dsz, temp ;
        double twoPi =2.*Math.PI;
        //
        //           Check delta eta
        //
        //   if ( baseHit.dz < 1000. && candidateHit.dz < 1000 ){
        deta = Math.abs((baseHit.eta)-(candidateHit.eta)) ;
        if ( deta > getPara().deta ) return 0 ;
        //   }
        //   else deta = 0.F ;
        //
        //           Check delta phi
        //
        dphi = Math.abs((baseHit.phi)-(candidateHit.phi)) ;
        if ( dphi > getPara().dphi && dphi < twoPi-getPara().dphi ) return 0 ;
        //
        //      If looking for secondaries calculate conformal coordinates
        //
        if ( (getPara().primaries)==0 )
        {
            double xx = candidateHit.x - xRefHit ;
            double yy = candidateHit.y - yRefHit ;
            double rr = xx * xx + yy * yy ;
            candidateHit.xp =   (float) (xx / rr) ;
            candidateHit.yp = (float) (- yy / rr) ;
            
            candidateHit.wxy  = (float) (rr * rr /
                    ( (getPara().xyErrorScale*getPara().xyErrorScale)  *
                    ( (candidateHit.dx*candidateHit.dx) + (candidateHit.dy*candidateHit.dy) ) ) );
        }
        //
        //      Calculate distance in x and y
        //
        temp = (a2Xy * candidateHit.xp - candidateHit.yp + a1Xy) ;
        dxy  = temp * temp / ( a2Xy * a2Xy + 1.F ) ;
        //
        //    Calculate chi2
        //
        
        lchi2    = (dxy * candidateHit.wxy) ;
        
        if ( lchi2 > chi2[0] ) return 0 ;
        //
        //      Now in the sz plane
        //
        if ( getPara().szFitFlag !=0 )
        {
            //
            //        Get "s" and calculate distance hit-line
            //
            dx     = baseHit.x - candidateHit.x ;
            dy     = baseHit.y - candidateHit.y ;
            slocal = length + Math.sqrt( dx * dx + dy * dy ) ;
            
            temp = (a2Sz * slocal - candidateHit.z + a1Sz) ;
            dsz  = temp * temp / ( a2Sz * a2Sz + 1 ) ;
            //
            //              Calculate chi2
            //
            lszChi2 = dsz * candidateHit.wz ;
            lchi2 += lszChi2 ;
        }
        else
        {
            lszChi2 = 0.F ;
        }
        //
        //         Check whether the chi2 square is better than previous one
        //
        if ( lchi2 < chi2[0] )
        {
            chi2[0]       = (double)lchi2    ;
            chi2[1]       = (double)lszChi2 ;
            
            if ( getPara().szFitFlag !=0  ) candidateHit.s = (float)slocal ;
            //
            //       if a good chi2 is found let's stop here
            //
            if ( lchi2 < getPara().goodHitChi2 ) return 2 ;
            
            return 1 ;
        }
        //
        //     Return the selected hit
        //
        
        return 0 ;
    }
    
    
    
    FtfTrack getNextTrack( )
    { return nxatrk ;
    }
    
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //    Merges tracks
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    int       mergePrimary          ( FtfContainer  trackArea )
    {
        
        //cng run into problems here with ftfcontainer[].
        // cng stop for now.
        //11/23/03
                /*
                        double twoPi = 2.*Math.PI;
                short  track_merged ;
                int  areaIndex ;
                int    i_phi, i_eta ;
                FtfTrack i_track = null;
                int    ip, ie ;
                double  delta_psi ;
                //
                //   Check Track is primary
                //
                if ( flag != 1 ) return 0 ;
                //-
                //   Get track area
                //
                i_phi = (int)(( psi - getPara().phiMinTrack ) / getPara().phiSliceTrack + 1 );
                if ( i_phi < 0 ) {
                System.out.println ( " Track phi index too low "+ i_phi ) ;
                i_phi = 1 ;
                }
                if ( i_phi >= getPara().nPhiTrackPlusOne ) {
                System.out.println ( " Track phi index too high "+ i_phi ) ;
                i_phi = getPara().nPhiTrack ;
                }
                //
                //     Now eta
                //
                i_eta = (int)(( eta - getPara().etaMinTrack ) / getPara().etaSliceTrack + 1 );
                if ( i_eta <= 0 ) {
                System.out.println ( " Track eta index too low "+ i_eta ) ;
                i_eta = 1 ;
                }
                if ( i_eta >= getPara().nEtaTrackPlusOne ) {
                System.out.println ( " Track eta index too high "+ i_eta ) ;
                i_eta = getPara().nEtaTrack ;
                }
                //
                //     Loop around selected area
                //
                track_merged = 0 ;
                for ( ip = Math.max(i_phi-1,1) ; ip < Math.min(i_phi+2,getPara().nPhiTrackPlusOne) ; ip++ ) {
                for ( ie = Math.max(i_eta-1,1) ; ie < Math.min(i_eta+2,getPara().nEtaTrackPlusOne) ; ie++ ) {
                areaIndex = ip * getPara().nEtaTrackPlusOne + ie ;
                //
                //    Loop over tracks
                //
                for ( i_track = (FtfTrack)trackArea[areaIndex].first ;
                i_track != null ;
                i_track = i_track.getNextTrack()  ) {
                //
                //    Reject track if it is not good
                //
                if ( i_track.flag < 0 ) continue ;
                //
                // Compare both tracks
                //
                //   No overlapping tracks
                short delta1 = i_track.outerMostRow - outerMostRow ;
                short delta2 = i_track.innerMostRow - innerMostRow ;
                if ( delta1 * delta2 <= 0 ) continue ;
                //
                //    Tracks close enough
                //
                if ( Math.abs(eta-i_track.eta) > getPara().detaMerge ) continue ;
                delta_psi = (double)Math.abs(psi - i_track.psi) ;
                if ( delta_psi > getPara().dphiMerge && delta_psi < twoPi - getPara().dphiMerge ) continue ;
                 
                i_track.add ( this ) ;
                if (FtfGeneral.TRDEBUG)
                {
                if ( getPara().debugLevel > 1 )
                System.out.println ( " \n Track "+this.id+" merge into "+ i_track.id ) ;
                }
                track_merged = 1 ;
                break ;
                }
                }
                }
                //
                //.  If track not matched add it
                //
                if ( track_merged == 0 ) {
                areaIndex = i_phi * getPara().nEtaTrackPlusOne + i_eta ;
                if ( trackArea[areaIndex].first == null )
                trackArea[areaIndex].first =
                trackArea[areaIndex].last = this  ;
                else {
                ((FtfTrack )trackArea[areaIndex].last).nxatrk = this ;
                 trackArea[areaIndex].last = this ;
                }
                }
                return track_merged ;
                 
                 */
        return 0;
    }
    
    
    void      reset                 ( )
    {
                /*----------------------------------------------------------------------
                Set fit parameters to zero
                ----------------------------------------------------------------------*/
        
        flag     = (short) (getPara().primaries);
        nHits    = 0 ;
        s11Xy   =
                s12Xy   =
                s22Xy   =
                g1Xy    =
                g2Xy    =
                chi2[0]  = 0.F ;
        nxatrk   = null ;
        if ( getPara().szFitFlag != 0 )
        {
            s11Sz =
                    s12Sz =
                    s22Sz =
                    g1Sz  =
                    g2Sz  =
                    chi2[1]  =
                    length         = 0.F ;
        }
    }
    
    
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //     Function to look for next hit
    // Input:	volume:         Volume pointer
    //          baseHit:       Last point in track
    //          n_r_steps:      How many rows search and which way (up or down)
    //		    which_function: Function to be used to decide whether the hit is good
    // Returns:	Selected hit
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    FtfHit    seekNextHit          ( FtfContainer  volume, FtfHit baseHit,
            int nradiusSteps, int whichFunction )
    {
                /*
                #define N_LOOP 9
                int loop_eta[N_LOOP] = { 0, 0, 0,-1,-1,-1, 1, 1, 1 } ;
                int loop_phi[N_LOOP] = { 0,-1, 1, 0,-1, 1, 0,-1, 1 };
                 
                 
                int ir, irp, ipp, itp, k;
                register int areaIndex ;
                int result ;
                 
                //-------------------------------------------------------------------------------
                //     Calculate limits on the volume loop
                //-----------------------------------------------------------------------------
                int initialRow, way ;
                if ( n_r_steps < 0 ) {
                initialRow = max(1, (baseHit->row - getPara()->rowInnerMost)/getPara()->modRow);
                n_r_steps  = min(initialRow,-n_r_steps ) ;
                way        = -1 ;
                }
                else {
                initialRow = max(1, (baseHit->row - getPara()->rowInnerMost + 2)/getPara()->modRow);
                n_r_steps  = min((getPara()->rowOuterMost-initialRow+1),n_r_steps) ;
                way = 1 ;
                }
                 
                 
                FtfHit *selected_hit  = 0 ;
                //
                //      Loop over modules
                //
                for ( ir = 0 ; ir < n_r_steps ; ir++ ){
                irp = initialRow + way * ir ;
                for ( k=0; k< N_LOOP; k++){
                ipp = baseHit->phiIndex + loop_phi[k];
                //
                //--   Gymnastics if phi is closed
                //
                if ( ipp < 1 ) {
                if ( getPara()->phiClosed )
                ipp = getPara()->nPhi + ipp ;
                else
                continue ;
                }
                else if ( ipp > getPara()->nPhi ) {
                if ( getPara()->phiClosed )
                ipp = ipp - getPara()->nPhi ;
                else
                continue ;
                }
                //
                //     Now get eta index
                //
                itp = baseHit->etaIndex + loop_eta[k];
                if ( itp <     1      ) continue ;
                if ( itp > getPara()->nEta ) continue ;
                //
                #ifdef TRDEBUG
                if ( getPara()->trackDebug && getPara()->debugLevel >= 4 )
                printf ( "FtfTrack::seekNextHit: search in row %d \n",irp ) ;
                #endif
                //
                //       Now loop over hits in each volume
                //
                areaIndex = irp   * getPara()->nPhiEtaPlusOne + ipp * getPara()->nEtaPlusOne + itp ;
                for ( FtfHit *candidateHit = (FtfHit *)volume[areaIndex].first ;
                candidateHit != 0 ;
                candidateHit = (FtfHit *)candidateHit->nextVolumeHit ){
                #ifdef TRDEBUG
                debugInVolume ( baseHit, candidateHit ) ;
                #endif
                //----------------------------------------------------------------------------
                //         Check whether the hit was used before
                //--------------------------------------------------------------------------
                if ( candidateHit->track != 0 ) continue ;
                //--------------------------------------------------------------------------
                //         If first points, just choose the closest hit
                //--------------------------------------------------------------------------
                if ( which_function == USE_SEGMENT )
                        result = segmentHitSelection ( baseHit, candidateHit ) ;
                     else
                result = followHitSelection  ( baseHit, candidateHit ) ;
                //
                //     Check result
                //
                if ( result > 0 ) {
                        selected_hit = candidateHit ;
                if ( result ==2  ) goto found ;
                }
                //
                //       End hit loop
                //
                }
                //
                //     End row loop
                //
                }
                //
                //   End volume loop inside cone
                //
                }
                found: ;
                 
                return selected_hit ;
                 */
        
        return new FtfHit();
    }
    
    
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //   Forms segments
    //   Arguments:
    //             volume     :    volume pointer
    //             way        :    whether to go to negative or positive ir
    //             row_to_stop:    row index where to stop
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    boolean     segment               ( FtfContainer volume, int way )
    {
                /*
                //
                //   Define some variables
                //
                double dx, dy, rr ;
                FtfHit* nextHit ;
                //
                //   Check which way to go
                //
                if ( way < 0 )
                   nextHit = (FtfHit *)lastHit ;
                else
                   nextHit = (FtfHit *)firstHit ;
                #ifdef TRDEBUG
                if ( getPara()->trackDebug && getPara()->debugLevel >= 4 )
                printf ( "FtfTrack:segment: **** Trying to form segment ****\n" );
                #endif
                //
                //    Loop as long a a hit is found and the segment
                //    is shorter than n_hit_segm
                //
                while ( nextHit != 0 && nHits < getPara()->nHitsForSegment ) {
                chi2[0] = getPara()->maxDistanceSegment ; ;
                nextHit = seekNextHit ( volume, nextHit, way*getPara()->segmentRowSearchRange,
                USE_SEGMENT ) ;
                #ifdef TRDEBUG
                if ( getPara()->trackDebug && getPara()->debugLevel > 0 ) {
                if ( nextHit != 0 ) {
                printf ( "FtfTrack::segment: Search succesful, hit %d selected\n",
                nextHit->id );
                //       nextHit->Show ( getPara()->color_track ) ;
                }
                else
                printf ( "FtfTrack::segment: Search unsuccesful\n" );
                debugAsk () ;
                }
                #endif
                //
                //     If sz fit update s
                //
                if ( nextHit != 0 ){
                //
                //   Calculate track length if sz plane considered
                //
                if ( getPara()->szFitFlag  ){
                dx = ((FtfBaseHit *)nextHit)->x - ((FtfBaseHit *)lastHit)->x ;
                dy = ((FtfBaseHit *)nextHit)->y - ((FtfBaseHit *)lastHit)->y ;
                length    += (double)sqrt ( dx * dx + dy * dy ) ;
                nextHit->s      = length ;
                }
                //
                //   Calculate conformal coordinates
                //
                if ( getPara()->primaries == 0 ){
                rr = square ( xRefHit - nextHit->x ) +
                square ( yRefHit - nextHit->y ) ;
                 
                 
                nextHit->xp    =   ( nextHit->x - xRefHit ) / rr ;
                nextHit->yp    = - ( nextHit->y - yRefHit ) / rr ;
                nextHit->wxy   = rr * rr / ( square(getPara()->xyErrorScale)  *
                square(nextHit->dx) + square(nextHit->dy) ) ;
                }
                //
                //     Add hit to track
                //
                 add ( nextHit, way ) ;
                }
                } // End while ( lastHit ...
                //
                //    If number of hits is as expected return 1
                //
                if ( nHits == getPara()->nHitsForSegment )
                return 1 ;
                else
                return 0 ;
                 */
        return false;
    }
    
    
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //     Routine to look for segments.
    //	 Arguments:
    //	 baseHit:       Hit from which track is being extrapolated
    //   candidateHit:  Hit being examined as a candidate to which extend track
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    int     segmentHitSelection( FtfHit baseHit, FtfHit candidateHit )
    {
                /*
                double dx, dy, dr, d3, dangle ;
                double dphi, deta ;
                double   angle ;
                //
                //   select hit with the
                //   the smallest value of d3 (defined below)
                //
                dphi  = (double)fabs((baseHit->phi) - (candidateHit->phi)) ;
                if ( dphi > pi ) dphi = (double)fabs( twoPi - dphi ) ;
                if ( dphi > getPara()->dphi && dphi < twoPi -getPara()->dphi ) return 0 ;
                //
                //    Make sure we want to look at the difference in eta
                //
                if ( baseHit->dz < 1000. && candidateHit->dz < 1000. ){
                deta  = (double)fabs((baseHit->eta) - (candidateHit->eta)) ;
                if ( deta > getPara()->deta ) return 0 ;
                }
                else deta = 0.F ;
                 
                dr    = (double)fabs((double)(baseHit->row - candidateHit->row));
                d3    = (double)(toDeg * dr * ( dphi  + deta ) ) ;
                //
                //     If initial segment is longer than 2 store angle info in
                //     a1Xy and a1_sz
                //
                if ( getPara()->nHitsForSegment > 2 && nHits-1 < getPara()->nHitsForSegment ) {
                dx = candidateHit->x - baseHit->x ;
                dy = candidateHit->y - baseHit->y ;
                angle = (double)atan2 ( dy, dx ) ;
                if ( angle < 0  ) angle = angle + twoPi ;
                lastXyAngle = angle ;
                }
                #ifdef TRDEBUG
                if ( getPara()->trackDebug && getPara()->debugLevel >= 3 ) {
                printf ( "FtfTrack::segmentHitSelection:\n");
                printf ( "dr,dphi,deta,distance, Min distance  %7.2f %7.2f %7.2f %7.2f %7.2f\n",
                dr,dphi,deta,d3,chi2[0] ) ;
                if ( d3 < chi2[0] )
                printf ( "Best point, keep it !!!\n" );
                else{
                printf ( "Worse than previous, reject !!\n" );
                //       candidateHit->Show ( getPara()->color_transparent );
                }
                debugAsk() ;
                }
                #endif
                if ( d3 < chi2[0] ) {
                //
                //   For second hit onwards check the difference in angle
                //   between the last two track segments
                //
                if ( nHits > 1 ) {
                 dx     = candidateHit->x - baseHit->x ;
                dy     = candidateHit->y - baseHit->y ;
                angle  = (double)atan2 ( dy, dx ) ;
                if ( angle < 0  ) angle = angle + twoPi ;
                    dangle = (double)fabs ( lastXyAngle - angle );
                    lastXyAngle = angle ;
                if ( dangle > getPara()->segmentMaxAngle ) return 0 ;
                }
                //
                //    Check whether this is the "closest" hit
                //
                chi2[0]          = d3 ;
                if ( d3 < getPara()->goodDistance ) return 2 ;
                  return 1 ;
                }
                //
                //    If hit does not fulfill criterai return 0
                //
                return 0 ;
                 */
        
        return 0;
    }
    FtfTrack nxatrk  ;
    
        /*
        void debugAsk                 ( ) ;
        void debugDeleteCandidate     ( ) ;
        void debugFill                ( ) ;
        void debugFollowCandidate     ( FtfHit *candidate_hit ) ;
        void debugFollowSuccess       ( double dxy, double dsz, double lchi2_xy,
        double lchi2_sz, double chi2_min,
        FtfHit *candidate_hit ) ;
        void debugInVolume            ( FtfHit *base_hit, FtfHit *current_hit ) ;
        void debugNew                 ( ) ;
         */
    
    float   lastXyAngle ;    // Angle in the xy plane of line connecting to last hits
    
    double    xRefHit ;
    double    yRefHit ;
    double    xLastHit ;
    double    yLastHit ;
    
    double    s11Xy  ;       // Fit Parameters
    double    s12Xy  ;
    double    s22Xy  ;
    double    g1Xy   ;
    double    g2Xy   ;
    double    s11Sz  ;
    double    s12Sz  ;
    double    s22Sz  ;
    double    g1Sz   ;
    double    g2Sz   ;
    
    double    ddXy, a1Xy, a2Xy ;    //fit par in xy
    double    ddSz, a1Sz, a2Sz ;    //fit par in sz
    
    public void nextHit()
    { currentHit = currentHit.nextTrackHit ;
    }
    
    
}
