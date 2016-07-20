/**
 * 
 */
package org.singsurf.singsurf.definitions;
import jv.object.PsDebug;

public class Parameter
{
	String name;
	double val;

	public Parameter(String varname,double val)
	{
		name = varname; this.val = val;
	}
	public static Parameter parseTag(String line)
	{
		String name = LsmpDefReader.getAttribute(line,"name");
		double val = 0.0;
		try
		{
			val = Double.valueOf(LsmpDefReader.getAttribute(line,"value")).doubleValue();
		}
		catch(NumberFormatException e)
		{
			PsDebug.warning("Format error parsing value in "+line);
		}
		return new Parameter(name,val);
	}
	public String getName() { return name; }
	public double getVal() { return val; }
	public String toString()
	{
		return "<parameter name=\"" + name + "\""
			+ " value=\""+val+"\">\n";
	}
	public Parameter duplicate() {
		Parameter res = new Parameter(this.name,this.val);
		return res;
	}
	public void setVal(double val) {
		this.val = val;
	}
}