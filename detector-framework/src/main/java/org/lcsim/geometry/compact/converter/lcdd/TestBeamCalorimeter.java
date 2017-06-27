package org.lcsim.geometry.compact.converter.lcdd;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.Box;
import org.lcsim.geometry.compact.converter.lcdd.util.Define;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.Material;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.Position;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Solids;
import org.lcsim.geometry.compact.converter.lcdd.util.Structure;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;
import org.lcsim.geometry.layer.LayerFromCompactCnv;

/**
 *
 * @author tonyj
 */
class TestBeamCalorimeter extends AbstractTestBeam
{
    TestBeamCalorimeter(Element node) throws JDOMException
    {
        super(node);
    }
    
    public boolean isCalorimeter()
    {
        return true;
    }
}
