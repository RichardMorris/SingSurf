/*
Created 25 Nov 2006 - Richard Morris
*/
package org.singsurf.singsurf.acurve;

/**
 * Represents a 2D rectangular range.
 * @author Richard Morris
 *
 */
public class Range2D {
	double xmin,xmax,ymin,ymax;
	double width,height;
	/**
	 * @param xmin
	 * @param xmax
	 * @param ymin
	 * @param ymax
	 */
	public Range2D(double xmin, double xmax, double ymin, double ymax) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.width = xmax-xmin;
		this.height = ymax-ymin;
	}
	
	
}
