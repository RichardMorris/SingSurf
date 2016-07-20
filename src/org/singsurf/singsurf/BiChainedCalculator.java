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
import org.singsurf.singsurf.jep.ExternalPartialDerivative;
import org.singsurf.singsurf.jep.ExternalVariable;

/**
 * A calculator which depends on two ingredients.
 * 
 * @author Richard Morris
 *
 */
public class BiChainedCalculator extends Calculator {
    Calculator ingredient1;
    Calculator ingredient2;
    DefVariable dependentVariable1 = null;
    DefVariable dependentVariable2 = null;
    ExternalVariable jepVar1 = null;
    ExternalVariable jepVar2 = null;
    int jepVarRef1;
    int jepVarRef2;

    public BiChainedCalculator(LsmpDef def, int nderiv) {
        super(def, nderiv);
        mj = (MatrixJep) mj.newInstance(new DSymbolTable(
                new BiChainedVariableFactory()));
        mj.setAllowAssignment(true);
        mj.setAllowUndeclared(true);
        mj.setImplicitMul(true);
        mj.addComplex();
        mj.addStandardConstants();
        mj.addStandardFunctions();
        mj.addStandardDiffRules();
        mrpe = new MRpEval(mj);
    }

    List<Integer> derivMrpeRefs1 = null;
    List<Integer> derivMrpeRefs2 = null;
    int jepNormVarRef1, jepNormVarRef2;

    @Override
    public void build() {
        DefType optype = this.definition.getOpType();

        List<DefVariable> var = this.definition.getVariablesByType(optype);
        if (var.size() != 2) {
            this.msg = "Definition must have exactly two variable of type "
                    + optype.toString();
            this.good = false;
            return;
        }
        dependentVariable1 = var.get(0);
        dependentVariable2 = var.get(1);
        jepVar1 = new ExternalVariable(this, dependentVariable1.getName(), 3);
        jepVar2 = new ExternalVariable(this, dependentVariable2.getName(), 3);
        super.build();
        if (!good)
            return;
        try {
            List<DefVariable> normalVars = this.definition
                    .getVariablesByType(DefType.none);

            Variable normVar1 = mj.getVar(normalVars.get(0).getName());
            if (normVar1 == null) {
                jepNormVarRef1 = -1;
                // mj.addVariable(normalVars.get(0).getName(), 0.0);
            } else
                jepNormVarRef1 = mrpe.getVarRef(normVar1);
            Variable normVar2 = mj.getVar(normalVars.get(1).getName());
            if (normVar2 == null)
                jepNormVarRef2 = -1;
            else
                jepNormVarRef2 = mrpe.getVarRef(normVar2);
            jepVarRef1 = mrpe.getVarRef((MatrixVariableI) jepVar1);
            jepVarRef2 = mrpe.getVarRef((MatrixVariableI) jepVar2);
            derivMrpeRefs1 = new ArrayList<Integer>();
            derivMrpeRefs2 = new ArrayList<Integer>();
            int dnum1 = 0;
            for (Enumeration<?> en = jepVar1.allDerivatives(); en
                    .hasMoreElements();) {
                Object o = en.nextElement();
                ExternalPartialDerivative diff = (ExternalPartialDerivative) o;
                int ref = mrpe.getVarRef((MatrixVariableI) diff);
                derivMrpeRefs1.add(dnum1, ref);
                ++dnum1;
            }
            int dnum2 = 0;
            for (Enumeration<?> en = jepVar2.allDerivatives(); en
                    .hasMoreElements();) {
                Object o = en.nextElement();
                ExternalPartialDerivative diff = (ExternalPartialDerivative) o;
                int ref = mrpe.getVarRef((MatrixVariableI) diff);
                derivMrpeRefs2.add(dnum2, ref);
                ++dnum2;
            }
        } catch (ParseException e) {
            this.good = false;
            this.msg = e.getMessage();
        }
        if (ingredient1 != null)
            buildIngr1();
        if (ingredient2 != null)
            buildIngr2();

    }

    // private Calculator getIngredient() { throw new RuntimeException("Fail");
    // }

    public Calculator getIngredient1() {
        return ingredient1;
    }

    public Calculator getIngredient2() {
        return ingredient2;
    }

    /** Translate number of derivative to reference in ingredient */
    List<Integer> derivTrans1;
    List<Integer> derivTrans2;

    public void buildIngr1() {
        derivTrans1 = new ArrayList<Integer>();
        int dnum = 0;
        Enumeration<?> e1 = jepVar1.allDerivatives();
        while (e1.hasMoreElements()) { /* for each derivative ... */
            Object o = e1.nextElement();
            MatrixPartialDerivative diff = (MatrixPartialDerivative) o;
            String dnames[] = diff.getDnames();
            String ingrNames[] = new String[dnames.length];
            List<DefVariable> normalVars = this.definition
                    .getVariablesByType(DefType.none);
            /** translate names used here to those used by the ingredient */
            for (int i = 0; i < dnames.length; ++i) {
                int pos = normalVars.indexOf(definition.getVariable(dnames[i]));
                if (pos != 0) {
                    System.out.println("Bad deriv varialbe");
                    good = false;
                }
                ingrNames[i] = ingredient1.getInputVariableName(0);
            }
            int ref = ingredient1.requireDerivative(ingrNames);
            derivTrans1.add(dnum, ref);
            ++dnum;
        }
    }

    public void buildIngr2() {

        derivTrans2 = new ArrayList<Integer>();
        int dnum = 0;
        Enumeration<?> e2 = jepVar2.allDerivatives();
        while (e2.hasMoreElements()) { /* for each derivative ... */
            Object o = e2.nextElement();
            MatrixPartialDerivative diff = (MatrixPartialDerivative) o;
            String dnames[] = diff.getDnames();
            String ingrNames[] = new String[dnames.length];
            List<DefVariable> normalVars = this.definition
                    .getVariablesByType(DefType.none);
            /** translate names used here to those used by the ingredient */
            for (int i = 0; i < dnames.length; ++i) {
                int pos = normalVars.indexOf(definition.getVariable(dnames[i]));
                if (pos != 1) {
                    System.out.println("Bad deriv varialbe");
                    good = false;
                }
                ingrNames[i] = ingredient2.getInputVariableName(0);
            }
            int ref = ingredient2.requireDerivative(ingrNames);
            derivTrans2.add(dnum, ref);
            ++dnum;
        }
    }

    public void setIngredient1(Calculator ingredient) {
        this.ingredient1 = ingredient;
        build();
    }

    public void setIngredient2(Calculator ingredient) {
        this.ingredient2 = ingredient;
        build();
    }

    class BiChainedVariableFactory extends MatrixVariableFactory {

        @Override
        public Variable createVariable(String name, Object value) {
            if (dependentVariable1 != null
                    && name.equals(dependentVariable1.getName()))
                return jepVar1;
            if (dependentVariable2 != null
                    && name.equals(dependentVariable2.getName()))
                return jepVar2;
            else
                return super.createVariable(name, value);
        }

        @Override
        public Variable createVariable(String name) {
            if (dependentVariable1 != null
                    && name.equals(dependentVariable1.getName()))
                return jepVar1;
            if (dependentVariable2 != null
                    && name.equals(dependentVariable2.getName()))
                return jepVar2;
            else
                return super.createVariable(name);
        }

    }

    double igr1in[] = new double[1];
    double igr2in[] = new double[2];

    @Override
    public double[] evalTop(double[] in) throws EvaluationException {
        try {
            igr1in[0] = in[0];
            igr2in[0] = in[1];
            if (jepNormVarRef1 >= 0)
                mrpe.setVarValue(jepNormVarRef1, in[0]);
            if (jepNormVarRef2 >= 0)
                mrpe.setVarValue(jepNormVarRef2, in[1]);
            double[] ingrRes1 = ingredient1.evalTop(igr1in);
            mrpe.setVarValue(jepVarRef1, ingrRes1);
            for (int i = 0; i < this.derivMrpeRefs1.size(); ++i) {
                double[] derivRes = ingredient1.evalDerivative(this.derivTrans1
                        .get(i));
                mrpe.setVarValue(this.derivMrpeRefs1.get(i), derivRes);
            }

            double[] ingrRes2 = ingredient2.evalTop(igr2in);
            mrpe.setVarValue(jepVarRef2, ingrRes2);
            for (int i = 0; i < this.derivMrpeRefs2.size(); ++i) {
                double[] derivRes = ingredient2.evalDerivative(this.derivTrans2
                        .get(i));
                mrpe.setVarValue(this.derivMrpeRefs2.get(i), derivRes);
            }

            for (MRpCommandList com : allComs)
                mrpe.evaluate(com);

            MRpRes res = mrpe.evaluate(topCom);
            double v[] = (double[]) res.toArray();
            return v;

        } catch (EvaluationException e) {
            throw e;
        } catch (Exception e) {
            throw new EvaluationException(e);
        }

    }

    public boolean goodIngredients() {
        boolean g0 = super.isGood();
        boolean g1 = this.ingredient1 == null ? false : this.ingredient1
                .isGood();
        boolean g2 = this.ingredient2 == null ? false : this.ingredient2
                .isGood();
        System.out.println("BiCh " + g0 + " " + g1 + " " + g2);
        return g0 && g1 && g2;
    }
}
