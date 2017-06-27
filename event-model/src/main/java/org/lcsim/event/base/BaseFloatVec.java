package org.lcsim.event.base;

import java.util.AbstractList;
import org.lcsim.event.FloatVec;

/**
 * Unmodifiable implementation of FloatVec. 
 * @author tonyj
 */
public class BaseFloatVec extends AbstractList<Float> implements FloatVec  {

    private float[] vec;

    public BaseFloatVec(float[] vec) {
        this.vec = vec;
    }

    protected BaseFloatVec() {

    }
    protected void setVec(float[] vec) {
        this.vec = vec;
    }

    @Override
    public Float get(int index) {
        return vec[index];
    }

    @Override
    public int size() {
        return vec.length;
    }

    public float[] toFloatArray() {
        return vec;
    }


}
