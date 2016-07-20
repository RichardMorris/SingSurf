/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

public class Node_link_info {
    Node_info A,B;
    Sing_info singA,singB;
    LinkStatus status;
    
    
    public Node_link_info(Node_info a, Node_info b, LinkStatus status) {
		super();
		A = a;
		B = b;
		this.status = status;
	}

	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

                        sb.append(String.format("NODE LINK: status %hd\n",status));
                        sb.append("\t");
                        sb.append(A.toString());       
                        sb.append("\t");
                        sb.append(B.toString());
                sb.append("\n");
        

    return sb.toString();
    
    }
    
}
