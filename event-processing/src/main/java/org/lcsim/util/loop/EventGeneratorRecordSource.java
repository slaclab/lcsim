package org.lcsim.util.loop;

import java.io.IOException;

import hep.physics.event.HEPEvent;
import hep.physics.event.generator.EventGenerator;
import org.freehep.record.source.AbstractRecordSource;
import org.freehep.record.source.NoSuchRecordException;

/**
 *
 * @author Tony Johnson
 */
public class EventGeneratorRecordSource extends AbstractRecordSource {

  private EventGenerator generator;
  private HEPEvent current;

  public EventGeneratorRecordSource(EventGenerator generator, String name) {
    super(name);
    this.generator = generator;
  }
  
  public boolean supportsRewind() {
    return true;
  }

  public boolean hasRewind() {
    return current != null;
  }

  public void rewind() throws IOException {
    generator.reset();
    current = null;
  }

  public Class<?> getRecordClass() {
    return HEPEvent.class;
  }

  public Object getCurrentRecord() throws IOException {
    if (current == null) throw new IllegalStateException();
    return current;
  }

  public boolean supportsNext() {
    return true;
  }

  public void next() throws IOException, NoSuchRecordException {
    current = generator.generate();
    if (current == null) throw new NoSuchRecordException();
  }

  EventGenerator getGenerator() {
    return generator;
  }
}