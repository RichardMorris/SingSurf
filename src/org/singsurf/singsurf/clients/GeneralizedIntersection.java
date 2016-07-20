/*
Created 17 Sep 2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.util.SortedSet;

import org.singsurf.singsurf.Calculator;
import org.singsurf.singsurf.ChainedCalculator;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.LsmpDef;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.operators.SimpleCalcIntersection;
import org.singsurf.singsurf.operators.SimpleCalcMap;

public class GeneralizedIntersection extends Intersection implements GeneralisedOperator {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8862422439891056104L;
	protected static final String programName = "GeneralisedIntersection";
	/**
	 * The default equation (x^2 y - y^3 - z^2 = 0.0;).
	 */
	protected	String		my_def	   = "[3 x^4 + y x^2,-4 x^3 - 2 y x,y];";

	/** The variable corresponding to the ingredient */
//	DefVariable ingredientVar;

	/** A choice of avaliable inputs */
	protected Choice ch_ingredient = new Choice();

	/** Whether to project onto the given surface. */
	protected Checkbox cbProject = new Checkbox("Project onto surface",false);

	/** The operator which performs the mapping */
	SimpleCalcMap projectionMap = null;

	
	public GeneralizedIntersection(GeomStore store, String projName) {
		super(store, projName);
		if (getClass() == GeneralizedIntersection.class)
		{
			setDisplayEquation(my_def);
			init(getDef(getDefaultDefName()),true);
		}
		// TODO Auto-generated constructor stub
	}

	public GeneralizedIntersection(GeomStore store,LsmpDef def) {
        super(store, def==null ? "Gen Int" :def.getName());
		if (getClass() == GeneralizedIntersection.class)
		{
	          if(def == null) 
                  def = createDefaultDef();

			setDisplayEquation(def.getEquation());
			init(def,false);
		}
	}
	
    public LsmpDef createDefaultDef() {
        LsmpDef def;
        def = new LsmpDef("Intersect",DefType.genInt,"");
        def.add(new DefVariable("x", "none"));
        def.add(new DefVariable("y", "none"));
        def.add(new DefVariable("z", "none"));
        def.add(new DefVariable("S", "psurf"));
        def.setOpType(DefType.psurf);
        return def;
    }


	public void init(LsmpDef def,boolean flag) {
		super.init(def,flag);
		ch_ingredient.addItemListener(this);
		cbProject.addItemListener(this);
	}

	public void loadDefinition(LsmpDef newdef)
	{
			LsmpDef def = newdef.duplicate(); 
			checkDef(def);
			def.setName(this.getName());
			this.getInfoPanel().setTitle(this.getName());
			calc = new ChainedCalculator(def,0);
			calc.build();
			if(!calc.isGood()) showStatus(calc.getMsg());
			intersectAlgorithm=new SimpleCalcIntersection(calc);
//			ingredientVar = calc.getDefVariable(0);
			setDisplayEquation(def.getEquation());
			refreshParams();
			//calcSurf();
	}

	public void setIngredient(Calculator inCalc)
	{
		((ChainedCalculator) calc).setIngredient(inCalc);
		this.projectionMap = new SimpleCalcMap(inCalc);
		//this.ch_inputSurf.setEnabled(goodIngredient());
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		ItemSelectable itSel = e.getItemSelectable();

		if(itSel == ch_ingredient)
		{
			String ingrName = ch_ingredient.getSelectedItem();
			if(ingrName.equals(NONE)) return;
			setIngredient(store.getGenerator(ingrName).getCalculator());
		}
		else if(itSel == this.cbProject) {
			setProject(cbProject.getState());
		}
		else super.itemStateChanged(e);
	}

	private void setProject(boolean state) {
		this.calcGeoms();
	}

	@Override
	public void geometryHasChanged(String geomName) {
		super.geometryHasChanged(geomName);
		if( ! calc.isGood()) return;
		if(goodIngredient() && ((ChainedCalculator) calc).getIngredient().getDefinition().getName().equals(geomName)) 
		{
			this.calcGeoms();
		}
	}

	@Override
	public void geometryDefHasChanged(Calculator inCalc) {
		if(goodIngredient() && ((ChainedCalculator) calc).getIngredient() == inCalc)
			this.setIngredient(inCalc);
	}

	@Override
	public void geometryNameHasChanged(String oldName, String newName) {
		// TODO Auto-generated method stub
		super.geometryNameHasChanged(oldName, newName);
	}

	@Override
	public void refreshList(SortedSet<String> list) {
		super.refreshList(list);
		String item = this.ch_ingredient.getSelectedItem();
		this.ch_ingredient.removeAll();
		this.ch_ingredient.add(NONE);
		for(String name: list)
			this.ch_ingredient.add(name);
		this.ch_ingredient.select(item);
	}
/*
	public void calcGeom(GeomPair p) {
		if( ! calc.isGood())
		{
			showStatus(calc.getMsg());
			return;
		}
		PgGeometryIf input = p.getInput();
		PgGeometryIf resultGeom = null;
		try {
			resultGeom = intersectAlgorithm.operate(input);
			if(this.cbProject.getState())
				resultGeom = this.projectionMap.operate(resultGeom);
		} catch (UnSuportedGeometryException e) {
			PsDebug.error("Intersection could not be calculated");
			return;
		}
		if(resultGeom==null)
		{
			PsDebug.error("Intersection could not be calculated");
			return;
		}
		PgGeometryIf out = p.getOutput();
		GeomStore.copySrcTgt(resultGeom,out);
		//setDisplayProperties(out);
		store.geomChanged(out);
	}
*/
	
	@Override
	public String getPreferredOutputName(String name)
	{
		return getName() + "(" +((ChainedCalculator) calc).getIngredient().getDefinition().getName()+","+ name +")";
	}

	public boolean goodIngredient() {
		return calc != null && ((ChainedCalculator) calc).goodIngredient();
	}

}
