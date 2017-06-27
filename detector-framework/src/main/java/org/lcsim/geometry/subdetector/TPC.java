/*
 * TPC.java
 * 
 * Created on August 20, 2005, 12:50 PM
 */
package org.lcsim.geometry.subdetector;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;

import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: TPC.java,v 1.7 2011/07/28 20:20:30 jeremy Exp $
 */
public class TPC extends AbstractCylindricalTracker
{
    /** Creates a new instance of TPC */
    public TPC( Element node ) throws JDOMException
    {
        super( node );
    }

    public boolean isBarrel()
    {
        return true;
    }

    public void appendHepRep( HepRepFactory factory, HepRep heprep )
    {
        HepRepInstanceTree instanceTree = heprep.getInstanceTreeTop( "Detector", "1.0" );
        HepRepTypeTree typeTree = heprep.getTypeTree( "DetectorType", "1.0" );
        HepRepType barrel = typeTree.getType( "Barrel" );

        HepRepType type = factory.createHepRepType( barrel, getName() );
        
        type.addAttValue( "drawAs", "Cylinder" );
        type.addAttValue( "color", getVisAttributes().getColor() );
        
        HepRepInstance instance = factory.createHepRepInstance( instanceTree, type );
        instance.addAttValue( "radius", getInnerRadius() );
        factory.createHepRepPoint( instance, 0, 0, getZMin() );
        factory.createHepRepPoint( instance, 0, 0, getZMax() );

        HepRepInstance instance2 = factory.createHepRepInstance( instanceTree, type );
        instance2.addAttValue( "radius", getOuterRadius() );
        factory.createHepRepPoint( instance2, 0, 0, getZMin() );
        factory.createHepRepPoint( instance2, 0, 0, getZMax() );
    }
}