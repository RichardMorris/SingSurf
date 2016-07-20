/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.number.PuDouble;
import jv.project.PgGeometryIf;

import org.singsurf.singsurf.Calculator;
import org.singsurf.singsurf.EvaluationException;
import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.LsmpDef;
import org.singsurf.singsurf.geometries.GeomPair;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.operators.ColourCalcMap;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;
/**
 * @author Rich Morris
 * Created on 30-Mar-2005
 */
public class Colourize extends AbstractOperatorClient {
	/** 
	 * 
	 */
	private static final long serialVersionUID = -4659133739866394363L;
	protected static final String programName = "Mapping";
	/**
	 * The default equation (x^2 y - y^3 - z^2 = 0.0;).
	 */
	protected	String		my_def	   = "[3 x^4 + y x^2,-4 x^3 - 2 y x,y];";

	/**
	 * The model loaded by default (defaultsurf.jvx.gz).
	 */
	protected	String		my_defModelName = "defaultpsurf.jvx.gz";

	/** file name for definitions */
	protected String	my_defFileName = "defs/mapping.defs";

	//LsmpDef def;
	//	PuVariable displayVars[];
	protected	PuDouble	m_Clipping;
	protected	PuDouble	m_ContDist;
	//int globalSteps = 40;
	DefVariable localX,localY,localZ;
	PgPointSet mappedGeom;

	/** The operator which performs the mapping */
	ColourCalcMap map = null;

	/********** Constructor *********/
	
	public Colourize(GeomStore store,String name) {
		super(store, name);
		if (getClass() == Colourize.class)
		{
			setDisplayEquation(my_def);
			init(getDef(getDefaultDefName()),true);
		}
	}
	public Colourize(GeomStore store,LsmpDef def) {
		super(store, def.getName());
		if (getClass() == Colourize.class)
		{
			setDisplayEquation(def.getEquation());
			init(def,false);
		}
	}

	public void init(LsmpDef def,boolean flag) {
		super.init(flag);
		newParams = new LParamList(this);
		m_Clipping = new PuDouble("Clipping", this);
		m_Clipping.setValue(100.0);
		m_Clipping.setBounds(0.0,1000.0);
		m_ContDist = new PuDouble("Continuity Distance", this);
		m_ContDist.setValue(10.0);
		m_ContDist.setBounds(0.0,1000.0);
		this.cbShowFace.setState(true);
		this.cbShowEdge.setState(true);
		this.cbShowVert.setState(false);
		this.chColours.addItemListener(this);
		loadDefinition(def);
	}

	void checkDef(LsmpDef def)
	{
	}
	public void loadDefinition(LsmpDef newdef)
	{
			LsmpDef def = newdef.duplicate(); 
			checkDef(def);
			def.setName(this.getName());
			this.getInfoPanel().setTitle(this.getName());
			calc = new Calculator(def,0);
			calc.build();
			if(!calc.isGood()) showStatus(calc.getMsg());
			ch_inputSurf.setEnabled(calc.isGood());
			map=new ColourCalcMap(calc);
			localX = calc.getDefVariable(0);
			localY = calc.getDefVariable(1);
			localZ = calc.getDefVariable(2);
			setDisplayEquation(def.getEquation());
			refreshParams();
			//calcSurf();
	}

	public void calcGeom(GeomPair p) {
		if(calc != null && !calc.isGood())
		{
			showStatus(calc.getMsg());
			return;
		}
		PgGeometryIf input = p.getInput();
		PgGeometryIf out = p.getOutput();
		GeomStore.copySrcTgt(input,out);
		((PgPointSet) out).showVertexColors(true);

		try {
			map.operate(out);
		} catch (UnSuportedGeometryException e) {
			showStatus("Unsupported geometry type");
			return;
        } catch (EvaluationException e) {
            System.out.println(e.toString());
            calc.setGood(false);
            return;
        }
		
		if(out instanceof PgElementSet) {
			((PgElementSet) out).assureElementColors();
			//((PgElementSet) out).showElementFromVertexColors(true);
			((PgElementSet) out).showSmoothElementColors(true);
			((PgElementSet) out).makeElementFromVertexColors();
			((PgElementSet) out).showElementColors(true);
		}
		if(out instanceof PgPolygonSet) {
			((PgPolygonSet) out).makePolygonFromVertexColors();
		}
		store.geomChanged(out);
	}

	/**
	 * Calculate all the needed geoms.
	 */
	public void calcGeoms()
	{
		for(GeomPair p : activePairs.values())
			calcGeom(p);
	}

	public void newActiveInput(String name)
	{
		if(activePairs.containsKey(name))
		{
			showStatus(name+" is already active");
			return;
		}
		PgGeometryIf input = store.getGeom(name);
		PgGeometryIf output = store.aquireGeometry(getPreferredOutputName(name),input,this);
		GeomPair p = new GeomPair(input,output);
		activePairs.put(name,p);
		activeInputNames.add(name);
		setDisplayProperties(p.getOutput());
		calcGeom(p);
	}
	
	
	public boolean update(Object o) 
	{
		if(o instanceof PuParameter) {
			return parameterChanged((PuParameter) o);
		}
		else if(o == m_Clipping || o == this.m_ContDist) {calcGeoms(); return true; }
		else return super.update(o);
	}

	
	/**
	 * Handles the selection of a new surface definition.
	 */

	public void itemStateChanged(ItemEvent e)
	{
		ItemSelectable itSel = e.getItemSelectable();
		if( itSel == chDefs )
		{
			int i = chDefs.getSelectedIndex();
			loadDefinition(lsmpDefs[i]);
		}
		else super.itemStateChanged(e);
	}

	public String getDefaultDefName() {
		return this.my_defModelName;
	}

	public String getDefinitionFileName() {
		return this.my_defFileName;
	}

	public String getProgramName() {
		return programName;
	}

	public String makeCGIstring() {
		return null;
	}

	public void geometryDefHasChanged(Calculator inCalc) {	}
	
    @Override
    public LsmpDef createDefaultDef() {
        // TODO Auto-generated method stub
        LsmpDef def;
        def = new LsmpDef("Colourize",DefType.colour,"");
        def.add(new DefVariable("x", -1, 1, 20));
        def.add(new DefVariable("y", -1, 1, 20));
        def.add(new DefVariable("z", -1, 1, 20));
        return def;
    }
}
