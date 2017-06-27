package org.lcsim.util.loop;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.freehep.record.source.AbstractRecordSource;
import org.freehep.record.source.NoSuchRecordException;
import org.lcsim.event.EventHeader;
import org.lcsim.lcio.*;

/**
 * Convert an LCIOReader to a SequentialRecordSource
 * @author tonyj
 * @version $Id: LCIOEventSource.java,v 1.7 2012/07/02 21:50:17 jeremy Exp $
 */
public class LCIOEventSource extends AbstractRecordSource {

  private List<File> files;
  private boolean atEnd = false;
  private boolean atStart = true;
  private LCIOReader reader;
  private EventHeader currentEvent;
  private int currentFile = 0;
  
  private long _index = -1L;
  private long[] _size;

  /**
   * Create an LCIO event source for reading a single LCIO file
   *
   * @param file The file to read
   */
  public LCIOEventSource(File file) throws IOException {
    super(file.getName());
    this.reader = new LCIOReader(file);
    this.files = Collections.singletonList(file);
    _size = new long[1]; _size[0] = -1L;
  }

  /**
   * Create an LCIO event source for reading a set of LCIO files.
   *
   * @param name The name of the collection of event files
   * @param files The list of files to read.
   */
  public LCIOEventSource(String name, List<File> files) throws IOException {
    super(name);
    if (files.isEmpty()) throw new IOException("File list is empty");
    this.reader = new LCIOReader(files.get(0));
    this.files = files;
    _size = new long[files.size()]; Arrays.fill(_size, -1L);
  }

  /**
   * Create an LCIO event source that will read a set of LCIO files
   *
   * @param list The list of files to open
   */
  public LCIOEventSource(FileList list) throws FileNotFoundException, IOException {
    super(list.getTitle());
    this.files = list.getFileList();
    if (files.isEmpty()) throw new IOException("File list is empty");
    this.reader = new LCIOReader(files.get(0));
    _size = new long[files.size()]; Arrays.fill(_size, -1L);
  }

  
  public Object getCurrentRecord() throws IOException {
    if (_index == -1L) throw new IllegalStateException();
    if (currentEvent == null) throw new IOException();
    return currentEvent;
  }
  
  public long getCurrentIndex() {
    return _index;
  }

  public Class<?> getRecordClass() {
    return EventHeader.class;
  }
  
  public boolean supportsCurrent() {return true;}

  public boolean supportsNext() {return true;}
  
  public boolean supportsPrevious() {return true;}
  
  public boolean supportsIndex() {return true;}
  
  public boolean supportsShift() {return true;}
  
  public boolean supportsRewind() {return true;}

  public boolean hasCurrent() {
    return _index != -1L;
  }

  public boolean hasNext() {
    return !atEnd;
  }
  public boolean hasPrevious() {
    return _index > 0L;
  }

  public boolean hasIndex(long index) {
    return index >= 0L;
  }

  public boolean hasShift(long numberOfRecords) {
    return hasIndex(_index + numberOfRecords);
  }
  
  public boolean hasRewind() {
    return _index != -1L;
  }
  
  public void current() throws IOException, NoSuchRecordException {
    jump(_index);
  }

  public void next() throws IOException, NoSuchRecordException {
    for (;;) {
      try {
        currentEvent = reader.read();
        //currentEvent.put("INPUT_FILE", files.get(currentFile));
        _index++;
      } catch (EOFException x) {
        _size[currentFile] = _index;
        if (currentFile + 1 >= files.size()) {
          atEnd = true;
          throw new NoSuchRecordException();
        } else {
          currentFile++;
          reader.close();
          reader = new LCIOReader(files.get(currentFile));
          continue;
        }
      }
      return;
    }
  }
  
  public void previous() throws IOException, NoSuchRecordException {
    jump(_index-1);
  }
  
  public void jump(long index) throws IOException, NoSuchRecordException {
    if (index < 0L) throw new NoSuchRecordException();
    long i = _index;
    int f = -1;
    while (++f < files.size() && _size[f] != -1L && _size[f] < index);
    if (f >= files.size()) throw new NoSuchRecordException();
    atEnd = false;
    long recoveryTarget = _index;
    if (f != currentFile || index <= _index) {
      reader.close();
      reader = new LCIOReader(files.get(f));
      i = (f == 0) ? -1L : _size[f-1];
    }
    int skipMax = Integer.MAX_VALUE - 10;
    for (long skip = index - i - 1L; skip > 0L; ) {
      int skipNow = skip > skipMax ? skipMax : (int)skip ;
      skip -= skipNow;
      while (true) {
        int skipped = reader.skipEventsChecked(skipNow);
        i += skipped;
        if (skipped == skipNow) {
          break;
        } else {
          skipNow -= skipped;
          reader.close();
          _size[f++] = i;
          if (f < files.size()) {
            reader = new LCIOReader(files.get(f));
          } else {
            reader = new LCIOReader(files.get(0));
            if (_index != -1L) {
              currentFile = 0;
              atEnd = false;
              _index = -1L;
              try {
                jump(recoveryTarget);
              } catch (NoSuchRecordException x) {
                throw new IOException(x);
              }
            }
            throw new NoSuchRecordException();
          }
        }
      }
    }
    _index = i;
    currentFile = f;
    try {
      next();
    } catch (NoSuchRecordException x) {
      try {
        jump(recoveryTarget);
      } catch (NoSuchRecordException xx) {
        throw new IOException(x);
      }
    }
  }
  
  public void shift(long numberOfRecords) throws IOException, NoSuchRecordException {
    jump(_index + numberOfRecords);
  }

  public void rewind() throws IOException {
    currentFile = 0;
    reader.close();
    reader = new LCIOReader(files.get(currentFile));
    atEnd = false;
    _index = -1L;
    currentEvent = null;
  }

  public void close() throws IOException {
    if (reader != null) {
      reader.close();
      reader = null;
    }
    currentEvent = null;
  }

  public void releaseRecord(Object obj) {
    currentEvent = null;
  }

  public void finalize() {
    try {
      close();
    } catch (IOException x) {
    }
  }
}
