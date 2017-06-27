package org.lcsim.recon.tracking.ftf;
//
//    Base Track class
//
public class FtfBaseTrack
{
    
    FtfBaseHit firstHit;// First hit belonging to track
    FtfBaseHit lastHit ;// Last  hit belonging to track
    FtfBaseHit currentHit ;
    
    
    double    bField ;
    
    int       id     ;  // primary key
    short     flag   ;  // Primaries flag=1, Secondaries flag=0
    short      innerMostRow ;
    short      outerMostRow ;
    short     nHits  ;  // Number of points assigned to that track
    short     nDedx  ;  // Number of points used for dEdx
    short     q  ;      // charge
    double[]    chi2 = new double[2];  // chi squared of the momentum fit
    double    dedx;     // dE/dx information
    double    pt  ;     // pt (transverse momentum) at (r,phi,z)
    double    phi0;     // azimuthal angle of point where parameters are given
    double    psi ;     // azimuthal angle of the momentum at (r,..
    double    r0  ;     // r (in cyl. coord.) for point where parameters given
    double    tanl;     // tg of the dip angle at (r,phi,z)
    double    z0  ;     // z coordinate of point where parameters are given
    double    length ;
    double    dpt ;
    double    dpsi;
    double    dz0 ;
    double    eta ;
    double    dtanl ;
    
    FtfPara para  ;    // Parameters pointer
    
    public    FtfBaseTrack( )
    {
    }
    
    
    public static void log(String s)
    {
        System.out.println(s);
    }
    public    int         fitHelix   (  )
    {
        if ( fitCircle( )!=0 )
        {
            log( " Problem in Fit_Circle " ) ;
            return 1 ;
        }
        //
        //     Fit line in s-z plane now
        //
        if ( fitLine( ) !=0)
        {
            log( " Problem fitting a line " ) ;
            return 1 ;
        }
        return 0 ;
    }
    
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //
    //  Fits circle parameters using algorithm
    //  described by ChErnov and Oskov in Computer Physics
    //  Communications.
    //
    //  Written in FORTRAN by Jawluen Tang, Physics department , UT-Austin
    //  Moved to C by Pablo Yepes
    //---------------------------------------------------------------
    
    public    int         fitCircle  (  )
    {
        double wsum  = 0.0 ;
        double xav   = 0.0 ;
        double yav   = 0.0 ;
        //
        //     Loop over hits calculating average
        //
        for ( startLoop() ; done() ; nextHit() )
        {
            
            FtfBaseHit cHit = currentHit ;
            cHit.wxy = 1.0F/(cHit.dx*cHit.dx + cHit.dy*cHit.dy) ;
            wsum      += cHit.wxy ;
            xav       += cHit.wxy * cHit.x ;
            yav       += cHit.wxy * cHit.y ;
        }
        if ( getPara().vertexConstrainedFit )
        {
            wsum += getPara().xyWeightVertex ;
            xav  += getPara().xVertex ;
            yav  += getPara().yVertex ;
        }
        xav = xav / wsum ;
        yav = yav / wsum ;
        //
        //  CALCULATE <X**2>, <XY>, AND <Y**2> WITH <X> = 0, & <Y> = 0
        //
        double xxav  = 0.0 ;
        double xyav  = 0.0 ;
        double yyav  = 0.0 ;
        double xi, yi ;
        
        for ( startLoop() ; done() ; nextHit() )
        {
            FtfBaseHit cHit = currentHit ;
            
            xi        = cHit.x - xav ;
            yi        = cHit.y - yav ;
            xxav     += xi * xi * cHit.wxy ;
            xyav     += xi * yi * cHit.wxy ;
            yyav     += yi * yi * cHit.wxy ;
        }
        
        if ( getPara().vertexConstrainedFit )
        {
            xi        = getPara().xVertex - xav ;
            yi        = getPara().yVertex - yav ;
            xxav     += xi * xi * getPara().xyWeightVertex ;
            xyav     += xi * yi * getPara().xyWeightVertex ;
            yyav     += yi * yi * getPara().xyWeightVertex ;
        }
        xxav = xxav / wsum ;
        xyav = xyav / wsum ;
        yyav = yyav / wsum ;
        //
        //-->  ROTATE COORDINATES SO THAT <XY> = 0
        //
        //-->  SIGN(C**2 - S**2) = SIGN(XXAV - YYAV) >
        //-->  &                                     > ==> NEW : (XXAV-YYAV) > 0
        //-->  SIGN(S) = SIGN(XYAV)                  >
        
        double a = Math.abs( xxav - yyav ) ;
        double b = 4.0 * xyav * xyav ;
        
        double asqpb  = a * a + b  ;
        double rasqpb = Math.sqrt( asqpb) ;
        
        double splus  = 1.0 + a / rasqpb ;
        double sminus = b / (asqpb * splus) ;
        
        splus  = Math.sqrt(0.5 * splus ) ;
        sminus = Math.sqrt(0.5 * sminus) ;
        //
        //->  FIRST REQUIRE : SIGN(C**2 - S**2) = SIGN(XXAV - YYAV)
        //
        double sinrot, cosrot ;
        if ( xxav <= yyav )
        {
            cosrot = sminus ;
            sinrot = splus  ;
        }
        else
        {
            cosrot = splus ;
            sinrot = sminus ;
        }
        
        //
        //->  REQUIRE : SIGN(S) = SIGN(XYAV) * SIGN(C) (ASSUMING SIGN(C) > 0)
        //
        if ( xyav < 0.0 ) sinrot = - sinrot ;
        //
        //-->  WE NOW HAVE THE SMALLEST ANGLE THAT GUARANTEES <X**2> > <Y**2>
        //-->  TO GET THE SIGN OF THE CHARGE RIGHT, THE NEW X-AXIS MUST POINT
        //-->  OUTWARD FROM THE ORGIN.  WE ARE FREE TO CHANGE SIGNS OF BOTH
        //-->  COSROT AND SINROT SIMULTANEOUSLY TO ACCOMPLISH THIS.
        //
        //-->  CHOOSE SIGN OF C WISELY TO BE ABLE TO GET THE SIGN OF THE CHARGE
        //
        if ( cosrot*xav+sinrot*yav < 0.0 )
        {
            cosrot = -cosrot ;
            sinrot = -sinrot ;
        }
        //
        //->  NOW GET <R**2> AND RSCALE= SQRT(<R**2>)
        //
        double rrav   = xxav + yyav ;
        double rscale = Math.sqrt(rrav) ;
        
        xxav   = 0.0 ;
        yyav   = 0.0 ;
        xyav   = 0.0 ;
        double xrrav	 = 0.0 ;
        double yrrav	 = 0.0 ;
        double rrrrav  = 0.0 ;
        
        double xixi, yiyi, riri, wiriri, xold, yold ;
        for ( startLoop() ; done() ; nextHit() )
        {
            FtfBaseHit cHit = currentHit ;
            xold = cHit.x - xav ;
            yold = cHit.y - yav ;
            //
            //-->  ROTATE SO THAT <XY> = 0 & DIVIDE BY RSCALE SO THAT <R**2> = 1
            //
            xi = (  cosrot * xold + sinrot * yold ) / rscale ;
            yi = ( -sinrot * xold + cosrot * yold ) / rscale ;
            
            xixi   = xi * xi ;
            yiyi   = yi * yi ;
            riri   = xixi + yiyi ;
            wiriri = cHit.wxy * riri ;
            
            xyav   += cHit.wxy * xi * yi ;
            xxav   += cHit.wxy * xixi ;
            yyav   += cHit.wxy * yiyi ;
            
            xrrav  += wiriri * xi ;
            yrrav  += wiriri * yi ;
            rrrrav += wiriri * riri ;
        }
        
        //
        //   Include vertex if required
        //
        if ( getPara().vertexConstrainedFit )
        {
            xold = getPara().xVertex - xav ;
            yold = getPara().yVertex - yav ;
            //
            //-->  ROTATE SO THAT <XY> = 0 & DIVIDE BY RSCALE SO THAT <R**2> = 1
            //
            xi = (  cosrot * xold + sinrot * yold ) / rscale ;
            yi = ( -sinrot * xold + cosrot * yold ) / rscale ;
            
            xixi   = xi * xi ;
            yiyi   = yi * yi ;
            riri   = xixi + yiyi ;
            wiriri = getPara().xyWeightVertex * riri ;
            
            xyav   += getPara().xyWeightVertex * xi * yi ;
            xxav   += getPara().xyWeightVertex * xixi ;
            yyav   += getPara().xyWeightVertex * yiyi ;
            
            xrrav  += wiriri * xi ;
            yrrav  += wiriri * yi ;
            rrrrav += wiriri * riri ;
        }
        //
        //
        //
        //-->  DIVIDE BY WSUM TO MAKE AVERAGES
        //
        xxav    = xxav   / wsum ;
        yyav    = yyav   / wsum ;
        xrrav   = xrrav  / wsum ;
        yrrav   = yrrav  / wsum ;
        rrrrav  = rrrrav / wsum ;
        xyav    = xyav   / wsum ;
        
        int   ntry = 5 ;
        //
        //-->  USE THESE TO GET THE COEFFICIENTS OF THE 4-TH ORDER POLYNIMIAL
        //-->  DON'T PANIC - THE THIRD ORDER TERM IS ZERO !
        //
        double xrrxrr = xrrav * xrrav ;
        double yrryrr = yrrav * yrrav ;
        double rrrrm1 = rrrrav - 1.0  ;
        double xxyy   = xxav  * yyav  ;
        
        double c0  =          rrrrm1*xxyy - xrrxrr*yyav - yrryrr*xxav ;
        double c1  =        - rrrrm1      + xrrxrr      + yrryrr   - 4.0*xxyy ;
        double c2  =   4.0  + rrrrm1                               - 4.0*xxyy ;
        double c4  = - 4.0  ;
        //
        //-->  COEFFICIENTS OF THE DERIVATIVE - USED IN NEWTON-RAPHSON ITERATIONS
        //
        double c2d =   2.0 * c2 ;
        double c4d =   4.0 * c4 ;
        //
        //-->  0'TH VALUE OF LAMDA - LINEAR INTERPOLATION BETWEEN P(0) & P(YYAV)
        //
        //   LAMDA = YYAV * C0 / (C0 + YRRSQ * (XXAV-YYAV))
        double lamda  = 0.0 ;
        double dlamda = 0.0 ;
        //
        double chiscl = wsum * rscale * rscale ;
        double dlamax = 0.001 / chiscl ;
        
        double p, pd ;
        for ( int itry = 1 ; itry <= ntry ; itry++ )
        {
            p      = c0 + lamda * (c1 + lamda * (c2 + lamda * lamda * c4 )) ;
            pd     = (c1 + lamda * (c2d + lamda * lamda * c4d)) ;
            dlamda = -p / pd ;
            lamda  = lamda + dlamda ;
            if (Math.abs(dlamda)<   dlamax) break ;
        }
        
        chi2[0]  = (double)(chiscl * lamda) ;
        // double dchisq = chiscl * dlamda ;
        //
        //-->  NOW CALCULATE THE MATRIX ELEMENTS FOR ALPHA, BETA & KAPPA
        //
        double h11   = xxav  -     lamda ;
        double h14   = xrrav ;
        double h22   = yyav  -     lamda ;
        double h24   = yrrav ;
        double h34   = 1.0   + 2.0*lamda ;
        if ( h11 == 0.0 || h22 == 0.0 )
        {
            log( " Problems fitting a circle " ) ;
            return 1 ;
        }
        double rootsq = (h14*h14)/(h11*h11) + 4.0*h34 ;
        
        double ratio, kappa, beta ;
        if ( Math.abs(h22) > Math.abs(h24) )
        {
            ratio  = h24 / h22 ;
            rootsq = ratio * ratio + rootsq ;
            kappa = 1.0 / Math.sqrt(rootsq) ;
            beta  = - ratio * kappa ;
        }
        else
        {
            ratio  = h22 / h24 ;
            rootsq = 1.0 + ratio * ratio * rootsq ;
            beta  = 1.0 / Math.sqrt(rootsq) ;
            if ( h24 > 0 ) beta = - beta ;
            kappa = -ratio * beta ;
        }
        double alpha = - (h14/h11) * kappa ;
        //
        //-->  transform these into the lab coordinate system
        //-->  first get kappa and back to real dimensions
        //
        double kappa1 = kappa / rscale ;
        double dbro   = 0.5   / kappa1 ;
        //
        //-->  next rotate alpha and beta and scale
        //
        double alphar = (cosrot * alpha - sinrot * beta)* dbro ;
        double betar  = (sinrot * alpha + cosrot * beta)* dbro ;
        //
        //-->  then translate by (xav,yav)
        //
        double acent  = (double)(xav - alphar) ;
        double bcent  = (double)(yav - betar ) ;
        double radius = (double)dbro ;
        //
        //   Get charge
        //
        q = (short)( ( yrrav < 0 ) ? 1 : -1 ) ;
        //
        //    Get other track parameters
        //
        double x0, y0 ;
        if ( getPara().vertexConstrainedFit )
        {
            flag = 1 ; // primary track flag
            x0   = getPara().xVertex ;
            y0   = getPara().yVertex ;
            phi0 = getPara().phiVertex ;
            r0   = getPara().rVertex ;
        }
        else
        {
            FtfBaseHit lHit = lastHit ;
            flag =  0 ; // primary track flag
            x0   =  lHit.x  ;
            y0   =  lHit.y  ;
            phi0 =  Math.atan2(lHit.y,lHit.x);
            if ( phi0 < 0 ) phi0 += 2.*Math.PI ;
            r0   =  Math.sqrt( lHit.x * lHit.x + lHit.y * lHit.y )  ;
        }
        //
        psi  = (double)Math.atan2(bcent-y0,acent-x0) ;
        psi  = psi + q * 0.5F * Math.PI ;
        if ( psi < 0 ) psi = psi + 2.*Math.PI ;
        pt   = (double)(2.9979e-3 * getPara().bField * radius ) ;
        //
        //    Get errors from fast fit
        //
        if ( getPara().getErrors ) getErrorsCircleFit( acent, bcent, radius ) ;
        //
        
        return 0 ;
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //    End Fit Circle
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        //    Fit Line in s-z plane
        //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        
    }
    public    int         fitLine    (  )
    {
        //
        //     initialization
        //
        double sum = 0.F ;
        double ss  = 0.F ;
        double sz  = 0.F ;
        double sss = 0.F ;
        double ssz = 0.F ;
        //
        //     find sum , sums ,sumz, sumss
        //
        double dx, dy ;
        double radius = (double)(pt / ( 2.9979e-3 * getPara().bField ) ) ;
        if ( getPara().vertexConstrainedFit )
        {
            dx   = firstHit.x - getPara().xVertex ;
            dy   = firstHit.y - getPara().yVertex ;
        }
        else
        {
            dx   = firstHit.x - lastHit.x ;
            dy   = firstHit.y - lastHit.y ;
        }
        double localPsi = 0.5F * Math.sqrt( dx*dx + dy*dy ) / radius ;
        double total_s ;
        if ( Math.abs(localPsi) < 1. )
        {
            total_s = 2.0F * radius * Math.asin( localPsi ) ;
        }
        else
        {
            total_s = 2.0F * radius * Math.PI ;
        }
        
        //
        FtfBaseHit previousHit = null  ;
        
        for ( startLoop() ; done() ; nextHit() )
        {
            FtfBaseHit cHit = currentHit ;
            if ( currentHit != firstHit )
            {
                dx   = cHit.x - previousHit.x ;
                dy   = cHit.y - previousHit.y ;
                dpsi = 0.5F * (double)Math.sqrt( dx*dx + dy*dy ) / radius ;
                if ( dpsi > 1.)
                {
                    log("FtfBaseHit::fitLine(): dpsi= "+dpsi);
                    dpsi = 1.;
                }
                cHit.s = (float)(previousHit.s - 2.0F * radius * Math.asin( dpsi ) );
            }
            else
                cHit.s = (float)total_s ;
            
            sum += cHit.wz ;
            ss  += cHit.wz * cHit.s ;
            sz  += cHit.wz * cHit.z ;
            sss += cHit.wz * cHit.s * cHit.s ;
            ssz += cHit.wz * cHit.s * cHit.z ;
            previousHit = cHit ;
        }
        
        double det = sum * sss - ss * ss;
        if ( Math.abs(det) < 1e-20)
        {
            chi2[1] = 99999.F ;
            return 0 ;
        }
        //
        //     compute the best fitted parameters A,B
        //
        tanl = (double)((sum * ssz - ss * sz ) / det );
        z0   = (double)((sz * sss - ssz * ss ) / det );
        //
        //     calculate chi-square
        //
        chi2[1] = 0.F ;
        double r1 ;
        for ( startLoop() ; done() ; nextHit() )
        {
            FtfBaseHit cHit = currentHit ;
            r1   = cHit.z - tanl * cHit.s - z0 ;
            chi2[1] += (double) ( (double)cHit.wz * (r1 * r1) );
        }
        //
        //     calculate estimated variance
        //      varsq=chi/(double(n)-2.)
        //     calculate covariance matrix
        //      siga=sqrt(varsq*sxx/det)
        //      sigb=sqrt(varsq*sum/det)
        //
        dtanl = (double) ( sum / det );
        dz0   = (double) ( sss / det );
        
        return 0 ;
    }
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //    End Fit Line
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    public    FtfBaseHit getCurrentHit( )
    {
        return currentHit ;
    }
    public    FtfPara    getPara()
    {
        return para ;
    }
    
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // CIRCOV - a covariance matrix calculation program for circle fitting
    // DESCRIPTION:
    // Compute the covariance matrix of an effective circle fitting algorithm
    // The circle equation is (X(I)-A)**2 + (Y(I)-B)**2 = R**2.
    // The functional minimum is W(I)*[(X(I)-A)**2+(Y(I)-B)**2-R*R]**2/(R*R)
    // For details about the algorithm, see
    // N.I. CHERNOV, G.A. OSOSKOV, COMPUT. PHYS. COMMUN. 33(1984) 329-333
    // INPUT ARGUMENTS: */
    //      A              - Best fitted circle center in X axis, REAL
    //      B              - Best fitted circle center in Y axis, REAL
    //      R              - Best fitted radius                   REAL
    //
    // From a routine written in Fortran by  AUTHOR:
    //  Jawluen Tang, Physics department , UT-Austin
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    
    public    int         getErrorsCircleFit( double a, double b, double r )
    {
        double[] h = new double[9];
        double dx, dy ;
        double h11, h22, h33 ;
        int j ;
        double ratio, c1, s1;
        double hyp;
        
        
        //
        //    If circle fit was not used the
        //    errors in the real space need to
        //    be calculated
        //
        if ( pt < getPara().ptMinHelixFit )
        {
            for ( startLoop() ; done() ; nextHit() )
            {
                
                FtfBaseHit cHit = currentHit ;
                cHit.wxy = 1.0F/ (float)(cHit.dx*cHit.dx + cHit.dy*cHit.dy) ;
            }
        }
        //
        //    Loop over points in fit
        //
        for ( startLoop() ; done() ; nextHit() )
        {
            FtfBaseHit cHit = currentHit ;
            dx = cHit.x - a;
            dy = cHit.y - b;
            hyp = (double)Math.sqrt( dx * dx + dy * dy );
            s1 = dx / hyp;
            c1 = dy / hyp;
            ratio = r / hyp;
            h[0] += cHit.wxy * (ratio * (s1 * s1 - 1) + 1);
            h[1] += cHit.wxy * ratio * s1 * c1;
            h[2] += cHit.wxy * s1;
            h[4] += cHit.wxy * (ratio * (c1 * c1 - 1) + 1);
            h[5] += cHit.wxy * c1;
            h[8] += cHit.wxy ;
        }
        h[3]  = h[1];
        h[6]  = h[2];
        h[7]  = h[5];
        double[] hxx = new double[3];
        FtfUtil.ftfMatrixDiagonal  ( h, hxx ) ;
        h11 = hxx[0];
        h22 = hxx[1];
        h33 = hxx[2];
        
        //
        //   Calculate pt error now
        //
        dpt          = (double)(2.9979e-3 * getPara().bField * h33 );
        //
        //     Get error in psi now
        //
        if ( getPara().vertexConstrainedFit )
        {
            dx = a ;
            dy = b ;
        }
        else
        {
            dx = lastHit.x + a - firstHit.x ;
            dy = lastHit.y + b + firstHit.y ;
        }
        double w   = dy / dx ;
        dpsi  = (double)(( 1. / ( 1. + w*w ) ) * ( h22 / dx - dy * h11 / ( dx*dx ) )) ;
        
        return 0 ;
    }
    
/*:>--------------------------------------------------------------------
 **: METHOD:   Calculates trajectory length between two points on a track
 **:
 **: AUTHOR:     ppy - P.P. Yepes,  yepes@rice.edu
 **: ARGUMENTS:
 **:          IN:
 **:             track           - Track pointer
 **:             x1, y1          - Point 1
 **:             x2, y2          - Point 2
 **:         OUT:
 **:
 **: RETURNS:    0=ok, <>0 error
 **:>------------------------------------------------------------------*/
    
    public    double   arcLength       ( double x1, double y1, double x2, double y2 )
    {
        double x0, y0, xc, yc, rc ;
        double angle_1, angle_2, d_angle, sleng_xy, sleng ;
/*----------------------------------------------------------
       Get track parameters
----------------------------------------------------------*/
        
        x0   = r0 * Math.cos(phi0) ;
        y0   = r0 * Math.sin(phi0) ;
        rc   = pt / ( FtfGeneral.bFactor * getPara().bField )  ;
        double tPhi0 = psi + (double)q * 0.5 * Math.PI / Math.abs((double)q) ;
        xc   = x0 - rc * Math.cos(tPhi0) ;
        yc   = y0 - rc * Math.sin(tPhi0) ;
/*
    Get angle difference
 */
        angle_1  = Math.atan2( (y1-yc), (x1-xc) ) ;
        if ( angle_1 < 0 ) angle_1 = angle_1 + 2. * Math.PI ;
        angle_2  = Math.atan2( (y2-yc), (x2-xc) ) ;
        if ( angle_2 < 0 ) angle_2 = angle_2 + 2. * Math.PI ;
        d_angle  = (double)q * ( angle_1 - angle_2 ) ;
        d_angle  = Math.IEEEremainder( d_angle, 2. * Math.PI ) ;
        if ( d_angle < 0 ) d_angle = d_angle + 2. * Math.PI ;
/*----------------------------------------------------------
       Get total angle and total trajectory
----------------------------------------------------------*/
        sleng_xy = Math.abs( rc ) * d_angle ;
        sleng    = sleng_xy * Math.sqrt( 1.0 + tanl * tanl )  ;
        return sleng ;
    }
    
    
/*:>--------------------------------------------------------------------
 **: METHOD:   Finds point of closest approach
 **:
 **: AUTHOR:     ppy - P.P. Yepes,  yepes@rice.edu
 **: ARGUMENTS:
 **:          IN:    xBeam, yBeam: beam position
 **:
 **: RETURNS:
 **:             tHit            - Point closest approach to center
 *:>------------------------------------------------------------------*/
    
    public    Ftf3DHit closestApproach( double xBeam, double yBeam )
    {
        //	return new Ftf3DHit();
        double rc=0.;
        double xc =0.;
        double yc=0. ;
        return getClosest( xBeam, yBeam, rc, xc, yc ) ;
        
    }
/*:>--------------------------------------------------------------------
 **: METHOD:   Extrapolates track to cylinder with radius r
 **:
 **:
 **: AUTHOR:     ppy - P.P. Yepes,  yepes@rice.edu
 **: ARGUMENTS:
 **:          IN:
 **:             track           - Global track pointer
 **:             r               - Cylinder radius
 **:         OUT:
 **:             x,y,z           - Extrapolated point
 **:             xc,yc,rr        - Center and radius track circle in x-y plane
 **:
 **: RETURNS:    0=ok, <>0 error
 **:>------------------------------------------------------------------*/
    
    public    Ftf3DHit extraRadius     ( double r )
    {
        double phi =0.;
        //
        // Default values
        //
        double x, y, z, rc, xc, yc ;
        x = y = z = 0.F ;
        rc = xc = yc = 0.F;
        //
        //    If error return with error
        //
        Ftf3DHit tHit = new Ftf3DHit(0,0,0) ;
        // r, phi, z, rc, xc, yc
        double[] vars = new double[6];
        if ( extraRCyl(vars) ) return tHit ;
        r = vars[0];
        phi = vars[1];
        z = vars[2];
        rc = vars[3];
        xc = vars[4];
        yc = vars[5];
        //
        //   Otherwise get point in cartesian coordinates
        //
        x = r * Math.cos(phi) ;
        y = r * Math.sin(phi) ;
        tHit.x = (float)x ;
        tHit.y = (float)y ;
        tHit.z = (float)z ;
        
        return tHit ;}
/*:>--------------------------------------------------------------------
 **: METHOD:   Extrapolates track to cylinder with radius r
 **:
 **:
 **: AUTHOR:     ppy - P.P. Yepes,  yepes@rice.edu
 **: ARGUMENTS:
 **:          IN:
 **:             r               - Cylinder radius
 **:         OUT:
 **:             phi,z           - Extrapolated point
 **:             xc,yc,rc        - Center and radius track circle in x-y plane
 **:
 **: RETURNS:    0=ok, <>0 error
 **:>------------------------------------------------------------------*/
    
    public    boolean      extraRCyl       ( double[] vars )
    {
        double r = vars[0];
        double phi = vars[1];
        double z = vars[2];
        double rc = vars[3];
        double xc = vars[4];
        double yc = vars[5];
        double td  ;
        double fac1,sfac, fac2,deltat ;
        //--------------------------------------------------------
        //     Get track parameters
        //--------------------------------------------------------
        double tPhi0 = psi + (double)q * 0.5 * Math.PI / Math.abs((double)q) ;
        double x0    = r0 * Math.cos(phi0) ;
        double y0    = r0 * Math.sin(phi0) ;
        rc    = Math.abs(pt) / ( FtfGeneral.bFactor * getPara().bField )  ;
        xc    = x0 - rc * Math.cos(tPhi0) ;
        yc    = y0 - rc * Math.sin(tPhi0) ;
        //
        //     Check helix and cylinder intersect
        //
        fac1 = xc*xc + yc*yc ;
        sfac = Math.sqrt( fac1 ) ;
        //
        //  If they don't intersect return
        //  Trick to solve equation of intersection of two circles
        //  rotate coordinates to have both circles with centers on x axis
        //  pretty simple system of equations, then rotate back
        //
        if ( Math.abs(sfac-rc) > r || Math.abs(sfac+rc) < r )
        {
            //    printf ( "particle does not intersect \n" ) ;
            return  false ;
        }
        //
        //     Find intersection
        //
        fac2   = ( r*r + fac1 - rc*rc) / (2.00 * r * sfac ) ;
        phi    = Math.atan2(yc,xc) + (float)q*Math.acos(fac2) ;
        td     = Math.atan2(r*Math.sin(phi) - yc,r*Math.cos(phi) - xc) ;
        
        //   double xx = x0 + rc * cos(phi);
        //   double yy = x0 + rc * cos(phi);
        //   double ttphi = atan2((yy-yc),(xx-xc));
        //   double ppsi = ttphi - double(q) * 0.5 * pi / fabs((double)q) ;
        
        //    Intersection in z
        
        if ( td < 0 ) td = td + 2. * Math.PI ;
        deltat = Math.IEEEremainder((-q*td + q*tPhi0),2*Math.PI) ;
        
        // if ( deltat < 0.      ) deltat += 2. * pi ;
        // if ( deltat > 2.*pi ) deltat -= 2. * pi ;
        z = z0 + rc * tanl * deltat ;
        //
        //    That's it
        //
        vars[0] = r;
        vars[1] = phi;
        vars[2] = z;
        vars[3] = rc;
        vars[4] = xc;
        vars[5] = yc;
        return true;
    }
/*:>--------------------------------------------------------------------
 **: METHOD:   Calculates intersection of track with plane define by line
 **:           y = a x + b and the z
 **:
 **: AUTHOR:     ppy - P.P. Yepes,  yepes@rice.edu
 **: ARGUMENTS:
 **:          IN:
 **:             a, b            - Line parameters
 **:         OUT:
 **:             crossPoint      - intersection point
 **:
 **: RETURNS:    0=ok, <>0 track does not cross the plane
 **:>------------------------------------------------------------------*/
    
    public    int      intersectorZLine    ( double a, double b, Ftf3DHit cross )
    {
        //
        //   Calculate circle center and radius
        //
        double x0    = r0 * Math.cos(phi0) ;
        double y0    = r0 * Math.sin(phi0) ;
        double trackPhi0 = psi + q * 0.5 * Math.PI / Math.abs((double)q) ;
        double rc   = pt  / ( FtfGeneral.bFactor * bField )  ;
        double xc   = x0 - rc * Math.cos(trackPhi0) ;
        double yc   = y0 - rc * Math.sin(trackPhi0) ;
        
        double ycPrime = yc - b ;
        double aa = ( 1. + a * a ) ;
        double bb = -2. * ( xc + a * ycPrime ) ;
        double cc = ( xc * xc + ycPrime * ycPrime - rc * rc ) ;
        
        double racine = bb * bb - 4. * aa * cc ;
        if ( racine < 0 ) return 1 ;
        double rootRacine = Math.sqrt(racine) ;
        
        double oneOverA = 1./aa;
        //
        //   First solution
        //
        double x1 = 0.5 * oneOverA * ( -1. * bb + rootRacine ) ;
        double y1 = a * x1 + b ;
        double r1 = Math.sqrt(x1*x1+y1*y1);
        //
        //   Second solution
        //
        double x2 = 0.5 * oneOverA * ( -1. * bb - rootRacine ) ;
        double y2 = a * x2 + b ;
        double r2 = Math.sqrt(x2*x2+y2*y2);
        //
        //    Choose close to (0,0)
        //
        double xHit ;
        double yHit ;
        if ( r1 < r2 )
        {
            xHit = x1 ;
            yHit = y1 ;
        }
        else
        {
            xHit = x2 ;
            yHit = y2 ;
        }
        //-------------------------------------------------------------------
        //     Get the z coordinate
        //-------------------------------------------------------------------
        double angle  = Math.atan2( (yHit-yc), (xHit-xc) ) ;
        if ( angle < 0. ) angle = angle + 2.0 * Math.PI ;
        //   printf ( " angle %f trackPhi0 %f \n ", angle, trackPhi0 ) ;
        double dangle = angle  - trackPhi0  ;
        dangle = Math.IEEEremainder( dangle, 2.0 * Math.PI ) ;
        if ( (q * dangle) > 0 ) dangle = dangle - q * 2. * Math.PI  ;
        
        double stot   = Math.abs(dangle) * rc ;
        double zHit   = z0 + stot * tanl ;
        //
        cross.set( (float)xHit, (float)yHit, (float)zHit ) ;
        //
        return 0 ;
    }
/*:>--------------------------------------------------------------------
 **: METHOD:   Finds point of closest approach
 **:
 **: AUTHOR:     ppy - P.P. Yepes,  yepes@rice.edu
 **: ARGUMENTS:
 **:          IN:    xBeam, yBeam: beam position
 **:         OUT:
 **:                  rc, xc, yc  track circle radius and center
 **:
 **: RETURNS:
 **:             tHit            - Point closest approach to center
 *:>------------------------------------------------------------------*/
    
    public    Ftf3DHit getClosest      ( double xBeam, double yBeam,
            double rc, double xc, double yc )
    {
        double xp, yp, zp ;
        xp = yp = 0. ;
        //--------------------------------------------------------
        //     Get track parameters
        //--------------------------------------------------------
        double tPhi0 = psi + (double)q * 0.5 * Math.PI / Math.abs((double)q) ;
        
        double x0   = r0 * Math.cos(phi0) ;
        double y0   = r0 * Math.sin(phi0) ;
        rc   = pt / ( FtfGeneral.bFactor * getPara().bField )  ;
        xc   = x0 - rc * Math.cos(tPhi0) ;
        yc   = y0 - rc * Math.sin(tPhi0) ;
        
        double[] xy = new double[2];
        getClosest( xBeam, yBeam, rc, xc, yc, xy ) ;
        xp = xy[0];
        yp = xy[1];
        
        //-----------------------------------------------------------------
        //     Get the z coordinate
        //-----------------------------------------------------------------
        double angle  = Math.atan2( (yp-yc), (xp-xc) ) ;
        if ( angle < 0. ) angle = angle + 2.0 * Math.PI ;
        
        double dangle = angle  - tPhi0  ;
        dangle = Math.IEEEremainder( dangle, 2.0 * Math.PI ) ;
        if ( Math.abs(dangle) < 1.e-10 ) dangle = 0 ; // Problems with -0.000 values
        if ( ( (float)q * dangle) < 0 )
            dangle = dangle + (float)q * 2. * Math.PI ;
        
        double stot   = Math.abs(dangle) * rc ;
        zp   = z0 - stot * tanl ;
        
        
        return new Ftf3DHit((float)xp,(float)yp,(float)zp) ;
    }
    
    public    int      getClosest      ( double xBeam, double yBeam,
            double rc, double xc, double yc,
            double[] xyClosest )
    {
        //----------------------------------------------------------
        //     Shift center to respect beam axis
        //----------------------------------------------------------
        double dx = xc - xBeam ;
        double dy = yc - yBeam ;
        //----------------------------------------------------------
        //     Solve the equations
        //----------------------------------------------------------
        double fact = rc / Math.sqrt( dx * dx + dy * dy ) ;
        double f1   = 1. + fact ;
        double f2   = 1. - fact ;
        
        double dx1 = dx * f1 ;
        double dy1 = dy * f1 ;
        double d1 = Math.sqrt( dx1 * dx1 + dy1 * dy1 ) ;
        
        double dx2 = dx * f2 ;
        double dy2 = dy * f2 ;
        double d2 = Math.sqrt( dx2 * dx2 + dy2 * dy2 ) ;
        //---------------------------------------------------------------
        //     Choose the closest
        //---------------------------------------------------------------
        if ( d1 < d2 )
        {
            xyClosest[0] = dx1 + xBeam ;
            xyClosest[1] = dy1 + yBeam ;
        }
        else
        {
            xyClosest[0] = dx2 + xBeam ;
            xyClosest[1] = dy2 + yBeam ;
        }
        return 0 ;						   }
    
/*:>--------------------------------------------------------------------
 **: METHOD:   Updates track parameters to point of intersection with
 **:           cylinder of radius r
 **:
 **:
 **: AUTHOR:     ppy - P.P. Yepes,  yepes@rice.edu
 **: ARGUMENTS:
 **:          IN:
 **:             radius         - Cylinder radius to extrapolate track
 **:         OUT:
 **:
 **:>------------------------------------------------------------------*/
    
    public    void     updateToRadius  ( double radius )
    {
        double phiExtra, zExtra, rCircle, xCircleCenter, yCircleCenter ;
        double[] vars = new double[6];
        vars[0] = radius;
        boolean ok = extraRCyl( vars );
        
        radius = vars[0];
        phiExtra = vars[1];
        zExtra = vars[2];
        rCircle = vars[3];
        xCircleCenter = vars[4];
        yCircleCenter  = vars[5];
        if ( !ok )
        {
            //    printf ( "FtfBaseTrack::updateToRadius: track %d does not intersect radius %f\n",
            //              id, radius ) ;
            return ;
        }
        
        double xExtra = radius * Math.cos(phiExtra) ;
        double yExtra = radius * Math.sin(phiExtra) ;
        
        double tPhi = Math.atan2(yExtra-yCircleCenter,xExtra-xCircleCenter);
        
        // if ( tPhi < 0 ) tPhi += 2. * pi ;
        
        double tPsi = tPhi - (double)q * 0.5 * Math.PI / Math.abs((double)q) ;
        if ( tPsi > 2. * Math.PI ) tPsi -= 2. * Math.PI ;
        if ( tPsi < 0.        ) tPsi += 2. * Math.PI ;
        //
        //    Update track parameters
        //
        r0   = radius ;
        phi0 = phiExtra ;
        z0   = zExtra ;
        psi  = tPsi ;
        
    }
/*:>--------------------------------------------------------------------
 **: METHOD:   Updates track parameters to point of closest approach
 **:
 **:
 **: AUTHOR:     ppy - P.P. Yepes,  yepes@rice.edu
 **: ARGUMENTS:
 **:          IN:
 **:             xBeam          - x Beam axis
 **:             yBeam          - y Beam axis
 **:
 **:>------------------------------------------------------------------*/
    
    public    void     updateToClosestApproach( double xBeam, double yBeam )
    {
        double rc=0.;
        double xc = 0.;
        double yc=0. ;
        Ftf3DHit closest = getClosest( xBeam, yBeam, rc, xc, yc ) ;
        //
        double tPhi = Math.atan2(closest.y-yc,closest.x-xc);
        
        // if ( tPhi < 0 ) tPhi += 2. * Math.PI ;
        
        double tPsi = tPhi - (double)q * 0.5 * Math.PI / Math.abs((double)q) ;
        if ( tPsi > 2. * Math.PI ) tPsi -= 2. * Math.PI ;
        if ( tPsi < 0.        ) tPsi += 2. * Math.PI ;
        //
        //   Update track parameters
        //
        r0   = Math.sqrt(closest.x*closest.x+closest.y*closest.y) ;
        phi0 = Math.atan2(closest.y,closest.x) ;
        if ( phi0 < 0 ) phi0 += 2. * Math.PI ;
        z0   = closest.z ;
        psi  = tPsi ;
    }
/*:>--------------------------------------------------------------------
 **: METHOD:   Phi rotates the track
 **:
 **: AUTHOR:     ppy - P.P. Yepes,  yepes@rice.edu
 **: ARGUMENTS:
 **:          IN:
 **:             deltaPhi        - Angle to rotate in phi
 **:
 **: RETURNS:    0=ok, <>0 error
 **:>------------------------------------------------------------------*/
    
    public    int      phiRotate       ( double deltaPhi )
    {
        phi0 += deltaPhi ;
        if ( phi0 > 2. * Math.PI ) phi0 -= 2. * Math.PI ;
        if ( phi0 <         0 ) phi0 += 2. * Math.PI ;
        psi  += deltaPhi ;
        if ( psi > 2. * Math.PI ) psi -= 2. * Math.PI ;
        if ( psi <         0 ) psi += 2. * Math.PI ;
        
        return 0 ;
    }
    
    public  void startLoop( )
    { currentHit = firstHit ; }
    {
    }
    public void nextHit  ( )
    {
    }
    public  boolean  done     ( )
    { return currentHit== null ; }
    public    void       Print       ( int level )
    {
    }
    
    
    
}

