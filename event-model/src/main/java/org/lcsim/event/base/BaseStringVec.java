package org.lcsim.event.base;

import java.util.AbstractList;
import org.lcsim.event.StringVec;

/**
 * Unmodifiable implementation of FloatVec. 
 * @author tonyj
 */
public class BaseStringVec extends AbstractList<String> implements StringVec  {

    private String[] vec;

    public BaseStringVec(String[] vec) {
        this.vec = vec;
    }

    protected BaseStringVec() {

    }
    protected void setVec(String[] vec) {
        this.vec = vec;
    }

    @Override
    public String get(int index) {
        return vec[index];
    }

    @Override
    public int size() {
        return vec.length;
    }

    public String[] toStringArray() {
        return vec;
    }


}
