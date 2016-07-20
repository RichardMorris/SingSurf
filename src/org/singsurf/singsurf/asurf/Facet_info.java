/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

import java.util.HashSet;
import java.util.Set;

public class Facet_info {
    Facet_sol sols;
    int     numsing;
    int flag;
    //Facet_info next;
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("FACET: \n");
		Facet_sol s1 = this.sols;
		while(s1!=null) {
			sb.append(s1.sol);
			s1 = s1.next;
		}
		return sb.toString();
	}
    
    Set<Sol_info> getSols() {
        Set<Sol_info> res = new HashSet<Sol_info>();
        Facet_sol s1 = this.sols;
        while(s1!=null) {
            res.add(s1.sol);
            s1 = s1.next;
        }
        return res;
    }
}
