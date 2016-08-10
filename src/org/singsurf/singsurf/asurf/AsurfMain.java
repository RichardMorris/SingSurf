package org.singsurf.singsurf.asurf;

import jv.geom.PgElementSet;
import jv.geom.PgPointSet;
import jv.geom.PgPolygonSet;

import org.nfunk.jep.function.Binomial;
import org.singsurf.singsurf.acurve.AsurfException;

public class AsurfMain {

    public static void main(String args[]) {
        @SuppressWarnings("unused")
        Binomial bi = new Binomial(); // needed so static initilisation done

//        double aa[][][] = new double[][][] {
//                {{0.1,0.0,1.0},{0.0,0.0,0.0},{-1.0,0.0,0.0}},
//                {{0.0,0.0,0.0},{0.0,0.0,0.0},{0.0,0.0,0.0}},
//                {{-1.0,0.0,0.0},{0.0,0.0,0.0},{0.0,0.0,0.0}}};

//        Region_info reg = new Region_info(-0.94,0.93,-1.13,1.04,-1.12,1.05);
        //Swallowtail
//        double aa[][][] = new double[][][] {
//                {{0.0,0.0,0.0,256.0},{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0},{-27.0,0.0,0.0,0.0}},
//                {{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0},{0.0,144.0,0.0,0.0},{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0}},
//                {{0.0,-0.0,-128.0,0.0},{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0}},
//                {{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0},{-4.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0}},
//                {{0.0,16.0,0.0,0.0},{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0}}};

        //Cayley's cubic
        //endless loop
        //				double aa[][][] = new double[][][] {
        //						{{-1.0,0.0,4.0},{0.0,0.0,0.0},{4.0,0.0,0.0}},
        //						{{0.0,0.0,0.0},{0.0,16.0,0.0},{0.0,0.0,0.0}},
        //						{{4.0,0.0,0.0},{0.0,0.0,0.0},{0.0,0.0,0.0}}};


        //		double aa[][][] = new double[][][] {
        //				{{0.0,0.0},{0.0,0.0}},
        //				{{0.0,0.0},{0.0,1.0}}};

        //		double aa[][][] = new double[][][] {
        //				{{0.0,1.0},{1.0,0.0}}};

        //Cross-cap
        //		double aa[][][] = new double[][][] {
        //				{{0.0,0.0,0.0},{0.0,0.0,1.0}},
        //				{{0.0,0.0,0.0},{0.0,0.0,0.0}},
        //				{{-1.0,0.0,0.0},{0.0,0.0,0.0}}};
        //Kummer
        //		double aa[][][] = new double[][][] {
        //				{{-0.009261000000000408,0.0,0.9282000000000021,0.0,1.7899999999999998},{0.0,0.0,0.0,0.0,0.0},{0.9282000000000021,0.0,-6.940000000000007,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{1.7899999999999998,0.0,0.0,0.0,0.0}},
        //				{{0.0,-0.0,10.520000000000005,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{-10.520000000000005,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0}},
        //				{{0.9282000000000012,0.0,8.840000000000002,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{8.840000000000002,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0}},
        //				{{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0}},
        //				{{-0.840000000000001,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0}}};
//      Region_info reg = new Region_info(-0.94,0.93,-1.13,1.04,-1.12,1.05);

        // Kummer 1.2
//        double aa[][][] = new double[][][] {
//                {{-0.08518400000000037,0.0,2.1472000000000016,0.0,1.56},{0.0,0.0,0.0,0.0,0.0},{2.1472000000000016,0.0,-10.160000000000004,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{1.56,0.0,0.0,0.0,0.0}},
//                {{0.0,-0.0,13.280000000000003,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{-13.280000000000003,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0}},
//                {{2.1472000000000007,0.0,9.760000000000002,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{9.760000000000002,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0}},
//                {{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0}},
//                {{-1.7600000000000002,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0,0.0}}};
//      Region_info reg = new Region_info(-1.54,1.63,-1.53,1.64,-1.52,1.65);


        // Sphere
//        double aa[][][] = new double[][][] {
//                {{-1.0, 0.0, 1.0}, {0.0, 0.0, 0.0}, {1.0, 0.0, 0.0}},
//                {{0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}}, 
//                {{1.0, 0.0, 0.0}, {0.0, 0.0, 0.0}, {0.0, 0.0, 0.0}}};

        // Plane
        //		double aa[][][] = new double[][][] {
        //				{{0.0,1.0},{1.0,0.0}}};

        // Two planes crossing
        //			double aa[][][] = new double[][][] {
        //					{{0.0,0.0,1.0},{0.0,0.0,0.0},{-1.0,0.0,0.0}}};
        
        //Diagonal Surface of Clebsch
//        double aa[][][] = new double[][][] {
//        {{0.0,0.0,0.0,16.0},{0.0,-0.0,-48.0,0.0},{0.0,-48.0,0.0,0.0},{16.0,0.0,0.0,0.0}},
//        {{-72.0,0.0,24.0,0.0},{0.0,0.0,0.0,0.0},{24.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0}},
//        {{-93.53074360871936,0.0,0.0,0.0},{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0}},
//        {{-31.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0}}};
//        Region_info reg = new Region_info(-4.14,4.03,-4.13,4.04,-4.12,4.05);


        double aa[][][] = new double[][][] {
        		{{2.0,2.0,-2.0,-2.0},{0.0,0.0,0.0,0.0},{6.0,2.0,0.0,0.0}},
        		{{0.0,0.0,0.0,0.0},{4.0,-4.0,0.0,0.0},{0.0,0.0,0.0,0.0}},
        		{{-2.0,-6.0,0.0,0.0},{0.0,0.0,0.0,0.0},{0.0,0.0,0.0,0.0}}};
        Region_info reg = new Region_info(1.36,2.03,-2.13,-1.4600000000000004,-1.5199999999999996,-0.8500000000000002);
        
        
        //Region_info reg = new Region_info(-1.14,1.03,-1.13,1.04,-1.12,1.05);

        for(int i=0;i<1;++i) {
            PgElementSet surf = new PgElementSet(3);
            PgPolygonSet curve = new PgPolygonSet(3);
            PgPointSet points = new PgPointSet(3);
//            BoxClevA bc = new Boxclev2(surf,curve,points,2,4,8,16,false);
            BoxClevA bc = new Boxclev2(surf,curve,points,4,16,64,256,false);
            //BoxClevA bc = new Boxclev2(surf,curve,points,8,16,128,512, false);
            //Boxclev bc = new Boxclev(surf,curve,points,16,64,256,1024);
//            Region_info reg = new Region_info(-1.14,1.03,-1.13,1.04,-1.12,1.05);
            //BoxClevA bc = new Boxclev2(surf,curve,points,2,4,8,16, false);
            //BoxClevA bc = new Boxclev2(surf,curve,points,4,16,64,256, false);
            //Boxclev bc = new Boxclev(surf,curve,points,8,16,128,512, false);
//            BoxClevA bc = new Boxclev2(surf,curve,points,16,64,256,1024,false);
            //BOXCLEV 16 64 256 1024 true
            //Boxclev bc = new Boxclev(surf,curve,points,32,256,1024,8192);

            try {
                bc.marmain(aa, reg);
            } catch (AsurfException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }



}
