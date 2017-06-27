package org.lcsim.geometry.compact.converter.pandora;

import static org.lcsim.geometry.Calorimeter.CalorimeterType.HAD_BARREL;
import static org.lcsim.geometry.Calorimeter.CalorimeterType.HAD_ENDCAP;
import static org.lcsim.geometry.Calorimeter.CalorimeterType.MUON_BARREL;
import static org.lcsim.geometry.Calorimeter.CalorimeterType.MUON_ENDCAP;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.lcsim.conditions.ConditionsSet;
import org.lcsim.geometry.Calorimeter;
import org.lcsim.geometry.Calorimeter.CalorimeterType;

/**
 * Represents CalorimeterConditions for a single subdetector.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
// FIXME: Put into separate package from Pandora converter.
public class CalorimeterConditions
{
    SamplingLayers samplingLayers;
    String name;
    double mipEnergy;
    double mipSigma;
    double mipCut;
    double timeCut;

    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        buff.append(name + '\n');
        for (SamplingLayerRange range : samplingLayers)
        {
            buff.append("[" + range.getLowerLayer() + " - " + range.getUpperLayer() + "]" + '\n');
            buff.append("    em = " + range.getEMSampling() + '\n');
            buff.append("    had = " + range.getHADSampling() + '\n');
        }

        return buff.toString();
    }

    public SamplingLayers getSamplingLayers()
    {
        return samplingLayers;
    }

    /**
     * Constructor that parses raw CalorimeterCalibration conditions for a
     * single subdetector.
     * 
     * @param calorimeter
     * @param conditions
     */
    public CalorimeterConditions(Calorimeter calorimeter, ConditionsSet conditions)
    {
        //System.out.println("conditions: " + calorimeter.getName());
        this.name = calorimeter.getName();

        // Figure out which layering conditions to use based on the
        // CalorimeterType.
        String layeringName = null;
        if (calorimeter.getCalorimeterType() == CalorimeterType.EM_BARREL || calorimeter.getCalorimeterType() == CalorimeterType.EM_ENDCAP)
        {
            layeringName = "ECalLayering";
        }
        else if (calorimeter.getCalorimeterType() == CalorimeterType.HAD_BARREL || calorimeter.getCalorimeterType() == CalorimeterType.HAD_ENDCAP)
        {
            layeringName = "HCalLayering";
        }
        else if (calorimeter.getCalorimeterType() == CalorimeterType.MUON_BARREL || calorimeter.getCalorimeterType() == CalorimeterType.MUON_ENDCAP)
        {
            layeringName = "MuonLayering";
        }
        else
        {
            throw new RuntimeException("Don't know how to handle CalorimeterConditions for " + calorimeter.getName() + ".");
        }

        String emName = null;
        String hadName = null;
        if (calorimeter.getCalorimeterType() == CalorimeterType.EM_BARREL || calorimeter.getCalorimeterType() == CalorimeterType.HAD_BARREL || calorimeter.getCalorimeterType() == CalorimeterType.MUON_BARREL)
        {
            emName = "EMBarrel_SF";
            hadName = "HadBarrel_SF";
        }
        else if (calorimeter.getCalorimeterType() == CalorimeterType.EM_ENDCAP || calorimeter.getCalorimeterType() == CalorimeterType.HAD_ENDCAP || calorimeter.getCalorimeterType() == CalorimeterType.MUON_ENDCAP)
        {
            emName = "EMEndcap_SF";
            hadName = "HadEndcap_SF";
        }

        if (emName == null || hadName == null)
        {
            throw new RuntimeException("Sampling fractions not found for " + calorimeter.getName() + ".");
        }

        String emSampling = conditions.getString(emName);
        String hadSampling = conditions.getString(hadName);
        List<Double> emSamplingFractions = new ArrayList<Double>();
        List<Double> hadSamplingFractions = new ArrayList<Double>();
        StringTokenizer tok = new StringTokenizer(emSampling, ",");
        while (tok.hasMoreTokens())
        {
            Double emSamplingFraction = Double.valueOf(tok.nextToken().trim());
            emSamplingFractions.add(emSamplingFraction);
        }
        tok = new StringTokenizer(hadSampling, ",");
        while (tok.hasMoreTokens())
        {
            Double hadSamplingFraction = Double.valueOf(tok.nextToken().trim());
            hadSamplingFractions.add(hadSamplingFraction);
        }

        String layering = conditions.getString(layeringName);
        tok = new StringTokenizer(layering, ",");
        List<Integer> layers = new ArrayList<Integer>();
        int maxLayer = calorimeter.getLayering().getLayerCount() - 1;
        while (tok.hasMoreTokens())
        {
            String nextToken = tok.nextToken().trim();
            int nextLayer = Integer.valueOf(nextToken);
            layers.add(nextLayer);
        }

        // FIXME Hack to get the correct starting index for the sampling
        // fractions. Ideally, the sampling fractions should be separated by subdetector name.
        int samplingIndex = 0;
        if (calorimeter.getCalorimeterType() == HAD_BARREL || calorimeter.getCalorimeterType() == HAD_ENDCAP)
        {
            samplingIndex = (new StringTokenizer(conditions.getString("ECalLayering"), ",").countTokens());
        }
        if (calorimeter.getCalorimeterType() == MUON_BARREL || calorimeter.getCalorimeterType() == MUON_ENDCAP)
        {
            samplingIndex = (new StringTokenizer(conditions.getString("ECalLayering"), ",").countTokens());
            samplingIndex += (new StringTokenizer(conditions.getString("HCalLayering"), ",").countTokens());
        }

        // System.out.println("    samplingIndex: " + samplingIndex);

        // Create the SamplingLayerRange list.
        samplingLayers = new SamplingLayers();
        for (int i = 0; i < layers.size(); i++)
        {
            // Figure out the layer range.
            int lowerLayer = layers.get(i);
            int upperLayer = 0;
            if (i + 1 > layers.size() - 1)
                upperLayer = maxLayer;
            else
                upperLayer = layers.get(i + 1) - 1;

            // Create the sampling layer range.
            double emSamplingFraction = emSamplingFractions.get(samplingIndex);
            double hadSamplingFraction = hadSamplingFractions.get(samplingIndex);
            SamplingLayerRange samplingLayerRange = new SamplingLayerRange(lowerLayer, upperLayer, emSamplingFraction, hadSamplingFraction);
            // System.out.println("    " + lowerLayer + " - " + upperLayer +
            // " : " + emSamplingFraction + ", " + hadSamplingFraction);

            samplingLayers.add(samplingLayerRange);

            ++samplingIndex;
        }

        // MIP energy.
        String mipCondition = null;
        String mipSigmaCondition = null;
        String mipCutCondition = null;
        
        // FIXME: Cleanup this ugliness.
        if (calorimeter.getCalorimeterType() == CalorimeterType.EM_BARREL || calorimeter.getCalorimeterType() == CalorimeterType.EM_ENDCAP)
        {
            mipCondition = "ECalMip_MPV";
            mipSigmaCondition = "ECalMip_sig";
            mipCutCondition = "ECalMip_Cut";
        }
        else if (calorimeter.getCalorimeterType() == CalorimeterType.HAD_BARREL || calorimeter.getCalorimeterType() == CalorimeterType.HAD_ENDCAP)
        {
            mipCondition = "HCalMip_MPV";
            mipSigmaCondition = "HCalMip_sig";
            mipCutCondition = "HCalMip_Cut";
        }
        else if (calorimeter.getCalorimeterType() == CalorimeterType.MUON_BARREL || calorimeter.getCalorimeterType() == CalorimeterType.MUON_ENDCAP)
        {
            mipCondition = "MuonMip_MPV";
            mipSigmaCondition = "MuonMip_sig";
            mipCutCondition = "MuonMip_Cut";
        }
        mipEnergy = conditions.getDouble(mipCondition);
        mipSigma = conditions.getDouble(mipSigmaCondition);
        mipCut = conditions.getDouble(mipCutCondition);
        timeCut = conditions.getDouble("timeCut");

        /*
         * System.out.println("    mipEnergy: " + mipEnergy);
         * System.out.println("    mipSigma: " + mipSigma);
         * System.out.println("    mipCut: " + mipCut);
         * System.out.println("    timeCut: " + timeCut);
         */
    }

    public SamplingLayerRange getSamplingLayerRange(int layer)
    {
        for (SamplingLayerRange layers : this.samplingLayers)
        {
            if (layers.inRange(layer))
                return layers;
        }
        return null;
    }

    public double getMipEnergy()
    {
        return mipEnergy;
    }

    public double getMipSigma()
    {
        return mipSigma;
    }

    public double getMipCut()
    {
        return mipCut;
    }

    public double getTimeCut()
    {
        return timeCut;
    }
}