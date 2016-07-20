/* @author rich
 * Created on 31-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf;

import java.awt.Component;
import java.awt.Label;
import java.awt.Panel;

import org.singsurf.singsurf.definitions.Parameter;

import jv.number.PuDouble;
import jv.object.PsObject;
import jv.object.PsPanel;

/**
 * A control for a single double value.
 * Based around PuDouble, but adds a ref field.
 * This field is useful when combined with MRpeEval
 * as it can store a reference to the corresponding variable.
 * 
 * @author Rich Morris
 * Created on 31-Mar-2005
 */
public class PuParameter extends PsObject {
		Fractometer control;
		Label label;
		PsObject parent;
		int ref=-1;
		/** Create an PuParameter object.
		 * 
		 * @param obj a reference to the parent object which is notified of changes.
		 * @param name the name of the variable
		 * @param ref used to store a reference.
		 */
		public PuParameter(PsObject obj,Parameter p) {
			parent = obj;
			label = new Label(p.getName());
			control = new Fractometer(p.getVal());
			control.setParent(this);
		}
		public String getName() { return label.getText(); }
		
		public boolean update(Object arg0) {
//			System.out.println("Lparam update"+arg0.toString());
			return parent.update(this);
		}
		
		public Component getLabel() {
			return label;
		}
		public Panel getControlPanel() {
			return control.getPanel();
		}
		
		public double getVal() {return control.getValue();}
		/**
		 * @param ref The ref to set.
		 */
		public void setVal(double val) {
			control.setValue(val);
		}
		void setRef(int r) { ref=r;}
		int getRef() { return ref;}
		public Fractometer getControl() {
			return control;
		}
		
		
	}