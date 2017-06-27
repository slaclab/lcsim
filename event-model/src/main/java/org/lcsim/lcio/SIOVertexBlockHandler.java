package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import hep.io.sio.SIOWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.lcsim.event.EventHeader;
import org.lcsim.event.Vertex;

/**
 *
 * @author tonyj
 */
class SIOVertexBlockHandler extends AbstractBlockHandler
{
   private static final String ALGORITHM_TYPES = "_lcio.VertexAlgorithmTypes";
   private static final String PARAMETER_NAMES = "VertexParameterNames";
   
   private List<String> algorithmTypeKeys = new ArrayList<String>();
   private List<String> parameterNameKeys = new ArrayList<String>();
   
   public String getType()
   { 
      return "Vertex"; 
   }
   public Class getClassForType()
   { 
      return Vertex.class; 
   }
   LCIOCallback addCollectionElements(LCIOEvent event, LCIOCollection collection, SIOInputStream in, int n, int version) throws IOException
   {
      String[] typeKeys = collection.getParameters().getStringMap().get(ALGORITHM_TYPES);
      String[] parameterNames = collection.getParameters().getStringMap().get(PARAMETER_NAMES);
      
      for (int i = 0; i < n; i++)
         collection.add(new SIOVertex(in, collection.getFlags(), version, typeKeys, parameterNames));
      
      return null;
   }
   void writeCollectionElement(Object element, SIOOutputStream out, int flags) throws IOException
   {
      SIOVertex.write((Vertex) element, out, flags, algorithmTypeKeys, parameterNameKeys);
   }
   
   public void writeBlock(SIOWriter writer, List collection, EventHeader.LCMetaData md) throws IOException
   {
      Set<String> types = new HashSet<String>();
      Set<String> names = new HashSet<String>();
      for (Vertex v : (List<Vertex>) collection)
      {
         types.add(v.getAlgorithmType());
         for (String name : v.getParameters().keySet())
         {
            names.add(name);
         }
      }
      algorithmTypeKeys = new ArrayList(types);
      String[] keyStrings = new String[types.size()];
      md.getStringParameters().put(ALGORITHM_TYPES,types.toArray(keyStrings));
      
      parameterNameKeys = new ArrayList(names);
      String[] nameStrings = new String[names.size()];
      md.getStringParameters().put(PARAMETER_NAMES,names.toArray(nameStrings));     
      
      super.writeBlock(writer, collection, md);
   }
}
