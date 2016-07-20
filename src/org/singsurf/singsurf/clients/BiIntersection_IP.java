/*
Created 6 Oct 2006 - Richard Morris
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

public class BiIntersection_IP extends PjProject_IP  implements ActionListener {
	private static final long serialVersionUID = -6967264854566009554L;

	/** Reference to main PjPsurfJepNew class */
	BiIntersection parent;

	/**
	 * 
	 */

	public BiIntersection_IP() {
		super();
				if (getClass() == BiIntersection_IP.class)
					init();
	}

	// Initialization.
	public void init() {
//System.out.println("IP init");

		super.init();
		addTitle("Chained Intersection");
		//bRngConfig = new Button("Set range Max/Mins");
//System.out.println("IP init done");
	}

	
	public void setParent(PsUpdateIf par) {
//		System.out.println("IP setParent");

		super.setParent(par);

		parent = (BiIntersection)par;
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
			p1.addSubTitle("Pre-defined intersection:");
			p1.add(parent.chDefs);
		}
		p1.addSubTitle("Ingredients:");
		p1.add(parent.ch_ingredient1);
        p1.add(parent.ch_ingredient2);
//		p1.add(parent.cbProject);
		//ScrollPane pane = new ScrollPane();
		p3.add(parent.newParams);
		//p3.add(pane);
//				p3.addSubTitle("Resolution control");
		//p4.add(parent.m_Clipping.assureInspector(PsPanel.INFO, PsPanel.INFO_EXT));
		p4.add(new Label("Select geometry in Current Inputs to change appearance"));
		p4.add(parent.cbColour);
		PsPanel p5 = new PsPanel(new FlowLayout(FlowLayout.RIGHT));
		p5.add(new Label("curves colours: "));
		p5.add(parent.chColours);
        p5.add(new Label("Itterations: "));
        p5.add(parent.chItts);

		p4.add(p5);
		p4.add(parent.cbCreateNew);
//		p4.add(parent.cbShowFace);
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
