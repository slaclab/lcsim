package org.lcsim.util.heprep;

import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstance;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepPoint;
import hep.graphics.heprep.HepRepType;
import hep.graphics.heprep.HepRepTypeTree;
import hep.physics.matrix.SymmetricMatrix;
import hep.physics.vec.Hep3Vector;

import java.awt.Color;
import java.util.List;

import org.lcsim.event.EventHeader;
import org.lcsim.event.Vertex;
import org.lcsim.event.EventHeader.LCMetaData;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * Representation of the Candidate vertices before fitting
 * 
 * @author jstrube
 * @version $Id: VertexConverter.java,v 1.5 2007/10/03 16:47:22 ngraf Exp $
 * 
 */
public class VertexConverter implements HepRepCollectionConverter {
    private Color[] colors;

    private static final double[] zero = { 0,0,0 };
    public VertexConverter() {
        ColorMap cm = new RainbowColorMap();
        colors = new Color[11];
        for (int i=0; i<colors.length; i++) colors[i] = cm.getColor(((double) i)/colors.length,1);
    }

    public boolean canHandle(Class k) {
        return Vertex.class.isAssignableFrom(k);
    }

    public void convert(EventHeader event, List collection, HepRepFactory factory
            , HepRepTypeTree typeTree, HepRepInstanceTree instanceTree) {
        LCMetaData meta = event.getMetaData(collection);
        String name = meta.getName();        
        HepRepType typeX = factory.createHepRepType(typeTree, name);
        typeX.addAttValue("layer", LCSimHepRepConverter.PARTICLES_LAYER);
        typeX.addAttValue("drawAs", "Ellipsoid");
        typeX.addAttValue("Radius", 1);
        typeX.addAttValue("Radius2", 2);
        typeX.addAttValue("Radius3", 3);
        typeX.addAttValue("color", Color.RED);
        typeX.addAttValue("fill", true);
        typeX.addAttValue("fillColor", Color.RED);
        typeX.addAttValue("MarkName", "Box");
        typeX.addAttDef("nTracks", "number of Tracks", "physics", "");
        
        // TODO allow the error ellipse to be defined in terms of a confidence level
        // for this, will need to get the number of degrees of freedom from the vertex
        // fit. See Numerical Recipes for the procedure to calculate the following numbers.
        //
        //  confidence level                    number of degrees of freedom
        //                          1        2       3      4       5      6
        //     68.3 %               1.00     2.30    3.53    4.72    5.89   7.04
        //     90   %               2.71     4.61    6.25    7.78    9.24  10.6
        //     95.4 %               4.00     6.17    8.02    9.70   11.3   12.8
        //     99   %               6.63     9.21   11.3    13.3    15.1   16.8
        //     99.99%              15.1     18.4    21.1    23.5    25.7   27.8
        //
        double nSigma = 30.; 
        typeX.addAttDef("sigma", "error scale factor", "physics", "The error in each dimension is multiplied by this factor");
        typeX.addAttValue("sigma", nSigma);
        
//        HepRepType typeY = factory.createHepRepType(typeTree, name);
//        typeY.addAttValue("layer", LCSimHepRepConverter.PARTICLES_LAYER);
//        typeY.addAttValue("drawAs","Line");
//        typeY.addAttDef("pT","Transverse momentum", "physics", "");
//        typeY.addAttDef("dedX","de/Dx", "physics", "");
        int iColor = 0;
        
        HepRepType typeY = factory.createHepRepType(typeTree, name+"Tracks");
        typeY.addAttValue("layer", LCSimHepRepConverter.PARTICLES_LAYER);
        typeY.addAttValue("drawAs", "Line");
        
        
        for (Vertex vtx : (List<Vertex>) collection) {
            Color vertexColor = colors[iColor];
            iColor = (iColor+2) % colors.length;
            Hep3Vector pos = vtx.getPosition();
            HepRepInstance instanceV = factory.createHepRepInstance(instanceTree, typeX);
            instanceV.addAttValue("color", vertexColor);
            instanceV.addAttValue("fillColor", vertexColor);
            
            SymmetricMatrix covMatrix = vtx.getCovMatrix();
            
            EigenvalueDecomposition vtxEigen = new EigenvalueDecomposition(Jama.util.Maths.toJamaMatrix(covMatrix));
            Matrix eigenVals = vtxEigen.getD();
            instanceV.addAttValue("Radius", nSigma*Math.sqrt(eigenVals.get(0, 0)));
            instanceV.addAttValue("Radius2", nSigma*Math.sqrt(eigenVals.get(1, 1)));
            instanceV.addAttValue("Radius3", nSigma*Math.sqrt(eigenVals.get(2, 2)));
            
            Matrix eigenVecs = vtxEigen.getV();
            assert eigenVecs.getRowDimension() == 3;
            assert eigenVecs.getColumnDimension() == 3;
            double theta = -Math.asin(eigenVecs.get(1, 2));
            double phi = Math.atan2(eigenVecs.get(0, 2),eigenVecs.get(2, 2));
            double omega = Math.atan2(eigenVecs.get(0, 1), eigenVecs.get(1,1));

            instanceV.addAttValue("Phi", phi);
            instanceV.addAttValue("Theta", theta);
            instanceV.addAttValue("Omega", omega);
            instanceV.addAttValue("x", pos.x());
            instanceV.addAttValue("y", pos.y());
            instanceV.addAttValue("z", pos.z());
            HepRepPoint pp = factory.createHepRepPoint(instanceV, pos.x(), pos.y(), pos.z());
            
        }
    }
}
