package org.lcsim.event;

/**
 * A single weighted relationship between two LCObjects. Stored in an
 * LCCollection like any other LCObject.  The types of the objects is
 * decoded in the collection parameters 'RelationFromType' and
 * 'RelationToType'.
 * 
 * @author Guilherme Lima
 * @version $Id: LCRelation.java,v 1.2 2005/04/26 20:49:37 tonyj Exp $
 */

public interface LCRelation {

    /** The 'from' object of the relation. 
     */
    public Object getFrom();

    /** The 'to' object of the relation. 
     */
    public Object getTo();

    /** The weight of the relation. 
     */
    public float getWeight();
} // class or interface

