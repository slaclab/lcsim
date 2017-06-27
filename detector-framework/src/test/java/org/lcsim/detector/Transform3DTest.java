package org.lcsim.detector;

import org.lcsim.detector.Transform3D;
import org.lcsim.detector.ITranslation3D;
import org.lcsim.detector.Translation3D;
import org.lcsim.detector.IRotation3D;
import org.lcsim.detector.RotationPassiveXYZ;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.VecOp;
import hep.physics.vec.Hep3Vector;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.Random;

import static java.lang.Math.PI;

public class Transform3DTest extends TestCase
{		
    public Transform3DTest(String name)
    {
        super(name);
    }
    
    public static junit.framework.Test suite()
    {
        return new TestSuite(Transform3DTest.class);
    }
    
    protected void setUp() throws Exception
    {}
    
    public void testSimpleTransform()
    {        
        IRotation3D r1 = new RotationPassiveXYZ(0, 0, PI/2);
        
        ITranslation3D v1 = new Translation3D(10,0,0);
        
        ITransform3D t1 = new Transform3D(v1,r1);
        
        Hep3Vector point = new BasicHep3Vector(1,0,0);
        
        Hep3Vector tpoint = VecOp.mult(r1.getRotationMatrix(), point);
        
        //System.out.println("point = " + tpoint);
        
        Hep3Vector tpoint2 = VecOp.add(tpoint, t1.getTranslation());
        
        //System.out.println("point2 = " + tpoint2);
        
        System.out.println(t1.transformed(point));
        
        assertTrue(t1.transformed(point).equals(tpoint2));
    }
       
    public void testIdentityTransformation()
    {
    	System.out.println("CoordinateTransformation3DTest.testIdentityTransformation");
    	
    	Transform3D transformation = new Transform3D();     
    	Hep3Vector point = new BasicHep3Vector(1.0,2.0,3.0);
    	
    	Hep3Vector transformed_point = transformation.transformed(point);
    	assertEquals(transformed_point.x(),point.x());
    	assertEquals(transformed_point.y(),point.y());
    	assertEquals(transformed_point.z(),point.z());
    }    
    
    public void testSimpleTranslationXYZ()
    {
    	System.out.println("CoordinateTransformation3DTest.testSimpleTranslationXYZ");
    	
        Random random = new Random();
        double random_x = random.nextDouble();
        double random_y = random.nextDouble();
        double random_z = random.nextDouble();
        
    	Transform3D transformation = new Transform3D(new Translation3D(random_x,random_y,random_z));
    	Hep3Vector point = new BasicHep3Vector(1.0,2.0,3.0);

    	Hep3Vector transformed_point = transformation.transformed(point);
        
//        System.out.println("x_1: "+(point.x()+random_x)+"  x_2: "+transformed_point.x());
//        System.out.println("y_1: "+(point.y()+random_y)+"  y_2: "+transformed_point.y());
//        System.out.println("z_1: "+(point.z()+random_z)+"  z_2: "+transformed_point.z());
        
    	assertEquals(transformed_point.x(),point.x()+random_x);
    	assertEquals(transformed_point.y(),point.y()+random_y);
    	assertEquals(transformed_point.z(),point.z()+random_z);
        
    }
    
    
    public void testSimpleRotationX()
    {
    	System.out.println("CoordinateTransformation3DTest.testSimpleRotationX");
    	
    	IRotation3D rotateX = new RotationPassiveXYZ(Math.PI / 2, 0, 0);

        ITransform3D transformation = new Transform3D(rotateX);

    	Hep3Vector point = new BasicHep3Vector(0.0,0.0,1.0);
    	Hep3Vector result = transformation.transformed(point);

//    	System.out.println("Transform");
    	((Rotation3D)transformation.getRotation()).printOut(System.out);
//    	System.out.printf("new point = ( %.4f %.4f %.4f )\n",result.x(),result.y(),result.z());
    	//CoordinateTransformation3D.mult(point, transform);
    	//Hep3Rotation rotation = result.getRotation();    	
    	
    	//System.out.println("transformation");
    	//rotateX.printOut(System.out);
    	//System.out.println();
    	//System.out.println("transformed point");
    	//rotation.printOut(System.out);    	
    	
    	//assertStrictEquals(rotation, rotateX);
    	
    }
    
    public void testMultiply()
    {
        
        System.out.println("CoordinateTransformation3DTest.testMultiply");
    	
        Random random = new Random();
        
        IRotation3D rotation_1 = new RotationPassiveXYZ(random.nextDouble(),random.nextDouble(),random.nextDouble());
        ITranslation3D translation_1 = new Translation3D(random.nextDouble(),random.nextDouble(),random.nextDouble());
        
        IRotation3D rotation_2 = new RotationPassiveXYZ(random.nextDouble(),random.nextDouble(),random.nextDouble());
        ITranslation3D translation_2 = new Translation3D(random.nextDouble(),random.nextDouble(),random.nextDouble());

    	ITransform3D transformation_1 = new Transform3D(translation_1,rotation_1);
    	ITransform3D transformation_2 = new Transform3D(translation_2,rotation_2);
       
        Hep3Vector point = new BasicHep3Vector(1.0,2.0,3.0);
        
        Hep3Vector transformed_point_1 = transformation_1.transformed(point);
        transformed_point_1 = transformation_2.transformed(transformed_point_1);
       
        ITransform3D transformation_product = Transform3D.multiply(transformation_2,transformation_1);
        Hep3Vector transformed_point_2 = transformation_product.transformed(point);
        
        System.out.println("x_1: "+transformed_point_1.x()+"  x_2: "+transformed_point_2.x());
        System.out.println("y_1: "+transformed_point_1.y()+"  y_2: "+transformed_point_2.y());
        System.out.println("z_1: "+transformed_point_1.z()+"  z_2: "+transformed_point_2.z());

        assertTrue(Math.abs(transformed_point_2.x()/transformed_point_1.x()-1) < 1E-12);
    	assertTrue(Math.abs(transformed_point_2.y()/transformed_point_1.y()-1) < 1E-12);
    	assertTrue(Math.abs(transformed_point_2.z()/transformed_point_1.z()-1) < 1E-12);
        
    }

    
    public void testMultiplyBy()
    {
        
        System.out.println("CoordinateTransformation3DTest.testMultiplyBy");
    	
        Random random = new Random();
        
        IRotation3D rotation_1 = new RotationPassiveXYZ(random.nextDouble(),random.nextDouble(),random.nextDouble());
        ITranslation3D translation_1 = new Translation3D(random.nextDouble(),random.nextDouble(),random.nextDouble());
        
        IRotation3D rotation_2 = new RotationPassiveXYZ(random.nextDouble(),random.nextDouble(),random.nextDouble());
        ITranslation3D translation_2 = new Translation3D(random.nextDouble(),random.nextDouble(),random.nextDouble());

    	Transform3D transformation_1 = new Transform3D(translation_1,rotation_1);
    	Transform3D transformation_2 = new Transform3D(translation_2,rotation_2);
        
        Hep3Vector point_1 = new BasicHep3Vector(1.0,2.0,3.0);
        Hep3Vector point_2 = new BasicHep3Vector(1.0,2.0,3.0);
                
        transformation_1.transform(point_1);
        transformation_2.transform(point_1);
        
        transformation_2.multiplyBy(transformation_1);        
        transformation_2.transform(point_2);
        
        System.out.println("x_1: "+point_1.x()+"  x_2: "+point_2.x());
        System.out.println("y_1: "+point_1.y()+"  y_2: "+point_2.y());
        System.out.println("z_1: "+point_1.z()+"  z_2: "+point_2.z());

        assertTrue(Math.abs(point_2.x()/point_1.x()-1) < 1E-12);
    	assertTrue(Math.abs(point_2.y()/point_1.y()-1) < 1E-12);
    	assertTrue(Math.abs(point_2.z()/point_1.z()-1) < 1E-12);
        
    }
    
    
    public void testInvert()
    {
        
        System.out.println("CoordinateTransformation3DTest.testInvert");
    	
        Random random = new Random();

        IRotation3D rotation = new RotationPassiveXYZ(random.nextDouble(),random.nextDouble(),random.nextDouble());
        ITranslation3D translation = new Translation3D(random.nextDouble(),random.nextDouble(),random.nextDouble());
    	Transform3D transformation = new Transform3D(translation,rotation);
        
        Hep3Vector point = new BasicHep3Vector(1.0,2.0,3.0);
        Hep3Vector transformed_point = transformation.transformed(point);
        
        transformation.invert();
        transformed_point = transformation.transformed(transformed_point);

//        System.out.println("x ratio: "+transformed_point.x()/point.x());
//        System.out.println("y ratio: "+transformed_point.y()/point.y());
//        System.out.println("z ratio: "+transformed_point.z()/point.z());
        
        assertTrue(Math.abs(transformed_point.x()/point.x()-1) < 1E-12);
    	assertTrue(Math.abs(transformed_point.y()/point.y()-1) < 1E-12);
    	assertTrue(Math.abs(transformed_point.z()/point.z()-1) < 1E-12);
        
    }

    
    public void testInverse()
    {
        
        System.out.println("CoordinateTransformation3DTest.testInverse");
    	
        Random random = new Random();

        IRotation3D rotation = new RotationPassiveXYZ(random.nextDouble(),random.nextDouble(),random.nextDouble());
        ITranslation3D translation = new Translation3D(random.nextDouble(),random.nextDouble(),random.nextDouble());

    	Transform3D transformation = new Transform3D(translation,rotation);  
        Hep3Vector point = new BasicHep3Vector(1.0,2.0,3.0);        
        Hep3Vector transformed_point = transformation.transformed(point);     
        
        Transform3D transformation_inverted = transformation.inverse();
        transformed_point = transformation_inverted.transformed(transformed_point);
        
//        System.out.println("x ratio: "+transformed_point.x()/point.x());
//        System.out.println("y ratio: "+transformed_point.y()/point.y());
//        System.out.println("z ratio: "+transformed_point.z()/point.z());
        
    	assertTrue(Math.abs(transformed_point.x()/point.x()-1) < 1E-12);
    	assertTrue(Math.abs(transformed_point.y()/point.y()-1) < 1E-12);
    	assertTrue(Math.abs(transformed_point.z()/point.z()-1) < 1E-12);
        
    } 
}