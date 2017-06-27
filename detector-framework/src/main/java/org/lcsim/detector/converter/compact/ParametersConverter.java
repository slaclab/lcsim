package org.lcsim.detector.converter.compact;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.detector.IParameters;
import org.lcsim.detector.Parameters;
import org.lcsim.detector.ParametersStore;
import org.lcsim.detector.converter.XMLConverter;

/**
 * Converts parameters from XML to {@link org.lcsim.detector.IParameters}.
 * 
 * Handles the following XML elements.
 * 
 * <ul>
 * <li>intParameter</li>
 * <li>intArrayParameter</li>
 * <li>doubleParameter</li>
 * <li>doubleArrayParameter</li>
 * <li>stringParameter</li>
 * <li>stringArrayParameter</li>
 * </ul>
 * 
 * All of these must have an attribute called <tt>name</tt> and valid content.
 * 
 * @author Jeremy McCormick
 * @version $Id: ParametersConverter.java,v 1.2 2010/11/30 00:16:27 jeremy Exp $
 */

public class ParametersConverter implements XMLConverter
{
    public void convert( Element element ) throws JDOMException
    {
        if ( element.getAttribute( "name" ) == null )
        {
            throw new RuntimeException( "Top element doesn't have a name attribute." );
        }

        IParameters param = new Parameters( element.getAttributeValue( "name" ) );

        for ( Object o : element.getChildren() )
        {
            Element e = ( Element ) o;
            String elementName = e.getName();
            if ( elementName.contains( "Parameter" ) )
            {
                if ( e.getAttribute( "name" ) == null )
                {
                    System.out.println( "Parameter is missing the name attribute." );
                }

                String paramName = e.getAttributeValue( "name" );
                String value = e.getValue();

                if ( value == null )
                {
                    System.out.println( "Parameter <" + paramName + "> is missing a value." );
                }

                String[] values = value.split( " " );

                if ( elementName.equals( "integerParameter" ) )
                {
                    param.addIntegerParameter( paramName, Integer.parseInt( value ) );
                }
                else if ( elementName.equals( "integerArrayParameter" ) )
                {
                    int[] intValues = new int[ values.length ];
                    for ( int i = 0; i < values.length; i++ )
                    {
                        if ( values[ i ] != null && !values[ i ].equals( "" ) )
                            intValues[ i ] = Integer.parseInt( values[ i ] );
                    }
                    param.addIntegerArrayParameter( paramName, intValues );
                }
                else if ( elementName.equals( "doubleParameter" ) )
                {
                    param.addDoubleParameter( paramName, Double.parseDouble( value ) );
                }
                else if ( elementName.equals( "doubleArrayParameter" ) )
                {
                    double[] doubleValues = new double[ values.length ];
                    for ( int i = 0; i < values.length; i++ )
                    {
                        if ( values[ i ] != null && !values[ i ].equals( "" ) )
                            doubleValues[ i ] = Double.parseDouble( values[ i ] );
                    }
                    param.addDoubleArrayParameter( paramName, doubleValues );
                }
                else if ( elementName.equals( "stringParameter" ) )
                {
                    param.addStringParameter( paramName, value );
                }
                else if ( elementName.equals( "stringArrayParameter" ) )
                {
                    String[] stringValues = new String[ values.length ];
                    for ( int i = 0; i < values.length; i++ )
                    {
                        if ( values[ i ] != null && !values[ i ].equals( "" ) )
                            stringValues[ i ] = values[ i ];
                    };
                    param.addStringArrayParameter( paramName, stringValues );
                }
                else if ( elementName.equals( "booleanParameter" ) )
                {
                    param.addBooleanParameter( paramName, Boolean.parseBoolean( value ) );
                }
                else if ( elementName.equals( "booleanArrayParameter" ) )
                {
                    boolean[] booleanValues = new boolean[ values.length ];
                    for ( int i = 0; i < values.length; i++ )
                    {
                        if ( values[ i ] != null && !values[ i ].equals( "" ) )
                            booleanValues[ i ] = Boolean.parseBoolean( values[ i ] );
                    }
                    param.addBooleanArrayParameter( paramName, booleanValues );
                }
            }
        }

        ParametersStore.getInstance().add( param );
    }
}
