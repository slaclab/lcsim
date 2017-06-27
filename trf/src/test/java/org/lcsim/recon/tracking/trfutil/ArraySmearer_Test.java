package org.lcsim.recon.tracking.trfutil;
import junit.framework.TestCase;
public class ArraySmearer_Test extends TestCase
{
    private boolean debug;
    public void testArraySmearer()
    {
        double[] _pulls = new double[3];
        double[][] _pulsum = new double[2][3];
        double[] _pullerr = new double[3];
        double[][] cov1 =  new double[1][1];
        // Single variable, should be gaussian smeared...
        cov1[0][0] = 3.;
        ArraySmearer as = new ArraySmearer( cov1 );
        if(debug) System.out.println(as);
        double[] vec1 = { 1.0 };
        double[] tmp = new double[1];
        int nsamples = 1000;
        for (int k = 0 ; k<nsamples ; ++k )
        {
            System.arraycopy(vec1, 0, tmp, 0, 1);
            as.smear(tmp);
            for (int i = 0; i<tmp.length ; ++i )
            {
                _pulls[i] = vec1[i] - tmp[i];
                _pulsum[0][i] += _pulls[i];
                _pulsum[1][i] += _pulls[i]*_pulls[i];
            }
        }
        double fac=1./Math.sqrt((double)(nsamples));
        for (int i=0; i<vec1.length; ++i)
        {
            _pulsum[0][i]=_pulsum[0][i]/(double)nsamples;
            _pulsum[1][i]=Math.sqrt(_pulsum[1][i]/((double)nsamples)-_pulsum[0][i]*_pulsum[0][i]);
            _pullerr[i]=fac*_pulsum[1][i];
            if(debug) System.out.println("Mean: "+_pulsum[0][i]+" +/- "+_pullerr[i]+" sigma= "+_pulsum[1][i]/Math.sqrt(cov1[i][i]));
        }
        
        
        // 2x2
        double[][] cov2 =  new double[2][2];
        // covariance matrix should be square, symmetric
        // and have positive-definite determinant
        cov2[0][0] = 4.;
        cov2[1][1] = 16.;
        cov2[0][1] = 1.;
        cov2[1][0] = 1.;
        ArraySmearer as2 = new ArraySmearer( cov2 );
        if(debug) System.out.println(as2);
        double[] vec2 =
        { 1.0, 1.0 };
        double[] tmp2 = new double[2];
        for (int i = 0; i< tmp2.length ; ++i )
        {
            _pulls[i] = 0.;
            _pulsum[0][i] = 0.;
            _pulsum[1][i] = 0.;
        }
        int nsamples2 = 1000;
        for (int k = 0 ; k<nsamples2 ; ++k )
        {
            System.arraycopy(vec2, 0, tmp2, 0, 2);
            as2.smear(tmp2);
            
            for (int i = 0; i<tmp2.length ; ++i )
            {
                _pulls[i] = vec2[i] - tmp2[i];
                _pulsum[0][i] += _pulls[i];
                _pulsum[1][i] += _pulls[i]*_pulls[i];
            }
        }
        double fac2=1./Math.sqrt((double)(nsamples2));
        for (int i=0; i<vec2.length; ++i)
        {
            if(debug) System.out.println("_pulsum[0]["+i+"]= "+_pulsum[0][i]);
            _pulsum[0][i]=_pulsum[0][i]/(double)nsamples2;
            _pulsum[1][i]=Math.sqrt(_pulsum[1][i]/((double)nsamples2)-_pulsum[0][i]*_pulsum[0][i]);
            _pullerr[i]=fac2*_pulsum[1][i];
            
            if(debug) System.out.println("Mean: "+_pulsum[0][i]+" +/- "+_pullerr[i]+" sigma= "+_pulsum[1][i]/Math.sqrt(cov2[i][i]));
        }
        
        //TODO introduce real tests with Assertions
        
    }
}

