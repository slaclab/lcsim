package org.lcsim.geometry.compact.converter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.spi.ServiceRegistry;

/**
 * A main routine which can be used to invoke other main routines.
 * @author tonyj
 */
public class Main
{
   private String inFile;
   private String outFile;
   private String format;
   private Converter converter;
   
   String getInputFile()
   {
      return inFile;
   }
   String getOutputFile()
   {
      return outFile;
   }
   String getFormat()
   {
      return format;
   }
   void parseArgs(String[] args) throws InvalidArgumentException
   {
      for (int i=0; i<args.length; i++)
      {
         String arg = args[i];
         if ("-o".equals(arg))
         {
            i++;
            if (i >= args.length) usage();
            format = args[i];
         }
         else if (arg.startsWith("-")) usage();
         else if (inFile == null) inFile = arg;
         else if (outFile == null) outFile = arg;
         else throw new InvalidArgumentException();
      }
      
      if (format != null)
      {
         for (Converter c : getConverters())
         {
            if (c.getOutputFormat().equalsIgnoreCase(format))
            {
               converter = c;
               break;
            }
         }
         if (converter == null) throw new InvalidArgumentException("Sorry, could not find converter for format: "+format);
      }
      // If not explicitly set try to guess output type from output file
      else if (outFile != null)
      {
         File out = new File(outFile);
         for (Converter c : getConverters())
         {
            if (c.getFileFilter().accept(out))
            {
               converter = c;
               break;
            }
         }
         if (converter == null) throw new InvalidArgumentException("Sorry, could not find converter for output file");
      }
   }
   public static void main(String[] args)
   {
      Main main = new Main();
      try
      {
         main.parseArgs(args);
      }
      catch (InvalidArgumentException x)
      {
         String message = x.getMessage();
         if (message != null) System.err.println(message);
         usage();
      }
      if (main.outFile == null)
      {
         MainGUI.main(args);
      }
      else if (main.converter != null)
      {
          try 
          {
              main.run();
          }
          catch (Exception x)
          {  
              throw new RuntimeException(x);
          }
      }
   }
   private void run() throws Exception
   {
      InputStream in = new BufferedInputStream(new FileInputStream(inFile));
      OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
      converter.convert(inFile,in,out);
      in.close();
      out.close();
   }
   
   private static void usage()
   {
      System.out.println("java "+Main.class.getName()+" [-o <format>] [<input> [<output>]]");
      System.exit(0);
   }
   static List<Converter> getConverters()
   {
      Iterator<Converter> iter = getServices(Converter.class);
      List<Converter> result = new ArrayList<Converter>();
      while (iter.hasNext()) result.add(iter.next());
      return result;
   }
   private static <T> Iterator<T> getServices(Class<T> providerClass)
   {
      return ServiceRegistry.lookupProviders(providerClass,Main.class.getClassLoader());
   }
   class InvalidArgumentException extends Exception
   {
      InvalidArgumentException()
      {
         super();
      }
      InvalidArgumentException(String message)
      {
         super(message);
      }
   }
}
