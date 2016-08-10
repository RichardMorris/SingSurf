/* @author rich
 * Created on 20-Jun-2003
 *
 * This code is covered by a Creative Commons
 * Attribution, Non Commercial, Share Alike license
 * <a href="http://creativecommons.org/licenses/by-nc-sa/1.0">License</a>
 */
package org.singsurf.singsurf;
import java.awt.Button;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.ItemSelectable;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import jv.object.PsViewerIf;
import jv.project.PgGeometryIf;
import jv.project.PjProject;
import jv.project.PjProject_IP;
import jv.project.PvCameraIf;
import jv.viewer.PvViewer;

import org.singsurf.singsurf.clients.AbstractClient;
import org.singsurf.singsurf.clients.AbstractOperatorClient;
import org.singsurf.singsurf.clients.SSHelp;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.LsmpDef;
import org.singsurf.singsurf.definitions.LsmpDefReader;
import org.singsurf.singsurf.definitions.LsmpDefReader.ProjectInput;
import org.singsurf.singsurf.definitions.LsmpDefReader.SceneGraph;
import org.singsurf.singsurf.geometries.GeomPair;
import org.singsurf.singsurf.geometries.GeomStore;

/**
 * Advanced version which allows multiple projects at same time.
 * @author Rich Morris
 * Created on 20-Jun-2003
 */
public class SingSurfPro extends PaSingSurf  implements ItemListener, ActionListener, MouseListener
{
	private static final long serialVersionUID = -7242387172848278637L;

	class Generator {
		java.util.List<LsmpDef> defs= null;
		String title, prefix;
		Class<?> clientClass,ipClass;
		DefType type;

		Generator(String title,String prefix,String defFile,String className,DefType type) throws IOException, ClassNotFoundException {
			defs = store.loadDefs(defFile);
			this.title = title;
			this.prefix = prefix;
			this.type = type;
			clientClass = Class.forName("org.singsurf.singsurf.clients." + className);
			ipClass = Class.forName("org.singsurf.singsurf.clients." + className+"_IP");
		}
		
		public AbstractClient newInstance(LsmpDef def) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
			Constructor<?> cons;
			AbstractClient newsurf=null;
			cons = clientClass.getConstructor(new Class[]{GeomStore.class,LsmpDef.class});
			newsurf = (AbstractClient) cons.newInstance(new Object[]{store,def});
 			newsurf.setFrame(SingSurfPro.this.m_frame);
			return newsurf;
		}
		
		public PjProject_IP newIpInstance() throws InstantiationException, IllegalAccessException {
			PjProject_IP res=null;
			res = (PjProject_IP) ipClass.newInstance();
			return res;
		}
	}
	java.util.List<Generator> generators = new ArrayList<Generator>();

	/** A list of all active projects **/
	protected List chProj = null;

	/** whether we auto fit **/
	protected boolean autoFit;
		
	PopupMenu rightClickPopup;

	public SingSurfPro() {
		super();
	}
	/** Project with help text */
	PjProject ssHelp;
	@Override
    public void init() 
	{
		super.init();
		if(PRINT_DEBUG) System.out.println("SSP init");
//		autoFit = m_viewer.getParameter("AutoFit").equals("true");
		autoFit = false;
		store.setDoFitDisplay(autoFit);
		
		/** The location the sing surf panels is displayed in. */
		String SSPanelPos = m_viewer.getParameter("SSPanel");

		chProj = new List();
		chProj.add("Display");
		chProj.addItemListener(this);
		chProj.addMouseListener(this);
		
		if(SSPanelPos.equals("Control"))
		{
			add("West",chProj);
		}
		else
		{
			pProject = m_viewer.getPanel(PsViewerIf.PROJECT);
			add(SSPanelPos,pProject);
		}
		//add("South",laStatus);
		try {
			loadGenerators();
		} catch (IOException e) {
            System.out.println(e.toString());
		} catch (ClassNotFoundException e) {
            System.out.println(e.toString());
		}
		buildMenus();
		ssHelp = new SSHelp();
		m_viewer.addProject(ssHelp);
		validate();
	}
	
	/** Start viewer, e.g. start animation if requested */
	@Override
    public void start()
	{ 
		if(PRINT_DEBUG) System.out.println("applet: start");
//			System.out.println("Cur Proj"+m_viewer.getCurrentProject());
			String name = chProj.getSelectedItem();
			if(name!=null)
				m_viewer.selectProject(m_viewer.getProject(name));
			else
				m_viewer.selectProject(ssHelp);
			m_viewer.start(); 
//			System.out.println("Viewer Started");
	}

	/**
	 * Switch to the project selected in chProj. 
	 */
	public void switchProject() {
		String name = chProj.getSelectedItem();
		if(name.equals("                                      ")) return;
		if(name.equals("Display")) {
			m_viewer.showPanel(PsViewerIf.DISPLAY);
			return;
		}
		m_viewer.showPanel(PvViewer.PROJECT);
		PjProject proj = m_viewer.getProject(name);
		if(proj==null) {
			this.showStatus("Invalid project "+name);
			return;
		}
		proj.setEnabledAutoFit(fit.getState());
		m_viewer.selectProject(m_viewer.getProject(name));
		PgGeometryIf geom = m_viewer.getProject(name).getGeometry();
		if(geom!=null)	m_viewer.getDisplay().selectGeometry(geom);
	}

	public void itemStateChanged(ItemEvent e) {
		ItemSelectable itSel = e.getItemSelectable();
		if(itSel == chProj)
		{
			switchProject();
			return;
		}
	}

	void addProject(Generator gen, LsmpDef def) throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
//		if(def==null) {
//		    def = new LsmpDef(gen.title,gen.type,"");
//		}
	    AbstractClient newsurf=null;
	    try {
	        newsurf = gen.newInstance(def);
	    } catch (Throwable e) {
//            e.printStackTrace();
            StackTraceElement[] trace = e.getStackTrace();
            System.err.println(e);
            for(StackTraceElement ele:trace) {
                System.err.println("\tat "+ele.toString());
                if(ele.getClassName().equals(this.getClass().getName())) break;
            }
            return;
        }
		if(m_viewer.hasProject(newsurf.getName()))
		{
			String name=null;
			for(int i=1;;++i)
				if(!m_viewer.hasProject(newsurf.getName()+i))
				{
					name = newsurf.getName()+i;
					break;
				}
			newsurf.setName(name);
		}
		newsurf.setEnabledAutoFit(fit.getState());
		m_viewer.addProject(newsurf);
		newsurf.init2();
		m_viewer.showPanel(PsViewerIf.PROJECT);
		m_viewer.selectProject(newsurf);
		chProj.add(newsurf.getName());
		chProj.select(chProj.getItemCount()-1);
		if(newsurf.getGeometry() != null)
			m_viewer.getDisplay().selectGeometry(newsurf.getGeometry());
		boolean manageIP = m_viewer.getParameter("Control").equalsIgnoreCase("Hide");
		if(manageIP){
			PjProject_IP my_IP=null;
            try {
                my_IP = gen.newIpInstance();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
			my_IP.setParent(newsurf);
			pProject.removeAll();
			pProject.add(my_IP);
			validate();
		}
	}

	private void changeProjectName(String oldName,String newName)
	{
		for(int i=0;i<chProj.getItemCount();++i)
			if(chProj.getItem(i).equals(oldName))
			{
				chProj.replaceItem(newName,i);
			}
		AbstractClient proj = (AbstractClient) m_viewer.getProject(oldName);
		m_viewer.removeProject(proj);
		proj.setName(newName);
		m_viewer.addProject(proj);
		m_viewer.selectProject(proj);
	}

	void loadGenerators() throws IOException, ClassNotFoundException
	{
		generators.add(new Generator("Algebraic Curves","Acurve","defs/acurve.defs","ACurve",DefType.acurve));
		generators.add(new Generator("Algebraic Surfaces","Asurf","defs/asurf.defs","ASurf",DefType.asurf));
		generators.add(new Generator("Parameterized Surfaces","Psurf","defs/psurf.defs","Psurf",DefType.psurf));
		generators.add(new Generator("Parameterized Curves","Pcurve","defs/pcurve.defs","Pcurve",DefType.pcurve));
        generators.add(new Generator("Intersections","Intersection","defs/intersect.defs",
                "Intersection",DefType.intersect));
		generators.add(new Generator("Mappings","Mapping","defs/mapping.defs","Mapping",DefType.mapping));
        generators.add(new Generator("Clip","Clip","defs/clip.defs",
                "Clip",DefType.clip));
		generators.add(new Generator("Generalized Intersections","Generalized Intersection","defs/genint.defs",
				"GeneralizedIntersection",DefType.genInt));
        generators.add(new Generator("Generalized Mapping","Generalized Mapping","defs/genmap.defs","GeneralizedMapping",DefType.genMap));
        generators.add(new Generator("Bi Intersections","BiInt","defs/biint.defs",
                "BiIntersection",DefType.biInt));
        generators.add(new Generator("Bi Mapping","BiMap","defs/bimap.defs",
                "BiMap",DefType.biMap));
		generators.add(new Generator("Colourize","Colourize","defs/colour.defs","Colourize",DefType.colour));
	}
	
	public LsmpDef getDef(java.util.List<LsmpDef> defs,String name)
	{
		for(LsmpDef def:defs)
			if(def.getName().equals(name))
				return def;
		return null;
	}

	Menu buildSubMenu(Generator gen)
	{
		Menu m = new Menu(gen.title);

		//m.add(gen.title);
        MenuItem it = new MenuItem(gen.title);
        it.addActionListener(this);
        it.setActionCommand(gen.prefix+':');
        m.add(it);
		m.addSeparator();
		for(LsmpDef def : gen.defs)
		{
			it = new MenuItem(def.getName());
			it.addActionListener(this);
			it.setActionCommand(gen.prefix+':'+def.getName());
			m.add(it);
		}
		return m;
	}
	CheckboxMenuItem fit,twoDview;
	Choice appletNewProj = new Choice();
    Choice appletSelProj = new Choice();
        
	void buildAppletMenu() {
//	    Panel p = new Panel();
	    for(Generator gen:generators) {
	        appletNewProj.add(gen.title);
               }

	}
	
	void buildMenus()
	{
//	           if(m_frame==null) {
//	               buildAppletMenu();
//	               return;
//	           }

		MenuBar mb = new MenuBar();
		Menu newProj = new Menu("New");
		mb.add(newProj);
		for(Generator gen:generators) {
			Menu m = buildSubMenu(gen);
		newProj.add(m);
		}
		
		Menu load = new Menu("Load");
		mb.add(load);
		MenuItem it = new MenuItem("Load");
		it.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				loadProject();
			}
		});
		load.add(it);
		
		Menu options = new Menu("Options");
		mb.add(options);
		fit = new CheckboxMenuItem("AutoFit",false);
		fit.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				store.setDoFitDisplay(fit.getState());
	//			for(PvDisplayIf disp : m_viewer.getDisplays())
	//			{
	//				disp.setEnabledFillDisplay(fit.getState());
	//				disp.setAutoCenter(fit.getState()); 
	//			}
	//			for(PvProjectIf proj : m_viewer.getP)
			}});
		options.add(fit);
		twoDview = new CheckboxMenuItem("2D view",false);
		twoDview.addItemListener(new ItemListener(){

			public void itemStateChanged(ItemEvent arg0) {
				if(twoDview.getState()) {
					m_viewer.getDisplay().getCamera().setProjectionMode(PvCameraIf.CAMERA_ORTHO_XY );
					m_viewer.getDisplay().setEnabled3DLook(false);
				}
				else {
					m_viewer.getDisplay().getCamera().setProjectionMode(PvCameraIf.CAMERA_PERSPECTIVE );
					m_viewer.getDisplay().setEnabled3DLook(true);
				}
			}});
		options.add(twoDview);
		
		if(m_frame!=null)
			m_frame.setMenuBar(mb);
		else
		{
		    final Button b = new Button("but");
		    final PopupMenu pum = new PopupMenu();
		    pum.add(it);
		    b.add(pum);
                    this.add("North",b);
		    b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    pum.show(b, 0, 0);
                }});
		}
		Menu help = new Menu("Help");
		help.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("help event");
				m_viewer.selectProject(ssHelp);
			}
		});
		mb.setHelpMenu(help);
		
		rightClickPopup = new PopupMenu();
		rightClickPopup.add("Project Name");
		rightClickPopup.addSeparator();
		
//		CheckboxMenuItem cbit = new CheckboxMenuItem("Show vertices");
//		cbit.addItemListener(new ItemListener(){
//			@Override
//			public void itemStateChanged(ItemEvent arg0) {
//				
//				
//			}});
//		rightClickPopup.add(cbit);
		
		
//		rightClickPopup.addSeparator();

		it = new MenuItem("Clone");
		it.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				cloneProject();
			}});
		rightClickPopup.add(it);
		
		it = new MenuItem("Save");
		it.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				saveProject(false);
			}});
		rightClickPopup.add(it);
		it = new MenuItem("Append");
		it.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				saveProject(true);
			}});
		rightClickPopup.add(it);
		it = new MenuItem("Save whole scene");
		it.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				saveScene();
			}});
		rightClickPopup.add(it);
		it = new MenuItem("Delete (keep geometries)");
		it.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				deleteProject(false,false);
			}});
		rightClickPopup.add(it);
		it = new MenuItem("Delete (remove geometries)");
		it.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				deleteProject(true,false);
			}});
		rightClickPopup.add(it);
		it = new MenuItem("Delete (remove geometries and dependents)");
		it.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				deleteProject(true,true);
			}});
		rightClickPopup.add(it);
		it = new MenuItem("Change name");
		it.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				changeProjectName();
			}});
		rightClickPopup.add(it);
		add(rightClickPopup);
		//rightClickPopup.addActionListener(this);
	}
	/**
	 * Standalone application support. The main() method acts as the applet's
	 * entry point when it is run as a standalone application. It is ignored
	 * if the applet is run from within an HTML page.
	 */

	public static void main(String args[]) {
		SingSurfPro va	= new SingSurfPro();
		commonMain(va,args);
	}

	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();
		StringTokenizer st = new StringTokenizer(command,":");
		String prefix = st.nextToken();
		String suffix=null;
		if(st.countTokens()>0)
			suffix = st.nextToken();
		
		for(Generator gen:generators) {
			if(prefix.equals(gen.prefix)) {
				try {
				    if(suffix == null) {
                        addProject(gen,null);
				    } else {
					for(LsmpDef def:gen.defs) {
						if(def.getName().equals(suffix))
							addProject(gen,def);
					}}
				    
				} catch (Exception e) {
				    System.out.println(e.toString());
				}
			}
		}
	}

	void deleteProject(boolean rmGeom,boolean rmDep) {
		String name = chProj.getSelectedItem();
		if(name==null || name.equals("Display")) return;
		AbstractClient proj = (AbstractClient) m_viewer.getProject(name);
		if(rmGeom)
			for(PgGeometryIf geom:proj.getOutputGeoms())
			{
				store.removeGeometry(geom,rmDep);
				//m_viewer.getDisplay().removeGeometry(geom);
			}
		m_viewer.removeProject(proj);
		proj.dispose();
		chProj.remove(name);
	}

	void loadProject() {
		FileDialog fd = new FileDialog(m_frame, "Load definition", FileDialog.LOAD);
		fd.setVisible(true);
		String imagefilename = fd.getDirectory() + fd.getFile();
		System.out.println("Load from "+imagefilename);
		try
		{
			FileReader fr = new FileReader(imagefilename);
			LsmpDefReader ldr = new LsmpDefReader(new BufferedReader(fr));
			ldr.read();
			fr.close();
			for(LsmpDef def : ldr.getDefs())
				createProject(def);
			SceneGraph sg = ldr.getSceneGraph();
			Map<ProjectInput,Boolean> doneMap = new HashMap<ProjectInput,Boolean>();
			for(ProjectInput pi:sg.getInputs())
				doneMap.put(pi,Boolean.FALSE);
			boolean flag=true;
			while(flag)
			{
				flag = false;
				for(ProjectInput pi:sg.getInputs())
				{
					System.out.println("ProjIn "+pi.getProject()+" "+pi.getGeometry());
					if(!doneMap.get(pi)) {
						System.out.println("Not done");
					if(m_viewer.hasProject(pi.getProject()))
					{
						System.out.println("Proj exists");
						PgGeometryIf geom = store.getGeom(pi.getGeometry());
						if(geom!=null)
						{
							System.out.println("Geom exists");
							AbstractOperatorClient proj =(AbstractOperatorClient) m_viewer.getProject(pi.getProject());
							proj.newActiveInput(geom.getName());
							flag = true;
							doneMap.put(pi,Boolean.TRUE);
						}
					}
					}
				}
			}
		}
		catch(IOException e)
		{
			System.out.println("Failed to write to "+imagefilename);
		}
		
	}
	void saveProject(boolean append) {
		String name = chProj.getSelectedItem();
		if(name.equals("Display")) return;
		FileDialog fd = new FileDialog(m_frame, "Save definition for "+name, FileDialog.SAVE);
		fd.setVisible(true);
		String filename = fd.getDirectory() + fd.getFile();
		System.out.println("Save to "+filename);
		AbstractClient proj = (AbstractClient) m_viewer.getProject(name);
		LsmpDef def = proj.getDefinition();

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

	void saveScene() {
		FileDialog fd = new FileDialog(m_frame, "Save scene", FileDialog.SAVE);
		fd.setVisible(true);
		String filename = fd.getDirectory() + fd.getFile();
		System.out.println("Save to "+filename);
		try
		{
			FileWriter fw = new FileWriter(filename);
			fw.write("<SingSurfScene>\n");
			fw.write("<"+"definitions"+">\n");
			for(String name:chProj.getItems())
			{
				if(name.equals("Display")) continue;
				AbstractClient proj = (AbstractClient) m_viewer.getProject(name);
				fw.write(proj.getDefinition().toString());
			}
			fw.write("</definitions>\n");
			fw.write("<calculateGeometries>\n");
			for(String name:chProj.getItems())
			{
				if(name.equals("Display")) continue;
				AbstractClient proj = (AbstractClient) m_viewer.getProject(name);
				if(proj instanceof AbstractOperatorClient)
				{
					for(GeomPair p:((AbstractOperatorClient)proj).getInputOutputPairs())
					{
						fw.write("\t<calculatedGeometry project=\""+proj.getName()+"\"" +
										" geometry=\""+p.getOutput().getName()+"\"" +
										" input=\""+p.getInput().getName()+"\"" +
										"\"/>\n");
						fw.write(LmsPointSetMaterial.getMaterial(p.getOutput()).toString());
						fw.write("\t</calculatedGeometry>\n");	
					}

				}
				else
				{
					for(PgGeometryIf geom:proj.getOutputGeoms())
					{
						fw.write("\t<calculatedGeometry project=\""+proj.getName()
								+"\" geometry=\""+geom.getName()+"\">\n");
						fw.write(LmsPointSetMaterial.getMaterial(geom).toString());
						fw.write("\t</calculatedGeometry>\n");	

					}
				}
			}
			
			fw.write("</calculateGeometries>\n");
			fw.write("</SingSurfScene>\n");
			fw.close();
		}
		catch(IOException e)
		{
			showStatus("Failed to write to "+filename);
		}
	}

	class ChangeNameDialog extends Dialog implements ActionListener
	{
        private static final long serialVersionUID = 1L;
        TextField tf;
		boolean state=false;
		Button b1,b2;
		
		public ChangeNameDialog(Frame parent,String name)
		{
			super(parent,"Change name for project "+name,true);
			Panel p=new Panel();
			add(p);
			tf = new TextField(name,20);
			p.add(tf);
			Button b1=new Button("OK");
			Button b2=new Button("Cancel");
			p.add(b1);
			p.add(b2);
			b1.addActionListener(this);
			b2.addActionListener(this);
			tf.addActionListener(this);
			this.addWindowListener(new WindowAdapter(){
				@Override
                public void windowClosing(WindowEvent e) {
					//ChangeNameDialog.this.setVisible(false);
					dispose();
				}
			});
			pack();
		}
		public void actionPerformed(ActionEvent arg0) {
			//System.out.println("ActCom "+arg0.getActionCommand());
			if(arg0.getActionCommand().equals("Cancel"))
				state=false;
			else
				state=true;
			//this.setVisible(false);
			dispose();
		}
		
	}
	void changeProjectName()
	{
		String name = chProj.getSelectedItem();
		if(name.equals("Display")) return;
		//AbstractClient proj = (AbstractClient) m_viewer.getProject(name);
		ChangeNameDialog d = new ChangeNameDialog(this.m_frame,name);
		d.setVisible(true);
		if(d.state)
		{
			this.changeProjectName(name,d.tf.getText());
		}
	}

	private void createProject(LsmpDef def)
	{
		for(Generator gen:generators) {
			if(def.getType() == gen.type) {
				try {
					addProject(gen,def);
				} catch (Exception e) {
				     System.out.println(e.toString());
				}
			}
		}
	}
	void cloneProject() {
		String name = chProj.getSelectedItem();
		if(name.equals("Display")) return;

		AbstractClient proj = (AbstractClient) m_viewer.getProject(name);
		LsmpDef def = proj.getDefinition();
		System.out.println("Clone: "+def.toString());
		createProject(def);
	}


	public void mousePressed(MouseEvent event) {
		int modifiers = event.getModifiers();
		if((modifiers & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) 
		{
//			System.out.println("Right pressed");
			rightClickPopup.remove(0);
			rightClickPopup.insert(chProj.getSelectedItem(),0);
			rightClickPopup.show(event.getComponent(), event.getX(), event.getY());
		}
//		else
//			System.out.println("Pressed "+modifiers);
	}

	public void mouseClicked(MouseEvent arg0) {	}
	public void mouseReleased(MouseEvent arg0) { }
	public void mouseEntered(MouseEvent arg0) {	}
	public void mouseExited(MouseEvent arg0) { }
}
