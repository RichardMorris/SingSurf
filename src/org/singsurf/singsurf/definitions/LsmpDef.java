package org.singsurf.singsurf.definitions;
import java.util.ArrayList;
import java.util.List;

/** A class to contain info about a surface definition. */

public class LsmpDef
{
	/** The name of the definition. */
	String name;
	/** The type of surface. */
	DefType type;
	/** The type of input for operators. */
	DefType opType=null;
	/** The main definition. */
	String	equation;
	/** The variables. */
	List<DefVariable> variables = null;
	/** The parameters. */
	List<Parameter> parameters = null;
	/** The options. */
	List<Option> options = null;

	public LsmpDef(String name,String type, String eqn)
	{
		this.name = name;
		this.type = DefType.get(type);
		equation = eqn;
		variables = new ArrayList<DefVariable>();
		parameters = new ArrayList<Parameter>();
		options = new ArrayList<Option>();
	}
	public LsmpDef(String name,DefType type, String eqn)
	{
		this.name = name;
		this.type = type;
		equation = eqn;
		variables = new ArrayList<DefVariable>();
		parameters = new ArrayList<Parameter>();
		options = new ArrayList<Option>();
	}
	public LsmpDef(String name,String type, String eqn,List<DefVariable> vars,List<Parameter> params,List<Option> opts)
	{
		this.name = name;
		this.type = DefType.get(type);
		equation = eqn;
		variables = vars;
		parameters = params;
		options = opts;
	}
	public LsmpDef(String name,String type, String eqn,String opType,List<DefVariable> vars,List<Parameter> params,List<Option> opts)
	{
		this.name = name;
		this.type = DefType.get(type);
		this.opType = DefType.get(opType);
		equation = eqn;
		variables = vars;
		parameters = params;
		options = opts;
	}
	

	public String getName() { return name; }
	public DefType getType() { return type; }
	public DefType getOpType() { return opType; }
	public void setOpType(DefType opType) {this.opType = opType; }
	public String getEquation() { return equation; }
	public void setEquation(String def) {
		this.equation = def;
	}

	public int getNumVars() { if(variables==null) return -1; else return variables.size(); }
//	public Variable[] getVars() { return variables; }
	public DefVariable getVar(int i) { return (DefVariable) variables.get(i); }
	public DefVariable getVariable(String name)
	{
		for(DefVariable v:variables)
			if(name.equals(v.getName())) return v;
		return null;
	}
	public int getVariableIndex(DefVariable v)
	{
		return variables.indexOf(v);
	}
	public void setVariable(int index,DefVariable var)
	{
		if(index>=variables.size())
			variables.add(index,var);
		variables.set(index,var);
	}
	
	public List<DefVariable> getVariablesByType(DefType type)
	{
		List<DefVariable> res = new ArrayList<DefVariable>();
		for(DefVariable var:variables) {
			if(var.getType() == type)
				res.add(var);
		}
		return res;
	}
	public int getNumParams() { if(parameters==null) return -1; else return parameters.size(); }
	public List<Parameter> getParams() { return parameters; }
	public Parameter getParam(int i) { return (Parameter) parameters.get(i); }
	//public Enumeration getParamEnumeration() { return parameters.elements(); }
	/** Tests whether a parameter with name exists. 
	 * 
	 * @param name - name of parameter
	 * @return the reference to the parameter or null if not found
	 */
	public Parameter getParameter(String name)
	{
		for(Parameter p:parameters)
			if(name.equals(p.getName())) return p;
		return null;
	}
	public boolean setParameterValue(String name,double val) {
		Parameter p=getParameter(name);
		if(p==null) return false;
		p.setVal(val);
		return true;
	}
	/**
	 * Deletes the parameter p 
	 * @param p
	 * @return true if parameter exists.
	 */
	public boolean deleteParameter(Parameter p)	{
		if(p==null) return false;
		return parameters.remove(p);
	}
	/**
	 * Deletes the parameter p 
	 * @param name - name of the parameter
	 * @return true if parameter exists.
	 */
	public boolean deleteParameter(String name)	{
		return deleteParameter(getParameter(name));
	}
	public Parameter addParameter(String name)
	{
		Parameter p = getParameter(name);
		if(p!=null) return p;
		p = new Parameter(name,0.0);
		parameters.add(p);
		return p;
	}
	public void setParamNames(Object newNames[])
	{
		int oldSize = parameters.size();
		boolean keep[]= new boolean[oldSize];
		for(int i=0;i<oldSize;++i) keep[i]=false;
		for(int i=0;i<newNames.length;++i)
		{
			Parameter p = getParameter((String) newNames[i]);
			if(p==null) addParameter((String) newNames[i]);
			else
				keep[parameters.indexOf(p)]=true;
		}
		for(int i=oldSize-1;i>=0;--i) 
			if(!keep[i]) parameters.remove(i);
	}
	public int getNumOpts() { if(options==null) return -1; else return options.size(); }
//	public LsmpDef.Option[] getOpts() { return options; }
	public Option getOpt(int i) { return (Option) options.get(i); }
	public Option getOpt(String optionName)
	{
		for(Option opt:options)
			if(optionName.equals(opt.getName()))
				return opt; 
		return null;
	}
	public Option getOption(String optionName)
	{
		return getOpt(optionName);
	}
	public Option setOption(String name,int val) {
		Option op = getOpt(name);
		if(op!=null)
			op.setValue(val);
		else
			op = new Option(name,Integer.toString(val));
		return op;
	}
	/** add a variable. */
	public void add(DefVariable var) { variables.add(var); }
	/** add a parameter. */
	public void add(Parameter param) { parameters.add(param); }
	/** add an option. */
	public void add(Option opt) { options.add(opt); }

	/** Returns a string with the XML for the definition. **/
	
	public String toString()
	{
		StringBuffer buf = new StringBuffer(1000);
		buf.append("<definition name=\""+name+"\"");
		if(type!=null)
			buf.append(" type=\""+type.toString()+"\"");
		if(opType!=null)
			buf.append(" opType=\""+opType.toString()+"\"");
		buf.append(">\n");
		buf.append(equation);
		buf.append('\n');

		for(DefVariable v:variables)
			buf.append(v.toString());
		for(Parameter p:parameters)
			buf.append(p.toString());
		for(Option op:options)
			buf.append(op.toString());

		buf.append("</definition>\n");
		return buf.toString();
	}

	public LsmpDef duplicate()
	{
		LsmpDef res = new LsmpDef(this.name,this.type,this.equation);
		res.setOpType(this.getOpType());
		for(DefVariable v:variables)
			res.variables.add(v.duplicate());
		for(Parameter p:parameters)
			res.parameters.add(p.duplicate());
		for(Option o:options)
			res.options.add(o.duplicate());
		return res;
	}
	public void setName(String name) {
		this.name = name;
	}
} // end if class

