package org.lcsim.event.base;

import java.util.AbstractList;
import org.lcsim.event.IntVec;

/**
 * Unmodifiable implementation of FloatVec. 
 * @author tonyj
 */
public class BaseIntVec extends AbstractList<Integer> implements IntVec  {

    private int[] vec;

    public BaseIntVec(int[] vec) {
        this.vec = vec;
    }

    protected BaseIntVec() {

    }
    protected void setVec(int[] vec) {
        this.vec = vec;
    }

    @Override
    public Integer get(int index) {
        return vec[index];
    }

    @Override
    public int size() {
        return vec.length;
    }

    public int[] toIntArray() {
        return vec;
    }


}
