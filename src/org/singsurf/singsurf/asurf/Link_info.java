/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

public class Link_info {
    Sol_info A,B;
    LinkStatus status; 
    boolean plotted;
    
    
    public Link_info(Sol_info a, Sol_info b, LinkStatus status) {
		super();
		A = a;
		B = b;
		this.status = status;
	}

	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        switch(status)
        {
                case NODE:      sb.append(String.format(" NODE plotstatus %d%n",plotted)); break;
                case LINK:      sb.append(String.format(" LINK %b%n",plotted)); break;
        }
        sb.append(A.toString());     
        sb.append(B.toString());     
        sb.append("\n");
        return sb.toString();
    }

}
