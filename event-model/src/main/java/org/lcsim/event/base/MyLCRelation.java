package org.lcsim.event.base;

import org.lcsim.event.LCRelation;

/**
 * Implements a simHit to rawHit relation, to be used within DigiSim.
 *
 * @author Guilherme Lima
 * @version $Id: MyLCRelation.java,v 1.1 2005/04/26 23:20:34 lima Exp $
 */
public class MyLCRelation implements LCRelation
{
    /** Private constructor */
    private MyLCRelation() { }

    /** Default weight is one
     */
    public MyLCRelation(Object from, Object to)
    {
	this(from,to,1);
    }

    /** Full constructor
     */
    public MyLCRelation(Object from, Object to, float weight)
    {
	this.from = from;
	this.to = to;
	this.weight = weight;
    }

    /** Returns the 'from' object
     */
    public Object getFrom() {
	return from;
    }

    /** Returns the 'to' object
     */
    public Object getTo() {
	return to;
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

    //=== FIELDS ===

    /** The 'from' object of the relation */
    protected Object from;

    /** The 'to' object of the relation */
    protected Object to;

    /** Relation weight */
    protected float weight;
}
