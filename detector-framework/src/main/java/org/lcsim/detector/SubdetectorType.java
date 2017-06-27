package org.lcsim.detector;

public class SubdetectorType 
{
	SystemType systemType = SystemType.UNKNOWN;
	BarrelType barrelType = BarrelType.UNKNOWN;
	GenericType genericType = GenericType.UNKNOWN;
	
	SubdetectorType(SystemType systemType, BarrelType barrelType)
	{
		this.systemType = systemType;
		this.barrelType = barrelType;
		if (systemType == SystemType.VERTEX_DETECTOR || systemType == SystemType.SILICON_TRACKER || systemType == SystemType.TPC)
			genericType = GenericType.TRACKER;
		else if (systemType == SystemType.ECAL || systemType == SystemType.HCAL || systemType == SystemType.MUON || systemType == SystemType.FORWARD)
			genericType = GenericType.CALORIMETER;
	}
	
	public enum GenericType
	{
		TRACKER,
		CALORIMETER,
		UNKNOWN
	}
		
	public enum SystemType
	{
		VERTEX_DETECTOR,
	    SILICON_TRACKER,
	    TPC,
	    ECAL,
	    HCAL,
	    MUON,
	    FORWARD,
	    UNKNOWN
	}
		
	public enum BarrelType
	{
		BARREL,
		ENDCAP,
		UNKNOWN
	}
		
	public SystemType getSystemType()
	{
		return systemType;
	}
	
	public BarrelType getBarrelType()
	{
		return barrelType;
	}
	
	public GenericType getGenericType()
	{
		return genericType;
	}
	
	/** 
	 * FIXME: This is rather fragile as it depends on names in the compact description 
	 * which may not follow these conventions.
	 */ 
	public static SubdetectorType convert(String subdetectorName)
	{
		SystemType system = SystemType.UNKNOWN;
		BarrelType barrel = BarrelType.UNKNOWN;
				
		if (subdetectorName.contains("endcap"))
			barrel = BarrelType.ENDCAP;
		else if (subdetectorName.contains("barrel"))
			barrel = BarrelType.BARREL;
		
		if (subdetectorName.startsWith("vtx") || subdetectorName.startsWith("vertex"))
			system = SystemType.VERTEX_DETECTOR;		
		else if (subdetectorName.contains("tracker"))
			system = SystemType.SILICON_TRACKER;
		else if (subdetectorName.startsWith("tpc"))
			system = SystemType.TPC;
		else if (subdetectorName.startsWith("ecal") || subdetectorName.startsWith("em"))
			system = SystemType.ECAL;
		else if (subdetectorName.startsWith("hcal") || subdetectorName.startsWith("had"))
			system = SystemType.HCAL;
		else if (subdetectorName.startsWith("muon"))
			system = SystemType.MUON;
		else if (subdetectorName.contains("forward") || subdetectorName.startsWith("lumi"))
			system = SystemType.FORWARD;		
						
		return new SubdetectorType(system, barrel);
	}
}