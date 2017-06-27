package org.lcsim.detector.solids;

import hep.physics.vec.Hep3Vector;
import java.util.List;

/**
 *
 * @author tknelson
 */
public interface IPolyhedron
{
    public List<Polygon3D> getFaces();
    public List<LineSegment3D> getEdges();
    public List<Point3D> getVertices();
    public List<Polygon3D> getFacesNormalTo(Hep3Vector normal);
    public int[] getHepRepVertexOrdering();
}
