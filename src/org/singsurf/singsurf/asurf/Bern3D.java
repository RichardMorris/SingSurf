/*
Created 25 Nov 2006 - Richard Morris
*/
package org.singsurf.singsurf.asurf;


import org.singsurf.singsurf.acurve.Bern1D;
import org.singsurf.singsurf.acurve.Bern2D;

public class Bern3D {
    public static final short BERN_NO_SIGN = -2;
	int xord;
	int yord;
	int zord;
	double[] coeff;
	short sign = BERN_NO_SIGN;
	static final Bern3D ZERO_BERN_3D = new Bern3D(0,0,0);
	
	public Bern3D(int xord,int yord,int zord)
	{
		this.xord = xord;
		this.yord = yord;
		this.zord = zord;
		this.coeff = new double[(xord+1)*(yord+1)*(zord+1)];
	}
	
	private final double coeff(int i,int j,int k) {
	    return coeff[(i*(yord+1)+j)*(zord+1)+k];
	}

	private final void setCoeff(int i,int j,int k,double val) {
	    int pos = (i*(yord+1)+j)*(zord+1)+k;
		coeff[pos] = val;
	}

	/**
	 * Test if all coefficient are strictly the same sign.
	 * @return tree if all positive or all negative false otherwise
	 */
	
	public boolean allOneSign() {
	    int i,j,k;

	    if(sign != BERN_NO_SIGN) 
	        return sign != 0;

	    if( coeff[0] < 0)
	    {
	        for(i=0;i<=xord;i++)
	            for(j=0;j<=yord;j++)
	                for(k=0;k<=zord;k++)
	                    if( coeff(i,j,k) >= 0.0)
	                    { sign = 0; 
	                    return(false); }
	        sign = -1;
	        return(true);
	    }
	    else
	    {
	        for(i=0;i<=xord;i++)
	            for(j=0;j<=yord;j++)
	                for(k=0;k<=zord;k++)
	                    if( coeff(i,j,k) <= 0.0)
	                    { sign = 0; 
	                    return(false); }
	        sign = 1;
	        return(true);
	    }
	}
	
	public short signOf() {
	    int i,j,k;

	    if(sign != BERN_NO_SIGN) 
	        return sign;

	    if( coeff[0] < 0)
	    {
	        for(i=0;i<=xord;i++)
	            for(j=0;j<=yord;j++)
	                for(k=0;k<=zord;k++)
	                    if( coeff(i,j,k) >= 0.0)
	                    { sign = 0; 
	                    return(sign); }
	        sign = -1;
	        return (short) sign;
	    }
	    else
	    {
	        for(i=0;i<=xord;i++)
	            for(j=0;j<=yord;j++)
	                for(k=0;k<=zord;k++)
	                    if( coeff(i,j,k) <= 0.0)
	                    { sign = 0; 
	                    return(sign); }
	        sign = 1;
	        return(sign);
	    }
	}


	public Bern3D diffX() {
	    int row,col,stack;
	    Bern3D xderiv;

	    if(xord == 0 )
	    {
	        return ZERO_BERN_3D;
	    }
	    xderiv = new Bern3D(this.xord-1,this.yord,this.zord);
	    for(row=0; row<= xord-1; row++)
	        for(col=0;col<=yord;col++)
	            for(stack=0;stack<=zord;stack++)
	                xderiv.setCoeff(row,col,stack,
	                        xord *
	                        ( coeff((row+1),col,stack)
	                                - coeff(row,col,stack)));
	    return(xderiv);
	}

        public Bern3D diffY() {
            int row,col,stack;
            Bern3D yderiv;

            if(yord == 0 )
            {
                return ZERO_BERN_3D;
            }
            yderiv = new Bern3D(this.xord,this.yord-1,this.zord);
            for(row=0; row<= xord; row++)
                for(col=0;col<=yord-1;col++)
                    for(stack=0;stack<=zord;stack++)
                        yderiv.setCoeff(row,col,stack,
                                yord *
                                ( coeff(row,col+1,stack)
                                        - coeff(row,col,stack)));
            return(yderiv);
        }
	
        public Bern3D diffZ() {
            int row,col,stack;
            Bern3D zderiv;

            if(zord == 0 )
            {
                return ZERO_BERN_3D;
            }
            zderiv = new Bern3D(this.xord,this.yord,this.zord-1);
            for(row=0; row<= xord; row++)
                for(col=0;col<=yord;col++)
                    for(stack=0;stack<=zord-1;stack++)
                        zderiv.setCoeff(row,col,stack,
                                zord *
                                ( coeff(row,col,stack+1)
                                        - coeff(row,col,stack)));
            return(zderiv);
        }


	public static class OctBern {
		Bern3D lfd,lfu,lbd,lbu,rfd,rfu,rbd,rbu;
		public OctBern(int xord,int yord,int zord) {
		    lfd = new Bern3D(xord,yord,zord);
                    lfu = new Bern3D(xord,yord,zord);
                    lbd = new Bern3D(xord,yord,zord);
                    lbu = new Bern3D(xord,yord,zord);
                    rfd = new Bern3D(xord,yord,zord);
                    rfu = new Bern3D(xord,yord,zord);
                    rbd = new Bern3D(xord,yord,zord);
                    rbu = new Bern3D(xord,yord,zord);
		}
		public void free() {
            lfu = null;
            lbd = null;
            lbu = null;
            rfd = null;
            rfu = null;
            rbd = null;
            rbu = null;
		}
	}
	
	static double[][][] pyramid;
        static double workA[],workB[],workC[];

	static int pyrX=-1,pyrY=-1,pyrZ=-1;
	public static void initPyramid(int xord,int yord,int zord) {
	    if(xord*2+1 > pyrX || yord*2+1 > pyrY || zord*2+1 > pyrZ) {
	        pyrX = xord*2+1;
	        pyrY = yord*2+1;
	        pyrZ = zord*2+1;
	        pyramid = new double[pyrX][pyrY][pyrZ];
	        workA = new double[pyrX];
                workB = new double[pyrY];
                workC = new double[pyrZ];
	    }
	}
	
	public OctBern reduce() {
	    initPyramid(this.xord,this.yord,this.zord);
	    OctBern temp = new OctBern(this.xord,this.yord,this.zord);
		
	    for(int row=0;row<=xord;row++)
	        for(int col=0;col<=yord;col++)
	        for(int stack=0; stack<=zord; stack++)
	              pyramid[2*row][2*col][2*stack] = 
	                     coeff(row,col,stack);

	        for(int level=1;level<=xord;level++)
	           for(int row=level;row<= 2*xord -level;row+=2)
	           for(int col=0;col<=2*yord;col+=2)
	           for(int stack=0; stack<=2*zord; stack+=2)
	                 pyramid[row][col][stack] =
	                     0.5*(pyramid[row-1][col][stack] + pyramid[row+1][col][stack]);

	        for(int level=1;level<=yord;level++)
	           for(int row=0;row<=2*xord;++row)
	           for(int col=level;col<=2*yord-level;col+=2)
	           for(int stack=0; stack<=2*zord; stack+=2)
	                 pyramid[row][col][stack] =
	                     0.5*(pyramid[row][col-1][stack] + pyramid[row][col+1][stack]);

	        for(int level=1;level<=zord;level++)
	           for(int row=0;row<=2*xord;++row)
	           for(int col=0;col<=2*yord;++col)
	           for(int stack=level; stack<=2*zord-level; stack+=2)
	                 pyramid[row][col][stack] =
	                     0.5*(pyramid[row][col][stack-1] + pyramid[row][col][stack+1]);

	        for(int row=0;row<=xord;row++)
	        for(int col=0;col<=yord;col++)
	        for(int stack=0; stack<=zord; stack++)
	        {
	             temp.lfd.setCoeff(row,col,stack, pyramid[row][col][stack]);
	             temp.rfd.setCoeff(row,col,stack, pyramid[row+xord][col][stack]);
	             temp.lbd.setCoeff(row,col,stack, pyramid[row][col+yord][stack]);
	             temp.rbd.setCoeff(row,col,stack, pyramid[row+xord][col+yord][stack]);
	             temp.lfu.setCoeff(row,col,stack, pyramid[row][col][stack+zord]);
	             temp.rfu.setCoeff(row,col,stack, pyramid[row+xord][col][stack+zord]);
	             temp.lbu.setCoeff(row,col,stack, pyramid[row][col+yord][stack+zord]);
	             temp.rbu.setCoeff(row,col,stack, pyramid[row+xord][col+yord][stack+zord]);
	        }
	        return temp;
	}
	
	double evalbern3D(double vec[])
	{
	        double oneminusroot,root;

	        for(int i=0;i<=xord;++i)
	        {
	                root = vec[2]; oneminusroot = 1.0 - root;

	                for(int j=0;j<=yord;++j)
	                {
	                        for(int element=0;element<=zord;element++)
	                           workC[2*element] =
	                                coeff(i,j,element);

	                        for(int level=1;level<=zord;level++)
	                           for(int element=level;element<=2*zord-level;element+=2)
	                              workC[element] = oneminusroot * workC[element-1] +
	                                  root * workC[element+1];

	                        workB[j*2] = workC[zord];
	                }

	                root = vec[1]; oneminusroot = 1.0 - root;

	                for(int level=1;level<=yord;level++)
	                    for(int element=level;element<=2*yord-level;element+=2)
	                       workB[element] = oneminusroot * workB[element-1] +
	                           root * workB[element+1];

	                workA[i*2] = workB[yord];
	        }
	        root = vec[0]; oneminusroot = 1.0 - root;

	        for(int level=1;level<=xord;level++)
	             for(int element=level;element<=2*xord-level;element+=2)
	                workA[element] = oneminusroot * workA[element-1] +
	                    root * workA[element+1];

	        return(workA[xord]);
	}

	public Bern3D(double aa[][][],Region_info region)
	{
	   //double c[MAXORDER];
	   Bern1D d;
	   int row,col,stack;

	        /*** first convert polynomials in z ***/

	   this.xord = aa.length-1;
	   this.yord = aa[0].length-1;
	   this.zord = aa[0][0].length-1;
	   this.coeff = new double[(xord+1)*(yord+1)*(zord+1)];

	   double c[] = new double[zord+1];
	   for(row = 0; row <= xord; row++) 
	   for(col = 0; col <= yord; col++)
	   {
	      for( stack = 0; stack <= zord; ++stack) c[stack] = aa[row][col][stack];

	      d = Bern1D.formbernstein1D(c,region.zmin,region.zmax);

	      for( stack = 0; stack <= zord; ++stack)
	                this.setCoeff(row,col,stack, 
	                         d.coeff[stack]);
	   }

	        /*** next polynomials in y ***/

           c = new double[yord+1];
	   for(row = 0; row <= xord; row++)
	   for(stack = 0; stack <= zord; ++stack)
	   {
	      for(col = 0; col <= yord; col++)
	                c[col] = this.coeff(row,col,stack);
	      d = Bern1D.formbernstein1D(c,region.ymin,region.ymax);
	      for(col = 0; col <= yord; col++)
	          this.setCoeff(row,col,stack, 
                          d.coeff[col]);
	   }
	        /*** Finally polynomial in x ***/

           c = new double[xord+1];
	   for(col = 0; col <= yord; col++)
	   for(stack = 0; stack <= zord; ++stack)
	   {
	      for(row = 0; row <= xord; row++)
	                c[row] = this.coeff(row,col,stack);
	      d = Bern1D.formbernstein1D(c,region.xmin,region.xmax);
	      for(row = 0; row <= xord; row++)
                  this.setCoeff(row,col,stack, 
                          d.coeff[row]);
	   }
	}


	@Override
    public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append("3D Bernstein ("+xord+","+yord+","+zord+")\n");
		for(int k=0;k<=zord;++k)
		{
		for(int j=0;j<=yord;++j) {
			sb.append("[");
			sb.append(coeff(0,j,k));
			for(int i=1;i<xord+1;++i)
				sb.append(","+coeff(i,j,k));
			sb.append("]\n");
		}
		sb.append("\n");
		}
		return sb.toString();
	}

	public Bern2D make_bern2D_of_box(Key3D code) {
		
		int i,j;
		Bern2D aa=null;

//		if( bb == posbern3D ) return(posbern2D);
//		if( bb == negbern3D ) return(negbern2D);

		switch(code)
		{
		case FACE_LL:
			aa = new Bern2D(this.yord,this.zord);
			for(i=0;i<=this.yord;++i)
			    for(j=0;j<=this.zord;++j)
				aa.setCoeff(i,j,this.coeff(0,i,j));
		break;
		case FACE_RR:
			aa = new Bern2D(this.yord,this.zord);
			for(i=0;i<=this.yord;++i)
			    for(j=0;j<=this.zord;++j)
				aa.setCoeff(i,j,this.coeff(this.xord,i,j));
		break;
		case FACE_FF:
			aa = new Bern2D(this.xord,this.zord);
			for(i=0;i<=this.xord;++i)
			    for(j=0;j<=this.zord;++j)
				aa.setCoeff(i,j,this.coeff(i,0,j));
		break;
		case FACE_BB:
			aa = new Bern2D(this.xord,this.zord);
			for(i=0;i<=this.xord;++i)
			    for(j=0;j<=this.zord;++j)
				aa.setCoeff(i,j,this.coeff(i,this.yord,j));
		break;
		case FACE_DD:
			aa = new Bern2D(this.xord,this.yord);
			for(i=0;i<=this.xord;++i)
			    for(j=0;j<=this.yord;++j)
				aa.setCoeff(i,j,this.coeff(i,j,0));
		break;
		case FACE_UU:
			aa = new Bern2D(this.xord,this.yord);
			for(i=0;i<=this.xord;++i)
			    for(j=0;j<=this.yord;++j)
				aa.setCoeff(i,j,this.coeff(i,j,this.zord));
		break;
		default:
			System.out.printf("bad type %d in make_bern2d_of_box\n",code);
			System.exit(1);
		}
		return(aa);
	}
}
