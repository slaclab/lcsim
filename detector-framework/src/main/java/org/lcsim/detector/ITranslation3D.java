/*
 * ITranslation3D.java
 *
 * Created on August 6, 2007, 2:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.detector;

import hep.physics.vec.Hep3Vector;

/**
 *
 * Interface for translations in 3D space. 
 * 
 * @author Tim Nelson <tknelson@slac.stanford.edu>
 */
public interface ITranslation3D extends Hep3Vector
{    
    public Hep3Vector getTranslationVector();   
    public void setTranslationVector(Hep3Vector translation);    
    public void translate(Hep3Vector coordinates);    
    public Hep3Vector translated(Hep3Vector coordinates);    
    public void invert();    
    public ITranslation3D inverse();
    
}
