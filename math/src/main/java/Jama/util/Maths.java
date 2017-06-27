package Jama.util;

import Jama.Matrix;

public class Maths {

   /** sqrt(a^2 + b^2) without under/overflow. **/

   public static double hypot(double a, double b) {
      double r;
      if (Math.abs(a) > Math.abs(b)) {
         r = b/a;
         r = Math.abs(a)*Math.sqrt(1+r*r);
      } else if (b != 0) {
         r = a/b;
         r = Math.abs(b)*Math.sqrt(1+r*r);
      } else {
         r = 0.0;
      }
      return r;
   }
   
   public static Matrix toJamaMatrix(hep.physics.matrix.Matrix mIn)
   {
      int nRows = mIn.getNRows();
      int nCols = mIn.getNColumns();
      Matrix result = new Matrix(nRows,nCols);
      for (int i=0;i<nRows;i++)
         for (int j=0; j<nCols;j++)
            result.set(i,j,mIn.e(i,j));
      return result;
   }
   public static hep.physics.matrix.Matrix fromJamaMatrix(Matrix mIn)
   {
      int nRows = mIn.getRowDimension();
      int nCols = mIn.getColumnDimension();
      hep.physics.matrix.BasicMatrix result = new hep.physics.matrix.BasicMatrix(nRows,nCols);
      for (int i=0;i<nRows;i++)
         for (int j=0; j<nCols; j++)
            result.setElement(i,j,mIn.get(i,j));
      return result;
   }
}
