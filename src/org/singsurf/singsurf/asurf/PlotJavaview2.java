package org.singsurf.singsurf.asurf;

import static org.singsurf.singsurf.asurf.Key3D.BOX;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;
import jv.project.PgGeometryIf;
import jv.vecmath.PdVector;
import jv.vecmath.PiVector;

public class PlotJavaview2 implements Plotter {
    private static final boolean PRINT_FACET=false;
    private static final boolean PLOT_NODE_LINKS=true;
    private static final boolean PLOT_SINGS=true;
    private static final int MODE_KNOWN_SING = 0;
    private static final boolean FIX_NORMS = false;


    protected PgGeometryIf outGeom;
    PgElementSet elements;
    PgPointSet points;
    PgPolygonSet lines;
    BoxClevA boxclev;

    /************************************************************************/
    /*									*/
    /*	some sub programs to plot the boxes.				*/
    /*	The general process is as follows:				*/
    /*	for each box {							*/
    /*	{   for each solution						*/
    /*		if solution already used continue;			*/
    /*		plot first face adjacient to solution, update solution  */
    /*									*/
    /************************************************************************/

    int vrml_version;
    boolean draw_lines;
    String global_geomname;

    /*
	private static final boolean ECHO_OUTPUT
     */

    int global_degen;
    int global_mode;
    Sol_info known_sings[];	/* The singularities known from external data */
    int num_known_sings;	/* number of such */

    Region_info region;
    int facet_vertex_count;		/*** The number of verticies on a facet ***/
    int total_face_sol_count;		/*** The number of solutions on faces ***/


    int tri_index[]=new int[3];	/* holds the indices of the current facet */
    int tri_count,total_tri_count;
    int vect_point_count = 0;	/* number of points on degenerate lines */
    int vect_count = 0;		/* number of line segments */
    int isolated_count = 0;		/* number of isolated points */
    List<PiVector> eles    = new ArrayList<PiVector>();
    List<PdVector> verts = new ArrayList<PdVector>();
    List<PdVector> norms = new ArrayList<PdVector>();
    List<Color> cols = new ArrayList<Color>();
    
    Map<Integer,List<Integer>> goodNorms = new HashMap<Integer,List<Integer>>();

    public PlotJavaview2(BoxClevA boxclev, PgElementSet elements,
            PgPolygonSet lines, PgPointSet points,boolean draw_lines) {
        super();
        this.boxclev = boxclev;
        this.elements = elements;
        this.points = points;
        this.lines = lines;
        this.draw_lines = draw_lines;
    }

    public void clear() {
    	this.eles.clear();
    	this.verts.clear();
    	this.norms.clear();
    	this.goodNorms.clear();
    }
    /************************************************************************/
    /*									*/
    /*	draws a box.							*/
    /*									*/
    /************************************************************************/

    int facet_count=0;
    public void plot_all_facets(Box_info box)
    {
        if(box.facets==null) return;
        if(boxclev.global_selx >=0 && (
             box.xl * boxclev.global_denom < boxclev.global_selx * box.denom 
          || box.xl * boxclev.global_denom > (boxclev.global_selx+1) * box.denom ) ) return;
        if(boxclev.global_sely >=0 && (
             box.yl * boxclev.global_denom < boxclev.global_sely * box.denom 
          || box.yl * boxclev.global_denom > (boxclev.global_sely+1) * box.denom ) ) return; 
        if(boxclev.global_selz >=0 && (
             box.zl * boxclev.global_denom < boxclev.global_selz * box.denom 
          || box.zl * boxclev.global_denom > (boxclev.global_selz+1) * box.denom ) ) return;
        if(boxclev.global_selx>=0|| boxclev.global_sely>=0 || boxclev.global_selz>=0 )
        {
            System.out.println(box);
            Set<Sol_info> allSols = new HashSet<Sol_info>();
            for(Facet_info f1:box.facets)
                allSols.addAll(f1.getSols());
//            int typeCount[] = countSolType(allSols);
//            if(typeCount[3]+typeCount[4]+typeCount[5]+typeCount[6]+typeCount[7]+typeCount[8]+typeCount[9]+typeCount[10]+typeCount[11] > 0)
//                System.out.println("TYPECOUNT"+Arrays.toString(typeCount));
//            if(typeCount[9]>0) {
                for(Sol_info s:allSols) {
                    if(s.type == Key3D.BOX) {
                        System.out.println(s);
                        
                        ((Boxclev) boxclev).solveSing(box,boxclev.BB,boxclev.CC,boxclev.DD,boxclev.EE,0,0,0,0);
                    }
            }
        }
        
        for(Facet_info f1:box.facets)
        {
            if(boxclev.global_selx>=0|| boxclev.global_sely>=0 || boxclev.global_selz>=0 )
                System.out.println(f1);

            plot_facet(f1,box);
        }
    }

    private int[] countSolType(Set<Sol_info> allSols) {
        int res[] = new int[12];
        for(Sol_info s:allSols) {
            switch(s.type) {
            case X_AXIS: ++res[0]; break;
            case Y_AXIS: ++res[1]; break;
            case Z_AXIS: ++res[2]; break;
            case FACE_BB: ++res[3]; break;
            case FACE_DD: ++res[4]; break;
            case FACE_FF: ++res[5]; break;
            case FACE_LL: ++res[6]; break;
            case FACE_RR: ++res[7]; break;
            case FACE_UU: ++res[8]; break;
            case BOX: ++res[9]; break;
            case VERTEX: ++res[10]; break;
            case NONE: ++res[11]; break;
            }
        }
        return res;
    }

    
    boolean problem=false;
    private void plot_facet(Facet_info f1, Box_info box) {
        Facet_sol s1;
        problem=false;
        //		System.out.println(f1);

        s1 = f1.sols;
        if(s1 == null) return;
        /* check the facet has at least 3 sols */
        if(s1.next == null) return;
        if(s1.next.next == null) return;

        bgnfacet();
        int nSols=0;
        while(s1 != null)
        {
            
                plot_sol(s1.sol);
                if(s1.sol.plotindex>=0)
                    ++nSols;
            s1 = s1.next;
        }
        int[] ind = new int[nSols];


        s1 = f1.sols;
        int pos=0;
        while(s1 != null)
        {
            if(
               s1.sol.plotindex>=0) {
                ind[pos]=s1.sol.plotindex;
                ++pos;
            }
            s1=s1.next;
        }
        if(FIX_NORMS && problem) {
            boolean flag = fixNorms(f1,nSols,ind);
            //if(!flag) return;
        }

        if(!testClockwise(f1,ind)) {
        	int l=ind.length;
        	for(int i=0;i<l/2;++i) {
        		int tm = ind[l-i-1];
        		ind[l-i-1]=ind[i];
        		ind[i]=tm;
        	}
        }
        ++facet_count;
        PiVector indices = new PiVector(ind);
        this.eles.add(indices);
        endfacet();
    }

    boolean simplePlot=true;
    boolean plotCond(Sol_info sol) {
        if(simplePlot) return true;
        switch(sol.type)
        {
        case X_AXIS:
        case Y_AXIS:
        case Z_AXIS:
        case BOX:
            return true;
        case FACE_LL: case FACE_RR:
        case FACE_FF: case FACE_BB:
        case FACE_DD: case FACE_UU:
            if( sol.root == 0.0 || sol.root == 1.0 || sol.root2 == 0.0 || sol.root2 == 1.0 )
                return false;
        default:
            return true;
        }
    }

    Color calcSolColour(Sol_info sol) {
        int r = (sol.dx == 0  ? 255 : 0);
        int g = (sol.dy == 0  ? 255 : 0);
        int b = (sol.dz == 0  ? 255 : 0);

        return new Color(r,g,b);
    }

    void plot_sol(Sol_info sol)
    {
        double vec[]=new double[3],norm[]=new double[3];
        //		float  fvec[]=new float[3],fnorm[]=new float[3];
        int  col[]=new int[3];

        /*
	if( total_tri_count > 10 ) return;
         */
        if(sol == null)
        {
            System.out.printf("Error: plot_sol: sol == null\n");
            return;
        }

        /* First calculate the position */

        if(sol.plotindex == -2) return;

        if(!plotCond(sol)) {
            sol.plotindex = -2;
            return;
        }
        if(sol.plotindex == -1 )
        {
            calc_pos_norm_actual(sol,vec,norm);
            if(vec[0] != vec[0])
            {
                System.out.printf("NaN in plot_sol\n");
                print_sol(sol);
                sol.plotindex = -2;
                return;
            }
            //			fvec[0] = (float) vec[0];
            //			fvec[1] = (float) vec[1];
            //			fvec[2] = (float) vec[2];
            //			fnorm[0] = (float) norm[0];
            //			fnorm[1] = (float) norm[1];
            //			fnorm[2] = (float) norm[2];
            //			goodnorm[tri_count] = unit3frobust(fnorm);
            unit3drobust(norm);

            sol.plotindex = total_face_sol_count++;

            verts.add(new PdVector(vec));
            norms.add(new PdVector(norm));
            cols.add(calcSolColour(sol));
            //			elements.setVertex(sol.plotindex,new PdVector(vec));
            //			elements.setVertexNormal(sol.plotindex,new PdVector(norm));
            //			elements.setVertexColor(sol.plotindex,new Color(col[0],col[1],col[2]));

        }
        if(sol.dx==0&&sol.dy==0&&sol.dz==0)
            problem=true;
        if(PRINT_FACET) {
            System.out.printf("No %d ",sol.plotindex);
            print_sol(sol);
            /*
			System.out.printf("%5.2f %5.2f %5.2f # %5.2f %5.2f %5.2f # %5.2f %5.2f %5.2f\n",
				fvec[0],fvec[1],fvec[2],norm[0],norm[1],norm[2],fnorm[0],fnorm[1],fnorm[2]);
             */
        }

        return;
    }

    private boolean fixNorms(Facet_info f1, int nSols, int[] ind) {
        Sol_info[] sols=new Sol_info[nSols];
        int[] status=new int[nSols];
        PdVector[] pos=new PdVector[nSols];
        PdVector[] norms=new PdVector[nSols];
        int goodIndex=-1;
        Facet_sol s1 = f1.sols;
        int n=0;
        while(s1 != null)
        {
            sols[n]=s1.sol;
            if(s1.sol.dx==0 && s1.sol.dy==0 && s1.sol.dz==0) {
                status[n]=0;
            }
            else {
                status[n]=1;
                norms[n]= this.norms.get(s1.sol.plotindex);
                goodIndex=n;
            }
            pos[n]=this.verts.get(s1.sol.plotindex);
            s1 = s1.next;
            ++n;
        }

        if(goodIndex==-1) {
            System.out.println("No good normals");
            System.out.print(f1);
        }

        // if no normal for a point calculate using cross product of adjacent edges.
        for(int i=0;i<n;++i) {
            int prev = i==0?n-1:i-1;
            int next = i==n-1?0:i+1;
            if(status[i]==1) continue;

            PdVector A = PdVector.subNew(pos[i], pos[prev]);
            PdVector B = PdVector.subNew(pos[i], pos[next]);
            //        		double[] a = A.getEntries();
            //        		double[] b = B.getEntries();
            //        		double[] nn = new double[3];
            //        		nn[0]= a[1] * b[2] - a[2] * b[1];
            //        		nn[1]= a[2] * b[0] - a[0] * b[2];
            //        		nn[2]= a[0] * b[1] - a[1] * b[0];
            PdVector nor = PdVector.crossNew(A, B);
            //nor.cross(A, B);
            double len=nor.length();
            if(Math.abs(len)<1e-6) {
                /* empty so other clauses work */
            }
            else if(goodIndex!=-1) {
                double dot= nor.dot(norms[goodIndex]);
                if(dot<0)
                    nor.multScalar(-1.0);
                nor.normalize();
            }
            else {
                goodIndex=i;
                nor.normalize();
            }
            norms[i]=nor;


        }
        if(goodIndex==-1) {
            System.out.println("Still no good normal");
            return false;
        }

        for(int i=0;i<n;++i) {
            double len=norms[i].length();
            if(Math.abs(len)<1e-6) {
                norms[i].set(norms[goodIndex], 0, 3);
            }

            if(status[i]==0) {
                ind[i] = total_face_sol_count++;
                verts.add(pos[i]);
                this.norms.add(norms[i]);
                //cols.add(new Color(128,128,128));
            }
            //			double[] p=pos[i].getEntries();
            //			double[] nn=norms[i].getEntries();
            //			System.out.printf("pt (%f,%f,%f) %f (%f,%f,%f)%n", p[0],p[1],p[2],len,nn[0],nn[1],nn[2]);
        }
        return true;
    }
    
    double tripleScalar(int a,int b,int c) {
    	PdVector A = this.verts.get(a);
    	PdVector B = this.verts.get(b);
    	PdVector C = this.verts.get(c);
    	PdVector N = this.norms.get(b);

    	PdVector BA = PdVector.subNew(A,B);
        PdVector BC = PdVector.subNew(C,B);
        PdVector nor = PdVector.crossNew(BA, BC);
        double dot = PdVector.dot(N, nor);
        return dot;
    }
    boolean testClockwise(Facet_info f,int ind[]) {
    	int l=ind.length;
    	if(l<3) return false;
    	double dots[]=new double[l];
    	double max=0;
    	double min=0;
    	int npos=0;
    	int nneg=0;
    	for(int i=0;i<l;++i) {
    		//int a = i-1>=0 ? i-1 : i-1+l;
    		dots[i]=tripleScalar(ind[(i-1+l)%l],ind[i],ind[(i+1)%l]);
    		if(dots[i]>max) max=dots[i];
    		if(dots[i]<min) min=dots[i];
    		if(dots[i]>0) ++npos;
    		if(dots[i]<0) ++nneg;
    	}
//    	if(npos>0 && nneg>0) {
//    		StringBuilder sb = new StringBuilder("Non convex "+npos+" "+nneg+" "+eles.size()+"[");
//    		for(int i=0;i<l;++i) {
//    			sb.append(String.format("%+5.3f ", dots[i]));
//    		}
//    		sb.append("]");
//    		System.out.println(sb.toString());
//    	}
    	return (-min>max);
    }

    void plot_all_sings(Box_info box,int flag)
    {
        if(box.sings==null) return;
        for(Sing_info sing:box.sings)
        {
            Sol_info sol;

            sol = sing.sing;
            if( flag == 2 && global_mode == MODE_KNOWN_SING)
            {
                int i;
                for(i=0;i<num_known_sings;++i)
                {
                    if( sol.xl == known_sings[i].xl
                            && sol.yl == known_sings[i].yl
                            && sol.zl == known_sings[i].zl
                            && sol.root == known_sings[i].root
                            && sol.root2 == known_sings[i].root2
                            && sol.root3 == known_sings[i].root3 )
                        plot_point(sing.sing);
                }
            }
            else if( flag == 0
                    || (sol.dx == 0 && sol.dy == 0 && sol.dz == 0) )
                plot_point(sing.sing);
        }
        if(box.lfd != null)
        {
            plot_all_sings(box.lfd,flag);
            plot_all_sings(box.lfu,flag);
            plot_all_sings(box.lbd,flag);
            plot_all_sings(box.lbu,flag);
            plot_all_sings(box.rfd,flag);
            plot_all_sings(box.rfu,flag);
            plot_all_sings(box.rbd,flag);
            plot_all_sings(box.rbu,flag);
        }
    }

    /* flag = 0 plot everything, = 1 polt only join sings, = 2 only draw sings with all derivs zero */

    void plot_all_node_links(Box_info box,int flag)
    {
        if(box.node_links==null) return;
        for(Node_link_info node_link: box.node_links)
        {
            if( flag == 0 
                    || ( flag == 1 && 
                            node_link.A.sol.type == BOX && node_link.B.sol.type == BOX) 
                            || ( flag == 2 
                                    && node_link.A.sol.dx == 0
                                    && node_link.A.sol.dy == 0
                                    && node_link.A.sol.dz == 0
                                    && node_link.B.sol.dx == 0
                                    && node_link.B.sol.dy == 0
                                    && node_link.B.sol.dz == 0 ) )
                plot_line(node_link.A.sol,node_link.B.sol);

        }

        if(box.lfd != null)
        {
            plot_all_node_links(box.lfd,flag);
            plot_all_node_links(box.lfu,flag);
            plot_all_node_links(box.lbd,flag);
            plot_all_node_links(box.lbu,flag);
            plot_all_node_links(box.rfd,flag);
            plot_all_node_links(box.rfu,flag);
            plot_all_node_links(box.rbd,flag);
            plot_all_node_links(box.rbu,flag);
        }
    }

    /********** Main entry point for routines *****************/

    public void plot_box(Box_info box)
    {
        plot_all_facets(box);

        /* Now draw the node_links */

        if(PLOT_NODE_LINKS) {
            if(draw_lines)
            {
                if(global_degen != -1)
                    plot_all_node_links(box,global_degen);
                else
                    plot_all_node_links(box,2);
            }
        }

        if(PLOT_SINGS) {
            if(draw_lines)
            {
                if(global_degen != -1)
                    plot_all_sings(box,global_degen);
                else
                    plot_all_sings(box,2);
            }
        }

    }

    /************************************************************************/
    /*									*/
    /*	Now some routines for drawing the facets			*/
    /*									*/
    /************************************************************************/

    public void initPlotter()
    {
        if(draw_lines)
        {

        }
        total_tri_count = 0;
        total_face_sol_count = 0;
        vect_point_count = 0;
        vect_count = 0;
        isolated_count = 0;
    }

    /*
     * Function:	rewindoogl
     * Action:	When output goes to standard output and more than
     *		model is produced it is imposible to rewind the standard
     *		output. One way round is to delay the creation of the
     *		ooglfile untill the program has ended. This comand
     *		is called intermediatly between models to clear out
     *		the vect and quad tempory vector files.
     */

    public void rewindoogl()
    {
        if(draw_lines)
        {

        }

        total_tri_count = 0;
        total_face_sol_count = 0;
        vect_point_count = 0;
        vect_count = 0;
        isolated_count = 0;
    }

    /*
     * Function:	finiflush
     * Action:	convert all the data into oogl and write it to the
			ooglfile. Called when the model is complete 'finioogl'
			or when a flush is required 'flushoogl'
     */


    void finiflush()
    {
        PiVector res[] = new PiVector[eles.size()];
        PdVector[] pts = new PdVector[verts.size()];
        PdVector[] ns  = new PdVector[norms.size()];
        Color[] co     = new Color[cols.size()];
        elements.setNumVertices(verts.size());
        elements.setVertices(verts.toArray(pts));
        elements.setVertexNormals(norms.toArray(ns));
        elements.setVertexColors(cols.toArray(co));
        elements.setNumElements(res.length);
        elements.setElements(eles.toArray(res));
    }


    void flushoogl()
    {
        finiflush();
    }

    public void finiPlotter()
    {
        finiflush();
    }

    /*
     * Function:	quadwrite
     * action:	writes out a quadrilateral, includes averaging normals.
     *		n is number of verticies to write.
     */

    void unit3f(float vec[])
    {
        float len;
        len = (float) Math.sqrt((double) vec[0]*vec[0]+vec[1]*vec[1]+vec[2]*vec[2]);
        if( len == 0.0 ) return;
        vec[0] /= len;
        vec[1] /= len;
        vec[2] /= len;
    }

    short unit3frobust(float vec[])
    {
        float len;
        len = (float) Math.sqrt((double) vec[0]*vec[0]+vec[1]*vec[1]+vec[2]*vec[2]);
        if( len == 0.0 || Float.isNaN(len) ) return 0;
        vec[0] /= len;
        vec[1] /= len;
        vec[2] /= len;
        return 1;
    }

    short unit3drobust(double vec[])
    {
        double len;
        len = Math.sqrt(vec[0]*vec[0]+vec[1]*vec[1]+vec[2]*vec[2]);
        if( len == 0.0 || Double.isNaN(len) ) return 0;
        vec[0] /= len;
        vec[1] /= len;
        vec[2] /= len;
        return 1;
    }

    void bgnfacet()
    {
        if(PRINT_FACET) {
            System.out.printf("bgnfacet:\n");
        }
        facet_vertex_count = 0;
        tri_count = 0;
    }

    void endfacet()
    {
    }

    /********* test using much bigger polygons ********/

    short goodnorm[]=new short[3];


    private void calc_pos_norm_actual(Sol_info sol, double[] vec, double[] norm) {
        boxclev.calc_pos_norm_actual(sol,vec,norm);
    }

    private void print_sol(Sol_info sol) {
        System.out.print(sol);
    }

    void plot_point(Sol_info sol)
    {
        double vec[]=new double[3];

        if(sol == null)
        {
            System.out.printf("Error: plot_sol: sol == null\n");
            return;
        }

        /* First calculate the position */

        calc_pos_actual(sol,vec);
        if(PRINT_FACET) {
            System.out.printf("point:\n");
            print_sol(sol);
        }
        PdVector pdv = new PdVector(vec);
        this.points.addVertex(pdv);

        ++isolated_count;
    }

    private void calc_pos_actual(Sol_info sol, double[] vec) {
        boxclev.calc_pos_actual(sol,vec);
    }

    void plot_line(Sol_info sol1,Sol_info sol2)
    {
        double vec[]=new double[3];

        if(sol1 == null)
        {
            System.out.printf("Error: plot_sol1: sol1 == null\n");
            return;
        }

        /* First calculate the position */

        calc_pos_actual(sol1,vec);
        if(vec[0] != vec[0] || vec[1] != vec[1] || vec[2] != vec[2] )
        {
            System.out.printf("bad posn vec %f %f %f\n",
                    vec[0],vec[1],vec[2] );
            print_sol(sol1);
        }
        lines.addVertex(new PdVector(vec));

        calc_pos_actual(sol2,vec);
        if(PRINT_FACET) {
            System.out.printf("line: \n");
            print_sol(sol1);
            print_sol(sol2);
        }

        if(vec[0] != vec[0] || vec[1] != vec[1] || vec[2] != vec[2] )
        {
            System.out.printf("bad posn vec %f %f %f\n",
                    vec[0],vec[1],vec[2] );
            print_sol(sol2);
        }
        lines.addVertex(new PdVector(vec));

        lines.addPolygon(new PiVector(vect_point_count,vect_point_count+1));
        ++vect_count;
        vect_point_count += 2;


    }
}
