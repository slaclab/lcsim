package org.lcsim.detector.converter.compact;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.lcsim.units.clhep.Constants;
import org.lcsim.util.xml.JDOMExpressionFactory;

public class CompactDocumentBuilder
{
    public static Document build( String resource ) throws JDOMException, IOException
    {
        return build( CompactDocumentBuilder.class.getResourceAsStream( resource ) );
    }

    public static Document build( InputStream in ) throws JDOMException, IOException
    {
        JDOMExpressionFactory eval = new JDOMExpressionFactory();
        registerCLHEPConstants( eval );
        SAXBuilder builder = new SAXBuilder();
        builder.setFactory( eval );
        Document doc = builder.build( in );
        return doc;
    }

    public static void registerCLHEPConstants( JDOMExpressionFactory f )
    {
        Constants units = Constants.getInstance();
        for ( Entry< String, Double > unit : units.entrySet() )
        {
            f.addConstant( unit.getKey(), unit.getValue() );
        }
    }
}