/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.detector.solids;

import hep.physics.vec.BasicHep3Vector;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.lcsim.detector.solids.Polycone.ZPlane;

/**
 *
 * @author cozzy
 */
public class PolyconeTest extends TestCase {
    
    public PolyconeTest(String testName) {
        super(testName);
    }            

    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        
        
    }


    /**
     * Test of getCubicVolume method, of class Polycone.
     * 
     */
    public void testGetCubicVolume() {
        ZPlane p1 = new ZPlane(1,2,0);
        ZPlane p2 = new ZPlane(1,2,10);
        ZPlane p3 = new ZPlane(2,3,15);
        
        List<ZPlane> l1 = new ArrayList<ZPlane>(); 
        List<ZPlane> l2 = new ArrayList<ZPlane>(); 
        List<ZPlane> l3 = new ArrayList<ZPlane>(); 
        
        l1.add(p1);
        l1.add(p2);
        
        l2.addAll(l1);
        l2.add(p3);
        
        l3.add(p2);
        l3.add(p3);
        
        Polycone pc1 = new Polycone("Simple Tube",l1);
        Polycone pc2 = new Polycone("Tube + bevel",l2);
        Polycone pc3 = new Polycone("Bevel",l3);
        
        Tube t = new Tube("Tube using Tube Class",1,2,5);
        
        double tv1 = t.getCubicVolume();
        double v1 = pc1.getCubicVolume();
        double v2 = pc2.getCubicVolume();
        double v3 = pc3.getCubicVolume();
        
        double realv3 = Math.PI*20; 
        assertEquals(tv1,v1);
        assertEquals(v1+v3,v2);
        
        double TOLERANCE = 0.000001; 
        double diff = Math.abs(v3 - realv3);
        assertTrue( diff < TOLERANCE) ;
        
        
        
        p1 = new ZPlane(4,5,-100);
        p2 = new ZPlane(4,5,100); 

        l1.clear();
        l1.add(p1);
        l1.add(p2);
        pc1 = new Polycone("Another tube with pc class",l1);
        Tube t2 = new Tube("Another tube",4,5,100); 
        assertTrue(Math.abs(t2.getCubicVolume()-pc1.getCubicVolume())<TOLERANCE); 
    
        
    }

    
    public void testSorted(){
        
        ZPlane z1 = new ZPlane(2,3,4);
        ZPlane z2 = new ZPlane(3,4,5);
        ZPlane z3 = new ZPlane(2,4,-5);
        
        List<ZPlane> l1 = new ArrayList<ZPlane>();
        List<ZPlane> l2 = new ArrayList<ZPlane>();
        
        l1.add(z1);
        l1.add(z2);
        l1.add(z3);
        
        l2.add(z3);
        l2.add(z1);
        l2.add(z2);
        
        Polycone p1 = new Polycone("unsorted",l1);
        Polycone p2 = new Polycone("sorted",l2);
        
        assertEquals(p1.zplanes.get(0).z,-5.0);
        assertEquals(p2.zplanes.get(0).z,-5.0);
        assertEquals(p1.zplanes.get(1).z,4.0);
        assertEquals(p2.zplanes.get(1).z,4.0);
        assertEquals(p1.zplanes.get(2).z,5.0);
        assertEquals(p2.zplanes.get(2).z,5.0);
        
    }
    
    public void testInside(){
        
        ZPlane p1 = new ZPlane(1,2,0);
        ZPlane p2 = new ZPlane(1,2,10);
        ZPlane p3 = new ZPlane(2,3,15);
        
        List<ZPlane> l1 = new ArrayList<ZPlane>(); 
        l1.add(p1);
        l1.add(p2);
        l1.add(p3);
        
        Polycone pc = new Polycone("test",l1);
        
        assertEquals(Inside.OUTSIDE,pc.inside(new BasicHep3Vector(3,0,5)));
        assertEquals(Inside.OUTSIDE,pc.inside(new BasicHep3Vector(0,3,5)));
        assertEquals(Inside.OUTSIDE,pc.inside(new BasicHep3Vector(1.5,1.5,5)));
        assertEquals(Inside.OUTSIDE,pc.inside(new BasicHep3Vector(0,0,1)));
        assertEquals(Inside.OUTSIDE,pc.inside(new BasicHep3Vector(1.5,0,13)));
        
        assertEquals(Inside.INSIDE,pc.inside(new BasicHep3Vector(0,2.5,14)));
        assertEquals(Inside.INSIDE,pc.inside(new BasicHep3Vector(1.1,0,5)));
        assertEquals(Inside.INSIDE,pc.inside(new BasicHep3Vector(1.1,0,10)));
        assertEquals(Inside.INSIDE,pc.inside(new BasicHep3Vector(1.1,1.1,5)));
        assertEquals(Inside.INSIDE,pc.inside(new BasicHep3Vector(1.8,0,13)));
        
        assertEquals(Inside.SURFACE,pc.inside(new BasicHep3Vector(1.1,1.1,0)));
        assertEquals(Inside.SURFACE,pc.inside(new BasicHep3Vector(2,0,4)));
        assertEquals(Inside.SURFACE,pc.inside(new BasicHep3Vector(1,0,4)));
        assertEquals(Inside.SURFACE,pc.inside(new BasicHep3Vector(2.5,0,12.5)));
        assertEquals(Inside.SURFACE,pc.inside(new BasicHep3Vector(1.5,0,12.5)));
        assertEquals(Inside.SURFACE,pc.inside(new BasicHep3Vector(2.5,0,15)));        
    }
    
    
    public void testRadiusAtZ(){
        
        
        ZPlane p1 = new ZPlane(1,2,0);
        ZPlane p2 = new ZPlane(1,2,10);
        ZPlane p3 = new ZPlane(2,3,15);
        
        List<ZPlane> l1 = new ArrayList<ZPlane>(); 
        l1.add(p1);
        l1.add(p2);
        l1.add(p3);
        
        Polycone pc = new Polycone("test",l1);
        
        assertEquals(1.0,pc.getInnerRadiusAtZ(0));
        assertEquals(2.0,pc.getOuterRadiusAtZ(0));
        assertEquals(2.0,pc.getOuterRadiusAtZ(5));
        assertEquals(2.0,pc.getOuterRadiusAtZ(10));
        assertEquals(1.5,pc.getInnerRadiusAtZ(12.5));
        assertEquals(2.5,pc.getOuterRadiusAtZ(12.5));
        assertEquals(3.0,pc.getOuterRadiusAtZ(15));
        assertEquals(0.0,pc.getOuterRadiusAtZ(16));
        
    }
}

