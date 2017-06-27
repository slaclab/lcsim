package org.lcsim.geometry.compact.converter.pandora;

/**
 * A range of layers with associated EM and HAD sampling fractions.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
// FIXME: Put into separate package from Pandora converter.
public class SamplingLayerRange
{
    int lowerLayer;
    int upperLayer;
    double em;
    double had;

    SamplingLayerRange(int lowerLayer, int upperLayer, double em, double had)
    {
        this.lowerLayer = lowerLayer;
        this.upperLayer = upperLayer;
        this.em = em;
        this.had = had;
    }

    public boolean inRange(int layerNumber)
    {
        return layerNumber >= lowerLayer && layerNumber <= upperLayer;
    }

    public int getLowerLayer()
    {
        return lowerLayer;
    }

    public int getUpperLayer()
    {
        return upperLayer;
    }

    public double getEMSampling()
    {
        return em;
    }

    public double getHADSampling()
    {
        return had;
    }
}