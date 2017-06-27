package org.lcsim.recon.tracking.ftf;
public class FtfUtil
{
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //   Invert matrix h of dimension n
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //
    //
    //   Originally written in FORTRAN by Jawluen Tang, Physics department , UT-Austin
    //              modified and translated to C by Pablo Yepes, Rice U.
    //
    //     The following routine is to invert a square symmetric matrix
    //     (in our case,it is a 3x3 matrix,so NOD=3)and calculate its
    //     determinant,substituting the inverse matrix into the same array
    //     of the original matrix H.
    //     See Philip R. Bevington,"Data reduction and error analysis for
    //     the physical science",p302
    //
    public static void ftfInvertMatrix(int n, double[] h)
    {
        double detm, dmax_, temp;
        
        int i, j, k, l;
        
        int[] ik = new int[3];
        
        int[] jk = new int[3];
        
        detm = 1.;
        
        for (k = 0 ; k < n ; ++k)
        {
            dmax_ = 0.;
            j = -1 ;
            while(j < k)
            {
                i = -1 ;
                while(i < k)
                {
                    
                    for (i = k; i < n; ++i)
                    {
                        
                        for (j = k; j < n; ++j)
                        {
                            if (Math.abs(dmax_) <= Math.abs(h[i+j*3]))
                            {
                                dmax_ = h[i + j * 3];
                                ik[k] = i;
                                jk[k] = j;
                            }
                        }
                    }
                    if (dmax_ == 0.)
                    {
                        System.out.println( "Determinant is ZERO!" );
                        return ;
                    }
                    i = ik[k];
                }
                if (i > k)
                {
                    
                    for (j = 0 ; j < n; ++j)
                    {
                        temp = h[k + j * 3];
                        h[k + j * 3] = h[i + j * 3];
                        h[i + j * 3] = -temp;
                    }
                }
                j = jk[k];
            }
            if (j != k)
            {
                
                for (i = 0 ; i < n; ++i)
                {
                    temp = h[i + k * 3];
                    h[i + k * 3] = h[i + j * 3];
                    h[i + j * 3] = -temp;
                }
            }
            
            for (i = 0 ; i < n; ++i)
            {
                if (i != k)
                {
                    h[i + k * 3] = -(double)h[i + k * 3] / dmax_;
                }
            }
            
            for (i = 0; i < n; ++i)
            {
                
                for (j = 0; j < n; ++j)
                {
                    if (i != k && j != k)
                    {
                        h[i + j * 3] += h[i + k * 3] * h[k + j * 3];
                    }
                }
            }
            for (j = 0; j < n; ++j)
            {
                if (j != k)
                {
                    h[k + j * 3] /= dmax_;
                }
            }
            h[k + k * 3] = 1.F / dmax_;
            detm *= dmax_;
        }
        for (l = 0; l < n; ++l)
        {
            k = n - l -1 ;
            j = ik[k];
            if (j > k)
            {
                for (i = 0; i < n; ++i)
                {
                    temp = h[i + k * 3];
                    h[i + k * 3] = -h[i + j * 3];
                    h[i + j * 3] = temp;
                }
            }
            i = jk[k];
            if (i > k)
            {
                for (j = 0; j < n; ++j)
                {
                    temp = h[k + j * 3];
                    h[k + j * 3] = -h[i + j * 3];
                    h[i + j * 3] = temp;
                }
            }
        }
        
    }
    
    
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //    Function to give the diagonal elements (h11,h22,h33)
    //    of the inverse symmetric 3x3 matrix of h
    //    Calculation by Geary Eppley (Rice University)
    //    coded by Pablo Yepes        (Rice University)
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    public static void ftfMatrixDiagonal( double[] h, double[] hxx )
    {
        double f1, f2, f3 ;
        
        f1 = h[5]*h[6]-h[8]*h[1] ;
        f2 = h[4]*h[8]-h[5]*h[5] ;
        f3 = h[8]*h[0]-h[2]*h[2] ;
        hxx[0] =  (h[8] / ( f3 - f1 * f1 / f2 )) ;
        
        f1 = h[2]*h[1]-h[0]*h[5] ;
        f2 = h[8]*h[0]-h[2]*h[2] ;
        f3 = h[0]*h[4]-h[1]*h[1] ;
        hxx[1] =  (h[0] / ( f3 - f1 * f1 / f2 )) ;
        
        f1 = h[1]*h[5]-h[4]*h[2] ;
        f2 = h[0]*h[4]-h[1]*h[1] ;
        f3 = h[4]*h[8]-h[7]*h[7] ;
        hxx[2] =  (h[4] / ( f3 - f1 * f1 / f2 )) ;
    }
    
    //
    // simple utility to print out 3x3 matrix
    //
    
    public static void ftfPrintMatrix( double[] h)
    {
        for(int i=0; i<3; ++i) System.out.print(h[i]+" ");
        System.out.println("\n");
        for(int i=3; i<6; ++i) System.out.print(h[i]+" ");
        System.out.println("\n");
        for(int i=6; i<9; ++i) System.out.print(h[i]+" ");
        System.out.println("\n");
        
    }
    
}
