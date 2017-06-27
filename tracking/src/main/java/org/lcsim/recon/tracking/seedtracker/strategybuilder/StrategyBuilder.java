/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lcsim.recon.tracking.seedtracker.strategybuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.lcsim.recon.tracking.seedtracker.SeedLayer;
import org.lcsim.recon.tracking.seedtracker.SeedLayer.SeedType;
import org.lcsim.recon.tracking.seedtracker.SeedStrategy;
import org.lcsim.recon.tracking.seedtracker.StrategyXMLMetadata;
import org.lcsim.recon.tracking.seedtracker.StrategyXMLUtils;
import org.lcsim.detector.DetectorElementStore;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.IDetectorElementContainer;
import org.lcsim.event.EventHeader;
import org.lcsim.event.MCParticle;
import org.lcsim.event.SimTrackerHit;
import org.lcsim.fit.helicaltrack.HitIdentifier;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.subdetector.BarrelEndcapFlag;

/**
 * StrategyBuilder automatically generates strategies for a detector by
 * seeing what layers MCParticles tend to go through. 
 * 
 * See interface for public method doc. 
 * 
 * @author cozzy
 */
public class StrategyBuilder extends AbstractStrategyBuilder implements IStrategyBuilder {
    
    public static final String defaultOutputFile = System.getProperties().getProperty("java.io.tmpdir")+
            System.getProperties().getProperty("file.separator")+
            "BuiltStrategies.xml"; 
    
    public static final int RandomSeed = 1234; 
    
    //these are defined separately because RunStrategyBuilder uses some of these too... 
    public static final boolean defaultVerbose = false; 
    public static final int defaultMinLayers = 7; 
    public static final int defaultConfirmLayers =1; 
    public static final int defaultSeedLayers =3; 
    public static final int defaultMinUnweightedScore = 1; 
    public static final boolean defaultSymmetrize = true;  
   
    private boolean verbose = defaultVerbose; 
    private boolean symmetrize = defaultSymmetrize; 
    private int min_layers = defaultMinLayers;  
    private int confirm_layers = defaultConfirmLayers;  
    private int seed_layers = defaultSeedLayers; 
    private String outputFile = defaultOutputFile; 
    private int minUnweightedScore = defaultMinUnweightedScore;  
    private boolean oldConfirm = false; 
    private List<SeedStrategy> startingStrategies = new ArrayList<SeedStrategy>(); 
    private Set<Set<DumbLayer>> startingSet = new HashSet<Set<DumbLayer>>(); //this will be generated from startingStrategies
    private SeedStrategy prototype = new SeedStrategy("null", new ArrayList<SeedLayer>()); 
    private LayerWeight weighter = null;
    private HitIdentifier ID = new HitIdentifier();
    //this stores the list of all sets of layers
    private List<Set<DumbLayer>> setlist = new ArrayList<Set<DumbLayer>>();
    private String detectorName; 
    private IParticleFilter filter; 
    private List<List<DumbLayer>> adjacentlist = new ArrayList<List<DumbLayer>>(); 
    //4:26
    private Random random = new Random(RandomSeed); 
    private Map<Set<DumbLayer>,SubsetScore> scoremap; 

    @Override 
    protected void startOfData(){
      //Use default filter if none is specified
        if (filter==null) {
            filter = new StrategyBasedFilter(prototype); 
        }       
    }
    
    //In the process step, we build two lists of collections:
    //      The set list is a list of the sets of layers hit by MCParticles hitting over min layers layers
    //      The adjacence list is a list of a list of hits that are determined to be adjacent based on MCParticle trajectory
    @Override
    protected void process(EventHeader event){

        super.process(event);
        
        if (verbose) {
            if (event.getEventNumber() % 100 == 0)
            System.out.println("Processed "+event.getEventNumber()+" events.");
        }
        
        filter.setEvent(event);
        
        //Build MCMap from SimTrackerHits, including inefficiency modeling
        Map<MCParticle, List<SimTrackerHit>> mcmap = buildMCMap(event);
        
        //filter MCs 
        Iterator<MCParticle> mciter = mcmap.keySet().iterator(); 
        while (mciter.hasNext()){
            MCParticle next = mciter.next(); 
            if (!filter.passes(next)){
                mciter.remove(); 
            }
        }
        
        //Build and add layer sets, as well as adjacent lists
        for(List<SimTrackerHit> l : mcmap.values()) {
            Set<DumbLayer> set = new HashSet<DumbLayer>(); 
            
            //sort by time, which allows creation of adjacence lists. 
            Collections.sort(l, new Comparator() {

                public int compare(Object o1, Object o2) {
                    SimTrackerHit h1 = (SimTrackerHit) o1; 
                    SimTrackerHit h2 = (SimTrackerHit) o2; 
                    return Double.compare(h1.getTime(), h2.getTime()); 
                }
            });
            
            //this will store all the working adjacent lists
            LinkedList<List<DumbLayer>> tempAdjacentLayersList = new LinkedList<List<DumbLayer>>(); 
            LinkedList<List<DumbLayer>> pendingAdjacentLayersList = new LinkedList<List<DumbLayer>>(); 
            for (SimTrackerHit h : l) {
                IDetectorElementContainer cont = DetectorElementStore.getInstance().find(h.getIdentifier());
                if(cont.isEmpty()) continue; 
                IDetectorElement de = cont.get(0); 
                String detname = ID.getName(de);
                int lyr = ID.getLayer(de);
                BarrelEndcapFlag be = ID.getBarrelEndcapFlag(de); 
                
                //kludgy divide by two thing
                if (weighter.isDivideByTwoInTrackerForward() && be.isEndcap() && 
                        (detname.indexOf("TrackerForward") > -1 || detname.indexOf("TkrForward") > -1) ) {
                        lyr/=2;  // sid01/sid02 doubles up on layer numbering in the endcap. 
                } else if (weighter.isDivideByTwoInTrackerEndcap() && be.isEndcap() && 
                        (detname.indexOf("TrackerEndcap") > -1 || detname.indexOf("TkrEndcap") > -1) ) {
                        lyr/=2;  // sid01 doubles up on layer numbering in the forward. 
                }
                
                //if symmetrizing, we want to treat North and South layers equivalently. 
                if (symmetrize && be.isEndcap()) be = BarrelEndcapFlag.ENDCAP; 
                DumbLayer dl = new DumbLayer(detname, lyr, be); 
                set.add(dl);
                
                //create a new adjacent list that starts with this layer if none already exists
                //(This is necessary because of the doubling of SimTrackerHits in the tracker endcap)
                if (tempAdjacentLayersList.isEmpty() || !tempAdjacentLayersList.getLast().contains(dl)) {
                    List<DumbLayer> adjacentLayers = new ArrayList<DumbLayer>(); 
                    tempAdjacentLayersList.addLast(adjacentLayers);
                }
                
                //see which adjacent lists already have enough layers, and add those to the list
                Iterator<List<DumbLayer>> it = tempAdjacentLayersList.iterator(); 
                while (it.hasNext()) {
                    List<DumbLayer> s = it.next(); 
                    if(!s.contains(dl)) s.add(dl); //otherwise we get doubled layers in the forward region of the tracker 
                    if (s.size() == confirm_layers + seed_layers) {
                        pendingAdjacentLayersList.add(s); 
                        it.remove(); 
                    }
                }
            }
                        
            //Ensure layer set has minimum number of layers
            if (set.size() >= min_layers) {
                setlist.add(set); 
                adjacentlist.addAll(pendingAdjacentLayersList);
            }
        }
    }
    
    @Override
    protected void suspend(){
        
        if (verbose) System.out.println("Finished processing. Beginning analysis."); 
        Set<DumbLayer> allLayers = new HashSet<DumbLayer>(); 
        
        //Get all layers that are used at some point
        for (Set<DumbLayer> set : setlist) 
            allLayers.addAll(set); 
        if (verbose) System.out.println(allLayers.size()+" total layers.");
        
       //create startingSet... this will be used so that new strategies aren't extraneously generated
       for (SeedStrategy strategy : startingStrategies){
            Set<DumbLayer> subset = StrategyBuilderUtils.getRelevantSet(strategy, true);
            startingSet.add(subset); 
        }
        if (verbose) System.out.println(startingStrategies.size()+" starting strategies defined.");
       
        //Generate the scorer and assign its weighter
        SubsetScorer scorer = new SubsetScorer(setlist,adjacentlist); 
        scorer.setLayerWeight(weighter); 
        scoremap = new HashMap<Set<DumbLayer>,SubsetScore>(); 
        
        
        for (Set<DumbLayer> starter : startingSet) {
            scoremap.put(starter, scorer.getScoreObject(starter)); 
            scorer.markUsed(starter);
        }
        
        //Generate all possible subsets of the right size of allLayers
        List<Set<DumbLayer>> all_subsets = StrategyBuilderUtils.generateAllPossibleDumbLayerSubsetsList(allLayers, confirm_layers+seed_layers); 
        if (verbose) System.out.println(all_subsets.size() + " possible subsets of size "+(confirm_layers+seed_layers)); 
        
        //convert setlist to set to eliminate duplicates... 
        Set<Set<DumbLayer>> setset = new HashSet<Set<DumbLayer>>(setlist.size()); 
        setset.addAll(setlist); 
        
        //usedSets keeps track of what has been used already
        Set<Set<DumbLayer>> usedSets = new HashSet<Set<DumbLayer>>(setset.size()); 
        
        //final_sets will store the generated seed + confirm layers
        Set<Set<DumbLayer>> final_sets = new HashSet<Set<DumbLayer>>(); 
        if (verbose) System.out.println("Layer set has "+setset.size()+" entries.");
        //map from a final_set to all other associated layers to generate extension layers
        Map<Set<DumbLayer>, Set<DumbLayer>> extendmap = new HashMap<Set<DumbLayer>,Set<DumbLayer>>();
        
        
        SubsetScore score = new SubsetScore(0,0,0); 
        //Figure out a "good" set of four-layer combinations by brute force...
        //We have a scoring algorithm and we find the maximal scoring one. 
        while (true){
            if (verbose) System.out.println(setset.size() - usedSets.size() + " entries left to be covered."); 
            if (usedSets.size() == setset.size()) break; //if we've used all sets, then we're done! 
            
            Set<DumbLayer> max = all_subsets.get(0); 
            SubsetScore maxScore = new SubsetScore(0,0,0); 
            
            //get the highest scoring strategy...
            for (Set<DumbLayer> trial : all_subsets){
                score = scorer.getScoreObject(trial); 
                if (score.score() > maxScore.score()) {
                    maxScore = score; 
                    max = trial; 
                }
            }
            
            //ignore anything that has too few occurrences... 
            if (maxScore.numTracks() <= minUnweightedScore) break; 
            
            scorer.markUsed(max);
            final_sets.add(max); 
            extendmap.put(max, new HashSet<DumbLayer>()); 
            scoremap.put(max,maxScore); 
            for (Set<DumbLayer> this_set : setset) {
                if (this_set.containsAll(max)) { //If this set contains all the layers in max, it should be findable! 
                    
                    //add extension layers to extendmap
                    Set<DumbLayer> nw = new HashSet<DumbLayer>(); 
                    for (DumbLayer dumb : this_set){                             
                        if(!max.contains(dumb)) nw.add(dumb);
                    }
                    Set<DumbLayer> old = extendmap.get(max); 
                    old.addAll(nw); 
                    extendmap.put(max, old); 
                    
                    //remove this set from consideration
                    usedSets.add(this_set); 
                }
            }
            
            
        }
        if(verbose) System.out.println("Done finding strategies"); 
        if(verbose) System.out.println(final_sets.toString());  
       
        //Generate the StrategyList 
        int counter = 0; 
                
        StrategyXMLMetadata meta = new StrategyXMLMetadata(); 
        
        List<SeedStrategy> strat_list = new ArrayList<SeedStrategy>(); 
        strat_list.addAll(startingStrategies); 
        
        //Write comments for starting strategies
        for (SeedStrategy starter : startingStrategies) {
            int unw_score = scoremap.get(StrategyBuilderUtils.getRelevantSet(starter,true)).numTracks(); 
            meta.strategyComments.put(starter, "Num findable tracks (total, not additional): "+unw_score);
        }
        
        //create Strategies from final_sets... this part is klunky right now. 
        for (Set<DumbLayer> s : final_sets) {

            List<DumbLayer> dlyrlst = new ArrayList<DumbLayer>(); 
            dlyrlst.addAll(s); 
            
            /**
             * Here we figure out which layers to use for seeding and which to use
             * for confirming. Since it is highly advantageous for the seed layers 
             * to be adjacent, we will try to enforce that if possible. 
             */
            
            //get adjacence info if set is adjacent. 
            List<DumbLayer> adjacenceInfo = null; 
            for (List<DumbLayer> l : adjacentlist) {
                if (s.containsAll(l) && l.containsAll(s)) {
                    adjacenceInfo = l; 
                    break; 
                }
            }
            
            //if not all layers are adjacent, then perhaps a subset of size seed_layers is... 
            if (adjacenceInfo == null) {
                for (Set<DumbLayer> ss : StrategyBuilderUtils.generateAllPossibleDumbLayerSubsetsList(s, seed_layers)){
                    for (List<DumbLayer> l : adjacentlist) {
                        if (ss.containsAll(l) && l.containsAll(ss)) {
                            adjacenceInfo = l; 
                            break; 
                        }
                    }
                }
            }
                        
            /**
             * The following operations manipulate dlyrlst such that the first num_confirm items will be
             * used for confirmation and the rest will be used for seeding.  
             * 
             * This is kind of kludgy and it might be less confusing if it were changed. 
             */
            
            //if these layers aren't adjacent, just use the weights to figure out which layers to confirm with
            if (adjacenceInfo == null || oldConfirm) {
                //sort the list from smallest to largest weight. Use smallest weight(s) for confirmation layer(s). 
                Collections.sort(dlyrlst, weighter.getComparator()); 
            } 
            
            //If all layers are adjacent, we use either the first or last layers to confirm, depending on the layer weights
            else if (adjacenceInfo.size() == dlyrlst.size()) { 
                dlyrlst = adjacenceInfo; 
                if (weighter.getWeight(dlyrlst.get(0)) > weighter.getWeight(dlyrlst.get(dlyrlst.size()-1)))
                    Collections.reverse(dlyrlst);
                    
            //Otherwise, use the adjacent layers as seeds and not the others... 
            } else {
                dlyrlst.removeAll(adjacenceInfo);
                dlyrlst.addAll(adjacenceInfo); 
            }
                     
            int confirmed = 0; //add the first num_confirm as confirmed... the rest as seed. 

            List<SeedLayer> lyrlst = new ArrayList<SeedLayer>(); 

            //get extension layers...sort from smallest weight to largest weight
            //                      because the list will be reversed. 
            List<SeedLayer> extendlyr = new ArrayList<SeedLayer>(); 
            List<DumbLayer> dumbextendlyr = new ArrayList<DumbLayer>(); 
            dumbextendlyr.addAll(extendmap.get(s)); 
            Collections.sort(dumbextendlyr, weighter.getComparator()); 
            
            for (DumbLayer lyr : dumbextendlyr) {
                extendlyr.add(new SeedLayer(lyr.detectorName, lyr.layer, lyr.be, SeedType.Extend)); 
            }
            
            lyrlst.addAll(extendlyr);             
            //get seed/confirmation layers
            for (DumbLayer lyr : dlyrlst){
                SeedType type;
                
                if (confirmed < confirm_layers) {
                    type = SeedType.Confirm;
                    confirmed++; 
                }
                else type = SeedType.Seed; 
                lyrlst.add(new SeedLayer(lyr.detectorName, lyr.layer, lyr.be, type)); 
            }
            Collections.reverse(lyrlst); // reverse so seed layers on top 
            
            String name = "AUTOGEN" + counter++ +"_"+lyrlst.hashCode(); 
            
            //copy over cutoff info from prototype
            SeedStrategy stgy = new SeedStrategy(name,lyrlst);
            stgy.copyCutoffsFromStrategy(prototype); 
            strat_list.add(stgy); 
            
            //Write in scoring information about each strategy  
            score = scoremap.get(s);
            String comment = "AUTOGEN STATISTICS: \n\t\t\tScore: "+score.score() + "\n";
            comment += "\t\t\tUnweighted Score (num new tracks): "+score.numTracks() + "\n"; 
            comment += "\t\t\tAdjacency: "+score.adjacency() +"\n\t\t"; 
            meta.strategyComments.put(stgy, comment); 
        }
        

        String comment = "Strategy list Autogenerated by Strategy Builder on "+new Date()+".";
        meta.targetDetector =  detectorName; 
        meta.comment = comment;
        
        //If symmetrizing, make Endcap Layers for both north and south
        if (symmetrize) {
            if (verbose) System.out.println("Symmetrizing..."); 
            StrategyBuilderUtils.symmetrizeStrategies(strat_list, meta);
        }
        
        //sort Strategy list
        if (verbose) System.out.println("Sorting output"); 
        Collections.sort(strat_list, new Comparator() {

            public int compare(Object o1, Object o2) {
                SeedStrategy one = (SeedStrategy) o1; 
                SeedStrategy two = (SeedStrategy) o2;
                return Double.compare(scoremap.get(StrategyBuilderUtils.getRelevantSet(two,true)).numTracks(), 
                        scoremap.get(StrategyBuilderUtils.getRelevantSet(one,true)).numTracks());
            }
        }); 
        
        StrategyXMLUtils.writeStrategyListToFile(strat_list, new File(outputFile), meta);
        if (verbose) System.out.println(strat_list.size()+" strategies generated."); 
        if (verbose) System.out.println("Strategies XML file written at "+outputFile); 
    }
    
    @Override
    protected void detectorChanged(Detector detector){
        detectorName = detector.getDetectorName(); 
        
        //use default weighter if not specified... default depends on detector name
        if (weighter==null) {
           setLayerWeight(new DefaultLayerWeight(detectorName).getWeight());
        }
        
        //check that detectors match layer weights (unless the TargetDetector is unspecified)
        if(!weighter.getTargetDetector().equals("None Specified") && !weighter.getTargetDetector().equals(detectorName)) {
            throw new DetectorMismatchException(detectorName, weighter.getTargetDetector()); 
        }
    }
    
    
    // ===============setters===============// documented in interface
    public void setOutput(String filename){
        outputFile = filename;
    }
    
    public void setLayerWeight(LayerWeight lw){
        weighter = lw; 
    }
    
    public void setMinLayers(int min){
        min_layers = min; 
    }
    
    public void setNumConfirmLayers(int clayers){
        confirm_layers = clayers; 
    }

    public void setNumSeedLayers(int slayers){
        seed_layers = slayers; 
    }
    
    public void setStrategyPrototype(SeedStrategy proto){
        prototype = proto; 
    }
    
    public void setStartingStrategyList(List<SeedStrategy> slist){
        startingStrategies = slist; 
    }
    
    public void setVerbose(boolean v){
        verbose = true; 
    }
    
    public void setMinimumUnweightedScore(int sc){
        minUnweightedScore = sc; 
    }
            
    public void setParticleFilter(IParticleFilter pfilter){
        filter = pfilter; 
    }
    
    public void setSymmetrize(boolean set){
        symmetrize = set; 
    }

    //========privates ============//
    private Map<MCParticle, List<SimTrackerHit>> buildMCMap(final EventHeader event) {

        //Build MC Map from SimTrackerHits
        Map<MCParticle, List<SimTrackerHit>> mcmap = new HashMap<MCParticle, List<SimTrackerHit>>();
        List<SimTrackerHit> allhits = new ArrayList<SimTrackerHit>();

        
        //This will return the list of lists in a random order... we want the order 
        // to be consistent so that the same hits are ignored due to inefficiency each time
        List<List<SimTrackerHit>> simhits = event.get(SimTrackerHit.class); 
        
        
        //Each collection should have a unique name, so sorting by name works here
        Collections.sort(simhits, new Comparator() {

            public int compare(Object o1, Object o2) {
                List<SimTrackerHit> l1 = (List<SimTrackerHit>)o1; 
                List<SimTrackerHit> l2 = (List<SimTrackerHit>)o2; 
                
                return String.CASE_INSENSITIVE_ORDER.compare(event.getMetaData(l1).getName(), event.getMetaData(l2).getName()); 
            }
        }); 
        
        for (List<SimTrackerHit> l : simhits) {

            Collections.sort(l, new Comparator() {

                //This might be a little overkill... but it should have a very
                //high probability of distinguishing between SimTrackerHits... 
                public int compare(Object o1, Object o2) {
                    SimTrackerHit h1 = (SimTrackerHit) o1;
                    SimTrackerHit h2 = (SimTrackerHit) o2; 
                    
                    if (h1.getTime()!=h2.getTime()) {
                        return Double.compare(h1.getTime(), h2.getTime()); 
                    } 
                    
                    if (h1.getCellID()!=h2.getCellID()) {
                        return Double.compare(h1.getCellID(), h2.getCellID()); 
                    }
                    
                    if (h1.getdEdx()!=h2.getdEdx()) {
                        return Double.compare(h1.getdEdx(), h2.getdEdx()); 
                    }
                    
                    return Double.compare(h1.getPathLength(), h2.getPathLength()); 
                }
            });
            
            /**
             * We simulate inefficiency in SimTrackerHit => TrackerHit conversion,
             * otherwise the strategies will miss certain classes of hits
             */

            EventHeader.LCMetaData meta = event.getMetaData(l);
            String readout = meta.getName();
            double efficiency = weighter.getReadoutEfficiency(readout);
            for (SimTrackerHit h : l) {
                if (random.nextDouble() < efficiency) {
                    allhits.add(h);
                }
            }
        }

        for (SimTrackerHit h : allhits) {
            MCParticle p = h.getMCParticle();
            List<SimTrackerHit> these_hits;
            if (mcmap.containsKey(p)) {
                these_hits = mcmap.get(p);
            } else {
                these_hits = new ArrayList<SimTrackerHit>();
            }

            these_hits.add(h);
            mcmap.put(p, these_hits);
        }

        return mcmap;
    }

}
