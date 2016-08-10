/**
 * 
 */
package org.singsurf.singsurf.asurf;
import static org.singsurf.singsurf.asurf.Key3D.FACE_DD;
import static org.singsurf.singsurf.asurf.Key3D.FACE_FF;
import static org.singsurf.singsurf.asurf.Key3D.FACE_LL;

class MapKey {
	int xl,yl,zl,denom;
    Key3D type;

    public MapKey(Key3D type,int xl,int yl,int zl,int denom)
    {
    	switch(type) {
    	case FACE_LL: case FACE_RR:
    		this.type = FACE_LL;
    		break;
    	case FACE_FF: case FACE_BB:
    		this.type = FACE_FF;
    		break;
    	case FACE_DD: case FACE_UU:
    		this.type = FACE_DD;
    		break;
    	default:
        	this.type = type;
    	}
    	this.xl = xl;
    	this.yl = yl;
    	this.zl = zl;
    	this.denom = denom;
    }

    public MapKey(Face_info face)
    {
    	switch(face.type) {
    	case FACE_LL: case FACE_RR:
    		this.type = FACE_LL;
    		break;
    	case FACE_FF: case FACE_BB:
    		this.type = FACE_FF;
    		break;
    	case FACE_DD: case FACE_UU:
    		this.type = FACE_DD;
    		break;
    	default:
        	break;
    	}

    	this.xl = face.xl;
    	this.yl = face.yl;
    	this.zl = face.zl;
    	this.denom = face.denom;
    }

    public MapKey(Box_info box)
    {
    	this.type = box.type;
    	this.xl = box.xl;
    	this.yl = box.yl;
    	this.zl = box.zl;
    	this.denom = box.denom;
    }
    
	@Override
	public int hashCode() {
        int res = 17;
        res = 37 * res + xl;
        res = 37 * res + yl;
        res = 37 * res + zl;
        res = 37 * res + type.hashCode();
        return res;
	}
	
	

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof MapKey) {
			MapKey key = (MapKey) obj;
			return this.type == key.type && this.denom == key.denom && this.xl == key.xl && this.yl == key.yl && this.zl == key.zl;
		}
		return false;
	}

	@Override
	public String toString() {
    	String s = String.format("%s: (%d,%d,%d)/%d ",
        		type.toString(),xl,yl,zl,denom);
		return s;
	}
	
	
}