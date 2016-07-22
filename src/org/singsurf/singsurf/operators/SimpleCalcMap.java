/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import jv.vecmath.PdVector;

import org.singsurf.singsurf.Calculator;
import org.singsurf.singsurf.jep.EvaluationException;

/**
 * @author Richard Morris
 *
 */
public class SimpleCalcMap extends SimpleMap {
	Calculator calc;
	@SuppressWarnings("unused")
    private SimpleCalcMap() {	}
	public SimpleCalcMap(Calculator calc) {
		this.calc = calc;
	}
	/* (non-Javadoc)
	 * @see org.singsurf.singsurf.operators.SimpleMap#map(jv.vecmath.PdVector)
	 */
	@Override
	public PdVector map(PdVector vec) throws EvaluationException {
		double topRes[] = calc.evalTop(vec.getEntries());
		PdVector out = new PdVector(topRes);
		return out;
	}

}
