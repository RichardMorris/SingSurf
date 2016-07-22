/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;


import java.awt.Choice;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.number.PuDouble;
import jv.object.PsDebug;
import jv.project.PgGeometryIf;

import org.singsurf.singsurf.Calculator;
import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.LsmpDef;
import org.singsurf.singsurf.geometries.GeomPair;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.jep.EvaluationException;
import org.singsurf.singsurf.operators.SimpleCalcIntersection;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;
/**
 * @author Rich Morris
 * Created on 30-Mar-2005
 */
public class Intersection extends AbstractOperatorClient {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4659133739866394363L;

	/** The name for the program */
	protected static final String programName = "Intersect";
	
	/**
	 * The default equation (x^2 y - y^3 - z^2 = 0.0;).
	 */
	protected	String		my_def	   = "[3 x^4 + y x^2,-4 x^3 - 2 y x,y];";

	/**
	 * The model loaded by default (defaultsurf.jvx.gz).
	 */
	protected	String		my_defModelName = "defaultpsurf.jvx.gz";

	/** file name for definitions */

	protected String	my_defFileName = "defs/intersect.defs";

	/** Default name for geometries **/

	protected	String		my_baseName = "intersect";

	//	PuVariable displayVars[];
	protected	PuDouble	m_Clipping;
	int globalSteps = 40;
	DefVariable localX,localY,localZ;
	
	SimpleCalcIntersection intersectAlgorithm = null;

    Choice chItts = new Choice();;
	
	public Intersection(GeomStore store,String name) {
		super(store,name);
		if (getClass() == Intersection.class)
		{
			setDisplayEquation(my_def);
			init(getDef(getDefaultDefName()),true);
		}
	}
	public Intersection(GeomStore store,LsmpDef def) {
        super(store, def==null ? "Intersection" :def.getName());
		if (getClass() == Intersection.class)
		{
	        if(def == null) 
	                def = createDefaultDef();
			init(def,false);
		}
	}

    public LsmpDef createDefaultDef() {
        LsmpDef def;
        def = new LsmpDef("Intersect",DefType.intersect,"");
        def.add(new DefVariable("x", "none"));
        def.add(new DefVariable("y", "none"));
        def.add(new DefVariable("z", "none"));
        return def;
    }

	public void init(LsmpDef def,boolean flag) {
	    super.init(flag);
	    newParams = new LParamList(this);

	    chItts.addItem("1");
	    chItts.addItem("2");
	    chItts.addItem("3");
	    chItts.addItem("4");
	    chItts.addItem("5");
	    chItts.addItem("6");
	    chItts.addItem("7");
	    chItts.addItem("8");
	    chItts.addItem("9");
	    chItts.select(4);
	    chItts.addItemListener(this);

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
			//calc.requireDerivative(names)
			if(!calc.isGood()) showStatus(calc.getMsg());
			//ch_inputSurf.setEnabled(calc.isGood());
			intersectAlgorithm=new SimpleCalcIntersection(calc);
			localX = calc.getDefVariable(0);
			localY = calc.getDefVariable(1);
			localZ = calc.getDefVariable(2);
			setDisplayEquation(def.getEquation());
			refreshParams();
			//calcSurf();
	}

	public void calcGeom(GeomPair p) {
		if(!calc.isGood())
		{
			showStatus(calc.getMsg());
			return;
		}
		PgGeometryIf input = p.getInput();
		PgGeometryIf resultGeom = null;
		try {
			resultGeom = intersectAlgorithm.operate(input);
		} catch (UnSuportedGeometryException e) {
			PsDebug.error("Intersection could not be calculated");
			return;
        } catch (EvaluationException e) {
            System.out.println(e.toString());
            calc.setGood(false);
            return;
        }

		if(resultGeom==null)
		{
			PsDebug.error("Intersection could not be calculated");
			return;
		}
		PgGeometryIf out = p.getOutput();
		GeomStore.copySrcTgt(resultGeom,out);
		setDisplayProperties(out);
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
		PgGeometryIf output = null;
		if(input instanceof PgElementSet)
			output = store.aquireCurve(getPreferredOutputName(name),this);
		else if(input instanceof PgPolygonSet)
			output = store.aquirePoints(getPreferredOutputName(name),this);
		else if(input instanceof PgPointSet)
			output = store.aquirePoints(getPreferredOutputName(name),this);
		GeomPair p = new GeomPair(input,output);
		setDisplayProperties(p.getOutput());
		activePairs.put(name,p);
		activeInputNames.add(name);
		calcGeom(p);
	}

	public boolean update(Object o) 
	{
		if(o instanceof PuParameter) {
			return parameterChanged((PuParameter) o);
		}
		else if(o == m_Clipping ) return true;
		else return super.update(o);
	}

	/**
	 * Handles the selection of a new surface definition.
	 */
	@Override
	public void itemStateChanged(ItemEvent e)
	{
		ItemSelectable itSel = e.getItemSelectable();
		if( itSel == chDefs )
		{
			int i = chDefs.getSelectedIndex();
			loadDefinition(lsmpDefs[i]);
		}
		else if( itSel == chItts) {
		    int i = chItts.getSelectedIndex();
		    this.intersectAlgorithm.setnItterations(i);
		}
		else super.itemStateChanged(e);
	}

	public String getProgramName() {
		return programName;
	}

	public String getDefaultDefName() {
		return this.my_defModelName;
	}

	public String getDefinitionFileName() {
		return this.my_defFileName;
	}

	public String makeCGIstring() {
		return null;
	}
	public void geometryDefHasChanged(Calculator inCalc) {	}

}
