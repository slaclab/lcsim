package org.lcsim.lcio;

import hep.io.sio.SIOInputStream;
import hep.io.sio.SIOOutputStream;
import hep.io.sio.SIORef;

import java.io.IOException;
import org.lcsim.event.LCRelation;

/**
 * SIO-based I/O implementation of the LCRelation interface
 *
 * @author Guilherme Lima
 * @version $Id: SIOLCRelation.java,v 1.4 2007/10/17 02:06:23 tonyj Exp $
 */
class SIOLCRelation implements LCRelation
{
   protected SIORef from;
   protected SIORef to;
   protected float weight;
   
   protected SIOLCRelation() {      
   }

    /** Constructor for reading objects from file
     */
    SIOLCRelation(SIOInputStream in, int flags, int version) throws IOException
    {
       from = in.readPntr();
       to = in.readPntr();
       if (LCIOUtil.bitTest(flags,LCIOConstants.LCREL_WEIGHTED)) weight = in.readFloat();
    }

    /** Returns the 'from' object
     */
    public Object getFrom() {
       return from.getObject();
    }
    /** Returns the 'to' object
     */
    public Object getTo() {
	return to.getObject();
    }

    /** Returns the weight of the relation
     */
    public float getWeight() {
	return weight;
    }

    /** Weight should be between zero and one
     */
    public void setWeight(float weight) {
	this.weight = weight;
    }

    /** Writes relations out to LCIO stream
     */
   static void write(LCRelation rel, SIOOutputStream out, int flags) throws IOException
   {
      out.writePntr( rel.getFrom() );
      out.writePntr( rel.getTo() );
      if (LCIOUtil.bitTest(flags,LCIOConstants.LCREL_WEIGHTED)) out.writeFloat( rel.getWeight() );
   }
}
