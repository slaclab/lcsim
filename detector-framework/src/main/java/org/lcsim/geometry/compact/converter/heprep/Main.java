package org.lcsim.geometry.compact.converter.heprep;

import hep.graphics.heprep.HepRep;
import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepWriter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.filechooser.FileFilter;
import org.jdom.input.SAXBuilder;
import org.lcsim.geometry.GeometryReader;
import org.lcsim.geometry.Detector;
import org.lcsim.geometry.compact.converter.Converter;

/**
 *
 * @author tonyj
 */
public class Main implements Converter
{
   private static boolean validateDefault = false; // FREEHEP-553
   private boolean validate;
   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) throws Exception
   {
      if (args.length < 1 || args.length >2) usage();
      InputStream in = new BufferedInputStream(new FileInputStream(args[0]));
      OutputStream out = args.length == 1 ?  System.out : new BufferedOutputStream(new FileOutputStream(args[1]));
      new Main().convert(args[0],in,out);
   }
   public Main()
   {
      this(validateDefault);
   }
   public Main(boolean validate)
   {
      this.validate = validate;
   }
   public void convert(String inputFileName, InputStream in, OutputStream out) throws Exception
   {
      GeometryReader reader = new GeometryReader();
      Detector det = reader.read(in);
      
      HepRep heprep = createHepRep(det);

      if (validate)
      {
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         writeHepRep(heprep,stream);
         stream.close();
         
         SAXBuilder builder = new SAXBuilder();
         builder.setValidation(true);
         builder.setFeature("http://apache.org/xml/features/validation/schema",true);
         builder.build(new ByteArrayInputStream(stream.toByteArray()));
      }    

      if (out != null) writeHepRep(heprep,out);
   }
   private static void writeHepRep(HepRep heprep, OutputStream out) throws Exception
   {
      HepRepWriter writer = HepRepFactory.create().createHepRepWriter(out,false,false);
      writer.write(heprep,"test");
      writer.close();  
   }
   private static HepRep createHepRep(Detector det) throws Exception
   {
      HepRepFactory factory = HepRepFactory.create();
      HepRep root = factory.createHepRep();
      det.appendHepRep(factory,root);
      return root;
   }
   private static void usage()
   {
      System.out.println("java "+Main.class.getName()+" <compact> [<heprep>]");
      System.exit(0);
   }

   public FileFilter getFileFilter()
   {
      return new HepRepFileFilter();
   }

   public String getOutputFormat()
   {
      return "heprep";
   }
   
   private static class HepRepFileFilter extends FileFilter
   {
      public boolean accept(java.io.File file)
      {
         return file.isDirectory() || file.getName().endsWith(".heprep");
      }
      
      public String getDescription()
      {
         return "HepRep file (*.heprep)";
      }
   }
}