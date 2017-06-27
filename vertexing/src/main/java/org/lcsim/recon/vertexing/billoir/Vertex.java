package org.lcsim.recon.vertexing.billoir;
/**
 * @version $Id: Vertex.java,v 1.2 2006/03/28 23:50:27 jstrube Exp $
 */
//Testing...
// A class encapsulating the behavior of a vertex constructed
// from tracks. It is written to be independent of any other
// packages and therefore makes extensive use of arrays and
// internal methods. It is not object-oriented in any way.
// But it should work.
//
public class Vertex {
    public int _ntrk; // the number of tracks used in this fit
    public double[] _xyzf; // the vertex position in Cartesian coordinates
    public double[] _vcov; // packed covariance matrix on _xyzf

    public double[][] _parf; // the fitted parameters at the vertex
    // 0: theta
    // 1: phi
    // 2: 1/R
    public double[][] _tcov; // covariance matrix on _parf

    public double _chi2; // chisquared of vertex fit;
    public double[] _chi2tr; // chisquared contribution of each track

    // default constructor
    public Vertex() {
        _ntrk = 0;
        _xyzf = new double[3];
        _parf = new double[3][0];
        _vcov = new double[6];
        _tcov = new double[6][0];
        _chi2 = 0.;
        _chi2tr = new double[0];
    }

    // constructor
    public Vertex(int ntrk, double[] xyz, double[][] parf, double[] vcov, double[][] tcov, double chi2, double[] chi2tr) {
        _ntrk = ntrk;
        _xyzf = xyz;
        _parf = parf;
        _vcov = vcov;
        _tcov = tcov;
        _chi2 = chi2;
        _chi2tr = chi2tr;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Vertex at : \nx= "
                + _xyzf[0]
                + " +/- "
                + Math.sqrt(_vcov[0])
                + "\ny= "
                + _xyzf[1]
                + " +/- "
                + Math.sqrt(_vcov[2])
                + "\nz= "
                + _xyzf[2]
                + " +/- "
                + Math.sqrt(_vcov[5])
                + "\nchi2 = "
                + _chi2);
        for (int i = 0; i < _ntrk; ++i) {
            sb.append("\n chi2[" + i + "]= " + _chi2tr[i]);
        }
        return sb.toString();

    }

}
