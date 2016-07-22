/*
Created 17 Sep 2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import org.singsurf.singsurf.Calculator;

public interface GeneralisedOperator {
	public void setIngredient(Calculator inCalc);
	public boolean goodIngredient();
}
