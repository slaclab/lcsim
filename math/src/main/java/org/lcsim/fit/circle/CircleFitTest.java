package org.lcsim.fit.circle;
// Circle Fitting Classes based on Karimaki's fast circle fit.
/*
$!       **************************************************
$!       *          CIRCLE FIT SAMPLE PROGRAM             *
$!       **************************************************
$ CREATE CIRCLEFIT.FOR
 ******************************************************************
      PROGRAM CIRCLEFIT
 *     ******* *********            4.6.1991   V. Karimaki        *
 *                                                                *
 *     Main program to test the circle fitting routine CIRCLF     *
 *                                                                *
 *     Circle data points are Monte Carlo generated and then      *
 *     used to test CIRCLF NTRGEN times. The pull values are      *
 *     calculated for the three fitted parameters.                *
 *                                                                *
 *     The test is passed, if the statistical distributions of    *
 *     the pull values follow the normal distribution. The means  *
 *     and standard deviations of the three pull values are       *
 *     printed in the test run output.                            *
 *                                                                *
 *     Constants (units are supposed to be in metres):            *
 *     AVEDEV = average residual                                  *
 *     STEP   = spacing between points along circle               *
 *     RHOMAX = maximal (absolute) value of curvature             *
 *     DISRMX = maximal ratio |DCA|/track length                  *
 *----------------------------------------------------------------*
 */
import java.util.Random;

class CircleFitTest
{
    double[] _xx; //array of simulated x points
    double[] _yy; //array of simulated y points
    double[] _wxy; //array of weights
    double[] _xyr; //array of radial positions
    Random _r;
    int _ncmax;
    CircleFitter _fitter;
    
    double[] _pulls;
    double[][] _pulsum;
    double[] _pullerr;
    
    // Constructor
    CircleFitTest()
    {
        _r = new Random();
        _fitter = new CircleFitter();
        _ncmax = 500;
        _xx = new double[_ncmax];
        _yy = new double[_ncmax];
        _wxy = new double[_ncmax];
        _xyr = new double[_ncmax];
        _pulls = new double[3];
        _pulsum = new double[2][3];
        _pullerr = new double[3];
        double tperpo[] = new double[2];
        double nptest[] = new double[4];
    }
    //      PARAMETER (NRAND=1000)
    //      COMMON/RANTAB/ LUNI,LGAU,UNITAB(NRAND),GAUTAB(NRAND)
    //      PARAMETER (NCMAX=500)
    //      COMMON /CIRPOI/ XX(NCMAX),YY(NCMAX),WXY(NCMAX),XYR(NCMAX)
    //      COMMON /CIRCFI/ RHO,PHI,DCA,CHICIR,XPCA,YPCA,COVRFD(6)
    //     +,          XX0,YY0,S1,S2,S3,S4,S5,S6,S7,S8,S9
    //      REAL*8          S1,S2,S3,S4,S5,S6,S7,S8,S9
    //      DIMENSION PULLS(3),PULSUM(2,3),PULERR(3)
    //      DIMENSION TPERPO(2),NPTEST(4)
    //      DATA NPTEST /4,20,100,500/
    //      DATA AVEDEV /.00030/,    STEP/0.010/
    //      DATA RHOMAX,DISRMX/ 1.00, 0.40/
    //      PI2=8.*ATAN(1.)
    
    
    void test()
    {
        //
        //--- Number of generations
        //
        int NTRGEN=2000;
        int NPRINT=10;
        int NMODUL=NTRGEN/NPRINT;
        
        double step = 0.01;
        double AVEDEV = 0.00030;
        double RHOMAX = 1.0;
        double DISRMX=0.40;
        
        System.out.println("TEST RUN OUTPUT");
        System.out.println("Circle fit routine is tested with "+NTRGEN+
                " randomly generated sets of circle data.");
        //
        //--- Loop to generate circle points and test the routine CIRCLF
        //
        //DO 100 ITR=1,NTRGEN
        for(int ITR=0; ITR<NTRGEN; ++ITR)
        {
            //--- Generate random curvature, direction and dca
            
            double RND1=_r.nextDouble();
            double RND2=_r.nextDouble();
            double RND3=_r.nextDouble();
            double RND4=_r.nextDouble();
            
            double RHOGEN=2.*RHOMAX*(RND1-0.5);
            double PHIGEN=2.*Math.PI*RND2;
            double AROGEN=Math.abs(RHOGEN);
            //
            //--- Number of points on circle  4<=NPO<=300
            //
            int NPO=4+(int)(296*RND4);
            double XLENG=(NPO-1)*step;
            double DISGEN=DISRMX*2.*XLENG*(RND3-0.5);
            if (AROGEN>0.)
            {
                double RADIUS=1./AROGEN;
                double DISLIM=0.75*RADIUS;
                if (Math.abs(DISGEN)>DISLIM)
                {
                    DISGEN = DISLIM;
                    if(DISGEN<0) DISGEN = -DISLIM;
                }
                int NPFULL=(int)(6.2832*RADIUS/step-2);
                NPO=Math.min(NPO,NPFULL);
            }
            double XREFE=RND1-0.5;
            double YREFE=RND2-0.5;
            //CNG
            // if I fix these parameters, and calculate pulls
            // with respect to these fixed parameters, everything
            // is fine. When I don't everything goes to hell.
            // Error must be in coding of modifications, or in propagate
            // method.
            // May, 18, 2000
            // I have now fixed the propagate method.
            // Everything works well if I fix these starting parameters
            // and propagate to a random XTEST, YTEST
                        /*
                        NPO = 40;
                        RHOGEN = 0.01;
                        PHIGEN = 1.23;
                        DISGEN = 0.1;
                        XREFE = 0.;
                        YREFE = 0.;
                         */
            //CNG
            double SINGEN=Math.sin(PHIGEN);
            double COSGEN=Math.cos(PHIGEN);
            double XSTART= XREFE+DISGEN*SINGEN;
            double YSTART= YREFE-DISGEN*COSGEN;
            //
            //--- Generate a circle with random parameters
            //
            simCirclePoints(RHOGEN,PHIGEN,XSTART,YSTART,NPO,AVEDEV,step);
            //Check the generation here...
            //System.out.println("phigen= "+PHIGEN+", rhogen= "+RHOGEN+", dca= "+DISGEN);
                        /*for(int i = 0; i<NPO; ++i)
                         {
                                System.out.println("_xx["+i+"]= "+_xx[i]+", _yy["+i+"]= "+_yy[i]+", _wxy["+i+"]= "+_wxy[i]);
                         }
                         */
            
            //
            //--- Perform circle fitting with error estimation
            //
            //         CALL CIRCLF(XREFE,YREFE,XX,YY,WXY,NPO,1,IER)
            _fitter.setreferenceposition( XREFE, YREFE);
            //System.out.println("xref= "+XREFE+", yref= "+YREFE);
            boolean OK = _fitter.fit( _xx, _yy, _wxy,NPO);
            //if(OK) System.out.println("Fit OK");
            CircleFit cf = _fitter.getfit();
            //System.out.println(cf);
            
            //
            //--- Define also another reference point to test propagation
            double XTEST=XSTART+DISGEN*(RND3-0.5);
            double YTEST=YSTART+DISGEN*(RND4-0.5);
            //
            //--- Calculate the true parameters at the propagation test point
            //
            double XMOVE=XREFE-XTEST;
            double YMOVE=YREFE-YTEST;
            double ROD1=1.+RHOGEN*DISGEN;
            double DPERP=XMOVE*SINGEN-YMOVE*COSGEN + DISGEN;
            double DPARA=XMOVE*COSGEN+YMOVE*SINGEN;
            double AA=2.*DPERP+RHOGEN*(DPERP*DPERP+DPARA*DPARA);
            double UU=Math.sqrt(1.+RHOGEN*AA);
            double SQ1AI=1./(1.+UU);
            double BB= RHOGEN*XMOVE+ROD1*SINGEN;
            double CC=-RHOGEN*YMOVE+ROD1*COSGEN;
            double RHOTES=RHOGEN;
            double PHITES=Math.atan2(BB,CC);
            double DISTES=AA*SQ1AI;
            //
            //--- Propagate to a test point
            //
            _fitter.propagatefit(XTEST, YTEST);
            cf = _fitter.getfit();
            
            double FIC=cf.phi();
            if(PHITES-cf.phi()>Math.PI) FIC+=2.*Math.PI;
            if(cf.phi()-PHITES>Math.PI) FIC-=2.*Math.PI;
            double[] covmat = cf.cov();
            double ROERR=Math.sqrt(covmat[0]);
            double FIERR=Math.sqrt(covmat[2]);
            double DCERR=Math.sqrt(covmat[5]);
            //
            //--- Print a few fit results
            //
                        /*
                        IF (MOD(ITR,NMODUL).EQ.1) THEN
                        WRITE(IUNIT,1005) RHO,ROERR,FIC,FIERR,DCA,DCERR,CHICIR,NPO
                        WRITE(IUNIT,1006) RHOTES,PHITES,DISTES
                        1005       FORMAT('    Fitted',3(2X,F7.4,'+-',F6.4),4X,F5.1,I6)
                        1006       FORMAT('    True  ',3(2X,F7.4,8X))
                        ENDIF
                         */
            //
            //--- Calculate the pull values
            //
            // use when I have implemented propagate...
            _pulls[0]=(RHOTES-cf.curvature())/ROERR;
            _pulls[1]=(PHITES-FIC)/FIERR;
            _pulls[2]=(DISTES-cf.dca())/DCERR;
            
            for(int i =0; i<3;++i)
            {
                //	System.out.println("pulls["+i+"]= "+_pulls[i]);
                _pulsum[0][i]+=_pulls[i];
                _pulsum[1][i]+=_pulls[i]*_pulls[i];
            }
            
        }
        
        //
        //--- Test run output
        //
        double FACTO=1./Math.sqrt((double)(NTRGEN));
        //
        //--- Calculate means and std's of the pull values
        //
        System.out.println("Mean +/- error and standard deviation of pulls");
        for (int i=0; i<3; ++i)
        {
            _pulsum[0][i]=_pulsum[0][i]/(double)NTRGEN;
            _pulsum[1][i]=Math.sqrt(_pulsum[1][i]/((double)NTRGEN)-_pulsum[0][i]*_pulsum[0][i]);
            _pullerr[i]=FACTO*_pulsum[1][i];
            System.out.println("Mean: "+_pulsum[0][i]+" +/- "+_pullerr[i]+" sigma= "+_pulsum[1][i]);
        }
                /*
                WRITE(IUNIT,1010)
                1010 FORMAT(///,17X,'Means and standard deviations of the pull values
                +,//,20X,'Circle parameter','   Pull mean       Pull std',/)
                WRITE(IUNIT,1011) pulsum(1,1),pullerr(1),pulsum(2,1)
                WRITE(IUNIT,1012) pulsum(1,2),pullerr(2),pulsum(2,2)
                WRITE(IUNIT,1013) pulsum(1,3),pullerr(3),pulsum(2,3)
                1011 FORMAT(22X,'Curvature',5X,F7.3,'+-',F5.3,5X,F6.3)
                1012 FORMAT(22X,'Angle    ',5X,F7.3,'+-',F5.3,5X,F6.3)
                1013 FORMAT(22X,'Distance ',5X,F7.3,'+-',F5.3,5X,F6.3)
                //
                //--- Means and std's should be those of the normal distribution
                //
                IOK=1
                DO 140 IPL=1,3
                TEST=ABS(pulsum(1,IPL)/pullerr(IPL))
                IF (TEST.GT.3.) IOK=0
                TEST=ABS(pulsum(2,IPL)-1.)
                IF (TEST.GT..2) IOK=0
                140 CONTINUE
                IF (IOK.EQ.1) WRITE(IUNIT,1020)
                IF (IOK.EQ.0) WRITE(IUNIT,1021)
                1020 FORMAT(/,16X,' Pulls test is passed OK.')
                1021 FORMAT(/,16X,' Something wrong: pulls are not normally'
                +,' distributed.')
                 */
                /*
                 for (int i = 0; i<3; ++i)
                 {
                   double TEST = Math.abs(_pulsum[0][i]/_pullerr[i]);
                System.out.println("Pull Test["+i+"]= "+TEST);
                 }
                 */
        
        //
        //--- Timing tests
        //
        //
        //--- Generate a circle with random parameters with 500 points
        //
        simCirclePoints(.01,1.23,0.009,0.0033,500,0.0003,0.01);
        int[] nptest = {4, 20, 100, 500};
        long[] times = new long[nptest.length];
        _fitter.setreferenceposition( 0., 0.);
        //Fit this circle a number of times and save times as
        //a function of the number of points fit.
        for(int i=0; i<nptest.length; ++i)
        {
            long t1 = System.currentTimeMillis();
            int ndo = 5000;
            for(int j=0; j<ndo; ++j)
            {
                boolean OK = _fitter.fit( _xx, _yy, _wxy,nptest[i]);
            }
            times[i] = System.currentTimeMillis()-t1;
            System.out.println("It took "+(double)times[i]/(double)(ndo*nptest[i])+" milliseconds for circles with "+nptest[i]+" hits");
        }
        
                /*
                CALL CIRGEN(RHOGEN,PHIGEN,XSTART,YSTART,500,AVEDEV,STEP)
                WRITE(IUNIT,2000)
                2000 FORMAT(////,16X,' Timing tests WITHOUT/WITH error estimation:'
                +,//,26X,'#Points',5X,'Time per point (us)',/)
                DO 400 JP=1,4
                NPO=NPTEST(JP)
                NDO= 25000/(30+NPO)
                 *
                 *--- II=1 without error estimation II=2 with
                 *
                DO 300 II=1,2
                IF (II.EQ.1) IES=-1
                IF (II.EQ.2) IES=+1
                CALL TIMVX(TSTART)
                TSTART=1000*TSTART
                 *
                 *---- call circle fitting NDO times for timing
                 *
                DO 200 I=1,NDO
                CALL CIRCLF(XREFE,YREFE,XX,YY,WXY,NPO,IES,IER)
                200       CONTINUE
                CALL TIMVX(TEND)
                TEND=1000*TEND
                TPERPO(II)=1000*(TEND-TSTART)/NDO/NPO
                300    CONTINUE
                WRITE(IUNIT,2001) NPO,TPERPO
                2001    FORMAT(26X,I6,7X,F6.1,' /',F6.1)
                400 CONTINUE
                STOP
                END
                 
                 */
    }
    void simCirclePoints(double rho, double phi, double x, double y,
            int npoints, double avedev, double step)
    {
                /*
                 *****************************************************************
                SUBROUTINE CIRGEN(RHO,PHI,X,Y,NPO,AVEDEV,STEP)
                 *                                                               *
                 *     SUBROUTINE TO GENERATE MEASURED POINTS ALONG CIRCLE       *
                 *     POINTS ARE GAUSSIAN FLUCTUATED ABOUT THE TRUE CIRCLE.     *
                 *                                                               *
                 *     THE ROUTINE IS PART OF THE CIRCLE FIT TEST PROGRAM        *
                 *                                                               *
                 *     INPUT PARAMETERS:                                         *
                 *     RHO    = SIGNED CURVATURE (+VE BENDING CLOCKWISE)         *
                 *     PHI    = DIRECTION ANGLE IN XY-PROJECTION                 *
                 *     X,Y    = STARTING POINT COORDINATES                       *
                 *     NPO    = NUMBER OF POINTS TO GENERATE                     *
                 *     AVEDEV  = MEAN DEVIATION NORMAL TO TRACK                  *
                 *     STEP   = STEP LENGTH ALONG TRACK                          *
                 *                                                               *
                 *     OUTPUT PARAMETERS:                                        *
                 *     OUTPUT COORDINATES ARE IN ARRAYS XX,YY                    *
                 * --------------------------------------------------------------*
                 */
                /*
                PARAMETER (NRAND=1000)
                COMMON/RANTAB/ LUNI,LGAU,UNITAB(NRAND),GAUTAB(NRAND)
                PARAMETER (NCMAX=500)
                COMMON /CIRPOI/ XX(NCMAX),YY(NCMAX),WXY(NCMAX),XYR(NCMAX)
                REAL*8 PHIS,HDPHI,CORD,XS,YS
                C-
                 */
        double HDPHI   = -rho*step/2.0;
        double CORD = step;
        //     if(Math.abs(HDPHI)<1.0e-5f) double CORD = step;
        if(Math.abs(HDPHI)>=1.0e-5f) CORD = 2.*Math.sin(-HDPHI)/rho;
        double XS = x;  // x start
        double YS = y;  // y start
        double PHIS = phi; // phi start
        
        // GENERATE POINTS ALONG CIRCLE
        for(int NP=0; NP<npoints; ++NP)
        {
            double AVERES = (0.5+_r.nextDouble())*avedev;
            double RESIXY = _r.nextGaussian()*AVERES;
            
            // FLUCTUATE NORMAL TO TRAJECTORY AND STORE POINT
            _xx[NP] = XS + Math.sin(PHIS)*RESIXY;
            _yy[NP] = YS - Math.cos(PHIS)*RESIXY;
            _wxy[NP]= 1./(AVERES*AVERES);
            _xyr[NP]= RESIXY;
            
            // STEP TO THE NEXT POINT ON CIRCLE
            XS     +=  CORD*Math.cos(PHIS+HDPHI);
            YS     +=  CORD*Math.sin(PHIS+HDPHI);
            PHIS   +=  2.*HDPHI;
        }
        
    }
    public static void main(String[] args)
    {
        CircleFitTest t = new CircleFitTest();
        t.test();
    }
}
