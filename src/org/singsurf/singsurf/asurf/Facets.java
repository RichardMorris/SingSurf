package org.singsurf.singsurf.asurf;

import static org.singsurf.singsurf.asurf.Key3D.FACE_DD;
import static org.singsurf.singsurf.asurf.Key3D.FACE_LL;
import static org.singsurf.singsurf.asurf.Key3D.FACE_UU;
import static org.singsurf.singsurf.asurf.Key3D.NONE;
import static org.singsurf.singsurf.asurf.Key3D.X_AXIS;
import static org.singsurf.singsurf.asurf.Key3D.Z_AXIS;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Facets {

//  private static final boolean PRINT_SOLVEEDGE=false;
//  private static final boolean PLOT_AS_LINES=false;
//  private static final boolean OLD_PLOT=false;
//  private static final boolean BINARY=false;
//  private static final boolean VERBOUSE=false;
//  private static final boolean NORM_ERR=false;
//  private static final boolean PRINT_FACET=false;
//  private static final boolean SHOW_VERTICES=false;
//  private static final boolean DEBUG_FACET=false;
//  private static final boolean PLOT_POINTS=false;
//  private static final boolean PLOT_CHAINS=false;
//  private static final boolean TEST_ALLOC=false;
//  private static final boolean PRINT_LINK_FACETS=true;

    private static final boolean PLOT_SMALL_FACETS = false;
    private static final boolean PRINT_FACET_ERR=false;
    private static final boolean PRINT_COMBINE_FACETS=false;
    private static final boolean PRINT_JOIN_CHAIN_POINT=false;
    private static final boolean PRINT_DRAW_BOX=false;
    private static final boolean PRINT_REFINE=false;
    private static final boolean PRINT_JOIN_FACETS=false;
    private static final boolean PRINT_FOLLOW_CHAIN = false;
    private static final boolean PRINT_NO_MATCH_LINK = false;


//  private static final boolean PLOT_LINES=true;
//  private static final boolean PLOT_NODE_LINKS=true;
//  private static final boolean PLOT_SINGS=true;
//
//  private static final short FORWARDS=1;
//  private static final short BACKWARDS=2;
//  private static final short FOUND_EVERYTHING=2;

    /*
    #define PLOTTEDBIT 8
     */

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
	int draw_lines;
	boolean global_do_refine=true;
	int global_facet_count;
	BoxClevA boxclev;
	
	public Facets(BoxClevA boxclev) {
		super();
		this.boxclev = boxclev;
	}

	/**
	It all works as follows:

	A facet consits of an ordered set of solutions (facet_sols).
	Sols can be added to a facet at (front add_sol_to_facet)
	or at the end (add_sol_to_facet_backwards)
	A global list of facets (all_facets) is maintained.
	Facets can be added to this with (add_facet) and removed with
	(remove_facet) and thw whole list is freed up with (free_facets).
	(plot_all_facets) plots the entire set of facets.
	(print_facets) prints details of them on stderr.
	(sol_on_facet) finds whether a sol lies on a facet and returns
	the coresponding facet_sol.
	(first_sol_on_facet) takes a pair of facet_sols and returns 
	1 if the first facet_sol occurs first.

	The first real routine is (split_facet_on_chains)
	A chain is an ordered list of sols which connects nodes on the
	faces of the box to to singularities in the interior.
	This first finds if a facet contains repeated sols, if so
	the facet is split into two and the fun is recuresed on each 
	facet.
	If then finds pairs of sols on the facet which are on the faces
	of the box and connected by a chain and finds the shortest
	such chain. 
	If then checks that the chain does not form part of the boundary
	of the facet. 
	Finally if all the above is satisfied then the facet is split into
	two which share a common edge which is the chain.
	 * ---- *
		    /	     \
		   /   chain  \
	 *---*----*---*
		   \	      /
		    \	     /
	 * ---- *

	The next major routine is (join_facets_by_chain) this takes
	a pair of facets and finds a pair of chains which link the two facets
	these chains must not contain any points in common.
	When two such chains have been found two new facets are constructed
	which asumes that the facets form the oposite ends of a cylinder.
	There is a bit of a logical problem here we do not know which way
	the two facets should be conected
	either
		  a ---------- e
		 / \          / \
		d   b        h   f
		 \ /          \ /
		  c ---------- g
	or
		  a ---------- e
		 / \          / \
		d   b        f   h
		 \ /          \ /
		  c ---------- g
	it might be posible to play a bit with the normals, but we cheet
	by finding the path where the two dist a-c. This is wrong!

	(refine_facets) manages the spliting up of facets
	it starts with facets which are just bounded by edges lying on the
	facets of the box.
	First it calls (join_facets) for each pair of facets
	then if finds facets which contain the same solution twice
	and it splits them. It also duplicates any chains which start at the
	linked facet (any chain can only be used to split a facet
	once).
	Finally it calls (split_facet_on_chains) for each facet.

	The main entry point for the drawing routine is (draw_box)
	this loops through all the the links joining solutions on the
	edges and faces of the box. When it finds such a link it 
	it calls (create_facet) this repeatadly calls (get_net_link)
	which finds a link adjacent to the current one until it gets back to the
	start. There is potential bug posibilities here where more than
	one link is adjacet to a a node, hopefully cured by (refine_facets).

	The main routine then calls (refine_facets) and then (plot_all_facets).
	If drawing of degenerate lines is switched on then it will
	find those chains which have not been used and print them
	as well as sings which have not been used.
	 */


	List<Facet_info> all_facets = null;

	void add_sol_to_facet(Facet_info f,Sol_info s)
	{
		Facet_sol fs;

		fs = new Facet_sol();
//		if(TEST_ALLOC){
//			++facet_solcount; ++facet_solmax; ++facet_solnew;
//		}
		fs.sol = s;
		fs.next = f.sols;
		f.sols = fs;
	}

	void add_sol_to_facet_backwards(Facet_info f,Sol_info s)
	{
		Facet_sol fs,fs1;

		fs = new Facet_sol();
//		fs = (Facet_sol ) malloc(sizeof(facet_sol));
//		if(TEST_ALLOC){
//			++facet_solcount; ++facet_solmax; ++facet_solnew;
//		}
		fs.sol = s;
		fs.next = null;
		if(f.sols == null)
		{
			f.sols = fs;
			return;
		}
		/* find end of list */
		for(fs1=f.sols;fs1.next!=null;fs1=fs1.next) {}
		fs1.next = fs;
	}

	void remove_sol_from_facet(Facet_info facet1,Facet_sol fs1)
	{
		Facet_sol cur,prev,next;

		prev = null;
		for(cur = facet1.sols;cur!=null;cur=cur.next)
		{
			next = cur.next;

			if(cur == fs1 )
			{
				if(cur == facet1.sols) { facet1.sols = next; }
				else prev.next = next;
				free(cur);
				return;
			}
			prev = cur;
		}
	}

	Facet_info make_facet()
	{
		Facet_info ele;
		ele = new Facet_info();
//		ele = (Facet_info ) malloc(sizeof(facet_info));
//		if(TEST_ALLOC){
//			++facetcount; ++facetmax; ++facetnew;
//		}

//		ele.next = all_facets;
		ele.sols = null;
		return(ele);
	}

	Facet_info add_facet()
	{
		Facet_info ele;
		ele = make_facet();
		if(all_facets==null)
			all_facets=new ArrayList<Facet_info>();
		all_facets.add(ele);
		return(ele);
	}

	void free_facet(Facet_info f1)
	{
		Facet_sol fs1,fs2;

		fs1=f1.sols;
		while(fs1!=null)
		{
			fs2 = fs1.next;
			free(fs1);
			fs1 = fs2;
		}
		free(f1);
	}

	private void free(Facet_info f1) {
		// TODO Auto-generated method stub
		
	}

	private void free(Facet_sol fs1) {
		// TODO Auto-generated method stub
		
	}

	void remove_facet(Facet_info f1)
	{
		all_facets.remove(f1);

	}

	void remove_facet_from_list(List<Facet_info> existing,Facet_info facet2)
	{
		existing.remove(facet2);
//		Facet_info facet3,prev=null;
//		for(facet3=existing;facet3!=null;facet3=facet3.next)
//		{
//			if(facet3==facet2)
//			{
//				if(prev==null) return facet3.next;
//				else
//				{
//					prev.next = facet3.next;
//					return existing;
//				}
//			}
//			prev = facet3;
//		}//		return existing;
	}

//	void free_facet_list(Facet_info f1)
//	{
//		Facet_info f2;
//
//		while(f1 != null)
//		{
//			f2 = f1.next;
//			free_facet(f1);
//			f1 = f2;
//		}
//	}

	void free_facets()
	{
		free_facet_list(all_facets);
		all_facets = null;
	}

	void print_facet(Facet_info f1)
	{
		Facet_sol s1;

		s1 = f1.sols;
		if(s1 == null)
		{
			System.out.printf("Empty facet\n");
			return;
		}
		System.out.printf("bgnfacet\n");
		while(s1 != null)
		{
			print_sol(s1.sol);
			s1 = s1.next;
		}
		System.out.printf("endfacet\n");
	}

	private void print_sol(Sol_info sol) {
		System.out.print(sol);
	}

	void print_all_facets()
	{
		for(Facet_info f1:all_facets)
		{
			print_facet(f1);
		}
	}

//	void print_facets(Facet_info f1)
//	{
//		if(f1 == null) System.out.printf("No facets\n");
//		while(f1 != null)
//		{
//			print_facet(f1);
//			f1 = f1.next;
//		}
//	}

	Facet_sol sol_on_facet(Facet_info f,Sol_info s)
	{
		Facet_sol s1;

		s1 = f.sols;
		while(s1 != null)
		{
			if(s1.sol == s) return s1;
			s1 = s1.next;
		}
		return null;
	}

	int first_sol_on_facet(Facet_info f,Facet_sol fs1,Facet_sol fs2)
	{
		Facet_sol s1;

		s1 = f.sols;
		while(s1 != null)
		{
			if(s1 == fs1) return 1;
			if(s1 == fs2) return 0;
			s1 = s1.next;
		}
		System.out.printf("Error: facet_sol not found on facet");
		return 0;
	}

	/***** Modindfying facets by chains *****************************************/

	/* actually split facet on a sub-chain
		results in f2,f3
	 */

	void cut_facet_on_sub_chain(Facet_info f1,Facet_info f2,Facet_info f3,
			Facet_sol fs1,Facet_sol fs2,
			Chain_info chain,int first,int last)
	{
		Facet_sol fs3;
		int i,j;

		/* first copy sols on facet to new facets */

		for(fs3=fs1;fs3!=fs2 && fs3!=null;fs3=fs3.next)
		{
			add_sol_to_facet(f2,fs3.sol);
		}
		if(fs3==null)
			for(fs3=f1.sols;fs3!=fs2;fs3=fs3.next)
			{
				add_sol_to_facet(f2,fs3.sol);
			}
		add_sol_to_facet(f2,fs2.sol);

		for(fs3=fs2;fs3!=fs1 && fs3!= null;fs3=fs3.next)
		{
			add_sol_to_facet(f3,fs3.sol);
		}
		if(fs3==null)
			for(fs3=f1.sols;fs3!=fs1;fs3=fs3.next)
			{
				add_sol_to_facet(f3,fs3.sol);
			}
		add_sol_to_facet(f3,fs1.sol);

		/* now add the sols on chain */

		if(fs1.sol == chain.getSol(first))
		{
			if(first < last)
				for(i=first+1,j=last-1;i<last;++i,--j)
				{
					add_sol_to_facet(f3,chain.getSol(i));
					add_sol_to_facet(f2,chain.getSol(j));
				}
			else
				for(i=first-1,j=last+1;i>last;--i,++j)
				{
					add_sol_to_facet(f3,chain.getSol(i));
					add_sol_to_facet(f2,chain.getSol(j));
				}
		}
		else if(fs1.sol == chain.getSol(last))
		{
			if(first < last)
				for(i=first+1,j=last-1;i<last;++i,--j)
				{
					add_sol_to_facet(f3,chain.getSol(j));
					add_sol_to_facet(f2,chain.getSol(i));
				}
			else
				for(i=first-1,j=last+1;i>last;--i,++j)
				{
					add_sol_to_facet(f3,chain.getSol(j));
					add_sol_to_facet(f2,chain.getSol(i));
				}

		}
		else
		{
			System.out.printf("Funny stuff happening with the chain sols\n");
		}
	}

	void split_facet_on_chains(Box_info box,Facet_info f1)
	{
		Facet_sol fs1,fs2,fs3,fs4;
		Facet_info f2,f3;
		Sol_info chainsol1,chainsol2;
		Chain_info chain2;
		double chain_length;
		int i;
		boolean flag;

		if(PRINT_REFINE){
			System.out.printf("split_facet_on_chain:\n");
			print_facet(f1);
		}

		/* First split on repeated vertices */

		for(fs1=f1.sols;fs1!=null;fs1=fs1.next)
		{
			for(fs2=fs1.next,fs3=fs1;fs2!=null;fs3=fs2,fs2=fs2.next)
			{
				if(fs1.sol == fs2.sol)
				{
					f2 = add_facet();
					fs4 = fs1.next;
					fs1.next = fs2.next;
					if(fs2 != fs4)
						fs2.next = fs4;
					if(fs3!=fs1)
						fs3.next = null;
					f2.sols = fs2;	
					if(PRINT_REFINE){
						System.out.printf("duplicate sols in facet\n");
						print_sol(fs1.sol);
					}
					split_facet_on_chains(box,f1);
					split_facet_on_chains(box,f2);
					return;
				}
			}
		}

		for(fs1=f1.sols;fs1!=null;fs1=fs1.next)
		{
			if(fs1.sol.type.compareTo(FACE_LL)<0 || fs1.sol.type.compareTo(FACE_UU)>0 ) continue;

			for(fs2=fs1.next;fs2!=null;fs2=fs2.next)
			{
				if(fs2.sol.type.compareTo(FACE_LL)<0 || fs2.sol.type.compareTo(FACE_UU)>0 ) continue;

				chain_length = (float) 100.0;
				chain2 = null;
				for(Chain_info chain:box.chains)
				{
					chainsol1 = chain.getSol(0);
					chainsol2 = chain.getSol(chain.length()-1);
					if(fs1.sol != chainsol1 && fs1.sol != chainsol2 ) continue;
					if(fs2.sol != chainsol1 && fs2.sol != chainsol2 ) continue;

					if(chain.metric_length < chain_length)
					{
						chain_length = chain.metric_length;
						chain2 = chain;
					}
					if(chain.metric_length == chain_length && chain2.used)
					{
						chain2 = chain;
					}
				}
				if(chain2 == null) continue;

				/* need to check that this chain is not already included as 
					segments round the facet */

				flag = true;
				for(fs3=fs1.next,i=1;fs3!=fs2;fs3=fs3.next,++i)
				{
					if(fs1.sol == chain2.getSol(0) )
					{
						if(fs3.sol != chain2.getSol(i) )
						{
							flag = false;
							break;
						}
					}
					else
					{
						if(fs3.sol != chain2.getSol(chain2.length()-i-1) )
						{
							flag = false;
							break;
						}
					}
				}
				if(flag) continue;
				/* could possible be the the other way round */

				flag = true;
				for(fs3=fs2.next,i=1;fs3!=null;fs3=fs3.next,++i)
				{
					if(fs2.sol == chain2.getSol(0) )
					{
						if(fs3.sol != chain2.getSol(i) )
						{
							flag = false;
							break;
						}
					}
					else
					{
						if(fs3.sol != chain2.getSol(chain2.length()-i-1) )
						{
							flag = false;
							break;
						}
					}
				}
				for(fs3=f1.sols;fs3!=fs1;fs3=fs3.next,++i)
				{
					if(fs2.sol == chain2.getSol(0) )
					{
						if(fs3.sol != chain2.getSol(i) )
						{
							flag = false;
							break;
						}
					}
					else
					{
						if(fs3.sol != chain2.getSol(chain2.length()-i-1) )
						{
							flag = false;
							break;
						}
					}
				}
				if(flag) continue;
				/*
				if(chain2.used) continue;
				 */
				chain2.used =true;

				chainsol1 = chain2.getSol(0);
				chainsol2 = chain2.getSol(chain2.length()-1);
				if(PRINT_REFINE){
					System.out.printf("Split on chain\n");
					print_chain(chain2);
				}
				f2 = add_facet();
				f3 = add_facet();

				for(fs3=fs1;fs3!=fs2;fs3=fs3.next)
				{
					add_sol_to_facet(f2,fs3.sol);
				}
				add_sol_to_facet(f2,fs2.sol);

				for(fs3=f1.sols;fs3!=fs1;fs3=fs3.next)
				{
					add_sol_to_facet(f3,fs3.sol);
				}
				add_sol_to_facet(f3,fs1.sol);

				if(chainsol1 == fs2.sol && chainsol2 == fs1.sol)
				{
					for(i=1;i<chain2.length()-1;++i)
					{
						add_sol_to_facet(f2,chain2.getSol(i));
						add_sol_to_facet(f3,chain2.getSol(chain2.length()-1-i));
					}
				}
				else if(chainsol1 == fs1.sol && chainsol2 == fs2.sol)
				{
					for(i=chain2.length()-2;i>0;--i)
					{
						add_sol_to_facet(f2,chain2.getSol(i));
						add_sol_to_facet(f3,chain2.getSol(chain2.length()-1-i));
					}
				}
				else
				{
					System.out.printf("Funny stuff happening with the chain sols\n");
				}

				for(fs3=fs2;fs3!=null;fs3=fs3.next)
				{
					add_sol_to_facet(f3,fs3.sol);
				}

				split_facet_on_chains(box,f2);
				split_facet_on_chains(box,f3);
				/* now have to remove f1 for list of facets */

				remove_facet(f1);
				return;
			}
		}						
	}

	private void print_chain(Chain_info chain2) {
		System.out.print(chain2);
		
	}

	/** fix hanging nodes it may happen that 
		some nodes have not been joined along their
		chains 
		@return true if list of facets altered
		**/

	boolean fix_hanging_nodes(Box_info box,Facet_info f1)
	{
		Facet_sol fs1=null,fs2=null,fs3=null,fs4=null,fs5=null;
		Facet_info f2=null,f3=null;
		Sol_info chainsol1,chainsol2;
		Chain_info chain2;
		double chain_length;
		int i;

		if(PRINT_REFINE){
			System.out.printf("fix_hanging_nodes:\n");
			print_facet(f1);
		}

		/* get last facet */

		for(fs1=f1.sols;fs1!=null;fs1=fs1.next)
		{
			fs2 = fs1; /* previous sol */
		}
		fs3 = null; /* next sol */

		for(fs1=f1.sols;fs1!=null;fs2=fs1,fs1=fs1.next)
		{
			int last_index=-1;

			if(fs1.sol.type.compareTo(FACE_LL)<0 || fs1.sol.type.compareTo(FACE_UU)>0 ) continue;


			fs3 = fs1.next;
			if(fs3 == null) { fs3=f1.sols; }

			chain_length = (float) 100.0;
			chain2 = null;
			if(box.chains!=null)
			for(Chain_info chain:box.chains)
			{
				double curLen = 0.0;

				chainsol1 = chain.getSol(0);
				chainsol2 = chain.getSol(chain.length()-1);
				if(fs1.sol == chainsol1 )
				{
					boolean flag3=false;

					for(i=1;i<chain.length();++i)
					{
						float dx,dy,dz;

						dx = ((float) chain.getSol(i-1).xl) / chain.getSol(i-1).denom
						- ((float) chain.getSol(i).xl) / chain.getSol(i).denom;
						dy = ((float) chain.getSol(i-1).yl) / chain.getSol(i-1).denom
						- ((float) chain.getSol(i).yl) / chain.getSol(i).denom;
						dz = ((float) chain.getSol(i-1).zl) / chain.getSol(i-1).denom
						- ((float) chain.getSol(i).zl) / chain.getSol(i).denom;

						curLen += Math.sqrt( dx * dx + dy * dy + dz * dz);

						for(fs4=f1.sols;fs4!=null;fs4=fs4.next)
						{
							if(fs4.sol==chain.getSol(i))
							{
								flag3 = true;
								break;
							}
						}
						if(flag3) break;
					}
					if(flag3 && curLen < chain_length )
					{
						chain2 = chain;
						chain_length = curLen;
						last_index = i;
						fs5 = fs4;
					}
				}		
				else if(fs1.sol == chainsol2 )
				{
					boolean flag3=false;

					for(i=chain.length()-2;i>=0;--i)
					{
						float dx,dy,dz;

						dx = ((float) chain.getSol(i+1).xl) / chain.getSol(i+1).denom
						- ((float) chain.getSol(i).xl) / chain.getSol(i).denom;
						dy = ((float) chain.getSol(i+1).yl) / chain.getSol(i+1).denom
						- ((float) chain.getSol(i).yl) / chain.getSol(i).denom;
						dz = ((float) chain.getSol(i+1).zl) / chain.getSol(i+1).denom
						- ((float) chain.getSol(i).zl) / chain.getSol(i).denom;

						curLen += Math.sqrt( dx * dx + dy * dy + dz * dz);

						for(fs4=f1.sols;fs4!=null;fs4=fs4.next)
						{
							if(fs4.sol==chain.getSol(i))
							{
								flag3 = true;
								break;
							}
						}
						if(flag3) break;
					}
					if(flag3 && curLen < chain_length )
					{
						chain2 = chain;
						chain_length = curLen;
						last_index = i;
						fs5 = fs4;
					}
				}
				else
					continue;
			} /* end loop through chains */

			if(chain2 == null) continue;
			chainsol1 = chain2.getSol(0);
			chainsol2 = chain2.getSol(chain2.length()-1);

			/* need to check that this chain is not already included as 
					segments round the facet */

			if(fs1.sol == chainsol1 )
			{
				if(chain2.getSol(1) == fs2.sol || chain2.getSol(1) == fs3.sol) continue;
			}
			else if(fs1.sol == chainsol2 )
			{
				if(chain2.getSol(chain2.length()-2) == fs2.sol || chain2.getSol(chain2.length()-2) == fs3.sol) continue;
			}
			/*
				if(chain2.used) continue;
			 */
			chain2.used =true;

			if(PRINT_REFINE){
				System.out.printf("Split on chain\n");
				print_chain(chain2);
			}

			/* now found a chain to split on */

			f2 = add_facet();
			f3 = add_facet();
			if(fs1.sol == chainsol1)
				cut_facet_on_sub_chain(f1,f2,f3,fs1,fs5,
						chain2,0,last_index);
			else if(fs1.sol == chainsol2)
				cut_facet_on_sub_chain(f1,f2,f3,fs1,fs5,
						chain2,chain2.length()-1,last_index);
			else
			{
				System.out.printf("Funny stuff happening with the chain sols\n");
			}


			if(PRINT_REFINE){
				System.out.printf("fixed_hanging_nodes:\n");
				print_facet(f2);
				print_facet(f3);
			}
			fix_hanging_nodes(box,f2);
			fix_hanging_nodes(box,f3);

			/* now have to remove f1 for list of facets */

			remove_facet(f1);
			return true;
		}				
		return false;
	}


	List<Facet_info> split_facet_by_sub_chains(Box_info box,Facet_info f1)
	{
		Facet_sol fs1,fs2,fs3;
		Facet_info f2,f3;
		Chain_info chain2;
		double chain_length;
		int i;
		boolean flag;
		int last_index=-1,first_index=-1;
		int found_first,found_second;

		if(f1.sols == null || f1.sols.next == null || f1.sols.next.next == null) return null;

		if(PRINT_REFINE){
			System.out.printf("split_facet_by_sub_chains:\n");
			print_facet(f1);
			if(box.xl==19 && box.yl==7 && box.zl==21)
			{
				System.out.printf("split_facet_by_sub_chains:\n");
				print_facet(f1);print_chains(box.chains);
			}
		}
		for(fs1=f1.sols;fs1!=null;fs1=fs1.next)
		{
			if(fs1.sol.type.compareTo(FACE_LL)<0) continue;
			for(fs2=f1.sols;fs2!=null;fs2=fs2.next)
			{
				if(fs2 == fs1) continue;
				if(fs2.sol.type.compareTo(FACE_LL)<0) continue;

				/* Now got a reasonable pair of facet sols lets look for chains */


				chain_length = 100.0;
				chain2 = null;
				if(box.chains!=null)
				for(Chain_info chain:box.chains)
				{
					double curLen = 0.0;
					found_first = found_second = -1;

					for(i=0;i<chain.length();++i)
					{
						/*
	System.out.printf("%p %p %p\t",fs1.sol,fs2.sol,chain.getSol(i));
	print_sol(chain.getSol(i));
						 */
						if(fs1.sol == chain.getSol(i)) found_first = i;
						if(fs2.sol == chain.getSol(i)) found_second = i;
					}
					/*
	System.out.printf("found_first %d %d\n",found_first,found_second);
					 */
					if(found_first == -1 || found_second == -1) continue;
					if(found_first < found_second)
					{
						for(i=found_first;i<found_second;++i)
							curLen += chain.metLens[i];
					}
					else
					{
						for(i=found_second;i<found_first;++i)
							curLen += chain.metLens[i];
					}

					if(curLen < chain_length )
					{
						chain2 = chain;
						chain_length = curLen;
						if(found_first < found_second)
						{
							first_index = found_first;
							last_index = found_second;
						}
						else
						{
							first_index = found_second;
							last_index = found_first;
						}
					}
				} /* end loop through chains */

				if(chain2 == null) continue;

				/* need to check that this chain is not already included as 
					segments round the facet */

				flag = false;
				for(i=first_index+1;i<last_index;++i)
				{
					for(fs3=f1.sols;fs3!=null;fs3=fs3.next)
						if(fs3.sol == chain2.getSol(i)) { flag = true; break; }
					if(flag) break;
				}
				if(flag) continue;
				if(chain2.length()==2 &&
						(fs2 == fs1.next || fs1 == fs2.next
								|| ( fs1 == f1.sols && fs2.next == null )
								|| ( fs2 == f1.sols && fs1.next == null ) ) )
					continue;
				/*
			if(chain2.used) continue;
				 */
				if(last_index == first_index ) continue;
				if(last_index-first_index == 1 
						&& ( fs2 == fs1.next 
								|| fs1 == fs2.next 
								|| ( fs1 == f1.sols && fs2.next == null )
								|| ( fs2 == f1.sols && fs1.next == null ) ) ) 
					continue;
				chain2.used =true;

				if(PRINT_REFINE){
					System.out.printf("Split on chain %d %d\n",first_index,last_index);
					print_chain(chain2);
				}
				/* now found a chain to split on */

//				f2 = add_facet();
//				f3 = add_facet();
				f2 = make_facet();
				f3 = make_facet();
				cut_facet_on_sub_chain(f1,f2,f3,fs1,fs2,
						chain2,first_index,last_index);


				if(PRINT_REFINE){
					System.out.printf("splited facet_by_sub_chains:\n");
					print_facet(f2);
					print_facet(f3);
				}
				List<Facet_info> res = new ArrayList<Facet_info>();
				List<Facet_info> list1 = split_facet_by_sub_chains(box,f2);
				List<Facet_info> list2 = split_facet_by_sub_chains(box,f3);
				if(list1!= null && !list1.isEmpty())
					res.addAll(list1);
				else
					res.add(f2);

				if(list2!= null && !list2.isEmpty())
					res.addAll(list2);
				else
					res.add(f3);
				/* now have to remove f1 for list of facets */

				//remove_facet(f1);
				return res;
			} /* end fs2 loop */
		}  /* end fs1 loop */
		return null;
	}

	private void print_chains(List<Chain_info> chains) {
		for(Chain_info chain:chains)
			System.out.print(chain);
	}

	double calc_fs_dist(Facet_sol fs1,Facet_sol fs2)
	{
		double vec1[]=new double[3],vec2[]=new double[3];
		double dx,dy,dz;

		calc_pos_actual(fs1.sol,vec1);
		calc_pos_actual(fs2.sol,vec2);
		dx = vec1[0]-vec2[0];
		dy = vec1[1]-vec2[1];
		dz = vec1[2]-vec2[2];
		return Math.sqrt(dx*dx+dy*dy+dz*dz);
	}

	
	private void calc_pos_actual(Sol_info sol, double[] vec) {
		boxclev.calc_pos_actual(sol, vec);
		
	}


	int calc_orint_of_joined_facets(Facet_info f1,Facet_info f2,
			Facet_sol fs1,Facet_sol fs2,Facet_sol fs3,Facet_sol fs4)
	{
		Facet_sol fs5,fs6,fs7,fs8;
		int test1=0,test2=0,test3=0,test4=0,test5=0,test6=0;
		int orient_error = 0; /* whether an error found in calculation */

		fs5 = fs1.next;
		if(fs5 == null ) fs5 = f1.sols;
		fs6 = fs2.next;
		if(fs6 == null ) fs6 = f2.sols;
		fs7 = fs3.next;
		if(fs7 == null ) fs7 = f1.sols;
		fs8 = fs4.next;
		if(fs8 == null ) fs8 = f2.sols;

		if(fs1.sol.dx == fs2.sol.dx 
				&& fs1.sol.dy == fs2.sol.dy
				&& fs1.sol.dz == fs2.sol.dz )
		{
			if(fs1.sol.dx == 0 && fs1.sol.dy != 0 && fs1.sol.dz != 0 )
			{
				test1 = fs5.sol.dx;
				test2 = fs6.sol.dx;
			}
			else if(fs1.sol.dx != 0 && fs1.sol.dy == 0 && fs1.sol.dz != 0 )
			{
				test1 = fs5.sol.dy;
				test2 = fs6.sol.dy;
			}
			else if(fs1.sol.dx != 0 && fs1.sol.dy != 0 && fs1.sol.dz == 0 )
			{
				test1 = fs5.sol.dz;
				test2 = fs6.sol.dz;
			}
			else
			{
				orient_error = 1;
			}
		}
		else
		{
			orient_error = 2;
		}

		if(fs3.sol.dx == fs4.sol.dx 
				&& fs3.sol.dy == fs4.sol.dy
				&& fs3.sol.dz == fs4.sol.dz )
		{
			if(fs3.sol.dx == 0 && fs3.sol.dy != 0 && fs3.sol.dz != 0 )
			{
				test3 = fs7.sol.dx;
				test4 = fs8.sol.dx;
			}
			else if(fs3.sol.dx != 0 && fs3.sol.dy == 0 && fs3.sol.dz != 0 )
			{
				test3 = fs7.sol.dy;
				test4 = fs8.sol.dy;
			}
			else if(fs3.sol.dx != 0 && fs3.sol.dy != 0 && fs3.sol.dz == 0 )
			{
				test3 = fs7.sol.dz;
				test4 = fs8.sol.dz;
			}
			else
			{
				orient_error = 3;
			}
		}
		else
		{
			orient_error = 4;
		}

		if(orient_error!=0 ) {}
		else if(test1 == 0 || test2 == 0 )
			orient_error = 5;
		else if(test1 == test2 )
			test5 = 1;
		else
			test5 = -1;

		if(orient_error!=0 ) {}
		else if(test1 == 0 || test2 == 0 )
			orient_error = 6;
		else if(test3 == test4 )
			test6 = 1;
		else
			test6 = -1;

		if(orient_error!=0 ) {}
		else if(test5 != test6 )
		{
			orient_error = 7; /* This is serious as get different info form the links */
		}
		else if(test5 != 0 ) return test5;

		/* well that failed */

		System.out.printf("Error calculation orientation %d\n",orient_error);
		if(PRINT_FACET_ERR){
			print_facet(f1);
			print_facet(f2);
//			print_chain(chain1);
//			print_chain(chain2);
		}
		/* try calculation normals at each point */

		{
//			double vec1[]=new double[3],vec2[]=new double[3],vec3[]=new double[3],vec4[]=new double[3],vec5[]=new double[3],vec6[]=new double[3],vec7[]=new double[3],vec8[]=new double[3];
//			double norm1[]=new double[3],norm2[]=new double[3],norm3[]=new double[3],norm4[]=new double[3],norm5[]=new double[3],norm6[]=new double[3],norm7[]=new double[3],norm8[]=new double[3];
//			double vec15[]=new double[3],vec12[]=new double[3],vec26[]=new double[3], vec34[]=new double[3],vec37[]=new double[3],vec48[]=new double[3];
//			double vnorm1[]=new double[3],vnorm2[]=new double[3],vnorm3[]=new double[3],vnorm4[]=new double[3];
			double dist,dist1 = 0,dist2=0,dist3=0,dist4=0;
//			int count1 = 0, count2 = 0, count3 = 0;
			boolean res2;
			boolean res1;


			dist = 0.0;
			for(fs5=f1.sols,fs6=null;fs5!=null;fs6=fs5,fs5=fs5.next)
			{
				if(fs6!=null) 
					dist += calc_fs_dist(fs5,fs6);
				if(fs5 == fs1) dist1 = dist;
				if(fs5 == fs3) dist3 = dist;
			}
			dist += calc_fs_dist(f1.sols,fs6);
			if(dist1<dist3) { res1 = ( ( 2.0 * ( dist3 - dist1 ) ) < dist ); }
			else		{ res1 = ( ( 2.0 * ( dist1 - dist3 ) ) > dist ); }

			dist = 0.0;
			for(fs5=f2.sols,fs6=null;fs5!=null;fs6=fs5,fs5=fs5.next)
			{
				if(fs6!=null) dist+= calc_fs_dist(fs5,fs6);
				if(fs5 == fs2) dist2 = dist;
				if(fs5 == fs4) dist4 = dist;
			}
			dist += calc_fs_dist(f2.sols,fs6);
			if(dist2<dist4) { res2 = ( ( 2.0 * ( dist4 - dist2 ) ) < dist ); }
			else		{ res2 = ( ( 2.0 * ( dist2 - dist4 ) ) > dist ); }

			/*
		System.out.printf("dist %f %f %f  %f %f %f res %d %d\n",dist5,dist1,dist3,dist,dist2,dist4,res1,res2);
			 */

			if( ( res1 && res2 ) || ( !res1 && !res2 ) ) return 1;
			else return -1;

// unreachable
//			for(fs5=f2.sols;fs5!=null;fs5=fs5.next)
//			{
//				++count3;
//				if(fs5 == fs2) count1 = count3;
//				if(fs5 == fs4) count2 = count3;
//			}
//
//			calc_pos_norm(fs1.sol,vec1,norm1);
//			calc_pos_norm(fs2.sol,vec2,norm2);
//			calc_pos_norm(fs3.sol,vec3,norm3);
//			calc_pos_norm(fs4.sol,vec4,norm4);
//			calc_pos_norm(fs5.sol,vec5,norm5);
//			calc_pos_norm(fs6.sol,vec6,norm6);
//			calc_pos_norm(fs7.sol,vec7,norm7);
//			calc_pos_norm(fs8.sol,vec8,norm8);
//
//			vec_sub(vec15,vec5,vec1);
//			vec_sub(vec12,vec2,vec1);
//			vec_sub(vec26,vec6,vec2);
//			vec_sub(vec34,vec4,vec3);
//			vec_sub(vec37,vec7,vec3);
//			vec_sub(vec48,vec8,vec4);
//
//			vec_cross(vnorm1,vec15,vec12);
//			vec_cross(vnorm2,vec12,vec26);
//			vec_cross(vnorm3,vec37,vec34);
//			vec_cross(vnorm4,vec34,vec48);
//
//			dot1 = vec_dot(vnorm1,vnorm2);
//			dot2 = vec_dot(vnorm3,vnorm4);
//
//			if(PRINT_FACET_ERR){
//				print_vec("v1",vec1);
//				print_vec("v2",vec2);
//				print_vec("v3",vec3);
//				print_vec("v4",vec4);
//				print_vec("v5",vec5);
//				print_vec("v6",vec6);
//				print_vec("v7",vec7);
//				print_vec("v8",vec8);
//
//				print_vec("v15",vec15);
//				print_vec("v12",vec12);
//				print_vec("v26",vec26);
//				print_vec("v34",vec34);
//				print_vec("v37",vec37);
//				print_vec("v48",vec48);
//
//				print_vec("vn1",vnorm1);
//				print_vec("vn2",vnorm2);
//				print_vec("vn3",vnorm3);
//				print_vec("vn4",vnorm4);
//
//				System.out.printf("dot1 %g dot2 %g\n",dot1,dot2);
//			}
//
//			if(dot1 > 0.0 && dot2 > 0.0 ) return 1;
//			if(dot1 < 0.0 && dot2 < 0.0 ) return -1;
//
//			System.out.printf("calc_orient: Normals different\n");
//
//			return 1;
//			/* now count round the second facet to see which is the shortest linking path 
//				this is on course rubbish as we don't know that we have shortets path
//				on the other facet */
//
//
//			for(fs5=f2.sols;fs5!=null;fs5=fs5.next)
//			{
//				++count3;
//				if(fs5 == fs2) count1 = count3;
//				if(fs5 == fs4) count2 = count3;
//			}
//			if(count1 < count2 )
//			{
//				if(count2-count1<count3-count2+count1) return 1;
//				else return -1;
//			}
//			else
//			{
//				if(count1-count2<count3-count1+count2) return 1;
//				else return -1;
//			}
		}

	}


	boolean join_on_chain_and_point(Box_info box,Facet_info f1,Facet_info f2,
			Chain_info chain,Facet_sol dp1,Facet_sol dp2,Facet_sol ch1,Facet_sol ch2)
	{
		int res,i;
		Facet_info f3=null,f4=null;
		Facet_sol fs5;

		System.out.printf("join on chain and point\n");
		if(dp1 == ch1 || dp2 == ch2 ) return false;

		res = calc_orint_of_joined_facets(f1,f2,dp1,dp2,ch1,ch2);
		if(PRINT_JOIN_CHAIN_POINT){
			System.out.printf("orient %d\n",res);
			print_sol(dp1.sol);
			print_chain(chain);
			print_facet(f1);
			print_facet(f2);
		}
		f3 = add_facet();
		f4 = add_facet();

		add_sol_to_facet(f3,dp1.sol);

		for(fs5=dp1.next;fs5!=ch1 && fs5!=null; fs5=fs5.next)
		{
			add_sol_to_facet(f3,fs5.sol);
		}
		if(fs5==null)
			for(fs5=f1.sols;fs5!=ch1; fs5=fs5.next)
			{
				add_sol_to_facet(f3,fs5.sol);
			}
		if(chain.getSol(0) == ch1.sol)
		{
			for(i=0;i<chain.length();++i)
				add_sol_to_facet(f3,chain.getSol(i));
		}
		else
		{
			for(i=chain.length()-1;i>=0;--i)
				add_sol_to_facet(f3,chain.getSol(i));
		}


		if(chain.getSol(0) == ch1.sol)
		{
			for(i=chain.length()-1;i>=0;--i)
				add_sol_to_facet(f4,chain.getSol(i));
		}
		else
		{
			for(i=0;i<chain.length();++i)
				add_sol_to_facet(f4,chain.getSol(i));
		}
		for(fs5=ch1.next;fs5!=dp1 && fs5!=null; fs5=fs5.next)
		{
			add_sol_to_facet(f4,fs5.sol);
		}
		if(fs5==null)
			for(fs5=f1.sols;fs5!=dp1; fs5=fs5.next)
			{
				add_sol_to_facet(f4,fs5.sol);
			}
		add_sol_to_facet(f4,dp2.sol);

		if(res>0)
		{
			for(fs5=dp2.next;fs5!=ch2 && fs5!=null; fs5=fs5.next)
			{
				add_sol_to_facet_backwards(f3,fs5.sol);
			}
			if(fs5==null)
				for(fs5=f2.sols;fs5!=ch2; fs5=fs5.next)
				{
					add_sol_to_facet_backwards(f3,fs5.sol);
				}

			for(fs5=ch2.next;fs5!=dp2 && fs5!=null; fs5=fs5.next)
			{
				add_sol_to_facet_backwards(f4,fs5.sol);
			}
			if(fs5==null)
				for(fs5=f2.sols;fs5!=dp2; fs5=fs5.next)
				{
					add_sol_to_facet_backwards(f4,fs5.sol);
				}
		}
		else
		{
			for(fs5=ch2.next;fs5!=dp2 && fs5!=null; fs5=fs5.next)
			{
				add_sol_to_facet(f3,fs5.sol);
			}
			if(fs5==null)
				for(fs5=f2.sols;fs5!=dp2; fs5=fs5.next)
				{
					add_sol_to_facet(f3,fs5.sol);
				}
			for(fs5=dp2.next;fs5!=ch2 && fs5!=null; fs5=fs5.next)
			{
				add_sol_to_facet(f4,fs5.sol);
			}
			if(fs5==null)
				for(fs5=f2.sols;fs5!=ch2; fs5=fs5.next)
				{
					add_sol_to_facet(f4,fs5.sol);
				}
		}

		if(PRINT_JOIN_CHAIN_POINT){
			System.out.printf("after join on chain and point\n");
			print_facet(f3);
			print_facet(f4);
		}
		remove_facet(f1);
		remove_facet(f2);
		return(true);
	}

	boolean join_facets_by_chains(Box_info box,Facet_info f1,Facet_info f2)
	{
		Facet_sol fs1=null,fs2=null,fs3=null,fs4=null,fs5=null;
		Chain_info chain2=null,chain3=null;
		int count = 0,i;
		Facet_info f3=null,f4=null;
		double chain_length;
		Sol_info chainsol1=null,chainsol2=null;
		int res;
		Facet_sol double_pointA=null,double_pointB=null;

		if(PRINT_REFINE){
			System.out.printf("Join facets\n");
			print_facet(f1);
			print_facet(f2);
		}

		for(fs1=f1.sols;fs1!=null;fs1=fs1.next)
		{
			for(fs2=f2.sols;fs2!=null;fs2=fs2.next)
			{
				if(fs1.sol == fs2.sol)
				{
					double_pointA = fs1;
					double_pointB = fs2;
				}
			}
		}

		chain3 = null;
		for(fs1=f1.sols;fs1!=null;fs1=fs1.next)
		{
			for(fs2=f2.sols;fs2!=null;fs2=fs2.next)
			{
				boolean flag5;

				if(fs1.sol == fs2.sol) continue;
				chain_length = 100.0;
				chain2 = null;
				if(box.chains!=null)
				for(Chain_info chain:box.chains)
				{
					int f1_index=-1,f2_index=-1;

					chainsol1 = chain.getSol(0);
					chainsol2 = chain.getSol(chain.length()-1);
					if(chainsol1 == fs1.sol ) f1_index = 0;
					if(chainsol2 == fs1.sol ) f1_index = chain.length()-1;

					if(chainsol1 == fs2.sol ) f2_index = 0;
					if(chainsol2 == fs2.sol ) f2_index = chain.length()-1;

					if(f1_index == -1 || f2_index == -1) continue;

					if(double_pointA != null &&
							(  chainsol1 == double_pointA.sol
									|| chainsol2 == double_pointA.sol ) ) continue;


					/* check that none of sols on chain are on facet */

					flag5 = false;

					for(fs5=f1.sols;fs5!=null;fs5=fs5.next)
					{
						for(i=1;i<chain.length()-1;++i)
						{
							if(chain.getSol(i)==fs5.sol)
							{
								flag5 = true;
								break;
							}
						}
						if(chain.getSol(f2_index) == fs5.sol)
							flag5 = true;

						if(flag5) break;
					}

					for(fs5=f2.sols;fs5!=null;fs5=fs5.next)
					{
						for(i=1;i<chain.length()-1;++i)
						{
							if(chain.getSol(i)==fs5.sol)
							{
								flag5 = true;
								break;
							}
						}
						if(chain.getSol(f1_index) == fs5.sol)
							flag5 = true;
						if(flag5) break;
					}

					if(flag5) continue;
					if(chain.metric_length < chain_length)
					{
						chain_length = chain.metric_length;
						chain2 = chain;
					}
				}
				if(chain2 == null) continue; /* didn't find a linking chain */
				if(chain2.used) continue;
				if(fs1 == fs3 || fs2 == fs4) continue;	/* ensure that start and end sols not the same */

				if(chain3!=null)
				{
					boolean flag2;
					int j;

					/* have two chains ensure they have no vertices in common */

					flag2 = false;
					for(i=0;i<chain2.length();++i)
						for(j=0;j<chain3.length();++j)
						{
							if(chain2.getSol(i)== chain3.getSol(j)) flag2 = true;
						}
					if(flag2) continue;
				}
				/* found a chain */
				++count;
				if(count == 2) break; /* have two OK chains */ 

				/* now got the first chain */

				fs3 = fs1; fs4 = fs2;
				chain3 = chain2;
			} /* end fs2 loop */
			if(count>=2) break;
		} /* end fs1 loop */

		if(count == 0) return false; /* no linking chains */
		if(double_pointA!=null)
		{
			return join_on_chain_and_point(box,f1,f2,chain3,
					double_pointA,double_pointB,fs3,fs4);
		}
		if(count == 1) return false;

		if(PRINT_REFINE){
			System.out.printf("Found two linking chains\n");
			print_chain(chain2);
			System.out.printf("and\n");
			print_chain(chain3);
		}
		if(fs1 == fs3 || fs2 == fs4)
		{
			System.out.printf("two of the linking facet sols arethe same\n");
			return false;
		}
		chain2.used = true;
		chain3.used = true;

		/*
			res = calc_orint_of_joined_facets(f1,f2,chain2,chain3,
				fs1,fs2,fs3,fs4);
		 */
		res = calc_orint_of_joined_facets(f1,f2,
				fs1,fs2,fs3,fs4);
		f3 = add_facet();
		f4 = add_facet();

		if(chain2.getSol(0) == fs1.sol)
		{
			for(i=chain2.length()-1;i>=0;--i)
				add_sol_to_facet(f3,chain2.getSol(i));
		}
		else
		{
			for(i=0;i<chain2.length();++i)
				add_sol_to_facet(f3,chain2.getSol(i));
		}
		for(fs5=fs1.next;fs5!=fs3 && fs5!=null; fs5=fs5.next)
		{
			add_sol_to_facet(f3,fs5.sol);
		}
		if(fs5==null)
			for(fs5=f1.sols;fs5!=fs3; fs5=fs5.next)
			{
				add_sol_to_facet(f3,fs5.sol);
			}
		if(chain3.getSol(0) == fs3.sol)
		{
			for(i=0;i<chain3.length();++i)
				add_sol_to_facet(f3,chain3.getSol(i));
		}
		else
		{
			for(i=chain3.length()-1;i>=0;--i)
				add_sol_to_facet(f3,chain3.getSol(i));
		}


		if(chain3.getSol(0) == fs3.sol)
		{
			for(i=chain3.length()-1;i>=0;--i)
				add_sol_to_facet(f4,chain3.getSol(i));
		}
		else
		{
			for(i=0;i<chain3.length();++i)
				add_sol_to_facet(f4,chain3.getSol(i));
		}
		for(fs5=fs3.next;fs5!=fs1 && fs5!=null; fs5=fs5.next)
		{
			add_sol_to_facet(f4,fs5.sol);
		}
		if(fs5==null)
			for(fs5=f1.sols;fs5!=fs1; fs5=fs5.next)
			{
				add_sol_to_facet(f4,fs5.sol);
			}
		if(chain2.getSol(0) == fs1.sol)
		{
			for(i=0;i<chain2.length();++i)
				add_sol_to_facet(f4,chain2.getSol(i));
		}
		else
		{
			for(i=chain2.length()-1;i>=0;--i)
				add_sol_to_facet(f4,chain2.getSol(i));
		}


		if(res>0)
		{
			for(fs5=fs2.next;fs5!=fs4 && fs5!=null; fs5=fs5.next)
			{
				add_sol_to_facet_backwards(f3,fs5.sol);
			}
			if(fs5==null)
				for(fs5=f2.sols;fs5!=fs4; fs5=fs5.next)
				{
					add_sol_to_facet_backwards(f3,fs5.sol);
				}
			for(fs5=fs4.next;fs5!=fs2 && fs5!=null; fs5=fs5.next)
			{
				add_sol_to_facet_backwards(f4,fs5.sol);
			}
			if(fs5==null)
				for(fs5=f2.sols;fs5!=fs2; fs5=fs5.next)
				{
					add_sol_to_facet_backwards(f4,fs5.sol);
				}
		}
		else
		{
			for(fs5=fs4.next;fs5!=fs2 && fs5!=null; fs5=fs5.next)
			{
				add_sol_to_facet(f3,fs5.sol);
			}
			if(fs5==null)
				for(fs5=f2.sols;fs5!=fs2; fs5=fs5.next)
				{
					add_sol_to_facet(f3,fs5.sol);
				}
			for(fs5=fs2.next;fs5!=fs4 && fs5!=null; fs5=fs5.next)
			{
				add_sol_to_facet(f4,fs5.sol);
			}
			if(fs5==null)
				for(fs5=f2.sols;fs5!=fs4; fs5=fs5.next)
				{
					add_sol_to_facet(f4,fs5.sol);
				}
		}



		remove_facet(f1);
		remove_facet(f2);
		return(true);
	}

	void refine_facets(Box_info box)
	{
		Facet_sol fs1,fs2,fs3,fs4;
		boolean flag;

		if(all_facets==null)
			return;
		/* May have a facet with repeated vertices
				if so split the facet */

		if(PRINT_REFINE){
			print_all_facets();
		}


		flag = true;
		while(flag)
		{
			flag = false;
			for(Facet_info f1:all_facets)
			{
				for(fs1=f1.sols;fs1!=null;fs1=fs1.next)
				{
					for(fs2=fs1.next,fs3=fs1;fs2!=null;fs3=fs2,fs2=fs2.next)
					{
						if(fs1.sol == fs2.sol)
						{
							if(PRINT_REFINE){
								System.out.printf("Split on sol\n");
								print_sol(fs1.sol);
								print_facet(f1);
							}
							Facet_info f2 = add_facet();
							fs4 = fs1.next;
							fs1.next = fs2.next;
							if(fs2 != fs4)
								fs2.next = fs4;
							if(fs3!=fs1)
								fs3.next = null;
							f2.sols = fs2;	
							flag = true;
							if(PRINT_REFINE){
								System.out.printf("after Split on sol\n");
								print_facet(f1);
								print_facet(f2);
							}
							break;
						}
						if(flag) break;
					} /* end fs2 loop */
					if(flag) break;
				} /* end fs1 loop */
				if(flag) break;
			} /* end f1 loop */
		}

		/* Now join together facets linked by chains */

		flag = true;
		while(flag)
		{
			flag = false;
			ListIterator<Facet_info> it1 = all_facets.listIterator();
			while(it1.hasNext()) {
				Facet_info f1 = it1.next();
				int nextIndex = it1.nextIndex();
				ListIterator<Facet_info> it2 = all_facets.listIterator(nextIndex);
				while(it2.hasNext()) {
					Facet_info f2 = it2.next();

//			for(Facet_info f1:all_facets)
//			for(f1=all_facets;f1!=null;f1=f3)
//			{
//				f3 = f1.next;
//				for(f2=f1.next;f2!=null;f2=f4)
//				{
//					f4 = f2.next;
					flag = join_facets_by_chains(box,f1,f2);
					if(flag) break;
				}
				if(flag) break;
			}
		}	
		if(PRINT_REFINE){
			System.out.printf("After joining\n");
			print_all_facets();
		}

		/* Now any chain which happens to start at fs1
				should be duplicated */
		/*
			for(chain=box.chains;chain!=null;chain=chain.next)
			{
				chainsol1 = chain.sols[0];
				chainsol2 = chain.sols[chain.length-1];
				if(fs1.sol == chainsol1 || fs1.sol == chainsol2 )
				{
					chain2 = (Chain_info ) malloc(sizeof(chain_info));
		if(TEST_ALLOC){
			++chaincount; ++chainmax; ++chainnew;
		}
					chain2.next = chain.next;
					chain.next = chain2;
					chain2.length() = chain.length;
					chain2.metric_length = chain.metric_length;
					chain2.used = 0;
					chain2.sols = (Sol_info *) malloc(sizeof(Sol_info )*chain.length);
		if(TEST_ALLOC){
			++chaincount; ++chainmax; ++chainnew;
		}
					bcopy(chain.sols,chain2.sols,sizeof(Sol_info )*chain.length);
					chain = chain2;
				}
			}								
		 */

		if(PRINT_REFINE){
			System.out.printf("After split on sols\n");
			print_all_facets();
			/*
			print_chains(box.chains);
			 */
		}

		/* Now split facets where two of the nodes are linked by a chain */

		boolean fhnAgain = true;
		while(fhnAgain)
		{
		ListIterator<Facet_info> li = all_facets.listIterator();
		while(li.hasNext())
		{
			fhnAgain=false;
			Facet_info f1 = li.next();
//			f2 = f1.next; /* cant guarentee that f1 will still exist after splitting */
			
			
			if(fix_hanging_nodes(box,f1)) {
				fhnAgain=true;
				break;
			}
			 
			List<Facet_info> res = split_facet_by_sub_chains(box,f1);
			if(res!=null && !res.isEmpty()) {
				li.remove();
				for(Facet_info f2:res)
					li.add(f2);
				fhnAgain=true;
				break;
			}
		} /* end loop through facets */
		}

		if(PRINT_REFINE){
			System.out.printf("Final facets\n");
			print_all_facets();
		}
	}						



	/************************************************************************/
	/*									*/
	/*	Working out chains of singularities through a box		*/
	/*	make_chains starts from each node_link				*/
	/*	if the node link has two faces at its end then we have a simple */
	/* 	two element chain.						*/
	/*	if it has two sings then we ignore it				*/
	/*	if it has only one sing then use the follow_chain procedure	*/
	/*	to follow the chain until it reaches another face node		*/
	/*	or if it joins back on itself					*/
	/*	If an intermediate sing has more than two adjacent node_links 	*/
	/*	create a new chain and recursivly call follow_chain on than	*/
	/*									*/
	/************************************************************************/

	void make_chains(Box_info box)
	{
		Chain_info chain;
		
		float dx,dy,dz;
		boolean flag;
		int i;

		box.chains = null;
		if(box.node_links!=null)
		for(Node_link_info n1:box.node_links)
		{
			if(n1.singA == null && n1.singB == null)
			{
				chain = new Chain_info();
//				if(TEST_ALLOC){
//					++chaincount; ++chainmax; ++chainnew;
//				}
				//chain.length =2;
				chain.used = false;
				//chain.sols = new Sol_info[2];
				chain.addSol(n1.A.sol);
				chain.addSol(n1.B.sol);

				dx = ((float) chain.getSol(0).xl) / chain.getSol(0).denom
				- ((float) chain.getSol(1).xl) / chain.getSol(1).denom;
				dy = ((float) chain.getSol(0).yl) / chain.getSol(0).denom
				- ((float) chain.getSol(1).yl) / chain.getSol(1).denom;
				dz = ((float) chain.getSol(0).zl) / chain.getSol(0).denom
				- ((float) chain.getSol(1).zl) / chain.getSol(1).denom;
				chain.metric_length = Math.sqrt( dx * dx + dy * dy + dz * dz);

				chain.metLens = new double[2];
				chain.metLens[0] = chain.metric_length;
				if(box.chains==null)
					box.chains=new ArrayList<Chain_info>();
				box.chains.add(chain);
			}
			else if(n1.singA != null && n1.singB != null)
			{
			}
			else
			{
				chain = new Chain_info();
//				if(TEST_ALLOC){
//					++chaincount; ++chainmax; ++chainnew;
//				}
				//chain.length =2;
				chain.used = false;
				//chain.sols = (Sol_info *) malloc(sizeof(Sol_info ) * (box.num_sings+2));
				chain.metLens = new double[box.num_sings+1];
				if(n1.singA == null)
				{
					chain.addSol(n1.A.sol);
					chain.addSol(n1.B.sol);
				}
				else
				{
					chain.addSol(n1.B.sol);
					chain.addSol(n1.A.sol);
				}

				dx = ((float) chain.getSol(0).xl) / chain.getSol(0).denom
				- ((float) chain.getSol(1).xl) / chain.getSol(1).denom;
				dy = ((float) chain.getSol(0).yl) / chain.getSol(0).denom
				- ((float) chain.getSol(1).yl) / chain.getSol(1).denom;
				dz = ((float) chain.getSol(0).zl) / chain.getSol(0).denom
				- ((float) chain.getSol(1).zl) / chain.getSol(1).denom;
				chain.metric_length = Math.sqrt( dx * dx + dy * dy + dz * dz);
				chain.metLens[0] = chain.metric_length;

				if(box.chains==null)
					box.chains = new ArrayList<Chain_info>();
				box.chains.add(chain);
				
				/* Now recurse along the chain */

				if(n1.singA != null)
				{
					follow_chain(box,chain,n1.singA,n1);
				}
				else
				{
					follow_chain(box,chain,n1.singB,n1);
				}
			}
		}

		/* Some chains may not end on a face - remove them */


		if(box.chains!=null) {
			ListIterator<Chain_info> li=box.chains.listIterator();

			while(li.hasNext())
			{
				Chain_info chain1 = li.next();

				if(chain1.getSol(0).type.compareTo(FACE_LL) < 0 || chain1.getSol(0).type.compareTo(FACE_UU) > 0 
						|| chain1.getSol(chain1.length()-1).type.compareTo(FACE_LL) < 0 
						|| chain1.getSol(chain1.length()-1).type.compareTo(FACE_UU) > 0 )
				{
					li.remove();
				}
			}


		/* Now want to remove duplicate chains */

			li=box.chains.listIterator();
			while(li.hasNext())
			{
				Chain_info chain1 = li.next();
				int ind = li.nextIndex();
				ListIterator<Chain_info> li2=box.chains.listIterator(ind);
				while(li2.hasNext())
				{
					Chain_info chain2 = li2.next();
					if(chain1.length() != chain2.length()) continue;
					flag = true;
					if(chain1.getSol(0) == chain2.getSol(0) 
							&& chain1.getSol(chain1.length()-1) == chain2.getSol(chain2.length()-1) )
					{
						for(i=1;i<chain1.length()-1;++i)
						{
							if(chain1.getSol(i) != chain2.getSol(i)) flag = false;
						}
					}
					else if(chain1.getSol(0) == chain2.getSol(chain2.length()-1) 
							&& chain1.getSol(chain1.length()-1) == chain2.getSol(0) )
					{
						for(i=1;i<chain1.length()-1;++i)
						{
							if(chain1.getSol(i) != chain2.getSol(chain2.length()-1-i)) flag = false;
						}
					}
					else flag = false;

					if(flag)
					{
						li.remove();
						//					chain3.next = chain2.next;
						//					free(chain2.sols);
						//					free(chain2.metLens);
						//					free(chain2);
						break;
					}
				}
			}
		}			
	}

	void follow_chain(Box_info box,Chain_info chain,Sing_info sing,Node_link_info nl)
	{
		Sol_info next_sol=null;
		Sing_info next_sing=null;
		Node_link_info next_nl=null;
		Chain_info chain2=null;
		int i,j,k;
		float dx,dy,dz;

		if(PRINT_FOLLOW_CHAIN){
			System.out.printf("follow_chain:\n");
			printsing(sing);
			printnode_link(nl);
		}
		while(true)
		{
			if(sing.numNLs == 0)
			{
				System.out.printf("Sing has zero adjNLs\n");
				break;
			}
			else if(sing.numNLs == 1)
			{
				System.out.printf("Sing has only one adjNLs\n");
				break;
			}
			else if(sing.numNLs == 2)
			{
				if(PRINT_FOLLOW_CHAIN){
					System.out.printf("Simple add\n");
				}
				if(sing.adjacentNLs[0] == nl)
				{
					next_nl = sing.adjacentNLs[1];
				}
				else if(sing.adjacentNLs[1] == nl)
				{
					next_nl = sing.adjacentNLs[0];
				}
				else
				{
					System.out.printf("node_link not adjacet to sing\n");
				}

				if(next_nl.singA == sing)
				{
					next_sing = next_nl.singB;
					next_sol = next_nl.B.sol;
				}
				else if(next_nl.singB == sing)
				{
					next_sing = next_nl.singA;
					next_sol = next_nl.A.sol;
				}
				else
				{
					System.out.printf("Sing not adjacent to NL\n");
					break;
				}

				/* now check that the next sol is not already in the chain */

				for(i=0;i<chain.length();++i)
				{
					if(next_sol == chain.getSol(i))
					{
						break;
					}
				}
				if(i!=chain.length()) break;

				/* everything OK add sol to the end of the chain */

				chain.addSol(next_sol);

				dx = ((float) chain.getSol(chain.length()-2).xl) / chain.getSol(chain.length()-2).denom
				- ((float) chain.getSol(chain.length()-1).xl) / chain.getSol(chain.length()-1).denom;
				dy = ((float) chain.getSol(chain.length()-2).yl) / chain.getSol(chain.length()-2).denom
				- ((float) chain.getSol(chain.length()-1).yl) / chain.getSol(chain.length()-1).denom;
				dz = ((float) chain.getSol(chain.length()-2).zl) / chain.getSol(chain.length()-2).denom
				- ((float) chain.getSol(chain.length()-1).zl) / chain.getSol(chain.length()-1).denom;
				chain.metLens[chain.length()-2] = Math.sqrt( dx * dx + dy * dy + dz * dz);
				chain.metric_length += chain.metLens[chain.length()-2];
				nl = next_nl;
				sing = next_sing;

				if(sing==null) break;	/* reached the end of the chain */
				continue;
			}

			/* now have more than two sings in the chain */
			if(PRINT_FOLLOW_CHAIN){
				System.out.printf("SIng with %d node links\n",sing.numNLs);
				printsing(sing);
			}
			j = 0;
			for(i=0;i<sing.numNLs;++i)
			{
				if(sing.adjacentNLs[i] == nl) continue;
				if(PRINT_FOLLOW_CHAIN){
					System.out.printf("Trying link no %d\n",i);
				}

				next_nl = sing.adjacentNLs[i];
				if(next_nl.singA == sing)
				{
					next_sing = next_nl.singB;
					next_sol = next_nl.B.sol;
				}
				else if(next_nl.singB == sing)
				{
					next_sing = next_nl.singA;
					next_sol = next_nl.A.sol;
				}
				else
				{
					System.out.printf("Sing not adjacent to NL\n");
					break;
				}
				if(PRINT_FOLLOW_CHAIN){
					printnode_link(next_nl);
					printsing(next_sing);
					print_sol(next_sol);
				}
				/* now check that the next sol is not already in the chain */


				for(k=0;k<chain.length();++k)
				{
					if(next_sol == chain.getSol(k))
					{
						break;
					}
				} 

				++j; /* always increment this so can pick up last adjNL to do */
				if(j < sing.numNLs -1 )
				{
					if(k!=chain.length()) continue;

					/* need to make a new chain */
					if(PRINT_FOLLOW_CHAIN){
						System.out.printf("Making a new chain\n");
					}
					chain2 =  new Chain_info();
//					if(TEST_ALLOC){
//						++chaincount; ++chainmax; ++chainnew;
//					}
//					chain2.length() = chain.length;
					chain2.metric_length = chain.metric_length;
					chain2.used = false;
					//chain2.sols = (Sol_info *) malloc(sizeof(Sol_info )*(box.num_sings+2));
					chain2.metLens = new double[box.num_sings+1];
					chain2.sols.addAll(chain.sols);
					//memcpy(chain2.sols,chain.sols,sizeof(Sol_info )*chain.length());
					//memcpy(chain2.metLens,chain.metLens,sizeof(float)*(chain.length-1));
					System.arraycopy(chain.metLens, 0, chain2.metLens, 0, chain.length()-1);
					chain2.addSol(next_sol);

					dx = ((float) chain2.getSol(chain2.length()-2).xl) / chain2.getSol(chain2.length()-2).denom
					- ((float) chain2.getSol(chain2.length()-1).xl) / chain2.getSol(chain2.length()-1).denom;
					dy = ((float) chain2.getSol(chain2.length()-2).yl) / chain2.getSol(chain2.length()-2).denom
					- ((float) chain2.getSol(chain2.length()-1).yl) / chain2.getSol(chain2.length()-1).denom;
					dz = ((float) chain2.getSol(chain2.length()-2).zl) / chain2.getSol(chain2.length()-2).denom
					- ((float) chain2.getSol(chain2.length()-1).zl) / chain2.getSol(chain2.length()-1).denom;
					chain2.metLens[chain2.length()-2] = Math.sqrt( dx * dx + dy * dy + dz * dz);
					chain2.metric_length += chain2.metLens[chain2.length()-2];

					box.chains.add(chain2);
					if(next_sing != null) 
					{
						follow_chain(box,chain2,next_sing,next_nl);
					}
				}
				else
				{
					if(k!=chain.length())
					{
						sing = null;
						break;
					}
					if(PRINT_FOLLOW_CHAIN){
						System.out.printf("Adding to existing chain\n");
					}
					/* Just add to this chain */

					chain.addSol(next_sol);

					dx = ((float) chain.getSol(chain.length()-2).xl) / chain.getSol(chain.length()-2).denom
					- ((float) chain.getSol(chain.length()-1).xl) / chain.getSol(chain.length()-1).denom;
					dy = ((float) chain.getSol(chain.length()-2).yl) / chain.getSol(chain.length()-2).denom
					- ((float) chain.getSol(chain.length()-1).yl) / chain.getSol(chain.length()-1).denom;
					dz = ((float) chain.getSol(chain.length()-2).zl) / chain.getSol(chain.length()-2).denom
					- ((float) chain.getSol(chain.length()-1).zl) / chain.getSol(chain.length()-1).denom;
					chain.metLens[chain.length()-2] = Math.sqrt( dx * dx + dy * dy + dz * dz);
					chain.metric_length += chain.metLens[chain.length()-2];

					nl = next_nl;
					sing = next_sing;

					if(sing==null) break;	/* reached the end of the chain */
					continue;
				}
			} /* end for i */

			if(sing==null) break;	/* reached the end of the chain */
		} /* end while */
	}

	private void printnode_link(Node_link_info nl) {
		System.out.print(nl);
	}

	private void printsing(Sing_info sing) {
		System.out.print(sing);
		
	}

	/*****	Combining Facets Routines **************************************/

	boolean sol_on_box_boundary_or_halfplane(Box_info box,Sol_info sol,Key3D plane)
	{
		boolean testX,testY,testZ;

		testX = (   sol.xl * box.denom == box.xl * sol.denom 
				|| sol.xl * box.denom == (box.xl+1) * sol.denom );
		testY = (   sol.yl * box.denom == box.yl * sol.denom 
				|| sol.yl * box.denom == (box.yl+1) * sol.denom );
		testZ = (   sol.zl * box.denom == box.zl * sol.denom 
				|| sol.zl * box.denom == (box.zl+1) * sol.denom );

		if(plane == X_AXIS)
			testY = testY
			|| 2 * sol.yl * box.denom == (2*box.yl+1) * sol.denom;
		if(plane == FACE_DD || plane == X_AXIS)
			testZ = testZ 
			|| 2 * sol.zl * box.denom == (2*box.zl+1) * sol.denom;

		switch(sol.type)
		{
		case X_AXIS:
			return testY && testZ;
		case Y_AXIS:
			return testX && testZ;
		case Z_AXIS:
			return testX && testY;
		default:
			System.out.printf("sol_on_bny_hlafplane bad Key3D %d %d\n",sol.type,plane);
			return true;
		}
		//return false;
	}

	boolean sol_on_box_boundary(Box_info box,Sol_info sol)
	{
		return sol_on_box_boundary_or_halfplane(box,sol,NONE);
	}

	void remove_sols_not_on_boundary(Facet_info facet1,Box_info box,Key3D type)
	{
		Facet_sol cur;

		for(cur = facet1.sols;cur!=null;cur=cur.next)
		{
			if(cur.sol.type.compareTo(Z_AXIS)<=0
					&& !sol_on_box_boundary_or_halfplane(box,cur.sol,type) )
			{
				/*
		System.out.printf("removing_sol plane %d (%d,%d,%d)/%d\n",type,box.xl,box.yl,box.zl,box.denom);
		print_sol(cur.sol);
				 */
				remove_sol_from_facet(facet1,cur);
				remove_sols_not_on_boundary(facet1,box,type);
				return;
			}
		}
	}

	void remove_repeated_and_hanging_points(Facet_info facet1)
	{
		Facet_sol prev, cur, next;
		int count=0;

		for(cur = facet1.sols;cur!=null;cur=cur.next)
		{	
			if(cur.next == null) prev = cur;
			++count; 
		}

		if(count<2) return;

		prev = null;
		for(cur = facet1.sols;cur!=null;cur=cur.next)
		{
			next = cur.next;
			if(next==null) next = facet1.sols;

			if(cur.sol == next.sol )
			{
				remove_sol_from_facet(facet1,cur);
				remove_repeated_and_hanging_points(facet1);
				return;
			}
			prev=cur;
		}
		if(count<3) return;
		for(cur = facet1.sols;cur!=null;prev=cur,cur=cur.next)
		{
			next = cur.next;
			if(next==null) next = facet1.sols;

			if(prev.sol == next.sol )
			{
				remove_sol_from_facet(facet1,cur);
				remove_repeated_and_hanging_points(facet1);
				return;
			}
		}
	}

//	void fix_facets(Facet_info facets,Box_info box,Key3D type)
//	{
//		Facet_info f2;
//
//		for(f2=facets;f2!=null;f2=f2.next)
//		{
//			remove_repeated_and_hanging_points(f2);
//			remove_sols_not_on_boundary(f2,box,type);
//		}
//	}

	boolean is_node_link(Sol_info fs1,Sol_info fs2)
	{
		switch(fs1.type)
		{
		case X_AXIS: case Y_AXIS: case Z_AXIS:
			return false;
		default: break;
		}
		switch(fs2.type)
		{
		case X_AXIS: case Y_AXIS: case Z_AXIS:
			return false;
		default: break;
		}
		return true;
	}

	Facet_sol link_on_facet(Facet_info  facet2,Facet_sol f1a,Facet_sol f1b)
	{
		Facet_sol f2a,f2b;

		for(f2a = facet2.sols;f2a!=null;f2a=f2a.next)
		{
			f2b = f2a.next; if(f2b==null) f2b=facet2.sols;

			if(f1a.sol == f2a.sol && f1b.sol == f2b.sol )
				return(f2a);
			if(f1a.sol == f2b.sol && f1b.sol == f2a.sol )
				return(f2a);
		}
		return null;
	}

	/** if facet1 and facet2 extend facet2 and return true. **/

	Facet_info link_facet(Facet_info facet2,Facet_info facet1)
	{
		Facet_sol f1a,f1b = null,f2a,f2b,fs1,fs2;
		Facet_info facet3;
		int orientation = 0;
		boolean include_next_point;
		boolean missed_prev_point;

		if(PRINT_JOIN_FACETS){
			System.out.printf("link_facet:\n");
		}
		f2a = null;

		for(f1a = facet1.sols;f1a!=null;f1a=f1a.next)
		{
			f1b = f1a.next; if(f1b==null) f1b=facet1.sols;

			if(is_node_link(f1a.sol,f1b.sol))
				continue;
			f2a = link_on_facet(facet2,f1a,f1b);
			if(f2a!=null) break;
		}
		if(f2a==null) return null;

		if(PRINT_JOIN_FACETS){
			System.out.printf("linking_facet:\n");
			print_facet(facet1);
			print_facet(facet2);
		}
		f2b = f2a.next; if(f2b==null) f2b=facet2.sols;

		if(f1a.sol == f2a.sol && f1b.sol == f2b.sol )
			orientation = -1;
		if(f1a.sol == f2b.sol && f1b.sol == f2a.sol )
			orientation = 1;

		/* now add all soln from facet2 add all sols from facet1
				if a sol is on a linking edge and a non linking edge
				add for facet2 but not for facet1 */

		facet3 = make_facet();
		fs1 = f2b;
		missed_prev_point = true;
		while(true)
		{
			fs2 = fs1.next; if(fs2==null) fs2=facet2.sols;
			if(/* is_node_link(fs1.sol,fs2.sol)
				 || */ link_on_facet(facet1,fs1,fs2) == null)
			{
				if(missed_prev_point)
					add_sol_to_facet(facet3,fs1.sol);
				add_sol_to_facet(facet3,fs2.sol);
				missed_prev_point = false;
			}
			else
				missed_prev_point = true;
			fs1 = fs2;
			if(fs1==f2a) break;
		}
		include_next_point = false;
		fs1 = f1b;
		while(true)
		{
			fs2 = fs1.next; if(fs2==null) fs2=facet1.sols;
			if(/* is_node_link(fs1.sol,fs2.sol)
				 || */ link_on_facet(facet2,fs1,fs2) == null)
			{
				if(include_next_point)
				{
					if(orientation==1)
						add_sol_to_facet(facet3,fs1.sol);
					else
						add_sol_to_facet_backwards(facet3,fs1.sol);
				}
				include_next_point = true;
			}
			else
				include_next_point = false;
			fs1 = fs2;
			if(fs1==f1a) break;
		}
		if(PRINT_JOIN_FACETS){
			System.out.printf("linking_facet: done\n");
			print_facet(facet3);
		}
		return(facet3);
	}

	int inc_count;

	void include_facet(List<Facet_info> existing,Facet_info facet1)
	{
		Facet_info target=facet1;

		if(PRINT_JOIN_FACETS){
			System.out.printf("inc_facet %d\n",inc_count);
			print_facet(facet1);
		}
	
		boolean flag = true;
		while(flag) {
			flag = false;
			ListIterator<Facet_info> li = existing.listIterator(); 
			while(li.hasNext())
			{
				Facet_info facet2 = li.next();
				Facet_info facet3 = link_facet(facet2,target);
				if(facet3!=null) {
					target=facet3;
					li.remove();
					flag=true;
				}
			}
		}
		existing.add(target);
	}

	List<Facet_info> include_facets(List<Facet_info> facetlist,List<Facet_info> boxfacets)
	{
		
		if(boxfacets!=null)
		for(Facet_info facet1:boxfacets)
		{
			if(PRINT_JOIN_FACETS){
				print_facet(facet1);
//				++inc_count;
//				if(global_facet_count != -1 && inc_count == global_facet_count )
//				{
//					Facet_sol fs1;
//					Facet_info facet2;
//					print_facet(facet1);
//					facet2 = make_facet();
//					for(fs1=facet1.sols;fs1!=null;fs1=fs1.next)
//						add_sol_to_facet(facet2,fs1.sol);
//					if(facetlist==null)
//						facetlist = new ArrayList<Facet_info>();
//					facetlist.add(facet2);
//					//return facet2;
//				}
//				if(global_facet_count != -1 && inc_count > global_facet_count )
//					return facetlist;// facetlist;
			}
			if(facetlist==null)
				facetlist = new ArrayList<Facet_info>();
			include_facet(facetlist,facet1);
		}
		return facetlist;
	}

	/*
	 * Function:	combine_facets
	 * Action:	combines all the facets in the sub boxes
	 *		to form the facets of the main box.
	 *		Removes the facets from the sub boxes.
	 */

	void combine_facets(Box_info box)
	{

		if(PRINT_COMBINE_FACETS){
			System.out.printf("Combine facets (%d,%d,%d)/%d\n",box.xl,box.yl,box.zl,box.denom);
			System.out.printf("lfd "); print_facets(box.lfd.facets);
			System.out.printf("lfu "); print_facets(box.lfu.facets);
			System.out.printf("lbd "); print_facets(box.lbd.facets);
			System.out.printf("lbu "); print_facets(box.lbu.facets);
			System.out.printf("rfd "); print_facets(box.rfd.facets);
			System.out.printf("rfu "); print_facets(box.rfu.facets);
			System.out.printf("rbd "); print_facets(box.rbd.facets);
			System.out.printf("rbu "); print_facets(box.rbu.facets);
		}
		inc_count=0;
		box.facets = null;
		box.facets = include_facets(box.facets,box.lbd.facets);
		box.facets = include_facets(box.facets,box.lbu.facets);
		box.facets = include_facets(box.facets,box.lfd.facets);
		box.facets = include_facets(box.facets,box.lfu.facets);
		box.facets = include_facets(box.facets,box.rbd.facets);
		box.facets = include_facets(box.facets,box.rbu.facets);
		box.facets = include_facets(box.facets,box.rfd.facets);
		box.facets = include_facets(box.facets,box.rfu.facets);

		fix_facets(box.facets,box,NONE);

		free_facet_list(box.lbd.facets);
		free_facet_list(box.lbu.facets);
		free_facet_list(box.lfd.facets);
		free_facet_list(box.lfu.facets);
		free_facet_list(box.rbd.facets);
		free_facet_list(box.rbu.facets);
		free_facet_list(box.rfd.facets);
		free_facet_list(box.rfu.facets);

		if(PRINT_COMBINE_FACETS){
			System.out.printf("Combine facets done\n");
			print_facets(box.facets);
		}

	}

	private void free_facet_list(List<Facet_info> facets) {
		// TODO Auto-generated method stub
		
	}

	private void fix_facets(List<Facet_info> facets, Box_info box, Key3D type) {

		if(facets!=null)
		for(Facet_info f2:facets)
		{
			remove_repeated_and_hanging_points(f2);
			remove_sols_not_on_boundary(f2,box,type);
		}

	}


	private void print_facets(List<Facet_info> facets) {
		for(Facet_info facet:facets)
			System.out.print(facet);
	}

	/********** Construct cycles round the boundary of box **************/

//	boolean get_next_link(Box_info box,Sol_info presentsol,Link_info currentlink,Key3D cycle)
//	{
//		Link_info link;
//		Link_info alllinks,matchinglinks[]=new Link_info[4];
//		Face_info face;
//		int count;
//
//		/* A link so loop through all the links starting from... */
//
//		if(currentlink == null) return 0;
//
//		link = (currentlink).next;
//
//		do
//		{
//			/*
//		print_link(link);
//			 */
//			if(link == null)
//			{
//				switch( cycle )
//				{
//				case FACE_LL: cycle = FACE_RR;
//				link = box.rr.links; break;
//				case FACE_RR: cycle = FACE_FF;
//				link = box.ff.links; break;
//				case FACE_FF: cycle = FACE_BB;
//				link = box.bb.links; break;
//				case FACE_BB: cycle = FACE_DD;
//				link = box.dd.links; break;
//				case FACE_DD: cycle = FACE_UU;
//				link = box.uu.links; break;
//				case FACE_UU: cycle = FACE_LL;
//				link = box.ll.links; break;
//				default:
//					System.out.printf("get_next_link: bad type %d\n",cycle);
//					return false;
//				}
//			}
//			else if(link == currentlink )
//			{
//				System.out.printf("gone all the way round\n");
//				if(PRINT_FACET_ERR){
//					printbox(box);
//				}
//				currentlink = null;
//				return false;
//			}
//			else if(link.plotted )
//			{
//				link = link.next;
//			}
//			else if(link.A == presentsol)
//			{
//				break;
//			}
//			else if(link.B == presentsol )
//			{
//				break;
//			}
//			else
//			{
//				link = link.next;
//			}
//
//		} while(true);  /* end do loop */
//
//		/* May have the case 
//				<>< i.e. a pair of links which link two nodes together 
//
//				find all the matching links
//		 */
//
//		switch( cycle )
//		{
//		case FACE_LL: face = box.ll; break;
//		case FACE_RR: face = box.rr; break;
//		case FACE_FF: face = box.ff; break;
//		case FACE_BB: face = box.bb; break;
//		case FACE_DD: face = box.dd; break;
//		case FACE_UU: face = box.uu; break;
//		default: break;
//		}
//		alllinks = face.links;
//		count = 0;
//		for(;alllinks != null;alllinks=alllinks.next)
//		{
//			if(alllinks.plotted) continue;
//			if(alllinks.A == presentsol || alllinks.B == presentsol)
//			{
//				if(count<4)
//					matchinglinks[count++] = alllinks;
//			}
//		}
//		if(count >1 )
//		{
//			if(PRINT_FACET_ERR){
//				int i;
//				System.out.printf("get_next_link: Potential double link %d\n",count);
//				print_link(currentlink);
//				for(i=0;i<count;++i)
//					print_link(matchinglinks[i]);
//				
//			} else {
//					System.out.printf("get_next_link: Potential double link %d\n",count);
//			}
//		}
//		if(count==3)
//		{
//			if(matchinglinks[0].A.type.compareTo(FACE_LL)>=0
//					&& matchinglinks[0].B.type.compareTo(FACE_LL)>=0 )
//			{
//				link = matchinglinks[0];
//			}
//			else if(matchinglinks[1].A.type.compareTo(FACE_LL)>=0
//					&& matchinglinks[1].B.type.compareTo(FACE_LL)>=0 )
//			{
//				link = matchinglinks[1];
//			}
//			else if(matchinglinks[2].A.type.compareTo(FACE_LL)>=0
//					&& matchinglinks[2].B.type.compareTo(FACE_LL)>=0 )
//			{
//				link = matchinglinks[2];
//			}
//			if(PRINT_FACET_ERR){
//				System.out.printf("Using link\n");
//				print_link(link);
//			}
//		}
//
//
//		if(link.A == presentsol)
//		{
//			link.plotted = true;
//			presentsol = link.B;
//			currentlink = link;
//		}
//		else if(link.B == presentsol )
//		{
//			link.plotted = true;
//			presentsol = link.A;
//			currentlink = link;
//		}
//		else
//		{
//			System.out.printf("get_next_link: wierdness\n");
//			return false;
//		}
//		return true;
//	}

	@SuppressWarnings("unused")
	private void print_link(Link_info currentlink) {
		System.out.print(currentlink);
		
	}

	@SuppressWarnings("unused")
	private void printbox(Box_info box) {
		System.out.print(box);
		
	}

	/*
	 * Function:	plot_facet
	 * action:	starting from a link, loop all the way round until
	 *		you get back to the begining.
	 */

	void create_facet(Link_info startlink,List<Link_info> list)
	{
		Link_info link;
		Sol_info startingsol,presentsol;
		Facet_info f;

		/* Now have a link which has not been plotted */

		link = startlink;

		f = add_facet();
		add_sol_to_facet(f,link.A);
		add_sol_to_facet(f,link.B);

		link.plotted = true;
		startingsol = link.A;
		presentsol = link.B;

		while( presentsol != startingsol )
		{
			boolean triedAll=true;
			for(Link_info link2:list) {
				if(link2.plotted) continue;
				
				if(link2.A == presentsol) {
					if(link2.B != startingsol) 
						add_sol_to_facet(f,link2.B);
					presentsol = link2.B;
					link2.plotted = true;
					triedAll=false;
					break;
				}
				if(link2.B == presentsol) {
					if(link2.A != startingsol) 
						add_sol_to_facet(f,link2.A);
					presentsol = link2.A;
					link2.plotted = true;
					triedAll=false;
					break;
				}
			}
			if(triedAll) {
				if(PRINT_NO_MATCH_LINK) {
					System.out.println("create_facet: no matching link found");
					System.out.print(startingsol);
					System.out.print(presentsol);
					System.out.print(list);
				}
				break;
			}
		}
	}

	void create_3node_link_facets(Box_info box)
	{
		
		int i=0;
		if(box.node_links!=null)
		for(Node_link_info nl:box.node_links) ++i;
		if(i<3) return;
		//TODO
	}

	/********** Main entry point for routines *****************/


	void make_facets(Box_info box)
	{
		all_facets = null;

		if(box.lfd != null)
		{
			make_facets(box.lfd);
			make_facets(box.lfu);
			make_facets(box.lbd);
			make_facets(box.lbu);
			make_facets(box.rfd);
			make_facets(box.rfu);
			make_facets(box.rbd);
			make_facets(box.rbu);
		
			if(PLOT_SMALL_FACETS) {
				box.facets = new ArrayList<Facet_info>();
				if(box.lfd.facets!=null)
				    box.facets.addAll(box.lfd.facets);
	                        if(box.lfu.facets!=null)
	                            box.facets.addAll(box.lfu.facets);
	                        if(box.lbd.facets!=null)
	                            box.facets.addAll(box.lbd.facets);
	                        if(box.lbu.facets!=null)
	                            box.facets.addAll(box.lbu.facets);
	                        if(box.rfd.facets!=null)
	                            box.facets.addAll(box.rfd.facets);
	                        if(box.rfu.facets!=null)
	                            box.facets.addAll(box.rfu.facets);
	                        if(box.rbd.facets!=null)
	                            box.facets.addAll(box.rbd.facets);
	                        if(box.rbu.facets!=null)
	                            box.facets.addAll(box.rbu.facets);
				
			}
			else
			{
			combine_facets(box);
			clean_facets(box);
			}
			return;
		}

		collect_sings(box);

		make_chains(box);

		if(PRINT_DRAW_BOX){
			System.out.printf("\nmake_facets: box (%d,%d,%d)/%d\n",
					box.xl,box.yl,box.zl,box.denom);
			print_box_brief(box);
			print_chains(box.chains);
		}



		/*** First find a link to start from. ***/

		List<Link_info> listOfAllLinks = new ArrayList<Link_info>();
		if(box.ll.links!=null)
		listOfAllLinks.addAll(box.ll.links);
		if(box.rr.links!=null)
		listOfAllLinks.addAll(box.rr.links);
		if(box.ff.links!=null)
		listOfAllLinks.addAll(box.ff.links);
		if(box.bb.links!=null)
		listOfAllLinks.addAll(box.bb.links);
		if(box.uu.links!=null)
		listOfAllLinks.addAll(box.uu.links);
		if(box.dd.links!=null)
		listOfAllLinks.addAll(box.dd.links);

		for(Link_info link:listOfAllLinks)
			link.plotted=false;
		
			for(Link_info link:listOfAllLinks)
				 if(!(link.plotted) )
					create_facet(link,listOfAllLinks);

		if(PRINT_DRAW_BOX){
			System.out.printf("Initial facets\n");
			print_facets(all_facets);
		}
		/* Now divide up the facets */


		create_3node_link_facets(box);

		if(global_do_refine)
			refine_facets(box);

		if(PRINT_DRAW_BOX){
			System.out.printf("Final facets\n");
			print_facets(all_facets);
		}

		box.facets = all_facets;
		all_facets = null;

		clean_facets(box);
	}

	/**
	 * This version calculates factes using refined versions of the faces.
	 * @param box
	 */
	void make_little_facets(Box_info box)
	{
//	    if(box.denom==8) {
//	    if( (box.xl==3 || box.xl ==4) && box.yl ==2 && box.zl == 4) {
//	        System.out.println(box.toString());
//	        
//	    }
//	    }
	    
	    all_facets = null;

	    if(box.lfd != null)
	    {
	        make_little_facets(box.lfd);
	        make_little_facets(box.lfu);
	        make_little_facets(box.lbd);
	        make_little_facets(box.lbu);
	        make_little_facets(box.rfd);
	        make_little_facets(box.rfu);
	        make_little_facets(box.rbd);
	        make_little_facets(box.rbu);

	        box.facets = new ArrayList<Facet_info>();
	        if(box.lfd.facets!=null)
	            box.facets.addAll(box.lfd.facets);
	        if(box.lfu.facets!=null)
	            box.facets.addAll(box.lfu.facets);
	        if(box.lbd.facets!=null)
	            box.facets.addAll(box.lbd.facets);
	        if(box.lbu.facets!=null)
	            box.facets.addAll(box.lbu.facets);
	        if(box.rfd.facets!=null)
	            box.facets.addAll(box.rfd.facets);
	        if(box.rfu.facets!=null)
	            box.facets.addAll(box.rfu.facets);
	        if(box.rbd.facets!=null)
	            box.facets.addAll(box.rbd.facets);
	        if(box.rbu.facets!=null)
	            box.facets.addAll(box.rbu.facets);
	        return;
	    }

	    collect_sings(box);

	    make_chains(box);

	    if(PRINT_DRAW_BOX){
	        System.out.printf("\nmake_facets: box (%d,%d,%d)/%d\n",
	                box.xl,box.yl,box.zl,box.denom);
	        print_box_brief(box);
	        print_chains(box.chains);
	    }



	    /*** First find a link to start from. ***/

	    List<Link_info> listOfAllLinks = new ArrayList<Link_info>();
	    addFaceLinks(listOfAllLinks,box.ll);
	    addFaceLinks(listOfAllLinks,box.rr);
	    addFaceLinks(listOfAllLinks,box.ff);
	    addFaceLinks(listOfAllLinks,box.bb);
	    addFaceLinks(listOfAllLinks,box.uu);
	    addFaceLinks(listOfAllLinks,box.dd);

	    for(Link_info link:listOfAllLinks)
	        link.plotted=false;

	    for(Link_info link:listOfAllLinks)
	        if(!(link.plotted) )
	            create_facet(link,listOfAllLinks);

//            if( box.xl==3 && box.yl ==2 && box.zl == 4) {
//                System.out.println(listOfAllLinks);
//	        System.out.println(all_facets);
//	    }

	    if(PRINT_DRAW_BOX){
	        System.out.printf("Initial facets\n");
	        print_facets(all_facets);
	    }
	    /* Now divide up the facets */


	    create_3node_link_facets(box);

	    if(global_do_refine)
	        refine_facets(box);

            if( box.xl==3 && box.yl ==2 && box.zl == 4) {
	        System.out.println(all_facets);
	    }

	    if(PRINT_DRAW_BOX){
	        System.out.printf("Final facets\n");
	        print_facets(all_facets);
	    }

	    box.facets = all_facets;
	    all_facets = null;

	    //clean_facets(box);

	}

	       private void addFaceLinks(List<Link_info> listOfAllLinks,Face_info face) {
	           if(face==null) return;
	           if(face.lb != null) {
	               addFaceLinks(listOfAllLinks,face.lb);
                       addFaceLinks(listOfAllLinks,face.lt);
                       addFaceLinks(listOfAllLinks,face.rb);
                       addFaceLinks(listOfAllLinks,face.rt);
	           }
	           else if(face.links!=null)
	               listOfAllLinks.addAll(face.links);
	       }
	       
	private void print_box_brief(Box_info box) {
		System.out.print(box.print_box_brief());
		
	}

	private void collect_sings(Box_info box) {
		box.collect_sings();
	}


	/**
	 * dose a final cleanup of facets
	 * removes thin identicle facets
	 * removes any sols not on boundary
	 */

	void clean_facets(Box_info box)
	{
		boolean more_to_do;

		Facet_sol fs1,fs2,next,prev;

		if(box.facets==null)
			return;
		for(Facet_info facet1:box.facets)
		{
			prev=null;
			for(fs1=facet1.sols;fs1!=null;fs1=next)
			{
				next=fs1.next;
				if(fs1.sol.type.compareTo(Z_AXIS)<=0
						&& !sol_on_box_boundary(box,fs1.sol) )
				{
					if(prev==null) facet1.sols = next;
					else		prev.next = next;
				}
				else prev=fs1;
			}
		}

		more_to_do =true;
		while(more_to_do)
		{
			more_to_do = false;
			Facet_info removeThis=null;
			ListIterator<Facet_info> it1 = box.facets.listIterator();
			outer: while(it1.hasNext()) {
				Facet_info facet1 = it1.next();
				int nextIndex = it1.nextIndex();
				ListIterator<Facet_info> it2 = box.facets.listIterator(nextIndex);
				while(it2.hasNext()) {
					Facet_info facet2 = it2.next();
// 			for(Facet_info facet1:box.facets)
//			for(facet1=box.facets;facet1!=null;facet1=facet1.next)
//			{
//				for(facet2=facet1.next;facet2!=null;facet2=facet2.next)
//				{
					boolean matched_all_sols = true;

					for(fs1=facet1.sols;fs1!=null;fs1=fs1.next)
					{
						boolean matched_sol=false;
						for(fs2=facet2.sols;fs2!=null;fs2=fs2.next)
							if(fs1.sol == fs2.sol)
							{	
								matched_sol = true; 
								break;
							}
						if(!matched_sol)
						{
							matched_all_sols = false;
							break;
						}
					}

					for(fs1=facet2.sols;fs1!=null;fs1=fs1.next)
					{
						boolean matched_sol=false;
						for(fs2=facet1.sols;fs2!=null;fs2=fs2.next)
							if(fs1.sol == fs2.sol)
							{	
								matched_sol = true; 
								break;
							}
						if(!matched_sol)
						{
							matched_all_sols = false;
							break;
						}
					}

					if(matched_all_sols)
					{
						/* now remove facet1 and facet2 from list */
						it1.remove();
						removeThis = facet2;
//						it2.remove();
//						box.facets.remove(facet1);
//						box.facets.remove(facet2);
//						Facet_info facet3,nextfacet,prevfacet;
//						prevfacet = null;
//						for(facet3=box.facets;facet3!=null;facet3=nextfacet)
//						{
//							nextfacet=facet3.next;
//
//							if(facet3==facet1 || facet3==facet2)
//							{
//								if(prevfacet==null) 
//									box.facets = nextfacet;
//								else
//									prevfacet.next = nextfacet;
//							}
//							else
//								prevfacet=facet3;
//						}
						more_to_do = true;
						break outer;
					}
					if(more_to_do) break;
				} /* end facet2 loop */
				if(more_to_do) break;
			} /* end facet1 loop */
			if(removeThis!=null)
				box.facets.remove(removeThis);
		}
		return;
		//fix_crossing_gaps(box);
	}

	private void fix_crossing_gaps(Box_info box)
	{
		
		Facet_sol fs1;
		int sol_count,i,j;
		Sol_info sol_array[],sol_replacements[];

		/** Now want to close up a few gaps which can occur along self intersections.
				it may happen that there are two nodes which are adjacent
				on the same face (may be internal). We want to consolodate
				these nodes into a single one. Always pick the one with
				smallest x (or y (or z)) **/

		sol_count = 0;
		for(Facet_info facet1:box.facets)
			for(fs1=facet1.sols;fs1!=null;fs1=fs1.next)
				if(fs1.sol.type.isFace())
					++sol_count;

		if(sol_count ==0) return;
		sol_array = new Sol_info[sol_count];
		sol_replacements = new Sol_info[sol_count];

		sol_count = 0;
		for(Facet_info facet1:box.facets)
			for(fs1=facet1.sols;fs1!=null;fs1=fs1.next)
				if(fs1.sol.type.isFace())
				{
					boolean matched_sol=false;

					for(i=0;i<sol_count;++i)
					{
						if(sol_array[i] == fs1.sol)
							matched_sol = true;
					}
					if(!matched_sol)
						sol_array[sol_count++] = fs1.sol;
				}

		for(i=0;i<sol_count;++i)
			for(j=i+1;j<sol_count;++j)
			{
				if(sol_array[i].type == sol_array[j].type
						&&  sol_array[i].denom == sol_array[j].denom 
						&&  Math.abs(sol_array[i].xl - sol_array[j].xl) <=1
						&&  Math.abs(sol_array[i].yl - sol_array[j].yl) <=1
						&&  Math.abs(sol_array[i].zl - sol_array[j].zl) <=1 )
				{
					if(sol_array[i].xl < sol_array[j].xl )
						sol_replacements[j] = sol_array[i];
					else if(sol_array[j].xl < sol_array[i].xl )
						sol_replacements[i] = sol_array[j];
					else if(sol_array[i].yl < sol_array[j].yl )
						sol_replacements[j] = sol_array[i];
					else if(sol_array[j].yl < sol_array[i].yl )
						sol_replacements[i] = sol_array[j];
					else if(sol_array[i].zl < sol_array[j].zl )
						sol_replacements[j] = sol_array[i];
					else if(sol_array[j].zl < sol_array[i].zl )
						sol_replacements[i] = sol_array[j];
					//TODO else if(sol_array[i] < sol_array[j])	/* comparing pointers */
						sol_replacements[j] = sol_array[i];
					//else
						//sol_replacements[i] = sol_array[j];
				}
			}

		for(Facet_info facet1:box.facets)
			for(fs1=facet1.sols;fs1!=null;fs1=fs1.next)
				if(fs1.sol.type.isFace())
					for(i=0;i<sol_count;++i)
						if(sol_array[i] == fs1.sol)
						{
							if(sol_replacements[i]!=null)
								fs1.sol = sol_replacements[i];
						}
		free(sol_array);
		free(sol_replacements);
	}								







	private void free(Sol_info[] solArray) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("unused")
	private void vec_sub(double[] v1,double[] v2,double[] v3) {
		v1[0] = v2[0]-v3[0]; v1[1] = v2[1]-v3[1]; v1[2] = v2[2]-v3[2]; }
	
	@SuppressWarnings("unused")
	private void vec_cross(double[] v1,double[] v2,double[] v3) {
	v1[0] = v2[1] * v3[2] - v2[2] * v3[1]; 
	v1[1] = v2[2] * v3[0] - v2[0] * v3[2]; 
	v1[2] = v2[0] * v3[1] - v2[1] * v3[0]; }
	@SuppressWarnings("unused")
	private double vec_dot(double v1[],double v2[]) {
		return (v1[0]*v2[0]+v1[1]*v2[1]+v1[2]*v2[2]);
	}
	@SuppressWarnings("unused")
	private void print_vec(String s1,double v1[]) { System.out.printf("%s %f %f %f\n",s1,v1[0],v1[1],v1[2]); }

}