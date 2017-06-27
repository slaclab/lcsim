package org.lcsim.geometry.util;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.lcsim.detector.RotationGeant;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;

public class TransformationUtils {

    /**
     * Extract the Cardan angles that describe a passive XYZ order rotation taking two unit vectors into two new ones. 
     * @param u - input unit vector
     * @param v - input unit vector
     * @param u_prime - result of rotation applied to @param u
     * @param v_prime - result of rotation applied to @param v
     * @return
     */
    public static Hep3Vector getCardanAngles(Hep3Vector u, Hep3Vector v, Hep3Vector u_prime, Hep3Vector v_prime) {
    	int debug = 0;
    	if (debug>0) System.out.printf("getEulerAngles: u %s v%s -> %s %s\n",u.toString(),v.toString(),u_prime.toString(),v_prime.toString());
    	// Convert to correct API
    	Vector3D u_3D = new Vector3D(u.v());
    	Vector3D v_3D = new Vector3D(v.v());
    	Vector3D u_prime_3D = new Vector3D(u_prime.v());
    	Vector3D v_prime_3D = new Vector3D(v_prime.v());
    	
    	//Create the rotation
    	// Note that the constructor used here creates the rotation using active transformations
    	Rotation rot = new Rotation(u_3D,v_3D,u_prime_3D,v_prime_3D);
    	
    	// Get the Cardan angles of the rotation
    	double res[] = rot.getAngles(RotationOrder.ZYX);

        // Since the rotation was created based on active transformations convert to passive right here. 
        // This conversion is simply to reverse the order of rotations.
    	Hep3Vector euler = new BasicHep3Vector(res[2],res[1],res[0]);
    			
    	if(debug>0) {
    		System.out.println("Input: u " + u_3D.toString() + " v " + v_3D.toString() + " u' " + u_prime_3D.toString() + " v' " + v_prime_3D.toString());
    		System.out.println("rot matrix:");
    		TransformationUtils.printMatrix(rot.getMatrix());    	
    		System.out.println("Resulting XYZ angles " + euler.toString());
    	}
    	
    	if(debug>1) {
    		System.out.println("------- TESTING ");
    		
    		Rotation r123 = new Rotation(RotationOrder.XYZ, res[0], res[1], res[2]);
    		
    		
    		Vector3D x_3D = new Vector3D(1,0,0);
    		Vector3D y_3D = new Vector3D(0,1,0);
    		Vector3D z_3D = new Vector3D(0,0,1);
    
    		
    		Rotation r1 = new Rotation(x_3D,res[0]);
    		Vector3D x_3D_p = r1.applyTo(x_3D);
    		Rotation r3 = new Rotation(z_3D,res[2]);
    		Rotation r13 = r3.applyTo(r1);
    		Vector3D x_3D_pp = r13.applyTo(x_3D);
    		Vector3D y_3D_pp = r13.applyTo(y_3D);
    		Vector3D y_3D_pp_2 = r123.applyTo(y_3D);
    		System.out.println("x_3D (" + x_3D.getX() + "," + x_3D.getY() + "," + x_3D.getZ());
    		System.out.println("y_3D (" + y_3D.getX() + "," + y_3D.getY() + "," + y_3D.getZ());
    		System.out.println("z_3D (" + z_3D.getX() + "," + z_3D.getY() + "," + z_3D.getZ());
    		System.out.println("r1 " + r1.toString());
    		TransformationUtils.printMatrix(r1.getMatrix());
    		System.out.println("x_3D_p (" + x_3D_p.getX() + "," + x_3D_p.getY() + "," + x_3D_p.getZ());
    		System.out.println("r3 " + r3.toString());
    		TransformationUtils.printMatrix(r3.getMatrix());
    		System.out.println("r13 " + r13.toString());
    		TransformationUtils.printMatrix(r13.getMatrix());
    		System.out.println("x_3D_pp (" + x_3D_pp.getX() + "," + x_3D_pp.getY() + "," + x_3D_pp.getZ());
    		System.out.println("y_3D_pp (" + y_3D_pp.getX() + "," + y_3D_pp.getY() + "," + y_3D_pp.getZ());
    		System.out.println("r123 " + r123.toString());
    		TransformationUtils.printMatrix(r123.getMatrix());
    		System.out.println("y_3D_pp_2 (" + y_3D_pp_2.getX() + "," + y_3D_pp_2.getY() + "," + y_3D_pp_2.getZ());
    		System.out.println("------- ");
    	}
    	return euler;
    }
    
    public static boolean areVectorsEqual(Hep3Vector a, Hep3Vector b, double thresh) {
        return VecOp.cross(a, b).magnitude() > thresh ? false : true;  
    }
    
    /**
     * Extract the Cardan angles that describe a passive XYZ order rotation taking a set of three unit vectors into three new one. 
     * @param u - input unit vector
     * @param v - input unit vector
     * @param w - input unit vector
     * @param u_prime - result of rotation applied to @param u
     * @param v_prime - result of rotation applied to @param v
     * @param w_prime - result of rotation applied to @param w
     * @return
     */
    public static Hep3Vector getCardanAngles(Hep3Vector u, Hep3Vector v, Hep3Vector w, 
                                            Hep3Vector u_prime, Hep3Vector v_prime, Hep3Vector w_prime) {

        Hep3Vector a1 = getCardanAngles(u, v, u_prime, v_prime);
        Hep3Vector a2 = getCardanAngles(u, w, u_prime, w_prime);
        Hep3Vector a3 = getCardanAngles(v, w, v_prime, w_prime);
        if( !areVectorsEqual(a1, a2, 0.00001) || !areVectorsEqual(a1, a3, 0.00001) || !areVectorsEqual(a2, a3, 0.00001)) {
            System.out.printf("u: %s -> %s \n", u.toString(), u_prime.toString());
            System.out.printf("v: %s -> %s \n", v.toString(), v_prime.toString());
            System.out.printf("w: %s -> %s \n", w.toString(), w_prime.toString());
            System.out.printf("a1: %s \n", a1.toString());
            System.out.printf("a2: %s \n", a1.toString());
            System.out.printf("a3: %s \n", a1.toString());
            System.out.printf("a1 a2: %f \n", Math.abs(VecOp.dot(a1, a2)-1));
            System.out.printf("a1 a3: %f \n", Math.abs(VecOp.dot(a1, a3)-1));
            System.out.printf("a2 a3: %f \n", Math.abs(VecOp.dot(a2, a3)-1));
            
            throw new RuntimeException("Cardan angles extracted for transformation depend on input unit vectors:");
        }
        return a1;
    }
    

    /**
     * Print matrix to stdout.
     * @param mat
     */
    public static void printMatrix(double [][] mat) {
    	for(int r=0;r<3;++r) {
    		String row = "";
    		for(int c=0;c<3;++c) {
    			row += String.format(" %f",mat[r][c]);
    		}
    		System.out.println(row);
    	}
    }

   

   
    /**
     * Find the displacement of a point when rotating around an arbitrary position.
     * @param originOfRotation
     * @param point to rotate
     * @param rotationAngles - Cardan angles describing a passive XYZ order rotation 
     * @return point after rotation
     */
    public static Hep3Vector getRotationDisplacement(Hep3Vector originOfRotation,
    		Hep3Vector point, Hep3Vector rotationAngles) {
        boolean _debug = false;

        // Find the vector from the center of rotation to the point
    	Hep3Vector s = VecOp.sub(point, originOfRotation );
    	// Build a rotataion object
    	RotationGeant r = new RotationGeant(rotationAngles.x(), rotationAngles.y(), rotationAngles.z());
    	// Apply the rotation to the vector
    	Hep3Vector s_prime = r.rotated(s);
    	// Find the displaced point
    	Hep3Vector point_rot = VecOp.add(originOfRotation, s_prime );
    	
    	if(_debug) {
    		System.out.println("--- getRotationDisplacement---");
    		System.out.println(String.format("point: %s", point.toString()));
    		System.out.println(String.format("origin_of_rotation: %s", originOfRotation.toString()));
    		System.out.println(String.format("s:\n%s", s.toString()));
    		System.out.println(String.format("r:\n%s", r.toString()));
    		System.out.println(String.format("s_prime:\n%s", s_prime.toString()));
    		System.out.println(String.format("point_rot:\n%s", point_rot.toString()));
    		System.out.println("--- getRotationDisplacement END---");
    	}
    	
    	return point_rot;
    }


    
    
}
