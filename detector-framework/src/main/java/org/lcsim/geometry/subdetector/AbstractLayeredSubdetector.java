/*
 * LayeredSubdetector.java
 * 
 * Created on July 17, 2005, 5:49 PM
 */

package org.lcsim.geometry.subdetector;

import hep.physics.vec.BasicHep3Vector;
import hep.physics.vec.Hep3Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.detector.material.BetheBlochCalculator;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.geometry.Layered;
import org.lcsim.geometry.layer.Layer;
import org.lcsim.geometry.layer.LayerSlice;
import org.lcsim.geometry.layer.Layering;
import org.lcsim.material.Material;
import org.lcsim.material.MaterialState;

/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: AbstractLayeredSubdetector.java,v 1.10 2011/03/11 19:22:20 jeremy Exp $
 */
abstract public class AbstractLayeredSubdetector extends AbstractSubdetector implements Layered
{
    protected Layering layering;

    private List<Double> nrad;
    private List<Double> nlam;
    private List<Double> de;
    private Map<String, Double> dedxmap = new HashMap<String, Double>();
    
    private double intLens;
    private double radLens;

    /**
     * Creates a new instance of a LayeredSubdetector
     */
    public AbstractLayeredSubdetector( Element node ) throws JDOMException
    {
        super( node );
        build( node );

        // Initialize parameter arrays using layer count.
        nrad = new ArrayList<Double>( this.getLayering().getNumberOfLayers() );
        de = new ArrayList<Double>( this.getLayering().getNumberOfLayers() );
        nlam = new ArrayList<Double>( this.getLayering().getNumberOfLayers() );

        // Compute layer derived quantities.
        computeLayerParameters();
    }

    private void build( Element node ) throws JDOMException
    {
        try
        {
            // Setup layering object.
            layering = org.lcsim.geometry.layer.Layering.makeLayering( node );
        }
        catch ( JDOMException x )
        {
            throw new RuntimeException( x );
        }
    }

    public boolean isLayered()
    {
        return true;
    }

    public Layering getLayering()
    {
        return layering;
    }
    
    public double getTotalThickess()
    {
        return layering.getThickness();
    }

    protected void setLayering( Layering layering )
    {
        // May only be called once at initialization time.
        if ( this.layering == null )
            this.layering = layering;
    }

    public Layer getLayer( int layern )
    {
        return this.layering.getLayer( layern );
    }

    public int getNumberOfLayers()
    {
        return this.layering.getLayerCount();
    }

    public double getDistanceToLayer( int layern )
    {
        return this.layering.getDistanceToLayer( layern );
    }

    public double getDistanceToSensor( int layern )
    {
        return this.layering.getDistanceToLayerSensorFront( layern );
    }

    public double getLayerThickness( int layern )
    {
        return this.layering.getLayer( layern ).getThickness();
    }

    public double getSensorThickness( int layern )
    {
        return this.layering.getLayer( layern ).getSensorThickness();
    }

    /**
     * Compute the radiation and interaction lengths for each layer of this subdetector.
     * FIXME Access to the dedx information by material name should be moved into
     * IMaterial interface because map is duplicated across subdetectors. The map could
     * also be made static.
     */    
    private void computeLayerParameters()
    {
        // System.out.println("nlayers = " + this.getLayering().getNumberOfLayers());

        // IMaterialStore ms = MaterialStore.getInstance();
        int nlayers = this.getNumberOfLayers();
        Hep3Vector p = new BasicHep3Vector( 0., 0., 100. );
        for ( int j = 0; j < nlayers; j++ )
        {
            // System.out.println("computing layer = " + j);
            Layer layer = getLayering().getLayer( j );
            double xrad = 0.;
            double xlam = 0.;
            double xde = 0.;
            for ( LayerSlice slice : layer.getSlices() )
            {
                Material m = slice.getMaterial();
                String materialName = m.getName();
                double dedx;
                if ( dedxmap.containsKey( materialName ) )
                    dedx = dedxmap.get( materialName ).doubleValue();
                else
                {
                    // Kludge to get material state to avoid using IMaterial objects that
                    // are not instantiated yet.
                    MaterialState state = m.getState();
                    IMaterial.State istate = null;
                    if ( state == MaterialState.GAS )
                    {
                        istate = IMaterial.Gas;
                    }
                    else if ( state == MaterialState.LIQUID )
                    {
                        istate = IMaterial.Liquid;
                    }
                    else if ( state == MaterialState.SOLID )
                    {
                        istate = IMaterial.Solid;
                    }
                    else if ( state == MaterialState.UNKNOWN )
                    {
                        istate = IMaterial.Unknown;
                    }
                    dedx = BetheBlochCalculator.computeBetheBloch( 
                            m.getZeff(), 
                            m.getAeff(), 
                            m.getDensity(), 
                            istate, 
                            Material.DEFAULT_PRESSURE,
                            Material.DEFAULT_TEMPERATURE,
                            p, 
                            105., 
                            1., 
                            .01 ) / 10000;
                    dedxmap.put( materialName, new Double( dedx ) );
                }
                double dx = slice.getThickness();
                xrad += dx / m.getRadiationLengthWithDensity();
                xlam += dx / m.getNuclearInteractionLengthWithDensity();
                xde += dx * dedx;
            }
            nrad.add( j, new Double( xrad / 10. ) );
            nlam.add( j, new Double( xlam / 10. ) );
            de.add( j, new Double( xde ) );
        }
        
        // Compute totals for all layers.
        for ( double lam : nlam )
        {
            intLens += lam;
        }
        for ( double rad : nrad )
        {
            radLens += rad;
        }
    }
    
    public double getInteractionLengths()
    {
        return intLens;
    }
    
    public double getRadiationLengths()
    {
        return radLens;
    }

    public double getInteractionLengths( int layern )
    {
        return nlam.get( layern );
    }

    public double getRadiationLengths( int layern )
    {
        return nrad.get( layern );
    }

    public double getDe( int layern )
    {
        return de.get( layern );
    }
}