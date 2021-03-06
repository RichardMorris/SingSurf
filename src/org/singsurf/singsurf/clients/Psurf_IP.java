/* @author rich
 * Created on 31-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
public class Psurf_IP extends PjProject_IP  implements ActionListener {
	private static final long serialVersionUID = -6486350319171058728L;
	/** Reference to main PjPsurfJepNew class */
	Psurf parent;

	/**
	 * 
	 */
	public Psurf_IP() {
		super();
//		System.out.println("IP constructor");
				if (getClass() == Psurf_IP.class)
					init();
//		System.out.println("IP constructor done");
	}

	// Initialization.
	public void init() {
//System.out.println("IP init");

		super.init();
		addTitle("Parameterized Surfaces");
//System.out.println("IP init done");
	}

	
	public void setParent(PsUpdateIf par) {
//		System.out.println("IP setParent");

		super.setParent(par);

		parent = (Psurf)par;
		parent.m_IP = this;

		PsPanel p1,p2,p3,p4;

//				addSubTitle("URL for Asurf server");
//				add(m_PjPsurfClient.tf_asurfURL);
//				addSubTitle("Temporary File");
//				add(m_PjPsurfClient.tf_tmpFile);

		p1 = new PsPanel();
		p2 = new PsPanel();
		p3 = new PsPanel();
		p4 = new PsPanel();

//				p1.addSubTitle("Definition");
		p1.add(parent.taDef);

		if(parent.chDefs!=null)
		{
			p1.addSubTitle("Pre-defined surfaces:");
			p1.add(parent.chDefs);
		}

		PsPanel p5 = new PsPanel();
		GridBagLayout gridbag = new GridBagLayout();
		p5.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;

		p5.add(parent.displayVars[0].getMinLabel(),c);
		c.gridx = 1;
		p5.add(parent.displayVars[0].getMinPanel(),c);
		c.gridx = 0; c.gridy++;
		p5.add(parent.displayVars[0].getMaxLabel(),c);
		c.gridx = 1;
		p5.add(parent.displayVars[0].getMaxPanel(),c);

		c.gridx = 0; c.gridy++;
		p5.add(parent.displayVars[1].getMinLabel(),c);
		c.gridx = 1;
		p5.add(parent.displayVars[1].getMinPanel(),c);
		c.gridx = 0; c.gridy++;
		p5.add(parent.displayVars[1].getMaxLabel(),c);
		c.gridx = 1;
		p5.add(parent.displayVars[1].getMaxPanel(),c);
		p2.add(p5);
		p2.add(parent.displayVars[0].getStepsPanel());
		p2.add(parent.displayVars[1].getStepsPanel());

		//ScrollPane pane = new ScrollPane();
		p3.add(parent.newParams);
		//p3.add(pane);
//				p3.addSubTitle("Resolution control");
		p4.add(parent.m_Clipping.assureInspector(PsPanel.INFO, PsPanel.INFO_EXT));
		p4.add(parent.cbColour);
		p4.add(parent.cbCreateNew);
		p4.add(parent.cbShowFace);
		p4.add(parent.cbShowEdge);
		p4.add(parent.cbShowVert);
		p4.add(parent.cbKeepMat);

        PsTabPanel tabPanel = new PsTabPanel();
    	add(tabPanel); // add tabbed panel like any other panel
    	tabPanel.addPanel("Definition", p1);
    	tabPanel.addPanel("Domain", p2);
    	tabPanel.addPanel("Parameters", p3);
    	tabPanel.addPanel("Options",p4);
    	tabPanel.setVisible("Definition"); // select initially active panel
    	tabPanel.validate();                 // layout panel again
	
		addLine(1);
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
/*		if(o == parent.m_Xmax || o == parent.m_Xmax || o == parent.m_Xstep)
		{
			System.out.println("X changed PsJIP");
			parent.variableChanged("x",
					parent.m_Xmin.getValue(),parent.m_Xmax.getValue(),parent.m_Xstep.getValue());
		}
		if(o == parent.m_Ymax || o == parent.m_Ymax || o == parent.m_Ystep)
		{
			System.out.println("Y changed");
			parent.variableChanged("y",
					parent.m_Ymin.getValue(),parent.m_Ymax.getValue(),parent.m_Ystep.getValue());
		}
*/
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
