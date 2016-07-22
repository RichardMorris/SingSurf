/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import java.awt.Color;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.vecmath.PdVector;

import org.singsurf.singsurf.Calculator;
import org.singsurf.singsurf.jep.EvaluationException;

/**
 * @author Richard Morris
 *
 */
public class ColourCalcMap extends AbstractModifier {
	Calculator calc;
	@SuppressWarnings("unused")
    private ColourCalcMap() {	}
	public ColourCalcMap(Calculator calc) {
		this.calc = calc;
	}
	
	private float clipCol(double val) { 
		if(val<0) val = 0; 
		else if(val>1.0) val = 1.0; 
		return (float) val; 
	}
	@Override
	public PgPointSet operatePoints(PgPointSet geom) throws EvaluationException {
		geom.assureVertexColors();
		Color cols[] = geom.getVertexColors();
		for(int i=0;i<geom.getNumVertices();++i)
		{
			PdVector vec = geom.getVertex(i);
			//Color col = geom.getVertexColor(i);
			double topRes[] = calc.evalTop(vec.getEntries());
			cols[i] = new Color(clipCol(topRes[0]),clipCol(topRes[1]),clipCol(topRes[2]));

			//geom.setVertexColor(i,col);
		}
		return geom;
	}
	@Override
	public PgPolygonSet operateCurve(PgPolygonSet geom) throws EvaluationException {
		return (PgPolygonSet) operatePoints((PgPointSet) geom);
	}
	@Override
	public PgElementSet operateSurface(PgElementSet geom) throws EvaluationException {
		return (PgElementSet) operatePoints((PgPointSet) geom);
	}


}
