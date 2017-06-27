package org.lcsim.geometry.compact.converter.lcdd.util;

import java.util.HashMap;
import java.util.Map;
import org.jdom.Element;

/**
 * 
 * @author tonyj
 */
public class Define extends Element {

    private Map rotations = new HashMap();
    private Map constants = new HashMap();
    private Map positions = new HashMap();
    private Map matrices = new HashMap();

    public Define() {
        super("define");
    }

    public void addConstant(Constant constant) {
        addContent(constant);
        constants.put(constant.getRefName(), constant);
    }

    public void addPosition(Position position) {
        addContent(position);
        positions.put(position.getRefName(), position);
    }

    public void addRotation(Rotation rotation) {
        addContent(rotation);
        rotations.put(rotation.getRefName(), rotation);
    }

    public Rotation getRotation(String name) {
        return (Rotation) rotations.get(name);
    }

    public Position getPosition(String name) {
        return (Position) positions.get(name);
    }

    public Constant getConstant(String name) {
        return (Constant) constants.get(name);
    }
    
    public void addMatrix(Matrix matrix) {
        addContent(matrix);
        this.matrices.put(matrix.getRefName(), matrix);
    }
    
    public Matrix getMatrix(String name) {
        return (Matrix) matrices.get(name);
    }
}
