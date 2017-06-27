package org.lcsim.recon.tracking.digitization.sisim;
/*
 * TrackSegment.java
 *
 * Created on July 27, 2005, 3:34 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

import hep.physics.vec.Hep3Vector;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.VecOp;

import org.lcsim.event.SimTrackerHit;
import org.lcsim.detector.ITransform3D;

import java.util.*;

/**
 *
 * @author tknelson
 */
public class TrackSegment
{
    
    // Fields
    private Hep3Vector _p1;
    private Hep3Vector _p2;
    private double _energy_loss;
    
    /**
     * Creates a new instance of TrackSegment
     */
    
    public TrackSegment(Hep3Vector p1, Hep3Vector p2, double energy_loss)
    {
        _p1 = p1;
        _p2 = p2;
        _energy_loss = energy_loss;
    }
    
    public TrackSegment(SimTrackerHit hit)
    {
        _p1 = new BasicHep3Vector(hit.getStartPoint());
        _p2 = new BasicHep3Vector(hit.getEndPoint());
        _energy_loss = hit.getdEdx();
    }
    
    // Accessors
    public Hep3Vector getP1()
    {
        return _p1;
    }
    
    public Hep3Vector getP2()
    {
        return _p2;
    }
    
    public double getEloss()
    {
        return _energy_loss;
    }
    
    public Hep3Vector getVector()
    {
        return VecOp.sub(_p2,_p1);
    }
    
    public Hep3Vector getDirection()
    {
        return VecOp.unit(getVector());
    }
    
    public double getLength()
    {
        return getVector().magnitude();
    }
    
    public double getDedx()
    {
        return _energy_loss/getLength();
    }
    
    public void transform(ITransform3D transformation)
    {
        transformation.transform(_p1);
        transformation.transform(_p2);
    }
    
}
