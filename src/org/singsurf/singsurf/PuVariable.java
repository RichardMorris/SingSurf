/**
 * 
 */
package org.singsurf.singsurf;

import java.awt.Component;
import java.awt.Label;
import java.awt.Panel;

import jv.number.PuInteger;
import jv.object.PsObject;
import jv.object.PsPanel;

import org.singsurf.singsurf.definitions.DefVariable;

public class PuVariable  extends PsObject {
	Label minLabel,maxLabel;
	Fractometer minControl,maxControl;
	PuInteger stepsControl;
	PsObject parent;
	Panel minPanel,maxPanel;
	public PuVariable(PsObject obj,DefVariable var)
	{
		this(obj,var.getName(),var.getMin(),var.getMax(),var.getSteps());
	}
	public PuVariable(PsObject obj,String name,double min,double max,int steps) {
		parent = obj;
		minLabel = new Label(name+" min");
		minControl = new Fractometer(min);
		minControl.setParent(this);
		maxControl = new Fractometer(max);
		maxControl.setParent(this);
		maxLabel = new Label(name+" max");
		stepsControl = new PuInteger(name+" steps",this);
		stepsControl.setDefBounds(0,100,1,10);
		stepsControl.setBounds(0,100,1,10);
		stepsControl.setDefValue(steps);
		stepsControl.setValue(steps);
		

	}
	public double getMin() {
		return minControl.getValue();
	}
	public double getMax() {
		return maxControl.getValue();
	}
	public int getSteps() {
		return stepsControl.getValue();
	}
	public void setMin(double m) {
		minControl.setValue(m);
	}
	public void setMax(double m) {
		maxControl.setValue(m);
	}
	public void setSteps(int s) {
		stepsControl.setValue(s);
	}
	public void setBounds(double mi,double ma,int s) {
		minControl.setValue(mi);
		maxControl.setValue(ma);
		stepsControl.setValue(s);
	}
	public void set(DefVariable var) {
		String name = var.getName();
        minLabel.setText(name + " min");
		minControl.setValue(var.getMin());
		maxLabel.setText(name + " max");
		maxControl.setValue(var.getMax());
		stepsControl.setName(name + " steps");
		stepsControl.setDefValue(var.getSteps());
		stepsControl.setValue(var.getSteps());
	}
	public Component getMinLabel() {
		return minLabel;
	}
	public Component getMaxLabel() {
		return maxLabel;
	}
	public Panel getMinPanel() {
		return minControl.getPanel();
	}
	public Panel getMaxPanel() {
		return maxControl.getPanel();
	}
	public PsPanel getStepsPanel() {
		return stepsControl.assureInspector(PsPanel.INFO, PsPanel.INFO_EXT);
	}

	@Override
    public boolean update(Object arg0) {
//		System.out.println("Lparam update"+arg0.toString());
		return parent.update(this);
	}
	public Fractometer getMaxControl() {
		return maxControl;
	}
	public Fractometer getMinControl() {
		return minControl;
	}
	public PuInteger getStepsControl() {
		return stepsControl;
	}

}