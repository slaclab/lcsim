package org.lcsim.util.heprep;

import hep.graphics.heprep.HepRepFactory;
import hep.graphics.heprep.HepRepInstanceTree;
import hep.graphics.heprep.HepRepTypeTree;
import java.util.List;
import org.lcsim.event.EventHeader;

/**
 *
 * @author tonyj
 */
public interface HepRepCollectionConverter
{
   public boolean canHandle(Class k);
   public void convert(EventHeader event, List collection, HepRepFactory factory, HepRepTypeTree typeTree, HepRepInstanceTree instanceTree);
}
