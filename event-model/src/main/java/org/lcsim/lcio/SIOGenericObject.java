package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import org.lcsim.event.GenericObject;

import java.io.IOException;

/**
 *
 * @author gaede
 * @version $Id: SIOGenericObject.java,v 1.8 2011/03/22 16:48:36 tonyj Exp $
 */
class SIOGenericObject implements GenericObject
{
   private boolean _isFixedSize ;
   private int[] _intVec;
   private float[] _floatVec;
   private double[] _doubleVec;
   
   // Creates a new instance of SIOGenericObject with variable size
   
   SIOGenericObject(SIOInputStream in, int flag, int version) throws IOException
   {
      int nInt = in.readInt();
      int nFloat = in.readInt();
      int nDouble = in.readInt();
      read(in,flag,version,nInt,nFloat,nDouble);
      _isFixedSize = false;
   }
   
   SIOGenericObject(SIOInputStream in, int flag, int version, int nInt, int nFloat, int nDouble) throws IOException
   {	
      read(in,flag,version,nInt,nFloat,nDouble);
      _isFixedSize = true;
   }   
   
   private void read(SIOInputStream in, int flag, int version, int nInt, int nFloat, int nDouble ) throws IOException
   {
      _intVec = new int[nInt] ;
      for (int i=0; i<nInt; i++)
      {
         _intVec[i] = in.readInt();
      }

      _floatVec = new float[nFloat] ;
      for (int i=0; i<nFloat; i++)
      {
         _floatVec[i] = in.readFloat();
      }
      
      _doubleVec = new double[nDouble] ;
      for (int i=0; i<nDouble; i++)
      {
         _doubleVec[i] = in.readDouble();
      }
      in.readPTag(this); 
   }
   
   static void write(GenericObject object, SIOOutputStream out, int flags) throws IOException
   {
      if (!LCIOUtil.bitTest(flags, LCIOConstants.GOBIT_FIXED))
      {
         out.writeInt(object.getNInt());
         out.writeInt(object.getNFloat());
         out.writeInt(object.getNDouble());
      }
      for(int i=0;i<object.getNInt();i++)
         out.writeInt( object.getIntVal(i)) ;
      for(int i=0;i<object.getNFloat();i++)
         out.writeFloat( object.getFloatVal(i)) ;
      for(int i=0;i<object.getNDouble();i++)
         out.writeDouble( object.getDoubleVal(i)) ;
      
      out.writePTag(object);
   }
   public int getNInt()
   {
      return _intVec.length;
   }

   public int getNFloat()
   {
      return _floatVec.length;
   }

   public int getNDouble()
   {
      return _doubleVec.length;
   }

   public int getIntVal(int index)
   {
      return _intVec[index];
   }
   
   public float getFloatVal(int index)
   {
      return _floatVec[index];
   }
   
   public double getDoubleVal(int index)
   {
      return _doubleVec[index];
   }

   public boolean isFixedSize()
   {
      return _isFixedSize;
   }
}
