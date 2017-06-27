package org.lcsim.util.loop;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Some utitities for dealing with file lists.
 * @author tonyj
 */
public class FileList
{
   private String title;
   private List<File> fileList;
   private final static String TITLE = "Title:";
   
   public FileList(List<File> files, String title)
   {
      this.fileList = files;
      this.title = title;
   }
   /**
    * Creates a FileListUtils by reading a file containing a file list.
    * The file may contain a title (a line starting with "Title:") and any number of files.
    * Blank lines, or lines beginning with # are ignored.
    * Files may be absolute, or specified relative to this filelist itself.
    * @param file The file to read.
    * @param defaultTitle The title to be used if the file does not contain a title.
    */
   public FileList(File file, String defaultTitle) throws IOException
   {
      this.fileList = new ArrayList<File>();
      this.title = defaultTitle;
      File base = file.getParentFile();
      BufferedReader fileListReader = new BufferedReader(new FileReader(file));
      try
      {
         for (;;)
         {
            String line = fileListReader.readLine();
            if (line == null) break;
            line = line.trim();
            if (line.length() == 0 || line.startsWith("#")) continue;
            if (line.startsWith(TITLE))
            {
               title = line.substring(TITLE.length()).trim();
            }
            else
            {
               File item = new File(base,line);
               if (!item.exists() || !item.canRead()) throw new IOException("File "+item+" cannot be read");
               fileList.add(item);
            }
         }
      }
      finally
      {
         fileListReader.close();
      }
   }
   /**
    * Writes this filelist to a file.
    */
   public void write(File file) throws IOException
   {
      String base = file.getParent();
      if (!base.endsWith(File.separator)) base += File.separator;
      
      BufferedWriter pw = new BufferedWriter(new FileWriter(file));
      if (title != null)
      {
         pw.write(TITLE);
         pw.write(' ');
         pw.write(title);
         pw.newLine();
      }
      try
      {
         for (File item : fileList)
         {
            String filePath = item.getPath();
            if (filePath.startsWith(base)) filePath = filePath.substring(base.length());
            pw.write(filePath);
            pw.newLine();
         }
      }
      finally
      {
         pw.close();
      }
   }
   
   public String getTitle()
   {
      return title;
   }
   
   public void setTitle(String title)
   {
      this.title = title;
   }
   
   public List<File> getFileList()
   {
      return fileList;
   }
   
   public void setFileList(List<File> fileList)
   {
      this.fileList = fileList;
   }
   
}
