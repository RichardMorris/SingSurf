/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

public class Sing_info {
    Sol_info sing;
    LinkStatus status;
    short numNLs=0;
    //List<Node_link_info> adjacentNLs=null;
    Node_link_info[] adjacentNLs=null;

    
    
	public Sing_info(Sol_info sing, LinkStatus status) {
		super();
		this.sing = sing;
		this.status = status;
	}

	/*
	 * Function:	adjacient_to_sing
	 * action:	returns true if sol is adjacient to the sing given
	 *		by the link. Must be strictly adjacient, i.e. touch
	 *		on whole edge.
	 */

	boolean adjacient_to_sing(Sol_info sol)
	{
		if(sol == null ) return(false);

		if(sol.type == Key3D.FACE_LL || sol.type == Key3D.FACE_RR )
		{
		return(sol.xl * this.sing.denom >= this.sing.xl * sol.denom &&
		       sol.xl * this.sing.denom<=(this.sing.xl+1)*sol.denom &&
		       (sol.yl+1)*this.sing.denom > this.sing.yl * sol.denom &&
		       sol.yl * this.sing.denom< (this.sing.yl+1)*sol.denom &&
		       (sol.zl+1)*this.sing.denom > this.sing.zl * sol.denom &&
		       sol.zl * this.sing.denom< (this.sing.zl+1)*sol.denom );
		}

		else if(sol.type == Key3D.FACE_FF || sol.type == Key3D.FACE_BB )
		{
		return((sol.xl+1)*this.sing.denom > this.sing.xl * sol.denom &&
		       sol.xl * this.sing.denom< (this.sing.xl+1)*sol.denom &&
		       sol.yl * this.sing.denom >= this.sing.yl * sol.denom &&
		       sol.yl * this.sing.denom<=(this.sing.yl+1)*sol.denom &&
		       (sol.zl+1)*this.sing.denom > this.sing.zl * sol.denom &&
		       sol.zl * this.sing.denom< (this.sing.zl+1)*sol.denom );
		}

		else if(sol.type == Key3D.FACE_DD || sol.type == Key3D.FACE_UU )
		{
		return((sol.xl+1)*this.sing.denom > this.sing.xl * sol.denom &&
		       sol.xl * this.sing.denom< (this.sing.xl+1)*sol.denom &&
		       (sol.yl+1)*this.sing.denom > this.sing.yl * sol.denom &&
		       sol.yl * this.sing.denom< (this.sing.yl+1)*sol.denom &&
		       sol.zl * this.sing.denom >= this.sing.zl * sol.denom &&
		       sol.zl * this.sing.denom<=(this.sing.zl+1)*sol.denom );
		}
		else return(false);
	}

	
	@Override
	public String toString() {
    	StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("SING: status %hd adjacent node links %d",status,numNLs));
		sb.append(this.sing);
		return sb.toString();
	}
}
