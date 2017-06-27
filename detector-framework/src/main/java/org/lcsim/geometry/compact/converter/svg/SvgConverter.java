package org.lcsim.geometry.compact.converter.svg;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;
import org.lcsim.detector.IDetectorElement;
import org.lcsim.detector.solids.Tube;
import org.lcsim.geometry.Calorimeter;
import org.lcsim.geometry.Calorimeter.CalorimeterType;
import org.lcsim.geometry.Tracker;
import org.lcsim.geometry.compact.Detector;
import org.lcsim.geometry.compact.Subdetector;
import org.lcsim.geometry.compact.VisAttributes;
import org.lcsim.geometry.subdetector.DiskTracker;
import org.lcsim.geometry.subdetector.MultiLayerTracker;
import org.lcsim.geometry.subdetector.PolyconeSupport;
import org.lcsim.geometry.subdetector.SiTrackerBarrel;
import org.lcsim.geometry.subdetector.TubeSegment;
import org.lcsim.geometry.subdetector.PolyconeSupport.ZPlane;

/**
 * Convert from a compact detector description to an SVG view. This will only work correctly for full ILC physics
 * detectors.
 * 
 * @author jeremym
 */
// TODO Fix manual scaling where possible (primarily in text and associated lines).
// TODO Add groups for label lines so user units (from Detector) can be used instead of scaling.
// TODO For Calorimeters that are large enough (Hcal, Muon) add layering instead of fill.
// TODO Add lefthand labels of Calorimeter types, Tracker types, and Coil/Solenoid.
class SvgConverter
{
    // Namespaces.
    private static final Namespace ns = Namespace.getNamespace("http://www.w3.org/2000/svg");
    private static final Namespace xlink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");

    // Scaling of Subdetector drawing from LCSim natural units (mm).
    private static final double scale = 0.1;

    // These margins are for positioning the Subdetector group and 
    // are in LCSim units (mm).
    private static final double xmargin = 1500;
    private static final double ymargin = 1000;

    // FIXME This needs to be determined dynamically from size of
    // left margin + left labels + detector area.
    private static final double viewportX = 1200;

    // FIXME This needs to be determined dynamically from size of
    // upper margin + detector area + bottom label area.
    private static final double viewportY = 1000;

    // Formatting for length measurements.
    private static final DecimalFormat df = new DecimalFormat("#.##");

    // Static access to important grouping elements.
    private static Element labelsY;
    private static Element labelsX;
    // private static Element labelLinesX;
    // private static Element labelLinesY;
    // private static Element subdetGroup;

    // If barrel detector is less than this thick,
    // its outer radius label will not be printed.
    private static double yLabelTolerance = 200.;

    // Label text margin from leftmost edge.
    private static double yLabelMarginX = 10;

    // Starting offset for text as bottom labels are created.
    // This incremented as Z labels are added.
    private static double zLabelOffsetY = 25;

    // If a layer is less than this size in LCSim units (mm),
    // then it will be drawn as a line rather than a rectangle.
    private static double minTrackerLayerThickness = 50;

    public static Element convert(Detector d)
    {
        // Convert to specific subclass for more method access.
        Detector detector = (org.lcsim.geometry.compact.Detector)d;

        // Compute max ZY measurements of detector.
        double[] zy = findMaxZY(detector);
        // System.out.println("max xy = " + zy[0] + ", " + zy[1]);

        // SVG root element.
        Element root = new Element("svg");
        root.setNamespace(ns);
        root.addNamespaceDeclaration(xlink);
        root.setAttribute("version", "1.2");

        // Set viewport window.
        root.setAttribute("width", viewportX + "px");
        root.setAttribute("height", viewportY + "px");

        // All elements go into this group.
        Element g = new Element("g", ns);
        root.addContent(g);

        // Header with name.
        Element header = text(g, detector.getName(), viewportX / 2, 50);
        header.setAttribute("font-family", "Arial");
        header.setAttribute("font-size", "32");

        // Subdetector group.
        Element gs = new Element("g", ns);
        g.addContent(gs);
        gs.setAttribute("transform", "scale(" + scale + ") " + "translate(" + xmargin + ", " + ymargin + ")");

        // SVG X axis.
        Element xaxis = line(gs, 0., zy[1], zy[0], zy[1]);
        xaxis.setAttribute("stroke-width", "1");
        xaxis.setAttribute("stroke", "black");
        xaxis.setAttribute("id", "xaxis");

        // SVG Y axis.
        Element yaxis = line(gs, 0., 0., 0., zy[1]);
        yaxis.setAttribute("stroke-width", "1");
        yaxis.setAttribute("stroke", "black");
        yaxis.setAttribute("id", "yaxis");

        // SVG Y labels group.
        labelsY = new Element("g", ns);
        g.addContent(labelsY);
        labelsY.setAttribute("font-size", "12");
        labelsY.setAttribute("id", "ylabels");
        labelsY.setAttribute("transform", "translate(" + 0 + ", " + ymargin * scale + ")");

        // SVG X labels group.
        labelsX = new Element("g", ns);
        g.addContent(labelsX);
        labelsX.setAttribute("font-size", "12");
        labelsX.setAttribute("id", "xlabels");
        labelsX.setAttribute("transform", "translate(" + (xmargin * scale) + ", " + ((ymargin + zy[1]) * scale) + ")");

        // Make ZY view of Detector.
        convertSubdetectors(gs, detector, zy[0], zy[1]);

        // Return the created Element to be written out by Main.
        return root;
    }

    private static String convertColor(VisAttributes vis)
    {
        float[] rgba = vis.getRGBA();
        return "rgb(" + rgba[0] * 100 + "%, " + rgba[1] * 100 + "%, " + rgba[2] * 100 + "%)";
    }

    private static class InnerRadiusCompare implements Comparator<Calorimeter>
    {
        public int compare(Calorimeter subdet1, Calorimeter subdet2)
        {
            double ir1 = subdet1.getInnerRadius();
            double ir2 = subdet2.getInnerRadius();
            if (ir1 > ir2)
            {
                return 1;
            }
            else if (ir1 < ir2)
            {
                return -1;
            }
            else
            {
                return 0;
            }
        }
    }

    private static class InnerZCompare implements Comparator<Calorimeter>
    {
        public int compare(Calorimeter subdet1, Calorimeter subdet2)
        {
            double ir1 = subdet1.getInnerZ();
            double ir2 = subdet2.getInnerZ();
            if (ir1 > ir2)
            {
                return 1;
            }
            else if (ir1 < ir2)
            {
                return -1;
            }
            else
            {
                return 0;
            }
        }
    }

    private static void convertSubdetectors(Element parent, Detector detector, double maxZ, double maxY)
    {        
        List<Subdetector> subdetectors = detector.getSubdetectorList();
        
        // Look at Calorimeters first.
        List<Calorimeter> calorimeters = new ArrayList<Calorimeter>();
        List<Calorimeter> barrelCalorimeters = new ArrayList<Calorimeter>();
        List<Calorimeter> endcapCalorimeters = new ArrayList<Calorimeter>();
        for (Subdetector subdet : subdetectors)
        {
            if (subdet instanceof Calorimeter)
            {
                if (subdet.isBarrel())
                {
                    barrelCalorimeters.add((Calorimeter)subdet);
                }
                else if (subdet.isEndcap())
                {
                    endcapCalorimeters.add((Calorimeter)subdet);
                }
            }
        }        
        
        // Sort Calorimeter barrels on innerR.
        Collections.sort(barrelCalorimeters, new InnerRadiusCompare());
        
        // Add sorted barrel calorimeters to list.
        calorimeters.addAll(barrelCalorimeters);        

        // Sort Calorimeter endcaps on innerZ.
        Collections.sort(endcapCalorimeters, new InnerZCompare());
        
        // Add sorted endcap calorimeters to list.
        calorimeters.addAll(endcapCalorimeters);

        // Make the SVG for ordered Calorimeters.
        for (Calorimeter cal : calorimeters)
        {
            SvgConverter.convertSubdetector(parent, (Subdetector)cal, maxZ, maxY);
        }
        
        // Now draw the Trackers, supports, and dead material.
        for (org.lcsim.geometry.Subdetector subdet : detector.getSubdetectors().values())
        {
            if (!(subdet instanceof Calorimeter))
            {
                SvgConverter.convertSubdetector(parent, (Subdetector)subdet, maxZ, maxY);
            }
        }
    }

    /**
     * Order a List of ZPlanes by Z value.
     * 
     * @author jeremym
     * 
     */
    private static class ZPlaneCompare implements Comparator<ZPlane>
    {
        public int compare(ZPlane zp1, ZPlane zp2)
        {
            double z1 = zp1.getZ();
            double z2 = zp2.getZ();
            if (z1 > z2)
                return 1;
            else if (z1 < z2)
                return -1;
            else
                return 0;
        }
    }

    private static void convertSubdetector(Element parent, Subdetector subdet, double maxZ, double maxY)
    {
        // Debug print.
        System.out.println(">> " + subdet.getName());

        // Get VisAttributes of Subdetector.
        VisAttributes vis = subdet.getVisAttributes();

        // If not visible then immediately return without drawing anything.
        if (!vis.getVisible())
        {
            System.out.println("    *not visible* ... skipping");
            return;
        }

        // Convert color parameters to SVG format.
        String color = convertColor(vis);
        float alpha = vis.getRGBA()[3];

        // Make a group for this Subdetector.
        Element g = new Element("g", ns);
        g.setAttribute("id", subdet.getName());
        parent.addContent(g);

        // Set Subdetector's line and fill colors.
        g.setAttribute("stroke", color);
        g.setAttribute("fill", color);
        g.setAttribute("stroke-width", "3"); // Default stroke-width.
        g.setAttribute("opacity", Float.toString(alpha));

        // Draw Calorimeters.
        if (subdet instanceof Calorimeter)
        {
            // Turn off shape outline.
            g.setAttribute("stroke-width", "0");

            // Get Calorimeter generic parameters.
            Calorimeter cal = (Calorimeter)subdet;
            double innerR = cal.getInnerRadius();
            double outerR = cal.getOuterRadius();            
            double halfZ = cal.getZLength() / 2;
            double zlength = cal.getZLength();
            
            // The labels group is accessed statically to avoid having to pass it down.
            Element labelGroup = labelsY;

            // Draw barrel calorimeters.
            if (subdet.isBarrel())
            {
                // Make a rectangular outline of calorimeter barrel.
                rect(g, 0., maxY - outerR, outerR - innerR, halfZ);

                // Line at inner radius of barrel calorimeter.
                Element lineInner =
                        line(labelGroup, 75., ((maxY - innerR) * scale), xmargin * scale, (maxY - innerR) * scale);
                lineInner.setAttribute("stroke-dasharray", "6,3");
                lineInner.setAttribute("stroke", "gray");
                lineInner.setAttribute("stroke-width", "1");

                // Label inner radius measurement.
                text(labelGroup, df.format(innerR), yLabelMarginX, ((maxY - innerR) * scale) + 5);

                // Outer R is only labeled if there is enough space for it.
                if (outerR - innerR > yLabelTolerance)
                {
                    // Line at outer radius of barrel calorimeter.
                    Element lineOuter =
                            line(labelGroup, 75., ((maxY - outerR) * scale), xmargin * scale, (maxY - outerR) * scale);
                    lineOuter.setAttribute("stroke-dasharray", "6,3");
                    lineOuter.setAttribute("stroke", "gray");
                    lineOuter.setAttribute("stroke-width", "1");

                    // Label outer radius measurement.
                    text(labelGroup, df.format(outerR), yLabelMarginX, ((maxY - outerR) * scale) + 5);
                }

                // Do bottom labels now. Switch variable reference.
                labelGroup = labelsX;

                // Dashed lines at bottom along z direction indicating barrel z measurements.
                Element lineBottom = line(labelGroup, 0, zLabelOffsetY, halfZ * scale, zLabelOffsetY);
                lineBottom.setAttribute("stroke-dasharray", "6,3");
                lineBottom.setAttribute("stroke", "gray");
                lineBottom.setAttribute("stroke-width", "1");

                // Label measurement for barrel z.
                text(labelGroup, df.format(halfZ), ((halfZ * scale) / 2), zLabelOffsetY - 5);

                // Increment for next label.
                zLabelOffsetY += 25;
                
                Calorimeter.CalorimeterType calType = cal.getCalorimeterType();
                System.out.println(calType.toString());
                if (!calType.equals(CalorimeterType.UNKNOWN))
                {
                    String calLabel = calType.toString().replace("_BARREL", "");
                    double calThickness = outerR - innerR;
                    double y = (maxY - outerR) + calThickness / 2;
                    y *= scale;
                    Element calText = text(labelsY, calLabel, 80, y + 5);
                    calText.setAttribute("font-size", "12");
                }
            }
            // Draw endcap calorimeters that are reflected.
            else if (subdet.isEndcap() && subdet.getReflect())
            {
                // Get geometry parameters.
                double innerZ = cal.getInnerZ();
                double outerZ = cal.getOuterZ();

                // Make a rectangle for the endpca.
                rect(g, innerZ, maxY - outerR, outerR - innerR, halfZ * 2);

                double thickness = outerZ - innerZ;

                // Setup reference to make a label and line on the xaxis.
                labelGroup = labelsX;

                // Line indicating endcap extent in X (or Z in LCSim coordinates).
                Element lineEndcap = line(labelGroup, innerZ * scale, zLabelOffsetY, outerZ * scale, zLabelOffsetY);
                lineEndcap.setAttribute("stroke-dasharray", "6,3");
                lineEndcap.setAttribute("stroke", "gray");
                lineEndcap.setAttribute("stroke-width", "1");

                // Label measurement of endcap z length.
                // FIXME Manual 10 pix adjustment.
                text(labelGroup, df.format(zlength), ((innerZ + thickness / 2) * scale) - 10, zLabelOffsetY - 5);

                // Increment labeling offset.
                zLabelOffsetY += 25;
            }
        }
        // Draw trackers.
        else if (subdet instanceof Tracker)
        {
            // Draw barrel trackers.
            if (subdet instanceof SiTrackerBarrel || subdet instanceof MultiLayerTracker)
            {
                double minR = 9999999.;
                double maxR = 0;
                // Loop over SiTrackerBarrel layers.
                IDetectorElement de = subdet.getDetectorElement();
                for (IDetectorElement layer : de.getChildren())
                {
                    // Get parameters from layer's tube shape.
                    Tube tube = (Tube)layer.getGeometry().getLogicalVolume().getSolid();
                    double thickness = tube.getOuterRadius() - tube.getInnerRadius();
                    double r = tube.getInnerRadius() + thickness / 2;
                    double halfZ = tube.getZHalfLength();
                    double outerR = tube.getOuterRadius();
                    double innerR = tube.getInnerRadius();
                    
                    if (innerR < minR)
                    {
                        minR = innerR;
                    }
                    
                    if (outerR > maxR)
                    {
                        maxR = outerR;
                    }

                    // Draw a line for this tracker layer.
                    if (thickness < minTrackerLayerThickness)
                    {
                        // Draw a line with Subdetector's color.
                        line(g, 0, maxY - r, halfZ, maxY - r);
                    }
                    // Draw a layer rectangle if component is thick enough.
                    else
                    {
                        // Use a black outline to separate nearby layers.
                        g.setAttribute("stroke", "black");

                        // Make a rectangle for the layer.
                        rect(g, 0., maxY - outerR, outerR - innerR, halfZ);
                    }
                }
                
                System.out.println("maxR = " + maxR);
                System.out.println("minR = " + minR);
            }
            // Draw DiskTracker.
            // FIXME Replace compact based code with IDetectorElement, but DiskTracker
            // layers need their own DetectorElements first.
            else if (subdet instanceof DiskTracker)
            {
                DiskTracker diskTracker = (DiskTracker)subdet;
                int nlayers = diskTracker.getInnerR().length;
                for (int i = 0, n = nlayers; i < n; i++ )
                {
                    double innerR = diskTracker.getInnerR()[i];
                    double outerR = diskTracker.getOuterR()[i];
                    double z = diskTracker.getThickness()[i];
                    double innerZ = diskTracker.getInnerZ()[i];
                    double midZ = innerZ + z / 2;

                    // Draw a line for this tracker layer.
                    if (z < minTrackerLayerThickness)
                    {
                        // Draw a line with Subdetector's color.
                        line(g, midZ, maxY - outerR, midZ, maxY - innerR);
                    }
                    // Draw a layer rectangle if component is thick enough.
                    else
                    {
                        // Use a black outline to separate nearby layers.
                        g.setAttribute("stroke", "black");

                        // Make a rectangle for the layer.
                        rect(g, innerZ, maxY - outerR, outerR - innerR, z);
                    }
                }
            }
        }
        else if (subdet instanceof PolyconeSupport)
        {
            PolyconeSupport support = (PolyconeSupport)subdet;

            // Sort zplanes by z, from negative to positive.
            List<ZPlane> zplanes = new ArrayList<ZPlane>(support.getZPlanes());
            Collections.sort(zplanes, new ZPlaneCompare());

            // Make a list of usable ZPlanes.
            List<ZPlane> zplanesUse = new ArrayList<ZPlane>();
            for (int i = 0, n = zplanes.size(); i < n; i++ )
            {
                ZPlane zplane = zplanes.get(i);

                // Only use ZPlanes in positive Z.
                if (zplane.getZ() > 0)
                {
                    // Make a modified ZPlane for components that cross the xaxis.
                    if (i > 0 && zplanesUse.size() == 0)
                    {
                        // Get prior ZPlane with negative Z coordinate.
                        ZPlane lastNegZPlane = zplanes.get(i - 1);

                        // If radii are the same, then draw from Y axis.
                        if (lastNegZPlane.getRMin() == zplane.getRMin() && lastNegZPlane.getRMax() == zplane.getRMax())
                        {
                            ZPlane borderZPlane = new ZPlane(lastNegZPlane.getRMin(), lastNegZPlane.getRMax(), 0);
                            zplanesUse.add(borderZPlane);
                        }
                        // FIXME Handle ZPlanes crossing xaxis that have different radii from the next ZPlane.
                    }
                    // Add positive ZPlane to usable list.
                    else
                    {
                        zplanesUse.add(zplane);
                    }
                }
            }

            if (zplanesUse.size() > 0)
            {
                // Buffer to store positions for polygon.
                StringBuffer buff = new StringBuffer();

                // Add outer radii points going in positive X direction.
                for (ZPlane zplane : zplanesUse)
                {
                    double outerR = zplane.getRMax();
                    double z = zplane.getZ();
                    buff.append(z + "," + (maxY - outerR) + " ");
                }

                // Make a reverse list of the ZPlanes.
                List<ZPlane> reverseZPlanes = new ArrayList<ZPlane>(zplanesUse);
                Collections.reverse(reverseZPlanes);

                // Add inner radii points going in the negative X direction.
                for (ZPlane zplane : reverseZPlanes)
                {
                    double innerR = zplane.getRMin();
                    double z = zplane.getZ();
                    buff.append(z + "," + (maxY - innerR) + " ");
                }
                                
                String points = buff.toString();
                points.trim();

                // Make the polygon using the list of points.
                Element polygon = new Element("polygon", ns);
                polygon.setAttribute("points", points);
                g.addContent(polygon);
            }
        }
        else if (subdet instanceof TubeSegment)
        {            
            TubeSegment tube = (TubeSegment)subdet;
                        
            if (tube.getTransform().getTranslation().z() > 0)
            {
                double innerR = tube.getInnerRadius();
                double outerR = tube.getOuterRadius();
                double halfZ = tube.getZHalfLength();
                double zmin = tube.getTransform().getTranslation().z() - halfZ;
                           
                // Only draw components with a positive z position.
                if (zmin > 0)
                {
                    rect(g, zmin, maxY - outerR, outerR - innerR, halfZ);
                }
                
                // FIXME: Rotation is completely ignored.
                // FIXME: TubeSegments that go across Y axis into positive X region are ignored.
            }
        }
        // TODO Handle these additional types...
        // SiTrackerEndcap
        // SiTrackerEndcap2
    }

    private static Element line(Element parent, double x1, double y1, double x2, double y2)
    {
        Element line = new Element("line", ns);
        parent.addContent(line);
        line.setAttribute("x1", df.format(x1));
        line.setAttribute("y1", df.format(y1));
        line.setAttribute("x2", df.format(x2));
        line.setAttribute("y2", df.format(y2));
        return line;
    }

    private static Element rect(Element parent, double x, double y, double height, double width)
    {
        Element rect = new Element("rect", ns);
        parent.addContent(rect);
        rect.setAttribute("x", df.format(x));
        rect.setAttribute("y", df.format(y));
        rect.setAttribute("height", df.format(height));
        rect.setAttribute("width", df.format(width));
        return rect;
    }

    private static Element text(Element parent, String text, double x, double y)
    {
        Element t = new Element("text", ns);
        parent.addContent(t);
        t.setText(text);
        t.setAttribute("x", df.format(x));
        t.setAttribute("y", df.format(y));
        return t;
    }

    private static double[] findMaxZY(Detector detector)
    {
        double[] zy = new double[2];
        double z = 0;
        double y = 0;

        // Assume calorimeter with largest extent defines max ZX.
        for (Subdetector subdet : detector.getSubdetectors().values())
        {
            if (subdet instanceof Calorimeter)
            {
                Calorimeter cal = (Calorimeter)subdet;
                if (cal.getOuterRadius() > y)
                {
                    y = cal.getOuterRadius();
                }
                if (cal.getOuterZ() > z)
                {
                    z = cal.getOuterZ();
                }
            }
        }

        if (z == 0 || y == 0)
        {
            throw new RuntimeException("Could not find ZY extent of this Detector!");
        }

        zy[0] = z;
        zy[1] = y;

        return zy;
    }
}