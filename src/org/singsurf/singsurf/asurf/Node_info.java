/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

public class Node_info {
    Sol_info sol;
    LinkStatus status;

    
    public Node_info(Sol_info sol, LinkStatus status) {
		super();
		this.sol = sol;
		this.status = status;
	}

    
    /*
     * Function:	adjacient_to_node
     * action:	returns true if sol is adjacient to the node given
     *		by the link. Must be strictly adjacient, i.e. touch
     *		on whole edge.
     */

    boolean adjacient_to_node(Sol_info sol)
    {
    	if(sol == null ) return(false);

    	if( this.sol.type == Key3D.FACE_LL || this.sol.type == Key3D.FACE_RR )
    	{
    	    if( sol.xl * this.sol.denom != sol.denom * this.sol.xl )
    		return(false);

    	    if( sol.type == Key3D.Y_AXIS )
    	    {
    		return((sol.yl+1)*this.sol.denom > this.sol.yl * sol.denom &&
    		       sol.yl * this.sol.denom< (this.sol.yl+1)*sol.denom &&
    		       sol.zl * this.sol.denom >=   this.sol.zl * sol.denom &&
    		       sol.zl * this.sol.denom <= (this.sol.zl+1)*sol.denom );
    	    }
    	    else if( sol.type == Key3D.Z_AXIS )
    	    {
    		return(sol.yl * this.sol.denom >= this.sol.yl * sol.denom &&
    		       sol.yl * this.sol.denom <= (this.sol.yl+1)*sol.denom &&
    		       (sol.zl+1)*this.sol.denom > this.sol.zl * sol.denom &&
    		       sol.zl * this.sol.denom< (this.sol.zl+1)*sol.denom );
    	    }
    	}
    	if( this.sol.type == Key3D.FACE_FF || this.sol.type == Key3D.FACE_BB )
    	{
    	    if( sol.yl * this.sol.denom != sol.denom * this.sol.yl )
    		return(false);

    	    if( sol.type == Key3D.X_AXIS )
    	    {
    		return((sol.xl+1)*this.sol.denom > this.sol.xl * sol.denom &&
    		       sol.xl * this.sol.denom< (this.sol.xl+1)*sol.denom &&
    		       sol.zl * this.sol.denom >=   this.sol.zl * sol.denom &&
    		       sol.zl * this.sol.denom <= (this.sol.zl+1)*sol.denom );
    	    }
    	    else if( sol.type == Key3D.Z_AXIS )
    	    {
    		return(sol.xl * this.sol.denom >= this.sol.xl * sol.denom &&
    		       sol.xl * this.sol.denom <= (this.sol.xl+1)*sol.denom &&
    		       (sol.zl+1)*this.sol.denom > this.sol.zl * sol.denom &&
    		       sol.zl * this.sol.denom< (this.sol.zl+1)*sol.denom );
    	    }
    	}
    	if( this.sol.type == Key3D.FACE_DD || this.sol.type == Key3D.FACE_UU )
    	{
    	    if( sol.zl * this.sol.denom != sol.denom * this.sol.zl )
    		return(false);

    	    if( sol.type == Key3D.X_AXIS )
    	    {
    		return((sol.xl+1)*this.sol.denom > this.sol.xl * sol.denom &&
    		       sol.xl * this.sol.denom< (this.sol.xl+1)*sol.denom &&
    		       sol.yl * this.sol.denom >=   this.sol.yl * sol.denom &&
    		       sol.yl * this.sol.denom <= (this.sol.yl+1)*sol.denom );
    	    }
    	    else if( sol.type == Key3D.Y_AXIS )
    	    {
    		return(sol.xl * this.sol.denom >= this.sol.xl * sol.denom &&
    		       sol.xl * this.sol.denom<=(this.sol.xl+1)*sol.denom &&
    		       (sol.yl+1)*this.sol.denom > this.sol.yl * sol.denom &&
    		       sol.yl * this.sol.denom< (this.sol.yl+1)*sol.denom );
    	    }
    	}
    /*
    	System.err.printf("adjacient_sol: bad types node ");
    	print_Key3D(node.sol.type);
    	System.err.printf(" sol ");
    	print_Key3D(sol.type);
    	System.err.printf("\n");
    */
    	return(false);
    }

	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Node: status %hd ",status));
        sb.append(sol.toString());
        return sb.toString();
    }

}
