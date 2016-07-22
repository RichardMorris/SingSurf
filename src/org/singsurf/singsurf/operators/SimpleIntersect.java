/*
Created 26-Apr-2006 - Richard Morris
*/
package org.singsurf.singsurf.operators;

import jv.geom.PgPointSet;
import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;

import org.singsurf.singsurf.jep.EvaluationException;

/**
 * Base class for intersection algorithms which test zero crossing of a real valued function.
 * @author Richard Morris
 *
 */
public abstract class SimpleIntersect extends AbstractIntersect {
    int nItterations = 5; 

    protected double vertexVals[];
	//protected int vertexSigns[];

	@Override
	protected void setup(PgGeometryIf geom) throws EvaluationException {
		super.setup(geom);
		int n=((PgPointSet)geom).getNumVertices();
		vertexVals = new double[n];
		for(int i=0;i<n;++i)
			vertexVals[i] = findValue(geom.getVertex(i));
	}

	@Override
	protected void tidyUp() {
		vertexVals = null;
		super.tidyUp();
	}

	@Override
	public boolean testIntersection(int a, int b) {
		if(vertexVals[a]==0) return true;
		if(vertexVals[b]==0) return false;
		return((vertexVals[a]>0 && vertexVals[b] <0)
		     ||(vertexVals[a]<0 && vertexVals[b] >0));
	}

	/* (non-Javadoc)
	 * @see org.singsurf.singsurf.operators.AbstractIntersect#testCrossing(jv.vecmath.PdVector, jv.vecmath.PdVector)
	 */
	@Override
	public boolean testIntersection(PdVector A, PdVector B) throws EvaluationException {
		double valA = findValue(A);
		double valB = findValue(B);
		return(valA==0||(valA>0 && valB <0)
			     ||(valA<0 && valB >0));
	}


	@Override
	protected PdVector calculateIntersection(int a, int b) throws EvaluationException {
		return calculateIntersection(
				inGeom.getVertex(a),vertexVals[a],
				inGeom.getVertex(b),vertexVals[b]);
	}
	@Override
	public PdVector calculateIntersection(PdVector A, PdVector B) throws EvaluationException {
		return calculateIntersection(A,findValue(A),B,findValue(B));
	}

	/**
	 * Calculates intersection between two points, with given function values.
	 * Uses a linear interpolation between the points.
	 * @param A first point
	 * @param aVal function value for first point
	 * @param B second point
	 * @param bVal function value for second point
	 * @return the intersection point or null if it cannot be found
	 * @throws EvaluationException 
	 */
	protected PdVector calculateIntersection(PdVector A,double aVal,PdVector B,double bVal) throws EvaluationException {
		if(aVal==0.0) return (PdVector) A.clone();
		if(bVal==0.0) return null;
		if(aVal*bVal>0.0) return null;
		PdVector C= new PdVector(dim);
        PdVector H,L;
        double cVal;
        if(aVal > 0.0) {
            H = A; L = B;
        } else {
            H = B; L = A;
            cVal = aVal; aVal = bVal; bVal = cVal;
        }
        for(int i=nItterations;i>=0;--i) {
            double lambda = - bVal / ( aVal - bVal);
            C.blend(lambda,H,1-lambda,L);
            if(i==0) break;
            
            cVal = findValue(C);
            if(cVal > 0.0) {
                H = C; aVal = cVal;
            } else {
                L = C; bVal = cVal;
            }
            C= new PdVector(dim);
        }
		return C;
	}


	/**
	 * Calculates the function value at a give point.
	 * @param vec
	 * @return the value
	 * @throws EvaluationException 
	 */

	public abstract double findValue(PdVector vec) throws EvaluationException;
	
	public double findValue(int a) throws EvaluationException {
	    return findValue(inGeom.getVertex(a));
	}
	
	   public int getnItterations() {
	        return nItterations;
	    }

	    public void setnItterations(int nItterations) {
	        this.nItterations = nItterations;
	    }

}
