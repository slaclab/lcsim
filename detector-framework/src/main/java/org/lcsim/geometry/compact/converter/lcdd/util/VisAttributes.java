package org.lcsim.geometry.compact.converter.lcdd.util;

import org.jdom.Element;

public class VisAttributes extends RefElement
{	
    public VisAttributes(String name)
    {
        super("vis",name);
        
        this.setAttribute("line_style","unbroken");
        this.setAttribute("drawing_style","wireframe");
        this.setAttribute("show_daughters","true");
        this.setAttribute("visible","true");
        
        Element color = new Element("color");
        color.setAttribute("R","1.0");
        color.setAttribute("G","1.0");
        color.setAttribute("B","1.0");
        color.setAttribute("alpha","1.0");  
        this.addContent(color);             
    }
    
	enum LineStyle
	{
		UNBROKEN("unbroken"),
		DASHED("dashed"),
		DOTTED("dotted");
		
		private String s;
		
		LineStyle(String s)
		{
			this.s = s;
		}
		
		public String toString()
		{
			return s;
		}
	}
	
	enum DrawingStyle
	{
		WIREFRAME("wireframe"),
		SOLID("solid");
		
		private String s;
		
		DrawingStyle(String s)
		{
			this.s = s;
		}
		
		public String toString()
		{
			return s;
		}
	}
		
	public final void setColor(float r, float g, float b, float a)
	{
		Element color = this.getChild("color");
		color.setAttribute("R",String.valueOf(r));
		color.setAttribute("G",String.valueOf(g));
		color.setAttribute("B",String.valueOf(b));
		color.setAttribute("alpha",String.valueOf(a));
	}
	
	public final void setShowDaughters(boolean b)
	{
		this.setAttribute("show_daughters",Boolean.toString(b));
	}
	
	public final void setDrawingStyle(DrawingStyle s)
	{
		this.setAttribute("drawing_style",s.toString());
	}
	
	public final void setDrawingStyle(String s)
	{
		this.setAttribute("drawing_style",s);
	}
	
	public final void setLineStyle(LineStyle s)
	{
		this.setAttribute("line_style",s.toString());
	}		
	
	public final void setLineStyle(String s)
	{
		this.setAttribute("line_style",s);
	}
	
	public final void setVisible(boolean v)
	{
		this.setAttribute("visible",Boolean.toString(v));
	}	
}