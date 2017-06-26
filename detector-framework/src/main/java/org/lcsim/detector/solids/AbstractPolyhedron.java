/*
 * AbstractPolyhedron.java
 *
 * Created on November 29, 2007, 9:45 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.lcsim.detector.solids;

import hep.physics.vec.Hep3Vector;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * This solid represents a bounded convex 3D polyhedron.
 *
 * @author tknelson
 */
public abstract class AbstractPolyhedron extends AbstractSolid implements IPolyhedron
{
    
    /** Creates a new instance of AbstractPolyhedron */
    public AbstractPolyhedron(String name)
    {
        super(name);
    }
    
    public Inside inside(Hep3Vector p)
    {
        Point3D point = new Point3D(p);
        
        boolean inside = false;
        for (Polygon3D face : getFaces())
        {
            if (GeomOp3D.intersects(point,face)) return Inside.SURFACE;
            if (!(inside = GeomOp3D.distanceBetween(point,face.getPlane()) < 0))
                break;
        }
        
        if (inside) return Inside.INSIDE;
        else return Inside.OUTSIDE;
    }
    
    public List<Polygon3D> getFacesNormalTo(Hep3Vector normal)
    {
        List<Polygon3D> faces = new ArrayList<Polygon3D>();
      
        for (Polygon3D face : getFaces())
        {
            if (GeomOp3D.isNormal(normal,face)) 
            {
                faces.add(face);
            }
        }
        return faces;
    }
    
}
