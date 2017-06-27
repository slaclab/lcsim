package org.lcsim.geometry.subdetector;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.Calorimeter;
import org.lcsim.geometry.compact.Segmentation;

/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: TestBeamCalorimeter.java,v 1.13 2011/01/04 21:58:51 jeremy Exp $
 */
// TODO Need to make this class inherit from AbstractCalorimeter.
public class TestBeamCalorimeter extends AbstractTestBeam implements Calorimeter {

    CalorimeterType calType = CalorimeterType.UNKNOWN;

    public TestBeamCalorimeter(Element node) throws JDOMException {
        super(node);
        if (node.getAttribute("calorimeterType") != null) {
            calType = CalorimeterType.fromString(node.getAttributeValue("calorimeterType"));
        }
    }

    // FIXME Duplicate implementation w.r.t. AbstractCalorimeter.
    public boolean isCalorimeter() {
        return true;
    }

    // FIXME Duplicate implementation w.r.t. AbstractCalorimeter.
    public CalorimeterType getCalorimeterType() {
        return calType;
    }

    // FIXME Duplicate implementation w.r.t. AbstractCalorimeter.
    public double getCellSizeU() {
        return ((Segmentation) this.getIDDecoder()).getCellSizeU();
    }

    // FIXME Duplicate implementation w.r.t. AbstractCalorimeter.
    public double getCellSizeV() {
        return ((Segmentation) this.getIDDecoder()).getCellSizeV();
    }

    public double getSectionPhi() {
        return 0;
    }

    // FIXME Duplicate implementation w.r.t. AbstractCalorimeter.
    public double getTotalThickness() {
        return layering.getThickness();
    }
}