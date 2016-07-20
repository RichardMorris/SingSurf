/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

import java.util.ArrayList;
import java.util.List;

public class Chain_info {
    boolean used;             /* whether this chain has already been used to split a facet */
    List<Sol_info> sols;        /* an array of sols */
    double metric_length;    /* the length of the chain */
    double[] metLens;
    //Chain_info next;        /* pointer to the next chain in the list */
    
    
	public Chain_info() {
		sols = new ArrayList<Sol_info>();
	}

	@Override
	public String toString() {
    	StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("Chain: used %b%n",used));
		for(Sol_info sol:sols)	
			sb.append(sol);
		return sb.toString();
	}
    
//	public String printList() {
//    	StringBuilder sb = new StringBuilder();
//    	Chain_info s=this;
//    	while(s!=null)
//    	{
//    		sb.append(s.toString());
//    		s = s.next;
//    	}
//    	return sb.toString();
//
//	}

	public Sol_info getSol(int i) {
		return sols.get(i);
	}

	public int length() {
		return sols.size();
	}

	public void addSol(Sol_info sol) {
			
			sols.add(sol);
	}

	public void free() {
		this.sols=null;
		this.metLens=null;
		
	}


}
