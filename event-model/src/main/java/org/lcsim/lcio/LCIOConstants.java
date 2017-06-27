package org.lcsim.lcio;

/**
 *
 * @author tonyj
 */
public interface LCIOConstants
{
   // major and minor version numbers
   int MAJORVERSION = 2;
   int MINORVERSION = 7;
    
   // bits in flag words
   // SimCalorimeterHit (CH)
   int CHBIT_LONG = 31;
   
   // long(1) - short(0) , incl./excl. position
   int CHBIT_BARREL = 30;
   
   // barrel(1) - endcap(0)
   int CHBIT_ID1 = 29;
   
   // cellid1 stored
   int CHBIT_PDG = 28; // deprecated
   
   int CHBIT_STEP = 28;
   
   int RCHBIT_LONG = 31;
   
   // store energy error
   int RCHBIT_ENERGY_ERROR = 26;

   // long(1) - short(0) , incl./excl. position
   int RCHBIT_BARREL = 30;

   // barrel(1) - endcap(0) 
   int RCHBIT_ID1 = 29;

   // cellid1 stored
   int RCHBIT_NO_PTR = 28;

   // 1: pointer tag not added
   int RCHBIT_TIME = 27;
   
   // 1: time information stored
   // SimTrackerHit
   int THBIT_BARREL = 31;
   
   // barrel(1) - endcap(0)   
   int THBIT_MOMENTUM = 30 ; 
   
   // CellID1 stored for SimTrackerHit and TrackerHit.
   int THBIT_ID1 = 29;

   // momentum of particle stored(1) - not stored(0)
   // Tracks
   int TRBIT_HITS = 31;
   
   // hits stored(1) - not stored(0)
   // Cluster
   int CLBIT_HITS = 31;
   
   // hits stored(1) - not stored(0)
   // TPCHit
   int TPCBIT_RAW = 31;
   
   // raw data stored(1) - not stored(0)
   int TPCBIT_NO_PTR = 30;
   
   //  cellid1 stored
   int TRAWBIT_ID1 = 31;
   
   // SimTrackerHit references stored (1) - not stored (0).
   // FIXME: Non-standard LCIO constant.  Needs to be propagated to lcio standalone project.
   //int RTHBIT_HITS = 27;
   
   // 1: pointer tag not added (needed for TrackerHit)
   // LCRelation
   //  public const int LCREL_ONE2MANY = 31 ; // relation is one to many
   int LCREL_WEIGHTED = 31;
   
   // relations has weights
   // LCGenericObject
   int GOBIT_FIXED = 31;
   
   int BITTransient = 16 ;
   int BITDefault   = 17 ;
   int BITSubset    = 18 ;
   
   String runRecordName = "LCRunHeader";
   String runBlockName = "RunHeader";
   String eventRecordName = "LCEvent";
   String eventBlockName = "Event";
   String eventHeaderRecordName = "LCEventHeader";
   String eventHeaderBlockName = "EventHeader";
   
   String references = "_References";
}
