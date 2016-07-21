/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;

import org.singsurf.singsurf.jep.EvaluationException;

/**
 * Abstract base class for all geometry operations. Handles redirection to sub types of PgGeometryIf.
 * @author Richard Morris
 *
 */
public abstract class AbstractOperator {

	public PgGeometryIf operate(PgGeometryIf geom) throws UnSuportedGeometryException, EvaluationException
	{
		if(geom instanceof PgElementSet)
			return operateSurface((PgElementSet) geom);
		if(geom instanceof PgPolygonSet)
			return operateCurve((PgPolygonSet) geom);
		if(geom instanceof PgPointSet)
			return operatePoints((PgPointSet) geom);
		throw new UnSuportedGeometryException("Bad geometry type: "+geom.getClass().getName());
	}
	
	abstract public PgGeometryIf operateSurface(PgElementSet geom) throws EvaluationException;
	abstract public PgGeometryIf operatePoints(PgPointSet geom) throws EvaluationException;
	abstract public PgGeometryIf operateCurve(PgPolygonSet geom) throws EvaluationException;
}
