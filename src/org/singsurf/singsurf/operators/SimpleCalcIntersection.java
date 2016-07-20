/*
Created 27-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import jv.vecmath.PdVector;

import org.singsurf.singsurf.Calculator;
import org.singsurf.singsurf.EvaluationException;

public class SimpleCalcIntersection extends SimpleIntersect {
	Calculator calc;
	public SimpleCalcIntersection(Calculator calc) {
		this.calc = calc;
	}
	private SimpleCalcIntersection() {}
	@Override
	public double findValue(PdVector vec) throws EvaluationException {
		double[] v = calc.evalTop(vec.getEntries());
		return v[0];
	}

}
