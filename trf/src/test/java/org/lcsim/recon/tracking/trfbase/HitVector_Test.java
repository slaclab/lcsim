/*
 * HitVector_Test.java
 *
 * Created on July 24, 2007, 2:49 PM
 *
 * $Id: HitVector_Test.java,v 1.1.1.1 2010/04/08 20:38:00 jeremy Exp $
 */

package org.lcsim.recon.tracking.trfbase;

import junit.framework.TestCase;
import org.lcsim.recon.tracking.trfutil.Assert;

/**
 *
 * @author Norman Graf
 */
public class HitVector_Test extends TestCase
{
    private boolean debug;
    /** Creates a new instance of HitVector_Test */
    public void testHitVector()
    {
        String ok_prefix = "HitVector (I): ";
        String error_prefix = "HitVector test (E): ";
        
        if(debug) System.out.println("-------- Testing component HitVector. --------" );
        
        //********************************************************************
        
        if(debug) System.out.println("Test constructor." );
        // vector
        HitVector hv1 = new HitVector(3);
        if(debug) System.out.println(hv1 );
        Assert.assertTrue( hv1.size() == 3 );
        Assert.assertTrue( hv1.amax() == 0.0 );
        
        // error
        HitError he1 = new HitError(3);
        if(debug) System.out.println(he1 );
        Assert.assertTrue( he1.size() == 3 );
        Assert.assertTrue( he1.amax() == 0.0 );
        
        // derivative
        HitDerivative hd1 = new HitDerivative(3);
        if(debug) System.out.println(hd1 );
        Assert.assertTrue( hd1.size() == 3 );
        Assert.assertTrue( hd1.amax() == 0.0 );
        
        //********************************************************************
        
        if(debug) System.out.println("Test accessors and copy constructor." );
        // vector
        hv1.set(0,1.0);
        hv1.set(1,-2.0);
        hv1.set(2,3.0);
        if(debug) System.out.println(hv1 );
        
        HitVector hv2 = new HitVector(hv1);
        Assert.assertTrue( hv2.get(0) == 1.0 );
        Assert.assertTrue( hv2.get(1) == -2.0 );
        Assert.assertTrue( hv2.get(2) == 3.0 );
        
        // error
        he1.set(0,0,11.0);
        he1.set(0,1,-12.0);
        he1.set(1,1,22.0);
        he1.set(0,2,31.0);
        he1.set(2,1,-32.0);
        he1.set(2,2,33.0);
        if(debug) System.out.println(he1 );
        HitError he2 = new HitError(he1);
        if(debug) System.out.println(he2 );
        Assert.assertTrue( he2.get(0,0) == 11.0 );
        Assert.assertTrue( he2.get(0,1) == -12.0 );
        Assert.assertTrue( he2.get(1,1) == 22.0 );
        Assert.assertTrue( he2.get(2,0) == 31.0 );
        Assert.assertTrue( he2.get(1,2) == -32.0 );
        Assert.assertTrue( he2.get(2,2) == 33.0 );
        // derivative
        hd1.set(0,0,1.0);
        hd1.set(0,1,0.1);
        hd1.set(0,2,0.0);
        hd1.set(0,3,0.0);
        hd1.set(0,4,0.0);
        hd1.set(1,0,-0.1);
        hd1.set(1,1,1.0);
        hd1.set(1,2,0.0);
        hd1.set(1,3,0.0);
        hd1.set(1,4,0.001);
        hd1.set(2,0,0.0);
        hd1.set(2,1,0.0);
        hd1.set(2,2,1.0);
        hd1.set(2,3,-0.2);
        hd1.set(2,4,0.2);
        if(debug) System.out.println(hd1 );
        
        HitDerivative hd2 = new HitDerivative(hd1);
        Assert.assertTrue( hd2.get(0,0) == 1.0 );
        Assert.assertTrue( hd2.get(0,1) == 0.1 );
        Assert.assertTrue( hd2.get(0,2) == 0.0 );
        Assert.assertTrue( hd2.get(0,3) == 0.0 );
        Assert.assertTrue( hd2.get(0,4) == 0.0 );
        Assert.assertTrue( hd2.get(1,0) == -0.1 );
        Assert.assertTrue( hd2.get(1,1) == 1.0 );
        Assert.assertTrue( hd2.get(1,2) == 0.0 );
        Assert.assertTrue( hd2.get(1,3) == 0.0 );
        Assert.assertTrue( hd2.get(1,4) == 0.001 );
        Assert.assertTrue( hd2.get(2,0) == 0.0 );
        Assert.assertTrue( hd2.get(2,1) == 0.0 );
        Assert.assertTrue( hd2.get(2,2) == 1.0 );
        Assert.assertTrue( hd2.get(2,3) == -0.2 );
        Assert.assertTrue( hd2.get(2,4) == 0.2 );
        
        //********************************************************************
        
        if(debug) System.out.println("Test min and max." );
        // vector
        if(debug) System.out.println(hv1.min() + ' ' + hv1.max() + ' '
                + hv1.amin() + ' ' + hv1.amax() );
        Assert.assertTrue( hv1.min() == -2.0 );
        Assert.assertTrue( hv1.max() ==  3.0 );
        Assert.assertTrue( hv1.amin() == 1.0 );
        Assert.assertTrue( hv1.amax() == 3.0 );
        // errorr
        if(debug) System.out.println(he1.min() + ' ' + he1.max() + ' '
                + he1.amin() + ' ' + he1.amax() );
        Assert.assertTrue( he1.min() == -32.0 );
        Assert.assertTrue( he1.max() ==  33.0 );
        Assert.assertTrue( he1.amin() == 11.0 );
        Assert.assertTrue( he1.amax() == 33.0 );
        // vector
        if(debug) System.out.println(hd1.min() + ' ' + hd1.max() + ' '
                + hd1.amin() + ' ' + hd1.amax() );
        Assert.assertTrue( hd1.min() == -0.2 );
        Assert.assertTrue( hd1.max() ==  1.0 );
        Assert.assertTrue( hd1.amin() == 0.0 );
        Assert.assertTrue( hd1.amax() == 1.0 );
        
        //********************************************************************
        
        if(debug) System.out.println("Test equality." );
        // vector
        HitVector hv3 = new HitVector(3);
        hv3.set(0,2.0);
        hv3.set(1,4.0);
        hv3.set(2,6.0);
        if(debug) System.out.println(hv1 );
        if(debug) System.out.println(hv2 );
        
        Assert.assertTrue( hv1.equals(hv2) );
        Assert.assertTrue( ! (hv1.notEquals(hv2)) );
        if(debug) System.out.println(hv3 );
        if(debug) System.out.println(hv2 );
        Assert.assertTrue( hv3.notEquals(hv2) );
        Assert.assertTrue( ! (hv3.equals(hv2)) );
        HitVector hv4 = new HitVector(2);
        hv4.set(0,hv1.get(0));
        hv4.set(1,hv1.get(1));
        if(debug) System.out.println(hv4 );
        if(debug) System.out.println(hv1 );
        Assert.assertTrue( hv4.notEquals(hv1) );
        
        // error
        HitError he3 = new HitError(3);
        if(debug) System.out.println(he1 );
        if(debug) System.out.println(he2 );
        Assert.assertTrue( he1.equals(he2) );
        if(debug) System.out.println(he3 );
        if(debug) System.out.println(he2 );
        Assert.assertTrue( he3.notEquals(he2) );
        Assert.assertTrue( ! (he3.equals(he2)) );
        
        HitError he4 = new HitError(2);
        he4.set(0,0,he1.get(0,0));
        he4.set(0,1,he1.get(0,1));
        he4.set(1,1,he1.get(1,1));
        if(debug) System.out.println(he4 );
        if(debug) System.out.println(he1 );
        Assert.assertTrue( he4.notEquals(he1) );
        Assert.assertTrue( ! (he4.equals(he1)) );
        
        // derivative
        HitDerivative hd3 = new HitDerivative(3);
        if(debug) System.out.println(hd1 );
        if(debug) System.out.println(hd2 );
        Assert.assertTrue( hd1.equals(hd2) );
        if(debug) System.out.println(hd3 );
        if(debug) System.out.println(hd2 );
        Assert.assertTrue( hd3.notEquals(hd2) );
        Assert.assertTrue( ! (hd3.equals(hd2)) );
        
        HitDerivative hd4 = new HitDerivative(2);
        hd4.set(0,0,hd1.get(0,0));
        hd4.set(0,1,hd1.get(0,1));
        hd4.set(1,1,hd1.get(1,1));
        if(debug) System.out.println(hd4 );
        if(debug) System.out.println(hd1 );
        Assert.assertTrue( hd4.notEquals(hd1) );
        Assert.assertTrue( ! (hd4.equals(hd1)) );
        
        //********************************************************************
/*
  if(debug) System.out.println("Test assignment." );
  // vector
  hv3 = hv2;
  if(debug) System.out.println(hv2 );
  if(debug) System.out.println(hv3 );
  Assert.assertTrue( hv2 == hv3 );
  // error
  he3 = he2;
  if(debug) System.out.println(he2 );
  if(debug) System.out.println(he3 );
  Assert.assertTrue( he2 == he3 );
  // derivative
  hd3 = hd2;
  if(debug) System.out.println(hd2 );
  if(debug) System.out.println(hd3 );
  Assert.assertTrue( hd2 == hd3 );
 */
        //********************************************************************
        
        if(debug) System.out.println("Test addition and subtraction." );
        double diff;
        double maxdiff = 1.e-12;
        // vector
        hv3 = new HitVector(hv2);    // hv1
        hv3.plusEquals(hv2);         // 2*hv1
        HitVector hv5 = new HitVector(hv3.plus(hv2));   // 3*hv1
        if(debug) System.out.println(hv5 );
        if(debug) System.out.println(hv1 );
        
        Assert.assertTrue( hv5.notEquals(hv1) );
        hv5 = new HitVector(hv5.minus(hv2));
        hv5.minusEquals(hv2);
        if(debug) System.out.println(hv5 );
        
        diff = (hv5.minus(hv1)).amax();
        if(debug) System.out.println(diff );
        Assert.assertTrue( diff < maxdiff );
        
        // error
        he3 = new HitError(he2);                   // he1
        he3.plusEquals(he2);                  // 2*he1
        HitError he5 = new HitError(he3.plus(he2));    // 3*he1
        if(debug) System.out.println(he5 );
        if(debug) System.out.println(he1 );
        Assert.assertTrue( he5.notEquals(he1) );
        he5 = new HitError(he5.minus(he2));
        he5.minusEquals(he2);
        if(debug) System.out.println(he5 );
        diff = (he5.minus(he1)).amax();
        if(debug) System.out.println(diff );
        Assert.assertTrue( diff < maxdiff );
        
        // derivative
        hd3 = new HitDerivative(hd2);                       // hd1
        hd3.plusEquals(hd2);                      // 2*hd1
        HitDerivative hd5 = new HitDerivative(hd3.plus(hd2));   // 3*hd1
        if(debug) System.out.println(hd5 );
        if(debug) System.out.println(hd1 );
        Assert.assertTrue( hd5.notEquals(hd1) );
        
        hd5 = new HitDerivative(hd5.minus(hd2));
        hd5.minusEquals(hd2);
        if(debug) System.out.println(hd5 );
        diff = (hd5.minus(hd1)).amax();
        if(debug) System.out.println(diff );
        Assert.assertTrue( diff < maxdiff );
        
        //********************************************************************
        
        if(debug) System.out.println("Test construction from an array." );
        double[] tmp = new double[100];
        int i, j, ij;
        int sz;
        // vector
        sz = he1.size();
        for ( i=0; i<sz; ++i ) tmp[i] = hv1.get(i);
        HitVector hv6 = new HitVector( hv1.size(), tmp );
        if(debug) System.out.println(hv1 );
        if(debug) System.out.println(hv6 );
        Assert.assertTrue( hv1.equals(hv6) );
        
        // error
        sz = he1.size();
        ij = 0;
        for ( i=0; i<sz; ++i )
            for ( j=0; j<=i; ++j ) tmp[ij++] = he1.get(i,j);
        if(debug) System.out.println(he1 );
        if(debug) System.out.println("he1.size()= "+he1.size());
        HitError he6 = new HitError( he1.size(), tmp );
        if(debug) System.out.println(he6 );
        Assert.assertTrue( he1.equals(he6) );
        
        // derivative
        sz = hd1.size();
        ij = 0;
        for ( i=0; i<sz; ++i )
            for ( j=0; j<5; ++j ) tmp[ij++] = hd1.get(i,j);
        if(debug) System.out.println(hd1 );
        HitDerivative hd6 = new HitDerivative( hd1.size(), tmp );
        if(debug) System.out.println(hd6 );
        Assert.assertTrue( hd1.equals(hd6) );
        
        //********************************************************************
        
        if(debug) System.out.println("Test 1D construction values." );
        // vector
        HitVector hv01 = new HitVector(1.0);
        if(debug) System.out.println(hv01 );
        Assert.assertTrue( hv01.size() == 1 );
        Assert.assertTrue( hv01.get(0) == 1.0 );
        // error
        HitError he01 = new HitError(0.11);
        if(debug) System.out.println(he01 );
        Assert.assertTrue( he01.size() == 1 );
        Assert.assertTrue( he01.get(0,0) == 0.11 );
        
        //********************************************************************
        
        if(debug) System.out.println("Test 2D construction values." );
        // vector
        HitVector hv02 = new HitVector(1.0,2.0);
        if(debug) System.out.println(hv02 );
        Assert.assertTrue( hv02.size() == 2 );
        Assert.assertTrue( hv02.get(0) == 1.0 );
        Assert.assertTrue( hv02.get(1) == 2.0 );
        // error
        HitError he02 = new HitError(0.11,0.21,0.22);
        if(debug) System.out.println(he02 );
        Assert.assertTrue( he02.size() == 2 );
        Assert.assertTrue( he02.get(0,0) == 0.11 );
        Assert.assertTrue( he02.get(1,0) == 0.21 );
        Assert.assertTrue( he02.get(1,1) == 0.22 );
        
        //********************************************************************
        
        if(debug) System.out.println("Test 3D construction values." );
        // vector
        HitVector hv03 = new HitVector(1.0,2.0,3.0);
        if(debug) System.out.println(hv03 );
        Assert.assertTrue( hv03.size() == 3 );
        Assert.assertTrue( hv03.get(0) == 1.0 );
        Assert.assertTrue( hv03.get(1) == 2.0 );
        Assert.assertTrue( hv03.get(2) == 3.0 );
        // error
        HitError he03 = new HitError(0.11,0.21,0.22,0.31,0.32,0.33);
        if(debug) System.out.println(he03 );
        Assert.assertTrue( he03.size() == 3 );
        Assert.assertTrue( he03.get(0,0) == 0.11 );
        Assert.assertTrue( he03.get(1,0) == 0.21 );
        Assert.assertTrue( he03.get(1,1) == 0.22 );
        Assert.assertTrue( he03.get(2,0) == 0.31 );
        Assert.assertTrue( he03.get(2,1) == 0.32 );
        Assert.assertTrue( he03.get(2,2) == 0.33 );
        
        //********************************************************************
        
        if(debug) System.out.println("Test constructing hit error from track error."
                );
        TrackError terr = new TrackError();
        HitDerivative hder = new HitDerivative(2);
        double der = 1.0;
        for ( i=0; i<5; ++i )
        {
            terr.set(i,i,(i+1));
            hder.set(0,i,Math.sqrt(der));
            hder.set(1,i,2.0*hder.get(0,i));
            der /= 10.0;
        }
        double e11 = 1.2345;
        HitError expected_err = new HitError(e11,2.0*e11,4.0*e11);
        HitError calculated_err = new HitError(hder,terr);
        HitError diff_err = calculated_err.minus(expected_err);
        if(debug) System.out.println( hder );
        if(debug) System.out.println( terr );
        if(debug) System.out.println( expected_err );
        if(debug) System.out.println( calculated_err );
        if(debug) System.out.println( diff_err );
        Assert.assertTrue( diff_err.amax() < 1.e-12 );
        
        //********************************************************************
        
        if(debug) System.out.println("Test inversion." );
        HitError test_err = new HitError(2.0,1.0,3.0);
        HitError inverted_err = new HitError(test_err);
        if(debug) System.out.println(inverted_err );
        int stat = inverted_err.invert();
        if(debug) System.out.println(stat );
        Assert.assertTrue( stat == 0 );
        
        HitError original_err = new HitError(inverted_err);
        stat = original_err.invert();
        if(debug) System.out.println(stat );
        Assert.assertTrue( stat == 0 );
        if(debug) System.out.println(inverted_err );
        if(debug) System.out.println(original_err );
        Assert.assertTrue( (original_err.minus(test_err)).amax() < 1.e-12 );
        
        //********************************************************************
        
        if(debug) System.out.println("Test fetching underlying representation."
                );
        Jama.Matrix uvec = hv1.matrix();
        Jama.Matrix usma = he1.matrix();
        Jama.Matrix umtx = hd1.matrix();
        
        //********************************************************************
        
        if(debug) System.out.println(ok_prefix
                + "------------- All tests passed. -------------" );
        //********************************************************************        
    }
    
}
