package org.singsurf.singsurf;
import java.awt.Color;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;

/**
	Class to hold all the global display attributes of a geometry.
	Note: anything to do with textures or vector fields has been ignored.
**/

public class LmsPointSetMaterial
{
		Color	gVertCol;
		Color	gVertNormCol;
        double		gVertNormLen;
        double		gVertNormSize;
        double		gVertSize;
        boolean		defaultLabEnable;
        boolean		showIndices;
        boolean		showVertCols;
        boolean		showVertLabels;
        boolean		showVertNormArrow;
        boolean		showVertNorms;
        boolean		showVerts;

/** Reads the display attributes from a geometry. **/

public LmsPointSetMaterial(PgPointSet geom)
{
	showVerts	= geom.isShowingVertices();
	showVertCols	= geom.isShowingVertexColors();
    gVertCol 	= geom.getGlobalVertexColor();
	gVertSize	= geom.getGlobalVertexSize();
	defaultLabEnable = geom.isEnabledIndexLabels();
	showIndices	= geom.isShowingIndices();
	showVertLabels = geom.isShowingVertexLabels();
	showVertNormArrow = geom.isShowingVertexNormalArrow();
	showVertNorms	= geom.isShowingVertexNormals();
	gVertNormCol	= geom.getGlobalVertexNormalColor();
	gVertNormLen	= geom.getGlobalVertexNormalLength();
	gVertNormSize   = geom.getGlobalVertexNormalSize();
}

/** Sets the display attributes of a geometry **/

public void apply(PgPointSet geom)
{
	geom.setGlobalVertexColor(gVertCol); 
	geom.setGlobalVertexNormalColor(gVertNormCol);
	geom.setGlobalVertexNormalLength(gVertNormLen);
	geom.setGlobalVertexNormalSize(gVertNormSize);
	geom.setGlobalVertexSize(gVertSize);
	geom.setEnabledIndexLabels(defaultLabEnable);
	geom.showIndices(showIndices);
	geom.showVertexColors(showVertCols); 
	geom.showVertexLabels(showVertLabels); 
	geom.showVertexNormalArrow(showVertNormArrow);
	geom.showVertexNormals(showVertNorms);
	geom.showVertices(showVerts);
}

	void appendShowHide(StringBuffer sb,String attName,boolean show)
	{
		if(show)
			sb.append(" "+attName+"=\"show\"");
		else
			sb.append(" "+attName+"=\"hide\"");
	}
	void appendColor(StringBuffer sb,String tag,Color c)
	{
		sb.append("\t<"+tag+">"+c.getRed()
				+" "+c.getGreen()+" "+c.getBlue()+"</"+tag+">\n");
		
	}
	void appendNumber(StringBuffer sb,String tag,double val)
	{
		sb.append("\t<"+tag+">");
		sb.append(val);
		sb.append("</"+tag+">\n");
	}
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<pointSetMaterial");
		appendShowHide(sb,"point",showVerts);
		appendShowHide(sb,"color",showVertCols);
		appendShowHide(sb,"normal",showVertNorms);
		appendShowHide(sb,"normalArrow",showVertNormArrow);
		sb.append(">\n");
		appendColor(sb,"color",gVertCol);
		appendNumber(sb,"thickness",gVertSize);
		appendColor(sb,"normColor",gVertNormCol);
		appendNumber(sb,"normThickness",gVertNormSize);
		appendNumber(sb,"normLength",gVertNormLen);

		sb.append("</pointSetMaterial>\n");
		return sb.toString();
	}
	
	/**
	 * Extract the material for a geometry.
	 * @param geom
	 * @return the correct material subclass for the geometry 
	 */
	public static LmsPointSetMaterial getMaterial(PgGeometryIf geom)
	{
		if(geom instanceof PgElementSet)
			return new LmsElementSetMaterial((PgElementSet) geom);
		if(geom instanceof PgPolygonSet)
			return new LmsPolygonSetMaterial((PgPolygonSet) geom);
		if(geom instanceof PgPointSet)
			return new LmsPointSetMaterial((PgPointSet) geom);
		return null;
	}
}
