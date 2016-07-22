/*
Created 17 Sep 2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import java.awt.Choice;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.util.SortedSet;

import org.singsurf.singsurf.BiChainedCalculator;
import org.singsurf.singsurf.Calculator;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.LsmpDef;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.operators.SimpleCalcMap;

public class BiMap extends Mapping implements GeneralisedBiOperator {
	private static final long serialVersionUID = 1L;
	
	protected static final String programName = "Bi-Map";
	/**
	 * The default equation (x^2 y - y^3 - z^2 = 0.0;).
	 */
	protected	String		my_def	   = "[3 x^4 + y x^2,-4 x^3 - 2 y x,y];";

	/** The variable corresponding to the ingredient */
//	DefVariable ingredientVar;

	/** A choice of avaliable inputs */
	protected Choice ch_ingredient1 = new Choice();
    protected Choice ch_ingredient2 = new Choice();

//	/** Whether to project onto the given surface. */
//	protected Checkbox cbProject = new Checkbox("Project onto surface",false);

	/** The operator which performs the mapping */
	//SimpleCalcMap projectionMap = null;

	
	public BiMap(GeomStore store, String projName) {
		super(store, projName);
		if (getClass() == BiMap.class)
		{
			setDisplayEquation(my_def);
			init(getDef(getDefaultDefName()),true);
		}
		// TODO Auto-generated constructor stub
	}

	public BiMap(GeomStore store,LsmpDef def) {
        super(store, def==null ? "Bi Mapping" :def.getName());
		if (getClass() == BiMap.class)
		{
            if(def == null) 
                def = createDefaultDef();

			setDisplayEquation(def.getEquation());
			init(def,false);
		}
	}

    public LsmpDef createDefaultDef() {
        LsmpDef def;
        def = new LsmpDef("BiMap",DefType.biMap,"");
        def.add(new DefVariable("s", "none"));
        def.add(new DefVariable("t", "none"));
        def.add(new DefVariable("S", "pcurve"));
        def.add(new DefVariable("T", "pcurve"));
        def.setOpType(DefType.pcurve);
        return def;
    }

	public void init(LsmpDef def,boolean flag) {
		super.init(def,flag);
		ch_ingredient1.addItemListener(this);
        ch_ingredient2.addItemListener(this);
	}

	public void loadDefinition(LsmpDef newdef)
	{
			LsmpDef def = newdef.duplicate(); 
			checkDef(def);
			def.setName(this.getName());
			this.getInfoPanel().setTitle(this.getName());
			calc = new BiChainedCalculator(def,0);
			calc.build();
			if(!calc.isGood()) showStatus(calc.getMsg());
	        map=new SimpleCalcMap(calc);
//			ingredientVar = calc.getDefVariable(0);
			setDisplayEquation(def.getEquation());
			refreshParams();
			//calcSurf();
	}

	public void setIngredient1(Calculator inCalc)
	{
		((BiChainedCalculator) calc).setIngredient1(inCalc);
		//this.projectionMap = new SimpleCalcMap(inCalc);
		//this.ch_inputSurf.setEnabled(goodIngredients());
	}

	public void setIngredient2(Calculator inCalc)
	{
	        ((BiChainedCalculator) calc).setIngredient2(inCalc);
	        //this.projectionMap = new SimpleCalcMap(inCalc);
	        boolean goodIngredients = goodIngredients();
            System.out.println("setIgr2 "+goodIngredients);
	        //this.ch_inputSurf.setEnabled(goodIngredients);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		ItemSelectable itSel = e.getItemSelectable();

		if(itSel == ch_ingredient1)
		{
			String ingrName = ch_ingredient1.getSelectedItem();
			if(ingrName.equals(NONE)) return;
			setIngredient1(store.getGenerator(ingrName).getCalculator());
		}
		else if(itSel == ch_ingredient2)
        {
            String ingrName = ch_ingredient2.getSelectedItem();
            if(ingrName.equals(NONE)) return;
            setIngredient2(store.getGenerator(ingrName).getCalculator());
        }
		else super.itemStateChanged(e);
	}

	@Override
	public void geometryHasChanged(String geomName) {
		super.geometryHasChanged(geomName);
		if( ! calc.isGood()) return;
		if(goodIngredients()
		   && 
		   (  ((BiChainedCalculator) calc).getIngredient1().getDefinition().getName().equals(geomName) 
		   || ((BiChainedCalculator) calc).getIngredient2().getDefinition().getName().equals(geomName) ))  
		{
			this.calcGeoms();
		}
	}

	@Override
	public void geometryDefHasChanged(Calculator inCalc) {
		if(goodIngredients() &&
		          ((BiChainedCalculator) calc).getIngredient1() == inCalc )
            this.setIngredient1(inCalc);
		    
        if(goodIngredients() &&
                 ((BiChainedCalculator) calc).getIngredient2() == inCalc  )
            this.setIngredient2(inCalc);
	}

	@Override
	public void geometryNameHasChanged(String oldName, String newName) {
		// TODO Auto-generated method stub
		super.geometryNameHasChanged(oldName, newName);
	}

	@Override
	public void refreshList(SortedSet<String> list) {
		super.refreshList(list);
		String item1 = this.ch_ingredient1.getSelectedItem();
        String item2 = this.ch_ingredient2.getSelectedItem();
		this.ch_ingredient1.removeAll();
        this.ch_ingredient2.removeAll();
		this.ch_ingredient1.add(NONE);
        this.ch_ingredient2.add(NONE);
		for(String name: list) {
			this.ch_ingredient1.add(name);
            this.ch_ingredient2.add(name);
		}
		this.ch_ingredient1.select(item1);
        this.ch_ingredient2.select(item2);
	}


	@Override
	public String getPreferredOutputName(String name)
	{
		return getName() 
		        + "(" + ((BiChainedCalculator) calc).getIngredient1().getDefinition().getName()
                + "," + ((BiChainedCalculator) calc).getIngredient2().getDefinition().getName()
		        +","+ name +")";
	}

	public boolean goodIngredients() {
		return calc != null 
		        && ((BiChainedCalculator) calc).goodIngredients();
	}

}
