package org.lcsim.detector.converter.compact;

import java.io.IOException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.lcsim.detector.IPhysicalVolume;
import org.lcsim.geometry.Detector;

public interface IDetectorConverter
{
    public IPhysicalVolume convert( Detector detector, Document doc ) throws JDOMException, IOException;
}
