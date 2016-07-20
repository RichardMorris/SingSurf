package org.singsurf.singsurf.asurf;

public interface Plotter {

    void plot_all_facets(Box_info box);

    void plot_box(Box_info box);

    void initoogl();

    void finioogl();

    void clear();

    void rewindoogl();
}