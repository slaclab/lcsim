package org.lcsim.detector.solids;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Trapezoid solid that is based on Geant4's
 * <a href="http://www.lcsim.org/software/geant4/doxygen/html/classG4Trd.html">G4Trd class</a>.
 *
 * @author Jeremy McCormick
 * @author Tim Nelson 
 * @version $Id: Trd.java,v 1.8 2010/04/14 18:24:54 jeremy Exp $
 */
public class Trd extends AbstractPolyhedron
{
    
    private final static int[] _HEPREP_VERTEX_ORDERING = {2,3,1,0,6,7,5,4};
    
    double dx1,dx2,dy1,dy2,dz;
    double volume;
    private static final double tolerance=1E-9;
    
    public Trd(
            String name,
            double dx1,
            double dx2,
            double dy1,
            double dy2,
            double dz)
    {
        super(name);
        
        this.dx1=dx1;
        this.dx2=dx2;
        this.dy1=dy1;
        this.dy2=dy2;
        this.dz=dz;
    }
    
    public double getCubicVolume()
    {
        if(volume != 0.)
        {;}
        else
        {
            volume = 2*dz*( (dx1+dx2)*(dy1+dy2) + (dx2-dx1)*(dy2-dy1)/3 );
        }
        return volume;
    }
    
    public double getXHalfLength1()
    {
        return dx1;
    }
    
    public double getXHalfLength2()
    {
        return dx2;
    }
    
    public double getYHalfLength1()
    {
        return dy1;
    }
    
    public double getYHalfLength2()
    {
        return dy2;
    }
    
    public double getZHalfLength()
    {
        return dz;
    }
    
    // Implementation of IPolyhedron
    public int[] getHepRepVertexOrdering()
    {
        return _HEPREP_VERTEX_ORDERING;
    }
    
    public List<Polygon3D> getFaces()
    {
        List<Polygon3D> faces = new ArrayList<Polygon3D>();
        
        List<Point3D> vertices = getVertices();
        
        // End with normal -Z
        faces.add(new Polygon3D(Arrays.asList(vertices.get(0),vertices.get(1),vertices.get(3),vertices.get(2))));
        
        // End with normal +Z
        faces.add(new Polygon3D(Arrays.asList(vertices.get(4),vertices.get(5),vertices.get(7),vertices.get(6))));
        
        // Bottom side with normal approx. -Y
        faces.add(new Polygon3D(Arrays.asList(vertices.get(0),vertices.get(4),vertices.get(5),vertices.get(1))));
        
        // Top side with normal approx. +Y
        faces.add(new Polygon3D(Arrays.asList(vertices.get(2),vertices.get(3),vertices.get(7),vertices.get(6))));
        
        // Front side with normal approx. -X
        faces.add(new Polygon3D(Arrays.asList(vertices.get(0),vertices.get(2),vertices.get(6),vertices.get(4))));
        
        // Back side iwth normal approx. +X
        faces.add(new Polygon3D(Arrays.asList(vertices.get(1),vertices.get(5),vertices.get(7),vertices.get(3))));
        
        for (Polygon3D face : faces) face.faceOutward();
        
        return faces;
        
    }
    
    public List<LineSegment3D> getEdges()
    {
        List<LineSegment3D> edges = new ArrayList<LineSegment3D>();
        
        List<Point3D> vertices = getVertices();
        
        // From -z to +z
        edges.add(new LineSegment3D(vertices.get(0),vertices.get(4)));
        edges.add(new LineSegment3D(vertices.get(1),vertices.get(5)));
        edges.add(new LineSegment3D(vertices.get(2),vertices.get(6)));
        edges.add(new LineSegment3D(vertices.get(3),vertices.get(7)));
        
        // From -y to +y
        edges.add(new LineSegment3D(vertices.get(0),vertices.get(1)));
        edges.add(new LineSegment3D(vertices.get(2),vertices.get(3)));
        edges.add(new LineSegment3D(vertices.get(4),vertices.get(5)));
        edges.add(new LineSegment3D(vertices.get(6),vertices.get(7)));
        
        // From -x to +x
        edges.add(new LineSegment3D(vertices.get(0),vertices.get(2)));
        edges.add(new LineSegment3D(vertices.get(1),vertices.get(3)));
        edges.add(new LineSegment3D(vertices.get(4),vertices.get(6)));
        edges.add(new LineSegment3D(vertices.get(5),vertices.get(7)));
        
        return edges;
    }
    
    public List<Point3D> getVertices()
    {
        List<Point3D> points = new ArrayList<Point3D>();
        
        points.add(new Point3D(-dx1,-dy1,-dz));
        points.add(new Point3D(+dx1,-dy1,-dz));
        points.add(new Point3D(-dx1,+dy1,-dz));
        points.add(new Point3D(+dx1,+dy1,-dz));
        
        points.add(new Point3D(-dx2,-dy2,+dz));
        points.add(new Point3D(+dx2,-dy2,+dz));
        points.add(new Point3D(-dx2,+dy2,+dz));
        points.add(new Point3D(+dx2,+dy2,+dz));
        
        return points;
    }
   
    public String toString()
    {
        return this.getClass().getSimpleName()+" "+name+" : x1= "+dx1+ " x2= "+dx2+" y1= "+dy1+" y2="+dy2+ " z= "+dz;
    }
}