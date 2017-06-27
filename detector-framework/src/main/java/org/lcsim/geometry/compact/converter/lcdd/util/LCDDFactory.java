package org.lcsim.geometry.compact.converter.lcdd.util;

import java.util.List;

/*
 * 
 * A collection of static utility methods for creating objects 
 * in the org.lcsim.geometry.compact.converter.lcdd.util package.
 * 
 * @author jeremym
 * @version $Id: LCDDFactory.java,v 1.5 2010/12/03 01:22:18 jeremy Exp $
 */
public final class LCDDFactory
{
	/** Create the top-level LCDD container element. */
	public static LCDD createLCDD()
	{
		LCDD lcdd = new LCDD();
		return lcdd;
	}

	/** Create the detector element for the header. */
	public static Detector createDetector(String title, String version,
			String url)
	{
		Detector det = new Detector();
		det.setTitle(title);
		det.setVersion(version);
		det.setURL(url);
		return det;
	}

	/** Create the author element for the header. */
	public static Author createAuthor(String name, String email)
	{
		Author author = new Author();
		author.setName(name);
		author.setAuthorEmail(email);
		return author;
	}

	/** Create the generator element for the header. */
	public static Generator createGenerator(String title, String version,
			String file, long checksum)
	{
		Generator generator = new Generator();
		generator.setTitle(title);
		generator.setVersion(version);
		generator.setFile(file);
		generator.setChecksum(checksum);
		return generator;
	}

	/** Create the header element. */
	public static Header createHeader(String comment)
	{
		Header header = new Header();
		header.setComment(comment);
		return header;
	}

	/** Create a box solid. */
	public static Box createBox(String name, double x, double y, double z)
	{
		Box box = new Box(name);
		box.setX(x);
		box.setY(y);
		box.setZ(z);
		return box;
	}

	/** Create a tube solid. */
	public static Tube createTube(String name, double rmin, double rmax,
			double z, double deltaphi)
	{
		Tube tube = new Tube(name);
		tube.setRMax(rmax);
		tube.setRMin(rmin);
		tube.setZ(z);
		tube.setDeltaPhi(deltaphi);
		return tube;
	}

	/** Create a trapezoid solid. */
	public static Trapezoid createTrapezoid(String name, double x1, double x2,
			double y1, double y2, double z)
	{
		Trapezoid trd = new Trapezoid(name);
		trd.setX1(x1);
		trd.setX2(x2);
		trd.setY1(y1);
		trd.setY2(y2);
		trd.setZ(z);
		return trd;
	}

	/** Create a boolean solid using subtraction. */
	public static SubtractionSolid createSubtractionSolid(String name,
			Solid first, Solid second, Position pos, Rotation rot)
	{
		SubtractionSolid sub = new SubtractionSolid(name);
		sub.setFirstSolid(first);
		sub.setSecondSolid(second);
		sub.setPosition(pos);
		sub.setRotation(rot);
		return sub;
	}

	/** Create a polycone solid. */
	public static Polycone createPolycone(String name, double startPhi,
			double deltaPhi, List<ZPlane> zplanes)
	{
		Polycone polycone = new Polycone(name);
		polycone.setStartPhi(startPhi);
		polycone.setDeltaPhi(deltaPhi);
		if (zplanes != null)
		{
			for (ZPlane zplane : zplanes)
			{
				polycone.addZPlane(zplane);
			}
		}
		return polycone;
	}

	/** Create a regular polyhedra solid. */
	public static PolyhedraRegular createPolyhedraRegular(String name,
			int nsides, double rmin, double rmax, double zlength)
	{
		PolyhedraRegular polyhedra = new PolyhedraRegular(name, nsides, rmin,
				rmax, zlength);
		return polyhedra;
	}

	/** Create a zplane for use by Polycone. */
	public static ZPlane createZPlane(double rmin, double rmax, double z)
	{
		ZPlane zplane = new ZPlane(rmin, rmax, z);
		return zplane;
	}

	/** Create a basic Volume. */
	public static Volume createVolume(String name, Material material,
			Solid solid)
	{
		Volume volume = new Volume(name);
		volume.setMaterial(material);
		volume.setSolid(solid);
		return volume;
	}

	/** Create a Volume with all parameters. */
	public static Volume createVolume(String name, Material material,
			Solid solid, SensitiveDetector sd, Region region, LimitSet limits,
			List<PhysVol> pvs)
	{
		Volume volume = createVolume(name, material, solid);

		volume.setSensitiveDetector(sd);
		
		if ( region != null )
		{
			volume.setRegion(region);
		}
		
		if ( limits != null )
		{
			volume.setLimitSet(limits);
		}

		if (pvs != null)
		{
			for (PhysVol pv : pvs)
			{
				volume.addPhysVol(pv);
			}
		}

		return volume;
	}

	/** Create a Volume with all parameters, but copy the name from the Solid. */
	public static Volume createVolume(Material material, Solid solid,
			SensitiveDetector sd, Region region, LimitSet limits,
			List<PhysVol> pvs)
	{
		Volume volume = createVolume(solid.getSolidName(), material, solid, sd,
				region, limits, pvs);

		return volume;
	}

	/** Create a named double constant. */
	public static Constant createConstant(String name, double value)
	{
		Constant constant = new Constant(name, value);
		return constant;
	}

	/** Create a named string constant. */
	public static Constant createConstant(String name, String value)
	{
		Constant constant = new Constant(name, value);
		return constant;
	}

	/** Create an ID field. */
	public static IDField createIDField(String label, int start, int length,
			boolean signed)
	{
		IDField field = new IDField();
		field.setLabel(label);
		field.setStart(start);
		field.setLength(length);
		field.setSigned(signed);
		return field;
	}

	/** Create an IDSpec. */
	public static IDSpec createIDSpec(String name, int length,
			List<IDField> fields)
	{
		IDSpec spec = new IDSpec(name);
		spec.setLength(length);
		if (fields != null)
		{
			for (IDField field : fields)
			{
				spec.addIDField(field);
			}
		}
		return spec;
	}

	/** Create a single physics limit. */
	public static Limit createLimit(String name, String particles,
			double value, String unit)
	{
		Limit limit = new Limit(name);
		limit.setParticles(particles);
		limit.setValue(value);
		limit.setUnit(unit);
		return limit;
	}

	/** Create a set of physics limits. */
	public static LimitSet createLimitSet(String name, List<Limit> limits)
	{
		LimitSet limitset = new LimitSet(name);
		if (limits != null)
		{
			for (Limit limit : limits)
			{
				limitset.addLimit(limit);
			}
		}
		return limitset;
	}

	/** Create a material. */
	public static Material createMaterial(String name)
	{
		Material material = new Material(name);
		return material;
	}

	/** Create a physical volume (placement). */
	public static PhysVol createPhysVol(Volume volume, Position position,
			Rotation rotation)
	{
		PhysVol pv = new PhysVol();
		pv.setVolume(volume);
		pv.setPosition(position);
		pv.setRotation(rotation);
		return pv;
	}
	
	/** Create a physical volume (placement) with a list of physical volume ids. */
	public static PhysVol createPhysVol(Volume volume, Position position,
			Rotation rotation, List<PhysVolID> ids)
	{
		PhysVol pv = createPhysVol(volume, position, rotation);
		
		if (ids != null)
		{
			for (PhysVolID id : ids)
			{
				pv.addPhysVolID(id);
			}
		}
		return pv;
	}

	/** Create a physical volume identifier. */
	public static PhysVolID createPhysVolID(String name, int value)
	{
		PhysVolID id = new PhysVolID(name, value);
		return id;
	}

	/** Create an XYZ position. */
	public static Position createPosition(String name, double x, double y,
			double z)
	{
		Position position = new Position(name);
		position.setX(x);
		position.setY(y);
		position.setZ(z);
		return position;
	}

	/** Create a calorimeter-type sensitive detector. */
	public static Calorimeter createCalorimeter(String name, Segmentation seg)
	{
		Calorimeter cal = new Calorimeter(name);
		cal.setSegmentation(seg);
		return cal;
	}

	/** Create a tracker-type sensitive detector. */
	public static Tracker createTracker(String name)
	{
		Tracker trk = new Tracker(name);
		return trk;
	}

	/** Create a projective cylinder segmentation. */
	public static ProjectiveCylinder createProjectiveCylinder(int ntheta,
			int nphi)
	{
		ProjectiveCylinder seg = new ProjectiveCylinder();
		seg.setNTheta(ntheta);
		seg.setNPhi(nphi);
		return seg;
	}

	/** Create a projective zplane segmentation. */
	public static ProjectiveZPlane createProjectiveZPlane(int ntheta, int nphi)
	{
		ProjectiveZPlane seg = new ProjectiveZPlane();
		seg.setNTheta(ntheta);
		seg.setNPhi(nphi);
		return seg;
	}

	/** Create a Cartesian grid segmentation. */
	public static GridXYZ createGridXYZ(double x, double y, double z)
	{
		GridXYZ seg = new GridXYZ();
		seg.setGridSizeX(x);
		seg.setGridSizeY(y);
		seg.setGridSizeZ(z);
		return seg;
	}

	/** Create a nonprojective cylinder segmentation. */
	public static NonprojectiveCylinder createNonprojectiveCylinder(
			double gridSizeZ, double gridSizePhi)
	{
		NonprojectiveCylinder seg = new NonprojectiveCylinder();
		seg.setGridSizeZ(gridSizeZ);
		seg.setGridSizePhi(gridSizePhi);
		return seg;
	}

	/** Create a region. */
	public static Region createRegion(String name, boolean storeSecondaries,
			double energyCut, double rangeCut, String eunit, String lunit)
	{
		Region region = new Region(name);
		region.setStoreSecondaries(storeSecondaries);
		region.setThreshold(energyCut);
		region.setCut(rangeCut);
		region.setEnergyUnit(eunit);
		region.setLengthUnit(lunit);
		return region;
	}

	/** Create a rotation with XYZ parameters. */
	public static Rotation createRotation(String name, double x, double y,
			double z)
	{
		Rotation rot = new Rotation(name);
		rot.setX(x);
		rot.setY(y);
		rot.setZ(z);
		return rot;
	}

	/** Create a solenoid field. */
	public static Solenoid createSolenoid(String name, double BInner,
			double BOuter, double rInner, double zMax)
	{
		Solenoid sol = new Solenoid(name);
		sol.setInnerField(BInner);
		sol.setOuterField(BOuter);
		sol.setInnerRadius(rInner);
		sol.setZMax(zMax);
		return sol;
	}

	/** Create an RZB field map. */
	public static RZFieldMap createRZFieldMap(String name, int numBinsR,
			int numBinsZ, double gridSizeR, double gridSizeZ, String lunit,
			String funit, List<RZBData> data)
	{
		RZFieldMap field = new RZFieldMap(name);
		field.setNumBinsR(numBinsR);
		field.setNumBinsZ(numBinsZ);
		field.setGridSizeR(gridSizeR);
		field.setGridSizeZ(gridSizeZ);
		field.setLengthUnit(lunit);
		field.setFieldUnit(funit);
		if (data != null)
		{
			for (RZBData rzb : data)
			{
				field.addRZBData(rzb);
			}
		}
		return field;
	}
	
	/** Create a Dipole magnetic field. */
	public static Dipole createDipole(String name, double zmin, double zmax, double rmax, double[] coeffs)
	{
		Dipole dipole = new Dipole(name);
		dipole.setZMax(zmax);
		dipole.setZMin(zmin);
		dipole.setRMax(rmax);
		for (int i=0; i<coeffs.length; i++)
		{
			dipole.addCoeff(coeffs[i]);
		}
		return dipole;
	}
	
	/** 
	 * Create a VisAttributes with color settings. 
	 */
	public static VisAttributes createVisAttributes(String name, float r, float g, float b, float a)
	{
		VisAttributes vis = new VisAttributes(name);
		vis.setColor(r,g,b,a);
		return vis;
	}
}
