package org.lcsim.geometry.compact.converter.lcdd;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;

public class FieldMap3D extends LCDDField 
{
	private Element node;
	
    public FieldMap3D(Element node)
    {
        super(node);
        this.node = node;
    }
	
	void addToLCDD(LCDD lcdd) throws JDOMException 
	{
		double xoffset, yoffset, zoffset;
		xoffset = yoffset = zoffset = 0;
                String filename = node.getAttribute("filename").getValue();
                String file=filename;
                if(filename.startsWith("http"))
                {
                   int index = filename.lastIndexOf("/");
                   file = "fieldmap/"+filename.substring(index + 1);
                }
		if (node.getAttribute("xoffset") != null)
			xoffset = node.getAttribute("xoffset").getDoubleValue();
		if (node.getAttribute("yoffset") != null)
			yoffset = node.getAttribute("yoffset").getDoubleValue();
		if (node.getAttribute("zoffset") != null)
			zoffset = node.getAttribute("zoffset").getDoubleValue();
		org.lcsim.geometry.compact.converter.lcdd.util.FieldMap3D fieldMap = 
				new org.lcsim.geometry.compact.converter.lcdd.util.FieldMap3D(
						node.getAttribute("name").getValue(),
//						node.getAttribute("filename").getValue(),
                                                file,
						xoffset,
						yoffset,
						zoffset);
		lcdd.add(fieldMap);
	}

}
