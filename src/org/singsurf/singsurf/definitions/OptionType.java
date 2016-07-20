package org.singsurf.singsurf.definitions;

import java.util.Enumeration;
import java.util.Vector;

/** typesafe enum for var types */
public class OptionType
{
	static Vector<OptionType> knownTypes = new Vector<OptionType>(20);
	String type;
	private OptionType(String s) { type = s; knownTypes.addElement(this); }
	public static final OptionType intType  = new OptionType("integer");
	public static final OptionType doubleType = new OptionType("double");
	public static final OptionType StringType = new OptionType("string");

	public String toString() { return type; }
	public static OptionType getOptionType(String s)
	{
		if(s == null) return null;
		Enumeration<OptionType> itt = knownTypes.elements();
		while(itt.hasMoreElements())
		{
			OptionType cur = (OptionType) itt.nextElement();
			if(s.equals(cur.type)) return(cur);
		}
		return null;
	}
}