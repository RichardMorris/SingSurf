/*
Created 12-Jun-2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import java.awt.Choice;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import jv.project.PgGeometryIf;

import org.singsurf.singsurf.geometries.GeomPair;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.geometries.SSGeomListener;

public abstract class AbstractOperatorClient extends AbstractClient  implements SSGeomListener {
	/** A choice of avaliable inputs */
	protected Choice ch_inputSurf;
	/** String to represent no input */
	public static final String NONE = "-- None --";
	
	/** Pairs of input and output geometries indexed by the input name */
	protected Map<String,GeomPair> activePairs = new HashMap<String,GeomPair>();
	/** A list of activeInputs **/
	protected java.awt.List activeInputNames = new java.awt.List(4,true);

	public AbstractOperatorClient(GeomStore store, String projName) {
		super(store, projName);
	}

	@Override
	public void init(boolean flag) {
		super.init(flag);
		ch_inputSurf = new Choice();
		ch_inputSurf.addItemListener(this);
		ch_inputSurf.setEnabled(true);
		store.addGeomListner(this);
	}

	public void geometryNameHasChanged(String oldName, String newName) {
		if(activePairs.containsKey(oldName))
		{
			GeomPair p = activePairs.get(oldName);
			activePairs.remove(oldName);
			activePairs.put(newName,p);
			activeInputNames.remove(oldName);
			activeInputNames.add(newName);
		}
	}

	public void geometryHasChanged(String geomName) {
		if(activePairs.containsKey(geomName))
			calcGeom(activePairs.get(geomName));
	}

	/* (non-Javadoc)
	 * @see org.singsurf.singsurf.geometries.SSGeomListener#geometryIsRemoved(java.lang.String)
	 */
	public void removeGeometry(String geomName,boolean rmDependants) {
		if(activePairs.containsKey(geomName))
		{
			GeomPair p = activePairs.get(geomName);
			activePairs.remove(geomName);
			activeInputNames.remove(geomName);
			if(rmDependants)
				store.removeGeometry(p.getOutput(),rmDependants);
		}
	}

	public abstract void calcGeom(GeomPair p);
	public abstract void newActiveInput(String name);

	/* (non-Javadoc)
	 * @see org.singsurf.singsurf.clients.AbstractClient#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		ItemSelectable itSel = e.getItemSelectable();

		if(itSel == ch_inputSurf)
		{
			if(ch_inputSurf.getSelectedItem().equals(NONE)) return;
			newActiveInput(ch_inputSurf.getSelectedItem());
		}
		else super.itemStateChanged(e);
	}

	public void refreshList(SortedSet<String> list) {
		String item = this.ch_inputSurf.getSelectedItem();
		this.ch_inputSurf.removeAll();
		ch_inputSurf.add(NONE);
		for(String name: list)
			this.ch_inputSurf.add(name);
		this.ch_inputSurf.select(item);
	}
	/** Returns the preferred name for an output geom, given the input name.
	 * The eventual name of the output geom may be changed to avoid name clashes.
	 * Names are of the form <tt>project(name)</tt>.
	 * @param name the name of the input geom
	 * @return the name of the output geom
	 */
	public String getPreferredOutputName(String name)
	{
		return getName() + "(" + name +")";
	}

	@Override
	public List<PgGeometryIf> getOutputGeoms() {
		List<PgGeometryIf> outputs = new ArrayList<PgGeometryIf>();
		for(GeomPair p:activePairs.values())
			outputs.add(p.getOutput());
		return outputs;
	}

	public List<PgGeometryIf> getInputGeoms() {
		List<PgGeometryIf> inputs = new ArrayList<PgGeometryIf>();
		for(GeomPair p:activePairs.values())
			inputs.add(p.getInput());
		return inputs;
	}

	public Collection<GeomPair> getInputOutputPairs() {
		return activePairs.values();
	}
	@Override
	public void setName(String arg0) {
		super.setName(arg0);
		if(activePairs==null) return;
		for(GeomPair p:activePairs.values())
		{
			store.setName(p.getOutput(),getPreferredOutputName(p.getInput().getName()));
		}
	}

	@Override
	public void setDisplayProperties() {
		for(String input :activeInputNames.getSelectedItems())
		{
			GeomPair p = activePairs.get(input);
			setDisplayProperties(p.getOutput());
		}
		
	}
}
