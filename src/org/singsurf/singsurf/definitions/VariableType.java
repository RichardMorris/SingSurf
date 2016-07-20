package org.singsurf.singsurf.definitions;

import java.util.Enumeration;
import java.util.Vector;

/** typesafe enum for var types */
public class VariableType
{
	static Vector<VariableType> knownTypes = new Vector<VariableType>(20);
	String type;
	private VariableType(String s) { type = s; knownTypes.addElement(this); }
	public static final VariableType Normal  = new VariableType("Normal");
	public static final VariableType AuxOutX = new VariableType("AuxOutX");
	public static final VariableType AuxOutY = new VariableType("AuxOutY");
	public static final VariableType AuxOutZ = new VariableType("AuxOutZ");
	public static final VariableType AuxInX  = new VariableType("AuxInX");
	public static final VariableType AuxInY  = new VariableType("AuxInY");
	public static final VariableType AuxInZ  = new VariableType("AuxInZ");
	public static final VariableType VectorX = new VariableType("VectorX");
	public static final VariableType VectorY = new VariableType("VectorY");
	public static final VariableType VectorZ = new VariableType("VectorZ");
	public static final VariableType EigenPx = new VariableType("EigenPx");
	public static final VariableType EigenPy = new VariableType("EigenPy");
	public static final VariableType EigenQx = new VariableType("EigenQx");
	public static final VariableType EigenQy = new VariableType("EigenQy");

	public String toString() { return type; }
	public static VariableType getType(String s)
	{
		if(s==null) return null;
		Enumeration<VariableType> itt = knownTypes.elements();
		while(itt.hasMoreElements())
		{
			VariableType cur = (VariableType) itt.nextElement();
			if(s.equals(cur.type)) return(cur);
		}
		return null;
	}
}