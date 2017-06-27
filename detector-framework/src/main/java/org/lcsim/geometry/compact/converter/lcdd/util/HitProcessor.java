package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;

/**
 * This class represents a hit_processor element in LCDD.
 * The usage of this child element on the sensitive detector is optional.
 * The LCDD processor will assign a reasonable default if it is not set explicitly.
 * 
 * @author Jeremy McCormick
 * @version $Id: $
 */
public class HitProcessor extends Element {

    /**
     * Class constructor.
     * @param typeName The type attribute setting.
     */
    public HitProcessor(String typeName) {
        super("hit_processor");
        setAttribute("type", typeName);
    }    
}
