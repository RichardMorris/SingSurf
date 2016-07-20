/*
Created 16-Jun-2006 - Richard Morris
*/
package org.singsurf.singsurf.clients;

import java.awt.TextArea;

import jv.project.PjProject;

/**
 * @author Richard Morris
 *
 */
public class SSHelp extends jv.project.PjProject {

	/** The main text area for the equation. **/
	protected	TextArea	helpText;

	/**
	 * 
	 */
	public SSHelp() {
		super("SingSurf Help");
		init();
	}

	public void init()
	{
		helpText = new TextArea("",40,40,TextArea.SCROLLBARS_VERTICAL_ONLY );
		helpText.setText("To create a new curve or surface use the\n"+
				"New menu");
		helpText.setEditable(false);
		super.init();
	}
}
