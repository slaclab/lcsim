package org.lcsim.detector.converter.lcdd;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

public class MaterialsConverter {

	public void convert(Document doc) throws JDOMException
	{	
		MaterialElementConverter elemCnv = new MaterialElementConverter();
		MaterialMixtureConverter matCnv = new MaterialMixtureConverter();
		
		Element root = doc.getRootElement().getChild("materials");
		for ( Object obj : root.getChildren() ) 
		{
			Element child = (Element)obj;
			if ( child.getName().equals("element") ) {
				elemCnv.convert(child);
			}
			else if ( child.getName().equals("material"))
			{
				matCnv.convert(child);
			}
		}
	}
}
