package org.lcsim.geometry.compact.converter.lcdd.util;

/**
 * @author jeremym
 */
public class Trapezoid extends Solid
{
    public Trapezoid(String name, double x1, double x2, double y1, double y2, double z)
    {
        super("trd",name);
        setX1(x1);
        setX2(x2);
        setY1(y1);
        setY2(y2);
        setZ(z);
    }

    public Trapezoid(String name)
    {
        super("trd",name);
    }

    public double x1()
    {
        return getDim("x1")/2;
    }

    public double x2()
    {
        return getDim("x2")/2;
    }

    public double y1()
    {
        return getDim("y1")/2;
    }

    public double y2()
    {
        return getDim("y2")/2;
    }

    public double z()
    {
        return getDim("z")/2;
    }

    // FIXME: Input is multiplied by 2 to conform to GDML "convention" of using full side lengths rather than
    //        half, which is the Geant4 standard.
    public void setX1(double x1)
    {
        setAttribute("x1",String.valueOf(x1*2));
    }

    public void setX2(double x2)
    {
        setAttribute("x2", String.valueOf(x2*2));
    }

    public void setY1(double y1)
    {
        setAttribute("y1", String.valueOf(y1*2));
    }

    public void setY2(double y2)
    {
        setAttribute("y2", String.valueOf(y2*2));
    }

    public void setZ(double z)
    {
        setAttribute("z",String.valueOf(z*2));
    }
}