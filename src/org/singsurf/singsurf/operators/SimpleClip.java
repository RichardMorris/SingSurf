/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.object.PsObject;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

public abstract class SimpleClip extends AbstractModifier {

	protected boolean goodVerts[];
	public SimpleClip() {
		super();
		// TODO Auto-generated constructor stub
	}

	public boolean findGoodVerts(PgPointSet geom)
	{
		boolean allGood=true;
		int nVert=geom.getNumVertices();
		goodVerts = new boolean[nVert];
		for(int i=0;i<nVert;++i)
		{
			goodVerts[i] = testClip(geom.getVertex(i));
			if(!goodVerts[i]) {
				allGood = false;
				geom.setTagVertex(i,PsObject.IS_DELETED);
			}
			
		}
		return allGood;
	}

	@Override
	public PgPointSet operatePoints(PgPointSet geom) {
		boolean allGood = findGoodVerts(geom);
		if(allGood) return geom;
		geom.removeMarkedVertices();
		return geom;
	}

	@Override
	public PgElementSet operateSurface(PgElementSet geom) {
		
		boolean allGood = findGoodVerts(geom);
		if(allGood) return geom;
		geom.removeMarkedVertices();
		return geom;
	}

	@Override
	public PgPolygonSet operateCurve(PgPolygonSet geom) {
		boolean allGood = findGoodVerts(geom);
		if(allGood) return geom;
		int newIndices[] = geom.removeMarkedVertices();
		for(int i=0;i<geom.getNumPolygons();++i)
		{
			PiVector eles = geom.getPolygon(i);
			PiVector newEles = new PiVector();
			for(int j=0;j<eles.getSize();++j) {
				int index = eles.getEntry(j);
				if(goodVerts[index])
					newEles.addEntry(newIndices[index]);
			}
			if(newEles.getSize()>0)
				geom.setPolygon(i,newEles);
			else
				geom.setTagPolygon(i,PsObject.IS_DELETED);
		}
		geom.removeMarkedPolygons();
		return geom;
	}

	public abstract boolean testClip(PdVector vec);
}
