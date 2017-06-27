package org.lcsim.util.aida;

import hep.aida.IAnalysisFactory;
import hep.aida.IBaseHistogram;
import hep.aida.ICloud1D;
import hep.aida.ICloud2D;
import hep.aida.ICloud3D;
import hep.aida.IHistogram1D;
import hep.aida.IHistogram2D;
import hep.aida.IHistogram3D;
import hep.aida.IHistogramFactory;
import hep.aida.IManagedObject;
import hep.aida.IProfile1D;
import hep.aida.IProfile2D;
import hep.aida.ITree;
import hep.aida.ref.rootwriter.RootFileStore;
import hep.aida.ref.xml.AidaXMLStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.freehep.application.Application;
import org.freehep.application.studio.Studio;
import org.freehep.record.loop.AbstractLoopListener;
import org.freehep.record.loop.LoopEvent;
import org.freehep.record.loop.RecordLoop;

/**
 * A convenience class for creating and filling histograms.
 * Histograms created using this class will be automatically
 * cleared when data is rewound.
 * @author Tony Johnson
 */
public class AIDA
{
   private int defMax = 10000;
   private IAnalysisFactory af;
   private ITree tree;
   private String compressFormat;
   private IHistogramFactory hf;
   private File tempFile;
   private static AIDA defaultInstance;
   public static String aidaTreeCompressProperty = "org.lcsim.util.aida.CompressOption";

   public static AIDA defaultInstance()
   {
      if (defaultInstance == null) defaultInstance = new AIDA();
      return defaultInstance;
   }

   private AIDA()
   {
      try
      {
         compressFormat = System.getProperty(aidaTreeCompressProperty, "gzip");
         tempFile = File.createTempFile("aida",".aida");
         tempFile.deleteOnExit();
         af = IAnalysisFactory.create();
         tree = af.createTreeFactory().create(tempFile.getAbsolutePath(), "xml", false,true, "compress="+compressFormat);
         hf = af.createHistogramFactory(tree);

         Application app = Application.getApplication();
         if (app instanceof Studio)
         {
            RecordLoop loop = (RecordLoop) ((Studio) app).getLookup().lookup(RecordLoop.class);
            if (loop != null) loop.addLoopListener(new RewindListener(this));
         }
      }
      catch (IOException x)
      {
         throw new AIDAException("IOException creating temporary store",x);
      }
   }

   private void checkPath(String path)
   {
      int pos = path.lastIndexOf('/');
      if (pos > 0) tree.mkdirs(path.substring(0, pos));
   }

   public ICloud1D cloud1D(String path)
   {
      return cloud1D(path, defMax);
   }

   public ICloud1D cloud1D(String path, int nMax)
   {
      try
      {
         IManagedObject obj = tree.find(path);
         if (obj instanceof ICloud1D) return (ICloud1D) obj;
         throw new RuntimeException(path +" is not a ICloud1D");
      }
      catch (IllegalArgumentException x)
      {
         checkPath(path);
         return hf.createCloud1D(path, path, nMax);
      }
   }

   public ICloud2D cloud2D(String path)
   {
      return cloud2D(path, defMax);
   }

   public ICloud2D cloud2D(String path, int nMax)
   {
      try
      {
         IManagedObject obj = tree.find(path);
         if (obj instanceof ICloud2D) return (ICloud2D) obj;
         throw new RuntimeException(path +" is not a ICloud2D");
      }
      catch (IllegalArgumentException x)
      {
         checkPath(path);
         return hf.createCloud2D(path, path, nMax);
      }
   }

   public ICloud3D cloud3D(String path)
   {
      return cloud3D(path, defMax);
   }

   public ICloud3D cloud3D(String path, int nMax)
   {
      try
      {
         IManagedObject obj = tree.find(path);
         if (obj instanceof ICloud3D) return (ICloud3D) obj;
         throw new RuntimeException(path +" is not a ICloud3D");
      }
      catch (IllegalArgumentException x)
      {
         checkPath(path);
         return hf.createCloud3D(path, path, nMax);
      }
   }

   public IHistogram1D histogram1D(String path)
   {
      IManagedObject obj = tree.find(path);
      if (obj instanceof IHistogram1D) return (IHistogram1D) obj;
      throw new RuntimeException(path +" is not a IHistogram1D");
   }

   public IHistogram1D histogram1D(String path, int bins, double lowerEdge, double upperEdge)
   {
      try
      {
         IManagedObject obj = tree.find(path);
         if (obj instanceof IHistogram1D) return (IHistogram1D) obj;
         throw new RuntimeException(path +" is not a IHistogram1D");
      }
      catch (IllegalArgumentException x)
      {
         checkPath(path);
         return hf.createHistogram1D(path, bins, lowerEdge, upperEdge);
      }
   }
   
   public IHistogram1D histogram1D(String path, int bins, double lowerEdge, double upperEdge, String options)
   {
      try
      {
         IManagedObject obj = tree.find(path);
         if (obj instanceof IHistogram1D) return (IHistogram1D) obj;
         throw new RuntimeException(path +" is not a IHistogram1D");
      }
      catch (IllegalArgumentException x)
      {
         checkPath(path);
         return hf.createHistogram1D(path,"", bins, lowerEdge, upperEdge, options);
      }
   }

   public IHistogram2D histogram2D(String path)
   {
      IManagedObject obj = tree.find(path);
      if (obj instanceof IHistogram2D) return (IHistogram2D) obj;
      throw new RuntimeException(path +" is not a IHistogram2D");
   }

   public IHistogram2D histogram2D(String path, int nBinsX, double lowerEdgeX, double upperEdgeX, int nBinsY, double lowerEdgeY, double upperEdgeY)
   {
      try
      {
         IManagedObject obj = tree.find(path);
         if (obj instanceof IHistogram2D) return (IHistogram2D) obj;
         throw new RuntimeException(path +" is not a IHistogram2D");
      }
      catch (IllegalArgumentException x)
      {
         checkPath(path);
         return hf.createHistogram2D(path, nBinsX, lowerEdgeX, upperEdgeX, nBinsY, lowerEdgeY, upperEdgeY);
      }
   }

   public IHistogram3D histogram3D(String path)
   {
      IManagedObject obj = tree.find(path);
      if (obj instanceof IHistogram3D) return (IHistogram3D) obj;
      throw new RuntimeException(path +" is not a IHistogram3D");
   }

   public IHistogram3D histogram3D(String path, int nBinsX, double lowerEdgeX, double upperEdgeX, int nBinsY, double lowerEdgeY, double upperEdgeY, int nBinsZ, double lowerEdgeZ, double upperEdgeZ)
   {
      try
      {
         IManagedObject obj = tree.find(path);
         if (obj instanceof IHistogram3D) return (IHistogram3D) obj;
         throw new RuntimeException(path +" is not a IHistogram3D");
      }
      catch (IllegalArgumentException x)
      {
         checkPath(path);
         return hf.createHistogram3D(path, nBinsX, lowerEdgeX, upperEdgeX, nBinsY, lowerEdgeY, upperEdgeY, nBinsZ, lowerEdgeZ, upperEdgeZ);
      }

   }

   public IHistogramFactory histogramFactory()
   {
      return hf;
   }

   public IProfile1D profile1D(String path)
   {
      IManagedObject obj = tree.find(path);
      if (obj instanceof IProfile1D) return (IProfile1D) obj;
      throw new RuntimeException(path +" is not a IProfile1D");
   }

   public IProfile1D profile1D(String path, int nBins, double lowerEdge, double upperEdge)
   {
      try
      {
         IManagedObject obj = tree.find(path);
         if (obj instanceof IProfile1D) return (IProfile1D) obj;
         throw new RuntimeException(path +" is not a IProfile1D");
      }
      catch (IllegalArgumentException x)
      {
         checkPath(path);
         return hf.createProfile1D(path, nBins, lowerEdge, upperEdge);
      }
   }

   public IProfile2D profile2D(String path)
   {
      IManagedObject obj = tree.find(path);
      if (obj instanceof IProfile2D) return (IProfile2D) obj;
      throw new RuntimeException(path +" is not a IProfile2D");
   }

   public IProfile2D profile2D(String path, int nBinsX, double lowerEdgeX, double upperEdgeX, int nBinsY, double lowerEdgeY, double upperEdgeY)
   {
      try
      {
         IManagedObject obj = tree.find(path);
         if (obj instanceof IProfile2D) return (IProfile2D) obj;
         throw new RuntimeException(path +" is not a IProfile2D");
      }
      catch (IllegalArgumentException x)
      {
         checkPath(path);
         return hf.createProfile2D(path, nBinsX, lowerEdgeX, upperEdgeX, nBinsY, lowerEdgeY, upperEdgeY);
      }
   }

   public IAnalysisFactory analysisFactory()
   {
      return af;
   }

   public ITree tree()
   {
      return tree;
   }

   public void saveAs(String name) throws IOException
   {
       if (name.toLowerCase().endsWith(".root")) {
         saveAsRoot(name);
         return;
       }
       if (!name.toLowerCase().endsWith(".aida")) name = name + ".aida";
       save(new File(name), false);
   }

   public void saveAs(File outFile) throws IOException
   {
       if (outFile.getName().toLowerCase().endsWith(".root")) {
         saveAsRoot(outFile.getPath());
         return;
       }
       save(outFile,false);
   }
   
   public void saveAsZip(String name) throws IOException
   {
       if (!name.toLowerCase().endsWith(".aida")) name = name + ".aida";
       save(new File(name), true);
   }

   public void saveAsZip(File outFile) throws IOException
   {
       save(outFile,true);
   }
   void save(File dest, boolean useZip) throws IOException {
       if (!useZip) {
           tree.commit();
           if (dest.exists()) dest.delete();
           boolean rc = tempFile.renameTo(dest);
           if (!rc) {
               byte[] buffer = new byte[32768];
               OutputStream out = new FileOutputStream(dest);
               InputStream in = new FileInputStream(tempFile);
               try {
                   for (;;) {
                       int l = in.read(buffer);
                       if (l<0) break;
                       out.write(buffer,0,l);
                   }
               } finally {
                   out.close();
                   in.close();
                   tempFile.delete();
               }
           }
      } else {
          AidaXMLStore store = new AidaXMLStore();
          if (dest.exists()) dest.delete();
          de.schlichtherle.io.File newFile = new de.schlichtherle.io.File(dest);
          //if (newFile.exists() && !newFile.isDirectory()) 
              //throw new IOException("File already exists: "+newFile.getAbsolutePath());
          store.commit(tree, newFile, null, useZip, false, false);
      }      
   }
   
   private void saveAsRoot(String path) throws IOException {
       RootFileStore store = new RootFileStore(path);
       tree.commit();
       store.open();
       store.add(tree);
       store.close();
   }
   
   private class AIDAException extends RuntimeException
   {
      AIDAException(String message, Throwable cause)
      {
         super(message,cause);
      }
   }
   public void clearAll()
   {
      String[] type = tree.listObjectNames("/",true);
      for (int i=0; i<type.length; i++)
      {
         if (type[i].endsWith("/")) continue;
         IManagedObject obj = tree.find(type[i]);
         if (obj instanceof IBaseHistogram) ((IBaseHistogram) obj).reset();
      }
   }
   private static class RewindListener extends AbstractLoopListener
   {
      private Reference weak;
      RewindListener(AIDA aida)
      {
         weak = new WeakReference(aida);
      }
      public void reset(LoopEvent loopEvent)
      {
         AIDA aida = (AIDA) weak.get();
         if (aida != null)
         {
            aida.clearAll();
         }
         else
         {
            RecordLoop loop = (RecordLoop) loopEvent.getSource();
            loop.removeLoopListener(this);
         }
      }
   }
}
