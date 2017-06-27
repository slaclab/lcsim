package org.lcsim.job;

import org.jdom.Element;
import org.lcsim.util.xml.JDOMExpressionFactory;

/**
 * Interface for converting from XML to typed objects for input to LCSim Drivers.
 * 
 * @author jeremym
 */
public interface IParameterConverter {

    /**
     * This method returns true if the converter can handle the given type.
     * 
     * @param propertyType The class of the parameter.
     * @return True if converter handles the given type; False if no.
     */
    public boolean handles(Class propertyType);

    /**
     * Convert an XML element parameter to a specific type and return as an Object.
     * 
     * @param factory The expression factory to be used for variable evaluation.
     * @param parameterElement The XML parameter data.
     * @return Parameter converted to specific type. Returned as generic object.
     */
    public Object convert(JDOMExpressionFactory factory, Element parameterElement);
}
