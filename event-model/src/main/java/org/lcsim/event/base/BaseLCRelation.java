package org.lcsim.event.base;

import org.lcsim.event.LCRelation;

/**
 * Implementation of the LCRelation interface.
 * 
 * @author <a href="mailto:christian.grefe@cern.ch">Christian Grefe</a>
 */
public class BaseLCRelation implements LCRelation {

	protected Object from;
	protected Object to;
	protected float weight;
	
	public BaseLCRelation(Object from, Object to) {
		this(from, to, 1);
	}
	
	public BaseLCRelation(Object from, Object to, double weight) {
		this.from = from;
		this.to = to;
		this.weight = (float)weight;
	}
	
	public Object getFrom() {
		return from;
	}
	
	public Object getTo() {
		return to;
	}
	
	public float getWeight() {
		return weight;
	}
	
	public void setWeight(double weight) {
		this.weight = (float)weight;
	}
	
}
