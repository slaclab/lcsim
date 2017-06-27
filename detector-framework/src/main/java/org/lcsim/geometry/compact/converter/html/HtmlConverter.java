package org.lcsim.geometry.compact.converter.html;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.tracker.silicon.SiSensor;
import org.lcsim.geometry.Calorimeter;
import org.lcsim.geometry.compact.Constant;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.geometry.compact.Field;
import org.lcsim.geometry.compact.Header;
import org.lcsim.geometry.compact.Readout;
import org.lcsim.geometry.compact.Segmentation;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.field.Solenoid;
import org.lcsim.geometry.layer.Layer;
import org.lcsim.geometry.layer.LayerSlice;
import org.lcsim.geometry.layer.LayerStack;
import org.lcsim.geometry.subdetector.AbstractLayeredSubdetector;
import org.lcsim.geometry.subdetector.PolyconeSupport;
import org.lcsim.geometry.subdetector.SiTrackerBarrel;
import org.lcsim.geometry.subdetector.SiTrackerEndcap;
import org.lcsim.geometry.subdetector.SiTrackerEndcap2;
import org.lcsim.geometry.subdetector.TubeSegment;

/**
 * Convert a compact description to an html information page.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: HtmlConverter.java,v 1.8 2012/08/21 21:55:28 jeremy Exp $
 */
// TODO add css
// TODO add converter for MultiLayerTracker to layer table
// TODO add Subdetectors by type lookup table
// TODO order of Subdetector details needs to make more sense; similar things together (readout, geom, etc.)
class HtmlConverter
{
    static DecimalFormat layerForm = new DecimalFormat( "#.##" );
    static DecimalFormat lenForm = new DecimalFormat( "#.######" );
    static DecimalFormat geomForm = new DecimalFormat( "#.##");

    private HtmlConverter()
    {}

    public static Element convert( Detector d )
    {
        // Convert to specific subclass for more method access.
        Detector detector = ( org.lcsim.geometry.compact.Detector ) d;

        // Root element.
        Element root = new Element( "html" );

        // Header.
        Element head = new Element( "head" );
        String detectorName = detector.getName();
        Element title = new Element( "title" );
        title.setText( detectorName );
        head.addContent( title );
        root.addContent( head );

        // Body.
        Element body = new Element( "body" );
        root.addContent( body );

        // Layout table.
        Element tbl = new Element( "table" );
        tbl.setAttribute( "cellspacing", "25" );
        tbl.setAttribute( "width", "100%" );
        body.addContent( tbl );

        // Layout row.
        Element td = addLayoutRow( tbl );

        // Add detector header.
        detectorHeader( td, detector );

        // Layout row.
        td = addLayoutRow( tbl );

        // Links to defines and fields at bottom of page.
        Element p = new Element( "p" );
        p.addContent( new Link( "Go to Constants", "#defines" ) );
        td.addContent( p );
        p = new Element( "p" );
        p.addContent( new Link( "Go to Fields", "#fields" ) );
        td.addContent( p );

        // Sort subdetectors alphabetically.
        List<String> subdets = new ArrayList<String>();
        subdets.addAll( detector.getSubdetectors().keySet() );
        java.util.Collections.sort( subdets );

        // Layout row.
        td = addLayoutRow( tbl );

        // Add subdetector index.
        subdetectorIndex( td, detector, subdets );

        // Layout row.
        td = addLayoutRow( tbl );

        // Add system ID index.
        sysIdIndex( td, detector );

        // Layout row.
        td = addLayoutRow( tbl );

        // Add readout index.
        readoutIndex( td, detector );

        // Layout row.
        td = addLayoutRow( tbl );

        // Radiation and interaction lengths table.
        lengthTable( td, detector, subdets );

        // Header for subdetector details section.
        addHeader2( td, "Subdetector Details" );

        // Add subdetectors.
        for ( String subdetName : subdets )
        {
            // Layout row.
            td = addLayoutRow( tbl );

            // Add subdetector info.
            subdetector( td, detector.getSubdetector( subdetName ) );
        }

        // Layout row.
        td = addLayoutRow( tbl );

        // Constants.
        defines( td, detector );

        // Layout row.
        td = addLayoutRow( tbl );

        // Fields.
        fields( td, detector );

        // Layout row.
        td = addLayoutRow( tbl );

        // Timestamp footer.
        timestamp( td );

        return root;
    }

    private static Element layers( Subdetector subdet )
    {
        Element tbl = null;
        if ( subdet instanceof AbstractLayeredSubdetector )
        {
            AbstractLayeredSubdetector layered = ( AbstractLayeredSubdetector ) subdet;
            LayerStack layers = layered.getLayering().getLayerStack();
            if ( layers.getNumberOfLayers() > 0 )
            {
                tbl = new Element( "table" );
                tbl.setAttribute( "border", "1" );

                Layer currentLayer = null;
                int bottomLayer = 0;
                int topLayer = 0;
                for ( int i = 0, nlayers = layers.getNumberOfLayers(); i < nlayers; i++ )
                {
                    if ( currentLayer == null )
                    {
                        currentLayer = layers.getLayer( i );
                    }

                    // FIXME Use a better way than thickness to determine if different layer.
                    if ( currentLayer.getThickness() != layers.getLayer( i )
                            .getThickness() || i == nlayers - 1 )
                    {
                        if ( i == nlayers - 1 )
                            topLayer = i;
                        else 
                            topLayer = i - 1;

                        // Special case of only one layer.
                        if ( topLayer == -1 )
                            topLayer = 0;

                        Element sliceTbl = new Element( "table" );
                        sliceTbl.setAttribute( "cellpadding", "2" );

                        for ( LayerSlice slice : currentLayer.getSlices() )
                        {
                            addRow( sliceTbl,
                                    layerForm.format( slice.getThickness() ) + " mm",
                                    slice.getMaterial().getName(),
                                    slice.isSensitive() ? new Element("i").addContent( "sensor") : " " );
                        }

                        addRow( tbl, bottomLayer + " - " + topLayer, sliceTbl );

                        bottomLayer = i;
                        topLayer = i;
                        currentLayer = layers.getLayer( i );
                    }
                }
            }
        }

        return tbl;
    }

    private static void timestamp( Element parent )
    {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        String ts = new String( "Generated by GeomConverter at " + sdf.format( cal
                .getTime() ) + " by user " + System.getProperty( "user.name" ) + "." );

        parent.addContent( new Element( "p" ).setText( ts ) );
    }

    private static void lengthTable( Element parent,
            Detector detector,
            List<String> subdets )
    {
        // Header.
        addHeader2( parent, "Radiation and Interaction Lengths" );

        Element tbl = new Element( "table" );
        parent.addContent( tbl );
        tbl.setAttribute( "border", "1" );
        tbl.setAttribute( "cellpadding", "2" );

        // Table header row.
        addRow( tbl, 
                new Element( "b" ).setText( "Subdetector" ), 
                new Element( "b" ).setText( "Radiation Lengths" ), 
                new Element( "b" ).setText( "Interaction Lengths" ) );

        for ( String subdetName : subdets )
        {
            Subdetector subdet = detector.getSubdetector( subdetName );
            AbstractLayeredSubdetector layers;
            if ( subdet instanceof AbstractLayeredSubdetector )
            {
                layers = ( AbstractLayeredSubdetector ) subdet;
                addRow( tbl, 
                        new Link( subdetName, "#" + subdetName ), 
                        lenForm.format( layers.getRadiationLengths() ), 
                        lenForm.format( layers.getInteractionLengths() ) );
            }
        }
    }

    private static void sysIdIndex( Element parent, Detector detector )
    {
        // Header.
        addHeader2( parent, "Subdetectors by System ID" );

        // Table
        Element tbl = new Element( "table" );
        tbl.setAttribute( "border", "1" );
        tbl.setAttribute( "cellpadding", "2" );
        parent.addContent( tbl );

        // Table header row.
        addRow( tbl, new Element( "b" ).setText( "Sys Id" ), new Element( "b" )
                .setText( "Subdetector" ) );

        // Make system ID list, only including non-zero values.
        List<Integer> systemIds = new ArrayList<Integer>();
        for ( Subdetector subdet : detector.getSubdetectors().values() )
        {
            if ( subdet.getSystemID() != 0 )
            {
                systemIds.add( subdet.getSystemID() );
            }
        }

        // Sort system ID list.
        java.util.Collections.sort( systemIds );

        for ( Integer sysId : systemIds )
        {
            Subdetector subdet = detector.getSubdetector( sysId );
            String subdetName = subdet.getName();
            addRow( tbl, subdet.getSystemID(), new Link( subdetName, "#" + subdetName ) );
        }
    }

    private static void subdetectorIndex( Element parent,
            Detector detector,
            List<String> names )
    {
        // Header.
        addHeader2( parent, "Components" );

        // Table.
        Element tbl = new Element( "table" );
        tbl.setAttribute( "border", "1" );
        tbl.setAttribute( "cellpadding", "2" );
        parent.addContent( tbl );

        // todo: addHeaderRow method
        addRow( tbl, new Element( "b" ).setText( "Subdetector" ) );

        for ( String subdetName : names )
        {
            Subdetector subdet = detector.getSubdetector( subdetName );
            String readoutName = "-";
            if ( subdet.getReadout() != null )
            {
                readoutName = subdet.getReadout().getName();
            }

            addRow( tbl, new Link( subdetName, "#" + subdetName ) );
        }
    }

    private static void readoutIndex( Element parent, Detector detector )
    {
        // Header.
        addHeader2( parent, "Readouts" );

        // Table
        Element tbl = new Element( "table" );
        tbl.setAttribute( "border", "1" );
        tbl.setAttribute( "cellpadding", "2" );
        parent.addContent( tbl );

        // Table header row.
        addRow( tbl, 
                new Element( "b" ).setText( "Readout" ), 
                new Element( "b" ).setText( "Subdetectors" ),
                new Element( "b" ).setText( "ID Description" ),
                new Element( "b" ).setText( "Cell Size U x V" ));

        // Alpha sort on readout name.
        List<String> readoutNames = new ArrayList<String>( detector.getReadouts()
                .keySet() );
        java.util.Collections.sort( readoutNames );

        // Process Readouts by name.
        for ( String readoutName : readoutNames )
        {
            // Get the Readout.
            Readout readout = detector.getReadout( readoutName );

            // Get the list of Subdetectors.
            List<Subdetector> subdets = new ArrayList<Subdetector>();

            // Make list of associated Subdetectors.
            Element span = new Element( "span" );
            for ( Subdetector subdet : detector.getSubdetectors().values() )
            {
                // Check if Readout matches.
                if ( subdet.getReadout() == readout )
                {
                    String subdetName = subdet.getName();
                    span.addContent( new Link( subdetName, "#" + subdetName ) );
                }
            }
            
            String cellSizes = "NA";
            try 
            {
                if ( readout.getIDDecoder() instanceof Segmentation )
                {
                    Segmentation seg = (Segmentation) readout.getIDDecoder();
                    cellSizes = seg.getCellSizeU() + " mm x " + seg.getCellSizeV() + " mm";
                }
            }
            catch ( Exception x )
            {
                // Ignore.
            }

            // Add row for this Readout.
            addRow( tbl, 
                    readout.getName(), 
                    span, 
                    readout.getIDDecoder().getIDDescription().toString(),
                    cellSizes );
        }
    }

    private static void detectorHeader( Element parent, Detector detector )
    {
        addHeader2( parent, "Summary" );

        Element tbl = new Element( "table" );
        tbl.setAttribute( "border", "1" );
        tbl.setAttribute( "cellpadding", "2" );
        parent.addContent( tbl );

        Header header = detector.getHeader();

        addLabeledRow( tbl, "Detector", detector.getName() );
        addLabeledRow( tbl, "Title", header.getTitle() );
        addLabeledRow( tbl, "Comment", header.getComment() );
        addLabeledRow( tbl, "Author", header.getAuthor() );
        addLabeledRow( tbl, "Status", header.getStatus() );
        addLabeledRow( tbl, "Version", header.getVersion() );
        addLabeledRow( tbl,
                "Documentation",
                header.getURL().equals( "NONE" ) ? "NONE" : new Link( header.getURL() ) );
        addLabeledRow( tbl,
                "Zip File",
                new Link( "http://www.lcsim.org/detectors/" + detector.getName() + ".zip" ) );
    }

    private static void subdetector( Element parent, Subdetector subdet )
    {
        // Make bookmark in page.
        bookmark( parent, subdet.getName() );

        // Subdetector data table.
        Element tbl = new Element( "table" );
        tbl.setAttribute( "border", "1" );
        tbl.setAttribute( "width", "80%" );
        tbl.setAttribute( "cellpadding", "2" );
        parent.addContent( tbl );

        // Basic info.
        addLabeledRow( tbl, "Subdetector", subdet.getName() );
        addLabeledRow( tbl, "System ID", subdet.getSystemID() );
        addLabeledRow( tbl, "Type", subdet.isCalorimeter() ? "Calorimeter" : "Tracker" );
        addLabeledRow( tbl, "Class", subdet.getClass().getSimpleName() );
        addLabeledRow( tbl, "Endcap", subdet.isEndcap() ? true : false );
        addLabeledRow( tbl, "Readout", subdet.getReadout() != null ? subdet.getReadout()
                .getName() : "NONE" );
        addLabeledRow( tbl, "Tracking Vol", subdet.isInsideTrackingVolume() );
        addLabeledRow( tbl, "ID Description", subdet.getReadout() != null ? subdet
                .getIDDecoder().getIDDescription().toString() : "NONE" );

        if ( subdet instanceof AbstractLayeredSubdetector )
        {
            AbstractLayeredSubdetector layers = ( AbstractLayeredSubdetector ) subdet;

            addLabeledRow( tbl, "Number Of Layers", layers.getNumberOfLayers() );
            addLabeledRow( tbl, "Interaction Lengths", lenForm.format( layers.getInteractionLengths() ) );
            addLabeledRow( tbl, "Radiation Lengths", lenForm.format( layers.getRadiationLengths() ) );
        }

        // Calorimeter info.
        if ( subdet instanceof Calorimeter )
        {
            Calorimeter cal = ( Calorimeter ) subdet;
            addLabeledRow( tbl, "Calorimeter Type", cal.getCalorimeterType().toString() );
            addLabeledRow( tbl, "Number Of Sides", cal.getNumberOfSides() );
            addLabeledRow( tbl, "Inner Radius", geomForm.format( cal.getInnerRadius() ) + " mm" );
            addLabeledRow( tbl, "Outer Radius", geomForm.format( cal.getOuterRadius() ) + " mm" );
            addLabeledRow( tbl, "Z Length", geomForm.format( cal.getZLength() ) + " mm" );
            addLabeledRow( tbl, "Inner Z", geomForm.format( cal.getInnerZ() ) + " mm" );
            addLabeledRow( tbl, "Outer Z", geomForm.format( cal.getOuterZ() ) + " mm" );
            addLabeledRow( tbl, "Section Phi", geomForm.format( cal.getSectionPhi() ) + " radians" );
            if ( subdet.getReadout() != null && subdet.getReadout().getIDDecoder() instanceof Segmentation )
            {
                Segmentation seg = ( Segmentation ) subdet.getReadout().getIDDecoder();
                addLabeledRow( tbl, "Segmentation Type", seg.getClass().getSimpleName() );
            }

            try
            {
                addLabeledRow( tbl, "Cell Size U", subdet.getReadout() != null ? cal
                        .getCellSizeU() + " mm" : "NA" );
                addLabeledRow( tbl, "Cell Size V", subdet.getReadout() != null ? cal
                        .getCellSizeV() + " mm" : "NA" );
            }
            catch ( IndexOutOfBoundsException x )
            {
                // This can happen sometimes with a few types of Segmentation that have no
                // cell U/V defined.
            }

            // Layering information.
            if ( subdet.getLayering() != null && subdet.getLayering().getNumberOfLayers() > 0 )
            {
                Element layerTbl = layers( subdet );

                addLabeledRow( tbl, "Layers", layerTbl );
            }
        }
        else if ( subdet instanceof TubeSegment )
        {
            TubeSegment tube = (TubeSegment) subdet;
            addLabeledRow( tbl, "Inner Radius", geomForm.format( tube.getInnerRadius() ) + " mm" );
            addLabeledRow( tbl, "Outer Radius", geomForm.format( tube.getOuterRadius() ) + " mm" );
            addLabeledRow( tbl, "Z Half Length", geomForm.format( tube.getZHalfLength() ) + " mm" );
        }
        else if ( subdet instanceof PolyconeSupport )
        {
            Element planesTbl = new Element("table");
            planesTbl.setAttribute( "border", "1" );
            planesTbl.setAttribute( "cellpadding", "2");
            
            PolyconeSupport poly = (PolyconeSupport) subdet;
            
            // Table header row.
            addRow( planesTbl, 
                    new Element( "b" ).setText( "R Min" ), 
                    new Element( "b" ).setText( "R Max" ),
                    new Element( "b" ).setText( "Z" ) );
            
            for ( int i = 0, nplanes = poly.getNumberOfZPlanes(); i < nplanes; i++ )
            {
                addRow( planesTbl, 
                        geomForm.format( poly.getZPlane( i ).getRMin() ),
                        geomForm.format( poly.getZPlane( i ).getRMax() ),
                        geomForm.format( poly.getZPlane( i ).getZ() )  );
            }
            
            addLabeledRow( tbl, "Z Planes", planesTbl );
        }
        // TODO Need "default" setup to get detailed tracking parameters (strip pitches, etc.).
        else if ( subdet instanceof SiTrackerBarrel || subdet instanceof SiTrackerEndcap || subdet instanceof SiTrackerEndcap2)
        {            
            IDetectorElement de = subdet.getDetectorElement();

            List<SiSensor> sensors = de.findDescendants( SiSensor.class );            
            int nsensors = sensors.size();      
                                   
            addLabeledRow( tbl, "Sensors", Integer.toString( nsensors ) );
        }
    }

    private static void defines( Element parent, Detector detector )
    {
        bookmark( parent, "defines" );

        addHeader2( parent, "Constants" );

        Element tbl = new Element( "table" );
        tbl.setAttribute( "border", "1" );
        tbl.setAttribute( "width", "50%" );
        tbl.setAttribute( "cellpadding", "1" );
        parent.addContent( tbl );

        // Alpha sort.
        Map<String, Constant> cmap = detector.getConstants();
        List<String> constants = new ArrayList<String>( cmap.keySet() );
        java.util.Collections.sort( constants );

        for ( String key : constants )
        {
            addLabeledRow( tbl, key, cmap.get( key ).getValue() );
        }
    }

    private static void fields( Element parent, Detector detector )
    {
        bookmark( parent, "fields" );

        addHeader2( parent, "Fields" );

        Element tbl = new Element( "table" );
        parent.addContent( tbl );
        tbl.setAttribute( "border", "1" );
        tbl.setAttribute( "width", "50%" );

        for ( Map.Entry<String, Field> entry : detector.getFields().entrySet() )
        {
            Field field = entry.getValue();
            addLabeledRow( tbl, "Field", entry.getKey() );
            addLabeledRow( tbl, "Type", field.getClass().getSimpleName() );
            if ( field instanceof Solenoid )
            {
                Solenoid solenoid = ( Solenoid ) field;
                addLabeledRow( tbl, "Inner Field", solenoid.getInnerField()[ 2 ] + " Tesla" );
                addLabeledRow( tbl, "Outer Field", solenoid.getOuterField()[ 2 ] + " Tesla" );
                addLabeledRow( tbl, "Z Max", solenoid.getZMax() + " mm" );
                addLabeledRow( tbl, "Outer Radius 2", solenoid.getOuterRadius2() + " mm" );
            }
        }
    }

    private static void addHeader2( Element parent, String text )
    {
        Element h = new Element( "h2" );
        h.setText( text );
        parent.addContent( h );
    }

    private static Element addLayoutRow( Element tbl )
    {
        Element tr = new Element( "tr" );
        tbl.addContent( tr );
        Element td = new Element( "td" );
        tr.addContent( td );
        return td;
    }

    private static void addRow( Element table, Object... values )
    {
        if ( !table.getName().equals( "table" ) )
        {
            throw new RuntimeException( "Element is not a <table>." );
        }

        Element tr = new Element( "tr" );
        table.addContent( tr );

        for ( Object value : values )
        {
            Element td = new Element( "td" );
            td.setAttribute( "valign", "top" );
            if ( value instanceof String )
                td.setText( ( String ) value );
            else if ( value instanceof Element )
                td.addContent( ( Element ) value );
            else
                td.setText( value.toString() );
            tr.addContent( td );
        }
    }

    private static void addLabeledRow( Element table, String label, Object value )
    {
        if ( !table.getName().equals( "table" ) )
        {
            throw new RuntimeException( "Element is not a <table>." );
        }

        Element tr = new Element( "tr" );
        table.addContent( tr );

        // Label.
        Element td = new Element( "td" );
        td.setAttribute( "width", "25%" );
        td.setAttribute( "valign", "top" );
        Element b = new Element( "b" );
        td.addContent( b );
        b.setText( label );
        tr.addContent( td );

        // Value.
        td = new Element( "td" );
        td.setAttribute( "valign", "top" );
        tr.addContent( td );
        if ( value instanceof String )
            td.setText( ( String ) value );
        else if ( value instanceof Element )
            td.addContent( ( Element ) value );
        else
            td.setText( value.toString() );
    }

    private static void bookmark( Element parent, String text )
    {
        Element a = new Element( "a" );
        a.setAttribute( "name", text );
        parent.addContent( a );
    }

    private static class Link extends Element
    {
        Link( String label, String url )
        {
            super( "a" );
            this.setAttribute( "href", url );
            this.setText( label );
        }

        Link( String url )
        {
            super( "a" );
            this.setAttribute( "href", url );
            this.setText( url );
        }
    }
}