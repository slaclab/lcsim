package org.lcsim.detector.solids;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * An @see ISolid representing a box.  Lengths are
 * half lengths in x, y, and z.
 *
 * @author Tim Nelson <tknelson@slac.stanford.edu>
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 * @version $Id: Box.java,v 1.6 2008/11/20 19:06:13 jeremy Exp $
 */
public class Box extends AbstractPolyhedron
{
    
    private final static int[] _HEPREP_VERTEX_ORDERING = {2,3,1,0,6,7,5,4};
    
    double xHalf, yHalf, zHalf;
    
    public Box(String name, double halfX, double halfY, double halfZ)
    {
        super(name);
        this.xHalf = halfX;
        this.yHalf = halfY;
        this.zHalf = halfZ;
    }
    
    public double getXHalfLength()
    {
        return xHalf;
    }
    
    public double getYHalfLength()
    {
        return yHalf;
    }
    
    public double getZHalfLength()
    {
        return zHalf;
    }

    public double getCubicVolume()
    {
        return 8 * (getXHalfLength() * getYHalfLength() * getZHalfLength());
    }
    
    // Implementation of IPolyhedron
    
    public int[] getHepRepVertexOrdering()
    {
        return _HEPREP_VERTEX_ORDERING;
    }
    
    public List<Polygon3D> getFaces()
    {
        List<Polygon3D> faces = new ArrayList<Polygon3D>();
        
        for (int ix = -1; ix <=+1; ix += 2)
        {
            List<Point3D> vertices = new ArrayList<Point3D>();
            vertices.add(new Point3D(ix*xHalf,+yHalf,+zHalf));
            vertices.add(new Point3D(ix*xHalf,-yHalf,+zHalf));
            vertices.add(new Point3D(ix*xHalf,-yHalf,-zHalf));
            vertices.add(new Point3D(ix*xHalf,+yHalf,-zHalf));
            faces.add(new Polygon3D(vertices));
        }
        for (int iy = -1; iy <=+1; iy += 2)
        {
            List<Point3D> vertices = new ArrayList<Point3D>();
            vertices.add(new Point3D(+xHalf,iy*yHalf,+zHalf));
            vertices.add(new Point3D(+xHalf,iy*yHalf,-zHalf));
            vertices.add(new Point3D(-xHalf,iy*yHalf,-zHalf));
            vertices.add(new Point3D(-xHalf,iy*yHalf,+zHalf));
            faces.add(new Polygon3D(vertices));
        }
        for (int iz = -1; iz <=+1; iz += 2)
        {
            List<Point3D> vertices = new ArrayList<Point3D>();
            vertices.add(new Point3D(+xHalf,+yHalf,iz*zHalf));
            vertices.add(new Point3D(-xHalf,+yHalf,iz*zHalf));
            vertices.add(new Point3D(-xHalf,-yHalf,iz*zHalf));
            vertices.add(new Point3D(+xHalf,-yHalf,iz*zHalf));
            faces.add(new Polygon3D(vertices));
        }
        
        for (Polygon3D face : faces) face.faceOutward();
        
        return faces;
    }
    
    public List<LineSegment3D> getEdges()
    {
        List<LineSegment3D> edges = new ArrayList<LineSegment3D>();
        
        for (int ix = -1; ix <=+1; ix += 2)
        {
            for (int iy = -1; iy <=+1; iy += 2)
            {
                Point3D startpoint = new Point3D(ix*xHalf,iy*yHalf,-zHalf);
                Point3D endpoint = new Point3D(ix*xHalf,iy*yHalf,zHalf);
                edges.add(new LineSegment3D(startpoint,endpoint));
            }
        }
        
        for (int ix = -1; ix <=+1; ix += 2)
        {
            for (int iz = -1; iz <=+1; iz += 2)
            {
                Point3D startpoint = new Point3D(ix*xHalf,-yHalf,iz*zHalf);
                Point3D endpoint = new Point3D(ix*xHalf,yHalf,iz*zHalf);
                edges.add(new LineSegment3D(startpoint,endpoint));
            }
        }
        
        for (int iy = -1; iy <=+1; iy += 2)
        {
            for (int iz = -1; iz <=+1; iz += 2)
            {
                Point3D startpoint = new Point3D(-xHalf,iy*yHalf,iz*zHalf);
                Point3D endpoint = new Point3D(xHalf,iy*yHalf,iz*zHalf);
                edges.add(new LineSegment3D(startpoint,endpoint));
            }
        }
        
        return edges;
    }
    
    public List<Point3D> getVertices()
    {
        List<Point3D> vertices = new ArrayList<Point3D>();
        for (int iz = -1; iz <=+1; iz += 2)
        {
            for (int iy = -1; iy <=+1; iy += 2)
            {
                for (int ix = -1; ix <=+1; ix += 2)
                {
                    vertices.add(new Point3D(ix*xHalf,iy*yHalf,iz*zHalf));
                }
            }
        }
        return vertices;
    }
    
    public String toString()
    {
        return this.getClass().getSimpleName()+" "+name+" : xHalf= "+xHalf+ " yHalf= "+yHalf+" zHalf= "+zHalf;
    }
}