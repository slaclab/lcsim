package org.lcsim.util.loop;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import hep.io.stdhep.StdhepEvent;
import hep.io.stdhep.StdhepReader;
import hep.io.stdhep.StdhepRecord;
import hep.physics.particle.properties.ParticlePropertyManager;
import org.freehep.record.source.AbstractRecordSource;
import org.freehep.record.source.NoSuchRecordException;
import org.freehep.record.source.RecordSource;
import org.lcsim.event.EventHeader;
import org.lcsim.event.util.LCSimFactory;


/**
 * {@link RecordSource} implementation that wraps {@link StdhepReader}.
 * 
 * @author tonyj
 * @version $Id: StdhepEventSource.java,v 1.10 2012/07/02 21:50:17 jeremy Exp $
 */
public class StdhepEventSource extends AbstractRecordSource {

  private List<File> files;
  private boolean atEnd;
  private StdhepReader reader;
  private final StdhepConverter converter;
  private EventHeader currentEvent;
  private long currentEventNumber = -1L;
  private int currentFile = 0;

  /**
   * Create an event source from a single stdhep file
   */
  public StdhepEventSource(File file, String detectorName) throws IOException {
    super(file.getName());
    reader = new StdhepReader(file.getAbsolutePath());
    converter = new StdhepConverter(ParticlePropertyManager.getParticlePropertyProvider(), new LCSimFactory(detectorName));
    this.files = Collections.singletonList(file);
  }

  /**
   * Create an event source from a list of stdhep files
   */
  public StdhepEventSource(FileList list, String detectorName) throws IOException {
    super(list.getTitle());
    this.files = list.getFileList();
    if (files.isEmpty()) throw new IOException("File list is empty");
    reader = new StdhepReader(files.get(0).getAbsolutePath());
    converter = new StdhepConverter(ParticlePropertyManager.getParticlePropertyProvider(), new LCSimFactory(detectorName));
  }

  public Object getCurrentRecord() throws IOException {
    if (currentEvent == null) throw new IllegalStateException();
    return currentEvent;
  }

  public long size() {
    if (files.size() == 1) return reader.getNumberOfEvents();
    throw new UnsupportedOperationException();
  }

  public Class<?> getRecordClass() {
    return EventHeader.class;
  }

  public boolean supportsNext() {
    return true;
  }

  public boolean hasNext() {
    return !atEnd;
  }

  public void next() throws IOException, NoSuchRecordException {
    for (;;) {
      try {
        for (;;) {
          StdhepRecord record = reader.nextRecord();
          if (record instanceof StdhepEvent) {
            currentEvent = (EventHeader) converter.convert((StdhepEvent) record);
            //currentEvent.put("INPUT_FILE", files.get(currentFile));
            currentEventNumber++;
            break;
          }
        }
      } catch (EOFException x) {
        currentFile++;
        if (currentFile >= files.size()) {
          atEnd = true;
          throw new NoSuchRecordException();
        } else {
          reader.close();
          reader = new StdhepReader(files.get(currentFile).getAbsolutePath());
          continue;
        }
      }
      return;
    }
  }

  public void releaseRecord(Object obj) {
    currentEvent = null;
  }

  public boolean supportsRewind() {
    return true;
  }

  public boolean hasRewind() {
    return currentEventNumber != -1L;
  }

  public void rewind() throws IOException {
    if (currentFile == 0) {
      reader.rewind();
    } else {
      currentFile = 0;
      reader.close();
      reader = new StdhepReader(files.get(0).getAbsolutePath());
    }
    atEnd = false;
    currentEventNumber = -1L;
    currentEvent = null;
  }

  public void close() throws IOException {
    if (reader != null) {
      reader.close();
      reader = null;
    }
    currentEvent = null;
  }

  public void finalize() {
    try {
      close();
    } catch (IOException x) {
    }
  }

  public boolean supportsIndex() {
    return true;
  }

  public boolean hasIndex(long index) {
    try {
      return index < size();
    } catch (UnsupportedOperationException x) {
      return index >= 0L;
    }
  }

  public void jump(long index) throws IOException, NoSuchRecordException {
    if (index < 0L) throw new NoSuchRecordException();
    if (index == currentEventNumber) return;
    if (index < currentEventNumber) rewind();
    try {
      for (;;) {
        try {
          for (;;) {
            StdhepRecord record = reader.nextRecord();
            if (record instanceof StdhepEvent) {
              currentEventNumber++;
              if (currentEventNumber == index) {
                currentEvent = (EventHeader) converter.convert((StdhepEvent) record);
                //currentEvent.put("INPUT_FILE", files.get(currentFile));
                return;
              }
            }
          }
        } catch (EOFException x) {
          currentFile++;
          if (currentFile >= files.size()) {
            atEnd = true;
            throw new NoSuchRecordException(x);
          } else {
            reader.close();
            reader = new StdhepReader(files.get(currentFile).getAbsolutePath());
            continue;
          }
        }
      }
    } catch (IOException x) {
      throw new NoSuchRecordException(x);
    }
  }

  public long getCurrentIndex() {
    return currentEventNumber;
  }
  
}
