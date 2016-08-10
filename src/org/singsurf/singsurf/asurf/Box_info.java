/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class Box_info {
    private static final boolean NOT_DEF = false;
	private static final boolean COLLECT = false;
	int xl,yl,zl,denom;
    Key3D type;
    int status;
	short num_sings;
    Box_info lfd,lfu,lbd,lbu,rfd,rfu,rbd,rbu;
    Face_info ll,rr,ff,bb,dd,uu;
    List<Node_link_info> node_links;
    List<Chain_info> chains;
    List<Sing_info> sings;
    List<Facet_info> facets;
    
    public Box_info(int xl,int yl,int zl,int denom)
    {
    	this.xl = xl;
    	this.yl = yl;
    	this.zl = zl;
    	this.denom = denom;
    	this.status = BoxClevA.EMPTY;
    	this.lfu = this.lfd = this.lbu = this.lbd = null;
    	this.rfu = this.rfd = this.rbu = this.rbd = null;
    	this.ll = this.rr = this.ff = this.bb = this.dd = this.uu = null;
    	this.node_links = null;
    	this.sings = null;
    	this.chains = null;
    	this.facets = null;
    }

    
    /*
     * Function:	make_box_face
     * action:	let face contain the info about the face 'type' of 'box'.
     */

    Face_info make_box_face(Key3D type)
    {
    	switch(type)
    	{
    	case FACE_LL:
    		return new Face_info(Key3D.FACE_LL,this.xl,this.yl,this.zl,this.denom);
    	case FACE_RR:
    		return new Face_info(Key3D.FACE_RR,this.xl+1,this.yl,this.zl,this.denom);
    	case FACE_FF:
    		return new Face_info(Key3D.FACE_FF,this.xl,this.yl,this.zl,this.denom);
    	case FACE_BB:
    		return new Face_info(Key3D.FACE_BB,this.xl,this.yl+1,this.zl,this.denom);
    	case FACE_DD:
    		return new Face_info(Key3D.FACE_DD,this.xl,this.yl,this.zl,this.denom);
    	case FACE_UU:
    		return new Face_info(Key3D.FACE_UU,this.xl,this.yl,this.zl+1,this.denom);
    	default:
    		System.err.printf("make_box_face: bad type %s",type.toString());
    		break;
    	}
    	return null;
    }

    /*
     * Function:	sub_devide_box
     * action:	create the apropriate information for all the sub boxes,
     *		does not play about with the faces.
     */

    void sub_devide_box()
    {
    	this.lfd = new Box_info(2*this.xl,  2*this.yl,  2*this.zl,  2*this.denom);
    	this.rfd = new Box_info(2*this.xl+1,2*this.yl,  2*this.zl,  2*this.denom);
    	this.lbd = new Box_info(2*this.xl,  2*this.yl+1,2*this.zl,  2*this.denom);
    	this.rbd = new Box_info(2*this.xl+1,2*this.yl+1,2*this.zl,  2*this.denom);
    	this.lfu = new Box_info(2*this.xl,  2*this.yl,  2*this.zl+1,2*this.denom);
    	this.rfu = new Box_info(2*this.xl+1,2*this.yl,  2*this.zl+1,2*this.denom);
    	this.lbu = new Box_info(2*this.xl,  2*this.yl+1,2*this.zl+1,2*this.denom);
    	this.rbu = new Box_info(2*this.xl+1,2*this.yl+1,2*this.zl+1,2*this.denom);
    }

    /*
     * Function:	calc_pos_in_box
     * action:	vec is the position of sol on the box
     */

    double[] calc_pos_in_box(Sol_info sol)
    {
    	double vec[]=new double[3];
    	switch(sol.type)
    	{
    	case X_AXIS:
    		vec[0] = this.denom * (sol.xl+sol.root)/sol.denom - this.xl;
    		vec[1] = this.denom * (sol.yl)/sol.denom - this.yl;
    		vec[2] = this.denom * (sol.zl)/sol.denom - this.zl;
    		break;

    	case Y_AXIS:
    		vec[0] = this.denom * (sol.xl)/sol.denom - this.xl;
    		vec[1] = this.denom * (sol.yl+sol.root)/sol.denom - this.yl;
    		vec[2] = this.denom * (sol.zl)/sol.denom - this.zl;
    		break;

    	case Z_AXIS:
    		vec[0] = this.denom * (sol.xl)/sol.denom - this.xl;
    		vec[1] = this.denom * (sol.yl)/sol.denom - this.yl;
    		vec[2] = this.denom * (sol.zl+sol.root)/sol.denom - this.zl;
    		break;

    	case FACE_LL: case FACE_RR:
    		vec[0] = this.denom * (sol.xl)/sol.denom - this.xl;
    		vec[1] = this.denom * (sol.yl+sol.root)/sol.denom - this.yl;
    		vec[2] = this.denom * (sol.zl+sol.root2)/sol.denom - this.zl;
    		break;
    	case FACE_FF: case FACE_BB:
    		vec[0] = this.denom * (sol.xl+sol.root)/sol.denom - this.xl;
    		vec[1] = this.denom * (sol.yl)/sol.denom - this.yl;
    		vec[2] = this.denom * (sol.zl+sol.root2)/sol.denom - this.zl;
    		break;
    	case FACE_DD: case FACE_UU:
    		vec[0] = this.denom * (sol.xl+sol.root)/sol.denom - this.xl;
    		vec[1] = this.denom * (sol.yl+sol.root2)/sol.denom - this.yl;
    		vec[2] = this.denom * (sol.zl)/sol.denom - this.zl;
    		break;

    	case BOX:
    		vec[0] = this.denom * (sol.xl+sol.root)/sol.denom - this.xl;
    		vec[1] = this.denom * (sol.yl+sol.root2)/sol.denom - this.yl;
    		vec[2] = this.denom * (sol.zl+sol.root3)/sol.denom - this.zl;
    		break;

    	default:
    		System.err.printf("calc_pos_in_box: bad types ");
    		System.err.printf(" sol %s",sol.type.toString());
    		System.err.printf("\n");
    		break;
    	}
    	return vec;
    }
    
    

    void add_node_link_simple(Node_info node1, Node_info node2)
    {
    	Node_link_info link = new Node_link_info(node1,node2,LinkStatus.NODE);
    	if(node_links==null)
    		node_links = new ArrayList<Node_link_info>();
    	node_links.add(link);
    }

    /*
     * Function:	add_node_link
     * action:	given a link between two nodes and a list of exsisting
     *		links in the box do the following:
     *		if neither sol in list add link to list;
     *		if one sol matches a sol in list extend the existing link;
     *		if link joins two exsisting links remove one and
     *		join the two together.
     *
     *		basically do the right thing to the list with the given link.
     */

    void add_node_link(Node_info node1, Node_info node2)
    {
    	Node_link_info link1=null, link2=null;
    	boolean link1_keepA = false;
		boolean link2_keepA = false;

    if(NOT_DEF) {
    	System.err.printf("include_node_link\n");
    	System.err.print(node1.sol);
    	System.err.print(node2.sol);
    	System.err.print(this);
    	
    }

    	if(node_links==null)
    	{
    		add_node_link_simple(node1,node2);
    		return;
    	}
    	for(Node_link_info link:node_links)
    	{
    		if( node1 == link.A && node1.sol.type != Key3D.BOX )
    		{
    			link1 = link;
    			if( link.B == null )
    			{
    				link.B = node2;
    				link1_keepA = true;
    			}
    			else
    			{
    				link.A = node2;
    				link1_keepA = false;
    			}
    		}
    		else if( node1 == link.B && node1.sol.type != Key3D.BOX )
    		{
    			link1 = link;
    			if( link.A == null )
    			{
    				link.A = node2;
    				link1_keepA = false;
    			}
    			else
    			{
    				link.B = node2;
    				link1_keepA = true;
    			}
    		}
    		else if( node2 == link.A && node2.sol.type != Key3D.BOX )
    		{
    			link2 = link;
    			if( link.B == null )
    			{
    				link.B = node1;
    				link2_keepA = true;
    			}
    			else
    			{
    				link.A = node1;
    				link2_keepA = false;
    			}
    		}
    		else if( node2 == link.B && node2.sol.type != Key3D.BOX )
    		{
    			link2 = link;
    			if( link.A == null )
    			{
    				link.A = node1;
    				link2_keepA = false;
    			}
    			else
    			{
    				link.B = node1;
    				link2_keepA = true;
    			}
    		}

    		
    	} /* end for */

    	if( link1 == null && link2 == null )	/* Didn't find link add it */
    	{
    		add_node_link_simple(node1,node2);
    	}
    	else if( link1 != null ) /* join two links together */
    	{
    		if( link2 != null )
    		{
    			if( link1_keepA )
    				if( link2_keepA )
    					link1.B = link2.A;
    				else
    					link1.B = link2.B;
    			else
    				if( link2_keepA )
    					link1.A = link2.A;
    				else
    					link1.A = link2.B;

    			node_links.remove(link2);
    		}

    		/*** Do nothing if only one end of a simple link. ***/
    	}
    }

    /*
     * Function:	add_sing(box,sol)
     * action:	add the sing to the list for the this.
     *		simple version where we don't try to join sings together.
     */

    void add_sing(Sol_info sol)
    {
    	Sing_info sing = new Sing_info(sol,LinkStatus.NODE);
    	if(sings==null)
    		sings=new ArrayList<Sing_info>();
    	sings.add(sing);
    }

  

    public String print_nodes_on_box()
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append(ll.print_nodes_on_face());
    	sb.append(rr.print_nodes_on_face());
    	sb.append(ff.print_nodes_on_face());
    	sb.append(bb.print_nodes_on_face());
    	sb.append(dd.print_nodes_on_face());
    	sb.append(uu.print_nodes_on_face());
    	return sb.toString();
    }

    public String print_box_header()
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append(String.format("BOX: (%d,%d,%d)/%d status %d\n",
    		xl,yl,zl,denom,status));
    	return sb.toString();
    }

    public String print_box_brief()
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append(String.format("BOX: (%d,%d,%d)/%d status %d\n",
    		xl,yl,zl,denom,status));
    	sb.append(this.print_nodes_on_box());
    	if(sings!=null)
    		for(Sing_info sing:sings)
    			sb.append(sing);
    	if(node_links!=null)
    		for(Node_link_info link:node_links)
    			sb.append(link);
    	return sb.toString();
    }

	@Override
	public String toString() {
    	StringBuilder sb = new StringBuilder();

    	sb.append(String.format("BOX: (%d,%d,%d)/%d status %d\n",
    			xl,yl,zl,denom,status));
    	
    	sb.append("face ll :"); 
    	if(ll!=null) sb.append(ll.toString()); else sb.append("null\n");
    	sb.append("face rr :"); 
    	if(rr!=null) sb.append(rr.toString()); else sb.append("null\n");
    	sb.append("face ff :"); 
    	if(ff!=null) sb.append(ff.toString()); else sb.append("null\n");
    	sb.append("face bb :"); 
    	if(bb!=null) sb.append(bb.toString()); else sb.append("null\n");
    	sb.append("face uu :"); 
    	if(uu!=null) sb.append(uu.toString()); else sb.append("null\n");
    	sb.append("face dd :"); 
    	if(dd!=null) sb.append(dd.toString()); else sb.append("null\n");
    	
    	if(sings!=null)
    		for(Sing_info sing:sings)
    			sb.append(sing);
    	else
    		sb.append("no sings\n");
    	
    	if(node_links!=null)
    		for(Node_link_info link:node_links)
    			sb.append(link);
    	else
    		sb.append("no node links\n");
    	
    	if(chains!=null)
    		for(Chain_info chain:chains)
        			sb.append(chain);
    	else
    		sb.append("no chains\n");


		return sb.toString();
	}

	/*
	 * Function:	collect_sings
	 * action:	simplyfy the list of node_links and sings
	 *		for each sing check whats adjacient to it
	 *		if there are only two adjaciencies eliminate the node
			if a node_link is adjacient to a sing then change the
			face to be a node.
	 */

	void collect_sings()
	{
		Node_link_info n2;//,p1,p2;
		//Sing_info sing;

		/* remove any repeated node links */
	/*
		print_node_links(this.node_links);
	*/
		if(this.node_links!=null) {
		ListIterator<Node_link_info> it1 = this.node_links.listIterator();
		while(it1.hasNext())
		{
			Node_link_info n1;
			n1 = it1.next();
			n1.singA = n1.singB = null;

			int index = it1.nextIndex();
			ListIterator<Node_link_info> it2 = this.node_links.listIterator(index);
			while(it2.hasNext())
			{
				n2 = it2.next();
//			}
//		}
//		for(Node_link_info n1:this.node_links)
//		//for(p1=NULL,n1=this.node_links;n1!=NULL;p1=p1,n1=n1.next)
//		{
//			for(p2=n1,n2=n1.next;n2!=NULL;p2=n2,n2=n2.next)
//			{
				if( (  (n1.A.sol == n2.A.sol )
				    && (n1.B.sol == n2.B.sol ) )
				 || (  (n1.B.sol == n2.A.sol )
				    && (n1.A.sol == n2.B.sol ) ) )
				{
					it1.remove();
					break;
					//node_links.remove(n1);
	/*
	fprintf(stderr,"rm nodel_link\n");
			print_node(n1.A);	
			fprintf(stderr,"\t");
			print_node(n1.B);
			fprintf(stderr,"\n");
			print_node(n2.A);	
			fprintf(stderr,"\t");
			print_node(n2.B);
			fprintf(stderr,"\n");
	*/
					//p2.next = n2.next;
					//free(n2);
//	#ifdef TEST_ALLOC
//			--node_linkcount;
//	#endif
//					n2 = p2;
				}
			}
		}
		}
		/* count up how many node_links adjacent to each sing */

		this.num_sings = 0;
		if(this.sings!=null)
		for(Sing_info sing:this.sings)
		{
			++this.num_sings;
			sing.numNLs = 0;
			if(this.node_links!=null)
			for(Node_link_info n1:this.node_links)
			{
	/*
				if( n1.A.sol.type != BOX
				 && n1.B.sol.type != BOX ) continue;
	*/
				if(n1.A.sol == sing.sing)
				{
					++sing.numNLs;
				}
				if(n1.B.sol == sing.sing)
				{
					++sing.numNLs;
				}
			}
			sing.adjacentNLs = new Node_link_info[sing.numNLs];
//	#ifdef TEST_ALLOC
//			++node_linkcount; ++node_linkmax; ++node_linknew;
//	#endif
			sing.numNLs = 0;
			if(this.node_links!=null)
				for(Node_link_info n1:this.node_links)
			{
	/*
				if( n1.A.sol.type != BOX
				 && n1.B.sol.type != BOX ) continue;
	*/
				if(n1.A.sol == sing.sing)
				{
					sing.adjacentNLs[sing.numNLs++] = n1;
					n1.singA = sing;
				}
				if(n1.B.sol == sing.sing)
				{
					sing.adjacentNLs[sing.numNLs++] = n1;
					n1.singB = sing;
				}
			}


		}


				
	if(COLLECT){

		if(this.sings!=null)
			for(Sing_info sing:this.sings)
		{
			if(this.node_links!=null)
				for(Node_link_info node_link:this.node_links)
			{
				if( adjacient_to_sing(sing,node_link.A.sol) )
				{
	/*
	*/
//					free(node_link.A.sol);
//	#ifdef TEST_ALLOC
//					--solcount;
//	#endif
					node_link.A.sol = sing.sing;
				}
				if( adjacient_to_sing(sing,node_link.B.sol) )
				{
	/*
	*/
//					free(node_link.B.sol);
//	#ifdef TEST_ALLOC
//					--solcount;
//	#endif
					node_link.B.sol = sing.sing;
				}
		    	}
		}

		/*** Now if any node is adjacient to a sing create a node_link ***/
		/*** with the node at one end and the sing at the other.  ***/
		Face_info face = null;
		int num_sols = Topology.count_sols_on_face(face);

		int num_nodes=-1;
		for(int i=1;i<=num_nodes;++i)
		{
			Node_info node = Topology.get_nth_node_on_face(face,i);

			for(int j=1;j<=num_sols;++j)
			{
				Sol_info sol = Topology.get_nth_sol_on_face(face,j);
				if(adjacient_to_node(node,sol) )
				{
					include_link(face,node.sol,sol);
				}
			}
		}
	}
	}



    



	private void include_link(Face_info face, Sol_info sol, Sol_info sol2) {
		// TODO Auto-generated method stub
		throw new RuntimeException("include link in boxclever");
	}


	private boolean adjacient_to_node(Node_info node, Sol_info sol) {
		throw new RuntimeException("adjacient_to_node in boxclever");	
	}


	private boolean adjacient_to_sing(Sing_info sing, Sol_info sol) {
		throw new RuntimeException("adjacient_to_sing in boxclever");	
	}


	public void free_bits_of_box() {

		/*
			free_sings(box->sings);
		*/
			this.sings=null;
			if(this.chains!=null)
			for(Chain_info chain:this.chains) {
				chain.free();
			}
			this.chains=null;

			//this.node_links.free();
			this.node_links=null;
			
			this.ll.free_bits_of_face();
			this.ff.free_bits_of_face();
			this.dd.free_bits_of_face();

			if(this.ll != null && this.ll.x_low!=null ) { 
				this.ll.x_low.free(); this.ll.x_low=null;}
			else if( this.ff != null && this.ff.x_low!=null) {
				this.ff.x_low.free(); this.ff.x_low=null;}

			if(this.ll != null && this.ll.y_low!=null) { 
				this.ll.y_low.free(); this.ll.y_low=null;}
			else if( this.dd != null &&this.dd.x_low!=null ) {
				this.dd.x_low.free(); this.dd.x_low=null;}

			if(this.ff != null && this.ff.y_low!=null) {
				this.ff.y_low.free(); this.ff.y_low=null;}
			else if( this.dd != null && this.dd.y_low!=null) {
				this.dd.y_low.free(); this.dd.y_low=null;}

			this.ll=this.ff=this.dd=null;

	}


	public void dump() {
    	System.out.printf("BOX: (%d,%d,%d)/%d status %d%n",
    		xl,yl,zl,denom,status);
    	if(this.lfd!=null) this.lfd.dump();
    	if(this.lfu!=null) this.lfu.dump();
    	if(this.lbd!=null) this.lbd.dump();	
    	if(this.lbu!=null) this.lbu.dump();
    	if(this.rfd!=null) this.rfd.dump();
    	if(this.rfu!=null) this.rfu.dump();
    	if(this.rbd!=null) this.rbd.dump();
    	if(this.rbu!=null) this.rbu.dump();
	}


	public void free_bit(boolean rr, boolean bb, boolean uu) {

		
    	if(this.lfd!=null) { this.lfd.free_bit(false,false,false); this.lfd = null; }
    	if(this.lfu!=null) { this.lfu.free_bit(false,false,uu); if(!uu) this.lfu=null; }
    	if(this.lbd!=null) { this.lbd.free_bit(false,bb,false); if(!bb) this.lbd=null; }	
    	if(this.lbu!=null) { this.lbu.free_bit(false,bb,uu); if(!bb&&!uu) this.lbu=null; }
    	if(this.rfd!=null) { this.rfd.free_bit(rr,false,false); if(!rr) this.rfd=null; }
    	if(this.rfu!=null) {this.rfu.free_bit(rr,false,uu); if(!rr&&!uu) this.rfu=null; }
    	if(this.rbd!=null) {this.rbd.free_bit(rr,bb,false); if(!rr&&!bb) this.rbd=null; }
    	if(this.rbu!=null) {this.rbu.free_bit(rr,bb,uu); if(!rr&&!bb&&!uu) this.rbu=null; }

    	this.sings=null;
		if(this.chains!=null)
		for(Chain_info chain:this.chains) {
			chain.free();
		}
		this.chains=null;

		this.node_links=null;

		if(!bb&&!uu) { this.ll = null; }
		if(!rr&&!uu) { this.ff = null; }
		if(!rr&&!bb) { this.dd = null; }
		if(!bb&&!rr&&!uu) { this.rr=this.ff=this.dd=null; }
	}
}