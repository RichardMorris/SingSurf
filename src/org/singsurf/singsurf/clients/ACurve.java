/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;


import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.util.Collections;
import java.util.List;

import jv.geom.PgPolygonSet;
import jv.object.PsDebug;
import jv.project.PgGeometryIf;

import org.nfunk.jep.ParseException;
import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.LmsPolygonSetMaterial;
import org.singsurf.singsurf.PolynomialCalculator;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.PuVariable;
import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.acurve.EquationConverter;
import org.singsurf.singsurf.acurve.Plotter2D;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.LsmpDef;
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.geometries.GeomStore;

/**
 * @author Rich Morris
 * Created on 30-Mar-2005
 */
public class ACurve extends AbstractClient {
	private static final long serialVersionUID = 6873398333105602351L;

	/** The name for the program */
	protected static final String programName = "ACurve";

	/** file name for definitions */
	protected String	my_defFileName = "defs/acurve.defs";

	/** default definition to use */
	private final String my_defaultDefName = "Limacon";

	LsmpDef def;
	PuVariable displayVars[];

	protected	CheckboxGroup	cbg_coarse;	// Coarse checkbox group
	protected	CheckboxGroup	cbg_fine;	// Coarse checkbox group
	protected	CheckboxGroup	cbg_timeout;	// Timeout checkbox group

	protected	Checkbox	cb_c_4,  cb_c_8,  cb_c_16,  cb_c_32,  cb_c_64, cb_c_128, cb_c_256;
//	protected	Checkbox	cb_fi_8,  cb_fi_16,  cb_fi_32,  cb_fi_64,
//					cb_fi_128,  cb_fi_256,  cb_fi_512,  cb_fi_1024; 

	
	//int globalSteps = 60;
	DefVariable localX,localY;
	protected PgPolygonSet outCurve;

	Plotter2D plotter;
	public ACurve(GeomStore store,String name) {
		super(store, name);
		if (getClass() == ACurve.class)
		{
			init(getDef(getDefaultDefName()),true);
		}
	}

	public ACurve(GeomStore store,LsmpDef def) {
        super(store, def==null ? "ACurve" :def.getName());
        if (getClass() == ACurve.class)
        {
            if(def == null) 
                def = createDefaultDef();
            init(def,false);
        }
	}
	
    public LsmpDef createDefaultDef() {
        LsmpDef def;
        def = new LsmpDef("ACurve",DefType.acurve,"");
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
		cbg_coarse = new CheckboxGroup();
		cbg_fine = new CheckboxGroup();

		cb_c_4 = new Checkbox("4",cbg_coarse,false);
		cb_c_8  = new Checkbox("8",cbg_coarse,false);
		cb_c_16  = new Checkbox("16",cbg_coarse,false);
		cb_c_32  = new Checkbox("32",cbg_coarse,false);
		cb_c_64  = new Checkbox("64",cbg_coarse,true);
        cb_c_128  = new Checkbox("128",cbg_coarse,true);
        cb_c_256  = new Checkbox("256",cbg_coarse,true);

//		cb_fi_8 = new Checkbox("8",cbg_fine,false);
//		cb_fi_16 = new Checkbox("16",cbg_fine,false);
//		cb_fi_32 = new Checkbox("32",cbg_fine,false);
//		cb_fi_64 = new Checkbox("64",cbg_fine,false);
//		cb_fi_128 = new Checkbox("128",cbg_fine,false);
//		cb_fi_256 = new Checkbox("256",cbg_fine,true);
//		cb_fi_512 = new Checkbox("512",cbg_fine,false);
//		cb_fi_1024 = new Checkbox("1024",cbg_fine,false); 

		cbShowVert.setState(false);
		int coarse = Integer.parseInt(cbg_coarse.getSelectedCheckbox().getLabel());
        plotter = new Plotter2D(coarse,coarse*4,4096);
//				Integer.parseInt(cbg_fine.getSelectedCheckbox().getLabel()),4096);
		loadDefinition(def);
	}

	public void setCoarse(int c){
		if(c==4)
			cbg_coarse.setSelectedCheckbox(cb_c_4);
		if(c==8)
			cbg_coarse.setSelectedCheckbox(cb_c_8);
		if(c==16)
			cbg_coarse.setSelectedCheckbox(cb_c_16);
		if(c==32)
			cbg_coarse.setSelectedCheckbox(cb_c_32);
		if(c==64)
			cbg_coarse.setSelectedCheckbox(cb_c_64);
		def.setOption("coarse", c);
	}
//	public void setFine(int f){
//		if(f==8)
//			cbg_fine.setSelectedCheckbox(cb_fi_8);
//		if(f==16)
//			cbg_fine.setSelectedCheckbox(cb_fi_16);
//		if(f==32)
//			cbg_fine.setSelectedCheckbox(cb_fi_32);
//		if(f==64)
//			cbg_fine.setSelectedCheckbox(cb_fi_64);
//		if(f==128)
//			cbg_fine.setSelectedCheckbox(cb_fi_128);
//		if(f==256)
//			cbg_fine.setSelectedCheckbox(cb_fi_256);
//		if(f==512)
//			cbg_fine.setSelectedCheckbox(cb_fi_512);
//		if(f==1024)
//			cbg_fine.setSelectedCheckbox(cb_fi_1024);
//		def.setOption("fine", f);
//	}
	void checkDef(LsmpDef def)
	{
		//DefVariable var =def.getVar(0); 
		//if(var.getSteps()==-1) var.setBounds(var.getMin(),var.getMax(),globalSteps);
	}
	public void loadDefinition(LsmpDef newdef)
	{
		def = newdef.duplicate(); 
		checkDef(def);
		def.setName(this.getName());
		this.getInfoPanel().setTitle(this.getName());
		calc = new PolynomialCalculator(def,0);
		calc.build();
		if(!calc.isGood()) showStatus(calc.getMsg());
		localX = calc.getDefVariable(0);
		localY = calc.getDefVariable(1);
		setDisplayEquation(def.getEquation());
		displayVars[0].set(localX);
		displayVars[1].set(localY);
		refreshParams();
		Option copt = def.getOption("coarse");
		if(copt!=null) setCoarse(copt.getIntegerVal());
//		Option fopt = def.getOption("fine");
//		if(fopt!=null) setFine(fopt.getIntegerVal());
		
		outCurve=store.aquireCurve(newdef.getName(),this);
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
	}

	@Override
    public void calcGeoms() {
		if(!calc.isGood())
		{
			showStatus(calc.getMsg());
			return;
		}
		int coarse = Integer.parseInt(cbg_coarse.getSelectedCheckbox().getLabel());
        plotter.setDepths(coarse,coarse*4,4096);
//		        Integer.parseInt(cbg_fine.getSelectedCheckbox().getLabel())

        //if(outCurve.getNumVertices()!= (localX.getSteps()+1))
			rebuildResultArray();
		if(outCurve != null) line_mat = new LmsPolygonSetMaterial(outCurve);

		PgGeometryIf resultGeom = null;
		
		EquationConverter ec = new EquationConverter(calc.getJep());
		try {
		double[][] coeffs = ec.convert2D(calc.getRawEqns(), 
				new String[]{localX.getName(),localY.getName()},
				calc.getParams()); 
		
		try {
			resultGeom = plotter.calculate(coeffs, localX.getMin(),localX.getMax(),localY.getMin(),localY.getMax());
		} catch (AsurfException e) {
			e.printStackTrace();
		}
		}
		catch(ParseException e) {
			PsDebug.error(e.getMessage());
		}
		if(resultGeom==null)
		{
			PsDebug.error("Algebraic curve could not be calculated");
			return;
		}
		GeomStore.copySrcTgt(resultGeom,outCurve);

		outCurve.showVertices(cbShowVert.getState());
		outCurve.showPolygons(cbShowCurves.getState());
		setColour(outCurve,chColours.getSelectedItem());
		setDisplayProperties(outCurve);
		store.geomChanged(outCurve);
	}

	@Override
	public void setDisplayProperties() {
		setDisplayProperties(outCurve);
		store.geomApperenceChanged(outCurve);
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
		return Collections.singletonList((PgGeometryIf) outCurve);
	}
}
