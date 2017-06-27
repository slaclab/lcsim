package org.lcsim.detector.converter.lcdd;

import static org.lcsim.units.clhep.SystemOfUnits.cm3;
import static org.lcsim.units.clhep.SystemOfUnits.g;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.detector.converter.XMLConverter;
import org.lcsim.detector.material.IMaterial;
import org.lcsim.detector.material.MaterialElement;
import org.lcsim.detector.material.MaterialMixture;
import org.lcsim.detector.material.MaterialStore;

/**
 * 
 * This converter takes a GDML element and converts
 * it to a MaterialMixture.
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class MaterialMixtureConverter 
implements XMLConverter
{	
	private static final String compositeStr = "composite";
	private static final String fractionStr = "fraction";
	
	public void convert(Element element) throws JDOMException
	{
		if ( element.getName().equals("material"))
		{
			String name = element.getAttributeValue("name");
						
			if ( element.getChild("D") != null)
			{
				Element D = element.getChild("D");
				//double unit = g / cm3;
				//if ( D.getAttribute("unit") != null)
				//{
				//	unit = D.getAttribute("unit").getDoubleValue();
				//}
				
				if ( D.getAttribute("value") != null )
				{
					double density = D.getAttribute("value").getDoubleValue();
					//density *= unit;
														
					String defType = "";
					
					boolean hasComposite = ( element.getChild("composite") != null);
					boolean hasFraction = ( element.getChild("fraction") != null);
					
					if ( hasComposite && hasFraction )
					{
						throw new JDOMException("The material <"+name+"> has both <composite> and <fraction> components, which is not allowed!");
					}
					
					if ( hasComposite )
					{
						
						defType = compositeStr;
					}
					else if ( hasFraction )
					{
						defType = fractionStr;
					}
					else {
						throw new JDOMException("MaterialMixture <" + name + "> is missing at least one <composite> or <fraction> component.");
					}
					
					int ncomponents = element.getChildren(defType).size();
					
					MaterialMixture material = 
						new MaterialMixture(
				    		name,
				            ncomponents,
				            density,
				            IMaterial.Unknown
				            );				   						
					
					// Add by number of atoms.
					if ( hasComposite )
					{
						for ( Object obj : element.getChildren("composite"))
						{
							Element composite = (Element)obj;
							IMaterial matlkp = 
								MaterialStore.getInstance().get(composite.getAttributeValue("ref"));
                                                        
							if ( matlkp == null )
							{
								throw new JDOMException("The material <" + composite.getAttributeValue("ref") + "> was not found!");
							}
							
							int n = composite.getAttribute("n").getIntValue();
							
							if ( matlkp instanceof MaterialElement )
							{
								material.addElement((MaterialElement)matlkp, n);
							}
							else if ( matlkp instanceof MaterialMixture )
							{
								material.addMaterial((MaterialMixture)matlkp, n);
							}							
						}
					}  
					// Add by mass fraction.
					// Already checked for neither or both.
					else {
						for ( Object obj : element.getChildren("fraction"))
						{
							Element fraction = (Element)obj;
                            
							IMaterial matlkp =
								MaterialStore.getInstance().get(fraction.getAttributeValue("ref"));                                                      
                                                                                    
							if ( matlkp == null )
							{
								throw new JDOMException("The material <" + fraction.getAttributeValue("ref") + "> was not found!");
							}
							
							double f = fraction.getAttribute("n").getDoubleValue();
							
							if ( matlkp instanceof MaterialElement )
							{
								material.addElement((MaterialElement)matlkp, f);
							}
							else if ( matlkp instanceof MaterialMixture )
							{
								material.addElement((MaterialMixture)matlkp, f);
							}
						}
					}
				}
				else {
					throw new JDOMException("The material <" + name + " is missing a density value.");
				}
				
			}
			else {
				throw new JDOMException("The material <" + name + "> is missing <D>.");
			}					
		}
		else {
			throw new JDOMException("Invalid element <" + element.getName() + "> for MaterialMixtureConverter.");
		}
	}	
}

/*

--Examples--

<material name="Air">
<D type="density" unit="g/cm3" value="0.0012"/>
<fraction n="0.754" ref="N"/>
<fraction n="0.234" ref="O"/>
<fraction n="0.012" ref="Ar"/>
</material>

<material name="Actinium">
<D type="density" unit="g/cm3" value="10.07" />
<composite n="1" ref="Ac" />
</material>

*/