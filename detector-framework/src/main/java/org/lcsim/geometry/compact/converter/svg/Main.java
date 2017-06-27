/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: Main.java,v 1.1 2011/01/26 01:19:39 jeremy Exp $
 */
package org.lcsim.geometry.compact.converter.svg;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.filechooser.FileFilter;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.geometry.compact.converter.Converter;

public class Main implements Converter
{
    /**
     * @param args the command line arguments
     */
    public static void main( String[] args ) throws Exception
    {
        if ( args.length < 1 || args.length > 2 )
            usage();
        InputStream in = new BufferedInputStream( new FileInputStream( args[ 0 ] ) );
        OutputStream out = args.length == 1 ? System.out : new BufferedOutputStream( new FileOutputStream( args[ 1 ] ) );
        new Main().convert( args[ 0 ], in, out );
    }

    public Main()
    {}

    public void convert( String inputFileName, InputStream in, OutputStream out ) throws Exception
    {
        // Read in detector.
        GeometryReader reader = new GeometryReader();
        Detector detector = reader.read( in );

        // Create the HTML.
        Element root = SvgConverter.convert( detector );

        // Create the document.
        Document doc = new Document();
        doc.setRootElement( root );                

        // Write out the document.
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat( Format.getPrettyFormat() );
        outputter.output( doc, out );
        out.close();
    }

    private static void usage()
    {
        System.out.println( "java " + Main.class.getName() + " <compact> [<svg>]" );
        System.exit( 0 );
    }

    public FileFilter getFileFilter()
    {
        return new HepRepFileFilter();
    }

    public String getOutputFormat()
    {
        return "svg";
    }

    private static class HepRepFileFilter extends FileFilter
    {
        public boolean accept( java.io.File file )
        {
            return file.isDirectory() || file.getName().endsWith( ".svg" );
        }

        public String getDescription()
        {
            return "HTML file (*.svg)";
        }
    }
}