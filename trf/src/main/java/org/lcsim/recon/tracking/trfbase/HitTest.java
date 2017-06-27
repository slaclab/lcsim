package org.lcsim.recon.tracking.trfbase;

// Dummy concrete cluster and hit classes.
// These are only used for testing.
import org.lcsim.recon.tracking.trfbase.ETrack;
import org.lcsim.recon.tracking.trfbase.Hit;
import org.lcsim.recon.tracking.trfbase.HitDerivative;
import org.lcsim.recon.tracking.trfbase.HitError;
import org.lcsim.recon.tracking.trfbase.HitVector;
import org.lcsim.recon.tracking.trfutil.Assert;

// Hit.
public class HitTest extends Hit
{
    
    private static double _pdata[]= { 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0,
            10.0, 11.0, 12.0, 13.0, 14.0, 15.0 };
            private static final int SIZE=2;
            private int _ival;
            
            public String toString()
            {
                return "Dummy hit prediction " + _ival + "\n"
                        + "Cluster address: " + _pclus + "\n"
                        + "Cluster: " + _pclus + "\n";
            }
            
            protected boolean equal(Hit hp)
            {
                Assert.assertTrue( hp.type().equals(type()) );
                return _ival == (( HitTest) hp)._ival;
            }
            
            // static methods
            // Return the type name.
            public static String typeName()
            { return "HitTest";
            }
            // Return the type.
            public static String staticType()
            { return typeName();
            }
            
            public HitTest(int ival)
            {
                _ival = ival;
            }
            
            HitTest( HitTest ht)
            {
                _ival = ht._ival;
            }
            public String type()
            { return staticType();
            }
            
            public int get_ival()
            { return _ival;
            }
            public int size()
            { return SIZE;
            };
            double[] tmp = new double[2];
            double[] tmp3 = new double[3];
            double[] tmp10 = new double[10];
            public HitVector measuredVector()
            {
                System.arraycopy(_pdata, 0, tmp, 0, 2);
                return new HitVector(2,tmp);
            }
            public HitError measuredError()
            {
                System.arraycopy(_pdata, 1, tmp3, 0, 3);
                return new HitError(2,tmp3);
            }
            public HitVector predictedVector()
            {
                System.arraycopy(_pdata, 2, tmp, 0, 2);
                return new HitVector(2,tmp);
            }
            public HitError predictedError()
            {
                System.arraycopy(_pdata, 3, tmp3, 0, 3);
                return new HitError(2,tmp3);
            }
            public HitDerivative dHitdTrack()
            {
                System.arraycopy(_pdata, 4, tmp10, 0, 10);
                return new HitDerivative(2,tmp10);
            }
            public HitVector differenceVector()
            { return predictedVector().minus(measuredVector());
            }
            public void update(ETrack tre)
            { _ival = 0;
            }
}



