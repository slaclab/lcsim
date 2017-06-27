package org.lcsim.geometry.compact.converter.lcdd;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.SiTrackerModuleComponentParameters;
import org.lcsim.geometry.compact.converter.SiTrackerModuleParameters;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.Material;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.Position;
import org.lcsim.geometry.compact.converter.lcdd.util.Rotation;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Trapezoid;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;

/**
 * An LCDD converter for a Silicon endcap tracker model based on Bill Cooper's design from
 * <a href="http://ilcagenda.linearcollider.org/materialDisplay.py?contribId=58&sessionId=1&materialId=slides&confId=2784">Boulder SiD Workshop 2008</a>.
 * 
 * @author jeremym 
 */
public class SiTrackerEndcap2 extends LCDDSubdetector 
{
    Map<String,SiTrackerModuleParameters> moduleParameters = new HashMap<String,SiTrackerModuleParameters>();
    Map<String,Volume> modules = new HashMap<String,Volume>();
    Material vacuum;
    
    public SiTrackerEndcap2(Element node) throws JDOMException
    {
        super(node);
    }	

	void addToLCDD(LCDD lcdd, SensitiveDetector sd) throws JDOMException 
	{
        int sysId = node.getAttribute("id").getIntValue();
        String subdetName = node.getAttributeValue("name");
        vacuum = lcdd.getMaterial("Vacuum");
        boolean reflect;
        if (node.getAttribute("reflect") != null)
            reflect = node.getAttribute("reflect").getBooleanValue();
        else
            reflect = true;
        
        for (Iterator i = node.getChildren("module").iterator(); i.hasNext();)
        {
            Element module = (Element)i.next();
            String moduleName = module.getAttributeValue("name");
            moduleParameters.put(moduleName, new SiTrackerModuleParameters(module));
            modules.put(moduleName, makeModule(moduleParameters.get(moduleName), sd, lcdd));
        }		              
        
        for (Iterator i = node.getChildren("layer").iterator(); i.hasNext();)            
        {
            Element layerElement = (Element) i.next();
            int layerId = layerElement.getAttribute("id").getIntValue();
            int ringCount = 0;
            int moduleNumber = 0;
            for (Iterator j = layerElement.getChildren("ring").iterator(); j.hasNext();)
            {
                Element ringElement = (Element) j.next();
                double r = ringElement.getAttribute("r").getDoubleValue();
                double phi0=0;
                if (ringElement.getAttribute("phi0") != null)
                {
                    phi0 = ringElement.getAttribute("phi0").getDoubleValue();
                }
                double zstart = ringElement.getAttribute("zstart").getDoubleValue();
                double dz=0;
                if (ringElement.getAttribute("dz") != null)
                {
                    dz = Math.abs(ringElement.getAttribute("dz").getDoubleValue());
                }
                int nmodules = ringElement.getAttribute("nmodules").getIntValue();
                String module = ringElement.getAttributeValue("module");
                Volume moduleVolume = modules.get(module);
                if (moduleVolume == null)
                    throw new RuntimeException("Module " + module + " was not found.");
                double iphi = (2 * Math.PI) / nmodules;
                double phi = phi0;
                for (int k = 0; k < nmodules; k++)
                {                    
                    String moduleBaseName = subdetName + "_layer" + layerId + "_module" + moduleNumber;
                    
                    double x = r * Math.cos(phi);
                    double y = r * Math.sin(phi);
                    
                    Position p = new Position(moduleBaseName + "_position");
                    p.setX(x);
                    p.setY(y);                        
                    p.setZ(zstart + dz);                    
                    Rotation rot = new Rotation(moduleBaseName + "_rotation");
                    rot.setX(-Math.PI/2);
                    rot.setY(-Math.PI/2 - phi);                    

                    lcdd.add(p);
                    lcdd.add(rot);
                    
                    PhysVol pv = new PhysVol(moduleVolume, lcdd.getTrackingVolume(), p, rot);
                    pv.addPhysVolID("system", sysId);
                    pv.addPhysVolID("barrel", 1); // positive endcap
                    pv.addPhysVolID("layer", layerId);
                    pv.addPhysVolID("module", moduleNumber);
                    
                    if (reflect)
                    {
                        Position pr = new Position(moduleBaseName + "_reflect_position");
                        pr.setX(x);
                        pr.setY(y);                        
                        pr.setZ(-zstart - dz);                    
                        Rotation rotr = new Rotation(moduleBaseName + "_reflect_rotation");
                        rotr.setX(-Math.PI/2);
                        rotr.setY(-Math.PI/2 - phi);
                        rotr.setZ(Math.PI);

                        lcdd.add(pr);
                        lcdd.add(rotr);
                        
                        PhysVol pvr = new PhysVol(moduleVolume, lcdd.getTrackingVolume(), pr, rotr);
                        pvr.addPhysVolID("system", sysId);
                        pvr.addPhysVolID("barrel", 2);
                        pvr.addPhysVolID("layer", layerId);
                        pvr.addPhysVolID("module", moduleNumber);                       
                    }
                    
                    dz = -dz;                    
                    phi += iphi;
                    ++ringCount;
                    ++moduleNumber;
                }
            }
        }                
	}
	
	private Volume makeModule(SiTrackerModuleParameters params, SensitiveDetector sd, LCDD lcdd)
	{
	    double thickness = params.getThickness();
	    double dx1, dx2, dy1, dy2, dz;
	    dy1 = dy2 = thickness / 2;
	    dx1 = params.getDimension(0);
	    dx2 = params.getDimension(1);
	    dz = params.getDimension(2);
	    Trapezoid envelope = new Trapezoid(params.getName() + "Trd", dx1, dx2, dy1, dy2, dz);
	    lcdd.add(envelope);
	    Volume volume = new Volume(params.getName() + "Volume", envelope, vacuum);
	    makeModuleComponents(volume, params, sd, lcdd);
	    if (params.getVis() != null)
	    {
	    	volume.setVisAttributes(lcdd.getVisAttributes(params.getVis()));
	    }
	    lcdd.add(volume);
	    return volume;
	}
	
	private void makeModuleComponents(Volume moduleVolume, SiTrackerModuleParameters moduleParameters, SensitiveDetector sd, LCDD lcdd)
    {
        Trapezoid trd = (Trapezoid)lcdd.getSolid(moduleVolume.getSolidRef());

        double x1 = trd.x1();
        double x2 = trd.x2();
        double y1 = trd.y1();
        double z = trd.z();

        double posY = -y1;

        String moduleName = moduleVolume.getVolumeName();

        int sensor=0;
        for (SiTrackerModuleComponentParameters component : moduleParameters)
        {
            double thickness = component.getThickness();

            Material material = null;
            try {
                material = lcdd.getMaterial(component.getMaterialName());
            }
            catch (JDOMException x)
            {
                throw new RuntimeException(x);
            }
            boolean sensitive = component.isSensitive();
            int componentNumber = component.getComponentNumber();            

            posY += thickness / 2;

            String componentName = moduleName + "_component" + componentNumber;

            Trapezoid sliceTrd = new Trapezoid(componentName + "_trd", x1, x2, thickness/2, thickness/2, z);
            lcdd.add(sliceTrd);

            Volume volume = new Volume(componentName, sliceTrd, material);
            lcdd.add(volume);

            Position position = new Position(componentName + "_position",0.,posY,0);
            lcdd.add(position);
            Rotation rotation = new Rotation(componentName + "_rotation");
            lcdd.add(rotation);

            PhysVol pv = new PhysVol(volume, moduleVolume, position, rotation);
            pv.addPhysVolID("component", componentNumber);

            if (sensitive)
            {
                if (sensor > 1)
                {
                    throw new RuntimeException("Maximum of 2 sensors per module.");
                }
                pv.addPhysVolID("sensor", sensor);
                volume.setSensitiveDetector(sd);
                ++sensor;
            }
            
            if (component.getVis() != null)
    	    {
    	    	volume.setVisAttributes(lcdd.getVisAttributes(component.getVis()));
    	    }

            posY += thickness / 2;
        }   
    }	
	
    public boolean isTracker()
    {
        return true;
    }	
}
