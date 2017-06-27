package org.lcsim.detector;

import java.util.HashMap;

import org.lcsim.detector.identifier.ExpandedIdentifier;
import org.lcsim.detector.identifier.IExpandedIdentifier;
import org.lcsim.detector.identifier.IIdentifier;
import org.lcsim.detector.identifier.IIdentifierDictionary;
import org.lcsim.detector.identifier.IdentifierHelper;

/**
 * <p>
 * An {@link org.lcsim.detector.identifier.IIdentifierHelper} for decoding {@link org.lcsim.detector.identifier.IIdentifier}s
 * from the top-level of the detector description.
 * </p>
 * 
 * <p>
 * This class uses the following fields from the ID description.
 * <ul>
 * <li><code>system</code> - The system number that uniquely identifies a subdetector.</li>
 * <li><code>barrel</code> - A flag that indicates whether a subdetector is a barrel (0), endcap positive (1), or endcap negative (2).</li>
 * <li><code>layer</code> - The layer number of a subcomponent (optional).</li>
 * </ul>
 * </p> 
 * 
 * <p>
 * The system numbers can be set from a {@see SystemMap} which is a map of strings 
 * to integer values for system.  These could be read from a compact description.
 * </p>
 * 
 * <p>
 * The basic method signatures are as follows.
 * <ul>
 * <li><code>int getASubdetectorValue()</code> - Get the system value of SomeSubdetector.
 * <li><code>isASubdetector(IIdentifier i)</code> - Check if the id is of some subdetector type or it it has matching barrel flag. 
 * <li><code>IIdentifier getASubdetectorId()</code> - Get the id for SomeSubdetector.
 * <li><code>int getAFieldValue(IIdentifier i)</code> - Get a field value from an id.
 * <li><code>boolean AFieldEquals(IIdentifier i, int x)</code> - Check if field from identifier equals the argument value.
 * </ul>
 * </p>
 * 
 * <p>
 * Every DetectorIdentifierHelper is associated with a single subdetector, such as the Hcal Barrel.
 * </p>
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class DetectorIdentifierHelper
extends IdentifierHelper
{	
    // Invalid index.
    public static final int invalidIndex=-1;

    // Index of system field in the IdentifierDictionary.  REQUIRED.
    int systemIndex=invalidIndex;

    // Index of barrel field in the IdentifierDictionary.  REQUIRED.
    int barrelIndex=invalidIndex;

    // Index of layer field in the IdentifierDictionary.  OPTIONAL.
    int layerIndex=invalidIndex;

    // Barrel flag values following the LCSim conventions.
    private final static int barrelValue=0;
    private final static int endcapPositiveValue=1;
    private final static int endcapNegativeValue=2;

    // Subsystem id values.  
    // These are set via a SystemMap, to allow the values to be read
    // from a compact detector description.  The default SystemMap is 
    // created if none is supplied.
    private int invalidSystemValue = -1;
    private int unknownValue = invalidSystemValue;
    private int vtxBarrelValue = invalidSystemValue;
    private int vtxEndcapValue = invalidSystemValue;
    private int sitBarrelValue = invalidSystemValue;
    private int sitEndcapValue = invalidSystemValue;
    private int sitForwardValue = invalidSystemValue;
    private int tpcValue = invalidSystemValue;
    private int ecalBarrelValue = invalidSystemValue;
    private int ecalEndcapValue = invalidSystemValue;
    private int hcalBarrelValue = invalidSystemValue;
    private int hcalEndcapValue = invalidSystemValue;
    private int muonBarrelValue = invalidSystemValue;
    private int muonEndcapValue = invalidSystemValue;
    private int ecalForwardValue = invalidSystemValue;
    private int lumiValue = invalidSystemValue;	

    // Barrel or endcap ids.
    IIdentifier barrelId;
    IIdentifier endcapPositiveId;
    IIdentifier endcapNegativeId;

    // Vertex detector ids.
    IIdentifier vtxBarrelId;
    IIdentifier vtxEndcapId;
    IIdentifier vtxEndcapPositiveId;
    IIdentifier vtxEndcapNegativeId;

    // Silicon tracker ids.
    IIdentifier sitBarrelId;
    IIdentifier sitEndcapId;
    IIdentifier sitEndcapPositiveId;
    IIdentifier sitEndcapNegativeId;

    // Forward silicon tracking.
    IIdentifier sitForwardId;
    IIdentifier sitForwardEndcapPositiveId;
    IIdentifier sitForwardEndcapNegativeId;	

    // TPC id.
    IIdentifier tpcId;

    // Ecal ids.
    IIdentifier ecalBarrelId;
    IIdentifier ecalEndcapId;
    IIdentifier ecalEndcapPositiveId;
    IIdentifier ecalEndcapNegativeId;

    // Hcal ids.
    IIdentifier hcalBarrelId;
    IIdentifier hcalEndcapId;
    IIdentifier hcalEndcapPositiveId;
    IIdentifier hcalEndcapNegativeId;

    // Muon ids.
    IIdentifier muonBarrelId;
    IIdentifier muonEndcapId;
    IIdentifier muonEndcapPositiveId;
    IIdentifier muonEndcapNegativeId;

    // Ecal forward ids.
    IIdentifier ecalForwardId;
    IIdentifier ecalForwardEndcapPositiveId;
    IIdentifier ecalForwardEndcapNegativeId;

    // Luminosity monitor ids.
    IIdentifier lumiId;
    IIdentifier lumiEndcapPositiveId;
    IIdentifier lumiEndcapNegativeId;
    
    private IDetectorElement subdetectorDetectorElement = null;
    private int subdetectorSystemNumber = -1;
    SubdetectorType subdetectorType;

    // LDC si tracking...
    // inner = sit
    // outer = set
    // ftc = forward tracking chamber

    // FIXME: Add an hcalForward subsystem.

    public static class SystemMap 
    extends HashMap<String,Integer>
    {
        public Integer put(String key, Integer value) 
        {
            if (containsKey(key))
            {
                System.err.println("The field " + key + " was already set to <" + value + ">.  Ignored!");
                return -1;
            }
            else {
                return super.put(key, value);
            }
        }					
    }

    private SystemMap defaultSystemMap;

    private SystemMap makeDefaultSystemMap()
    {
        if (defaultSystemMap == null)
        {
            defaultSystemMap = new SystemMap();

            SystemMap s = defaultSystemMap;

            s.put("unknown",0);
            s.put("vtxBarrel",1);
            s.put("vtxEndcap",2);
            s.put("sitBarrel",3);
            s.put("sitEndcap",4);
            s.put("sitForward", 5);
            s.put("tpc",6);
            s.put("ecalBarrel",7);
            s.put("ecalEndcap",8);
            s.put("hcalBarrel",9);
            s.put("hcalEndcap",10);
            s.put("muonBarrel",11);
            s.put("muonEndcap",12);
            s.put("ecalForward",13);
            s.put("lumi",14);
        }
        return defaultSystemMap;		
    }	

    private void setSystemValues(SystemMap s)
    {
        if (s != null)
        {
            if (s.containsKey("unknown"))
                unknownValue = s.get("unknown");

            if (s.containsKey("vtxBarrel"))
                vtxBarrelValue = s.get("vtxBarrel");

            if (s.containsKey("vtxEndcap"))
                vtxEndcapValue = s.get("vtxEndcap");

            if (s.containsKey("sitBarrel"))
                sitBarrelValue = s.get("sitBarrel");

            if (s.containsKey("sitEndcap"))
                sitEndcapValue = s.get("sitEndcap");

            if (s.containsKey("sitForward"))
                sitForwardValue = s.get("sitForward");

            if (s.containsKey("tpc"))
                tpcValue = s.get("tpc");

            if (s.containsKey("ecalBarrel"))
                ecalBarrelValue = s.get("ecalBarrel");

            if (s.containsKey("ecalEndcap"))
                ecalEndcapValue = s.get("ecalEndcap");

            if (s.containsKey("hcalBarrel"))
                hcalBarrelValue = s.get("hcalBarrel");

            if (s.containsKey("hcalEndcap"))
                hcalEndcapValue = s.get("hcalEndcap");

            if (s.containsKey("muonBarrel"))
                muonBarrelValue = s.get("muonBarrel");

            if (s.containsKey("muonEndcap"))
                muonEndcapValue = s.get("muonEndcap");

            if (s.containsKey("ecalForward"))
                ecalForwardValue = s.get("ecalForward");

            if (s.containsKey("lumi"))
                lumiValue = s.get("lumi");
        }
    }

    private void setup(IIdentifierDictionary dict, SystemMap systemMap)
    {
        if (systemMap == null)
            systemMap = makeDefaultSystemMap();

        setSystemValues(systemMap);

        systemIndex = dict.getFieldIndex("system");
        barrelIndex = dict.getFieldIndex("barrel");

        // Optional layer field.
        if (dict.hasField("layer"))
            layerIndex = dict.getFieldIndex("layer");		

        barrelId         = makeBarrelId(barrelValue);
        endcapPositiveId = makeBarrelId(endcapPositiveValue);
        endcapNegativeId = makeBarrelId(endcapNegativeValue);

        vtxBarrelId         = makeSubsysId(vtxBarrelValue,barrelValue);
        vtxEndcapPositiveId = makeSubsysId(vtxEndcapValue,endcapPositiveValue);
        vtxEndcapNegativeId = makeSubsysId(vtxEndcapValue,endcapNegativeValue);

        sitBarrelId         = makeSubsysId(sitBarrelValue,barrelValue);
        sitEndcapPositiveId = makeSubsysId(sitEndcapValue,endcapPositiveValue);
        sitEndcapNegativeId = makeSubsysId(sitEndcapValue,endcapNegativeValue);

        sitForwardId               = makeSubsysId(sitForwardValue);
        sitForwardEndcapPositiveId = makeSubsysId(sitForwardValue,endcapPositiveValue);
        sitForwardEndcapNegativeId = makeSubsysId(sitForwardValue,endcapNegativeValue);

        tpcId = makeSubsysId(tpcValue);

        ecalBarrelId         = makeSubsysId(ecalBarrelValue,barrelValue);
        ecalEndcapPositiveId = makeSubsysId(ecalEndcapValue,endcapPositiveValue);
        ecalEndcapNegativeId = makeSubsysId(ecalEndcapValue,endcapNegativeValue);

        hcalBarrelId         = makeSubsysId(hcalBarrelValue,barrelValue);
        hcalEndcapPositiveId = makeSubsysId(hcalEndcapValue,endcapPositiveValue);
        hcalEndcapNegativeId = makeSubsysId(hcalEndcapValue,endcapNegativeValue);

        muonBarrelId         = makeSubsysId(muonBarrelValue,barrelValue);
        muonEndcapPositiveId = makeSubsysId(muonEndcapValue,endcapPositiveValue);
        muonEndcapNegativeId = makeSubsysId(muonEndcapValue,endcapNegativeValue);			

        ecalForwardId = makeSubsysId(ecalForwardValue);
        ecalForwardEndcapPositiveId = makeSubsysId(ecalForwardValue, endcapPositiveValue);
        ecalForwardEndcapNegativeId = makeSubsysId(ecalForwardValue, endcapNegativeValue);

        lumiId = makeSubsysId(lumiValue);
        lumiEndcapPositiveId = makeSubsysId(lumiValue,endcapPositiveValue);
        lumiEndcapNegativeId = makeSubsysId(lumiValue,endcapNegativeValue);		
    }
    
    // Public ctor.
    public DetectorIdentifierHelper(
    		IDetectorElement subdetectorDetectorElement, 
    		IIdentifierDictionary dict, 
    		SystemMap systemMap)
    {
        super(dict);
        try 
        {
            setup(dict,systemMap);
        }
        catch (Exception x)
        {
            throw new RuntimeException(x);
        }
        
        // Make association to a subdetector's DetectorElement.
        this.subdetectorDetectorElement = subdetectorDetectorElement;
        
        // Make assocation to a subdetector's system value.
        this.subdetectorSystemNumber = this.getSystemValue(this.subdetectorDetectorElement.getIdentifier());
        
        subdetectorType = SubdetectorType.convert(subdetectorDetectorElement.getName());
    }	

    private IIdentifier makeSubsysId(int system)
    {		
        // Check for invalid system id and replace with 0 to get a valid identifier.
        if (system == this.invalidSystemValue)
            system = 0;
        IExpandedIdentifier expid = 
            new ExpandedIdentifier(getIdentifierDictionary().getNumberOfFields());
        expid.setValue(systemIndex, system);
        IIdentifier id = pack(expid);
        return id;
    }

    private IIdentifier makeSubsysId(int system, int barrel)
    {		
        // Check for invalid system id and replace with 0 to get a valid identifier.
        if (system == this.invalidSystemValue)
            system = 0;
        IExpandedIdentifier expid = 
            new ExpandedIdentifier(getIdentifierDictionary().getNumberOfFields());
        expid.setValue(systemIndex, system);
        expid.setValue(barrelIndex, barrel);
        IIdentifier id = pack(expid);
        return id;
    }	

    private IIdentifier makeBarrelId(int barrel)
    {		
        IExpandedIdentifier expid = 
            new ExpandedIdentifier(getIdentifierDictionary().getNumberOfFields());
        expid.setValue(barrelIndex, barrel);
        IIdentifier id = pack(expid);
        return id;
    }	

    private boolean compareSystem(IIdentifier id, int system)
    {
        return unpack(id).getValue(systemIndex) == system;
    }	

    public int getBarrelValue()
    {
        return barrelValue;
    }

    public int getEndcapPositiveValue()
    {
        return endcapPositiveValue;
    }

    public int getEndcapNegativeValue()
    {
        return endcapNegativeValue;
    }

    public int getUnknownValue()
    {
        return unknownValue;
    }

    public int getVtxBarrelValue()
    {
        return vtxBarrelValue;
    }

    public int getVtxEndcapValue()
    {
        return vtxEndcapValue;
    }	

    public int getSitForwardValue()
    {
        return sitForwardValue;
    }

    public int getSitBarrelValue()
    {
        return sitBarrelValue;
    }
    public int getSitEndcapValue()
    {
        return sitEndcapValue;
    }

    public int getEcalBarrelValue()
    {
        return ecalBarrelValue;
    }
    public int getEcalEndcapValue()
    {
        return ecalEndcapValue;
    }		

    public IIdentifier getBarrelId()
    {
        return barrelId;
    }

    public IIdentifier getEndcapPositiveId()
    {
        return endcapPositiveId;
    }

    public IIdentifier getEndcapNegativeId()
    {
        return endcapNegativeId;
    }    

    public IIdentifier getVtxBarrelId()
    {
        return vtxBarrelId;
    }

    public IIdentifier getVtxEndcapId()
    {
        return vtxEndcapId;
    }
    public IIdentifier getVtxEndcapPositiveId()
    {
        return vtxEndcapPositiveId;
    }

    public IIdentifier getVtxEndcapNegativeId()
    {
        return vtxEndcapNegativeId;
    }   

    public IIdentifier getSitEndcapId()
    {
        return sitEndcapId;
    }   
    public IIdentifier getSitBarrelId()
    {
        return sitBarrelId;
    }

    public IIdentifier getSitEndcapPositiveId()
    {
        return sitEndcapPositiveId;
    }

    public IIdentifier getSitEndcapNegativeId()
    {
        return sitEndcapNegativeId;
    }    

    public IIdentifier getSitForwardId()
    {
        return sitForwardId;
    }

    public IIdentifier getSitForwardEndcapPositiveId()
    {
        return sitForwardEndcapPositiveId;
    }

    public IIdentifier getSitForwardEndcapNegativeId()
    {
        return sitForwardEndcapNegativeId;
    }

    public IIdentifier getTpcId()
    {
        return tpcId;
    }    

    public IIdentifier getEcalEndcapId()
    {
        return ecalEndcapId;
    }

    public IIdentifier getEcalBarrelId()
    {
        return ecalBarrelId;
    }

    public IIdentifier getEcalEndcapNegativeId()
    {
        return ecalEndcapNegativeId;
    }

    public IIdentifier getEcalEndcapPositiveId()    
    {
        return ecalEndcapPositiveId;
    }    

    public IIdentifier getHcalBarrelId()
    {
        return hcalBarrelId;
    }

    public IIdentifier getHcalEndcapNegativeId()
    {
        return hcalEndcapNegativeId;
    }

    public IIdentifier getHcalEndcapPositiveId()
    {
        return hcalEndcapPositiveId;
    }       

    public IIdentifier getMuonBarrelId()
    {
        return muonBarrelId;
    }
    public IIdentifier getMuonEndcapId()
    {
        return muonEndcapId;
    }
    public IIdentifier getMuonEndcapNegativeId()
    {
        return muonEndcapNegativeId;
    }

    public IIdentifier getMuonEndcapPositiveId()
    {
        return muonEndcapPositiveId;
    }       

    public IIdentifier getEcalForwardId()
    {
        return ecalForwardId;
    }    

    public IIdentifier getEcalForwardEndcapPositiveId()
    {
        return ecalForwardEndcapPositiveId;
    }

    public IIdentifier getEcalForwardEndcapNegativeId()
    {
        return ecalForwardEndcapNegativeId;
    }

    public IIdentifier getLumiId()
    {
        return lumiId;
    }

    public IIdentifier getLumiEndcapPositiveId()
    {
        return lumiEndcapPositiveId;
    }

    public IIdentifier getLumiEndcapNegativeId()
    {
        return lumiEndcapNegativeId;
    }    

    public boolean isBarrel(IIdentifier i)
    {
        return unpack(i).getValue(barrelIndex) == barrelValue;
    }

    public boolean isEndcap(IIdentifier i)
    {
        return isEndcapPositive(i) || isEndcapNegative(i);
    }

    public boolean isEndcapPositive(IIdentifier i)
    {
        return unpack(i).getValue(barrelIndex) == endcapPositiveValue;
    }

    public boolean isEndcapNegative(IIdentifier i)
    {
        return unpack(i).getValue(barrelIndex) == endcapNegativeValue;
    }    

    public boolean isTracker(IIdentifier i)
    { 
        return isVtx(i) || isTpc(i) || isSit(i) || isSitForward(i);
    }

    public boolean isTrackerBarrel(IIdentifier i)
    {
        return isTracker(i) && isBarrel(i);
    }

    public boolean isTrackerEndcap(IIdentifier i)
    {
        return isTracker(i) && isEndcap(i);
    }

    public boolean isTrackerEndcapPositive(IIdentifier i)
    {
        return isTracker(i) && isEndcapPositive(i);
    }

    public boolean isTrackerEndcapNegative(IIdentifier i)
    {
        return isTracker(i) && isEndcapNegative(i);
    }            

    public boolean isCalorimeter(IIdentifier i)
    {
        return isEcal(i) || isHcal(i) || isMuon(i) || isEcalForward(i) || isLumi(i);
    }

    public boolean isCalorimeterBarrel(IIdentifier i)
    {
        return isCalorimeter(i) && isBarrel(i);
    }

    public boolean isCalorimeterEndcap(IIdentifier i)
    {
        return isCalorimeter(i) && isEndcap(i);
    }

    public boolean isCalorimeterEndcapPositive(IIdentifier i)
    {
        return isCalorimeter(i) && isEndcapPositive(i);
    }

    public boolean isCalorimeterEndcapNegative(IIdentifier i)
    {
        return isCalorimeter(i) && isEndcapNegative(i);
    }

    public boolean isVtx(IIdentifier i)
    {
        return compareSystem(i,vtxBarrelValue) || compareSystem(i,vtxEndcapValue);
    }

    public boolean isVtxBarrel(IIdentifier i)
    {
        return isVtx(i) && isBarrel(i);
    }

    public boolean isVtxEndcap(IIdentifier i)
    {
        return compareSystem(i,vtxEndcapValue) && isEndcap(i);
    }

    public boolean isVtxEndcapPositive(IIdentifier i)
    {
        return isVtx(i) && isEndcapPositive(i);
    }

    public boolean isVtxEndcapNegative(IIdentifier i)
    {
        return isVtx(i) && isEndcapNegative(i);
    }    

    public boolean isSit(IIdentifier i)
    {
        return compareSystem(i,sitBarrelValue) || compareSystem(i,sitEndcapValue);
    }

    public boolean isSitBarrel(IIdentifier i)
    {
        return isSit(i) && isBarrel(i);
    }

    public boolean isSitEndcap(IIdentifier i)
    {
        return isSit(i) && isEndcap(i);
    }

    public boolean isSitEndcapPositive(IIdentifier i)
    {
        return isSit(i) && isEndcapPositive(i);
    }

    public boolean isSitEndcapNegative(IIdentifier i)
    {
        return isSit(i) && isEndcapNegative(i);
    }      

    public boolean isSitForward(IIdentifier i)
    {
        return compareSystem(i,sitForwardValue);
    }

    public boolean isSitForwardEndcapNegative(IIdentifier i)
    {
        return isSitForward(i) && isEndcapNegative(i);
    }

    public boolean isSitForwardEndcapPositive(IIdentifier i)
    {
        return isSitForward(i) && isEndcapPositive(i);
    }

    public boolean isTpc(IIdentifier i)
    {
        return compareSystem(i,tpcValue);
    }        

    public boolean isEcal(IIdentifier i)
    {
        return compareSystem(i,ecalBarrelValue) || compareSystem(i,ecalEndcapValue);
    }

    public boolean isEcalBarrel(IIdentifier i)
    {
        return isEcal(i) && isBarrel(i);
    }

    public boolean isEcalEndcap(IIdentifier i)
    {
        return isEcal(i) && isEndcap(i);
    }

    public boolean isEcalEndcapPositive(IIdentifier i)
    {
        return isEcal(i) && isEndcapPositive(i);
    }

    public boolean isEcalEndcapNegative(IIdentifier i)
    {
        return isEcal(i) && isEndcapNegative(i);
    }            

    public boolean isHcal(IIdentifier i)
    {
        return compareSystem(i,hcalBarrelValue) || compareSystem(i,hcalEndcapValue);
    }

    public boolean isHcalBarrel(IIdentifier i)
    {
        return isHcal(i) && isBarrel(i);
    }

    public boolean isHcalEndcap(IIdentifier i)
    {
        return isHcal(i) && isEndcap(i);
    }

    public boolean isHcalEndcapPositive(IIdentifier i)
    {
        return isHcal(i) && isEndcapPositive(i);
    }

    public boolean isHcalEndcapNegative(IIdentifier i)
    {
        return isHcal(i) && isEndcapNegative(i);
    }                

    public boolean isMuon(IIdentifier i)
    {
        return compareSystem(i,muonBarrelValue) || compareSystem(i,muonEndcapValue);
    }

    public boolean isMuonBarrel(IIdentifier i)
    {
        return isMuon(i) && isBarrel(i);
    }   

    public boolean isMuonEndcap(IIdentifier i)
    {
        return isMuon(i) && isEndcap(i);
    }

    public boolean isMuonEndcapPositive(IIdentifier i)
    {
        return isMuon(i) && isEndcapPositive(i);
    }

    public boolean isMuonEndcapNegative(IIdentifier i)
    {
        return isMuon(i) && isEndcapNegative(i);
    }       

    public boolean isEcalForward(IIdentifier i)
    {
        return compareSystem(i,ecalForwardValue);
    }

    public boolean isEcalForwardEndcapPositive(IIdentifier i)
    {
        return isEcalForward(i) && isEndcapPositive(i);
    }

    public boolean isEcalForwardEndcapNegative(IIdentifier i)
    {
        return isEcalForward(i) && isEndcapNegative(i);
    }       

    public boolean isLumi(IIdentifier i)
    {
        return compareSystem(i,lumiValue);
    }        

    public boolean isLumiEndcapPositive(IIdentifier i)
    {
        return isLumi(i) && isEndcapPositive(i);
    }

    public boolean isLumiEndcapNegative(IIdentifier i)
    {
        return isLumi(i) && isEndcapNegative(i);
    }

    public int getSystemValue(IIdentifier i)
    {
        return unpack(i).getValue(systemIndex);
    }

    public int getBarrelValue(IIdentifier i)
    {
        return unpack(i).getValue(barrelIndex);
    }

    public int getLayerValue(IIdentifier i)
    {
        if (layerIndex == invalidIndex)
            throw new RuntimeException("The layer number is not available, because " + getIdentifierDictionary().getName() + " does not have a layer field!");
        return unpack(i).getValue(layerIndex);
    }

    public boolean layerEquals(IIdentifier i, int layer)
    {
        return getLayerValue(i) == layer;
    }

    public boolean systemEquals(IIdentifier i, int system)
    {
        return getSystemValue(i) == system;
    }

    public boolean barrelEquals(IIdentifier i, int barrel)
    {
        return getBarrelValue(i) == barrel;
    }

    public int getSystemIndex()
    {
        return systemIndex;
    }

    public int getLayerIndex()
    {
        return layerIndex;
    }

    public int getBarrelIndex()
    {
        return barrelIndex;
    }

    public int getInvalidIndex()
    {
        return invalidIndex;
    }
    
    public String getSubdetectorName()
    {
    	return subdetectorDetectorElement.getName();
    }
    
    public int getSubdetectorSystemNumber()
    {
    	return subdetectorSystemNumber;
    }
    
    public IDetectorElement getSubdetectorDetectorElement()
    {
    	return subdetectorDetectorElement;
    }      
    
    public SubdetectorType getSubdetectorType()
    {
    	return subdetectorType;
    }
}