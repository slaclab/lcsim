package org.lcsim.geometry.compact.converter;

import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.filechooser.FileFilter;

/**
 * An interface to be implemented by all converters
 * @author tonyj
 */
public interface Converter
{
   void convert(String inputFileName, InputStream in, OutputStream out) throws Exception;
   String getOutputFormat();
   FileFilter getFileFilter();
}