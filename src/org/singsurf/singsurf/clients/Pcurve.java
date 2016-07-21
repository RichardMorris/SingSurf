/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;


import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.util.Collections;
import java.util.List;

import jv.geom.PgPolygonSet;
import jv.number.PuDouble;
import jv.project.PgGeometryIf;
import jv.vecmath.PiVector;

import org.singsurf.singsurf.Calculator;
import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.LmsPolygonSetMaterial;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.PuVariable;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.LsmpDef;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.jep.EvaluationException;
import org.singsurf.singsurf.operators.SphereClip;
/**
 * @author Rich Morris
 * Created on 30-Mar-2005
 */
public class Pcurve extends AbstractClient {
	private static final long serialVersionUID = 6873398333105602351L;

	/** The name for the program */
	protected static final String programName = "PCurve";

	/** file name for definitions */
	protected String	my_defFileName = "defs/pcurve.defs";

	/** default definition to use */
	private String my_defaultDefName = "Limacon";

	LsmpDef def;
	PuVariable displayVars[];
	protected	PuDouble	m_Clipping;
	int globalSteps = 60;
	DefVariable localX;
	protected PgPolygonSet outCurve;

	public Pcurve(GeomStore store,String name) {
		super(store, name);
		if (getClass() == Pcurve.class)
		{
			init(getDef(getDefaultDefName()),true);
		}
	}

	public Pcurve(GeomStore store,LsmpDef def) {
		super(store, def==null ? "PCurve" :def.getName());
		if (getClass() == Pcurve.class)
		{
	        if(def == null) 
	            def = createDefaultDef();
			init(def,false);
		}
	}
	
	public void init(LsmpDef def,boolean flag) {
		super.init(flag);
		String vname = "x"; //def.getVar(0).getName();
		localX = new DefVariable(vname,"Normal");
		displayVars = new PuVariable[]{
				new PuVariable(this,localX)};
		newParams = new LParamList(this);
		m_Clipping = new PuDouble("Clipping", this);
		m_Clipping.setValue(100);
		this.cbShowVert.setState(false);
		loadDefinition(def);
	}

    public LsmpDef createDefaultDef() {
        LsmpDef def;
        def = new LsmpDef("PCurve",DefType.pcurve,"");
        def.add(new DefVariable("x", -1, 1, 20));
        return def;
    }

	boolean checkDef(LsmpDef def)
	{
	    if(def.getNumVars()!=1) return false;
		DefVariable var =def.getVar(0); 
		if(var.getSteps()==-1) var.setBounds(var.getMin(),var.getMax(),globalSteps);
		return true;
	}
	public void loadDefinition(LsmpDef newdef)
	{
		def = newdef.duplicate(); 
		boolean flag = checkDef(def);
		def.setName(this.getName());
		this.getInfoPanel().setTitle(this.getName());
		calc = new Calculator(def,0);
		calc.build();
		if(!calc.isGood()) showStatus(calc.getMsg());
		setDisplayEquation(def.getEquation());
        localX = calc.getDefVariable(0);
		displayVars[0].set(localX);
		refreshParams();
		outCurve=store.aquireCurve(newdef.getName(),this);
		calcGeoms();
	}

	public void variableRangeChanged(int n,PuVariable v)
	{
		calc.setVarBounds(n,v.getMin(),v.getMax(),v.getSteps());
		if(n==0) localX.setBounds(v.getMin(),v.getMax(),v.getSteps());
		rebuildResultArray();
		calcGeoms();
	}
	
	public void rebuildResultArray() {
		if(outCurve==null)return;
		outCurve.setNumVertices(localX.getSteps()+1);
		outCurve.setNumPolygons(1);
		outCurve.setDimOfPolygons(localX.getSteps()+1);
		int a[] = new int[localX.getSteps()+1];
		for(int i=0;i<localX.getSteps()+1;++i) a[i]=i;
		outCurve.setPolygon(0,new PiVector(a));
		outCurve.removeVertexColors();
		outCurve.showVertexColors(false);
	}

	public void calcGeoms() {
		if(!calc.isGood())
		{
			showStatus(calc.getMsg());
			return;
		}
		//if(outCurve.getNumVertices()!= (localX.getSteps()+1))
			rebuildResultArray();
		if(outCurve != null) line_mat = new LmsPolygonSetMaterial(outCurve);

		//		System.out.println("Num vertices: "+m_geom.getNumVertices());
//		System.out.println("x steps "+psVars[0].steps);

        try {

		
		int index=0;
		for(int i=0;i<=localX.getSteps();++i) {
			double x = localX.getMin() + ((localX.getMax() - localX.getMin())*i)/localX.getSteps();
			//calc.setVarValue(0,x);

				double topRes[];
                    topRes = calc.evalTop(new double[]{x});
				if(topRes.length <3) {
					double oldRes[] = topRes;
					topRes = new double[3];
					for(int k=0;k<oldRes.length;++k)
						topRes[k]=oldRes[k];
				}
				//System.out.println("i "+i+" j "+j+" index "+index);
				outCurve.setVertex(index,
						topRes[0],topRes[1],topRes[2]);
				++index;
		}
		showSurf();
		
        } catch (EvaluationException e) {
            System.out.println(e.toString());
            calc.setGood(false);
            return;
        }

	}
	public void showSurf()
	{
		SphereClip clip = new SphereClip(m_Clipping.getValue());
		clip.operateCurve(outCurve);
		outCurve.showVertices(cbShowVert.getState());
		outCurve.showPolygons(cbShowCurves.getState());
		//setColour(outCurve,chColours.getSelectedItem());
		setDisplayProperties(outCurve);
		store.geomChanged(outCurve);
	}

	@Override
	public void setDisplayProperties() {
		setDisplayProperties(outCurve);
		store.geomApperenceChanged(outCurve);
	}


	public boolean update(Object o) 
	{
		if(o == displayVars[0])
		{
			variableRangeChanged(0,(PuVariable) o);
			return true;
		}
		else if(o instanceof PuParameter) {
			return parameterChanged((PuParameter) o);
		}
		else if(o == m_Clipping ) return true;
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
		return this.my_defaultDefName;
	}

	public String getDefinitionFileName() {
		return this.my_defFileName;
	}

	public String getProgramName() {
		return programName;
	}

	@Override
	public List<PgGeometryIf> getOutputGeoms() {
		return Collections.singletonList((PgGeometryIf) outCurve);
	}
}
