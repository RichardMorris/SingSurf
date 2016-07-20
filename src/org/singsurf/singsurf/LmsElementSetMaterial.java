package org.singsurf.singsurf;
import jv.geom.PgElementSet;
import java.awt.Color;

/**
	Class to hold all the global display attributes of a geometry.
	Note: anything to do with textures or vector fields has been ignored.
**/

public class LmsElementSetMaterial extends LmsPointSetMaterial
{
	Color	gEleCol;
	Color	gEleBackCol;
	Color	gEleNormCol;
	Color	gEdgeCol;
	Color	gBndCol;
	double		gEleNormLen;
	double		gEleNormSize;
	double		gBndSize;
	double		gEdgeSize;

	boolean		showEdgeLabels;
	boolean		showEleCols;
    boolean		showEleLabels;
    boolean		showEleNormArrow;
    boolean		showEleNorms;
    boolean		showEles;

	boolean		showEdge;
	boolean		showBnd;
	boolean		showEdgeCols;
	boolean		showBack;
	boolean		showEleBackCols;

/** Reads the display attributes from a geometry. **/

public LmsElementSetMaterial(PgElementSet geom)
{
	super(geom);

	gEleCol 	= geom.getGlobalElementColor();
	gEleBackCol 	= geom.getGlobalElementBackColor();
	gEleNormCol	= geom.getGlobalElementNormalColor();
	gEleNormLen	= geom.getGlobalElementNormalLength();
	gEleNormSize	= geom.getGlobalElementNormalSize();

	gEdgeCol 	= geom.getGlobalEdgeColor();
	gEdgeSize	= geom.getGlobalEdgeSize();

	gBndCol 	= geom.getGlobalBndColor();
	gBndSize	= geom.getGlobalBndSize();

	showEdgeLabels  = geom.isShowingEdgeLabels();
	showEleCols	= geom.isShowingElementColors();
	showEleLabels	= geom.isShowingElementLabels();
	showEleNormArrow = geom.isShowingElementNormalArrow();
	showEleNorms	= geom.isShowingElementNormals();
	showEles	= geom.isShowingElements();

	showEdge	= geom.isShowingEdges();
	showBnd		= geom.isShowingBoundaries();
	showEdgeCols	= geom.isShowingEdgeColors();
	showBack	= geom.isShowingBackface();
	showEleBackCols	= geom.isShowingElementBackColors();
}

/** Sets the display attributes of a geometry **/

public void apply(PgElementSet geom)
{
	super.apply(geom);

	geom.setGlobalElementColor(gEleCol); 
	geom.setGlobalElementBackColor(gEleBackCol); 
	geom.setGlobalElementNormalColor(gEleNormCol);
	geom.setGlobalElementNormalLength(gEleNormLen);
	geom.setGlobalElementNormalSize(gEleNormSize);

	geom.setGlobalEdgeColor(gEdgeCol); 
	geom.setGlobalEdgeSize(gEdgeSize);
	geom.setGlobalBndColor(gBndCol); 
	geom.setGlobalBndSize(gBndSize);

    geom.showEdgeLabels(showEdgeLabels);
    geom.showElementColors(showEleCols);
    geom.showElementLabels(showEleLabels);
    geom.showElementNormalArrow(showEleNormArrow);
    geom.showElementNormals(showEleNorms);
    geom.showElements(showEles);

	geom.showEdges(showEdge);
	geom.showBoundaries(showBnd);
	geom.showEdgeColors(showEdgeCols);
	geom.showBackface(showBack);
	geom.showElementBackColors(showEleBackCols);
// useGlobalElementSize() 
}

public String toString()
{
	StringBuffer sb = new StringBuffer(super.toString());
	sb.append("<elementSetMaterial");
	appendShowHide(sb,"face",showEles);
	appendShowHide(sb,"edge",showEdge);
	appendShowHide(sb,"color",showEleCols);
	appendShowHide(sb,"normal",showEleNorms);
	appendShowHide(sb,"normalArrow",showEleNormArrow);
	appendShowHide(sb,"backface",showBack);
	appendShowHide(sb,"boundary",showBnd);
	sb.append(">\n");

	appendColor(sb,"color",gEleCol);
	appendColor(sb,"colorBack",gEleBackCol);
	appendColor(sb,"normColor",gEleNormCol);
	appendColor(sb,"edgeColor",gEdgeCol);
	
	appendNumber(sb,"normLen",gEleNormLen);
	appendNumber(sb,"normThickness",gEleNormSize);
	appendNumber(sb,"edgeThickness",gEdgeSize);
	appendNumber(sb,"boundaryThickness",gBndSize);

	sb.append("</elementSetMaterial>\n");
	return sb.toString();
}
}
