package org.lcsim.recon.tracking.vsegment.digitization;

import java.util.*;

/**
 *
 *
 * @author D. Onoprienko
 * @version $Id: NoiseGenerator.java,v 1.1 2008/12/06 21:53:43 onoprien Exp $
 */
abstract public class NoiseGenerator {
  
// -- Constructors :  ----------------------------------------------------------
  
  public NoiseGenerator() {
    _random = new Random();
  }
  
  public NoiseGenerator(Random random) {
    _random = random;
  }
  
  public NoiseGenerator(double threshold) {
    _threshold = threshold;
  }
  
  public NoiseGenerator(Random random, double threshold) {
    _random = random;
    _threshold = threshold;
  }
  
// -- Setters :  ---------------------------------------------------------------
  
  public void setRandom(Random random) {
    _random = random;
  }
  
  public void setThreshold(double threshold) {
    _threshold = threshold;
  }
  
// -- Getters :  ---------------------------------------------------------------
  
  abstract public int channelsAboveThreshold(int channels);
  
  abstract public double noise();
  
  abstract public double noiseAboveThreshold();
  
  public Random getRandom() {
    return _random;
  }
  
  public double getThreshold() {
    return _threshold;
  }
  
// -- Private parts :  ---------------------------------------------------------
  
  protected Random _random;
  protected double _threshold;
}
