package org.singsurf.singsurf.operators;

import jv.vecmath.PdVector;

import org.singsurf.singsurf.Calculator;
import org.singsurf.singsurf.jep.EvaluationException;

public class CalcClip extends IntersectionClip {

    
    Calculator calc;
    public CalcClip(Calculator calc,int nItt) {
        super(nItt);
        this.calc = calc;
    }

    
    @Override
    public boolean testClip(PdVector vec) throws EvaluationException {
        double[] v = calc.evalTop(vec.getEntries());
        return v[0] >= 0;
    }


    @Override
    public double findValue(PdVector vec) throws EvaluationException {
        double[] v = calc.evalTop(vec.getEntries());
        return v[0];
    }

}
