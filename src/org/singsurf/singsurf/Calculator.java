package org.singsurf.singsurf;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.lsmp.djep.djep.DPrintVisitor;
import org.lsmp.djep.matrixJep.MatrixJep;
import org.lsmp.djep.matrixJep.MatrixVariableI;
import org.lsmp.djep.matrixJep.nodeTypes.MatrixNodeI;
import org.lsmp.djep.mrpe.MRpCommandList;
import org.lsmp.djep.mrpe.MRpEval;
import org.lsmp.djep.mrpe.MRpRes;
import org.lsmp.djep.vectorJep.values.Scaler;
import org.lsmp.djep.xjep.PrintVisitor;
import org.lsmp.djep.xjep.XJep;
import org.lsmp.djep.xjep.XSymbolTable;
import org.lsmp.djep.xjep.XVariable;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.LsmpDef;
import org.singsurf.singsurf.definitions.Parameter;
import org.singsurf.singsurf.jep.CDiv;
import org.singsurf.singsurf.jep.CMul;
import org.singsurf.singsurf.jep.EvaluationException;
import org.singsurf.singsurf.jep.ExternalPartialDerivative;
import org.singsurf.singsurf.jep.ExternalVariable;
import org.singsurf.singsurf.jep.MinMax;

public class Calculator {
	/** The MatrixJep instance */
	protected MatrixJep mj=null;
	/** The mrpe instance */
	MRpEval mrpe = null;

	/** The definition of the mapping */
	LsmpDef definition;

	/** The top node of the equation set */
	MatrixNodeI top=null;
	
	/** The command list for the top eqn */
	MRpCommandList topCom;
	/** The command list for derivatives */
	List<MRpCommandList> derivComs=new ArrayList<MRpCommandList>();
	/** The command list for normals */
	MRpCommandList normalComs=null;
	/** The command list for colours */
	MRpCommandList colourCom=null;
	/** The command list for subsequent equations */
	List<MRpCommandList> allComs = new ArrayList<MRpCommandList>();

	//MatrixNodeI firstDerivs[];
	//MRpCommandList firstDerivComs[];
	int derivOrder = 0;

	/** The raw list of equations */
	Vector<Node> rawEqns;
	/** All variables found in equations */
	Vector<?> depVars;
	
	/** mrpe reference numbers for each DefVariable, indexed by posn in definition */
	int variableRefs[];

	/** Jep variable, indexed by posn in definition */
	protected MatrixVariableI[] jepVars=null;

	/** mrpe reference numbers for each parameter */
	Map<String,Integer> paramRefs=new HashMap<String,Integer>();

	/** Index for derivatives */
	Map<String [],Integer> derivativesIndex = new HashMap<String[],Integer>();
	
	/** Equations for derivatives */
	List<Node> derivivativeEquations = new ArrayList<Node>();

	/** The dimension the input required */
	int inputDim=0;

	/** Can the calculator be used? */
	boolean good=false;
	/** Error message on error */
	protected String msg=null;
	
	@SuppressWarnings("unused")
    private Calculator() {}
	
	public Calculator(LsmpDef def,int nderiv) {
		definition = def;
		mj = new MatrixJep();
		mj.setAllowAssignment(true);
		mj.setAllowUndeclared(true);
		mj.setImplicitMul(true);
		mj.addComplex();
		mj.addStandardConstants();
		mj.addStandardFunctions();
		mj.addStandardDiffRules();
		mrpe = new MRpEval(mj);
		variableRefs = new int[def.getNumVars()];
		jepVars = new MatrixVariableI[def.getNumVars()];
		derivOrder = nderiv;
        CMul cmul = new CMul();
        CDiv cdiv = new CDiv(cmul);
        mj.addFunction("cmul",cmul);
        mj.addFunction("cdiv",cdiv);
        mj.addFunction("min", new MinMax(true));
        mj.addFunction("max", new MinMax(false));
//		if(nderiv==1)
//			firstDerivs = new MatrixNodeI[def.getNumVars()];
	}

	/**
	 * Builds all the necessary components from a definition.
	 * 
	 * @param def
	 */
	public void build() {
		good = false; msg = null;
		
		DefType type = this.definition.getType();
		if(type==DefType.psurf) { this.inputDim = 2;	}
		else if(type==DefType.pcurve) { this.inputDim = 1; }
		else this.inputDim = 3;

		try {
			parseDef();
			if(this.rawEqns.size()==0)
			    return;
			buildKeyEqns();
			buildDepVars();
			buildCommands();
			buildDerivatives();
			good = true;
		}
		catch(ParseException e) {
			msg = e.getMessage();
		}
		//printEquationsAndVariables();
	}
	/** Parse the definition producing a list of equations */ 
	void parseDef() throws ParseException
	{
		mj.getSymbolTable().clearNonConstants();
		mj.restartParser(definition.getEquation());
		//mj.getPrintVisitor().setMode(DPrintVisitor.PRINT_PARTIAL_EQNS,false);

		Vector<Node> v1= new Vector<Node>();
		Node n;
		while((n = mj.continueParsing())!=null)
			v1.add(n);
		rawEqns = new Vector<Node>();
		for(int i=v1.size()-1;i>=0;--i)
		{
			Node n1 = v1.elementAt(i);
			//System.out.print("Node i: ");
			mj.println(n1);
			Node n2 = mj.simplify(mj.preprocess(n1));
			rawEqns.add(n2);
		}
	}
	
	/** Finds the top equation and equations for first derivatives if required. 
	 * TODO higher derivatives, equations for colours and normals
	 * */
	void buildKeyEqns() throws ParseException
	{
		top = (MatrixNodeI) rawEqns.elementAt(rawEqns.size()-1);
	}
	
	void printEquationsAndVariables()
	{
		if(!good) { System.out.println("Calculator is corrupt: " + msg ); return;}
		System.out.print("Top\t");
		mj.println(top);
		PrintVisitor pv = ((XJep) mj).getPrintVisitor();
		pv.setMode(DPrintVisitor.PRINT_PARTIAL_EQNS,false);
		for(String[] names:this.derivativesIndex.keySet()) {
			int index = this.derivativesIndex.get(names);
			for(int i=0;i<names.length;++i)
				System.out.print("D"+names[i]);
			System.out.print("\t");
			mj.println(this.derivivativeEquations.get(index));
		}
		XSymbolTable st = (XSymbolTable) mj.getSymbolTable();
		st.print(pv);

		System.out.println("Variables:");
		for(Enumeration  loop = st.keys();loop.hasMoreElements();)
		{
			String s = (String) loop.nextElement();
			XVariable var = (XVariable) st.getVar(s);
			System.out.println("\t"+var.toString(pv));
		}
		pv.setMode(DPrintVisitor.PRINT_PARTIAL_EQNS,true);
	}
	
	/**
	 * Finds the dependency table for variable.
	 * @throws ParseException
	 */
	void buildDepVars() throws ParseException
	{
		depVars = mj.recursiveGetVarsInEquation(top,new Vector());
	}
	
	void extendDepVars(Node eqn) throws ParseException
	{
		int oldSize = depVars.size();
		depVars = mj.recursiveGetVarsInEquation(eqn,depVars);
		for(int i=oldSize;i<depVars.size();++i)
		{
			MatrixVariableI var = (MatrixVariableI) depVars.get(i);
			MRpCommandList com = mrpe.compile(var,var.getEquation());
			allComs.add(com);
		}
	}
	/**
	 * Finds the MRpCommandLists, assigns references to definitions variables and parameters.
	 * @throws ParseException
	 */
	void buildCommands() throws ParseException
	{
		this.allComs.clear();
		this.paramRefs.clear();
		
		for(int i=0;i<variableRefs.length;++i) {
		    variableRefs[i] = -1;
		    jepVars[i] = null;
		}
		for(Enumeration en=depVars.elements();en.hasMoreElements();)
		{
			MatrixVariableI var = (MatrixVariableI) en.nextElement();

			DefVariable defVariable = definition.getVariable(var.getName());
			if(defVariable!=null)
			{
				int index = definition.getVariableIndex(defVariable);
				variableRefs[index] = mrpe.getVarRef(var);
				jepVars[index] = var;
			}
			else if(var.isConstant()) {}
			else if(var.hasEquation()) {
				System.out.print(var.getName()+":=");
				mj.println(var.getEquation());
				MRpCommandList com = mrpe.compile(var,var.getEquation());
				allComs.add(com);
			}
			else if(var instanceof ExternalPartialDerivative || var instanceof ExternalVariable) {
				// These are handles but the subclass
			}
			else // Not a variable, does not have an equation so must be a parameter 
			{
				// Check if this parameter already exists
				Parameter p = definition.getParameter(var.getName());
				if(p==null)
					p = definition.addParameter(var.getName());
				var.setMValue(Scaler.getInstance(new Double(p.getVal())));
				int ref = mrpe.getVarRef(var);
				paramRefs.put(p.getName(),new Integer(ref));
			}
		}
		//mj.println(top);
		topCom = mrpe.compile(top);
		
/*		if(derivOrder == 1)
		{
			firstDerivComs = new MRpCommandList[firstDerivs.length];
			for(int i=0;i<firstDerivs.length;++i)
			{
				firstDerivComs[i] = mrpe.compile(firstDerivs[i]);
			}
		}
*/
		definition.setParamNames(paramRefs.keySet().toArray());
	}
	
	void buildDerivatives() {
		this.derivativesIndex.clear();
		this.derivComs.clear();
		this.derivivativeEquations.clear();

		if(derivOrder == 1)
			for(int i=0;i<getNumInputVariables();++i)
				requireDerivative(new String[]{getInputVariableName(i)});
	}

	/** Evaluate the top equation 
	 * @throws ParseException */
	public double[] evalTop(double in[]) throws EvaluationException
	{
		double v[];
        try {
            for(int i=0;i<inputDim;++i) {
                if(variableRefs[i] >=0 )
                    mrpe.setVarValue(variableRefs[i],in[i]);
            }

            for(MRpCommandList com:allComs)
            	mrpe.evaluate(com);
            
            MRpRes res = mrpe.evaluate(topCom);
            v = (double []) res.toArray();
        } catch (Exception e) {
            throw new EvaluationException(e);
        }
		return v;
	}
	
	/** Evaluates a first derivative. */
	public double[] evalDerivative(int i)
	{
		MRpRes res = mrpe.evaluate(derivComs.get(i));
		return (double []) res.toArray();
	}

	/** Sets the name of the definition */
	public void setName(String name)
	{
		definition.setName(name);
	}
	/** Sets the main text of the definition */
	public void setEquation(String s)
	{
		definition.setEquation(s);
		build();
	}
	/** Sets the value of an input variable */
	public void setVarValue(int n,double val)
	{
		if(isGood())
			mrpe.setVarValue(variableRefs[n],val);
	}
	public void setVarBounds(int n,double min,double max,int steps)
	{
		definition.getVar(n).setBounds(min,max,steps);
	}
	public void setParamValue(String name,double val)
	{
		if(isGood())
			mrpe.setVarValue(paramRefs.get(name).intValue(),val);
		definition.setParameterValue(name,val);
	}
	public DefVariable getDefVariable(int i) {
		return definition.getVar(i).duplicate();
	}
	public int getNumInputVariables() {
		return definition.getNumVars();
	}
	public String getInputVariableName(int i) {
		return definition.getVar(i).getName();
	}
	public int getNParam() {
		return definition.getNumParams();
	}
	public Parameter getParam(int i) {
		return definition.getParam(i).duplicate();
	}
	
	public List<Parameter> getParams() {
		return definition.getParams();
	}
	public boolean isGood() {return good;}
	public String getMsg() { return msg; }
	public LsmpDef getDefinition() {
		return definition.duplicate();
	}

	/**
	 * Requires that the calculator can evaluate the given derivative.
	 * If necessary will calculate the required derivative.
	 * @param names
	 * @return the index number for the derivative
	 */
	public int requireDerivative(String[] names) {
		Integer index = getDerivative(names);
		return index;
	}

	int getDerivative(String[] names) {
		Integer index = derivativesIndex.get(names);
		if(index!=null) return index;
		try {
			Node node;
			if(names.length==1) {
				node = mj.differentiate(top, names[0]);
			}
			else
			{	
				String[] subNames = new String[names.length-1];
				System.arraycopy(names, 0, subNames, 0, names.length-1);
				int lowerIndex = getDerivative(subNames);
				Node lower = derivivativeEquations.get(lowerIndex);
				node = mj.differentiate(lower, names[names.length-1]);
			}
			derivivativeEquations.add(node);
			int i = derivivativeEquations.size()-1;
			derivativesIndex.put(names, i);
			extendDepVars(node);
			MRpCommandList com = mrpe.compile(node);
			derivComs.add(i,com);
			return i;
		} catch (ParseException e) {
			good = false;
			msg = e.getMessage();
			return -1;
		}
	}
	public Vector<Node> getRawEqns() {
		return rawEqns;
	}
	
	public MatrixJep getJep() { return mj; }

    public void setGood(boolean b) {
       this.good = b;
    }
}
