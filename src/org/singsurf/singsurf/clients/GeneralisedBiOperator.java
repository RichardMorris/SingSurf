/*
Created 17 Sep 2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import org.singsurf.singsurf.Calculator;

public interface GeneralisedBiOperator {
	public void setIngredient1(Calculator inCalc);
    public void setIngredient2(Calculator inCalc);
	public boolean goodIngredients();
}
