/*
 * TrackVector_Test.java
 *
 * Created on July 24, 2007, 11:55 AM
 *
 * $Id: TrackVector_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import Jama.Matrix;
import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class TrackVector_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of TrackVector_Test */
    public void testTrackVector()
    {
        String ok_prefix = "TrackVector test (I): ";
        String error_prefix = "TrackVector test (E): ";
        
        if(debug) System.out.println( ok_prefix
                + "------- Testing component TrackVector. -------" );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Test constructors and indexing." );
        
        // Create a track vector.
        // Do not change these numbers--they are hardwired below.
        TrackVector tv1 = new TrackVector();
        Assert.assertTrue( tv1.amax() == 0.0 );
        
        tv1.set(0, -1.2);
        tv1.set(1, 2.3);
        tv1.set(2, -3.4);
        tv1.set(3, 4.5);
        tv1.set(4, -5.6);
        if(debug) System.out.println( tv1 );
        
        // Create an error matrix.
        TrackError te1 = new TrackError();
        Assert.assertTrue( te1.amax() == 0.0 );
        te1.set(0,0, 0.1);
        te1.set(0,1, -0.021);
        te1.set(1,1, 0.2);
        te1.set(2,0, 0.031);
        te1.set(2,1, 0.032);
        te1.set(2,2, 0.3);
        te1.set(0,3, -0.041);
        te1.set(1,3, -0.042);
        te1.set(2,3, -0.043);
        te1.set(3,3, 0.4);
        te1.set(4,0, 0.051);
        te1.set(4,1, 0.052);
        te1.set(4,2, 0.053);
        te1.set(4,3, 0.054);
        te1.set(4,4, 0.5);
        if(debug) System.out.println( te1 );
        
        // Create a derivative matrix.
        TrackDerivative td1 = new TrackDerivative();
        if(debug) System.out.println(td1);
        Assert.assertTrue( td1.amax() == 1.0 );
        for ( int i=0; i<5; ++i )
        {
            for ( int j=0; j<5; ++j )
            {
                if ( i == j ) td1.set(i,j, 1.0);
                else td1.set(i,j, 0.1*(i-j));
            }
        }
        if(debug) System.out.println( td1 );
        
        //********************************************************************
        
        // Check min, max, amin and amax.
        if(debug) System.out.println( ok_prefix + "Test max, min, amax, amin" );
        if(debug) System.out.println( "Minimum:  " + tv1.min() + "  " + te1.min()
        + "  " + td1.min() );
        if(debug) System.out.println( "Maximum:  " + tv1.max() + "  " + te1.max()
        + "  " + td1.max() );
        if(debug) System.out.println( "Abs Min:  " + tv1.amin() + "  " + te1.amin()
        + "  " + td1.amin() );
        if(debug) System.out.println( "Abs Max:  " + tv1.amax() + "  " + te1.amax()
        + "  " + td1.amax() );
        Assert.assertTrue( tv1.min() == -5.6 );
        Assert.assertTrue( tv1.max() ==  4.5 );
        Assert.assertTrue( tv1.amin() ==  1.2 );
        Assert.assertTrue( tv1.amax() ==  5.6 );
        Assert.assertTrue( te1.min() == -0.043 );
        Assert.assertTrue( te1.max() ==  0.5 );
        Assert.assertTrue( te1.amin() ==  0.021 );
        Assert.assertTrue( te1.amax() ==  0.5 );
        Assert.assertTrue( td1.min() == -0.1*4 );
        Assert.assertTrue( td1.max() ==  1.0 );
        Assert.assertTrue( td1.amin() ==  0.1*1 );
        Assert.assertTrue( td1.amax() ==  1.0 );
        
        //********************************************************************
        
        // Check assignment and copy.
        if(debug) System.out.println( ok_prefix + "Testing assignment and copy." );
        // vector
        TrackVector tv2 = new TrackVector(tv1);
        TrackVector dv2 = tv2.minus(tv1);
        TrackVector tv3 = new TrackVector(tv1); //
        TrackVector dv3 = tv3.minus(tv2);
        if(debug) System.out.println( tv1 );
        if(debug) System.out.println( tv2 );
        if(debug) System.out.println( tv3 );
        
        Assert.assertTrue( tv2.equals(tv1) );
        Assert.assertTrue( dv2.amax() == 0.0 );
        Assert.assertTrue( tv3.equals(tv1) );
        Assert.assertTrue( dv3.amax() == 0.0 );
        
        // error
        TrackError te2 = new TrackError(te1);
        TrackError de2 = te2.minus(te1);
        TrackError te3 = te1;
        TrackError de3 = te3.minus(te2);
        if(debug) System.out.println( te1 );
        if(debug) System.out.println( te2 );
        if(debug) System.out.println( te3 );
        Assert.assertTrue( te2.equals(te1) );
        Assert.assertTrue( de2.amax() == 0.0 );
        Assert.assertTrue( te3.equals(te1) );
        Assert.assertTrue( de3.amax() == 0.0 );
        
        // derivative
        TrackDerivative td2 = new TrackDerivative(td1);
        TrackDerivative dd2 = td2.minus(td1);
        TrackDerivative td3 = td1;
        TrackDerivative dd3 = td3.minus(td2);
        if(debug) System.out.println( "td1= " + td1 );
        if(debug) System.out.println( "td2= " + td2 );
        if(debug) System.out.println( "td3= " + td3 );
        Assert.assertTrue( td2.equals(td1) );
        Assert.assertTrue( dd2.amax() == 0.0 );
        Assert.assertTrue( td3.equals(td1) );
        Assert.assertTrue( dd3.amax() == 0.0 );
        
        //********************************************************************
        
        // Check additon and subtraction.
        if(debug) System.out.println( ok_prefix + "Testing addition and subtraction." );
        double small = 1.e-10;
        // vector
        if(debug) System.out.println( "tv1= " + tv1 );
        if(debug) System.out.println( "tv2= " + tv2 );
        if(debug) System.out.println( "tv3= " + tv3 );
        TrackVector tv4 = new TrackVector(tv1.plus(tv2));  // = 2*tv1
        TrackVector tv5 = new TrackVector(tv4.minus(tv3).minus(tv2));  // = 0
        if(debug) System.out.println( "tv4= " + tv4 );
        if(debug) System.out.println( "tv5= " + tv5 );
        
        Assert.assertTrue( (tv4.amax()-2.0*5.6) < small );
        if(debug) System.out.println(tv5.amax());
        Assert.assertTrue( tv5.amax() < small );
        
        tv4 = tv4.minus(tv1);  // = tv1
        tv5 = tv5.plus(tv2);
        tv5 = tv5.plus(tv3);  // = 2*tv1
        Assert.assertTrue( (tv5.amax()-2.0*5.6) < small );
        Assert.assertTrue( (tv4.min()+5.6) < small );
        
        // error
        TrackError te4 = new TrackError(te1.plus(te2));
        TrackError te5 = new TrackError(te4.minus(te3).minus(te2));
        Assert.assertTrue( (te4.amax()-2.0*0.5) < small );
        Assert.assertTrue( te5.amax() < small );
        
        te4 = te4.minus(te1);  // = te1
        te5 = te5.plus(te2);
        te5 = te5.plus(te3);  // 2*te1
        Assert.assertTrue( (te5.amax()-2.0*0.5) < small );
        Assert.assertTrue( (te4.min()+0.043) < small );
        
        // derivative
        TrackDerivative td4 = new TrackDerivative(td1.plus(td2));
        TrackDerivative td5 = new TrackDerivative(td4.minus(td3).minus(td2));
        Assert.assertTrue( (td4.amax()-2.0*1.0) < small );
        Assert.assertTrue( td5.amax() < small );
        
        td4 = td4.minus(td1);  // = td1
        td5 = td5.plus(td2);
        td5 = td5.plus(td3);  // 2*td1
        Assert.assertTrue( (td5.amax()-2.0*1.0) < small );
        Assert.assertTrue( (td4.min()+0.4) < small );
        
        //********************************************************************
        
        // Check equality and inequality.
        if(debug) System.out.println( ok_prefix + "Testing equality and inequality" );
        Assert.assertTrue( tv1.equals(tv2) );
        Assert.assertTrue( tv1.notEquals(tv5) );
        Assert.assertTrue( tv1.equals(tv1) );
        Assert.assertTrue( te1.equals(te2) );
        Assert.assertTrue( te1.notEquals(te5) );
        Assert.assertTrue( te1.equals(te1) );
        Assert.assertTrue( td1.equals(td2) );
        Assert.assertTrue( td1.notEquals(td5) );
        Assert.assertTrue( td1.equals(td1) );
        Assert.assertTrue( tv1.isEqual(tv2) );
        Assert.assertTrue( ! tv1.isEqual(tv5) );
        Assert.assertTrue( te1.isEqual(te2) );
        Assert.assertTrue( ! te1.isEqual(te5) );
        Assert.assertTrue( td1.isEqual(td2) );
        Assert.assertTrue( ! td1.isEqual(td5) );
        
        //********************************************************************
        
        // invert matrix
        if(debug) System.out.println( ok_prefix + "Testing inversion." );
        te2 = new TrackError(te1);
        int stat2 = te2.invert();
        te3 = new TrackError(te2);
        int stat3 = te3.invert();
        de3 = te3.minus(te1);
        if(debug) System.out.println( "te1= " + te1 ); //original
        if(debug) System.out.println( "te2= " + te2 ); //original inverse
        if(debug) System.out.println( "te3= " + te3 ); //original (inverse inverse)
        if(debug) System.out.println( "de3= " + de3 ); // original minus inverse inverse
        if ( stat2!=0 || stat3!=0 || de3.amax()>small )
        {
            if(debug) System.out.println( error_prefix + "Invalid error += or -=" );
            System.exit(13);
        }
        
        //********************************************************************
        
        // calculate a chi-square difference
        if(debug) System.out.println( ok_prefix + "Calculating chi-square difference." );
        double diff = TrackVector.chisqDiff(tv1,te3);
        if(debug) System.out.println("chisq_diff= "+diff);
        if ( diff < 0.0 )
        {
            if(debug) System.out.println(error_prefix + "Unable to evaluate difference." );
            System.exit(14);
        }
        
        //********************************************************************
        
        // derivative transpose
        if(debug) System.out.println( "\n"+ok_prefix + "Testing transpose. \n " );
        td2 = new TrackDerivative(td1);
        td2.transpose();
        if(debug) System.out.println( "td1= \n"+td1 );
        if(debug) System.out.println( "td2= \n"+td2 );
        Assert.assertTrue( td2.notEquals(td1) );
        td2.transpose();
        Assert.assertTrue( td2.equals(td1) );
        
        //********************************************************************
        
        // transform
        if(debug) System.out.println( "\n"+ok_prefix + "Testing transform. \n" );
        te2 = new TrackError(te1);
        te2 = te2.Xform(td1);
        if(debug) System.out.println( "te1= \n" + te1 );
        if(debug) System.out.println( "te2= \n" + te2 );
        Assert.assertTrue( te1.notEquals(te2) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Testing fetching underlying representation."
                );
        Matrix uvec = tv1.getMatrix();
        Matrix usma = te1.getMatrix();
        Matrix umtx = td1.getMatrix();
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix + "Testing construction from base class." );
        if(debug) System.out.println( new TrackVector(uvec) );
        if(debug) System.out.println( new TrackError(usma) );
        if(debug) System.out.println( new TrackDerivative(umtx) );
        
        //********************************************************************
        
        if(debug) System.out.println( ok_prefix
                + "------------- All tests passed. -------------" );        
    }
    
}
