/*
Created 25-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import java.util.HashMap;
import java.util.Map;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

/**
 * Abstract base class for intersection methods.
 * @author Richard Morris
 */
public abstract class CalcExtrude extends AbstractOperator {
	protected static final boolean PRINTDEBUG=false;
	protected int dim;
	protected PgGeometryIf inGeom,outGeom;
	
	Map<Pair,Integer> pairSolutions = new HashMap<Pair,Integer>(); 
	
	protected void setup(PgGeometryIf geom)
	{
		this.inGeom = geom;
		dim = geom.getDimOfVertices();
	}
	
	protected void tidyUp()
	{
		inGeom=null; 
	}
	

	protected void extrudePolygon(PiVector element)
	{
		int len = element.getSize();
		int crossingPoints[] = new int[len];
		int crossCount=0;
		for(int i=0;i<element.getSize();++i)
		{
			int a = element.getEntry(i);
			int b = element.getEntry((i+1)%element.getSize());
			if(testIntersection(a,b))
			{	
				int crossing = findIntersection(a,b);
				//elementSwitch[crossCount]=i-1;
				crossingPoints[crossCount]=crossing;
				++crossCount;
			}
		}
	}

	@Override
	public PgPolygonSet operatePoints(PgPointSet geom) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PgElementSet operateCurve(PgPolygonSet geom) {
		setup(geom);
		outGeom = new PgElementSet(dim);
	    pairSolutions.clear();
		for(int i=0;i<geom.getNumPolygons();++i)
		{
			extrudePolygon(geom.getPolygon(i));
		}
		tidyUp();
		return null;
	}

	@Override
    public PgElementSet operateSurface(PgElementSet geom) {
		setup(geom);
		outGeom = new PgPolygonSet(dim);
		pairSolutions.clear();
		for(int i=0;i<geom.getNumElements();++i)
		{
			PiVector lines[] = intersectFace(geom.getElement(i));
			for(int j=0;j<lines.length;++j)
			{
				outGeom.addPolygon(lines[j]);
			}
		}
		tidyUp();
		return (PgElementSet) outGeom;
	}

	private PiVector[] intersectFace(PiVector element) {
        // TODO Auto-generated method stub
        return null;
    }

    /** Tests whether there is an intersection between two points in the original geometry.
	 * 
	 * @param a index of first point
	 * @param b index of second point
	 * @return true if there is an intersection in [a,b)
	 */
	public boolean testIntersection(int a,int b)
	{
		return testIntersection(inGeom.getVertex(a),inGeom.getVertex(b));
	}
	/**
	 * Tests whether there is an intersection between two points.
	 * @param A
	 * @param B
	 * @return true if there is an intersection
	 */
	public abstract boolean testIntersection(PdVector A,PdVector B);
	
	/**
	 * Finds the intersection between two points on the original geometry.
	 * A hashtable is used to store previously found crossings.
	 * If no solution is found calculateIntersection will be called and 
	 * the point will be added to the result geometry.
	 * @param a index of first point in original geometry.
	 * @param b index of second point in original geometry.
	 * @return index of point in result geometry or -1 on error
	 */
	protected int findIntersection(int a,int b) {
		Pair p = new Pair(a,b);
		Integer index = (Integer) pairSolutions.get(p);
		if(index!=null) return index;
		PdVector C = calculateIntersection(a,b);
		if(C==null)
		{
			pairSolutions.put(p,new Integer(-1));
			return -1;
		}
		index = outGeom.addVertex(C);
		pairSolutions.put(p,new Integer(index));
		return index;
	}

	/**
	 * Calculates the intersection between two point on the original geometry.
	 * @param a index of first point
	 * @param b index of second point
	 * @return the intersection point or null on error
	 */
	protected PdVector calculateIntersection(int a,int b)
	{
		return calculateIntersection(inGeom.getVertex(a),inGeom.getVertex(b));
	}
	/**
	 * Calculates the crossing value from
	 * @param A input point
	 * @param B input point
	 * @return the intersection point
	 */
	protected abstract PdVector calculateIntersection(PdVector A,PdVector B);
}
