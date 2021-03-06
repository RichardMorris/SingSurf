/* @author rich
 * Created on 30-Mar-2005
 *
 * See LICENSE.txt for license information.
 */
package org.singsurf.singsurf.clients;

import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.number.PuDouble;
import jv.project.PgGeometryIf;

import org.singsurf.singsurf.Calculator;
import org.singsurf.singsurf.LParamList;
import org.singsurf.singsurf.PuParameter;
import org.singsurf.singsurf.definitions.DefType;
import org.singsurf.singsurf.definitions.DefVariable;
import org.singsurf.singsurf.definitions.LsmpDef;
import org.singsurf.singsurf.geometries.GeomPair;
import org.singsurf.singsurf.geometries.GeomStore;
import org.singsurf.singsurf.jep.EvaluationException;
import org.singsurf.singsurf.operators.ContinuityClip;
import org.singsurf.singsurf.operators.SimpleCalcMap;
import org.singsurf.singsurf.operators.SimpleMap;
import org.singsurf.singsurf.operators.UnSuportedGeometryException;

/**
 * @author Rich Morris Created on 30-Mar-2005
 */
public class Mapping extends AbstractOperatorClient {
    /** 
	 * 
	 */
    private static final long serialVersionUID = -4659133739866394363L;
    protected static final String programName = "Mapping";
    /**
     * The default equation (x^2 y - y^3 - z^2 = 0.0;).
     */
    protected String my_def = "[3 x^4 + y x^2,-4 x^3 - 2 y x,y];";

    /**
     * The model loaded by default (defaultsurf.jvx.gz).
     */
    protected String my_defModelName = "defaultpsurf.jvx.gz";

    /** file name for definitions */
    protected String my_defFileName = "defs/mapping.defs";

    // LsmpDef def;
    // PuVariable displayVars[];
    protected PuDouble m_Clipping;
    protected PuDouble m_ContDist;
    // int globalSteps = 40;
    DefVariable localX, localY, localZ;
    PgPointSet mappedGeom;

    /** The operator which performs the mapping */
    SimpleMap map = null;

    /********** Constructor *********/

    public Mapping(GeomStore store, String name) {
        super(store, name);
        if (getClass() == Mapping.class) {
            setDisplayEquation(my_def);
            init(getDef(getDefaultDefName()), true);
        }
    }

    public Mapping(GeomStore store, LsmpDef def) {
        super(store, def == null ? "Mapping" : def.getName());
        if (getClass() == Mapping.class) {
            if (def == null)
                def = createDefaultDef();
            init(def, false);
        }
    }

    public LsmpDef createDefaultDef() {
        LsmpDef def;
        def = new LsmpDef("Mapping", DefType.mapping, "");
        def.add(new DefVariable("x", "none"));
        def.add(new DefVariable("y", "none"));
        def.add(new DefVariable("z", "none"));
        return def;
    }

    public void init(LsmpDef def, boolean flag) {
        super.init(flag);
        newParams = new LParamList(this);
        m_Clipping = new PuDouble("Clipping", this);
        m_Clipping.setValue(100.0);
        m_Clipping.setBounds(0.0, 1000.0);
        m_ContDist = new PuDouble("Continuity Distance", this);
        m_ContDist.setValue(1.0);
        m_ContDist.setBounds(0.0, 10.0);
        this.cbShowFace.setState(true);
        this.cbShowEdge.setState(true);
        this.cbShowVert.setState(false);
        this.chColours.addItemListener(this);
        loadDefinition(def);
    }

    void checkDef(LsmpDef def) {
    }

    public void loadDefinition(LsmpDef newdef) {
        LsmpDef def = newdef.duplicate();
        checkDef(def);
        def.setName(this.getName());
        this.getInfoPanel().setTitle(this.getName());
        calc = new Calculator(def, 0);
        calc.build();
        if (!calc.isGood())
            showStatus(calc.getMsg());
        map = new SimpleCalcMap(calc);
        localX = calc.getDefVariable(0);
        localY = calc.getDefVariable(1);
        localZ = calc.getDefVariable(2);
        setDisplayEquation(def.getEquation());
        refreshParams();
        // calcSurf();
    }

    void debugCols(PgElementSet geom) {
        /*
         * System.out.println("EleCol" + geom.isShowingElementColors());
         * System.out.println("EleFromVertCol" +
         * geom.isShowingElementFromVertexColors());
         * System.out.println("hasEleCol" + geom.hasElementColors());
         * System.out.println("VertCol" + geom.isShowingVertexColors());
         * System.out.println("hasVertCol" + geom.hasVertexColors());
         */}

    public void calcGeom(GeomPair p) {
        if (calc != null && !calc.isGood()) {
            showStatus(calc.getMsg());
            return;
        }
        PgGeometryIf input = p.getInput();

        System.out.println("input");
        // debugCols((PgElementSet) input);
        mappedGeom = (PgPointSet) input.clone();
        System.out.println("mapped before "+mappedGeom.hasVertexColors());
//        System.out.println("Vert cols "+geom.hasVertexColors()+" "+geom.hasElementColors());

        // debugCols((PgElementSet) mappedGeom);
        try {
            map.operate(mappedGeom);
            ContinuityClip cclip = new ContinuityClip(
                    this.m_ContDist.getValue());
            cclip.operate(mappedGeom);
            // SphereClip clip = new SphereClip(this.m_Clipping.getValue());
            // clip.operate(mappedGeom);
        } catch (UnSuportedGeometryException e) {
            showStatus("Unsupported geometry type");
            return;
        } catch (EvaluationException e) {
            System.out.println(e.toString());
            calc.setGood(false);
            return;
        }

        System.out.println("mapped outer");
        // debugCols((PgElementSet) mappedGeom);
        PgGeometryIf out = p.getOutput();

        System.out.println("out");
        // debugCols((PgElementSet) out);

        GeomStore.copySrcTgt(mappedGeom, out);

        System.out.println("out");
        // debugCols((PgElementSet) out);

        if (input instanceof PgElementSet) {
            if (((PgElementSet) input).isShowingElementColors())
                ((PgElementSet) out).showElementColors(true);
            if (((PgElementSet) input).hasVertexColors()) {
                ((PgElementSet) out).makeElementFromVertexColors();
            } else if (((PgElementSet) input).hasElementColors()) {
                ((PgElementSet) out).showElementColors(true);
            }
            ((PgElementSet) out).removeVertexNormals();
        }
        else if(input instanceof PgPolygonSet) {
            if( ((PgPolygonSet) input).isShowingPolygonColors())
                ((PgPolygonSet) out).showPolygonColors(true);
            
        } else {
            if( ((PgPointSet) input).isShowingVertexColors())
                ((PgPointSet) out).showVertexColors(true);
            
        }

        setDisplayProperties(out);

        System.out.println("out");
        // debugCols((PgElementSet) out);

        store.geomChanged(out);
    }

    /**
     * Calculate all the needed geoms.
     */
    public void calcGeoms() {
        for (GeomPair p : activePairs.values())
            calcGeom(p);
    }

    public void newActiveInput(String name) {
        if (activePairs.containsKey(name)) {
            showStatus(name + " is already active");
            return;
        }
        PgGeometryIf input = store.getGeom(name);
        PgGeometryIf output = store.aquireGeometry(
                getPreferredOutputName(name), input, this);
        GeomPair p = new GeomPair(input, output);
        activePairs.put(name, p);
        activeInputNames.add(name);
        setDisplayProperties(p.getOutput());
        calcGeom(p);
    }

    public boolean update(Object o) {
        if (o instanceof PuParameter) {
            return parameterChanged((PuParameter) o);
        } else if (o == m_Clipping || o == this.m_ContDist) {
            calcGeoms();
            return true;
        } else
            return super.update(o);
    }

    /**
     * Handles the selection of a new surface definition.
     */

    public void itemStateChanged(ItemEvent e) {
        ItemSelectable itSel = e.getItemSelectable();
        if (itSel == chDefs) {
            int i = chDefs.getSelectedIndex();
            loadDefinition(lsmpDefs[i]);
        } else
            super.itemStateChanged(e);
    }

    public String getDefaultDefName() {
        return this.my_defModelName;
    }

    public String getDefinitionFileName() {
        return this.my_defFileName;
    }

    public String getProgramName() {
        return programName;
    }

    public String makeCGIstring() {
        return null;
    }

    public void geometryDefHasChanged(Calculator inCalc) {
    }
}
