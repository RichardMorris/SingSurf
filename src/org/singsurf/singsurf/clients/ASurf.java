/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;


import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingUtilities;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.object.PsDebug;
import jv.project.PgGeometryIf;

import org.nfunk.jep.ASTConstant;
import org.nfunk.jep.ASTVarNode;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.LmsElementSetMaterial;
import org.singsurf.singsurf.LmsPointSetMaterial;
import org.singsurf.singsurf.LmsPolygonSetMaterial;
import org.singsurf.singsurf.PolynomialCalculator;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.PuVariable;
import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.acurve.EquationConverter;
import org.singsurf.singsurf.asurf.BoxClevA;
import org.singsurf.singsurf.asurf.Boxclev;
import org.singsurf.singsurf.asurf.Region_info;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.LsmpDef;
import org.singsurf.singsurf.definitions.Option;
import org.singsurf.singsurf.geometries.GeomStore;

/**
 * @author Rich Morris
 * Created on 30-Mar-2005
 */
public class ASurf extends AbstractClient {
    private static final long serialVersionUID = 1L;

    /** The name for the program */
    protected static final String programName = "Asurf";

    /** file name for definitions */
    protected String	my_defFileName = "defs/asurf.defs";

    /** default definition to use */
    private final String my_defaultDefName = "A1 (cone)"; //"A Sphere";

    LsmpDef def;
    PuVariable displayVars[];

    protected	CheckboxGroup	cbg_coarse;	// Coarse checkbox group
    protected	CheckboxGroup	cbg_fine;	// Coarse checkbox group
    protected	CheckboxGroup	cbg_face;	// Coarse checkbox group
    protected	CheckboxGroup	cbg_edge;	// Coarse checkbox group
    protected	CheckboxGroup	cbg_timeout;	// Timeout checkbox group

    protected	Checkbox	cb_c_4,  cb_c_8,  cb_c_16,  cb_c_32,  cb_c_64, cb_c_128, cb_c_256, cb_c_512;
    protected	Checkbox	cb_fi_8,  cb_fi_16,  cb_fi_32,  cb_fi_64,
    cb_fi_128,  cb_fi_256,  cb_fi_512,  cb_fi_1024; 
    protected	Checkbox	cb_fa_64,  cb_fa_128, cb_fa_256,  cb_fa_512,  cb_fa_1024,  cb_fa_2048; 
    protected	Checkbox	cb_e_128,  cb_e_256,  cb_e_512,  cb_e_1024,  cb_e_2048,cb_e_4096,cb_e_8192; 

    protected	Checkbox	cb_t_1, cb_t_10, cb_t_100,cb_t_1000;

    //int globalSteps = 60;
    DefVariable localX,localY,localZ;

    protected Checkbox cb_autoUpdate;

    protected PgElementSet outSurf;
    protected PgPolygonSet outCurve;
    protected PgPointSet outPoints;
    //java.util.List<LsmpDef> defs= null;

    protected boolean singleGeom = true;
    protected boolean advancedOptions = false;

    //Plotter2D plotter;

    public ASurf(GeomStore store,String name,String defFile) {
        super(store, name);
        try {
            java.util.List<LsmpDef> defs = store.loadDefs(defFile);
            this.lsmpDefs = new LsmpDef[defs.size()];
            this.lsmpDefs = defs.toArray(this.lsmpDefs);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (getClass() == ASurf.class)
        {
            init(getDef(getDefaultDefName()),true);
            //init(null,true);
        }
    }

    public ASurf(GeomStore store,LsmpDef def) {
        super(store, def==null ? "ASurf" :def.getName());
        if (getClass() == ASurf.class)
        {            
            if(def == null) 
                def = createDefaultDef();
            init(def,false);
        }
    }

    
    public ASurf(GeomStore store,String name,String defFile,LsmpDef initialDef) {
        super(store, name);
        try {
            java.util.List<LsmpDef> defs = store.loadDefs(defFile);
            this.lsmpDefs = new LsmpDef[defs.size()];
            this.lsmpDefs = defs.toArray(this.lsmpDefs);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (getClass() == ASurf.class)
        {
            init(initialDef,true);
            //init(null,true);
        }
    }

    public ASurf(GeomStore store,String name,String defFile,String model) {
        super(store, model);
        try {
            java.util.List<LsmpDef> defs = store.loadDefs(defFile);
            this.lsmpDefs = new LsmpDef[defs.size()];
            this.lsmpDefs = defs.toArray(this.lsmpDefs);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (getClass() == ASurf.class)
        {
            LsmpDef initialDef = getDef(model);
            
			init(initialDef,true);
            //init(null,true);
        }
    }

    public LsmpDef createDefaultDef() {
        LsmpDef def;
        def = new LsmpDef("ASurf",DefType.asurf,"");
        def.add(new DefVariable("x", -1.03, 1.02));
        def.add(new DefVariable("y", -1.04, 1.01));
        def.add(new DefVariable("z", -1.05, 1));
        return def;
    }

    public void init(LsmpDef def,boolean flag) {
        super.init(flag);
        localX = new DefVariable("x","Normal");
        localY = new DefVariable("y","Normal");
        localZ = new DefVariable("z","Normal");
        displayVars = new PuVariable[]{
                new PuVariable(this,localX),
                new PuVariable(this,localY),
                new PuVariable(this,localZ)};
        newParams = new LParamList(this);
        cbg_coarse = new CheckboxGroup();
        cbg_fine = new CheckboxGroup();
        cbg_face = new CheckboxGroup();
        cbg_edge = new CheckboxGroup();

        cb_c_4 = new Checkbox("4",cbg_coarse,false);
        cb_c_8  = new Checkbox("8",cbg_coarse,false);
        cb_c_16  = new Checkbox("16",cbg_coarse,false);
        cb_c_32  = new Checkbox("32",cbg_coarse,true);
        cb_c_64  = new Checkbox("64",cbg_coarse,false);
        cb_c_128  = new Checkbox("128",cbg_coarse,false);
        cb_c_256  = new Checkbox("256",cbg_coarse,false);
        cb_c_512  = new Checkbox("512",cbg_coarse,false);
        cb_c_4.addItemListener(this);

        cb_fi_8 = new Checkbox("8",cbg_fine,false);
        cb_fi_16 = new Checkbox("16",cbg_fine,false);
        cb_fi_32 = new Checkbox("32",cbg_fine,false);
        cb_fi_64 = new Checkbox("64",cbg_fine,true);
        cb_fi_128 = new Checkbox("128",cbg_fine,false);
        cb_fi_256 = new Checkbox("256",cbg_fine,false);
        cb_fi_512 = new Checkbox("512",cbg_fine,false);
        cb_fi_1024 = new Checkbox("1024",cbg_fine,false); 

        cb_fa_64 = new Checkbox("64",cbg_face,false);
        cb_fa_128 = new Checkbox("128",cbg_face,true);
        cb_fa_256 = new Checkbox("256",cbg_face,false);
        cb_fa_512 = new Checkbox("512",cbg_face,false);
        cb_fa_1024 = new Checkbox("1024",cbg_face,false); 
        cb_fa_2048 = new Checkbox("2048",cbg_face,false); 

        cb_e_128 = new Checkbox("128",cbg_edge,false);
        cb_e_256 = new Checkbox("256",cbg_edge,false);
        cb_e_512 = new Checkbox("512",cbg_edge,false);
        cb_e_1024 = new Checkbox("1024",cbg_edge,false); 
        cb_e_2048 = new Checkbox("2048",cbg_edge,false); 
        cb_e_4096 = new Checkbox("4096",cbg_edge,true); 
        cb_e_8192 = new Checkbox("8192",cbg_edge,false); 

        cb_t_1 = new Checkbox("1",cbg_timeout,false); 
        cb_t_10 = new Checkbox("10",cbg_timeout,true); 
        cb_t_100 = new Checkbox("100",cbg_timeout,false); 
        cb_t_1000 = new Checkbox("1000",cbg_timeout,false); 
        //		cb_degen_lines = new Checkbox("Draw degenerate lines",false); 
        this.cb_autoUpdate = new Checkbox("Automatically re-calculate",true); 
        this.cb_autoUpdate.addItemListener(new ItemListener(){

            public void itemStateChanged(ItemEvent arg0) {
                autoUpdate = cb_autoUpdate.getState();
            }});

        cbShowVert.setState(false);
        
        


        //		plotter = new Plotter2D(Integer.parseInt(cbg_coarse.getSelectedCheckbox().getLabel()),
        //				Integer.parseInt(cbg_fine.getSelectedCheckbox().getLabel()),4096);
        loadDefinition(def);
    }

	@Override
    public void init2()
	{
		super.init2();
	}

    public void setCoarse(int c){
        if(c==4)
            cbg_coarse.setSelectedCheckbox(cb_c_4);
        if(c==8)
            cbg_coarse.setSelectedCheckbox(cb_c_8);
        if(c==16)
            cbg_coarse.setSelectedCheckbox(cb_c_16);
        if(c==32)
            cbg_coarse.setSelectedCheckbox(cb_c_32);
        if(c==64)
            cbg_coarse.setSelectedCheckbox(cb_c_64);
        if(c==128)
            cbg_coarse.setSelectedCheckbox(cb_c_128);
        if(c==256)
            cbg_coarse.setSelectedCheckbox(cb_c_256);
        if(c==512)
            cbg_coarse.setSelectedCheckbox(cb_c_512);
        def.setOption("coarse", c);
    }

    public int getCoarse() {
        //		return def.getOpt("coarse").getIntegerVal();
        return Integer.parseInt(cbg_coarse.getSelectedCheckbox().getLabel());
    }
    public void setFine(int f){
        if(f==8)
            cbg_fine.setSelectedCheckbox(cb_fi_8);
        if(f==16)
            cbg_fine.setSelectedCheckbox(cb_fi_16);
        if(f==32)
            cbg_fine.setSelectedCheckbox(cb_fi_32);
        if(f==64)
            cbg_fine.setSelectedCheckbox(cb_fi_64);
        if(f==128)
            cbg_fine.setSelectedCheckbox(cb_fi_128);
        if(f==256)
            cbg_fine.setSelectedCheckbox(cb_fi_256);
        if(f==512)
            cbg_fine.setSelectedCheckbox(cb_fi_512);
        if(f==1024)
            cbg_fine.setSelectedCheckbox(cb_fi_1024);
        def.setOption("fine", f);
    }
    public int getFine() {
        //		return def.getOpt("fine").getIntegerVal();
        return Integer.parseInt(cbg_fine.getSelectedCheckbox().getLabel());
    }
    public void setFace(int f){
        if(f==64)
            cbg_face.setSelectedCheckbox(cb_fa_64);
        if(f==128)
            cbg_face.setSelectedCheckbox(cb_fa_128);
        if(f==256)
            cbg_face.setSelectedCheckbox(cb_fa_256);
        if(f==512)
            cbg_face.setSelectedCheckbox(cb_fa_512);
        if(f==1024)
            cbg_face.setSelectedCheckbox(cb_fa_1024);
        if(f==2048)
            cbg_face.setSelectedCheckbox(cb_fa_2048);
        def.setOption("face", f);
    }
    public int getFace() {
        //		return def.getOpt("face").getIntegerVal();
        return Integer.parseInt(cbg_face.getSelectedCheckbox().getLabel());
    }

    public void setEdge(int f){
        if(f==128)
            cbg_edge.setSelectedCheckbox(cb_e_128);
        if(f==256)
            cbg_edge.setSelectedCheckbox(cb_e_256);
        if(f==512)
            cbg_edge.setSelectedCheckbox(cb_e_512);
        if(f==1024)
            cbg_edge.setSelectedCheckbox(cb_e_1024);
        if(f==2048)
            cbg_edge.setSelectedCheckbox(cb_e_2048);
        if(f==4096)
            cbg_edge.setSelectedCheckbox(cb_e_4096);
        if(f==8192)
            cbg_edge.setSelectedCheckbox(cb_e_8192);
        def.setOption("edge", f);
    }

    public int getEdge() {
        //return def.getOpt("edge").getIntegerVal();
        return Integer.parseInt(cbg_edge.getSelectedCheckbox().getLabel());
    }

    void checkDef(LsmpDef def)
    {
        //DefVariable var =def.getVar(0); 
        //if(var.getSteps()==-1) var.setBounds(var.getMin(),var.getMax(),globalSteps);
    }
    public void loadDefinition(LsmpDef newdef)
    {
        //System.out.println(Thread.currentThread()); 
        System.out.println("Load def "+newdef.getName()+" this" + this.getName()); 
        def = newdef.duplicate(); 
        checkDef(def);
        //def.setName(this.getName());
        this.getInfoPanel().setTitle(def.getName());
        calc = new PolynomialCalculator(def,0);
        calc.build();
        if(!calc.isGood()) showStatus(calc.getMsg());
        localX = calc.getDefVariable(0);
        localY = calc.getDefVariable(1);
        localZ = calc.getDefVariable(2);
        setDisplayEquation(def.getEquation());
        displayVars[0].set(localX);
        displayVars[1].set(localY);
        displayVars[2].set(localZ);
        refreshParams();
        Option copt = def.getOption("coarse");
        if(copt!=null) setCoarse(copt.getIntegerVal());
        Option fopt = def.getOption("fine");
        if(fopt!=null) setFine(fopt.getIntegerVal());
        Option faopt = def.getOption("face");
        if(faopt!=null) setFace(faopt.getIntegerVal());
        Option eopt = def.getOption("edge");
        if(eopt!=null) setEdge(eopt.getIntegerVal());

        if(this.singleGeom) {
            if(outSurf!=null)
                store.removeGeometry(outSurf,false);
            if(outCurve!=null)
                store.removeGeometry(outCurve,false);
            if(outPoints!=null)
                store.removeGeometry(outPoints,false);
        }
        this.m_name = newdef.getName();
        store.showStatus("Loaded "+m_name);
        this.getInfoPanel().repaint();
        outSurf=store.aquireSurface(newdef.getName(),this);
//        outCurve=store.aquireCurve(newdef.getName()+" lines",this);
//        outPoints=store.aquirePoints(newdef.getName()+" points",this);
        calcGeoms();
    }

    public void variableRangeChanged(int n,PuVariable v)
    {
        calc.setVarBounds(n,v.getMin(),v.getMax(),v.getSteps());
        if(n==0) localX.setBounds(v.getMin(),v.getMax(),v.getSteps());
        if(n==1) localY.setBounds(v.getMin(),v.getMax(),v.getSteps());
        if(n==2) localZ.setBounds(v.getMin(),v.getMax(),v.getSteps());
        if(autoUpdate) {
            calcGeoms();
        }
    }


    @Override
    public void calcGeoms() {
        System.out.println("CalcGeoms");
        if(!calc.isGood())
        {
            showStatus(calc.getMsg());
            return;
        }
        if(outSurf != null) face_mat = new LmsElementSetMaterial(outSurf);
        if(outCurve != null) line_mat = new LmsPolygonSetMaterial(outCurve);
        if(outPoints != null) point_mat = new LmsPointSetMaterial(outPoints);

        Thread t = new Thread(new CalcGeomRunnable());
        t.start();


    }

    String getModelName() {
        return m_name;
    }

    class CalcGeomRunnable implements Runnable {

        public void run() {
            store.showStatus("Calculating geometry \""+getModelName()+"\"");
            PgElementSet surfRes = new PgElementSet(3);
            PgPolygonSet curveRes = new PgPolygonSet(3);
            PgPointSet pointsRes = new PgPointSet(3);

            EquationConverter ec = new EquationConverter(calc.getJep());
            try {
                double[][][] coeffs = ec.convert3D(calc.getRawEqns(), 
                        new String[]{localX.getName(),localY.getName(),localZ.getName()},
                        calc.getParams()); 
                if(coeffs.length==1 && coeffs[0].length==1 && coeffs[0][0].length==1)
                	throw new AsurfException("Equation is a constant");
                int coarse = getCoarse();
                int fine = advancedOptions ? getFine() : coarse * 2;
                int face = advancedOptions ? getFace() : fine * 2;
                int edge = advancedOptions ? getEdge() : face * 2;
                BoxClevA boxclev= new Boxclev(surfRes,curveRes,pointsRes,
                        coarse,fine,face,edge,cbShowCurves.getState());
                boxclev.global_selx = -1;
                boxclev.global_sely = -1;
                boxclev.global_selz = -1;
                
                for(Node n:calc.getRawEqns()) {
                    if(!(n.jjtGetChild(0) instanceof ASTVarNode)) continue;
                    String p = ((ASTVarNode) n.jjtGetChild(0)).getName();
                    System.out.println(p);
                    if(p.equals("selx")) 
                        boxclev.global_selx = ((Number) ((ASTConstant) n.jjtGetChild(1)).getValue()).intValue();
                    if(p.equals("sely")) 
                        boxclev.global_sely = ((Number) ((ASTConstant) n.jjtGetChild(1)).getValue()).intValue();
                    if(p.equals("selz")) 
                        boxclev.global_selz = ((Number) ((ASTConstant) n.jjtGetChild(1)).getValue()).intValue();
                    if(p.equals("seld")) 
                        boxclev.global_denom = ((Number) ((ASTConstant) n.jjtGetChild(1)).getValue()).intValue();
                }
                System.out.printf("Global sel %d %d %d / %d%n", boxclev.global_selx, boxclev.global_sely, boxclev.global_selz, boxclev.global_denom);
                Region_info region = new Region_info(localX.getMin(),localX.getMax(),
                        localY.getMin(),localY.getMax(),
                        localZ.getMin(),localZ.getMax());
                boxclev.marmain(coeffs, region);
                store.showStatus("Geometry \""+getModelName()+"\" sucessfully calculated");

            } catch (AsurfException e) {
                store.showStatus("Error calculating geometry");
                PsDebug.error(e.getMessage());
                e.printStackTrace();
            }
            catch(ParseException e) {
                store.showStatus("Error parsing expression");
                PsDebug.error(e.getMessage());
            }
            catch(Exception e) {
                store.showStatus(e.toString());
                PsDebug.error(e.toString());
                e.printStackTrace();
            }

            DisplayGeomRunnable runnable = new DisplayGeomRunnable(surfRes,curveRes,pointsRes);
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }
    @Override
    public void setDisplayProperties() {
        if(outSurf!=null) {
            setDisplayProperties(outSurf);
            store.geomApperenceChanged(outSurf);
        }
        if(outCurve!=null) {
            setDisplayProperties(outCurve);
            store.geomApperenceChanged(outCurve);
        }
        if(outPoints!=null) {
            setDisplayProperties(outPoints);
            store.geomApperenceChanged(outPoints);
        }
    }


    @Override
    public boolean update(Object o) 
    {
        if(o == displayVars[0])
        {
            variableRangeChanged(0,(PuVariable) o);
            return true;
        }
        if(o == displayVars[1])
        {
            variableRangeChanged(1,(PuVariable) o);
            return true;
        }
        if(o == displayVars[2])
        {
            variableRangeChanged(2,(PuVariable) o);
            return true;
        }
        else if(o instanceof PuParameter) {
            return parameterChanged((PuParameter) o);
        }
        else return super.update(o);
    }

    class DisplayGeomRunnable implements Runnable {
        PgElementSet surfRes;
        PgPolygonSet curveRes;
        PgPointSet pointsRes;

        /**
         * @param surfRes
         * @param curveRes
         * @param pointsRes
         */
        public DisplayGeomRunnable(PgElementSet surfRes,
                PgPolygonSet curveRes, PgPointSet pointsRes) {
            this.surfRes = surfRes;
            this.curveRes = curveRes;
            this.pointsRes = pointsRes;
        }

        public void run() {
            //System.out.println("MyRunnable copy surfs");
//            store.removeAll();

            if(outSurf==null)
                outSurf = store.aquireSurface(getModelName(), null);

            GeomStore.copySrcTgt(surfRes,outSurf);
            //System.out.println("outSurf "+outSurf.getNumElements()+" "+outSurf.getNumVertices());
            GeomStore.copySrcTgt(curveRes,outCurve);
            GeomStore.copySrcTgt(pointsRes,outPoints);

            try {
            if( cbColour.getState() )
            {
//                if(outSurf.hasVertexColors())
//                    outSurf.makeElementFromVertexColors();
//                else
                    outSurf.makeElementColorsFromXYZ();
                outSurf.showElementColors(true);
                outSurf.showVertexColors(true);
            }
            else {
                String s = chColours.getSelectedItem();
                Color c=null;
                if(s.equals("Red")) c = Color.red;
                if(s.equals("Green")) c = Color.green;
                if(s.equals("Blue")) c = Color.blue;
                if(s.equals("Cyan")) c = Color.cyan;
                if(s.equals("Magenta")) c = Color.magenta;
                if(s.equals("Yellow")) c = Color.yellow;
                if(s.equals("Black")) c = Color.black;
                if(s.equals("White")) c = Color.white;

                outSurf.setGlobalElementColor(c);
                outSurf.showElementColors(false);
                outSurf.showVertexColors(true);
                System.out.println("Global ele colour "+outSurf.getGlobalElementColor());

                
            }
            }
            catch(Exception e) {
                System.out.println(e.getLocalizedMessage());
            }
            setDisplayProperties(outSurf);

            outSurf.showBoundaries(false);
            outSurf.makeNeighbour();
            if(outCurve!=null) {
                outCurve.showVertices(cbShowVert.getState());
                outCurve.showPolygons(cbShowCurves.getState());
                setColour(outCurve,chColours.getSelectedItem());
                setDisplayProperties(outCurve);
            }
            store.geomChanged(outSurf);
            //store.showStatus("Geometry displayed");
            //store.addGeometry(surfRes);
            
        }
    }


    /**
     * Handles the selection of a new surface definition.
     */

    @Override
    public void itemStateChanged(ItemEvent e)
    {
        ItemSelectable itSel = e.getItemSelectable();
        if( itSel == chDefs )
        {
            int i = chDefs.getSelectedIndex();
            loadDefinition(lsmpDefs[i]);
        }
        else super.itemStateChanged(e);
    }

    @Override
    public String getDefaultDefName() {
        return this.my_defaultDefName;
    }

    @Override
    public String getDefinitionFileName() {
        return this.my_defFileName;
    }

    @Override
    public String getProgramName() {
        return programName;
    }

    @Override
    public List<PgGeometryIf> getOutputGeoms() {
        return Collections.singletonList((PgGeometryIf) outCurve);
    }

    @Override
    public boolean loadDefByName(String s) {
        LsmpDef def = getDef(s);
        if(def==null) return false;
        loadDefinition(def);
        return true;
    }

}
