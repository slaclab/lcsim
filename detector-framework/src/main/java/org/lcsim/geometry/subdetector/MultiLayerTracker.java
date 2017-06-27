package org.lcsim.geometry.subdetector;

import java.util.Iterator;
import java.util.List;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jdom.JDOMException;
import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;
import org.lcsim.geometry.layer.LayerStack;

/**
 * 
 * @author tonyj
 * 
 */
public class MultiLayerTracker extends AbstractTracker
{
    // FIXME: This is a bad way to store geometry data for each layer.
    private double[] innerR;
    private double[] outerZ;

    // FIXME: This duplicates functionality provided by the layering engine.
    private double[] thickness;

    public double[] getInnerR()
    {
        return ( innerR );
    }

    public double[] getOuterZ()
    {
        return ( outerZ );
    }

    MultiLayerTracker( Element node ) throws JDOMException
    {
        super( node );
        build( node );
    }

    public boolean isBarrel()
    {
        return true;
    }

    private void build( Element node ) throws DataConversionException
    {
        List layers = node.getChildren( "layer" );
        int n = layers.size();
        innerR = new double[ n ];
        outerZ = new double[ n ];
        thickness = new double[ n ];
        LayerStack layerStack = getLayering().getLayerStack();
        double prevOuterR = 0;
        double thisOffset = 0;
        for ( int i = 0; i < n; i++ )
        {
            Element layer = ( Element ) layers.get( i );

            innerR[ i ] = layer.getAttribute( "inner_r" ).getDoubleValue();
            outerZ[ i ] = layer.getAttribute( "outer_z" ).getDoubleValue();

            /* Base offset for this layer */
            thisOffset = innerR[ i ];

            /* Subtract the previous outerR to get distance between adjacent layers */
            thisOffset -= prevOuterR;

            /* Set next outerR */
            prevOuterR = innerR[ i ];

            /* Store the pre-offset into the layer object for distance calcs */
            layerStack.getLayer( i ).setPreOffset( thisOffset );

            thickness[ i ] = 0;
            for ( Iterator iter = layer.getChildren( "slice" ).iterator(); iter.hasNext(); )
            {
                Element slice = ( Element ) iter.next();
                thickness[ i ] += slice.getAttribute( "thickness" ).getDoubleValue();
            }

            /* Incr next outerR by thickness of this layer */
            prevOuterR += thickness[ i ];
        }
        // System.out.println("layering total thickness = " +
        // layerStack.getTotalThickness());
    }

    public void appendHepRep( HepRepFactory factory, HepRep heprep )
    {
        HepRepInstanceTree instanceTree = heprep.getInstanceTreeTop( "Detector", "1.0" );
        HepRepTypeTree typeTree = heprep.getTypeTree( "DetectorType", "1.0" );
        HepRepType barrel = typeTree.getType( "Barrel" );

        HepRepType type = factory.createHepRepType( barrel, getName() );
        
        type.addAttValue( "drawAs", "Cylinder" );
        type.addAttValue( "color", getVisAttributes().getColor() );
        
        for ( int i = 0; i < innerR.length; i++ )
        {
            HepRepInstance instance = factory.createHepRepInstance( instanceTree, type );
            instance.addAttValue( "radius", innerR[ i ] );
            factory.createHepRepPoint( instance, 0, 0, -outerZ[ i ] );
            factory.createHepRepPoint( instance, 0, 0, outerZ[ i ] );

            HepRepInstance instance2 = factory.createHepRepInstance( instanceTree, type );
            instance2.addAttValue( "radius", innerR[ i ] + thickness[ i ] );
            factory.createHepRepPoint( instance2, 0, 0, -outerZ[ i ] );
            factory.createHepRepPoint( instance2, 0, 0, outerZ[ i ] );
        }
    }
}