/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;

import org.singsurf.singsurf.EvaluationException;

/**
 * Abstract base class for geometry operations which operate in place on a geometry.
 * Handles redirection to sub types of PgGeometryIf.
 * @author Richard Morris
 *
 */
public abstract class AbstractModifier {

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
	
	abstract public PgElementSet operateSurface(PgElementSet geom) throws EvaluationException;
	abstract public PgPointSet operatePoints(PgPointSet geom) throws EvaluationException;
	abstract public PgPolygonSet operateCurve(PgPolygonSet geom) throws EvaluationException;
}
