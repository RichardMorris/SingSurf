/*
Created 17 Sep 2006 - Richard Morris
*/
package org.singsurf.singsurf;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.lsmp.djep.djep.DSymbolTable;
import org.lsmp.djep.matrixJep.MatrixJep;
import org.lsmp.djep.matrixJep.MatrixPartialDerivative;
import org.lsmp.djep.matrixJep.MatrixVariableFactory;
import org.lsmp.djep.matrixJep.MatrixVariableI;
import org.lsmp.djep.mrpe.MRpCommandList;
import org.lsmp.djep.mrpe.MRpEval;
import org.lsmp.djep.mrpe.MRpRes;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.Variable;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.LsmpDef;
import org.singsurf.singsurf.jep.EvaluationException;
import org.singsurf.singsurf.jep.ExternalPartialDerivative;
import org.singsurf.singsurf.jep.ExternalVariable;

/**
 * A calculator which depends on a set of ingredients.
 * @author Richard Morris
 *
 */
public class ChainedCalculator extends Calculator {
	Calculator ingredient;
	DefVariable dependentVariable=null;
	ExternalVariable jepVar=null;
	int jepVarRef;

	public ChainedCalculator(LsmpDef def, int nderiv) {
		super(def, nderiv);
		mj = (MatrixJep) mj.newInstance(new DSymbolTable(new ChainedVariableFactory()));
		mj.setAllowAssignment(true);
		mj.setAllowUndeclared(true);
		mj.setImplicitMul(true);
		mj.addComplex();
		mj.addStandardConstants();
		mj.addStandardFunctions();
		mj.addStandardDiffRules();
		mrpe = new MRpEval(mj);
	}

	List<Integer> derivMrpeRefs=null;
	@Override
	public void build() {
		DefType type = this.definition.getOpType();
		if(type == null) {
            this.msg = "OpType must be specified it is null";
            this.good = false;
            return;
		}
		List<DefVariable> var = this.definition.getVariablesByType(type);
		if(var.size() != 1) {
			this.msg = "Definition must have exactly one variable of type " + type.toString();
			this.good = false;
			return;
		}
		dependentVariable = var.get(0);
		if(type == DefType.psurf)
		    jepVar = new ExternalVariable(this,dependentVariable.getName(),3);
		else if(type == DefType.asurf)
            jepVar = new ExternalVariable(this,dependentVariable.getName(),1);
		else {
		    this.msg = "OpType must be asurf or psurf its is "+type;
            this.good = false;
            return;
		}
		
		super.build();
		if(!good) return;
		try {
			jepVarRef = mrpe.getVarRef((MatrixVariableI)jepVar);
			derivMrpeRefs = new ArrayList<Integer>();
			int dnum=0;
			for(Enumeration en=jepVar.allDerivatives();en.hasMoreElements();)
			{
				Object o = en.nextElement();
				ExternalPartialDerivative diff = (ExternalPartialDerivative) o;
					int ref = mrpe.getVarRef((MatrixVariableI) diff);
					derivMrpeRefs.add(dnum,ref);
					++dnum;
		}
		} catch (ParseException e) {
			this.good = false;
			this.msg = e.getMessage();
		}
		
		if(ingredient == null) { 
		    this.good = false;
		    return;
		}
	      Enumeration e = jepVar.allDerivatives();
	        derivTrans = new ArrayList<Integer>();
	        int dnum=0;
	        while(e.hasMoreElements()) { /* for each derivative ... */
	            Object o = e.nextElement();
	            MatrixPartialDerivative diff = (MatrixPartialDerivative) o;
	            String dnames[] = diff.getDnames();
	            String ingrNames[] = new String[dnames.length];
	            List<DefVariable> normalVars = this.definition.getVariablesByType(DefType.none);
	            /** translate names used here to those used by the ingredient */
	            for(int i=0;i<dnames.length;++i){
	                int pos = normalVars.indexOf(definition.getVariable(dnames[i]));
	                ingrNames[i] = ingredient.getInputVariableName(pos);
	            }
	            int ref =ingredient.requireDerivative(ingrNames);
	            derivTrans.add(dnum,ref);
	            ++dnum;
	        }
	        
	}

	public Calculator getIngredient() {
		return ingredient;
	}
	/** Translate number of derivative to reference in ingredient */
	List<Integer> derivTrans;
	public void setIngredient(Calculator ingredient) {
		this.ingredient = ingredient;
		build();
	}

	private class ChainedVariableFactory extends MatrixVariableFactory {

		@Override
		public Variable createVariable(String name, Object value) {
			if(dependentVariable!=null && name.equals(dependentVariable.getName()))
				return jepVar;
			else
				return super.createVariable(name, value);
		}

		@Override
		public Variable createVariable(String name) {
			if(dependentVariable!=null && name.equals(dependentVariable.getName()))
				return jepVar;
			else
				return super.createVariable(name);
		}
		
	}

    @Override
    public double[] evalTop(double[] in) throws EvaluationException {
        double[] ingrRes = ingredient.evalTop(in);
        try {
            mrpe.setVarValue(jepVarRef, ingrRes);
            for (int i = 0; i < this.derivMrpeRefs.size(); ++i) {
                double[] derivRes = ingredient.evalDerivative(this.derivTrans
                        .get(i));
                mrpe.setVarValue(this.derivMrpeRefs.get(i), derivRes);
            }

            for (MRpCommandList com : allComs)
                mrpe.evaluate(com);

            MRpRes res = mrpe.evaluate(topCom);
            double v[] = (double[]) res.toArray();
            return v;
        } catch (Exception e) {
            throw new EvaluationException(e);
        }

    }
	
	public boolean goodIngredient() {
		return super.isGood() && this.ingredient != null && this.ingredient.isGood();
	}
}
