package org.lcsim.geometry.subdetector;

import org.jdom.Element;
import org.jdom.JDOMException;
import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;
import org.lcsim.geometry.layer.LayerStack;

import java.util.Iterator;
import java.util.List;
import org.jdom.DataConversionException;

/**
 * @author Tony Johnson
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: DiskTracker.java,v 1.14 2010/12/03 01:21:39 jeremy Exp $
 */
public class DiskTracker extends AbstractTracker
{
    private double[] innerR;
    private double[] outerR;
    private double[] innerZ;
    private double[] thickness;

    public double[] getInnerR()
    {
        return ( innerR );
    }

    public double[] getOuterR()
    {
        return ( outerR );
    }

    public double[] getInnerZ()
    {
        return ( innerZ );
    }

    public double[] getThickness()
    {
        return ( thickness );
    }

    DiskTracker( Element node ) throws JDOMException
    {
        super( node );
        build( node );
    }

    public boolean isEndcap()
    {
        return true;
    }

    private void build( Element node ) throws DataConversionException
    {
        List layers = node.getChildren( "layer" );
        int n = layers.size();
        innerR = new double[ n ];
        outerR = new double[ n ];
        innerZ = new double[ n ];

        thickness = new double[ n ];
        LayerStack layerStack = getLayering().getLayerStack();
        double prevOuterZ = 0;
        double thisOffset = 0;
        for ( int i = 0; i < n; i++ )
        {
            Element layer = ( Element ) layers.get( i );
            innerR[ i ] = layer.getAttribute( "inner_r" ).getDoubleValue();
            outerR[ i ] = layer.getAttribute( "outer_r" ).getDoubleValue();
            innerZ[ i ] = layer.getAttribute( "inner_z" ).getDoubleValue();

            /* Base offset for this layer */
            thisOffset = innerZ[ i ];

            /* Subtract the previous outerZ to get distance between adjacent layers */
            thisOffset -= prevOuterZ;

            /* Set next outerZ */
            prevOuterZ = innerZ[ i ];

            /* Store the pre-offset into the layer object for distance calcs */
            layerStack.getLayer( i ).setPreOffset( thisOffset );

            thickness[ i ] = 0;
            for ( Iterator iter = layer.getChildren( "slice" ).iterator(); iter.hasNext(); )
            {
                Element slice = ( Element ) iter.next();
                thickness[ i ] += slice.getAttribute( "thickness" ).getDoubleValue();
            }

            /* Incr next outerZ by thickness of this layer */
            prevOuterZ += thickness[ i ];
        }

        // System.out.println("DiskTracker total thickness=" +
        // getLayering().getThickness());
    }

    public void appendHepRep( HepRepFactory factory, HepRep heprep )
    {
        HepRepInstanceTree instanceTree = heprep.getInstanceTreeTop( "Detector", "1.0" );
        HepRepTypeTree typeTree = heprep.getTypeTree( "DetectorType", "1.0" );
        HepRepType endcap = typeTree.getType( "Endcap" );

        HepRepType type = factory.createHepRepType( endcap, getName() );
        type.addAttValue( "drawAs", "Cylinder" );

        type.addAttValue( "color", getVisAttributes().getColor() );

        double flip = 1;
        for ( ;; )
        {
            for ( int i = 0; i < innerR.length; i++ )
            {
                HepRepInstance instance = factory.createHepRepInstance( instanceTree, type );
                instance.addAttValue( "radius", innerR[ i ] );
                factory.createHepRepPoint( instance, 0, 0, flip * innerZ[ i ] );
                factory.createHepRepPoint( instance, 0, 0, flip * ( innerZ[ i ] + thickness[ i ] ) );

                HepRepInstance instance2 = factory.createHepRepInstance( instanceTree, type );
                instance2.addAttValue( "radius", outerR[ i ] );
                factory.createHepRepPoint( instance2, 0, 0, flip * innerZ[ i ] );
                factory.createHepRepPoint( instance2, 0, 0, flip * ( innerZ[ i ] + thickness[ i ] ) );
            }
            if ( !getReflect() || flip < 0 )
                break;
            flip = -1;
        }
    }
}