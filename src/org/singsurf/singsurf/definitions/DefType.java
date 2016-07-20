package org.singsurf.singsurf.definitions;

import java.util.ArrayList;
import java.util.List;

/** typesafe enum for var types */
public class DefType
{
	static List<DefType> knownTypes = new ArrayList<DefType>(20);
	String type;
	private DefType(String s) { type = s; knownTypes.add(this); }
	public static final DefType none  = new DefType("none");
	public static final DefType psurf = new DefType("psurf");
	public static final DefType pcurve = new DefType("pcurve");
	public static final DefType asurf = new DefType("asurf");
	public static final DefType acurve = new DefType("acurve");
	//public static final DefType acurve3 = new DefType("acurve3");
	public static final DefType intersect = new DefType("intersect");
	//public static final DefType impsurf = new DefType("impsurf");
	//public static final DefType icurve = new DefType("icurve");
	public static final DefType mapping = new DefType("mapping");
	//public static final DefType psurf_icurve = new DefType("psurf icurve");
    //public static final DefType pcurve_pcurve = new DefType("pcurve pcurve");
	//public static final DefType sceneGraph = new DefType("scene graph");
	public static final DefType genMap = new DefType("genMap");
	public static final DefType genInt = new DefType("genInt");
    public static final DefType biMap = new DefType("biMap");
    public static final DefType biInt = new DefType("biInt");
	public static final DefType colour = new DefType("colour");

	public String toString() { return type; }
	public static DefType get(String s)
	{
		if(s == null) return none;
		for(DefType cur: knownTypes) {
			if(s.equals(cur.type)) return(cur);
		}
		return null;
	}
}