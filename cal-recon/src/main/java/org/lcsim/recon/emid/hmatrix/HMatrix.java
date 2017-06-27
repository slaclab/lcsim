package org.lcsim.recon.emid.hmatrix;
import java.io.*;
/**
 *This class allows one to calculate how well a measurement agrees with expectations.
 *The vector of average values and the associated covariance matrix is used to construct
 *the HMatrix. The degree of agreement is calculated as a chi-squared of a measurement
 *vector against the expectations.
 *
 *
 * The covariance matrix is defined as:
 * <br clear="all" /><table border="0" width="100%"><tr><td>
 * <table align="center"><tr><td nowrap="nowrap" align="center">
 * <i>M</i><sub><i>ij</i></sub> = </td><td nowrap="nowrap" align="center">
 * &nbsp;1
 * <div class="hrcomp"><hr noshade="noshade" size="1"/></div><i>N</i><br /></td><td nowrap="nowrap" align="center">
 * </td><td nowrap="nowrap" align="center">
 * <font size="-1"><i>N</i></font><!--sup
 * --><br /><font size="+3"><font face="symbol"><br />
 * </font></font><font size="-1"><i>n</i> = 1</font>&nbsp;<br /></td><td nowrap="nowrap" align="center">
 * (<i>E</i><sub><i>i</i></sub><sup>(<i>n</i>)</sup> <font face="symbol">-</font
 * ></td><td nowrap="nowrap" align="center">
 *
 * <div class="hrcomp"><hr noshade="noshade" size="1"/></div>
 * <div class="norm"><i>E</i><br /></div>
 * <div class="comb">&nbsp;</div>
 * </td><td nowrap="nowrap" align="center">
 * <font size="-1"></font><!--sup
 * --><br /><font size="-1"><i>i</i></font>&nbsp;<br /></td><td nowrap="nowrap" align="center">
 * )(<i>E</i><sub><i>j</i></sub><sup>(<i>n</i>)</sup> <font face="symbol">-</font
 * ></td><td nowrap="nowrap" align="center">
 *
 * <div class="hrcomp"><hr noshade="noshade" size="1"/></div>
 * <div class="norm"><i>E</i><br /></div>
 * <div class="comb">&nbsp;</div>
 * </td><td nowrap="nowrap" align="center">
 * <font size="-1"></font><!--sup
 * --><br /><font size="-1"><i>j</i></font>&nbsp;<br /></td><td nowrap="nowrap" align="center">
 * )</td></tr></table>
 * </td></tr></table>
 *
 * The H(essian)Matrix is the inverse of the covariance matrix.
 * The chi-squared is then calculated <it>via</it>
 * <br clear="all" /><table border="0" width="100%"><tr><td>
 * <table align="center"><tr><td nowrap="nowrap" align="center">
 * <font face="symbol">z</font
 * ><sub><i>m</i></sub>  <font face="symbol"></font
 * > </td><td nowrap="nowrap" align="center">
 * <font size="-1"><i>N</i></font><!--sup
 * --><br /><font size="+3"><font face="symbol"><br />
 * </font></font><font size="-1"><i>i</i>,<i>j</i> = 1</font>&nbsp;<br /></td><td nowrap="nowrap" align="center">
 * (<i>E</i><sub><i>i</i></sub><sup>(<i>m</i>)</sup> <font face="symbol">-</font
 * ></td><td nowrap="nowrap" align="center">
 *
 * <div class="hrcomp"><hr noshade="noshade" size="1"/></div>
 * <div class="norm"><i>E</i><br /></div>
 * <div class="comb">&nbsp;</div>
 * </td><td nowrap="nowrap" align="center">
 * <font size="-1"></font><!--sup
 * --><br /><font size="-1"><i>i</i></font>&nbsp;<br /></td><td nowrap="nowrap" align="center">
 * )<i>H</i><sub><i>ij</i></sub> (<i>E</i><sub><i>j</i></sub><sup>(<i>m</i>)</sup> <font face="symbol">-</font
 * ></td><td nowrap="nowrap" align="center">
 *
 * <div class="hrcomp"><hr noshade="noshade" size="1"/></div>
 * <div class="norm"><i>E</i><br /></div>
 * <div class="comb">&nbsp;</div>
 * </td><td nowrap="nowrap" align="center">
 * <font size="-1"></font><!--sup
 * --><br /><font size="-1"><i>j</i></font>&nbsp;<br /></td><td nowrap="nowrap" align="center">
 * )</td></tr></table>
 * </td></tr></table>
 *
 *Methods to persist the HMatrix are provided in both
 *plain ASCII format and Java serialized format.
 *
 *@author Norman A. Graf
 *@version $Id: HMatrix.java,v 1.7 2012/08/08 00:06:33 jeremy Exp $
 */
public class HMatrix implements Serializable
{
    private double[][] _invcov; // the inverse covariance matrix
    private double[] _vec; // the vector of measurements
    private double[] _cov; // the variance on the measurements
    private int _dim; // the dimensionality
    private int _key; // the key for the HMatrix
    private double[] _tmp; // temporary array
    private double[] _tmp2; // temporary array
    
    
    public HMatrix()
    {}
    /**
     * Constructor
     *
     * @param   dim  the dimensionality of the matrix
     * @param   key the key for indexing this HMatrix
     * @param   vec  The array of average values for the measurement space.
     * @param   cov  The inverse of the covariance matrix for the averages.
     */
    public HMatrix(int dim, int key, double [] vec, double[] cov, double[][] invcov)
    {
        _dim = dim;
        _key = key;
        _vec = new double[_dim];
        _cov = new double[_dim];
        System.arraycopy(vec, 0, _vec, 0, _dim);
        System.arraycopy(cov, 0, _cov, 0, _dim);
        _invcov = new double[_dim][_dim];
        for(int i =0; i<_dim; ++i)
        {
            System.arraycopy(invcov[i], 0, _invcov[i], 0, _dim);
        }
        _tmp = new double[_dim];
        _tmp2 = new double[_dim];
        
    }
    
    
    public HMatrix(String resourceFileName)
    {
        InputStream in = this.getClass().getResourceAsStream(resourceFileName);
        if(in!=null)
        {
            try
            {
                BufferedReader bufferedreader
                        = new BufferedReader(new InputStreamReader(in));
                create(bufferedreader);
                bufferedreader.close();
                
            }
            catch(IOException _ex)
            {
                System.err.println("HMatrixBuilder::read -> Error reading HMatrix from input reader.");
                System.exit(0);
            }
        }
    }
    
    
    /**
     * Calculates the chi-squared for the measurement compared to
     * the expected values and covariances represented by the HMatrix.
     *
     * @param   dat  The array of measured values
     * @return  The value of chi-squared.
     */
    public double chisquared(double[] dat)
    {
        // vector of measured-predicted values
        for(int i=0; i<_dim; ++i)
        {
            _tmp[i] = dat[i]-_vec[i];
        }
        
        double chisqr = 0.;
        // covariance matrix times difference vector
        for(int i=0; i<_dim; ++i)
        {
            _tmp2[i] = 0.;
            for(int j=0; j<_dim; ++j)
            {
                _tmp2[i]+=_invcov[j][i]*_tmp[j];
            }
            chisqr += _tmp[i]*_tmp2[i];
        }
        return chisqr;
    }
    
    /**
     * Calculates the diagonal chi-squared for the measurement compared to
     * the expected values and covariances represented by the HMatrix,
     * neglecting correlations.
     *
     * @param   dat  The array of measured values
     * @return  The value of the diagonal chi-squared.
     */
    public double chisquaredDiagonal(double[] dat)
    {
        double chisqr = 0.;
        // diagonal terms of covariance matrix times difference vector
        for(int i=0; i<_dim; ++i)
        {
            //measured-predicted values
            _tmp[i] = dat[i]-_vec[i];
            //squared, divided by the variance of the prediction
            chisqr += _tmp[i]*_tmp[i]/_cov[i];
        }
        return chisqr;
    }
    
    /**
     *Output stream
     *
     * @return   A String representation of the object.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer("HMatrix: dimension "+_dim+" key "+_key+ "\n");
        sb.append("vector: \n");
        for(int i = 0; i<_dim; ++i)
        {
            sb.append(_vec[i]+" ");
        }
        sb.append("\n\nInverse covariance matrix: \n");
        for(int i = 0; i<_dim; ++i)
        {
            for(int j = 0; j<_dim; ++j)
            {
                sb.append(_invcov[i][j]+" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    
//    /**
//     * Writes the HMatrix to a Serialized file
//     * @return
//     */
//
//    public void writeSerialized(String filename)
//    {
//
//        try
//        {
//            FileOutputStream fos =  new FileOutputStream(filename);
//            ObjectOutputStream objOs = new ObjectOutputStream(fos);
//            objOs.writeObject(this);
//            objOs.flush();
//            fos.close();
//        }  catch (NotSerializableException se)
//        {
//            System.err.println(se);
//        }  catch (FileNotFoundException fe)
//        {
//            System.err.println(fe);
//        }  catch (IOException se)
//        {
//            System.err.println(se);
//        }
//
//    }
    
    
//    /**
//     * Reads the HMatrix from a Serialized file
//     * @return
//     */
//
//    public static HMatrix readSerialized(String filename)
//    {
//        HMatrix hm = null;
//
//        try
//        {
//            FileInputStream fis = new FileInputStream(filename);
//            ObjectInputStream objIs = new ObjectInputStream(fis);
//            hm = (HMatrix)objIs.readObject();
//            fis.close();
//        }   catch (NotSerializableException se)
//        {
//            System.err.println(se);
//        }  catch (FileNotFoundException fe)
//        {
//            System.err.println(fe);
//        }  catch (IOException se)
//        {
//            System.err.println(se);
//        }  catch (ClassNotFoundException ce)
//        {
//            System.err.println(ce);
//        }
//        return hm;
//    }
    
    
    /**
     * Return the vector of averages
     * @return the measurement vector averages
     */
    public double[] averageVector()
    {
        double[] a = new double[_dim];
        System.arraycopy(_vec, 0, a, 0, _dim);
        return a;
    }
    
    /**
     * Return the inverse covariance matrix packed in lower-diagonal form
     * @return the inverse covariance matrix packed in lower-diagonal form
     */
    public double[] packedInverseCovarianceMatrix()
    {
        int dim = (_dim*(_dim+1))/2;
        double[] a = new double[dim];
        int counter = 0;
        for(int i=0; i<_dim; ++i)
        {
            for(int j=0; j<i+1; ++j)
            {
                a[counter++] = _invcov[i][j];
            }
        }
        return a;
    }
    /**
     *  Writes out the HMatrix to an ASCII file
     */
    public void write(String filename, String comment)
    {
        System.out.println("Writing matrix to '" + filename + "'.");
        try
        {
            PrintWriter printwriter = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
            // Print the comment
            String s2 = "# " + comment;
            printwriter.println(s2);
            // Now the dimension and index of the matrix
            String s3 = _dim+" "+_key;
            printwriter.println(s3);
            String s4 = "";
            //Now the vector of average values
            {
                String s5 = "";
                for(int i = 0; i < _dim; i++) s5 = s5 + _vec[i] + " ";
                printwriter.println(s5.substring(0, s5.length() - 1));
            }
            //Now the vector of variance of average values
            {
                String s5 = "";
                for(int i = 0; i < _dim; i++) s5 = s5 + _cov[i] + " ";
                printwriter.println(s5.substring(0, s5.length() - 1));
            }
            
            //Now the covariance matrix
            for(int i = 0; i < _dim; i++)
            {
                String s6 = "";
                for(int j = 0; j < _dim; j++)
                    s6 = s6 + _invcov[i][j] + " ";
                
                printwriter.println(s6.substring(0, s6.length() - 1));
            }
            
            printwriter.close();
        }
        catch(IOException _ex)
        {
            System.err.println("Matrix::write -> Error writing to '" + filename + "'.");
            System.exit(0);
        }
        System.out.println("Matrix written.");
    }
    
    /**
     * Reads the HMatrix from an ASCII file
     */
    public static HMatrix read(String filename)
    {
        /*
        boolean flag = false;
        boolean readVec = false;
        int i = 0;
        int ii = 0;
        int j = 0;
        int dim=0;
        int key = 0;
        double[] vec=null;
        double[][] cov=null;
         */
        try
        {
            BufferedReader bufferedreader = new BufferedReader(new FileReader(filename));
            HMatrix mat =  create(bufferedreader);
            /*
            for(String s1 = new String(); (s1 = bufferedreader.readLine()) != null;)
                if(s1.startsWith("#"))
                    System.out.println(s1);
                else
                    if(!flag)
                    {
                        dim = Integer.parseInt(s1.substring(0, s1.indexOf(" ")));
                        key = Integer.parseInt(s1.substring(s1.indexOf(" ") + 1, s1.length()));
                        vec = new double[dim];
                        cov = new double[dim][dim];
                        flag = true;
                    }
                    else
                    {
                        if (!readVec)
                        {
             
                            // Read the vector of averages
                            while(s1.length() > 0 && s1.indexOf(" ") != -1)
                            {
                                double d = Double.valueOf(s1.substring(0, s1.indexOf(" "))).doubleValue();
                                s1 = s1.substring(s1.indexOf(" ") + 1, s1.length());
                                vec[ii] = d;
                                ii++;
                            }
                            double d1 = Double.valueOf(s1).doubleValue();
                            vec[ii] = d1;
                            readVec = true;
                        }
                        else
                        {
                            // Read the covariance matrix
                            while(s1.length() > 0 && s1.indexOf(" ") != -1)
                            {
                                double d = Double.valueOf(s1.substring(0, s1.indexOf(" "))).doubleValue();
                                s1 = s1.substring(s1.indexOf(" ") + 1, s1.length());
                                cov[i][j] = d;
                                j++;
                            }
                            double d1 = Double.valueOf(s1).doubleValue();
                            cov[i][j] = d1;
                            i++;
                            j = 0;
                        }
                    }
             */
            bufferedreader.close();
            return mat;
        }
        catch(IOException _ex)
        {
            System.err.println("HMatrixBuilder::read -> Error reading '" + filename + "#.");
            System.exit(0);
        }
        //        return new HMatrix(dim, key, vec, cov);
        return null;
    }
    
    /**
     * Reads the HMatrix from a Reader
     */
    public static HMatrix read(Reader reader)
    {
        /*
        boolean flag = false;
        boolean readVec = false;
        int i = 0;
        int ii = 0;
        int j = 0;
        int dim=0;
        int key = 0;
        double[] vec=null;
        double[][] cov=null;
         */
        try
        {
            BufferedReader bufferedreader = new BufferedReader(reader);
            HMatrix mat =  create(bufferedreader);
            /*
            for(String s1 = new String(); (s1 = bufferedreader.readLine()) != null;)
                if(s1.startsWith("#"))
                    System.out.println(s1);
                else
                    if(!flag)
                    {
                        dim = Integer.parseInt(s1.substring(0, s1.indexOf(" ")));
                        key = Integer.parseInt(s1.substring(s1.indexOf(" ") + 1, s1.length()));
                        vec = new double[dim];
                        cov = new double[dim][dim];
                        flag = true;
                    }
                    else
                    {
                        if (!readVec)
                        {
             
                            // Read the vector of averages
                            while(s1.length() > 0 && s1.indexOf(" ") != -1)
                            {
                                double d = Double.valueOf(s1.substring(0, s1.indexOf(" "))).doubleValue();
                                s1 = s1.substring(s1.indexOf(" ") + 1, s1.length());
                                vec[ii] = d;
                                ii++;
                            }
                            double d1 = Double.valueOf(s1).doubleValue();
                            vec[ii] = d1;
                            readVec = true;
                        }
                        else
                        {
                            // Read the covariance matrix
                            while(s1.length() > 0 && s1.indexOf(" ") != -1)
                            {
                                double d = Double.valueOf(s1.substring(0, s1.indexOf(" "))).doubleValue();
                                s1 = s1.substring(s1.indexOf(" ") + 1, s1.length());
                                cov[i][j] = d;
                                j++;
                            }
                            double d1 = Double.valueOf(s1).doubleValue();
                            cov[i][j] = d1;
                            i++;
                            j = 0;
                        }
                    }
             */
            bufferedreader.close();
            return mat;
        }
        catch(IOException _ex)
        {
            System.err.println("HMatrixBuilder::read -> Error reading HMatrix from input reader.");
            System.exit(0);
        }
        //        return new HMatrix(dim, key, vec, cov);
        return null;
    }
    
    public static HMatrix create(InputStream in)
    {
        
        try
        {
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(in));
            HMatrix hm = create(bufferedreader);
            bufferedreader.close();
            return hm;
        }
        catch(IOException _ex)
        {
            System.err.println("HMatrixBuilder::read -> Error reading HMatrix from input reader.");
            System.exit(0);
        }
        return null;
    }
    
    
    
    private static HMatrix create(BufferedReader bufferedreader) throws IOException
    {
        boolean flag = false;
        boolean readVec = false;
        boolean readCovDiag = false;
        int i = 0;
        int ii = 0;
        int j = 0;
        int dim=0;
        int key = 0;
        double[] vec=null;
        double[] covDiag=null;
        double[][] cov=null;
        for(String s1 = new String(); (s1 = bufferedreader.readLine()) != null;)
            if(s1.startsWith("#"))
                System.out.println(s1);
            else
                if(!flag)
                {
            dim = Integer.parseInt(s1.substring(0, s1.indexOf(" ")));
            key = Integer.parseInt(s1.substring(s1.indexOf(" ") + 1, s1.length()));
            vec = new double[dim];
            covDiag = new double[dim];
            cov = new double[dim][dim];
            flag = true;
                }
                else
                {
            if (!readVec)
            {
//                        System.out.println("read vec");
//                        System.out.println(s1);
                // Read the vector of averages
                while(s1.length() > 0 && s1.indexOf(" ") != -1)
                {
                    double d = Double.valueOf(s1.substring(0, s1.indexOf(" "))).doubleValue();
                    s1 = s1.substring(s1.indexOf(" ") + 1, s1.length());
                    vec[ii] = d;
                    ii++;
                }
                double d1 = Double.valueOf(s1).doubleValue();
                vec[ii] = d1;
                readVec = true;
                ii=0;
            }
            else if(!readCovDiag)
            {
//                        System.out.println("read covDiag");
//                        System.out.println(s1);
                // Read the vector of variance of averages
                while(s1.length() > 0 && s1.indexOf(" ") != -1)
                {
                    double d = Double.valueOf(s1.substring(0, s1.indexOf(" "))).doubleValue();
                    s1 = s1.substring(s1.indexOf(" ") + 1, s1.length());
                    covDiag[ii] = d;
                    ii++;
                }
                double d1 = Double.valueOf(s1).doubleValue();
                covDiag[ii] = d1;
                readCovDiag = true;
            }
            else
            {
//                        System.out.println("read cov");
//                        System.out.println(s1);
                // Read the covariance matrix
                while(s1.length() > 0 && s1.indexOf(" ") != -1)
                {
                    double d = Double.valueOf(s1.substring(0, s1.indexOf(" "))).doubleValue();
                    s1 = s1.substring(s1.indexOf(" ") + 1, s1.length());
                    cov[i][j] = d;
                    j++;
                }
                double d1 = Double.valueOf(s1).doubleValue();
                cov[i][j] = d1;
                i++;
                j = 0;
            }
                }
        return new HMatrix(dim, key, vec, covDiag, cov);
    }
    
}
