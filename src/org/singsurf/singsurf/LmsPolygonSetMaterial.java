package org.singsurf.singsurf;
import jv.geom.PgPolygonSet;
import java.awt.Color;

/**
	Class to hold all the global display attributes of a geometry.
	Note: anything to do with textures or vector fields has been ignored.
**/

public class LmsPolygonSetMaterial extends LmsPointSetMaterial
{
	Color	gPolyCol;
	Color	gPolyNormCol;
	double		gPolyNormLen;
	double		gPolyNormSize;
	double		gPolySize;

	boolean		showEdgeLabels;
	boolean		showPolyCols;
        boolean		showPolyEndArrow;
        boolean		showPolyLabels;
        boolean		showPolyNormArrow;
        boolean		showPolyNorms;
        boolean		showPolys;
        boolean		showPolyStartArrow;

/** Reads the display attributes from a geometry. **/

public LmsPolygonSetMaterial(PgPolygonSet geom)
{
	super(geom);

	gPolyCol 	= geom.getGlobalPolygonColor();
	gPolyNormCol	= geom.getGlobalPolygonNormalColor();
	gPolyNormLen	= geom.getGlobalPolygonNormalLength();
	gPolyNormSize	= geom.getGlobalPolygonNormalSize();
	gPolySize	= geom.getGlobalPolygonSize();

	showEdgeLabels  = geom.isShowingEdgeLabels();
	showPolyCols	= geom.isShowingPolygonColors();
	showPolyEndArrow = geom.isShowingPolygonEndArrow();
	showPolyLabels	= geom.isShowingPolygonLabels();
	showPolyNormArrow = geom.isShowingPolygonNormalArrow();
	showPolyNorms	= geom.isShowingPolygonNormals();
	showPolys	= geom.isShowingPolygons();
	showPolyStartArrow = geom.isShowingPolygonStartArrow();
}

/** Sets the display attributes of a geometry **/

public void apply(PgPolygonSet geom)
{
	super.apply(geom);

	geom.setGlobalPolygonColor(gPolyCol); 
	geom.setGlobalPolygonNormalColor(gPolyNormCol);
	geom.setGlobalPolygonNormalLength(gPolyNormLen);
	geom.setGlobalPolygonNormalSize(gPolyNormSize);
	geom.setGlobalPolygonSize(gPolySize);

        geom.showEdgeLabels(showEdgeLabels);
        geom.showPolygonColors(showPolyCols);
        geom.showPolygonEndArrow(showPolyEndArrow);
        geom.showPolygonLabels(showPolyLabels);
        geom.showPolygonNormalArrow(showPolyNormArrow);
        geom.showPolygonNormals(showPolyNorms);
        geom.showPolygons(showPolys);
        geom.showPolygonStartArrow(showPolyStartArrow);
// useGlobalPolygonSize() 
}

public String toString()
{
	StringBuffer sb = new StringBuffer(super.toString());
	sb.append("<polygonSetMaterial");
	appendShowHide(sb,"line",showPolys);
	appendShowHide(sb,"arrow",showPolyEndArrow);
	appendShowHide(sb,"color",showPolyCols);
	appendShowHide(sb,"arrowStart",showPolyStartArrow);
	sb.append(">\n");

	appendColor(sb,"color",gPolyCol);
	appendNumber(sb,"thickness",gPolySize);

	sb.append("</polygonSetMaterial>\n");
	return sb.toString();
}
}
