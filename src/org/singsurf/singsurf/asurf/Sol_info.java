/*
Created 14 Jun 2010 - Richard Morris
*/
package org.singsurf.singsurf.asurf;

public class Sol_info {
	public static final double BAD_EDGE=-0.5;
    int xl,yl,zl,denom;
    double root,root2,root3;
    int dx,dy,dz; 
    short dxx,dxy,dxz,dyy,dyz,dzz;
    Key3D type;
    boolean status=false; 
    boolean is_sing=false;
    int plotindex=-1;

    
    public Sol_info(int xl, int yl, int zl, int denom, double root,
			double root2, double root3) {
		super();
		this.xl = xl;
		this.yl = yl;
		this.zl = zl;
		this.denom = denom;
		this.root = root;
		this.root2 = root2;
		this.root3 = root3;
	}

    public Sol_info(int xl, int yl, int zl, int denom, double root,
			double root2) {
		super();
		this.xl = xl;
		this.yl = yl;
		this.zl = zl;
		this.denom = denom;
		this.root = root;
		this.root2 = root2;
		this.root3 = 0.0;
	}

    public Sol_info(int xl, int yl, int zl, int denom, double root) {
		super();
		this.xl = xl;
		this.yl = yl;
		this.zl = zl;
		this.denom = denom;
		this.root = root;
		this.root2 = 0.0;
		this.root3 = 0.0;
	}

    public Sol_info(Key3D type, int xl, int yl, int zl, int denom, double root) {
    	this.type = type;
		this.xl = xl;
		this.yl = yl;
		this.zl = zl;
		this.denom = denom;
		this.root = root;
		this.root2 = 0.0;
		this.root3 = 0.0;
	}

    public Sol_info(Key3D type, int xl, int yl, int zl, int denom, double root1,double root2) {
    	this.type = type;
		this.xl = xl;
		this.yl = yl;
		this.zl = zl;
		this.denom = denom;
		this.root = root1;
		this.root2 = root2;
		this.root3 = 0.0;
	}

    public Sol_info(Key3D type, int xl, int yl, int zl, int denom, double root1,double root2,double root3) {
    	this.type = type;
		this.xl = xl;
		this.yl = yl;
		this.zl = zl;
		this.denom = denom;
		this.root = root1;
		this.root2 = root2;
		this.root3 = root3;
	}

    /*
     * Function:	calc_pos
     * Action;	calculates position of point relative to the big box
     */

	double[] calc_pos()
    {
    	double vec[]=new double[3];
            switch(this.type)
            {
            case X_AXIS:
                    if( this.root == BAD_EDGE )
                            vec[0] = (this.xl + 0.5)/this.denom;
                    else
                            vec[0] = (this.xl+this.root)/this.denom;
                    vec[1] = ((double) this.yl)/this.denom;
                    vec[2] = ((double) this.zl)/this.denom;
                    break;
            case Y_AXIS:
                    vec[0] = ((double) this.xl)/this.denom;
                    if( this.root == BAD_EDGE )
                            vec[1] = (this.yl + 0.5)/this.denom;
                    else
                            vec[1] = (this.yl+this.root)/this.denom;
                    vec[2] = ((double) this.zl)/this.denom;
                    break;
            case Z_AXIS:
                    vec[0] = ((double) this.xl)/this.denom;
                    vec[1] = ((double) this.yl)/this.denom;
                    if( this.root == BAD_EDGE )
                            vec[2] = (this.zl + 0.5)/this.denom;
                    else
                            vec[2] = (this.zl+this.root)/this.denom;
                    break;
            case FACE_LL:
            case FACE_RR:
                    vec[0] = ((double) this.xl)/this.denom;
                    vec[1] = (this.yl + this.root)/this.denom;
                    vec[2] = (this.zl + this.root2)/this.denom;
                    break;
            case FACE_FF:
            case FACE_BB:
                    vec[0] = (this.xl + this.root)/this.denom;
                    vec[1] = ((double) this.yl)/this.denom;
                    vec[2] = (this.zl + this.root2)/this.denom;
                    break;
            case FACE_DD:
            case FACE_UU:
                    vec[0] = (this.xl + this.root)/this.denom;
                    vec[1] = (this.yl + this.root2)/this.denom;
                    vec[2] = ((double) this.zl)/this.denom;
                    break;
            case BOX:
                    vec[0] = (this.xl + this.root)/this.denom;
                    vec[1] = (this.yl + this.root2)/this.denom;
                    vec[2] = (this.zl + this.root3)/this.denom;
                    break;
            default:
                    vec[0] = ((double) this.xl)/this.denom;
                    vec[1] = ((double) this.yl)/this.denom;
                    vec[2] = ((double) this.zl)/this.denom;
                    break;
            }
            return vec;
    }

    /* get the values for the roots, inverse of calc_pos */

    void calc_relative_pos(double vec[])
    {
    	this.root  = vec[0] * this.denom - this.xl;		
    	this.root2 = vec[1] * this.denom - this.yl;		
    	this.root3 = vec[2] * this.denom - this.zl;
    }

	public String toStringBrief() {
		return this.toStringCore() + "\n";
	}
		public String toStringCore() {
        StringBuilder sb = new StringBuilder();
        sb.append("\tsol ");
        sb.append(type);
        sb.append(String.format(" (%d,%d,%d)/%d ",
                xl,yl,zl,denom));
        switch(type)
        {
        case X_AXIS: case Y_AXIS: case Z_AXIS:
            sb.append(String.format("root %6.3f ",root));
            break;
        case FACE_LL: case FACE_RR: case FACE_FF: case FACE_BB:
        case FACE_DD: case FACE_UU:
            sb.append(String.format("roots %6.3f %6.3f ",root,root2));
            break;
        case BOX:
            sb.append(String.format("roots %6.3f %6.3f %6.3f ",
                    root,root2,root3));
            break;
        default:
            sb.append("Bad type:");
        }
        sb.append(String.format("deriv %2d %2d %2d",
                dx,dy,dz ));
        return sb.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.toStringCore());
        Region_info region = BoxClevA.globalRegion;
        switch(type)
        {
        case X_AXIS:
            sb.append(String.format("%6.3f %6.3f %6.3f\n",
                region.xmin + ( (xl + root)/denom ) * (region.xmax-region.xmin),
                region.ymin + ( ((double) yl)/denom ) * (region.ymax-region.ymin),
                region.zmin + ( ((double) zl)/denom ) * (region.zmax-region.zmin)));
                break;
        case Y_AXIS:
            sb.append(String.format("%6.3f %6.3f %6.3f\n",
                region.xmin + ( ((double) xl)/denom ) * (region.xmax-region.xmin),
                region.ymin + ( (yl + root)/denom ) * (region.ymax-region.ymin),
                region.zmin + ( ((double) zl)/denom ) * (region.zmax-region.zmin)));
                break;
        case Z_AXIS:
            sb.append(String.format("%6.3f %6.3f %6.3f\n",
                region.xmin + ( ((double) xl)/denom ) * (region.xmax-region.xmin),
                region.ymin + ( ((double) yl)/denom ) * (region.ymax-region.ymin),
                region.zmin + ( (zl + root)/denom ) * (region.zmax-region.zmin)));
                break;

        case FACE_RR: case FACE_LL:
            sb.append(String.format("%6.3f %6.3f %6.3f\n",
                region.xmin + ( ((double) xl)/denom ) * (region.xmax-region.xmin),
                region.ymin + ( (yl + root)/denom ) * (region.ymax-region.ymin),
                region.zmin + ( (zl + root2)/denom ) * (region.zmax-region.zmin)));
                break;
        case FACE_FF: case FACE_BB:
            sb.append(String.format("%6.3f %6.3f %6.3f\n",
                region.xmin + ( (xl + root)/denom ) * (region.xmax-region.xmin),
                region.ymin + ( ((double) yl)/denom ) * (region.ymax-region.ymin),
                region.zmin + ( (zl + root2)/denom ) * (region.zmax-region.zmin)));
                break;
        case FACE_UU: case FACE_DD:
            sb.append(String.format("%6.3f %6.3f %6.3f\n",
                region.xmin + ( (xl + root)/denom ) * (region.xmax-region.xmin),
                region.ymin + ( (yl + root2)/denom ) * (region.ymax-region.ymin),
                region.zmin + ( ((double) zl)/denom ) * (region.zmax-region.zmin)));
                break;
        case BOX:
            sb.append(String.format("%6.3f %6.3f %6.3f\n",
                region.xmin + ( (xl + root)/denom ) * (region.xmax-region.xmin),
                region.ymin + ( (yl + root2)/denom ) * (region.ymax-region.ymin),
                region.zmin + ( (zl + root3)/denom ) * (region.zmax-region.zmin)));
                break;
        default:
                break;
        }
        return sb.toString();
    }

	public void calc_pos(double[] vec) {
		double v[] = calc_pos();
		vec[0]=v[0];
		vec[1]=v[1];
		vec[2]=v[2];

	}
    
}