package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.DataConversionException;

/**
 * 
 * @author tonyj
 */
public class Box extends Solid {
    /** Creates a new instance of Box */
    public Box(String name) {
        super("box", name);
        setAttribute("x", String.valueOf(0.));
        setAttribute("y", String.valueOf(0.));
        setAttribute("z", String.valueOf(0.));
    }

    public Box(String name, double x, double y, double z) {
        super("box", name);
        setX(x);
        setY(y);
        setZ(z);
    }

    public void setX(double x) {
        setAttribute("x", String.valueOf(x));
    }

    public void setY(double y) {
        setAttribute("y", String.valueOf(y));
    }

    public void setZ(double z) {
        setAttribute("z", String.valueOf(z));
    }

    public double getX() {
        try {
            return getAttribute("x").getDoubleValue();
        } catch (DataConversionException x) {
            throw new RuntimeException(x);
        }
    }

    public double getY() {
        try {
            return getAttribute("y").getDoubleValue();
        } catch (DataConversionException x) {
            throw new RuntimeException(x);
        }
    }

    public double getZ() {
        try {
            return getAttribute("z").getDoubleValue();
        } catch (DataConversionException x) {
            throw new RuntimeException(x);
        }
    }
}
