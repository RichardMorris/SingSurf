package org.singsurf.singsurf;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.TextField;

import jv.object.PsConfig;
import jv.object.PsMainFrame;
import jv.viewer.PvViewer;

import org.singsurf.singsurf.clients.AbstractClient;
import org.singsurf.singsurf.geometries.GeomStore;

/**
 * Applet which provides the client end of the algebric surface visulisation tool.
 * 
 * @author		Richard Morris
 * @version		0.618	May 03: public release se releasenotes for details.
 *			0.4	Feb 03 : adding the mapping and intersect functions
 *			0.3	20 Oct 2001 : added lots of other servers
 *				: added drawing of degenerate lines
 * 		0.2.1	23 Jan 2001 : added definitions of default object (PjAcurveClient_IP)
 *					    : added a status bar when running as an application
 *					    : cleaned up debug messages to write to JavaView consol.
 *			0.2	15 Jan 2001
 */

abstract public class PaSingSurf extends Applet {
    private static final long serialVersionUID = 1L;

    static final boolean PRINT_DEBUG = false;

    /** Version number */
    String singSurfVersion = "2.0";

    /** frame if run standalone, null if run as applet. */
    public		Frame	m_frame			= null;

    /** 3D-viewer window for graphics output and which is embedded into the applet. */
    protected	PvViewer			m_viewer;
    /** Class which represents interface between SingSurf and JavaView */
    protected GeomStore store;
    /** Applet parameters: {"Name", "Type", "Default value", "Description"} */

    protected	String [][]	m_parm = {
            {"Console",	"String", "Hide",		"Show/Hide VGP-console for debugging"},
            {"Control",	"String", "Show",		"Show/Hide control panel"},
            {"Frame",	"String", "Show",		"Show/Hide frame around applet"},
            {"Panel",	"String", "Project",		"Name of initial panel if control panel is showing"},
            {"SSPanel",	"String", "Control",		"Where to place the Sing Surf Panel, Control, North, South, East, West"},
            {"ServerDir",	"String", "http://localhost/scripts/lsmp/asurf/",
            "Base directory for servers"},
            {"ServerExtension",	"String", ".exe",	"Extension used by executables"},
            {"AutoFit",	"String", "true",		"Whether rescale the display each time a geometry is loaded"},
            //		{"ExecDir",	"String", null,			"Optional executable for running on a local machine."},
            {"UseDefaultModel",	"String","true",	"Whether to start up with pre loaded model and project"},
            {"Raytracier",	"String","false",	"Whether the surf raytracier is enabled."},
            {"DefUrl",  "String","false",       "Url for finding the definition files."},
    };

    /** Panel for show the project panel in **/

    protected Panel pProject = null;

    /**
     * Interface used by design tools to show properties of applet.
     * This method returns a list of string arrays, each of length 4 rather than 3
     * as suggest by Java. The additional string at third position contains
     * the value of the parameter.
     * @see		jv.viewer.PvViewer#getParameter(String)
     */
    @Override
    public String[][] getParameterInfo() { return m_parm; }

    /** Interface of applet to inform about author, version, and copyright. */

    @Override
    public String getAppletInfo() {
        return "Name: "		+ this.getClass().getName()+ "\r\n" +
        "Author: "		+ "Richard Morris" + "\r\n" +
        "Version: "	+ singSurfVersion + "\r\n" +
        "Date: 11 Dec 2010\r\n" +
        "Visualise algebraic and other surfaces using JavaView and a server on the internet." + "\r\n";
    }

    /**
     * Configure and initialize the viewer, loads the PjAcurveClient project.
     */
    public TextField messageField = new TextField(40);
    @Override
    public void init() 
    {
        if(PRINT_DEBUG) System.out.println("PaSS init");

        // Create viewer for viewing 3d geometries
        m_viewer = new PvViewer(this, m_frame);
        // Get 3d display from viewer and add it to applet
        setLayout(new BorderLayout());
        Component disp = (Component)m_viewer.getDisplay();
        
        disp.setSize(325,550);
        add("Center",disp );
        add("South",messageField);
        m_viewer.getDisplay().setEnabled3DLook(true);
        store = new GeomStore(m_viewer);
        disp.validate();
        if(PRINT_DEBUG) System.out.println("PaSS done");
    }


    @Override
    public void showStatus(String arg0) {
        messageField.setText(arg0);
    }

    /**
     * Does clean-up when applet is destroyed by the browser.
     * Here we just close and dispose all our control windows.
     */
    @Override
    public void destroy()	{ m_viewer.destroy(); }

    /** Start viewer, e.g. start animation if requested */
    @Override
    public void start()
    { 
        if(PRINT_DEBUG) System.out.println("applet: start");
        //		System.out.println("Cur Proj"+m_viewer.getCurrentProject());
        m_viewer.start(); 
        //		System.out.println("Viewer Started");
    }

    /** Stop viewer, e.g. stop animation if requested */

    @Override
    public void stop()
    { 
        if(PRINT_DEBUG) System.out.println("Stop viewer");
        //		System.out.println("Cur Proj"+m_viewer.getCurrentProject());
        m_viewer.stop(); 
        //		System.out.println("Stop viewer done");
    }


    /** Print info while initializing applet and viewer. */

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.blue);
        g.drawString("JavaView, Version "+PsConfig.getVersion(), 20, 40);
        g.drawString("Loading Project:", 20, 60);
        g.drawString("SingSurf Version "+ singSurfVersion, 20, 100);
    }


    /** Loads the definition with the specified name in the current project. 
	Used from javascript to change the selected definition. **/

    public void loadDefinitionByName(String name)
    {
        System.out.println("loadModel " + name);
        AbstractClient myProj = (AbstractClient) m_viewer.getCurrentProject();
        //boolean flag = 
        myProj.loadDefByName(name);
//        String str = 	m_viewer.getParameter("ExecDir");
//        if(flag)
//        {
//            if( str == null || str.equals("") )
//                myProj.calculate();
//            else
//                showStatus("Press the calculate button to calculate the surface");
//        }
    }

    
    /**
     * Common standalone startup script.
     * @param va Applet we want to run
     * @param args arguments to applet
     */
    protected static void commonMain(PaSingSurf va,String [] args)
    {
        // Create toplevel window of application containing the applet
        PsMainFrame frame	= new PsMainFrame(va, args);
        frame.setBounds(new Rectangle(395, 5, 630, 650));
        //		frame.setBounds(new Rectangle(100, 5, 830, 550));
        //frame.setIconImage(va.getToolkit().getImage("images/icon.gif"));
        frame.setVisible(true);
        //frame.
        va.m_frame = frame;
        va.init();
        va.start();
    }

}
