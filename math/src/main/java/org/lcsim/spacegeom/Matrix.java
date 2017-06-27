package org.lcsim.spacegeom;
import java.io.*;
/**
 *
 *
 *@version $Id: Matrix.java,v 1.1.1.1 2010/12/01 00:15:57 jeremy Exp $
 */
class Matrix
{
    
    protected int _row;
    protected int _col;
    protected double _a[][];
    protected Matrix _eigenvalues;
    protected Eigensystem _eigensystem;
    
    public Matrix()
    {
        _row = 0;
        _col = 0;
        createData();
    }
    
    public Matrix(int i, int j)
    {
        _row = i;
        _col = j;
        createData();
    }
    
    public Matrix(Matrix matrix)
    {
        _row = matrix._row;
        _col = matrix._col;
        _eigensystem = matrix._eigensystem;
        createData();
        for(int i = 0; i < _row; i++)
        {
            for(int j = 0; j < _col; j++)
                _a[i][j] = matrix._a[i][j];
            
        }
        
    }
    
    protected void QL(Matrix matrix, Matrix matrix1, Matrix matrix2, boolean flag)
    {
        int k1 = _row;
        for(int i = 1; i < k1; i++)
            matrix2.set(i - 1, 0, matrix2.at(i, 0));
        
        matrix2.set(k1 - 1, 0, 0.0D);
        for(int l = 0; l < k1; l++)
        {
            int j1 = 0;
            int i1;
            do
            {
                for(i1 = l; i1 < k1 - 1; i1++)
                {
                    double d2 = Math.abs(matrix1.at(i1, 0)) + Math.abs(matrix1.at(i1 + 1, 0));
                    if(Math.abs(matrix2.at(i1, 0)) + d2 == d2)
                        break;
                }
                
                if(i1 != l)
                {
                    if(j1++ == 30)
                    {
                        System.err.println("Matrix::QL -> Too many iterations.");
                        System.exit(0);
                    }
                    double d5 = (matrix1.at(l + 1, 0) - matrix1.at(l, 0)) / (2D * matrix2.at(l, 0));
                    double d7 = pythagoras(d5, 1.0D);
                    d5 = (matrix1.at(i1, 0) - matrix1.at(l, 0)) + matrix2.at(l, 0) / (d5 + SIGN(d7, d5));
                    double d1;
                    double d8 = d1 = 1.0D;
                    double d6 = 0.0D;
                    int j;
                    for(j = i1 - 1; j >= l; j--)
                    {
                        double d3 = d8 * matrix2.at(j, 0);
                        double d = d1 * matrix2.at(j, 0);
                        matrix2.set(j + 1, 0, d7 = pythagoras(d3, d5));
                        if(d7 == 0.0D)
                        {
                            matrix1.set(j + 1, 0, matrix1.at(j + 1, 0) - d6);
                            matrix2.set(i1, 0, 0.0D);
                            break;
                        }
                        d8 = d3 / d7;
                        d1 = d5 / d7;
                        d5 = matrix1.at(j + 1, 0) - d6;
                        d7 = (matrix1.at(j, 0) - d5) * d8 + 2D * d1 * d;
                        matrix1.set(j + 1, 0, d5 + (d6 = d8 * d7));
                        d5 = d1 * d7 - d;
                        if(flag)
                        {
                            for(int k = 0; k < k1; k++)
                            {
                                double d4 = matrix.at(k, j + 1);
                                matrix.set(k, j + 1, d8 * matrix.at(k, j) + d1 * d4);
                                matrix.set(k, j, d1 * matrix.at(k, j) - d8 * d4);
                            }
                            
                        }
                    }
                    
                    if(d7 != 0.0D || j < l)
                    {
                        matrix1.set(l, 0, matrix1.at(l, 0) - d6);
                        matrix2.set(l, 0, d5);
                        matrix2.set(i1, 0, 0.0D);
                    }
                }
            } while(i1 != l);
        }
        
    }
    
    protected double SIGN(double d, double d1)
    {
        if(d1 >= 0.0D)
            return Math.abs(d);
        else
            return -Math.abs(d);
    }
    
    
    public double at(int i, int j)
    {
        return _a[i][j];
    }
    
    public int cols()
    {
        return _col;
    }
    
    protected void createData()
    {
        int i = _row + 1;
        int j = _col + 1;
        _a = new double[i][j];
    }
    
    public Eigensystem eigensystem()
    {
        if(!isSymmetric())
        {
            System.err.println("Matrix::eigensystem -> Matrix is not symmetric.");
            System.exit(0);
        }
        int i = _row;
        Matrix matrix = new Matrix(this);
        Matrix matrix1 = new Matrix(i, 1);
        Matrix matrix2 = new Matrix(i, 1);
        householder(matrix, matrix1, matrix2, true);
        QL(matrix, matrix1, matrix2, true);
        if(_eigensystem == null)
            _eigensystem = new Eigensystem();
        Matrix matrix3 = new Matrix(matrix);
        _eigensystem.setEigenvectors(matrix3);
        Matrix matrix4 = new Matrix(matrix1);
        _eigensystem.setEigenvalues(matrix4);
        return _eigensystem;
    }
    
    public Matrix eigenvalues()
    {
        if(!isSymmetric())
        {
            System.err.println("Matrix::eigenvalues -> Matrix is not symmetric.");
            System.exit(0);
        }
        int i = _row;
        Matrix matrix = new Matrix(this);
        Matrix matrix1 = new Matrix(i, 1);
        Matrix matrix2 = new Matrix(i, 1);
        householder(matrix, matrix1, matrix2, false);
        QL(matrix, matrix1, matrix2, false);
        if(_eigenvalues == null)
            _eigenvalues = new Matrix(i, 1);
        _eigenvalues = new Matrix(matrix1);
        return _eigenvalues;
    }
    
    
    public Eigensystem getEigensystem()
    {
        return _eigensystem;
    }
    
    public Matrix getEigenvalues()
    {
        return _eigenvalues;
    }
    
    protected void householder(Matrix matrix, Matrix matrix1, Matrix matrix2, boolean flag)
    {
        int j3 = 0;
        int k3 = _row;
        for(int i = k3 - 1; i > 0; i--)
        {
            j3 = i - 1;
            double d;
            double d7 = d = 0.0D;
            if(j3 > 0)
            {
                for(int k1 = 0; k1 <= j3; k1++)
                    d += Math.abs(matrix.at(i, k1));
                
                if(d == 0.0D)
                {
                    matrix2.set(i, 0, matrix.at(i, i));
                }
                else
                {
                    for(int l1 = 0; l1 <= j3; l1++)
                    {
                        matrix.set(i, l1, matrix.at(i, l1) / d);
                        d7 += matrix.at(i, l1) * matrix.at(i, l1);
                    }
                    
                    double d1 = matrix.at(i, j3);
                    double d3 = d1 < 0.0D ? Math.sqrt(d7) : -Math.sqrt(d7);
                    matrix2.set(i, 0, d * d3);
                    d7 -= d1 * d3;
                    matrix.set(i, j3, d1 - d3);
                    d1 = 0.0D;
                    for(int k = 0; k <= j3; k++)
                    {
                        if(flag)
                            matrix.set(k, i, matrix.at(i, k) / d7);
                        double d4 = 0.0D;
                        for(int i2 = 0; i2 <= k; i2++)
                            d4 += matrix.at(k, i2) * matrix.at(i, i2);
                        
                        for(int j2 = k + 1; j2 <= j3; j2++)
                            d4 += matrix.at(j2, k) * matrix.at(i, j2);
                        
                        matrix2.set(k, 0, d4 / d7);
                        d1 += matrix2.at(k, 0) * matrix.at(i, k);
                    }
                    
                    double d8 = d1 / (d7 + d7);
                    for(int l = 0; l <= j3; l++)
                    {
                        double d2 = matrix.at(i, l);
                        double d5 = matrix2.at(l, 0) - d8 * d2;
                        matrix2.set(l, 0, d5);
                        for(int k2 = 0; k2 <= l; k2++)
                            matrix.set(l, k2, matrix.at(l, k2) - (d2 * matrix2.at(k2, 0) + d5 * matrix.at(i, k2)));
                        
                    }
                    
                }
            }
            else
            {
                matrix2.set(i, 0, matrix.at(i, j3));
            }
            matrix1.set(i, 0, d7);
        }
        
        if(flag)
            matrix1.set(0, 0, 0.0D);
        matrix2.set(0, 0, 0.0D);
        for(int j = 0; j < k3; j++)
        {
            if(flag)
            {
                j3 = j - 1;
                if(matrix1.at(j, 0) != 0.0D)
                {
                    for(int i1 = 0; i1 <= j3; i1++)
                    {
                        double d6 = 0.0D;
                        for(int l2 = 0; l2 <= j3; l2++)
                            d6 += matrix.at(j, l2) * matrix.at(l2, i1);
                        
                        for(int i3 = 0; i3 <= j3; i3++)
                            matrix.set(i3, i1, matrix.at(i3, i1) - d6 * matrix.at(i3, j));
                        
                    }
                    
                }
            }
            matrix1.set(j, 0, matrix.at(j, j));
            if(flag)
            {
                matrix.set(j, j, 1.0D);
                for(int j1 = 0; j1 <= j3; j1++)
                {
                    matrix.set(j, j1, 0.0D);
                    matrix.set(j1, j, matrix.at(j, j1));
                }
                
            }
        }
        
    }
    
    public Matrix inverse()
    {
        if(!isSquare())
        {
            System.err.println("Matrix::inverse -> Matrix is not square.");
            System.exit(0);
        }
        int i = _row;
        Matrix matrix = new Matrix(i, 2 * i);
        Matrix matrix1 = new Matrix(i, 1);
        Matrix matrix2 = new Matrix(i, 1);
        for(int j = 0; j < i; j++)
        {
            for(int i2 = 0; i2 < i; i2++)
                matrix.set(j, i2, _a[j][i2]);
            
        }
        
        for(int k = 0; k < i; k++)
        {
            for(int j2 = i; j2 < 2 * i; j2++)
                matrix.set(k, j2, 0.0D);
            
            matrix.set(k, i + k, 1.0D);
        }
        
        for(int i4 = 0; i4 < i; i4++)
        {
            for(int l = i4; l < i; l++)
            {
                matrix2.set(l, 0, 0.0D);
                for(int k2 = i4; k2 < i; k2++)
                    matrix2.set(l, 0, matrix2.at(l, 0) + matrix.at(l, k2));
                
                matrix1.set(l, 0, Math.abs(matrix.at(l, i4)) / matrix2.at(l, 0));
            }
            
            int k4 = i4;
            for(int i1 = i4 + 1; i1 < i; i1++)
                if(matrix1.at(i1, 0) > matrix1.at(i1 - 1, 0))
                    k4 = i1;
            
            if(matrix1.at(k4, 0) == 0.0D)
            {
                System.err.println("Matrix::inverse -> Matrix is singular.");
                System.exit(0);
            }
            if(k4 != i4)
            {
                for(int l2 = 0; l2 < 2 * i; l2++)
                {
                    double d = matrix.at(i4, l2);
                    matrix.set(i4, l2, matrix.at(k4, l2));
                    matrix.set(k4, l2, d);
                }
                
            }
            for(int i3 = 2 * i - 1; i3 >= i4; i3--)
                matrix.set(i4, i3, matrix.at(i4, i3) / matrix.at(i4, i4));
            
            for(int j1 = i4 + 1; j1 < i; j1++)
            {
                for(int j3 = 2 * i - 1; j3 >= i4 + 1; j3--)
                    matrix.set(j1, j3, matrix.at(j1, j3) - matrix.at(i4, j3) * matrix.at(j1, i4));
                
            }
            
        }
        
        for(int j4 = i - 1; j4 >= 0; j4--)
        {
            for(int k1 = j4 - 1; k1 >= 0; k1--)
            {
                for(int k3 = i; k3 < 2 * i; k3++)
                    matrix.set(k1, k3, matrix.at(k1, k3) - matrix.at(j4, k3) * matrix.at(k1, j4));
                
            }
            
        }
        
        Matrix matrix3 = new Matrix(i, i);
        for(int l1 = 0; l1 < i; l1++)
        {
            for(int l3 = 0; l3 < i; l3++)
                matrix3.set(l1, l3, matrix.at(l1, l3 + i));
            
        }
        
        return matrix3;
    }
    
    public boolean isLowerDiagonal()
    {
        for(int i = 0; i < _row - 1; i++)
        {
            for(int j = i + 1; j < _col; j++)
                if(_a[i][j] != 0.0D)
                    return false;
            
        }
        
        return true;
    }
    
    public boolean isSquare()
    {
        return _row == _col;
    }
    
    public boolean isSymmetric()
    {
        if(!isSquare())
            return false;
        for(int i = 0; i < _row; i++)
        {
            for(int j = i + 1; j < _col; j++)
                if(_a[i][j] != _a[j][i])
                    return false;
            
        }
        
        return true;
    }
    
    public boolean isUpperDiagonal()
    {
        for(int i = 1; i < _row; i++)
        {
            for(int j = 0; j < i; j++)
                if(_a[i][j] != 0.0D)
                    return false;
            
        }
        
        return true;
    }
    
    public void makeIdentity()
    {
        if(_row != _col)
        {
            System.err.println("Matrix::makeIdentity -> Matrix is not square.");
            System.exit(0);
        }
        int i = _row;
        for(int j = 0; j < i; j++)
        {
            for(int k = 0; k < i; k++)
                _a[j][k] = 0.0D;
            
            _a[j][j] = 1.0D;
        }
        
    }
    
    public Matrix times(double d)
    {
        int i = _row;
        int j = _col;
        Matrix matrix = new Matrix(i, j);
        for(int k = 0; k < i; k++)
        {
            for(int l = 0; l < j; l++)
                matrix.set(k, l, d * _a[k][l]);
            
        }
        
        return matrix;
    }
    
    public Matrix times(Matrix matrix)
    {
        int i = _row;
        int j = _col;
        int k = matrix.cols();
        if(_col != matrix.rows())
        {
            System.err.println("Matrix::operator* -> Invalid dimensions of matrices.");
            System.exit(0);
        }
        Matrix matrix1 = new Matrix(i, k);
        for(int l = 0; l < i; l++)
        {
            for(int i1 = 0; i1 < k; i1++)
            {
                double d = 0.0D;
                for(int j1 = 0; j1 < j; j1++)
                    d += _a[l][j1] * matrix.at(j1, i1);
                
                matrix1.set(l, i1, d);
            }
            
        }
        
        return matrix1;
    }
    
    protected double pythagoras(double d, double d1)
    {
        double d2 = Math.abs(d);
        double d3 = Math.abs(d1);
        if(d2 > d3)
            return d2 * Math.sqrt(1.0D + square(d3 / d2));
        else
            return d3 != 0.0D ? d3 * Math.sqrt(1.0D + square(d2 / d3)) : 0.0D;
    }
    
    
    
    public int rows()
    {
        return _row;
    }
    
    public void set(int i, int j, double d)
    {
        _a[i][j] = d;
    }
    
    public void plusequal(int i, int j, double d)
    {
        _a[i][j] += d;
    }
    
    public void plusEqual(Matrix b)
    {
        for(int i=0; i<rows(); ++i)
        {
            for(int j=0; j<cols(); ++j)
            {
                _a[i][j] += b._a[i][j];
            }
        }
    }
    
    protected double square(double d)
    {
        if(d == 0.0D)
            return 0.0D;
        else
            return d * d;
    }
    
    public Matrix submatrix(int i, int j, int k, int l)
    {
        Matrix matrix = new Matrix(i, j);
        for(int i1 = 0; i1 < i; i1++)
        {
            for(int j1 = 0; j1 < j; j1++)
                matrix._a[i1][j1] = _a[i1 + k][j1 + l];
            
        }
        
        return matrix;
    }
    
    public Matrix subtract(Matrix matrix)
    {
        int i = _row;
        int j = _col;
        if(i != matrix.rows() || j != matrix.cols())
        {
            System.err.println("Matrix::operator- -> Invalid dimensions of matrices.");
            System.exit(0);
        }
        Matrix matrix1 = new Matrix(i, j);
        for(int k = 0; k < i; k++)
        {
            for(int l = 0; l < j; l++)
                matrix1.set(k, l, _a[k][l] - matrix.at(k, l));
            
        }
        
        return matrix1;
    }
    
    public Matrix transposed()
    {
        int i = _row;
        int j = _col;
        Matrix matrix = new Matrix(j, i);
        for(int k = 0; k < j; k++)
        {
            for(int l = 0; l < i; l++)
                matrix.set(k, l, _a[l][k]);
            
        }
        
        return matrix;
    }
    
    public Matrix upperDiagonal()
    {
        int i2 = _row;
        int j2 = _col;
        if(i2 < j2)
        {
            System.err.println("Matrix::upperDiagonal -> More rows than columns required.");
            System.exit(0);
        }
        Matrix matrix = new Matrix(this);
        int l1 = j2;
        if(i2 - 1 < j2)
            l1 = i2 - 1;
        for(int i1 = 0; i1 <= l1; i1++)
        {
            double d1 = 0.0D;
            for(int i = i1; i < i2; i++)
                d1 += matrix.at(i, i1) * matrix.at(i, i1);
            
            double d4 = 0.0D;
            if(matrix.at(i1, i1) < 0.0D)
                d4 = -1D;
            if(matrix.at(i1, i1) > 0.0D)
                d4 = 1.0D;
            double d2 = -d4 * Math.sqrt(d1);
            double d = d1 - matrix.at(i1, i1) * d2;
            matrix.set(i1, i1, matrix.at(i1, i1) - d2);
            for(int k1 = i1 + 1; k1 < j2; k1++)
            {
                double d3 = 0.0D;
                for(int j = i1; j < i2; j++)
                    d3 += matrix.at(j, i1) * matrix.at(j, k1);
                
                double d5 = d3 / d;
                for(int k = i1; k < i2; k++)
                    matrix.set(k, k1, matrix.at(k, k1) - matrix.at(k, i1) * d5);
                
            }
            
            matrix.set(i1, i1, d2);
        }
        
        for(int l = 1; l < i2; l++)
        {
            for(int j1 = 0; j1 < l; j1++)
                matrix.set(l, j1, 0.0D);
            
        }
        
        if(matrix.at(j2 - 1, j2 - 1) == 0.0D)
        {
            System.err.println("Matrix::householder -> Rank of matrix < n");
            System.exit(0);
        }
        int k2 = _row;
        if(_col < _row)
            k2 = _col;
        return matrix.submatrix(k2, k2, 0, 0);
    }
    
    public void write(String s)
    {
        write(s, "");
    }
    
    public void write(String s, String s1)
    {
        System.out.println("Writing matrix to '" + s + "'.");
        try
        {
            PrintWriter printwriter = new PrintWriter(new BufferedWriter(new FileWriter(s)));
            String s2 = "# " + s1;
            printwriter.println(s2);
            String s3 = _row + " " + _col;
            printwriter.println(s3);
            String s4 = "";
            for(int i = 0; i < _row; i++)
            {
                String s5 = "";
                for(int j = 0; j < _col; j++)
                    s5 = s5 + _a[i][j] + " ";
                
                printwriter.println(s5.substring(0, s5.length() - 1));
            }
            
            printwriter.close();
        }
        catch(IOException _ex)
        {
            System.err.println("Matrix::write -> Error writing to '" + s + "'.");
            System.exit(0);
        }
        System.out.println("Matrix written.");
    }
    
    public String toString()
    {
        return "Matrix: ";
    }
    
    public Matrix column(int col)
    {
        int rows = _row;
        int cols = 1;
        Matrix matrix = new Matrix(rows, cols);
        for(int i = 0; i < rows; ++i)
        {
            matrix.set(i, 0, _a[i][col]);
        }
        
        return matrix;
    }
    
    public Matrix row(int row)
    {
        int rows = 1;
        int cols = _col;
        Matrix matrix = new Matrix(rows, cols);
        for(int i = 0; i < cols; ++i)
        {
            matrix.set(0, i, _a[row][i]);
        }
        
        return matrix;
    }
}
