/* @author rich
 * Created on 31-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import jv.object.PsDebug;
import jv.object.PsPanel;
import jv.object.PsUpdateIf;
import jv.objectGui.PsTabPanel;
import jv.project.PjProject_IP;

/**
 * @author Rich Morris Created on 31-Mar-2005
 */
public class ACurve_IP extends PjProject_IP implements ActionListener {
    private static final long serialVersionUID = -8025333797850761023L;

    /** Reference to main PjPsurfJepNew class */
    ACurve parent;

    /**
	 * 
	 */
    public ACurve_IP() {
        super();
        // System.out.println("IP constructor");
        if (getClass() == ACurve_IP.class)
            init();
        // System.out.println("IP constructor done");
    }

    // Initialization.
    @Override
    public void init() {
        // System.out.println("IP init");

        super.init();
        addTitle("Algebraic Curve");
        // System.out.println("IP init done");
    }

    @Override
    public void setParent(PsUpdateIf par) {
        // System.out.println("IP setParent");

        super.setParent(par);

        parent = (ACurve) par;
        parent.m_IP = this;

        PsPanel p1, p2, p3, p4;

        // addSubTitle("URL for Asurf server");
        // add(m_PjPsurfClient.tf_asurfURL);
        // addSubTitle("Temporary File");
        // add(m_PjPsurfClient.tf_tmpFile);

        p1 = new PsPanel();
        p2 = new PsPanel();
        p3 = new PsPanel();
        p4 = new PsPanel();

        // p1.addSubTitle("Definition");
        p1.add(parent.taDef);

        if (parent.chDefs != null) {
            p1.addSubTitle("Pre-defined surfaces:");
            p1.add(parent.chDefs);
        }
        
        PsPanel p2a = new PsPanel();
        GridBagLayout gridbag = new GridBagLayout();
        p2a.setLayout(gridbag);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.weightx = 1;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;


        // p2.addSubTitle("Region of interest");
        
        p2a.add(parent.displayVars[0].getMinLabel(),c);
        c.gridx = 1;
        p2a.add(parent.displayVars[0].getMinPanel(),c);
        c.gridx = 0; c.gridy++;
        p2a.add(parent.displayVars[0].getMaxLabel(),c);
        c.gridx = 1;
        p2a.add(parent.displayVars[0].getMaxPanel(),c);

        c.gridx = 0; c.gridy++;
        p2a.add(parent.displayVars[1].getMinLabel(),c);
        c.gridx = 1;
        p2a.add(parent.displayVars[1].getMinPanel(),c);
        c.gridx = 0; c.gridy++;
        p2a.add(parent.displayVars[1].getMaxLabel(),c);
        c.gridx = 1;
        p2a.add(parent.displayVars[1].getMaxPanel(),c);

        p2.add(p2a);

        // ScrollPane pane = new ScrollPane();
        p3.add(parent.newParams);
        // p3.add(pane);
        // p3.addSubTitle("Resolution control");

        Panel p4a = new Panel(new FlowLayout(FlowLayout.LEFT, 1, 0));
        p4a.add(new Label("Resolution:"));
//        p4a.add(parent.cb_c_4);
//        p4a.add(parent.cb_c_8);
        p4a.add(parent.cb_c_16);
        p4a.add(parent.cb_c_32);
        p4a.add(parent.cb_c_64);
        p4.add(p4a);
        Panel p4b = new Panel(new FlowLayout(FlowLayout.LEFT, 1, 0));
        p4b.add(new Label("           "));
        p4b.add(parent.cb_c_128);
        p4b.add(parent.cb_c_256);
//        p4b.add(new Label("Fine:"));
//        p4b.add(parent.cb_fi_8);
//        p4b.add(parent.cb_fi_16);
//        p4b.add(parent.cb_fi_32);
//        p4b.add(parent.cb_fi_64);
//        p4b.add(parent.cb_fi_128);
//        p4b.add(parent.cb_fi_256);
//        p4b.add(parent.cb_fi_512);
//        p4b.add(parent.cb_fi_1024);
        p4.add(p4b);

        // p4.add(parent.cb_colour);
        PsPanel p5 = new PsPanel(new FlowLayout(FlowLayout.RIGHT));
        p5.add(new Label("colours: "));
        p5.add(parent.chColours);
        p4.add(p5);
        p4.add(parent.cbCreateNew);
        // p4.add(parent.cb_keepMat);
        p4.add(parent.cbShowCurves);
        p4.add(parent.cbShowVert);

        PsTabPanel tabPanel = new PsTabPanel();
        add(tabPanel); // add tabbed panel like any other panel
        tabPanel.addPanel("Definition", p1);
        tabPanel.addPanel("Domain", p2);
        tabPanel.addPanel("Parameters", p3);
        tabPanel.addPanel("Options", p4);
        tabPanel.setVisible("Definition"); // select initially active panel
        tabPanel.validate(); // layout panel again

        addLine(1);
        add(parent.m_go);
        parent.m_go.addActionListener(this);
        
        PsPanel pSave = new PsPanel(new GridLayout(2,1));
        pSave.add(parent.bLoad);
        pSave.add(parent.bSave);
        add(pSave);

        // System.out.println("IP setParent done");
    }

    /*
     * (non-Javadoc)
     * 
     * @see jv.object.PsUpdateIf#update(java.lang.Object)
     */
    @Override
    public boolean update(Object o) {
        if (parent == null) {
            if (PsDebug.WARNING)
                PsDebug
                        .warning("PjPsurfClient_IP.update(): missing parent, setParent not called");
            return false;
        }
        if (o == parent) {
            return true;
        }
        return super.update(o);
    }

    public void actionPerformed(ActionEvent event) {
        if (parent == null)
            return;
        Object source = event.getSource();
        if (source == parent.m_go) {
            // cmproj.init();
            parent.update(parent.m_go);
        }
    }
}
