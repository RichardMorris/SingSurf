/* @author rich
 * Created on 31-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import jv.object.PsDebug;
import jv.object.PsPanel;
import jv.object.PsUpdateIf;
import jv.objectGui.PsTabPanel;
import jv.project.PjProject_IP;

/**
 * @author Rich Morris
 * Created on 31-Mar-2005
 */
public class Mapping_IP extends PjProject_IP  implements ActionListener {
	private static final long serialVersionUID = -5968740970457076690L;
	/** Refrerence to main PjPsurfJepNew class */
	Mapping parent;

	/**
	 * 
	 */
	public Mapping_IP() {
		super();
//		System.out.println("IP constructor");
				if (getClass() == Mapping_IP.class)
					init();
//		System.out.println("IP constructor done");
	}

	// Initialization.
	public void init() {
//System.out.println("IP init");

		super.init();
		addTitle("Mapping");
		//bRngConfig = new Button("Set range Max/Mins");
//System.out.println("IP init done");
	}

	
	public void setParent(PsUpdateIf par) {
//		System.out.println("IP setParent");

		super.setParent(par);

		parent = (Mapping)par;
		parent.m_IP = this;

		PsPanel p1,p3,p4;

//				addSubTitle("URL for Asurf server");
//				add(m_PjPsurfClient.tf_asurfURL);
//				addSubTitle("Temporary File");
//				add(m_PjPsurfClient.tf_tmpFile);

		p1 = new PsPanel();
		p3 = new PsPanel();
		p4 = new PsPanel();

//				p1.addSubTitle("Definition");
		p1.add(parent.taDef);

		if(parent.chDefs!=null)
		{
			p1.addSubTitle("Pre-defined mappings:");
			p1.add(parent.chDefs);
		}

/*
		fract = new Fractometer(0.1234,0.01);
		Button b1 = new Button("FractVal");
		b1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Fract value "+fract.getValue());
			}});
		p1.add(fract);
		p1.add(b1);
*/

//				p2.addSubTitle("Region of interest");
		//PsPanel p5 = new PsPanel(new GridLayout(1,2));

		//ScrollPane pane = new ScrollPane();
		p3.add(parent.newParams);
		//p3.add(pane);
//				p3.addSubTitle("Resolution control");
		p4.add(parent.m_Clipping.assureInspector(PsPanel.INFO, PsPanel.INFO_EXT));
		p4.add(parent.m_ContDist.assureInspector(PsPanel.INFO, PsPanel.INFO_EXT));
		p4.add(new Label("Select geometry in Current Inputs to change appearance"));
		PsPanel p5 = new PsPanel(new FlowLayout(FlowLayout.RIGHT));
		p5.add(parent.cbColour);
		p5.add(new Label("curves colours: "));
		p5.add(parent.chColours);
		p4.add(p5);
		p4.add(parent.cbCreateNew);
		p4.add(parent.cbShowFace);
		p4.add(parent.cbShowEdge);
		p4.add(parent.cbShowCurves);
		p4.add(parent.cbShowVert);
		p4.add(parent.cbKeepMat);

        PsTabPanel tabPanel = new PsTabPanel();
    	add(tabPanel); // add tabbed panel like any other panel
    	tabPanel.addPanel("Definition", p1);
    	//tabPanel.addPanel("Domain", p2);
    	tabPanel.addPanel("Parameters", p3);
    	tabPanel.addPanel("Options",p4);
    	tabPanel.setVisible("Definition"); // select initially active panel
    	tabPanel.validate();                 // layout panel again
	
		addLine(1);
		addSubTitle("New Input Geometry");
		add(parent.ch_inputSurf);
		addSubTitle("Current Inputs");
		add(parent.activeInputNames);
		//parent.activeInputNames.addItemListener(parent);
		add(parent.m_go);
		parent.m_go.addActionListener(this);
		
	      PsPanel pSave = new PsPanel(new GridLayout(2,1));
	        pSave.add(parent.bLoad);
	        pSave.add(parent.bSave);
	        add(pSave);


//		System.out.println("IP setParent done");
	}


	
	/* (non-Javadoc)
	 * @see jv.object.PsUpdateIf#update(java.lang.Object)
	 */
	public boolean update(Object o) {
		if (parent==null) {
			if (PsDebug.WARNING) PsDebug.warning("PjPsurfClient_IP.update(): missing parent, setParent not called");
			return false;
		}
		if (o == parent) {
			return true;
		}
		return super.update(o);
	}


	public void actionPerformed(ActionEvent event) {
		if (parent==null)
			return;
		Object source = event.getSource();
		if (source == parent.m_go) {
			//cmproj.init();
			parent.update(parent.m_go);
		}
	}
}
