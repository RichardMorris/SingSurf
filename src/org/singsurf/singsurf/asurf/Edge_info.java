/*
Created 14 Jun 2010 - Richard Morris
 */
package org.singsurf.singsurf.asurf;

public class Edge_info {
	private static final boolean PRI_SPLIT_EDGE = false;
	int xl,yl,zl,denom;
	Key3D type;
	int status;
	short refcount;
	Sol_info sol;
	Edge_info left,right;

	/*
	 * Function:	make_edge
	 *		define an edge
	 */

	public Edge_info(Key3D type, int xl,int yl,int zl,int denom)
	{
		this.type = type;
		this.xl = xl;
		this.yl = yl;
		this.zl = zl;
		this.denom = denom;
		this.status = BoxClevA.EMPTY;
		this.sol =  null;
		this.left = this.right =  null;
	}

	Edge_info[]  subdevideedge()
	{
		Edge_info edge1=null;
		Edge_info edge2=null;
		
		switch(type)
		{
		case X_AXIS: 
			edge1 = new Edge_info(this.type,this.xl*2,
				this.yl*2,this.zl*2,this.denom*2);
			edge2 = new Edge_info(this.type,this.xl*2 + 1,
				this.yl*2,this.zl*2,this.denom*2);
			break;
		case Y_AXIS: 
			edge1 = new Edge_info(this.type,this.xl*2,
				this.yl*2,this.zl*2,this.denom*2);
			edge2 = new Edge_info(this.type,this.xl*2,
				this.yl*2 + 1,this.zl*2,this.denom*2);
			break;
		case Z_AXIS: 
			edge1 = new Edge_info(this.type,this.xl*2,
				this.yl*2,this.zl*2,this.denom*2);
			edge2 = new Edge_info(this.type,this.xl*2,
				this.yl*2,this.zl*2 + 1,this.denom*2);
			break;
		default:
			System.err.printf("bad type %d in subdevideedge\n",this.type);
			System.exit(1);
		}
		return new Edge_info[]{edge1,edge2};
	}


	/*
	 * Function:	split_edge
	 * action:	ensures that edge comprises of two halves and that
	 *		if a solution exists then it lies in one of the two
	 *		halves.
	 */

	void split_edge()
	{
		if( left == null )
		{
			left = new Edge_info(type,xl*2,yl*2,zl*2,denom*2);
			left.status = status;
		}

		if( right == null )
		{

			switch( type )
			{
			case X_AXIS: 
				right = new Edge_info(type,xl*2 + 1,yl*2,zl*2,denom*2);
				break;
			case Y_AXIS: 
				right = new Edge_info(type,xl*2,yl*2 + 1,zl*2,denom*2);
				break;
			case Z_AXIS: 
				right = new Edge_info(type,xl*2,yl*2,zl*2 + 1,denom*2);
				break;
			default:
				System.err.printf("bad type %d in split_edge\n",type);
				System.exit(1);
			}
			right.status = status;
		}

		if( sol != null )
		{
			if( sol.root > 0.0 && sol.root < 0.5 )
			{
				if( left.sol == null )
				{
					left.sol = sol;
					sol = null;
					left.sol.root *= 2.0;
					left.sol.xl = left.xl;
					left.sol.yl = left.yl;
					left.sol.zl = left.zl;
					left.sol.denom = left.denom;
				}
				else
				{
					if( PRI_SPLIT_EDGE) {
						System.err.printf("split_edge: left.sol != null\n");
						System.err.println(this.toString());
					}
				}
			}
			else if( sol.root > 0.5 && sol.root < 1.0 )
			{
				if( right.sol == null )
				{
					right.sol = sol;
					sol = null;
					right.sol.root *= 2.0;
					right.sol.root -= 1.0;
					right.sol.xl = right.xl;
					right.sol.yl = right.yl;
					right.sol.zl = right.zl;
					right.sol.denom = right.denom;
				}
				else
				{
					if(PRI_SPLIT_EDGE){
						System.err.printf("split_edge: right.sol != null\n");
						System.err.println(this.toString());
					}
				}
			}
			else
			{
				if(PRI_SPLIT_EDGE){
					System.err.printf("split_edge: sol.root = %f\n",
							sol.root);
				}
			}
		}
	}



	/*
	 * Function:    printsols_on_edge(edge)
	 * action:      prints the solutions lying on the edge.
	 */

	public void printsols(StringBuilder sb) {
		if(sol != null ) sb.append(sol.toString());
		if(left != null )
		{
			left.printsols(sb);
		}
		if(right != null )
		{
			right.printsols(sb);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("EDGE: type ");
		sb.append(type);
		sb.append(String.format(" (%d,%d,%d)/%d,status %d\n",
				xl,yl,zl,denom,status));
		printsols(sb);
		return sb.toString();
	}

	public void free() {
		if(this.left!=null)
			this.left.free();
		if(this.right!=null)
			this.right.free();
		this.left=this.right=null;
		this.sol=null;
	}


}
