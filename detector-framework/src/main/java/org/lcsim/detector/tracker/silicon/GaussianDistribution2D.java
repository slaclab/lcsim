/*
 * GaussianDistribution2D.java
 *
 * Created on October 10, 2007, 10:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.detector.tracker.silicon;

import org.lcsim.detector.ITransform3D;
import org.lcsim.util.probability.Erf;
import hep.physics.vec.Hep3Vector;
import hep.physics.vec.VecOp;
import org.lcsim.detector.solids.GeomOp3D;

/**
 *
 * @author tknelson
 */
public class GaussianDistribution2D implements ChargeDistribution
{
    private double _normalization; // = 1.0;
    private Hep3Vector _mean; // = new BasicHep3Vector(0.0,0.0,0.0);
    private Hep3Vector _major_axis; // = new BasicHep3Vector(1.0,0.0,0.0);
    private Hep3Vector _minor_axis; // = new BasicHep3Vector(0.0,1.0,0.0);
    
    /** Creates a new instance of GaussianDistribution2D */
    public GaussianDistribution2D(double normalization, Hep3Vector mean, Hep3Vector major_axis, Hep3Vector minor_axis)
    {
//        System.out.println("Constructing GaussianDistribution2D");
//        
//        System.out.println("normalization: "+normalization);
//        System.out.println("mean: "+mean);
//        System.out.println("major axis: "+major_axis);
//        System.out.println("minor axis: "+minor_axis);
//        
//        System.out.println("VecOp.dot(major_axis,minor_axis): "+VecOp.dot(major_axis,minor_axis));
        
        _normalization = normalization;
        _mean = mean;
  
//        if (VecOp.dot(major_axis,minor_axis) == 0.0)
        if (Math.abs(VecOp.dot(major_axis,minor_axis)) < GeomOp3D.DISTANCE_TOLERANCE)  // must have a tolerance on this
        {
            _major_axis = major_axis;
            _minor_axis = minor_axis;
        }
        else
        {
            throw new RuntimeException("Axes not perpendicular!");
        }
        
    }
    
    public void transform(ITransform3D transform)
    {
        transform.transform(_mean);
        transform.rotate(_major_axis);
        transform.rotate(_major_axis);
    }
    
    public ChargeDistribution transformed(ITransform3D transform)
    {
        Hep3Vector transformed_mean = transform.transformed(_mean);
        Hep3Vector transformed_major_axis = transform.rotated(_major_axis);
        Hep3Vector transformed_minor_axis = transform.rotated(_minor_axis);
        return new GaussianDistribution2D(_normalization, transformed_mean, transformed_major_axis, transformed_minor_axis);
    }
    
    public double getNormalization()
    {
        return _normalization;
    }
    
    public Hep3Vector getMean()
    {
        return _mean;
    }
    
    public double sigma1D(Hep3Vector axis)
    {
        Hep3Vector uaxis = VecOp.unit(axis);
        return Math.sqrt( Math.pow(VecOp.dot(uaxis,_major_axis),2) + Math.pow(VecOp.dot(uaxis,_minor_axis),2) );
    }
    
    //  Calculate the x-y off-diagonal covariance matrix element
    public double covxy(Hep3Vector xaxis, Hep3Vector yaxis)
    {
        //  Check that axes are orthogonal
        if (Math.abs(VecOp.dot(xaxis, yaxis)) > GeomOp3D.ANGULAR_TOLERANCE)
            throw new RuntimeException("Pixel axes not orthogonal");

        //  Find the sin and cos of the angle between the x axis and the major axis
        double cth = VecOp.dot(VecOp.unit(xaxis), VecOp.unit(_major_axis));
        double sth = VecOp.dot(VecOp.unit(yaxis), VecOp.unit(_major_axis));

        //  Calculate the x-y covariance matrix element
        return sth * cth * (_major_axis.magnitudeSquared() - _minor_axis.magnitudeSquared());
    }

    // One dimensional upper integral of charge distribution along a given axis
    public double upperIntegral1D(Hep3Vector axis, double integration_limit)
    {
        double normalized_integration_limit = (integration_limit-VecOp.dot(getMean(),axis))/sigma1D(axis);
        
//        System.out.println("Integration limit: "+integration_limit);
//        System.out.println("Mean: "+getMean());
//        System.out.println("Axis: "+axis);
//        System.out.println("VecOp.dot(getMean(),axis)): "+VecOp.dot(getMean(),axis));
//        System.out.println("integration_limit-VecOp.dot(getMean(),axis)): "+(integration_limit - VecOp.dot(getMean(),axis)));
//        System.out.println("sigma1D(axis): "+sigma1D(axis));
//        System.out.println("Normalized integration limit: "+normalized_integration_limit);
        
        return _normalization * Erf.phic(normalized_integration_limit);
        
    }
    
}
