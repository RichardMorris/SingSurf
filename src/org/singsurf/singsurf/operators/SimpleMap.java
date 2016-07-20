/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.vecmath.PdVector;

import org.singsurf.singsurf.EvaluationException;

public abstract class SimpleMap extends AbstractModifier {

	@Override
	public PgElementSet operateSurface(PgElementSet geom) throws EvaluationException {
		return (PgElementSet) operatePoints((PgPointSet) geom);
	}

	@Override
	public PgPolygonSet operateCurve(PgPolygonSet geom) throws EvaluationException {
		return (PgPolygonSet) operatePoints((PgPointSet) geom);
	}

	@Override
	public PgPointSet operatePoints(PgPointSet geom) throws EvaluationException {
		for(int i=0;i<geom.getNumVertices();++i)
		{
			PdVector vec = map(geom.getVertex(i));
			geom.setVertex(i,vec);
		}
		return geom;
	}

	public abstract PdVector map(PdVector vec) throws EvaluationException;
}
