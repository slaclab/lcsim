package org.lcsim.util.xml;

import gnu.jel.CompilationException;
import gnu.jel.CompiledExpression;
import gnu.jel.DVMap;
import gnu.jel.Evaluator;
import gnu.jel.Library;
import java.util.HashMap;
import java.util.Map;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.DefaultJDOMFactory;
import org.jdom.Namespace;

/**
 *
 * @author tonyj
 */
public class JDOMExpressionFactory extends DefaultJDOMFactory
{
   private Object[] resolver = { new Resolver() };
   private Library jelLibrary = setUpLibrary();
   private Map<String,Double> constants = new HashMap<String,Double>();
   
   public void addConstant(String name, double value)
   {
      constants.put(name,value);
   }
   
   public org.jdom.Attribute attribute(String name, String value, int type, Namespace namespace)
   {
      return new CustomAttribute(name,value,type,namespace);
   }
   
   public org.jdom.Attribute attribute(String name, String value, Namespace namespace)
   {
      return new CustomAttribute(name,value,namespace);
   }
   
   public org.jdom.Attribute attribute(String name, String value, int type)
   {
      return new CustomAttribute(name,value,type);
   }
   
   public org.jdom.Attribute attribute(String name, String value)
   {
      return new CustomAttribute(name,value);
   }
   
   private class CustomAttribute extends Attribute
   {
      CustomAttribute(String name, String value)
      {
         super(name,value);
      }
      CustomAttribute(String name, String value, int type)
      {
         super(name,value,type);
      }
      CustomAttribute(String name, String value, Namespace namespace)
      {
         super(name,value,namespace);
      }
      CustomAttribute(String name, String value, int type, Namespace namespace)
      {
         super(name,value,type,namespace);
      }
      
      //      public boolean getBooleanValue() throws DataConversionException
      //      {
      //         String expression = super.getValue();
      //         try
      //         {
      //            CompiledExpression expr = Evaluator.compile(expression,jelLibrary,Boolean.TYPE);
      //            return expr.evaluate_boolean(resolver);
      //         }
      //         catch (Throwable x)
      //         {
      //            DataConversionException xx = new DataConversionException(expression,"boolean");
      //            xx.initCause(x);
      //            throw xx;
      //         }
      //      }
      
      public long getLongValue() throws DataConversionException
      {
         String expression = super.getValue();
         try
         {
            CompiledExpression expr = Evaluator.compile(expression,jelLibrary,Long.TYPE);
            return expr.evaluate_long(resolver);
         }
         catch (Throwable x)
         {
            DataConversionException xx = new DataConversionException(expression,"long");
            xx.initCause(x);
            throw xx;
         }
      }
      
      public int getIntValue() throws DataConversionException
      {
         String expression = super.getValue();
         try
         {
            CompiledExpression expr = Evaluator.compile(expression,jelLibrary,Integer.TYPE);
            return expr.evaluate_int(resolver);
         }
         catch (Throwable x)
         {
            DataConversionException xx = new DataConversionException(expression,"int");
            xx.initCause(x);
            throw xx;
         }
      }
      
      public float getFloatValue() throws DataConversionException
      {
         String expression = super.getValue();
         try
         {
            CompiledExpression expr = Evaluator.compile(expression,jelLibrary,Float.TYPE);
            return expr.evaluate_float(resolver);
         }
         catch (Throwable x)
         {
            DataConversionException xx = new DataConversionException(expression,"float");
            xx.initCause(x);
            throw xx;
         }
      }
      
      public double getDoubleValue() throws DataConversionException
      {
         String expression = super.getValue();
         try
         {
            CompiledExpression expr = Evaluator.compile(expression,jelLibrary,Double.TYPE);
            return expr.evaluate_double(resolver);
         }
         catch (Throwable x)
         {
            DataConversionException xx = new DataConversionException(expression,"double");
            xx.initCause(x);
            throw xx;
         }
      }
   }
      
   public double computeDouble(String expression)
   {
	   try
       {
          CompiledExpression expr = Evaluator.compile(expression,jelLibrary,Double.TYPE);
          return expr.evaluate_double(resolver);
       }	
	   catch (Throwable x)
	   {
		   throw new RuntimeException(x);
	   }
   }
   
   public float computeFloat(String expression)
   {
	   try
       {
          CompiledExpression expr = Evaluator.compile(expression,jelLibrary,Float.TYPE);
          return expr.evaluate_float(resolver);
       }	
	   catch (Throwable x)
	   {
		   throw new RuntimeException(x);
	   }
   }
   
   /*
   public int computeInteger(String expression)
   {
	   try
       {
          CompiledExpression expr = Evaluator.compile(expression,jelLibrary,Integer.TYPE);
          return expr.evaluate_int(resolver);
       }	
	   catch (Throwable x)
	   {
		   throw new RuntimeException(x);
	   }
   }
   */
        
   private Library setUpLibrary()
   {
      try
      {
         Class[] staticLib = { Math.class };
         Class[] dynamicLib = { Resolver.class };
         Library lib = new Library(staticLib,dynamicLib,null,(Resolver) resolver[0],null);
         lib.markStateDependent("random",null);
         return lib;
      }
      catch (CompilationException x)
      {
         throw new RuntimeException(x); // should never happen
      }
   }
   
   public class Resolver extends DVMap
   {
      public String getTypeName(String str)
      {
         if (constants.containsKey(str)) return "Double";
         return null;
      }
      public double getDoubleProperty(String str)
      {
         return ((Number) constants.get(str)).doubleValue();
      }
   }
}
