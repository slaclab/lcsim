package org.lcsim.geometry.subdetector;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.detector.ITransform3D;
import org.lcsim.detector.RotationGeant;
import org.lcsim.detector.Transform3D;
import org.lcsim.detector.Translation3D;

/**
 * 
 * @author Jeremy McCormick
 * @version $Id: TubeSegment.java,v 1.4 2010/12/03 01:21:39 jeremy Exp $
 */

public class TubeSegment extends AbstractSubdetector
{
    double rmin, rmax, zhalf;
    ITransform3D trans;

    public TubeSegment( Element node ) throws JDOMException
    {
        super( node );
        Element tubs = node.getChild( "tubs" );

        rmin = tubs.getAttribute( "rmin" ).getDoubleValue();
        rmax = tubs.getAttribute( "rmax" ).getDoubleValue();
        zhalf = tubs.getAttribute( "zhalf" ).getDoubleValue();

        Element pos = node.getChild( "position" );
        double x, y, z;
        x = y = z = 0;
        if ( pos != null )
        {
            try
            {
                x = pos.getAttribute( "x" ).getDoubleValue();
            }
            catch ( Exception ex )
            {
            }
            try
            {
                y = pos.getAttribute( "y" ).getDoubleValue();
            }
            catch ( Exception ex )
            {
            }
            try
            {
                z = pos.getAttribute( "z" ).getDoubleValue();
            }
            catch ( Exception ex )
            {
            }
        }

        Element rot = node.getChild( "rotation" );
        double rx, ry, rz;
        rx = ry = rz = 0;
        if ( rot != null )
        {
            try
            {
                rx = rot.getAttribute( "x" ).getDoubleValue();
            }
            catch ( Exception ex )
            {
            }
            try
            {
                ry = rot.getAttribute( "y" ).getDoubleValue();
            }
            catch ( Exception ex )
            {
            }
            try
            {
                rz = rot.getAttribute( "z" ).getDoubleValue();
            }
            catch ( Exception ex )
            {
            }
        }

        trans = new Transform3D( new Translation3D( x, y, z ), new RotationGeant( rx, ry, rz ) );
    }

    public void appendHepRep( HepRepFactory factory, HepRep heprep )
    {
        HepRepInstanceTree instanceTree = heprep.getInstanceTreeTop( "Detector", "1.0" );
        HepRepTypeTree typeTree = heprep.getTypeTree( "DetectorType", "1.0" );

        HepRepType barrel = typeTree.getType( "Barrel" );

        HepRepType type = factory.createHepRepType( barrel, getName() );
        
        type.addAttValue( "drawAs", "Cylinder" );
        type.addAttValue( "color", getVisAttributes().getColor() );

        double zmin = -zhalf;
        double zmax = zhalf;

        Hep3Vector point1 = trans.transformed( new BasicHep3Vector( 0, 0, zmin ) );
        Hep3Vector point2 = trans.transformed( new BasicHep3Vector( 0, 0, zmax ) );

        HepRepInstance instance = factory.createHepRepInstance( instanceTree, type );
        instance.addAttValue( "radius", rmin );
        factory.createHepRepPoint( instance, point1.x(), point1.y(), point1.z() );
        factory.createHepRepPoint( instance, point2.x(), point2.y(), point2.z() );

        HepRepInstance instance2 = factory.createHepRepInstance( instanceTree, type );
        instance2.addAttValue( "radius", rmax );
        factory.createHepRepPoint( instance2, point1.x(), point1.y(), point1.z() );
        factory.createHepRepPoint( instance2, point2.x(), point2.y(), point2.z() );
    }

    public final double getInnerRadius()
    {
        return rmin;
    }

    public final double getOuterRadius()
    {
        return rmax;
    }

    public final double getZHalfLength()
    {
        return zhalf;
    }

    public ITransform3D getTransform()
    {
        return trans;
    }
}