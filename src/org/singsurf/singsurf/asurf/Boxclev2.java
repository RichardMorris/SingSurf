
package org.singsurf.singsurf.asurf;

import static org.singsurf.singsurf.asurf.Key3D.BOX;
import static org.singsurf.singsurf.asurf.Key3D.FACE_BB;
import static org.singsurf.singsurf.asurf.Key3D.FACE_DD;
import static org.singsurf.singsurf.asurf.Key3D.FACE_FF;
import static org.singsurf.singsurf.asurf.Key3D.FACE_LL;
import static org.singsurf.singsurf.asurf.Key3D.FACE_RR;
import static org.singsurf.singsurf.asurf.Key3D.FACE_UU;
import static org.singsurf.singsurf.asurf.Key3D.Z_AXIS;

import java.util.ArrayList;
import java.util.List;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;

import org.singsurf.singsurf.acurve.AsurfException;
import org.singsurf.singsurf.acurve.Bern1D;
import org.singsurf.singsurf.acurve.Bern2D;
/**
 * This version has the little factes code
 * 
 * @author Richard Morris
 *
 */
public class Boxclev2 extends BoxClevA {
    private static final boolean PLOT_BITS = true;
    private static final boolean FAKE_SINGS = false;
    static final private boolean USE_2ND_DERIV=false;
    static final private boolean FACETS=false;
    static final private boolean NON_GENERIC_EDGE=true;
    private static final boolean LITTLE_FACETS = false; // whether to call facets.make_little_facets
    static final boolean FACEHASH=true; // whether to use a hash-table for storing faces


    /* unused 
    static final private boolean PRINT_PYRIMID=false;
    static final private boolean PRINT_SOLVEEDGE=false;
    static final private boolean PLOT_AS_LINES=false;
    static final private boolean PRINT_REDUCE_EDGE=false;
    static final private boolean PRINT_FOLLOW=false;
    static final private boolean PRINT_DRAW_BOX=false;
    static final private boolean PRINT_SUSLINK=false;
    static final private boolean PRINT_PRE_COLLECT=false;
    static final private boolean PRINT_SECOND=false;
    static final private boolean PRINT_SOL_VAL_ERR=false;
    static final private boolean PRINT_ODD_BOX=false;
    static final private boolean TEST_ALLOC=false;
    static final private boolean PRINT_LINK_FACES_ALL=false;
    static final private boolean LITTLE_FACETS=false;
    static final private boolean USE_STURM=false;

    static final private boolean TESTNODES=true;
    static final private boolean NON_GENERIC_NODES=true;
    static final private boolean DO_FREE=true;
    static final private boolean LINK_SING=true;
    static final private boolean LINK_SOLS=true;
    static final private boolean LINK_FACE=true;
    static final private boolean DO_PLOT=true;
     */
    /* purposes of drawing */
    private static final boolean PRINT_LINK_CROSSCAP = false;
    static final private boolean PRINT_LINK_FACE_ALL=false;
    static final private boolean PRINT_FIND_EDGE_SOL=false;
    static final private boolean PRINT_GEN_BOXES=false;
    static final private boolean VERBOUSE=false;
    static final private boolean PRINT_LINK_FACE=false;
    static final private boolean PRINT_LINK_NODES=false;
    static final private boolean PRINT_CONVERGE=false;
    static final private boolean PRINT_LINKFACE04=false;
    static final private boolean PRINT_LINK_SING=false;
    private static final boolean PRINT_SING = false;

    //	static final int MAX_EDGE_LEVEL=1048576; /* 32768 */
    //	static final int LINK_FACE_LEVEL = 512, LINK_SING_LEVEL = 128, RESOLUTION = 8;
    //	static final int SUPER_FINE = 8192;
    static final int MAX_EDGE_LEVEL= 32768;
    //	static final int LINK_FACE_LEVEL = 128, LINK_SING_LEVEL = 16, RESOLUTION = 8;
    //	static final int SUPER_FINE = 1024;
//    static int LINK_FACE_LEVEL;
//    static int LINK_SING_LEVEL;
//    static int RESOLUTION;
//    static int SUPER_FINE;



    static final int MODE_KNOWN_SING = 1;

    /*********************** Start of Sub-routines **************************/
    /*									*/
    /* draws an arbitrary polynomial curve in a specified region of space   */
    /*									*/
    /*********************** Start of Sub-routines **************************/




    public Boxclev2(PgElementSet outSurf, PgPolygonSet outCurve,
            PgPointSet outPoints, int coarse, int fine, int face, int edge,boolean draw_lines) {
        facets = new Facets(this);
        plotter = new PlotJavaview(this,outSurf,outCurve,outPoints,draw_lines);
        Face_info.allFaces.clear();
        RESOLUTION = coarse;
        LINK_SING_LEVEL = fine;
        LINK_FACE_LEVEL = face;
        SUPER_FINE = edge;

        System.out.printf("BOXCLEV %d %d %d %d %b\n", coarse, fine, face, edge,draw_lines);
    }

    /** Entry point 
     * @throws AsurfException **/

    @Override
    public boolean marmain(
            double aa[][][],
            Region_info region) throws AsurfException
            {
        boolean flag;

        printInput(aa,region);
        globalRegion = region;

        BB = new Bern3D(aa,region);
        CC = BB.diffX();
        DD = BB.diffY();
        EE = BB.diffZ();
        Dxx = CC.diffX();
        Dxy = CC.diffY();
        Dxz = CC.diffZ();

        Dyy = DD.diffY();
        Dyz = DD.diffZ();

        Dzz = EE.diffZ();

        //	  if(global_mode == MODE_KNOWN_SING)
        //		  calc_known_sings(pl,num_pts);
        if(VERBOUSE){
            System.out.printf("Initial polynomial\n");
            System.out.println(aa);
            System.out.println(region);
            System.out.printf("Bernstein polynomial is:\n");
            System.out.println(BB);
        }
        whole_box = new Box_info(0,0,0,1);
        Topology.whole_box = whole_box;

        Runtime rt = Runtime.getRuntime();
        rt.gc();
        long t1 = System.currentTimeMillis();
        System.out.printf("start mem %,d%n",rt.totalMemory()-rt.freeMemory());
        plotter.initPlotter();
        flag = generate_boxes(whole_box,BB);
        plot_and_clear(whole_box,true,true,true);
        plotter.finiPlotter();
        rt.gc();
        long t2 = System.currentTimeMillis();
        System.out.printf("time %d mem %,d%n",t2-t1,rt.totalMemory()-rt.freeMemory());

        //Face_info.print_keys();
        Face_info.allFaces.clear();
        plotter.clear();
        whole_box = null;
        plotter = null;
        this.facets = null;
        rt.gc();
        System.out.printf("Clear mem %,d%n",rt.totalMemory()-rt.freeMemory());
        
        //	  free_box(whole_box);
        //	  free_bern3D(BB);
        //	  free_bern3D(CC);
        //	  free_bern3D(DD);
        //	  free_bern3D(EE);
        //	  flag = flag && !check_interupt("Writing Data");
        //	  fini_berns();
        //	  fini_cells();
        return(flag);
            }


    /*	The main routine for the first pass of the algorithim.		*/
    /*	This recursivly creates a tree of boxes where each box contains */
    /*	eight smaller boxes, only those boxes where there might be a	*/
    /*	solution are considered (i.e. !allonesign ).			*/
    /*	The recursion ends when the none of the derivatives have	*/
    /*	solutions, and a set depth has been reach or when 		*/
    /*	a greater depth has been reached.				*/
    /*									*/

    
    boolean generate_boxes(Box_info box,Bern3D bb) throws AsurfException
    {
        int xl,yl,zl,denom;
        double percent = 0.0;
        boolean flag;
        String string;

        xl = box.xl; yl = box.yl; zl = box.zl;
        for( denom = box.denom;denom>1;denom /= 2)
        {
            percent += (xl%2);
            percent /= 2.0;
            xl /= 2;
            percent += (yl%2);
            percent /= 2.0;
            yl /= 2;
            percent += (zl%2);
            percent /= 2.0;
            zl /= 2;
        }
        string = String.format("Done %6.2f percent.",percent*100.0);
        if( check_interupt( string ) ) return(false);

        if(global_denom == box.denom)
        {
            if(global_selx != -1) if(box.xl != global_selx) return true;
            if(global_sely != -1) if(box.yl != global_sely) return true;
            if(global_selz != -1) if(box.zl != global_selz) return true;
        }

        if( bb.allOneSign() ) /* no component in box */
        {
            if(PRINT_GEN_BOXES){
                System.out.printf("generate_boxes: box (%d,%d,%d)/%d no conponant\n",
                        box.xl,box.yl,box.zl,box.denom);
            }
            box.status = EMPTY;
            return(true);
        }

        /*** If all derivatives non zero and the region is sufficiently	***/
        /***  small then draw the surface.				***/

        if( box.denom >= RESOLUTION )
        {
            if(PRINT_GEN_BOXES){
                System.out.printf("generate_boxes: box (%d,%d,%d)/%d LEAF\n",
                        box.xl,box.yl,box.zl,box.denom);
            }

            return(find_box(box,bb));
        }
        else
        {		/**** Sub-divide the region into 8 sub boxes.  ****/
            if(PRINT_GEN_BOXES){
                System.out.printf("generate_boxes: box (%d,%d,%d)/%d NODE\n",
                        box.xl,box.yl,box.zl,box.denom);
            }

            Bern3D.OctBern temp = bb.reduce();
            box.sub_devide_box();
            
            boolean f1 = generate_boxes(box.lfd,temp.lfd); temp.lfd=null;
            if(!f1) return false;
            boolean f2 = generate_boxes(box.rfd,temp.rfd); temp.rfd=null;
            if(!f2) return false;
            boolean f3 = generate_boxes(box.lbd,temp.lbd); temp.lbd=null;
            if(!f3) return false;
            boolean f4 = generate_boxes(box.lfu,temp.lfu); temp.lfu=null;
            if(!f4) return false;
            plot_and_clear(box.lfd,true,true,true); box.lfd=null;

            boolean f5 = generate_boxes(box.rbd,temp.rbd); temp.rbd=null;
            if(!f5) return false;
            boolean f6 = generate_boxes(box.rfu,temp.rfu); temp.rfu=null;
            if(!f6) return false;
            plot_and_clear(box.rfd,false,true,true);
            
            boolean f7 = generate_boxes(box.lbu,temp.lbu); temp.lbu=null;
            if(!f7) return false;
            plot_and_clear(box.lbd,true,false,true);
            plot_and_clear(box.lfu,true,true,false);
            
            boolean f8 = generate_boxes(box.rbu,temp.rbu); temp.rbu=null;
            if(!f8) return false;
            plot_and_clear(box.rbd,false,false,true);
            plot_and_clear(box.rfu,false,true,false);
            plot_and_clear(box.lbu,true,false,false);
            plot_and_clear(box.rbu,false,false,false);
            
            flag = f1 && f2 && f3 && f4 && f5 && f6 && f7 && f8;

            return(flag);
        }
    }

    private void plot_all_boxes(Box_info box) {
        if( box.denom >= RESOLUTION )
        {
            facets.make_little_facets(box);
            if(!check_sane_facets(box)) {
                System.out.print(box);
                System.out.print(box.facets);
            }
            draw_box(box);
        }
        else 
        {
            if(box.lfd!=null) { plot_all_boxes(box.lfd); }
            if(box.lfu!=null) { plot_all_boxes(box.lfu);  }
            if(box.lbd!=null) { plot_all_boxes(box.lbd); }        
            if(box.lbu!=null) { plot_all_boxes(box.lbu); }
            if(box.rfd!=null) { plot_all_boxes(box.rfd); }
            if(box.rfu!=null) { plot_all_boxes(box.rfu); }
            if(box.rbd!=null) { plot_all_boxes(box.rbd); }
            if(box.rbu!=null) { plot_all_boxes(box.rbu);  }
        }

    }

    private void plot_bits(Box_info box,boolean rr, boolean bb, boolean uu) {

        if( box.denom >= RESOLUTION )
        {
            if(!rr && !bb && ! uu) {


                facets.make_little_facets(box);
                if(!check_sane_facets(box)) {
                    System.out.print(box);
                    System.out.print(box.facets);
                }
                //if(box.xl==5 && box.yl==5 && box.zl==2 )
                //                      if(box.xl==4 && box.yl==4 && box.zl==0 )
                //if(box.xl==1 && box.yl==4 && box.zl==4 )
                draw_box(box);

            }
        }
        else 
        {
            if(box.lfd!=null) { plot_bits(box.lfd,false,false,false); }
            if(box.lfu!=null) { plot_bits(box.lfu,false,false,uu);  }
            if(box.lbd!=null) { plot_bits(box.lbd,false,bb,false); }        
            if(box.rfd!=null) { plot_bits(box.rfd,rr,false,false); }
            if(box.lbu!=null) { plot_bits(box.lbu,false,bb,uu); }
            if(box.rfu!=null) { plot_bits(box.rfu,rr,false,uu); }
            if(box.rbd!=null) { plot_bits(box.rbd,rr,bb,false); }
            if(box.rbu!=null) { plot_bits(box.rbu,rr,bb,uu);  }
        }
    }

    /**
     * 
     * @param box
     * @param rr whether right face is complete and can be plotted
     * @param bb
     * @param uu
     */
    private void plot_and_clear_old(Box_info box,boolean rr, boolean bb, boolean uu) {

    	if( box.denom >= RESOLUTION )
    	{
    		if(rr && bb && uu) {


    			facets.make_little_facets(box);
    			if(!check_sane_facets(box)) {
    				System.out.print(box);
    				System.out.print(box.facets);
    			}
    			//if(box.xl==5 && box.yl==5 && box.zl==2 )
    				//                      if(box.xl==4 && box.yl==4 && box.zl==0 )
    			//if(box.xl==1 && box.yl==4 && box.zl==4 )
    			draw_box(box);
    		}
    	}
    	else 
    	{
    		if(box.lfd!=null) { 
    			plot_bits(box.lfd,true,true,true); 
    			box.lfd = null; 
    			if(box.lfd!=null) { 
    				plot_bits(box.lfd,true,true,true); 
    				box.lfd = null; 
    			}
    			if(box.rfd!=null) { 
    				plot_bits(box.rfd,rr,true,true);
    				if(rr)
    					box.rfd=null;
    			}

    			if(box.lbd!=null) {
    				plot_bits(box.lbd,true,bb,true);
    				if(bb)
    					box.lbd=null;
    			}
    			if(box.lfu!=null) { 
    				plot_bits(box.lfu,true,true,uu); 
    				if(uu)
    					box.lfu=null;
    			}
    			if(box.rbd!=null) {
    				plot_bits(box.rbd,rr,bb,true);
    				if(rr && bb) box.rbd = null;
    			}
    			if(box.rfu!=null) {
    				plot_bits(box.rfu,rr,true,uu);
    				if(rr && uu) box.rfu=null;

    			}
    			if(box.lbu!=null) {
    				plot_bits(box.lbu,true,bb,uu); 
    				if(bb && uu) box.lbu=null;
    			}
    			if(box.rbu!=null) { 
    				plot_bits(box.rbu,rr,bb,uu);  
    				if(rr && bb && uu) box.rbu=null;
    			}
    		}

    		box.chains = null;
    		box.sings = null;
    		box.facets = null;
    		box.node_links = null;
    		box.ll=null;
    		if(rr) box.rr = null;
    		box.ff=null;
    		if(bb) box.bb = null;
    		box.dd=null;
    		if(uu) box.uu = null;
    	}

    }

    /**
     * 
     * @param box
     * @param rr whether right face is complete and can be plotted
     * @param bb
     * @param uu
     */
    public void plot_and_clear(Box_info box,boolean rr, boolean bb, boolean uu) {
    	if(!rr && box.xl+1 == box.denom)
    		rr = true;
    	if(!bb && box.yl+1 == box.denom)
    		bb = true;
    	if(!uu && box.zl+1 == box.denom)
    		uu = true;
    	
    	
    	boolean flag=true;
        if(box.lfd!=null) { 
        	plot_and_clear(box.lfd,true,true,true); 
            box.lfd = null;
            flag=false;
        }
        if(box.rfd!=null) { 
        	plot_and_clear(box.rfd,rr,true,true);
            if(rr)
                box.rfd=null;
            flag=false;
        }

        if(box.lbd!=null) {
        	plot_and_clear(box.lbd,true,bb,true);
            if(bb)
                box.lbd=null;
            flag=false;
        }
        if(box.lfu!=null) { 
        	plot_and_clear(box.lfu,true,true,uu); 
            if(uu)
                box.lfu=null;
            flag=false;
        }
        if(box.rbd!=null) {
        	plot_and_clear(box.rbd,rr,bb,true);
            if(rr && bb) box.rbd = null;
            flag=false;
        }
        if(box.rfu!=null) {
        	plot_and_clear(box.rfu,rr,true,uu);
            if(rr && uu) box.rfu=null;
            
            flag=false;
            }
        if(box.lbu!=null) {
        	plot_and_clear(box.lbu,true,bb,uu); 
            if(bb && uu) box.lbu=null;
            flag=false;
            }
        if(box.rbu!=null) { 
        	plot_and_clear(box.rbu,rr,bb,uu);  
            if(rr && bb && uu) box.rbu=null;
            flag=false;
            }

            if(rr && bb && uu) {

            	if( flag )
            	{
//            		if(box.xl==8 && box.yl==7 && box.zl ==4) {
//            			System.out.println(box.print_box_header());
//            			System.out.println("HI");
//            		}
            		facets.make_little_facets(box);
            		if(!check_sane_facets(box)) {
            			//                	System.out.println("check_sane_facets failed");
            			//                    System.out.print(box);
            			//                    System.out.print(box.facets);
            		}

            		//if(box.xl==5 && box.yl==5 && box.zl==2 )
            		//                      if(box.xl==4 && box.yl==4 && box.zl==0 )
            		//if(box.xl==1 && box.yl==4 && box.zl==4 )
            		draw_box(box);

            	}
                
                if(FACEHASH) {
                	Face_info.remove(box.ll);
                	Face_info.remove(box.ff);
                	Face_info.remove(box.dd);
                	if(box.xl+1==box.denom)
                		Face_info.remove(box.rr);
                	if(box.yl+1==box.denom)
                		Face_info.remove(box.bb);
                	if(box.zl+1==box.denom)
                		Face_info.remove(box.uu);
                	box.ll = box.rr = box.bb = box.ff = box.bb = box.dd = box.uu = null;
                }
                else
                {
                	box.ll=null;
                	if(rr) box.rr = null;
                	box.ff=null;
                	if(bb) box.bb = null;
                	box.dd=null;
                	if(uu) box.uu = null;
                }
                box.chains = null;
                box.sings = null;
                box.facets = null;
                box.node_links = null;
          
        }
    }

    /**
     * Finds all solutions, nodes and singularities for a box
     *		together with the topological linkage information.
     * @return false if a need to quit through interupt etc 
     */


    boolean find_box(Box_info box,Bern3D bb) throws AsurfException
    {
//    	if(box.xl!=8 || box.yl!=6 || box.zl !=4) 

    	
//    	if(box.xl==8 && box.yl==6 && box.zl ==4) 
//    		System.out.println(box.print_box_header());
//
//		if(box.xl <7 ||box.xl> 9 || box.yl<6 || box.yl>8 || box.zl < 3 || box.zl > 5) 
//    		return true;

        find_all_faces(box,bb);
//        if(box.xl==4 && box.yl==4 && box.zl==0 ) {
//            System.out.println(box);
//        }
        if( !link_box(box,bb) ) return(false);
        box.status = FOUND_EVERYTHING ;

        if(!PLOT_BITS) {
            if(!global_lf)
            {
                if(LITTLE_FACETS)
                    facets.make_little_facets(box);
                else
                    facets.make_facets(box);
                if(!check_sane_facets(box)) {
                    System.out.print(box);
                    System.out.print(box.facets);
                }
                //if(box.xl==5 && box.yl==5 && box.zl==2 )
                //			if(box.xl==4 && box.yl==4 && box.zl==0 )
                //if(box.xl==1 && box.yl==4 && box.zl==4 )
                if(box.xl==4 && box.yl==4 && box.zl==0 ) {
                    System.out.println(box);
                }

                draw_box(box);
            }
            //	#ifdef DO_FREE
            free_bits_of_box(box);
            //	}
        }
        
        return(true);
    }

    private void free_bits_of_box(Box_info box) {
        box.free_bits_of_box();
    }

    private boolean check_sane_facets(Box_info box) {
        double pos[];
        if(box.facets!=null) {
            for(Facet_info f:box.facets) {
                Facet_sol s = f.sols;
                while(s!=null) {
                    pos = box.calc_pos_in_box(s.sol);
                    if(pos[0]<0.0 || pos[0]> 1.0 ||
                            pos[1]<0.0 || pos[1]> 1.0 ||
                            pos[2]<0.0 || pos[2]> 1.0)
                        return false;
                    s=s.next;
                }
            }
        }
        return true;
    }

    /*
     * Function:	find_all_faces
     * action:	for all the faces of the box find the information
     *		about the solutions and nodes.
     *		takes information already found about faces of adjacent
     *		boxes.
     */

    void find_all_faces( Box_info box, Bern3D bb) throws AsurfException
    {
    	if(FACEHASH)
    		Topology2.get_existing_faces(box);
    	else
    		Topology.get_existing_faces(box);
        Topology.create_new_faces(box);

        /* none of the faces are internal */

        find_face(box,bb,box.ll,FACE_LL,false);
        find_face(box,bb,box.rr,FACE_RR,false);
        find_face(box,bb,box.ff,FACE_FF,false);
        find_face(box,bb,box.bb,FACE_BB,false);
        find_face(box,bb,box.dd,FACE_DD,false);
        find_face(box,bb,box.uu,FACE_UU,false);
    }


    /*
     * Function:	find_face
     * action:	find all the information about solutions and nodes on face.
     */

    void find_face( Box_info box, Bern3D bb, Face_info face,Key3D code,boolean internal) throws AsurfException
    {
        Bern2D aa,dx,dy,dz,d2 = null;
        Bern3D temp,temp2;

        if(face.status == FOUND_EVERYTHING ) return;
        aa = bb.make_bern2D_of_box(code);
        if( aa.allOneSign()!=0 )
        {
            face.status = FOUND_EVERYTHING;
            //	free_bern2D(aa);
            return;
        }
        if(face.type == FACE_LL || face.type == FACE_RR)
        {
            temp = bb.diffX();
            dx = temp.make_bern2D_of_box(code);
            if(USE_2ND_DERIV){
                if(dx.allOneSign()==0)
                {
                    temp2 = temp.diffX();
                    d2  = temp2.make_bern2D_of_box(code);
                    //free_bern3D(temp2);
                }
            }
            //free_bern3D(temp);
        }
        else
            dx = aa.diffX();

        if(face.type == FACE_FF || face.type == FACE_BB)
        {
            temp = bb.diffY();
            dy = temp.make_bern2D_of_box(code);
            if(USE_2ND_DERIV){
                if(dx.allOneSign()==0)
                {
                    temp2 = temp.diffY();
                    d2  = temp2.make_bern2D_of_box(code);
                    //free_bern3D(temp2);
                }
            }
            //free_bern3D(temp);
        }
        else if(face.type == FACE_LL || face.type == FACE_RR)
            dy = aa.diffX();
        else
            dy = aa.diffY();

        if(face.type == FACE_UU || face.type == FACE_DD)
        {
            temp = bb.diffZ();
            dz = temp.make_bern2D_of_box(code);
            if(USE_2ND_DERIV){
                if(dz.allOneSign()==0)
                {
                    temp2 = temp.diffZ();
                    d2  = temp2.make_bern2D_of_box(code);
                    //free_bern3D(temp2);
                }
            }
            //free_bern3D(temp);
        }
        else
            dz = aa.diffY();

        find_all_edges(box,face,aa,dx,dy,dz,code);

        link_face(face,face,aa,dx,dy,dz,d2,internal);
        if(FACETS) {
            face.colect_nodes();
        } else {
            if( !internal ) face.colect_nodes();
        }
        face.status = FOUND_EVERYTHING;
        //free_bern2D(dx);
        //free_bern2D(dy);
        //free_bern2D(dz);
        //free_bern2D(aa);
    }



    /*
     * Function:	find_all_edges
     * action:	finds all the solutions on the edges of a face.
     *		uses the information already found from adjacient faces.
     */

    private void find_all_edges(Box_info box, Face_info face, Bern2D bb,
            Bern2D dx, Bern2D dy, Bern2D dz, Key3D code) throws AsurfException {
        get_existing_edges(box,face,code);
        Topology.create_new_edges(face);
        find_edge(face.x_low,bb,dx,dy,dz,Face_info.X_LOW);
        find_edge(face.x_high,bb,dx,dy,dz,Face_info.X_HIGH);
        find_edge(face.y_low,bb,dx,dy,dz,Face_info.Y_LOW);
        find_edge(face.y_high,bb,dx,dy,dz,Face_info.Y_HIGH);
    }

    /*
     * Function:	find_edge
     * action:	finds all the solutions on an edge.
     */

    private void find_edge(Edge_info edge, Bern2D bb, Bern2D dx, Bern2D dy,
            Bern2D dz, int code) throws AsurfException {
        Bern1D aa,dx1,dy1,dz1;

        if( edge.status == FOUND_EVERYTHING ) return;
        aa = bb.make_bern1D_of_face(code);
        dx1 = dx.make_bern1D_of_face(code);
        dy1 = dy.make_bern1D_of_face(code);
        dz1 = dz.make_bern1D_of_face(code);
        find_sols_on_edge(edge,aa,dx1,dy1,dz1);
        edge.status = FOUND_EVERYTHING;
        //		free_bern1D(aa);
        //		free_bern1D(dx1);
        //		free_bern1D(dy1);
        //		free_bern1D(dz1);
    }


    /*
     * Function:	find_sols_on_edge
     * action:	finds all the solutions on the edge
     */

    void find_sols_on_edge(Edge_info edge,Bern1D bb,Bern1D dx,Bern1D dy,Bern1D dz) throws AsurfException
    {
        double vall,valm;
        double rootl,rooth,rootm,res;
        long level;
        int f1,f2,f3;
        double vec[] = new double[3];

        edge.status = FOUND_EVERYTHING;
        if( bb.allOneSign()!=0 ) return;

        f1 = dx.allOneSign();
        f2 = dy.allOneSign();
        f3 = dz.allOneSign();

        if( ( f1==0 || f2==0 || f3==0 ) && edge.denom < SUPER_FINE )
        {
            Bern1D.BinBern aa,dx1,dy1,dz1;

            aa = bb.reduce();
            if( f1 > 0 )      dx1 = Bern1D.posBern1D.reduce();
            else if( f1 < 0 ) dx1 = Bern1D.negBern1D.reduce();
            else if(edge.type == Key3D.X_AXIS)
                dx1 = aa.binDiff1D();
            else		  dx1 = dx.reduce();

            if( f2 > 0 )      dy1 = Bern1D.posBern1D.reduce();
            else if( f2 < 0 ) dy1 = Bern1D.negBern1D.reduce();
            else if(edge.type == Key3D.Y_AXIS)
                dy1 = aa.binDiff1D();
            else		  dy1 = dy.reduce();

            if( f3 > 0 )      dz1 = Bern1D.posBern1D.reduce();
            else if( f3 < 0 ) dz1 = Bern1D.negBern1D.reduce();
            else if(edge.type == Z_AXIS)
                dz1 = aa.binDiff1D();
            else		  dz1 = dz.reduce();

            Edge_info edges[]=edge.subdevideedge();
            edge.left = edges[0];
            edge.right = edges[1];


            find_sols_on_edge(edge.left, aa.l, dx1.l,dy1.l,dz1.l);
            find_sols_on_edge(edge.right,aa.r, dx1.r,dy1.r,dz1.r);

            if( aa.r.coeff[0] == 0.0 )
            {
                edge.sol = new Sol_info(edge.type,edge.xl,
                        edge.yl,edge.zl,edge.denom, 0.5 );
                edge.sol.dx = f1;
                edge.sol.dy = f2;
                edge.sol.dz = f3;
            }
            //			free_binbern1D(aa);
            //			free_binbern1D(dx1);
            //			free_binbern1D(dy1);
            //			free_binbern1D(dz1);
            return;
        }

        /*** Either a simple interval or at bottom of tree ***/

        if( ( f1!=0 && f2!=0 && f3!=0 ) || bb.allOneSignDeriv() )
        {
            /*** A simple interval ***/
            vall = bb.coeff[0];
            rootl = 0.0;
            rootm = 0.5;
            rooth = 1.0;
            level = edge.denom;
            while( level <= MAX_EDGE_LEVEL )
            {
                level *= 2;
                rootm = (rootl +rooth) * 0.5;
                valm = bb.evaluate(rootm);
                if((vall<0) != (valm<0)) rooth = rootm;
                else
                {
                    vall = valm; 
                    rootl = rootm;
                }
            }
        }
        else
        {
            //	if(USE_STURM){
            //			//extern int calc_sterm_root(bern1D *bb,double roots[MAXORDER]);
            //
            //			double roots[MAXORDER];
            //			int num,i,calcnum;
            //
            //			System.out.printf("BAD EDGE: ");
            //			print_edge(edge);
            //			printbern1D_normal(bb);
            //			System.out.printf("\n");
            //
            //			num = calc_sterm_root(bb,roots);
            //			calcnum = 0;
            //			if(num==0) return;
            //			System.out.printf("ROOTS: ");
            //			for(i=0;i<num;++i)
            //			{
            //				System.out.printf("%g ",roots[i]);
            //				System.out.printf("\n");
            //				if(roots[i]>=0.0 && roots[i]<=1.0) ++calcnum;
            //			}		
            //			
            //			if(calcnum>2)
            //			{
            //				System.out.printf("More than 2 sturm sols on the edge %d\n",num);
            //				return;
            //			}
            //			if(calcnum==2)
            //			{
            //				edge.left = grballoc(edge_info);
            //				edge.right = grballoc(edge_info);
            //				make_edge(edge.left,edge.type,edge.xl,edge.yl,edge.zl,edge.denom);
            //				make_edge(edge.right,edge.type,edge.xl,edge.yl,edge.zl,edge.denom);
            //			}
            //		
            //			for(i=0;i<num;++i)
            //			{
            //				sol_info sol;
            //				if(roots[i]<0.0 || roots[i]>1.0) continue;
            //
            //				sol = make_sol(edge.type,edge.xl,edge.yl,edge.zl,
            //					edge.denom, roots[i] );
            //				sol.dx = f1;
            //				sol.dy = f2;
            //				sol.dz = f3;
            //				if( !f1 )
            //				{
            //					res = evalbern3D(CC,vec);
            //					if( res < 0 ) sol.dx = -1;
            //					if( res > 0 ) sol.dx = 1;
            //				}
            //				if( !f2 )
            //				{
            //					res = evalbern3D(DD,vec);
            //					if( res < 0 ) sol.dy = -1;
            //					if( res > 0 ) sol.dy = 1;
            //				}
            //				if( !f3 )
            //				{
            //					res = evalbern3D(EE,vec);
            //					if( res < 0 ) sol.dz = -1;
            //					if( res > 0 ) sol.dz = 1;
            //				}
            //				if(calcnum==1) edge.sol = sol;
            //				else if(i==0) edge.left.sol = sol;
            //				else if(i==1) edge.right.sol = sol;
            //			}
            //			return;
            //	}
            if(NON_GENERIC_EDGE){
                if( (bb.coeff[0]) * (bb.coeff[bb.xord]) > 0 ) return;
            }
            rootm = BAD_EDGE;
        }


        edge.sol = new Sol_info(edge.type,edge.xl,edge.yl,edge.zl,
                edge.denom, rootm );
        edge.sol.dx = f1;
        edge.sol.dy = f2;
        edge.sol.dz = f3;

        if( f1==0 || f2==0 || f3==0 )
        {
            /*** Can't work out derivatives easily ***/
            /*** use actual values ***/

            vec = edge.sol.calc_pos();
            if( f1==0 )
            {
                res = CC.evalbern3D(vec);
                if( res < 0 ) edge.sol.dx = -1;
                if( res > 0 ) edge.sol.dx = 1;
            }
            if( f2==0 )
            {
                res = DD.evalbern3D(vec);
                if( res < 0 ) edge.sol.dy = -1;
                if( res > 0 ) edge.sol.dy = 1;
            }
            if( f3==0 )
            {
                res = EE.evalbern3D(vec);
                if( res < 0 ) edge.sol.dz = -1;
                if( res > 0 ) edge.sol.dz = 1;
            }

            if(PRINT_FIND_EDGE_SOL) {
                System.out.printf("find_sol_on_edge: f1 %d f2 %d f3 %d\n",f1,f2,f3);
                System.out.print(edge.sol);
            }
        }
        return;
    }


    //	static int TestSignNum = 0;

    boolean Test4nodesLike011(Node_info nodes[],int count,int order[])
    {
        int i,j,num_match;

        for(i=0;i<count;++i)
        {
            if( (nodes[i].sol.dx == 0 && nodes[i].sol.dy == 0 )
                    || (nodes[i].sol.dx == 0 && nodes[i].sol.dz == 0 )
                    || (nodes[i].sol.dy == 0 && nodes[i].sol.dz == 0 ) )
                continue;
            if( nodes[i].sol.dx != 0 && nodes[i].sol.dy != 0 && nodes[i].sol.dz != 0)
                continue;
            num_match = 1;
            order[0] = i;
            for(j=i+1;j<count;++j)
            {
                if( nodes[j].sol.dx == nodes[i].sol.dx
                        && nodes[j].sol.dy == nodes[i].sol.dy
                        && nodes[j].sol.dz == nodes[i].sol.dz )
                {
                    order[num_match++] = j;
                    if(num_match == 4) return true;
                }
            }
        }
        return false;
    }

    boolean PairTest(Sol_info sols[],int a,int b) {
        return
        sols[a].dx == sols[b].dx 
        && sols[a].dy == sols[b].dy 
        && sols[a].dz == sols[b].dz; }

    boolean MatchDeriv(Sol_info sols[],int a,int f1,int f2,int f3) {
        return sols[a].dx == f1 
        && sols[a].dy == f2 
        && sols[a].dz == f3; }


    boolean StraddleDeriv(Sol_info sols[],int a,int b,int f1,int f2,int f3) {
        return (  f1 !=0 
                || ( sols[a].dx == 1 && sols[b].dx == -1 ) 
                || ( sols[a].dx == -1 && sols[b].dx == 1 ) ) 
                && (  f2 !=0 
                        || ( sols[a].dy == 1 && sols[b].dy == -1 ) 
                        || ( sols[a].dy == -1 && sols[b].dy == 1 ) ) 
                        && (  f3 !=0
                                || ( sols[a].dz == 1 && sols[b].dz == -1 ) 
                                || ( sols[a].dz == -1 && sols[b].dz == 1 ) )  ; }


    private Face_info SusFace2(DerivContext dc,Bern2D bb) throws AsurfException {
        Face_info q_face;

        q_face = new Face_info(dc.face.type,dc.face.xl,dc.face.yl,dc.face.zl, 
                dc.face.denom); 
        Topology.create_new_edges(q_face); 
        find_edge(q_face.x_low,bb,dc.dx,dc.dy,dc.dz,Face_info.X_LOW); 
        find_edge(q_face.x_high,bb,dc.dx,dc.dy,dc.dz,Face_info.X_HIGH); 
        find_edge(q_face.y_low,bb,dc.dx,dc.dy,dc.dz,Face_info.Y_LOW);  
        find_edge(q_face.y_high,bb,dc.dx,dc.dy,dc.dz,Face_info.Y_HIGH); 
        return q_face;
    }

    static class CalcCrossRes {
        double pos_x,pos_y;
        boolean DerivFlag;
    }
    private CalcCrossRes CalcCross(Face_info face,Sol_info a_sols[],Sol_info b_sols[]) {
        double vec0[] = face.calc_pos_on_face(a_sols[0]); 
        double vec1[] = face.calc_pos_on_face(a_sols[1]); 
        double vec2[] = face.calc_pos_on_face(b_sols[0]); 
        double vec3[] = face.calc_pos_on_face(b_sols[1]); 

        double lam = -( (vec3[1]-vec2[1])*(vec0[0]-vec2[0]) 
                -(vec3[0]-vec2[0])*(vec0[1]-vec2[1]) ) 
                /( (vec3[1]-vec2[1]) * (vec1[0]-vec0[0]) 
                        -(vec3[0]-vec2[0]) * (vec1[1]-vec0[1]) ); 

        if( Double.isNaN( lam) ) 
        { 
            return null;
        } 
        else if( lam >= 0.0 && lam <= 1.0 ) 
        { 
            CalcCrossRes ccr = new CalcCrossRes();
            ccr.pos_x = lam * vec1[0] + (1.0-lam)*vec0[0]; 
            ccr.pos_y = lam * vec1[1] + (1.0-lam)*vec0[1]; 
            ccr.DerivFlag = false; 
            return ccr;
        }
        return null; }

    static class DerivContext {
        Face_info face;
        boolean DerivFlag;
        Bern2D dx, dy, dz;
    }
    private void DerivTest(DerivContext dc,Face_info a_face,Bern2D da,int fa,
            Face_info b_face,Bern2D db,int fb
    ) throws AsurfException {
        int a_count=0,b_count=0;
        Sol_info a_sols[]=new Sol_info[2],b_sols[]=new Sol_info[2];
        if( dc.DerivFlag && fa==0 && fb==0 
                && ( da.xord != 0 || da.yord != 0 ) 
                && ( db.xord != 0 || db.yord != 0 ) ) 
        { 
            if(a_face==null) {
                a_face = SusFace2(dc,da); 
                a_count = Topology.get_sols_on_face(a_face,a_sols);
                if( a_count == 2 ) 
                { 
                    if(b_face==null) {
                        b_face = SusFace2(dc,db); 
                        b_count = Topology.get_sols_on_face(b_face,b_sols);
                    }
                    if( b_count == 2 ) 
                    {	CalcCross(dc.face,a_sols,b_sols); } 
                    else if( b_count != 0 ) dc.DerivFlag = false; 
                } 
                else if( a_count != 0 ) dc.DerivFlag = false; 
            }
        }
    }

    void calc_2nd_derivs(Sol_info sol,Bern2D dx,Bern2D dy,Bern2D dz,Bern2D d2) throws AsurfException
    {
        Bern2D dxx=null,dxy=null,dxz=null,dyy=null,dyz=null,dzz=null;

        System.out.printf("Calc 2nd derivs\n");
        if(sol.type == FACE_LL || sol.type == FACE_RR)
        { /* s=y, t = z */
            dxx = d2;
            dxy = dx.diffX(); /* dyx */
            dxz = dx.diffY(); /* dzx */
            dyy = dy.diffX();
            dyz = dy.diffY(); /* dzy */
            dzz = dz.diffY();
        }
        else if(sol.type == FACE_FF || sol.type == FACE_BB)
        { /* s=x, t = z */
            dxx = dx.diffX();
            dxy = dy.diffX(); 
            dxz = dz.diffX(); /* dz dx */
            dyy = d2;
            dyz = dy.diffY(); /* dz dy */
            dzz = dz.diffY();
        }
        else if(sol.type == FACE_UU || sol.type == FACE_DD)
        {
            dxx = dx.diffX();
            dxy = dy.diffX(); /* dydx */
            dxz = dz.diffX(); /* dzdx */
            dyy = dy.diffY();
            dyz = dz.diffY(); /* dzdy */
            dzz = d2;
        }
        sol.dxx = dxx.allOneSign();
        sol.dxy = dxy.allOneSign();
        sol.dxz = dxz.allOneSign();
        sol.dyy = dyy.allOneSign();
        sol.dyz = dyz.allOneSign();
        sol.dzz = dzz.allOneSign();

        //					free_bern2D(dxy);
        //					free_bern2D(dxz);
        //					free_bern2D(dyz);
        //					if(sol.type != FACE_LL && sol.type != FACE_RR)
        //						free_bern2D(dxx);
        //					if(sol.type != FACE_FF && sol.type != FACE_BB)
        //						free_bern2D(dyy);
        //					if(sol.type != FACE_DD && sol.type != FACE_UU)
        //						free_bern2D(dzz);
    }

    Sol_info MakeNode(Face_info face,double pos_x,double pos_y,int f1,int f2,int f3,
            Bern2D dx,Bern2D dy,Bern2D dz,Bern2D d2) throws AsurfException
            {
        Sol_info temp;

        temp = new Sol_info(face.type,face.xl,face.yl,face.zl,face.denom,pos_x,pos_y);
        temp.dx = f1;
        temp.dy = f2;
        temp.dz = f3; 
        if( USE_2ND_DERIV){
            calc_2nd_derivs(temp,dx,dy,dz,d2);
        }
        return(temp);
            }

    static class Face_context {
        double pos_x,pos_y;
        int count;
        Face_info face;
        //Sol_info[] sols;
        double[] vec;
    }
    private void GetMid(Face_context fc) { 
        fc.pos_x = fc.pos_y = 0.0; 
        for(int i=1; i <= fc.count; ++i ) 
        { 
            Sol_info s = Topology.get_nth_sol_on_face(fc.face,i); 
            fc.vec = fc.face.calc_pos_on_face(s); 
            fc.pos_x += fc.vec[0]; 
            fc.pos_y += fc.vec[1]; 
        } 
        if( fc.count == 0 ) 
        {	fc.pos_x = fc.pos_y = 0.5; } 
        else 
        {	fc.pos_x /= fc.count; 
        fc.pos_y /= fc.count; 
        } }

    void combine_links(Face_info face)
    {
        face.links = null;

        if(face.lb.links!=null)
            for(Link_info l1:face.lb.links)
                include_link(face,l1.A,l1.B);
        if(face.lt.links!=null)
            for(Link_info l1:face.lt.links)
                include_link(face,l1.A,l1.B);
        if(face.rb.links!=null)
            for(Link_info l1:face.rb.links)
                include_link(face,l1.A,l1.B);
        if(face.rt.links!=null)
            for(Link_info l1:face.rt.links)
                include_link(face,l1.A,l1.B);

        /*
					if( 512 * face.yl == 264 * face.denom)
					{
						System.out.printf("combine_links: ");
						System.out.print(face);
					}
         */
    }


    void ReduceFace(Face_info big_face,Face_info face,
            Bern2D bb,Bern2D dx,Bern2D dy,Bern2D dz,Bern2D d2,
            boolean internal,int f1,int f2,int f3) throws AsurfException
            {
        Bern2D.QuadBern b1;
        Bern2D.QuadBern dx1;
        Bern2D.QuadBern dy1;
        Bern2D.QuadBern dz1;
        Bern2D.QuadBern dd2 = null;


        b1 = bb.reduce();
        if(USE_2ND_DERIV) {
            dd2 = d2.reduce();
        }
        if( f1 > 0 )      
            dx1 = Bern2D.posBern2D.reduce();
        else if( f1 < 0 ) 
            dx1 = Bern2D.negBern2D.reduce();
        else if(face.type == FACE_LL || face.type == FACE_RR)
            dx1 = dx.reduce();
        else		  
            dx1 = b1.quadDiff2Dx();

        if( f2 > 0 )      dy1 = Bern2D.posBern2D.reduce();
        else if( f2 < 0 ) dy1 = Bern2D.negBern2D.reduce();
        else if(face.type == FACE_FF || face.type == FACE_BB)
            dy1 = dy.reduce();
        else if(face.type == FACE_LL || face.type == FACE_RR)
            dy1 = b1.quadDiff2Dx();
        else		  dy1 = b1.quadDiff2Dy();

        if( f3 > 0 )      dz1 = Bern2D.posBern2D.reduce();
        else if( f3 < 0 ) dz1 = Bern2D.negBern2D.reduce();
        else if(face.type == FACE_UU || face.type == FACE_DD)
            dz1 = dz.reduce();
        else		  dz1 = b1.quadDiff2Dy();

        Face_info[] faces= face.make_sub_faces();
        face.lb =faces[0];
        face.rb =faces[1];
        face.lt =faces[2];
        face.rt =faces[3];
        Topology.split_face(face,face.lb,face.rb,face.lt,face.rt);

        find_edge(face.lb.x_high,b1.lb,dx1.lb,dy1.lb,dz1.lb,Face_info.X_HIGH);
        find_edge(face.lb.y_high,b1.lb,dx1.lb,dy1.lb,dz1.lb,Face_info.Y_HIGH);
        find_edge(face.rt.x_low,b1.rt,dx1.rt,dy1.rt,dz1.rt,Face_info.X_LOW);
        find_edge(face.rt.y_low,b1.rt,dx1.rt,dy1.rt,dz1.rt,Face_info.Y_LOW);

        if(dd2==null) {
            link_face(big_face,face.lb,b1.lb,dx1.lb,dy1.lb,dz1.lb,null,internal);
            face.lb.status = FOUND_EVERYTHING;
            link_face(big_face,face.rb,b1.rb,dx1.rb,dy1.rb,dz1.rb,null,internal);
            face.rb.status = FOUND_EVERYTHING;
            link_face(big_face,face.lt,b1.lt,dx1.lt,dy1.lt,dz1.lt,null,internal);
            face.lt.status = FOUND_EVERYTHING;
            link_face(big_face,face.rt,b1.rt,dx1.rt,dy1.rt,dz1.rt,null,internal);
            face.rt.status = FOUND_EVERYTHING;
        }
        else
        {
            link_face(big_face,face.lb,b1.lb,dx1.lb,dy1.lb,dz1.lb,dd2.lb,internal);
            face.lb.status = FOUND_EVERYTHING;
            link_face(big_face,face.rb,b1.rb,dx1.rb,dy1.rb,dz1.rb,dd2.rb,internal);
            face.rb.status = FOUND_EVERYTHING;
            link_face(big_face,face.lt,b1.lt,dx1.lt,dy1.lt,dz1.lt,dd2.lt,internal);
            face.lt.status = FOUND_EVERYTHING;
            link_face(big_face,face.rt,b1.rt,dx1.rt,dy1.rt,dz1.rt,dd2.rt,internal);
            face.rt.status = FOUND_EVERYTHING;
        }
        if(FACETS){
            /* Now need to combine links from sub face to big face */
            combine_links(face);
        }
        //					free_quadbern2D(b1);
        //					free_quadbern2D(dx1);
        //					free_quadbern2D(dy1);
        //					free_quadbern2D(dz1);
            }

	void link_face(Face_info big_face, Face_info face, Bern2D bb, Bern2D dx,
			Bern2D dy, Bern2D dz, Bern2D d2, boolean internal)
			throws AsurfException {
		int f1, f2, f3, count;
		Sol_info sols[] = new Sol_info[5];

		if (bb.allOneSign() != 0)
			return;
		f1 = dx.allOneSign();
		f2 = dy.allOneSign();
		f3 = dz.allOneSign();

		sols[0] = sols[1] = sols[2] = sols[3] = sols[4] = null;
		count = Topology.get_sols_on_face(face, sols);

		if (PRINT_LINK_FACE_ALL) {
			System.out.printf("link_face: ");
			System.out.print(face.type);
			System.out.printf(" (%d,%d,%d)/%d count %d f1 %d f2 %d f3 %d\n",
					face.xl, face.yl, face.zl, face.denom, count, f1, f2, f3);
		}
		boolean reduce_face = false;
		if (count == 0) {
			if (f1 != 0 && f2 != 0 && f3 != 0)
				reduce_face = false;
			else
				reduce_face = true;
		} else if (count == 1)
			reduce_face = false;

		else if (count == 2) {
			if (f1 == 0 && f2 == 0 && f3 == 0)
				reduce_face = true;

			else if (PairTest(sols, 0, 1) && MatchDeriv(sols, 0, f1, f2, f3)) {
				include_link(face, sols[0], sols[1]);
				reduce_face = false;
			} else
				reduce_face = true;
		}

		else if (count == 3)
			reduce_face = true;

		else if (count == 4) {
			sols[2] = Topology.get_nth_sol_on_face(face, 3);
			sols[3] = Topology.get_nth_sol_on_face(face, 4);

			if (f1 != 0 && f2 != 0 && f3 != 0)
				reduce_face = true;
			else if (PairTest(sols, 0, 1) && PairTest(sols, 2, 3)) {
				if (PairTest(sols, 0, 2))
					reduce_face = true;
				else if (StraddleDeriv(sols, 0, 2, f1, f2, f3)) {
					include_link(face, sols[0], sols[1]);
					include_link(face, sols[2], sols[3]);
					reduce_face = false;
				} else
					reduce_face = true;
			} else if (PairTest(sols, 0, 2) && PairTest(sols, 1, 3)) {
				if (StraddleDeriv(sols, 0, 1, f1, f2, f3)) {
					include_link(face, sols[0], sols[2]);
					include_link(face, sols[1], sols[3]);
					reduce_face = false;
				} else
					reduce_face = true;
			} else if (PairTest(sols, 0, 3) && PairTest(sols, 1, 2)) {
				if (StraddleDeriv(sols, 0, 1, f1, f2, f3)) {
					include_link(face, sols[0], sols[3]);
					include_link(face, sols[1], sols[2]);
					reduce_face = false;
				} else
					reduce_face = true;
			} else
				reduce_face = true;

		} else
			reduce_face = true;

		if (reduce_face) {
			if (face.denom < LINK_FACE_LEVEL) {
				// String s = big_face.toString();
				ReduceFace(big_face, face, bb, dx, dy, dz, d2, internal, f1,
						f2, f3);
			} else {
				if (count == 0)
					link_face0sols(face, sols, bb, dx, dy, dz, d2, f1, f2, f3);
				else if (count == 2)
					link_face2sols(face, sols, bb, dx, dy, dz, d2, f1, f2, f3);
				else if (count == 3)
					link_face3sols(face, sols, bb, dx, dy, dz, d2, f1, f2, f3);
				else if (count == 4)
					link_face4sols(face, sols, bb, dx, dy, dz, d2, f1, f2, f3);
				else
					link_facemanysols(face, sols, bb, dx, dy, dz, d2, count,
							f1, f2, f3);
			}
		}
		// fini_link_face:

		return;
	}

    private void link_facemanysols(Face_info face, Sol_info[] sols, Bern2D bb2,
            Bern2D dx, Bern2D dy, Bern2D dz, Bern2D d2, int count, int f1,
            int f2, int f3) throws AsurfException {

        int i;

        sols[4] = null;
        Face_context fc = new Face_context();
        fc.count = 2; fc.face=face;
        fc.vec=new double[2];
        //fc.sols = sols;

        GetMid(fc);
        sols[4] = MakeNode(face,fc.pos_x,fc.pos_y,f1,f2,f3,dx,dy,dz,d2);
        face.add_node(sols[4]);
        if(PRINT_LINKFACE04){
            System.out.printf("link_face many sols: ");
            System.out.print(face.type);
            System.out.printf(" (%d,%d,%d)/%d count %d f1 %d f2 %d f3 %d\n",
                    face.xl,face.yl,face.zl,face.denom,
                    count,f1,f2,f3);
        }
        for(i=1;i<=count;++i)
        {
            sols[0] = Topology.get_nth_sol_on_face(face,i);
            include_link(face,sols[0],sols[4]);
        }
    }

    void link_face4sols(Face_info face,Sol_info sols[],
            Bern2D bb,Bern2D dx,Bern2D dy,Bern2D dz,Bern2D d2,
            int f1,int f2,int f3) throws AsurfException
            {
        Face_info x_face=null,y_face=null,z_face=null;
        double vec[],pos_x=0.0,pos_y=0.0;
        Bern2D dxx=null,dxy=null,dyy=null,det=null;
        int count=4,sign,order[]=new int[4];
        boolean res1;
        char signStr[]=new char[80];

        sols[2] = Topology.get_nth_sol_on_face(face,3);
        sols[3] = Topology.get_nth_sol_on_face(face,4);
        sols[4] = null;

        SignTest.BuildSolSigns(sols,4,signStr);
        if( SignTest.TestSigns(signStr,4,3,"+++|+++|++-|+--","+++|++-|+-+|+--|-++|-+-|--+|---","abc|bca|cab",order)
                || SignTest.TestSigns(signStr,4,3,"+++|+++|+-+|+--","+++|++-|+-+|+--|-++|-+-|--+|---","abc|bca|cab",order) )
        {
            System.out.printf("Node and Link\n");
            include_link(face,sols[0],sols[1]);
            vec = face.calc_pos_on_face(sols[order[2]]);
            pos_x = vec[0];
            pos_y = vec[1];
            vec=face.calc_pos_on_face(sols[order[3]]);
            pos_x += vec[0];
            pos_y += vec[1];
            sols[4] = MakeNode(face,pos_x,pos_y,f1,f2,f3,dx,dy,dz,d2);
            face.add_node(sols[4]);
            include_link(face,sols,order[2],4);
            include_link(face,sols,order[3],4);
            if(PRINT_LINKFACE04){
                System.out.print(face);
            }
            return;
        }
        else if( SignTest.TestSigns(signStr,4,3,"+++|+++|++-|+-+","+++|++-|+-+|+--|-++|-+-|--+|---","abc|bca|cab",order) )
        {
            System.out.printf("2 Nodes and a Link\n");

            include_link(face,sols,order[0],order[1]);
            vec = face.calc_pos_on_face(sols[order[2]]);
            pos_x = vec[0];
            pos_y = vec[1];
            vec=face.calc_pos_on_face(sols[order[3]]);
            pos_x += vec[0];
            pos_y += vec[1];
            sols[4] = MakeNode(face,pos_x,pos_y,f1,f2,f3,dx,dy,dz,d2);
            face.add_node(sols[4]);
            include_link(face,sols,order[2],4);
            include_link(face,sols,order[3],4);
            if(PRINT_LINKFACE04){
                System.out.print(face);
            }
            return;
        }

        switch(face.type)
        {
        case FACE_LL: case FACE_RR:
            if(f2!=0 || f3!=0) break;
            dxx = dy.diffX();
            dxy = dy.diffY();
            dyy = dz.diffY();
            break;
        case FACE_FF: case FACE_BB:
            if(f1!=0 || f3!=0) break;
            dxx = dx.diffX();
            dxy = dx.diffY();
            dyy = dz.diffY();
            break;
        case FACE_UU: case FACE_DD:
            if(f1!=0 || f2!=0) break;
            dxx = dx.diffX();
            dxy = dx.diffY();
            dyy = dy.diffY();
            break;
        }
        if(dxx!=null)
            det = Bern2D.symetricDet2D(dxx,dxy,dyy);
        if(det == null)
        {
            if(PRINT_LINKFACE04){
                System.out.printf("null det\n");
            }
            sign = 0;
        }
        else	sign = det.allOneSign();

        if(PRINT_LINKFACE04){
            System.out.printf("link4: %d %d %d %d\n",f1,f2,f3,sign);
            System.out.print(face);
        }
        if(sign>0)
        {
            link_face4solsPos(face,sols,bb,dx,dy,dz,d2,f1,f2,f3);
            return;
        }
        if(sign==0)
        {
            if(PRINT_LINKFACE04){
                System.out.printf("Zero det\n");
                System.out.print(face);
                System.out.print(bb);
                System.out.print(dx);
                System.out.print(dy);
                System.out.print(dz);
                System.out.print(dxx);
                System.out.print(dxy);
                System.out.print(dyy);
                System.out.print(det);
            }
        }
        Face_context fc=new Face_context();
        fc.count = count;
        fc.face = face;
        //fc.sols=sols;
        GetMid(fc);

        DerivContext dc = new DerivContext();
        dc.face = face;
        dc.dx = dx; dc.dy = dy; dc.dz = dz;

        if( PairTest(sols,0,1) && PairTest(sols,2,3) && !PairTest(sols,0,2) )
        {
            dc.DerivFlag = true;
            DerivTest(dc,x_face,dx,f1, y_face,dy,f2);
            DerivTest(dc,x_face,dx,f1, z_face,dz,f3);
            DerivTest(dc,y_face,dy,f2, z_face,dz,f3);
            if( dc.DerivFlag)
            {


                include_link(face,sols,0,1);
                include_link(face,sols,2,3);
                return; // goto fini_link_face;
            }
            sols[4] = MakeNode(face,pos_x,pos_y,f1,f2,f3,dx,dy,dz,d2);
            res1 = converge_node(sols[4],bb,dx,dy,dz,f1,f2,f3);
            if(!res1)
            {

                include_link(face,sols,0,1);
                include_link(face,sols,2,3);
                return; // goto fini_link_face;
            }
        }
        else if( PairTest(sols,0,2) && PairTest(sols,1,3) && !PairTest(sols,0,1) )
        {
            dc.DerivFlag = true;
            DerivTest(dc,x_face,dx,f1, y_face,dy,f2);
            DerivTest(dc,x_face,dx,f1, z_face,dz,f3);
            DerivTest(dc,y_face,dy,f2, z_face,dz,f3);
            if( dc.DerivFlag)
            {


                include_link(face,sols,0,2);
                include_link(face,sols,1,3);
                return; // goto fini_link_face;
            }
            sols[4] = MakeNode(face,pos_x,pos_y,f1,f2,f3,dx,dy,dz,d2);
            res1 = converge_node(sols[4],bb,dx,dy,dz,f1,f2,f3);
            if(!res1)
            {


                include_link(face,sols,0,2);
                include_link(face,sols,1,3);
                return; // goto fini_link_face;
            }
        }
        else if( PairTest(sols,0,3) && PairTest(sols,1,2) && !PairTest(sols,0,1) )
        {
            dc.DerivFlag = true;
            DerivTest(dc,x_face,dx,f1, y_face,dy,f2);
            DerivTest(dc,x_face,dx,f1, z_face,dz,f3);
            DerivTest(dc,y_face,dy,f2, z_face,dz,f3);
            if( dc.DerivFlag)
            {


                include_link(face,sols,0,3);
                include_link(face,sols,1,2);
                return; // goto fini_link_face;
            }
            sols[4] = MakeNode(face,pos_x,pos_y,f1,f2,f3,dx,dy,dz,d2);
            res1 = converge_node(sols[4],bb,dx,dy,dz,f1,f2,f3);
            if(!res1)
            {


                include_link(face,sols,0,3);
                include_link(face,sols,1,2);
                return; // goto fini_link_face;
            }
        }
        else
        {
            sols[4] = MakeNode(face,pos_x,pos_y,f1,f2,f3,dx,dy,dz,d2);
            res1 = converge_node(sols[4],bb,dx,dy,dz,f1,f2,f3);
        }

        /*
			if( pos_x != pos_x || pos_y != pos_y )
				System.out.printf("pos_x %f pos_y %f\n",pos_x,pos_y);
         */
        face.add_node(sols[4]);




        include_link(face,sols,0,4);
        include_link(face,sols,1,4);
        include_link(face,sols,2,4);
        include_link(face,sols,3,4);

        //fini_link_face:

        if(PRINT_LINKFACE04){
            System.out.printf("link_face4: finished DerivFalg %d res1 %d\n",dc.DerivFlag,res1);
            System.out.print(face.print_face_brief());
        }
        //			if( x_face != null ) free_face(x_face);
        //			if( y_face != null ) free_face(y_face);
        //			if( z_face != null ) free_face(z_face);
            }


    private void link_face4solsPos(Face_info face, Sol_info[] sols, Bern2D bb2,
            Bern2D dx, Bern2D dy, Bern2D dz, Bern2D d2, int f1, int f2, int f3) throws AsurfException {

        double vec[]=new double[2],pos_x=0.0,pos_y=0.0;
        int i,count=4;
        int Aind=-1,Bind=-1,Cind=-1,Dind=-1;

        sols[4] = null;
        if( PairTest(sols,0,1) ) { Aind = 0; Bind = 1; Cind = 2; Dind = 3;	}
        if( PairTest(sols,0,2) ) { Aind = 0; Bind = 2; Cind = 1; Dind = 3;	}
        if( PairTest(sols,0,3) ) { Aind = 0; Bind = 3; Cind = 1; Dind = 2;	}
        if( PairTest(sols,1,2) ) { Aind = 1; Bind = 2; Cind = 0; Dind = 3;	}
        if( PairTest(sols,1,3) ) { Aind = 1; Bind = 3; Cind = 0; Dind = 2;	}
        if( PairTest(sols,2,3) ) { Aind = 2; Bind = 3; Cind = 0; Dind = 2;	}
        if(Aind != -1)
        {
            include_link(face,sols,Aind,Bind);
            if( PairTest(sols,Cind,Dind) )
            {
                include_link(face,sols,Cind,Dind);
                return;
            }
            pos_x = pos_y = 0.0;
            vec = face.calc_pos_on_face(sols[Cind]);
            pos_x += vec[0];
            pos_y += vec[1];
            vec = face.calc_pos_on_face(sols[Dind]);
            pos_x += vec[0];
            pos_y += vec[1];
            sols[4] = MakeNode(face,pos_x,pos_y,f1,f2,f3,dx,dy,dz,d2);
            face.add_node(sols[4]);
            include_link(face,sols,2,4);
            include_link(face,sols,3,4);
            return;
        }

        /* None of the point match */
        /* Test we have (1,1) (1,-1) (-1,1) (-1,-1) */

        Aind = Bind = Cind = Dind = -1;
        for(i=0;i<4;++i)
            switch(face.type)
            {
            case FACE_LL: case FACE_RR:
                if(sols[i].dy ==  1 && sols[i].dz ==  1) Aind = i;
                if(sols[i].dy ==  1 && sols[i].dz == -1) Bind = i;
                if(sols[i].dy == -1 && sols[i].dz ==  1) Cind = i;
                if(sols[i].dy == -1 && sols[i].dz == -1) Dind = i;
                break;
            case FACE_FF: case FACE_BB:
                if(sols[i].dx ==  1 && sols[i].dz ==  1) Aind = i;
                if(sols[i].dx ==  1 && sols[i].dz == -1) Bind = i;
                if(sols[i].dx == -1 && sols[i].dz ==  1) Cind = i;
                if(sols[i].dx == -1 && sols[i].dz == -1) Dind = i;
                break;
            case FACE_DD: case FACE_UU:
                if(sols[i].dx ==  1 && sols[i].dy ==  1) Aind = i;
                if(sols[i].dx ==  1 && sols[i].dy == -1) Bind = i;
                if(sols[i].dx == -1 && sols[i].dy ==  1) Cind = i;
                if(sols[i].dx == -1 && sols[i].dy == -1) Dind = i;
                break;
            }
        if(Aind != -1 && Bind != -1 && Cind != -1 && Dind != -1 )
        {
            /* Now a nicly behaved example */
            /* I think all sols should be on two oposite edges */
            if(sameEdge(sols[Aind],sols[Bind]) && sameEdge(sols[Cind],sols[Dind]))
            {
                if(PRINT_LINKFACE04){
                    System.out.printf("link4+ AB CD: %d %d %d %d\n",Aind,Bind,Cind,Dind);
                }
                pos_x = pos_y = 0.0;
                vec= face.calc_pos_on_face(sols[Aind]);
                pos_x += vec[0];
                pos_y += vec[1];
                vec= face.calc_pos_on_face(sols[Cind]);
                pos_x += vec[0];
                pos_y += vec[1];
                sols[4] = MakeNode(face,pos_x,pos_y,f1,f2,f3,dx,dy,dz,d2);
                if(sols[Aind].dx != 0 && sols[Aind].dx == sols[Cind].dx)
                    sols[4].dx = sols[Aind].dx;
                if(sols[Aind].dy != 0 && sols[Aind].dy == sols[Cind].dy)
                    sols[4].dy = sols[Aind].dy;
                if(sols[Aind].dz != 0 && sols[Aind].dz == sols[Cind].dz)
                    sols[4].dz = sols[Aind].dz;

                face.add_node(sols[4]);
                include_link(face,sols,Aind,4);
                include_link(face,sols,Cind,4);
                pos_x = pos_y = 0.0;
                vec= face.calc_pos_on_face(sols[Bind]);
                pos_x += vec[0];
                pos_y += vec[1];
                vec= face.calc_pos_on_face(sols[Dind]);
                pos_x += vec[0];
                pos_y += vec[1];

                sols[4] = MakeNode(face,pos_x,pos_y,f1,f2,f3,dx,dy,dz,d2);

                if(sols[Bind].dx != 0 && sols[Bind].dx == sols[Dind].dx)
                    sols[4].dx = sols[Bind].dx;
                if(sols[Bind].dy != 0 && sols[Bind].dy == sols[Dind].dy)
                    sols[4].dy = sols[Bind].dy;
                if(sols[Bind].dz != 0 && sols[Bind].dz == sols[Dind].dz)
                    sols[4].dz = sols[Bind].dz;

                face.add_node(sols[4]);
                include_link(face,sols,Bind,4);
                include_link(face,sols,Dind,4);
                if( PRINT_LINKFACE04){
                    System.out.print(face);
                }
                return;
            }
            else if(sameEdge(sols[Aind],sols[Cind]) && sameEdge(sols[Bind],sols[Dind]))
            {
                if(PRINT_LINKFACE04){
                    System.out.printf("link4+ AC BD: %d %d %d %d\n",Aind,Bind,Cind,Dind);
                }
                pos_x = pos_y = 0.0;
                vec= face.calc_pos_on_face(sols[Aind]);
                pos_x += vec[0];
                pos_y += vec[1];
                vec= face.calc_pos_on_face(sols[Bind]);
                pos_x += vec[0];
                pos_y += vec[1];
                sols[4] = MakeNode(face,pos_x,pos_y,f1,f2,f3,dx,dy,dz,d2);

                if(sols[Aind].dx != 0 && sols[Aind].dx == sols[Bind].dx)
                    sols[4].dx = sols[Aind].dx;
                if(sols[Aind].dy != 0 && sols[Aind].dy == sols[Bind].dy)
                    sols[4].dy = sols[Aind].dy;
                if(sols[Aind].dz != 0 && sols[Aind].dz == sols[Bind].dz)
                    sols[4].dz = sols[Aind].dz;

                face.add_node(sols[4]);
                include_link(face,sols,Aind,4);
                include_link(face,sols,Bind,4);
                pos_x = pos_y = 0.0;
                vec= face.calc_pos_on_face(sols[Cind]);
                pos_x += vec[0];
                pos_y += vec[1];
                vec= face.calc_pos_on_face(sols[Dind]);
                pos_x += vec[0];
                pos_y += vec[1];
                sols[4] = MakeNode(face,pos_x,pos_y,f1,f2,f3,dx,dy,dz,d2);

                if(sols[Cind].dx != 0 && sols[Cind].dx == sols[Dind].dx)
                    sols[4].dx = sols[Cind].dx;
                if(sols[Cind].dy != 0 && sols[Cind].dy == sols[Dind].dy)
                    sols[4].dy = sols[Cind].dy;
                if(sols[Cind].dz != 0 && sols[Cind].dz == sols[Dind].dz)
                    sols[4].dz = sols[Cind].dz;

                face.add_node(sols[4]);
                include_link(face,sols,Cind,4);
                include_link(face,sols,Dind,4);
                return;
            }
        }
        if(PRINT_LINKFACE04){
            System.out.printf("linkFace4Pos: odd sols not in expected posn\n");
            System.out.print(face);
        }

        Face_context fc=new Face_context();
        fc.count = count;
        fc.face = face;
        //fc.sols=sols;
        GetMid(fc);

        sols[4] = MakeNode(face,pos_x,pos_y,f1,f2,f3,dx,dy,dz,d2);
        face.add_node(sols[4]);
        include_link(face,sols,0,4);
        include_link(face,sols,1,4);
        include_link(face,sols,2,4);
        include_link(face,sols,3,4);
    }			

    void link_face0sols(Face_info face,Sol_info sols[],
            Bern2D bb,Bern2D dx,Bern2D dy,Bern2D dz,Bern2D d2,
            int f1,int f2,int f3) throws AsurfException
            {
        DerivContext dc = new DerivContext();
        dc.face = face;
        dc.dx = dx; dc.dy = dy; dc.dz = dz;
        Face_info x_face=null,y_face=null,z_face=null;
        //Sol_info x_sols[],y_sols[],z_sols[];
        //int x_count=0, y_count=0, z_count=0;
        double pos_x=0.0,pos_y=0.0;
        int sign;
        Bern2D det,dxx,dxy,dyy;

        if(PRINT_LINKFACE04) {
            System.out.printf("link0: %d %d %d\n",f1,f2,f3);
            System.out.print(face);
        }
        sols[3] = sols[4] = null;

        switch(face.type)
        {
        case FACE_LL: case FACE_RR:
            if(f2!=0 || f3!=0) return;
            dxx = dy.diffX();
            dxy = dy.diffY();
            dyy = dz.diffY();
            break;
        case FACE_FF: case FACE_BB:
            if(f1!=0 || f3!=0) return;
            dxx = dx.diffX();
            dxy = dx.diffY();
            dyy = dz.diffY();
            break;
        case FACE_UU: case FACE_DD:
            if(f1!=0 || f2!=0) return;
            dxx = dx.diffX();
            dxy = dx.diffY();
            dyy = dy.diffY();
            break;
        default:
            return;
        }
        det = Bern2D.symetricDet2D(dxx,dxy,dyy);
        if(det == null)
        {
            System.out.printf("null det\n");
            System.out.printf("link_face0sols: %d %d %d\n",f1,f2,f3);
            System.out.print(face);
            System.out.print(dx);
            System.out.print(dy);
            System.out.print(dz);
            System.out.print(dxx);
            System.out.print(dxy);
            System.out.print(dyy);
            sign = 0;
        }
        else
            sign = det.allOneSign();
        if(sign<0) return;

        dc.DerivFlag = true;
        DerivTest(dc,x_face,dx,f1, y_face,dy,f2);
        DerivTest(dc,x_face,dx,f1, z_face,dz,f3);
        DerivTest(dc,y_face,dy,f2, z_face,dz,f3);

        //			DerivTest(dc,x_face,x_count,x_sols,dx,f1, y_face,y_count,y_sols,dy,f2);
        //			DerivTest(dc,x_face,x_count,x_sols,dx,f1, z_face,z_count,z_sols,dz,f3);
        //			DerivTest(dc,y_face,y_count,y_sols,dy,f2, z_face,z_count,z_sols,dz,f3);

        if( dc.DerivFlag ) 
        {
            if(PRINT_LINKFACE04) {
                System.out.printf("DerivFlag %d\n",dc.DerivFlag);
            }
            return;
        }
        /*
			if( pos_x != pos_x || pos_y != pos_y )
				System.out.printf("pos_x %f pos_y %f\n",pos_x,pos_y);
         */
        if(pos_x == 0.0 || pos_x == 1.0 || pos_y == 0.0 || pos_y == 1.0)
        {
            if(PRINT_LINKFACE04) {
                System.out.printf("Pos on boundary %f %f\n",pos_x,pos_y);
            }
            return;
        }
        sols[4] = MakeNode(face,pos_x,pos_y,f1,f2,f3,dx,dy,dz,d2);
        boolean flag = converge_node(sols[4],bb,dx,dy,dz,1,1,1);
        if(!flag)
        {
            if(PRINT_LINKFACE04) {
                System.out.printf("conv_failed\n");
            }
        }
        else
            face.add_node(sols[4]);

        if(PRINT_LINKFACE04) {
            System.out.printf("link_face: count %d f1 %d f2 %d f3 %d\n",
                    0,f1,f2,f3);
            System.out.print(sols[4]);
            /*
			System.out.print(x_face);
			System.out.print(y_face);
			System.out.print(z_face);
             */
        }
            }



    private boolean  converge_node(Sol_info sol,
            Bern2D bb,Bern2D dx,Bern2D dy,Bern2D dz,
            int signDx,int signDy,int signDz) throws AsurfException
            {
        double vec[] = new double[2],oldvec[] = new double[2];
        Bern2D bb_x,bb_y;
        Bern2D dx_x=null,dx_y=null, dy_x=null,dy_y=null, dz_x=null, dz_y=null;
        int i;
        boolean flag=true;
        double sumsq,a,b,val;
        CN_context cn = new CN_context();
        cn.bb = bb; 
        cn.dx = dx;
        cn.dy = dy;
        cn.dz = dz;
        cn.signDx = signDx;
        cn.signDy = signDy;
        cn.signDz = signDz;
        cn.vec = vec;
        cn.sol = sol;

        bb_x = bb.diffX(); bb_y = bb.diffY();
        if(signDx==0) { dx_x = dx.diffX(); dx_y = dx.diffY(); }
        if(signDy==0) { dy_x = dy.diffX(); dy_y = dy.diffY(); }
        if(signDz==0) { dz_x = dz.diffX(); dz_y = dz.diffY(); }

        if(PRINT_CONVERGE){
            System.out.printf("converge_node: ");
            System.out.print(sol);
            /* System.out.print(bb); */
            System.out.printf("init"); PRINT_CN_VEC(cn);
        }	
        vec[0] = sol.root;
        vec[1] = sol.root2;

        for(i=0;i<10;++i)
        {
            oldvec[0] = vec[0];
            oldvec[1] = vec[1];

            /* first converge onto surface */

            val = bb.evalbern2D(vec);
            b   = bb_y.evalbern2D(vec);
            a   = bb_x.evalbern2D(vec);

            /*		vec[0] -= val / a;		
			if(PRINT_CONVERGE){
			System.out.printf("a %d",i); PRINT_CN_VEC();
			}	
             */

            /*		vec[1] -= val / b;
             */
            sumsq = a * a + b * b;
            vec[0] -= val * a / sumsq;
            vec[1] -= val * b / sumsq;

            if(PRINT_CONVERGE){
                System.out.printf("b %d",i); PRINT_CN_VEC(cn);
            }	

            if( vec[0] != vec[0] )
            {
                System.out.printf("NaN in converge_node\n");
                PRINT_CN_VEC(cn);
                System.out.print(sol);
                vec[0] = oldvec[0];
                vec[1] = oldvec[1];
                flag = false;
                break;		
            }

            if( 
                    vec[0] < 0.0 
                    ||	vec[0] > 1.0 
                    ||	vec[1] < 0.0 	
                    ||	vec[1] > 1.0 )
            {
                if(PRINT_CONVERGE){
                    System.out.printf("failed"); PRINT_CN_VEC(cn);
                }	
                vec[0] = oldvec[0];
                vec[1] = oldvec[1];
                flag = false;
                break;		
            }

            if(PRINT_CONVERGE){
                System.out.printf("%d",i); PRINT_CN_VEC(cn);
            }	

            /* then converge onto dx */

            if(signDx==0)
            {
                val = dx.evalbern2D(vec);
                a   = dx_x.evalbern2D(vec);
                b   = dx_y.evalbern2D(vec);
                sumsq = a * a + b * b;
                vec[0] -= val * a / sumsq;
                vec[1] -= val * b / sumsq;

                if(PRINT_CONVERGE){
                    System.out.printf("dx"); PRINT_CN_VEC(cn);
                }	
            }

            if(signDy==0)
            {
                val = dy.evalbern2D(vec);
                a   = dy_x.evalbern2D(vec);
                b   = dy_y.evalbern2D(vec);
                sumsq = a * a + b * b;
                vec[0] -= val * a / sumsq;
                vec[1] -= val * b / sumsq;

                if(PRINT_CONVERGE){
                    System.out.printf("dy"); PRINT_CN_VEC(cn);
                }	
            }

            if(signDz==0)
            {
                val = dz.evalbern2D(vec);
                a   = dz_x.evalbern2D(vec);
                b   = dz_y.evalbern2D(vec);
                sumsq = a * a + b * b;
                vec[0] -= val * a / sumsq;
                vec[1] -= val * b / sumsq;
                if(PRINT_CONVERGE){
                    System.out.printf("dz"); PRINT_CN_VEC(cn);
                }	
            }

            if( vec[0] != vec[0] )
            {
                System.out.printf("NaN in converge_node\n");
                PRINT_CN_VEC(cn);
                System.out.print(sol);
                vec[0] = oldvec[0];
                vec[1] = oldvec[1];
                flag = false;
                break;		
            }

            if( 
                    vec[0] < 0.0 
                    ||	vec[0] > 1.0 
                    ||	vec[1] < 0.0 	
                    ||	vec[1] > 1.0 )
            {
                if(PRINT_CONVERGE){
                    System.out.printf("failed"); PRINT_CN_VEC(cn);
                }	
                vec[0] = oldvec[0];
                vec[1] = oldvec[1];
                flag = false;
                break;		
            }

        }
        if(PRINT_CONVERGE){
            if(flag) System.out.printf("sucess"); PRINT_CN_VEC(cn);
        }
        sol.root = vec[0];
        sol.root2 = vec[1];

        //				free_bern2D(bb_x); free_bern2D(bb_y);
        //				if(!signDx) { free_bern2D(dx_x); free_bern2D(dx_y); }
        //				if(!signDy) { free_bern2D(dy_x); free_bern2D(dy_y); }
        //				if(!signDz) { free_bern2D(dz_x); free_bern2D(dz_y); }
        return flag;
            }


    void link_face2sols(Face_info face,Sol_info sols[],
            Bern2D bb,Bern2D dx,Bern2D dy,Bern2D dz,Bern2D d2,
            int f1,int f2,int f3) throws AsurfException
            {
        Face_context fc = new Face_context();
        fc.count = 2; fc.face=face;
        fc.vec=new double[2];
        //fc.sols=sols;
        int f1a,f2a,f3a;

        if(PRINT_LINKFACE04) {
            System.out.printf("link_face: count %d f1 %d f2 %d f3 %d\n",
                    2,f1,f2,f3);
            System.out.print(face);
        }
        sols[3] = sols[4] = null;
        if( PairTest(sols,0,1) ) { face.include_link(sols[0],sols[1]); }
        else
        {
            GetMid(fc);

            f1a = f1; f2a = f2; f3a = f3;
            if(sols[0].dx == sols[1].dx) f1a = sols[0].dx;
            if(sols[0].dy == sols[1].dy) f2a = sols[0].dy;
            if(sols[0].dz == sols[1].dz) f3a = sols[0].dz;
            /*
				f1 = f1a; f2 = f2a; f3 = f3a;
             */
            /* do we want a duplicate node */
            if( ( f1a == 0 && f2a == 0 && f3a == 0 )
                    || sols[0].dx == 0 || sols[0].dy == 0 || sols[0].dz == 0
                    || sols[1].dx == 0 || sols[1].dy == 0 || sols[1].dz == 0 )
            {
                boolean res2;

                sols[4] = MakeNode(face,fc.pos_x,fc.pos_y,f1,f2,f3,dx,dy,dz,d2);
                res2 = converge_node(sols[4],bb,dx,dy,dz,f1,f2,f3);
                face.add_node(sols[4]);
                face.include_link(sols[0],sols[4]);
                face.include_link(sols[1],sols[4]);
                if(PRINT_LINKFACE04) {
                    System.out.printf("link_face2sols: All three zero conv %d\n",res2);
                    System.out.print(face.print_face_brief());
                }
                return;
            }
            else if( ( f1a == 0 && (f2a == 0 || f3a == 0 ) ) || ( f2a == 0 && f3a == 0 ) )
            {
                boolean res1=false;
                boolean res2=false;
                double vec1[],vec2[],dist1,dist2,dist3,dist4;

                sols[3] = MakeNode(face,fc.pos_x,fc.pos_y,f1,f2,f3,dx,dy,dz,d2);
                sols[4] = MakeNode(face,fc.pos_x,fc.pos_y,f1,f2,f3,dx,dy,dz,d2);

                vec1 = face.calc_pos_on_face(sols[0]);
                vec2 = face.calc_pos_on_face(sols[1]);
                if(f1a == 0 && f2a == 0)
                {
                    res1 = converge_node(sols[3],bb,dx,dy,dz,0,1,1);
                    res2 = converge_node(sols[4],bb,dx,dy,dz,1,0,1);
                }
                if(f1a == 0 && f3a == 0)
                {
                    res1 = converge_node(sols[3],bb,dx,dy,dz,0,1,1);
                    res2 = converge_node(sols[4],bb,dx,dy,dz,1,1,0);
                }
                if(f2a == 0 && f3a == 0)
                {
                    res1 = converge_node(sols[3],bb,dx,dy,dz,1,0,1);
                    res2 = converge_node(sols[4],bb,dx,dy,dz,1,1,0);
                }
                if(!res1 || ! res2)
                {
                    System.out.printf("link_face2sols: converge failed! %d %d %d %b %b\n",f1,f2,f3,res1,res2);
                    GetMid(fc);
                    sols[4] = MakeNode(face,fc.pos_x,fc.pos_y,f1,f2,f3,dx,dy,dz,d2);
                    face.add_node(sols[4]);
                    face.include_link(sols[0],sols[4]);
                    face.include_link(sols[1],sols[4]);
                    if(PRINT_LINKFACE04) {
                    }
                    //System.out.print(face.print_face_brief());
                    return;
                }
                dist1 = Math.sqrt((vec1[0]-sols[3].root )*(vec1[0]-sols[3].root)
                        +   (vec1[1]-sols[3].root2)*(vec1[1]-sols[3].root2) );
                dist2 = Math.sqrt((vec1[0]-sols[4].root )*(vec1[0]-sols[4].root)
                        +   (vec1[1]-sols[4].root2)*(vec1[1]-sols[4].root2) );
                dist3 = Math.sqrt((vec2[0]-sols[3].root )*(vec2[0]-sols[3].root)
                        +   (vec2[1]-sols[3].root2)*(vec2[1]-sols[3].root2) );
                dist4 = Math.sqrt((vec2[0]-sols[4].root )*(vec2[0]-sols[4].root)
                        +   (vec2[1]-sols[4].root2)*(vec2[1]-sols[4].root2) );
                if(dist1 < dist2 && dist4 < dist3 )
                {
                    if(f1a == 0 && f2a == 0)
                    {
                        sols[3].dx = 0; sols[4].dx = sols[1].dx;
                        sols[3].dy = sols[0].dy; sols[4].dy = 0;
                        sols[3].dz = sols[4].dz = f3a;
                    }
                    if(f1a == 0 && f3a == 0)
                    {
                        sols[3].dx = 0; sols[4].dx = sols[1].dx;
                        sols[3].dy = sols[4].dy = f2a;
                        sols[3].dz = sols[0].dz; sols[4].dz = 0;
                    }
                    if(f2a == 0 && f3a == 0)
                    {
                        sols[3].dx = sols[4].dx = f1a;
                        sols[3].dy = 0; sols[4].dy = sols[1].dy;
                        sols[3].dz = sols[0].dz; sols[4].dz = 0;
                    }
                    face.add_node(sols[3]);
                    face.add_node(sols[4]);
                    face.include_link(sols[0],sols[3]);			
                    face.include_link(sols[3],sols[4]);			
                    face.include_link(sols[4],sols[1]);			
                }
                else if(dist1 > dist2 && dist4 > dist3 )
                {
                    if(f1a == 0 && f2a == 0)
                    {
                        sols[3].dx = 0; sols[4].dx = sols[0].dx;
                        sols[3].dy = sols[1].dy; sols[4].dy = 0;
                        sols[3].dz = sols[4].dz = f3a;
                    }
                    if(f1a == 0 && f3a == 0)
                    {
                        sols[3].dx = 0; sols[4].dx = sols[0].dx;
                        sols[3].dy = sols[4].dy = f2a;
                        sols[3].dz = sols[1].dz; sols[4].dz = 0;
                    }
                    if(f2a == 0 && f3a == 0)
                    {
                        sols[3].dx = sols[4].dx = f1a;
                        sols[3].dy = 0; sols[4].dy = sols[0].dy;
                        sols[3].dz = sols[1].dz; sols[4].dz = 0;
                    }
                    face.add_node(sols[3]);
                    face.add_node(sols[4]);
                    face.include_link(sols[1],sols[3]);			
                    face.include_link(sols[3],sols[4]);			
                    face.include_link(sols[4],sols[0]);			
                }
                else
                {
                    System.out.printf("link_face2sols: Wierd distances %f %f %f %f\n",dist1,dist2,dist3,dist4);
                    if(PRINT_LINKFACE04) {
                        System.out.print(sols[0]);
                        System.out.print(sols[1]);
                        System.out.print(sols[3]);
                        System.out.print(sols[4]);
                    }
                    sols[4] = MakeNode(face,fc.pos_x,fc.pos_y,f1,f2,f3,dx,dy,dz,d2);
                    face.add_node(sols[4]);
                    face.include_link(sols[0],sols[4]);
                    face.include_link(sols[1],sols[4]);
                }
                if(PRINT_LINKFACE04) {
                    System.out.printf("link_face2sols: added two nodes %d %d %d %d %d\n",f1,f2,f3,res1,res2);
                    System.out.print(face.print_face_brief());
                }

            }
            else
            {
                if(f1!=f1a || f2!= f2a || f3 != f3a)
                {
                    if(PRINT_LINKFACE04) {
                        System.out.printf("link_face2: default ");
                        /* change this line to fix crash f1 = f1a; f2 = f2a; f3 = f3a; */
                        System.out.printf("f1 %d %d f2 %d %d f3 %d %d\n",
                                f1,f1a,f2,f2a,f3,f3a);
                    }
                    sols[4] = MakeNode(face,fc.pos_x,fc.pos_y,f1,f2,f3,dx,dy,dz,d2);
                    face.add_node(sols[4]);
                    face.include_link(sols[0],sols[4]);
                    face.include_link(sols[1],sols[4]);
                    if(PRINT_LINKFACE04) {
                        System.out.print(face.print_face_brief());
                    }
                }
                else
                {
                    sols[4] = MakeNode(face,fc.pos_x,fc.pos_y,f1,f2,f3,dx,dy,dz,d2);
                    face.add_node(sols[4]);
                    face.include_link(sols[0],sols[4]);
                    face.include_link(sols[1],sols[4]);
                }
            }
        }
            }

    void link_face3sols(Face_info face,Sol_info sols[],
            Bern2D bb,Bern2D dx,Bern2D dy,Bern2D dz,Bern2D d2,
            int f1,int f2,int f3) throws AsurfException
            {
        Face_context fc = new Face_context();
        fc.face = face;
        //fc.sols = sols;
        fc.vec = new double[2];

        sols[2] = Topology.get_nth_sol_on_face(face,3);
        sols[3] = sols[4] = null;
        GetMid(fc);
        /*
			if( pos_x != pos_x || pos_y != pos_y )
				System.out.printf("pos_x %f pos_y %f\n",pos_x,pos_y);
         */
        sols[4] = MakeNode(face,fc.pos_x,fc.pos_y,f1,f2,f3,dx,dy,dz,d2);
        face.add_node(sols[4]);
        face.include_link(sols[0],sols[4]);
        face.include_link(sols[1],sols[4]);
        face.include_link(sols[2],sols[4]);
        if(PRINT_LINK_FACE){
            System.out.printf("link_face3sols: count %d f1 %d f2 %d f3 %d\n",
                    fc.count,f1,f2,f3);
            System.out.print(sols[4]);
            System.out.print(sols[0]);
            System.out.print(sols[1]);
            System.out.print(sols[2]);
        }

            }

    boolean sameEdge(Sol_info s1,Sol_info s2)
    {
        if(s1.type != s2.type) return false;
        switch(s1.type)
        {
        case X_AXIS:
            if( s1.yl * s2.denom == s2.yl * s1.denom
                    && s1.zl * s2.denom == s2.zl * s1.denom ) return true;
            else return false;
        case Y_AXIS:
            if( s1.xl * s2.denom == s2.xl * s1.denom
                    && s1.zl * s2.denom == s2.zl * s1.denom ) return true;
            else return false;
        case Z_AXIS:
            if( s1.xl * s2.denom == s2.xl * s1.denom
                    && s1.yl * s2.denom == s2.yl * s1.denom ) return true;
            else return false;
        default:
            return false;
        }
    }

    private boolean MatchNodes(Node_info nodes[],int a,int b) {
        return ( (nodes[a].sol.dx == nodes[b].sol.dx) 
                && (nodes[a].sol.dy == nodes[b].sol.dy) 
                && (nodes[a].sol.dz == nodes[b].sol.dz) );
    }

    /**
     * 
     * @param a
     * @param b
     * @return drue if first derives of a and b match
     */
    private boolean MatchNodes(Node_info a,Node_info b) {
        return ( (a.sol.dx == b.sol.dx) 
                && (a.sol.dy == b.sol.dy) 
                && (a.sol.dz == b.sol.dz) );
    }

    /**
     * Links a box.
     * Recurse if necessary
     * @return false on interrupt;
     */


    private boolean link_box(Box_info box,Bern3D bb) throws AsurfException
    {
        boolean f1=false,f2 = false,f3=false,doReduce;
        int count;
        List<Node_info> nodes=new ArrayList<Node_info>();
        Bern3D dx,dy,dz;
        boolean flag = true;

        if( check_interupt( null ) ) return(false);
        if( bb.allOneSign() ) {
            return(true);
        }

        dx = bb.diffX();
        dy = bb.diffY();
        dz = bb.diffZ();
        f1 = dx.allOneSign();
        f2 = dy.allOneSign();
        f3 = dz.allOneSign();

        if(f1 && f2 && f3 ) 
            count = 0;
        else
            count = Topology.get_nodes_on_box_faces(box,nodes);

        if(PRINT_LINK_NODES) {
            System.out.printf("link_nodes (%d,%d,%d)/%d: dx %d %d %d count %d\n",
                    box.xl,box.yl,box.zl,box.denom,f1,f2,f3,count);
            System.out.print(box.print_box_brief());
        }

        doReduce = true;

        if(f1 && f2 && f3 ) 
        {
            doReduce = false;
        }
        else if( count == 0 )
        {
            Sol_info sols[] = new Sol_info[2];

            /* test for isolated zeros: require !f1 !f2 !f3 and */
            /* no solutions on faces.			    */

            if( f1 || f2 || f3 ) {
                doReduce = false;
            }
            else if( Topology.get_sols_on_face(box.ll,sols) != 0
                    || Topology.get_sols_on_face(box.rr,sols) != 0
                    || Topology.get_sols_on_face(box.ff,sols) != 0
                    || Topology.get_sols_on_face(box.bb,sols) != 0
                    || Topology.get_sols_on_face(box.dd,sols) != 0
                    || Topology.get_sols_on_face(box.uu,sols) != 0)
            {
                /* non zero count return */

                doReduce = false;
            }

            /* no solutions, possible isolated zero */
        }
        else if( count == 2 )
        {
            /* Only add links whose derivs match */

            if( MatchNodes(nodes.get(0),nodes.get(1))
                    && ( nodes.get(0).sol.dx != 0 || nodes.get(0).sol.dy != 0 
                            || nodes.get(0).sol.dz != 0 ) )
            {
                box.add_node_link(nodes.get(0),nodes.get(1));
                doReduce = false;
            }
        }
        else if( count == 4 )
        {
            int i;
            /*** possible for two different node_links across box ***/

            boolean flagReduce=false;
            for(i=0;i<4;++i)
                if(nodes.get(i).sol.dx == 0 && nodes.get(i).sol.dy == 0 
                        && nodes.get(i).sol.dz == 0 )
                    flagReduce=true;

            if(flagReduce) {
            }
            else if(MatchNodes(nodes.get(0),nodes.get(1)) && MatchNodes(nodes.get(2),nodes.get(3)) )
            {
                if(MatchNodes(nodes.get(0),nodes.get(2))) 
                {
                    flagReduce=true;
                }
                else {
                    box.add_node_link(nodes.get(0),nodes.get(1));
                    box.add_node_link(nodes.get(2),nodes.get(3));
                    doReduce = false;
                }
            }

            else if(MatchNodes(nodes.get(0),nodes.get(2)) && MatchNodes(nodes.get(1),nodes.get(3)) )
            {
                if(MatchNodes(nodes.get(0),nodes.get(1))) 
                {
                    flagReduce=true;
                }
                else {
                    box.add_node_link(nodes.get(0),nodes.get(2));
                    box.add_node_link(nodes.get(1),nodes.get(3));
                    doReduce = false;
                }
            }

            else if(MatchNodes(nodes.get(0),nodes.get(3)) && MatchNodes(nodes.get(1),nodes.get(2)) )
            {
                if(MatchNodes(nodes.get(0),nodes.get(2))) 
                {
                    flagReduce=true;
                }
                else {
                    box.add_node_link(nodes.get(0),nodes.get(3));
                    box.add_node_link(nodes.get(1),nodes.get(2));
                    doReduce = false;
                }
            }
        }

        if(doReduce)
        {
            /*** Too difficult to handle, either sub-divide or create a node ***/

            if( box.denom >= LINK_SING_LEVEL )
                flag = link_node_sing(box,bb,dx,dy,dz,f1,f2,f3,count);
            else
            {
                flag = link_nodes_reduce(box,bb,dx,dy,dz);
                return(flag);
            }
        }
        //fini_nodes:
        if(PRINT_LINK_SING){
            System.out.printf("link_nodes: done %d %d %d count %d\n",f1,f2,f3,count);
            System.out.print(box.print_box_brief());
        }

        if(global_lf)
        {
            make_facets(box);
            draw_box(box);
        }

        return(flag);
    }


    boolean link_nodes_reduce(Box_info box,Bern3D bb,Bern3D dx,Bern3D dy,Bern3D dz) throws AsurfException
    {
        Bern3D.OctBern aa;
        boolean flag;

        aa = bb.reduce();
        box.sub_devide_box();
        Topology.split_box(box,box.lfd,box.rfd,box.lbd,box.rbd,
                box.lfu,box.rfu,box.lbu,box.rbu);

        find_all_faces(box.lfd,aa.lfd);
        find_all_faces(box.lfu,aa.lfu);
        find_all_faces(box.lbd,aa.lbd);
        find_all_faces(box.lbu,aa.lbu);
        find_all_faces(box.rfd,aa.rfd);
        find_all_faces(box.rfu,aa.rfu);
        find_all_faces(box.rbd,aa.rbd);
        find_all_faces(box.rbu,aa.rbu);

        box.lfd.status = FOUND_FACES;
        box.rbd.status = FOUND_FACES;
        box.rfu.status = FOUND_FACES;
        box.rfd.status = FOUND_FACES;
        box.lbu.status = FOUND_FACES;
        box.lfu.status = FOUND_FACES;
        box.lbd.status = FOUND_FACES;
        box.rbu.status = FOUND_FACES;

        flag = link_box(box.lfd,aa.lfd)
        && link_box(box.rfd,aa.rfd)
        && link_box(box.lbd,aa.lbd)
        && link_box(box.rbd,aa.rbd)
        && link_box(box.lfu,aa.lfu)
        && link_box(box.rfu,aa.rfu)
        && link_box(box.lbu,aa.lbu)
        && link_box(box.rbu,aa.rbu);

        if(FACETS) {
            facets.combine_facets(box);
        }
        //free_octbern3D(aa);
        return(flag);
    }

    private boolean link_node_sing(Box_info box, Bern3D bb2, Bern3D dx,
            Bern3D dy, Bern3D dz, boolean f1, boolean f2, boolean f3, int count) {
        return link_node_sing(box,bb2, dx,
                dy,  dz,  f1?1:0,  f2?1:0, f3?1:0,  count);
    }

    boolean link_node_sing(Box_info box,Bern3D bb,Bern3D dx,Bern3D dy,Bern3D dz,
            int f1,int f2,int f3,int count)
    {
        double pos_x,pos_y,pos_z,vec0[] = new double[3];
        Sol_info sol;
        int i,j,unmatched,order[] = new int[8],all_zero_count;
        boolean flag;
        Node_info node,midnode;
        char signStr[];

        if(PRINT_LINK_SING){
            System.out.print(box.print_box_brief());
        }

        flag = false;


        Node_info nodes[] = new Node_info[count];
        //Node_info resNodes[] = new Node_info[count];
        boolean done[]  = new boolean[count];
        int undone[]  = new int[count];
        all_zero_count = 0;
        pos_x = pos_y = pos_z = 0.0;
        for( i=0; i<count; ++i)
        {
            node = Topology.get_nth_node_on_box(box,i);
            if(node.sol.dx == 0 && node.sol.dy == 0 && node.sol.dz == 0 ) 
                ++all_zero_count;
            vec0 = box.calc_pos_in_box(node.sol);
            pos_x +=  vec0[0];
            pos_y +=  vec0[1];
            pos_z +=  vec0[2];

            nodes[i] = node;
            done[i] = false;
            undone[i] = -1;
        }
        if( count == 0 ) pos_x = pos_y = pos_z = 0.5;
        else
        {
            pos_x /= count;
            pos_y /= count;
            pos_z /= count;
        }
        if(pos_x < 0 || pos_x > 1.0 || pos_y < 0.0 || pos_y > 1.0 || pos_z < 0.0 || pos_z > 1.0 )
        {
            if(PRINT_LINK_SING){
                System.out.printf("link_sing: odd posn A %f %f %f\n",pos_x,pos_y,pos_z);
                System.out.print(box.print_box_brief());
            }
        }

        if(all_zero_count==2 && global_mode != MODE_KNOWN_SING)
        {
            flag = link_sing_many_zeros(box,bb,dx,dy,dz,f1,f2,f3,count,nodes);
            if(PRINT_LINK_SING){
                System.out.printf("Crosscap test %d\n",flag);
            }
            if(flag) return true;
        }
        if(all_zero_count==6 && global_mode != MODE_KNOWN_SING)
        {
            flag = link_node_three_planes(box,bb,dx,dy,dz,f1,f2,f3,count,nodes);
            if(flag) return true;
        }


        signStr = SignTest.BuildNodeSigns2(nodes,count);
        sol = new Sol_info(BOX,box.xl,box.yl,box.zl,box.denom,
                pos_x,pos_y,pos_z );
        sol.dx = f1;
        sol.dy = f2;
        sol.dz = f3;
        flag = converge_sing(box,sol,f1,f2,f3);
        if(PRINT_LINK_SING){
            System.out.printf("converge test %d\n",flag);
        }

        if((global_mode == MODE_KNOWN_SING )&& flag)
        {
            System.out.printf("Sing with known sings\n");
            box.add_sing(sol);

            if( count == 0 ) return true;
            midnode = new Node_info(sol,LinkStatus.NODE);
            //		#ifdef TEST_ALLOC
            //				++nodecount; ++nodemax; ++nodenew;
            //		}
            for( i=0; i<count; ++i)
            {
                box.add_node_link(midnode,nodes[i]);
            }
            return true;
        }

        /* the singularity converged to outside the box */

        if(count == 0)
        {
            if(flag)
            {
                if(sol.root < 0.0 || sol.root > 1.0 || sol.root2 < 0.0 || sol.root2 > 1.0 || sol.root3 < 0.0 || sol.root3 > 1.0 )
                {
                    System.out.printf("link_sing: odd posn B %f %f %f\n",sol.root,sol.root2,sol.root3);
                    System.out.print(box.print_box_brief());
                }
                box.add_sing(sol);

                if( count == 0 ) return true;
                midnode = new Node_info(sol,LinkStatus.NODE);
                //		#ifdef TEST_ALLOC
                //				++nodecount; ++nodemax; ++nodenew;
                //		}
                for( i=0; i<count; ++i)
                {
                    box.add_node_link(midnode,nodes[i]);
                }
            }
            return true;
        }


        if(count==4)
        {
            if( SignTest.TestSigns(signStr,4,3,"++0|+-0|+0+|+0-","+++|-++","abc|bca|cab",order)
                    || SignTest.TestSigns(signStr,4,3,"--0|+-0|0-0|0-0","+++|+-+","abc|bca|cab",order)
                    || SignTest.TestSigns(signStr,4,3,"--0|+-0|0+0|0+0","+++|+-+","abc|bca|cab",order) )
            {
                //System.out.printf("Forcing a singularity\n");
                unmatched =4;
                return force_sing(box,count,done,nodes,sol,unmatched);
            }
        }

        for(i=0;i<count;++i)
        {
            if(done[i]) continue;
            for(j=i+1;j<count;++j)
            {
                if(done[i] || done[j]) continue;
                if(MatchNodes(nodes,i,j))
                    done[i] = done[j] = true;
            }
        }
        unmatched = 0;
        for(i=0;i<count;++i)
        {
            if(!done[i]) { undone[unmatched] = i; ++unmatched; }
            done[i] = false;
        }

        if(unmatched == 3)
        {	/* if there are three unmatched then one might be degenerate */
            /* typically its (1,0,1)--(1,0,0)--(1,0,-1) */
            /* or            (0,1,-1) -- (0,1,0) -- (-1,1,0) */

            int k;
            boolean matchedX=false,matchedY=false,matchedZ=false;

            i = undone[0];
            j = undone[1];
            k = undone[2];
            /*
		System.out.printf("Linking 3 unmatched\n");
		print_sol(nodes[i].sol);
		print_sol(nodes[j].sol);
		print_sol(nodes[k].sol);
             */
            if( nodes[i].sol.dx == nodes[j].sol.dx
                    && nodes[i].sol.dx == nodes[k].sol.dx ) matchedX = true;
            if( nodes[i].sol.dy == nodes[j].sol.dy
                    && nodes[i].sol.dy == nodes[k].sol.dy ) matchedY = true;
            if( nodes[i].sol.dz == nodes[j].sol.dz
                    && nodes[i].sol.dz == nodes[k].sol.dz ) matchedZ = true;

            if(matchedX && matchedY)
            {
                if(nodes[i].sol.dz == 0)
                {		
                    box.add_node_link_simple(nodes[i],nodes[j]);
                    box.add_node_link_simple(nodes[i],nodes[k]);
                }
                else if(nodes[j].sol.dz == 0)
                {		
                    box.add_node_link_simple(nodes[i],nodes[j]);
                    box.add_node_link_simple(nodes[j],nodes[k]);
                }
                else
                {		
                    box.add_node_link_simple(nodes[i],nodes[k]);
                    box.add_node_link_simple(nodes[j],nodes[k]);
                }
                done[i] = done[j] = done[k];
                unmatched = 0;
            }
            else if(matchedX && matchedZ)
            {
                if(nodes[i].sol.dy == 0)
                {		
                    box.add_node_link_simple(nodes[i],nodes[j]);
                    box.add_node_link_simple(nodes[i],nodes[k]);
                }
                else if(nodes[j].sol.dy == 0)
                {		
                    box.add_node_link_simple(nodes[i],nodes[j]);
                    box.add_node_link_simple(nodes[j],nodes[k]);
                }
                else
                {		
                    box.add_node_link_simple(nodes[i],nodes[k]);
                    box.add_node_link_simple(nodes[j],nodes[k]);
                }
                done[i] = done[j] = done[k];
                unmatched = 0;
            }
            else if(matchedY && matchedZ)
            {
                if(nodes[i].sol.dx == 0)
                {		
                    box.add_node_link_simple(nodes[i],nodes[j]);
                    box.add_node_link_simple(nodes[i],nodes[k]);
                }
                else if(nodes[j].sol.dx == 0)
                {		
                    box.add_node_link_simple(nodes[i],nodes[j]);
                    box.add_node_link_simple(nodes[j],nodes[k]);
                }
                else
                {		
                    box.add_node_link_simple(nodes[i],nodes[k]);
                    box.add_node_link_simple(nodes[j],nodes[k]);
                }
                done[i] = done[j] = done[k];
                unmatched = 0;
            }
            else if(matchedX)
            {
                /* or            (0,1,-1) -- (0,1,0) -- (-1,1,0) */

                if(nodes[i].sol.dx != 0 && nodes[i].sol.dy == 0 && nodes[i].sol.dz == 0)
                {
                    box.add_node_link_simple(nodes[i],nodes[j]);
                    box.add_node_link_simple(nodes[i],nodes[k]);
                }				
                else if(nodes[j].sol.dx != 0 && nodes[j].sol.dy == 0 && nodes[j].sol.dz == 0)
                {
                    box.add_node_link_simple(nodes[j],nodes[i]);
                    box.add_node_link_simple(nodes[j],nodes[k]);
                }				
                else if(nodes[k].sol.dx != 0 && nodes[k].sol.dy == 0 && nodes[k].sol.dz == 0)
                {
                    box.add_node_link_simple(nodes[k],nodes[i]);
                    box.add_node_link_simple(nodes[k],nodes[j]);
                }
                else
                {
                    System.out.printf("link_sing: wierdness\n");
                    box.add_node_link_simple(nodes[i],nodes[j]);
                    box.add_node_link_simple(nodes[i],nodes[k]);
                }
                done[i] = done[j] = done[k];
                unmatched = 0;
            }
            else if(matchedY)
            {
                /* or            (0,1,-1) -- (0,1,0) -- (-1,1,0) */

                if(nodes[i].sol.dx == 0 && nodes[i].sol.dz == 0 && nodes[i].sol.dy != 0)
                {
                    box.add_node_link_simple(nodes[i],nodes[j]);
                    box.add_node_link_simple(nodes[i],nodes[k]);
                }				
                else if(nodes[j].sol.dx == 0 && nodes[j].sol.dz == 0 && nodes[j].sol.dy != 0)
                {
                    box.add_node_link_simple(nodes[j],nodes[i]);
                    box.add_node_link_simple(nodes[j],nodes[k]);
                }				
                else if(nodes[k].sol.dx == 0 && nodes[k].sol.dz == 0 && nodes[k].sol.dy != 0)
                {
                    box.add_node_link_simple(nodes[k],nodes[i]);
                    box.add_node_link_simple(nodes[k],nodes[j]);
                }
                else
                {
                    System.out.printf("link_sing: wierdness\n");
                    box.add_node_link_simple(nodes[i],nodes[j]);
                    box.add_node_link_simple(nodes[i],nodes[k]);
                }								
                done[i] = done[j] = done[k];
                unmatched = 0;
            }
            else if(matchedZ)
            {
                /* or            (0,1,-1) -- (0,1,0) -- (-1,1,0) */

                if(nodes[i].sol.dx == 0 && nodes[i].sol.dy == 0 && nodes[i].sol.dz != 0)
                {
                    box.add_node_link_simple(nodes[i],nodes[j]);
                    box.add_node_link_simple(nodes[i],nodes[k]);
                }				
                else if(nodes[j].sol.dx == 0 && nodes[j].sol.dy == 0 && nodes[j].sol.dz != 0)
                {
                    box.add_node_link_simple(nodes[j],nodes[i]);
                    box.add_node_link_simple(nodes[j],nodes[k]);
                }				
                else if(nodes[k].sol.dx == 0 && nodes[k].sol.dy== 0 && nodes[k].sol.dz != 0)
                {
                    box.add_node_link_simple(nodes[k],nodes[i]);
                    box.add_node_link_simple(nodes[k],nodes[j]);
                }
                else
                {
                    System.out.printf("link_sing: wierdness\n");
                    box.add_node_link_simple(nodes[i],nodes[j]);
                    box.add_node_link_simple(nodes[i],nodes[k]);
                }								
                done[i] = done[j] = done[k];
                unmatched = 0;
            }
            else if(nodes[i].sol.dx == 0 && nodes[i].sol.dy== 0 && nodes[i].sol.dz == 0)
            {
                System.out.printf("link_sing: matching 000\n");
                box.add_node_link_simple(nodes[i],nodes[j]);
                box.add_node_link_simple(nodes[i],nodes[k]);
                done[i] = done[j] = done[k];
                unmatched = 0;
            }			
            else if(nodes[j].sol.dx == 0 && nodes[j].sol.dy== 0 && nodes[j].sol.dz == 0)
            {
                System.out.printf("link_sing: matching 000\n");
                box.add_node_link_simple(nodes[j],nodes[i]);
                box.add_node_link_simple(nodes[j],nodes[k]);
                done[i] = done[j] = done[k];
                unmatched = 0;
            }			
            else if(nodes[k].sol.dx == 0 && nodes[k].sol.dy== 0 && nodes[k].sol.dz == 0)
            {
                System.out.printf("link_sing: matching 000\n");
                box.add_node_link_simple(nodes[k],nodes[i]);
                box.add_node_link_simple(nodes[k],nodes[j]);
                done[i] = done[j] = done[k];
                unmatched = 0;
            }			
            else
            {
                System.out.printf("No two unmatched\n");
            }
            /*
				print_box(box);
             */
        }
        if(unmatched == 0 || unmatched == 2)
        {
            boolean force_deriv_cross_sing=false;
            if( count >= 4 && Test4nodesLike011(nodes,count,order)	)
            {
                double vecs[][] = new double[4][3];
                int A,B,C,D;
                Bern3D mat=null,ddx,ddy,ddz;
                double distAB,distAC,distAD,distBC,distBD,distCD;
                double dist1x,dist1y,dist1z, dist2x,dist2y,dist2z;
                double dist3x,dist3y,dist3z, dist4x,dist4y,dist4z;
                double dist5x,dist5y,dist5z, dist6x,dist6y,dist6z;

                if(nodes[order[0]].sol.dx == 0)
                    mat = dx;
                else if(nodes[order[0]].sol.dy == 0)
                    mat = dy;
                else if(nodes[order[0]].sol.dz == 0)
                    mat = dz;

                ddx = mat.diffX();
                ddy = mat.diffY();
                ddz = mat.diffZ();
                if( !ddx.allOneSign() && !ddy.allOneSign() && !ddz.allOneSign() )
                {
                    //free_bern3D(ddx); free_bern3D(ddy); free_bern3D(ddz);
                    //System.out.printf("all derivs zero\n");
                    pos_x = pos_y = pos_z = 0.0;
                    for( i=0; i<4; ++i)
                    {
                        vec0 = box.calc_pos_in_box(nodes[order[i]].sol);
                        pos_x +=  vec0[0];
                        pos_y +=  vec0[1];
                        pos_z +=  vec0[2];
                    }
                    pos_x /= 4;
                    pos_y /= 4;
                    pos_z /= 4;
                    //System.out.printf("Calculated posn %f %f %f\n",pos_x,pos_y,pos_z);
                    sol.root = pos_x;
                    sol.root2 = pos_y;
                    sol.root3 = pos_z;
                    if(sol.root < 0.0 || sol.root > 1.0 || sol.root2 < 0.0 || sol.root2 > 1.0 || sol.root3 < 0.0 || sol.root3 > 1.0 )
                    {
                        System.out.printf("link_sing: odd posn C %f %f %f\n",sol.root,sol.root2,sol.root3);
                        System.out.print(box.print_box_brief());
                    }
                    box.add_sing(sol);

                    midnode = new Node_info(sol,LinkStatus.NODE);
                    //		if(TEST_ALLOC) {
                    //						++nodecount; ++nodemax; ++nodenew;
                    //		}
                    for( i=0; i<4; ++i)
                    {
                        box.add_node_link(midnode,nodes[order[i]]);
                        done[order[i]] = true;
                    }
                    force_deriv_cross_sing=true;
                }
                //free_bern3D(ddx); free_bern3D(ddy); free_bern3D(ddz);

                if(!force_deriv_cross_sing)
                {
                    if(SameFace(nodes[order[0]].sol,nodes[order[1]].sol))
                    {	A = order[0]; B = order[1]; C = order[2]; D = order[3];	}
                    else if(SameFace(nodes[order[0]].sol,nodes[order[2]].sol))
                    {	A = order[0]; B = order[2]; C = order[1]; D = order[3];	}
                    else if(SameFace(nodes[order[0]].sol,nodes[order[3]].sol))
                    {	A = order[0]; B = order[3]; C = order[2]; D = order[1];	}
                    else if(SameFace(nodes[order[1]].sol,nodes[order[2]].sol))
                    {	A = order[1]; B = order[2]; C = order[0]; D = order[3];	}
                    else if(SameFace(nodes[order[1]].sol,nodes[order[3]].sol))
                    {	A = order[1]; B = order[3]; C = order[0]; D = order[2];	}
                    else if(SameFace(nodes[order[2]].sol,nodes[order[3]].sol))
                    {	A = order[2]; B = order[3]; C = order[0]; D = order[1];	}
                    else
                    {
                        System.out.printf("link_sing: 4 id nodes but non on same face\n");
                        A = order[0]; B = order[1]; C = order[2]; D = order[3];
                    }
                    calc_pos_actual(nodes[A].sol,vecs[0]);
                    calc_pos_actual(nodes[B].sol,vecs[1]);
                    calc_pos_actual(nodes[C].sol,vecs[2]);
                    calc_pos_actual(nodes[D].sol,vecs[3]);
                    dist1x = vecs[0][0] - vecs[1][0]; dist1y = vecs[0][1] - vecs[1][1]; dist1z = vecs[0][2] - vecs[1][2];
                    dist2x = vecs[0][0] - vecs[2][0]; dist2y = vecs[0][1] - vecs[2][1]; dist2z = vecs[0][2] - vecs[2][2];
                    dist3x = vecs[0][0] - vecs[3][0]; dist3y = vecs[0][1] - vecs[3][1]; dist3z = vecs[0][2] - vecs[3][2];
                    dist4x = vecs[1][0] - vecs[2][0]; dist4y = vecs[1][1] - vecs[2][1]; dist4z = vecs[1][2] - vecs[2][2];
                    dist5x = vecs[1][0] - vecs[3][0]; dist5y = vecs[1][1] - vecs[3][1]; dist5z = vecs[1][2] - vecs[3][2];
                    dist6x = vecs[2][0] - vecs[3][0]; dist6y = vecs[2][1] - vecs[3][1]; dist6z = vecs[2][2] - vecs[3][2];

                    distAB = Math.sqrt(dist1x*dist1x+dist1y*dist1y+dist1z*dist1z);
                    distAC = Math.sqrt(dist2x*dist2x+dist2y*dist2y+dist2z*dist2z);
                    distAD = Math.sqrt(dist3x*dist3x+dist3y*dist3y+dist3z*dist3z);
                    distBC = Math.sqrt(dist4x*dist4x+dist4y*dist4y+dist4z*dist4z);
                    distBD = Math.sqrt(dist5x*dist5x+dist5y*dist5y+dist5z*dist5z);
                    distCD = Math.sqrt(dist6x*dist6x+dist6y*dist6y+dist6z*dist6z);
                    if(PRINT_LINK_SING){
                        System.out.printf("4 indentical nodes, distances %f %f %f %f %f %f\n",distAB,distAC,distAD,distBC,distBD,distCD);
                    }
                    /* found 4 nodes with identical sign pattern */

                    if(distAC < distBC && distBD < distAD )
                    {
                        if(PRINT_LINK_SING){
                            System.out.printf("Linking nodes %d, %d and  %d, %d\n",A,C,B,D);
                        }
                        box.add_node_link_simple(nodes[A],nodes[C]);
                        box.add_node_link_simple(nodes[B],nodes[D]);
                        done[A] = done[B] = done[C] = done[D] = true;
                    }
                    else if(distAC > distBC && distBD > distAD )
                    {
                        if(PRINT_LINK_SING){
                            System.out.printf("Linking nodes %d, %d and  %d, %d\n",A,D,B,C);
                        }
                        box.add_node_link_simple(nodes[A],nodes[D]);
                        box.add_node_link_simple(nodes[B],nodes[C]);
                        done[A] = done[B] = done[C] = done[D] = true;
                    }
                    else
                    {
                        //System.out.printf("link_sing: wierd distances\n");
                        box.add_node_link_simple(nodes[A],nodes[C]);
                        box.add_node_link_simple(nodes[B],nodes[D]);
                        done[A] = done[B] = done[C] = done[D] = true;
                    }
                }	
            }
            if(force_deriv_cross_sing) {
                //System.out.printf("At fdcs: unmatched %d\n",unmatched);
                if(NOT_DEF){
                    for(i=0;i<count;++i)
                    {
                        if(done[i]) continue;
                        for(j=i+1;j<count;++j)
                        {
                            if(done[i] || done[j]) continue;
                            if(MatchNodes(nodes,i,j))
                            {
                                if(PRINT_LINK_SING){
                                    System.out.printf("Linking nodes fdcs: %d and %d done %d %d\n",i,j,done[i],done[j]);
                                }
                                box.add_node_link_simple(nodes[i],nodes[j]);
                                done[i] = done[j] = true;
                            }
                        }
                    }
                }
            }
        }

        unmatched=0;
        for(i=0;i<count;++i)
            if(!done[i]) ++unmatched;
        if(unmatched == 0 ) return true;

        if(PRINT_LINK_SING){
            System.out.printf("unmatched %d\n",unmatched);
            System.out.print(box.print_box_brief());
        }

        for(i=0;i<count;++i)
        {
            if(done[i]) continue;
            for(j=i+1;j<count;++j)
            {
                if(done[i] || done[j]) continue;
                if( MatchNodes(nodes,i,j) 
                        && (nodes[i].sol.dx != 0 || nodes[i].sol.dy != 0 || nodes[i].sol.dz != 0 ) )
                {
                    if(PRINT_LINK_SING){
                        System.out.printf("Linking nodes fdcs: %d and %d done %d %d\n",i,j,done[i],done[j]);
                    }
                    box.add_node_link_simple(nodes[i],nodes[j]);
                    done[i] = done[j] = true;
                }
            }
        }
        unmatched=0;
        for(i=0;i<count;++i)
            if(!done[i]) ++unmatched;
        if(PRINT_LINK_SING)
            System.out.printf("unmatched %d all zero %d\n",unmatched,all_zero_count);
        if(unmatched == 0 ) return true;

        if(all_zero_count == 1)
        {

            int zero_index=0;
            double vec[]=new double[3];

            for(i=0;i<count;++i)
                if(nodes[i].sol.dx == 0 && nodes[i].sol.dy == 0 && nodes[i].sol.dz == 0 )
                    zero_index = i;
            vec = box.calc_pos_in_box(nodes[zero_index].sol);
            sol.root = vec[0]; sol.root2 = vec[1]; sol.root3 = vec[2];
            box.add_sing(sol);
            midnode = new Node_info(sol,LinkStatus.NODE);
            //if(TEST_ALLOC){
            //		++nodecount; ++nodemax; ++nodenew;
            //}
            for( i=0; i<count; ++i)
            {
                if(!done[i])
                {
                    box.add_node_link_simple(nodes[i],midnode);
                }
            }
            if(PRINT_LINK_SING){
                System.out.printf("link_sing: one all zero\n");
                System.out.print(box.print_box_brief());
            }
            return true;
        }

        /* now if converged to sing add that. */

        if(flag)
        {
            if(sol.root < 0.0 || sol.root > 1.0 || sol.root2 < 0.0 || sol.root2 > 1.0 || sol.root3 < 0.0 || sol.root3 > 1.0 )
            {
                System.out.printf("link_sing: odd posn B %f %f %f\n",sol.root,sol.root2,sol.root3);
                System.out.print(box.print_box_brief());
            }
            box.add_sing(sol);

            if( count == 0 ) return true;
            midnode = new Node_info(sol,LinkStatus.NODE);
            //		#ifdef TEST_ALLOC
            //				++nodecount; ++nodemax; ++nodenew;
            //		}
            for( i=0; i<count; ++i)
            {
                box.add_node_link_simple(midnode,nodes[i]);
            }
            return true;
        }

        /* Now lets get really hacky if there is a node (1,0,0) link it
				to all nodes (1,+/-1,0) and (1,0,+/-0) 
			   then if there is a node (0,0,0) link to all undone nodes
				and all nodes like (1,0,0) 
         */

        for(i=0;i<count;++i)
        {
            if(done[i]) continue;
            for(j=i+1;j<count;++j)
            {
                if(done[i] || done[j]) continue;
                if(MatchNodes(nodes,i,j))
                {
                    if(PRINT_LINK_SING){
                        System.out.printf("Linking nodes %d and %d done %d %d\n",i,j,done[i],done[j]);
                        box.add_node_link_simple(nodes[i],nodes[j]);
                        done[i] = done[j] = true;
                    }
                }
            }
        }

        /* Link 	(1,1,0) to (1,0,0) or (0,1,0) */

        for(i=0;i<count;++i)
        {
            if(done[i]) continue;
            if( (nodes[i].sol.dx == 0 && nodes[i].sol.dy == 0 )
                    || (nodes[i].sol.dx == 0 && nodes[i].sol.dz == 0 )
                    || (nodes[i].sol.dy == 0 && nodes[i].sol.dz == 0 ) ) continue;
            for(j=0;j<count;++j)
            {
                if(j==i) continue;
                if(nodes[j].sol.dx == 0 && nodes[j].sol.dy == 0 && nodes[j].sol.dz == 0)
                    continue;
                if(SameFace(nodes[i].sol,nodes[j].sol) ) continue;
                if(nodes[i].sol.dx == 0)
                {
                    if(nodes[j].sol.dx != 0) continue;
                    if(nodes[j].sol.dy != 0 && nodes[j].sol.dy != nodes[i].sol.dy )
                        continue;
                    if(nodes[j].sol.dz != 0 && nodes[j].sol.dz != nodes[i].sol.dz )
                        continue;
                    if(PRINT_LINK_SING){
                        System.out.printf("Linking nodes2a %d and %d done %d %d\n",i,j,done[i],done[j]);
                    }
                    box.add_node_link_simple(nodes[i],nodes[j]);
                    done[i] = done[j] = true;
                    break;
                }
                if(nodes[i].sol.dy == 0)
                {
                    if(nodes[j].sol.dy != 0) continue;
                    if(nodes[j].sol.dx != 0 && nodes[j].sol.dx != nodes[i].sol.dx )
                        continue;
                    if(nodes[j].sol.dz != 0 && nodes[j].sol.dz != nodes[i].sol.dz )
                        continue;
                    if(PRINT_LINK_SING){
                        System.out.printf("Linking nodes2b %d and %d done %d %d\n",i,j,done[i],done[j]);
                    }
                    box.add_node_link_simple(nodes[i],nodes[j]);
                    done[i] = done[j] = true;
                    break;
                }
                if(nodes[i].sol.dz == 0)
                {
                    if(nodes[j].sol.dz != 0) continue;
                    if(nodes[j].sol.dx != 0 && nodes[j].sol.dx != nodes[i].sol.dx )
                        continue;
                    if(nodes[j].sol.dy != 0 && nodes[j].sol.dy != nodes[i].sol.dy )
                        continue;
                    if(PRINT_LINK_SING){
                        System.out.printf("Linking nodes2c %d and %d done %d %d\n",i,j,done[i],done[j]);
                    }
                    box.add_node_link_simple(nodes[i],nodes[j]);
                    done[i] = done[j] = true;
                    break;
                }
            }
        }


        for(i=0;i<count;++i)
        {
            if(nodes[i].sol.dx == 0 && nodes[i].sol.dy == 0 && nodes[i].sol.dz == 0 )
            {


                for(j=0;j<count;++j)
                {
                    if(j==i) continue;
                    if( !done[j] && ! SameFace(nodes[i].sol,nodes[j].sol) )
                        /*
						 || (nodes[j].sol.dx != 0 && nodes[j].sol.dy == 0 && nodes[j].sol.dz == 0 )
						 || (nodes[j].sol.dx == 0 && nodes[j].sol.dy != 0 && nodes[j].sol.dz == 0 )
						 || (nodes[j].sol.dx == 0 && nodes[j].sol.dy == 0 && nodes[j].sol.dz != 0 ) )
                         */
                    {
                        if(PRINT_LINK_SING){
                            System.out.printf("Linking nodes3 %d and %d done %d %d\n",i,j,done[i],done[j]);
                        }
                        box.add_node_link_simple(nodes[i],nodes[j]);
                        done[i] = done[j] = true;
                    }
                }
                if( FAKE_SINGS) {
                    if(done[i])
                    {
                        box.add_sing(nodes[i].sol);
                    }
                }
            }
        }

        unmatched = 0;
        for(i=0;i<count;++i)
            if(!done[i])
            {
                if(PRINT_LINK_SING){
                    System.out.print(nodes[i]);
                }
                ++unmatched;
            }
        if(unmatched==0 || unmatched == 1) return true;
        System.out.printf("unmatched %d\n",unmatched);
        if(global_mode == MODE_KNOWN_SING) return true;

        return force_sing(box,count,done,nodes,sol,unmatched);
    }

    private boolean	force_sing(Box_info box,int count,boolean done[],Node_info nodes[],Sol_info sol,int unmatched) {
        double pos_x,pos_y,pos_z;
        double vec0[];
        //System.out.printf("force_sing: (%d,%d,%d)/%d\n",box.xl,box.yl,box.zl,box.denom);

        if(global_mode == MODE_KNOWN_SING)
            return true;

        pos_x = pos_y = pos_z = 0.0;
        for(int  i=0; i<count; ++i)
        {
            if(!done[i])
            {
                vec0 = box.calc_pos_in_box(nodes[i].sol);
                pos_x +=  vec0[0];
                pos_y +=  vec0[1];
                pos_z +=  vec0[2];
            }
        }
        pos_x /= unmatched;
        pos_y /= unmatched;
        pos_z /= unmatched;
        sol.root = pos_x;
        sol.root2 = pos_y;
        sol.root3 = pos_z;
        if(sol.root < 0.0 || sol.root > 1.0 || sol.root2 < 0.0 || sol.root2 > 1.0 || sol.root3 < 0.0 || sol.root3 > 1.0 )
        {
            System.out.printf("link_sing: odd posn C %f %f %f\n",sol.root,sol.root2,sol.root3);
            if(PRINT_LINK_SING){
                System.out.print(box.print_box_brief());
            }
        }
        box.add_sing(sol);

        Node_info midnode = new Node_info(sol,LinkStatus.NODE);
        //	if(TEST_ALLOC){
        //			++nodecount; ++nodemax; ++nodenew;
        //	}
        for(int i=0; i<count; ++i)
        {
            if(!done[i])
                box.add_node_link_simple(midnode,nodes[i]);
        }
        return true;
    }


    private boolean check_interupt(String string) {
        //		if(string!=null)
        //			System.out.println(string);
        return false;
    }





    private void include_link(Face_info face, Sol_info a, Sol_info b) {
        face.include_link(a, b);
    }

    private void include_link(Face_info face, Sol_info sols[], int a,int b) {
        include_link(face,sols[a],sols[b]);
    }

    /* All cals in resaled function */

    @Override
    void calc_pos_norm(Sol_info sol,double vec[],double norm[])
    {
        calc_pos(sol,vec);

        if(sol.dx == 0) norm[0] = 0.0;
        else             norm[0] = evalbern3D(CC,vec);
        if(sol.dy == 0) norm[1] = 0.0;
        else             norm[1] = evalbern3D(DD,vec);
        if(sol.dz == 0) norm[2] = 0.0;
        else             norm[2] = evalbern3D(EE,vec);
    }


    /** finds the pos in original domain **/
    @Override
    public void calc_pos_actual(Sol_info sol, double[] vec) {
        double v[]=sol.calc_pos();

        /* The above is of course incorrect as its been incorrectly scaled */

        vec[0] = globalRegion.xmin + v[0] * (globalRegion.xmax-globalRegion.xmin);
        vec[1] = globalRegion.ymin + v[1] * (globalRegion.ymax-globalRegion.ymin);
        vec[2] = globalRegion.zmin + v[2] * (globalRegion.zmax-globalRegion.zmin);
    }

    /** finds the pos and norm in original domain **/

    @Override
    void calc_pos_norm_actual(Sol_info sol,double vec[],double norm[])
    {
        calc_pos_norm(sol,vec,norm);

        /* The above is of corse incorrect as its been incorectly scaled */

        vec[0] = globalRegion.xmin + vec[0] * (globalRegion.xmax-globalRegion.xmin);
        vec[1] = globalRegion.ymin + vec[1] * (globalRegion.ymax-globalRegion.ymin);
        vec[2] = globalRegion.zmin + vec[2] * (globalRegion.zmax-globalRegion.zmin);

        norm[0] = norm[0] / (globalRegion.xmax-globalRegion.xmin);
        norm[1] = norm[1] / (globalRegion.ymax-globalRegion.ymin);
        norm[2] = norm[2] / (globalRegion.zmax-globalRegion.zmin);
    }


    boolean SameFace(Sol_info s1,Sol_info s2)
    {
        switch(s1.type)
        {
        case FACE_LL: case FACE_RR:
            if(s2.type != FACE_LL && s2.type != FACE_RR) return false;
            if( s1.xl * s2.denom == s2.xl * s1.denom ) return true;
            else return false;
        case FACE_FF: case FACE_BB:
            if(s2.type != FACE_FF && s2.type != FACE_BB) return false;
            if( s1.yl * s2.denom == s2.yl * s1.denom ) return true;
            else return false;
        case FACE_UU: case FACE_DD:
            if(s2.type != FACE_UU && s2.type != FACE_DD) return false;
            if( s1.zl * s2.denom == s2.zl * s1.denom ) return true;
            else return false;
        default:
            return false;
        }
    }


    boolean converge_sing(Box_info box,Sol_info sol,int signDx,int signDy,int signDz)
    {
        if(global_mode == MODE_KNOWN_SING)
            return find_known_sing(sol);
        else
            return converge_sing_old(box,sol,signDx,signDy,signDz);
    }

    boolean converge_sing_old(Box_info box,Sol_info sol,int signDx,int signDy,int signDz)
    {
        double vec[]=new double[3],oldvec[]=new double[3];
        double val,dx,dy,dz,dxx=0.0,dxy=0.0,dxz=0.0,dyy=0.0,dyz=0.0,dzz=0.0;
        int i;
        double sumsq;

        calc_pos(sol,vec);

        if(PRINT_CONVERGE){
            System.out.printf("converge_sing: ");
            System.out.print(sol);
            System.out.printf("init"); PRINT_CS_VEC();
        }	

        for(i=0;i<10;++i)
        {
            oldvec[0] = vec[0];
            oldvec[1] = vec[1];
            oldvec[2] = vec[2];

            /* first converge onto surface */

            val = BB.evalbern3D(vec);
            dx  = CC.evalbern3D(vec);
            dy  = DD.evalbern3D(vec);
            dz  = EE.evalbern3D(vec);
            sumsq = dx * dx + dy * dy + dz * dz;
            vec[0] -= val * dx / sumsq;
            vec[1] -= val * dy / sumsq;
            vec[2] -= val * dz / sumsq;

            if(PRINT_CONVERGE){
                System.out.printf("%d",i); PRINT_CS_VEC();
            }	

            /* then converge onto dx */

            if(signDx==0)
            {
                dx  = CC.evalbern3D(vec);
                dxx = Dxx.evalbern3D(vec);
                dxy = Dxy.evalbern3D(vec);
                dxz = Dxz.evalbern3D(vec);
                sumsq = dxx * dxx + dxy * dxy + dxz * dxz;
                vec[0] -= dx * dxx / sumsq;
                vec[1] -= dx * dxy / sumsq;
                vec[2] -= dx * dxz / sumsq;

                if(PRINT_CONVERGE){
                    System.out.printf("dx"); PRINT_CS_VEC();
                }	
            }


            /* then converge onto dy */

            if(signDy==0)
            {
                dy  = DD.evalbern3D(vec);
                dxy = Dxy.evalbern3D(vec);
                dyy = Dyy.evalbern3D(vec);
                dyz = Dyz.evalbern3D(vec);
                sumsq = dxy * dxy + dyy * dyy + dyz * dyz;
                vec[0] -= dy * dxy / sumsq;
                vec[1] -= dy * dyy / sumsq;
                vec[2] -= dy * dyz / sumsq;

                if(PRINT_CONVERGE){
                    System.out.printf("dy"); PRINT_CS_VEC();
                }	
            }

            /* then converge onto dz */

            if(signDz==0)
            {
                dy  = DD.evalbern3D(vec);
                dxz = Dxz.evalbern3D(vec);
                dyz = Dyz.evalbern3D(vec);
                dzz = Dzz.evalbern3D(vec);
                sumsq = dxz * dxz + dyz * dyz + dzz * dzz;
                vec[0] -= dz * dxz / sumsq;
                vec[1] -= dz * dyz / sumsq;
                vec[2] -= dz * dzz / sumsq;

                if(PRINT_CONVERGE){
                    System.out.printf("dz"); PRINT_CS_VEC();
                }	
            }

            if( vec[0] != vec[0] )
            {
                System.out.printf("NaN in converge_sing\n");
                System.out.printf("%f %f, %f %f %f, %f %f %f %f %f %f\n",
                        sumsq,val, dx,dy,dz, dxx,dxy,dxz,dyy,dyz,dzz);
                PRINT_CS_VEC();
                System.out.print(sol);
                vec[0] = oldvec[0];
                vec[1] = oldvec[1];
                vec[2] = oldvec[2];
                calc_relative_pos(sol,vec);
                return true;	/* we've found a zero! */
            }

            if( 
                    vec[0] * sol.denom - sol.xl < 0.0 
                    ||	vec[0] * sol.denom - sol.xl > 1.0 
                    ||	vec[1] * sol.denom - sol.yl < 0.0 	
                    ||	vec[1] * sol.denom - sol.yl > 1.0 	
                    ||	vec[2] * sol.denom - sol.zl < 0.0
                    ||	vec[2] * sol.denom - sol.zl > 1.0 )
            {
                if(PRINT_CONVERGE){
                    System.out.printf("converge_sing failed\n%f %f, %f %f %f, %f %f %f %f %f %f\n",
                            sumsq,val,dx,dy,dz,dxx,dxy,dxz,dyy,dyz,dzz);
                }	
                vec[0] = oldvec[0];
                vec[1] = oldvec[1];
                vec[2] = oldvec[2];
                calc_relative_pos(sol,vec);
                return false;			
            }

        }
        calc_relative_pos(sol,vec);
        if(PRINT_CONVERGE){
            System.out.printf("converge_sing done: ");
            System.out.print(sol);
        }	

        if( sol.root  < 0.0 || sol.root  > 1.0
                || sol.root2 < 0.0 || sol.root2 > 1.0
                || sol.root3 < 0.0 || sol.root3 > 1.0 )
            return false;
        else
            return true;
    }

    private boolean link_node_three_planes(Box_info box, Bern3D bb2, Bern3D dx,
            Bern3D dy, Bern3D dz, int f1, int f2, int f3, int count,
            Node_info[] nodes) 
    {
        int num_all_zero=0,i;
        double pos_x,pos_y,pos_z;
        double vec[]=new double[3];
        Sol_info sol;
        Node_info midnode;

        if(count<6) return false;
        for(i=0;i<count;++i)
        {
            if(nodes[i].sol.dx == 0 && nodes[i].sol.dy == 0 && nodes[i].sol.dz == 0 )
                ++num_all_zero;
        }
        if(num_all_zero < 6) return false;

        if(PRINT_SING){
            System.out.printf("link_three_planes sucess\n");
            System.out.print(box.print_box_brief());
        }

        pos_x = pos_y = pos_z = 0.0;
        for( i=0; i<count; ++i)
        {
            calc_pos_in_box(box,nodes[i].sol,vec);
            pos_x +=  vec[0];
            pos_y +=  vec[1];
            pos_z +=  vec[2];
        }
        pos_x /= count; pos_y /= count; pos_z /= count;

        sol = make_sol3(BOX,box.xl,box.yl,box.zl,box.denom,
                pos_x,pos_y,pos_z );
        sol.dx = 0;
        sol.dy = 0;
        sol.dz = 0;

        boolean flag = find_known_sing(sol);

        box.add_sing(sol);
        midnode = new Node_info(sol,LinkStatus.NODE);
        //		#ifdef TEST_ALLOC
        //			++nodecount; ++nodemax; ++nodenew;
        //		#endif
        for( i=0; i<count; ++i)
        {
            box.add_node_link(midnode,nodes[i]);
        }
        return true;
    }

    private boolean find_known_sing(Sol_info sol) {

        int i;
        if(global_mode != MODE_KNOWN_SING)
            return false;

        for(i=0;i<num_known_sings;++i)
        {
            if( sol.xl == known_sings[i].xl
                    && sol.yl == known_sings[i].yl
                    && sol.zl == known_sings[i].zl )
            {
                if(PRINT_CONVERGE){
                }
                System.out.printf("converge_sing: matched ");
                System.out.print(known_sings[i]);
                sol.root = known_sings[i].root;
                sol.root2 = known_sings[i].root2;
                sol.root3 = known_sings[i].root3;
                return true;
            }
        }
        return false;
    }

    private Sol_info make_sol3(Key3D key, int xl, int yl, int zl, int denom,
            double posX, double posY, double posZ) {
        return new Sol_info(key,xl,yl,zl,denom,posX,posY,posZ);
    }

    private void calc_pos_in_box(Box_info box, Sol_info sol, double[] vec) {
        double v[] = box.calc_pos_in_box(sol);
        vec[0]=v[0];vec[1]=v[1];vec[2]=v[2];

    }

    private boolean link_sing_many_zeros(Box_info box, Bern3D bb, Bern3D dx,
            Bern3D dy, Bern3D dz, int f1, int f2, int f3, int count,
            Node_info[] nodes) {

        Bern3D dxx,dxy,dxz,dyy,dyz,dzz,mat1,mat2,mat3;
        Node_info midnode;
        double vec0[]=new double[3],val,val_array[];
        short fxx,fxy,fxz,fyy,fyz,fzz;
        int i,j,unmatched;
        boolean flag;
        int sign_array[];
        int negxx, negxy, negxz, negyy, negyz, negzz;
        int posxx, posxy, posxz, posyy, posyz, poszz;
        Sol_info sol;

        if(PRINT_LINK_CROSSCAP)
            System.out.printf("link_crosscap (%d,%d,%d)/%d\n",box.xl,box.yl,box.zl,box.denom);

        dxx = diffx3D(dx); dxy = diffy3D(dx); dxz = diffz3D(dx);
        dyy = diffy3D(dy); dyz = diffz3D(dy);
        dzz = diffz3D(dz);
        fxx = signOf(dxx); fxy = signOf(dxy); fxz = signOf(dxz);
        fyy = signOf(dxx); fyz = signOf(dyz); fzz = signOf(dzz);
        sign_array = new int[count*6];
        val_array = new double[count*6];

        negxx = negxy = negxz = negyy = negyz = negzz = 0;
        posxx = posxy = posxz = posyy = posyz = poszz = 0;
        for(i=0;i<count;++i)
        {
            if(PRINT_LINK_CROSSCAP)
                print_sol(nodes[i].sol);
            /*		if(nodes[i].sol.dx == 0 && nodes[i].sol.dy == 0 && nodes[i].sol.dz == 0 )
				{
             */
            calc_pos_in_box(box,nodes[i].sol,vec0);
            if(fxx==0) {
                val = evalbern3D(dxx,vec0);
                if(val < 0.0) { negxx = 1; sign_array[i*6 + 0] = -1; }
                else if(val > 0.0) { posxx = 1; sign_array[i*6 + 0] = 1; }
                else sign_array[i*6 + 0] = 0;
                val_array[i*6 + 0] = val;
            }
            else sign_array[i*6 + 0] = 0;

            if(fxy==0) {
                val = evalbern3D(dxy,vec0);
                if(val < 0.0) { negxy = 1; sign_array[i*6 + 1] = -1; }
                else if(val > 0.0) { posxy = 1; sign_array[i*6 + 1] = 1; }
                else sign_array[i*6 + 1] = 0;
                val_array[i*6 + 1] = val;
            }
            else sign_array[i*6 + 1] = fxy;

            if(fxz==0) {
                val = evalbern3D(dxz,vec0);
                if(val < 0.0) { negxz = 1; sign_array[i*6 + 2] = -1; }
                else if(val > 0.0) { posxz = 1; sign_array[i*6 + 2] = 1; }
                else sign_array[i*6 + 2] = 0;
                val_array[i*6 + 2] = val;
            }
            else sign_array[i*6 + 2] = fxz;

            if(fyy==0) {
                val = evalbern3D(dyy,vec0);
                if(val < 0.0) { negyy = 1; sign_array[i*6 + 3] = -1; }
                else if(val > 0.0) { posyy = 1; sign_array[i*6 + 3] = 1; }
                else sign_array[i*6 + 3] = 0;
                val_array[i*6 + 3] = val;
            }
            else sign_array[i*6 + 3] = fyy;

            if(fyz==0) {
                val = evalbern3D(dyz,vec0);
                if(val < 0.0) { negyz = 1; sign_array[i*6 + 4] = -1; }
                else if(val > 0.0) { posyz = 1; sign_array[i*6 + 4] = 1; }
                else sign_array[i*6 + 4] = 0;
                val_array[i*6 + 4] = val;
            }
            else sign_array[i*6 + 4] = fyz;

            if(fzz==0) {
                val = evalbern3D(dzz,vec0);
                if(val < 0.0) { negzz = 1; sign_array[i*6 + 5] = -1; }
                else if(val > 0.0) { poszz = 1; sign_array[i*6 + 5] = 1; }
                else sign_array[i*6 + 5] = 0;
                val_array[i*6 + 5] = val;
            }
            else sign_array[i*6 + 5] = fzz;

            if(USE_2ND_DERIV){
                if(nodes[i].sol.dxx > 0) posxx = 1;
                if(nodes[i].sol.dxx < 0) negxx = 1;
                if(nodes[i].sol.dxy > 0) posxy = 1;
                if(nodes[i].sol.dxy < 0) negxy = 1;
                if(nodes[i].sol.dxz > 0) posxz = 1;
                if(nodes[i].sol.dxz < 0) negxz = 1;

                if(nodes[i].sol.dyy > 0) posyy = 1;
                if(nodes[i].sol.dyy < 0) negyy = 1;
                if(nodes[i].sol.dyz > 0) posyz = 1;
                if(nodes[i].sol.dyz < 0) negyz = 1;
                if(nodes[i].sol.dzz > 0) poszz = 1;
                if(nodes[i].sol.dzz < 0) negzz = 1;
                System.out.printf("signs %d %d %d yy %d %d %d\t",
                        nodes[i].sol.dxx,nodes[i].sol.dxy,nodes[i].sol.dxz,
                        nodes[i].sol.dyy,nodes[i].sol.dyz,nodes[i].sol.dzz);
                System.out.printf("signs %d %d %d yy %d %d %d\n",
                        sign_array[i*6+0],sign_array[i*6+1],sign_array[i*6+2],
                        sign_array[i*6+3],sign_array[i*6+4],sign_array[i*6+5]);
                System.out.printf("fxx %d %d %d yy %d %d %d\t",
                        fxx,fxy,fxz, fyy,fyz,fzz);
                System.out.printf("vals %f %f %f yy %f %f %f\n",
                        val_array[i*6+0],val_array[i*6+1],val_array[i*6+2],
                        val_array[i*6+3],val_array[i*6+4],val_array[i*6+5]);
            }
        }
        //		if(NOT_DEF){
        //			done  = (int *) malloc(sizeof(int) * count);
        //			matches  = (int *) malloc(sizeof(int) * count);
        //
        //			for( i=0; i<count; ++i)
        //			{
        //				done[i] = 0;
        //				matches[i]=-1;
        //			}
        //			for(i=0;i<count;++i)
        //				for(j=i+1;j<count;++j)
        //				{
        //					if(done[i] || done[j] ) continue;
        //
        //		#ifdef NOT_DEF
        //					if(nodes[i].sol.dx || nodes[i].sol.dy || nodes[i].sol.dz)
        //					{
        //						if( nodes[i].sol.dx == nodes[j].sol.dx
        //						 && nodes[i].sol.dy == nodes[j].sol.dy
        //						 && nodes[i].sol.dz == nodes[j].sol.dz )
        //						{
        //							matches[i] = j;
        //							done[i] = done[j] = 1;
        //						}
        //					}
        //					else if(nodes[j].sol.dx && !nodes[j].sol.dy && !nodes[j].sol.dz)
        //		#endif
        //					{
        //						/* both are all zero */
        //						int has_match=0,has_unmatch=0;
        //
        //						if( nodes[i].sol.dx != 0 || nodes[j].sol.dx != 0)
        //						{
        //						   if(nodes[i].sol.dx == nodes[j].sol.dx) 
        //							has_match=1; 
        //						   else
        //							has_unmatch=1;
        //						}
        //
        //						if( nodes[i].sol.dy != 0 || nodes[j].sol.dy != 0)
        //						{
        //						   if(nodes[i].sol.dy == nodes[j].sol.dy) 
        //							has_match=1; 
        //						   else
        //							has_unmatch=1;
        //						}
        //
        //						if( nodes[i].sol.dz != 0 || nodes[j].sol.dz != 0)
        //						{
        //						   if(nodes[i].sol.dz == nodes[j].sol.dz) 
        //							has_match=1; 
        //						   else
        //							has_unmatch=1;
        //						}
        //
        //						if( nodes[i].sol.dxx != 0 || nodes[j].sol.dxx != 0 )
        //						{
        //						   if(nodes[i].sol.dxx == nodes[j].sol.dxx) 
        //							has_match=1; 
        //						   else
        //							has_unmatch=1;
        //						}
        //						if( nodes[i].sol.dxy != 0 || nodes[j].sol.dxy != 0 )
        //						{
        //						   if(nodes[i].sol.dxy == nodes[j].sol.dxy) 
        //							has_match=1; 
        //						   else
        //							has_unmatch=1;
        //						}
        //						if( nodes[i].sol.dxz != 0 || nodes[j].sol.dxz != 0 )
        //						{
        //						   if(nodes[i].sol.dxz == nodes[j].sol.dxz) 
        //							has_match=1; 
        //						   else
        //							has_unmatch=1;
        //						}
        //						if( nodes[i].sol.dyy != 0 || nodes[j].sol.dyy != 0 )
        //						{
        //						   if(nodes[i].sol.dyy == nodes[j].sol.dyy) 
        //							has_match=1; 
        //						   else
        //							has_unmatch=1;
        //						}
        //						if( nodes[i].sol.dyz != 0 || nodes[j].sol.dyz != 0 )
        //						{
        //						   if(nodes[i].sol.dyz == nodes[j].sol.dyz) 
        //							has_match=1; 
        //						   else
        //							has_unmatch=1;
        //						}
        //
        //						if( nodes[i].sol.dzz != 0 || nodes[j].sol.dzz != 0 )
        //						{
        //						   if(nodes[i].sol.dzz == nodes[j].sol.dzz) 
        //							has_match=1; 
        //						   else
        //							has_unmatch=1;
        //
        //						}
        //						if(has_match && !has_unmatch)
        //						{
        //							matches[i] = j;
        //							done[i] = done[j] = 1;
        //						}
        //					}
        //				}
        //					
        //			unmatched = 0;
        //			for( i=0; i<count; ++i)
        //			{
        //				if(!done[i]) ++unmatched;
        //			}
        //		System.out.printf("unmatched %d\n",unmatched);
        //
        //			if(unmatched == 0)
        //			{
        //				for( i=0; i<count; ++i)
        //				{
        //					if(matches[i]!=-1)
        //						add_node_link_simple(box,nodes[i],nodes[matches[i]]);
        //				}
        //				return true;
        //			}
        //		/*		
        //			if(unmatched==2)
        //			{
        //				j = -1;
        //				for( i=0; i<count; ++i)
        //					if(!done[i]) { if(j==-1) j=i; break; }
        //				add_node_link_simple(box,nodes[i],nodes[j]);
        //			}
        //			if(unmatched<=2) return true;
        //		*/
        //		#endif

        mat1 = mat2 = mat3 = null;
        if( ( negxx!=0 && posxx!=0 ) ) mat1 = dxx;
        if( ( negxy!=0 && posxy!=0 ) ) { if(mat1!=null) mat1 = dxy; else mat2 = dxy; }
        if( ( negxz!=0 && posxz!=0 ) ) { if(mat1!=null) mat1 = dxz; else if(mat2!=null) mat2 = dxz; else mat3 = dxz; }
        if( ( negyy!=0 && posyy!=0 ) ) { if(mat1!=null) mat1 = dyy; else if(mat2!=null) mat2 = dyy; else mat3 = dyy; }
        if( ( negyz!=0 && posyz!=0 ) ) { if(mat1!=null) mat1 = dyz; else if(mat2!=null) mat2 = dyz; else mat3 = dyz; }
        if( ( negzz!=0 && poszz!=0 ) ) { if(mat1!=null) mat1 = dzz; else if(mat2!=null) mat2 = dzz; else mat3 = dzz; }
        if(mat1 != null)
        {
            sol = make_sol3(BOX,box.xl,box.yl,box.zl,box.denom,
                    0.5,0.5,0.5 );
            sol.dx = f1;
            sol.dy = f2;
            sol.dz = f3;
            flag = converge_sing2(box,sol,bb,mat1,mat2,mat3);
            if(PRINT_LINK_CROSSCAP)
                System.out.printf("link_sing_many_zeros conv %b%n",flag);
            if(!flag) return false;
            if(PRINT_LINK_CROSSCAP)
                print_sol(sol);
            if(sol.root < 0.0 || sol.root > 1.0 || sol.root2 < 0.0 || sol.root2 > 1.0 || sol.root3 < 0.0 || sol.root3 > 1.0 )
                if(PRINT_LINK_CROSSCAP)
                    System.out.printf("link_crosscap: odd posn D %f %f %f\n",sol.root,sol.root2,sol.root3);

            box.add_sing(sol);

            midnode = new Node_info(sol,LinkStatus.NODE);
            //		#ifdef TEST_ALLOC
            //			++nodecount; ++nodemax; ++nodenew;
            //		#endif
            for( i=0; i<count; ++i)
            {
                box.add_node_link_simple(midnode,nodes[i]);
            }
            if(PRINT_LINK_SING){
                if(PRINT_LINK_CROSSCAP)
                    System.out.print(box.print_box_brief());
            }
            return true;
        }
        unmatched=count;
        if(PRINT_LINK_CROSSCAP)
            System.out.printf("link_sing_many_zeros; mat1 == null count %d\n",count);
        if(unmatched==2)
        {
            box.add_node_link_simple(nodes[0],nodes[1]);
            return true;
        }
        if(unmatched==3)
        {
            box.add_node_link_simple(nodes[0],nodes[1]);
            box.add_node_link_simple(nodes[0],nodes[2]);
            box.add_node_link_simple(nodes[1],nodes[2]);
            return true;
        }
        if(unmatched==4)
        {
            boolean matchAB=true,matchAC=true,matchAD=true,matchBC=true,matchBD=true,matchCD=true;


            for(j=0;j<6;++j)
            {
                if(sign_array[0*6+j]!=sign_array[1*6+j]) matchAB=false;
                if(sign_array[0*6+j]!=sign_array[2*6+j]) matchAC=false;
                if(sign_array[0*6+j]!=sign_array[3*6+j]) matchAD=false;
                if(sign_array[1*6+j]!=sign_array[2*6+j]) matchBC=false;
                if(sign_array[1*6+j]!=sign_array[3*6+j]) matchBD=false;
                if(sign_array[2*6+j]!=sign_array[3*6+j]) matchCD=false;
            }
            if(matchAB && matchCD && !matchAC)
            {
                box.add_node_link_simple(nodes[0],nodes[1]);
                box.add_node_link_simple(nodes[2],nodes[3]);
                return true;
            }
            if(matchAC && matchBD && !matchAB)
            {
                box.add_node_link_simple(nodes[0],nodes[2]);
                box.add_node_link_simple(nodes[1],nodes[3]);
                return true;
            }
            if(matchAD && matchBC && !matchAB)
            {
                box.add_node_link_simple(nodes[0],nodes[3]);
                box.add_node_link_simple(nodes[1],nodes[2]);
                return true;
            }
            System.out.printf("link 4 with zeros failed\n");
            return false;
        }
        return false;
    }




    private boolean converge_sing2(Box_info box, Sol_info sol, Bern3D bb,
            Bern3D A, Bern3D B, Bern3D C) {


        double vec[]=new double[3],oldvec[]=new double[3];
        double val,dx,dy,dz;
        int i;
        double sumsq;
        Bern3D bbx=null,bby=null,bbz=null,Ax=null,Ay=null,Az=null,
        Bx=null,By=null,Bz=null,Cx=null,Cy=null,Cz=null;

        vec[0] = sol.root;
        vec[1] = sol.root2;
        vec[2] = sol.root3;
        if(bb!=null) { bbx = diffx3D(bb); bby = diffy3D(bb); bbz = diffz3D(bb); }
        if(A!=null) { Ax = diffx3D(A); Ay = diffy3D(A); Az = diffz3D(A); }
        if(B!=null) { Bx = diffx3D(B); By = diffy3D(B); Bz = diffz3D(B); }
        if(C!=null) { Cx = diffx3D(C); Cy = diffy3D(C); Cz = diffz3D(C); }
        if(PRINT_CONVERGE){
            System.out.printf("converge_sing2: ");
            print_sol(sol);
            System.out.printf("init"); PRINT_CS_VEC();
        }	

        for(i=0;i<10;++i)
        {
            oldvec[0] = vec[0];
            oldvec[1] = vec[1];
            oldvec[2] = vec[2];

            /* first converge onto surface */

            val = evalbern3D(bb,vec);
            dx  = evalbern3D(bbx,vec);
            dy  = evalbern3D(bby,vec);
            dz  = evalbern3D(bbz,vec);
            sumsq = dx * dx + dy * dy + dz * dz;
            vec[0] -= val * dx / sumsq;
            vec[1] -= val * dy / sumsq;
            vec[2] -= val * dz / sumsq;

            if(PRINT_CONVERGE){
                System.out.printf("%d",i); PRINT_CS_VEC();
            }	

            /* then converge onto dx */

            if(A!=null)
            {
                val = evalbern3D(A,vec);
                dx  = evalbern3D(Ax,vec);
                dy  = evalbern3D(Ay,vec);
                dz  = evalbern3D(Az,vec);
                sumsq = dx * dx + dy * dy + dz * dz;
                vec[0] -= val * dx / sumsq;
                vec[1] -= val * dy / sumsq;
                vec[2] -= val * dz / sumsq;
                if(PRINT_CONVERGE){
                    System.out.printf("dx"); PRINT_CS_VEC();
                }	
            }

            if(B!=null)
            {
                val = evalbern3D(B,vec);
                dx  = evalbern3D(Bx,vec);
                dy  = evalbern3D(By,vec);
                dz  = evalbern3D(Bz,vec);
                sumsq = dx * dx + dy * dy + dz * dz;
                vec[0] -= val * dx / sumsq;
                vec[1] -= val * dy / sumsq;
                vec[2] -= val * dz / sumsq;
                if(PRINT_CONVERGE){
                    System.out.printf("dx"); PRINT_CS_VEC();
                }	
            }

            if(C!=null)
            {
                val = evalbern3D(C,vec);
                dx  = evalbern3D(Cx,vec);
                dy  = evalbern3D(Cy,vec);
                dz  = evalbern3D(Cz,vec);
                sumsq = dx * dx + dy * dy + dz * dz;
                vec[0] -= val * dx / sumsq;
                vec[1] -= val * dy / sumsq;
                vec[2] -= val * dz / sumsq;
                if(PRINT_CONVERGE){
                    System.out.printf("dx"); PRINT_CS_VEC();
                }	
            }

            if( vec[0] != vec[0] )
            {
                System.out.printf("NaN in converge_sing2\n");
                System.out.printf("%f %f %f %f %f\n",val,dx,dy,dz,sumsq);
                print_sol(sol);
                vec[0] = oldvec[0];
                vec[1] = oldvec[1];
                vec[2] = oldvec[2];
                sol.root = vec[0];
                sol.root2 = vec[1];
                sol.root3 = vec[2];
                return false;			
            }

            if( 
                    vec[0] < 0.0 
                    ||	vec[0] > 1.0 
                    ||	vec[1] < 0.0 	
                    ||	vec[1] > 1.0 	
                    ||	vec[2] < 0.0
                    ||	vec[2] > 1.0 )
            {
                vec[0] = oldvec[0];
                vec[1] = oldvec[1];
                vec[2] = oldvec[2];
                sol.root = vec[0];
                sol.root2 = vec[1];
                sol.root3 = vec[2];
                return false;			
            }

        }
        if(PRINT_CONVERGE){
            System.out.printf("converge_sing2 done: ");
            print_sol(sol);
        }	
        sol.root = vec[0];
        sol.root2 = vec[1];
        sol.root3 = vec[2];

        if( sol.root  < 0.0 || sol.root  > 1.0
                || sol.root2 < 0.0 || sol.root2 > 1.0
                || sol.root3 < 0.0 || sol.root3 > 1.0 )
            return false;
        return true;
    }

    private void get_existing_edges(Box_info box, Face_info face, Key3D code) {
        Topology.get_existing_edges(box, face, code);
    }

    private void make_facets(Box_info box) {
        this.facets.make_facets(box);
    }
}
