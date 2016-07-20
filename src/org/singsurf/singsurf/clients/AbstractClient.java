package org.singsurf.singsurf.clients;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.ItemSelectable;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.object.PsDebug;
import jv.project.PgGeometryIf;
import jv.project.PjProject;
import jv.project.PjProject_IP;

import org.singsurf.singsurf.Calculator;
import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.LmsElementSetMaterial;
import org.singsurf.singsurf.LmsPointSetMaterial;
import org.singsurf.singsurf.LmsPolygonSetMaterial;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.definitions.LsmpDef;
import org.singsurf.singsurf.definitions.LsmpDefReader;
import org.singsurf.singsurf.geometries.GeomStore;


/**
 * JavaView project which is sub classed by all the different LSMP project.
 * <p>
 * This class contains 
 * @author		Richard Morris
 */

public abstract class AbstractClient extends PjProject implements ItemListener, ActionListener
{
	private static final long serialVersionUID = 1L;
    static final boolean PRINT_TIME = false;
	static final boolean PRINT_DEBUG = false;
	static final boolean JAVA_1_2 = false;
	static final boolean DEBUG_ECHO_INPUT = false;
    
    Frame m_frame;

	/** Material properties for faces. */
	LmsElementSetMaterial face_mat = null;
	/** Material properties for lines. */
	LmsPolygonSetMaterial line_mat = null;
	/** Material properties for points. */
	LmsPointSetMaterial point_mat = null;

	/** The name of the base geometry. For project where the full geometry
	 * consists of surfaces, lines and points this the names of the secondary geoms will be constructed from the baseName. **/
	private		String		baseName;


	//******************** Definitions of geometries and defaults

	/** Array of definitions */
	protected LsmpDef lsmpDefs[] = null;

	/** The definition. TODO needed when called before text area define. 	 */
	String s_def;
	//*********** Info Panel elements *************/

   	/** The Calculate button. When pressed the surface will be calculated.  */
	protected	Button		m_go;

	/** The main text area for the equation. **/
	protected	TextArea	taDef;

	/** Checkbox for selecting whether colour info should be calculated. **/
    protected       Checkbox        cbColour;

	/** Checkbox for selecting whether new objects should be created each time. **/
    protected       Checkbox        cbCreateNew;

	/** Checkbox for selecting whether the existing material properties should be retained */
    protected       Checkbox        cbKeepMat;

    /** Checkbox for whether to draw faces */
    protected       Checkbox        cbShowFace;
    /** Checkbox for whether to draw edges */
    protected       Checkbox        cbShowEdge;
    /** Checkbox for whether to draw vertices */
    protected       Checkbox        cbShowVert;
    /** Checkbox for whether to draw curves */
    protected       Checkbox        cbShowCurves;
    /** Checkbox for whether to draw points */
    protected       Checkbox        cbShowPoints;
    /** Checkbox for whether to draw boundary */
    protected       Checkbox        cbShowBoundary;
    
   	/** This contains the list of pre-defined definitions. */
   	protected	Choice	chDefs;
   	/** Choice for colours */
   	protected   Choice chColours;
   	
   	protected Button bLoad;
   	protected Button bSave;
    
	
	//******************* Various other fields
	
	/** The project panel **/
	protected PjProject_IP	m_IP;

	/** The main GeomStore object */
	protected GeomStore store;
	/** A reference to the containing viewer. */
	//protected	PvViewerIf	myViewer = null;
	/** A reference to the containing applet. */
	//protected	Applet		myApplet;

	protected Choice ch_auxSurf;
	protected Calculator calc;
	protected LParamList newParams;
	/** Whether changing variable or parameters force a redrawing of surface */
	protected boolean autoUpdate=true;

	/** Whether to do a fit display after constructing geoms. **/
	//public static	boolean doFitDisplay = false;

	private AbstractClient() {
		super("LSMP surface Client");
	}

	/** Constructor which just passes project name to super class. *
	 * @param store TODO*/

	public AbstractClient(GeomStore store, String projName)
	{
		super(projName);
		if(PRINT_DEBUG) System.out.println("PjLC constructor");
		this.store = store;
//              tf_tmpFile = new TextField(s_tmpFile);
//              tf_asurfURL = new TextField(s_asurfURL);

		cbColour = new Checkbox("Draw in Colour",true);
		cbCreateNew = new Checkbox("Create new geoms",false);
		cbKeepMat = new Checkbox("Keep materials props",false);
		cbShowFace = new Checkbox("Show faces",true);
		cbShowEdge = new Checkbox("Show edges",false);
		cbShowVert = new Checkbox("Show vertices",false);
		cbShowCurves = new Checkbox("Show curves",true);
		cbShowPoints = new Checkbox("Show points",true);
        cbShowBoundary = new Checkbox("Show boundary",false);
		cbShowFace.addItemListener(this);
		cbShowEdge.addItemListener(this);
		cbShowVert.addItemListener(this);
		cbShowCurves.addItemListener(this);
		cbShowPoints.addItemListener(this);
        cbShowBoundary.addItemListener(this);
		cbColour.addItemListener(this);
		chColours = new Choice();
		chColours.addItem("Black");
		chColours.addItem("Red");
		chColours.addItem("Green");
		chColours.addItem("Blue");
		chColours.addItem("Cyan");
		chColours.addItem("Magenta");
		chColours.addItem("Yellow");
		chColours.addItem("White");
		chColours.addItem("None");
		chColours.addItemListener(this);
		m_go = new Button("Calculate");
//		dRngConfig = new Dialog(PsConfig.getFrame(),true);
		//m_geom = new PgElementSet(3);
		//m_line_geom = new PgPolygonSet(3);
		//m_point_geom = new PgPointSet(3);


		if (getClass() == AbstractClient.class)
			init();
		if(PRINT_DEBUG) System.out.println("PjLC constructor done");
	}

	String getFullPath(String fileName)
	{
		return store.getFullPath(fileName);
	}

	/**
	 * Initialise the project. 
	 */
	
	public void init(boolean flag) {
		if(PRINT_DEBUG) System.out.println("PjLC init");
	
	    taDef = new TextArea("",15,10);
	    if(flag)
	    {
	    	chDefs = new Choice();
	    	loadDefs();
	    	chDefs.addItemListener(this);
	    	chDefs.select(getDefaultDefName());
	    }
	    else
	    	chDefs = null;
	    
	    bLoad = new Button("Load");
	    bSave = new Button("Save");
	    bLoad.addActionListener(this);
        bSave.addActionListener(this);
		super.init();
		if(PRINT_DEBUG) System.out.println("PjLC init done");
	}

	@Override
    public void init() {this.init(true);}
	
	/** Second stage initialisation, called after project registered with viewer. **/
	
	public void init2() {	}

	/**
	 * Start method is invoke when project is selected in the viewer.
	 */
	
	@Override
    public void start() {
		if(PRINT_DEBUG) System.out.println("PjLC start");
		super.start();
		if(PRINT_DEBUG) System.out.println("PjLC start done");
	}

	/**
	 * Stop method is invoke when project is de-selected in the viewer.
	 */

	@Override
    public void stop() {
		if(PRINT_DEBUG) System.out.println("PjLC stop");
		super.stop();
		if(PRINT_DEBUG) System.out.println("PjLC stop done");
	}

	/** Print a message with a timestamp. **/
	
	public static void timemessage(String str)
	{
		long t = System.currentTimeMillis()/10;
		long tsec = (t/100) % 100;
		long thund = t % 1000;
		Runtime r = Runtime.getRuntime();
		System.out.println(tsec+"."+thund+"\t"+str
			+"\tmem "+r.totalMemory()/1024+"K\tfree "+r.freeMemory()/1024+"K");
	}

	public void loadDefs()
	{
		try
		{
			LsmpDefReader ldr = new LsmpDefReader(getFullPath(getDefinitionFileName()));
			ldr.read();
			for(LsmpDef def:ldr.getDefs())
			{
					chDefs.addItem(def.getName());
			}
		}
		catch(Exception e)
		{
			PsDebug.warning("Error reading definition file: "+e.getMessage());
		}
	}

	public LsmpDef getDef(String name)
	{
		for(int i=0;i<lsmpDefs.length;++i)
			if(lsmpDefs[i].getName().equals(name))
				return lsmpDefs[i];
		return null;
	}
	
	public void calculate()
	{
		equationChanged(taDef.getText());
	}
	
	/**
	 * Update method of project. Responds to mouse events. 
	 */

	@Override
    public boolean update(Object event) 
	{
		if(PRINT_DEBUG) System.out.println("PjLC update");
		if( event!=null && event==m_go)
		{
			calculate();
			return true;
		}
		return super.update(event);
	}

/**
 * Overwrite method of superclass to be able to react when new geometry
 * is loaded from file by menu import.
 * This method is invoked when the import menu is pressed.
 */

@Override
public boolean addGeometry(PgGeometryIf geom) {
//		PsDebug.message("PjAsurfClient.addGeometry(): new geometry added.");
		return super.addGeometry(geom);
}

/**
 * Overwrite method of superclass to be able to react when new geometry
 * is loaded from file by menu import.
 * This method is invoked when the imported geometry is accepted.
 */

@Override
public void selectGeometry(PgGeometryIf geom) {
//		PsDebug.message("PjAsurfClient.selectGeometry(): new geometry selected.");
		super.selectGeometry(geom);
}

/**
 * Overwrite method of superclass to be able to react when new geometry
 * is loaded from file by menu import.
 * This method is invoked when the imported geometry is cancelled.
 */

@Override
public void removeGeometry(PgGeometryIf geom) {
//		PsDebug.message("PjAsurfClient.removeGeometry(): geometry removed.");
		super.removeGeometry(geom);
}


/** Set the base name for the geometries **/

private final void setBaseName(String name)
{
	baseName = name;
}

/** Get the base name for the geometries **/

protected final String getBaseName()
{
	return baseName;
}


/* (non-Javadoc)
 * @see jv.object.PsObject#setName(java.lang.String)
 */
@Override
public void setName(String arg0) {
	if(calc!=null)
		calc.setName(arg0);
	super.setName(arg0);
}

/** Sets the defining equation **/

public void setDisplayEquation(String def)
{
	if(taDef != null)
		taDef.setText(def);
	else
		s_def = def;


}

/** Gets the defining equation **/

public String getDisplayEquation()
{
	if(taDef != null)
		return taDef.getText();
	else
		return s_def;
}



/** Loads a definition.
	Typically this should be overwritten
	to load a def of the specified type. 
	TODO make an abstract method. **/

public boolean loadDefByName(String s) 
{
	PsDebug.warning("Loading definition by name not supported for current project");
	showStatus("Loading definition by name not supported for current project");
	return false;
}

/** Shows a Status message */

public void showStatus(String str)
{
	store.showStatus(str);
}

/** Gets an applet parameter */

@Override
public String getParameter(String str)
{
	return store.getParameter(str);
}

abstract public String getDefinitionFileName();

abstract public String getDefaultDefName();

public void itemStateChanged(ItemEvent e)
{
	ItemSelectable itSel = e.getItemSelectable();
	if(itSel == cbShowFace 
	 || itSel == cbShowEdge
	 || itSel == cbShowVert
	 || itSel == cbShowCurves
	 || itSel == cbShowPoints
	 || itSel == cbShowBoundary
	 || itSel == chColours ) {
		setDisplayProperties();
	}
}

public abstract void setDisplayProperties();
/**
 * Sets the display properties of the output geom according to state of different checkboxes.
 * Gracefully ignores case when geom is null.
 */
public void setDisplayProperties(PgGeometryIf geom)
{
	if(geom==null) return;
	if(geom instanceof PgElementSet) {
		((PgElementSet) geom).showElements(cbShowFace.getState());
		((PgElementSet) geom).showEdges(cbShowEdge.getState());
		((PgElementSet) geom).showVertices(cbShowVert.getState());
        ((PgElementSet) geom).showBoundaries(cbShowBoundary.getState());
	}
	else if(geom instanceof PgPolygonSet) {
		((PgPolygonSet) geom).showPolygons(cbShowCurves.getState());
		((PgPolygonSet) geom).showVertices(cbShowVert.getState());
		setColour((PgPolygonSet) geom,chColours.getSelectedItem());
	}
	else if(geom instanceof PgPointSet) {
		((PgPointSet) geom).showVertices(cbShowPoints.getState());
	}
	store.geomApperenceChanged(geom);
}
/*
public void  refreshGeomList(Vector allSurfs) {}
*/
/*
public void showSurf(PgGeometryIf resultGeom) {
	if(resultGeom==null) return;
	if(outGeom==null){
		outGeom=resultGeom;
		setDisplayProperties(outGeom);
		myViewer.getDisplay().addGeometry(outGeom);
		myViewer.setGeometry(outGeom);
	}
	else if(outGeom==resultGeom) {
		setDisplayProperties(outGeom);
		myViewer.setGeometry(outGeom);
		outGeom.update(null);
	}
	else {
	PgGeometryIf targetGeom = getGeometry(resultGeom.getName());
	if(targetGeom==null)
	{
		outGeom=resultGeom;
		setDisplayProperties(outGeom);
		myViewer.getDisplay().addGeometry(outGeom);
		myViewer.setGeometry(outGeom);
	}
	else
	{
		boolean flag=false;
		if(targetGeom instanceof PgElementSet) {
			if(resultGeom instanceof PgElementSet) {
				((PgElementSet) targetGeom).copy((PgElementSet) resultGeom);
				flag = true;
			}
		}
		else if(targetGeom instanceof PgPolygonSet) {
			if(resultGeom instanceof PgPolygonSet) {
				((PgPolygonSet) targetGeom).copy((PgPolygonSet)resultGeom);
				flag = true;
			}
		}
		else if(targetGeom instanceof PgPointSet) {
			if(resultGeom instanceof PgPointSet) {
				((PgPointSet) targetGeom).copy((PgPointSet)resultGeom);
				flag = true;
			}
		}
		if(flag)
		{
			outGeom=targetGeom;
			outGeom.update(null);
			myViewer.setGeometry(outGeom);
		}
		else
		{
			outGeom.setName(outGeom.getName()+"#1");
			myViewer.getDisplay().addGeometry(outGeom);
			myViewer.setGeometry(outGeom);
		}
	}
	}
	showStatus(getProgramName() + " calculated");
}
*/
	public abstract String getProgramName(); 
/*	
	public PgGeometryIf getGeometry(String name)
	{
		return null;
	}
*/
	protected void refreshParams() {
		newParams.reset();
		int size = calc.getNParam();
		for(int i=0;i<size;++i)
			newParams.addParameter(calc.getParam(i));
		newParams.rebuild();
	}

	protected void setColour(PgPolygonSet curve, String s) {
		if(s.equals("None")) {
			curve.showPolygonColors(false);
			return;
		}
		curve.showPolygonColors(false);
//		for(int i=0;i<curve.getNumPolygons();++i)
//		{
		if(s.equals("Red")) curve.setGlobalPolygonColor(Color.red);
		if(s.equals("Green")) curve.setGlobalPolygonColor(Color.green);
		if(s.equals("Blue")) curve.setGlobalPolygonColor(Color.blue);
		if(s.equals("Cyan")) curve.setGlobalPolygonColor(Color.cyan);
		if(s.equals("Magenta")) curve.setGlobalPolygonColor(Color.magenta);
		if(s.equals("Yellow")) curve.setGlobalPolygonColor(Color.yellow);
		if(s.equals("Black")) curve.setGlobalPolygonColor(Color.black);
		if(s.equals("White")) curve.setGlobalPolygonColor(Color.white);
//		}
	}
	
	public LsmpDef getDefinition() {
		return calc.getDefinition();
	}
	
	public abstract List<PgGeometryIf> getOutputGeoms();

	/** Save this projects definition to a file */
	public void save(String filename,boolean append) {
		LsmpDef def = getDefinition();

		try
		{
			FileWriter fw = new FileWriter(filename,append);
			fw.append(def.toString());
			fw.close();
		}
		catch(IOException e)
		{
			showStatus("Failed to write to "+filename);
		}
	}

	/** Called when the displayed equation is changed */
	public void equationChanged(String text) {
	    System.out.println("equationChanged");
		calc.setEquation(text);
		refreshParams();
        store.geomDefinitionChanged(calc);
		calcGeoms();
	}

	/** Called when a displayed parameter is changed */
	public boolean parameterChanged(PuParameter p) {
		System.out.println("parmChanged "+p.getName()+":"+p.getVal());
		calc.setParamValue(p.getName(),p.getVal());
		if(this.autoUpdate)
		    calcGeoms();
		return true;
	}

	public abstract void calcGeoms();

	public Calculator getCalculator() {
		return calc;
	}

    @Override
    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == bLoad) 
            loadDef();
        else if (source == bSave) 
            saveDef();

    }

    protected void saveDef() {
        String txt = calc.getDefinition().toString();
        FileDialog fd = new FileDialog(m_frame,"Save",FileDialog.SAVE);
        fd.setVisible(true);
        String filename = fd.getDirectory() + fd.getFile();
        File f = new File(fd.getDirectory(), fd.getFile());
        
        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(                    
                  new FileOutputStream(f.getPath()), "utf-8"));
            writer.write(txt);
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
           try {
               writer.close();
           }  catch (Exception ex) {/*ignore*/}
        }
    }
    abstract public void loadDefinition(LsmpDef newdef);

    protected void loadDef() {
        FileDialog fd = new FileDialog(m_frame,"Load",FileDialog.LOAD);
        fd.setVisible(true);
        String filename = fd.getDirectory() + fd.getFile();
        File f = new File(fd.getDirectory(), fd.getFile());

        try {
            LsmpDefReader reader = new LsmpDefReader(filename);
            reader.read();
            LsmpDef def = reader.getDefs().get(0);
            System.out.println(def);
            loadDefinition(def);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    protected void loadDefOld() {
//        String txt = calc.getDefinition().toString();
        FileDialog fd = new FileDialog(m_frame,"Save",FileDialog.SAVE);
        fd.setVisible(true);
        String filename = fd.getDirectory() + fd.getFile();
        File f = new File(fd.getDirectory(), fd.getFile());
        
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(                    
                  new FileInputStream(f.getPath()), "utf-8"));
            String line = null;
            String text = "";
            while ((line = reader.readLine()) != null) {
                text += line;
            }
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
           try {
               reader.close();
           }  catch (Exception ex) {/*ignore*/}
        }
        //calc.
    }
    
    public void setFrame(Frame frame) {
        this.m_frame = frame;
    }

    public abstract LsmpDef createDefaultDef(); 
	
} // end of class

