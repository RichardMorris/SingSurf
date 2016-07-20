/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Face_info {
	/*** Some definitions to assist in making edges on faces ***/

	public static final int X_LOW =1;
	public static final int  X_HIGH=2;
	public static final int  Y_LOW=3;
	public static final int  Y_HIGH=4;
	public static final int  MID_FACE=5;
	public static final int  X_LOW_Y_LOW=6;
	public static final int  X_LOW_Y_HIGH=7;
	public static final int  X_HIGH_Y_LOW=8;
	
	public static final int  X_HIGH_Y_HIGH=9;
	private static final boolean PRINT_INCLUDE_LINK = false;
	private static final boolean PRINT_INCLUDE_NODE_LINK = false;
	private static final boolean COLLECT = false;
    private static final boolean RAW_LINKS = true;
    private static final boolean PRINT_FACEHASH = false;

	static Map<MapKey,Face_info> allFaces = new HashMap<MapKey,Face_info>(); 

	
	
	
	
	int xl,yl,zl,denom;
    Key3D type;
    int status;
    Edge_info x_low, y_low, x_high, y_high;
    List<Link_info> links;
    /** These are the links without attempting to join them */
    List<Link_info> rawLinks;
    List<Node_info> nodes;
    Face_info lb,rb,lt,rt;

    /*
     * Function:	make_face
     * action	fill the structure pointed to by face with info.
     */

    public Face_info(Key3D type,int xl,int yl,int zl,int denom)
    {
    	this.type = type;
    	this.xl = xl;
    	this.yl = yl;
    	this.zl = zl;
    	this.denom = denom;
    	this.status = BoxClevA.EMPTY;
    	this.x_low = this.x_high = this.y_low = this.y_high = null;
    	this.lb = this.rb = this.lt = this.rt = null;
    	this.links = null;
    	this.nodes = null;
    	
    	MapKey key = new MapKey(this);
    	if(Boxclev2.FACEHASH)
    	{
    		allFaces.put(key, this);
    		if(PRINT_FACEHASH)
    			System.out.println(key+"\tADD");
    	}
		if(key.xl==2 && key.yl==1 && key.zl ==2 && key.denom == 4 && key.type == Key3D.FACE_FF) {
			System.out.println(key+"\tFOUND");
		}

    }

	/*
	 * Function:	make_sub_faces
	 * action:	creates the four sub faces of a face
	 */

	Face_info[] make_sub_faces()
	{
		Face_info res[] = new Face_info[4];
		switch(this.type)
		{
		case FACE_LL: case FACE_RR:
			res[0] = new Face_info(this.type,
				this.xl*2,this.yl*2,this.zl*2,this.denom*2);
			res[1] = new Face_info(this.type,
				this.xl*2,this.yl*2+1,this.zl*2,this.denom*2);
			res[2] = new Face_info(this.type,
				this.xl*2,this.yl*2,this.zl*2+1,this.denom*2);
			res[3] = new Face_info(this.type,
				this.xl*2,this.yl*2+1,this.zl*2+1,this.denom*2);
			break;
		case FACE_FF: case FACE_BB:
			res[0] = new Face_info(this.type,
				this.xl*2,this.yl*2,this.zl*2,this.denom*2);
			res[1] = new Face_info(this.type,
				this.xl*2+1,this.yl*2,this.zl*2,this.denom*2);
			res[2] = new Face_info(this.type,
				this.xl*2,this.yl*2,this.zl*2+1,this.denom*2);
			res[3] = new Face_info(this.type,
				this.xl*2+1,this.yl*2,this.zl*2+1,this.denom*2);
			break;
		case FACE_DD: case FACE_UU:
			res[0] = new Face_info(this.type,
				this.xl*2,this.yl*2,this.zl*2,this.denom*2);
			res[1] = new Face_info(this.type,
				this.xl*2+1,this.yl*2,this.zl*2,this.denom*2);
			res[2] = new Face_info(this.type,
				this.xl*2,this.yl*2+1,this.zl*2,this.denom*2);
			res[3] = new Face_info(this.type,
				this.xl*2+1,this.yl*2+1,this.zl*2,this.denom*2);
			break;
		default:
			System.err.printf("bad type %d in make_sub_face\n",this.type);
			System.exit(1);
		}
		return res;
	}

    
    /*
     * Function:	make_edge_on_face
     * action:	fill loctaion pointed to by sol with information
     *		about the edge on the face refered to by code.
     */

    Edge_info make_face_edge(int code)
    {
    	switch(type)
    	{
    	case FACE_LL: case FACE_RR:
    		switch(code)
    		{
    		case X_LOW:
    			return new Edge_info(Key3D.Z_AXIS,xl,yl,
    				zl,denom);
    			
    		case X_HIGH:
    			return new Edge_info(Key3D.Z_AXIS,xl,yl+1,
    				zl,denom);
    			
    		case Y_LOW:
    			return new Edge_info(Key3D.Y_AXIS,xl,yl,
    				zl,denom);
    			
    		case Y_HIGH:
    			return new Edge_info(Key3D.Y_AXIS,xl,yl,
    				zl+1,denom);
    			
    		case MID_FACE:
    			return new Edge_info(type,xl,yl,
    				zl,denom);
    			
    		}
    		break;

    	case FACE_FF: case FACE_BB:
    		switch(code)
    		{
    		case X_LOW:
    			return new Edge_info(Key3D.Z_AXIS,xl,yl,
    				zl,denom);
    			
    		case X_HIGH:
    			return new Edge_info(Key3D.Z_AXIS,xl+1,yl,
    				zl,denom);
    			
    		case Y_LOW:
    			return new Edge_info(Key3D.X_AXIS,xl,yl,
    				zl,denom);
    			
    		case Y_HIGH:
    			return new Edge_info(Key3D.X_AXIS,xl,yl,
    				zl+1,denom);
    			
    		case MID_FACE:
    			return new Edge_info(type,xl,yl,
    				zl,denom);
    			
    		}
    		break;

    	case FACE_DD: case FACE_UU:
    		switch(code)
    		{
    		case X_LOW:
    			return new Edge_info(Key3D.Y_AXIS,xl,yl,
    				zl,denom);
    			
    		case X_HIGH:
    			return new Edge_info(Key3D.Y_AXIS,xl+1,yl,
    				zl,denom);
    			
    		case Y_LOW:
    			return new Edge_info(Key3D.X_AXIS,xl,yl,
    				zl,denom);
    			
    		case Y_HIGH:
    			return new Edge_info(Key3D.X_AXIS,xl,yl+1,
    				zl,denom);
    			
    		case MID_FACE:
    			return new Edge_info(type,xl,yl,
    				zl,denom);
    			
    		default:
    			System.err.printf("bad type %d in make_face_edge\n",code);
    			System.exit(1);
    		}
    		break;
    	default:
    		System.err.printf("bad type %d in make_face_edge\n",type);
    		System.exit(1);
    		
    	}
		return null;
    }

    /*
     * Function:	calc_pos_on_face
     * action:	vec is the position of sol on the face
     */

    double[] calc_pos_on_face(Sol_info sol)
    {
    	double[] vec = new double[2];
    	switch(this.type)
    	{
    	case FACE_LL: case FACE_RR:
    		switch(sol.type)
    		{
    		case Y_AXIS:
    			vec[0] = this.denom * (sol.yl + sol.root)/sol.denom
    				- this.yl;
    			vec[1] = this.denom * sol.zl / sol.denom - this.zl;
    			break;
    		case Z_AXIS:
    			vec[0] = this.denom * sol.yl / sol.denom - this.yl;
    			vec[1] = this.denom * (sol.zl + sol.root)/sol.denom
    				- this.zl;
    			break;
    		default:
    			System.err.printf("calc_pos_on_face: bad types face %s",this.type.toString());
    			System.err.printf(" sol %s",sol.type.toString());
    			System.err.printf("\n");
    			break;
    		}
    		break;
    	case FACE_FF: case FACE_BB:
    		switch(sol.type)
    		{
    		case X_AXIS:
    			vec[0] = this.denom * (sol.xl + sol.root)/sol.denom
    				- this.xl;
    			vec[1] = this.denom * sol.zl / sol.denom - this.zl;
    			break;
    		case Z_AXIS:
    			vec[0] = this.denom * sol.xl / sol.denom - this.xl;
    			vec[1] = this.denom * (sol.zl + sol.root)/sol.denom
    				- this.zl;
    			break;
    		default:
    			System.err.printf("calc_pos_on_face: bad types face %s",this.type.toString());
    			System.err.printf(" sol %s",sol.type.toString());
    			System.err.printf("\n");
    			break;
    		}
    		break;
    	case FACE_DD: case FACE_UU:
    		switch(sol.type)
    		{
    		case X_AXIS:
    			vec[0] = this.denom * (sol.xl + sol.root)/sol.denom
    				- this.xl;
    			vec[1] = this.denom * sol.yl / sol.denom - this.yl;
    			break;
    		case Y_AXIS:
    			vec[0] = this.denom * sol.xl / sol.denom - this.xl;
    			vec[1] = this.denom * (sol.yl + sol.root)/sol.denom
    				- this.yl;
    			break;
    		default:
    			System.err.printf("calc_pos_on_face: bad types face %s",this.type.toString());
    			System.err.printf(" sol %s",sol.type.toString());
    			System.err.printf("\n");
    			break;
    		}
    		break;
    	default:
    		System.err.printf("bad type %d in calc_pos_on_face\n",this.type);
    		System.exit(1);
    	}	/* end switch(this.type) */
    	return vec;
    }

    
    /*
     * Function:	include_link
     * action:	given a link between two solutions and a list of existing
     *		links on the face do the following:
     *		if neither sol in list add link to list;
     *		if one sol matches a sol in list extend the existing link;
     *		if link joins two existing links remove one and
     *		join the two together.
     *
     *		basically do the right thing to the list with the given link.
     */


    void include_link(Sol_info sol1, Sol_info sol2)
    {
    	Link_info link1=null, link2=null;
    	boolean link1_keepA = false, link2_keepA = false;

    if(PRINT_INCLUDE_LINK)
    /*
    	if( 512 * this.yl == 264 * this.denom)
    */
    	{
    	System.err.printf("include_link\n");
    	System.err.println(this);
    	System.err.println(sol1);
    	System.err.println(sol2);
    	}
    
    if(RAW_LINKS) {
        if(this.rawLinks==null) 
            this.rawLinks = new ArrayList<Link_info>();
        Link_info link = new Link_info(sol1,sol2,LinkStatus.LINK);
        rawLinks.add(link);
    }
    
    if(this.links==null) 
    	this.links = new ArrayList<Link_info>();
 
    	for(Link_info link:links)
    	{
    		if( sol1 == link.A && sol1.type.compareTo( Key3D.FACE_LL) < 0 )
    		{
    			if(link.B == sol2) return;

    			link1 = link;
    			if( link.B == null )
    			{
    				link.B = sol2;
    				link1_keepA = true;
    			}
    			else
    			{
    				link.A = sol2;
    				link1_keepA = false;
    			}
    		}
    		else if( sol1 == link.B && sol1.type.compareTo( Key3D.FACE_LL) < 0 )
    		{
    			if(link.A == sol2) return;

    			link1 = link;
    			if( link.A == null )
    			{
    				link.A = sol2;
    				link1_keepA = false;
    			}
    			else
    			{
    				link.B = sol2;
    				link1_keepA = true;
    			}
    		}
    		else if( sol2 == link.A && sol2.type.compareTo( Key3D.FACE_LL) < 0 )
    		{
    			link2 = link;
    			if( link.B == null )
    			{
    				link.B = sol1;
    				link2_keepA = true;
    			}
    			else
    			{
    				link.A = sol1;
    				link2_keepA = false;
    			}
    		}
    		else if( sol2 == link.B && sol2.type.compareTo( Key3D.FACE_LL) < 0 )
    		{
    			link2 = link;
    			if( link.A == null )
    			{
    				link.A = sol1;
    				link2_keepA = false;
    			}
    			else
    			{
    				link.B = sol1;
    				link2_keepA = true;
    			}
    		}

    	} /* end while */

    	if( link1 == null && link2 == null )	/* Didn't find link add it */
    	{
    		Link_info link = new Link_info(sol1,sol2,LinkStatus.LINK);
    		this.links.add(link);
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

    			links.remove(link2);
    		}

    		/*** Do nothing if only one end of a simple link. ***/
    	}
    if( PRINT_INCLUDE_LINK) 
    	{
    	System.err.printf("include_link done\n");
    	System.err.println(this);
    	}
    
    }

    void add_node(Node_info node) {
    	if(nodes==null)
    		nodes=new ArrayList<Node_info>();
    	nodes.add(node);
    }
    /*
     * Function:	add_node(face,sol)
     * action:	add the node to the list for the face.
     *		simple version where we don't try to join nodes together.
     */

    void add_node(Sol_info sol)
    {
    	Node_info node;

    /*	System.err.printf("add_node: "); print_Key3D(this.type); 
    	System.err.printf(" (%d,%d,%d)/%d\n",this.xl,this.yl,this.zl,this.denom);
    */
    	if(sol.type.compareTo(Key3D.X_AXIS) < 0 || sol.type.compareTo(Key3D.BOX)>0 )
    	{
    		System.err.printf("add_node: bad Key3D\n"); 
    		System.err.print(sol);
    		System.err.print(this);
    		System.exit(1);
    	}
    	node = new Node_info(sol,LinkStatus.NODE);
    	if(this.nodes==null)
    		this.nodes=new ArrayList<Node_info>();
    		nodes.add(node);

    if(PRINT_INCLUDE_NODE_LINK) 
    	if( sol.xl == 317 && sol.yl == 112 && sol.zl == 345)
    	{
    	System.err.printf("add_node\n");
    	System.err.print(node.sol);
    	System.err.print(this);
    	}
    

    }


    int count_nodes_on_face()
    {
    	int count=0;

    	count = nodes.size();

    	if( this.lb != null )
    	{
    		count += lb.count_nodes_on_face()
    		 + lt.count_nodes_on_face()
    		 + rb.count_nodes_on_face()
    		 + rt.count_nodes_on_face();
    	}
    	return(count);
    }

    /*
     * Function:	get_nth_node_on_face
     * action:	finds the nth node on a face and returns a pointer to it.
     *		If there is no nth node return (nil).
     */

    Node_info get_nth_node_on_face(int n)
    {
    	Node_info temp;
    	if(n<nodes.size())
    		return nodes.get(n);
    	n-=nodes.size();
    	
    	/* Now try sub faces */

    	if( this.lb != null )
    	{
    		temp = lb.get_nth_node_on_face(n);
    		if(temp != null ) return(temp);
    		n -= lb.count_nodes_on_face();
    	}

    	if( this.lt != null )
    	{
    		temp = lt.get_nth_node_on_face(n);
    		if(temp != null ) return(temp);
    		n -= lt.count_nodes_on_face();
    	}

    	if( this.rb != null )
    	{
    		temp = rb.get_nth_node_on_face(n);
    		if(temp != null ) return(temp);
    		n -= rb.count_nodes_on_face();
    	}

    	if( this.rt != null )
    	{
    		temp = rt.get_nth_node_on_face(n);
    		if(temp != null ) return(temp);
    		n -= rt.count_nodes_on_face();
    	}

    	/* couldn't find an nth node */

    	return(null);
    }

    /*
     * Function:    printnodes_on_face(face)
     * action:      prints the nodes lying on the face.
     */

    String print_nodes_on_face()
    {
        StringBuilder sb = new StringBuilder();
            if(nodes != null ) 
            {
            	for(Node_info node:nodes)
            		sb.append(node);
            }
            	
            if(lb != null ) sb.append(lb.print_nodes_on_face());
            if(lb != null ) sb.append(rb.print_nodes_on_face());
            if(lt != null ) sb.append(lt.print_nodes_on_face());
            if(rt != null ) sb.append(rt.print_nodes_on_face());
            
            return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("FACE: type ");
        sb.append(type);
        sb.append(String.format(" (%d,%d,%d)/%d,status %d\n",
                xl,yl,zl,denom,status));
        if(x_low!=null)
        	sb.append(x_low.toString());
        if(x_high!=null)
        sb.append(x_high.toString());
        if(y_low!=null)
        sb.append(y_low.toString());
        if(y_high!=null)
        sb.append(y_high.toString());
        sb.append(this.print_nodes_on_face());
        if(links!=null) {
        	for(Link_info link:links)
        		sb.append(link);
        }
        
        return sb.toString();
    }

    public String print_face_brief()
    {
        StringBuilder sb = new StringBuilder();
            sb.append("FACE: type ");
            sb.append(type);
            sb.append(String.format(" (%d,%d,%d)/%d,status %d\n",
                    xl,yl,zl,denom,status));
            if(links!=null) {
            	for(Link_info link:links)
            		sb.append(link);
            }
            return sb.toString();
    }
    
	/*
	 * Function:	colect_node
	 * action:	collect all the MID_FACE solutions together to make
	 *		a single node, and have all the solutions joined to it.
	 * BUGS:	solution round edge adjicient to node?
	 */

	void colect_nodes()
	{
		if(COLLECT) {
			Sol_info sol;

			Node_info node;
			int num_nodes,num_sols,i,j;

			num_nodes = Topology.count_nodes_on_face(this);

			/*** First if a link is adjacient to a node change the end sol ***/
			/***	to point to the node.				       ***/

			for(i=1;i<=num_nodes;++i)
			{
				node = Topology.get_nth_node_on_face(this,i);

				for(Link_info link:this.links)
				{
					if( node.adjacient_to_node(link.A) )
					{
						/*
					free(link.A);
						 */
						link.A = node.sol;
					}
					if( node.adjacient_to_node(link.B) )
					{
						/*
					free(link.B);
						 */
						link.B = node.sol;
					}
				}

				/*** Now if any edge sol is adjacient to a node create a link ***/
				/*** with the edge sol at one end and the node at the other.  ***/

				num_sols = Topology.count_sols_on_face(this);

				for(i=1;i<=num_nodes;++i)
				{
					node = Topology.get_nth_node_on_face(this,i);

					for(j=1;j<=num_sols;++j)
					{
						sol = Topology.get_nth_sol_on_face(this,j);
						if(node.adjacient_to_node(sol) )
						{
							this.include_link(node.sol,sol);
						}
					}
				}
			}
		}
	}

	public void free_bits_of_face() {
		//if(this.nodes!=null) this.nodes.free(); 
		this.nodes = null;
		//if(this.links!=null) this.links.free(); 
		this.links = null;
		if(this.lb != null )
		{
			this.lb.x_high.free();
			this.lb.y_high.free();
			this.rt.x_low.free();
			this.rt.y_low.free();
			this.lb.free();
			this.rb.free();
			this.lt.free();
			this.rt.free();
		}
		this.lb = this.rb = this.lt = this.rt = null;
}

	private void free() {
		//if(this.nodes!=null) this.nodes.free(); 
		this.nodes = null;
		//if(this.links!=null) this.links.free(); 
		this.links = null;
		if(this.lb != null )
		{
			this.lb.x_high.free();
			this.lb.y_high.free();
			this.rt.x_low.free();
			this.rt.y_low.free();
			this.lb.free();
			this.rb.free();
			this.lt.free();
			this.rt.free();
		}
		this.lb = this.rb = this.lt = this.rt = null;
	}

	static Face_info get(MapKey key) {
		Face_info f =  allFaces.get(key);
		if(PRINT_FACEHASH && f!=null) 
			System.out.println(key+"\tFOUND");
		if(key.xl==2 && key.yl==1 && key.zl ==2 && key.denom == 4 && key.type == Key3D.FACE_FF) {
			System.out.println(key+"\tFOUND");
		}
		return f;
	}
	
	static Face_info remove(MapKey key) {
		return allFaces.remove(key);
	}
	
	static Face_info remove(Face_info face) {
		if(face==null) return null;
		remove(face.lb);
		remove(face.lt);
		remove(face.rb);
		remove(face.rt);
		MapKey mapKey = new MapKey(face);
		if(PRINT_FACEHASH)
			System.out.println(mapKey+"\tREMOVE");
		return allFaces.remove(mapKey);
	}
	
	static void print_keys() {
		for(MapKey key:allFaces.keySet()) {
			System.out.println(key);
		}
	}

}
