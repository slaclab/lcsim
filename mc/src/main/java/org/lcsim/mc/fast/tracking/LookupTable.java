package org.lcsim.mc.fast.tracking;

import java.io.BufferedReader;
import java.io.IOException;

import java.util.Arrays;
import java.util.StringTokenizer;

public class LookupTable {
    // cosine theta
    private double[] m_key1;
    // momentum
    private double[] m_key2;
    private double[][] m_matrix;
    private int m_numBins1;
    private int m_numBins2;

    // vector units
    // 1 dr [ cm ] 10.
    // 2 dphi [ ] 1.
    // 3 domega [ cm-1 ] .1
    // 4 dz [ cm ] 10.
    // 5 dlambda [ ] 1.
    //
    double[] conversionFromCmToMm = { 10.0, 1.0, 0.1, 10.0, 1.0 };

    double[][] conversionFromCmToMmMatrix = { { 100.00, 10.00, 1.00, 100.00, 10.00 }, { 10.00, 1.00, 0.10, 10.00, 1.00 }, { 1.00, 0.10, 0.01, 1.00, 0.10 }, { 100.00, 10.00, 1.00, 100.00, 10.00 }, { 10.00, 1.00, 0.10, 10.00, 1.00 } };

    LookupTable(BufferedReader in, int iTerm, int jTerm) throws IOException {
        // read in the number of cosine theta points
        int m_numBins1 = Integer.parseInt(in.readLine());

        // read in the number of momentum points
        int m_numBins2 = Integer.parseInt(in.readLine());

        m_matrix = new double[m_numBins1][m_numBins2];
        m_key1 = new double[m_numBins1];
        m_key2 = new double[m_numBins2];

        for (int i = 0; i < m_numBins1; i++) // i is # of cosine theta bin
        {
            m_key1[i] = Double.valueOf(in.readLine()).doubleValue(); // cosine theta
            for (int j = 0; j < m_numBins2; j++) // j is # of momentum bin
            {
                StringTokenizer t = new StringTokenizer(in.readLine());
                m_key2[j] = Double.valueOf(t.nextToken()).doubleValue();
                m_matrix[i][j] = Double.valueOf(t.nextToken()).doubleValue() * conversionFromCmToMmMatrix[iTerm][jTerm]; // momentum
            }
        }
        if (!in.readLine().equals("end")) {
            throw new IOException("Missing end in lookup table");
        }
    }

    public double interpolateVal(double val1, double val2) {
        int index1 = binarySearch(m_key1, val1);
        // implement cut-off
        double t;
        if (index1 < 0) {
            t = m_key1[0];
            index1 = 0;
        } else if (index1 >= m_key1.length - 1) {
            t = m_key1[m_key1.length - 1];
            index1 = m_key1.length - 1;
        } else
            t = (val1 - m_key1[index1]) / (m_key1[index1 + 1] - m_key1[index1]);

        double u;
        int index2 = binarySearch(m_key2, val2);
        if (index2 < 0) {
            u = m_key2[0];
            index2 = 0;
        } else if (index2 >= m_key2.length - 1) {
            u = m_key2[m_key2.length - 1];
            index2 = m_key2.length - 1;
        } else
            u = (val2 - m_key2[index2]) / (m_key2[index2 + 1] - m_key2[index2]);

        double y1 = m_matrix[index1][index2];
        double y2 = m_matrix[index1 + 1][index2];
        double y3 = m_matrix[index1 + 1][index2 + 1];
        double y4 = m_matrix[index1][index2 + 1];

        return ((1 - t) * (1 - u) * y1) + (t * (1 - u) * y2) + (t * u * y3) + ((1 - t) * u * y4);
    }

    private int binarySearch(double[] key, double value)
    // {
    // int result = binarySearchX(key,value);
    // System.out.print("Looking for "+value+" in [");
    // for (int i=0; i<key.length; i++) System.out.print(key[i]+",");
    // System.out.print("] ");
    // System.out.println("result="+result);
    // return result;
    // }
    //
    // private int binarySearchX(double[] key, double value)
    {
        if (value < key[0]) {
            // throw new RuntimeException("Interpolation out of range: lower: "+value+" < "+key[0]);
            return 0;
        }

        int pos = Arrays.binarySearch(key, value);
        if (pos > 0) {
            return Math.min(pos, key.length - 2);
        } else {
            return Math.min(-pos - 2, key.length - 2);
        }

        // // Ok, this isn't really a binary search, probably doesn't matter
        // for (int i=1; i<key.length; i++) if (value<key[i]) return i-1;
        //
        //
        // throw new LCDException("Interpolation out of range: upper: "
        // +value+" >= "+key[key.length-1]);
    }
}
