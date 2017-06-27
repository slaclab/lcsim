/*
 * PolyconeSupport.java
 * 
 * Created on October 31, 2005, 10:18 AM
 */

package org.lcsim.geometry.subdetector;

import org.jdom.Element;
import org.jdom.DataConversionException;
import org.jdom.JDOMException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import org.lcsim.material.Material;
import org.lcsim.material.MaterialManager;

/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: PolyconeSupport.java,v 1.8 2013/05/01 20:48:35 jeremy Exp $
 */
public class PolyconeSupport extends AbstractSubdetector {

    List<ZPlane> zplanes = new ArrayList<ZPlane>();
    Material material;

    public PolyconeSupport(Element node) throws JDOMException {
        super(node);
        material = MaterialManager.instance().getMaterial(node.getChild("material").getAttributeValue("name"));
        for (Iterator i = node.getChildren("zplane").iterator(); i.hasNext();) {
            try {
                Element zplane = (Element) i.next();
                zplanes.add(new ZPlane(zplane.getAttribute("rmin").getDoubleValue(), zplane.getAttribute("rmax").getDoubleValue(), zplane.getAttribute("z").getDoubleValue()));
            } catch (DataConversionException dce) {
                throw new RuntimeException("bad values to zplane", dce);
            }
        }
    }

    public Material getMaterial() {
        return material;
    }

    public List<ZPlane> getZPlanes() {
        return zplanes;
    }

    public int getNumberOfZPlanes() {
        return zplanes.size();
    }

    public ZPlane getZPlane(int idx) {
        return zplanes.get(idx);
    }

    public static class ZPlane {

        double rmin, rmax, z;

        public ZPlane(double rmin, double rmax, double z) {
            this.rmin = rmin;
            this.rmax = rmax;
            this.z = z;
        }

        public double getRMin() {
            return rmin;
        }

        public double getRMax() {
            return rmax;
        }

        public double getZ() {
            return z;
        }
    }
}
