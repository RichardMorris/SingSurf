/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

public class Region_info {
    double xmin,xmax,ymin,ymax,zmin,zmax;

	public Region_info(double xmin, double xmax, double ymin, double ymax,
			double zmin, double zmax) {
		super();
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		this.zmin = zmin;
		this.zmax = zmax;
	}

	@Override
	public String toString() {
		return String.format(
		"range %f %f %f %f %f %f\n",
	  	xmin,xmax,ymin,ymax,zmin,zmax);
	}
    
}
