package org.lcsim.util.heprep;
import java.awt.Color;

/**
 *
 * @author tonyj
 */
public class GrayScaleColorMap implements ColorMap {
    
    public Color getColor(double value, float alpha) {
       if (value < 0 || value > 1) throw new IllegalArgumentException("Value must be in range [0,1]: "+value);
       float v = (float) value;
       return new Color(v,v,v,alpha);
    }
}
