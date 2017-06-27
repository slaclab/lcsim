package org.lcsim.plugin.browser;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.Vector;
import org.lcsim.event.EventHeader.LCMetaData;
import org.lcsim.event.GenericObject;
import org.lcsim.lcio.LCIOConstants;
import org.lcsim.lcio.LCIOUtil;

/** 
 * Table model for LCGenericObjects. If the objects are fixed size and have a
 * valid data description in the collection parameters this is used for
 * displaying the data. Otherwise the data is displayed in a generic format, 
 * i.e. using nInt, nFloat and nDouble.
 *
 * @author gaede
 * @version $Id: LCGenericObjectTableModel.java,v 1.2 2011/04/01 22:05:04 tonyj Exp $
 */
class LCGenericObjectTableModel extends AbstractTableModel implements EventBrowserTableModel
{
   private List data;
   private String[] columns = {"index","nInt","intValues","nFloat","floatValues","nDouble","doubleValues"};
   private Class[] klasses = { Integer.class, Integer.class , new int[0].getClass(), Integer.class , new float[0].getClass(), Integer.class , new double[0].getClass() };
   private boolean isFixedSize = false;
   private int[] indices = null;
   
   public boolean canDisplay(Class c)
   {
      return GenericObject.class.isAssignableFrom(c);
   }
   public void setData(LCMetaData meta, List data)
   {
      this.data = data;
      int flags = meta.getFlags();
      
      String[] desc = meta.getStringParameters().get("DataDescription");
      isFixedSize = LCIOUtil.bitTest(flags, LCIOConstants.GOBIT_FIXED);
      
      if( isFixedSize && desc != null && desc.length != 0 && desc[0].length() != 0 )
      {
         // some helper vectors
         Vector colVec = new Vector() ;
         Vector klassVec = new Vector() ;
         Vector indexVec = new Vector() ;
         
         colVec.add( "index" ) ;
         klassVec.add( Integer.class ) ;
         
         try
         {
            
            evaluateDataDescription( data, desc[0], colVec, klassVec , indexVec ) ;
            
            // copy vectors to arrays ...
            String[] newColumns = new String[ colVec.size() ] ;
            for(int i=0;i<colVec.size() ;i++)
               newColumns[i] = (String) colVec.get(i) ;
            columns = newColumns ;
            
            Class[] newKlasses = new Class[ klassVec.size() ] ;
            for(int i=0;i<klassVec.size() ;i++)
               newKlasses[i] = (Class) klassVec.get(i) ;
            klasses =  newKlasses ;
            
            indices = new int[ indexVec.size() ] ;
            for(int i=0;i<indexVec.size() ;i++)
               indices[i] = ( (Integer) indexVec.get(i) ).intValue() ;
         }
         catch( Exception e)
         {
            isFixedSize = false ; // sth. went wrong with the data description ...
         }
      }
      else
      {
         isFixedSize = false ; // in case ther is no data description ...
      }
      fireTableDataChanged();
   }
   
   /** Helper method that determines the colums and classes for the table from the data description string */
   void evaluateDataDescription(List data, String desc, Vector colVec, Vector klassVec, Vector indexVec ) throws Exception
   {
      
      int nInt=0, nFloat=0, nDouble=0 ;
      
      String values[] = desc.split(",") ;
      for( int i=0 ; i< values.length ; i++)
      {
         
         String value[] = values[i].split(":") ;
         
         if( value[0].equals("i"))
         {
            colVec.add( value[1] ) ;
            klassVec.add( Integer.class ) ;
            indexVec.add( new Integer( nInt ++ ) )  ;
         }
         if( value[0].equals("x"))
         {
            colVec.add( value[1] ) ;
            klassVec.add( String.class ) ;  // print hex numbers in table
            indexVec.add( new Integer( nInt ++ ) )  ;
         }
         if( value[0].equals("f"))
         {
            colVec.add( value[1] ) ;
            klassVec.add( Float.class ) ;
            indexVec.add( new Integer( nFloat ++ ) )  ;
         }
         if( value[0].equals("d"))
         {
            colVec.add( value[1] ) ;
            klassVec.add( Double.class ) ;
            indexVec.add( new Integer( nDouble ++ ) )  ;
         }
      }
      
      // now do some sanity checks:
      
      GenericObject obj = (GenericObject) data.get(0) ;
      if(  obj.getNInt() != nInt || obj.getNFloat() != nFloat || obj.getNDouble() != nDouble )
      {
         throw new Exception("Wrong data description string !"
                 +  obj.getNInt()+" : " + nInt
                 +  obj.getNFloat()+" : " + nFloat
                 +  obj.getNDouble()+" : " + nDouble ) ;
      }
   }
   
   public int getRowCount()
   {
      return data == null ? 0 : data.size();
   }
   public int getColumnCount()
   {
      return columns.length;
   }
   public String getColumnName(int index)
   {
      return columns[index];
   }
   public Class getColumnClass(int index)
   {
      return klasses[index];
   }
   
   public Object getValueAt(int row, int column)
   {
      GenericObject obj = (GenericObject) data.get(row);
      
      if( isFixedSize )
      {
         
         if( column == 0 )
            return new Integer(row);
         
         if( klasses[column] == Integer.class )
            return new Integer( obj.getIntVal( indices[ column-1 ] ) ) ;
         
         if( klasses[column] == String.class )
         {
            String zeros = "0x00000000" ;
            String hex = Integer.toHexString( obj.getIntVal( indices[ column-1 ]  ) ) ;
            return zeros.substring(0, ( 10 - hex.length() ) ) + hex  ;
         }
         
         if( klasses[column] == Float.class )
            return new Float( obj.getFloatVal( indices[ column-1 ] ) );
         
         if( klasses[column] == Double.class )
            return new Double( obj.getDoubleVal( indices[ column-1 ] ) ) ;
         
      }
      else
      {  // generic format
         
         switch (column)
         {
            
            case 0:
               return new Integer(row);
               
            case 1:
               return new Integer( obj.getNInt() );
               
            case 2:
               int[] ints = new int[ obj.getNInt() ] ;
               for(int i=0;i<obj.getNInt();i++)
                  ints[i] = obj.getIntVal(i) ;
               return ints ;
               
            case 3:
               return new Integer( obj.getNFloat() );
               
            case 4:
               float[] floats = new float[ obj.getNFloat() ] ;
               for(int i=0;i<obj.getNFloat();i++)
                  floats[i] = obj.getFloatVal(i) ;
               return floats ;
               
            case 5:
               return new Integer( obj.getNDouble() );
               
            case 6:
               double[] doubles = new double[ obj.getNDouble() ] ;
               for(int i=0;i<obj.getNDouble();i++)
                  doubles[i] = obj.getDoubleVal(i) ;
               return doubles ;
               
         }
         
      }
      return " " ;
   }
}
