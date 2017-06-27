package org.lcsim.detector.solids;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Port of Geant4's
 * <a href="http://www.lcsim.org/software/geant4/doxygen/html/classG4Trap.html">G4Trap</a>.
 *
 * @author Jeremy McCormick
 * @author Tim Nelson (modified to use standard geometry objects and operations)
 * @version $Id: Trap.java,v 1.9 2010/04/14 18:24:54 jeremy Exp $
 */

public class Trap extends AbstractPolyhedron
{
    
    private final static int[] _HEPREP_VERTEX_ORDERING = {2,3,1,0,6,7,5,4};
    
    double dz;
    double TthetaCphi;
    double TthetaSphi;
    double dy1;
    double dx1;
    double dx2;
    double Talpha1;
    double dy2;
    double dx3;
    double dx4;
    double Talpha2;
    double theta;
    double phi;
    double alpha1;
    double alpha2;
    
    double cubicVolume;
    
    public Trap(
            String name,
            double dz,
            double theta,
            double phi,
            double dy1,
            double dx1,
            double dx2,
            double alp1,
            double dy2,
            double dx3,
            double dx4,
            double alp2)
    {
        super(name);
        
        if ( dz > 0 && dy1 > 0 && dx1 > 0 && dx2 > 0 && dy2 > 0 && dx3 > 0 && dx4 > 0 )
        {
            this.dz=dz;
            this.TthetaCphi=tan(theta)*cos(phi);
            this.TthetaSphi=tan(theta)*sin(phi);
            
            this.dy1=dy1;
            this.dx1=dx1;
            this.dx2=dx2;
            Talpha1=tan(alp1);
            
            this.dy2=dy2;
            this.dx3=dx3;
            this.dx4=dx4;
            Talpha2=tan(alp2);
            
            this.theta = theta;
            this.phi = phi;
            
            this.alpha1 = alp1;
            this.alpha2 = alp2;
        }
        else
        {
            throw new IllegalArgumentException("bad parameters");
        }
    }
    
    public double getZHalfLength()
    {
        return dz;
    }
    
    public double getYHalfLength1()
    {
        return dy1;
    }
    
    public double getXHalfLength1()
    {
        return dx1;
    }
    
    public double getXHalfLength2()
    {
        return dx2;
    }
    
    public double getTanAlpha1()
    {
        return Talpha1;
    }
    
    public double getYHalfLength2()
    {
        return dy2;
    }
    
    public double getXHalfLength3()
    {
        return dx3;
    }
    
    public double getXHalfLength4()
    {
        return dx4;
    }
    
    public double getTanAlpha2()
    {
        return Talpha2;
    }
    
    public double getTheta()
    {
        return theta;
    }
    
    public double getPhi()
    {
        return phi;
    }
    
    public double getAlpha1()
    {
        return alpha1;
    }
    
    public double getAlpha2()
    {
        return alpha1;
    }
    
    public double getCubicVolume()
    {
        if(cubicVolume != 0.)
        {;}
        else
        { cubicVolume = dz*( (dx1+dx2+dx3+dx4)*(dy1+dy2)
          + (dx4+dx3-dx2-dx1)*(dy2-dy1)/3 ); }
        return cubicVolume;
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
        points.add(new Point3D(-dz*TthetaCphi-dy1*Talpha1-dx1,-dz*TthetaSphi-dy1,-dz));
        points.add(new Point3D(-dz*TthetaCphi-dy1*Talpha1+dx1,-dz*TthetaSphi-dy1,-dz));
        points.add(new Point3D(-dz*TthetaCphi+dy1*Talpha1-dx2,-dz*TthetaSphi+dy1,-dz));
        points.add(new Point3D(-dz*TthetaCphi+dy1*Talpha1+dx2,-dz*TthetaSphi+dy1,-dz));
        points.add(new Point3D(+dz*TthetaCphi-dy2*Talpha2-dx3,+dz*TthetaSphi-dy2,+dz));
        points.add(new Point3D(+dz*TthetaCphi-dy2*Talpha2+dx3,+dz*TthetaSphi-dy2,+dz));
        points.add(new Point3D(+dz*TthetaCphi+dy2*Talpha2-dx4,+dz*TthetaSphi+dy2,+dz));
        points.add(new Point3D(+dz*TthetaCphi+dy2*Talpha2+dx4,+dz*TthetaSphi+dy2,+dz));
        return points;
    }

   public String toString()
    {
        return this.getClass().getSimpleName()+" "+name;
    } 
}