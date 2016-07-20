package org.singsurf.singsurf.asurf;

import static org.singsurf.singsurf.asurf.Key3D.FACE_BB;
import static org.singsurf.singsurf.asurf.Key3D.FACE_DD;
import static org.singsurf.singsurf.asurf.Key3D.FACE_FF;
import static org.singsurf.singsurf.asurf.Key3D.FACE_LL;
import static org.singsurf.singsurf.asurf.Key3D.FACE_RR;
import static org.singsurf.singsurf.asurf.Key3D.FACE_UU;

import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.acurve.Bern2D;

public abstract class BoxClevA {

	//private static final int numberofbitsinmantissaofrealnumber = 27;
	protected static final int MAX_EDGE_LEVEL = 32768;
	protected static final int EMPTY = 0;
	protected static final int FOUND_EVERYTHING = 2;
	protected static final int FOUND_FACES = 3;
	/*** A value returned by follow when there a sol not on an edge is found. ***/
	static final int NEW_NODE = 2;
	protected static final int global_mode = 0;
	protected static final double BAD_EDGE = -0.5;
	protected static final boolean NOT_DEF = false;
	protected Box_info whole_box;
	protected Bern3D BB;
	protected Bern3D CC;
	protected Bern3D DD;
	protected Bern3D EE;
	protected Bern3D Dxx;
	protected Bern3D Dxy;
	protected Bern3D Dxz;
	protected Bern3D Dyy;
	protected Bern3D Dyz;
	protected Bern3D Dzz;
    protected int LINK_FACE_LEVEL;
    protected int LINK_SING_LEVEL;
    protected int RESOLUTION;
    protected int SUPER_FINE;
	protected static Region_info globalRegion;
	protected Sol_info[] known_sings;
	protected int num_known_sings;
	/*********************** Start of Sub-routines **************************/
	public int global_selx = -1;
	public int global_sely;
	public int global_selz;
	public int global_denom;
	public boolean global_lf = false;
	protected Facets facets;
	protected Plotter plotter;

	public BoxClevA() {
		super();
	}

	protected void printInput(double[][][] aa, Region_info region) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("double aa[][][] = new double[][][] {\n");
	    for(int i=0;i<aa.length;++i) {
	        sb.append("{");
	        for(int j=0;j<aa[0].length;++j) {
	            sb.append("{");
	            for(int k=0;k<aa[0][0].length;++k) {
	                sb.append(aa[i][j][k]);
	                if(k<aa[0][0].length-1)
	                    sb.append(",");
	            }
	            sb.append("}");
	            if(j<aa[0].length-1)
	                sb.append(",");
	        }
	        sb.append("}");
	        if(i<aa.length-1)
	            sb.append(",\n");
	    }
	    sb.append("};\n");
	    sb.append("["+region.xmin+","+region.xmax+"],["+region.ymin+","+region.ymax+"],["+region.zmin+","+region.zmax+"]\n");
	    //				{{-1.0, 0.0, 1.0}, {0.0, 0.0, 0.0}, {1.0, 0.0, 0.0}},
	    //		        {{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}}, 
	    //		        {{1.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}}};	
	    System.out.print(sb.toString());
	
	}

	/** finds the pos in original domain **/
	public void calc_pos_actual(Sol_info sol, double[] vec) {
	    double v[]=sol.calc_pos();
	
	    /* The above is of course incorrect as its been incorrectly scaled */
	
	    vec[0] = globalRegion.xmin + v[0] * (globalRegion.xmax-globalRegion.xmin);
	    vec[1] = globalRegion.ymin + v[1] * (globalRegion.ymax-globalRegion.ymin);
	    vec[2] = globalRegion.zmin + v[2] * (globalRegion.zmax-globalRegion.zmin);
	}

	/** Find the pos and norm in original domain 
	 * The actual derivative is the evalbern result / range 
	 * (Second derivatives is Dxx / range^2)
	 **/
	void calc_pos_norm_actual(Sol_info sol, double vec[], double norm[]) {
	    calc_pos_norm(sol,vec,norm);
	
	    vec[0] = globalRegion.xmin + vec[0] * (globalRegion.xmax-globalRegion.xmin);
	    vec[1] = globalRegion.ymin + vec[1] * (globalRegion.ymax-globalRegion.ymin);
	    vec[2] = globalRegion.zmin + vec[2] * (globalRegion.zmax-globalRegion.zmin);
	
	    norm[0] = norm[0] / (globalRegion.xmax-globalRegion.xmin);
	    norm[1] = norm[1] / (globalRegion.ymax-globalRegion.ymin);
	    norm[2] = norm[2] / (globalRegion.zmax-globalRegion.zmin);
	}

	void calc_pos_norm(Sol_info sol, double vec[], double norm[]) {
	    calc_pos(sol,vec);
	
	    if(sol.dx == 0) norm[0] = 0.0;
	    else             norm[0] = evalbern3D(CC,vec);
	    if(sol.dy == 0) norm[1] = 0.0;
	    else             norm[1] = evalbern3D(DD,vec);
	    if(sol.dz == 0) norm[2] = 0.0;
	    else             norm[2] = evalbern3D(EE,vec);
	}

	protected void calc_pos(Sol_info sol, double[] vec) {
	    sol.calc_pos(vec);
	}

	protected double evalbern3D(Bern3D dxx2, double[] vec0) {
	    return dxx2.evalbern3D(vec0);
	}
	
    static class CN_context {
        Bern2D bb,dx,dy,dz; double vec[];
        int signDx,signDy,signDz;
        Sol_info sol;
    }


	protected void PRINT_CN_VEC(CN_context cn) {
	    double v=0.0,vx=0.0,vy=0.0,vz=0.0;	
	    v = cn.bb.evalbern2D(cn.vec);	
	    if(cn.signDx==0) vx = cn.dx.evalbern2D(cn.vec);	
	    if(cn.signDy==0) vy = cn.dy.evalbern2D(cn.vec);	
	    if(cn.signDz==0) vz = cn.dz.evalbern2D(cn.vec);	
	    System.out.printf("t%6.3f %6.3ft%g %g %g %g\n\t",cn.vec[0],cn.vec[1],v,vx,vy,vz); 
	    if(cn.sol.type == FACE_RR || cn.sol.type == FACE_LL )	
	        System.out.printf("%f %f %f\n",	
	                globalRegion.xmin + ( ((double) cn.sol.xl)/cn.sol.denom ) * (globalRegion.xmax-globalRegion.xmin), 	
	                globalRegion.ymin + ( (cn.sol.yl + cn.vec[0])/cn.sol.denom ) * (globalRegion.ymax-globalRegion.ymin),	
	                globalRegion.zmin + ( (cn.sol.zl + cn.vec[1])/cn.sol.denom ) * (globalRegion.zmax-globalRegion.zmin));	
	    else if(cn.sol.type == FACE_BB || cn.sol.type == FACE_FF)	
	        System.out.printf("%f %f %f\n",	
	                globalRegion.xmin + ( (cn.sol.xl + cn.vec[0])/cn.sol.denom ) * (globalRegion.xmax-globalRegion.xmin), 	
	                globalRegion.ymin + ( ((double) cn.sol.yl)/cn.sol.denom ) * (globalRegion.ymax-globalRegion.ymin),	
	                globalRegion.zmin + ( (cn.sol.zl + cn.vec[1])/cn.sol.denom ) * (globalRegion.zmax-globalRegion.zmin));	
	    else if(cn.sol.type == FACE_UU || cn.sol.type == FACE_DD)	
	        System.out.printf("%f %f %f\n",	
	                globalRegion.xmin + ( (cn.sol.xl + cn.vec[0])/cn.sol.denom ) * (globalRegion.xmax-globalRegion.xmin), 	
	                globalRegion.ymin + ( (cn.sol.yl + cn.vec[1])/cn.sol.denom ) * (globalRegion.ymax-globalRegion.ymin),	
	                globalRegion.zmin + ( ((double) cn.sol.zl)/cn.sol.denom ) * (globalRegion.zmax-globalRegion.zmin));	
	}

	protected void calc_relative_pos(Sol_info sol, double[] vec) {
	    sol.calc_relative_pos(vec);
	}

	protected void PRINT_CS_VEC() {
	    // TODO Auto-generated method stub
	
	}

	protected short signOf(Bern3D bb) {
	
	    return bb.signOf();
	}

	protected void print_sol(Sol_info sol) {
	    System.out.print(sol);
	}

	protected Bern3D diffz3D(Bern3D dx) {
	    return dx.diffZ();
	}

	protected Bern3D diffy3D(Bern3D dx) {
	    return dx.diffY();
	}

	protected Bern3D diffx3D(Bern3D dx) {
	    return dx.diffX();
	}

	protected void draw_box(Box_info box) {
	    plotter.plot_box(box);
	}

	abstract public boolean marmain(double[][][] coeffs, Region_info region) throws AsurfException;



}