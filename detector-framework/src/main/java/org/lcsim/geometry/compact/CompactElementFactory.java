package org.lcsim.geometry.compact;

import org.lcsim.util.xml.DefaultElementFactory;

/**
 *
 * @author jeremym
 */
public class CompactElementFactory extends DefaultElementFactory
{
    
    /** Creates a new instance of CompactElementFactory */
    public CompactElementFactory()
    {
        super();
        register(Constant.class);
        register(Detector.class);
        register(Header.class);
        register(Readout.class);
        register(Subdetector.class);
        register(Segmentation.class);
        register(Field.class);
        register(LimitSet.class);
        register(Region.class);
        register(VisAttributes.class);
    }    
}
