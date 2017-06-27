package org.lcsim.detector;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class RotationApacheTest extends TestCase
{		
    public RotationApacheTest(String name)
    {
        super(name);
    }
    
    public static junit.framework.Test suite()
    {
        return new TestSuite(RotationApacheTest.class);
    }
    
    protected void setUp() throws Exception
    {}
    
    
    private boolean checkRotationEquality(Rotation r1, Rotation r2 ) {
        double mat1[][] = r1.getMatrix();
        double mat2[][] = r2.getMatrix();
        boolean isEqual = true;
        double diff = 0.0;
        for(int row=0;row<3;++row) {
            for(int col=0;col<3;++col) {
                diff = mat1[row][col]-mat2[row][col];
                if(Math.abs(diff) > 0.00001) {
                    System.out.printf("rotation matrix index row %d and col %d is not equal: %f vs %f diff %f\n",row,col,mat1[row][col],mat2[row][col],diff);
                    isEqual = false;
                }
            }
        }
        
        return isEqual;
    }
    
    private boolean checkCardanAnglesEquality(double[] r1, double[] r2 ) {
        boolean isEqual = true;
        double diff = 0.0;
        for(int row=0;row<3;++row) {
            diff = r1[row]-r2[row];
            if(Math.abs(diff) > 0.00001) {
                System.out.printf("angle at index %d is not equal: %f vs %f diff %f\n",row,r1[row],r2[row],diff);
                isEqual = false;
            }
        }
        return isEqual;
    }
    
    private void printMatrix(double m[][]) {
       
        for(int row=0;row<3;++row) {
            System.out.printf("[ ");
            for(int col=0;col<3;++col) {
                System.out.printf(" %f ", m[row][col]);
                
            }
            System.out.printf("]\n");
        }
    }
    
    public void testSimpleTransform()
    {        
        
        Vector3D base_u = new Vector3D(1,0,0);
        Vector3D base_v = new Vector3D(0,1,0);
        Vector3D base_w = new Vector3D(0,0,1);
        
        //Rotate around w 
        
        double a = 0.03;
        Rotation ra = new Rotation(base_w, a);
        
        //Apply rotation
        
        Vector3D ua = ra.applyTo(base_u);
        Vector3D va = ra.applyTo(base_v);
        Vector3D wa = ra.applyTo(base_w);

        //Flip around the rotated axis u 

        double b = Math.PI;
        Rotation rb = new Rotation(ua, b);

        //Apply rotation
        
        Vector3D ub = rb.applyTo(ua);
        Vector3D vb = rb.applyTo(va);
        Vector3D wb = rb.applyTo(wa);
        
        
        
        System.out.printf("u %s\n",base_u.toString());
        System.out.printf("v %s\n",base_v.toString());
        System.out.printf("w %s\n",base_w.toString());
        
        System.out.printf("ua %s\n",ua.toString());
        System.out.printf("va %s\n",va.toString());
        System.out.printf("wa %s\n",wa.toString());
        
        System.out.printf("ub %s\n",ub.toString());
        System.out.printf("vb %s\n",vb.toString());
        System.out.printf("wb %s\n",wb.toString());
        
        // Create rotation from unit vector combinations
        
        Rotation rc_uv = new Rotation(ub, vb, base_u, base_v);
        Rotation rc_uw = new Rotation(ub, wb, base_u, base_w);
        Rotation rc_vw = new Rotation(vb, wb, base_v, base_w);
        
        // Get rotation matrix
        
        double rc_uv_mat[][] = rc_uv.getMatrix();
        double rc_uw_mat[][] = rc_uw.getMatrix();
        double rc_vw_mat[][] = rc_vw.getMatrix();

        System.out.printf("rc_uv_mat\n");
        printMatrix(rc_uv_mat);
        System.out.printf("rc_uw_mat\n");
        printMatrix(rc_uw_mat);
        System.out.printf("rc_vw_mat\n");
        printMatrix(rc_vw_mat);
        
        // check that the matrix is the same
        
        boolean eq1 = checkRotationEquality(rc_uv, rc_uw);
        boolean eq2 = checkRotationEquality(rc_uv, rc_vw);
        boolean eq3 = checkRotationEquality(rc_uw, rc_vw);
        
        assertTrue(eq1 && eq2 &&eq3);
        
        // Get Cardan-angles
        
        double angles_rc_uv[] = rc_uv.getAngles(RotationOrder.XYZ);
        double angles_rc_uw[] = rc_uw.getAngles(RotationOrder.XYZ);
        double angles_rc_vw[] = rc_vw.getAngles(RotationOrder.XYZ);
        
        System.out.printf("angles_rc_uv (%f,%f,%f)\n",angles_rc_uv[0],angles_rc_uv[1],angles_rc_uv[2]);
        System.out.printf("angles_rc_uw (%f,%f,%f)\n",angles_rc_uw[0],angles_rc_uw[1],angles_rc_uw[2]);
        System.out.printf("angles_rc_vw (%f,%f,%f)\n",angles_rc_vw[0],angles_rc_vw[1],angles_rc_vw[2]);
        
        eq1 = checkCardanAnglesEquality(angles_rc_uv, angles_rc_uw);
        eq2 = checkCardanAnglesEquality(angles_rc_uv, angles_rc_vw);
        eq3 = checkCardanAnglesEquality(angles_rc_uw, angles_rc_vw);
        
        assertTrue(eq1 && eq2 &&eq3);
        
        
        
    }
       
   
   
}