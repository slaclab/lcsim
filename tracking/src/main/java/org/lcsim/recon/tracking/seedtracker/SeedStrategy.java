/*
 * SeedStrategy.java
 *
 * Created on March 29, 2006, 3:04 PM
 *
 */

package org.lcsim.recon.tracking.seedtracker;

import java.util.List;
import java.util.ArrayList;

import org.lcsim.recon.tracking.seedtracker.SeedLayer.SeedType;

/**
 * Encapsulate the parameters and layers used for this track finding strategy
 * @author Richard Partridge
 * @version 1.0
 */
public class SeedStrategy {
    private String _Name;
    private List<SeedLayer> _LayerList = new ArrayList<SeedLayer>();
    private double _MinPT = 1.0;
    private double _MaxDCA = 10.0;
    private double _MaxZ0 = 10.0;
    private double _MaxChisq = 100.;
    private double _BadHitChisq = 15.;
    private int _MinConfirm = 1;
    private int _MinHits = 7;
    
    /**
     * Fully qualified constructor
     * @param Name Name assigned to this strategy
     * @param LayerList SeedLayers for this strategy
     * @param MinPT Minimum pT for this strategy
     * @param MaxDCA Maximum DCA for this strategy
     * @param MaxZ0 Maximum z0 for this strategy
     * @param MaxChisq Maximum chi^2 for this strategy
     * @param BadHitChisq chi^2 that invokes bad hit treatment
     * @param MinConfirm Minimum confirmation hits for this strategy
     * @param MinHits Minimum total number of hits for this strategy
     */
    public SeedStrategy(String Name, List<SeedLayer> LayerList, double MinPT,
            double MaxDCA, double MaxZ0, double MaxChisq, double BadHitChisq, int MinConfirm, int MinHits) {
        this(Name, LayerList);
        _Name = Name;
        _MinPT = MinPT;
        _MaxDCA = MaxDCA;
        _MaxZ0 = MaxZ0;
        _MaxChisq = MaxChisq;
        _BadHitChisq = BadHitChisq;
        _MinConfirm = MinConfirm;
        _MinHits = MinHits;
    }
    
    /**
     * Constructor for a strategy with the default parameter settings
     * @param Name Name assigned to this strategy
     * @param LayerList List of SeedLayers for this strategy
     */
    public SeedStrategy(String Name, List<SeedLayer> LayerList) {
        this(Name);
        _LayerList.addAll(LayerList);
    }
    
    /**
     * Bare-bones constructor - layers must be added and any changes to default parameters must be made
     * @param Name Name assigned to this strategy
     */
     public SeedStrategy(String Name) {
        _Name = Name;
    }
    
    /**
     * Return name assigned to this strategy
     * @return Strategy name
     */
    public String getName() {
        return _Name;
    }
    
    /**
     * Return list of SeedLayers used by this strategy
     * @return List of SeedLayers used by this strategy
     */
    public List<SeedLayer> getLayerList() {
        return _LayerList;
    }
    
    /**
     * Return minimum pT for this strategy
     * @return Minimum pT for this strategy
     */
    public double getMinPT() {
        return _MinPT;
    }
    
    /**
     * Return maximum Distance of Closest Approach (DCA) in the x-y plane for this strategy
     * @return Maximum DCA for this strategy
     */
    public double getMaxDCA() {
        return _MaxDCA;
    }
    
    /**
     * Return maximum s-z intercept z0 for this strategy
     * @return Maximum z0 for this strategy
     */
    public double getMaxZ0() {
        return _MaxZ0;
    }
    
    /**
     * Return maximum chi^2 for this strategy
     * @return Maximum chi^2 for this strategy
     */
    public double getMaxChisq() {
        return _MaxChisq;
    }
    
    public double getBadHitChisq() {
        return _BadHitChisq;
    }
    
    /**
     * Return minimum number of confirmation hits for this strategy
     * @return Minimum number of confirmation hits for this strategy
     */
    public int getMinConfirm() {
        return _MinConfirm;
    }
    
    /**
     * Return minimum number of total hits for this strategy
     * @return Minimum number of total hits for this strategy
     */
    public int getMinHits() {
        return _MinHits;
    }
    
    /**
     * Specify Seedlayers to be used for this strategy
     * @param LayerList List of SeedLayers used by this strategy
     */
    public void putLayerList(List<SeedLayer> LayerList) {
        _LayerList.addAll(LayerList);
        return;
    }
    
    /**
     * Set the minimum pT for this strategy
     * @param MinPT Minimum pT of this strategy
     */
    public void putMinPT(double MinPT) {
        _MinPT = MinPT;
        return;
    }
    
    /**
     * Set the maximum Distance of Closest Approach (DCA) in the x-y plane for this strategy
     * @param MaxDCA Maximum DCA for this strategy
     */
    public void putMaxDCA(double MaxDCA) {
        _MaxDCA = MaxDCA;
        return;
    }
    
    /**
     * Set the maximum s-z intercept z0 for this strategy
     * @param MaxZ0 Maximum z0 for this strategy
     */
    public void putMaxZ0(double MaxZ0) {
        _MaxZ0 = MaxZ0;
        return;
    }
    
    /**
     * Set the maximum chi^2 for this strategy
     * @param MaxChisq Maximum chi^2 for this strategy
     */
    public void putMaxChisq(double MaxChisq) {
        _MaxChisq = MaxChisq;
        return;
    }
    
    public void putBadHitChisq(double BadHitChisq) {
        _BadHitChisq = BadHitChisq;
        return;
    }
    
    /**
     * Set the minimum number of confirmation hits for this strategy
     * @param MinConfirm Minimum number of confirmation hits for this strategy
     */
    public void putMinConfirm(int MinConfirm) {
        _MinConfirm = MinConfirm;
        return;
    }
    
    /**
     * Set the minimum number of total hits for this strategy
     * @param MinHits Minimum number of total hits for this strategy
     */
    public void putMinHits(int MinHits) {
        _MinHits = MinHits;
        return;
    }
    
    /**
     * Add a SeedLayer for this strategy
     * @param Layer SeedLayer to be added to this strategy
     */
    public void addLayer(SeedLayer Layer) {
        _LayerList.add(Layer);
        return;
    }
    
    /**
     * Return the list of SeedLayers of a given SeedType for this strategy
     * @param type SeedType of the layers to be returned
     * @return SeedLayers of the specified type for this strategy
     */
     public List<SeedLayer> getLayers(SeedType type) {
        List<SeedLayer> layers = new ArrayList();
        for (SeedLayer layer : _LayerList) {
            if (layer.getType() == type) layers.add(layer);
        }
        return layers;
    }
     
    /**
     * Copies all the cutoffs (minpt, numhits etc.) from another strategy.
     * Does not modify the name or the layer list. 
     * @param other Another SeedStrategy
     */ 
    public void copyCutoffsFromStrategy(SeedStrategy other) {
        this.putBadHitChisq(other.getBadHitChisq());
        this.putMaxChisq(other.getMaxChisq());
        this.putMaxDCA(other.getMaxDCA()); 
        this.putMaxZ0(other.getMaxZ0());; 
        this.putMinConfirm(other.getMinConfirm());
        this.putMinHits(other.getMinHits());
        this.putMinPT(other.getMinPT());
    }
     
     @Override
     public boolean equals(Object other) {
         
         if (this == other) return true; 
         if (!(other instanceof SeedStrategy)) return false; 
         SeedStrategy strat = (SeedStrategy) other; 
         
         return ( this._BadHitChisq == strat.getBadHitChisq() 
               && this._MaxChisq == strat.getMaxChisq()
               && this._MaxDCA == strat.getMaxDCA() 
               && this._MaxZ0 == strat.getMaxZ0()
               && this._MinConfirm == strat.getMinConfirm()
               && this._MinHits == strat.getMinHits()
               && this._MinPT == strat.getMinPT()
               && this._Name.equals(strat.getName()) 
               && this._LayerList.equals(strat.getLayerList()) ) ;
         
     }
}