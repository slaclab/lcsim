package org.lcsim.detector.converter.heprep;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepTreeID;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;
import hep.graphics.heprep.HepRepWriter;
import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import java.awt.Color;
import java.io.FileOutputStream;
import java.util.List;

import org.lcsim.detector.DetectorElementStore;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IGeometryInfo;
import org.lcsim.detector.solids.IPolyhedron;
import org.lcsim.detector.solids.ISolid;
import org.lcsim.detector.solids.IsoscelesTrapezoid;
import org.lcsim.detector.solids.Point3D;
import org.lcsim.detector.solids.RegularPolygon;
import org.lcsim.detector.solids.RightIsoscelesTrapezoid;
import org.lcsim.detector.solids.RightRegularPolyhedron;
import org.lcsim.detector.solids.Tube;

/**
 * This class converters from an {@link org.lcsim.detector.IDetectorElement} to an
 * in-memory HepRep description.
 * 
 * @see org.lcsim.detector.IDetectorElement
 * @see hep.graphics.heprep
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: DetectorElementToHepRepConverter.java,v 1.25 2010/12/03 01:20:24 jeremy Exp $
 */
public class DetectorElementToHepRepConverter
{
    /**
     * This is the primary conversion method. It recursively translates a DetectorElement
     * into the HepRep format.
     * 
     * @param detelem The DetectorElement.
     * @param factory The HepRepFactory object.
     * @param heprep The HepRep object.
     * @param parentType The parent HepRepType.
     * @param currentDepth The current depth of the DetectorElement traversal.
     * @param maxDepth The max depth of the DetectorElement traversal.
     * @param endcap True if DetectorElement is an endcap; false if not.
     */
    public static void convert( IDetectorElement detelem,
            HepRepFactory factory,
            HepRep heprep,
            HepRepType parentType,
            int currentDepth,
            int maxDepth,
            boolean endcap,
            Color color )
    {
        // Check for null DetectorElement.  Fail silently because this can 
        // happen if component has no detailed geometry representation.
        if ( detelem == null )
            //throw new RuntimeException( "DetectorElement points to null!" );
            return;

        // If we are at max depth, then don't continue.
        // Since depth starts at 0, less than or equal is used.
        if ( maxDepth != -1 && currentDepth >= maxDepth )
            return;
        
        //System.out.println("converting: " + detelem.getName());

        // Get the detector InstanceTree and TypeTree.
        HepRepInstanceTree instanceTree = heprep.getInstanceTreeTop( "Detector", "1.0" );
        HepRepTypeTree typeTree = heprep.getTypeTree( "DetectorType", "1.0" );

        // Find this component's base type.
        HepRepType baseType;
        if ( parentType != null )
        {
            baseType = parentType;
        }
        else
        {
            baseType = typeTree.getType( "Barrel" );
            if ( endcap )
            {
                baseType = typeTree.getType( "Endcap" );
            }
        }

        // Create a type for this DetectorElement.
        HepRepType newType = factory.createHepRepType( baseType, detelem.getName() );
        
        // Assign color to type.
        if ( color != null )
        {
            newType.addAttValue( "color", color );
            //System.out.println("  color: " + color);
        }            
        
        // Add geometric information to HepRep if there is a geometry
        // associated with this DetectorElement.
        if ( detelem.hasGeometryInfo() )
        {                        
            IGeometryInfo geo = detelem.getGeometry();
            ISolid solid = geo.getLogicalVolume().getSolid();

            if ( solid instanceof IPolyhedron )
            {
                IPolyhedron polyhedron = ( IPolyhedron ) geo.getLogicalVolume()
                        .getSolid();

                newType.addAttValue( "drawAs", "Prism" );
                                                                
                HepRepInstance instance = factory.createHepRepInstance(
                        instanceTree,
                        newType );
                
                List< Point3D > points = polyhedron.getVertices();
                int[] point_ordering = polyhedron.getHepRepVertexOrdering();

                for ( int i = 0; i < point_ordering.length; i++ )
                {
                    Hep3Vector p = geo.transformLocalToGlobal( points
                            .get( point_ordering[ i ] ) );
                    factory.createHepRepPoint( instance, p.x(), p.y(), p.z() );
                }                
            }
            else if ( solid instanceof Tube )
            {                
                Tube tube = ( Tube ) geo.getLogicalVolume().getSolid();

                newType.addAttValue( "drawAs", "Cylinder" );
                
                double zmin = -tube.getZHalfLength();
                double zmax = tube.getZHalfLength();

                Hep3Vector point1 = new BasicHep3Vector( 0, 0, zmin );
                Hep3Vector point2 = new BasicHep3Vector( 0, 0, zmax );

                point1 = geo.transformLocalToGlobal( point1 );
                point2 = geo.transformLocalToGlobal( point2 );

                HepRepInstance instance = factory.createHepRepInstance(
                        instanceTree,
                        newType );
                                
                instance.addAttValue( "radius", tube.getInnerRadius() );
                factory.createHepRepPoint( instance, point1.x(), point1.y(), point1.z() );
                factory.createHepRepPoint( instance, point2.x(), point2.y(), point2.z() );

                HepRepInstance instance2 = factory.createHepRepInstance(
                        instanceTree,
                        newType );
                instance2.addAttValue( "radius", tube.getOuterRadius() );
                factory.createHepRepPoint( instance2, point1.x(), point1.y(), point1.z() );
                factory.createHepRepPoint( instance2, point2.x(), point2.y(), point2.z() );
            }
            else if ( solid instanceof RightRegularPolyhedron )
            {
                RightRegularPolyhedron poly = ( RightRegularPolyhedron ) geo
                        .getLogicalVolume().getSolid();

                newType.addAttValue( "drawAs", "Prism" );
                                
                HepRepInstance instance = factory.createHepRepInstance(
                        instanceTree,
                        newType );

                // Outer polygon.
                RegularPolygon outerPolygon = poly.getOuterPolygon();
                Hep3Vector vertices[] = outerPolygon.getVertices();
                double z = poly.getZMin();
                for ( int i = 0; i < 2; i++ )
                {
                    for ( int j = 0; j < vertices.length; j++ )
                    {
                        Hep3Vector point = vertices[ j ];
                        Hep3Vector tpoint = new BasicHep3Vector( point.x(), point.y(), z );
                        tpoint = geo.transformLocalToGlobal( tpoint );

                        factory.createHepRepPoint(
                                instance,
                                tpoint.x(),
                                tpoint.y(),
                                tpoint.z() );
                    }
                    z = poly.getZMax();
                }

                // Inner polygon.
                if ( poly.isHollow() )
                {
                    instance = factory.createHepRepInstance( instanceTree, newType );
                    RegularPolygon innerPolygon = poly.getInnerPolygon();
                    vertices = innerPolygon.getVertices();
                    z = poly.getZMin();
                    for ( int i = 0; i < 2; i++ )
                    {
                        for ( int j = 0; j < vertices.length; j++ )
                        {
                            Hep3Vector point = vertices[ j ];
                            Hep3Vector tpoint = new BasicHep3Vector( point.x(),
                                                                     point.y(),
                                                                     z );
                            tpoint = geo.transformLocalToGlobal( tpoint );

                            factory.createHepRepPoint(
                                    instance,
                                    tpoint.x(),
                                    tpoint.y(),
                                    tpoint.z() );
                        }
                        z = poly.getZMax();
                    }
                }
            }
            else if ( solid instanceof RightIsoscelesTrapezoid )
            {
                RightIsoscelesTrapezoid poly = ( RightIsoscelesTrapezoid ) geo
                        .getLogicalVolume().getSolid();

                newType.addAttValue( "drawAs", "Prism" );
                                
                HepRepInstance instance = factory.createHepRepInstance(
                        instanceTree,
                        newType );

                // Outer polygon.
                IsoscelesTrapezoid face = poly.face();
                Hep3Vector vertices[] = face.getVertices();
                double z = poly.zMin();
                for ( int i = 0; i < 2; i++ )
                {
                    for ( int j = 0; j < vertices.length; j++ )
                    {
                        Hep3Vector point = vertices[ j ];
                        Hep3Vector tpoint = new BasicHep3Vector( point.x(), point.y(), z );
                        tpoint = geo.transformLocalToGlobal( tpoint );

                        factory.createHepRepPoint(
                                instance,
                                tpoint.x(),
                                tpoint.y(),
                                tpoint.z() );
                    }
                    z = poly.zMax();
                }
            }
        }
        
        // Process children recursively.
        if ( detelem.hasChildren() )
        {
            for ( IDetectorElement child : detelem.getChildren() )
            {
                // Passing color is not necessary, as child types
                // should pickup from their parent.
                DetectorElementToHepRepConverter.convert(
                        child,
                        factory,
                        heprep,
                        newType,
                        currentDepth + 1,
                        maxDepth,
                        endcap,
                        null );
            }
        }
    }

    /**
     * Convert from DetectorElements to HepRep using default depth parameters.
     * 
     * @param detelem
     * @param factory
     * @param heprep
     * @param endcap
     */
    public static void convert( IDetectorElement detelem,
            HepRepFactory factory,
            HepRep heprep,
            boolean endcap,
            Color color )
    {
        convert( detelem, factory, heprep, -1, endcap, color );
    }

    /**
     * Convert from DetectorElements to HepRep, specifying the max depth parameter.
     * 
     * @param detelem
     * @param factory
     * @param heprep
     * @param maxDepth
     * @param endcap
     */
    public static void convert( IDetectorElement detelem,
            HepRepFactory factory,
            HepRep heprep,
            int maxDepth,
            boolean endcap,
            Color color )
    {
        convert( detelem, factory, heprep, null, 0, maxDepth, endcap, color );
    }

    // FIXME Duplicate constants.
    public final static String HITS_LAYER = "Hits";
    public final static String PARTICLES_LAYER = "Particles";

    // This method is used by some test cases but shouldn't be used in
    // any "production" code due to a few oddities/bugs.
    //
    // TODO This needs to put components under correct Barrel or Endcap type.
    // Currently, everything goes into Barrel.
    public static void writeHepRep( String filepath ) throws Exception
    {
        HepRepFactory factory = HepRepFactory.create();
        HepRep root = factory.createHepRep();

        // detector

        HepRepTreeID treeID = factory.createHepRepTreeID( "DetectorType", "1.0" );
        HepRepTypeTree typeTree = factory.createHepRepTypeTree( treeID );
        root.addTypeTree( typeTree );

        HepRepInstanceTree instanceTree = factory.createHepRepInstanceTree(
                "Detector",
                "1.0",
                typeTree );
        root.addInstanceTree( instanceTree );

        String detectorLayer = "Detector";
        root.addLayer( detectorLayer );

        HepRepType barrel = factory.createHepRepType( typeTree, "Barrel" );
        barrel.addAttValue( "layer", detectorLayer );
        HepRepType endcap = factory.createHepRepType( typeTree, "Endcap" );
        endcap.addAttValue( "layer", detectorLayer );

        for ( IDetectorElement de : DetectorElementStore.getInstance() )
        {
            DetectorElementToHepRepConverter.convert( de, factory, root, false, null );
        }

        // end detector

        root.addLayer( PARTICLES_LAYER );
        root.addLayer( HITS_LAYER );
        root.addLayer( "axis" );

        treeID = factory.createHepRepTreeID( "EventType", "1.0" );
        typeTree = factory.createHepRepTypeTree( treeID );
        root.addTypeTree( typeTree );
        instanceTree = factory.createHepRepInstanceTree( "Event", "1.0", typeTree );
        root.addInstanceTree( instanceTree );

        // axis

        HepRepType axis = factory.createHepRepType( typeTree, "axis" );
        axis.addAttValue( "drawAs", "Line" );
        axis.addAttValue( "layer", "axis" );

        HepRepType xaxis = factory.createHepRepType( axis, "xaxis" );
        xaxis.addAttValue( "color", Color.RED );
        xaxis.addAttValue( "fill", true );
        xaxis.addAttValue( "fillColor", Color.RED );
        HepRepInstance x = factory.createHepRepInstance( instanceTree, xaxis );
        factory.createHepRepPoint( x, 0, 0, 0 );
        factory.createHepRepPoint( x, 1000, 0, 0 );

        HepRepType yaxis = factory.createHepRepType( axis, "yaxis" );
        yaxis.addAttValue( "color", Color.GREEN );
        yaxis.addAttValue( "fill", true );
        yaxis.addAttValue( "fillColor", Color.GREEN );
        HepRepInstance y = factory.createHepRepInstance( instanceTree, yaxis );
        factory.createHepRepPoint( y, 0, 0, 0 );
        factory.createHepRepPoint( y, 0, 1000, 0 );

        HepRepType zaxis = factory.createHepRepType( axis, "zaxis" );
        zaxis.addAttValue( "color", Color.BLUE );
        zaxis.addAttValue( "fill", true );
        zaxis.addAttValue( "fillColor", Color.BLUE );
        HepRepInstance z = factory.createHepRepInstance( instanceTree, zaxis );
        factory.createHepRepPoint( z, 0, 0, 0 );
        factory.createHepRepPoint( z, 0, 0, 1000 );

        // done axis

        HepRepWriter writer = HepRepFactory.create().createHepRepWriter(
                new FileOutputStream( filepath ),
                false,
                false );
        writer.write( root, "test" );
        writer.close();
    }
}
