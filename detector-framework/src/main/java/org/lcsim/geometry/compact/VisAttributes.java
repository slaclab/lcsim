package org.lcsim.geometry.compact;

import java.awt.Color;

import org.jdom.DataConversionException;
import org.jdom.Element;

/**
 * This class stores visualization settings for Subdetector objects.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: VisAttributes.java,v 1.5 2010/12/03 01:21:14 jeremy Exp $
 *
 */
public class VisAttributes
{
    // Visualization attributes.
    private float[] rgba = new float[4];
    private Color color = null;
    private String name = null;
    private String linestyle = "unbroken";
    private String drawingstyle = "wireframe";
    private boolean visible = true;
    private boolean showdaughters = true;

    /**
     * Constructor with name and defaults.
     * @param name The name of the visualization settings.
     */
    public VisAttributes( String name )
    {
        this.name = name;
    }

    /**
     * Constructor that takes an XML element in the compact format.
     * @param node The XML node.
     */
    protected VisAttributes( Element node )
    {
        try
        {
            this.name = node.getAttributeValue( "name" );

            // Create a Java Color from the input RGBA values.
            if ( node.getAttribute( "r" ) != null )
            {
                this.rgba[ 0 ] = (float) node.getAttribute( "r" ).getDoubleValue();
            }

            if ( node.getAttribute( "g" ) != null )
            {
                this.rgba[ 1 ] = (float) node.getAttribute( "g" ).getDoubleValue();
            }

            if ( node.getAttribute( "b" ) != null )
            {
                this.rgba[ 2 ] = (float) node.getAttribute( "b" ).getDoubleValue();
            }

            if ( node.getAttribute( "alpha" ) != null )
            {
                this.rgba[ 3 ] = (float) node.getAttribute( "alpha" ).getDoubleValue();
            }

            // Create the Java Color from the RGBA input values.
            this.color = new Color( rgba[ 0 ], rgba[ 1 ], rgba[ 2 ], rgba[ 3 ] );

            if ( node.getAttribute( "lineStyle" ) != null )
            {
                this.linestyle = node.getAttributeValue( "lineStyle" );
            }

            if ( node.getAttribute( "showDaughters" ) != null )
            {
                this.showdaughters = node.getAttribute( "showDaughters" )
                        .getBooleanValue();
            }

            if ( node.getAttribute( "visible" ) != null )
            {
                this.visible = node.getAttribute( "visible" ).getBooleanValue();
            }

            if ( node.getAttribute( "drawingStyle" ) != null )
            {
                this.drawingstyle = node.getAttributeValue( "drawingStyle" );
            }
        }
        catch ( DataConversionException e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     * Set visibility.
     * @param visible True to make visible; false to make invisible.
     */
    public void setVisible( boolean visible )
    {
        this.visible = visible;
    }

    /**
     * Set colors between 0.0 and 1.0.
     * @param r Red color component value.
     * @param g Green color component value.
     * @param b Blue color component value.
     * @param a Alpha component value.
     */
    public final void setColor( float r, float g, float b, float a )
    {
        rgba[ 0 ] = r;
        rgba[ 1 ] = g;
        rgba[ 2 ] = b;
        rgba[ 3 ] = a;

        color = new Color( r, g, b, a );
    }

    /**
     * Set the color from an {@see java.awt.Color} object.
     * @param color The java Color.
     */
    public final void setColor( Color color )
    {
        this.color = color;

        rgba[ 0 ] = color.getRed();
        rgba[ 1 ] = color.getGreen();
        rgba[ 2 ] = color.getBlue();
        rgba[ 3 ] = color.getAlpha();
    }

    /**
     * Set whether daughters are visible.
     * @param b True to show daughters; false to hide.
     */
    public final void setShowDaughters( boolean b )
    {
        showdaughters = b;
    }

    /**
     * Set the drawing style.
     * @param drawingstyle
     */
    public final void setDrawingStyle( String drawingstyle )
    {
        this.drawingstyle = drawingstyle;
    }

    /**
     * Set the lineStyle of these attributes.
     * @param s The lineStyle.
     */
    public final void setLineStyle( String s )
    {
        this.linestyle = s;
    }

    /**
     * Get the <code>Color</code> of these settings.
     * @return The Color.
     */
    public final Color getColor()
    {
        return color;
    }

    /**
     * Get whether visibility is on or off. 
     * @return True if visible; false if not.
     */
    public final boolean getVisible()
    {
        return visible;
    }
    
    /**
     * Get whether visibility is on or off. 
     * @return True if visible; false if not.
     */
    public final boolean isVisible()
    {
        return visible;
    }

    /**
     * Get showDaughters setting.
     * @return showDaughters setting.
     */
    public final boolean getShowDaughters()
    {
        return showdaughters;
    }

    /**
     * Get the drawing style.  
     * Possible settings same as Geant4.
     * @return The drawing style.
     */
    public final String getDrawingStyle()
    {
        return drawingstyle;
    }

    /**
     * Get the line style.  
     * Possible settings same as Geant4.
     * @return The line style.
     */
    public final String getLineStyle()
    {
        return linestyle;
    }

    /**
     * Get the name of these visualization settings.
     * @return The vis name.
     */
    public final String getName()
    {
        return name;
    }

    /**
     * Get the RGBA values as a float array.
     * @return The RGBA values.
     */
    public final float[] getRGBA()
    {
        return rgba;
    }
}