/*
Created 29 Nov 2006 - Richard Morris
*/
package org.singsurf.singsurf;

import org.singsurf.singsurf.definitions.LsmpDef;

public class PolynomialCalculator extends Calculator {

	public PolynomialCalculator(LsmpDef def, int nderiv) {
		super(def, nderiv);
	}

	@Override
	public void setParamValue(String name, double val) {
		super.setParamValue(name, val);
		mj.setVarValue(name, val);
	}

}
