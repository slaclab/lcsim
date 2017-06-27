package org.lcsim.lcio;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author tonyj
 */
class HandlerManager
{
   private Map<String,LCIOBlockHandler> handlerForType = new HashMap<String,LCIOBlockHandler>();
   private Map<Class,LCIOBlockHandler> handlerForClass = new LinkedHashMap<Class,LCIOBlockHandler>();
   private void register(LCIOBlockHandler handler)
   {
      handlerForType.put(handler.getType(),handler);
      handlerForClass.put(handler.getClassForType(),handler);
   }
   private static HandlerManager theHandlerManager = new HandlerManager();
   /** Creates a new instance of HandlerManager */
   private HandlerManager()
   {
      // Note: order is important, most specific (e.g. SimCalorimeterHit) must come
      // before less specific (e.g. CalorimeterHit).
      register(new SIOSimCalorimeterHitBlockHandler());
      register(new SIOSimTrackerHitBlockHandler());
      register(new SIOMCParticleBlockHandler());
      register(new SIOClusterBlockHandler());
      register(new SIOTPCHitBlockHandler());
      register(new SIOTrackerHitBlockHandler());
      register(new SIOTrackBlockHandler());
      register(new SIORawCalorimeterHitBlockHandler());
      register(new SIOLCRelationBlockHandler());
      register(new SIOReconstructedParticleBlockHandler());
      register(new SIOCalorimeterHitBlockHandler());
      register(new SIOGenericObjectBlockHandler());
      register(new SIORawTrackerHitBlockHandler());
      register(new SIOSiliconTrackerHitBlockHandler());
      register(new SIOTrackerDataBlockHandler());
      register(new SIOTrackerPulseBlockHandler());
      register(new SIOVertexBlockHandler());
      register(new SIOFloatVecBlockHandler());
      register(new SIOIntVecBlockHandler());
      register(new SIOStringVecBlockHandler());
   }
   static HandlerManager instance()
   {
      return theHandlerManager;
   }
   LCIOBlockHandler handlerForType(String type)
   {
      if (type.endsWith(LCIOConstants.references))
      {
         String subType = type.substring(0,type.length()-LCIOConstants.references.length());
         LCIOBlockHandler subHandler = handlerForType.get(subType);
         if (subHandler == null) return null;
         else return new SIOReferencesBlockHandler(subType,subHandler.getClassForType());
      }
      else return handlerForType.get(type);
   }
   LCIOBlockHandler handlerForClass(Class type, int flags)
   {
      LCIOBlockHandler handler = handlerForClass.get(type);

      if (handler == null)
      {
         for (LCIOBlockHandler h : handlerForType.values())
         {
            if (h.getClassForType().isAssignableFrom(type))
            {
               handler = h;
               handlerForClass.put(type,h);
               break; // Fixme: What if a better match
            }
         }
      }
      if (LCIOUtil.bitTest(flags,LCIOConstants.BITSubset))
      {
         return new SIOReferencesBlockHandler(handler.getType()+LCIOConstants.references,type);
      }
      else return handler;
   }
}
