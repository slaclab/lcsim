package org.lcsim.detector;

import org.lcsim.detector.IRotation3D;
import org.lcsim.detector.RotationPassiveXYZ;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class Rotation3DTest extends TestCase
{		
    public Rotation3DTest(String name)
    {
        super(name);
    }
    
    public static junit.framework.Test suite()
    {
        return new TestSuite(Rotation3DTest.class);
    }
    
    protected void setUp() throws Exception
    {}
       
    public void testIdentityIsDefault()
    {
    	Rotation3D rotation = new Rotation3D();
    	assertTrue(rotation.getRotationMatrix().trace() == 3.0);
    }    
        
    public void testSimpleRotationX()
    {
    	Hep3Vector coordinate = new BasicHep3Vector(0.0,0.0,1.0);
    	IRotation3D rotation = new RotationPassiveXYZ(Math.PI / 2, 0, 0);
    	Hep3Vector result = VecOp.mult(rotation.getRotationMatrix(),coordinate);
    	System.out.println("90 degree X rotation: Y'=Z");
    	rotation.printOut(System.out);
    	System.out.printf("original point -> ( %.4f %.4f %.4f ) \n",coordinate.x(),coordinate.y(),coordinate.z());
    	System.out.printf("rotated 90 deg -> ( %.4f %.4f %.4f ) \n",result.x(),result.y(),result.z());
        rotation.invert();
        result = VecOp.mult(rotation.getRotationMatrix(),result);
        System.out.printf("inverse rotated 90 deg -> ( %.4f %.4f %.4f ) \n",result.x(),result.y(),result.z());
    }
    
    public void testSimpleRotationY()
    {
    	Hep3Vector coordinate = new BasicHep3Vector(1.0,0.0,0.0);
    	IRotation3D rotation = new RotationPassiveXYZ(0, Math.PI / 2, 0);
    	Hep3Vector result = VecOp.mult(rotation.getRotationMatrix(),coordinate);
    	System.out.println("90 degree Y rotation: Z'=X");
    	rotation.printOut(System.out);
    	System.out.printf("original point -> ( %.4f %.4f %.4f ) \n",coordinate.x(),coordinate.y(),coordinate.z());
    	System.out.printf("rotated 90 deg -> ( %.4f %.4f %.4f ) \n",result.x(),result.y(),result.z());
        rotation.invert();
        result = VecOp.mult(rotation.getRotationMatrix(),result);
        System.out.printf("inverse rotated 90 deg -> ( %.4f %.4f %.4f ) \n",result.x(),result.y(),result.z());
    }
        
    public void testSimpleRotationZ()
    {
    	Hep3Vector coordinate = new BasicHep3Vector(0.0,1.0,0.0);
    	IRotation3D rotation = new RotationPassiveXYZ(0, 0, Math.PI / 2);
    	Hep3Vector result = VecOp.mult(rotation.getRotationMatrix(),coordinate);
    	System.out.println("90 degree Z rotation: X'=Y");
    	rotation.printOut(System.out);
    	System.out.printf("original point -> ( %.4f %.4f %.4f ) \n",coordinate.x(),coordinate.y(),coordinate.z());
    	System.out.printf("rotated 90 deg -> ( %.4f %.4f %.4f ) \n",result.x(),result.y(),result.z());
        rotation.invert();
        result = VecOp.mult(rotation.getRotationMatrix(),result);
        System.out.printf("inverse rotated 90 deg -> ( %.4f %.4f %.4f ) \n",result.x(),result.y(),result.z());
    }
    
    public void testRotationXYZ()
    {
    	Hep3Vector coordinate = new BasicHep3Vector(1.0,2.0,3.0);
    	IRotation3D rotation = new RotationPassiveXYZ(Math.PI/2, Math.PI/2, Math.PI/2);
    	Hep3Vector result = VecOp.mult(rotation.getRotationMatrix(),coordinate);
    	System.out.println("Sequential 90 degree rotations in X,Y,Z: X'=Z, Y'=-Y ,Z'=X");
    	rotation.printOut(System.out);
    	System.out.printf("original point -> ( %.4f %.4f %.4f ) \n",coordinate.x(),coordinate.y(),coordinate.z());
    	System.out.printf("rotated 90 deg -> ( %.4f %.4f %.4f ) \n",result.x(),result.y(),result.z());
        rotation.invert();
        result = VecOp.mult(rotation.getRotationMatrix(),result);
        System.out.printf("inverse rotated 90 deg -> ( %.4f %.4f %.4f ) \n",result.x(),result.y(),result.z());
    }
    
}