package org.lcsim.geometry;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepTreeID;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;

import java.util.HashMap;
import java.util.Map;

import org.jdom.Element;
import org.lcsim.geometry.Calorimeter.CalorimeterType;
import org.lcsim.geometry.compact.Field;
import org.lcsim.geometry.compact.Readout;
import org.lcsim.geometry.field.FieldOverlay;
import org.lcsim.geometry.util.BaseIDDecoder;

/**
 * @author tonyj
 * @author jeremym
 * 
 * @version $Id: Detector.java,v 1.38 2011/03/11 19:22:20 jeremy Exp $
 */
// FIXME Determine which methods should go here or on in compact Detector.
// FIXME Following convention in this package, shouldn't this just be an interface?
public class Detector extends org.lcsim.geometry.compact.Detector implements HepRepProvider
{
    private FieldOverlay fieldOverlay = new FieldOverlay();
    private Map< CalorimeterType, Calorimeter > calTypeMap = new HashMap< CalorimeterType, Calorimeter >();

    protected Detector( Element node )
    {
        super( node );
    }

    public String getName()
    {
        // The name from the compact header.
        return getDetectorName();
    }

    public IDDecoder getDecoder( String readout )
    {
        Readout r = getReadouts().get( readout );
        return ( r == null ) ? null : r.getIDDecoder();
    }

    protected void addSubdetector( org.lcsim.geometry.compact.Subdetector sub )
    {
        super.addSubdetector( sub );
        setupIDDecoder( sub );

        // Add Calorimeter to CalorimeterType map.
        if ( sub.isCalorimeter() )
        {
            Calorimeter cal = ( Calorimeter ) sub;
            if ( !cal.getCalorimeterType().equals( CalorimeterType.UNKNOWN ) )
            {
                if ( calTypeMap.get( cal.getCalorimeterType() ) != null )
                {
                    throw new RuntimeException( "Cannot add duplicate CalorimeterType <" + CalorimeterType
                            .toString( cal.getCalorimeterType() ) + "> from subdetector <" + cal.getName() + ">." );
                }
                calTypeMap.put( cal.getCalorimeterType(), cal );
            }
        }
    }

    /*
     * FIXME There is not necessary a 1-to-1 between subdetectors and readouts as implied here. 
     * FIXME This function is just a hack to setup the IDDecoder. There is probably
     *       a better way to do it.
     */
    private void setupIDDecoder( org.lcsim.geometry.compact.Subdetector subdet )
    {
        if ( subdet.getReadout() != null )
        {
            BaseIDDecoder decoder = ( BaseIDDecoder ) subdet.getIDDecoder();

            if ( decoder != null )
            {
                decoder.setSubdetector( subdet );
            }
        }
    }

    protected void addReadout( Readout r )
    {
        super.addReadout( r );
    }

    public FieldMap getFieldMap()
    {
        return fieldOverlay;
    }

    protected void addField( Field field )
    {
        super.addField( ( Field ) field );
        fieldOverlay.addField( ( FieldMap ) field );
    }

    public Calorimeter getCalorimeterByType( CalorimeterType calType )
    {
        return calTypeMap.get( calType );
    }

    public void appendHepRep( HepRepFactory factory, HepRep heprep )
    {
        HepRepTreeID treeID = factory.createHepRepTreeID( "DetectorType", "1.0" );
        HepRepTypeTree typeTree = factory.createHepRepTypeTree( treeID );
        heprep.addTypeTree( typeTree );

        HepRepInstanceTree instanceTree = factory.createHepRepInstanceTree( "Detector", "1.0", typeTree );
        heprep.addInstanceTree( instanceTree );

        String detectorLayer = "Detector";
        heprep.addLayer( detectorLayer );

        HepRepType barrel = factory.createHepRepType( typeTree, "Barrel" );
        barrel.addAttValue( "layer", detectorLayer );
        HepRepType endcap = factory.createHepRepType( typeTree, "Endcap" );
        endcap.addAttValue( "layer", detectorLayer );

        for ( Object sub : getSubdetectors().values() )
        {
            if ( sub instanceof HepRepProvider )
            {
                // Add Subdetector to HepRep scene but only if visible.
                if (((org.lcsim.geometry.compact.Subdetector)sub).getVisAttributes().isVisible())
                {
                    ( ( HepRepProvider ) sub ).appendHepRep( factory, heprep );
                }
                // DEBUG
                //else
                //{
                //    System.out.println("skipped " + ((Subdetector)sub).getName());
                //}
            }
        }
    }

}
