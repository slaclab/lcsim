package org.lcsim.geometry.compact.converter.lcdd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.lcsim.geometry.compact.converter.SiTrackerModuleComponentParameters;
import org.lcsim.geometry.compact.converter.SiTrackerModuleParameters;
import org.lcsim.geometry.compact.converter.lcdd.util.Box;
import org.lcsim.geometry.compact.converter.lcdd.util.LCDD;
import org.lcsim.geometry.compact.converter.lcdd.util.Material;
import org.lcsim.geometry.compact.converter.lcdd.util.PhysVol;
import org.lcsim.geometry.compact.converter.lcdd.util.Position;
import org.lcsim.geometry.compact.converter.lcdd.util.Rotation;
import org.lcsim.geometry.compact.converter.lcdd.util.SensitiveDetector;
import org.lcsim.geometry.compact.converter.lcdd.util.Trap;
import org.lcsim.geometry.compact.converter.lcdd.util.Trapezoid;
import org.lcsim.geometry.compact.converter.lcdd.util.Tube;
import org.lcsim.geometry.compact.converter.lcdd.util.VisAttributes;
import org.lcsim.geometry.compact.converter.lcdd.util.Volume;

public class SiTrackerEndcap extends LCDDSubdetector
{
    Map<String,SiTrackerModuleParameters> moduleParameters = new HashMap<String,SiTrackerModuleParameters>();
    SensitiveDetector sd = null;
    
    public SiTrackerEndcap(Element node) throws JDOMException
    {
        super(node);
    }
    
    public void addToLCDD(LCDD lcdd, SensitiveDetector sens) throws JDOMException
    {
        if (sens == null)
            throw new RuntimeException("SD is null");
        
        this.sd = sens;
        
        for (Object n : node.getChildren("module"))
        {
            Element e = (Element)n;
            moduleParameters.put(e.getAttributeValue("name"), new SiTrackerModuleParameters(e));
        }
        
        int sysId = node.getAttribute("id").getIntValue();
        String subdetName = node.getAttributeValue("name");
        
        for (Object o : node.getChildren("layer"))
        {
            Element layerElement = (Element)o;
            
            int nwedges;
            try
            {
                nwedges = layerElement.getAttribute("nwedges").getIntValue();
            }
            catch (DataConversionException x)
            {
                throw new RuntimeException(x);
            }
            
            double innerR, outerR, innerZ, thickness;
            int layerN;
            try
            {
                layerN = layerElement.getAttribute("id").getIntValue();
                innerR = layerElement.getAttribute("inner_r").getDoubleValue();
                outerR = layerElement.getAttribute("outer_r").getDoubleValue();
                innerZ = layerElement.getAttribute("inner_z").getDoubleValue();
                thickness = layerElement.getAttribute("thickness").getDoubleValue();
            }
            catch (DataConversionException x)
            {
                throw new RuntimeException(x);
            }
            
            String layerName = subdetName + "_layer" + layerN;
            
            Volume layerVolume = makeLayer(node, layerElement, layerN, innerR, outerR, thickness, nwedges, lcdd);
            
            lcdd.add(layerVolume);
            
            Rotation rotation = new Rotation(layerName + "_rotation");
            Position position = new Position(layerName + "_position");
            double layerZ = innerZ + thickness/2;
            position.setZ(layerZ);
            
            lcdd.add(rotation);
            lcdd.add(position);
            
            // Positive endcap.
            PhysVol posEC = new PhysVol(layerVolume, lcdd.getTrackingVolume(), position, rotation);
            posEC.addPhysVolID("system", sysId);
            posEC.addPhysVolID("barrel", 1); // Positive endcap flag.
            posEC.addPhysVolID("layer", layerN);
            
            // Negative endcap.
            Rotation rotationReflect = new Rotation(layerName + "_rotation_reflect");
            rotationReflect.setY(Math.PI);
            Position positionReflect = new Position(layerName + "_position_negative");
            positionReflect.setZ(-layerZ);
            lcdd.add(rotationReflect);
            lcdd.add(positionReflect);
            PhysVol negEC = new PhysVol(layerVolume, lcdd.getTrackingVolume(), positionReflect, rotationReflect);
            negEC.addPhysVolID("system", sysId);
            negEC.addPhysVolID("barrel", 2); // Negative endcap flag.
            negEC.addPhysVolID("layer", layerN);
        }
        
        moduleParameters = null;
    }
    
    private Volume makeLayer(
            Element subdetElement,
            Element layerElement,
            int layerN,
            double innerR,
            double outerR,
            double thickness,
            int nwedges,
            LCDD lcdd)
    {
        double dphi = Math.PI / nwedges;
        
        String subdetName = subdetElement.getAttributeValue("name");
        
        String layerName = subdetName + "_layer" + layerN;
        
        double tubeInnerR, tubeOuterR;
        tubeInnerR = innerR;
        tubeOuterR = outerR / Math.cos(dphi);
        
        // FIXME: The z dimension should go in as a half length, but GDML does not use Geant4's convention.
        Tube layerTube = new Tube(layerName + "_tube", tubeInnerR, tubeOuterR, thickness/2);
        
        lcdd.add(layerTube);
        
        Material material;
        try
        {
            material = lcdd.getMaterial("Air");
        }
        catch (JDOMException x)
        {
            throw new RuntimeException(x);
        }
        
        Volume layerLV = new Volume(layerName, layerTube, material);
        
        Volume wedgeLV = makeWedge(subdetElement, layerElement, innerR, outerR, thickness, nwedges, layerN, lcdd);
        lcdd.add(wedgeLV);
        
        // Turn off wedges.  Show daughters.
    	if (lcdd.getVisAttributes("InvisibleWithDaughters") != null)
    	{
    		wedgeLV.setVisAttributes(lcdd.getVisAttributes("InvisibleWithDaughters"));
    	}
        
        double r = (innerR + outerR) / 2;
        String wedgeName = wedgeLV.getVolumeName();
        for (int i=0; i<nwedges; i++)
        {
            double phi = i * 2 * Math.PI / nwedges;
            double x = r * Math.cos(phi);
            double y = r * Math.sin(phi);
            
            Position p = new Position(wedgeName + i + "_position");
            p.setX(x);
            p.setY(y);
            Rotation rot = new Rotation(wedgeName + i + "_rotation");
            rot.setX(-Math.PI/2);
            rot.setY(-Math.PI/2 - phi);
            
            lcdd.add(p);
            lcdd.add(rot);
            
            PhysVol wedgePhysVol = new PhysVol(wedgeLV, layerLV, p, rot);
            wedgePhysVol.addPhysVolID("wedge", i);
        }
        
        // Set layer visualization.
        setVisAttributes(lcdd, layerElement, layerLV);
        
        // Set the layer envelope to invisible to help Geant4 visualization.
        //if (layerLV.getAttribute("vis") == null)
        //{
        //	if (lcdd.getVisAttributes("InvisibleWithDaughters") != null)
        //	{
        //		layerLV.setVisAttributes(lcdd.getVisAttributes("InvisibleWithDaughters"));
        //	}
        //}
        //else
        //{
        //	
        //}
        
        return layerLV;
    }
    
    Volume makeWedge(Element subdetElement, Element layerElement, double innerR, double outerR, double thickness, int nwedges, int layerN, LCDD lcdd)
    {
        Material material;
        try
        {
            material = lcdd.getMaterial("Air");
        }
        catch (JDOMException x)
        {
            throw new RuntimeException(x);
        }
        
        String subdetName = subdetElement.getAttributeValue("name");
        String name = subdetName + "_layer" + layerN + "_wedge";
        
        double dz = (outerR - innerR) / 2;
        double dy1, dy2;
        dy1 = dy2 = thickness / 2;
        double dx1, dx2;
        double dphi = Math.PI / nwedges;
        dx1 = innerR * Math.tan(dphi);
        dx2 = outerR * Math.tan(dphi);
        
        // pull corners in by 0.1 microns to eliminate overlaps.
        dx1 -= 0.0001;
        dx2 -= 0.0001;
        dz -= 0.0001;
        
        Trapezoid wedgeTrd = new Trapezoid(name + "_trapezoid",dx1,dx2,dy1,dy2,dz);
        
        lcdd.add(wedgeTrd);
        
        Volume wedgeLV = new Volume(name, wedgeTrd, material);
        //lcdd.add(wedgeLV);
        
        Attribute moduleref = layerElement.getAttribute("module");
        
        if (moduleref == null)
            throw new RuntimeException("module reference is missing for layer number " + layerN);
        
        SiTrackerModuleParameters module = moduleParameters.get(moduleref.getValue());
        
        makeModules(subdetElement, wedgeLV, layerElement.getChild("module_parameters"), module, layerN, lcdd);
        
        return wedgeLV;
    }
    
    private void makeModules(Element subdetElement, Volume wedgeLV, Element moduleParameters, SiTrackerModuleParameters module, int layerN, LCDD lcdd)
    {
        double r_size;
        try
        {
            r_size = moduleParameters.getAttribute("r_size").getDoubleValue();
        }
        catch (DataConversionException x)
        {
            throw new RuntimeException(x);
        }
        
        double phi_size_max;
        try
        {
            phi_size_max = moduleParameters.getAttribute("phi_size_max").getDoubleValue();
        }
        catch (DataConversionException x)
        {
            throw new RuntimeException(x);
        }
        
        VisAttributes vis = null;
        if (module.getVis() != null)
        {
        	vis = lcdd.getVisAttributes(module.getVis());
        }
        
        Trapezoid wedgeTrd = (Trapezoid)lcdd.getSolid(wedgeLV.getSolidRef());
        double dz = wedgeTrd.z();
        double dx1 = wedgeTrd.x1();
        double dx2 = wedgeTrd.x2();
        double dy = wedgeTrd.y1();
        double deltax = dx2 - dx1;
        double side_slope = deltax / (2*dz);
        
        List<Double> zcenters = new ArrayList<Double>();
        List<Double> zsizes = new ArrayList<Double>();
        List<Double> xsizes1 = new ArrayList<Double>();
        List<Double> xsizes2 = new ArrayList<Double>();
        
        {
            double zcurr = dz;
            while (zcurr - r_size > -dz)
            {
                double zmax = zcurr;
                double zmin = zcurr-r_size;
                zcenters.add((zmin+zmax)/2);
                zsizes.add((zmax-zmin)/2);
                
                double xsize1 = dx1 + side_slope*(zmin+dz);
                double xsize2 = dx1 + side_slope*(zmax+dz);
                
                xsizes1.add(xsize1);
                xsizes2.add(xsize2);
                
                zcurr -= r_size;
            }
            double zmax = zcurr;
            double zmin = -dz;
            zcenters.add((zmin+zmax)/2);
            zsizes.add((zmax-zmin)/2);
            double xsize1 = dx1 + side_slope*(zmin+dz);
            double xsize2 = dx1 + side_slope*(zmax+dz);
            xsizes1.add(xsize1);
            xsizes2.add(xsize2);
        }
        
        Material moduleMaterial;
        try
        {
            moduleMaterial = lcdd.getMaterial("Air");
        }
        catch (JDOMException x)
        {
            throw new RuntimeException(x);
        }
        
        double zsize_last = 0.0;
        double xsize1_min = phi_size_max/2;
        double xsize_box = 0.0;
        int nboxes = 0;
        
        int imodule = 0;
        
        for (int i=zcenters.size()-1; i >= 0; i--)
        {
            
            if (zsizes.get(i) != zsize_last)
            {
                zsize_last = zsizes.get(i);
                xsize1_min = phi_size_max/2;
                xsize_box = 0.0;
                nboxes = 0;
            }
            
            int ntraps = (int)Math.ceil(  2*(xsizes1.get(i) - nboxes*xsize_box) / phi_size_max );
            
            // Squares to fill extra space
            if (ntraps > 2)
            {
                double delta_x = xsizes2.get(i) - xsizes1.get(i);

                if (phi_size_max > delta_x)
                {
                    xsize_box = delta_x*(int)Math.floor(phi_size_max/delta_x);
                }
                else
                {
                    xsize_box = delta_x/(int)Math.floor(delta_x/phi_size_max);
                }

                if (xsize_box > 0.0)
                {
                    nboxes = (int)Math.floor((xsizes1.get(i)-2*xsize1_min)/xsize_box);
                }
                ntraps = 2;
            }
            
            double xmin = -nboxes*xsize_box;
            double xmax = xmin+2*xsize_box;
            
            for (int ibox = 0; ibox < nboxes; ibox++)
            {
                double xcenter = (xmin+xmax)/2;
                xmin += 2*xsize_box;
                xmax += 2*xsize_box;
                
                String moduleName = subdetElement.getAttributeValue("name") + "_layer" + layerN + "_module" + imodule;
                
                // FIXME: Multiply by 2 to conform to GDML convention of dividing inputs by 2.
                Box sliceBox = new Box(moduleName + "_box");
                sliceBox.setX(xsize_box*2);
                sliceBox.setY(dy*2);
                sliceBox.setZ(zsizes.get(i)*2);
                lcdd.add(sliceBox);
                
                Volume moduleLV = new Volume(moduleName, sliceBox, moduleMaterial);
                
                // Make the box module.
                makeBoxModule(moduleLV, module, lcdd);
                
                moduleLV.setVisAttributes(vis);
                
                lcdd.add(moduleLV);
                
                Position p = new Position(moduleName + "_position");
                p.setX(xcenter);
                p.setZ(zcenters.get(i));
                lcdd.add(p);
                Rotation rot = new Rotation(moduleName + "_rotation");
                lcdd.add(rot);
                
                PhysVol modulePV = new PhysVol(moduleLV, wedgeLV, p, rot);
                modulePV.addPhysVolID("module", imodule);
                
                imodule++;
            }
            
            // Small symmetric trapezoids
            if (ntraps == 1)
            {
                String moduleName = subdetElement.getAttributeValue("name") + "_layer" + layerN + "_module" + imodule;
                
                Trapezoid moduleTrd = new Trapezoid(moduleName+"_trapezoid", xsizes1.get(i), xsizes2.get(i), dy, dy, zsizes.get(i));
                lcdd.add(moduleTrd);
                
                Volume moduleLV = new Volume(moduleName, moduleTrd, moduleMaterial);
                
                makeTrdModule(moduleLV, module, lcdd);
                
                moduleLV.setVisAttributes(vis);
                
                lcdd.add(moduleLV);
                
                Position p = new Position(moduleName + "_position");
                p.setZ(zcenters.get(i));
                lcdd.add(p);
                
                Rotation rot = new Rotation(moduleName + "_rotation");
                lcdd.add(rot);
                
                PhysVol slicePV = new PhysVol(moduleLV, wedgeLV, p, rot);
                slicePV.addPhysVolID("module", imodule);
                
                imodule++;
            }
            
            // Split trapezoids
            if (ntraps == 2)
            {
                
                double xoffset = xsize_box*nboxes;
                
                double xsize1 = (xsizes1.get(i)-xoffset)/ntraps;
                if (xsize1_min == 0.0) xsize1_min = xsize1;
                double xsize2 = (xsizes2.get(i)-xoffset)/ntraps;
                
                double xcenter = (xsize1+xsize2)/2 + xoffset;
                double theta = Math.abs(Math.atan(side_slope/2));
                
                for (int ix = -1; ix <=1; ix=ix+2)
                {
                    String moduleName = subdetElement.getAttributeValue("name") + "_layer" + layerN + "_module" + imodule;
                    
                    Trap moduleTrap = new Trap(moduleName+"_trap",zsizes.get(i),theta*ix,0.0,dy,xsize1,xsize1,0.0,dy,xsize2,xsize2,0.0);
                    lcdd.add(moduleTrap);
                    
                    Volume moduleLV = new Volume(moduleName, moduleTrap, moduleMaterial);
                    
                    makeTrapModule(moduleLV, module, lcdd);
                    
                    moduleLV.setVisAttributes(vis);
                    
                    lcdd.add(moduleLV);
                    
                    Position p = new Position(moduleName + "_position");
                    p.setX(ix*xcenter);
                    p.setZ(zcenters.get(i));
                    lcdd.add(p);
                    Rotation rot = new Rotation(moduleName + "_rotation");
                    lcdd.add(rot);
                    
                    PhysVol modulePV = new PhysVol(moduleLV, wedgeLV, p, rot);
                    modulePV.addPhysVolID("module", imodule);
                    
                    imodule++;
                }
            }
        }
    }
    
    void makeBoxModule(Volume moduleVolume, SiTrackerModuleParameters moduleParameters, LCDD lcdd)
    {
        if (moduleParameters == null)
        {
            throw new IllegalArgumentException("moduleParameters is null");
        }

        Box moduleBox = (Box)lcdd.getSolid(moduleVolume.getSolidRef());
        
        double moduleX = moduleBox.getX();
        double posY = -(moduleBox.getY() / 2);
        double moduleZ = moduleBox.getZ();
        
        // pull corners in by 0.5 microns to eliminate overlaps.
        moduleX -= 0.0005;
        moduleZ -= 0.0005;
        
        String moduleName = moduleVolume.getVolumeName();
        
        int sensor=0;
        
        VisAttributes vis = null;
        if (moduleParameters.getVis() != null)
        	vis = lcdd.getVisAttributes(moduleParameters.getVis());        
        
        for (SiTrackerModuleComponentParameters component : moduleParameters)
        {
            double thickness = component.getThickness();
            Material material = null;
            try
            {
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
            
            Box sliceBox = new Box(componentName + "_box", moduleX, thickness, moduleZ);
            lcdd.add(sliceBox);
            
            Volume volume = new Volume(componentName, sliceBox, material);
            volume.setVisAttributes(vis);
            lcdd.add(volume);
            
            Position position = new Position(componentName + "_position", 0., posY, 0.);
            lcdd.add(position);
            Rotation rotation = new Rotation(componentName + "_rotation");
            rotation.setY(Math.PI);
            lcdd.add(rotation);
            PhysVol pv = new PhysVol(volume, moduleVolume, position, rotation);
            pv.addPhysVolID("component", componentNumber);
            
            if (sensitive)
            {
                if (sensor > 1)
                {
                    throw new RuntimeException("Maximum of 2 sensors per module.");
                }
                volume.setSensitiveDetector(this.sd);
                pv.addPhysVolID("sensor", sensor);
                ++sensor;
            }
            
            volume.setVisAttributes(vis);
         
            posY += thickness / 2;
        }
    }
    
    void makeTrdModule(Volume moduleVolume, SiTrackerModuleParameters moduleParameters, LCDD lcdd)
    {
        Trapezoid trd = (Trapezoid)lcdd.getSolid(moduleVolume.getSolidRef());
        
        double x1 = trd.x1();
        double x2 = trd.x2();
        double y1 = trd.y1();
        double z = trd.z();
        
        // pull corners in by 0.5 microns to eliminate overlaps.
        x1 -= 0.0005;
        x2 -= 0.0005;
        z -= 0.0005;
        
        double posY = -y1;
        
        String moduleName = moduleVolume.getVolumeName();
        
        int sensor=0;
        for (SiTrackerModuleComponentParameters component : moduleParameters)
        {
            double thickness = component.getThickness();
            
            Material material = null;
            try
            {
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
                volume.setSensitiveDetector(this.sd);
                ++sensor;
            }
            
            // Set vis attributes of module component.
            if (component.getVis() != null)
            	volume.setVisAttributes(lcdd.getVisAttributes(component.getVis()));
            
            posY += thickness / 2;
        }
    }
    
    public void makeTrapModule(Volume module, SiTrackerModuleParameters moduleParameters, LCDD lcdd)
    {
        Trap trap = (Trap)lcdd.getSolid(module.getSolidRef());
                
        double a1 = trap.getAlpha1();
        double a2 = trap.getAlpha2();
        double x1 = trap.getXHalfLength1();
        double x2 = trap.getXHalfLength2();
        double x3 = trap.getXHalfLength3();
        double x4 = trap.getXHalfLength4();
        double y1 = trap.getYHalfLength1();
        double z = trap.getZHalfLength();
        double theta = trap.getTheta();
        double phi = trap.getPhi();
        
        // pull corners in by 0.5 microns to eliminate overlaps.
        x1 -= 0.0005;
        x2 -= 0.0005;
        x3 -= 0.0005;
        x4 -= 0.0005;
        z -= 0.0005;
        
        double posY = -y1;
        
        int sensor = 0;
        
        for (SiTrackerModuleComponentParameters component : moduleParameters)
        {
            double thickness = component.getThickness();
            Material material = null;
            try
            {
                material = lcdd.getMaterial(component.getMaterialName());
            }
            catch (JDOMException x)
            {
                throw new RuntimeException(x);
            }

            int componentNumber = component.getComponentNumber();
            
            posY += thickness / 2;
            
            String componentName = module.getVolumeName() + "_component" + componentNumber;
            
            Trap sliceTrap = new Trap(componentName + "_trap", z, theta, phi, thickness/2, x1, x2, a1, thickness/2, x3, x4, a2);
            lcdd.add(sliceTrap);
            
            Volume volume = new Volume(componentName, sliceTrap, material);
            lcdd.add(volume);
            
            Position position = new Position(componentName + "_position",0,posY,0);
            lcdd.add(position);
            Rotation rotation = new Rotation(componentName + "_rotation");
            lcdd.add(rotation);
            
            PhysVol pv = new PhysVol(volume, module, position, rotation);
            pv.addPhysVolID("component", componentNumber);
            
            // Set component sensitive detector.
            if (component.isSensitive())
            {
                if (sensor > 1)
                {
                    throw new RuntimeException("Maximum of 2 sensors per module.");
                }
                //System.out.println(volume.getVolumeName() + " - " + this.sd.getRefName());
                volume.setSensitiveDetector(this.sd);
                pv.addPhysVolID("sensor", sensor);
                ++sensor;
            }
            
            // Set vis attributes of module component.
            if (component.getVis() != null)
            	volume.setVisAttributes(lcdd.getVisAttributes(component.getVis()));
                                    
            posY += thickness / 2;
        }
    }
    
    public boolean isTracker()
    {
        return true;
    }
}
