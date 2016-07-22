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

import jv.geom.PgElementSet;
import jv.number.PuDouble;
import jv.project.PgGeometryIf;
import jv.rsrc.PsGeometryInfo;
import jv.vecmath.PdVector;

import org.singsurf.singsurf.Calculator;
import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.LmsElementSetMaterial;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.PuVariable;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.LsmpDef;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.jep.EvaluationException;
import org.singsurf.singsurf.operators.SphereClip;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;
/**
 * @author Rich Morris
 * Created on 30-Mar-2005
 */
public class Psurf extends AbstractClient {
	private static final long serialVersionUID = -8086698325031357265L;

	/**
	 * The default equation (x^2 y - y^3 - z^2 = 0.0;).
	 */
	private final	String		my_def	   = "[3 x^4 + y x^2,-4 x^3 - 2 y x,y];";

	/** The name for the program */
	private static final String programName = "PSurf";

	/** file name for definitions */
	private final String	my_defFileName = "defs/psurf.defs";

	/** default definition to use */
	private final String my_defaultDefName = "Cross cap";
	
	PuVariable displayVars[]=new PuVariable[2];
	protected	PuDouble	m_Clipping;
	int globalSteps = 40;
	DefVariable localX,localY;
	protected PgElementSet outSurf;
	public Psurf(GeomStore store,String name) {
		super(store, name);
		if (getClass() == Psurf.class)
		{
			setDisplayEquation(my_def);
			init(getDef(getDefaultDefName()),true);
		}
	}
	public Psurf(GeomStore store,LsmpDef def) {
        super(store, def==null ? "PSurf" :def.getName());
        if(def == null) 
                def = createDefaultDef();
		if (getClass() == Psurf.class)
		{
			setDisplayEquation(def.getEquation());
			init(def,false);
		}
	}

    public LsmpDef createDefaultDef() {
        LsmpDef def;
        def = new LsmpDef("PSurf",DefType.psurf,"");
        def.add(new DefVariable("x", -1, 1));
        def.add(new DefVariable("y", -1, 1));
        return def;
    }

	public void init(LsmpDef def,boolean flag) {
		super.init(flag);
		localX = new DefVariable("x","Normal");
		localY = new DefVariable("y","Normal");
		displayVars = new PuVariable[]{
				new PuVariable(this,localX),
				new PuVariable(this,localY)};

		newParams = new LParamList(this);
		m_Clipping = new PuDouble("Clipping", this);
		m_Clipping.setValue(100.0);
		this.cbShowFace.setState(true);
		this.cbShowEdge.setState(true);
		this.cbShowVert.setState(false);
		//outSurf = store.aquireSurface("psurf");
		loadDefinition(def);
	}
	

	void checkDef(LsmpDef def)
	{
		DefVariable var =def.getVar(0); 
		if(var.getSteps()==-1) var.setBounds(var.getMin(),var.getMax(),globalSteps);
		var =def.getVar(1); 
		if(var.getSteps()==-1) var.setBounds(var.getMin(),var.getMax(),globalSteps);
	}
	public void loadDefinition(LsmpDef newdef)
	{
		LsmpDef def = newdef.duplicate(); 
		checkDef(def);
		def.setName(this.getName());
		this.getInfoPanel().setTitle(this.getName());
		calc = new Calculator(def,1);
		calc.build();
		if(!calc.isGood()) showStatus(calc.getMsg());
		localX = calc.getDefVariable(0);
		localY = calc.getDefVariable(1);
		setDisplayEquation(def.getEquation());
		displayVars[0].set(localX);
		displayVars[1].set(localY);
		refreshParams();
		outSurf = store.aquireSurface(newdef.getName(),this);
		calcGeoms();
	}

	public void variableRangeChanged(int n,PuVariable v)
	{
		calc.setVarBounds(n,v.getMin(),v.getMax(),v.getSteps());
		if(n==0) localX.setBounds(v.getMin(),v.getMax(),v.getSteps());
		if(n==1) localY.setBounds(v.getMin(),v.getMax(),v.getSteps());
		rebuildResultArray();
		calcGeoms();
	}
	
	public void rebuildResultArray() {
		outSurf.setNumVertices((localX.getSteps()+1)*(localY.getSteps()+1));
		outSurf.makeQuadrConn((localX.getSteps()+1),(localY.getSteps()+1));
		outSurf.setDimOfElements(-1);
		outSurf.removeElementColors();
		outSurf.removeVertexColors();
		outSurf.showElementColors(false);
		outSurf.showVertexColors(false);
		outSurf.showBoundaries(false);
	}

	@Override
    public void calcGeoms() {
		if(!calc.isGood())
		{
			showStatus(calc.getMsg());
			return;
		}
	//	if(m_geom.getNumVertices()!= (localX.getSteps()+1)*(localY.getSteps()+1))
			rebuildResultArray();
		if(outSurf != null) face_mat = new LmsElementSetMaterial(outSurf);

		try {
		//		System.out.println("Num vertices: "+m_geom.getNumVertices());
//		System.out.println("x steps "+psVars[0].steps);
		int index=0;
		for(int i=0;i<=localX.getSteps();++i) {
			double x = localX.getMin() + ((localX.getMax() - localX.getMin())*i)/localX.getSteps();
			calc.setVarValue(0,x);
			for(int j=0;j<=localY.getSteps();++j) {
				double y = localY.getMin() + ((localY.getMax() - localY.getMin())*j)/localY.getSteps();
				calc.setVarValue(1,y);

				double topRes[] = calc.evalTop(new double[]{x,y});
				PdVector dxVec = new PdVector(calc.evalDerivative(0));
				PdVector dyVec = new PdVector(calc.evalDerivative(1));
				PdVector norm = PdVector.crossNew(dxVec,dyVec);
				norm.normalize();
				//System.out.println("i "+i+" j "+j+" index "+index);
				outSurf.setVertex(index,
						topRes[0],topRes[1],topRes[2]);
				outSurf.setVertexNormal(index,norm);
				++index;
			}
		}
		PsGeometryInfo info = new PsGeometryInfo();
		info.setDetail(calc.getDefinition().toString());
		outSurf.setGeometryInfo(info);
		showSurf();
		
    } catch (EvaluationException e) {
        System.out.println(e.toString());
    }

	}

	public void showSurf()
	{
		SphereClip clip = new SphereClip(this.m_Clipping.getValue());
		try {
			clip.operate((outSurf));

		if( cbColour.getState() )
		{
			if(outSurf.hasVertexColors())
				outSurf.makeElementFromVertexColors();
			else
				outSurf.makeElementColorsFromXYZ();
			outSurf.showElementColors(true);
		}
		else
			outSurf.showElementColors(false);

		setDisplayProperties(outSurf);
		store.geomChanged(outSurf);
        } catch (UnSuportedGeometryException e) {
            showStatus("Unknown geometry type");
            return;
        } catch (EvaluationException e) {
            System.out.println(e.toString());
            calc.setGood(false);
            return;
        }
	}

	@Override
    public boolean update(Object o) 
	{
		if(o == displayVars[0])
		{
			variableRangeChanged(0,(PuVariable) o);
			return true;
		}
		if(o == displayVars[1])
		{
			variableRangeChanged(1,(PuVariable) o);
			return true;
		}
		else if(o instanceof PuParameter) {
			return parameterChanged((PuParameter) o);
		}
		else if(o == m_Clipping ) {calcGeoms(); return true; }
		else return super.update(o);
	}

	
	/* (non-Javadoc)
	 * @see org.singsurf.singsurf.clients.AbstractClient#setDisplayProperties()
	 */
	@Override
	public void setDisplayProperties() {
		setDisplayProperties(outSurf);
		store.geomApperenceChanged(outSurf);
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
		else super.itemStateChanged(e);
	}

	@Override
    public String getDefaultDefName() {
		return this.my_defaultDefName;
	}

	@Override
    public String getDefinitionFileName() {
		return this.my_defFileName;
	}
	@Override
	public String getProgramName() {
		return programName;
	}

	@Override
	public List<PgGeometryIf> getOutputGeoms() {
		return Collections.singletonList((PgGeometryIf) outSurf);
	}
	
}
