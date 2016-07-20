package org.singsurf.singsurf.asurf;

public class Topology2 {

	public static void get_existing_faces(Box_info box) {

		MapKey key = new MapKey(Key3D.FACE_LL,box.xl,box.yl,box.zl,box.denom);
		box.ll = Face_info.get(key);
		key = new MapKey(Key3D.FACE_LL,box.xl+1,box.yl,box.zl,box.denom);
		box.rr = Face_info.get(key);

		key = new MapKey(Key3D.FACE_FF,box.xl,box.yl,box.zl,box.denom);
		box.ff = Face_info.get(key);

		key = new MapKey(Key3D.FACE_FF,box.xl,box.yl+1,box.zl,box.denom);
		box.bb = Face_info.get(key);

		key = new MapKey(Key3D.FACE_DD,box.xl,box.yl,box.zl,box.denom);
		box.dd = Face_info.get(key);

		key = new MapKey(Key3D.FACE_DD,box.xl,box.yl,box.zl+1,box.denom);
		box.uu = Face_info.get(key);

	}

}
