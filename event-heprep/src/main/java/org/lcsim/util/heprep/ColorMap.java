package org.lcsim.util.heprep;

import java.awt.Color;

/**
 * Controls mapping of values to colors
 * @author tonyj
 */
public interface ColorMap {
    /**
     * @param value The value to convert, must be normalized to [0,1]
     */
    Color getColor(double value, float alpha); 
}
