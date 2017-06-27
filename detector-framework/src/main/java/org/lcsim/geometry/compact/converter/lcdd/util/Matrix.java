package org.lcsim.geometry.compact.converter.lcdd.util;


/**
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: Matrix.java,v 1.1 2013/04/24 02:00:35 jeremy Exp $
 */
public class Matrix extends RefElement {
    
    int coldim = 0;
    
    public Matrix(String name, int coldim) {
        super("matrix", name);
        if (name == null)
            throw new IllegalArgumentException("name is null");
        if (coldim <= 0) {
            throw new IllegalArgumentException("invalid coldim value: " + coldim);
        }
        setAttribute("coldim", String.valueOf(coldim));        
    }
    
    public void setValues(String values) {
        setAttribute("values", values);
    }
}
