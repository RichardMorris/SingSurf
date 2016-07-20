/*
Created 25 Nov 2006 - Richard Morris
*/
package org.singsurf.singsurf.acurve;

import java.util.ArrayList;
import java.util.List;

import org.nfunk.jep.function.Binomial;
import org.singsurf.singsurf.acurve.Bern1D.BinBern;
import org.singsurf.singsurf.acurve.Bern1D.NegBern1D;
import org.singsurf.singsurf.acurve.Bern1D.PosBern1D;
import org.singsurf.singsurf.asurf.Face_info;

public class Bern2D {
	private static final boolean NOT_DEF = false;
	private static final Object BERN_NO_SIGN = null;
	public int xord;
	public int yord;
	public double[][] coeffs;
	private Object sign;
	
	public Bern2D(int xord,int yord)
	{
		this.xord = xord;
		this.yord = yord;
		this.coeffs = new double[xord+1][yord+1];
	}

	public final void setCoeff(int i, int j, double coeff) {
		coeffs[i][j]= coeff;
		
	}
	
	public final void addCoeff(int i, int j, double val) {
		coeffs[i][j] += val;
		
	}

	public final double ele2D(int i, int j) {
		return coeffs[i][j];
	}

	/**
	 * Test if all coefficient are strictly the same sign.
	 * @return tree if all positive or all negative false otherwise
	 */
	public short allOneSign() {
		//if(xord==0||yord==0) return (coeffs[0][0]>0?1:-1);
		boolean sign = (coeffs[0][0] < 0);
		for (int i = 0; i <= xord; ++i)
			for (int j = 0; j <= yord; ++j) {
				boolean flag = coeffs[i][j] < 0;
				if ((coeffs[i][j] == 0.0) || ((flag) != sign))
					return (0);
			}
		return (short) (sign?-1:1);
	}

	public Bern2D diffX() throws AsurfException {
		int row,element;
		if(xord==0)
			return zeroBern2D;
		
		Bern2D xderiv = new Bern2D(this.xord-1,this.yord);

		for(row=0;row<=yord;row++)
			for(element=0;element<=xord-1;element++)
				xderiv.coeffs[element][row] = this.xord *(this.coeffs[element+1][row] - this.coeffs[element][row]);
		return xderiv;
	}

	public Bern2D diffY() throws AsurfException {
		int col,element;
		if(yord==0)
			return zeroBern2D;

		Bern2D yderiv = new Bern2D(this.xord,this.yord-1);

		for(col=0;col<=xord;col++)
			for(element=0;element<=yord-1;element++)
				yderiv.coeffs[col][element] = this.yord*(this.coeffs[col][element+1] - this.coeffs[col][element]);
		return yderiv;
	}

	public static class QuadBern {
		public Bern2D lb,lt,rb,rt;
		public QuadBern(int xord,int yord) {
			lb = new Bern2D(xord,yord);
			lt = new Bern2D(xord,yord);
			rb = new Bern2D(xord,yord);
			rt = new Bern2D(xord,yord);
		}
		public QuadBern(Bern2D a, Bern2D b,
				Bern2D c, Bern2D d) {
			lb = a;
			lt = b;
			rb=c;
			rt=d;
		}
		
		public QuadBern quadDiff2Dx() throws AsurfException {
			QuadBern temp = new QuadBern(
			this.lb.diffX(), this.lt.diffX(), this.rb.diffX(), this.rt.diffX());
			return temp;
		}

		public QuadBern quadDiff2Dy() throws AsurfException {
			QuadBern temp = new QuadBern(
			this.lb.diffY(), this.lt.diffY(), this.rb.diffY(), this.rt.diffY());
			return temp;
		}

	}
	
	public QuadBern reduce() throws AsurfException {
		QuadBern res = new QuadBern(this.xord,this.yord);
		
		   double[][] pyramid=new double[xord*2+1][yord*2+1];
		   int col,row;
		   int level;

		   for(col=0;col<=xord;col++)
		      for(row=0;row<=yord;row++)
		         pyramid[2*col][2*row] = this.coeffs[col][row];

		   for(level=1;level<=yord;level++)
		      for(col=0;col<=2*xord;col+=2)
		         for(row=level;row<=2*yord-level;row+=2)
		            pyramid[col][row] = 0.5 * ( pyramid[col][row-1] +
		                pyramid[col][row+1] );

		   for(level=1;level<=xord;level++)
		      for(col=level;col<= 2*xord -level;col+=2)
		         for(row=0;row<=2*yord;row++)
		            pyramid[col][row] = 0.5 * ( pyramid[col-1][row] +
		                pyramid[col+1][row] );

		   for(col=0;col<=xord;col++)
		      for(row=0;row<=yord;row++)
		      {
		         res.lb.coeffs[col][row] = pyramid[col][row];
		         res.lt.coeffs[col][row] = pyramid[col][row+yord];
		         res.rb.coeffs[col][row] = pyramid[col+xord][row];
		         res.rt.coeffs[col][row] = pyramid[col+xord][row+yord];
		      }
		   return res;
	}
	
	public static Bern2D fromPolyCoeffs(double[][] aa,Range2D range) {
		int xord = aa[0].length-1;
		int yord = aa.length-1;
		Bern2D res = new Bern2D(xord,yord);

		double[][][] d = new double[Math.max(xord+1,yord+1)][xord+2][yord+2];
		int col,row;
		int i,j,k;

		for( col = 0 ; col <= xord; col++)  /**** loop thru powers of x ****/
		{
			d[0][col+1][1]=aa[yord][col];

			for( i = 1 ; i <= yord;i++)
			{
				d[i-1][col+1][0] = 0;
				d[i-1][col+1][i+1] = 0;

				for( j = 0 ;j <= i;j++)
				{
					d[i][col+1][j+1]= range.ymax*d[i-1][col+1][j]+
					range.ymin*d[i-1][col+1][j+1]+
					Binomial.binom(i, j)*aa[yord-i][col];
				}
			};

			for( k = 0 ; k<= yord;k++)
			{
				res.coeffs[col][k] = d[yord][col+1][k+1] / Binomial.binom(yord,k);
			}
		};

		for( row = 0 ; row<= yord ;row++)
		{
			d[0][1][row+1] = res.coeffs[xord][row];
			for( i = 1 ; i<= xord;i++)
			{
				d[i-1][0][row+1] = 0;
				d[i-1][i+1][row+1] = 0;

				for( j = 0 ; j<= i;j++)
					d[i][j+1][row+1] = range.xmax * d[i-1][j][row+1] +
					range.xmin * d[i-1][j+1][row+1] +
					Binomial.binom(i, j) * res.coeffs[xord-i][row];
			};

			for( k = 0 ; k<= xord;k++)
				res.coeffs[k][row] = d[xord][k+1][row+1]/Binomial.binom(xord,k);
		}
		return res;
	}
	
	Bern1D bottom() {
		Bern1D res = new Bern1D(xord);
		for(int i=0;i<=xord;++i)
			res.coeff[i] = this.coeffs[i][0];
		return res;
	}

	Bern1D top() {
		Bern1D res = new Bern1D(xord);
		for(int i=0;i<=xord;++i)
			res.coeff[i] = this.coeffs[i][yord];
		return res;
	}
	
	/*
	 * Function:	make_bern1D_of_face(bb,type,aa)
	 * action:	create a 2D bernstein poly for face type of 2D bern-poly bb
	 */

	public Bern1D make_bern1D_of_face(int type) throws AsurfException
	{
		int i;
		Bern1D aa;

		//if( bb == posbern2D ) return(posbern1D);
		//if( bb == negbern2D ) return(negbern1D);

		switch(type)
		{
		case Face_info.X_LOW:
			return left();

		case Face_info.X_HIGH:
			return right();

		case Face_info.Y_LOW:
			return bottom();
			
		case Face_info.Y_HIGH:
			 return top();
			 
		default:
			aa = null;
			System.err.printf("make_bern1D_of_face: Whoopse bad type %d\n",type);
		}
		return(aa);
	}


	Bern1D left() {
		Bern1D res = new Bern1D(yord);
		for(int i=0;i<=yord;++i)
			res.coeff[i] = this.coeffs[0][i];
		return res;
	}

	Bern1D right() {
		Bern1D res = new Bern1D(yord);
		for(int i=0;i<=yord;++i)
			res.coeff[i] = this.coeffs[xord][i];
		return res;
	}

	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append("2D Bernstein ("+xord+","+yord+")\n");
		for(int j=0;j<=yord;++j) {
			sb.append("[");
			sb.append(coeffs[0][j]);
			for(int i=1;i<xord+1;++i)
				sb.append(","+coeffs[i][j]);
			sb.append("]\n");
		}
		return sb.toString();
	}
	
	public static class Tower {
		List<Bern2D> derivs;
		List<String> labels;
		public Tower(Bern2D dx,Bern2D dy) throws AsurfException {
			derivs = new ArrayList<Bern2D>();
			labels = new ArrayList<String>();
			addX(dx,"x");
			addY(dy,"y");
		}
		void addX(Bern2D bb,String label) throws AsurfException {
			if(bb.allOneSign()!=0) return;
			derivs.add(bb);
			labels.add(label);
			if(bb.xord>1) {
				Bern2D dx=bb.diffX();
				addX(dx,label+"x");
			}
			if(bb.yord>1) {
				Bern2D dy=bb.diffY();
				addY(dy,label+"y");
			}
		}
		void addY(Bern2D bb,String label) throws AsurfException {
			if(bb.allOneSign()!=0) return;
			derivs.add(bb);
			labels.add(label);
			if(bb.yord>1) {
				Bern2D dy=bb.diffY();
				addY(dy,label+"y");
			}
		}
		public DerivBits calc(Face2D.FaceSol fs) {
			int i = 0;
			DerivBits db = new DerivBits(derivs.size());
			for (Bern2D bb : derivs) {
				switch (fs.fe) {
				case L:
				case LB:
				case LT:
					db.signs[i] = (int) Math.signum(bb.left().evaluate(fs.y));
					break;
				case R:
				case RB:
				case RT:
					db.signs[i] = (int) Math.signum(bb.right().evaluate(fs.y));
					break;
				case B:
					db.signs[i] = (int) Math.signum(bb.bottom().evaluate(fs.x));
					break;
				case T:
					db.signs[i] = (int) Math.signum(bb.top().evaluate(fs.x));
					break;
				}
				++i;
			}
			return db;
		}
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for(String l:labels)
				sb.append(l+",");
			return sb.toString();
		}
	}
	
	public static class DerivBits {
		int[] signs;
		public DerivBits(int n) {
			signs = new int[n];
		}
		@Override
		public boolean equals(Object obj) {
			DerivBits db = (DerivBits) obj;
			for(int i=0;i<signs.length;++i)
				if(db.signs[i]!=this.signs[i]) return false;
			return true;
		}
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(signs[0]);
			for(int i=1;i<signs.length;++i)
				sb.append(" "+signs[1]);
			return sb.toString();
		}
	}
	

	static Bern2D multiplyBern2D(Bern2D M,Bern2D N)
	{
		Bern2D aa;
		double val;

		if( M== null || M == posBern2D || M == negBern2D ) return(null);
		if( N== null || N == posBern2D || N == negBern2D ) return(null);

		aa = new Bern2D(M.xord+N.xord,M.yord+N.yord);

		for(int i=0;i<=M.xord+N.xord;++i)
			for(int j=0;j<=M.yord+N.yord;++j)
					aa.setCoeff(i,j,0.0);
		for(int i=0;i<=M.xord;++i)
		    for(int j=0;j<=N.xord;++j)
			for(int k=0;k<=M.yord;++k)
			    for(int l=0;l<=N.yord;++l)
				{
					val = 	Binomial.binom(M.xord,i) * Binomial.binom(N.xord,j) * 
					Binomial.binom(M.yord,k) * Binomial.binom(N.yord,l) *
					M.ele2D(i,k) * N.ele2D(j,l);
					aa.addCoeff((i+j),(k+l),val);
	/*
					System.err.printf("(%d+%d,%d+%d) += %f =%f %f\n",i,j,k,l,
						val,ele2D(M,i,k),ele2D(N,j,l));
	*/
				}
		for(int i=0;i<=M.xord+N.xord;++i)
			for(int j=0;j<=M.yord+N.yord;++j)
					aa.setCoeff(i, j, aa.ele2D(i, j) / (
							Binomial.binom(M.xord+N.xord,i)
							* Binomial.binom(M.yord+N.yord,j)));
		return(aa);
	}


	static Bern2D identityBern2D(int m,int n)
	{
		Bern2D aa;
		int i,j;

		aa = new Bern2D(m,n);

		for(i=0;i<=m;++i)
		    for(j=0;j<=n;++j)
			aa.setCoeff(i,j,1.0);
		return(aa);
	}

	static Bern2D addBern2D(Bern2D M,Bern2D N)
	{
		int i,j,xord,yord;
		Bern2D aa,bb,cc,ll,rr;
		
		if( M== null || M == posBern2D || M == negBern2D ) return(null);
		if( N== null || N == posBern2D || N == negBern2D ) return(null);

		xord = M.xord > N.xord ? M.xord : N.xord;
		yord = M.yord > N.yord ? M.yord : N.yord;

		aa = new Bern2D(xord,yord);

		if(M.xord < N.xord)
		{
			if(M.yord < N.yord)
			{
				bb = identityBern2D(N.xord-M.xord,N.yord-M.yord);
				ll = multiplyBern2D(M,bb);
				rr = N;
			}
			else if(M.yord > N.yord)
			{
				bb = identityBern2D(N.xord-M.xord,0);
				cc = identityBern2D(0,M.yord-N.yord);
				ll = multiplyBern2D(M,bb);
				rr = multiplyBern2D(N,cc);
			}
			else
			{
				bb = identityBern2D(N.xord-M.xord,0);
				ll = multiplyBern2D(M,bb);
				rr = N;
			}
		}
		else if(M.xord > N.xord)
		{
			if(M.yord < N.yord)
			{
				bb = identityBern2D(0,N.yord-M.yord);
				cc = identityBern2D(M.xord-N.xord,0);
				ll = multiplyBern2D(M,bb);
				rr = multiplyBern2D(M,bb);
			}
			else if(M.yord > N.yord)
			{
				cc = identityBern2D(M.xord-N.xord,M.yord-N.yord);
				ll = M;
				rr = multiplyBern2D(N,cc);
			}
			else
			{
				cc = identityBern2D(M.xord-N.xord,0);
				ll = M;
				rr = multiplyBern2D(N,cc);
			}
		}
		else
		{
			if(M.yord < N.yord)
			{
				bb = identityBern2D(0,N.yord-M.yord);
				ll = multiplyBern2D(M,bb);
				rr = N;
			}
			else if(M.yord > N.yord)
			{
				cc = identityBern2D(0,M.yord-N.yord);
				ll = M;
				rr = multiplyBern2D(N,cc);
			}
			else
			{
				ll = M;
				rr = N;
			}
		}

		for(i=0;i<=xord;++i)
		    for(j=0;j<=yord;++j)
			aa.setCoeff(i,j, ll.ele2D(i,j) + rr.ele2D(i,j));
		return(aa);
	}

	static Bern2D subtractBern2D(Bern2D M,Bern2D N)
	{
		int i,j,xord,yord;
		Bern2D aa,bb,cc,ll,rr;
		
		if( M== null || M == posBern2D || M == negBern2D ) return(null);
		if( N== null || N == posBern2D || N == negBern2D ) return(null);

		xord = M.xord > N.xord ? M.xord : N.xord;
		yord = M.yord > N.yord ? M.yord : N.yord;

		aa = new Bern2D(xord,yord);
		aa.sign = BERN_NO_SIGN;
		if(M.xord < N.xord)
		{
			if(M.yord < N.yord)
			{
				bb = identityBern2D(N.xord-M.xord,N.yord-M.yord);
				ll = multiplyBern2D(M,bb);
				rr = N;
			}
			else if(M.yord > N.yord)
			{
				bb = identityBern2D(N.xord-M.xord,0);
				cc = identityBern2D(0,M.yord-N.yord);
				ll = multiplyBern2D(M,bb);
				rr = multiplyBern2D(N,cc);
			}
			else
			{
				bb = identityBern2D(N.xord-M.xord,0);
				ll = multiplyBern2D(M,bb);
				rr = N;
			}
		}
		else if(M.xord > N.xord)
		{
			if(M.yord < N.yord)
			{
				bb = identityBern2D(0,N.yord-M.yord);
				cc = identityBern2D(M.xord-N.xord,0);
				ll = multiplyBern2D(M,bb);
				rr = multiplyBern2D(M,bb);
			}
			else if(M.yord > N.yord)
			{
				cc = identityBern2D(M.xord-N.xord,M.yord-N.yord);
				ll = M;
				rr = multiplyBern2D(N,cc);
			}
			else
			{
				cc = identityBern2D(M.xord-N.xord,0);
				ll = M;
				rr = multiplyBern2D(N,cc);
			}
		}
		else
		{
			if(M.yord < N.yord)
			{
				bb = identityBern2D(0,N.yord-M.yord);
				ll = multiplyBern2D(M,bb);
				rr = N;
			}
			else if(M.yord > N.yord)
			{
				cc = identityBern2D(0,M.yord-N.yord);
				ll = M;
				rr = multiplyBern2D(N,cc);
			}
			else
			{
				ll = M;
				rr = N;
			}
		}

		for(i=0;i<=xord;++i)
		    for(j=0;j<=yord;++j)
			aa.setCoeff(i,j, ll.ele2D(i,j) - rr.ele2D(i,j));
		return(aa);
	}

	public static Bern2D symetricDet2D(Bern2D a,Bern2D b,Bern2D c)
	{
	if(NOT_DEF) {
		Bern2D ac,bb,sub;
		
		ac = multiplyBern2D(a,c);
		bb = multiplyBern2D(b,b);
		sub = 	subtractBern2D(ac,bb);
	System.err.print("ac\n");
	System.err.print(ac);
	System.err.print(bb);
	System.err.print(sub);
	}	
		return
			subtractBern2D(
				multiplyBern2D(a,c),
				multiplyBern2D(b,b));
	}



	
	public static class PosBern2D extends Bern2D {

		public PosBern2D() {
			super(0,0);
			coeffs[0][0]=1.0;
		}

		@Override
		public short allOneSign() {
			return 1;
		}

		@Override
		public Bern2D diffX() throws AsurfException {
			throw new AsurfException("Tried to differentiate a posative bern");
		}

		@Override
		public Bern2D diffY() throws AsurfException {
			throw new AsurfException("Tried to differentiate a posative bern");
		}

		@Override
		public Bern1D make_bern1D_of_face(int type) {
			return Bern1D.posBern1D;
		}

		@Override
		public QuadBern reduce() {
			return posQuad;
		}

		@Override
		public String toString() {
			return "Positive Bern 2D\n";
		}

		
		
	}

	public static class NegBern2D extends Bern2D {

		public NegBern2D() {
			super(0,0);
			coeffs[0][0]=1.0;
		}

		@Override
		public short allOneSign() {
			return -1;
		}

		@Override
		public Bern2D diffX() throws AsurfException {
			throw new AsurfException("Tried to differentiate a negative bern");
		}

		@Override
		public Bern2D diffY() throws AsurfException {
			throw new AsurfException("Tried to differentiate a negative bern");
		}
		
		
		@Override
		public Bern1D make_bern1D_of_face(int type) {
			return Bern1D.negBern1D;
		}

		@Override
		public QuadBern reduce() {
			return negQuad;
}
	}
	
		public static class ZeroBern2D extends Bern2D {

			public ZeroBern2D() {
				super(0,0);
				coeffs[0][0]=0.0;
			}

			@Override
			public short allOneSign() {
				return 0;
			}

			@Override
			public Bern2D diffX() throws AsurfException {
				//throw new AsurfException("Tried to differentiate a negative bern");
				return zeroBern2D;
			}

			@Override
			public Bern2D diffY() throws AsurfException {
				//throw new AsurfException("Tried to differentiate a negative bern");
				return zeroBern2D;
			}
			
			
			@Override
			public Bern1D make_bern1D_of_face(int type) throws AsurfException {
				//throw new AsurfException("zeroBern.make_bern1D_of_face");
				return Bern1D.zeroBern1D;
			}

			@Override
			public QuadBern reduce() throws AsurfException {
				throw new AsurfException("zeroBern.reduce");
				//return negQuad;
			}
			
			

		@Override
			public double evalbern2D(double[] vec) {
				return 0.0;
			}

		@Override
		public String toString() {
			return "Zero Bern 2D\n";
		}

	}

	public static final PosBern2D posBern2D = new PosBern2D();
	public static final NegBern2D negBern2D = new NegBern2D();
	public static final ZeroBern2D zeroBern2D = new ZeroBern2D();
	public static final QuadBern negQuad =  new QuadBern(negBern2D,negBern2D,negBern2D,negBern2D); 
	public static final QuadBern posQuad = new QuadBern(posBern2D,posBern2D,posBern2D,posBern2D);

	public double evalbern2D(double vec[])
	{
		int element,level;
		int i,j;
		double workA[] = new double[this.xord*2+1],workB[]=new double[this.yord*2+1];
		double oneminusroot,root;
	
	   if(this == posBern2D )
	   {
		System.err.printf("evalbern2D: tryied to evaluate posbern2D\n");
		return(1.0);
	   }
	   if(this == negBern2D )
	   {
		System.err.printf("evalbern2D: tryied to evaluate negbern2D\n");
		return(-1.0);
	   }
	
		for(i=0;i<=this.xord;++i)
		{
			for(j=0;j<=this.yord;++j)
			      workB[2*j] = ele2D(i,j);
	
			root = vec[1]; oneminusroot = 1.0 - root;
	
			for(level=1;level<=this.yord;level++)
			    for(element=level;element<=2*this.yord-level;element+=2)
			       workB[element] = oneminusroot * workB[element-1] +
			           root * workB[element+1];
	
			workA[i*2] = workB[this.yord];
		}
		root = vec[0]; oneminusroot = 1.0 - root;
	
		for(level=1;level<=this.xord;level++)
		     for(element=level;element<=2*this.xord-level;element+=2)
		        workA[element] = oneminusroot * workA[element-1] +
		            root * workA[element+1];
	
		return(workA[this.xord]);
	}
	

}
